import { describe, expect, it } from 'vitest';
import {
    buildImportedMetricsView,
    chooseInitialWindowForSnapshot,
    extractExportFilename,
    parsePerformanceMetricsJson,
} from './historySnapshot';
import { PerformanceMetricsResponse } from './types';

const pad2 = (n: number): string => String(n).padStart(2, '0');

const formatTs = (epochMs: number): string => {
    const d = new Date(epochMs);
    return (
        `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}` +
        `T${pad2(d.getHours())}:${pad2(d.getMinutes())}:${pad2(d.getSeconds())}`
    );
};

describe('historySnapshot', () => {
    it('parsePerformanceMetricsJson valida fitxer exportat complet, JSON invalid i format incorrecte', () => {
        const validPayload: PerformanceMetricsResponse = {
            collectedAt: '2026-11-24T10:00:00',
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 120,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    minSize: 5,
                    maxSize: 50,
                    history: [
                        {
                            timestamp: '2026-11-24T09:59:59',
                            inUseCount: 3,
                            activeCount: 10,
                            waitCount: 1,
                            maxWaitTimeMs: 12,
                        },
                    ],
                },
            ],
            workerPools: [
                {
                    poolName: 'salutWorkerExecutor',
                    poolType: 'EXECUTOR',
                    history: [
                        {
                            timestamp: '2026-11-24T09:59:59',
                            poolSize: 5,
                            activeCount: 2,
                            queueSize: 0,
                            completedTasks: 4,
                        },
                    ],
                },
            ],
            jmsQueues: [
                {
                    queueName: 'tasquesQueue',
                    history: [
                        {
                            timestamp: '2026-11-24T09:59:59',
                            consumerCount: 5,
                            deliveringCount: 1,
                            pendingCount: 2,
                            completedMessages: 6,
                            addedMessages: 7,
                        },
                    ],
                },
            ],
        };

        const okResult = parsePerformanceMetricsJson(JSON.stringify(validPayload));
        expect(okResult.ok).toBe(true);
        if (okResult.ok) {
            expect(okResult.data.datasources).toHaveLength(1);
            expect(okResult.data.workerPools).toHaveLength(1);
            expect(okResult.data.jmsQueues).toHaveLength(1);
        }

        expect(parsePerformanceMetricsJson('{not-json')).toEqual({
            ok: false,
            reason: 'invalidJson',
        });

        expect(parsePerformanceMetricsJson('{}')).toEqual({
            ok: false,
            reason: 'invalidFormat',
        });
        expect(parsePerformanceMetricsJson('[]')).toEqual({
            ok: false,
            reason: 'invalidFormat',
        });
        expect(
            parsePerformanceMetricsJson(JSON.stringify({ datasources: [{ statisticsEnabled: true }] }))
        ).toEqual({
            ok: false,
            reason: 'invalidFormat',
        });
        expect(
            parsePerformanceMetricsJson(
                JSON.stringify({
                    datasources: [{ poolName: 'db', statisticsEnabled: true, history: [{}] }],
                })
            )
        ).toEqual({
            ok: false,
            reason: 'invalidFormat',
        });
    });

    it('chooseInitialWindowForSnapshot escull la finestra minima que cobreix la durada del fitxer', () => {
        const makeSnapshot = (spanSec: number): PerformanceMetricsResponse => {
            const t0 = new Date(2026, 10, 24, 9, 0, 0).getTime();
            return {
                samplingIntervalSeconds: 1,
                maxHistoryPoints: 3600,
                windowSeconds: spanSec,
                bucketSeconds: 1,
                datasources: [
                    {
                        poolName: 'comandaDB',
                        statisticsEnabled: true,
                        history: [
                            { timestamp: formatTs(t0), inUseCount: 1 },
                            { timestamp: formatTs(t0 + spanSec * 1000), inUseCount: 2 },
                        ],
                    },
                ],
            };
        };

        expect(chooseInitialWindowForSnapshot(makeSnapshot(60))).toBe(120);
        expect(chooseInitialWindowForSnapshot(makeSnapshot(240))).toBe(300);
        expect(chooseInitialWindowForSnapshot(makeSnapshot(600))).toBe(900);
        expect(chooseInitialWindowForSnapshot(makeSnapshot(1500))).toBe(1800);
        expect(chooseInitialWindowForSnapshot(makeSnapshot(3000))).toBe(3600);
    });

    it('buildImportedMetricsView agrega preservant pics en 1h i augmenta a 1s amb preview en fer zoom', () => {
        const t0 = new Date(2026, 10, 24, 10, 0, 0).getTime();
        const dsDeltaHistory = [];
        const dsInstHistory = [];
        const workerHistory = [];
        const jmsHistory = [];

        for (let i = 0; i < 3600; i++) {
            const ts = formatTs(t0 + i * 1000);
            const isSpike = i === 1205 || i === 1206;
            dsDeltaHistory.push({
                timestamp: ts,
                inUseCount: isSpike ? 48 : 2,
                activeCount: 15,
                waitCount: i === 1205 ? 10 : i === 1206 ? 5 : 0,
                maxWaitTimeMs: i >= 1205 ? 95 : 10,
            });
            dsInstHistory.push({
                timestamp: ts,
                inUseCount: 1,
                activeCount: 2,
                waitCount: i === 1205 ? 6 : i === 1206 ? 2 : 0,
                maxWaitTimeMs: 12,
            });
            workerHistory.push({
                timestamp: ts,
                poolSize: 5,
                activeCount: isSpike ? 4 : 1,
                queueSize: isSpike ? 20 : 0,
                completedTasks: 4,
            });
            jmsHistory.push({
                timestamp: ts,
                consumerCount: 5,
                deliveringCount: 1,
                pendingCount: isSpike ? 10 : 0,
                completedMessages: 2,
                addedMessages: 3,
            });
        }

        const snapshot: PerformanceMetricsResponse = {
            collectedAt: formatTs(t0 + 3599 * 1000),
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 3599,
            bucketSeconds: 1,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    history: dsDeltaHistory,
                },
                {
                    poolName: 'HikariPool',
                    statisticsEnabled: true,
                    waitCountType: 'INSTANTANEOUS',
                    history: dsInstHistory,
                },
            ],
            workerPools: [
                {
                    poolName: 'salutWorkerExecutor',
                    poolType: 'EXECUTOR',
                    history: workerHistory,
                },
            ],
            jmsQueues: [
                {
                    queueName: 'tasquesQueue',
                    history: jmsHistory,
                },
            ],
        };

        // 1h full view -> bucketSeconds = 20, preserves spike MAX & INTERVAL_DELTA SUM, preview null
        const fullHour = buildImportedMetricsView(snapshot, 3600, null, 180);
        expect(fullHour.bucketSeconds).toBe(20);
        expect(fullHour.datasources[0].preview).toBeNull();
        expect(fullHour.datasources[0].history.length).toBeGreaterThanOrEqual(179);
        expect(fullHour.datasources[0].history.length).toBeLessThanOrEqual(181);

        const spikeTs = formatTs(t0 + 1200 * 1000);
        const deltaSpikeBucket = fullHour.datasources[0].history.find(s => s.timestamp === spikeTs);
        expect(deltaSpikeBucket?.inUseCount).toBe(48);
        expect(deltaSpikeBucket?.waitCount).toBe(15);

        const instSpikeBucket = fullHour.datasources[1].history.find(s => s.timestamp === spikeTs);
        expect(instSpikeBucket?.waitCount).toBe(6);

        const workerSpikeBucket = fullHour.workerPools?.[0].history.find(s => s.timestamp === spikeTs);
        expect(workerSpikeBucket?.queueSize).toBe(20);
        expect(workerSpikeBucket?.completedTasks).toBe(80);

        const jmsSpikeBucket = fullHour.jmsQueues?.[0].history.find(s => s.timestamp === spikeTs);
        expect(jmsSpikeBucket?.completedMessages).toBe(40);
        expect(jmsSpikeBucket?.addedMessages).toBe(60);

        // Zoom to [120s] -> bucketSeconds = 1, raw 1s samples returned, preview keeps full ~180pt resolution
        const zoomed = buildImportedMetricsView(
            snapshot,
            3600,
            {
                from: formatTs(t0 + 1140 * 1000),
                to: formatTs(t0 + 1260 * 1000),
            },
            180
        );
        expect(zoomed.bucketSeconds).toBe(1);
        expect(zoomed.previewBucketSeconds).toBe(20);
        expect(zoomed.datasources[0].history).toHaveLength(121);
        expect(zoomed.datasources[0].history[65].waitCount).toBe(10);
        expect(zoomed.datasources[0].preview).not.toBeNull();
        expect(zoomed.datasources[0].preview).toHaveLength(fullHour.datasources[0].history.length);
    });

    it('buildImportedMetricsView conserva bucketSeconds original quan el fitxer ja ve agregat', () => {
        const t0 = new Date(2026, 10, 24, 10, 0, 0).getTime();
        const history = Array.from({ length: 10 }, (_, idx) => ({
            timestamp: formatTs(t0 + idx * 20_000),
            inUseCount: idx + 1,
            waitCount: 0,
        }));
        const snapshot: PerformanceMetricsResponse = {
            collectedAt: formatTs(t0 + 9 * 20_000),
            samplingIntervalSeconds: 1,
            maxHistoryPoints: 3600,
            windowSeconds: 180,
            bucketSeconds: 20,
            datasources: [
                {
                    poolName: 'comandaDB',
                    statisticsEnabled: true,
                    waitCountType: 'INTERVAL_DELTA',
                    history,
                },
            ],
        };

        const view = buildImportedMetricsView(snapshot, 300, null, 180);
        expect(view.bucketSeconds).toBe(20);
        expect(view.datasources[0].history).toHaveLength(10);
    });

    it('extractExportFilename extreu nom de Content-Disposition i fa fallback segur', () => {
        expect(
            extractExportFilename('attachment; filename="performance-metrics-20261124-093000.json"')
        ).toBe('performance-metrics-20261124-093000.json');

        expect(
            extractExportFilename("attachment; filename*=UTF-8''metrics%20snapshot.json")
        ).toBe('metrics snapshot.json');

        const fixedDate = new Date(2026, 10, 24, 9, 15, 30);
        expect(extractExportFilename(null, fixedDate)).toBe('performance-metrics-20261124-091530.json');
    });
});
