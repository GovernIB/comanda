import { TIME_WINDOW_OPTIONS } from './chartModel';
import {
    DatasourcePoolMetrics,
    DatasourcePoolSample,
    JmsQueueMetrics,
    JmsQueueSample,
    PerformanceMetricsResponse,
    TimeWindowSeconds,
    WorkerPoolMetrics,
    WorkerPoolSample,
    ZoomTimeRange,
} from './types';

export type ParsePerformanceMetricsResult =
    | { ok: true; data: PerformanceMetricsResponse }
    | { ok: false; reason: 'invalidJson' | 'invalidFormat' };

const NICE_BUCKET_SECONDS = [1, 2, 5, 10, 15, 20, 30, 60] as const;

const isPlainObject = (value: unknown): value is Record<string, unknown> =>
    typeof value === 'object' && value !== null && !Array.isArray(value);

const isSampleWithTimestamp = (value: unknown): value is { timestamp: string } =>
    isPlainObject(value) && typeof value.timestamp === 'string';

const hasValidHistory = (value: unknown): boolean =>
    value === undefined || (Array.isArray(value) && value.every(isSampleWithTimestamp));

const isValidDatasource = (value: unknown): value is DatasourcePoolMetrics =>
    isPlainObject(value) &&
    typeof value.poolName === 'string' &&
    typeof value.statisticsEnabled === 'boolean' &&
    hasValidHistory(value.history);

const isValidWorkerPool = (value: unknown): value is WorkerPoolMetrics =>
    isPlainObject(value) &&
    typeof value.poolName === 'string' &&
    hasValidHistory(value.history);

const isValidJmsQueue = (value: unknown): value is JmsQueueMetrics =>
    isPlainObject(value) &&
    typeof value.queueName === 'string' &&
    hasValidHistory(value.history);

/**
 * Validates and normalizes an exported performance metrics JSON payload.
 */
export const parsePerformanceMetricsJson = (rawText: string): ParsePerformanceMetricsResult => {
    let parsed: unknown;
    try {
        parsed = JSON.parse(rawText);
    } catch {
        return { ok: false, reason: 'invalidJson' };
    }

    if (!isPlainObject(parsed)) {
        return { ok: false, reason: 'invalidFormat' };
    }
    if (!Array.isArray(parsed.datasources) || !parsed.datasources.every(isValidDatasource)) {
        return { ok: false, reason: 'invalidFormat' };
    }
    if (
        parsed.workerPools !== undefined &&
        (!Array.isArray(parsed.workerPools) || !parsed.workerPools.every(isValidWorkerPool))
    ) {
        return { ok: false, reason: 'invalidFormat' };
    }
    if (
        parsed.jmsQueues !== undefined &&
        (!Array.isArray(parsed.jmsQueues) || !parsed.jmsQueues.every(isValidJmsQueue))
    ) {
        return { ok: false, reason: 'invalidFormat' };
    }

    const datasources: DatasourcePoolMetrics[] = parsed.datasources.map(ds => ({
        ...ds,
        history: Array.isArray(ds.history) ? ds.history : [],
    }));
    const workerPools: WorkerPoolMetrics[] = Array.isArray(parsed.workerPools)
        ? parsed.workerPools.map(wp => ({
              ...wp,
              history: Array.isArray(wp.history) ? wp.history : [],
          }))
        : [];
    const jmsQueues: JmsQueueMetrics[] = Array.isArray(parsed.jmsQueues)
        ? parsed.jmsQueues.map(q => ({
              ...q,
              history: Array.isArray(q.history) ? q.history : [],
          }))
        : [];

    const data: PerformanceMetricsResponse = {
        collectedAt: typeof parsed.collectedAt === 'string' ? parsed.collectedAt : null,
        samplingIntervalSeconds:
            typeof parsed.samplingIntervalSeconds === 'number' && parsed.samplingIntervalSeconds > 0
                ? parsed.samplingIntervalSeconds
                : 1,
        maxHistoryPoints:
            typeof parsed.maxHistoryPoints === 'number' && parsed.maxHistoryPoints > 0
                ? parsed.maxHistoryPoints
                : 3600,
        windowSeconds:
            typeof parsed.windowSeconds === 'number' && parsed.windowSeconds >= 0
                ? parsed.windowSeconds
                : 300,
        bucketSeconds:
            typeof parsed.bucketSeconds === 'number' && parsed.bucketSeconds > 0
                ? parsed.bucketSeconds
                : 1,
        previewBucketSeconds:
            typeof parsed.previewBucketSeconds === 'number' && parsed.previewBucketSeconds > 0
                ? parsed.previewBucketSeconds
                : null,
        collectionEnabled:
            typeof parsed.collectionEnabled === 'boolean' ? parsed.collectionEnabled : true,
        estimatedMemoryBytes:
            typeof parsed.estimatedMemoryBytes === 'number' &&
            Number.isFinite(parsed.estimatedMemoryBytes) &&
            parsed.estimatedMemoryBytes >= 0
                ? parsed.estimatedMemoryBytes
                : null,
        windowStart: typeof parsed.windowStart === 'string' ? parsed.windowStart : null,
        windowEnd: typeof parsed.windowEnd === 'string' ? parsed.windowEnd : null,
        rangeStart: typeof parsed.rangeStart === 'string' ? parsed.rangeStart : null,
        rangeEnd: typeof parsed.rangeEnd === 'string' ? parsed.rangeEnd : null,
        datasources,
        workerPools,
        jmsQueues,
    };

    return { ok: true, data };
};

const parseTimestampMs = (ts: string | null | undefined): number | null => {
    if (!ts) {
        return null;
    }
    const ms = new Date(ts).getTime();
    return Number.isNaN(ms) ? null : ms;
};

const pad2 = (n: number): string => String(n).padStart(2, '0');

const formatLocalDateTime = (ms: number): string => {
    const d = new Date(ms);
    return (
        `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}` +
        `T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
    );
};

const formatUtcBucketDateTime = (bucketEpochSec: number): string => {
    const d = new Date(bucketEpochSec * 1000);
    return (
        `${d.getUTCFullYear()}-${pad2(d.getUTCMonth() + 1)}-${pad2(d.getUTCDate())}` +
        `T${pad2(d.getUTCHours())}:${pad2(d.getUTCMinutes())}:${pad2(d.getUTCSeconds())}`
    );
};

const toWallClockEpochSeconds = (ms: number): number => {
    const d = new Date(ms);
    return Math.floor(
        Date.UTC(
            d.getFullYear(),
            d.getMonth(),
            d.getDate(),
            d.getHours(),
            d.getMinutes(),
            d.getSeconds()
        ) / 1000
    );
};

interface SpanBounds {
    minTsMs: number | null;
    maxTsMs: number | null;
}

const findSnapshotSpan = (snapshot: PerformanceMetricsResponse): SpanBounds => {
    let minTsMs: number | null = null;
    let maxTsMs: number | null = null;

    const observeSamples = (samples: ReadonlyArray<{ timestamp: string }> | undefined) => {
        if (!samples) {
            return;
        }
        for (const s of samples) {
            const ms = parseTimestampMs(s.timestamp);
            if (ms == null) {
                continue;
            }
            if (minTsMs == null || ms < minTsMs) {
                minTsMs = ms;
            }
            if (maxTsMs == null || ms > maxTsMs) {
                maxTsMs = ms;
            }
        }
    };

    for (const ds of snapshot.datasources) {
        if (ds.statisticsEnabled) {
            observeSamples(ds.history);
        }
    }
    for (const wp of snapshot.workerPools ?? []) {
        observeSamples(wp.history);
    }
    for (const q of snapshot.jmsQueues ?? []) {
        observeSamples(q.history);
    }

    return { minTsMs, maxTsMs };
};

/**
 * Selects the smallest time-window button that covers the full imported snapshot span.
 */
export const chooseInitialWindowForSnapshot = (
    snapshot: PerformanceMetricsResponse
): TimeWindowSeconds => {
    const { minTsMs, maxTsMs } = findSnapshotSpan(snapshot);
    if (minTsMs == null || maxTsMs == null || maxTsMs <= minTsMs) {
        return 300;
    }
    const spanSec = (maxTsMs - minTsMs) / 1000;
    for (const opt of TIME_WINDOW_OPTIONS) {
        if (opt.seconds >= spanSec) {
            return opt.seconds;
        }
    }
    return 3600;
};

interface RangeStats {
    maxSamples: number;
    minTsMs: number | null;
    maxTsMs: number | null;
}

const createRangeStats = (): RangeStats => ({
    maxSamples: 0,
    minTsMs: null,
    maxTsMs: null,
});

interface TimestampedSample<T> {
    sample: T;
    ms: number;
}

const filterAndSortRange = <T extends { timestamp: string }>(
    history: T[],
    startMs: number,
    endMs: number,
    stats: RangeStats
): Array<TimestampedSample<T>> => {
    const out: Array<TimestampedSample<T>> = [];
    for (const sample of history) {
        const ms = parseTimestampMs(sample.timestamp);
        if (ms != null && ms >= startMs && ms <= endMs) {
            out.push({ sample, ms });
        }
    }
    out.sort((a, b) => a.ms - b.ms);
    if (out.length > 0) {
        stats.maxSamples = Math.max(stats.maxSamples, out.length);
        const first = out[0].ms;
        const last = out[out.length - 1].ms;
        if (stats.minTsMs == null || first < stats.minTsMs) {
            stats.minTsMs = first;
        }
        if (stats.maxTsMs == null || last > stats.maxTsMs) {
            stats.maxTsMs = last;
        }
    }
    return out;
};

const computeSpanSeconds = (stats: RangeStats): number => {
    if (stats.minTsMs == null || stats.maxTsMs == null || stats.maxTsMs <= stats.minTsMs) {
        return 0;
    }
    return Math.floor((stats.maxTsMs - stats.minTsMs) / 1000);
};

const computeBucketSeconds = (sampleCount: number, spanSeconds: number, maxPoints: number): number => {
    if (sampleCount <= maxPoints || spanSeconds <= 0) {
        return 1;
    }
    for (const candidate of NICE_BUCKET_SECONDS) {
        if (Math.floor(spanSeconds / candidate) + 1 <= maxPoints) {
            return candidate;
        }
    }
    return Math.max(1, Math.ceil(spanSeconds / maxPoints));
};

const maxNullable = (a: number | null | undefined, b: number | null | undefined): number | null => {
    if (b == null) {
        return a ?? null;
    }
    return a == null ? b : Math.max(a, b);
};

const sumNullable = (a: number | null | undefined, b: number | null | undefined): number | null => {
    if (b == null) {
        return a ?? null;
    }
    return a == null ? b : a + b;
};

const downsampleGeneric = <S extends { timestamp: string }, A>(
    items: Array<TimestampedSample<S>>,
    bucketSeconds: number,
    createAccumulator: (timestamp: string) => A,
    accept: (acc: A, sample: S) => void,
    toDto: (acc: A) => S
): S[] => {
    if (bucketSeconds <= 1 || items.length === 0) {
        return items.map(item => ({ ...item.sample }));
    }

    const buckets = new Map<number, A>();
    for (const { sample, ms } of items) {
        const wallClockSec = toWallClockEpochSeconds(ms);
        const bucketKey = Math.floor(wallClockSec / bucketSeconds);
        let acc = buckets.get(bucketKey);
        if (!acc) {
            acc = createAccumulator(formatUtcBucketDateTime(bucketKey * bucketSeconds));
            buckets.set(bucketKey, acc);
        }
        accept(acc, sample);
    }

    const result: S[] = [];
    buckets.forEach(acc => result.push(toDto(acc)));
    return result;
};

const downsampleDatasources = (
    items: Array<TimestampedSample<DatasourcePoolSample>>,
    bucketSeconds: number,
    sumWaitCount: boolean
): DatasourcePoolSample[] =>
    downsampleGeneric(
        items,
        bucketSeconds,
        timestamp => ({
            timestamp,
            inUseCount: null as number | null,
            activeCount: null as number | null,
            waitCount: null as number | null,
            maxWaitTimeMs: null as number | null,
        }),
        (acc, s) => {
            acc.inUseCount = maxNullable(acc.inUseCount, s.inUseCount);
            acc.activeCount = maxNullable(acc.activeCount, s.activeCount);
            acc.maxWaitTimeMs = maxNullable(acc.maxWaitTimeMs, s.maxWaitTimeMs);
            acc.waitCount = sumWaitCount
                ? sumNullable(acc.waitCount, s.waitCount)
                : maxNullable(acc.waitCount, s.waitCount);
        },
        acc => acc
    );

const downsampleWorkers = (
    items: Array<TimestampedSample<WorkerPoolSample>>,
    bucketSeconds: number
): WorkerPoolSample[] =>
    downsampleGeneric(
        items,
        bucketSeconds,
        timestamp => ({
            timestamp,
            poolSize: null as number | null,
            activeCount: null as number | null,
            queueSize: null as number | null,
            completedTasks: null as number | null,
        }),
        (acc, s) => {
            acc.poolSize = maxNullable(acc.poolSize, s.poolSize);
            acc.activeCount = maxNullable(acc.activeCount, s.activeCount);
            acc.queueSize = maxNullable(acc.queueSize, s.queueSize);
            acc.completedTasks = sumNullable(acc.completedTasks, s.completedTasks);
        },
        acc => acc
    );

const downsampleJmsQueues = (
    items: Array<TimestampedSample<JmsQueueSample>>,
    bucketSeconds: number
): JmsQueueSample[] =>
    downsampleGeneric(
        items,
        bucketSeconds,
        timestamp => ({
            timestamp,
            consumerCount: null as number | null,
            deliveringCount: null as number | null,
            pendingCount: null as number | null,
            completedMessages: null as number | null,
            addedMessages: null as number | null,
        }),
        (acc, s) => {
            acc.consumerCount = maxNullable(acc.consumerCount, s.consumerCount);
            acc.deliveringCount = maxNullable(acc.deliveringCount, s.deliveringCount);
            acc.pendingCount = maxNullable(acc.pendingCount, s.pendingCount);
            acc.completedMessages = sumNullable(acc.completedMessages, s.completedMessages);
            acc.addedMessages = sumNullable(acc.addedMessages, s.addedMessages);
        },
        acc => acc
    );

/**
 * Computes a windowed, zoomed, and peak-preserving downsampled view from an imported snapshot,
 * mirroring backend {@code PerformanceMetricsServiceImpl.getMetrics} behavior.
 */
export const buildImportedMetricsView = (
    snapshot: PerformanceMetricsResponse,
    windowSeconds: TimeWindowSeconds,
    zoomRange: ZoomTimeRange | null,
    maxPoints = 180
): PerformanceMetricsResponse => {
    const { minTsMs, maxTsMs } = findSnapshotSpan(snapshot);
    const windowEndMs =
        maxTsMs ?? parseTimestampMs(snapshot.windowEnd) ?? parseTimestampMs(snapshot.collectedAt);
    if (windowEndMs == null) {
        return {
            ...snapshot,
            windowSeconds,
        };
    }

    const windowStartMs =
        windowSeconds === 3600 && minTsMs != null && minTsMs < windowEndMs - 3_600_000
            ? minTsMs
            : windowEndMs - windowSeconds * 1000;

    let zoomed = false;
    let rangeStartMs = windowStartMs;
    let rangeEndMs = windowEndMs;

    if (zoomRange) {
        const fromMs = parseTimestampMs(zoomRange.from);
        const toMs = parseTimestampMs(zoomRange.to);
        if (fromMs != null && toMs != null && toMs >= fromMs) {
            const candStart = Math.max(windowStartMs, fromMs);
            const candEnd = Math.min(windowEndMs, toMs);
            if (candEnd >= candStart && (candStart > windowStartMs || candEnd < windowEndMs)) {
                rangeStartMs = candStart;
                rangeEndMs = candEnd;
                zoomed = true;
            }
        }
    }

    const rangeStats = createRangeStats();
    const windowStats = createRangeStats();

    const dsRangeLists = snapshot.datasources.map(ds =>
        ds.statisticsEnabled
            ? filterAndSortRange(ds.history ?? [], rangeStartMs, rangeEndMs, rangeStats)
            : []
    );
    const dsWindowLists = zoomed
        ? snapshot.datasources.map(ds =>
              ds.statisticsEnabled
                  ? filterAndSortRange(ds.history ?? [], windowStartMs, windowEndMs, windowStats)
                  : []
          )
        : [];

    const wpList = snapshot.workerPools ?? [];
    const wpRangeLists = wpList.map(wp =>
        filterAndSortRange(wp.history ?? [], rangeStartMs, rangeEndMs, rangeStats)
    );
    const wpWindowLists = zoomed
        ? wpList.map(wp =>
              filterAndSortRange(wp.history ?? [], windowStartMs, windowEndMs, windowStats)
          )
        : [];

    const jmsList = snapshot.jmsQueues ?? [];
    const jmsRangeLists = jmsList.map(q =>
        filterAndSortRange(q.history ?? [], rangeStartMs, rangeEndMs, rangeStats)
    );
    const jmsWindowLists = zoomed
        ? jmsList.map(q =>
              filterAndSortRange(q.history ?? [], windowStartMs, windowEndMs, windowStats)
          )
        : [];

    const computedBucket = computeBucketSeconds(
        rangeStats.maxSamples,
        computeSpanSeconds(rangeStats),
        maxPoints
    );
    const previewBucket = zoomed
        ? computeBucketSeconds(
              windowStats.maxSamples,
              computeSpanSeconds(windowStats),
              maxPoints
          )
        : computedBucket;

    const datasources: DatasourcePoolMetrics[] = snapshot.datasources.map((ds, idx) => {
        if (!ds.statisticsEnabled) {
            return { ...ds, history: [], preview: null };
        }
        const sumWait = ds.waitCountType === 'INTERVAL_DELTA';
        return {
            ...ds,
            history: downsampleDatasources(dsRangeLists[idx], computedBucket, sumWait),
            preview: zoomed ? downsampleDatasources(dsWindowLists[idx], previewBucket, sumWait) : null,
        };
    });

    const workerPools: WorkerPoolMetrics[] = wpList.map((wp, idx) => ({
        ...wp,
        history: downsampleWorkers(wpRangeLists[idx], computedBucket),
        preview: zoomed ? downsampleWorkers(wpWindowLists[idx], previewBucket) : null,
    }));

    const jmsQueues: JmsQueueMetrics[] = jmsList.map((q, idx) => ({
        ...q,
        history: downsampleJmsQueues(jmsRangeLists[idx], computedBucket),
        preview: zoomed ? downsampleJmsQueues(jmsWindowLists[idx], previewBucket) : null,
    }));

    const baseBucket = snapshot.bucketSeconds > 0 ? snapshot.bucketSeconds : 1;

    return {
        ...snapshot,
        collectedAt: snapshot.collectedAt ?? formatLocalDateTime(windowEndMs),
        windowSeconds,
        bucketSeconds: Math.max(baseBucket, computedBucket),
        previewBucketSeconds: Math.max(baseBucket, previewBucket),
        windowStart: formatLocalDateTime(windowStartMs),
        windowEnd: formatLocalDateTime(windowEndMs),
        rangeStart: formatLocalDateTime(rangeStartMs),
        rangeEnd: formatLocalDateTime(rangeEndMs),
        datasources,
        workerPools,
        jmsQueues,
    };
};

/**
 * Extracts a safe download filename from a Content-Disposition header or falls back to a local timestamp.
 */
export const extractExportFilename = (
    contentDisposition: string | null,
    fallbackDate: Date = new Date()
): string => {
    if (contentDisposition) {
        const utf8Match = /filename\*\s*=\s*(?:UTF-8''|utf-8'')([^;\r\n]+)/i.exec(contentDisposition);
        if (utf8Match?.[1]) {
            let decoded = utf8Match[1].trim().replace(/^["']|["']$/g, '');
            try {
                decoded = decodeURIComponent(decoded);
            } catch {
                // Keep raw string if URI decoding fails
            }
            const leaf = decoded.split(/[\\/]/).pop()?.trim();
            if (leaf) {
                return leaf;
            }
        }
        const plainMatch = /filename\s*=\s*"([^"]+)"|filename\s*=\s*([^;\r\n]+)/i.exec(
            contentDisposition
        );
        const rawName = (plainMatch?.[1] ?? plainMatch?.[2] ?? '').trim().replace(/^["']|["']$/g, '');
        const leaf = rawName.split(/[\\/]/).pop()?.trim();
        if (leaf) {
            return leaf;
        }
    }

    const d = fallbackDate;
    const stamp =
        `${d.getFullYear()}${pad2(d.getMonth() + 1)}${pad2(d.getDate())}` +
        `-${pad2(d.getHours())}${pad2(d.getMinutes())}${pad2(d.getSeconds())}`;
    return `performance-metrics-${stamp}.json`;
};
