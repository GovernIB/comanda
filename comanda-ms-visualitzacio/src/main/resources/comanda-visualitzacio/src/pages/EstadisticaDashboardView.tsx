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
    const {
        isReady: apiDashboardItemIsReady,
        // getOne: getOneDashboardItem,
        artifactReport: artifactReportDashboardItem,
    } = useResourceApiService('dashboardItem');
    const [dashboard, setDashboard] = useState<any>();
    const [dashboardWidgets, setDashboardWidgets] = useState<any>();
    const [loading, setLoading] = useState(false);

    // // Utilitza el custom hook per connexions SSE
    // const { connectToSSE, sseConnected } = useDashboardSSE(dashboardId);

    useEffect(() => {
        (async () => {
            if (apiDashboardIsReady && apiDashboardItemIsReady) {
                setLoading(true);

                // Primer, obtenir la informació del dashboard
                setDashboard(await getOneDashboard(dashboardId));

                const widgetsDataResponse = (await artifactReport(dashboardId, {
                    code: 'widgets_data',
                })) as any[];
                setDashboardWidgets(widgetsDataResponse);

                // Després, carregar la informació de cada un dels items
                // Iterar sobre cada widget i processar-lo individualment
                widgetsDataResponse.filter((widget: any) => widget.tipus !== 'TITOL')
                    .forEach(async (widget) => {
                        console.log('WIDGET', widget);
                        const dashboardItemData = await artifactReportDashboardItem(widget.dashboardItemId, {code: 'widget_data'}) as any[];
                        const firstDashboardItemData = dashboardItemData[0];
                        if(!firstDashboardItemData)
                            return;
                        // Actualitzar el llistat de dashboardWidgets a mesura que es reben les dades
                        setDashboardWidgets((prevWidgets) => prevWidgets.map((item) => (widget.dashboardItemId === item.dashboardItemId ? {
                            ...firstDashboardItemData,
                            loading: false
                        } : item)));
                    });
                console.log("Widget END")
                setLoading(false);
            }
        })();
    }, [dashboardId, apiDashboardIsReady, apiDashboardItemIsReady]);
    console.log("dashboardWidgets2", dashboardWidgets)
    //         try {
    //             const data = JSON.parse(event.data);
    //             const dashboardItemId = data.dashboardItemId;
    //             const widgetItem = data.informeWidgetItem;
    //             const tempsCarrega = data.tempsCarrega;
    //             console.log('SSE item carregat:', dashboardItemId, tempsCarrega);
    //             const trobatIndex = dashboardWidgets.findIndex(
    //                 (dashboardWidget) => dashboardWidget.dashboardItemId === dashboardItemId
    //             );
    //             if (trobatIndex !== -1) {
    //                 // Crea un nou array amb el widget actualitzat
    //                 const updatedWidgets = [...dashboardWidgets];
    //                 // Crea un nou objecte pel widget actualitzat
    //                 updatedWidgets[trobatIndex] = {
    //                     ...dashboardWidgets[trobatIndex],
    //                     ...widgetItem,
    //                     loading: false
    //                 };
    //                 // Actualitza el l'array de dashboardWidgets amb el nou array
    //                 dashboardWidgets.splice(0, dashboardWidgets.length, ...updatedWidgets);
    //                 // Força un re-render per actualitzar els widgets
    //                 setForceUpdate(prev => prev + 1);
    //             } else {
    //                 console.log('Widget no trobat:', widgetItem);
    //             }
    //         } catch (error) {
    //             console.error(`Error processant SSE: ${sseDashboardItemLoadedKey}`, error);
    //         }

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
