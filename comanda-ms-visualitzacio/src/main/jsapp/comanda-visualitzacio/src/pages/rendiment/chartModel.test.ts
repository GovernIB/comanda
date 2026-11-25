import { describe, expect, it } from 'vitest';
import {
    buildComposableChartModel,
    buildEntityColorMap,
    buildPreviewChartModel,
    buildWorkerColorMap,
    CategoryPrefixMap,
    centerPctRange,
    computeJmsKpiSummary,
    computeKpiSummary,
    computeWorkerKpiSummary,
    createDefaultEnabledLayers,
    createDefaultSplitLayers,
    formatBucketResolution,
    getAutoRefreshIntervalMs,
    getBucketCenterMs,
    LayerColorMap,
    LayerLabelMap,
    panPctRange,
    pctRangeToTimeRange,
    pctToTimestamp,
    PerformanceLayerId,
    resolveTimeDomain,
    resolveWaitCountVariant,
    timeRangeToPctRange,
    WORKER_QUEUE_PALETTE,
} from './chartModel';
import { DatasourcePoolMetrics, JmsQueueMetrics, WorkerPoolMetrics } from './types';

const LABELS: LayerLabelMap = {
    inUseCount: 'Connexions en ús',
    activeCount: 'Connexions físiques',
    waitCount: 'Esperes',
    maxWaitTimeMs: 'Temps màx. espera (ms)',
    maxSize: 'Màx. connexions',
    minSize: 'Mín. connexions',
    workerActiveCount: 'Fils executant',
    workerQueueSize: 'Tasques en cua',
    workerPoolSize: 'Fils creats',
    workerCompletedTasks: 'Tasques completades',
    workerMaxSize: 'Màx. fils',
    workerCoreSize: 'Fils core',
    jmsDeliveringCount: 'Missatges en procés',
    jmsPendingCount: 'Missatges pendents',
    jmsConsumerCount: 'Consumidors actius',
    jmsCompletedMessages: 'Missatges processats',
    jmsAddedMessages: 'Missatges encuats',
    jmsMaxConsumers: 'Màx. consumidors',
};

const CATEGORY_PREFIXES: CategoryPrefixMap = {
    datasource: 'BD',
    worker: 'Workers',
    jms: 'JMS',
};

const COLORS: LayerColorMap = {
    inUseCount: '#1976d2',
    activeCount: '#0288d1',
    waitCount: '#d32f2f',
    maxWaitTimeMs: '#ed6c02',
    maxSize: '#9c27b0',
    minSize: '#616161',
    workerActiveCount: '#2e7d32',
    workerQueueSize: '#f4511e',
    workerPoolSize: '#26a69a',
    workerCompletedTasks: '#7cb342',
    workerMaxSize: '#5c6bc0',
    workerCoreSize: '#8d6e63',
    jmsDeliveringCount: '#00897b',
    jmsPendingCount: '#ff8f00',
    jmsConsumerCount: '#039be5',
    jmsCompletedMessages: '#ab47bc',
    jmsAddedMessages: '#ec407a',
    jmsMaxConsumers: '#78909c',
};

const dsComanda: DatasourcePoolMetrics = {
    poolName: 'comandaDB',
    jndiName: 'java:jboss/datasources/comandaDS',
    statisticsEnabled: true,
    waitCountType: 'INTERVAL_DELTA',
    minSize: 5,
    maxSize: 50,
    inUseCount: 4,
    activeCount: 12,
    waitCount: 2,
    maxWaitTimeMs: 40,
    history: [
        {
            timestamp: '2026-11-19T10:00:00',
            inUseCount: 3,
            activeCount: 10,
            waitCount: 0,
            maxWaitTimeMs: 12,
        },
        {
            timestamp: '2026-11-19T10:00:15',
            inUseCount: 4,
            activeCount: 12,
            waitCount: 2,
            maxWaitTimeMs: 40,
        },
    ],
};

const dsSecondary: DatasourcePoolMetrics = {
    poolName: 'auditDS',
    jndiName: 'java:jboss/datasources/auditDS',
    statisticsEnabled: true,
    waitCountType: 'INTERVAL_DELTA',
    minSize: 2,
    maxSize: 20,
    inUseCount: 1,
    activeCount: 4,
    waitCount: 5,
    maxWaitTimeMs: 95,
    history: [
        {
            timestamp: '2026-11-19T10:00:15',
            inUseCount: 1,
            activeCount: 4,
            waitCount: 5,
            maxWaitTimeMs: 95,
        },
    ],
};

const dsDisabled: DatasourcePoolMetrics = {
    poolName: 'ExampleDS',
    jndiName: 'java:jboss/datasources/ExampleDS',
    statisticsEnabled: false,
    waitCountType: 'INTERVAL_DELTA',
    minSize: 1,
    maxSize: 20,
    history: [],
};

const wpSalut: WorkerPoolMetrics = {
    poolName: 'salutWorkerExecutor',
    threadNamePrefix: 'salut-worker-',
    poolType: 'EXECUTOR',
    coreSize: 5,
    maxSize: 5,
    poolSize: 5,
    activeCount: 3,
    queueSize: 12,
    queueCapacity: 1000,
    completedTasks: 8,
    history: [
        {
            timestamp: '2026-11-19T10:00:00',
            poolSize: 5,
            activeCount: 1,
            queueSize: 0,
            completedTasks: 3,
        },
        {
            timestamp: '2026-11-19T10:00:15',
            poolSize: 5,
            activeCount: 3,
            queueSize: 12,
            completedTasks: 8,
        },
    ],
};

const wpScheduler: WorkerPoolMetrics = {
    poolName: 'monitorTaskScheduler',
    threadNamePrefix: 'monitor-tasques-',
    poolType: 'SCHEDULER',
    coreSize: 10,
    maxSize: 10,
    poolSize: 4,
    activeCount: 1,
    queueSize: 2,
    queueCapacity: null,
    completedTasks: 2,
    history: [
        {
            timestamp: '2026-11-19T10:00:15',
            poolSize: 4,
            activeCount: 1,
            queueSize: 2,
            completedTasks: 2,
        },
    ],
};

const jmsTasques: JmsQueueMetrics = {
    queueName: 'tasquesQueue',
    listenerCount: 1,
    minConsumers: 5,
    maxConsumers: 50,
    consumerCount: 7,
    deliveringCount: 4,
    pendingCount: 9,
    completedMessages: 11,
    addedMessages: 15,
    history: [
        {
            timestamp: '2026-11-19T10:00:00',
            consumerCount: 5,
            deliveringCount: 2,
            pendingCount: 1,
            completedMessages: 4,
            addedMessages: 5,
        },
        {
            timestamp: '2026-11-19T10:00:15',
            consumerCount: 7,
            deliveringCount: 4,
            pendingCount: 9,
            completedMessages: 11,
            addedMessages: 15,
        },
    ],
};

describe('chartModel', () => {
    it('buildComposableChartModel_agregaMultiplesDatasourcesSumantComptadorsIMaxWait', () => {
        const enabledLayers = new Set<PerformanceLayerId>([
            'inUseCount',
            'activeCount',
            'waitCount',
            'maxWaitTimeMs',
            'maxSize',
            'minSize',
        ]);

        const model = buildComposableChartModel({
            datasources: [dsComanda, dsSecondary, dsDisabled],
            enabledLayers,
            aggregationMode: 'aggregated',
            labels: LABELS,
            colors: COLORS,
        });

        expect(model.timestamps).toEqual([
            '2026-11-19T10:00:00',
            '2026-11-19T10:00:15',
        ]);
        expect(model.hasCountAxis).toBe(true);
        expect(model.hasDurationAxis).toBe(true);
        expect(Array.from(model.activeCategories)).toEqual(['datasource']);
        expect(model.dashedSeriesIds).toEqual(['maxSize']);
        expect(model.dottedSeriesIds).toEqual(['minSize']);

        const inUse = model.series.find(s => s.id === 'inUseCount');
        const wait = model.series.find(s => s.id === 'waitCount');
        const maxWait = model.series.find(s => s.id === 'maxWaitTimeMs');
        const maxSize = model.series.find(s => s.id === 'maxSize');

        expect(inUse?.data).toEqual([3, 5]);
        expect(wait?.type).toBe('bar');
        expect(wait?.data).toEqual([0, 7]);
        expect(maxWait?.yAxisId).toBe('durationMs');
        expect(maxWait?.data).toEqual([12, 95]);
        expect(maxSize?.data).toEqual([50, 70]);
    });

    it('buildComposableChartModel_combinaBdWorkersIJmsAmbCapesCuradesPerDefecteIModePerPool', () => {
        const defaults = createDefaultEnabledLayers();
        expect(Array.from(defaults).sort()).toEqual([
            'inUseCount',
            'jmsDeliveringCount',
            'jmsPendingCount',
            'waitCount',
            'workerActiveCount',
            'workerQueueSize',
        ]);
        expect(Array.from(createDefaultSplitLayers())).toEqual(['workerQueueSize']);
        expect(defaults.has('maxWaitTimeMs')).toBe(false);
        expect(defaults.has('activeCount')).toBe(false);
        expect(defaults.has('workerCompletedTasks')).toBe(false);
        expect(defaults.has('jmsCompletedMessages')).toBe(false);

        const aggregated = buildComposableChartModel({
            datasources: [dsComanda],
            workerPools: [wpSalut, wpScheduler],
            jmsQueues: [jmsTasques],
            enabledLayers: defaults,
            aggregationMode: 'aggregated',
            labels: LABELS,
            colors: COLORS,
        });

        expect(aggregated.series).toHaveLength(6);
        expect(aggregated.hasDurationAxis).toBe(false);
        expect(aggregated.activeCategories).toEqual(new Set(['datasource', 'worker', 'jms']));
        expect(aggregated.series.find(s => s.id === 'workerActiveCount')?.data).toEqual([1, 4]);
        expect(aggregated.series.find(s => s.id === 'workerQueueSize')?.data).toEqual([0, 14]);
        expect(aggregated.series.find(s => s.id === 'jmsDeliveringCount')?.data).toEqual([2, 4]);
        expect(aggregated.series.find(s => s.id === 'jmsPendingCount')?.data).toEqual([1, 9]);

        // Capes opcionals de throughput (completedTasks i completedMessages)
        const withThroughput = new Set<PerformanceLayerId>([
            'workerCompletedTasks',
            'jmsCompletedMessages',
            'jmsAddedMessages',
        ]);
        const throughputModel = buildComposableChartModel({
            datasources: [dsDisabled],
            workerPools: [wpSalut, wpScheduler],
            jmsQueues: [jmsTasques],
            enabledLayers: withThroughput,
            aggregationMode: 'aggregated',
            labels: LABELS,
            colors: COLORS,
        });
        expect(throughputModel.series.find(s => s.id === 'workerCompletedTasks')?.data).toEqual([3, 10]);
        expect(throughputModel.series.find(s => s.id === 'jmsCompletedMessages')?.data).toEqual([4, 11]);
        expect(throughputModel.series.find(s => s.id === 'jmsAddedMessages')?.data).toEqual([5, 15]);
        expect(throughputModel.activeCategories).toEqual(new Set(['worker', 'jms']));

        // Mode Per pool amb multiple categories
        const perPool = buildComposableChartModel({
            datasources: [dsComanda],
            workerPools: [wpSalut, wpScheduler],
            jmsQueues: [jmsTasques],
            enabledLayers: new Set<PerformanceLayerId>(['inUseCount', 'workerActiveCount', 'jmsDeliveringCount']),
            aggregationMode: 'perPool',
            labels: LABELS,
            categoryPrefixes: CATEGORY_PREFIXES,
            colors: COLORS,
        });
        expect(perPool.series.map(s => s.label)).toEqual([
            'comandaDB · Connexions en ús',
            'salutWorkerExecutor · Fils executant',
            'monitorTaskScheduler · Fils executant',
            'tasquesQueue · Missatges en procés',
        ]);
    });

    it('buildComposableChartModel_ambSplitWorkerQueuesApilaCuesPerWorkerISeparaColors', () => {
        const defaults = createDefaultEnabledLayers();
        const workerColorMap = buildWorkerColorMap([wpSalut, wpScheduler]);
        const dsColorMap = buildEntityColorMap(['comandaDB', 'auditDS']);
        expect(workerColorMap).toEqual({
            salutWorkerExecutor: WORKER_QUEUE_PALETTE[0],
            monitorTaskScheduler: WORKER_QUEUE_PALETTE[1],
        });

        const splitModel = buildComposableChartModel({
            datasources: [dsComanda],
            workerPools: [wpSalut, wpScheduler],
            jmsQueues: [jmsTasques],
            enabledLayers: defaults,
            labels: LABELS,
            categoryPrefixes: CATEGORY_PREFIXES,
            colors: COLORS,
            splitLayers: createDefaultSplitLayers(),
            entityColorMaps: { worker: workerColorMap },
        });

        // 2 DB + 1 workerActiveCount + 2 stacked workerQueueSize + 2 JMS = 7 sèries
        expect(splitModel.series).toHaveLength(7);
        expect(splitModel.series.find(s => s.id === 'inUseCount')?.label).toBe('BD · Connexions en ús');
        expect(splitModel.series.find(s => s.id === 'workerActiveCount')?.label).toBe('Workers · Fils executant');
        expect(splitModel.series.find(s => s.id === 'jmsDeliveringCount')?.label).toBe('JMS · Missatges en procés');
        expect(splitModel.series.find(s => s.id === 'jmsPendingCount')?.label).toBe('JMS · Missatges pendents');
        expect(splitModel.series.find(s => s.id === 'workerActiveCount')?.data).toEqual([1, 4]);
        expect(splitModel.series.find(s => s.id === 'jmsPendingCount')?.data).toEqual([1, 9]);

        const salutQueue = splitModel.series.find(s => s.label === 'salutWorkerExecutor · Tasques en cua');
        const schedulerQueue = splitModel.series.find(s => s.label === 'monitorTaskScheduler · Tasques en cua');
        expect(salutQueue).toMatchObject({
            type: 'bar',
            stack: 'workerQueueSize',
            color: WORKER_QUEUE_PALETTE[0],
            data: [0, 12],
        });
        expect(schedulerQueue).toMatchObject({
            type: 'bar',
            stack: 'workerQueueSize',
            color: WORKER_QUEUE_PALETTE[1],
            data: [null, 2],
        });

        // Exclusions per capa i split independent a altres capes (ex: inUseCount)
        const filteredAndSplit = buildComposableChartModel({
            datasources: [dsComanda, dsSecondary],
            workerPools: [wpSalut, wpScheduler],
            enabledLayers: new Set<PerformanceLayerId>(['inUseCount', 'workerQueueSize']),
            labels: LABELS,
            categoryPrefixes: CATEGORY_PREFIXES,
            colors: COLORS,
            splitLayers: new Set<PerformanceLayerId>(['inUseCount', 'workerQueueSize']),
            excludedEntitiesByLayer: {
                workerQueueSize: new Set(['salutWorkerExecutor']),
            },
            entityColorMaps: {
                datasource: dsColorMap,
                worker: workerColorMap,
            },
        });
        expect(filteredAndSplit.series.map(s => s.label)).toEqual([
            'comandaDB · Connexions en ús',
            'auditDS · Connexions en ús',
            'monitorTaskScheduler · Tasques en cua',
        ]);
        expect(filteredAndSplit.series[2]).toMatchObject({
            color: WORKER_QUEUE_PALETTE[1],
        });

        // En mode perPool, les barres amb múltiples ítems també s'apilen
        const perPoolBars = buildComposableChartModel({
            datasources: [dsComanda, dsSecondary],
            workerPools: [wpSalut, wpScheduler],
            enabledLayers: new Set<PerformanceLayerId>(['waitCount', 'workerQueueSize']),
            aggregationMode: 'perPool',
            labels: LABELS,
            categoryPrefixes: CATEGORY_PREFIXES,
            colors: COLORS,
            workerColorMap,
        });
        expect(perPoolBars.series.find(s => s.label === 'comandaDB · Esperes')).toMatchObject({
            type: 'bar',
            stack: 'waitCount',
        });
        expect(perPoolBars.series.find(s => s.label === 'salutWorkerExecutor · Tasques en cua')).toMatchObject({
            type: 'bar',
            stack: 'workerQueueSize',
            color: WORKER_QUEUE_PALETTE[0],
        });
    });

    it('buildComposableChartModel_enModePerPoolSeparaLesSeriesPerDatasource', () => {
        const enabledLayers = new Set<PerformanceLayerId>(['inUseCount', 'maxWaitTimeMs']);

        const model = buildComposableChartModel({
            datasources: [dsComanda, dsSecondary],
            enabledLayers,
            aggregationMode: 'perPool',
            labels: LABELS,
            colors: COLORS,
        });

        expect(model.series).toHaveLength(4);
        expect(model.series.map(s => s.label)).toEqual([
            'comandaDB · Connexions en ús',
            'auditDS · Connexions en ús',
            'comandaDB · Temps màx. espera (ms)',
            'auditDS · Temps màx. espera (ms)',
        ]);
        expect(model.series[1].data).toEqual([null, 1]);
        expect(model.series[3].data).toEqual([null, 95]);
    });

    it('buildComposableChartModel_adaptaEixosSegonsCapesActives', () => {
        const onlyDuration = new Set<PerformanceLayerId>(['maxWaitTimeMs']);
        const durationModel = buildComposableChartModel({
            datasources: [dsComanda],
            enabledLayers: onlyDuration,
            aggregationMode: 'aggregated',
            labels: LABELS,
            colors: COLORS,
        });
        expect(durationModel.hasCountAxis).toBe(false);
        expect(durationModel.hasDurationAxis).toBe(true);
        expect(durationModel.activeCategories.size).toBe(0);
        expect(durationModel.series).toHaveLength(1);

        const emptyModel = buildComposableChartModel({
            datasources: [dsComanda],
            enabledLayers: new Set(),
            aggregationMode: 'aggregated',
            labels: LABELS,
            colors: COLORS,
        });
        expect(emptyModel.series).toEqual([]);
    });

    it('computeKpiSummariesIResolveWaitCountVariant_calculenAgregatsPerTotesLesCategories', () => {
        const kpi = computeKpiSummary([dsComanda, dsSecondary, dsDisabled]);
        expect(kpi).toEqual({
            inUseCount: 5,
            activeCount: 16,
            minSize: 7,
            maxSize: 70,
            waitCount: 7,
            maxWaitTimeMs: 95,
        });

        const workerKpi = computeWorkerKpiSummary([wpSalut, wpScheduler]);
        expect(workerKpi).toEqual({
            activeCount: 4,
            poolSize: 9,
            coreSize: 15,
            maxSize: 15,
            queueSize: 14,
            queueCapacity: 1000,
            hasUnboundedQueue: true,
            completedTasks: 10,
        });

        const jmsKpi = computeJmsKpiSummary([jmsTasques]);
        expect(jmsKpi).toEqual({
            deliveringCount: 4,
            pendingCount: 9,
            consumerCount: 7,
            minConsumers: 5,
            maxConsumers: 50,
            completedMessages: 11,
            addedMessages: 15,
        });

        expect(resolveWaitCountVariant([dsComanda, dsDisabled])).toBe('INTERVAL_DELTA');
        expect(
            resolveWaitCountVariant([
                { ...dsComanda, waitCountType: 'INSTANTANEOUS' },
            ])
        ).toBe('INSTANTANEOUS');
        expect(
            resolveWaitCountVariant([
                dsComanda,
                { ...dsSecondary, waitCountType: 'INSTANTANEOUS' },
            ])
        ).toBe('MIXED');
    });

    it('buildPreviewChartModelIPctConversions_gestionenPreviewCombinatIRangZoom', () => {
        const withPreview: DatasourcePoolMetrics = {
            ...dsComanda,
            preview: [
                { timestamp: '2026-11-19T10:00:00', inUseCount: 1, waitCount: 0 },
                { timestamp: '2026-11-19T10:10:00', inUseCount: 8, waitCount: 2 },
                { timestamp: '2026-11-19T10:20:00', inUseCount: 3, waitCount: 0 },
                { timestamp: '2026-11-19T10:30:00', inUseCount: 5, waitCount: 1 },
                { timestamp: '2026-11-19T10:40:00', inUseCount: 2, waitCount: 0 },
            ],
        };

        const preview = buildPreviewChartModel([withPreview, dsDisabled], COLORS);
        expect(preview.timestamps).toHaveLength(5);
        expect(preview.series.find(s => s.id === 'preview-inUse')?.data).toEqual([1, 8, 3, 5, 2]);
        expect(preview.series.find(s => s.id === 'preview-wait')?.data).toEqual([0, 2, 0, 1, 0]);

        // Preview funciona amb workers + JMS encara que tots els datasources estiguin desactivats
        const workerOnlyPreview = buildPreviewChartModel([dsDisabled], COLORS, [wpSalut], [jmsTasques]);
        expect(workerOnlyPreview.timestamps).toEqual(['2026-11-19T10:00:00', '2026-11-19T10:00:15']);
        expect(workerOnlyPreview.series.map(s => s.id)).toEqual([
            'preview-workerActive',
            'preview-workerQueue',
            'preview-jmsDelivering',
            'preview-jmsPending',
        ]);

        expect(pctRangeToTimeRange(preview.timestamps, 0, 100)).toBeNull();
        expect(pctRangeToTimeRange(preview.timestamps, 25, 75)).toEqual({
            from: '2026-11-19T10:10:00',
            to: '2026-11-19T10:30:00',
        });
        expect(
            timeRangeToPctRange(preview.timestamps, {
                from: '2026-11-19T10:10:00',
                to: '2026-11-19T10:30:00',
            })
        ).toEqual([25, 75]);
        expect(timeRangeToPctRange(preview.timestamps, null)).toEqual([0, 100]);

        expect(formatBucketResolution(1)).toBe('1 s');
        expect(formatBucketResolution(20)).toBe('20 s');
        expect(formatBucketResolution(60)).toBe('1 min');
    });

    it('panPctRangeICenterPctRange_desplacenICentrenPreservantLamplada', () => {
        expect(panPctRange(20, 40, 15)).toEqual([35, 55]);
        expect(panPctRange(20, 40, -30)).toEqual([0, 20]);
        expect(panPctRange(75, 95, 20)).toEqual([80, 100]);
        expect(panPctRange(0, 100, 10)).toEqual([0, 100]);

        expect(centerPctRange(10, 30, 60)).toEqual([50, 70]);
        expect(centerPctRange(10, 30, 5)).toEqual([0, 20]);
        expect(centerPctRange(10, 30, 95)).toEqual([80, 100]);

        expect(getAutoRefreshIntervalMs(120)).toBe(3000);
        expect(getAutoRefreshIntervalMs(300)).toBe(3000);
        expect(getAutoRefreshIntervalMs(900)).toBe(5000);
        expect(getAutoRefreshIntervalMs(1800)).toBe(10000);
        expect(getAutoRefreshIntervalMs(3600)).toBe(10000);
    });

    it('resolveTimeDomainIGetBucketCenterMs_alineenBucketsCentratsICobrixenPicsSubBucket', () => {
        const bucketTimestamps = [
            '2026-11-19T10:30:00',
            '2026-11-19T10:30:20',
            '2026-11-19T10:30:40',
        ];
        const domain = resolveTimeDomain(bucketTimestamps, {
            bucketSeconds: 20,
            windowStart: '2026-11-19T10:30:00',
            windowEnd: '2026-11-19T10:31:00',
        });
        expect(domain).not.toBeNull();
        expect(domain?.bucketSeconds).toBe(20);

        const tStart = new Date('2026-11-19T10:30:00').getTime();
        const tEnd = new Date('2026-11-19T10:31:00').getTime();
        expect(domain?.startMs).toBe(tStart);
        expect(domain?.endMs).toBe(tEnd);

        // El primer bucket [10:30:00 .. 10:30:20] es dibuixa centrat a 10:30:10
        expect(getBucketCenterMs(tStart, domain!)).toBe(tStart + 10_000);

        // Selecció sub-bucket contínua de 16.67%..33.33% cobreix [10:30:10 .. 10:30:20] i inclou un pic a 10:30:19
        const subRange = pctRangeToTimeRange(bucketTimestamps, 100 / 6, 100 / 3, domain);
        expect(subRange).toEqual({
            from: '2026-11-19T10:30:10',
            to: '2026-11-19T10:30:20',
        });
        expect(timeRangeToPctRange(bucketTimestamps, subRange, domain)).toEqual([16.67, 33.33]);
        expect(pctToTimestamp(bucketTimestamps, 50, domain)).toBe('2026-11-19T10:30:30');

        // Quan l'històric és parcial (menys punts que la finestra), no afegeix espai buit a l'esquerra
        const sparseDomain = resolveTimeDomain(bucketTimestamps, {
            bucketSeconds: 20,
            windowStart: '2026-11-19T10:00:00',
            windowEnd: '2026-11-19T10:30:55',
        });
        expect(sparseDomain?.startMs).toBe(tStart);
        expect(sparseDomain?.endMs).toBe(new Date('2026-11-19T10:30:55').getTime());
    });
});
