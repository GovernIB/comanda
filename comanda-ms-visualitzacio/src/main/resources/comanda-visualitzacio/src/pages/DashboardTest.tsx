import { BarChart, PieChart } from '@mui/x-charts';
import * as React from 'react';
import { Layout, Layouts } from 'react-grid-layout';
import { Responsive, WidthProvider } from 'react-grid-layout';
import Paper from '@mui/material/Paper';
import { isEqual } from 'lodash';
import SimpleWidgetVisualization from '../components/estadistiques/SimpleWidgetVisualization.tsx';
import GraficWidgetVisualization from '../components/estadistiques/GraficWidgetVisualization.tsx';
import TaulaWidgetVisualization from '../components/estadistiques/TaulaWidgetVisualization.tsx';
import {useEffect, useMemo, useRef, useState} from 'react';

// Keys d'events SSE
const sseConnectedKey = 'dashboard_connect';
const sseDashboardItemLoadedKey = 'item_carregat';
const sseDashboardItemLoadingErrorKey = 'item_error';

const CustomGridLayout = WidthProvider(Responsive);

const SimpleChartWrapper = React.memo(({ dashboardWidget }) => {
    return <SimpleWidgetVisualization
        {...dashboardWidget}
        {...dashboardWidget.atributsVisuals}
    />;
});

const GraficChartWrapper = React.memo(({ dashboardWidget }) => {
    return (
        <GraficWidgetVisualization
            {...dashboardWidget}
            {...dashboardWidget.atributsVisuals}
        />
    );
});

const TaulaChartWrapper = React.memo(({ dashboardWidget }) => {
    return (
        <TaulaWidgetVisualization
            {...dashboardWidget}
            {...dashboardWidget.atributsVisuals}
        />
    );
});

function ChartsOverviewDemo(props) {
    return (
        <BarChart
            {...props}
            series={[
                { data: [35, 44, 24, 34] },
                { data: [51, 6, 49, 30] },
                { data: [15, 25, 30, 50] },
                { data: [60, 50, 15, 25] },
            ]}
            // height={290}
            xAxis={[{ data: ['Q1', 'Q2', 'Q3', 'Q4'], scaleType: 'band' }]}
            margin={{ top: 10, bottom: 30, left: 40, right: 10 }}
        />
    );
}

function PieChartDemo(props) {
    return (
        <PieChart
            {...props}
            series={[
                {
                    data: [
                        { id: 0, value: 10, label: 'series A' },
                        { id: 1, value: 15, label: 'series B' },
                        { id: 2, value: 20, label: 'series C' },
                    ],
                },
            ]}
            // width={200}
            // height={200}
        />
    );
}

const CustomGridItemComponent = React.forwardRef<HTMLDivElement, any>(
    ({ style, className, onMouseDown, onMouseUp, onTouchEnd, editable, children, ...props }, ref) => {
        return (
            <div
                style={{
                    ...style,
                }}
                className={className}
                ref={ref}
                onMouseDown={onMouseDown}
                onMouseUp={onMouseUp}
                onTouchEnd={onTouchEnd}
            >
                <div
                    style={{
                        padding: '8px',
                        position: 'relative',
                        height: '100%',
                        pointerEvents: editable ? 'none' : undefined,
                    }}
                >
                    {children}
                </div>
            </div>
        );
    }
);

const CustomHandle = React.forwardRef<HTMLDivElement, any>((props, ref) => {
    const { handleAxis, ...restProps } = props;
    return (
        <div
            ref={ref}
            className={`react-resizable-handle react-resizable-handle-${handleAxis}`}
            style={{
                pointerEvents: 'all',
            }}
            {...restProps}
        />
    );
});

const getMinDimensionsByType = (type: WidgetType) => {
    switch (type) {
        case 'barChart':
            return { minW: 2, minH: 3 };
        case 'pieChart':
            return { minW: 5, minH: 4 };
        default:
            // TODO Reimplementar para todos los tipos correctos
            return { minW: 1, minH: 1 };
    }
};

type WidgetType = 'SIMPLE' | 'GRAFIC' | 'TAULA';

const isValidWidgetType = (type: string): type is WidgetType => {
    return type === 'SIMPLE' || type === 'GRAFIC' || type === 'TAULA';
};

export type GridLayoutItem = {
    id: string;
    type: WidgetType;
    x: number;
    y: number;
    w: number;
    h: number;
};

type AppEstadisticaTestProps = {
    dashboardId: number;
    dashboardWidgets: any[];
    gridLayoutItems: GridLayoutItem[];
    onGridLayoutItemsChange?: (gridLayoutItems: GridLayoutItem[]) => void;
    editable: boolean;
};

export const useMapDashboardItems = (dashboardWidgets) => {
    return useMemo(
        () =>
            dashboardWidgets?.map((widget: any) => ({
                id: String(widget.dashboardItemId),
                x: widget.posX,
                y: widget.posY,
                w: widget.width,
                h: widget.height,
                type: widget.tipus,
            })),
        [dashboardWidgets]
    );
};

export const AppEstadisticaTest: React.FC<AppEstadisticaTestProps> = ({
    dashboardId,
    dashboardWidgets,
    editable,
    gridLayoutItems,
    onGridLayoutItemsChange,
}) => {
    const canvasRef = useRef();
    // Add a state variable to force re-renders when dashboardWidgets is updated
    const [forceUpdate, setForceUpdate] = useState(0);
    // const { isReady: dashboardItemApiIsReady, artifactReport: dashboardItemReport } =
    //     useResourceApiService('dashboardItem');
    //
    // useEffect(() => {
    //     if (dashboardItemApiIsReady){
    //         dashboardItemReport(null, { code: 'widget_data' })
    //     }
    // }, [dashboardItemApiIsReady]);

    // const [gridLayoutItems, setGridLayoutItems] = React.useState<GridLayoutItem[]>(defaultGridLayout ?? [
    //     {
    //         id: 'sample-1',
    //         type: 'barChart',
    //         x: 0,
    //         y: 0,
    //         w: 4,
    //         h: 5,
    //     },
    //     {
    //         id: 'sample-2',
    //         type: 'pieChart',
    //         x: 5,
    //         y: 0,
    //         w: 5,
    //         h: 4,
    //     },
    //     {
    //         id: 'sample-3',
    //         type: 'barChart',
    //         x: 4,
    //         y: 4,
    //         w: 2,
    //         h: 3,
    //     },
    // ]);

    // Carregar widgets via SSE
    const eventSourceRef = useRef<EventSource | null>(null);

      useEffect(() => {
          const connectToSSE = () => {
              // Tancar la connexió anterior si existeix
              if (eventSourceRef.current) {
                  eventSourceRef.current.close();
              }

              // Crear una nova connexió
              const apiUrl = import.meta.env.VITE_API_URL || '/api';
              const sseUrl = `${apiUrl}/dashboards/subscribe/${dashboardId}`;

              const eventSource = new EventSource(sseUrl, {withCredentials: true});
              eventSourceRef.current = eventSource;

              // Gestionar l'esdeveniment de connexió
              eventSource.addEventListener(sseConnectedKey, (event) => {
                  console.log('SSE connectat:', event.data);
              });

              // Gestionar l'esdeveniment de dashboardItem carregat
              eventSource.addEventListener(sseDashboardItemLoadedKey, (event) => {
                  // console.log('SSE item carregat:', event.data);
                  try {
                      const data = JSON.parse(event.data);
                      const dashboardItemId = data.dashboardItemId;
                      const widgetItem = data.informeWidgetItem;
                      // const widgetItemLoaded = {...widgetItem, loading: false};
                      const tempsCarrega = data.tempsCarrega;
                      console.log('SSE item carregat:', dashboardItemId, tempsCarrega);
                      const trobatIndex = dashboardWidgets.findIndex(
                          (dashboardWidget) => dashboardWidget.dashboardItemId === dashboardItemId
                      );
                      if (trobatIndex !== -1) {
                          // Create a new array with the updated widget
                          const updatedWidgets = [...dashboardWidgets];
                          // Create a new object for the updated widget
                          updatedWidgets[trobatIndex] = {
                              ...dashboardWidgets[trobatIndex],
                              ...widgetItem,
                              loading: false
                          };
                          // Update the dashboardWidgets array with the new array
                          dashboardWidgets.splice(0, dashboardWidgets.length, ...updatedWidgets);
                          // Force a re-render to update the ChartWrapper components
                          setForceUpdate(prev => prev + 1);
                          // console.log('Widget modificat:', updatedWidgets[trobatIndex]);
                      } else {
                          console.log('Widget no trobat:', widgetItem);
                      }
                  } catch (error) {
                      console.error(`Error processant SSE: ${sseDashboardItemLoadingErrorKey}`, error);
                  }
              });


              // Gestionar l'esdeveniment d'error en la càrrega de dashboardItem
              eventSource.addEventListener(sseDashboardItemLoadingErrorKey, (event) => {
                  // console.log('SSE error carregat item:', event.data);
                  try {
                      const data = JSON.parse(event.data);
                      const dashboardItemId = data.dashboardItemId;
                      const widgetItem = data.informeWidgetItem;
                      // const widgetItemLoaded = {...widgetItem, loading: false};
                      const errorMsg = widgetItem?.errorMsg;
                      console.log('SSE item amb error:', dashboardItemId, errorMsg);
                      const trobatIndex = dashboardWidgets.findIndex(
                          (dashboardWidget) => dashboardWidget.dashboardItemId === dashboardItemId
                      );
                      if (trobatIndex !== -1) {
                          // Create a new array with the updated widget
                          const updatedWidgets = [...dashboardWidgets];
                          // Create a new object for the updated widget
                          updatedWidgets[trobatIndex] = {
                              ...dashboardWidgets[trobatIndex],
                              ...widgetItem,
                              loading: false,
                              error: true
                          };
                          // Update the dashboardWidgets array with the new array
                          dashboardWidgets.splice(0, dashboardWidgets.length, ...updatedWidgets);
                          // Force a re-render to update the ChartWrapper components
                          setForceUpdate(prev => prev + 1);
                          // console.log('Widget amb error modificat:', updatedWidgets[trobatIndex]);
                      } else {
                          console.log('Widget no trobat:', widgetItem);
                      }
                  } catch (error) {
                      console.error(`Error processant SSE: ${sseDashboardItemLoadingErrorKey}`, error);
                  }
              });


              // Gestionar errors
              eventSource.onerror = (error) => {
                  console.error('Error de connexió SSE:', error);

                  // Tancar la connexió actual
                  eventSource.close();
                  eventSourceRef.current = null;

                  // Intentar reconnectar després d'un temps
                  setTimeout(connectToSSE, 2000);
              };
          };

          // Iniciar la connexió
          connectToSSE();

          // Netejar en desmuntar el component
          return () => {
              console.log('Netejam o desmontam el component');
              if (eventSourceRef.current) {
                  console.log('Desconnectam SSE');
                  eventSourceRef.current.close();
                  eventSourceRef.current = null;
              }
          };
      }, []);

      // This useEffect will trigger a re-render when forceUpdate changes
      useEffect(() => {
          console.log('forceUpdate changed, triggering re-render');
      }, [forceUpdate]);

    const onLayoutChange = (_currentLayout: Layout[], allLayouts: Layouts) => {
        drawGrid();
        console.log('onLayoutChange:', _currentLayout);
        const mappedLayouts: (GridLayoutItem | undefined)[] = allLayouts.md.map((item) => {
            const typeInGridLayoutItems = gridLayoutItems.find((i) => i.id === item.i)?.type;
            const typeFromAutogeneratedId = item.i.split('-')[1];

            if (typeInGridLayoutItems == null && !isValidWidgetType(typeFromAutogeneratedId)) {
                console.error(`Invalid widget type: ${typeFromAutogeneratedId}`);
                return undefined;
            }
            return {
                id: item.i,
                type: typeInGridLayoutItems ?? typeFromAutogeneratedId,
                x: item.x,
                y: item.y,
                w: item.w,
                h: item.h,
            };
        });
        const filteredMappedLayouts = mappedLayouts.filter((i) => i !== undefined);
        if (!isEqual(filteredMappedLayouts, gridLayoutItems)) {
            console.log('Layout not equal, propagating', filteredMappedLayouts, gridLayoutItems);
            onGridLayoutItemsChange(filteredMappedLayouts);
        }
    };

    const [sizeLock, setSizeLock] = React.useState<boolean>(false);

    const layout = React.useMemo(
        () =>
            gridLayoutItems.map((item) => ({
                i: item.id,
                x: item.x,
                y: item.y,
                w: item.w,
                h: item.h,
                ...getMinDimensionsByType(item.type),
            })),
        [gridLayoutItems]
    );
    console.log(gridLayoutItems, layout);

    const rowHeight = 50;
    const horizontalSubdivisions = 24;

    const drawGrid = () => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');

        // Obtenir la mida del contenidor
        const parent = canvas.parentElement;
        canvas.width = parent.clientWidth;
        canvas.height = parent.clientHeight;

        const cols = 24;
        const colWidth = canvas.width / cols;

        ctx.clearRect(0, 0, canvas.width, canvas.height); // Netejar el canvas

        // Dibuixar línies verticals
        for (let i = 0; i <= cols; i++) {
            const x = i * colWidth;
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, canvas.height);
            ctx.strokeStyle = '#ccc';
            ctx.stroke();
        }

        // Dibuixar línies horitzontals
        for (let y = 0; y <= canvas.height; y += rowHeight) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(canvas.width, y);
            ctx.strokeStyle = '#ccc';
            ctx.lineWidth = 1;
            ctx.stroke();
        }
    };

    useEffect(() => {
        console.log('useEffect layout: ', layout);

        drawGrid();
        window.addEventListener('resize', drawGrid);

        return () => window.removeEventListener('resize', drawGrid);
    }, [layout]);

    const isReadonly = sizeLock || !editable;

    return (
        <>
            <Paper
                sx={{
                    // width: width + 'px',
                    position: 'relative',
                    width: '100%',
                    // TODO Calcular dinamicament
                    minHeight: '800px',
                }}
            >
                {editable && (
                    <canvas
                        style={{
                            width: '100%',
                            height: '100%',
                            position: 'absolute',
                            top: 0,
                            left: 0,
                            zIndex: 0,
                            pointerEvents: 'none',
                        }}
                        ref={canvasRef}
                    />
                )}
                <CustomGridLayout
                    className="layout"
                    breakpoints={{
                        // lg: 1200,
                        md: 996,
                        sm: 768,
                        // xs: 480,
                    }}
                    layouts={{ md: layout }}
                    onLayoutChange={onLayoutChange}
                    cols={{
                        // lg: 12,
                        md: horizontalSubdivisions,
                        sm: 1,
                        // xs: 1,
                        // xxs: 1,
                    }}
                    margin={[0, 0]}
                    rowHeight={rowHeight}
                    compactType={null}
                    preventCollision
                    onWidthChange={(_containerWidth, _margin, cols) => {
                        setSizeLock(cols === 1);
                    }}
                    isDraggable={!isReadonly}
                    isResizable={!isReadonly}
                    resizeHandle={<CustomHandle />}
                    resizeHandles={!isReadonly ? ['s', 'w', 'e', 'n', 'sw', 'nw', 'se', 'ne'] : []}
                >
                    {gridLayoutItems.map((item) => {
                        const dashboardWidget = dashboardWidgets.find(
                            (dashboardWidget) => String(dashboardWidget.dashboardItemId) === item.id
                        );
                        console.log(dashboardWidget, item.id, dashboardWidgets);
                        return (
                            <CustomGridItemComponent key={item.id} editable={editable}>
                                {(() => {
                                    switch (item.type) {
                                        // TODO Completar
                                        case 'SIMPLE':
                                            return (
                                                <SimpleChartWrapper
                                                    dashboardWidget={dashboardWidget}
                                                />
                                            );
                                        case 'GRAFIC':
                                            return (
                                                <GraficChartWrapper
                                                    dashboardWidget={dashboardWidget}
                                                />
                                            );
                                        case 'TAULA':
                                            return (
                                                <TaulaChartWrapper
                                                    dashboardWidget={dashboardWidget}
                                                />
                                            );
                                    }
                                })()}
                            </CustomGridItemComponent>
                        );
                    })}
                </CustomGridLayout>
            </Paper>
        </>
    );
};
