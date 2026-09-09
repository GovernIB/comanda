import MuiToolbar from '@mui/material/Toolbar';
import {
    Alert, Box, Button, Icon, ToggleButton, Tooltip, Typography,
    Select, MenuItem, IconButton, Divider, ListItemIcon, ListItemText
} from '@mui/material';
import {
    DashboardReactGridLayout,
    useMapDashboardItems,
    useStoredLargeScreenMode,
} from '../components/estadistiques/DashboardReactGridLayout.tsx';
import {
    BasePage,
    useResourceApiService,
    useMuiDataGridApiRef,
    useBaseAppContext,
    MuiDataGrid
} from 'reactlib';
import { useTheme } from '@mui/material/styles';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { useNavigate, useParams } from 'react-router-dom';
import { useEffect, useState, useMemo, useCallback } from 'react';
import Dialog from '../../lib/components/mui/Dialog.tsx';
import { ESTADISTIQUES_PATH } from '../AppRoutes.tsx';
import { useTranslation } from "react-i18next";
import PageTitle from '../components/PageTitle.tsx';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';
import { FooterHeightPlaceholder } from '../components/ComandaFooter.tsx';
import { useEntornCodi } from '../components/estadistiques/dashboardPlantillaHook.ts';
import DashboardFiltreBar from '../components/estadistiques/DashboardFiltreBar.tsx';
import { DashboardFiltreSeleccio } from '../types/dashboardFiltre.model.ts';

const LAST_VIEWED_STORAGE_KEY = 'lastViewedDashboardId';
const LARGE_SCREEN_MODE_STORAGE_KEY = 'comanda.dashboardView.largeScreenMode';
const NO_DASHBOARD_FOUND = 'NO_DASHBOARD_FOUND';

interface DashboardPreferitToggleProps {
    dashboardId: number | string;
    isPreferit: boolean;
    onRefresh?: () => void;
}

const DashboardPreferitToggle: React.FC<DashboardPreferitToggleProps> = ({ dashboardId, isPreferit, onRefresh }) => {
    const { t } = useTranslation();
    const { artifactAction } = useResourceApiService('dashboard');
    const { temporalMessageShow } = useBaseAppContext();
    const [loading, setLoading] = useState(false);

    const handleClick = useCallback(async (e: React.MouseEvent) => {
        e.stopPropagation();
        setLoading(true);
        try {
            await artifactAction(dashboardId, { code: 'marcar_preferit', data: { marcar: !isPreferit } });
            temporalMessageShow(
                null,
                isPreferit ? t($ => $.page.dashboards.view.favorite.removed) : t($ => $.page.dashboards.view.favorite.added),
                'success'
            );
            onRefresh?.();
        } catch (error: any) {
            temporalMessageShow(null, error.message || 'Error', 'error');
        } finally {
            setLoading(false);
        }
    }, [dashboardId, isPreferit, artifactAction, temporalMessageShow, t, onRefresh]);

    return (
        <IconButton size="small" onClick={handleClick} disabled={loading}>
            <Icon sx={{
                color: isPreferit ? 'warning.main' : 'action.disabled',
                transition: 'color 0.2s'
            }}>
                {isPreferit ? 'star' : 'star_border'}
            </Icon>
        </IconButton>
    );
};

function useDashboardFavorites() {
    const { isReady: apiIsReady, find: findDashboards } = useResourceApiService('dashboard');
    const [favorites, setFavorites] = useState<any[]>([]);

    const refreshFavorites = useCallback(async () => {
        if (!apiIsReady) return;
        try {
            const response = await findDashboards({ namedQueries: ['preferit'], size: 100 });
            setFavorites(response.rows || []);
        } catch (error) {
            console.error('Error loading favorites', error);
        }
    }, [apiIsReady, findDashboards]);

    useEffect(() => {
        refreshFavorites();
    }, [refreshFavorites]);

    return { favorites, refreshFavorites };
}

function useDashboardSelector(
    currentDashboardId: string | number | null,
    currentDashboardTitol: string | undefined,
    favorites: any[],
    onSeeAll: () => void
) {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const options = useMemo(() => {
        const currentIsInFavs = favorites.some((d: any) => String(d.id) === String(currentDashboardId));
        if (!currentIsInFavs && currentDashboardTitol && currentDashboardId) {
            return [{ id: currentDashboardId, titol: currentDashboardTitol }, ...favorites];
        }
        return favorites;
    }, [favorites, currentDashboardId, currentDashboardTitol]);

    const handleSelectChange = (event: any) => {
        const selectedId = event.target.value;
        if (selectedId === '__VIEW_ALL__') {
            onSeeAll();
        } else if (!!selectedId) {
            navigate(`/${ESTADISTIQUES_PATH}/${selectedId}`);
        }
    };

    return (
        <Select
            value={currentDashboardId || ''}
            onChange={handleSelectChange}
            displayEmpty
            renderValue={(selected) => {
                if (!selected) return t($ => $.page.dashboards.view.selector.loading);
                const selectedDash = options.find((d: any) => String(d.id) === String(selected));
                return (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Icon fontSize="small" color="primary">dashboard</Icon>
                        <Typography sx={{ textTransform: 'none', fontWeight: 500 }}>
                            {selectedDash ? selectedDash.titol : currentDashboardTitol}
                        </Typography>
                    </Box>
                );
            }}
            sx={{
                height: 45,
                minWidth: 350,
                backgroundColor: 'background.paper',
                borderRadius: 1,
                boxShadow: 1
            }}
            MenuProps={{
                PaperProps: { sx: { maxHeight: 400 } }
            }}
        >
            {options.map((dash) => (
                <MenuItem key={dash.id} value={dash.id}>
                    {dash.titol}
                </MenuItem>
            ))}
            <Divider sx={{ my: 1 }} />
            <MenuItem value="__VIEW_ALL__">
                <ListItemIcon>
                    <Icon fontSize="small" color="primary">list</Icon>
                </ListItemIcon>
                <ListItemText
                    primary={t($ => $.page.dashboards.view.selector.seeAll)}
                    primaryTypographyProps={{ fontWeight: 600, color: 'primary.main' }}
                />
            </MenuItem>
        </Select>
    );
}

function useDashboardManagerDialog(refreshFavorites: () => void) {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const gridApiRef = useMuiDataGridApiRef();

    const refreshGrid = useCallback(() => {
        gridApiRef.current?.refresh?.();
        refreshFavorites();
    }, [gridApiRef, refreshFavorites]);

    const columns = useMemo(() => [
        { field: 'titol', headerName: t($ => $.page.dashboards.view.columns.titol), flex: 1 },
        { field: 'descripcio', headerName: t($ => $.page.dashboards.view.columns.descripcio), flex: 2 },
        {
            field: 'esPreferit',
            headerName: t($ => $.page.dashboards.view.columns.esPreferit),
            sortable: false,
            flex: 0.5,
            renderCell: (params: any) => (
                <DashboardPreferitToggle
                    dashboardId={params.id}
                    isPreferit={params.row.esPreferit}
                    onRefresh={refreshGrid}
                />
            ),
        },
    ], [t, refreshGrid]);

    const handleRowClick = (params: any) => {
        navigate(`/${ESTADISTIQUES_PATH}/${params.id}`);
        setOpen(false);
    };

    const dialog = (
        <Dialog
            open={open}
            closeCallback={() => setOpen(false)}
            componentProps={{ maxWidth: 'xl', fullWidth: true }}
        >
            <Box sx={{ mt: 2, height: '750px' }}>
                <MuiDataGrid
                    title={t($ => $.page.dashboards.title)}
                    resourceName="dashboard"
                    columns={columns}
                    apiRef={gridApiRef}
                    perspectives={['PREFERIT_USUARI_ACTUAL']}
                    paginationActive
                    onRowClick={handleRowClick}
                    sx={{ cursor: 'pointer' }}
                    readOnly
                />
            </Box>
        </Dialog>
    );

    return { dialog, openDialog: () => setOpen(true) };
}

const EstadisticaDashboardView = () => {
    const { t } = useTranslation();
    const theme = useTheme();
    const temaFosc = theme.palette.mode === 'dark';
    const routeParams = useParams();
    const [firstDashboard, setFirstDashboard] = useState<Record<string, unknown> | string | null>(null);
    const idFromFirstDashboard: string | null =
        (firstDashboard as { id?: string | number })?.id != null ? String((firstDashboard as { id?: string | number }).id) : null;
    const dashboardIdFromRouteAndLocalStorage =
        routeParams.id ?? localStorage.getItem(LAST_VIEWED_STORAGE_KEY);
    const dashboardId = dashboardIdFromRouteAndLocalStorage ?? idFromFirstDashboard;
    const {
        dashboard,
        loading: loadingDashboard,
        exception: dashboardException,
    } = useDashboard(dashboardId);
    const { entornCodi: dashboardEntornCodi, loading: loadingEntornCodi } = useEntornCodi(dashboard?.entorn?.id);
    const [filtreSeleccio, setFiltreSeleccio] = useState<DashboardFiltreSeleccio>({});
    useEffect(() => {
        // En canviar de dashboard, la selecció de filtres de l'anterior ja no és vàlida (dimensions diferents).
        setFiltreSeleccio({});
    }, [dashboardId]);
    const { dashboardWidgets, loadingWidgetPositions } = useDashboardWidgets(dashboardId, temaFosc, filtreSeleccio);
    const [largeScreenMode, setLargeScreenMode] = useStoredLargeScreenMode(LARGE_SCREEN_MODE_STORAGE_KEY);
    const { isReady: apiDashboardIsReady, find: findDashboard } = useResourceApiService('dashboard');
    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);
    const navigate = useNavigate();
    const { favorites, refreshFavorites } = useDashboardFavorites();
    const { dialog: managerDialog, openDialog: openManagerDialog } = useDashboardManagerDialog(refreshFavorites);
    const dashboardSelector = useDashboardSelector(dashboardId, dashboard?.titol, favorites, openManagerDialog);

    const loading = loadingDashboard || loadingWidgetPositions || loadingEntornCodi;

    useEffect(() => {
        if (apiDashboardIsReady && dashboardIdFromRouteAndLocalStorage == null && firstDashboard == null) {
            findDashboard({ size: 1 }).then((dashboardResponse) => {
                const resultFirstDashboard = dashboardResponse.rows[0];
                setFirstDashboard(resultFirstDashboard ?? NO_DASHBOARD_FOUND);
            });
        }
    }, [apiDashboardIsReady, dashboardId, dashboardIdFromRouteAndLocalStorage, firstDashboard, findDashboard]);

    useEffect(() => {
        if (dashboardId != null) localStorage.setItem(LAST_VIEWED_STORAGE_KEY, dashboardId);
    }, [dashboardId]);

    const returnToDefaultDashboardAndClear = () => {
        localStorage.removeItem(LAST_VIEWED_STORAGE_KEY);
        navigate(`/${ESTADISTIQUES_PATH}`);
    }

    if (dashboardException) {
        if (dashboardException.status === 404)
            return (
                <Alert severity="warning" action={<Button onClick={returnToDefaultDashboardAndClear}>{t($ => $.page.dashboards.alert.tornarTauler)}</Button>}>
                    {t($ => $.page.dashboards.alert.notExists)}
                </Alert>
            );
        else return <Alert severity="error">{t($ => $.page.dashboards.alert.carregar)}.</Alert>;
    }

    if (dashboardId == null && firstDashboard === NO_DASHBOARD_FOUND)
        return <Alert severity="warning">{t($ => $.page.dashboards.alert.notDefined)}</Alert>;

    return (
        <>
            <PageTitle title={t($ => $.page.dashboards.title)} />
            {managerDialog}
            {loading ? <CenteredCircularProgress /> : null}
            <BasePage
                toolbar={
                    <Box sx={{ width: '100%' }}>
                        <MuiToolbar
                            disableGutters
                            sx={{
                                width: '100%',
                                display: 'flex',
                                flexWrap: 'wrap',
                                justifyContent: 'space-between',
                                alignItems: 'center',
                                rowGap: 1,
                                px: 2,
                                ml: 0, mr: 0, mt: 0,
                                backgroundColor: (theme) =>
                                    theme.palette.mode === 'dark' ? theme.palette.grey['900'] : theme.palette.grey['200'],
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                {dashboardSelector}
                                {/* <Tooltip title={t($ => $.page.dashboards.view.selector.seeAll)}>
                                    <IconButton
                                        color="primary"
                                        onClick={openManagerDialog}
                                        sx={{ backgroundColor: 'background.paper', boxShadow: 1, '&:hover': { backgroundColor: 'action.hover' } }}
                                    >
                                        <Icon>list</Icon>
                                    </IconButton>
                                </Tooltip> */}
                            </Box>
                            <DashboardFiltreBar
                                filtres={dashboard?.filtres}
                                value={filtreSeleccio}
                                onChange={setFiltreSeleccio}
                                aplicacioId={dashboard?.aplicacio?.id}
                            />
                            <Tooltip title={t($ => $.page.dashboards.view.largeScreenModeFit)}>
                                <ToggleButton
                                    value="fit"
                                    size="small"
                                    selected={largeScreenMode === 'fit'}
                                    color="primary"
                                    onChange={() => setLargeScreenMode(largeScreenMode === 'fit' ? 'centered' : 'fit')}
                                    aria-label={t($ => $.page.dashboards.view.largeScreenModeFit)}
                                    sx={{ height: '32px' }}
                                >
                                    <Icon fontSize="small">aspect_ratio</Icon>
                                </ToggleButton>
                            </Tooltip>
                        </MuiToolbar>
                    </Box>
                }
            >
                {dashboardWidgets && (
                    <DashboardReactGridLayout
                        dashboardId={dashboard.id}
                        editable={false}
                        dashboardWidgets={dashboardWidgets}
                        gridLayoutItems={mappedDashboardItems}
                        dashboardEntornCodi={dashboardEntornCodi}
                        backgroundColor={temaFosc ? dashboard.colorFonsFosc : dashboard.colorFonsClar}
                        largeScreenMode={largeScreenMode}
                    />
                )}
                <FooterHeightPlaceholder />
            </BasePage>
        </>
    );
};

export default EstadisticaDashboardView;
