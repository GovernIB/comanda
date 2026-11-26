import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    Alert,
    Box,
    Button,
    Chip,
    Icon,
    IconButton,
    Paper,
    Stack,
    ToggleButton,
    ToggleButtonGroup,
    Tooltip,
    Typography,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import {
    AreaPlot,
    BarPlot,
    ChartsAxisHighlight,
    ChartsBrushOverlay,
    ChartsDataProvider,
    ChartsGrid,
    ChartsSurface,
    ChartsTooltip,
    ChartsXAxis,
    ChartsYAxis,
    LinePlot,
    MarkPlot,
    useBrush,
    useDrawingArea,
    YAxis,
} from '@mui/x-charts';
import {
    DatasourcePoolMetrics,
    JmsQueueMetrics,
    TimeWindowSeconds,
    WorkerPoolMetrics,
    ZoomTimeRange,
} from './types';
import {
    buildComposableChartModel,
    CategoryPrefixMap,
    ComposableChartModel,
    DATASOURCE_LAYERS,
    formatBucketResolution,
    JMS_LAYERS,
    LayerColorMap,
    LayerLabelMap,
    MetricLayerCategory,
    MetricLayerDefinition,
    pctRangeToTimeRange,
    PerformanceLayerId,
    resolveTimeDomain,
    TIME_WINDOW_OPTIONS,
    WaitCountVariant,
    WORKER_LAYERS,
} from './chartModel';
import MetricsTreeSidebar from './MetricsTreeSidebar';
import ZoomPreviewSlider from './ZoomPreviewSlider';
import { MUI_AXIS_WORKAROUND_HEIGHT } from '../../util/muiWorkarounds';

const BRUSH_CONFIG = {
    enabled: true,
    preventTooltip: true,
    preventHighlight: true,
} as const;

const BrushZoomListener: React.FC<{
    timestamps: string[];
    bucketSeconds?: number;
    rangeStart?: string | null;
    rangeEnd?: string | null;
    collectedAt?: string | null;
    onBrushSelect: (range: ZoomTimeRange) => void;
}> = ({ timestamps, bucketSeconds, rangeStart, rangeEnd, collectedAt, onBrushSelect }) => {
    const brush = useBrush();
    const drawingArea = useDrawingArea();
    const lastBrushRef = React.useRef<{ startX: number; currentX: number } | null>(null);
    const onBrushSelectRef = React.useRef(onBrushSelect);

    const mainDomain = React.useMemo(
        () =>
            resolveTimeDomain(timestamps, {
                bucketSeconds,
                windowStart: rangeStart,
                windowEnd: rangeEnd,
                fallbackEnd: collectedAt,
            }),
        [timestamps, bucketSeconds, rangeStart, rangeEnd, collectedAt]
    );

    React.useEffect(() => {
        onBrushSelectRef.current = onBrushSelect;
    }, [onBrushSelect]);

    React.useEffect(() => {
        if (brush) {
            lastBrushRef.current = {
                startX: brush.start.x,
                currentX: brush.current.x,
            };
            return;
        }
        const active = lastBrushRef.current;
        lastBrushRef.current = null;
        if (!active || drawingArea.width <= 0 || timestamps.length < 2) {
            return;
        }
        if (Math.abs(active.currentX - active.startX) < 8) {
            return;
        }
        const minX = Math.min(active.startX, active.currentX);
        const maxX = Math.max(active.startX, active.currentX);
        const startPct = Math.max(0, Math.min(100, ((minX - drawingArea.left) / drawingArea.width) * 100));
        const endPct = Math.max(0, Math.min(100, ((maxX - drawingArea.left) / drawingArea.width) * 100));
        const nextRange = pctRangeToTimeRange(timestamps, startPct, endPct, mainDomain);
        if (nextRange) {
            onBrushSelectRef.current(nextRange);
        }
    }, [brush, drawingArea.left, drawingArea.width, timestamps, mainDomain]);

    return null;
};

export interface PerformanceComposableChartProps {
    datasources: DatasourcePoolMetrics[];
    workerPools?: WorkerPoolMetrics[];
    jmsQueues?: JmsQueueMetrics[];
    previewDatasources?: DatasourcePoolMetrics[];
    previewWorkerPools?: WorkerPoolMetrics[];
    previewJmsQueues?: JmsQueueMetrics[];
    enabledLayers: Set<PerformanceLayerId>;
    excludedEntitiesByLayer: Partial<Record<PerformanceLayerId, ReadonlySet<string>>>;
    splitLayers: Set<PerformanceLayerId>;
    entityColorMaps: Record<MetricLayerCategory, Record<string, string>>;
    onSelectionChange: (
        nextEnabled: Set<PerformanceLayerId>,
        nextExcluded: Partial<Record<PerformanceLayerId, Set<string>>>
    ) => void;
    onToggleSplitLayer: (layerId: PerformanceLayerId) => void;
    onSelectAllLayers: () => void;
    onResetDefaultLayers: () => void;
    onClearAllLayers: () => void;
    windowSeconds: TimeWindowSeconds;
    onWindowChange: (value: TimeWindowSeconds) => void;
    waitCountVariant: WaitCountVariant;
    bucketSeconds: number;
    previewBucketSeconds?: number;
    windowStart?: string | null;
    windowEnd?: string | null;
    collectedAt?: string | null;
    rangeStart?: string | null;
    rangeEnd?: string | null;
    zoomRange: ZoomTimeRange | null;
    onZoomChange: (range: ZoomTimeRange | null) => void;
    onResetZoom: () => void;
}

const MainChartCanvas = React.memo<{
    model: ComposableChartModel;
    yAxis: YAxis[];
    surfaceSx: Record<string, unknown>;
    hasZoom: boolean;
    bucketSeconds: number;
    rangeStart?: string | null;
    rangeEnd?: string | null;
    collectedAt?: string | null;
    onZoomChange: (range: ZoomTimeRange | null) => void;
    onResetZoom: () => void;
}>(({ model, yAxis, surfaceSx, hasZoom, bucketSeconds, rangeStart, rangeEnd, collectedAt, onZoomChange, onResetZoom }) => {
    const xAxis = React.useMemo(
        () => [
            {
                id: 'time',
                scaleType: 'band' as const,
                data: model.xLabels,
                height: MUI_AXIS_WORKAROUND_HEIGHT,
            },
        ],
        [model.xLabels]
    );

    const margin = React.useMemo(
        () => ({
            top: 12,
            right: model.hasCountAxis && model.hasDurationAxis ? 4 : 12,
            bottom: 8,
            left: 4,
        }),
        [model.hasCountAxis, model.hasDurationAxis]
    );

    const containerRef = React.useRef<HTMLDivElement | null>(null);
    const [containerSize, setContainerSize] = React.useState<{ width: number; height: number }>({
        width: 0,
        height: 0,
    });

    React.useLayoutEffect(() => {
        const el = containerRef.current;
        if (!el) {
            return;
        }
        const updateSize = () => {
            const nextWidth = Math.floor(el.clientWidth);
            const nextHeight = Math.floor(el.clientHeight);
            setContainerSize(prev =>
                prev.width === nextWidth && prev.height === nextHeight
                    ? prev
                    : { width: nextWidth, height: nextHeight }
            );
        };
        updateSize();
        if (typeof ResizeObserver === 'undefined') {
            return;
        }
        const observer = new ResizeObserver(updateSize);
        observer.observe(el);
        return () => observer.disconnect();
    }, []);

    const handleDoubleClick = React.useCallback(() => {
        if (hasZoom) {
            onResetZoom();
        }
    }, [hasZoom, onResetZoom]);

    return (
        <Box
            ref={containerRef}
            sx={{
                position: 'relative',
                width: '100%',
                flex: 1,
                minHeight: 250,
            }}
            data-testid="rendiment-chart-box"
            onDoubleClick={handleDoubleClick}
        >
            <Box
                sx={{
                    position: 'absolute',
                    inset: 0,
                    overflow: 'hidden',
                }}
            >
                <ChartsDataProvider<'bar' | 'line'>
                    series={model.series}
                    xAxis={xAxis}
                    yAxis={yAxis}
                    width={containerSize.width > 0 ? containerSize.width : undefined}
                    height={containerSize.height > 0 ? containerSize.height : undefined}
                    skipAnimation
                    brushConfig={BRUSH_CONFIG}
                    margin={margin}
                >
                    <ChartsSurface sx={surfaceSx}>
                        <ChartsGrid horizontal />
                        <AreaPlot />
                        <BarPlot renderer="svg-batch" />
                        <LinePlot />
                        {model.timestamps.length <= 25 && <MarkPlot />}
                        <ChartsAxisHighlight x="band" />
                        <ChartsXAxis axisId="time" />
                        {model.hasCountAxis && <ChartsYAxis axisId="count" />}
                        {model.hasDurationAxis && <ChartsYAxis axisId="durationMs" />}
                        <ChartsBrushOverlay />
                        <BrushZoomListener
                            timestamps={model.timestamps}
                            bucketSeconds={bucketSeconds}
                            rangeStart={rangeStart}
                            rangeEnd={rangeEnd}
                            collectedAt={collectedAt}
                            onBrushSelect={onZoomChange}
                        />
                    </ChartsSurface>
                    <ChartsTooltip />
                </ChartsDataProvider>
            </Box>
        </Box>
    );
});

const PerformanceComposableChart: React.FC<PerformanceComposableChartProps> = ({
    datasources,
    workerPools = [],
    jmsQueues = [],
    previewDatasources,
    previewWorkerPools,
    previewJmsQueues,
    enabledLayers,
    excludedEntitiesByLayer,
    splitLayers,
    entityColorMaps,
    onSelectionChange,
    onToggleSplitLayer,
    onSelectAllLayers,
    onResetDefaultLayers,
    onClearAllLayers,
    windowSeconds,
    onWindowChange,
    waitCountVariant,
    bucketSeconds,
    previewBucketSeconds,
    windowStart,
    windowEnd,
    collectedAt,
    rangeStart,
    rangeEnd,
    zoomRange,
    onZoomChange,
    onResetZoom,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();
    const [sidebarOpen, setSidebarOpen] = React.useState<boolean>(true);
    const primaryColor = theme.palette.primary.main;
    const infoColor = theme.palette.info.main;
    const errorColor = theme.palette.error.main;
    const warningColor = theme.palette.warning.main;
    const secondaryColor = theme.palette.secondary.main;
    const textSecondaryColor = theme.palette.text.secondary;
    const successColor = theme.palette.success.main;

    const colors: LayerColorMap = React.useMemo(
        () => ({
            inUseCount: primaryColor,
            activeCount: infoColor,
            waitCount: errorColor,
            maxWaitTimeMs: warningColor,
            maxSize: secondaryColor,
            minSize: textSecondaryColor,
            workerActiveCount: successColor,
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
        }),
        [
            primaryColor,
            infoColor,
            errorColor,
            warningColor,
            secondaryColor,
            textSecondaryColor,
            successColor,
        ]
    );

    const waitCountLabel = waitCountVariant === 'INSTANTANEOUS'
        ? t($ => $.page.rendiment.layers.waitCountInstantaneous)
        : waitCountVariant === 'MIXED'
            ? t($ => $.page.rendiment.layers.waitCountMixed)
            : t($ => $.page.rendiment.layers.waitCountDelta);

    const labels: LayerLabelMap = React.useMemo(
        () => ({
            inUseCount: t($ => $.page.rendiment.layers.inUseCount),
            activeCount: t($ => $.page.rendiment.layers.activeCount),
            waitCount: waitCountLabel,
            maxWaitTimeMs: t($ => $.page.rendiment.layers.maxWaitTimeMs),
            maxSize: t($ => $.page.rendiment.layers.maxSize),
            minSize: t($ => $.page.rendiment.layers.minSize),
            workerActiveCount: t($ => $.page.rendiment.layers.workerActiveCount),
            workerQueueSize: t($ => $.page.rendiment.layers.workerQueueSize),
            workerPoolSize: t($ => $.page.rendiment.layers.workerPoolSize),
            workerCompletedTasks: t($ => $.page.rendiment.layers.workerCompletedTasks),
            workerMaxSize: t($ => $.page.rendiment.layers.workerMaxSize),
            workerCoreSize: t($ => $.page.rendiment.layers.workerCoreSize),
            jmsDeliveringCount: t($ => $.page.rendiment.layers.jmsDeliveringCount),
            jmsPendingCount: t($ => $.page.rendiment.layers.jmsPendingCount),
            jmsConsumerCount: t($ => $.page.rendiment.layers.jmsConsumerCount),
            jmsCompletedMessages: t($ => $.page.rendiment.layers.jmsCompletedMessages),
            jmsAddedMessages: t($ => $.page.rendiment.layers.jmsAddedMessages),
            jmsMaxConsumers: t($ => $.page.rendiment.layers.jmsMaxConsumers),
        }),
        [t, waitCountLabel]
    );

    const categoryPrefixes: CategoryPrefixMap = React.useMemo(
        () => ({
            datasource: t($ => $.page.rendiment.categories.datasourceShort),
            worker: t($ => $.page.rendiment.categories.workerShort),
            jms: t($ => $.page.rendiment.categories.jmsShort),
        }),
        [t]
    );

    const activeDatasources = React.useMemo(
        () => datasources.filter(ds => ds.statisticsEnabled),
        [datasources]
    );

    const activePreviewDatasources = React.useMemo(
        () => (previewDatasources ?? datasources).filter(ds => ds.statisticsEnabled),
        [previewDatasources, datasources]
    );

    const hasApplicableLayers = React.useMemo(() => {
        const activeLayerGroups: Array<readonly MetricLayerDefinition[]> = [];
        if (activeDatasources.length > 0) activeLayerGroups.push(DATASOURCE_LAYERS);
        if (workerPools.length > 0) activeLayerGroups.push(WORKER_LAYERS);
        if (jmsQueues.length > 0) activeLayerGroups.push(JMS_LAYERS);
        return activeLayerGroups.some(group => group.some(layer => enabledLayers.has(layer.id)));
    }, [activeDatasources.length, workerPools.length, jmsQueues.length, enabledLayers]);

    const model = React.useMemo(
        () =>
            buildComposableChartModel({
                datasources: activeDatasources,
                workerPools,
                jmsQueues,
                enabledLayers,
                labels,
                categoryPrefixes,
                colors,
                splitLayers,
                excludedEntitiesByLayer,
                entityColorMaps,
            }),
        [
            activeDatasources,
            workerPools,
            jmsQueues,
            enabledLayers,
            labels,
            categoryPrefixes,
            colors,
            splitLayers,
            excludedEntitiesByLayer,
            entityColorMaps,
        ]
    );

    const countAxisLabel = React.useMemo(() => {
        if (model.activeCategories.size > 1) {
            return t($ => $.page.rendiment.chart.combinedCountAxis);
        }
        if (model.activeCategories.has('worker')) {
            return t($ => $.page.rendiment.chart.workersAxis);
        }
        if (model.activeCategories.has('jms')) {
            return t($ => $.page.rendiment.chart.jmsAxis);
        }
        return t($ => $.page.rendiment.chart.connectionsAxis);
    }, [model.activeCategories, t]);

    const yAxis: YAxis[] = React.useMemo(() => {
        const axes: YAxis[] = [];
        if (model.hasCountAxis) {
            axes.push({
                id: 'count',
                position: 'left',
                min: 0,
                label: countAxisLabel,
            });
        }
        if (model.hasDurationAxis) {
            axes.push({
                id: 'durationMs',
                position: model.hasCountAxis ? 'right' : 'left',
                min: 0,
                label: 'ms',
                valueFormatter: (v: number) => `${v} ms`,
            });
        }
        return axes;
    }, [model.hasCountAxis, model.hasDurationAxis, countAxisLabel]);

    const surfaceSx = React.useMemo(() => {
        const styles: Record<string, unknown> = {
            width: '100%',
            height: '100%',
            display: 'block',
            cursor: model.timestamps.length >= 2 ? 'crosshair' : 'default',
            '& .MuiLineChart-area, & .MuiAreaElement-root': {
                fillOpacity: 0.16,
            },
            '& .MuiBar-element, & .MuiBarElement-root, & .MuiBar-series path': {
                fillOpacity: 0.72,
            },
        };
        for (const id of model.dashedSeriesIds) {
            styles[`& [data-series-id="${id}"]`] = {
                strokeDasharray: '6 4',
                strokeWidth: 1.75,
            };
        }
        for (const id of model.dottedSeriesIds) {
            styles[`& [data-series-id="${id}"]`] = {
                strokeDasharray: '3 3',
                strokeWidth: 1.5,
            };
        }
        return styles;
    }, [model.dashedSeriesIds, model.dottedSeriesIds, model.timestamps.length]);

    return (
        <Paper
            variant="outlined"
            sx={{
                display: 'flex',
                flexDirection: { xs: 'column', md: 'row' },
                alignItems: 'stretch',
                flex: 1,
                minHeight: { xs: 560, md: 440 },
                overflow: { xs: 'visible', md: 'hidden' },
            }}
        >
            {/* Primer contenidor (esquerra): dividit verticalment en títol i gràfic */}
            <Box
                sx={{
                    flex: 1,
                    minWidth: 0,
                    minHeight: 0,
                    display: 'flex',
                    flexDirection: 'column',
                    p: 2.5,
                }}
            >
                <Box
                    sx={{
                        display: 'flex',
                        flexWrap: 'wrap',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: 1.5,
                        mb: 2,
                        flexShrink: 0,
                    }}
                >
                    <Box>
                        <Typography variant="subtitle1" fontWeight="bold">
                            {t($ => $.page.rendiment.chart.title)}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                            {t($ => $.page.rendiment.chart.zoomHint)}
                        </Typography>
                    </Box>
                    <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap" useFlexGap>
                        <Chip
                            size="small"
                            variant="outlined"
                            color="info"
                            label={`${t($ => $.page.rendiment.chart.resolution)}: ${formatBucketResolution(bucketSeconds)}`}
                        />
                        {zoomRange && (
                            <>
                                <Chip
                                    size="small"
                                    color="primary"
                                    label={t($ => $.page.rendiment.chart.zoomActive)}
                                />
                                <Button size="small" variant="outlined" onClick={onResetZoom}>
                                    {t($ => $.page.rendiment.chart.resetZoom)}
                                </Button>
                            </>
                        )}
                        <ToggleButtonGroup
                            size="small"
                            exclusive
                            value={windowSeconds}
                            onChange={(_e, value: TimeWindowSeconds | null) => {
                                if (value) {
                                    onWindowChange(value);
                                }
                            }}
                            aria-label={t($ => $.page.rendiment.window.label)}
                        >
                            {TIME_WINDOW_OPTIONS.map(opt => (
                                <ToggleButton
                                    key={opt.seconds}
                                    value={opt.seconds}
                                    data-testid={`window-option-${opt.label}`}
                                >
                                    {opt.label}
                                </ToggleButton>
                            ))}
                        </ToggleButtonGroup>
                        <Tooltip title={t($ => $.page.rendiment.chart.toggleSidebar)} arrow>
                            <IconButton
                                size="small"
                                aria-label={t($ => $.page.rendiment.chart.toggleSidebar)}
                                aria-expanded={sidebarOpen}
                                data-testid="toggle-sidebar-button"
                                onClick={() => setSidebarOpen(prev => !prev)}
                            >
                                <Icon>{sidebarOpen ? 'chevron_right' : 'layers'}</Icon>
                            </IconButton>
                        </Tooltip>
                    </Stack>
                </Box>

                <Box sx={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
                    {!hasApplicableLayers ? (
                        <Alert severity="info">{t($ => $.page.rendiment.chart.noLayersSelected)}</Alert>
                    ) : model.timestamps.length === 0 ? (
                        <Alert severity="info">{t($ => $.page.rendiment.chart.noHistory)}</Alert>
                    ) : (
                        <>
                            {!sidebarOpen && model.series.length > 0 && (
                                <Box
                                    data-testid="collapsed-chart-legend"
                                    sx={{
                                        display: 'flex',
                                        flexWrap: 'wrap',
                                        alignItems: 'center',
                                        columnGap: 2,
                                        rowGap: 0.75,
                                        mb: 1,
                                        flexShrink: 0,
                                    }}
                                >
                                    {model.series.map(s => {
                                        const seriesId = String(s.id ?? '');
                                        const isDashed = model.dashedSeriesIds.includes(seriesId);
                                        const isDotted = model.dottedSeriesIds.includes(seriesId);
                                        const isBar = s.type === 'bar';
                                        return (
                                            <Box
                                                key={seriesId}
                                                sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.75, minWidth: 0 }}
                                            >
                                                {isBar ? (
                                                    <Box
                                                        component="span"
                                                        sx={{
                                                            width: 10,
                                                            height: 10,
                                                            borderRadius: 0.5,
                                                            bgcolor: s.color,
                                                            opacity: 0.72,
                                                            flexShrink: 0,
                                                        }}
                                                    />
                                                ) : isDashed || isDotted ? (
                                                    <Box
                                                        component="span"
                                                        sx={{
                                                            width: 14,
                                                            height: 0,
                                                            borderTop: 2,
                                                            borderTopStyle: isDashed ? 'dashed' : 'dotted',
                                                            borderColor: s.color,
                                                            flexShrink: 0,
                                                        }}
                                                    />
                                                ) : (
                                                    <Box
                                                        component="span"
                                                        sx={{
                                                            width: 10,
                                                            height: 10,
                                                            borderRadius: '50%',
                                                            bgcolor: s.color,
                                                            flexShrink: 0,
                                                        }}
                                                    />
                                                )}
                                                <Typography variant="caption" color="text.secondary" noWrap>
                                                    {typeof s.label === 'string' ? s.label : seriesId}
                                                </Typography>
                                            </Box>
                                        );
                                    })}
                                </Box>
                            )}

                            <MainChartCanvas
                                model={model}
                                yAxis={yAxis}
                                surfaceSx={surfaceSx}
                                hasZoom={Boolean(zoomRange)}
                                bucketSeconds={bucketSeconds}
                                rangeStart={rangeStart}
                                rangeEnd={rangeEnd}
                                collectedAt={collectedAt}
                                onZoomChange={onZoomChange}
                                onResetZoom={onResetZoom}
                            />

                            <ZoomPreviewSlider
                                datasources={activePreviewDatasources}
                                workerPools={previewWorkerPools ?? workerPools}
                                jmsQueues={previewJmsQueues ?? jmsQueues}
                                zoomRange={zoomRange}
                                previewBucketSeconds={previewBucketSeconds}
                                windowStart={windowStart}
                                windowEnd={windowEnd}
                                collectedAt={collectedAt}
                                onZoomChange={onZoomChange}
                                onResetZoom={onResetZoom}
                            />
                        </>
                    )}
                </Box>
            </Box>

            {/* Segon contenidor (dreta): TreeView ocupant el 100% de l'altura del contenidor */}
            {sidebarOpen && (
                <Box
                    sx={{
                        width: { xs: '100%', md: 320 },
                        flexShrink: 0,
                        borderLeft: { md: 1 },
                        borderTop: { xs: 1, md: 0 },
                        borderColor: 'divider',
                        p: 2,
                        display: 'flex',
                        flexDirection: 'column',
                        minHeight: 0,
                    }}
                >
                    <MetricsTreeSidebar
                        datasources={datasources}
                        workerPools={workerPools}
                        jmsQueues={jmsQueues}
                        enabledLayers={enabledLayers}
                        excludedEntitiesByLayer={excludedEntitiesByLayer}
                        splitLayers={splitLayers}
                        labels={labels}
                        colors={colors}
                        entityColorMaps={entityColorMaps}
                        onSelectionChange={onSelectionChange}
                        onToggleSplitLayer={onToggleSplitLayer}
                        onResetDefaultLayers={onResetDefaultLayers}
                        onSelectAllLayers={onSelectAllLayers}
                        onClearAllLayers={onClearAllLayers}
                    />
                </Box>
            )}
        </Paper>
    );
};

export default React.memo(PerformanceComposableChart);
