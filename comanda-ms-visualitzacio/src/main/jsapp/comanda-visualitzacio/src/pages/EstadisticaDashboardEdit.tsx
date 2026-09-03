import * as React from 'react';
import MuiToolbar from '@mui/material/Toolbar';
import {
    useBaseAppContext,
    useResourceApiService,
    springFilterBuilder,
    useMessageDialogButtons,
    useConfirmDialogButtons,
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
    Tooltip,
    ToggleButton,
    Typography,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import { useContentDialog } from '../../lib/components/mui/Dialog.tsx';
import TableBody from '@mui/material/TableBody';
import { useDashboard, useDashboardFiltres, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { useStoredLargeScreenMode } from '../components/estadistiques/DashboardReactGridLayout.tsx';
import { DASHBOARDS_PATH } from '../AppRoutes.tsx';
import AddIcon from '@mui/icons-material/Add';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import PageTitle from '../components/PageTitle.tsx';
import CenteredCircularProgress from '../components/CenteredCircularProgress.tsx';
import {SimpleTreeView, TreeItem} from "@mui/x-tree-view";
import Divider from "@mui/material/Divider";
import { ThemeProvider, useTheme } from '@mui/material/styles';
import { useEntornCodi } from '../components/estadistiques/dashboardPlantillaHook.ts';
import IOSSwitch from '../components/IOSSwitch.tsx';
import { darkTheme, lightTheme } from '../theme.ts';

type WidgetsErrorAlertProps = {
    errorWidgets: Array<{
        errorMsg: string;
    }>;
};

function WidgetsErrorAlert({ errorWidgets }: WidgetsErrorAlertProps) {
    const { t } = useTranslation();
    const buttons = useMessageDialogButtons();
    const [showDialog, dialog] = useContentDialog(buttons);

    const openDialog = () => {
        showDialog(
            null,
            <TableContainer component={Paper} sx={{ mt: 3, }} >
                <Table sx={{ width: 500 }} aria-label="simple table">
                    <TableHead>
                        <TableRow>
                            <TableCell>{t($ => $.page.dashboards.editor.errorAlert.errorsHeader)}</TableCell>
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
                        {t($ => $.page.dashboards.editor.errorAlert.viewButton)}
                    </Button>
                }
            >
                {t($ => $.page.dashboards.editor.errorAlert.message)}
            </Alert>
        </>
    );
}

const defaultSizeAndPosition = {
    width: 3,
    height: 3,
};

const LARGE_SCREEN_MODE_STORAGE_KEY = 'comanda.dashboardEdit.largeScreenMode';

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
    // Per defecte es mostra el disseny en el mode (clar/fosc) configurat al perfil de l'usuari; l'switch
    // de la capçalera permet commutar-lo només per a la previsualització del disseny, sense afectar el perfil.
    const [designDarkMode, setDesignDarkMode] = useState(() => theme.palette.mode === 'dark');
    const temaFosc = designDarkMode;
    const {
        isReady: apiDashboardItemIsReady,
        patch: patchDashboardItem,
        delete: deleteDashboardItem,
        artifactAction: executeDashboardItemAction,
    } = useResourceApiService('dashboardItem');
    const {
        isReady: apiDashboardTitolIsReady,
        patch: patchDashboardTitol,
        delete: deleteDashboardTitol,
        artifactAction: executeDashboardTitolAction,
    } = useResourceApiService('dashboardTitol');
    const { artifactAction: executeDashboardAction } = useResourceApiService('dashboard');
    const { temporalMessageShow, messageDialogShow, t: tLib, goBack } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const {
        dashboard,
        loading: loadingDashboard,
        exception: dashboardException,
        forceRefresh: forceRefreshDashboard,
    } = useDashboard(dashboardId);
    const { entornCodi: dashboardEntornCodi, loading: loadingEntornCodi } = useEntornCodi(dashboard?.entorn?.id);
    const {
        dashboardWidgets,
        errorDashboardWidgets,
        loadingWidgetPositions,
        forceRefresh: forceRefreshDashboardWidgets,
        refreshWidget,
    } = useDashboardWidgets(dashboardId, temaFosc);
    const {
        dashboardFiltres,
        forceRefresh: forceRefreshDashboardFiltres,
    } = useDashboardFiltres(dashboardId);
    const handleWidgetSaved = (dashboardItemId?: any) => {
        if (dashboardItemId != null) {
            refreshWidget(dashboardItemId);
        } else {
            forceRefreshDashboardWidgets();
        }
    };
    const [largeScreenMode, setLargeScreenMode] = useStoredLargeScreenMode(LARGE_SCREEN_MODE_STORAGE_KEY);
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

    // Previsualització en viu (encara no desada) d'un títol que s'està editant al panell lateral: es
    // reflecteix al canvas mentre s'edita, i el panell la neteja (passant data=null) quan es deselecciona
    // sense desar, fent que el canvas torni a l'últim estat efectivament desat.
    const [liveTitlePreview, setLiveTitlePreview] = useState<{ id: any; data: any } | null>(null);
    const handleLiveTitleDataChange = useCallback((dashboardTitolId: any, data: any) => {
        setLiveTitlePreview(data == null ? null : { id: dashboardTitolId, data });
    }, []);
    const displayedDashboardWidgets = React.useMemo(() => {
        if (!liveTitlePreview || !dashboardWidgets) return dashboardWidgets;
        return dashboardWidgets.map((widget: any) =>
            String(widget.dashboardTitolId) === String(liveTitlePreview.id)
                ? { ...widget, ...liveTitlePreview.data }
                : widget
        );
    }, [dashboardWidgets, liveTitlePreview]);
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

    const addWidget = async (widgetId: string | number, entornId: string | number, widgetType?: DashboardWidgetType) => {
        if (!widgetType) return;
        try {
            const createdItem = await executeDashboardAction(dashboardId, {
                code: 'clone_and_add_widget',
                data: {
                    widgetId,
                    entornId,
                    ...defaultSizeAndPosition,
                },
            });

            temporalMessageShow(null, t($ => $.page.dashboards.action.addWidget.success), 'success');
            forceRefreshDashboardWidgets();

            if (createdItem?.id) {
                setEditorSelection({
                    kind: 'widget',
                    mode: 'edit',
                    widgetType,
                    dashboardItemId: createdItem.id,
                    widgetId: createdItem.widget?.id ?? widgetId,
                });
            }
        } catch (error: any) {
            temporalMessageShow(null, error?.message ?? t($ => $.page.dashboards.action.addWidget.error), 'error');
            console.error('Widget clone add error', error);
        }
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

    const multiSelectedGridItemIds = React.useMemo(() => {
        return editorSelection.kind === 'multi' ? editorSelection.ids : [];
    }, [editorSelection]);

    const selectedFiltreId = React.useMemo(() => {
        if (editorSelection.kind === 'filtre' && editorSelection.mode === 'edit') {
            return String(editorSelection.dashboardFiltreId);
        }
        return null;
    }, [editorSelection]);

    const selectDashboardFiltre = (filtre: { id?: string | number } | null | undefined) => {
        if (!filtre) {
            setEditorSelection({ kind: 'none' });
            return;
        }
        setEditorSelection({ kind: 'filtre', mode: 'edit', dashboardFiltreId: filtre.id });
    };

    const addDashboardFiltre = () => {
        const nextOrdre = (dashboardFiltres ?? []).reduce(
            (max, filtre) => Math.max(max, filtre.ordre ?? 0),
            -1
        ) + 1;
        setEditorSelection({ kind: 'filtre', mode: 'create', nextOrdre });
    };

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

    /**
     * Selecció múltiple (marc de selecció amb el ratolí, vegeu DashboardReactGridLayout). Amb 0 elements es
     * comporta com netejar la selecció i amb exactament 1 com una selecció normal (mostra les seves
     * propietats); només amb 2 o més es mostra com a selecció múltiple (sense panell de propietats).
     */
    const selectDashboardElements = (entities: Array<{ tipus?: string; id?: string | number; dashboardTitolId?: string | number; dashboardItemId?: string | number; widgetId?: string | number }>) => {
        if (!entities || entities.length === 0) {
            setEditorSelection({ kind: 'none' });
            return;
        }
        if (entities.length === 1) {
            selectDashboardElement(entities[0]);
            return;
        }
        const ids = entities.map((entity) => String(entity.dashboardItemId ?? entity.dashboardTitolId ?? entity.id));
        setEditorSelection({ kind: 'multi', ids });
    };

    const handleDeleteItem = (entity: any) => {
        if (!entity) return;
        const isTitol = entity.tipus === 'TITOL';
        const entityId = isTitol ? (entity.dashboardTitolId ?? entity.id) : (entity.dashboardItemId ?? entity.id);
        messageDialogShow(
            t($ => $.page.dashboards.editor.deleteItem.title),
            t($ => $.page.dashboards.editor.deleteItem.confirm),
            confirmDialogButtons,
            { maxWidth: 'sm', fullWidth: true }
        ).then((value: any) => {
            if (!value) return;
            const deletePromise = isTitol ? deleteDashboardTitol(entityId) : deleteDashboardItem(entityId);
            deletePromise
                .then(() => {
                    temporalMessageShow(null, t($ => $.page.dashboards.editor.deleteItem.success), 'success');
                    if (
                        (isTitol && editorSelection.kind === 'title' && editorSelection.mode === 'edit' && editorSelection.dashboardTitolId === entityId) ||
                        (!isTitol && editorSelection.kind === 'widget' && editorSelection.mode === 'edit' && editorSelection.dashboardItemId === entityId)
                    ) {
                        setEditorSelection({ kind: 'none' });
                    }
                    forceRefreshDashboardWidgets();
                })
                .catch((reason: any) => {
                    temporalMessageShow(null, reason?.message ?? t($ => $.page.dashboards.editor.deleteItem.error), 'error');
                    console.error('Widget delete error', reason);
                });
        });
    };

    const handleDuplicateItem = async (entity: any) => {
        if (!entity) return;
        try {
            if (entity.tipus === 'TITOL') {
                const titolId = entity.dashboardTitolId ?? entity.id;
                await executeDashboardTitolAction(titolId, {
                    code: 'duplicate',
                });
            } else if (entity.tipus === 'SIMPLE' || entity.tipus === 'GRAFIC' || entity.tipus === 'TAULA') {
                const dashboardItemId = entity.dashboardItemId ?? entity.id;
                await executeDashboardItemAction(dashboardItemId, {
                    code: 'duplicate',
                });
            } else {
                return;
            }
            temporalMessageShow(null, t($ => $.page.dashboards.editor.duplicateItem.success), 'success');
            forceRefreshDashboardWidgets();
        } catch (error: any) {
            temporalMessageShow(null, error?.message ?? t($ => $.page.dashboards.editor.duplicateItem.error), 'error');
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
                // Quan es mou un grup (selecció múltiple), react-grid-layout només reflecteix internament
                // la posició de l'element realment arrossegat: la resta s'han mogut igualment (i s'acaben de
                // desar aquí a sobre), però el canvas no ho mostra fins que `gridLayoutItems` es refresca amb
                // dades noves. En un moviment/redimensionament normal (com a màxim 1 item afectat) no cal, ja
                // que react-grid-layout ja mostra l'element arrossegat/redimensionat a la posició correcta.
                if (promises.length > 1) {
                    forceRefreshDashboardWidgets();
                }
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
        // Tota la pantalla de disseny (no només els colors dels widgets) s'ha de mostrar amb el tema
        // (clar/fosc) seleccionat a l'switch de la capçalera, independentment del tema real del perfil.
        <ThemeProvider theme={designDarkMode ? darkTheme : lightTheme}>
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
                    data-testid="dashboard-editor-toolbar"
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
                    {errorDashboardWidgets?.length ? (
                        <Box><WidgetsErrorAlert errorWidgets={errorDashboardWidgets} /></Box>
                    ) : undefined}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Tooltip title={t($ => $.page.dashboards.view.largeScreenModeFit)}>
                            <ToggleButton
                                value="fit"
                                size="small"
                                selected={largeScreenMode === 'fit'}
                                color="primary"
                                onChange={() => setLargeScreenMode(largeScreenMode === 'fit' ? 'centered' : 'fit')}
                                aria-label={t($ => $.page.dashboards.view.largeScreenModeFit)}
                                sx={{ height: '32px' }}
                            >
                                <Icon fontSize="small">aspect_ratio</Icon>
                            </ToggleButton>
                        </Tooltip>
                        <Icon fontSize="small">light_mode</Icon>
                        <IOSSwitch
                            checked={designDarkMode}
                            onChange={(_event, checked) => setDesignDarkMode(checked)}
                            slotProps={{ input: { 'aria-label': t($ => $.page.dashboards.editor.darkModeToggle) } }}
                        />
                        <Icon fontSize="small">dark_mode</Icon>
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
                                    dashboardWidgets={displayedDashboardWidgets}
                                    gridLayoutItems={mappedDashboardItems}
                                    onGridLayoutItemsChange={onGridLayoutItemsChange}
                                    onSelectItem={selectDashboardElement}
                                    onSelectItems={selectDashboardElements}
                                    onDeleteItem={handleDeleteItem}
                                    onDuplicateItem={handleDuplicateItem}
                                    onClearSelection={() => setEditorSelection({ kind: 'none' })}
                                    selectedItemId={selectedGridItemId}
                                    multiSelectedItemIds={multiSelectedGridItemIds}
                                    dashboardEntornCodi={dashboardEntornCodi}
                                    backgroundColor={designDarkMode ? dashboard.colorFonsFosc : dashboard.colorFonsClar}
                                    largeScreenMode={largeScreenMode}
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
                                        dashboardFiltres={dashboardFiltres}
                                        onSelectFiltre={selectDashboardFiltre}
                                        onAddFiltre={addDashboardFiltre}
                                        selectedFiltreId={selectedFiltreId}
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
                                    title={leftPanelCollapsed ? t($ => $.page.dashboards.editor.expandPanel) : t($ => $.page.dashboards.editor.collapsePanel)}
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
                                    title={panelCollapsed ? t($ => $.page.dashboards.editor.expandPanel) : t($ => $.page.dashboards.editor.collapsePanel)}
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
                                        dashboardFiltres={dashboardFiltres}
                                        onLiveTitleDataChange={handleLiveTitleDataChange}
                                        onSaved={(dashboardItemId?: any) => {
                                            if (editorSelection.kind === 'filtre') {
                                                forceRefreshDashboardFiltres();
                                            } else if (editorSelection.kind === 'none') {
                                                // Configuració del propi dashboard (aplicació/entorn/colors de
                                                // fons): cal refrescar-lo perquè els canvis (p.ex. el color de
                                                // fons del canvas) s'apliquin sense haver de recarregar la pàgina.
                                                forceRefreshDashboard();
                                            } else {
                                                handleWidgetSaved(dashboardItemId);
                                            }
                                        }}
                                        onDeleted={() => {
                                            const wasFiltre = editorSelection.kind === 'filtre';
                                            setEditorSelection({ kind: 'none' });
                                            if (wasFiltre) {
                                                forceRefreshDashboardFiltres();
                                            } else {
                                                forceRefreshDashboardWidgets();
                                            }
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
        </ThemeProvider>
    );
};

const TIPUS_ICON: Record<string, string> = {
    SIMPLE: 'description',
    GRAFIC: 'bar_chart_4_bars',
    TAULA: 'table',
    TITOL: 'title',
};

const SideMenu = ({
    dashboard,
    addWidget,
    dashboardWidgets,
    onSelectItem,
    selectedItemId,
    dashboardFiltres,
    onSelectFiltre,
    onAddFiltre,
    selectedFiltreId,
}:{
    dashboard?: any;
    addWidget: (widgetId: string | number, entornId: string | number, widgetType?: DashboardWidgetType) => void;
    dashboardWidgets: Array<Record<string, unknown>>;
    onSelectItem?: (item: Record<string, unknown>) => void;
    selectedItemId?: string | null;
    /** Filtres de capçalera configurats al dashboard (vegeu useDashboardFiltres a dashboardRequests.ts) */
    dashboardFiltres?: Array<{ id?: string | number; tipus?: string; titol?: string; dimensioCodi?: string }>;
    onSelectFiltre?: (filtre: { id?: string | number } | null) => void;
    onAddFiltre?: () => void;
    selectedFiltreId?: string | null;
}) => {
    const { t } = useTranslation();
    // L'aplicació i l'entorn del dashboard es configuren al panell de propietats (quan no hi ha cap
    // element seleccionat), no aquí: aquest menú només els usa per filtrar els widgets disponibles.
    const entornId = dashboard?.entorn?.id as string | undefined;
    const springFilter = dashboard?.aplicacio?.id != null
        ? springFilterBuilder.eq('appId', dashboard.aplicacio.id)
        : undefined;
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
                aria-label={t($ => $.page.dashboards.editor.addWidgetAria, { titol: widget?.titol })}
                onClick={() => widget?.id != null && addWidget(widget.id, entornId, widgetType)}
            >
                <Icon sx={{ fontSize: '0.875rem' }}>add</Icon>
            </IconButton>
        }
    </Box>} />

    return <Paper elevation={1} sx={{ p: 1, height: '100%', display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ flexShrink: 0 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', px: 0.5 }}>
                <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary' }}>
                    {t($ => $.page.dashboards.sideMenu.filtresTitle)}
                </Typography>
                <IconButton
                    size="small"
                    aria-label={t($ => $.page.dashboards.sideMenu.addFiltre)}
                    title={t($ => $.page.dashboards.sideMenu.addFiltre)}
                    onClick={onAddFiltre}
                    disabled={dashboard?.id == null}
                >
                    <Icon sx={{ fontSize: '1rem' }}>add</Icon>
                </IconButton>
            </Box>
            <Box sx={{ maxHeight: 160, overflow: 'auto', mb: 0.5 }}>
                {!dashboardFiltres?.length ? (
                    <Typography variant="body2" sx={{ px: 1, py: 0.25, color: 'text.secondary', fontStyle: 'italic' }}>
                        {t($ => $.page.dashboards.sideMenu.noFiltres)}
                    </Typography>
                ) : (
                    dashboardFiltres.map((filtre) => {
                        const filtreId = String(filtre.id);
                        const isSelected = selectedFiltreId === filtreId;
                        return (
                            <Box
                                key={filtreId}
                                onClick={() => onSelectFiltre?.(filtre)}
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
                                <Icon sx={{ fontSize: '0.875rem' }}>{filtre.tipus === 'PERIODE' ? 'event' : 'filter_alt'}</Icon>
                                <Box component="span" sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {filtre.titol || (filtre.tipus === 'PERIODE' ? t($ => $.page.dashboards.sideMenu.periode) : filtre.dimensioCodi)}
                                </Box>
                            </Box>
                        );
                    })
                )}
            </Box>

            <Divider sx={{ my: 1 }}/>

            <Typography variant="caption" sx={{ px: 0.5, fontWeight: 700, color: 'text.secondary', display: 'block' }}>
                {t($ => $.page.dashboards.editor.availableElements)}
            </Typography>
        </Box>

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
            <TreeItem key={'simple'} itemId={'simple'} label={t($ => $.page.dashboards.editor.types.simple)}>
                {simpleWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget} widgetType="SIMPLE"/>
                )}
            </TreeItem>
            <TreeItem key={'grafic'} itemId={'grafic'} label={t($ => $.page.dashboards.editor.types.grafic)}>
                {graficWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget} widgetType="GRAFIC"/>
                )}
            </TreeItem>
            <TreeItem key={'taula'} itemId={'taula'} label={t($ => $.page.dashboards.editor.types.taula)}>
                {taulaWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget} widgetType="TAULA"/>
                )}
            </TreeItem>
        </SimpleTreeView>

        {dashboardWidgets?.length > 0 && (
            <>
                <Divider sx={{ my: 1 }}/>
                <Typography variant="caption" sx={{ px: 0.5, fontWeight: 700, color: 'text.secondary', display: 'block' }}>
                    {t($ => $.page.dashboards.editor.dashboardElements)}
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
