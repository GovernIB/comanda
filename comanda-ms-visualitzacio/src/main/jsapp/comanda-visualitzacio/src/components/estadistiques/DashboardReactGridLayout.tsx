import * as React from 'react';
import {useEffect, useMemo, useRef} from 'react';
import {Layout, Layouts, Responsive, WidthProvider} from 'react-grid-layout';
import {isEqual} from 'lodash';
import SimpleWidgetVisualization from './SimpleWidgetVisualization.tsx';
import GraficWidgetVisualization from './GraficWidgetVisualization.tsx';
import TaulaWidgetVisualization from './TaulaWidgetVisualization.tsx';
import {ErrorBoundary} from 'react-error-boundary';
import {Box, Icon, ListItemIcon, ListItemText, Menu, MenuItem} from '@mui/material';
import 'react-grid-layout/css/styles.css';
import './react-resizable-custom.css';
import TitolWidgetVisualization from "./TitolWidgetVisualization.tsx";
import {SalutErrorBoundaryFallback} from '../salut/SalutErrorBoundaryFallback';
import {useTranslation} from 'react-i18next';

const CustomGridLayout = WidthProvider(Responsive);

const SimpleChartWrapper = React.memo<{
    dashboardWidget: any,
    dashboardEntornCodi?: string | undefined
}>(({dashboardWidget, dashboardEntornCodi}) => {
    return <SimpleWidgetVisualization {...dashboardWidget} {...dashboardWidget.atributsVisuals}
                                      dashboardEntornCodi={dashboardEntornCodi}/>;
});

const GraficChartWrapper = React.memo<{
    dashboardWidget: any,
    dashboardEntornCodi?: string | undefined
}>(({dashboardWidget, dashboardEntornCodi}) => {
    return <GraficWidgetVisualization {...dashboardWidget} {...dashboardWidget.atributsVisuals}
                                      dashboardEntornCodi={dashboardEntornCodi}/>;
});

const TaulaChartWrapper = React.memo<{
    dashboardWidget: any,
    dashboardEntornCodi?: string | undefined
}>(({dashboardWidget, dashboardEntornCodi}) => {
    return <TaulaWidgetVisualization {...dashboardWidget} {...dashboardWidget.atributsVisuals}
                                     dashboardEntornCodi={dashboardEntornCodi}/>;
});

const TitolChartWrapper = React.memo<{ dashboardTitol: any }>(({dashboardTitol}) => {
    return <TitolWidgetVisualization {...dashboardTitol} {...dashboardTitol.atributsVisuals}/>;
});

const CustomGridItemComponent = React.forwardRef<HTMLDivElement, any>(
    (
        {style, className, onMouseDown, onMouseUp, onTouchEnd, editable, selected, entity, onItemContextMenu, children},
        ref
    ) => {
        // onMouseDown/onMouseUp aquí són els mètodes propis de react-draggable (GridItem els injecta
        // clonant l'element), no simples "pass-through": cal cridar-los perquè l'arrossegament
        // continuï funcionant. La detecció de "clic vs arrossegament" es fa a un altre nivell
        // (onDragStart/onDragStop de <CustomGridLayout>) perquè react-grid-layout re-renderitza la
        // graella en cada mousedown/mouseup, cosa que faria perdre qualsevol estat local guardat aquí.
        return (
            <div
                data-testid="grid-item"
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
                    }
                }}
                onContextMenu={(event) => {
                    if (editable && entity) {
                        event.preventDefault();
                        event.stopPropagation();
                        onItemContextMenu?.(event, entity);
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
                    <div style={{pointerEvents: editable ? 'none' : undefined, height: '100%'}}>
                        {children}
                    </div>
                </div>
            </div>
        );
    }
);

const CustomHandle = React.forwardRef<HTMLDivElement, any>((props, ref) => {
    const {handleAxis, ...restProps} = props;
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
            return {minW: 1, minH: 1};
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
    onDeleteItem?: (entity: any) => void;
    onDuplicateItem?: (entity: any) => void;
    onClearSelection?: () => void;
    selectedItemId?: string | null;
    dashboardEntornCodi?: string;
    editable: boolean;
    backgroundColor?: string;
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

export const horizontalSubdivisions = 24;

/** Distància màxima (en px) entre l'inici i el final d'un arrossegament perquè es consideri un simple clic */
const CLICK_MOVEMENT_THRESHOLD = 5;

export const DashboardReactGridLayout: React.FC<DashboardReactGridLayoutProps> = ({
                                                                                      dashboardWidgets,
                                                                                      editable,
                                                                                      gridLayoutItems,
                                                                                      onGridLayoutItemsChange,
                                                                                      onSelectItem,
                                                                                      onDeleteItem,
                                                                                      onDuplicateItem,
                                                                                      onClearSelection,
                                                                                      selectedItemId,
                                                                                      dashboardEntornCodi,
                                                                                      backgroundColor,
                                                                                  }) => {
    const {t} = useTranslation();
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const findEntityById = (id: string) =>
        dashboardWidgets.find((widget) => String(widget.dashboardItemId) === id) ??
        dashboardWidgets.find((widget) => String(widget.dashboardTitolId) === id);

    const [contextMenu, setContextMenu] = React.useState<{ mouseX: number; mouseY: number; entity: any } | null>(null);
    const handleItemContextMenu = (event: React.MouseEvent, entity: any) => {
        setContextMenu({mouseX: event.clientX, mouseY: event.clientY, entity});
    };
    const closeContextMenu = () => setContextMenu(null);
    const handleContextMenuModificar = () => {
        if (contextMenu) onSelectItem?.(contextMenu.entity);
        closeContextMenu();
    };
    const handleContextMenuDuplicar = () => {
        if (contextMenu) onDuplicateItem?.(contextMenu.entity);
        closeContextMenu();
    };
    const handleContextMenuEliminar = () => {
        if (contextMenu) onDeleteItem?.(contextMenu.entity);
        closeContextMenu();
    };

    // react-grid-layout no distingeix un clic simple d'un arrossegament: onDragStart/onDragStop es
    // disparen sempre, fins i tot sense moviment. Comparem la posició del ratolí a l'inici i al final
    // per decidir si s'ha de seleccionar l'element (clic) o no (arrossegament real).
    // IMPORTANT: ReactGridLayout crida this.props.onDragStop(...) de manera síncrona i SENSE try/catch
    // abans d'acabar la seva pròpia transició d'estat (setState + onLayoutMaybeChanged, vegeu
    // node_modules/react-grid-layout/lib/ReactGridLayout.jsx). Si aquí llancem una excepció, o si
    // onSelectItem provoca un re-render síncron del pare, es pot interrompre aquella transició i
    // l'arrossegament deixa de reflectir-se. Per això aquest handler mai llança (try/catch) i la
    // selecció es dispara al següent tick (setTimeout), un cop ReactGridLayout ja ha acabat.
    const dragStartPosRef = useRef<{ x: number; y: number } | null>(null);
    const onItemDragStart = (
        _layout: Layout[],
        _oldItem: Layout,
        _newItem: Layout,
        _placeholder: Layout,
        event: MouseEvent
    ) => {
        try {
            dragStartPosRef.current = {x: event.clientX, y: event.clientY};
        } catch (error) {
            dragStartPosRef.current = null;
            console.error('Error registrant l\'inici de l\'arrossegament', error);
        }
    };
    const onItemDragStop = (
        _layout: Layout[],
        oldItem: Layout,
        _newItem: Layout,
        _placeholder: Layout,
        event: MouseEvent
    ) => {
        const startPos = dragStartPosRef.current;
        dragStartPosRef.current = null;
        try {
            if (!editable || !startPos) return;
            const dx = Math.abs(event.clientX - startPos.x);
            const dy = Math.abs(event.clientY - startPos.y);
            if (dx <= CLICK_MOVEMENT_THRESHOLD && dy <= CLICK_MOVEMENT_THRESHOLD) {
                const entity = findEntityById(oldItem.i);
                if (entity) {
                    setTimeout(() => onSelectItem?.(entity), 0);
                }
            }
        } catch (error) {
            console.error('Error detectant si l\'acció era un clic o un arrossegament', error);
        }
    };

    const onLayoutChange = (_currentLayout: Layout[], allLayouts: Layouts) => {
        drawGrid();
        const mappedLayouts: (GridLayoutItem | undefined)[] = allLayouts.md.map((item) => {
            const typeInGridLayoutItems = gridLayoutItems.find((i) => i.id === item.i)?.type;
            const typeFromAutogeneratedId: string = item.i.split('-')[1];
            const mergedType = typeInGridLayoutItems ?? typeFromAutogeneratedId;

            if (!isValidWidgetType(mergedType)) {
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
                data-testid="dashboard-canvas"
                sx={{
                    position: 'relative',
                    width: '100%',
                    minHeight: 'calc(100vh - 196px)',
                    // Sense color configurat al dashboard, s'ha de reflectir igualment el tema actiu
                    // (clar/fosc) en lloc de quedar transparent i mostrar el fons real de l'aplicació.
                    backgroundColor: backgroundColor || 'background.default',
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
                    breakpoints={{md: 0}}
                    layouts={{md: layout}}
                    onLayoutChange={onLayoutChange}
                    cols={{md: horizontalSubdivisions}}
                    margin={[0, 0]}
                    rowHeight={rowHeight}
                    compactType={null}
                    preventCollision
                    onWidthChange={() => {
                        setSizeLock(false);
                    }}
                    isDraggable={!isReadonly}
                    isResizable={!isReadonly}
                    resizeHandle={<CustomHandle/>}
                    resizeHandles={!isReadonly ? ['s', 'w', 'e', 'n', 'sw', 'nw', 'se', 'ne'] : []}
                    onDragStart={onItemDragStart}
                    onDragStop={onItemDragStop}
                >
                    {gridLayoutItems.map((item) => {
                        const dashboardWidget = dashboardWidgets.find(
                            (dashboardWidget) => String(dashboardWidget.dashboardItemId) === item.id
                        );
                        if (dashboardWidget) dashboardWidget.id = dashboardWidget?.dashboardItemId;
                        const dashboardTitol = dashboardWidgets.find(
                            (dashboardWidget) => String(dashboardWidget.dashboardTitolId) === item.id
                        );
                        if (dashboardTitol) dashboardTitol.id = dashboardTitol?.dashboardTitolId;
                        return (
                            <CustomGridItemComponent
                                key={item.id}
                                editable={editable}
                                selected={selectedItemId === item.id}
                                entity={dashboardWidget ?? dashboardTitol}
                                onItemContextMenu={handleItemContextMenu}
                            >
                                <ErrorBoundary fallback={<SalutErrorBoundaryFallback/>}>
                                    {(() => {
                                        switch (item.type) {
                                            case 'SIMPLE':
                                                return (<SimpleChartWrapper dashboardWidget={dashboardWidget}
                                                                            dashboardEntornCodi={dashboardEntornCodi}/>);
                                            case 'GRAFIC':
                                                return (<GraficChartWrapper dashboardWidget={dashboardWidget}
                                                                            dashboardEntornCodi={dashboardEntornCodi}/>);
                                            case 'TAULA':
                                                return (<TaulaChartWrapper dashboardWidget={dashboardWidget}
                                                                           dashboardEntornCodi={dashboardEntornCodi}/>);
                                            case 'TITOL':
                                                return (<TitolChartWrapper dashboardTitol={dashboardTitol}/>);
                                        }
                                    })()}
                                </ErrorBoundary>
                            </CustomGridItemComponent>
                        );
                    })}
                </CustomGridLayout>
            </Box>
            <Menu
                open={contextMenu !== null}
                onClose={closeContextMenu}
                anchorReference="anchorPosition"
                anchorPosition={contextMenu ? {top: contextMenu.mouseY, left: contextMenu.mouseX} : undefined}
            >
                <MenuItem onClick={handleContextMenuModificar}>
                    <ListItemIcon><Icon fontSize="small">edit</Icon></ListItemIcon>
                    <ListItemText>{t($ => $.common.modify)}</ListItemText>
                </MenuItem>
                <MenuItem onClick={handleContextMenuDuplicar}>
                    <ListItemIcon><Icon fontSize="small">content_copy</Icon></ListItemIcon>
                    <ListItemText>{t($ => $.common.duplicate)}</ListItemText>
                </MenuItem>
                <MenuItem onClick={handleContextMenuEliminar}>
                    <ListItemIcon><Icon fontSize="small">delete</Icon></ListItemIcon>
                    <ListItemText>{t($ => $.common.delete)}</ListItemText>
                </MenuItem>
            </Menu>
        </>
    );
};
