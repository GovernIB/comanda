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
import useSizeTracker from '../../hooks/useSizeTracker.ts';

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
        {style, className, onMouseDown, onMouseUp, onTouchEnd, editable, selected, itemId, entity, onItemContextMenu, children},
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
                data-grid-item-id={itemId}
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
    /** Notifica una selecció múltiple feta arrossegant un marc de selecció sobre el canvas. Buit = neteja la selecció. */
    onSelectItems?: (entities: any[]) => void;
    onDeleteItem?: (entity: any) => void;
    onDuplicateItem?: (entity: any) => void;
    onClearSelection?: () => void;
    selectedItemId?: string | null;
    /** Ids seleccionats en una selecció múltiple (vegeu onSelectItems). Es ressalten igual que selectedItemId. */
    multiSelectedItemIds?: string[];
    dashboardEntornCodi?: string;
    editable: boolean;
    backgroundColor?: string;
    /** Vegeu DashboardLargeScreenMode. Per defecte 'fit'. */
    largeScreenMode?: DashboardLargeScreenMode;
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

/**
 * Amplada de disseny de referència del dashboard, en píxels: és l'amplada a la qual el canvas es mostra a
 * escala 100% (sense necessitat d'escalar). Correspon a una pantalla de 1920px menys els 240px que ocupa el
 * menú lateral de l'aplicació (vegeu `drawerWidth` a SideMenu.tsx), és a dir l'espai real disponible pel
 * canvas en aquesta pantalla de referència. Tots els widgets es dissenyen i posicionen pensant en aquesta
 * amplada; a pantalles més petites o més grans el canvas s'escala (vegeu DashboardScaledCanvas) mantenint
 * sempre la mateixa proporció visual (mides de fonts, icones, etc.).
 */
export const DASHBOARD_DESIGN_WIDTH = 1640;

export const horizontalSubdivisions = 60;

/** Alçada de fila del grid, calculada perquè les cel·les siguin el més quadrades possible a l'amplada de disseny. */
export const dashboardRowHeight = DASHBOARD_DESIGN_WIDTH / horizontalSubdivisions;

/**
 * Comportament del canvas:
 * - 'centered': el canvas es mostra sempre a mida real (1 a 1, sense escalar). A pantalles més amples que
 *   DASHBOARD_DESIGN_WIDTH es centra horitzontalment; a pantalles més estretes, en lloc d'escalar-lo cap
 *   avall, es manté la mida original i cal fer scroll horitzontal per veure'l sencer.
 * - 'fit': el canvas s'escala (cap amunt o cap avall) per ocupar sempre tot l'ample disponible.
 */
export type DashboardLargeScreenMode = 'centered' | 'fit';

/**
 * Envolta el contingut del dashboard (canvas de graella + overlay) en un contenidor d'amplada fixa
 * (DASHBOARD_DESIGN_WIDTH) i, en mode 'fit', l'escala amb un `transform: scale(...)` perquè ocupi tot l'ample
 * disponible. Com que és un transform CSS, TOT el contingut (mides de widgets, fonts, icones, espaiats...)
 * s'escala de manera proporcional i coherent, sense haver de tocar cap component individual.
 *
 * S'utilitza `useSizeTracker` (ResizeObserver) tant per l'ample disponible del contenidor com per l'alçada
 * natural (sense escalar) del contingut, ja que un `transform` no altera la mida de caixa que un element
 * reporta al seu contenidor (cal calcular-la manualment perquè el contenidor extern ocupi l'espai correcte
 * i, en mode 'centered', es pugui centrar amb `margin: auto`).
 *
 * `children` és una render-prop (rep l'`scale` efectiu) perquè `<CustomGridLayout>` necessita conèixer'l:
 * react-draggable/react-resizable (que hi ha per sota de react-grid-layout) calculen els desplaçaments de
 * ratolí en píxels reals de pantalla, sense tenir en compte cap `transform: scale` dels seus avantpassats.
 * Sense compensar-ho, qualsevol clic o arrossegament dins d'un canvas escalat es tradueix a una posició de
 * graella incorrecta (per exemple, un simple clic per seleccionar un component es detecta com un petit
 * arrossegament i el component "salta" unes files). Es compensa passant aquest mateix `scale` a la prop
 * `transformScale` de react-grid-layout (vegeu el seu ús a <CustomGridLayout>).
 */
const DashboardScaledCanvas: React.FC<{
    largeScreenMode: DashboardLargeScreenMode;
    children: (scale: number) => React.ReactNode;
}> = ({largeScreenMode, children}) => {
    const {size: containerSize, refCallback: containerRef} = useSizeTracker(100);
    const {size: designSize, refCallback: designRef} = useSizeTracker(100);

    const containerWidth = containerSize?.width || DASHBOARD_DESIGN_WIDTH;
    const designHeight = designSize?.height || 0;
    // En mode 'centered' mai s'escala (ni cap amunt ni cap avall): a pantalles més estretes es manté la
    // mida real i es mostra scroll horitzontal en lloc d'encongir el contingut.
    const scale = largeScreenMode === 'fit' ? containerWidth / DASHBOARD_DESIGN_WIDTH : 1;

    return (
        <Box
            ref={containerRef}
            data-testid="dashboard-scale-container"
            sx={{width: '100%', overflowX: largeScreenMode === 'centered' ? 'auto' : 'hidden'}}
        >
            <Box
                data-testid="dashboard-scaled-box"
                sx={{
                    width: `${DASHBOARD_DESIGN_WIDTH * scale}px`,
                    height: designHeight ? `${designHeight * scale}px` : undefined,
                    mx: largeScreenMode === 'centered' ? 'auto' : 0,
                    position: 'relative',
                    overflow: 'hidden',
                }}
            >
                <Box
                    ref={designRef}
                    data-testid="dashboard-scale-design"
                    sx={{
                        width: `${DASHBOARD_DESIGN_WIDTH}px`,
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        transform: `scale(${scale})`,
                        transformOrigin: 'top left',
                    }}
                >
                    {children(scale)}
                </Box>
            </Box>
        </Box>
    );
};

/** Distància màxima (en px) entre l'inici i el final d'un arrossegament perquè es consideri un simple clic */
const CLICK_MOVEMENT_THRESHOLD = 5;

/** Files buides addicionals que es deixen sota l'últim component en mode edició, per facilitar arrossegar-los cap avall. */
const EXTRA_EDIT_SCROLL_ROWS = 20;

export const DashboardReactGridLayout: React.FC<DashboardReactGridLayoutProps> = ({
                                                                                      dashboardWidgets,
                                                                                      editable,
                                                                                      gridLayoutItems,
                                                                                      onGridLayoutItemsChange,
                                                                                      onSelectItem,
                                                                                      onSelectItems,
                                                                                      onDeleteItem,
                                                                                      onDuplicateItem,
                                                                                      onClearSelection,
                                                                                      selectedItemId,
                                                                                      multiSelectedItemIds,
                                                                                      dashboardEntornCodi,
                                                                                      backgroundColor,
                                                                                      largeScreenMode = 'fit',
                                                                                  }) => {
    const {t} = useTranslation();
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const findEntityById = (id: string) =>
        dashboardWidgets.find((widget) => String(widget.dashboardItemId) === id) ??
        dashboardWidgets.find((widget) => String(widget.dashboardTitolId) === id);

    const [contextMenu, setContextMenu] = React.useState<{ mouseX: number; mouseY: number; entity: any } | null>(null);
    const handleItemContextMenu = (event: React.MouseEvent, entity: any) => {
        // Per ara, amb una selecció múltiple activa no s'obre el menú contextual (vegeu comentari a
        // multiSelectedItemIds a les props del component).
        if (multiSelectedItemIds && multiSelectedItemIds.length > 1) return;
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
    // Quan es finalitza un arrossegament real d'un element que forma part d'una selecció múltiple, es guarda
    // aquí el desplaçament (en columnes/files) perquè onLayoutChange l'apliqui també a la resta d'elements
    // seleccionats (react-grid-layout només mou nativament l'element arrossegat).
    const multiDragDeltaRef = useRef<{ draggedId: string; deltaX: number; deltaY: number } | null>(null);
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
        newItem: Layout,
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
            } else if (multiSelectedItemIds && multiSelectedItemIds.length > 1 && multiSelectedItemIds.includes(oldItem.i)) {
                // Arrossegament real d'un element seleccionat dins un grup: la resta d'elements
                // seleccionats s'han de moure amb el mateix desplaçament (vegeu onLayoutChange).
                multiDragDeltaRef.current = {
                    draggedId: oldItem.i,
                    deltaX: newItem.x - oldItem.x,
                    deltaY: newItem.y - oldItem.y,
                };
            }
        } catch (error) {
            console.error('Error detectant si l\'acció era un clic o un arrossegament', error);
        }
    };

    const onLayoutChange = (_currentLayout: Layout[], allLayouts: Layouts) => {
        drawGrid();
        const pendingDelta = multiDragDeltaRef.current;
        multiDragDeltaRef.current = null;
        const mappedLayouts: (GridLayoutItem | undefined)[] = allLayouts.md.map((item) => {
            const typeInGridLayoutItems = gridLayoutItems.find((i) => i.id === item.i)?.type;
            const typeFromAutogeneratedId: string = item.i.split('-')[1];
            const mergedType = typeInGridLayoutItems ?? typeFromAutogeneratedId;

            if (!isValidWidgetType(mergedType)) {
                console.error(`Invalid widget type: ${typeFromAutogeneratedId}`);
                return undefined;
            }

            let x = item.x;
            let y = item.y;
            if (pendingDelta && item.i !== pendingDelta.draggedId && multiSelectedItemIds?.includes(item.i)) {
                x = Math.max(0, Math.min(horizontalSubdivisions - item.w, x + pendingDelta.deltaX));
                y = Math.max(0, y + pendingDelta.deltaY);
            }

            return {
                id: item.i,
                type: mergedType,
                x,
                y,
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

    const rowHeight = dashboardRowHeight;

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

        // Línies a punts (en lloc de contínues): un segment de traç mínim amb extrem arrodonit ("lineCap
        // round") dibuixa un punt rodó a cada interval, en lloc d'un guionet.
        ctx.strokeStyle = '#ccc';
        ctx.lineWidth = 0.5;
        ctx.lineCap = 'round';
        ctx.setLineDash([1, 3]);

        // Dibuixar línies verticals
        for (let i = 0; i <= cols; i++) {
            const x = i * colWidth;
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, canvas.height);
            ctx.stroke();
        }

        // Dibuixar línies horitzontals
        for (let y = 0; y <= canvas.height; y += rowHeight) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(canvas.width, y);
            ctx.stroke();
        }
    };

    useEffect(() => {
        drawGrid();
        window.addEventListener('resize', drawGrid);

        return () => window.removeEventListener('resize', drawGrid);
    }, [layout]);

    const isReadonly = sizeLock || !editable;

    // Selecció múltiple arrossegant un marc (marquee) sobre el fons del canvas: mousedown fora de qualsevol
    // element inicia el marc, es va actualitzant amb mousemove i, en soltar, se selecciona tot element el
    // requadre (getBoundingClientRect) del qual intersecti amb el marc. S'usa getBoundingClientRect en lloc
    // de calcular files/columnes perquè ja reflecteix l'escalat CSS aplicat pel canvas (vegeu DashboardScaledCanvas).
    const canvasContainerRef = useRef<HTMLDivElement>(null);
    const justFinishedMarqueeRef = useRef(false);
    const [marqueeRect, setMarqueeRect] = React.useState<{ left: number; top: number; width: number; height: number } | null>(null);
    const MARQUEE_MOVEMENT_THRESHOLD = 4;

    const handleCanvasMouseDown = (event: React.MouseEvent) => {
        if (!editable) return;
        const target = event.target as HTMLElement;
        if (target.closest('[data-grid-item-id]')) return;
        const startX = event.clientX;
        const startY = event.clientY;

        const onMouseMove = (ev: MouseEvent) => {
            const container = canvasContainerRef.current;
            if (!container) return;
            const containerRect = container.getBoundingClientRect();
            setMarqueeRect({
                left: Math.min(startX, ev.clientX) - containerRect.left,
                top: Math.min(startY, ev.clientY) - containerRect.top,
                width: Math.abs(ev.clientX - startX),
                height: Math.abs(ev.clientY - startY),
            });
        };
        const onMouseUp = (ev: MouseEvent) => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            setMarqueeRect(null);

            const dx = Math.abs(ev.clientX - startX);
            const dy = Math.abs(ev.clientY - startY);
            if (dx <= MARQUEE_MOVEMENT_THRESHOLD && dy <= MARQUEE_MOVEMENT_THRESHOLD) return; // simple clic: ja el gestiona onClick

            justFinishedMarqueeRef.current = true;
            const selRect = {
                left: Math.min(startX, ev.clientX),
                top: Math.min(startY, ev.clientY),
                right: Math.max(startX, ev.clientX),
                bottom: Math.max(startY, ev.clientY),
            };
            const matchedIds: string[] = [];
            canvasContainerRef.current?.querySelectorAll('[data-grid-item-id]').forEach((node) => {
                const rect = node.getBoundingClientRect();
                const intersects = rect.left < selRect.right && rect.right > selRect.left
                    && rect.top < selRect.bottom && rect.bottom > selRect.top;
                if (intersects) {
                    const id = node.getAttribute('data-grid-item-id');
                    if (id) matchedIds.push(id);
                }
            });
            const matchedEntities = matchedIds.map(findEntityById).filter((entity) => entity != null);
            onSelectItems?.(matchedEntities);
        };

        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    };

    return (
        <>
            <Box
                data-testid="dashboard-canvas"
                ref={canvasContainerRef}
                sx={{
                    position: 'relative',
                    width: '100%',
                    minHeight: 'calc(100vh - 196px)',
                    // Sense color configurat al dashboard, s'ha de reflectir igualment el tema actiu
                    // (clar/fosc) en lloc de quedar transparent i mostrar el fons real de l'aplicació.
                    backgroundColor: backgroundColor || 'background.default',
                }}
                onMouseDown={handleCanvasMouseDown}
                onClick={() => {
                    if (justFinishedMarqueeRef.current) {
                        justFinishedMarqueeRef.current = false;
                        return;
                    }
                    if (editable) onClearSelection?.();
                }}
            >
                {marqueeRect && (
                    <Box
                        data-testid="selection-marquee"
                        sx={{
                            position: 'absolute',
                            left: `${marqueeRect.left}px`,
                            top: `${marqueeRect.top}px`,
                            width: `${marqueeRect.width}px`,
                            height: `${marqueeRect.height}px`,
                            border: '1px solid #1976d2',
                            backgroundColor: 'rgba(25, 118, 210, 0.12)',
                            pointerEvents: 'none',
                            zIndex: 30,
                        }}
                    />
                )}
                <DashboardScaledCanvas largeScreenMode={largeScreenMode}>
                    {(scale) => (<>
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
                        transformScale={scale}
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
                                    selected={selectedItemId === item.id || !!multiSelectedItemIds?.includes(item.id)}
                                    itemId={item.id}
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
                    {editable && (
                        <Box
                            data-testid="dashboard-extra-scroll-space"
                            aria-hidden="true"
                            sx={{height: `${EXTRA_EDIT_SCROLL_ROWS * rowHeight}px`}}
                        />
                    )}
                    </>)}
                </DashboardScaledCanvas>
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
