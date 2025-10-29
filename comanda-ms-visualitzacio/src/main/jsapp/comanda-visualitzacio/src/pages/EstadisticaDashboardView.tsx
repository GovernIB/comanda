import MenuIcon from '@mui/icons-material/Menu';
import MuiToolbar from '@mui/material/Toolbar';
import { Alert, Box, Button, Typography } from '@mui/material';
import CircularProgress from '@mui/material/CircularProgress';
import {
    DashboardReactGridLayout,
    useMapDashboardItems,
} from '../components/estadistiques/DashboardReactGridLayout.tsx';
import { BasePage, MuiGrid, useCloseDialogButtons, useResourceApiService } from 'reactlib';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { useNavigate, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Dialog from '../../lib/components/mui/Dialog.tsx';
import { ESTADISTIQUES_PATH } from '../AppRoutes.tsx';
import {useTranslation} from "react-i18next";

const LAST_VIEWED_STORAGE_KEY = 'lastViewedDashboardId';
const NO_DASHBOARD_FOUND = 'NO_DASHBOARD_FOUND';

function useDashboardSelect(currentDashboardId: any) {
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
                <MuiGrid
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
    const routeParams = useParams();
    const [firstDashboard, setFirstDashboard] = useState<any>(null);
    const idFromFirstDashboard: string | null =
        firstDashboard?.id != null ? String(firstDashboard.id) : null;
    const dashboardIdFromRouteAndLocalStorage =
        routeParams.id ?? localStorage.getItem(LAST_VIEWED_STORAGE_KEY);
    const dashboardId = dashboardIdFromRouteAndLocalStorage ?? idFromFirstDashboard;
    const {
        dashboard,
        loading: loadingDashboard,
        exception: dashboardException,
    } = useDashboard(dashboardId);
    const { dashboardWidgets, loadingWidgetPositions } = useDashboardWidgets(dashboardId);
    const { isReady: apiDashboardIsReady, find: findDashboard } =
        useResourceApiService('dashboard');
    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);
    const { open: openDashboardSelect, dialog: dashboardSelectDialog } =
        useDashboardSelect(dashboardId);
    const navigate = useNavigate();

    const loading = loadingDashboard || loadingWidgetPositions;

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
            {dashboardSelectDialog}
            {loading ? (
                <Box
                    sx={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                        zIndex: 10,
                    }}
                >
                    <CircularProgress />
                </Box>
            ) : null}
            <BasePage
                toolbar={
                    <MuiToolbar
                        disableGutters
                        sx={{
                            width: '100%',
                            display: 'flex',
                            px: 2,
                            ml: 0,
                            mr: 0,
                            mt: 0,
                            backgroundColor: (theme) => theme.palette.grey[200],
                        }}
                    >
                        <Button
                            color="primary"
                            variant="outlined"
                            size="small"
                            onClick={openDashboardSelect}
                            endIcon={<MenuIcon />}
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
                    </MuiToolbar>
                }
            >
                {dashboardWidgets && (
                    <DashboardReactGridLayout
                        dashboardId={dashboard.id}
                        editable={false}
                        dashboardWidgets={dashboardWidgets}
                        gridLayoutItems={mappedDashboardItems}
                    />
                )}
            </BasePage>
        </>
    );
};

export default EstadisticaDashboardView;
