import * as React from 'react';
import MuiToolbar from '@mui/material/Toolbar';
import {
    BasePage,
    MuiGrid,
    useBaseAppContext,
    useResourceApiService,
    MuiFilter,
    springFilterBuilder,
    FormField,
    useFormContext,
    useMessageDialogButtons,
} from 'reactlib';
import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { DashboardReactGridLayout, GridLayoutItem, useMapDashboardItems } from '../components/estadistiques/DashboardReactGridLayout.tsx';
import { isEqual } from 'lodash';
import {
    Alert,
    Box,
    Button,
    Dialog,
    Paper,
    Tab,
    Table,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Tabs,
} from '@mui/material';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { useContentDialog } from '../../lib/components/mui/Dialog.tsx';
import TableBody from '@mui/material/TableBody';
import CircularProgress from '@mui/material/CircularProgress';
import { useDashboard, useDashboardWidgets } from '../hooks/dashboardRequests.ts';

const EntornAppFilterContent = () => {
    const { data } = useFormContext();
    return (
        <Grid container spacing={2} sx={{ mt: 2 }}>
            <Grid size={6}>
                <FormField
                    name="app"
                    componentProps={{
                        size: 'small',
                    }}
                />
            </Grid>

            <Grid size={6}>
                <FormField
                    name="entornApp"
                    disabled={data.app?.id == null || undefined}
                    filter={
                        data.app?.id != null
                            ? springFilterBuilder.eq('app.id', data.app?.id)
                            : undefined
                    }
                    componentProps={{
                        size: 'small',
                    }}
                />
            </Grid>
        </Grid>
    );
};

const EntornAppFilter = ({ onDataChange, onSpringFilterChange }) => {
    return (
        <MuiFilter
            resourceName="entornApp"
            code="entornApp_filter"
            springFilterBuilder={(data) => {
                return springFilterBuilder.and(springFilterBuilder.eq('appId', data.app?.id));
            }}
            onSpringFilterChange={onSpringFilterChange}
            onDataChange={onDataChange}
        >
            <EntornAppFilterContent />
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
        flex: 1,
    },
    {
        field: 'descripcio',
        flex: 3,
    },
];

const AddWidgetDialogGrid = ({ resourceName, onAddClick, filter }) => {
    return (
        <MuiGrid
            resourceName={resourceName}
            columns={addWidgetDialogGridColumns}
            toolbarType="upper"
            paginationActive
            readOnly
            filter={filter}
            rowAdditionalActions={[
                {
                    title: 'Afegir',
                    icon: 'add',
                    onClick: onAddClick,
                },
            ]}
            rowActionsColumnProps={{ width: 10 }}
        />
    );
};

const AddWidgetDialog = ({ open, onClose, onAdd }) => {
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };
    const [filterData, setFilterData] = useState<any>(null);
    const [filterString, setFilterString] = useState<string | null>(null);

    const onAddClick = (id) => {
        onAdd(id, filterData.entornApp.id);
    };

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogTitle>Afegir widget</DialogTitle>
            <DialogContent
                style={{
                    width: '600px',
                    height: '500px',
                    display: 'flex',
                    flexDirection: 'column',
                }}
            >
                <EntornAppFilter
                    onDataChange={setFilterData}
                    onSpringFilterChange={setFilterString}
                />
                {filterData?.app && filterData?.entornApp && (
                    <>
                        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                            <Tabs value={tab} onChange={handleChange}>
                                <Tab label={t('page.widget.simple.tab.title')} />
                                <Tab label={t('page.widget.grafic.tab.title')} />
                                <Tab label={t('page.widget.taula.tab.title')} />
                            </Tabs>
                        </Box>
                        {tab === 0 && (
                            <AddWidgetDialogGrid
                                resourceName="estadisticaSimpleWidget"
                                filter={filterString}
                                onAddClick={onAddClick}
                            />
                        )}
                        {tab === 1 && (
                            <AddWidgetDialogGrid
                                resourceName="estadisticaGraficWidget"
                                filter={filterString}
                                onAddClick={onAddClick}
                            />
                        )}
                        {tab === 2 && (
                            <AddWidgetDialogGrid
                                resourceName="estadisticaTaulaWidget"
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

function WidgetsErrorAlert({ errorWidgets }) {
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
    posY: 0,
    width: 3,
    height: 3,
};

const EstadisticaDashboardEdit: React.FC = () => {
    const { id: dashboardId } = useParams();
    const {
        isReady: apiDashboardItemIsReady,
        patch: patchDashboardItem,
        create: createDashboardItem,
    } = useResourceApiService('dashboardItem');
    const { temporalMessageShow } = useBaseAppContext();
    const { dashboard, loading: loadingDashboard } = useDashboard(dashboardId);
    const {
        dashboardWidgets,
        errorDashboardWidgets,
        loadingWidgetPositions,
        forceRefresh: forceRefreshDashboardWidgets,
    } = useDashboardWidgets(dashboardId);
    const [addWidgetDialogOpen, setAddWidgetDialogOpen] = useState(false);

    const openAddWidgetDialog = () => setAddWidgetDialogOpen(true);
    const closeAddWidgetDialog = () => setAddWidgetDialogOpen(false);

    const addWidget = (widgetId: any, entornAppId: any) => {
        createDashboardItem({
            data: {
                dashboard: { id: dashboardId },
                widget: { id: widgetId },
                entornId: entornAppId,
                ...defaultSizeAndPosition,
            },
        })
            .then(async () => {
                temporalMessageShow(null, 'Widget afegit correctament', 'success');
                forceRefreshDashboardWidgets();
                closeAddWidgetDialog();
            })
            .catch((reason) => {
                temporalMessageShow(null, 'Error al afegir el widget', 'error');
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
                console.error(
                    `Failed to find newDashboardItem with id ${oldDashboardItem.id}, update won't propagate.`
                );
            } else if (!isEqual(oldDashboardItem, newDashboardItem)) {
                const patchPromise = patchDashboardItem(oldDashboardItem.id, {
                    data: {
                        posX: newDashboardItem.x,
                        posY: newDashboardItem.y,
                        width: newDashboardItem.w,
                        height: newDashboardItem.h,
                    },
                });
                promises.push(patchPromise);
            }
        });

        // TODO Traducir mensajes
        Promise.all(promises)
            .then(() => {
                temporalMessageShow(null, 'Guardat correctament', 'success');
            })
            .catch((reason) => {
                temporalMessageShow(null, 'Error al guardar', 'error');
                console.error('Save error', reason);
            });
    };

    const loading = loadingDashboard || loadingWidgetPositions;

    return (
        <>
            {loading ? (
                <Box
                    sx={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: 'translate(-50%, -50%)',
                        zIndex: 10,
                    }}
                >
                    <CircularProgress />
                </Box>
            ) : null}
            {dashboard && (
                <BasePage
                    toolbar={
                        <MuiToolbar
                            disableGutters
                            sx={{
                                width: '100%',
                                display: 'flex',
                                px: 2,
                                ml: 0,
                                mr: 0,
                                mt: 0,
                                backgroundColor: (theme) => theme.palette.grey[200],
                            }}
                        >
                            {dashboard.titol}
                            <Button
                                disabled={!apiDashboardItemIsReady}
                                onClick={openAddWidgetDialog}
                            >
                                {/* TODO */}
                                Afegir widget
                            </Button>
                            <Box sx={{ flexGrow: 1 }} />
                            {errorDashboardWidgets?.length ? (
                                <WidgetsErrorAlert errorWidgets={errorDashboardWidgets} />
                            ) : undefined}
                        </MuiToolbar>
                    }
                >
                    {dashboardWidgets && (
                        <DashboardReactGridLayout
                            dashboardId={dashboard.id}
                            dashboardWidgets={dashboardWidgets}
                            gridLayoutItems={mappedDashboardItems}
                            onGridLayoutItemsChange={onGridLayoutItemsChange}
                            editable
                        />
                    )}
                </BasePage>
            )}

            <AddWidgetDialog
                open={addWidgetDialogOpen}
                onClose={closeAddWidgetDialog}
                onAdd={addWidget}
            />
        </>
    );
};

export default EstadisticaDashboardEdit;
