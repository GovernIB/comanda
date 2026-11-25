import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    Alert,
    AlertTitle,
    Box,
    Button,
    Chip,
    CircularProgress,
    FormControlLabel,
    Icon,
    IconButton,
    Paper,
    Switch,
    Toolbar,
    Tooltip,
    Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { useTheme } from '@mui/material/styles';
import { useResourceApiContext } from 'reactlib';
import PageTitle from '../../components/PageTitle.tsx';
import useInterval from '../../hooks/useInterval';
import { iniciaDescargaBlob } from '../../util/commonsActions';
import { buildHref } from '../../util/requestUtils';
import {
    DatasourcePoolMetrics,
    JmsQueueMetrics,
    PerformanceMetricsResponse,
    TimeWindowSeconds,
    WorkerPoolMetrics,
    ZoomTimeRange,
} from './types';
import {
    buildImportedMetricsView,
    chooseInitialWindowForSnapshot,
    extractExportFilename,
    parsePerformanceMetricsJson,
} from './historySnapshot';
import {
    ALL_LAYERS,
    buildEntityColorMap,
    computeKpiSummary,
    computeWorkerKpiSummary,
    createDefaultEnabledLayers,
    createDefaultSplitLayers,
    getAutoRefreshIntervalMs,
    MetricLayerCategory,
    PerformanceLayerId,
    resolveWaitCountVariant,
} from './chartModel';
import PerformanceComposableChart from './PerformanceComposableChart';

const DEFAULT_MAX_POINTS = 180;

const formatNumber = (n: number): string => n.toLocaleString('ca-ES');

const formatMemoryUpperBound = (bytes: number): string => {
    if (bytes <= 0) {
        return '0 B';
    }
    if (bytes < 1024) {
        return `≤ ${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
        return `≤ ${(bytes / 1024).toFixed(1)} KB`;
    }
    return `≤ ${(bytes / (1024 * 1024)).toFixed(2)} MB`;
};

const readFileAsText = (file: File): Promise<string> => {
    if (typeof file.text === 'function') {
        return file.text();
    }
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '');
        reader.onerror = () => reject(reader.error ?? new Error('File read error'));
        reader.readAsText(file);
    });
};

interface KpiCardProps {
    icon: string;
    label: string;
    value: string;
    caption?: string;
    tooltip: string;
    color?: 'success' | 'warning' | 'error' | 'info' | 'primary';
    unavailable?: boolean;
}

const KpiCard: React.FC<KpiCardProps> = ({
    icon,
    label,
    value,
    caption,
    tooltip,
    color = 'primary',
    unavailable = false,
}) => (
    <Tooltip title={tooltip} arrow placement="top">
        <Paper
            elevation={2}
            sx={{
                p: 2.5,
                width: '100%',
                height: '100%',
                boxSizing: 'border-box',
                display: 'flex',
                alignItems: 'center',
                gap: 2,
                borderLeft: 4,
                cursor: 'help',
                borderColor: unavailable ? 'grey.400' : `${color}.main`,
            }}
        >
            <Icon sx={{ fontSize: 36, color: unavailable ? 'grey.400' : `${color}.main` }}>
                {icon}
            </Icon>
            <Box>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                    {label}
                </Typography>
                <Typography variant="h6" fontWeight="bold" color={unavailable ? 'text.disabled' : 'text.primary'}>
                    {unavailable ? '—' : value}
                </Typography>
                {caption && (
                    <Typography variant="caption" color="text.secondary">
                        {caption}
                    </Typography>
                )}
            </Box>
        </Paper>
    </Tooltip>
);

interface PreviewBaseline {
    datasources: DatasourcePoolMetrics[];
    workerPools: WorkerPoolMetrics[];
    jmsQueues: JmsQueueMetrics[];
    windowStart: string | null;
    windowEnd: string | null;
    collectedAt: string | null;
    previewBucketSeconds: number;
}

const Rendiment: React.FC = () => {
    const { t } = useTranslation();
    const { requestHref } = useResourceApiContext();
    const theme = useTheme();
    const toolbarBg = theme.palette.mode === 'light' ? theme.palette.grey[200] : theme.palette.grey[900];

    const [data, setData] = React.useState<PerformanceMetricsResponse | null>(null);
    const [loading, setLoading] = React.useState<boolean>(true);
    const [error, setError] = React.useState<string | null>(null);
    const [autoRefresh, setAutoRefresh] = React.useState<boolean>(true);
    const [windowSeconds, setWindowSeconds] = React.useState<TimeWindowSeconds>(300);
    const [zoomRange, setZoomRange] = React.useState<ZoomTimeRange | null>(null);
    const [frozenPreviewBaseline, setFrozenPreviewBaseline] = React.useState<PreviewBaseline | null>(
        null
    );
    const [dismissedDsStatsAlert, setDismissedDsStatsAlert] = React.useState<boolean>(false);
    const [exporting, setExporting] = React.useState<boolean>(false);
    const [importedSource, setImportedSource] = React.useState<{
        fileName: string;
        snapshot: PerformanceMetricsResponse;
    } | null>(null);
    const fileInputRef = React.useRef<HTMLInputElement | null>(null);
    const isImportedMode = importedSource !== null;

    const [enabledLayers, setEnabledLayers] = React.useState<Set<PerformanceLayerId>>(
        createDefaultEnabledLayers
    );
    const [excludedEntitiesByLayer, setExcludedEntitiesByLayer] = React.useState<
        Partial<Record<PerformanceLayerId, Set<string>>>
    >({});
    const [splitLayers, setSplitLayers] = React.useState<Set<PerformanceLayerId>>(
        createDefaultSplitLayers
    );
    const [isPageVisible, setIsPageVisible] = React.useState<boolean>(() =>
        typeof document === 'undefined' ? true : !document.hidden
    );
    const requestIdRef = React.useRef<number>(0);

    const fetchMetrics = React.useCallback(async () => {
        const reqId = ++requestIdRef.current;
        setError(null);
        const params = new URLSearchParams({
            windowSeconds: String(windowSeconds),
            maxPoints: String(DEFAULT_MAX_POINTS),
        });
        if (zoomRange) {
            params.set('from', zoomRange.from);
            params.set('to', zoomRange.to);
        }
        try {
            const response = await requestHref(
                buildHref(`performance-metrics?${params.toString()}`)
            );
            if (reqId !== requestIdRef.current) {
                return;
            }
            setData(response.data as PerformanceMetricsResponse);
        } catch (err) {
            if (reqId === requestIdRef.current) {
                setError(String(err));
            }
        } finally {
            if (reqId === requestIdRef.current) {
                setLoading(false);
            }
        }
    }, [requestHref, windowSeconds, zoomRange]);

    const visibleRefreshRef = React.useRef({
        autoRefresh,
        zoomRange,
        isImportedMode,
        fetchMetrics,
    });
    React.useEffect(() => {
        visibleRefreshRef.current = { autoRefresh, zoomRange, isImportedMode, fetchMetrics };
    }, [autoRefresh, zoomRange, isImportedMode, fetchMetrics]);

    React.useEffect(() => {
        if (typeof document === 'undefined') {
            return;
        }
        const handleVisibilityChange = () => {
            const visible = !document.hidden;
            setIsPageVisible(visible);
            if (
                visible &&
                visibleRefreshRef.current.autoRefresh &&
                !visibleRefreshRef.current.zoomRange &&
                !visibleRefreshRef.current.isImportedMode
            ) {
                void visibleRefreshRef.current.fetchMetrics();
            }
        };
        document.addEventListener('visibilitychange', handleVisibilityChange);
        return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
    }, []);

    React.useEffect(() => {
        if (isImportedMode) {
            return;
        }
        void fetchMetrics();
    }, [fetchMetrics, isImportedMode]);

    useInterval({
        tick: () => {
            void fetchMetrics();
        },
        timeout:
            autoRefresh && !zoomRange && !isImportedMode && isPageVisible
                ? getAutoRefreshIntervalMs(windowSeconds)
                : null,
    });

    const handleExportJson = React.useCallback(async () => {
        setExporting(true);
        setError(null);
        try {
            const response = await requestHref(buildHref('performance-metrics/export'));
            const blob =
                response.data instanceof Blob
                    ? response.data
                    : new Blob(
                          [
                              typeof response.data === 'string'
                                  ? response.data
                                  : JSON.stringify(response.data),
                          ],
                          { type: 'application/json' }
                      );
            const fileName = extractExportFilename(
                response.headers?.get?.('content-disposition') ?? null
            );
            iniciaDescargaBlob({ blob, fileName });
        } catch (err) {
            setError(`${t($ => $.page.rendiment.exportError)}: ${String(err)}`);
        } finally {
            setExporting(false);
        }
    }, [requestHref, t]);

    const handleImportFileChange = React.useCallback(
        async (event: React.ChangeEvent<HTMLInputElement>) => {
            const file = event.target.files?.[0];
            event.target.value = '';
            if (!file) {
                return;
            }
            try {
                const rawText = await readFileAsText(file);
                const result = parsePerformanceMetricsJson(rawText);
                if (!result.ok) {
                    setError(
                        result.reason === 'invalidJson'
                            ? t($ => $.page.rendiment.importInvalidJsonError)
                            : t($ => $.page.rendiment.importInvalidFormatError)
                    );
                    return;
                }
                requestIdRef.current += 1;
                setError(null);
                setDismissedDsStatsAlert(false);
                setLoading(false);
                setFrozenPreviewBaseline(null);
                setZoomRange(null);
                setWindowSeconds(chooseInitialWindowForSnapshot(result.data));
                setImportedSource({ fileName: file.name, snapshot: result.data });
            } catch (err) {
                setError(String(err));
            }
        },
        [t]
    );

    const handleExitImportedMode = React.useCallback(() => {
        setError(null);
        setDismissedDsStatsAlert(false);
        setFrozenPreviewBaseline(null);
        setZoomRange(null);
        setWindowSeconds(300);
        setImportedSource(null);
    }, []);

    const effectiveData = React.useMemo<PerformanceMetricsResponse | null>(() => {
        if (importedSource) {
            return buildImportedMetricsView(
                importedSource.snapshot,
                windowSeconds,
                zoomRange,
                DEFAULT_MAX_POINTS
            );
        }
        return data;
    }, [importedSource, windowSeconds, zoomRange, data]);

    const datasources: DatasourcePoolMetrics[] = React.useMemo(
        () => effectiveData?.datasources ?? [],
        [effectiveData]
    );
    const workerPools: WorkerPoolMetrics[] = React.useMemo(
        () => effectiveData?.workerPools ?? [],
        [effectiveData]
    );
    const jmsQueues: JmsQueueMetrics[] = React.useMemo(
        () => effectiveData?.jmsQueues ?? [],
        [effectiveData]
    );

    const enabledDatasources = React.useMemo(
        () => datasources.filter(ds => ds.statisticsEnabled),
        [datasources]
    );

    const entityColorMaps = React.useMemo<Record<MetricLayerCategory, Record<string, string>>>(
        () => ({
            datasource: buildEntityColorMap(enabledDatasources.map(ds => ds.poolName)),
            worker: buildEntityColorMap(workerPools.map(wp => wp.poolName)),
            jms: buildEntityColorMap(jmsQueues.map(q => q.queueName)),
        }),
        [enabledDatasources, workerPools, jmsQueues]
    );

    const kpiDatasources = React.useMemo(() => {
        const excluded = excludedEntitiesByLayer.inUseCount;
        if (!excluded || excluded.size === 0) {
            return enabledDatasources;
        }
        const filtered = enabledDatasources.filter(ds => !excluded.has(ds.poolName));
        return filtered.length > 0 ? filtered : enabledDatasources;
    }, [enabledDatasources, excludedEntitiesByLayer.inUseCount]);

    const kpiWorkerPools = React.useMemo(() => {
        const excluded = excludedEntitiesByLayer.workerQueueSize;
        if (!excluded || excluded.size === 0) {
            return workerPools;
        }
        const filtered = workerPools.filter(wp => !excluded.has(wp.poolName));
        return filtered.length > 0 ? filtered : workerPools;
    }, [workerPools, excludedEntitiesByLayer.workerQueueSize]);

    const noDatasourcesDetected = datasources.length === 0;
    const allDsStatsDisabled = datasources.length > 0 && enabledDatasources.length === 0;
    const nothingDetected = datasources.length === 0 && workerPools.length === 0 && jmsQueues.length === 0;
    const hasAnyActiveData =
        enabledDatasources.length > 0 || workerPools.length > 0 || jmsQueues.length > 0;

    const waitCountVariant = React.useMemo(
        () => resolveWaitCountVariant(kpiDatasources),
        [kpiDatasources]
    );
    const dsKpi = React.useMemo(
        () => computeKpiSummary(kpiDatasources),
        [kpiDatasources]
    );
    const workerKpi = React.useMemo(
        () => computeWorkerKpiSummary(kpiWorkerPools),
        [kpiWorkerPools]
    );

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

    const selectAllLayers = React.useCallback(() => {
        setEnabledLayers(new Set(ALL_LAYERS.map(l => l.id)));
        setExcludedEntitiesByLayer({});
    }, []);

    const resetDefaultLayers = React.useCallback(() => {
        setEnabledLayers(createDefaultEnabledLayers());
        setExcludedEntitiesByLayer({});
        setSplitLayers(createDefaultSplitLayers());
    }, []);

    const clearAllLayers = React.useCallback(() => {
        setEnabledLayers(new Set());
        setExcludedEntitiesByLayer({});
    }, []);

    const handleResetZoom = React.useCallback(() => {
        setFrozenPreviewBaseline(null);
        setZoomRange(null);
    }, []);

    const handleZoomChange = React.useCallback(
        (nextRange: ZoomTimeRange | null) => {
            if (!nextRange) {
                setFrozenPreviewBaseline(null);
                setZoomRange(null);
                return;
            }
            if (!isImportedMode && !zoomRange && data) {
                setFrozenPreviewBaseline({
                    datasources: data.datasources ?? [],
                    workerPools: data.workerPools ?? [],
                    jmsQueues: data.jmsQueues ?? [],
                    windowStart: data.windowStart ?? null,
                    windowEnd: data.windowEnd ?? null,
                    collectedAt: data.collectedAt ?? null,
                    previewBucketSeconds: data.previewBucketSeconds ?? data.bucketSeconds ?? 1,
                });
            }
            setZoomRange(nextRange);
        },
        [isImportedMode, zoomRange, data]
    );

    const handleWindowChange = React.useCallback((value: TimeWindowSeconds) => {
        setFrozenPreviewBaseline(null);
        setZoomRange(null);
        setWindowSeconds(value);
    }, []);

    const handleManualRefresh = React.useCallback(() => {
        setFrozenPreviewBaseline(null);
        void fetchMetrics();
    }, [fetchMetrics]);

    const poolUsageRatio =
        dsKpi.inUseCount != null && dsKpi.maxSize != null && dsKpi.maxSize > 0
            ? dsKpi.inUseCount / dsKpi.maxSize
            : null;
    const usageColor: KpiCardProps['color'] =
        dsKpi.inUseCount == null
            ? 'info'
            : (dsKpi.waitCount ?? 0) > 0 || (poolUsageRatio != null && poolUsageRatio >= 0.9)
                ? 'error'
                : poolUsageRatio != null && poolUsageRatio >= 0.75
                    ? 'warning'
                    : 'success';

    const waitLabel = waitCountVariant === 'INSTANTANEOUS'
        ? t($ => $.page.rendiment.kpi.waitCountInstantaneous)
        : waitCountVariant === 'MIXED'
            ? t($ => $.page.rendiment.kpi.waitCountMixed)
            : t($ => $.page.rendiment.kpi.waitCountDelta);

    const waitTooltip = waitCountVariant === 'INSTANTANEOUS'
        ? t($ => $.page.rendiment.kpi.waitCountInstantaneousTooltip)
        : t($ => $.page.rendiment.kpi.waitCountDeltaTooltip);

    const waitColor: KpiCardProps['color'] =
        dsKpi.waitCount == null ? 'info' : dsKpi.waitCount > 0 ? 'error' : 'success';

    const maxWaitColor: KpiCardProps['color'] =
        dsKpi.maxWaitTimeMs == null
            ? 'info'
            : dsKpi.maxWaitTimeMs >= 1000
                ? 'error'
                : dsKpi.maxWaitTimeMs >= 100
                    ? 'warning'
                    : 'success';

    const workerQueueColor: KpiCardProps['color'] =
        workerKpi.queueSize == null
            ? 'info'
            : workerKpi.queueSize > 0
                ? 'warning'
                : 'success';

    const isCollectionDisabled = !isImportedMode && effectiveData?.collectionEnabled === false;
    const estimatedMemoryBytes = effectiveData?.estimatedMemoryBytes ?? null;
    const lastUpdate = effectiveData?.collectedAt
        ? new Date(effectiveData.collectedAt).toLocaleString('ca-ES')
        : null;

    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
                overflow: 'hidden',
            }}
        >
            <PageTitle title={t($ => $.page.rendiment.title)} />
            <input
                ref={fileInputRef}
                type="file"
                accept=".json,application/json"
                onChange={e => void handleImportFileChange(e)}
                style={{ display: 'none' }}
                data-testid="import-json-input"
            />
            <Toolbar
                disableGutters
                sx={{ width: '100%', px: 2, backgroundColor: toolbarBg, mb: 2, gap: 1, flexWrap: 'wrap', flexShrink: 0 }}
            >
                <Typography variant="h6">{t($ => $.page.rendiment.title)}</Typography>
                <Box sx={{ flexGrow: 1 }} />
                {estimatedMemoryBytes != null && (
                    <Tooltip title={t($ => $.page.rendiment.estimatedMemoryTooltip)}>
                        <Chip
                            size="small"
                            variant="outlined"
                            icon={<Icon>memory</Icon>}
                            label={`${t($ => $.page.rendiment.estimatedMemoryLabel)}: ${formatMemoryUpperBound(estimatedMemoryBytes)}`}
                            data-testid="estimated-memory-chip"
                            sx={{
                                color: 'text.secondary',
                                borderColor: 'divider',
                                '& .MuiChip-icon': {
                                    color: 'text.secondary',
                                },
                            }}
                        />
                    </Tooltip>
                )}
                <Tooltip title={t($ => $.page.rendiment.exportJsonTooltip)}>
                    <span>
                        <Button
                            size="small"
                            variant="outlined"
                            startIcon={<Icon>download</Icon>}
                            disabled={exporting || isImportedMode || isCollectionDisabled}
                            onClick={() => void handleExportJson()}
                            data-testid="export-json-button"
                        >
                            {t($ => $.page.rendiment.exportJson)}
                        </Button>
                    </span>
                </Tooltip>
                <Tooltip title={t($ => $.page.rendiment.importJsonTooltip)}>
                    <span>
                        <Button
                            size="small"
                            variant="outlined"
                            startIcon={<Icon>upload_file</Icon>}
                            onClick={() => fileInputRef.current?.click()}
                            data-testid="import-json-button"
                        >
                            {t($ => $.page.rendiment.importJson)}
                        </Button>
                    </span>
                </Tooltip>
                {lastUpdate && (
                    <Typography
                        variant="caption"
                        color="text.secondary"
                        sx={{ mr: 1 }}
                        data-testid="last-update-label"
                    >
                        {isImportedMode
                            ? t($ => $.page.rendiment.snapshotDate)
                            : t($ => $.page.rendiment.lastUpdate)}
                        : {lastUpdate}
                    </Typography>
                )}
                <FormControlLabel
                    control={
                        <Switch
                            size="small"
                            checked={autoRefresh}
                            disabled={isImportedMode}
                            onChange={(_e, checked) => setAutoRefresh(checked)}
                        />
                    }
                    label={
                        <Typography variant="caption">
                            {t($ => $.page.rendiment.autoRefresh)}
                        </Typography>
                    }
                    sx={{ mr: 1 }}
                />
                <Tooltip title={t($ => $.page.rendiment.refresh)}>
                    <span>
                        <IconButton
                            aria-label={t($ => $.page.rendiment.refresh)}
                            disabled={isImportedMode}
                            onClick={handleManualRefresh}
                            size="small"
                        >
                            <Icon>refresh</Icon>
                        </IconButton>
                    </span>
                </Tooltip>
            </Toolbar>

            <Box
                sx={{
                    px: 2,
                    pb: 2,
                    flex: 1,
                    minHeight: 0,
                    overflowY: 'auto',
                    display: 'flex',
                    flexDirection: 'column',
                }}
            >
                {importedSource && (
                    <Alert
                        severity="info"
                        data-testid="imported-snapshot-banner"
                        sx={{ mb: 2, flexShrink: 0 }}
                        action={
                            <Button
                                color="inherit"
                                size="small"
                                onClick={handleExitImportedMode}
                                data-testid="return-to-live-button"
                            >
                                {t($ => $.page.rendiment.returnToLive)}
                            </Button>
                        }
                    >
                        <AlertTitle>
                            {t($ => $.page.rendiment.importedBannerTitle)}: {importedSource.fileName}
                        </AlertTitle>
                        {t($ => $.page.rendiment.importedBannerSubtitle)}
                    </Alert>
                )}

                {error && (
                    <Alert severity="error" sx={{ mb: 2, flexShrink: 0 }}>
                        {error}
                    </Alert>
                )}

                {loading && !effectiveData ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
                        <CircularProgress />
                    </Box>
                ) : isCollectionDisabled ? (
                    <Alert severity="info" data-testid="collection-disabled-alert">
                        <AlertTitle>{t($ => $.page.rendiment.collectionDisabledTitle)}</AlertTitle>
                        {t($ => $.page.rendiment.collectionDisabledMessage)}
                    </Alert>
                ) : nothingDetected ? (
                    <Alert severity="info">{t($ => $.page.rendiment.noMetrics)}</Alert>
                ) : (
                    <>
                        {noDatasourcesDetected && (
                            <Alert severity="warning" sx={{ mb: 2.5, flexShrink: 0 }} data-testid="no-datasources-alert">
                                {t($ => $.page.rendiment.noDatasources)}
                            </Alert>
                        )}

                        {allDsStatsDisabled && !dismissedDsStatsAlert && (
                            <Alert
                                severity="warning"
                                onClose={() => setDismissedDsStatsAlert(true)}
                                data-testid="ds-stats-disabled-alert"
                                sx={{ mb: 2.5, flexShrink: 0 }}
                            >
                                <AlertTitle>{t($ => $.page.rendiment.statisticsDisabledTitle)}</AlertTitle>
                                <Box>{t($ => $.page.rendiment.statisticsDisabledMessage)}</Box>
                                <Typography variant="caption" sx={{ display: 'block', mt: 1 }}>
                                    {datasources
                                        .map(
                                            ds =>
                                                `${ds.poolName} (${ds.minSize ?? '—'}–${ds.maxSize ?? '—'}) · ${t($ => $.page.rendiment.poolDisabledBadge)}`
                                        )
                                        .join(' · ')}
                                </Typography>
                            </Alert>
                        )}

                        {hasAnyActiveData && (
                            <>
                                {(enabledDatasources.length > 0 || workerPools.length > 0) && (
                                    <Grid container spacing={2} alignItems="stretch" sx={{ mb: 2.5, flexShrink: 0 }}>
                                        {enabledDatasources.length > 0 && (
                                            <>
                                                <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
                                                    <KpiCard
                                                        icon="cable"
                                                        label={t($ => $.page.rendiment.kpi.inUse)}
                                                        value={`${dsKpi.inUseCount != null ? formatNumber(dsKpi.inUseCount) : '—'} / ${dsKpi.maxSize != null ? formatNumber(dsKpi.maxSize) : '—'}`}
                                                        caption={
                                                            dsKpi.minSize != null || dsKpi.maxSize != null
                                                                ? `${t($ => $.page.rendiment.kpi.minMax)}: ${dsKpi.minSize ?? '—'} / ${dsKpi.maxSize ?? '—'}`
                                                                : undefined
                                                        }
                                                        tooltip={t($ => $.page.rendiment.kpi.inUseTooltip)}
                                                        color={usageColor}
                                                        unavailable={dsKpi.inUseCount == null}
                                                    />
                                                </Grid>
                                                <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
                                                    <KpiCard
                                                        icon="hourglass_top"
                                                        label={waitLabel}
                                                        value={dsKpi.waitCount != null ? formatNumber(dsKpi.waitCount) : '—'}
                                                        tooltip={waitTooltip}
                                                        color={waitColor}
                                                        unavailable={dsKpi.waitCount == null}
                                                    />
                                                </Grid>
                                                <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
                                                    <KpiCard
                                                        icon="timer"
                                                        label={t($ => $.page.rendiment.kpi.maxWaitTime)}
                                                        value={dsKpi.maxWaitTimeMs != null ? `${formatNumber(dsKpi.maxWaitTimeMs)} ms` : '—'}
                                                        tooltip={t($ => $.page.rendiment.kpi.maxWaitTimeTooltip)}
                                                        color={maxWaitColor}
                                                        unavailable={dsKpi.maxWaitTimeMs == null}
                                                    />
                                                </Grid>
                                            </>
                                        )}

                                        {workerPools.length > 0 && (
                                            <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex' }}>
                                                <KpiCard
                                                    icon="queue"
                                                    label={t($ => $.page.rendiment.kpi.workerQueueSize)}
                                                    value={workerKpi.queueSize != null ? formatNumber(workerKpi.queueSize) : '—'}
                                                    caption={
                                                        workerKpi.queueCapacity != null
                                                            ? `${t($ => $.page.rendiment.kpi.queueCapLabel)}: ${formatNumber(workerKpi.queueCapacity)}${workerKpi.hasUnboundedQueue ? '+' : ''}`
                                                            : t($ => $.page.rendiment.unboundedQueue)
                                                    }
                                                    tooltip={t($ => $.page.rendiment.kpi.workerQueueSizeTooltip)}
                                                    color={workerQueueColor}
                                                    unavailable={workerKpi.queueSize == null}
                                                />
                                            </Grid>
                                        )}
                                    </Grid>
                                )}

                                <PerformanceComposableChart
                                    datasources={datasources}
                                    workerPools={workerPools}
                                    jmsQueues={jmsQueues}
                                    previewDatasources={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.datasources
                                            : undefined
                                    }
                                    previewWorkerPools={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.workerPools
                                            : undefined
                                    }
                                    previewJmsQueues={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.jmsQueues
                                            : undefined
                                    }
                                    enabledLayers={enabledLayers}
                                    excludedEntitiesByLayer={excludedEntitiesByLayer}
                                    splitLayers={splitLayers}
                                    entityColorMaps={entityColorMaps}
                                    onSelectionChange={handleSelectionChange}
                                    onToggleSplitLayer={handleToggleSplitLayer}
                                    onSelectAllLayers={selectAllLayers}
                                    onResetDefaultLayers={resetDefaultLayers}
                                    onClearAllLayers={clearAllLayers}
                                    windowSeconds={windowSeconds}
                                    onWindowChange={handleWindowChange}
                                    waitCountVariant={waitCountVariant}
                                    bucketSeconds={effectiveData?.bucketSeconds ?? 1}
                                    previewBucketSeconds={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.previewBucketSeconds
                                            : (effectiveData?.previewBucketSeconds ?? effectiveData?.bucketSeconds ?? 1)
                                    }
                                    windowStart={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.windowStart
                                            : (effectiveData?.windowStart ?? null)
                                    }
                                    windowEnd={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.windowEnd
                                            : (effectiveData?.windowEnd ?? null)
                                    }
                                    collectedAt={
                                        !isImportedMode && zoomRange && frozenPreviewBaseline
                                            ? frozenPreviewBaseline.collectedAt
                                            : (effectiveData?.collectedAt ?? null)
                                    }
                                    rangeStart={effectiveData?.rangeStart ?? null}
                                    rangeEnd={effectiveData?.rangeEnd ?? null}
                                    zoomRange={zoomRange}
                                    onZoomChange={handleZoomChange}
                                    onResetZoom={handleResetZoom}
                                />
                            </>
                        )}
                    </>
                )}
            </Box>
        </Box>
    );
};

export default Rendiment;
