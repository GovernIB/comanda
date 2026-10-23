import MuiToolbar from '@mui/material/Toolbar';
import {
    Alert,
    Box,
    Button,
    Chip,
    Icon,
    IconButton,
    ToggleButton,
    Tooltip,
    useMediaQuery,
} from '@mui/material';
import {
    DashboardReactGridLayout,
    useMapDashboardItems,
    useStoredLargeScreenMode,
} from '../components/estadistiques/DashboardReactGridLayout.tsx';
import {
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
import { ScreenFrame } from '../lot2';
import { screenIcons } from '../util/screenIcons.ts';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';
import { useEntornCodi } from '../components/estadistiques/dashboardPlantillaHook.ts';
import DashboardFiltreBar from '../components/estadistiques/DashboardFiltreBar.tsx';
import { DashboardFiltreSeleccio } from '../types/dashboardFiltre.model.ts';

const LAST_VIEWED_STORAGE_KEY = 'lastViewedDashboardId';
const LARGE_SCREEN_MODE_STORAGE_KEY = 'comanda.dashboardView.largeScreenMode';
const NO_DASHBOARD_FOUND = 'NO_DASHBOARD_FOUND';

interface DashboardFavoriteItem {
    id: number | string;
    titol: string;
    descripcio?: string;
    esPreferit?: boolean;
}

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
        } catch (error: unknown) {
            temporalMessageShow(null, (error as Error)?.message || 'Error', 'error');
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
    const [favorites, setFavorites] = useState<DashboardFavoriteItem[]>([]);

    const refreshFavorites = useCallback(async () => {
        if (!apiIsReady) return;
        try {
            const response = await findDashboards({ namedQueries: ['preferit'], size: 100 });
            setFavorites((response.rows as DashboardFavoriteItem[]) || []);
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
    favorites: DashboardFavoriteItem[],
    onSeeAll: () => void,
    onSelectDashboard?: (id: string | number, titol?: string) => void
) {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const options = useMemo(() => {
        if (currentDashboardId == null || currentDashboardId === '') return favorites;
        const currentIsInFavs = favorites.some((d) => String(d.id) === String(currentDashboardId));
        if (!currentIsInFavs) {
            return [
                {
                    id: currentDashboardId,
                    titol: currentDashboardTitol || t($ => $.page.dashboards.view.selector.loading),
                },
                ...favorites,
            ];
        }
        return favorites;
    }, [favorites, currentDashboardId, currentDashboardTitol, t]);

    return (
        <Box
            sx={{
                display: 'flex',
                alignItems: 'center',
                flex: '1 1 auto',
                flexWrap: 'nowrap',
                minWidth: 280,
                gap: 1,
                overflow: 'hidden',
                py: 0.5,
            }}
        >
            <Tooltip title={t($ => $.page.dashboards.view.selector.seeAll)}>
                <Button
                    size="small"
                    variant="outlined"
                    color="primary"
                    startIcon={
                        <Icon fontSize="small">
                            list
                        </Icon>
                    }
                    aria-label={t($ => $.page.dashboards.view.selector.listDashboards)}
                    onClick={onSeeAll}
                    sx={{
                        height: 32,
                        flexShrink: 0,
                        whiteSpace: 'nowrap',
                        backgroundColor: 'background.paper',
                        fontWeight: 600,
                        boxShadow: 1,
                        '&:hover': {
                            backgroundColor: 'action.hover',
                        },
                    }}
                >
                    {t($ => $.page.dashboards.view.selector.listDashboards)}
                </Button>
            </Tooltip>

            {options.map(dash => {
                const isSelected =
                    currentDashboardId != null &&
                    String(dash.id) === String(currentDashboardId);
                const titol =
                    isSelected && currentDashboardTitol
                        ? currentDashboardTitol
                        : dash.titol || String(dash.id);
                const isFavorite =
                    favorites.some((fav) => String(fav.id) === String(dash.id)) ||
                    Boolean((dash as { esPreferit?: boolean }).esPreferit);
                return (
                    <Tooltip key={dash.id} title={titol}>
                        <Chip
                            icon={
                                isFavorite ? (
                                    <Icon
                                        fontSize="small"
                                        color="warning"
                                        aria-hidden="true"
                                        data-testid="favorite-chip-icon"
                                    >
                                        star
                                    </Icon>
                                ) : undefined
                            }
                            label={titol}
                            onClick={() => {
                                onSelectDashboard?.(dash.id, dash.titol);
                                navigate(`/${ESTADISTIQUES_PATH}/${dash.id}`);
                            }}
                            color={isSelected ? 'primary' : 'default'}
                            variant={isSelected ? 'filled' : 'outlined'}
                            clickable
                            aria-current={isSelected ? 'page' : undefined}
                            sx={{
                                maxWidth: 200,
                                flexShrink: 0,
                                cursor: 'pointer',
                                fontWeight: isSelected ? 600 : 400,
                                backgroundColor: isSelected ? undefined : 'background.paper',
                                '& .MuiChip-label': {
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    whiteSpace: 'nowrap',
                                },
                            }}
                        />
                    </Tooltip>
                );
            })}
        </Box>
    );
}

function useDashboardManagerDialog(
    refreshFavorites: () => void,
    onSelectDashboard?: (id: string | number, titol?: string) => void
) {
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
            renderCell: (params: { id: string | number; row: { esPreferit?: boolean } }) => (
                <DashboardPreferitToggle
                    dashboardId={params.id}
                    isPreferit={Boolean(params.row.esPreferit)}
                    onRefresh={refreshGrid}
                />
            ),
        },
    ], [t, refreshGrid]);

    const handleRowClick = (params: { id: string | number; row?: { titol?: string } }) => {
        onSelectDashboard?.(params.id, params.row?.titol);
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

    const openDialog = useCallback(() => setOpen(true), []);
    return { dialog, openDialog };
}

const EstadisticaDashboardView = () => {
    const { t } = useTranslation();
    const theme = useTheme();
    const temaFosc = theme.palette.mode === 'dark';
    const isSmallScreen = useMediaQuery(theme.breakpoints.down('lg'));
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

    const [knownDashboardTitles, setKnownDashboardTitles] = useState<Record<string, string>>({});
    const handleSelectDashboard = useCallback((id: string | number, titol?: string) => {
        if (titol) {
            setKnownDashboardTitles((prev) => ({ ...prev, [String(id)]: titol }));
        }
    }, []);

    const isCurrentDashboardLoaded =
        dashboard?.id != null && String(dashboard.id) === String(dashboardId);
    const currentDashboardTitol = isCurrentDashboardLoaded
        ? dashboard.titol
        : (dashboardId != null ? knownDashboardTitles[String(dashboardId)] : undefined);

    const { dialog: managerDialog, openDialog: openManagerDialog } = useDashboardManagerDialog(
        refreshFavorites,
        handleSelectDashboard
    );
    const dashboardSelector = useDashboardSelector(
        dashboardId,
        currentDashboardTitol,
        favorites,
        openManagerDialog,
        handleSelectDashboard
    );

    const loading =
        loadingDashboard ||
        loadingWidgetPositions ||
        loadingEntornCodi ||
        (dashboardId != null && !isCurrentDashboardLoaded);

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
                <ScreenFrame icon={screenIcons.dashboard} title={t($ => $.page.dashboards.title)} padded>
                    <Alert severity="warning" action={<Button onClick={returnToDefaultDashboardAndClear}>{t($ => $.page.dashboards.alert.tornarTauler)}</Button>}>
                        {t($ => $.page.dashboards.alert.notExists)}
                    </Alert>
                </ScreenFrame>
            );
        else return (
            <ScreenFrame icon={screenIcons.dashboard} title={t($ => $.page.dashboards.title)} padded>
                <Alert severity="error">{t($ => $.page.dashboards.alert.carregar)}.</Alert>
            </ScreenFrame>
        );
    }

    if (dashboardId == null && firstDashboard === NO_DASHBOARD_FOUND)
        return (
            <ScreenFrame icon={screenIcons.dashboard} title={t($ => $.page.dashboards.title)} padded>
                <Alert severity="warning">{t($ => $.page.dashboards.alert.notDefined)}</Alert>
            </ScreenFrame>
        );

    return (
        <>
            <PageTitle title={t($ => $.page.dashboards.title)} />
            {managerDialog}
            <ScreenFrame icon={screenIcons.dashboard} title={t($ => $.page.dashboards.title)}>
                {loading ? <CenteredCircularProgress /> : null}
                <Box sx={{ width: '100%' }}>
                    <MuiToolbar
                        disableGutters
                        sx={{
                            width: '100%',
                            display: 'flex',
                            flexWrap: isSmallScreen ? 'wrap' : 'nowrap',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            rowGap: 1,
                            columnGap: 2,
                            px: 2,
                            pt: 2,
                            pb: 1,
                            ml: 0, mr: 0, mt: 0,
                            backgroundColor: (theme) =>
                                theme.palette.mode === 'dark' ? theme.palette.grey['900'] : theme.palette.grey['200'],
                        }}
                    >
                        {dashboardSelector}
                        <DashboardFiltreBar
                            filtres={isCurrentDashboardLoaded ? dashboard?.filtres : undefined}
                            value={filtreSeleccio}
                            onChange={setFiltreSeleccio}
                            aplicacioId={isCurrentDashboardLoaded ? dashboard?.aplicacio?.id : undefined}
                            wrap={isSmallScreen ? 'wrap' : 'nowrap'}
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
                <Box sx={{ flex: 1, minHeight: 0, overflow: 'auto', p: '16px', boxSizing: 'border-box' }}>
                    {isCurrentDashboardLoaded && dashboardWidgets && (
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
                </Box>
            </ScreenFrame>
        </>
    );
};

export default EstadisticaDashboardView;
