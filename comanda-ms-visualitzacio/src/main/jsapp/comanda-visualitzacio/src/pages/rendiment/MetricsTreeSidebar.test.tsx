import * as React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import MetricsTreeSidebar from './MetricsTreeSidebar';
import translationCa from '../../i18n/translationCa';
import {
    ALL_LAYERS,
    buildEntityColorMap,
    createDefaultEnabledLayers,
    createDefaultSplitLayers,
    LayerColorMap,
    LayerLabelMap,
    MetricLayerCategory,
    PerformanceLayerId,
    WORKER_LAYERS,
} from './chartModel';
import { DatasourcePoolMetrics, JmsQueueMetrics, WorkerPoolMetrics } from './types';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: unknown) =>
            typeof selector === 'function'
                ? (selector as (tr: typeof translationCa) => string)(translationCa)
                : String(selector),
    }),
}));

const LABELS: LayerLabelMap = {
    inUseCount: 'Connexions en ús',
    activeCount: 'Connexions físiques',
    waitCount: 'Esperes (interval)',
    maxWaitTimeMs: "Temps màx. d'espera",
    maxSize: 'Màx. connexions',
    minSize: 'Mín. connexions',
    workerActiveCount: 'Fils en execució',
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

const DATASOURCES: DatasourcePoolMetrics[] = [
    {
        poolName: 'comandaDB',
        statisticsEnabled: true,
        waitCountType: 'INTERVAL_DELTA',
        minSize: 5,
        maxSize: 50,
        inUseCount: 3,
        activeCount: 10,
        waitCount: 0,
        maxWaitTimeMs: 12,
        history: [],
    },
];

const WORKER_POOLS: WorkerPoolMetrics[] = [
    {
        poolName: 'salutWorkerExecutor',
        poolType: 'EXECUTOR',
        coreSize: 5,
        maxSize: 5,
        poolSize: 5,
        activeCount: 2,
        queueSize: 1,
        queueCapacity: 1000,
        completedTasks: 9,
        history: [],
    },
    {
        poolName: 'monitorTaskScheduler',
        poolType: 'SCHEDULER',
        coreSize: 10,
        maxSize: 10,
        poolSize: 4,
        activeCount: 1,
        queueSize: 0,
        queueCapacity: null,
        completedTasks: 2,
        history: [],
    },
];

interface HarnessState {
    enabledLayers: Set<PerformanceLayerId>;
    excludedEntitiesByLayer: Partial<Record<PerformanceLayerId, Set<string>>>;
    splitLayers: Set<PerformanceLayerId>;
}

const clickTreeCheckbox = (nodeTestId: string): void => {
    const node = screen.getByTestId(nodeTestId);
    const checkbox = node.querySelector('input[type="checkbox"]');
    if (!checkbox) {
        throw new Error(`No checkbox found inside ${nodeTestId}`);
    }
    fireEvent.click(checkbox);
};

interface SidebarHarnessProps {
    datasources?: DatasourcePoolMetrics[];
    workerPools?: WorkerPoolMetrics[];
    jmsQueues?: JmsQueueMetrics[];
    onStateChange: (state: HarnessState) => void;
}

const SidebarHarness: React.FC<SidebarHarnessProps> = ({
    datasources = [],
    workerPools = [],
    jmsQueues = [],
    onStateChange,
}) => {
    const [enabledLayers, setEnabledLayers] = React.useState<Set<PerformanceLayerId>>(
        createDefaultEnabledLayers
    );
    const [excludedEntitiesByLayer, setExcludedEntitiesByLayer] = React.useState<
        Partial<Record<PerformanceLayerId, Set<string>>>
    >({});
    const [splitLayers, setSplitLayers] = React.useState<Set<PerformanceLayerId>>(
        createDefaultSplitLayers
    );

    const entityColorMaps = React.useMemo<Record<MetricLayerCategory, Record<string, string>>>(
        () => ({
            datasource: buildEntityColorMap(datasources.map(ds => ds.poolName)),
            worker: buildEntityColorMap(workerPools.map(wp => wp.poolName)),
            jms: buildEntityColorMap(jmsQueues.map(q => q.queueName)),
        }),
        [datasources, workerPools, jmsQueues]
    );

    React.useEffect(() => {
        onStateChange({ enabledLayers, excludedEntitiesByLayer, splitLayers });
    }, [enabledLayers, excludedEntitiesByLayer, splitLayers, onStateChange]);

    const handleSelectionChange = React.useCallback(
        (
            nextEnabled: Set<PerformanceLayerId>,
            nextExcluded: Partial<Record<PerformanceLayerId, Set<string>>>
        ) => {
            setEnabledLayers(nextEnabled);
            setExcludedEntitiesByLayer(nextExcluded);
        },
        []
    );

    const handleToggleSplitLayer = React.useCallback((layerId: PerformanceLayerId) => {
        setSplitLayers(prev => {
            const next = new Set(prev);
            if (next.has(layerId)) {
                next.delete(layerId);
            } else {
                next.add(layerId);
            }
            return next;
        });
    }, []);

    const handleSelectAllLayers = React.useCallback(() => {
        setEnabledLayers(new Set(ALL_LAYERS.map(l => l.id)));
        setExcludedEntitiesByLayer({});
    }, []);

    const handleResetDefaultLayers = React.useCallback(() => {
        setEnabledLayers(createDefaultEnabledLayers());
        setExcludedEntitiesByLayer({});
        setSplitLayers(createDefaultSplitLayers());
    }, []);

    const handleClearAllLayers = React.useCallback(() => {
        setEnabledLayers(new Set());
        setExcludedEntitiesByLayer({});
    }, []);

    return (
        <MetricsTreeSidebar
            datasources={datasources}
            workerPools={workerPools}
            jmsQueues={jmsQueues}
            enabledLayers={enabledLayers}
            excludedEntitiesByLayer={excludedEntitiesByLayer}
            splitLayers={splitLayers}
            labels={LABELS}
            colors={COLORS}
            entityColorMaps={entityColorMaps}
            onSelectionChange={handleSelectionChange}
            onToggleSplitLayer={handleToggleSplitLayer}
            onResetDefaultLayers={handleResetDefaultLayers}
            onSelectAllLayers={handleSelectAllLayers}
            onClearAllLayers={handleClearAllLayers}
        />
    );
};

describe('MetricsTreeSidebar', () => {
    it('MetricsTreeSidebar_quanEsCommutaDesglossamentIExclusionsFills_actualitzaSplitISeleccioIndividual', () => {
        let latestState: HarnessState | null = null;
        render(
            <SidebarHarness
                workerPools={WORKER_POOLS}
                onStateChange={state => {
                    latestState = state;
                }}
            />
        );

        const splitToggle = screen.getByTestId('split-toggle-workerQueueSize');
        expect(splitToggle).toHaveAttribute(
            'aria-label',
            'Desglossat (≡) · Clic per agregar en una sola sèrie'
        );
        expect(splitToggle).not.toHaveAttribute('aria-pressed');

        fireEvent.click(splitToggle);
        expect(splitToggle).toHaveAttribute(
            'aria-label',
            'Agregat (Σ) · Clic per desglossar per pool/cua'
        );
        expect(latestState!.splitLayers.has('workerQueueSize')).toBe(false);

        fireEvent.click(splitToggle);
        expect(splitToggle).toHaveAttribute(
            'aria-label',
            'Desglossat (≡) · Clic per agregar en una sola sèrie'
        );
        expect(latestState!.splitLayers.has('workerQueueSize')).toBe(true);

        // Desmarcar un worker fill individual exclou només aquella entitat
        clickTreeCheckbox('tree-series-workerQueueSize-monitorTaskScheduler');
        expect(latestState!.enabledLayers.has('workerQueueSize')).toBe(true);
        expect(latestState!.excludedEntitiesByLayer.workerQueueSize?.has('monitorTaskScheduler')).toBe(
            true
        );
        expect(latestState!.excludedEntitiesByLayer.workerQueueSize?.has('salutWorkerExecutor')).toBe(
            false
        );

        // Tornar a marcar el worker fill neteja l'exclusió
        clickTreeCheckbox('tree-series-workerQueueSize-monitorTaskScheduler');
        expect(latestState!.excludedEntitiesByLayer.workerQueueSize?.size ?? 0).toBe(0);
    });

    it('MetricsTreeSidebar_quanEsClicCategoriaIndeterminada_seleccionaTotesIDespresSilenciaCategoria', () => {
        let latestState: HarnessState | null = null;
        render(
            <SidebarHarness
                datasources={DATASOURCES}
                workerPools={WORKER_POOLS}
                onStateChange={state => {
                    latestState = state;
                }}
            />
        );

        // 1r clic sobre categoria indeterminada (2 de 6 capes worker actives) -> activa totes les capes worker
        clickTreeCheckbox('tree-cat-worker');
        for (const layer of WORKER_LAYERS) {
            expect(latestState!.enabledLayers.has(layer.id)).toBe(true);
            expect(latestState!.excludedEntitiesByLayer[layer.id]?.size ?? 0).toBe(0);
        }
        expect(latestState!.enabledLayers.has('inUseCount')).toBe(true);
        expect(latestState!.enabledLayers.has('waitCount')).toBe(true);
        expect(latestState!.enabledLayers.has('jmsDeliveringCount')).toBe(true);
        expect(latestState!.enabledLayers.has('jmsPendingCount')).toBe(true);

        // 2n clic -> silencia tota la categoria worker preservant BD i JMS (incloent capes ocultes)
        clickTreeCheckbox('tree-cat-worker');
        for (const layer of WORKER_LAYERS) {
            expect(latestState!.enabledLayers.has(layer.id)).toBe(false);
        }
        expect(latestState!.enabledLayers.has('inUseCount')).toBe(true);
        expect(latestState!.enabledLayers.has('waitCount')).toBe(true);
        expect(latestState!.enabledLayers.has('jmsDeliveringCount')).toBe(true);
        expect(latestState!.enabledLayers.has('jmsPendingCount')).toBe(true);
    });

    it('MetricsTreeSidebar_botonsAccioRapida_apliquenTotesCapIPerDefecte', () => {
        let latestState: HarnessState | null = null;
        render(
            <SidebarHarness
                datasources={DATASOURCES}
                onStateChange={state => {
                    latestState = state;
                }}
            />
        );

        fireEvent.click(screen.getByTestId('layers-select-all-button'));
        expect(latestState!.enabledLayers.size).toBe(ALL_LAYERS.length);
        expect(Object.keys(latestState!.excludedEntitiesByLayer)).toHaveLength(0);

        fireEvent.click(screen.getByTestId('layers-clear-all-button'));
        expect(latestState!.enabledLayers.size).toBe(0);

        fireEvent.click(screen.getByTestId('layers-reset-default-button'));
        expect(latestState!.enabledLayers).toEqual(createDefaultEnabledLayers());
        expect(latestState!.splitLayers).toEqual(createDefaultSplitLayers());
        expect(Object.keys(latestState!.excludedEntitiesByLayer)).toHaveLength(0);
    });
});
