import * as React from 'react';
import MuiToolbar from '@mui/material/Toolbar';
import {
    MuiDataGrid,
    useBaseAppContext,
    useResourceApiService,
    MuiFilter,
    springFilterBuilder,
    FormField,
    useFormContext,
    useMessageDialogButtons,
    useConfirmDialogButtons,
    useMuiDataGridApiRef,
    MuiDataGridColDef, useFilterApiRef, MuiFormDialogApi, MuiFormDialog,
} from 'reactlib';
import { useNavigate, useParams } from 'react-router-dom';
import {useCallback, useEffect, useRef, useState} from 'react';
import {
    DashboardReactGridLayout,
    GridLayoutItem,
    useMapDashboardItems,
} from '../components/estadistiques/DashboardReactGridLayout.tsx';
import DashboardEditorSidePanel, {
    DashboardEditorSelection,
    DashboardWidgetType,
} from '../components/estadistiques/DashboardEditorSidePanel.tsx';
import { isEqual } from 'lodash';
import {
    Alert,
    Box,
    Button,
    ListItemIcon,
    Paper,
    Table,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Typography,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { useContentDialog } from '../../lib/components/mui/Dialog.tsx';
import TableBody from '@mui/material/TableBody';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { DASHBOARDS_PATH } from '../AppRoutes.tsx';
import AddIcon from '@mui/icons-material/Add';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import ButtonMenu from '../components/ButtonMenu.tsx';
import MenuItem from '@mui/material/MenuItem';
import ListItemText from '@mui/material/ListItemText';
import { ResourceApiError } from '../../lib/components/ResourceApiProvider.tsx';
import TitolWidgetVisualization from "../components/estadistiques/TitolWidgetVisualization.tsx";
import PageTitle from '../components/PageTitle.tsx';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';
import {SimpleTreeView, TreeItem} from "@mui/x-tree-view";
import Divider from "@mui/material/Divider";
import EstadisticaSimpleWidgetForm from "../components/estadistiques/EstadisticaSimpleWidgetForm.tsx";
import EstadisticaGraficWidgetForm from "../components/estadistiques/EstadisticaGraficWidgetForm.tsx";
import EstadisticaTaulaWidgetForm from "../components/estadistiques/EstadisticaTaulaWidgetForm.tsx";
import {FooterHeightPlaceholder} from "../components/ComandaFooter.tsx";
import Menu from "@mui/material/Menu";
import { useTheme } from '@mui/material/styles';

type WidgetsErrorAlertProps = {
    errorWidgets: Array<{
        errorMsg: string;
    }>;
};

function WidgetsErrorAlert({ errorWidgets }: WidgetsErrorAlertProps) {
    const buttons = useMessageDialogButtons();
    const [showDialog, dialog] = useContentDialog(buttons);

    const openDialog = () => {
        showDialog(
            null,
            <TableContainer
                component={Paper}
                sx={{
                    mt: 3,
                }}
            >
                <Table sx={{ width: 500 }} aria-label="simple table">
                    <TableHead>
                        <TableRow>
                            <TableCell>Errors</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {errorWidgets.map((widget, index) => (
                            <TableRow key={index}>
                                <TableCell>{widget.errorMsg}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        );
    };

    return (
        <>
            {dialog}
            <Alert
                severity="warning"
                variant="filled"
                action={
                    <Button color="inherit" size="small" onClick={openDialog}>
                        Visualitzar
                    </Button>
                }
            >
                S'han trobat errors a algun dels widgets
            </Alert>
        </>
    );
}

const defaultSizeAndPosition = {
    posX: 0,
    // posY: 0, //Sense valor, el back el possicionara abaix de tot.
    width: 3,
    height: 3,
};

type ListWidgetDialogContentProps = {
    title: string;
    resourceName: string;
    form?: React.ReactElement;
    dashboardId: string;
    baseColumns: MuiDataGridColDef[];
    onDelete?: () => void;
    onUpdate?: () => void;
};

const ListWidgetDialogContent = ({ title, resourceName, form, dashboardId, baseColumns, onDelete, onUpdate }: ListWidgetDialogContentProps) => {
    const { isReady: apiIsReady, delete: apiDelete } = useResourceApiService(resourceName);
    const { t } = useTranslation();
    const gridApiRef = useMuiDataGridApiRef();
    const { messageDialogShow, temporalMessageShow, t: tLib } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const confirmDialogComponentProps = { maxWidth: 'sm', fullWidth: true };
    const onDeleteClick = (id: any) => {
        messageDialogShow(
            tLib('datacommon.delete.single.label'),
            tLib('datacommon.delete.single.confirm'),
            confirmDialogButtons,
            confirmDialogComponentProps
        )
            .then((value: any) => {
                if (value && apiIsReady) {
                    apiDelete(id)
                        .then(() => {
                            gridApiRef.current?.refresh();
                            onDelete?.();
                            temporalMessageShow(
                                null,
                                tLib('datacommon.delete.single.success'),
                                'success'
                            );
                        })
                        .catch((error: ResourceApiError) => {
                            temporalMessageShow(
                                tLib('datacommon.delete.single.error'),
                                error.message,
                                'error'
                            );
                        });
                }
            })
            .catch(() => {});
    };

    return (
        <Box
            sx={{
                mt: 2,
                width: '900px',
            }}
        >
            <MuiDataGrid
                title={title}
                apiRef={gridApiRef}
                height={500}
                resourceName={resourceName}
                filter={`dashboard.id : ${dashboardId}`}
                popupEditFormDialogResourceTitle={''}
                rowHideUpdateButton
                popupEditUpdateActive
                popupEditFormContent={form}
                popupEditFormComponentProps={{
                    onUpdateSuccess: onUpdate,
                }}
                columns={[
                    ...baseColumns,
                    {
                        field: 'position',
                        flex: 1,
                        headerName: t($ => $.page.widget.grid.position),
                        valueGetter: (_value, row) => `${row.posX}, ${row.posY}`,
                    },
                    {
                        field: 'size',
                        flex: 1,
                        headerName: t($ => $.page.widget.grid.size),
                        valueGetter: (_value, row) => `${row.width}, ${row.height}`,
                    },
                ]}
                rowHeight={26}
                columnHeaderHeight={30}
                rowActionsColumnProps={{ width: 10 }}
                rowAdditionalActions={[
                    {
                        label: tLib('datacommon.update.label'),
                        icon: 'edit',
                        clickShowUpdateDialog: true,
                        hidden: !form,
                    },
                    {
                        label: tLib('datacommon.delete.label'),
                        icon: 'delete',
                        onClick: onDeleteClick,
                    },
                ]}
                rowHideDeleteButton
                toolbarHideCreate
            />
        </Box>
    );
};

export const AfegirTitolFormContent = () => {
    const { data } = useFormContext();

    return (<Grid container spacing={2}>
        <Grid size={12}>
            <TitolWidgetVisualization {...data}/>
        </Grid>
        <Grid size={12}>
            <FormField name="titol" />
        </Grid>
        <Grid size={12}>
            <FormField name="subtitol" />
        </Grid>
        <Grid size={12}>
            <FormField name="tipusTitol" />
        </Grid>
        <Grid size={6}>
            <FormField name="midaFontTitol" />
        </Grid>
        <Grid size={6}>
            <FormField name="midaFontSubtitol" />
        </Grid>
        <Grid size={6}>
            <FormField name="colorTitol" type={"color"} />
        </Grid>
        <Grid size={6}>
            <FormField name="colorSubtitol" type={"color"} />
        </Grid>
        <Grid size={6}>
            <FormField name="colorFons" type={"color"} />
        </Grid>
        <Grid size={3} sx={{
            minHeight: "53px", // TODO Evitar layout shift
        }}>
            <FormField name="mostrarVora" />
        </Grid>
        {data?.mostrarVora ? (
            <>
                {/*<Grid size={3}>*/}
                {/*    <FormField name="mostrarVoraBottom" type={"checkbox"} /> TODO Desactivado hasta que se añada la columna en base de datos */}
                {/*</Grid>*/}
                <Grid size={6}>
                    <FormField name="colorVora" type={"color"} />
                </Grid>
                <Grid size={6}>
                    <FormField name="ampleVora" />
                </Grid>
            </>
        ) : (
            <Grid size={8} />
        )}
    </Grid>);
};

const EstadisticaDashboardEdit: React.FC = () => {
    const { t } = useTranslation();
    const { id: paramsId } = useParams();
    const dashboardId = paramsId as string;
    const theme = useTheme();
    const temaFosc = theme.palette.mode === 'dark';
    const {
        isReady: apiDashboardItemIsReady,
        patch: patchDashboardItem,
        create: createDashboardItem,
    } = useResourceApiService('dashboardItem');
    const {
        isReady: apiDashboardTitolIsReady,
        patch: patchDashboardTitol,
    } = useResourceApiService('dashboardTitol');
    const { temporalMessageShow, t: tLib, goBack } = useBaseAppContext();
    const {
        dashboard,
        loading: loadingDashboard,
        exception: dashboardException,
    } = useDashboard(dashboardId);
    const {
        dashboardWidgets,
        errorDashboardWidgets,
        loadingWidgetPositions,
        forceRefresh: forceRefreshDashboardWidgets,
    } = useDashboardWidgets(dashboardId, temaFosc);
    const [panelWidth, setPanelWidth] = useState(340);
    const [panelCollapsed, setPanelCollapsed] = useState(false);
    const panelWidthRef = useRef(panelWidth);
    panelWidthRef.current = panelWidth;

    const handleResizeMouseDown = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        const startX = e.clientX;
        const startWidth = panelWidthRef.current;
        const onMouseMove = (ev: MouseEvent) => {
            const newWidth = Math.max(240, Math.min(700, startWidth + (startX - ev.clientX)));
            setPanelWidth(newWidth);
        };
        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
        };
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    }, []);

    const [leftPanelWidth, setLeftPanelWidth] = useState(240);
    const [leftPanelCollapsed, setLeftPanelCollapsed] = useState(false);
    const leftPanelWidthRef = useRef(leftPanelWidth);
    leftPanelWidthRef.current = leftPanelWidth;

    const handleLeftResizeMouseDown = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        const startX = e.clientX;
        const startWidth = leftPanelWidthRef.current;
        const onMouseMove = (ev: MouseEvent) => {
            const newWidth = Math.max(160, Math.min(600, startWidth + (ev.clientX - startX)));
            setLeftPanelWidth(newWidth);
        };
        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
        };
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    }, []);

    const [showContentDialog, contentDialogComponent] = useContentDialog();
    const messageDialogButtons = useMessageDialogButtons();
    const [editorSelection, setEditorSelection] = useState<DashboardEditorSelection>({ kind: 'none' });
    useEffect(() => {
        if (editorSelection.kind !== 'none' && panelCollapsed) {
            setPanelCollapsed(false);
        }
    }, [editorSelection]);
    const navigate = useNavigate();
    const openCreateTitolForm = () => {
        setEditorSelection({ kind: 'title', mode: 'create' });
    };

    const openCreateWidgetForm = (widgetType?: DashboardWidgetType, entornId?: any, aplicacio?: any) => {
        setEditorSelection({ kind: 'widget', mode: 'create', widgetType, entornId, aplicacio });
    };

    const addWidget = (widgetId: any, entornId: any, widgetType?: DashboardWidgetType) => {
        createDashboardItem({
            data: {
                dashboard: { id: dashboardId },
                widget: { id: widgetId },
                entornId,
                ...defaultSizeAndPosition,
            },
        })
            .then(async (createdItem: any) => {
                temporalMessageShow(null, t($ => $.page.dashboards.action.addWidget.success), 'success');
                forceRefreshDashboardWidgets();
                if (createdItem?.id && widgetType) {
                    setEditorSelection({
                        kind: 'widget',
                        mode: 'edit',
                        widgetType,
                        dashboardItemId: createdItem.id,
                        widgetId,
                    });
                }
            })
            .catch((reason) => {
                temporalMessageShow(null, t($ => $.page.dashboards.action.addWidget.error), 'error');
                console.error('Widget add error', reason);
            });
    };

    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);

    const selectedGridItemId = React.useMemo(() => {
        if (editorSelection.kind === 'widget' && editorSelection.mode === 'edit') {
            return String(editorSelection.dashboardItemId);
        }
        if (editorSelection.kind === 'title' && editorSelection.mode === 'edit') {
            return String(editorSelection.dashboardTitolId);
        }
        return null;
    }, [editorSelection]);

    const selectDashboardElement = (entity: any) => {
        if (!entity) {
            setEditorSelection({ kind: 'none' });
            return;
        }
        if (entity.tipus === 'TITOL') {
            setEditorSelection({
                kind: 'title',
                mode: 'edit',
                dashboardTitolId: entity.dashboardTitolId ?? entity.id,
            });
            return;
        }
        if (entity.tipus === 'SIMPLE' || entity.tipus === 'GRAFIC' || entity.tipus === 'TAULA') {
            setEditorSelection({
                kind: 'widget',
                mode: 'edit',
                widgetType: entity.tipus,
                dashboardItemId: entity.dashboardItemId ?? entity.id,
                widgetId: entity.widgetId,
            });
        }
    };

    const onGridLayoutItemsChange = (newLayoutItems: GridLayoutItem[]) => {
        const promises: Promise<any>[] = [];
        mappedDashboardItems.forEach((oldDashboardItem: GridLayoutItem) => {
            const newDashboardItem = newLayoutItems.find(
                (newLayoutItem: GridLayoutItem) => newLayoutItem.id === oldDashboardItem.id
            );

            if (newDashboardItem === undefined) {
                console.error(t($ => $.page.dashboards.action.patchItem.warning, oldDashboardItem));
            } else if (!isEqual(oldDashboardItem, newDashboardItem)) {
                const patchArgs = {
                    data: {
                        posX: newDashboardItem.x,
                        posY: newDashboardItem.y,
                        width: newDashboardItem.w,
                        height: newDashboardItem.h,
                    },
                };
                const isTitol = newDashboardItem.type === 'TITOL';
                const patchPromise = !isTitol
                    ? patchDashboardItem(oldDashboardItem.id, patchArgs)
                    : patchDashboardTitol(oldDashboardItem.id, patchArgs);
                promises.push(patchPromise);
            }
        });

        Promise.all(promises)
            .then(() => {
                temporalMessageShow(null, t($ => $.page.dashboards.action.patchItem.success), 'success');
            })
            .catch((reason) => {
                temporalMessageShow(null, t($ => $.page.dashboards.action.patchItem.error), 'error');
                console.error(t($ => $.page.dashboards.action.patchItem.saveError), reason);
            });
    };

    const loading = loadingDashboard || loadingWidgetPositions;

    if (dashboardException) {
        if (dashboardException.status === 404) {
            return (
                <Alert
                    severity="warning"
                    action={
                        <Button onClick={() => navigate(`/${DASHBOARDS_PATH}`)}>
                            {t($ => $.page.dashboards.alert.tornarLlistat)}
                        </Button>
                    }
                >
                    {t($ => $.page.dashboards.alert.notExists)}
                </Alert>
            );
        } else return <Alert severity="error">{t($ => $.page.dashboards.alert.carregar)}</Alert>;
    }

    return (
        <Box sx={{
            flex: 1,
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
        }}>
            <PageTitle title={t($ => $.page.dashboards.title)} />
            {contentDialogComponent}
            {loading ? <CenteredCircularProgress /> : null}
            {dashboard && (<>
                <MuiToolbar
                    disableGutters
                    sx={{
                        width: '100%',
                        display: 'flex',
                        justifyContent: 'space-between',
                        px: 2,
                        ml: 0,
                        mr: 0,
                        mt: 0,
                        backgroundColor: (theme) => theme.palette.mode === 'light' ? theme.palette.grey[200] : theme.palette.grey[900],
                    }}
                >
                    <Box>
                        <IconButton
                            title={tLib('form.goBack.title')}
                            onClick={() => goBack(`/${DASHBOARDS_PATH}`)}
                        >
                            <Icon>arrow_back</Icon>
                        </IconButton>
                        <Typography
                            sx={{
                                display: 'inline',
                                mx: 2,
                            }}
                        >
                            {dashboard.titol}
                        </Typography>
                    </Box>
                    <Box>
                        {errorDashboardWidgets?.length ? (
                            <WidgetsErrorAlert errorWidgets={errorDashboardWidgets} />
                        ) : undefined}
                        <ButtonMenu title={t($ => $.page.dashboards.components.llistar)}>
                            {[
                                {
                                    icon: 'widgets',
                                    title: t($ => $.page.dashboards.action.llistarWidget.label),
                                    resourceName: 'dashboardItem',
                                    baseColumns: [
                                        {
                                            field: 'widget',
                                            flex: 1,
                                        },
                                        {
                                            field: 'entornId',
                                            flex: 1,
                                        },
                                    ],
                                },
                                {
                                    icon: 'title',
                                    title: t($ => $.page.dashboards.action.llistarTitle.label),
                                    resourceName: 'dashboardTitol',
                                    form: <AfegirTitolFormContent />,
                                    baseColumns: [
                                        {
                                            field: 'titol',
                                            flex: 1,
                                        },
                                    ],
                                },
                            ].map((item, index) => (
                                <MenuItem
                                    key={'llistarComponents-' + index}
                                    onClick={() =>
                                        showContentDialog(
                                            '',
                                            <ListWidgetDialogContent
                                                title={item.title}
                                                baseColumns={item.baseColumns}
                                                resourceName={item.resourceName}
                                                form={item.form}
                                                dashboardId={dashboardId}
                                                onDelete={forceRefreshDashboardWidgets}
                                                onUpdate={forceRefreshDashboardWidgets}
                                            />,
                                            messageDialogButtons,
                                            {
                                                maxWidth: 'lg',
                                            }
                                        )
                                    }
                                >
                                    <ListItemIcon>
                                        <Icon fontSize="small">{item.icon}</Icon>
                                    </ListItemIcon>
                                    <ListItemText>{item.title}</ListItemText>
                                </MenuItem>
                            ))}
                        </ButtonMenu>
                        <ButtonMenu
                            title={t($ => $.page.dashboards.components.afegir)}
                            disabled={!apiDashboardItemIsReady || !dashboard}
                            buttonIcon={<AddIcon />}
                        >
                            <MenuItem onClick={() => openCreateWidgetForm(undefined, dashboard?.entorn?.id, dashboard?.aplicacio)}>
                                <ListItemIcon>
                                    <Icon fontSize="small">widgets</Icon>
                                </ListItemIcon>
                                <ListItemText>
                                    {t($ => $.page.dashboards.action.addWidget.label)}
                                </ListItemText>
                            </MenuItem>
                            <MenuItem onClick={openCreateTitolForm}>
                                <ListItemIcon>
                                    <Icon fontSize="small">title</Icon>
                                </ListItemIcon>
                                <ListItemText>
                                    {t($ => $.page.dashboards.action.afegirTitle.label)}
                                </ListItemText>
                            </MenuItem>
                        </ButtonMenu>
                        {/*<DashboardSideMenu dashboard={dashboard} addAction={addWidget}/>*/}
                    </Box>
                </MuiToolbar>
                <Box sx={{ flex: 1, overflow: 'hidden', display: 'flex', minHeight: 0 }}>
                    {/* Left panel: resizable and collapsible */}
                    <Box
                        sx={{
                            width: leftPanelCollapsed ? '40px' : `${leftPanelWidth}px`,
                            flexShrink: 0,
                            height: '100%',
                            display: 'flex',
                            flexDirection: 'row',
                        }}
                    >
                        {/* Panel content */}
                        <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
                            {/* Collapse/expand toggle */}
                            <Box
                                sx={{
                                    display: 'flex',
                                    justifyContent: leftPanelCollapsed ? 'center' : 'flex-end',
                                    backgroundColor: 'background.paper',
                                    borderRight: '1px solid',
                                    borderColor: 'divider',
                                    borderBottom: '1px solid',
                                    py: 0.5,
                                    px: leftPanelCollapsed ? 0 : 0.5,
                                    flexShrink: 0,
                                }}
                            >
                                <IconButton
                                    size="small"
                                    onClick={() => setLeftPanelCollapsed(c => !c)}
                                    title={leftPanelCollapsed ? 'Expandir panell' : 'Compactar panell'}
                                >
                                    <Icon sx={{ fontSize: '1rem' }}>
                                        {leftPanelCollapsed ? 'chevron_right' : 'chevron_left'}
                                    </Icon>
                                </IconButton>
                            </Box>
                            <Box sx={{ flex: 1, overflow: 'hidden', borderRight: '1px solid', borderColor: 'divider', display: leftPanelCollapsed ? 'none' : 'flex', flexDirection: 'column' }}>
                                <SideMenu
                                    dashboard={dashboard}
                                    addWidget={addWidget}
                                    createWidget={openCreateWidgetForm}
                                    dashboardWidgets={dashboardWidgets}
                                    onSelectItem={selectDashboardElement}
                                    selectedItemId={selectedGridItemId}
                                />
                            </Box>
                        </Box>
                        {/* Resize handle on right edge */}
                        {!leftPanelCollapsed && (
                            <Box
                                onMouseDown={handleLeftResizeMouseDown}
                                sx={{
                                    width: '5px',
                                    flexShrink: 0,
                                    cursor: 'ew-resize',
                                    backgroundColor: 'divider',
                                    '&:hover': { backgroundColor: 'primary.main', opacity: 0.6 },
                                }}
                            />
                        )}
                    </Box>
                    {/* Canvas + overlay panel wrapper */}
                    <Box sx={{ flex: 1, position: 'relative', display: 'flex', minHeight: 0, overflow: 'hidden' }}>
                        {/* Scrollable canvas area */}
                        <Box sx={{ flex: 1, overflow: 'auto', minHeight: 0 }}>
                            {apiDashboardItemIsReady && apiDashboardTitolIsReady && dashboardWidgets && (
                                <DashboardReactGridLayout
                                    dashboardId={dashboard.id}
                                    dashboardWidgets={dashboardWidgets}
                                    gridLayoutItems={mappedDashboardItems}
                                    onGridLayoutItemsChange={onGridLayoutItemsChange}
                                    onSelectItem={selectDashboardElement}
                                    onClearSelection={() => setEditorSelection({ kind: 'none' })}
                                    selectedItemId={selectedGridItemId}
                                    editable
                                />
                            )}
                        </Box>
                        {/* Overlay side panel (non-scrolling, always in view) */}
                        <Box
                            sx={{
                                position: 'absolute',
                                top: 0,
                                right: 0,
                                height: '100%',
                                width: panelCollapsed ? '40px' : `${panelWidth}px`,
                                display: 'flex',
                                flexDirection: 'row',
                                zIndex: 20,
                                pointerEvents: 'none',
                            }}
                        >
                            {/* Resize handle */}
                            {!panelCollapsed && (
                                <Box
                                    onMouseDown={handleResizeMouseDown}
                                    sx={{
                                        width: '5px',
                                        flexShrink: 0,
                                        cursor: 'ew-resize',
                                        backgroundColor: 'divider',
                                        pointerEvents: 'all',
                                        '&:hover': { backgroundColor: 'primary.main', opacity: 0.6 },
                                    }}
                                />
                            )}
                            {/* Panel content */}
                            <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', pointerEvents: 'all', overflow: 'hidden' }}>
                                {/* Collapse/expand toggle */}
                                <Box
                                    sx={{
                                        display: 'flex',
                                        justifyContent: panelCollapsed ? 'center' : 'flex-start',
                                        backgroundColor: 'background.paper',
                                        borderLeft: '1px solid',
                                        borderColor: 'divider',
                                        borderBottom: '1px solid',
                                        py: 0.5,
                                        px: panelCollapsed ? 0 : 0.5,
                                        flexShrink: 0,
                                    }}
                                >
                                    <IconButton
                                        size="small"
                                        onClick={() => setPanelCollapsed(c => !c)}
                                        title={panelCollapsed ? 'Expandir panell' : 'Compactar panell'}
                                    >
                                        <Icon sx={{ fontSize: '1rem' }}>
                                            {panelCollapsed ? 'chevron_left' : 'chevron_right'}
                                        </Icon>
                                    </IconButton>
                                </Box>
                                {!panelCollapsed && (
                                    <Box sx={{ flex: 1, overflow: 'hidden', borderLeft: '1px solid', borderColor: 'divider' }}>
                                        <DashboardEditorSidePanel
                                            dashboard={dashboard}
                                            dashboardId={dashboardId}
                                            selection={editorSelection}
                                            onSelectionChange={setEditorSelection}
                                            onSaved={forceRefreshDashboardWidgets}
                                            onDeleted={() => {
                                                setEditorSelection({ kind: 'none' });
                                                forceRefreshDashboardWidgets();
                                            }}
                                        />
                                    </Box>
                                )}
                            </Box>
                        </Box>
                    </Box>
                </Box>
                <FooterHeightPlaceholder />
            </>)}
        </Box>
    );
};

const TIPUS_ICON: Record<string, string> = {
    SIMPLE: 'description',
    GRAFIC: 'bar_chart_4_bars',
    TAULA: 'table',
    TITOL: 'title',
};

const SideMenu = ({ dashboard, addWidget, createWidget, dashboardWidgets, onSelectItem, selectedItemId }:any) => {
    // const { t } = useTranslation();
    const appEntornFilterApiRef = useFilterApiRef();
    const [springFilter, setSpringFilter] = useState<string>()
    const [entornId, setEntornId] = useState<string>(dashboard?.entorn?.id)
    const [aplicacio, setAplicacio] = useState<any>(dashboard?.aplicacio)
    const [simpleWidgets, setSimpleWidgets] = useState<any[]>()
    const [graficWidgets, setGraficWidgets] = useState<any[]>()
    const [taulaWidgets, setTaulaWidgets] = useState<any[]>()

    const {
        isReady: apiSimpleIsReady,
        find: apiSimpleFind,
    } = useResourceApiService('estadisticaSimpleWidget');
    const {
        isReady: apiGraficIsReady,
        find: apiGraficFind,
    } = useResourceApiService('estadisticaGraficWidget');
    const {
        isReady: apiTaulaIsReady,
        find: apiTaulaFind,
    } = useResourceApiService('estadisticaTaulaWidget');

    useEffect(() => {
        // console.log("springFilter", springFilter)
        if (apiSimpleIsReady && apiGraficIsReady && apiTaulaIsReady) {
            apiSimpleFind({filter: springFilter, unpaged:true})
                .then((response) => setSimpleWidgets(response.rows))
            apiGraficFind({filter: springFilter, unpaged:true})
                .then((response) => setGraficWidgets(response.rows))
            apiTaulaFind({filter: springFilter, unpaged:true})
                .then((response) => setTaulaWidgets(response.rows))
        }
    }, [springFilter, apiSimpleIsReady, apiGraficIsReady, apiTaulaIsReady]);

    const WidgetTreeItem = ({widget, widgetType}:any) => <TreeItem key={widget?.id} itemId={widget?.id} label={<Box
        display={'flex'}
        flexDirection={'row'}
        justifyContent={'space-between'}
        alignItems={'center'}
    >
        {widget?.titol}
        {entornId &&
            <IconButton
                size={'small'}
                aria-label={`Afegir ${widget?.titol}`}
                onClick={() => addWidget(widget?.id, entornId, widgetType)}
            >
                <Icon sx={{ fontSize: '0.875rem' }}>add</Icon>
            </IconButton>
        }
    </Box>} />

    return <Paper elevation={1} sx={{ p: 1, height: '100%', display: 'flex', flexDirection: 'column' }}>
        <MuiFilter
            initialData={dashboard?.aplicacio && {
                app: {
                    id: dashboard?.aplicacio?.id,
                    description: dashboard?.aplicacio?.description,
                },
                entorn: {
                    id: dashboard?.entorn?.id,
                    description: dashboard?.entorn?.description,
                },
            }}
            detached
            apiRef={appEntornFilterApiRef}
            resourceName="entornApp"
            code="optional_entornApp_filter"
            commonFieldComponentProps={{ size: 'small' }}
            onDataChange={(data) => {
                    setEntornId(data?.entorn?.id);
                    setAplicacio(data?.app);
                }}
            springFilterBuilder={(data:any) => springFilterBuilder.and(
                springFilterBuilder.eq("appId", data?.app?.id)
            )}
            onSpringFilterChange={setSpringFilter}>
            <Grid container spacing={1}>
                <Grid size={12}><Typography>Seleccionar aplicació</Typography></Grid>
                <Grid size={12}><FormField name="entorn" disabled={dashboard?.entorn} /></Grid>
                <Grid size={12}><FormField name="app" disabled={dashboard?.aplicacio} /></Grid>
            </Grid>
        </MuiFilter>

        <Divider sx={{ my: 1 }}/>

        <Box sx={{
            overflow: 'auto',
            minHeight: 0,
            maxHeight: '100%'
        }}>
        <SimpleTreeView
            sx={{
                overflow: 'hidden',
                '& .MuiTreeItem-label': {
                    fontSize: '0.875rem', // 14px
                    // fontWeight: '500', // opcional
                }
            }}>
            <TreeItem key={'simple'} itemId={'simple'} label={'Simple'}>
                {simpleWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget} widgetType="SIMPLE"/>
                )}
            </TreeItem>
            <TreeItem key={'grafic'} itemId={'grafic'} label={'Grafic'}>
                {graficWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget} widgetType="GRAFIC"/>
                )}
            </TreeItem>
            <TreeItem key={'taula'} itemId={'taula'} label={'Taula'}>
                {taulaWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget} widgetType="TAULA"/>
                )}
            </TreeItem>
        </SimpleTreeView>

        {dashboardWidgets?.length > 0 && (
            <>
                <Divider sx={{ my: 1 }}/>
                <Typography variant="caption" sx={{ px: 0.5, fontWeight: 700, color: 'text.secondary', display: 'block' }}>
                    Elements del dashboard
                </Typography>
                {dashboardWidgets.map((widget: any) => {
                    const itemId = String(widget.dashboardItemId ?? widget.dashboardTitolId);
                    const isSelected = selectedItemId === itemId;
                    return (
                        <Box
                            key={itemId}
                            onClick={() => onSelectItem?.(widget)}
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: 0.5,
                                px: 1,
                                py: 0.25,
                                cursor: 'pointer',
                                borderRadius: 1,
                                fontSize: '0.875rem',
                                backgroundColor: isSelected ? 'primary.main' : 'transparent',
                                color: isSelected ? 'primary.contrastText' : 'inherit',
                                '&:hover': { backgroundColor: isSelected ? 'primary.dark' : 'action.hover' },
                            }}
                        >
                            <Icon sx={{ fontSize: '0.875rem' }}>{TIPUS_ICON[widget.tipus] ?? 'widgets'}</Icon>
                            <Box component="span" sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                {widget.titol ?? itemId}
                            </Box>
                        </Box>
                    );
                })}
            </>
        )}
        </Box>

        <Box sx={{ mt: 'auto' }}>
            <Divider sx={{ my: 1, alignSelf: 'end' }}/>
        </Box>

        <WidgetMenu createWidget={createWidget} entornId={entornId} aplicacio={aplicacio}/>
    </Paper>
}

const WidgetMenu = ({createWidget, entornId, aplicacio}:any) => {
    return <>
        <MyButtonMenu
            title={'Nou Widget'}
            buttonProps={{
                variant: 'contained',
                startIcon: <Icon>add</Icon>,
                sx: { width: '100%' },
            }}
        >
            {[
                {
                    icon: 'description',
                    title: 'Simple',
                    onClick: () => createWidget('SIMPLE', entornId, aplicacio),
                },
                {
                    icon: 'bar_chart_4_bars',
                    title: 'Gràfic',
                    onClick: () => createWidget('GRAFIC', entornId, aplicacio),
                },
                {
                    icon: 'table',
                    title: 'Taula',
                    onClick: () => createWidget('TAULA', entornId, aplicacio),
                },
            ].map((item, index) => (
                <MenuItem
                    key={'nowWidget-' + index}
                    onClick={() => item?.onClick?.()}
                >
                    <ListItemIcon>
                        <Icon fontSize="small">{item.icon}</Icon>
                    </ListItemIcon>
                    <ListItemText>{item.title}</ListItemText>
                </MenuItem>
            ))}
        </MyButtonMenu>
    </>
}
export const useSimpleWidgetFormDialog = () => {
    const formApiRef = React.useRef<MuiFormDialogApi>(undefined)

    const handleOpen = (id?:any) => {
        return formApiRef?.current?.show(id)
    }

    const dialog = <>
        <MuiFormDialog
            resourceName={'estadisticaSimpleWidget'}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'xl' }}
            apiRef={formApiRef}
        >
            <EstadisticaSimpleWidgetForm/>
        </MuiFormDialog>
    </>

    return {
        handleOpen,
        dialog,
    }
}
export const useGraficWidgetFormDialog = () => {
    const formApiRef = React.useRef<MuiFormDialogApi>(undefined)

    const handleOpen = (id?:any) => {
        return formApiRef?.current?.show(id)
    }

    const dialog = <>
        <MuiFormDialog
            resourceName={'estadisticaGraficWidget'}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'xl' }}
            apiRef={formApiRef}
        >
            <EstadisticaGraficWidgetForm/>
        </MuiFormDialog>
    </>

    return {
        handleOpen,
        dialog,
    }
}
export const useTaulaWidgetFormDialog = () => {
    const formApiRef = React.useRef<MuiFormDialogApi>(undefined)

    const handleOpen = (id?:any) => {
        return formApiRef?.current?.show(id)
    }

    const dialog = <>
        <MuiFormDialog
            resourceName={'estadisticaTaulaWidget'}
            dialogComponentProps={{ fullWidth: true, maxWidth: 'xl' }}
            apiRef={formApiRef}
        >
            <EstadisticaTaulaWidgetForm/>
        </MuiFormDialog>
    </>

    return {
        handleOpen,
        dialog,
    }
}


const MyButtonMenu = ({ buttonProps, title, disabled, children }:any) => {
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);
    const handleClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };
    return (
        <>
            <Button
                onClick={handleClick}
                disabled={disabled}
                {...buttonProps}
            >
                {title}
            </Button>
            <Menu
                anchorEl={anchorEl}
                id="account-menu"
                open={open}
                onClose={handleClose}
                onClick={handleClose}
            >
                {children}
            </Menu>
        </>
    );
};

// type DashboardSideMenuProps = {
//     dashboard: any;
//     addAction: (widgetId: any, entornId: any) => void;
// };
// const DashboardSideMenu = ({dashboard, addAction}: DashboardSideMenuProps) => {
//     const { t } = useTranslation()
//
//     const [open, setOpen] = React.useState<boolean>(false);
//     const handelOpen = () => setOpen(true)
//     const handelClose = () => setOpen(false)
//
//     const [filterData, setFilterData] = useState<any>(null);
//     const [filterString, setFilterString] = useState<string>('');
//     const [widgetsSimple, setWidgetsSimple] = useState<any[]>([]);
//     const [widgetsGrafic, setWidgetsGrafic] = useState<any[]>([]);
//     const [widgetsTaula , setWidgetsTaula ] = useState<any[]>([]);
//
//     const { isReady: isReadySimple , find: findSimple } = useResourceApiService('estadisticaSimpleWidget');
//     const { isReady: isReadyGrafic , find: findGrafic } = useResourceApiService('estadisticaGraficWidget');
//     const { isReady: isReadyTaula  , find: findTaula  } = useResourceApiService('estadisticaTaulaWidget');
//
//     const refreshSimple = () => {
//         if(isReadySimple) {
//             findSimple({unpaged: true, filter: filterString})
//                 .then((data:any) => setWidgetsSimple(data?.rows ?? []))
//         }
//     }
//     const refreshGrafic = () => {
//         if(isReadyGrafic) {
//             findGrafic({unpaged: true, filter: filterString})
//                 .then((data:any) => setWidgetsGrafic(data?.rows ?? []))
//         }
//     }
//     const refreshTaula = () => {
//         if(isReadyTaula) {
//             findTaula({unpaged: true, filter: filterString})
//                 .then((data:any) => setWidgetsTaula(data?.rows ?? []))
//         }
//     }
//
//     useEffect(() => {
//         if (filterString) {
//             refreshSimple()
//             refreshGrafic()
//             refreshTaula()
//         }
//     }, [filterString]);
//
//     const width = 400
//     return (
//         <>
//             <IconButton
//                 color="inherit"
//                 aria-label="open menu"
//                 onClick={handelOpen}
//                 edge="start"
//                 sx={{ mr: 2 }}
//             >
//                 <Icon sx={{ fontSize: '24px'}} fontSize={'medium'}>menu</Icon>
//             </IconButton>
//             {open && <ShrinkableDrawer
//                 className={"side-menu"}
//                 variant={'permanent'}
//                 open={true}
//                 {...{ width: width }}
//                 sx={{
//                     '& .MuiDrawer-paper': { right: 0, left: 'auto', backgroundColor: '#ef955e', color: '#fff', pt: '64px' },
//                 }}>
//                 <SideWrapper style={{width: `calc(100% - ${width}px)`}} onOutsideClick={handelClose}>
//                     <Box sx={{p: 1}}>
//                         <Typography variant={'h5'} color={'white'}>{t($ => $.page.dashboards.action.addWidget.title)}</Typography>
//
//                         <EntornAppFilter
//                             onDataChange={setFilterData}
//                             onSpringFilterChange={(filter) => setFilterString(filter ?? "")}
//                             initialData={{
//                                 app: dashboard?.aplicacio,
//                                 entorn: dashboard?.entorn
//                             }}
//                         />
//
//                         <List hidden={!filterData?.entorn?.id}>
//                             <ExpandElementList label={t($ => $.page.widget.simple.tab.title)} icon={'border_clear'}>
//                                 {widgetsSimple.map((widget:any) => <Box key={`simple-${widget?.id}`} sx={{ p: 1 }} onDoubleClick={()=>{addAction(widget?.id, filterData.entorn.id)}}>
//                                     <SimpleWidgetVisualization {...widget}/>
//                                 </Box>)}
//                             </ExpandElementList>
//                             <ExpandElementList label={t($ => $.page.widget.grafic.tab.title)} icon={'align_vertical_bottom'}>
//                                 {widgetsGrafic.map((widget:any) => <Box key={`grafic-${widget?.id}`} sx={{ p: 1 }} onDoubleClick={()=>{addAction(widget?.id, filterData.entorn.id)}}>
//                                     <GraficWidgetVisualization {...widget}/>
//                                 </Box>)}
//                             </ExpandElementList>
//                             <ExpandElementList label={t($ => $.page.widget.taula.tab.title)} icon={'table_view'}>
//                                 {widgetsTaula.map((widget:any) => <Box key={`taula-${widget?.id}`} sx={{ p: 1 }} onDoubleClick={()=>{addAction(widget?.id, filterData.entorn.id)}}>
//                                     <TaulaWidgetVisualization {...widget}/>
//                                 </Box>)}
//                             </ExpandElementList>
//                         </List>
//                     </Box>
//                 </SideWrapper>
//             </ShrinkableDrawer>}
//         </>
//     );
// }

// type ExpandElementListProps = {
//     label: string;
//     icon?: string;
//     children: React.ReactNode;
// };
//
// const ExpandElementList = ({label, icon, children}: ExpandElementListProps) => {
//     const [open, setOpen] = React.useState(false);
//
//     const handleClick = () => {
//         setOpen(!open);
//     };
//     return <>
//         <ListItemButton onClick={handleClick}>
//             {icon && <Icon sx={{mr: 1}}>{icon}</Icon>}
//             <ListItemText primary={label} />
//             {open ? <Icon>expand_less</Icon> : <Icon>expand_more</Icon>}
//         </ListItemButton>
//         <Collapse in={open} timeout="auto" unmountOnExit>
//             <Box sx={{ ml: 2 }}>
//                 {children}
//             </Box>
//         </Collapse>
//     </>
// }

export default EstadisticaDashboardEdit;
