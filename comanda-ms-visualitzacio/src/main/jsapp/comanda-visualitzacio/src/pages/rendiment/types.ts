export type WaitCountType = 'INTERVAL_DELTA' | 'INSTANTANEOUS';

export type WorkerPoolType = 'EXECUTOR' | 'SCHEDULER';

export type TimeWindowSeconds = 120 | 300 | 900 | 1800 | 3600;

export interface ZoomTimeRange {
    from: string;
    to: string;
}

export interface DatasourcePoolSample {
    timestamp: string;
    inUseCount?: number | null;
    activeCount?: number | null;
    waitCount?: number | null;
    maxWaitTimeMs?: number | null;
}

export interface DatasourcePoolMetrics {
    poolName: string;
    jndiName?: string | null;
    statisticsEnabled: boolean;
    waitCountType?: WaitCountType | null;
    minSize?: number | null;
    maxSize?: number | null;
    inUseCount?: number | null;
    activeCount?: number | null;
    waitCount?: number | null;
    maxWaitTimeMs?: number | null;
    history: DatasourcePoolSample[];
    preview?: DatasourcePoolSample[] | null;
}

export interface WorkerPoolSample {
    timestamp: string;
    poolSize?: number | null;
    activeCount?: number | null;
    queueSize?: number | null;
    completedTasks?: number | null;
}

export interface WorkerPoolMetrics {
    poolName: string;
    threadNamePrefix?: string | null;
    poolType?: WorkerPoolType | null;
    coreSize?: number | null;
    maxSize?: number | null;
    poolSize?: number | null;
    activeCount?: number | null;
    queueSize?: number | null;
    queueCapacity?: number | null;
    completedTasks?: number | null;
    history: WorkerPoolSample[];
    preview?: WorkerPoolSample[] | null;
}

export interface JmsQueueSample {
    timestamp: string;
    consumerCount?: number | null;
    deliveringCount?: number | null;
    pendingCount?: number | null;
    completedMessages?: number | null;
    addedMessages?: number | null;
}

export interface JmsQueueMetrics {
    queueName: string;
    listenerCount?: number | null;
    minConsumers?: number | null;
    maxConsumers?: number | null;
    consumerCount?: number | null;
    deliveringCount?: number | null;
    pendingCount?: number | null;
    messageCount?: number | null;
    completedMessages?: number | null;
    addedMessages?: number | null;
    history: JmsQueueSample[];
    preview?: JmsQueueSample[] | null;
}

/**
 * Unified payload for GET /api/performance-metrics.
 * Organized into sibling categories synchronized on the same 1s sampling clock.
 */
export interface PerformanceMetricsResponse {
    collectedAt?: string | null;
    samplingIntervalSeconds: number;
    maxHistoryPoints: number;
    windowSeconds: number;
    bucketSeconds: number;
    previewBucketSeconds?: number | null;
    collectionEnabled?: boolean;
    estimatedMemoryBytes?: number | null;
    windowStart?: string | null;
    windowEnd?: string | null;
    rangeStart?: string | null;
    rangeEnd?: string | null;
    datasources: DatasourcePoolMetrics[];
    workerPools?: WorkerPoolMetrics[];
    jmsQueues?: JmsQueueMetrics[];
}
