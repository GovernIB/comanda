import * as React from 'react';
import { Layout, Layouts } from 'react-grid-layout';
import { Responsive, WidthProvider } from 'react-grid-layout';
import { isEqual } from 'lodash';
import SimpleWidgetVisualization from './SimpleWidgetVisualization.tsx';
import GraficWidgetVisualization from './GraficWidgetVisualization.tsx';
import TaulaWidgetVisualization from './TaulaWidgetVisualization.tsx';
import { useEffect, useMemo, useRef } from 'react';
import { ErrorBoundary } from 'react-error-boundary';
import { Box } from '@mui/material';
import 'react-grid-layout/css/styles.css';
import './react-resizable-custom.css';
import TitolWidgetVisualization from "./TitolWidgetVisualization.tsx";
import { SalutErrorBoundaryFallback } from '../salut/SalutErrorBoundaryFallback';

const CustomGridLayout = WidthProvider(Responsive);

const SimpleChartWrapper = React.memo<{ dashboardWidget: any }>(({ dashboardWidget }) => {
    return <SimpleWidgetVisualization {...dashboardWidget} {...dashboardWidget.atributsVisuals} />;
});

const GraficChartWrapper = React.memo<{ dashboardWidget: any }>(({ dashboardWidget }) => {
    return <GraficWidgetVisualization {...dashboardWidget} {...dashboardWidget.atributsVisuals} />;
});

const TaulaChartWrapper = React.memo<{ dashboardWidget: any }>(({ dashboardWidget }) => {
    return <TaulaWidgetVisualization {...dashboardWidget} {...dashboardWidget.atributsVisuals} />;
});

const TitolChartWrapper = React.memo<{ dashboardTitol: any }>(({ dashboardTitol }) => {
    return <TitolWidgetVisualization {...dashboardTitol} {...dashboardTitol.atributsVisuals} />;
});

const CustomGridItemComponent = React.forwardRef<HTMLDivElement, any>(
    (
        { entity, style, className, onMouseDown, onMouseUp, onTouchEnd, editable, selected, onSelect, children },
        ref
    ) => {
        return (<>
            <div
                style={{
                    ...style,
                    cursor: editable ? 'pointer' : style?.cursor,
                    outline: selected ? '2px solid #1976d2' : undefined,
                    outlineOffset: selected ? '-2px' : undefined,
                }}
                className={className}
                ref={ref}
                onMouseDown={onMouseDown}
                onMouseUp={onMouseUp}
                onTouchEnd={onTouchEnd}
                onClick={(event) => {
                    if (editable) {
                        event.stopPropagation();
                        onSelect?.(entity);
                    }
                }}
            >
                <div
                    style={{
                        padding: '8px',
                        position: 'relative',
                        height: '100%',
                    }}
                >
                    <div style={{ pointerEvents: editable ? 'none' : undefined, height: '100%' }}>
                        {children}
                    </div>
                </div>
            </div>
        </>);
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
        case 'SIMPLE':
        case 'GRAFIC':
        case 'TAULA':
        case 'TITOL':
            return { minW: 1, minH: 1 };
    }
};

type WidgetType = 'SIMPLE' | 'GRAFIC' | 'TAULA' | 'TITOL';

const isValidWidgetType = (type: string): type is WidgetType => {
    return type === 'SIMPLE' || type === 'GRAFIC' || type === 'TAULA' || type === 'TITOL';
};

export type GridLayoutItem = {
    id: string;
    type: WidgetType;
    x: number;
    y: number;
    w: number;
    h: number;
};

type DashboardReactGridLayoutProps = {
    dashboardId: number;
    dashboardWidgets: any[];
    gridLayoutItems: GridLayoutItem[];
    onGridLayoutItemsChange?: (gridLayoutItems: GridLayoutItem[]) => void;
    onSelectItem?: (entity: any) => void;
    onClearSelection?: () => void;
    selectedItemId?: string | null;
    editable: boolean;
};

export const useMapDashboardItems = (dashboardWidgets: unknown[]) => {
    return useMemo(
        () =>
            dashboardWidgets?.map((widget: any) => ({
                id: String(widget.dashboardItemId ?? widget.dashboardTitolId),
                x: widget.posX,
                y: widget.posY,
                w: widget.width,
                h: widget.height,
                type: widget.tipus,
            })),
        [dashboardWidgets]
    );
};

export const horizontalSubdivisions = 30;

export const DashboardReactGridLayout: React.FC<DashboardReactGridLayoutProps> = ({
    dashboardWidgets,
    editable,
    gridLayoutItems,
    onGridLayoutItemsChange,
    onSelectItem,
    onClearSelection,
    selectedItemId,
}) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const onLayoutChange = (_currentLayout: Layout[], allLayouts: Layouts) => {
        drawGrid();
        const mappedLayouts: (GridLayoutItem | undefined)[] = allLayouts.md.map((item) => {
            const typeInGridLayoutItems = gridLayoutItems.find((i) => i.id === item.i)?.type;
            const typeFromAutogeneratedId: string = item.i.split('-')[1];
            const mergedType = typeInGridLayoutItems ?? typeFromAutogeneratedId;

            if (!isValidWidgetType(mergedType))
            {
                console.error(`Invalid widget type: ${typeFromAutogeneratedId}`);
                return undefined;
            }

            return {
                id: item.i,
                type: mergedType,
                x: item.x,
                y: item.y,
                w: item.w,
                h: item.h,
            };
        });
        const filteredMappedLayouts = mappedLayouts.filter((i) => i !== undefined);
        if (!isEqual(filteredMappedLayouts, gridLayoutItems)) {
            onGridLayoutItemsChange?.(filteredMappedLayouts);
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

    const rowHeight = 50;

    const drawGrid = () => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext('2d');

        // Obtenir la mida del contenidor
        const parent = canvas.parentElement;
        if (!parent || !ctx) return;
        canvas.width = parent.clientWidth;
        canvas.height = parent.clientHeight;

        const cols = horizontalSubdivisions;
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
        drawGrid();
        window.addEventListener('resize', drawGrid);

        return () => window.removeEventListener('resize', drawGrid);
    }, [layout]);

    const isReadonly = sizeLock || !editable;

    return (
        <>
            <Box
                sx={{
                    position: 'relative',
                    width: '100%',
                    minHeight: 'calc(100vh - 196px)',
                }}
                onClick={() => editable && onClearSelection?.()}
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
                    breakpoints={{ md: 0 }}
                    layouts={{ md: layout }}
                    onLayoutChange={onLayoutChange}
                    cols={{ md: horizontalSubdivisions }}
                    margin={[0, 0]}
                    rowHeight={rowHeight}
                    compactType={null}
                    preventCollision
                    onWidthChange={() => {
                        setSizeLock(false);
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
                        if (dashboardWidget) dashboardWidget.id = dashboardWidget?.dashboardItemId;
                        const dashboardTitol = dashboardWidgets.find(
                            (dashboardWidget) => String(dashboardWidget.dashboardTitolId) === item.id
                        );
                        if(dashboardTitol) dashboardTitol.id = dashboardTitol?.dashboardTitolId;
                        return (
                            <CustomGridItemComponent
                                key={item.id}
                                entity={dashboardWidget ?? dashboardTitol}
                                editable={editable}
                                selected={selectedItemId === item.id}
                                onSelect={onSelectItem}
                            >
                                <ErrorBoundary fallback={<SalutErrorBoundaryFallback />}>
                                    {(() => {
                                        switch (item.type) {
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
                                            case 'TITOL':
                                                return (
                                                    <TitolChartWrapper
                                                        dashboardTitol={dashboardTitol}
                                                    />
                                                );
                                        }
                                    })()}
                                </ErrorBoundary>
                            </CustomGridItemComponent>
                        );
                    })}
                </CustomGridLayout>
            </Box>
        </>
    );
};
