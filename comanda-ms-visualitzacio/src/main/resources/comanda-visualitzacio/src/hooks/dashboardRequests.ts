import { useResourceApiService } from 'reactlib';
import { useCallback, useEffect, useMemo, useState } from 'react';

export const useDashboard = (dashboardId: any) => {
    type RequestStateType = {
        loading: boolean;
        dashboard?: any;
    };

    const { isReady: apiDashboardIsReady, getOne: getOneDashboard } =
        useResourceApiService('dashboard');
    const [requestState, setRequestState] = useState<RequestStateType>({
        loading: false,
    });
    useEffect(() => {
        let cancelRequests = false;
        (async () => {
            if (apiDashboardIsReady) {
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    loading: true,
                }));

                const dashboardData = await getOneDashboard(dashboardId);
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    dashboard: dashboardData,
                    loading: false,
                }));
            }
        })();
        return () => {
            cancelRequests = true;
        };
    }, [dashboardId, apiDashboardIsReady]);
    return requestState;
};

export const useDashboardWidgets = (dashboardId: any) => {
    type RequestStateType = {
        loadingWidgetPositions: boolean;
        loadingWidgetData: boolean;
        widgets?: any;
    };

    const { isReady: apiDashboardIsReady, artifactReport } = useResourceApiService('dashboard');
    const { isReady: apiDashboardItemIsReady, artifactReport: artifactReportDashboardItem } =
        useResourceApiService('dashboardItem');
    const [requestState, setRequestState] = useState<RequestStateType>({
        loadingWidgetPositions: false,
        loadingWidgetData: false,
    });
    const effectFunction = useCallback(() => {
        let cancelRequests = false;
        (async () => {
            if (apiDashboardIsReady && apiDashboardItemIsReady) {
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    loadingWidgetPositions: true,
                    loadingWidgetData: true,
                }));

                const widgetsPositionResponse = (await artifactReport(dashboardId, {
                    code: 'widgets_data',
                })) as any[];
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    loadingWidgetPositions: false,
                    widgets: widgetsPositionResponse,
                }));

                // Després, carregar la informació de cada un dels items
                // Iterar sobre cada widget i processar-lo individualment
                const widgetsDataPromises = widgetsPositionResponse
                    .filter((widget: any) => widget.tipus !== 'TITOL')
                    .map(async (widget) => {
                        console.log('WIDGET', widget);
                        const dashboardItemData = (await artifactReportDashboardItem(
                            widget.dashboardItemId,
                            { code: 'widget_data' }
                        )) as any[];
                        const firstDashboardItemData = dashboardItemData[0];
                        if (!firstDashboardItemData) return;
                        // Actualitzar el llistat de dashboardWidgets a mesura que es reben les dades
                        if (cancelRequests) return;
                        setRequestState((prevState) => ({
                            ...prevState,
                            widgets: prevState.widgets.map((item: any) =>
                                widget.dashboardItemId === item.dashboardItemId
                                    ? {
                                          ...firstDashboardItemData,
                                          loading: false,
                                      }
                                    : item
                            ),
                        }));
                    });
                await Promise.all(widgetsDataPromises);
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    loadingWidgetData: false,
                }));
            }
        })();
        return () => {
            cancelRequests = true;
        };
    }, [dashboardId, apiDashboardIsReady, apiDashboardItemIsReady]);

    useEffect(effectFunction, [effectFunction]);

    const forceRefresh = useCallback(() => {
        effectFunction();
    }, [effectFunction]);

    const errorDashboardWidgets = useMemo(
        () => requestState.widgets?.filter((widget: any) => widget.error),
        [requestState.widgets]
    );
    return {
        dashboardWidgets: requestState.widgets,
        errorDashboardWidgets,
        loadingWidgetPositions: requestState.loadingWidgetPositions,
        loadingWidgetData: requestState.loadingWidgetData,
        forceRefresh,
    };
};
