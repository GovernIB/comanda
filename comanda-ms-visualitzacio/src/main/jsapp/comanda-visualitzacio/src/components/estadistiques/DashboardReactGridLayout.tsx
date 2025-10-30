import * as React from 'react';
import { Layout, Layouts } from 'react-grid-layout';
import { Responsive, WidthProvider } from 'react-grid-layout';
import { isEqual } from 'lodash';
import SimpleWidgetVisualization from './SimpleWidgetVisualization.tsx';
import GraficWidgetVisualization from './GraficWidgetVisualization.tsx';
import TaulaWidgetVisualization from './TaulaWidgetVisualization.tsx';
import { useEffect, useMemo, useRef } from 'react';
import { ErrorBoundary } from 'react-error-boundary';
import { ErrorBoundaryFallback } from '../../pages/SalutAppInfo.tsx';
import { Box, Icon, Menu, MenuItem } from '@mui/material';
import 'react-grid-layout/css/styles.css';
import './react-resizable-custom.css';
import TitolWidgetVisualization from "./TitolWidgetVisualization.tsx";
import {MuiFormDialog, MuiFormDialogApi, useBaseAppContext, useConfirmDialogButtons, useResourceApiService} from "reactlib";
import {useTranslation} from "react-i18next";
import {AfegirTitolFormContent} from "../../pages/EstadisticaDashboardEdit.tsx";

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

const useMenu = (props:any) => {
    const { entity, actions } = props;

    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    const content = <Menu
        id={`menu-button-${entity?.id}`}
        MenuListProps={{
            'aria-labelledby': 'demo-customized-button',
        }}
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}

        elevation={0}
        anchorOrigin={{
            vertical: 'top',
            horizontal: 'right',
        }}
        transformOrigin={{
            vertical: 'top',
            horizontal: 'right',
        }}
    >
        {actions?.map?.((action:any, index:number) =>
                !(typeof action.hidden === 'function' ? action.hidden(entity) : action.hidden)
                && <div key={`action-${index}`} title={ typeof action.title == 'function' ?action.title?.(entity) :action.title}>
                    <MenuItem onClick={()=>action?.onClick?.(entity?.id, entity)}
                              disabled={typeof action?.disabled === 'function' ? action?.disabled(entity) : action?.disabled}
                    >
                        {action.icon && <Icon>{action.icon}</Icon>}{action.label}
                    </MenuItem>
                </div>
        )}
    </Menu>

    return {
        handleOpen,
        handleClose,
        content
    }
}

const useCustomGridItemActions = (refresh?: () => void) => {
    const { t } = useTranslation();
    const formApiRef = React.useRef<MuiFormDialogApi>(undefined)
    const {messageDialogShow, temporalMessageShow} = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = {maxWidth: 'sm', fullWidth: true};

    const { delete: deleteDashboardItem } = useResourceApiService('dashboardItem');
    const { delete: deleteDashboardTitol } = useResourceApiService('dashboardTitol');

    const check = (action: () => void) => {
        messageDialogShow(
            'Confirmació',
            'Estau segur que voleu esborrar aquest element (aquesta acció no es pot desfer)?',
            confirmDialogButtons,
            confirmDialogComponentProps)
            .then((value: any) => {
                if (value) {
                    action()
                }
            });
    }

    const deleteDashboardItemCheck = (id:any) => {
        check(() => {
            deleteDashboardItem(id)
                .then(() => {
                    refresh?.()
                    temporalMessageShow(null, '', 'success');
                })
                .catch((error) => {
                    if (error?.message) temporalMessageShow(null, error?.message, 'error');
                });
        });
    }
    const deleteDashboardTitolCheck = (id:any) => {
        check(() => {
            deleteDashboardTitol(id)
                .then(() => {
                    refresh?.();
                    temporalMessageShow(null, '', 'success');
                })
                .catch((error) => {
                    if (error?.message) temporalMessageShow(null, error?.message, 'error');
                });
        });
    }

    const actions = [
        {
            label: "Editar",
            icon: 'edit',
            showInMenu: true,
            onClick: (id:any, row:any) => {
                if (row.tipus === 'TITOL') { formApiRef.current?.show(id).then(()=>refresh?.()) }
            },
            hidden: (row:any) => row.tipus !== 'TITOL', // TODO: implementar formulario para otros tipos
        },
        {
            label: "Borrar",
            icon: 'delete',
            showInMenu: true,
            onClick: (id:any, row:any) => {
                if (row.tipus === 'TITOL') { deleteDashboardTitolCheck(id) }
                else { deleteDashboardItemCheck(id) }
            },
        },
    ]

    const content = <>
        <MuiFormDialog
            resourceName={'dashboardTitol'}
            title={t($ => $.page.dashboards.action.llistarTitle.label)}
            apiRef={formApiRef}
        >
            <AfegirTitolFormContent/>
        </MuiFormDialog>
    </>

    return { actions, content }
}

const CustomGridItemComponent = React.forwardRef<HTMLDivElement, any>(
    (
        { entity, refresh, style, className, onMouseDown, onMouseUp, onTouchEnd, editable, children },
        ref
    ) => {
        const { actions, content: contentActions } = useCustomGridItemActions(()=>refresh?.())
        const { handleOpen, content } = useMenu({entity, actions})
        return (<>
            <div
                style={{
                    ...style,
                }}
                className={className}
                ref={ref}
                onMouseDown={onMouseDown}
                onMouseUp={onMouseUp}
                onTouchEnd={onTouchEnd}
                onContextMenu={(event) => {
                    if(editable){
                        event.preventDefault()
                        handleOpen(event)
                    }
                }}
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
            {content}
            {contentActions}
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
    editable: boolean;
    refresh?: () => void;
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
    refresh,
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
                        if (dashboardWidget) dashboardWidget.id = dashboardWidget?.dashboardItemId;
                        const dashboardTitol = dashboardWidgets.find(
                            (dashboardWidget) => String(dashboardWidget.dashboardTitolId) === item.id
                        );
                        if(dashboardTitol) dashboardTitol.id = dashboardTitol?.dashboardTitolId;
                        return (
                            <CustomGridItemComponent key={item.id} refresh={refresh} entity={dashboardWidget ?? dashboardTitol} editable={editable}>
                                <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
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
