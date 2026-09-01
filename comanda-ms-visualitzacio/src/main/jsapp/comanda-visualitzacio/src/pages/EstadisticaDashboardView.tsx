import MenuIcon from '@mui/icons-material/Menu';
import MuiToolbar from '@mui/material/Toolbar';
import { Alert, Box, Button, Icon, ToggleButton, Tooltip, Typography } from '@mui/material';
import {
    DashboardLargeScreenMode,
    DashboardReactGridLayout,
    useMapDashboardItems,
} from '../components/estadistiques/DashboardReactGridLayout.tsx';
import { BasePage, MuiDataGrid, useCloseDialogButtons, useResourceApiService } from 'reactlib';
import { useTheme } from '@mui/material/styles';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { useNavigate, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Dialog from '../../lib/components/mui/Dialog.tsx';
import { ESTADISTIQUES_PATH } from '../AppRoutes.tsx';
import {useTranslation} from "react-i18next";
import PageTitle from '../components/PageTitle.tsx';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';
import { FooterHeightPlaceholder } from '../components/ComandaFooter.tsx';
import { useEntornCodi } from '../components/estadistiques/dashboardPlantillaHook.ts';
import DashboardFiltreBar from '../components/estadistiques/DashboardFiltreBar.tsx';
import { DashboardFiltreSeleccio } from '../types/dashboardFiltre.model.ts';

const LAST_VIEWED_STORAGE_KEY = 'lastViewedDashboardId';
const LARGE_SCREEN_MODE_STORAGE_KEY = 'comanda.dashboardView.largeScreenMode';
const NO_DASHBOARD_FOUND = 'NO_DASHBOARD_FOUND';

/**
 * Recorda, entre sessions, si l'usuari prefereix veure els dashboards a mida real de disseny (1920px,
 * centrats) o escalats per ocupar tot l'ample de pantalles més grans (vegeu DashboardLargeScreenMode).
 */
function useStoredLargeScreenMode(): [DashboardLargeScreenMode, (mode: DashboardLargeScreenMode) => void] {
    const [largeScreenMode, setLargeScreenModeState] = useState<DashboardLargeScreenMode>(() => {
        try {
            return localStorage.getItem(LARGE_SCREEN_MODE_STORAGE_KEY) === 'fit' ? 'fit' : 'centered';
        } catch {
            return 'centered';
        }
    });
    const setLargeScreenMode = (mode: DashboardLargeScreenMode) => {
        setLargeScreenModeState(mode);
        try {
            localStorage.setItem(LARGE_SCREEN_MODE_STORAGE_KEY, mode);
        } catch {
            // localStorage no disponible (mode privat, etc.): es descarta silenciosament
        }
    };
    return [largeScreenMode, setLargeScreenMode];
}

function useDashboardSelect(currentDashboardId: string | number | null) {
    const { t } = useTranslation();
    const buttons = useCloseDialogButtons();
    const [open, setOpen] = useState(false);

    const columns = [
        {
            field: 'titol',
            flex: 1,
        },
        {
            field: 'descripcio',
            flex: 2,
        },
    ];

    const dialog = (
        <Dialog
            open={open}
            buttonCallback={() => setOpen(false)}
            closeCallback={() => setOpen(false)}
            buttons={buttons}
            componentProps={{
                maxWidth: 'md',
            }}
        >
            <Box
                sx={{
                    mt: 3,
                    height: '500px',
                    width: '600px',
                }}
            >
                <MuiDataGrid
                    title={t($ => $.page.dashboards.action.select.title)}
                    resourceName="dashboard"
                    columns={columns}
                    toolbarType="upper"
                    paginationActive
                    rowLink={`/${ESTADISTIQUES_PATH}/{{id}}`}
                    onRowClick={() => setOpen(false)}
                    filter={currentDashboardId != null ? `id ! ${currentDashboardId}` : undefined}
                    readOnly
                />
            </Box>
        </Dialog>
    );

    return { dialog, open: () => setOpen(true) };
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
    const [largeScreenMode, setLargeScreenMode] = useStoredLargeScreenMode();
    const { isReady: apiDashboardIsReady, find: findDashboard } =
        useResourceApiService('dashboard');
    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);
    const { open: openDashboardSelect, dialog: dashboardSelectDialog } =
        useDashboardSelect(dashboardId);
    const navigate = useNavigate();

    const loading = loadingDashboard || loadingWidgetPositions || loadingEntornCodi;

    useEffect(() => {
        if (
            apiDashboardIsReady &&
            dashboardIdFromRouteAndLocalStorage == null &&
            firstDashboard == null
        ) {
            findDashboard({ size: 1 }).then((dashboardResponse) => {
                const resultFirstDashboard = dashboardResponse.rows[0];
                setFirstDashboard(resultFirstDashboard ?? NO_DASHBOARD_FOUND);
            });
        }
    }, [
        apiDashboardIsReady,
        dashboardId,
        dashboardIdFromRouteAndLocalStorage,
        firstDashboard,
        findDashboard,
    ]);

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
                <Alert
                    severity="warning"
                    action={
                        <Button onClick={returnToDefaultDashboardAndClear}>
                            {t($ => $.page.dashboards.alert.tornarTauler)}
                        </Button>
                    }
                >
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
            {dashboardSelectDialog}
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
                                ml: 0,
                                mr: 0,
                                mt: 0,
                                backgroundColor: (theme) =>
                                    theme.palette.mode === 'dark'
                                        ? theme.palette.grey['900']
                                        : theme.palette.grey['200'],
                            }}
                        >
                            <Button
                                color="primary"
                                variant="outlined"
                                size="small"
                                onClick={openDashboardSelect}
                                startIcon={<MenuIcon />}
                                sx={{
                                    borderRadius: 1,
                                }}
                            >
                                <Typography
                                    color="textPrimary"
                                    sx={{
                                        textTransform: 'none',
                                    }}
                                >
                                    {dashboard?.titol}
                                </Typography>
                            </Button>
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
