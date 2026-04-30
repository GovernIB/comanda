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
import {useEffect, useState} from 'react';
import {
    DashboardReactGridLayout,
    GridLayoutItem,
    useMapDashboardItems,
} from '../components/estadistiques/DashboardReactGridLayout.tsx';
import { isEqual } from 'lodash';
import {
    Alert,
    Box,
    Button,
    Dialog,
    ListItemIcon,
    Paper,
    Tab,
    Table,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tabs,
    Typography,
} from '@mui/material';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { useContentDialog } from '../../lib/components/mui/Dialog.tsx';
import TableBody from '@mui/material/TableBody';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';
import { DASHBOARDS_PATH } from '../AppRoutes.tsx';
import AddIcon from '@mui/icons-material/Add';
import { useFormDialog } from '../../lib/components/mui/form/FormDialog.tsx';
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

type EntornAppFilterContentProps = {
    initialData?: {
        app?: any;
        entorn?: any;
    };
};

const EntornAppFilterContent = (props: EntornAppFilterContentProps) => {
    const { initialData } = props;
    const { data } = useFormContext();
    return (
        <Grid container spacing={2} sx={{ mt: 2 }}>
            <Grid size={6}>
                <FormField
                    name="app"
                    componentProps={{ size: 'small' }}
                    readOnly={initialData?.app}
                    disabled={initialData?.app}
                    filter={springFilterBuilder.exists(
                        springFilterBuilder.and(
                            springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id)
                        )
                    )}
                />
            </Grid>
            <Grid size={6}>
                <FormField
                    name="entorn"
                    componentProps={{ size: 'small' }}
                    readOnly={initialData?.entorn}
                    disabled={initialData?.entorn}
                    filter={springFilterBuilder.exists(
                        springFilterBuilder.and(
                            springFilterBuilder.eq('entornAppEntities.app.id', data?.app?.id)
                        )
                    )}
                />
            </Grid>
        </Grid>
    );
};

type EntornAppFilterProps = {
    onDataChange: (data: any) => void;
    onSpringFilterChange: (filter?: string) => void;
    initialData?: any;
};

const EntornAppFilter = ({
    onDataChange,
    onSpringFilterChange,
    initialData,
}: EntornAppFilterProps) => {
    return (
        <MuiFilter
            resourceName="entornApp"
            code="entornApp_filter"
            springFilterBuilder={(data) => {
                return springFilterBuilder.and(springFilterBuilder.eq('appId', data.app?.id));
            }}
            onSpringFilterChange={onSpringFilterChange}
            onDataChange={onDataChange}
            // initialData={initialData} TODO Deberia bastar con settear initialData, pero al hacerlo el componente da un error de link no incializado, debuggear componente lib
            additionalData={initialData}
        >
            <EntornAppFilterContent initialData={initialData} />
        </MuiFilter>
    );
};

const addWidgetDialogGridColumns = [
    {
        field: 'aplicacio',
        flex: 1,
    },
    {
        field: 'titol',
        flex: 2,
    },
];

type AddWidgetDialogGridProps = {
    resourceName: string;
    onAddClick: (id: any) => void;
    filter: string | null;
    title: string;
};

const AddWidgetDialogGrid = ({ resourceName, onAddClick, filter, title }: AddWidgetDialogGridProps) => {
    const { t } = useTranslation();
    return (
        <MuiDataGrid
            resourceName={resourceName}
            title={title}
            columns={addWidgetDialogGridColumns}
            rowHeight={26}
            columnHeaderHeight={30}
            paginationActive
            readOnly
            filter={filter ?? undefined}
            rowAdditionalActions={[
                {
                    label: t($ => $.page.widget.action.add.label),
                    icon: 'add',
                    onClick: onAddClick,
                },
            ]}
            rowActionsColumnProps={{ width: 10 }}
        />
    );
};

type AddWidgetDialogProps = {
    open: boolean;
    onClose: () => void;
    onAdd: (widgetId: any, entornId: any) => void;
    initialData?: any;
};

const AddWidgetDialog: React.FC<AddWidgetDialogProps> = ({ open, onClose, onAdd, initialData }) => {
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };
    const [filterData, setFilterData] = useState<any>(null);
    const [filterString, setFilterString] = useState<string | null>(null);

    const onAddClick = (id: any) => {
        onAdd(id, filterData.entorn.id);
    };

    return (
        <Dialog maxWidth="lg" open={open} onClose={onClose}>
            <DialogTitle>
                {t($ => $.page.dashboards.action.addWidget.title)}
                <IconButton
                    aria-label="close"
                    onClick={onClose}
                    size="small"
                    sx={(theme) => ({
                        position: 'absolute',
                        right: 8,
                        top: 8,
                        color: theme.palette.grey[500],
                    })}
                >
                    <Icon fontSize="small">close</Icon>
                </IconButton>
            </DialogTitle>
            <DialogContent
                style={{
                    width: '900px',
                    height: '500px',
                    display: 'flex',
                    flexDirection: 'column',
                }}
            >
                <EntornAppFilter
                    onDataChange={setFilterData}
                    onSpringFilterChange={(filter) => setFilterString(filter ?? null)}
                    initialData={initialData}
                />
                {filterData?.app && filterData?.entorn && (
                    <>
                        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                            <Tabs value={tab} onChange={handleChange}>
                                <Tab label={t($ => $.page.widget.simple.tab.title)} />
                                <Tab label={t($ => $.page.widget.grafic.tab.title)} />
                                <Tab label={t($ => $.page.widget.taula.tab.title)} />
                            </Tabs>
                        </Box>
                        {tab === 0 && (
                            <AddWidgetDialogGrid
                                resourceName="estadisticaSimpleWidget"
                                title={t($ => $.page.widget.simple.title)}
                                filter={filterString}
                                onAddClick={onAddClick}
                            />
                        )}
                        {tab === 1 && (
                            <AddWidgetDialogGrid
                                resourceName="estadisticaGraficWidget"
                                title={t($ => $.page.widget.grafic.title)}
                                filter={filterString}
                                onAddClick={onAddClick}
                            />
                        )}
                        {tab === 2 && (
                            <AddWidgetDialogGrid
                                resourceName="estadisticaTaulaWidget"
                                title={t($ => $.page.widget.taula.title)}
                                filter={filterString}
                                onAddClick={onAddClick}
                            />
                        )}
                    </>
                )}{' '}
            </DialogContent>
        </Dialog>
    );
};

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
    } = useDashboardWidgets(dashboardId);
    const [showContentDialog, contentDialogComponent] = useContentDialog();
    const messageDialogButtons = useMessageDialogButtons();
    const [addWidgetDialogOpen, setAddWidgetDialogOpen] = useState(false);
    const navigate = useNavigate();
    const [titolFormDialogShow, titolFormDialogComponent] = useFormDialog('dashboardTitol');
    const openCreateTitolForm = () => {
        titolFormDialogShow(null, {
            title: t($ => $.page.dashboards.action.afegirTitle.title),
            formContent: <AfegirTitolFormContent />,
            additionalData: { dashboard: { id: dashboardId }, ...defaultSizeAndPosition },
            dialogComponentProps: { maxWidth: 'md', fullWidth: true },
        }).then(() => forceRefreshDashboardWidgets());
    };

    const openAddWidgetDialog = () => setAddWidgetDialogOpen(true);
    const closeAddWidgetDialog = () => setAddWidgetDialogOpen(false);

    const addWidget = (widgetId: any, entornId: any) => {
        createDashboardItem({
            data: {
                dashboard: { id: dashboardId },
                widget: { id: widgetId },
                entornId,
                ...defaultSizeAndPosition,
            },
        })
            .then(async () => {
                temporalMessageShow(null, t($ => $.page.dashboards.action.addWidget.success), 'success');
                forceRefreshDashboardWidgets();
                closeAddWidgetDialog();
            })
            .catch((reason) => {
                temporalMessageShow(null, t($ => $.page.dashboards.action.addWidget.error), 'error');
                console.error('Widget add error', reason);
            });
    };

    const mappedDashboardItems = useMapDashboardItems(dashboardWidgets);

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
            {titolFormDialogComponent}
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
                            <MenuItem onClick={openAddWidgetDialog}>
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
                <Grid container sx={{ flex: 1, overflow: 'hidden' }}>
                    <Grid size={2} sx={{ height: '100%' }}>
                        <SideMenu dashboard={dashboard} addWidget={addWidget}/>
                    </Grid>
                    <Grid size={10} sx={{
                        overflow: 'auto',
                        minHeight: 0,
                        maxHeight: '100%'
                    }}>
                        {/* Se espera a tener los datos a mostrar y a todas las APIs que puedan ser llamadas por onGridLayoutItemsChange */}
                        {apiDashboardItemIsReady && apiDashboardTitolIsReady && dashboardWidgets && (
                            <DashboardReactGridLayout
                                dashboardId={dashboard.id}
                                dashboardWidgets={dashboardWidgets}
                                gridLayoutItems={mappedDashboardItems}
                                onGridLayoutItemsChange={onGridLayoutItemsChange}
                                editable
                                refresh={forceRefreshDashboardWidgets}
                            />
                        )}
                    </Grid>
                </Grid>
                <FooterHeightPlaceholder />
            </>)}
            <AddWidgetDialog
                open={addWidgetDialogOpen}
                onClose={closeAddWidgetDialog}
                onAdd={addWidget}
                initialData={{
                    app: dashboard?.aplicacio,
                    entorn: dashboard?.entorn
                }}
            />
        </Box>
    );
};

const SideMenu = ({ dashboard, addWidget }:any) => {
    // const { t } = useTranslation();
    const appEntornFilterApiRef = useFilterApiRef();
    const [springFilter, setSpringFilter] = useState<string>()
    const [entornId, setEntornId] = useState<string>(dashboard?.entorn?.id)
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

    const WidgetTreeItem = ({widget}:any) => <TreeItem key={widget?.id} itemId={widget?.id} label={<Box
        display={'flex'}
        flexDirection={'row'}
        justifyContent={'space-between'}
        alignItems={'center'}
    >
        {widget?.titol}
        {entornId &&
            <IconButton size={'small'} onClick={() => addWidget(widget?.id, entornId)}>
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
            onDataChange={(data) => setEntornId(data?.entorn?.id)}
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
                    <WidgetTreeItem widget={widget}/>
                )}
            </TreeItem>
            <TreeItem key={'grafic'} itemId={'grafic'} label={'Grafic'}>
                {graficWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget}/>
                )}
            </TreeItem>
            <TreeItem key={'taula'} itemId={'taula'} label={'Taula'}>
                {taulaWidgets?.map?.((widget) =>
                    <WidgetTreeItem widget={widget}/>
                )}
            </TreeItem>
        </SimpleTreeView>
        </Box>

        <Box sx={{ mt: 'auto' }}>
            <Divider sx={{ my: 1, alignSelf: 'end' }}/>
        </Box>

        <WidgetMenu addWidget={addWidget} entornId={entornId}/>
    </Paper>
}

const WidgetMenu = ({addWidget, entornId}:any) => {
    const {handleOpen: handleSimpleOpen, dialog: dialogSimple} = useSimpleWidgetFormDialog();
    const {handleOpen: handleGraficOpen, dialog: dialogGrafic} = useGraficWidgetFormDialog();
    const {handleOpen: handleTaulaOpen, dialog: dialogTaula} = useTaulaWidgetFormDialog();

    return <>
        <MyButtonMenu
            title={'Nou Widget'}
            disabled={!entornId}
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
                    onClick: () => {
                        handleSimpleOpen()
                            ?.then((response:any) => addWidget(response.id, entornId))
                    },
                },
                {
                    icon: 'bar_chart_4_bars',
                    title: 'Gràfic',
                    onClick: () => {
                        handleGraficOpen()
                            ?.then((response:any) => addWidget(response.id, entornId))
                    },
                },
                {
                    icon: 'table',
                    title: 'Taula',
                    onClick: () => {
                        handleTaulaOpen()
                            ?.then((response:any) => addWidget(response.id, entornId))
                    },
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
        {dialogSimple}
        {dialogGrafic}
        {dialogTaula}
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
