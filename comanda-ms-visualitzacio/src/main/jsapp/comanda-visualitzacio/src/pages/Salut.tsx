import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import { useTheme } from '@mui/material/styles';
import {
    BasePage,
    useResourceApiService,
    useBaseAppContext,
    MuiDataGridColDef,
    springFilterBuilder,
    MuiDataGrid,
} from 'reactlib';
import SalutToolbar from '../components/SalutToolbar';
import UpdownBarChart from '../components/UpdownBarChart';
import {
    GridSlots,
    GridTreeDataGroupingCell,
    GridColumnHeaderTitle,
    useGridApiRef,
} from '@mui/x-data-grid-pro';
import { PieChart } from '@mui/x-charts';
import DataGridNoRowsOverlay from '../../lib/components/mui/datagrid/DataGridNoRowsOverlay';
import { Icon, IconButton } from '@mui/material';
import { useState } from 'react';
import { GridGroupingColDefOverride } from '@mui/x-data-grid-pro';
import { useParams } from 'react-router-dom';
import SalutAppInfo from './SalutAppInfo';

type OnRowExpansionChangeFunction = (id: string | number, expanded: boolean) => void;

interface DefaultRowExpansionState {
    [id: string | number]: boolean;
}

const useAppData = () => {
    const { isReady: appEntornApiIsReady, artifactAction: appEntornApiAction } =
        useResourceApiService('entornApp');
    const { isReady: salutApiIsReady, artifactReport: salutApiReport } =
        useResourceApiService('salut');
    const [loading, setLoading] = React.useState<boolean>(true);
    const [estats, setEstats] = React.useState<Record<string, any>>({});
    const [salutLastItems, setSalutLastItems] = React.useState<any[]>();
    const [reportParams, setReportParams] = React.useState<any>();
    const [springFilter, setSpringFilter] = React.useState<string>('');
    const refresh = (
        dataInici: string,
        dataFi: string,
        agrupacio: string,
        actionExec?: boolean,
        springFilter?: string
    ) => {
        if (appEntornApiIsReady && salutApiIsReady) {
            setLoading(true);
            let salutLastItemsResponse: any[] | null = null;
            let estatsResponse: Record<string, any> | null = null;
            const newReportParams = {
                dataInici,
                dataFi,
                agrupacio,
            };
            new Promise((resolve, reject) => {
                if (actionExec) {
                    appEntornApiAction(null, { code: 'refresh' }).then(resolve).catch(reject);
                } else {
                    resolve(null);
                }
            })
                .then(() => {
                    return salutApiReport(null, { code: 'salut_last', data: springFilter });
                })
                .then((apiResponse) => {
                    salutLastItemsResponse = apiResponse as any[];
                    const reportData = {
                        ...newReportParams,
                        entornAppIdList: salutLastItemsResponse.map(
                            ({ entornAppId }) => entornAppId
                        ),
                    };

                    return new Promise((resolve, reject) => {
                        salutApiReport(null, { code: 'estats', data: reportData })
                            .then((ii: any) => {
                                // TODO: eliminar 'links' de respuesta
                                estatsResponse = Object.fromEntries(
                                    Object.entries(ii[0]).filter(([key]) => key !== 'links')
                                );
                                resolve(null);
                            })
                            .catch(reject);
                    });
                })
                .finally(() => {
                    setSalutLastItems(salutLastItemsResponse as any[]);
                    setEstats(estatsResponse as Record<string, any>);
                    setLoading(false);
                    if (springFilter != null) setSpringFilter(springFilter);
                    setReportParams(newReportParams);
                });
        }
    };
    return {
        ready: appEntornApiIsReady && salutApiIsReady,
        loading,
        refresh,
        springFilter,
        salutLastItems,
        estats,
        reportParams,
    };
};

const UpdownPieChart: React.FC<any> = (props: { salutLastItems: any[] }) => {
    const { salutLastItems } = props;
    const { t } = useTranslation();
    const theme = useTheme();

    const upValue = salutLastItems.filter((salutItem) => salutItem.appEstat === 'UP').length;
    const warnValue = salutLastItems.filter((salutItem) => salutItem.appEstat === 'WARN').length;
    const downValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === 'DOWN' || salutItem.appEstat === 'ERROR'
    ).length;
    const degradedValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === 'DEGRADED'
    ).length;
    const maintenanceValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === 'MAINTENANCE'
    ).length;
    const unknownValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === 'UNKNOWN'
    ).length;

    return (
        <PieChart
            series={[
                {
                    // innerRadius: 30,
                    // outerRadius: 100,
                    // paddingAngle: 1,
                    highlightScope: { fade: 'global', highlight: 'item' },
                    highlighted: {
                        additionalRadius: 1,
                    },
                    cornerRadius: 5,
                    data: [
                        {
                            id: 'UP',
                            label: `${t('enum.appEstat.UP')} (${upValue})`,
                            value: upValue,
                            color: theme.palette.success.main,
                        },
                        {
                            id: 'WARN',
                            label: `${t('enum.appEstat.WARN')} (${warnValue})`,
                            value: warnValue,
                            color: theme.palette.warning.light,
                        },
                        {
                            id: 'DOWN',
                            label: `${t('enum.appEstat.DOWN')} (${downValue})`,
                            value: downValue,
                            color: theme.palette.error.main,
                        },
                        {
                            id: 'DEGRADED',
                            label: `${t('enum.appEstat.DEGRADED')} (${degradedValue})`,
                            value: degradedValue,
                            color: theme.palette.warning.dark,
                        },
                        {
                            id: 'MAINTENANCE',
                            label: `${t('enum.appEstat.MAINTENANCE')} (${maintenanceValue})`,
                            value: maintenanceValue,
                            color: theme.palette.primary.main,
                        },
                        {
                            id: 'UNKNOWN',
                            label: `${t('enum.appEstat.UNKNOWN')} (${unknownValue})`,
                            value: unknownValue,
                            color: theme.palette.grey[600],
                        },
                    ],
                },
            ]}
        />
    );
};

const ItemStateChip: React.FC<any> = (props: { up: boolean; date?: string }) => {
    const { up, date } = props;
    const { t } = useTranslation();
    return (
        <>
            {up ? (
                <Chip label={t('enum.appEstat.UP')} size="small" color="success" />
            ) : (
                <Chip label={t('enum.appEstat.DOWN')} size="small" color="error" />
            )}
            {date && (<><br />
            <Typography variant="caption">{date}</Typography></>)}
        </>
    );
};

const AppDataTable: React.FC<any> = (props: { springFilter?: string; salutLastItems: any[]; }) => {
    const { springFilter, salutLastItems } = props;
    const { t } = useTranslation();
    const { getLinkComponent } = useBaseAppContext();
    const gridApiRef = useGridApiRef();
    const [apps, setApps] = React.useState<any[]>();
    const [expandAll, setExpandAll] = useState<boolean>(true);
    const [expansionState, setExpansionState] = React.useState<DefaultRowExpansionState>({});
    const theme = useTheme();

    const { isReady: appApiIsReady, find: appApiFind } = useResourceApiService('app');

    const onRowExpansionChange: OnRowExpansionChangeFunction = (id, expanded) => {
        setExpansionState((prevState) => ({
            ...prevState,
            [id]: expanded,
        }));
    };

    React.useEffect(() => {
        if (appApiIsReady) {
            appApiFind({ unpaged: true, filter: springFilterBuilder.eq('activa', true) })
                .then((response) => setApps(response.rows))
                .catch(() => setApps([]));
        }
    }, [appApiIsReady]);

    React.useEffect(() => {
        if (gridApiRef.current) {
            gridApiRef.current.subscribeEvent?.('rowExpansionChange', (node) => {
                onRowExpansionChange(node.id, !!node.childrenExpanded);
            });
        }
    }, [gridApiRef, gridApiRef.current, onRowExpansionChange]);
    const findUpdownItem = React.useCallback(
        (id: any) => {
            return salutLastItems?.find((entry) => entry.entornAppId === id);
        },
        [salutLastItems]
    );

    const renderItemStateChip = React.useCallback(
        (id: any, upField: string) => {
            const updownItem = findUpdownItem(id);
            if (updownItem == null) {
                return undefined;
            }
            return (
                <ItemStateChip
                    up={updownItem[upField]}
                />
            );
        },
        [findUpdownItem]
    );

    const columns: MuiDataGridColDef[] = React.useMemo(() => {
        return [
            {
                flex: 0.3,
                field: 'estat',
                headerName: t('page.salut.apps.column.estat'),
                minWidth: 100,
                renderCell: ({ id }) => renderItemStateChip(id, 'appUp'),
            },
            {
                flex: 0.3,
                field: 'infoData',
                description: t('page.salut.apps.column.infoData'),
                minWidth: 150,
            },
            {
                flex: 0.3,
                field: 'versio',
                // headerName: t('page.salut.apps.column.versio'),
                minWidth: 100,
            },
            {
                flex: 0.3,
                field: 'revisioSimplificat',
                // headerName: t('page.salut.apps.column.revisio'),
                minWidth: 100,
            },
            {
                flex: 0.3,
                field: 'bd',
                headerName: t('page.salut.apps.column.bd'),
                minWidth: 100,
                renderCell: ({ id }) => renderItemStateChip(id, 'bdUp'),
            },
            {
                flex: 0.3,
                field: 'latencia',
                headerName: t('page.salut.apps.column.latencia'),
                minWidth: 100,
                valueGetter: (_value, row) => {
                    const updownItem = findUpdownItem(row.id);
                    return updownItem?.appLatencia != null
                        ? updownItem.appLatencia + ' ms'
                        : t('page.salut.nd');
                },
            },
            {
                flex: 0.5,
                field: 'integracions',
                // headerName: t('page.salut.apps.column.integ'),
                minWidth: 100,
                renderCell: ({ id }) => {
                    const updownItem = findUpdownItem(id);

                    if (!updownItem) {
                        return null;
                    }

                    return (
                        <>
                            <Chip
                                label={updownItem.integracioUpCount}
                                size="small"
                                color="success"
                            />
                            &nbsp;/&nbsp;
                            <Chip
                                label={updownItem.integracioDownCount}
                                size="small"
                                color="error"
                            />
                            &nbsp;/&nbsp;
                            <Chip sx={{ bgcolor: theme.palette.grey[600], color: 'white' }}
                                label={updownItem.integracioDesconegutCount}
                                size="small"
                            />
                        </>
                    );
                },
            },
            {
                flex: 0.5,
                field: 'subsistemes',
                // headerName: t('page.salut.apps.column.subsis'),
                minWidth: 100,
                renderCell: ({ id }) => {
                    const updownItem = findUpdownItem(id);

                    if (!updownItem) {
                        return null;
                    }
                    return (
                        <>
                            <Chip
                                label={updownItem.subsistemaUpCount}
                                size="small"
                                color="success"
                            />
                            &nbsp;/&nbsp;
                            <Chip
                                label={updownItem.subsistemaDownCount}
                                size="small"
                                color="error"
                            />
                        </>
                    );
                },
            },
            {
                flex: 0.5,
                field: 'msgs',
                headerName: t('page.salut.apps.column.msgs'),
                minWidth: 150,
                renderCell: ({ id }) => {
                    const updownItem = findUpdownItem(id);

                    if (!updownItem) {
                        return null;
                    }
                    return (
                        <>
                            <Chip
                                label={updownItem.missatgeErrorCount}
                                size="small"
                                color="error"
                            />
                            &nbsp;/&nbsp;
                            <Chip
                                label={updownItem.missatgeWarnCount}
                                size="small"
                                color="warning"
                            />
                            &nbsp;/&nbsp;
                            <Chip label={updownItem.missatgeInfoCount} size="small" color="info" />
                        </>
                    );
                },
            },
            {
                field: 'detalls',
                headerName: '',
                minWidth: 100,
                renderCell: (params) => params.rowNode.type !== 'group' && <Button
                    variant="contained"
                    size="small"
                    component={getLinkComponent()}
                    to={'appinfo/' + params.id}>
                    {t('page.salut.apps.detalls')}
                </Button>,
            },
        ];
    }, [findUpdownItem, getLinkComponent, renderItemStateChip, t]);

    const groupingColDef: GridGroupingColDefOverride = React.useMemo(() => ({
        flex: 1,
        headerName: t('page.salut.apps.column.group'),
        renderHeader: (params: any) => (<Box
            sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            width: "100%",
            flex: 1,
            }}>
            <GridColumnHeaderTitle label={params?.colDef?.headerName} columnWidth={params.colDef.computedWidth} />
            <Box sx={{ display: 'flex', gap: 1, ml: 1 }}>
                <IconButton
                    size="small"
                    onClick={() => {
                        setExpandAll(true);
                        if (expansionState != null) {
                            Object.keys(expansionState).map((id) => {
                                onRowExpansionChange(id, true);
                            });
                        }
                    }}>
                    <Icon fontSize="small">unfold_more</Icon>
                </IconButton>
                <IconButton
                    size="small"
                    onClick={() => {
                        setExpandAll(false);
                        if (expansionState != null) {
                            Object.keys(expansionState).map((id) => {
                                onRowExpansionChange(id, false);
                            });
                        }
                    }}>
                    <Icon fontSize="small">unfold_less</Icon>
                </IconButton>
            </Box>
        </Box>),
        renderCell: (params: any) => {
            const app = apps?.find((app) => app.id === params.formattedValue);
            if (typeof params.id === 'number' || app == null) {
                return <GridTreeDataGroupingCell {...params} />;
            }
            return (
                <GridTreeDataGroupingCell
                    {...params}
                    formattedValue={
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                            }}>
                            {app.logo && <img
                                src={'data:image/png;base64,' + app.logo}
                                alt="Logo de l'aplicació"
                                style={{ height: '48px' }}/>}
                            {app.nom}
                        </Box>
                    }/>
            );
        },
    }), [columns.length, expansionState, onRowExpansionChange, apps]);

    return (
        <MuiDataGrid
            titleDisabled
            resourceName={'entornApp'}
            datagridApiRef={gridApiRef}
            columns={columns}
            filter={springFilterBuilder.and(
                springFilterBuilder.eq('activa', true),
                springFilterBuilder.eq('app.activa', true),
                springFilter
            )}
            toolbarHideQuickFilter
            toolbarHideRefresh
            readOnly
            treeData
            getTreeDataPath={(row) => [row.app.id, row.entorn.description]}
            groupingColDef={groupingColDef}
            isGroupExpandedByDefault={(node) => expansionState[node.id] != null ? expansionState[node.id] : expandAll}
            hideFooter
            slots={{
                noRowsOverlay: DataGridNoRowsOverlay as GridSlots['noRowsOverlay'],
            }}
            autoHeight
        />
    );
};

const Salut: React.FC = () => {
    const { t } = useTranslation();
    const {
        ready,
        loading,
        refresh: appDataRefresh,
        salutLastItems,
        estats,
        reportParams,
        springFilter,
    } = useAppData();
    const { id } = useParams();
    const dataLoaded = ready && loading != null && !loading;
    const toolbar = (
        <SalutToolbar
            title={t('page.salut.title')}
            ready={ready}
            onRefresh={appDataRefresh}
            appDataLoading={!dataLoaded}
        />
    );

    const isAppInfoRouteActive = id != null;

    if (isAppInfoRouteActive) return <SalutAppInfo />;

    return (
        <BasePage toolbar={toolbar}>
            {salutLastItems == null || estats == null || reportParams == null ? (
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'center',
                        alignItems: 'center',
                        minHeight: 'calc(100vh - 80px)',
                    }}
                >
                    <CircularProgress size={100} />
                </Box>
            ) : (
                <Grid container spacing={2}>
                    <Grid size={{xs: 12, sm: 3}} sx={{ height: '200px' }}>
                        <Box
                            sx={{
                                height: '100%',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                            }}
                        >
                            <UpdownPieChart salutLastItems={salutLastItems} />
                        </Box>
                    </Grid>
                    <Grid size={{xs: 12, sm: 9}} sx={{ height: '200px' }}>
                        <UpdownBarChart
                            dataInici={reportParams.dataInici}
                            agrupacio={reportParams.agrupacio}
                            estats={estats}
                        />
                    </Grid>
                    <Grid size={12}>
                        <AppDataTable
                            springFilter={springFilter}
                            salutLastItems={salutLastItems}
                        />
                    </Grid>
                </Grid>
            )}
        </BasePage>
    );
};

export default Salut;
