import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    Box, Tab, Tabs, Alert, Chip, Tooltip,
    IconButton, Icon, Typography, Paper, Toolbar,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import Grid from '@mui/material/Grid';
import { GridRenderCellParams } from '@mui/x-data-grid';
import {
    MuiDataGrid, MuiDataGridColDef, useMuiDataGridApiRef,
    useAuthContext, useResourceApiContext,
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

interface DbOverview {
    sessionsActives: number;
    sessionsTotal: number;
    hitRatioCache: number | null;
    taulesCom: number;
    totalBytesReservats: number;
    ultimaActualitzacio: string | null;
    sessionsDisponibles: boolean;
    activitatDisponible: boolean;
    sqlDisponible: boolean;
    tablespacesDisponibles: boolean;
    bloqueigsDisponibles: boolean;
    bloqueigsActius: number;
    indexosInvalidsCount: number;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

const formatBytes = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
    return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`;
};

const formatNumber = (n: number): string =>
    n.toLocaleString('ca-ES');

const pctColor = (pct: number): 'success' | 'warning' | 'error' => {
    if (pct >= 90) return 'error';
    if (pct >= 75) return 'warning';
    return 'success';
};

// ─── KPI Card ─────────────────────────────────────────────────────────────────

interface KpiCardProps {
    icon: string;
    label: string;
    value: string;
    tooltip: string;
    color?: 'success' | 'warning' | 'error' | 'info';
    unavailable?: boolean;
}

const KpiCard: React.FC<KpiCardProps> = ({ icon, label, value, tooltip, color, unavailable }) => (
    <Tooltip title={tooltip} arrow placement="top">
        <Paper
            elevation={2}
            sx={{
                p: 2.5,
                display: 'flex',
                alignItems: 'center',
                gap: 2,
                borderLeft: 4,
                cursor: 'help',
                borderColor: unavailable
                    ? 'grey.400'
                    : color === 'error' ? 'error.main'
                    : color === 'warning' ? 'warning.main'
                    : color === 'success' ? 'success.main'
                    : 'primary.main',
            }}
        >
            <Icon sx={{ fontSize: 36, color: unavailable ? 'grey.400' : `${color ?? 'primary'}.main` }}>
                {icon}
            </Icon>
            <Box>
                <Typography variant="caption" color="text.secondary">{label}</Typography>
                <Typography variant="h6" fontWeight="bold" color={unavailable ? 'text.disabled' : 'text.primary'}>
                    {unavailable ? '—' : value}
                </Typography>
            </Box>
        </Paper>
    </Tooltip>
);

// ─── Tab: Resum ────────────────────────────────────────────────────────────────

const TabResum: React.FC<{ overview: DbOverview | null }> = ({ overview }) => {
    const { t } = useTranslation();

    const hitRatioPct = overview?.hitRatioCache ?? null;
    const hitRatioColor = hitRatioPct == null ? 'info'
        : hitRatioPct < 85 ? 'error'
        : hitRatioPct < 90 ? 'warning'
        : 'success';

    const tsColumns: MuiDataGridColDef[] = [
        { field: 'id', headerName: t($ => $.page.monitorDb.health.tablespace), flex: 2, minWidth: 160 },
        {
            field: 'usatMb', headerName: t($ => $.page.monitorDb.health.usedMb),
            flex: 1, minWidth: 120, type: 'number',
            valueFormatter: (v: number) => `${v.toFixed(0)} MB`,
        },
        {
            field: 'totalMb', headerName: t($ => $.page.monitorDb.health.totalMb),
            flex: 1, minWidth: 110, type: 'number',
            valueFormatter: (v: number) => v > 0 ? `${v.toFixed(0)} MB` : 'N/D',
        },
        {
            field: 'maxMb', headerName: t($ => $.page.monitorDb.health.maxMb),
            flex: 1, minWidth: 110, type: 'number',
            valueFormatter: (v: number) => v > 0 ? `${v.toFixed(0)} MB` : 'N/D',
        },
        {
            field: 'lliureMb', headerName: t($ => $.page.monitorDb.health.freeMb),
            flex: 1, minWidth: 110, type: 'number',
            valueFormatter: (v: number) => v > 0 ? `${v.toFixed(0)} MB` : 'N/D',
        },
        {
            field: 'pctUsat', headerName: t($ => $.page.monitorDb.health.pctUsed),
            flex: 1, minWidth: 100, type: 'number',
            renderCell: (params: GridRenderCellParams) => {
                const pct: number = params.value ?? 0;
                return pct > 0
                    ? <Chip label={`${pct.toFixed(1)}%`} color={pctColor(pct)} size="small" />
                    : <span>N/D</span>;
            },
        },
    ];

    return (
        <Box>
            <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <KpiCard
                        icon="groups"
                        label={t($ => $.page.monitorDb.overview.activeSessions)}
                        tooltip={t($ => $.page.monitorDb.overview.activeSessionsTooltip)}
                        value={`${formatNumber(overview?.sessionsActives ?? 0)} / ${formatNumber(overview?.sessionsTotal ?? 0)}`}
                        color={overview && overview.sessionsTotal > 0
                            ? (overview.sessionsActives / overview.sessionsTotal > 0.8 ? 'warning' : 'success')
                            : 'info'}
                        unavailable={!overview?.sessionsDisponibles}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <KpiCard
                        icon="memory"
                        label={t($ => $.page.monitorDb.overview.cacheHitRatio)}
                        tooltip={t($ => $.page.monitorDb.overview.cacheHitRatioTooltip)}
                        value={hitRatioPct != null ? `${hitRatioPct.toFixed(1)}%` : '—'}
                        color={hitRatioColor}
                        unavailable={hitRatioPct == null}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <KpiCard
                        icon="table_chart"
                        label={t($ => $.page.monitorDb.overview.totalTables)}
                        tooltip={t($ => $.page.monitorDb.overview.totalTablesTooltip)}
                        value={formatNumber(overview?.taulesCom ?? 0)}
                        color="info"
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <KpiCard
                        icon="storage"
                        label={t($ => $.page.monitorDb.overview.totalReserved)}
                        tooltip={t($ => $.page.monitorDb.overview.totalReservedTooltip)}
                        value={formatBytes(overview?.totalBytesReservats ?? 0)}
                        color="info"
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <KpiCard
                        icon="lock"
                        label={t($ => $.page.monitorDb.overview.activeLocks)}
                        tooltip={t($ => $.page.monitorDb.overview.activeLocksTooltip)}
                        value={String(overview?.bloqueigsActius ?? 0)}
                        color={!overview?.bloqueigsDisponibles ? 'info'
                            : (overview.bloqueigsActius === 0 ? 'success'
                            : overview.bloqueigsActius <= 2 ? 'warning' : 'error')}
                        unavailable={!overview?.bloqueigsDisponibles}
                    />
                </Grid>
                <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                    <KpiCard
                        icon="key_off"
                        label={t($ => $.page.monitorDb.overview.invalidIndexes)}
                        tooltip={t($ => $.page.monitorDb.overview.invalidIndexesTooltip)}
                        value={String(overview?.indexosInvalidsCount ?? 0)}
                        color={overview?.indexosInvalidsCount === 0 ? 'success' : 'error'}
                    />
                </Grid>
            </Grid>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
                {t($ => $.page.monitorDb.health.title)}
            </Typography>
            <MuiDataGrid
                resourceName="dbTablespace"
                columns={tsColumns}
                readOnly
                autoHeight
                paginationActive
                defaultPaginationModel={{ page: 0, pageSize: 10 }}
            />
        </Box>
    );
};

// ─── Tab: Emmagatzematge ───────────────────────────────────────────────────────

const TabStorage: React.FC = () => {
    const { t } = useTranslation();

    const columns: MuiDataGridColDef[] = [
        { field: 'id', headerName: t($ => $.page.monitorDb.storage.table), flex: 2, minWidth: 180 },
        {
            field: 'numFiles', headerName: t($ => $.page.monitorDb.storage.numRows),
            flex: 1, minWidth: 100, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
        {
            field: 'bytesReservats', headerName: t($ => $.page.monitorDb.storage.bytesReserved),
            flex: 1, minWidth: 130, type: 'number',
            valueFormatter: (v: number) => formatBytes(v),
        },
        {
            field: 'bytesEstimats', headerName: t($ => $.page.monitorDb.storage.estimatedData),
            flex: 1, minWidth: 130, type: 'number',
            valueFormatter: (v: number) => formatBytes(v),
        },
        {
            field: 'pctUs', headerName: t($ => $.page.monitorDb.storage.pctUsed),
            flex: 1, minWidth: 100, type: 'number',
            valueGetter: (_v: unknown, row) =>
                row.bytesReservats > 0
                    ? Math.round((row.bytesEstimats / row.bytesReservats) * 100)
                    : 0,
            renderCell: (params: GridRenderCellParams) => {
                const pct: number = params.value;
                return <Chip label={`${pct}%`} color={pctColor(pct)} size="small" />;
            },
        },
        {
            field: 'ultimaAnalisi', headerName: t($ => $.page.monitorDb.storage.lastAnalyzed),
            flex: 1.5, minWidth: 150,
            valueFormatter: (v: string | null) => v ? new Date(v).toLocaleString('ca-ES') : '—',
        },
    ];

    return (
        <MuiDataGrid
            resourceName="dbTaulaStorage"
            columns={columns}
            readOnly
            autoHeight
            paginationActive
            defaultPaginationModel={{ page: 0, pageSize: 10 }}
        />
    );
};

// ─── Tab: Activitat ────────────────────────────────────────────────────────────

const TabActivitat: React.FC = () => {
    const { t } = useTranslation();

    const columns: MuiDataGridColDef[] = [
        { field: 'id', headerName: t($ => $.page.monitorDb.activity.table), flex: 2, minWidth: 180 },
        {
            field: 'lecturesFisiques', headerName: t($ => $.page.monitorDb.activity.physicalReads),
            flex: 1, minWidth: 140, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
        {
            field: 'lecturesLogiques', headerName: t($ => $.page.monitorDb.activity.logicalReads),
            flex: 1, minWidth: 140, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
        {
            field: 'hitRatio', headerName: t($ => $.page.monitorDb.activity.cacheHitRatio),
            flex: 1, minWidth: 110, type: 'number',
            valueGetter: (_v: unknown, row) => {
                const total = row.lecturesFisiques + row.lecturesLogiques;
                return total > 0 ? Math.round((row.lecturesLogiques / total) * 100) : 0;
            },
            renderCell: (params: GridRenderCellParams) => {
                const pct: number = params.value;
                return <Chip label={`${pct}%`} color={pct >= 90 ? 'success' : pct >= 75 ? 'warning' : 'error'} size="small" />;
            },
        },
        {
            field: 'esperesBuffer', headerName: t($ => $.page.monitorDb.activity.bufferWaits),
            flex: 1, minWidth: 130, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
        {
            field: 'esperesFila', headerName: t($ => $.page.monitorDb.activity.rowLockWaits),
            flex: 1, minWidth: 130, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
    ];

    return (
        <MuiDataGrid
            resourceName="dbTaulaActivitat"
            columns={columns}
            readOnly
            autoHeight
            paginationActive
            defaultPaginationModel={{ page: 0, pageSize: 10 }}
        />
    );
};

// ─── Tab: Top SQL ──────────────────────────────────────────────────────────────

const TabTopSql: React.FC = () => {
    const { t } = useTranslation();

    const columns: MuiDataGridColDef[] = [
        {
            field: 'sqlText', headerName: t($ => $.page.monitorDb.sql.sqlText),
            flex: 3, minWidth: 250,
            renderCell: (params: GridRenderCellParams) => (
                <Tooltip title={params.value} placement="top">
                    <Typography variant="body2" noWrap sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}>
                        {params.value}
                    </Typography>
                </Tooltip>
            ),
        },
        {
            field: 'tempsTotalS', headerName: t($ => $.page.monitorDb.sql.totalTime),
            width: 130, type: 'number',
            valueFormatter: (v: number) => `${v.toFixed(2)} s`,
        },
        {
            field: 'execucions', headerName: t($ => $.page.monitorDb.sql.executions),
            width: 110, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
        {
            field: 'msPerExec', headerName: t($ => $.page.monitorDb.sql.msPerExec),
            width: 120, type: 'number',
            renderCell: (params: GridRenderCellParams) => {
                const ms: number = params.value;
                const color = ms > 2000 ? 'error' : ms > 500 ? 'warning' : 'success';
                return <Chip label={`${ms.toFixed(1)} ms`} color={color} size="small" />;
            },
        },
        {
            field: 'buffersPerExec', headerName: t($ => $.page.monitorDb.sql.buffersPerExec),
            width: 130, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
    ];

    return (
        <MuiDataGrid
            resourceName="dbTopSql"
            columns={columns}
            readOnly
            autoHeight
            paginationActive
            defaultPaginationModel={{ page: 0, pageSize: 10 }}
        />
    );
};

// ─── Tab: Bloquejos ────────────────────────────────────────────────────────────

const TabBloquejos: React.FC = () => {
    const { t } = useTranslation();

    const columns: MuiDataGridColDef[] = [
        { field: 'id', headerName: t($ => $.page.monitorDb.locks.sid), width: 80, type: 'number' },
        { field: 'username', headerName: t($ => $.page.monitorDb.locks.username), flex: 1, minWidth: 130 },
        { field: 'status', headerName: t($ => $.page.monitorDb.locks.status), width: 120 },
        { field: 'objectName', headerName: t($ => $.page.monitorDb.locks.objectName), flex: 1.5, minWidth: 180 },
        { field: 'objectType', headerName: t($ => $.page.monitorDb.locks.objectType), width: 100 },
        { field: 'lockMode', headerName: t($ => $.page.monitorDb.locks.lockMode), flex: 1, minWidth: 130 },
        { field: 'lockRequest', headerName: t($ => $.page.monitorDb.locks.lockRequest), flex: 1, minWidth: 110 },
        {
            field: 'blocking', headerName: t($ => $.page.monitorDb.locks.blocking), width: 110,
            renderCell: (params: GridRenderCellParams) => params.value
                ? <Chip label="Sí" color="error" size="small" />
                : <Chip label="No" color="default" size="small" />,
        },
    ];

    return (
        <MuiDataGrid
            resourceName="dbBloqueig"
            columns={columns}
            readOnly
            autoHeight
            paginationActive
            defaultPaginationModel={{ page: 0, pageSize: 10 }}
            getRowClassName={params => params.row.blocking ? 'row-blocking' : ''}
            sx={{ '& .row-blocking': { backgroundColor: 'rgba(211,47,47,0.08)' } }}
        />
    );
};

// ─── Tab: Índexos ──────────────────────────────────────────────────────────────

const TabIndexos: React.FC = () => {
    const { t } = useTranslation();
    const { getToken } = useAuthContext();
    const { apiUrl } = useResourceApiContext();
    const indexApiRef = useMuiDataGridApiRef();
    const [rebuildMsg, setRebuildMsg] = React.useState<{ text: string; ok: boolean } | null>(null);

    const columns: MuiDataGridColDef[] = [
        { field: 'id', headerName: t($ => $.page.monitorDb.indexes.indexName), flex: 2, minWidth: 200 },
        { field: 'tableName', headerName: t($ => $.page.monitorDb.indexes.tableName), flex: 1.5, minWidth: 160 },
        {
            field: 'status', headerName: t($ => $.page.monitorDb.indexes.status), width: 110,
            renderCell: (params: GridRenderCellParams) => {
                const s: string = params.value;
                const color = s === 'VALID' ? 'success' : s === 'UNUSABLE' ? 'error' : 'default';
                return <Chip label={s} color={color as 'success' | 'error' | 'default'} size="small" />;
            },
        },
        {
            field: 'uniqueness', headerName: t($ => $.page.monitorDb.indexes.uniqueness), width: 110,
            renderCell: (params: GridRenderCellParams) => params.value === 'UNIQUE'
                ? <Chip label="UNIQUE" color="info" size="small" />
                : <span>{params.value}</span>,
        },
        {
            field: 'numRows', headerName: t($ => $.page.monitorDb.indexes.numRows),
            width: 100, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
        {
            field: 'lastAnalyzed', headerName: t($ => $.page.monitorDb.indexes.lastAnalyzed),
            flex: 1, minWidth: 150,
            valueFormatter: (v: string | null) => v ? new Date(v).toLocaleString('ca-ES') : '—',
        },
        { field: 'blevel', headerName: t($ => $.page.monitorDb.indexes.blevel), width: 100, type: 'number' },
        {
            field: 'leafBlocks', headerName: t($ => $.page.monitorDb.indexes.leafBlocks),
            width: 120, type: 'number',
            valueFormatter: (v: number) => formatNumber(v),
        },
    ];

    const rebuildActions = React.useMemo(() => [{
        icon: 'build',
        title: t($ => $.page.monitorDb.indexes.rebuild),
        action: 'REBUILD',
        hidden: (row: { status: string }) => row.status !== 'UNUSABLE',
        onClick: async (id: string | number) => {
            setRebuildMsg(null);
            const token = getToken?.();
            const headers: Record<string, string> = token ? { Authorization: `Bearer ${token}` } : {};
            try {
                const resp = await fetch(`${apiUrl}/db-indexos/${id}/artifacts/action/REBUILD`, {
                    method: 'POST', headers,
                });
                if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
                setRebuildMsg({ text: t($ => $.page.monitorDb.indexes.rebuildSuccess).replace('{name}', String(id)), ok: true });
                indexApiRef.current?.refresh();
            } catch {
                setRebuildMsg({ text: t($ => $.page.monitorDb.indexes.rebuildError).replace('{name}', String(id)), ok: false });
            }
        },
    }], [t, getToken, apiUrl, indexApiRef]);

    return (
        <Box>
            {rebuildMsg && (
                <Alert severity={rebuildMsg.ok ? 'success' : 'error'} sx={{ mb: 2 }} onClose={() => setRebuildMsg(null)}>
                    {rebuildMsg.text}
                </Alert>
            )}
            <MuiDataGrid
                resourceName="dbIndex"
                columns={columns}
                readOnly
                autoHeight
                paginationActive
                defaultPaginationModel={{ page: 0, pageSize: 10 }}
                apiRef={indexApiRef}
                rowAdditionalActions={rebuildActions}
            />
        </Box>
    );
};

// ─── Main Component ────────────────────────────────────────────────────────────

const MonitorDb: React.FC = () => {
    const { t } = useTranslation();
    const { getToken } = useAuthContext();
    const { apiUrl } = useResourceApiContext();
    const theme = useTheme();
    const toolbarBg = theme.palette.mode === 'light' ? theme.palette.grey[200] : theme.palette.grey[900];

    const [tab, setTab] = React.useState(0);
    const [overview, setOverview] = React.useState<DbOverview | null>(null);
    const [overviewError, setOverviewError] = React.useState<string | null>(null);

    const fetchOverview = React.useCallback(async () => {
        setOverviewError(null);
        const token = getToken?.();
        const headers: Record<string, string> = token ? { Authorization: `Bearer ${token}` } : {};
        try {
            const resp = await fetch(`${apiUrl}/db-metrics/overview`, { headers });
            if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
            setOverview(await resp.json());
        } catch (err) {
            setOverviewError(String(err));
        }
    }, [getToken, apiUrl]);

    React.useEffect(() => { void fetchOverview(); }, [fetchOverview]);

    const lastUpdate = overview?.ultimaActualitzacio
        ? new Date(overview.ultimaActualitzacio).toLocaleString('ca-ES')
        : null;

    return (
        <>
            <PageTitle title={t($ => $.page.monitorDb.title)} />
            <Toolbar
                disableGutters
                sx={{ width: '100%', px: 2, backgroundColor: toolbarBg, mb: 2 }}
            >
                <Typography variant="h6">{t($ => $.page.monitorDb.title)}</Typography>
                <Box sx={{ flexGrow: 1 }} />
                {lastUpdate && (
                    <Typography variant="caption" color="text.secondary" sx={{ mr: 1 }}>
                        {t($ => $.page.monitorDb.lastUpdate)}: {lastUpdate}
                    </Typography>
                )}
                <Tooltip title={t($ => $.page.monitorDb.refresh)}>
                    <span>
                        <IconButton onClick={() => void fetchOverview()} size="small">
                            <Icon>refresh</Icon>
                        </IconButton>
                    </span>
                </Tooltip>
            </Toolbar>

            {overviewError && (
                <Alert severity="error" sx={{ mb: 2 }}>{overviewError}</Alert>
            )}

            <Tabs
                value={tab}
                onChange={(_e, v) => setTab(v)}
                textColor="primary"
                indicatorColor="primary"
                sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
            >
                <Tab label={t($ => $.page.monitorDb.tabs.overview)} icon={<Icon>dashboard</Icon>} iconPosition="start" />
                <Tab label={t($ => $.page.monitorDb.tabs.storage)} icon={<Icon>storage</Icon>} iconPosition="start" />
                <Tab label={t($ => $.page.monitorDb.tabs.activity)} icon={<Icon>bar_chart</Icon>} iconPosition="start" />
                <Tab label={t($ => $.page.monitorDb.tabs.sql)} icon={<Icon>code</Icon>} iconPosition="start" />
                <Tab label={t($ => $.page.monitorDb.tabs.locks)} icon={<Icon>lock</Icon>} iconPosition="start" />
                <Tab label={t($ => $.page.monitorDb.tabs.indexes)} icon={<Icon>list_alt</Icon>} iconPosition="start" />
            </Tabs>

            {tab === 0 && <Box sx={{ mx: 2, pb: 4 }}><TabResum overview={overview} /></Box>}
            {tab === 1 && <Box sx={{ mx: 2, pb: 4 }}><TabStorage /></Box>}
            {tab === 2 && <Box sx={{ mx: 2, pb: 4 }}><TabActivitat /></Box>}
            {tab === 3 && <Box sx={{ mx: 2, pb: 4 }}><TabTopSql /></Box>}
            {tab === 4 && <Box sx={{ mx: 2, pb: 4 }}><TabBloquejos /></Box>}
            {tab === 5 && <Box sx={{ mx: 2, pb: 4 }}><TabIndexos /></Box>}
        </>
    );
};

export default MonitorDb;
