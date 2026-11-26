import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { SimpleTreeView, TreeItem } from '@mui/x-tree-view';
import { Box, Button, Chip, Stack, Tooltip, Typography } from '@mui/material';
import { DatasourcePoolMetrics, JmsQueueMetrics, WorkerPoolMetrics } from './types';
import {
    computeJmsKpiSummary,
    computeKpiSummary,
    computeWorkerKpiSummary,
    DATASOURCE_LAYERS,
    JMS_LAYERS,
    LayerColorMap,
    LayerLabelMap,
    MetricLayerCategory,
    MetricLayerDefinition,
    PerformanceLayerId,
    WORKER_LAYERS,
    WORKER_QUEUE_PALETTE,
} from './chartModel';

export interface MetricsTreeSidebarProps {
    datasources: DatasourcePoolMetrics[];
    workerPools: WorkerPoolMetrics[];
    jmsQueues: JmsQueueMetrics[];
    enabledLayers: ReadonlySet<PerformanceLayerId>;
    excludedEntitiesByLayer: Partial<Record<PerformanceLayerId, ReadonlySet<string>>>;
    splitLayers: ReadonlySet<PerformanceLayerId>;
    labels: LayerLabelMap;
    colors: LayerColorMap;
    entityColorMaps: Record<MetricLayerCategory, Record<string, string>>;
    onSelectionChange: (
        nextEnabled: Set<PerformanceLayerId>,
        nextExcluded: Partial<Record<PerformanceLayerId, Set<string>>>
    ) => void;
    onToggleSplitLayer: (layerId: PerformanceLayerId) => void;
    onResetDefaultLayers: () => void;
    onSelectAllLayers: () => void;
    onClearAllLayers: () => void;
}

interface TreeEntityMeta {
    name: string;
    tooltip: string;
    item: DatasourcePoolMetrics | WorkerPoolMetrics | JmsQueueMetrics;
}

interface TreeCategoryMeta {
    key: MetricLayerCategory;
    title: string;
    layers: readonly MetricLayerDefinition[];
    entities: TreeEntityMeta[];
}

const DEFAULT_EXPANDED_ITEMS = [
    'cat:datasource',
    'cat:worker',
    'cat:jms',
    'layer:workerQueueSize',
];

const formatCount = (n: number | null | undefined): string =>
    n == null ? '—' : n.toLocaleString('ca-ES');

const isBacklogAlertLayer = (layerId: PerformanceLayerId): boolean =>
    layerId === 'waitCount' || layerId === 'workerQueueSize' || layerId === 'jmsPendingCount';

const formatLayerLiveValue = (
    category: MetricLayerCategory,
    layerId: PerformanceLayerId,
    activeEntities: TreeEntityMeta[]
): { text: string; numeric: number | null } => {
    if (activeEntities.length === 0) {
        return { text: '—', numeric: null };
    }

    if (category === 'datasource') {
        const items = activeEntities.map(e => e.item as DatasourcePoolMetrics);
        const kpi = computeKpiSummary(items);
        switch (layerId) {
            case 'inUseCount':
                return {
                    text: `${formatCount(kpi.inUseCount)}/${formatCount(kpi.maxSize)}`,
                    numeric: kpi.inUseCount,
                };
            case 'waitCount':
                return { text: formatCount(kpi.waitCount), numeric: kpi.waitCount };
            case 'maxWaitTimeMs':
                return {
                    text: kpi.maxWaitTimeMs != null ? `${formatCount(kpi.maxWaitTimeMs)} ms` : '—',
                    numeric: kpi.maxWaitTimeMs,
                };
            case 'activeCount':
                return { text: formatCount(kpi.activeCount), numeric: kpi.activeCount };
            case 'maxSize':
                return { text: formatCount(kpi.maxSize), numeric: kpi.maxSize };
            case 'minSize':
                return { text: formatCount(kpi.minSize), numeric: kpi.minSize };
        }
    }

    if (category === 'worker') {
        const items = activeEntities.map(e => e.item as WorkerPoolMetrics);
        const kpi = computeWorkerKpiSummary(items);
        switch (layerId) {
            case 'workerActiveCount':
                return {
                    text: `${formatCount(kpi.activeCount)}/${formatCount(kpi.maxSize)}`,
                    numeric: kpi.activeCount,
                };
            case 'workerQueueSize':
                return { text: formatCount(kpi.queueSize), numeric: kpi.queueSize };
            case 'workerPoolSize':
                return { text: formatCount(kpi.poolSize), numeric: kpi.poolSize };
            case 'workerCompletedTasks':
                return { text: formatCount(kpi.completedTasks), numeric: kpi.completedTasks };
            case 'workerMaxSize':
                return { text: formatCount(kpi.maxSize), numeric: kpi.maxSize };
            case 'workerCoreSize':
                return { text: formatCount(kpi.coreSize), numeric: kpi.coreSize };
        }
    }

    const items = activeEntities.map(e => e.item as JmsQueueMetrics);
    const kpi = computeJmsKpiSummary(items);
    switch (layerId) {
        case 'jmsDeliveringCount':
            return { text: formatCount(kpi.deliveringCount), numeric: kpi.deliveringCount };
        case 'jmsPendingCount':
            return { text: formatCount(kpi.pendingCount), numeric: kpi.pendingCount };
        case 'jmsConsumerCount':
            return { text: formatCount(kpi.consumerCount), numeric: kpi.consumerCount };
        case 'jmsCompletedMessages':
            return { text: formatCount(kpi.completedMessages), numeric: kpi.completedMessages };
        case 'jmsAddedMessages':
            return { text: formatCount(kpi.addedMessages), numeric: kpi.addedMessages };
        case 'jmsMaxConsumers':
            return { text: formatCount(kpi.maxConsumers), numeric: kpi.maxConsumers };
    }

    return { text: '—', numeric: null };
};

const MetricsTreeSidebar: React.FC<MetricsTreeSidebarProps> = ({
    datasources,
    workerPools,
    jmsQueues,
    enabledLayers,
    excludedEntitiesByLayer,
    splitLayers,
    labels,
    colors,
    entityColorMaps,
    onSelectionChange,
    onToggleSplitLayer,
    onResetDefaultLayers,
    onSelectAllLayers,
    onClearAllLayers,
}) => {
    const { t } = useTranslation();
    const [expandedItems, setExpandedItems] = React.useState<string[]>(DEFAULT_EXPANDED_ITEMS);

    const activeDatasources = React.useMemo(
        () => datasources.filter(ds => ds.statisticsEnabled),
        [datasources]
    );
    const disabledDatasources = React.useMemo(
        () => datasources.filter(ds => !ds.statisticsEnabled),
        [datasources]
    );

    const categories: TreeCategoryMeta[] = React.useMemo(() => {
        const list: TreeCategoryMeta[] = [];
        if (activeDatasources.length > 0) {
            list.push({
                key: 'datasource',
                title: t($ => $.page.rendiment.categories.datasources),
                layers: DATASOURCE_LAYERS,
                entities: activeDatasources.map(ds => ({
                    name: ds.poolName,
                    tooltip: `${ds.jndiName ?? ds.poolName} · ${t($ => $.page.rendiment.kpi.inUse)}: ${ds.inUseCount ?? '—'}/${ds.maxSize ?? '—'} · ${t($ => $.page.rendiment.kpi.minMax)}: ${ds.minSize ?? '—'}–${ds.maxSize ?? '—'}`,
                    item: ds,
                })),
            });
        }
        if (workerPools.length > 0) {
            list.push({
                key: 'worker',
                title: t($ => $.page.rendiment.categories.workers),
                layers: WORKER_LAYERS,
                entities: workerPools.map(wp => {
                    const typeLabel = wp.poolType === 'SCHEDULER'
                        ? t($ => $.page.rendiment.workerTypeScheduler)
                        : t($ => $.page.rendiment.workerTypeExecutor);
                    const queueCapText = wp.queueCapacity != null
                        ? String(wp.queueCapacity)
                        : t($ => $.page.rendiment.unboundedQueue);
                    const tooltip = `${typeLabel}${wp.threadNamePrefix ? ` (${wp.threadNamePrefix}*)` : ''} · ${t($ => $.page.rendiment.activeThreadsShort)}: ${wp.activeCount ?? 0}/${wp.maxSize ?? '—'} · ${t($ => $.page.rendiment.kpi.workerPoolSize)}: ${wp.poolSize ?? '—'} · ${t($ => $.page.rendiment.kpi.coreMax)}: ${wp.coreSize ?? '—'}/${wp.maxSize ?? '—'} · ${t($ => $.page.rendiment.queueShort)}: ${wp.queueSize ?? 0}/${queueCapText}`;
                    return {
                        name: wp.poolName,
                        tooltip,
                        item: wp,
                    };
                }),
            });
        }
        if (jmsQueues.length > 0) {
            list.push({
                key: 'jms',
                title: t($ => $.page.rendiment.categories.jmsQueues),
                layers: JMS_LAYERS,
                entities: jmsQueues.map(q => ({
                    name: q.queueName,
                    tooltip: `${t($ => $.page.rendiment.listenersShort)}: ${q.listenerCount ?? 0} · ${t($ => $.page.rendiment.kpi.consumersCap)}: ${q.consumerCount ?? '—'}/${q.maxConsumers ?? '—'} · ${t($ => $.page.rendiment.consumersMinMax)}: ${q.minConsumers ?? '—'}–${q.maxConsumers ?? '—'} · ${t($ => $.page.rendiment.kpi.jmsPending)}: ${q.pendingCount ?? '—'}`,
                    item: q,
                })),
            });
        }
        return list;
    }, [activeDatasources, workerPools, jmsQueues, t]);

    const selectedItems = React.useMemo(() => {
        const ids: string[] = [];
        for (const cat of categories) {
            const multi = cat.entities.length > 1;
            let allLayersSelected = true;

            for (const layer of cat.layers) {
                if (!enabledLayers.has(layer.id)) {
                    allLayersSelected = false;
                    continue;
                }
                if (!multi) {
                    ids.push(`layer:${layer.id}`);
                    continue;
                }
                const excluded = excludedEntitiesByLayer[layer.id];
                let selectedCount = 0;
                for (const ent of cat.entities) {
                    if (!excluded?.has(ent.name)) {
                        ids.push(`series:${layer.id}:${ent.name}`);
                        selectedCount += 1;
                    }
                }
                if (selectedCount === cat.entities.length) {
                    ids.push(`layer:${layer.id}`);
                } else {
                    allLayersSelected = false;
                }
            }

            if (allLayersSelected) {
                ids.push(`cat:${cat.key}`);
            }
        }
        return ids;
    }, [categories, enabledLayers, excludedEntitiesByLayer]);

    const handleSelectedItemsChange = React.useCallback(
        (_event: React.SyntheticEvent | null, itemIds: string[] | string | null) => {
            const nextSet = new Set(
                Array.isArray(itemIds) ? itemIds : itemIds ? [itemIds] : []
            );
            const nextEnabled = new Set<PerformanceLayerId>();
            const nextExcluded: Partial<Record<PerformanceLayerId, Set<string>>> = {};
            const visibleCategories = new Set(categories.map(c => c.key));

            // Preservar selecció de categories que ara mateix no són presents al DOM
            for (const layerId of enabledLayers) {
                const isInHiddenCategory =
                    (!visibleCategories.has('datasource') && DATASOURCE_LAYERS.some(l => l.id === layerId)) ||
                    (!visibleCategories.has('worker') && WORKER_LAYERS.some(l => l.id === layerId)) ||
                    (!visibleCategories.has('jms') && JMS_LAYERS.some(l => l.id === layerId));
                if (isInHiddenCategory) {
                    nextEnabled.add(layerId);
                    const prevExcl = excludedEntitiesByLayer[layerId];
                    if (prevExcl && prevExcl.size > 0) {
                        nextExcluded[layerId] = new Set(prevExcl);
                    }
                }
            }

            for (const cat of categories) {
                const multi = cat.entities.length > 1;
                for (const layer of cat.layers) {
                    const hasLayerId = nextSet.has(`layer:${layer.id}`);
                    if (!multi) {
                        if (hasLayerId) {
                            nextEnabled.add(layer.id);
                        }
                        continue;
                    }

                    const wasEnabled = enabledLayers.has(layer.id);
                    const prevExcl = excludedEntitiesByLayer[layer.id];
                    const wasAllSelected = wasEnabled && (!prevExcl || prevExcl.size === 0);
                    const selectedEntities = cat.entities.filter(ent =>
                        nextSet.has(`series:${layer.id}:${ent.name}`)
                    );

                    // Quan un node pare/capa està plegat, MUI X TreeView no té els fills desmuntats
                    // al registre intern i propaga només el node `layer:*`.
                    if (!wasAllSelected && hasLayerId) {
                        nextEnabled.add(layer.id);
                        continue;
                    }
                    if (
                        wasAllSelected &&
                        !hasLayerId &&
                        (selectedEntities.length === 0 || selectedEntities.length === cat.entities.length)
                    ) {
                        continue;
                    }

                    if (selectedEntities.length > 0) {
                        nextEnabled.add(layer.id);
                        if (selectedEntities.length < cat.entities.length) {
                            nextExcluded[layer.id] = new Set(
                                cat.entities
                                    .filter(ent => !nextSet.has(`series:${layer.id}:${ent.name}`))
                                    .map(ent => ent.name)
                            );
                        }
                    }
                }
            }

            onSelectionChange(nextEnabled, nextExcluded);
        },
        [categories, enabledLayers, excludedEntitiesByLayer, onSelectionChange]
    );

    const handleExpandedItemsChange = React.useCallback(
        (_event: React.SyntheticEvent | null, itemIds: string[]) => {
            setExpandedItems(Array.isArray(itemIds) ? itemIds : []);
        },
        []
    );

    return (
        <Box
            data-testid="metrics-tree-sidebar"
            sx={{
                display: 'flex',
                flexDirection: 'column',
                flex: 1,
                height: '100%',
                minHeight: 0,
            }}
        >
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    gap: 0.5,
                    mb: 1,
                    flexWrap: 'wrap',
                    flexShrink: 0,
                }}
            >
                <Typography variant="subtitle2" fontWeight="bold">
                    {t($ => $.page.rendiment.chart.layersSidebarTitle)}
                </Typography>
                <Stack direction="row" spacing={0.25}>
                    <Button
                        size="small"
                        variant="text"
                        onClick={onResetDefaultLayers}
                        data-testid="layers-reset-default-button"
                        sx={{ minWidth: 'auto', px: 0.75, fontSize: '0.72rem' }}
                    >
                        {t($ => $.page.rendiment.chart.resetDefaultLayers)}
                    </Button>
                    <Button
                        size="small"
                        variant="text"
                        onClick={onSelectAllLayers}
                        data-testid="layers-select-all-button"
                        sx={{ minWidth: 'auto', px: 0.75, fontSize: '0.72rem' }}
                    >
                        {t($ => $.page.rendiment.chart.selectAllLayers)}
                    </Button>
                    <Button
                        size="small"
                        variant="text"
                        onClick={onClearAllLayers}
                        data-testid="layers-clear-all-button"
                        sx={{ minWidth: 'auto', px: 0.75, fontSize: '0.72rem' }}
                    >
                        {t($ => $.page.rendiment.chart.clearAllLayers)}
                    </Button>
                </Stack>
            </Box>

            <SimpleTreeView
                checkboxSelection
                multiSelect
                itemChildrenIndentation={16}
                selectionPropagation={{ parents: true, descendants: true }}
                selectedItems={selectedItems}
                onSelectedItemsChange={handleSelectedItemsChange}
                expandedItems={expandedItems}
                onExpandedItemsChange={handleExpandedItemsChange}
                sx={{
                    flex: { xs: '0 0 auto', md: 1 },
                    minHeight: { xs: 180, md: 0 },
                    maxHeight: { xs: 360, md: 'none' },
                    overflowY: 'auto',
                    pr: 0.5,
                    '& .MuiTreeItem-content': {
                        py: 0.25,
                        px: 0.5,
                        borderRadius: 1,
                        '&[data-selected], &.Mui-selected': {
                            backgroundColor: 'transparent',
                        },
                        '&[data-selected][data-focused], &.Mui-selected.Mui-focused': {
                            backgroundColor: theme => theme.palette.action.focus,
                        },
                        '&[data-selected]:hover, &.Mui-selected:hover': {
                            backgroundColor: theme => theme.palette.action.hover,
                            '@media (hover: none)': {
                                backgroundColor: 'transparent',
                            },
                        },
                    },
                    '& .MuiTreeItem-groupTransition': {
                        ml: 1.5,
                        borderLeft: theme => `1px dashed ${theme.palette.divider}`,
                    },
                    '& .MuiTreeItem-label': {
                        flex: 1,
                        minWidth: 0,
                        overflow: 'hidden',
                    },
                }}
            >
                {categories.map(cat => {
                    const multi = cat.entities.length > 1;
                    const activeLayerCount = cat.layers.filter(l => enabledLayers.has(l.id)).length;
                    const singleEntity = cat.entities.length === 1 ? cat.entities[0] : null;
                    const catColorMap = entityColorMaps[cat.key] ?? {};

                    const categoryHeader = (
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'space-between',
                                gap: 1,
                                width: '100%',
                                minWidth: 0,
                            }}
                        >
                            <Typography variant="body2" fontWeight={600} noWrap>
                                {cat.title}
                                {singleEntity ? ` · ${singleEntity.name}` : ''}
                            </Typography>
                            <Chip
                                size="small"
                                label={multi ? `${activeLayerCount}/${cat.layers.length} · ${cat.entities.length}` : `${activeLayerCount}/${cat.layers.length}`}
                                sx={{ height: 18, fontSize: '0.68rem', '& .MuiChip-label': { px: 0.75 } }}
                            />
                        </Box>
                    );

                    return (
                        <TreeItem
                            key={`cat:${cat.key}`}
                            itemId={`cat:${cat.key}`}
                            data-testid={`tree-cat-${cat.key}`}
                            label={
                                singleEntity ? (
                                    <Tooltip title={singleEntity.tooltip} arrow placement="left">
                                        {categoryHeader}
                                    </Tooltip>
                                ) : (
                                    categoryHeader
                                )
                            }
                        >
                            {cat.layers.map(layer => {
                                const isEnabled = enabledLayers.has(layer.id);
                                const excluded = excludedEntitiesByLayer[layer.id];
                                const activeEntities = isEnabled
                                    ? cat.entities.filter(e => !excluded?.has(e.name))
                                    : [];
                                const live = formatLayerLiveValue(
                                    cat.key,
                                    layer.id,
                                    isEnabled ? activeEntities : cat.entities
                                );
                                const isSplit = splitLayers.has(layer.id);
                                const splitTooltip = isSplit
                                    ? t($ => $.page.rendiment.chart.aggregateMetricTooltip)
                                    : t($ => $.page.rendiment.chart.splitMetricTooltip);
                                const layerColor = colors[layer.id];
                                const warnValue = isEnabled && isBacklogAlertLayer(layer.id) && (live.numeric ?? 0) > 0;

                                return (
                                    <TreeItem
                                        key={`layer:${layer.id}`}
                                        itemId={`layer:${layer.id}`}
                                        data-testid={`tree-layer-${layer.id}`}
                                        label={
                                            <Box
                                                sx={{
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    justifyContent: 'space-between',
                                                    gap: 0.75,
                                                    width: '100%',
                                                    minWidth: 0,
                                                }}
                                            >
                                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, minWidth: 0, flex: 1 }}>
                                                    <Box
                                                        component="span"
                                                        sx={{
                                                            width: 9,
                                                            height: 9,
                                                            borderRadius: '50%',
                                                            bgcolor: layerColor,
                                                            opacity: isEnabled ? 1 : 0.35,
                                                            flexShrink: 0,
                                                        }}
                                                    />
                                                    <Typography
                                                        variant="body2"
                                                        noWrap
                                                        color={isEnabled ? 'text.primary' : 'text.secondary'}
                                                        sx={{ fontSize: '0.8125rem' }}
                                                    >
                                                        {labels[layer.id]}
                                                    </Typography>
                                                </Box>
                                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, flexShrink: 0 }}>
                                                    {multi && (
                                                        <Tooltip title={splitTooltip} arrow>
                                                            <Box
                                                                component="span"
                                                                role="button"
                                                                tabIndex={0}
                                                                aria-label={splitTooltip}
                                                                data-testid={`split-toggle-${layer.id}`}
                                                                onMouseDown={e => e.stopPropagation()}
                                                                onClick={e => {
                                                                    e.stopPropagation();
                                                                    onToggleSplitLayer(layer.id);
                                                                }}
                                                                onKeyDown={e => {
                                                                    if (e.key === 'Enter' || e.key === ' ') {
                                                                        e.preventDefault();
                                                                        e.stopPropagation();
                                                                        onToggleSplitLayer(layer.id);
                                                                    }
                                                                }}
                                                                sx={{
                                                                    display: 'inline-flex',
                                                                    alignItems: 'center',
                                                                    justifyContent: 'center',
                                                                    width: 20,
                                                                    height: 18,
                                                                    borderRadius: 0.75,
                                                                    border: 1,
                                                                    borderColor: 'divider',
                                                                    bgcolor: 'transparent',
                                                                    color: 'text.secondary',
                                                                    fontSize: '0.72rem',
                                                                    fontWeight: 700,
                                                                    lineHeight: 1,
                                                                    cursor: 'pointer',
                                                                    userSelect: 'none',
                                                                    '&:hover': {
                                                                        borderColor: 'text.secondary',
                                                                        bgcolor: theme => theme.palette.action.hover,
                                                                    },
                                                                }}
                                                            >
                                                                {isSplit ? '≡' : 'Σ'}
                                                            </Box>
                                                        </Tooltip>
                                                    )}
                                                    <Typography
                                                        variant="caption"
                                                        color={warnValue ? 'warning.main' : 'text.secondary'}
                                                        fontWeight={warnValue ? 700 : 400}
                                                        sx={{ fontVariantNumeric: 'tabular-nums', minWidth: 24, textAlign: 'right' }}
                                                    >
                                                        {live.text}
                                                    </Typography>
                                                </Box>
                                            </Box>
                                        }
                                    >
                                        {multi &&
                                            cat.entities.map((ent, idx) => {
                                                const entSelected = isEnabled && !excluded?.has(ent.name);
                                                const entColor =
                                                    catColorMap[ent.name] ??
                                                    WORKER_QUEUE_PALETTE[idx % WORKER_QUEUE_PALETTE.length];
                                                const entLive = formatLayerLiveValue(cat.key, layer.id, [ent]);
                                                const warnEnt = entSelected && isBacklogAlertLayer(layer.id) && (entLive.numeric ?? 0) > 0;

                                                return (
                                                    <TreeItem
                                                        key={`series:${layer.id}:${ent.name}`}
                                                        itemId={`series:${layer.id}:${ent.name}`}
                                                        data-testid={`tree-series-${layer.id}-${ent.name}`}
                                                        label={
                                                            <Tooltip title={ent.tooltip} arrow placement="left">
                                                                <Box
                                                                    sx={{
                                                                        display: 'flex',
                                                                        alignItems: 'center',
                                                                        justifyContent: 'space-between',
                                                                        gap: 0.75,
                                                                        width: '100%',
                                                                        minWidth: 0,
                                                                    }}
                                                                >
                                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, minWidth: 0, flex: 1 }}>
                                                                        {isSplit && (
                                                                            <Box
                                                                                component="span"
                                                                                sx={{
                                                                                    width: 8,
                                                                                    height: 8,
                                                                                    borderRadius: '50%',
                                                                                    bgcolor: entColor,
                                                                                    opacity: entSelected ? 1 : 0.35,
                                                                                    flexShrink: 0,
                                                                                }}
                                                                            />
                                                                        )}
                                                                        <Typography
                                                                            variant="caption"
                                                                            noWrap
                                                                            color={entSelected ? 'text.primary' : 'text.secondary'}
                                                                        >
                                                                            {ent.name}
                                                                        </Typography>
                                                                    </Box>
                                                                    <Typography
                                                                        variant="caption"
                                                                        color={warnEnt ? 'warning.main' : 'text.secondary'}
                                                                        fontWeight={warnEnt ? 700 : 400}
                                                                        sx={{ fontVariantNumeric: 'tabular-nums', flexShrink: 0 }}
                                                                    >
                                                                        {entLive.text}
                                                                    </Typography>
                                                                </Box>
                                                            </Tooltip>
                                                        }
                                                    />
                                                );
                                            })}
                                    </TreeItem>
                                );
                            })}
                        </TreeItem>
                    );
                })}
            </SimpleTreeView>

            {disabledDatasources.length > 0 && (
                <Box
                    sx={{ mt: 1.5, pt: 1, borderTop: 1, borderColor: 'divider', flexShrink: 0 }}
                    data-testid="disabled-datasources-note"
                >
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontWeight: 600 }}>
                        {t($ => $.page.rendiment.chart.disabledDatasourcesNote)}:
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                        {disabledDatasources
                            .map(ds => `${ds.poolName} (${ds.minSize ?? '—'}–${ds.maxSize ?? '—'})`)
                            .join(', ')}
                    </Typography>
                </Box>
            )}
        </Box>
    );
};

export default React.memo(MetricsTreeSidebar);
