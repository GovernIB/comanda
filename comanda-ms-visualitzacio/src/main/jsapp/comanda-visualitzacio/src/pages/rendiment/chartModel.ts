import { BarSeriesType, LineSeriesType } from '@mui/x-charts';
import {
    DatasourcePoolMetrics,
    DatasourcePoolSample,
    JmsQueueMetrics,
    JmsQueueSample,
    TimeWindowSeconds,
    WaitCountType,
    WorkerPoolMetrics,
    WorkerPoolSample,
    ZoomTimeRange,
} from './types';

export type DatasourceLayerId =
    | 'inUseCount'
    | 'activeCount'
    | 'waitCount'
    | 'maxWaitTimeMs'
    | 'maxSize'
    | 'minSize';

export type WorkerLayerId =
    | 'workerActiveCount'
    | 'workerQueueSize'
    | 'workerPoolSize'
    | 'workerCompletedTasks'
    | 'workerMaxSize'
    | 'workerCoreSize';

export type JmsLayerId =
    | 'jmsDeliveringCount'
    | 'jmsPendingCount'
    | 'jmsConsumerCount'
    | 'jmsCompletedMessages'
    | 'jmsAddedMessages'
    | 'jmsMaxConsumers';

export type PerformanceLayerId = DatasourceLayerId | WorkerLayerId | JmsLayerId;

export type MetricLayerCategory = 'datasource' | 'worker' | 'jms';

export type AggregationMode = 'aggregated' | 'perPool';

export type WaitCountVariant = WaitCountType | 'MIXED';

export interface TimeWindowOption {
    seconds: TimeWindowSeconds;
    label: string;
}

export const TIME_WINDOW_OPTIONS: readonly TimeWindowOption[] = [
    { seconds: 120, label: '2m' },
    { seconds: 300, label: '5m' },
    { seconds: 900, label: '15m' },
    { seconds: 1800, label: '30m' },
    { seconds: 3600, label: '1h' },
];

export const getAutoRefreshIntervalMs = (windowSeconds: TimeWindowSeconds): number => {
    if (windowSeconds <= 300) {
        return 3_000;
    }
    if (windowSeconds <= 900) {
        return 5_000;
    }
    return 10_000;
};

export interface MetricLayerDefinition<ID extends PerformanceLayerId = PerformanceLayerId> {
    id: ID;
    category: MetricLayerCategory;
    axis: 'count' | 'durationMs';
    plotType: 'area' | 'line' | 'bar' | 'dashedLine' | 'dottedLine';
    aggregation: 'sum' | 'max';
    defaultEnabled: boolean;
}

export const DATASOURCE_LAYERS: readonly MetricLayerDefinition<DatasourceLayerId>[] = [
    {
        id: 'inUseCount',
        category: 'datasource',
        axis: 'count',
        plotType: 'area',
        aggregation: 'sum',
        defaultEnabled: true,
    },
    {
        id: 'waitCount',
        category: 'datasource',
        axis: 'count',
        plotType: 'bar',
        aggregation: 'sum',
        defaultEnabled: true,
    },
    {
        id: 'maxWaitTimeMs',
        category: 'datasource',
        axis: 'durationMs',
        plotType: 'line',
        aggregation: 'max',
        defaultEnabled: false,
    },
    {
        id: 'activeCount',
        category: 'datasource',
        axis: 'count',
        plotType: 'line',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'maxSize',
        category: 'datasource',
        axis: 'count',
        plotType: 'dashedLine',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'minSize',
        category: 'datasource',
        axis: 'count',
        plotType: 'dottedLine',
        aggregation: 'sum',
        defaultEnabled: false,
    },
];

export const WORKER_LAYERS: readonly MetricLayerDefinition<WorkerLayerId>[] = [
    {
        id: 'workerActiveCount',
        category: 'worker',
        axis: 'count',
        plotType: 'area',
        aggregation: 'sum',
        defaultEnabled: true,
    },
    {
        id: 'workerQueueSize',
        category: 'worker',
        axis: 'count',
        plotType: 'bar',
        aggregation: 'sum',
        defaultEnabled: true,
    },
    {
        id: 'workerPoolSize',
        category: 'worker',
        axis: 'count',
        plotType: 'line',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'workerCompletedTasks',
        category: 'worker',
        axis: 'count',
        plotType: 'line',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'workerMaxSize',
        category: 'worker',
        axis: 'count',
        plotType: 'dashedLine',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'workerCoreSize',
        category: 'worker',
        axis: 'count',
        plotType: 'dottedLine',
        aggregation: 'sum',
        defaultEnabled: false,
    },
];

export const JMS_LAYERS: readonly MetricLayerDefinition<JmsLayerId>[] = [
    {
        id: 'jmsDeliveringCount',
        category: 'jms',
        axis: 'count',
        plotType: 'area',
        aggregation: 'sum',
        defaultEnabled: true,
    },
    {
        id: 'jmsPendingCount',
        category: 'jms',
        axis: 'count',
        plotType: 'bar',
        aggregation: 'sum',
        defaultEnabled: true,
    },
    {
        id: 'jmsConsumerCount',
        category: 'jms',
        axis: 'count',
        plotType: 'line',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'jmsCompletedMessages',
        category: 'jms',
        axis: 'count',
        plotType: 'line',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'jmsAddedMessages',
        category: 'jms',
        axis: 'count',
        plotType: 'line',
        aggregation: 'sum',
        defaultEnabled: false,
    },
    {
        id: 'jmsMaxConsumers',
        category: 'jms',
        axis: 'count',
        plotType: 'dashedLine',
        aggregation: 'sum',
        defaultEnabled: false,
    },
];

export const ALL_LAYERS: readonly MetricLayerDefinition[] = [
    ...DATASOURCE_LAYERS,
    ...WORKER_LAYERS,
    ...JMS_LAYERS,
];

export const createDefaultEnabledLayers = (): Set<PerformanceLayerId> =>
    new Set(ALL_LAYERS.filter(l => l.defaultEnabled).map(l => l.id));

export const createDefaultSplitLayers = (): Set<PerformanceLayerId> =>
    new Set<PerformanceLayerId>(['workerQueueSize']);

export type PerformanceChartSeries = BarSeriesType | LineSeriesType;

export type LayerColorMap = Record<PerformanceLayerId, string>;

export type LayerLabelMap = Record<PerformanceLayerId, string>;

export type CategoryPrefixMap = Record<MetricLayerCategory, string>;

export interface ComposableChartModel {
    timestamps: string[];
    xLabels: string[];
    series: PerformanceChartSeries[];
    hasCountAxis: boolean;
    hasDurationAxis: boolean;
    activeCategories: Set<MetricLayerCategory>;
    dashedSeriesIds: string[];
    dottedSeriesIds: string[];
}

export interface PreviewChartModel {
    timestamps: string[];
    xLabels: string[];
    series: PerformanceChartSeries[];
}

export interface PoolKpiSummary {
    inUseCount: number | null;
    activeCount: number | null;
    minSize: number | null;
    maxSize: number | null;
    waitCount: number | null;
    maxWaitTimeMs: number | null;
}

export interface WorkerKpiSummary {
    activeCount: number | null;
    poolSize: number | null;
    coreSize: number | null;
    maxSize: number | null;
    queueSize: number | null;
    queueCapacity: number | null;
    hasUnboundedQueue: boolean;
    completedTasks: number | null;
}

export interface JmsKpiSummary {
    deliveringCount: number | null;
    pendingCount: number | null;
    consumerCount: number | null;
    minConsumers: number | null;
    maxConsumers: number | null;
    completedMessages: number | null;
    addedMessages: number | null;
}

const PER_POOL_ACCENTS = [
    '#1565c0',
    '#00838f',
    '#6a1b9a',
    '#2e7d32',
    '#ef6c00',
    '#c62828',
    '#283593',
    '#4e342e',
];

export const WORKER_QUEUE_PALETTE: readonly string[] = [
    '#f4511e',
    '#1e88e5',
    '#8e24aa',
    '#00897b',
    '#d81b60',
    '#fb8c00',
    '#3949ab',
    '#43a047',
    '#6d4c41',
    '#00acc1',
];

export const buildEntityColorMap = (names: string[]): Record<string, string> => {
    const map: Record<string, string> = {};
    names.forEach((name, idx) => {
        map[name] = WORKER_QUEUE_PALETTE[idx % WORKER_QUEUE_PALETTE.length];
    });
    return map;
};

export const buildWorkerColorMap = (workerPools: WorkerPoolMetrics[]): Record<string, string> =>
    buildEntityColorMap(workerPools.map(wp => wp.poolName));

const TIME_SHORT_FORMATTER = new Intl.DateTimeFormat('ca-ES', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
});

export const formatTimeShort = (ts: string): string => {
    const d = new Date(ts);
    if (Number.isNaN(d.getTime())) {
        return ts;
    }
    return TIME_SHORT_FORMATTER.format(d);
};

export const formatBucketResolution = (bucketSeconds: number): string => {
    if (bucketSeconds >= 60 && bucketSeconds % 60 === 0) {
        return `${bucketSeconds / 60} min`;
    }
    return `${bucketSeconds} s`;
};

export const resolveWaitCountVariant = (datasources: DatasourcePoolMetrics[]): WaitCountVariant => {
    const enabled = datasources.filter(ds => ds.statisticsEnabled);
    const types = new Set(
        enabled
            .map(ds => ds.waitCountType)
            .filter((t): t is WaitCountType => t === 'INTERVAL_DELTA' || t === 'INSTANTANEOUS')
    );
    if (types.size === 1) {
        return Array.from(types)[0];
    }
    if (types.size > 1) {
        return 'MIXED';
    }
    return 'INTERVAL_DELTA';
};

const sumNullable = (values: Array<number | null | undefined>): number | null => {
    let sum = 0;
    let count = 0;
    for (const v of values) {
        if (v != null && !Number.isNaN(v)) {
            sum += v;
            count += 1;
        }
    }
    return count > 0 ? sum : null;
};

const maxNullable = (values: Array<number | null | undefined>): number | null => {
    let max: number | null = null;
    for (const v of values) {
        if (v != null && !Number.isNaN(v)) {
            max = max == null ? v : Math.max(max, v);
        }
    }
    return max;
};

export const computeKpiSummary = (datasources: DatasourcePoolMetrics[]): PoolKpiSummary => {
    const enabled = datasources.filter(ds => ds.statisticsEnabled);
    return {
        inUseCount: sumNullable(enabled.map(ds => ds.inUseCount)),
        activeCount: sumNullable(enabled.map(ds => ds.activeCount)),
        minSize: sumNullable(enabled.map(ds => ds.minSize)),
        maxSize: sumNullable(enabled.map(ds => ds.maxSize)),
        waitCount: sumNullable(enabled.map(ds => ds.waitCount)),
        maxWaitTimeMs: maxNullable(enabled.map(ds => ds.maxWaitTimeMs)),
    };
};

export const computeWorkerKpiSummary = (workerPools: WorkerPoolMetrics[]): WorkerKpiSummary => ({
    activeCount: sumNullable(workerPools.map(wp => wp.activeCount)),
    poolSize: sumNullable(workerPools.map(wp => wp.poolSize)),
    coreSize: sumNullable(workerPools.map(wp => wp.coreSize)),
    maxSize: sumNullable(workerPools.map(wp => wp.maxSize)),
    queueSize: sumNullable(workerPools.map(wp => wp.queueSize)),
    queueCapacity: sumNullable(workerPools.map(wp => wp.queueCapacity)),
    hasUnboundedQueue: workerPools.some(wp => wp.queueCapacity == null),
    completedTasks: sumNullable(workerPools.map(wp => wp.completedTasks)),
});

export const computeJmsKpiSummary = (jmsQueues: JmsQueueMetrics[]): JmsKpiSummary => ({
    deliveringCount: sumNullable(jmsQueues.map(q => q.deliveringCount)),
    pendingCount: sumNullable(jmsQueues.map(q => q.pendingCount)),
    consumerCount: sumNullable(jmsQueues.map(q => q.consumerCount)),
    minConsumers: sumNullable(jmsQueues.map(q => q.minConsumers)),
    maxConsumers: sumNullable(jmsQueues.map(q => q.maxConsumers)),
    completedMessages: sumNullable(jmsQueues.map(q => q.completedMessages)),
    addedMessages: sumNullable(jmsQueues.map(q => q.addedMessages)),
});

const extractDatasourceMetric = (
    sample: DatasourcePoolSample | undefined,
    ds: DatasourcePoolMetrics,
    layerId: DatasourceLayerId
): number | null => {
    if (!sample) {
        return null;
    }
    switch (layerId) {
        case 'minSize':
            return ds.minSize ?? null;
        case 'maxSize':
            return ds.maxSize ?? null;
        case 'inUseCount':
            return sample.inUseCount ?? null;
        case 'activeCount':
            return sample.activeCount ?? null;
        case 'waitCount':
            return sample.waitCount ?? null;
        case 'maxWaitTimeMs':
            return sample.maxWaitTimeMs ?? null;
    }
};

const extractWorkerMetric = (
    sample: WorkerPoolSample | undefined,
    wp: WorkerPoolMetrics,
    layerId: WorkerLayerId
): number | null => {
    if (!sample) {
        return null;
    }
    switch (layerId) {
        case 'workerCoreSize':
            return wp.coreSize ?? null;
        case 'workerMaxSize':
            return wp.maxSize ?? null;
        case 'workerPoolSize':
            return sample.poolSize ?? null;
        case 'workerActiveCount':
            return sample.activeCount ?? null;
        case 'workerQueueSize':
            return sample.queueSize ?? null;
        case 'workerCompletedTasks':
            return sample.completedTasks ?? null;
    }
};

const extractJmsMetric = (
    sample: JmsQueueSample | undefined,
    q: JmsQueueMetrics,
    layerId: JmsLayerId
): number | null => {
    if (!sample) {
        return null;
    }
    switch (layerId) {
        case 'jmsDeliveringCount':
            return sample.deliveringCount ?? null;
        case 'jmsPendingCount':
            return sample.pendingCount ?? null;
        case 'jmsConsumerCount':
            return sample.consumerCount ?? null;
        case 'jmsCompletedMessages':
            return sample.completedMessages ?? null;
        case 'jmsAddedMessages':
            return sample.addedMessages ?? null;
        case 'jmsMaxConsumers':
            return q.maxConsumers ?? null;
    }
};

const buildSeriesForLayer = (
    id: string,
    label: string,
    layer: MetricLayerDefinition,
    data: Array<number | null>,
    color: string,
    showMarks: boolean,
    connectNulls: boolean,
    stack?: string
): PerformanceChartSeries => {
    const isDuration = layer.axis === 'durationMs';
    const valueFormatter = (v: number | null) => {
        if (v == null) {
            return '';
        }
        return isDuration ? `${v} ms` : `${v}`;
    };

    if (layer.plotType === 'bar') {
        return {
            id,
            type: 'bar',
            label,
            data,
            color,
            yAxisId: layer.axis,
            valueFormatter,
            ...(stack ? { stack } : {}),
        };
    }

    return {
        id,
        type: 'line',
        label,
        data,
        color,
        yAxisId: layer.axis,
        area: layer.plotType === 'area',
        showMark: layer.plotType === 'dashedLine' || layer.plotType === 'dottedLine' ? false : showMarks,
        connectNulls,
        valueFormatter,
    };
};

interface CategorySeriesParams<Item, Sample extends { timestamp: string }, LayerId extends PerformanceLayerId> {
    prefix: string;
    categoryPrefix?: string;
    items: Item[];
    getName: (item: Item) => string;
    sampleMaps: Array<Map<string, Sample>>;
    activeLayers: readonly MetricLayerDefinition<LayerId>[];
    extractor: (sample: Sample | undefined, item: Item, layerId: LayerId) => number | null;
    timestamps: string[];
    effectiveMode: AggregationMode;
    labels: LayerLabelMap;
    colors: LayerColorMap;
    showMarks: boolean;
    series: PerformanceChartSeries[];
    dashedSeriesIds: string[];
    dottedSeriesIds: string[];
    splitLayerIds?: ReadonlySet<PerformanceLayerId>;
    excludedEntitiesByLayer?: Partial<Record<PerformanceLayerId, ReadonlySet<string>>>;
    itemColorMap?: Record<string, string>;
    emittedLayers: MetricLayerDefinition[];
}

const appendCategorySeries = <Item, Sample extends { timestamp: string }, LayerId extends PerformanceLayerId>({
    prefix,
    categoryPrefix,
    items,
    getName,
    sampleMaps,
    activeLayers,
    extractor,
    timestamps,
    effectiveMode,
    labels,
    colors,
    showMarks,
    series,
    dashedSeriesIds,
    dottedSeriesIds,
    splitLayerIds,
    excludedEntitiesByLayer,
    itemColorMap,
    emittedLayers,
}: CategorySeriesParams<Item, Sample, LayerId>): void => {
    if (items.length === 0 || activeLayers.length === 0) {
        return;
    }

    activeLayers.forEach((layer, layerIdx) => {
        const excluded = excludedEntitiesByLayer?.[layer.id];
        const activeIndices: number[] = [];
        for (let i = 0; i < items.length; i++) {
            if (!excluded?.has(getName(items[i]))) {
                activeIndices.push(i);
            }
        }
        if (activeIndices.length === 0) {
            return;
        }

        const isSplitLayer =
            effectiveMode === 'perPool' ||
            (Boolean(splitLayerIds?.has(layer.id)) && items.length > 1);

        emittedLayers.push(layer);

        if (isSplitLayer) {
            for (const itemIdx of activeIndices) {
                const item = items[itemIdx];
                const sampleMap = sampleMaps[itemIdx];
                const itemName = getName(item);
                const data = timestamps.map(ts =>
                    extractor(sampleMap.get(ts), item, layer.id)
                );
                const seriesId = `${prefix}-${itemIdx}-${layer.id}`;
                const useMapColor =
                    itemColorMap?.[itemName] != null &&
                    (effectiveMode !== 'perPool' || layer.id === 'workerQueueSize');
                const color = useMapColor
                    ? itemColorMap![itemName]
                    : effectiveMode === 'perPool'
                        ? items.length === 1 && activeLayers.length === 1
                            ? colors[layer.id]
                            : PER_POOL_ACCENTS[(itemIdx * activeLayers.length + layerIdx) % PER_POOL_ACCENTS.length]
                        : WORKER_QUEUE_PALETTE[itemIdx % WORKER_QUEUE_PALETTE.length];
                const label = `${itemName} · ${labels[layer.id]}`;
                const stack = layer.plotType === 'bar' && activeIndices.length > 1 ? layer.id : undefined;
                series.push(
                    buildSeriesForLayer(seriesId, label, layer, data, color, showMarks, true, stack)
                );
                if (layer.plotType === 'dashedLine') {
                    dashedSeriesIds.push(seriesId);
                } else if (layer.plotType === 'dottedLine') {
                    dottedSeriesIds.push(seriesId);
                }
            }
        } else {
            const data = timestamps.map(ts => {
                const values = activeIndices.map(idx =>
                    extractor(sampleMaps[idx].get(ts), items[idx], layer.id)
                );
                return layer.aggregation === 'max' ? maxNullable(values) : sumNullable(values);
            });
            const seriesId = layer.id;
            const aggregatedLabel = categoryPrefix
                ? `${categoryPrefix} · ${labels[layer.id]}`
                : labels[layer.id];
            series.push(
                buildSeriesForLayer(seriesId, aggregatedLabel, layer, data, colors[layer.id], showMarks, false)
            );
            if (layer.plotType === 'dashedLine') {
                dashedSeriesIds.push(seriesId);
            } else if (layer.plotType === 'dottedLine') {
                dottedSeriesIds.push(seriesId);
            }
        }
    });
};

const toSampleMaps = <Item, Sample extends { timestamp: string }>(
    items: Item[],
    getSamples: (item: Item) => Sample[],
    timestampSet: Set<string>
): Array<Map<string, Sample>> =>
    items.map(item => {
        const map = new Map<string, Sample>();
        for (const s of getSamples(item)) {
            map.set(s.timestamp, s);
            timestampSet.add(s.timestamp);
        }
        return map;
    });

export interface BuildChartModelParams {
    datasources: DatasourcePoolMetrics[];
    workerPools?: WorkerPoolMetrics[];
    jmsQueues?: JmsQueueMetrics[];
    enabledLayers: Set<PerformanceLayerId>;
    aggregationMode?: AggregationMode;
    labels: LayerLabelMap;
    categoryPrefixes?: CategoryPrefixMap;
    colors: LayerColorMap;
    splitLayers?: ReadonlySet<PerformanceLayerId>;
    splitWorkerQueues?: boolean;
    excludedEntitiesByLayer?: Partial<Record<PerformanceLayerId, ReadonlySet<string>>>;
    entityColorMaps?: Partial<Record<MetricLayerCategory, Record<string, string>>>;
    workerColorMap?: Record<string, string>;
}

export const buildComposableChartModel = ({
    datasources,
    workerPools = [],
    jmsQueues = [],
    enabledLayers,
    aggregationMode = 'aggregated',
    labels,
    categoryPrefixes,
    colors,
    splitLayers,
    splitWorkerQueues = false,
    excludedEntitiesByLayer,
    entityColorMaps,
    workerColorMap,
}: BuildChartModelParams): ComposableChartModel => {
    const activeDatasources = datasources.filter(ds => ds.statisticsEnabled);
    const activeWorkers = workerPools;
    const activeJms = jmsQueues;

    const activeDsLayers = activeDatasources.length > 0
        ? DATASOURCE_LAYERS.filter(l => enabledLayers.has(l.id))
        : [];
    const activeWorkerLayers = activeWorkers.length > 0
        ? WORKER_LAYERS.filter(l => enabledLayers.has(l.id))
        : [];
    const activeJmsLayers = activeJms.length > 0
        ? JMS_LAYERS.filter(l => enabledLayers.has(l.id))
        : [];

    const allActiveLayers: MetricLayerDefinition[] = [
        ...activeDsLayers,
        ...activeWorkerLayers,
        ...activeJmsLayers,
    ];

    if (allActiveLayers.length === 0) {
        return {
            timestamps: [],
            xLabels: [],
            series: [],
            hasCountAxis: false,
            hasDurationAxis: false,
            activeCategories: new Set<MetricLayerCategory>(),
            dashedSeriesIds: [],
            dottedSeriesIds: [],
        };
    }

    const timestampSet = new Set<string>();
    const dsMaps = toSampleMaps(activeDatasources, ds => ds.history ?? [], timestampSet);
    const wpMaps = toSampleMaps(activeWorkers, wp => wp.history ?? [], timestampSet);
    const jmsMaps = toSampleMaps(activeJms, q => q.history ?? [], timestampSet);

    const timestamps = Array.from(timestampSet).sort((a, b) => (a < b ? -1 : a > b ? 1 : 0));
    const xLabels = timestamps.map(formatTimeShort);
    const showMarks = timestamps.length <= 25;

    const series: PerformanceChartSeries[] = [];
    const dashedSeriesIds: string[] = [];
    const dottedSeriesIds: string[] = [];
    const emittedLayers: MetricLayerDefinition[] = [];

    const totalEntities = activeDatasources.length + activeWorkers.length + activeJms.length;
    const effectiveMode: AggregationMode =
        aggregationMode === 'perPool' && totalEntities > 1 ? 'perPool' : 'aggregated';

    const effectiveSplitLayers = new Set<PerformanceLayerId>(splitLayers ?? []);
    if (splitWorkerQueues) {
        effectiveSplitLayers.add('workerQueueSize');
    }

    appendCategorySeries({
        prefix: 'ds',
        categoryPrefix: categoryPrefixes?.datasource,
        items: activeDatasources,
        getName: ds => ds.poolName,
        sampleMaps: dsMaps,
        activeLayers: activeDsLayers,
        extractor: extractDatasourceMetric,
        timestamps,
        effectiveMode,
        labels,
        colors,
        showMarks,
        series,
        dashedSeriesIds,
        dottedSeriesIds,
        splitLayerIds: effectiveSplitLayers,
        excludedEntitiesByLayer,
        itemColorMap: entityColorMaps?.datasource,
        emittedLayers,
    });

    appendCategorySeries({
        prefix: 'wp',
        categoryPrefix: categoryPrefixes?.worker,
        items: activeWorkers,
        getName: wp => wp.poolName,
        sampleMaps: wpMaps,
        activeLayers: activeWorkerLayers,
        extractor: extractWorkerMetric,
        timestamps,
        effectiveMode,
        labels,
        colors,
        showMarks,
        series,
        dashedSeriesIds,
        dottedSeriesIds,
        splitLayerIds: effectiveSplitLayers,
        excludedEntitiesByLayer,
        itemColorMap: entityColorMaps?.worker ?? workerColorMap,
        emittedLayers,
    });

    appendCategorySeries({
        prefix: 'jms',
        categoryPrefix: categoryPrefixes?.jms,
        items: activeJms,
        getName: q => q.queueName,
        sampleMaps: jmsMaps,
        activeLayers: activeJmsLayers,
        extractor: extractJmsMetric,
        timestamps,
        effectiveMode,
        labels,
        colors,
        showMarks,
        series,
        dashedSeriesIds,
        dottedSeriesIds,
        splitLayerIds: effectiveSplitLayers,
        excludedEntitiesByLayer,
        itemColorMap: entityColorMaps?.jms,
        emittedLayers,
    });

    const activeCategories = new Set<MetricLayerCategory>();
    for (const layer of emittedLayers) {
        if (layer.axis === 'count') {
            activeCategories.add(layer.category);
        }
    }

    const hasCountAxis = emittedLayers.some(l => l.axis === 'count');
    const hasDurationAxis = emittedLayers.some(l => l.axis === 'durationMs');

    return {
        timestamps,
        xLabels,
        series,
        hasCountAxis,
        hasDurationAxis,
        activeCategories,
        dashedSeriesIds,
        dottedSeriesIds,
    };
};

export interface PreviewColorMap {
    inUseCount: string;
    waitCount: string;
    workerActiveCount?: string;
    workerQueueSize?: string;
    jmsDeliveringCount?: string;
    jmsPendingCount?: string;
}

export const buildPreviewChartModel = (
    datasources: DatasourcePoolMetrics[],
    colors: PreviewColorMap,
    workerPools: WorkerPoolMetrics[] = [],
    jmsQueues: JmsQueueMetrics[] = []
): PreviewChartModel => {
    const activeDatasources = datasources.filter(ds => ds.statisticsEnabled);
    if (activeDatasources.length === 0 && workerPools.length === 0 && jmsQueues.length === 0) {
        return { timestamps: [], xLabels: [], series: [] };
    }

    const timestampSet = new Set<string>();
    const dsMaps = toSampleMaps(
        activeDatasources,
        ds => (ds.preview && ds.preview.length > 0 ? ds.preview : (ds.history ?? [])),
        timestampSet
    );
    const wpMaps = toSampleMaps(
        workerPools,
        wp => (wp.preview && wp.preview.length > 0 ? wp.preview : (wp.history ?? [])),
        timestampSet
    );
    const jmsMaps = toSampleMaps(
        jmsQueues,
        q => (q.preview && q.preview.length > 0 ? q.preview : (q.history ?? [])),
        timestampSet
    );

    const timestamps = Array.from(timestampSet).sort((a, b) => (a < b ? -1 : a > b ? 1 : 0));
    if (timestamps.length === 0) {
        return { timestamps: [], xLabels: [], series: [] };
    }

    const series: PerformanceChartSeries[] = [];

    if (activeDatasources.length > 0) {
        series.push(
            {
                id: 'preview-inUse',
                type: 'line',
                data: timestamps.map(ts => sumNullable(dsMaps.map(m => m.get(ts)?.inUseCount))),
                color: colors.inUseCount,
                area: true,
                showMark: false,
                connectNulls: true,
                disableHighlight: true,
            },
            {
                id: 'preview-wait',
                type: 'bar',
                data: timestamps.map(ts => sumNullable(dsMaps.map(m => m.get(ts)?.waitCount))),
                color: colors.waitCount,
            }
        );
    }

    if (workerPools.length > 0) {
        series.push(
            {
                id: 'preview-workerActive',
                type: 'line',
                data: timestamps.map(ts => sumNullable(wpMaps.map(m => m.get(ts)?.activeCount))),
                color: colors.workerActiveCount ?? '#2e7d32',
                area: true,
                showMark: false,
                connectNulls: true,
                disableHighlight: true,
            },
            {
                id: 'preview-workerQueue',
                type: 'bar',
                data: timestamps.map(ts => sumNullable(wpMaps.map(m => m.get(ts)?.queueSize))),
                color: colors.workerQueueSize ?? '#f4511e',
            }
        );
    }

    if (jmsQueues.length > 0) {
        series.push(
            {
                id: 'preview-jmsDelivering',
                type: 'line',
                data: timestamps.map(ts => sumNullable(jmsMaps.map(m => m.get(ts)?.deliveringCount))),
                color: colors.jmsDeliveringCount ?? '#00897b',
                area: true,
                showMark: false,
                connectNulls: true,
                disableHighlight: true,
            },
            {
                id: 'preview-jmsPending',
                type: 'bar',
                data: timestamps.map(ts => sumNullable(jmsMaps.map(m => m.get(ts)?.pendingCount))),
                color: colors.jmsPendingCount ?? '#ff8f00',
            }
        );
    }

    return {
        timestamps,
        xLabels: timestamps.map(formatTimeShort),
        series,
    };
};

export interface TimeDomain {
    startMs: number;
    endMs: number;
    bucketSeconds: number;
}

export interface TimeDomainOptions {
    bucketSeconds?: number | null;
    windowStart?: string | null;
    windowEnd?: string | null;
    fallbackEnd?: string | null;
}

export const parseTimestampMs = (ts: string | null | undefined): number | null => {
    if (!ts) {
        return null;
    }
    const ms = new Date(ts).getTime();
    return Number.isNaN(ms) ? null : ms;
};

const pad2 = (n: number): string => String(n).padStart(2, '0');

export const formatLocalDateTime = (ms: number): string => {
    const d = new Date(ms);
    return (
        `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}` +
        `T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
    );
};

export const resolveTimeDomain = (
    timestamps: string[],
    options?: TimeDomainOptions
): TimeDomain | null => {
    if (timestamps.length < 2) {
        return null;
    }
    const firstMs = parseTimestampMs(timestamps[0]);
    const lastMs = parseTimestampMs(timestamps[timestamps.length - 1]);
    if (firstMs == null || lastMs == null) {
        return null;
    }

    const bucketSeconds =
        options?.bucketSeconds != null &&
        Number.isFinite(options.bucketSeconds) &&
        options.bucketSeconds > 1
            ? Math.floor(options.bucketSeconds)
            : 1;
    const bucketMs = bucketSeconds * 1000;

    let startMs = firstMs;
    if (bucketSeconds > 1) {
        const wsMs = parseTimestampMs(options?.windowStart);
        if (wsMs != null && wsMs > firstMs && wsMs < firstMs + bucketMs) {
            startMs = wsMs;
        }
    }

    let endMs = lastMs;
    if (bucketSeconds > 1) {
        const boundEndMs =
            parseTimestampMs(options?.windowEnd) ?? parseTimestampMs(options?.fallbackEnd);
        if (boundEndMs != null) {
            if (boundEndMs >= lastMs && boundEndMs <= lastMs + bucketMs) {
                endMs = boundEndMs;
            } else if (boundEndMs > lastMs + bucketMs) {
                endMs = lastMs + bucketMs;
            } else {
                endMs = lastMs;
            }
        } else {
            endMs = lastMs + bucketMs;
        }
    }

    if (endMs <= startMs) {
        return null;
    }

    return { startMs, endMs, bucketSeconds };
};

export const getBucketCenterMs = (tsMs: number, domain: TimeDomain): number => {
    if (domain.bucketSeconds <= 1) {
        return Math.max(domain.startMs, Math.min(domain.endMs, tsMs));
    }
    const bStart = Math.max(domain.startMs, tsMs);
    const bEnd = Math.min(domain.endMs, tsMs + domain.bucketSeconds * 1000);
    return bEnd > bStart ? (bStart + bEnd) / 2 : bStart;
};

export const pctToTimestamp = (
    timestamps: string[],
    pct: number,
    domain?: TimeDomain | null
): string => {
    const clampedPct = Math.max(0, Math.min(100, pct));
    const effectiveDomain = domain ?? resolveTimeDomain(timestamps);
    if (effectiveDomain) {
        const spanMs = effectiveDomain.endMs - effectiveDomain.startMs;
        const minSec = Math.floor(effectiveDomain.startMs / 1000);
        const maxSec = Math.ceil(effectiveDomain.endMs / 1000);
        const targetSec = Math.max(
            minSec,
            Math.min(
                maxSec,
                Math.round((effectiveDomain.startMs + (clampedPct / 100) * spanMs) / 1000)
            )
        );
        return formatLocalDateTime(targetSec * 1000);
    }
    if (timestamps.length === 0) {
        return '';
    }
    const idx = Math.min(
        timestamps.length - 1,
        Math.max(0, Math.round((clampedPct / 100) * (timestamps.length - 1)))
    );
    return timestamps[idx] ?? '';
};

export const pctRangeToTimeRange = (
    timestamps: string[],
    rawStartPct: number,
    rawEndPct: number,
    domain?: TimeDomain | null
): ZoomTimeRange | null => {
    if (timestamps.length < 2) {
        return null;
    }
    const startPct = Math.max(0, Math.min(100, Math.min(rawStartPct, rawEndPct)));
    const endPct = Math.max(0, Math.min(100, Math.max(rawStartPct, rawEndPct)));

    if (startPct <= 0.25 && endPct >= 99.75) {
        return null;
    }

    const effectiveDomain = domain ?? resolveTimeDomain(timestamps);
    if (effectiveDomain) {
        const spanMs = effectiveDomain.endMs - effectiveDomain.startMs;
        const minSec = Math.floor(effectiveDomain.startMs / 1000);
        const maxSec = Math.ceil(effectiveDomain.endMs / 1000);
        let startSec = Math.round((effectiveDomain.startMs + (startPct / 100) * spanMs) / 1000);
        let endSec = Math.round((effectiveDomain.startMs + (endPct / 100) * spanMs) / 1000);
        startSec = Math.max(minSec, Math.min(maxSec, startSec));
        endSec = Math.max(minSec, Math.min(maxSec, endSec));

        if (startSec >= endSec) {
            if (endSec < maxSec) {
                endSec = startSec + 1;
            } else {
                startSec = Math.max(minSec, endSec - 1);
            }
        }

        if (startSec <= minSec && endSec >= maxSec) {
            return null;
        }

        return {
            from: formatLocalDateTime(startSec * 1000),
            to: formatLocalDateTime(endSec * 1000),
        };
    }

    if (startPct <= 0.5 && endPct >= 99.5) {
        return null;
    }

    const maxIdx = timestamps.length - 1;
    let startIdx = Math.round((startPct / 100) * maxIdx);
    let endIdx = Math.round((endPct / 100) * maxIdx);

    if (startIdx >= endIdx) {
        if (endIdx < maxIdx) {
            endIdx = startIdx + 1;
        } else {
            startIdx = Math.max(0, endIdx - 1);
        }
    }

    if (startIdx === 0 && endIdx === maxIdx) {
        return null;
    }

    return {
        from: timestamps[startIdx],
        to: timestamps[endIdx],
    };
};

const findNearestTimestampIndex = (timestamps: string[], target: string): number => {
    const targetMs = new Date(target).getTime();
    if (Number.isNaN(targetMs)) {
        const idx = timestamps.findIndex(ts => ts >= target);
        return idx === -1 ? timestamps.length - 1 : idx;
    }
    let bestIdx = 0;
    let bestDiff = Number.POSITIVE_INFINITY;
    for (let i = 0; i < timestamps.length; i++) {
        const ms = new Date(timestamps[i]).getTime();
        const diff = Number.isNaN(ms) ? Number.POSITIVE_INFINITY : Math.abs(ms - targetMs);
        if (diff < bestDiff) {
            bestDiff = diff;
            bestIdx = i;
        }
    }
    return bestIdx;
};

export const timeRangeToPctRange = (
    timestamps: string[],
    zoomRange: ZoomTimeRange | null,
    domain?: TimeDomain | null
): [number, number] => {
    if (!zoomRange || timestamps.length < 2) {
        return [0, 100];
    }

    const effectiveDomain = domain ?? resolveTimeDomain(timestamps);
    const fromMs = parseTimestampMs(zoomRange.from);
    const toMs = parseTimestampMs(zoomRange.to);

    if (effectiveDomain && fromMs != null && toMs != null) {
        const spanMs = effectiveDomain.endMs - effectiveDomain.startMs;
        if (spanMs > 0) {
            const rawStart = ((fromMs - effectiveDomain.startMs) / spanMs) * 100;
            const rawEnd = ((toMs - effectiveDomain.startMs) / spanMs) * 100;
            const startPct = Math.max(0, Math.min(99.9, Number(rawStart.toFixed(2))));
            const endPct = Math.max(
                Math.min(100, Number((startPct + 0.1).toFixed(2))),
                Math.min(100, Number(rawEnd.toFixed(2)))
            );
            return [startPct, endPct];
        }
    }

    const maxIdx = timestamps.length - 1;
    const startIdx = findNearestTimestampIndex(timestamps, zoomRange.from);
    let endIdx = findNearestTimestampIndex(timestamps, zoomRange.to);
    if (endIdx <= startIdx) {
        endIdx = Math.min(maxIdx, startIdx + 1);
    }
    const startPct = (startIdx / maxIdx) * 100;
    const endPct = (endIdx / maxIdx) * 100;
    return [
        Math.max(0, Math.min(99, Number(startPct.toFixed(1)))),
        Math.max(1, Math.min(100, Number(endPct.toFixed(1)))),
    ];
};

export const panPctRange = (
    startPct: number,
    endPct: number,
    deltaPct: number
): [number, number] => {
    const clampedStart = Math.max(0, Math.min(100, Math.min(startPct, endPct)));
    const clampedEnd = Math.max(0, Math.min(100, Math.max(startPct, endPct)));
    const span = clampedEnd - clampedStart;
    if (span >= 100) {
        return [0, 100];
    }
    const nextStart = Math.max(0, Math.min(100 - span, clampedStart + deltaPct));
    const nextEnd = nextStart + span;
    return [Number(nextStart.toFixed(2)), Number(nextEnd.toFixed(2))];
};

export const centerPctRange = (
    startPct: number,
    endPct: number,
    centerPct: number
): [number, number] => {
    const clampedStart = Math.max(0, Math.min(100, Math.min(startPct, endPct)));
    const clampedEnd = Math.max(0, Math.min(100, Math.max(startPct, endPct)));
    const span = clampedEnd - clampedStart;
    if (span >= 100) {
        return [0, 100];
    }
    const nextStart = Math.max(0, Math.min(100 - span, centerPct - span / 2));
    const nextEnd = nextStart + span;
    return [Number(nextStart.toFixed(2)), Number(nextEnd.toFixed(2))];
};
