import MuiToolbar from '@mui/material/Toolbar';
import { Box } from '@mui/material';
import CircularProgress from '@mui/material/CircularProgress';
import { DashboardReactGridLayout, useMapDashboardItems } from '../components/estadistiques/DashboardReactGridLayout.tsx';
import { BasePage } from 'reactlib';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { useParams } from 'react-router-dom';
import { useEffect } from 'react';

const LAST_VIEWED_STORAGE_KEY = 'lastViewedDashboardId';

const EstadisticaDashboardView = () => {
    const routeParams = useParams();
    const dashboardId = routeParams.id ?? localStorage.getItem(LAST_VIEWED_STORAGE_KEY); // TODO Tratar null y selector de dashboard
    const { dashboard, loading: loadingDashboard } = useDashboard(dashboardId);
    const { dashboardWidgets, loadingWidgetPositions } = useDashboardWidgets(dashboardId);
    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);

    const loading = loadingDashboard || loadingWidgetPositions;

    useEffect(() => {
        localStorage.setItem(LAST_VIEWED_STORAGE_KEY, dashboardId);
    }, [dashboardId]);

    return (
        <>
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
            {dashboard && (
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
                            {dashboard.titol}
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
            )}
        </>
    );
};

export default EstadisticaDashboardView;
