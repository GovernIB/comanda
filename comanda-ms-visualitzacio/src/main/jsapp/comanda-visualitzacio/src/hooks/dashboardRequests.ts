import { springFilterBuilder, useResourceApiService } from 'reactlib';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { pick } from 'lodash';
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

/**
 * Camps d'identitat i posició d'un widget que es conserven en refrescar-ne les dades: el backend no envia els camps
 * `null` (spring.jackson.default-property-inclusion=non_null), de manera que fer merge de la resposta sobre l'ítem
 * antic deixaria el valor anterior d'un camp que l'usuari acaba de buidar (p. ex. la descripció). Tota la resta de
 * camps s'agafa només de la resposta.
 */
const WIDGET_IDENTITY_KEYS = ['tipus', 'dashboardItemId', 'dashboardTitolId', 'widgetId', 'posX', 'posY', 'width', 'height', 'destacat'];

export const useDashboardWidgets = (
    dashboardId: any,
    temaFosc = false,
    filtreSeleccio?: DashboardFiltreSeleccio,
    /** Només la pantalla de disseny ha de demanar (i mostrar) la traça real de l'error de backend. */
    traceEnabled = false
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
                        // Cada widget es carrega individualment: si la petició d'un falla (p. ex. error SQL
                        // al backend que no s'ha pogut convertir en una resposta 200 amb error:true), no s'ha
                        // de perdre silenciosament — es marca aquest widget concret com a erroni perquè
                        // GraficWidgetVisualization/WidgetErrorDisplay el puguin mostrar.
                        let widgetResult: any;
                        try {
                            const dashboardItemData = (await artifactReportDashboardItem(
                                widget.dashboardItemId,
                                {
                                    code: 'widget_data',
                                    data: { temaFosc, filtreSeleccio },
                                    ...(traceEnabled ? { urlData: { trace: 'true' } } : {}),
                                }
                            )) as any[];
                            widgetResult = dashboardItemData[0];
                            // El backend pot retornar error:true en una resposta 200 (p.ex. error SQL capturat
                            // internament en generar les dades) amb errorTrace ja ple amb la traça real: la
                            // visualització no l'ha de mostrar mai, només la pantalla de disseny (traceEnabled).
                            if (widgetResult?.error && !traceEnabled) {
                                widgetResult = { ...widgetResult, errorTrace: undefined };
                            }
                        } catch (exception: any) {
                            widgetResult = {
                                error: true,
                                errorMsg: exception?.message,
                                errorTrace: exception?.stackTrace ?? exception?.description,
                            };
                        }
                        if (!widgetResult) return;
                        // Actualitzar el llistat de dashboardWidgets a mesura que es reben les dades
                        if (cancelRequests) return;
                        setRequestState((prevState) => ({
                            ...prevState,
                            widgets: prevState.widgets.map((item: any) =>
                                widget.dashboardItemId === item.dashboardItemId
                                    ? {
                                          ...item,
                                          ...widgetResult,
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
    }, [dashboardId, temaFosc, filtreSeleccio, traceEnabled, apiDashboardIsReady, apiDashboardItemIsReady]);

    useEffect(effectFunction, [effectFunction]);

    const forceRefresh = useCallback(() => {
        effectFunction();
    }, [effectFunction]);

    /** Refresca només un widget (no cal recarregar tot el dashboard quan només s'ha modificat un component existent) */
    const refreshWidget = useCallback((dashboardItemId: any) => {
        if (!apiDashboardItemIsReady || dashboardItemId == null) return;
        artifactReportDashboardItem(dashboardItemId, {
            code: 'widget_data',
            data: { temaFosc, filtreSeleccio },
            ...(traceEnabled ? { urlData: { trace: 'true' } } : {}),
        })
            .then((dashboardItemData: any) => {
                let firstDashboardItemData = (dashboardItemData as any[])?.[0];
                if (!firstDashboardItemData) return;
                if (firstDashboardItemData.error && !traceEnabled) {
                    firstDashboardItemData = { ...firstDashboardItemData, errorTrace: undefined };
                }
                setRequestState((prevState) => ({
                    ...prevState,
                    widgets: prevState.widgets?.map((item: any) =>
                        String(item.dashboardItemId) === String(dashboardItemId)
                            ? { ...pick(item, WIDGET_IDENTITY_KEYS), ...firstDashboardItemData, loading: false }
                            : item
                    ),
                }));
            })
            .catch((exception: any) => {
                // Vegeu el comentari equivalent a useDashboardWidgets: si la petició falla no s'ha de perdre
                // silenciosament, s'ha de marcar aquest widget com a erroni.
                setRequestState((prevState) => ({
                    ...prevState,
                    widgets: prevState.widgets?.map((item: any) =>
                        String(item.dashboardItemId) === String(dashboardItemId)
                            ? { ...item, error: true, errorMsg: exception?.message, errorTrace: exception?.stackTrace ?? exception?.description, loading: false }
                            : item
                    ),
                }));
            });
    }, [apiDashboardItemIsReady, artifactReportDashboardItem, temaFosc, filtreSeleccio, traceEnabled]);

    /**
     * Actualitza localment (sense demanar res al backend) la posició i mida dels elements indicats. S'usa quan el
     * canvas els ha mogut/redimensionat: si l'estat no s'actualitza, el següent moviment es compara contra la
     * posició antiga i, en tornar un element a la seva posició original, es considera "sense canvis" i no es desa.
     */
    const updateWidgetsLayout = useCallback(
        (layoutItems: { id: string; x: number; y: number; w: number; h: number }[]) => {
            setRequestState((prevState) => ({
                ...prevState,
                widgets: prevState.widgets?.map((item: any) => {
                    const layoutItem = layoutItems.find(
                        (li) => li.id === String(item.dashboardItemId ?? item.dashboardTitolId)
                    );
                    return layoutItem
                        ? { ...item, posX: layoutItem.x, posY: layoutItem.y, width: layoutItem.w, height: layoutItem.h }
                        : item;
                }),
            }));
        },
        []
    );

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
        updateWidgetsLayout,
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
