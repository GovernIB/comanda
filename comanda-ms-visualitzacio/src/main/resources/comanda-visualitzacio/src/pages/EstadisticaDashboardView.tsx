import MuiToolbar from '@mui/material/Toolbar';
import { Box, Button } from '@mui/material';
import CircularProgress from '@mui/material/CircularProgress';
import { AppEstadisticaTest, useMapDashboardItems, useDashboardSSE } from './DashboardTest.tsx';
import { BasePage, useResourceApiService } from 'reactlib';
import { useEffect, useMemo, useState } from 'react';

const EstadisticaDashboardView = () => {
    // const { id: dashboardId } = useParams();
    const dashboardId = 1;
    const {
        isReady: apiDashboardIsReady,
        getOne: getOneDashboard,
        artifactReport,
    } = useResourceApiService('dashboard');
    const [dashboard, setDashboard] = useState<any>();
    const [dashboardWidgets, setDashboardWidgets] = useState<any>();
    const [loading, setLoading] = useState(false);

    // Utilitza el custom hook per connexions SSE
    const { connectToSSE, sseConnected } = useDashboardSSE(dashboardId);

    useEffect(() => {
        (async () => {
            if (apiDashboardIsReady) {
                setLoading(true);

                // Primer, obtenir la informació del dashboard
                setDashboard(await getOneDashboard(dashboardId));

                // Després, assegurar que la connexió SSE s'ha establert abans de consultar la informació dels widgets
                try {
                    // Wait for the SSE connection to be established
                    await connectToSSE();
                    console.log('SSE connection established, requesting widget data');

                    // Now that the SSE connection is established, request the widget data
                    const widgetsDataResponse = (await artifactReport(dashboardId, {
                        code: 'widgets_data',
                    })) as any[];

                    setDashboardWidgets(widgetsDataResponse.filter((widget: any) => !widget.error));
                } catch (error) {
                    console.error('Error establishing SSE connection:', error);
                } finally {
                    setLoading(false);
                }
            }
        })();
    }, [dashboardId, apiDashboardIsReady, connectToSSE]);

    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);


    if (!dashboard) return 'Loading';
    return (
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
            {loading ? (
                <Box
                    sx={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                    }}
                >
                    <CircularProgress />
                </Box>
            ) : null}
            {!dashboardWidgets ? (
                'Loading' // TODO
            ) : (
                <AppEstadisticaTest
                    dashboardId={dashboard.id}
                    editable={false}
                    dashboardWidgets={dashboardWidgets}
                    gridLayoutItems={mappedDashboardItems}
                />
            )}
        </BasePage>
    );
};

export default EstadisticaDashboardView;
