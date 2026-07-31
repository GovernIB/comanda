import * as React from 'react';
import MuiToolbar from '@mui/material/Toolbar';
import {
    useBaseAppContext,
    useResourceApiService,
    MuiFilter,
    springFilterBuilder,
    FormField,
    useMessageDialogButtons,
    useConfirmDialogButtons,
    useFilterApiRef,
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
import WidgetCreationWizard from '../components/estadistiques/WidgetCreationWizard.tsx';
import { isEqual } from 'lodash';
import {
    Alert,
    Box,
    Button,
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
import PageTitle from '../components/PageTitle.tsx';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';
import {SimpleTreeView, TreeItem} from "@mui/x-tree-view";
import Divider from "@mui/material/Divider";
import { useTheme } from '@mui/material/styles';
import { useEntornCodi } from '../components/estadistiques/dashboardPlantillaHook.ts';

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
            <TableContainer component={Paper} sx={{ mt: 3, }} >
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

/**
 * Genera un títol de duplicat afegint un número seqüencial entre parèntesis, evitant col·lisions amb
 * els títols ja existents al dashboard (p. ex. "Widget" -> "Widget (2)" -> "Widget (3)").
 */
const buildDuplicateTitle = (originalTitle: string, existingTitles: string[]): string => {
    const match = originalTitle?.match(/^(.*) \((\d+)\)$/);
    const baseTitle = match ? match[1] : (originalTitle ?? '');
    const escapedBase = baseTitle.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const pattern = new RegExp(`^${escapedBase} \\((\\d+)\\)$`);
    let maxSeq = 1;
    existingTitles.forEach(title => {
        const titleMatch = title?.match(pattern);
        if (titleMatch) {
            maxSeq = Math.max(maxSeq, parseInt(titleMatch[1], 10));
        }
    });
    return `${baseTitle} (${maxSeq + 1})`;
};

const PANEL_COLLAPSED_STORAGE_PREFIX = 'comanda.dashboardEdit.panelCollapsed.';

/** Recorda si un panell del dissenyador de dashboards està contret o expandit entre sessions. */
const useStoredPanelCollapsed = (key: string): [boolean, React.Dispatch<React.SetStateAction<boolean>>] => {
    const storageKey = PANEL_COLLAPSED_STORAGE_PREFIX + key;
    const [collapsed, setCollapsed] = useState<boolean>(() => {
        try {
            return localStorage.getItem(storageKey) === 'true';
        } catch {
            return false;
        }
    });
    useEffect(() => {
        try {
            localStorage.setItem(storageKey, String(collapsed));
        } catch {
            // localStorage no disponible (mode privat, etc.): es descarta silenciosament
        }
    }, [collapsed, storageKey]);
    return [collapsed, setCollapsed];
};

const PANEL_WIDTH_STORAGE_PREFIX = 'comanda.dashboardEdit.panelWidth.';

/** Recorda l'amplada d'un panell del dissenyador de dashboards entre sessions. */
const useStoredPanelWidth = (key: string, defaultWidth: number): [number, React.Dispatch<React.SetStateAction<number>>] => {
    const storageKey = PANEL_WIDTH_STORAGE_PREFIX + key;
    const [width, setWidth] = useState<number>(() => {
        try {
            const stored = Number(localStorage.getItem(storageKey));
            return Number.isFinite(stored) && stored > 0 ? stored : defaultWidth;
        } catch {
            return defaultWidth;
        }
    });
    useEffect(() => {
        try {
            localStorage.setItem(storageKey, String(width));
        } catch {
            // localStorage no disponible (mode privat, etc.): es descarta silenciosament
        }
    }, [width, storageKey]);
    return [width, setWidth];
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
        getOne: getOneDashboardItem,
        delete: deleteDashboardItem,
    } = useResourceApiService('dashboardItem');
    const {
        isReady: apiDashboardTitolIsReady,
        patch: patchDashboardTitol,
        create: createDashboardTitol,
        getOne: getOneDashboardTitol,
        delete: deleteDashboardTitol,
    } = useResourceApiService('dashboardTitol');
    const { getOne: getOneSimpleWidget, create: createSimpleWidget } = useResourceApiService('estadisticaSimpleWidget');
    const { getOne: getOneGraficWidget, create: createGraficWidget } = useResourceApiService('estadisticaGraficWidget');
    const { getOne: getOneTaulaWidget, create: createTaulaWidget } = useResourceApiService('estadisticaTaulaWidget');
    const { temporalMessageShow, messageDialogShow, t: tLib, goBack } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const {
        dashboard,
        loading: loadingDashboard,
        exception: dashboardException,
    } = useDashboard(dashboardId);
    const { entornCodi: dashboardEntornCodi, loading: loadingEntornCodi } = useEntornCodi(dashboard?.entorn?.id);
    const {
        dashboardWidgets,
        errorDashboardWidgets,
        loadingWidgetPositions,
        forceRefresh: forceRefreshDashboardWidgets,
        refreshWidget,
    } = useDashboardWidgets(dashboardId, temaFosc);
    const handleWidgetSaved = (dashboardItemId?: any) => {
        if (dashboardItemId != null) {
            refreshWidget(dashboardItemId);
        } else {
            forceRefreshDashboardWidgets();
        }
    };
    const [panelWidth, setPanelWidth] = useStoredPanelWidth('right', 440);
    const [panelCollapsed, setPanelCollapsed] = useStoredPanelCollapsed('right');
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

    const [leftPanelWidth, setLeftPanelWidth] = useStoredPanelWidth('left', 240);
    const [leftPanelCollapsed, setLeftPanelCollapsed] = useStoredPanelCollapsed('left');
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

    const [editorSelection, setEditorSelection] = useState<DashboardEditorSelection>({ kind: 'none' });
    useEffect(() => {
        if (editorSelection.kind !== 'none' && panelCollapsed) {
            setPanelCollapsed(false);
        }
    }, [editorSelection]);
    const navigate = useNavigate();
    const [wizardState, setWizardState] = useState<{
        open: boolean;
        seed: number;
        initialWidgetType?: DashboardWidgetType;
        initialEntornId?: any;
        initialAplicacio?: any;
    }>({ open: false, seed: 0 });
    const openWizard = (initialWidgetType?: DashboardWidgetType, initialEntornId?: any, initialAplicacio?: any) => {
        setWizardState(state => ({
            open: true,
            seed: state.seed + 1,
            initialWidgetType,
            initialEntornId,
            initialAplicacio,
        }));
    };
    const closeWizard = () => setWizardState(state => ({ ...state, open: false }));

    const addWidget = (widgetId: string | number, entornId: string | number, widgetType?: DashboardWidgetType) => {
        createDashboardItem({
            data: {
                dashboard: { id: dashboardId },
                widget: { id: widgetId },
                entornId,
                ...defaultSizeAndPosition,
            },
        })
            .then(async (createdItem: { id?: string | number }) => {
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

    const selectDashboardElement = (entity: { tipus?: string; id?: string | number; dashboardTitolId?: string | number; dashboardItemId?: string | number; widgetId?: string | number } | null | undefined) => {
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
                widgetType: entity.tipus as DashboardWidgetType,
                dashboardItemId: entity.dashboardItemId ?? entity.id,
                widgetId: entity.widgetId,
            });
        }
    };

    const cloneWithoutFields = (data: any, fields: string[]): any => {
        const clone = { ...data };
        fields.forEach(field => delete clone[field]);
        return clone;
    };

    const handleDeleteItem = (entity: any) => {
        if (!entity) return;
        const isTitol = entity.tipus === 'TITOL';
        const entityId = isTitol ? (entity.dashboardTitolId ?? entity.id) : (entity.dashboardItemId ?? entity.id);
        messageDialogShow(
            'Confirmació',
            'Estau segur que voleu esborrar aquest element del dashboard?',
            confirmDialogButtons,
            { maxWidth: 'sm', fullWidth: true }
        ).then((value: any) => {
            if (!value) return;
            const deletePromise = isTitol ? deleteDashboardTitol(entityId) : deleteDashboardItem(entityId);
            deletePromise
                .then(() => {
                    temporalMessageShow(null, 'Element eliminat', 'success');
                    if (
                        (isTitol && editorSelection.kind === 'title' && editorSelection.mode === 'edit' && editorSelection.dashboardTitolId === entityId) ||
                        (!isTitol && editorSelection.kind === 'widget' && editorSelection.mode === 'edit' && editorSelection.dashboardItemId === entityId)
                    ) {
                        setEditorSelection({ kind: 'none' });
                    }
                    forceRefreshDashboardWidgets();
                })
                .catch((reason: any) => {
                    temporalMessageShow(null, reason?.message ?? 'No s\'ha pogut eliminar', 'error');
                    console.error('Widget delete error', reason);
                });
        });
    };

    const handleDuplicateItem = async (entity: any) => {
        if (!entity) return;
        try {
            const existingTitles = (dashboardWidgets ?? []).map((widget: any) => widget.titol).filter(Boolean);
            if (entity.tipus === 'TITOL') {
                const titolId = entity.dashboardTitolId ?? entity.id;
                const rawTitol = await getOneDashboardTitol(titolId);
                const newTitol = buildDuplicateTitle(rawTitol.titol, existingTitles);
                await createDashboardTitol({
                    data: {
                        ...cloneWithoutFields(rawTitol, ['id', 'posY']),
                        titol: newTitol,
                        dashboard: { id: dashboardId },
                    },
                });
            } else if (entity.tipus === 'SIMPLE' || entity.tipus === 'GRAFIC' || entity.tipus === 'TAULA') {
                const widgetType = entity.tipus as DashboardWidgetType;
                const dashboardItemId = entity.dashboardItemId ?? entity.id;
                const widgetId = entity.widgetId;
                const getOneWidget = widgetType === 'SIMPLE' ? getOneSimpleWidget : widgetType === 'GRAFIC' ? getOneGraficWidget : getOneTaulaWidget;
                const createWidget = widgetType === 'SIMPLE' ? createSimpleWidget : widgetType === 'GRAFIC' ? createGraficWidget : createTaulaWidget;
                const [rawWidget, rawDashboardItem] = await Promise.all([
                    getOneWidget(widgetId),
                    getOneDashboardItem(dashboardItemId),
                ]);
                const newTitol = buildDuplicateTitle(rawWidget.titol, existingTitles);
                const newWidget = await createWidget({
                    data: {
                        ...cloneWithoutFields(rawWidget, ['id']),
                        titol: newTitol,
                    },
                });
                await createDashboardItem({
                    data: {
                        ...cloneWithoutFields(rawDashboardItem, ['id', 'posY']),
                        dashboard: { id: dashboardId },
                        widget: { id: newWidget?.id },
                    },
                });
            } else {
                return;
            }
            temporalMessageShow(null, 'Element duplicat correctament', 'success');
            forceRefreshDashboardWidgets();
        } catch (error: any) {
            temporalMessageShow(null, error?.message ?? 'No s\'ha pogut duplicar l\'element', 'error');
            console.error('Widget duplicate error', error);
        }
    };

    const onGridLayoutItemsChange = (newLayoutItems: GridLayoutItem[]) => {
        const promises: Promise<unknown>[] = [];
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

    const loading = loadingDashboard || loadingWidgetPositions || loadingEntornCodi;

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
                        <Typography sx={{ display: 'inline', mx: 2, }} >
                            {dashboard.titol}
                        </Typography>
                    </Box>
                    <Box>
                        {errorDashboardWidgets?.length ? (
                            <WidgetsErrorAlert errorWidgets={errorDashboardWidgets} />
                        ) : undefined}
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            disabled={!apiDashboardItemIsReady || !dashboard}
                            onClick={() => openWizard(undefined, dashboard?.entorn?.id, dashboard?.aplicacio)}
                        >
                            {t($ => $.page.dashboards.action.createComponent.label)}
                        </Button>
                        {/*<DashboardSideMenu dashboard={dashboard} addAction={addWidget}/>*/}
                    </Box>
                </MuiToolbar>
                <Box sx={{ flex: 1, overflow: 'hidden', display: 'flex', minHeight: 0 }}>
                    {/* Canvas + overlay panels wrapper */}
                    <Box sx={{ flex: 1, position: 'relative', display: 'flex', minHeight: 0, overflow: 'hidden' }}>
                        {/* Scrollable canvas area (ocupa tot l'ample, per sota dels panells flotants) */}
                        <Box sx={{ flex: 1, overflow: 'auto', minHeight: 0 }}>
                            {apiDashboardItemIsReady && apiDashboardTitolIsReady && dashboardWidgets && (
                                <DashboardReactGridLayout
                                    dashboardId={dashboard.id}
                                    dashboardWidgets={dashboardWidgets}
                                    gridLayoutItems={mappedDashboardItems}
                                    onGridLayoutItemsChange={onGridLayoutItemsChange}
                                    onSelectItem={selectDashboardElement}
                                    onDeleteItem={handleDeleteItem}
                                    onDuplicateItem={handleDuplicateItem}
                                    onClearSelection={() => setEditorSelection({ kind: 'none' })}
                                    selectedItemId={selectedGridItemId}
                                    dashboardEntornCodi={dashboardEntornCodi}
                                    editable
                                />
                            )}
                        </Box>
                        {/* Overlay left panel (non-scrolling, always in view) */}
                        <Box
                            sx={{
                                position: 'absolute',
                                top: 0,
                                left: 0,
                                height: '100%',
                                width: leftPanelCollapsed ? '40px' : `${leftPanelWidth}px`,
                                display: 'flex',
                                flexDirection: 'row',
                                zIndex: 20,
                                pointerEvents: 'none',
                            }}
                        >
                            {/* Panel content */}
                            {!leftPanelCollapsed && (
                                <Box sx={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column', pointerEvents: 'all' }}>
                                    <SideMenu
                                        dashboard={dashboard}
                                        addWidget={addWidget}
                                        dashboardWidgets={dashboardWidgets}
                                        onSelectItem={selectDashboardElement}
                                        selectedItemId={selectedGridItemId}
                                    />
                                </Box>
                            )}
                            {/* Resize handle amb el botó de contreure/expandir centrat */}
                            <Box
                                data-testid="left-panel-resize-handle"
                                onMouseDown={!leftPanelCollapsed ? handleLeftResizeMouseDown : undefined}
                                sx={{
                                    width: leftPanelCollapsed ? '40px' : '10px',
                                    flexShrink: 0,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    cursor: leftPanelCollapsed ? 'pointer' : 'ew-resize',
                                    backgroundColor: 'divider',
                                    borderRight: '1px solid',
                                    borderColor: 'divider',
                                    pointerEvents: 'all',
                                    ...(!leftPanelCollapsed && {
                                        '&:hover': { backgroundColor: 'primary.main', opacity: 0.6 },
                                    }),
                                }}
                            >
                                <IconButton
                                    size="small"
                                    onClick={() => setLeftPanelCollapsed(c => !c)}
                                    title={leftPanelCollapsed ? 'Expandir panell' : 'Compactar panell'}
                                    sx={leftPanelCollapsed ? {
                                        backgroundColor: 'primary.main',
                                        color: 'primary.contrastText',
                                        boxShadow: 2,
                                        '&:hover': { backgroundColor: 'primary.dark' },
                                    } : {
                                        backgroundColor: 'background.paper',
                                        border: '1px solid',
                                        borderColor: 'divider',
                                    }}
                                >
                                    <Icon sx={{ fontSize: '1rem' }}>
                                        {leftPanelCollapsed ? 'chevron_right' : 'chevron_left'}
                                    </Icon>
                                </IconButton>
                            </Box>
                        </Box>
                        {/* Overlay right side panel (non-scrolling, always in view) */}
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
                            {/* Resize handle amb el botó de contreure/expandir centrat */}
                            <Box
                                data-testid="right-panel-resize-handle"
                                onMouseDown={!panelCollapsed ? handleResizeMouseDown : undefined}
                                sx={{
                                    width: panelCollapsed ? '40px' : '10px',
                                    flexShrink: 0,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    cursor: panelCollapsed ? 'pointer' : 'ew-resize',
                                    backgroundColor: 'divider',
                                    borderLeft: '1px solid',
                                    borderColor: 'divider',
                                    pointerEvents: 'all',
                                    ...(!panelCollapsed && {
                                        '&:hover': { backgroundColor: 'primary.main', opacity: 0.6 },
                                    }),
                                }}
                            >
                                <IconButton
                                    size="small"
                                    onClick={() => setPanelCollapsed(c => !c)}
                                    title={panelCollapsed ? 'Expandir panell' : 'Compactar panell'}
                                    sx={panelCollapsed ? {
                                        backgroundColor: 'primary.main',
                                        color: 'primary.contrastText',
                                        boxShadow: 2,
                                        '&:hover': { backgroundColor: 'primary.dark' },
                                    } : {
                                        backgroundColor: 'background.paper',
                                        border: '1px solid',
                                        borderColor: 'divider',
                                    }}
                                >
                                    <Icon sx={{ fontSize: '1rem' }}>
                                        {panelCollapsed ? 'chevron_left' : 'chevron_right'}
                                    </Icon>
                                </IconButton>
                            </Box>
                            {/* Panel content */}
                            {!panelCollapsed && (
                                <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', pointerEvents: 'all', overflow: 'hidden' }}>
                                    <DashboardEditorSidePanel
                                        dashboard={dashboard}
                                        dashboardId={dashboardId}
                                        selection={editorSelection}
                                        onSelectionChange={setEditorSelection}
                                        onSaved={handleWidgetSaved}
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
                {wizardState.open && (
                    <WidgetCreationWizard
                        key={wizardState.seed}
                        open={wizardState.open}
                        dashboard={dashboard}
                        dashboardId={dashboardId}
                        initialWidgetType={wizardState.initialWidgetType}
                        initialEntornId={wizardState.initialEntornId}
                        initialAplicacio={wizardState.initialAplicacio}
                        onClose={closeWizard}
                        onCreated={forceRefreshDashboardWidgets}
                    />
                )}
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

const SideMenu = ({ dashboard, addWidget, dashboardWidgets, onSelectItem, selectedItemId }:{
    dashboard?: any;
    addWidget: (widgetId: string | number, entornId: string | number, widgetType?: DashboardWidgetType) => void;
    dashboardWidgets: Array<Record<string, unknown>>;
    onSelectItem?: (item: Record<string, unknown>) => void;
    selectedItemId?: string | null;
}) => {
    // const { t } = useTranslation();
    const appEntornFilterApiRef = useFilterApiRef();
    const [springFilter, setSpringFilter] = useState<string>()
    const [entornId, setEntornId] = useState<string>(dashboard?.entorn?.id as string)
    const [simpleWidgets, setSimpleWidgets] = useState<Array<{ id?: string | number; titol?: string }>>()
    const [graficWidgets, setGraficWidgets] = useState<Array<{ id?: string | number; titol?: string }>>()
    const [taulaWidgets, setTaulaWidgets] = useState<Array<{ id?: string | number; titol?: string }>>()

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
        if (apiSimpleIsReady && apiGraficIsReady && apiTaulaIsReady) {
            apiSimpleFind({filter: springFilter, unpaged:true})
                .then((response) => setSimpleWidgets(response.rows))
            apiGraficFind({filter: springFilter, unpaged:true})
                .then((response) => setGraficWidgets(response.rows))
            apiTaulaFind({filter: springFilter, unpaged:true})
                .then((response) => setTaulaWidgets(response.rows))
        }
    }, [springFilter, apiSimpleIsReady, apiGraficIsReady, apiTaulaIsReady]);

    // TODO Extreure a component extern (dins el mateix fitxer)
    const WidgetTreeItem = ({widget, widgetType}:{ widget: { id?: string | number; titol?: string }; widgetType: DashboardWidgetType }) => <TreeItem key={widget?.id} itemId={String(widget?.id)} label={<Box
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
                onClick={() => widget?.id != null && addWidget(widget.id, entornId, widgetType)}
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
                }}
            springFilterBuilder={(data: any) => springFilterBuilder.and(
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

        <Typography variant="caption" sx={{ px: 0.5, fontWeight: 700, color: 'text.secondary', display: 'block' }}>
            Elements disponibles
        </Typography>

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
    </Paper>
}

export default EstadisticaDashboardEdit;
