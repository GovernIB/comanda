import { BarChart, PieChart } from '@mui/x-charts';
import * as React from 'react';
import { Layout, Layouts } from 'react-grid-layout';
import { Responsive, WidthProvider } from 'react-grid-layout';
import Paper from '@mui/material/Paper';
import { isEqual } from 'lodash';
import SimpleWidgetVisualization from '../components/estadistiques/SimpleWidgetVisualization.tsx';

// TODO Cambiar esta constante por una prop
const editing = true;

const CustomGridLayout = WidthProvider(Responsive);

function SimpleChartWrapper({ dashboardWidget }) {
    return <SimpleWidgetVisualization
        titol={dashboardWidget.titol}
        valor={dashboardWidget.valor}
        // unitat={} TODO
        descripcio={dashboardWidget.descripcio}
        icona={dashboardWidget.atributsVisuals?.icona}
        vora={dashboardWidget.atributsVisuals?.vora}
        ampleVora={dashboardWidget.atributsVisuals?.ampleVora}
    />;
}

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
    ({ style, className, onMouseDown, onMouseUp, onTouchEnd, children, ...props }, ref) => {
        return (
            <div
                style={{ ...style,
                }}
                className={className}
                ref={ref}
                onMouseDown={onMouseDown}
                onMouseUp={onMouseUp}
                onTouchEnd={onTouchEnd}
            >
                <div
                    style={{
                        padding: "8px",
                        position: 'relative',
                        height: '100%',
                        pointerEvents: editing ? 'none' : undefined,
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
    dashboardWidgets: any[];
    gridLayoutItems: GridLayoutItem[];
    onGridLayoutItemsChange: (gridLayoutItems: GridLayoutItem[]) => void;
};

export const AppEstadisticaTest: React.FC<AppEstadisticaTestProps> = ({
    dashboardWidgets,
    gridLayoutItems,
    onGridLayoutItemsChange,
}) => {
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
    const onLayoutChange = (_currentLayout: Layout[], allLayouts: Layouts) => {
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
    return (
        <>
            <Paper
                sx={{
                    // width: width + 'px',
                    width: '100%',
                }}
            >
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
                        md: 24,
                        sm: 1,
                        // xs: 1,
                        // xxs: 1,
                    }}
                    rowHeight={30}
                    compactType={null}
                    preventCollision
                    onWidthChange={(_containerWidth, _margin, cols) => {
                        setSizeLock(cols === 1);
                    }}
                    isDraggable={!sizeLock}
                    isResizable={!sizeLock}
                    resizeHandle={<CustomHandle />}
                    resizeHandles={!sizeLock ? ['s', 'w', 'e', 'n', 'sw', 'nw', 'se', 'ne'] : []}
                >
                    {gridLayoutItems.map((item) => {
                        const dashboardWidget = dashboardWidgets.find(
                            (dashboardWidget) => String(dashboardWidget.dashboardItemId) === item.id
                        );
                        console.log(dashboardWidget, item.id, dashboardWidgets);
                        return (
                            <CustomGridItemComponent key={item.id}>
                                {(() => {
                                    switch (item.type) {
                                        // TODO Completar
                                        case 'GRAFIC':
                                        // return <GraficChartWrapper />;
                                        case 'SIMPLE':
                                            return (
                                                <SimpleChartWrapper
                                                    dashboardWidget={dashboardWidget}
                                                />
                                            );
                                        default:
                                            return <PieChartDemo />;
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
