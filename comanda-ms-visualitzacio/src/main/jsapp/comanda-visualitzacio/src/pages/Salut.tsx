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
    MuiDataGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    useBaseAppContext,
    useResourceApiService,
} from 'reactlib';
import SalutToolbar from '../components/SalutToolbar';
import UpdownBarChart from '../components/UpdownBarChart';
import {
    GridRowId,
    GridSlots,
    GridTreeDataGroupingCell,
    useGridApiRef,
} from '@mui/x-data-grid-pro';
import { PieChart } from '@mui/x-charts';
import DataGridNoRowsOverlay from '../../lib/components/mui/datagrid/DataGridNoRowsOverlay';
import { useParams } from 'react-router-dom';
import SalutAppInfo from './SalutAppInfo';
import { ENUM_APP_ESTAT_PREFIX, getColorByStatEnum, SalutEstatEnum, SalutModel } from '../types/salut.model';
import { BaseEntity } from '../types/base-entity.model';
import { ChipColor } from '../util/colorUtil';
import { useTreeData } from '../hooks/treeData';

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
            let salutLastItemsResponse: SalutModel[] | null = null;
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
                    salutLastItemsResponse = (apiResponse as SalutModel[]).map(item => new SalutModel(item));
                    const reportData = {
                        ...newReportParams,
                        entornAppIdList: salutLastItemsResponse.map(
                            ({ entornAppId }) => entornAppId
                        ),
                    };

                    return new Promise((resolve, reject) => {
                        salutApiReport(null, { code: 'estats', data: reportData })
                            .then((response: any) => {
                                // TODO: eliminar 'links' de respuesta
                                estatsResponse = Object.fromEntries(
                                    Object.entries(response[0]).filter(([key]) => key !== BaseEntity.LINKS)
                                );
                                resolve(null);
                            })
                            .catch(reject);
                    });
                })
                .finally(() => {
                    setSalutLastItems(salutLastItemsResponse as SalutModel[]);
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

const UpdownPieChart: React.FC<any> = (props: { salutLastItems: SalutModel[] }) => {
    const { salutLastItems } = props;
    const { t } = useTranslation();

    const upValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.UP).length;
    const warnValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.WARN).length;
    const downValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.DOWN).length;
    const errorValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.ERROR).length;
    const degradedValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.DEGRADED).length;
    const maintenanceValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.MAINTENANCE).length;
    const unknownValue = salutLastItems.filter((salutItem) => salutItem.appEstat === SalutEstatEnum.UNKNOWN).length;

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
                            id: SalutEstatEnum.UP,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UP)} (${upValue})`,
                            value: upValue,
                            color: getColorByStatEnum(SalutEstatEnum.UP),
                        },
                        {
                            id: SalutEstatEnum.WARN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.WARN)} (${warnValue})`,
                            value: warnValue,
                            color: getColorByStatEnum(SalutEstatEnum.WARN),
                        },
                        {
                            id: SalutEstatEnum.DEGRADED,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DEGRADED)} (${degradedValue})`,
                            value: degradedValue,
                            color: getColorByStatEnum(SalutEstatEnum.DEGRADED),
                        },
                        {
                            id: SalutEstatEnum.DOWN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DOWN)} (${downValue})`,
                            value: downValue,
                            color: getColorByStatEnum(SalutEstatEnum.DOWN),
                        },
                        {
                            id: SalutEstatEnum.ERROR,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.ERROR)} (${errorValue})`,
                            value: errorValue,
                            color: getColorByStatEnum(SalutEstatEnum.ERROR),
                        },
                        {
                            id: SalutEstatEnum.MAINTENANCE,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.MAINTENANCE)} (${maintenanceValue})`,
                            value: maintenanceValue,
                            color: getColorByStatEnum(SalutEstatEnum.MAINTENANCE),
                        },
                        {
                            id: SalutEstatEnum.UNKNOWN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UNKNOWN)} (${unknownValue})`,
                            value: unknownValue,
                            color: getColorByStatEnum(SalutEstatEnum.UNKNOWN),
                        },
                    ],
                },
            ]}
        />
    );
};

const ItemStateChip: React.FC<any> = (props: { salutStatEnum: SalutEstatEnum; date?: string }) => {
    const { salutStatEnum, date } = props;
    const { t } = useTranslation();
    return (
        <>
            {salutStatEnum && (
                <Chip sx={{ bgcolor: getColorByStatEnum(salutStatEnum), color: ChipColor.WHITE,
                    "& .MuiChip-label": {
                        fontSize: "0.7rem !important",
                    }}}
                      label={t(ENUM_APP_ESTAT_PREFIX + salutStatEnum)}
                      size="small"
                />
            )}
            {!salutStatEnum && (
                <Chip sx={{ bgcolor: getColorByStatEnum(SalutEstatEnum.UNKNOWN), color: ChipColor.WHITE,
                    "& .MuiChip-label": {
                        fontSize: "0.7rem !important",
                    }}}
                      label={t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UNKNOWN)}
                      size="small"
                />
            )}
            {date && (<><br />
            <Typography variant="caption">{date}</Typography></>)}
        </>
    );
};

const AppDataTable: React.FC<any> = (props: {
    springFilter?: string;
    salutLastItems: SalutModel[];
    onRowExpansionChange: OnRowExpansionChangeFunction;
    defaultRowExpansion: DefaultRowExpansionState;
}) => {
    const { springFilter, salutLastItems } = props;
    const { t } = useTranslation();
    const { getLinkComponent } = useBaseAppContext();
    const gridApiRef = useGridApiRef();
    const [apps, setApps] = React.useState<any[]>();
    const theme = useTheme();

    const { isReady: appApiIsReady, find: appApiFind } = useResourceApiService('app');

    React.useEffect(() => {
        if (appApiIsReady) {
            appApiFind({ unpaged: true, filter: springFilterBuilder.eq('activa', true) })
                .then((response) => setApps(response.rows))
                .catch(() => setApps([]));
        }
    }, [appApiIsReady]);
    const findSalutItem:(id: GridRowId) => SalutModel | null = React.useCallback(
        (id: GridRowId) => {
            const itemFounded = salutLastItems?.find((entry: SalutModel) => entry.entornAppId === id)
            return itemFounded !== undefined ? itemFounded : null;
        },
        [salutLastItems]
    );

    const renderItemStateChip = React.useCallback(
        (id: GridRowId, stateEnum: keyof SalutModel) => {
            const salutItem: SalutModel | null = findSalutItem(id);
            if (salutItem == null) {
                return undefined;
            }
            return (
                <ItemStateChip
                    salutStatEnum={salutItem[stateEnum]}
                />
            );
        },
        [findSalutItem]
    );

    const columns: MuiDataGridColDef[] = React.useMemo(() => {
        return [
            {
                flex: 0.3,
                field: 'estat',
                headerName: t('page.salut.apps.column.estat'),
                minWidth: 100,
                renderCell: ({ row }) => renderItemStateChip(row.id, SalutModel.APP_ESTAT),
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
                renderCell: ({ row }) => renderItemStateChip(row.id, SalutModel.BD_ESTAT),
            },
            {
                flex: 0.3,
                field: 'latencia',
                headerName: t('page.salut.apps.column.latencia'),
                minWidth: 100,
                valueGetter: (_value, row) => {
                    const salutItem: SalutModel | null = findSalutItem(row.id);
                    return salutItem?.appLatencia != null
                        ? salutItem.appLatencia + ' ms'
                        : t('page.salut.nd');
                },
            },
            {
                flex: 0.5,
                field: SalutModel.INTEGRACIONS,
                // headerName: t('page.salut.apps.column.integ'),
                minWidth: 100,
                renderCell: ({ row }) => {
                    const salutItem: SalutModel | null = findSalutItem(row.id);

                    if (!salutItem) {
                        return null;
                    }

                    return (
                        <>
                            <Chip
                                label={salutItem.integracioUpCount}
                                size="small"
                                color="success"
                            />
                            &nbsp;/&nbsp;
                            <Chip
                                label={salutItem.integracioDownCount}
                                size="small"
                                color="error"
                            />
                            &nbsp;/&nbsp;
                            <Chip sx={{ bgcolor: theme.palette.grey[600], color: ChipColor.WHITE }}
                                label={salutItem.integracioDesconegutCount}
                                size="small"
                            />
                        </>
                    );
                },
            },
            {
                flex: 0.5,
                field: SalutModel.SUBSISTEMES,
                // headerName: t('page.salut.apps.column.subsis'),
                minWidth: 100,
                renderCell: ({ id }) => {
                    const salutItem: SalutModel | null = findSalutItem(id);

                    if (!salutItem) {
                        return null;
                    }
                    return (
                        <>
                            <Chip
                                label={salutItem.subsistemaUpCount}
                                size="small"
                                color="success"
                            />
                            &nbsp;/&nbsp;
                            <Chip
                                label={salutItem.subsistemaDownCount}
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
                    const salutItem: SalutModel | null = findSalutItem(id);

                    if (!salutItem) {
                        return null;
                    }
                    return (
                        <>
                            <Chip
                                label={salutItem.missatgeErrorCount}
                                size="small"
                                color="error"
                            />
                            &nbsp;/&nbsp;
                            <Chip
                                label={salutItem.missatgeWarnCount}
                                size="small"
                                color="warning"
                            />
                            &nbsp;/&nbsp;
                            <Chip label={salutItem.missatgeInfoCount} size="small" color={ChipColor.INFO} />
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
    }, [findSalutItem, getLinkComponent, renderItemStateChip, t]);

    const { dataGridProps: treeDataGridProps } = useTreeData(
        (row) => [row.app.id, row.entorn.description],
        t('page.salut.apps.column.group'),
        1, {
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
            }
        });

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
            {...treeDataGridProps}
            hideFooter
            slots={{
                noRowsOverlay: DataGridNoRowsOverlay as GridSlots['noRowsOverlay'],
            }}
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
