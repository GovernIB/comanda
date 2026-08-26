import { springFilterBuilder, useResourceApiService } from 'reactlib';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { horizontalSubdivisions } from '../components/estadistiques/DashboardReactGridLayout';
import { DashboardFiltre, DashboardFiltreSeleccio } from '../types/dashboardFiltre.model.ts';

export const useDashboard = (dashboardId: any) => {
    type RequestStateType = {
        loading: boolean;
        dashboard?: any;
        exception?: any;
    };

    const { isReady: apiDashboardIsReady, getOne: getOneDashboard } =
        useResourceApiService('dashboard');
    const [requestState, setRequestState] = useState<RequestStateType>({
        loading: false,
    });
    const effectFunction = useCallback(() => {
        let cancelRequests = false;
        (async () => {
            if (apiDashboardIsReady && dashboardId != null) {
                try {
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
                        exception: null,
                        loading: false,
                    }));
                } catch (exception) {
                    if (cancelRequests) return;
                    setRequestState((prevState) => ({
                        ...prevState,
                        exception,
                        dashboard: null,
                        loading: false,
                    }));
                }
            }
        })();
        return () => {
            cancelRequests = true;
        };
    }, [dashboardId, apiDashboardIsReady, getOneDashboard]);

    useEffect(effectFunction, [effectFunction]);

    const forceRefresh = useCallback(() => {
        effectFunction();
    }, [effectFunction]);

    return { ...requestState, forceRefresh };
};

export const useDashboardWidgets = (
    dashboardId: any,
    temaFosc = false,
    filtreSeleccio?: DashboardFiltreSeleccio
) => {
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
            if (apiDashboardIsReady && apiDashboardItemIsReady && dashboardId != null) {
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    loadingWidgetPositions: true,
                    loadingWidgetData: true,
                }));

                const widgetsPositionResponse = (await artifactReport(dashboardId, {
                    code: 'widgets_data', data: { temaFosc }
                })) as any[];
                if (cancelRequests) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    loadingWidgetPositions: false,
                    widgets: widgetsPositionResponse.sort((a, b) => {
                        const getPosicioValor = (widget: any) => widget.posY * horizontalSubdivisions + widget.posX;
                        return getPosicioValor(a) - getPosicioValor(b);
                    }),
                }));

                // Després, carregar la informació de cada un dels items
                // Iterar sobre cada widget i processar-lo individualment
                const widgetsDataPromises = widgetsPositionResponse
                    .filter((widget: any) => widget.tipus !== 'TITOL')
                    .map(async (widget) => {
                        const dashboardItemData = (await artifactReportDashboardItem(
                            widget.dashboardItemId,
                            { code: 'widget_data', data: { temaFosc, filtreSeleccio } }
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
    }, [dashboardId, temaFosc, filtreSeleccio, apiDashboardIsReady, apiDashboardItemIsReady]);

    useEffect(effectFunction, [effectFunction]);

    const forceRefresh = useCallback(() => {
        effectFunction();
    }, [effectFunction]);

    /** Refresca només un widget (no cal recarregar tot el dashboard quan només s'ha modificat un component existent) */
    const refreshWidget = useCallback((dashboardItemId: any) => {
        if (!apiDashboardItemIsReady || dashboardItemId == null) return;
        artifactReportDashboardItem(dashboardItemId, { code: 'widget_data', data: { temaFosc, filtreSeleccio } })
            .then((dashboardItemData: any) => {
                const firstDashboardItemData = (dashboardItemData as any[])?.[0];
                if (!firstDashboardItemData) return;
                setRequestState((prevState) => ({
                    ...prevState,
                    widgets: prevState.widgets?.map((item: any) =>
                        String(item.dashboardItemId) === String(dashboardItemId)
                            ? { ...firstDashboardItemData, loading: false }
                            : item
                    ),
                }));
            });
    }, [apiDashboardItemIsReady, artifactReportDashboardItem, temaFosc, filtreSeleccio]);

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
        refreshWidget,
    };
};

/**
 * Filtres de capçalera configurats per a un dashboard (recurs `dashboardFiltre`), usats pel dissenyador per
 * llistar-los i editar-los al panell esquerre; la visualització del dashboard (DashboardFiltreBar) en canvi
 * ja els rep incrustats a `dashboard.filtres` (vegeu Dashboard.java al backend).
 */
export const useDashboardFiltres = (dashboardId: any) => {
    const { isReady, find } = useResourceApiService('dashboardFiltre');
    const [dashboardFiltres, setDashboardFiltres] = useState<DashboardFiltre[]>();
    const [loading, setLoading] = useState(false);

    const effectFunction = useCallback(() => {
        let cancelled = false;
        if (isReady && dashboardId != null) {
            setLoading(true);
            find({
                filter: springFilterBuilder.eq('dashboard.id', dashboardId),
                sorts: ['ordre'],
                unpaged: true,
            })
                .then((response) => {
                    if (cancelled) return;
                    setDashboardFiltres(response.rows as DashboardFiltre[]);
                })
                .finally(() => {
                    if (!cancelled) setLoading(false);
                });
        }
        return () => {
            cancelled = true;
        };
    }, [isReady, dashboardId]);

    useEffect(effectFunction, [effectFunction]);

    const forceRefresh = useCallback(() => {
        effectFunction();
    }, [effectFunction]);

    return { dashboardFiltres, loading, forceRefresh };
};
