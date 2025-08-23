import * as React from 'react';
import {useTranslation} from 'react-i18next';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
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
    useGridApiRef,
} from '@mui/x-data-grid-pro';
import {PieChart} from '@mui/x-charts';
import DataGridNoRowsOverlay from '../../lib/components/mui/datagrid/DataGridNoRowsOverlay';
import {useParams} from 'react-router-dom';
import SalutAppInfo from './SalutAppInfo';
import {
    ENUM_APP_ESTAT_PREFIX, getColorByIntegracio, getColorByMissatge,
    getColorByStatEnum, getColorBySubsistema,
    getMaterialIconByState,
    SalutEstatEnum,
    SalutModel,
    TITLE
} from "../types/salut.model.tsx";
import {BaseEntity} from "../types/base-entity.model.ts";
import {ChipColor} from "../util/colorUtil.ts";
import {SalutChipTooltip, SalutGenericTooltip} from "../components/SalutChipTooltip.tsx";
import {useTreeData, useTreeDataEntornAppRenderCell} from "../hooks/treeData.tsx";

type OnRowExpansionChangeFunction = (id: string | number, expanded: boolean) => void;

interface DefaultRowExpansionState {
    [id: string | number]: boolean;
}

const useAppData = () => {
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
        springFilter?: string
    ) => {
        if (salutApiIsReady) {
            setLoading(true);
            let salutLastItemsResponse: SalutModel[] | null = null;
            let estatsResponse: Record<string, any> | null = null;
            const newReportParams = {
                dataInici,
                dataFi,
                agrupacio,
            };
            salutApiReport(null, { code: 'salut_last', data: springFilter })
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
        ready: salutApiIsReady,
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
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UP + TITLE)} (${upValue})`,
                            value: upValue,
                            color: getColorByStatEnum(SalutEstatEnum.UP),
                        },
                        {
                            id: SalutEstatEnum.WARN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.WARN + TITLE)} (${warnValue})`,
                            value: warnValue,
                            color: getColorByStatEnum(SalutEstatEnum.WARN),
                        },
                        {
                            id: SalutEstatEnum.DEGRADED,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DEGRADED + TITLE)} (${degradedValue})`,
                            value: degradedValue,
                            color: getColorByStatEnum(SalutEstatEnum.DEGRADED),
                        },
                        {
                            id: SalutEstatEnum.DOWN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DOWN + TITLE)} (${downValue})`,
                            value: downValue,
                            color: getColorByStatEnum(SalutEstatEnum.DOWN),
                        },
                        {
                            id: SalutEstatEnum.ERROR,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.ERROR + TITLE)} (${errorValue})`,
                            value: errorValue,
                            color: getColorByStatEnum(SalutEstatEnum.ERROR),
                        },
                        {
                            id: SalutEstatEnum.MAINTENANCE,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.MAINTENANCE + TITLE)} (${maintenanceValue})`,
                            value: maintenanceValue,
                            color: getColorByStatEnum(SalutEstatEnum.MAINTENANCE),
                        },
                        {
                            id: SalutEstatEnum.UNKNOWN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UNKNOWN + TITLE)} (${unknownValue})`,
                            value: unknownValue,
                            color: getColorByStatEnum(SalutEstatEnum.UNKNOWN),
                        },
                    ],
                },
            ]}
        />
    );
};

const ItemStateChip: React.FC<any> = (props: { salutField: keyof SalutModel; salutStatEnum: SalutEstatEnum; date?: string }) => {
    const { salutField, salutStatEnum, date } = props;
    const { t } = useTranslation();
    return (
        <>
            {salutStatEnum && (
                <SalutChipTooltip stateEnum={salutStatEnum} salutField={salutField}>
                    <Chip sx={{ bgcolor: getColorByStatEnum(salutStatEnum), color: ChipColor.WHITE,
                        "& .MuiChip-label": {
                            fontSize: "0.7rem !important",
                        }}}
                          icon={getMaterialIconByState(salutStatEnum)}
                          label={t(ENUM_APP_ESTAT_PREFIX + salutStatEnum + TITLE)}
                          size="small"
                    />
                </SalutChipTooltip>

            )}
            {!salutStatEnum && (
                <SalutChipTooltip stateEnum={SalutEstatEnum.UNKNOWN} salutField={salutField}>
                    <Chip sx={{ bgcolor: getColorByStatEnum(SalutEstatEnum.UNKNOWN), color: ChipColor.WHITE,
                        "& .MuiChip-label": {
                            fontSize: "0.7rem !important",
                        }}}
                          icon={getMaterialIconByState(SalutEstatEnum.UNKNOWN)}
                          label={t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UNKNOWN + TITLE)}
                          size="small"
                    />
                </SalutChipTooltip>
            )}
            {date && (<><br />
            <Typography variant="caption">{date}</Typography></>)}
        </>
    );
};

const AppDataTable: React.FC<any> = (props: {
    springFilter?: string;
    salutLastItems: SalutModel[];
}) => {
    const { springFilter, salutLastItems } = props;
    const { t } = useTranslation();
    const { getLinkComponent } = useBaseAppContext();
    const gridApiRef = useGridApiRef();

    const findSalutItem:(id: GridRowId) => SalutModel | null = React.useCallback(
        (id: GridRowId) => {
            return salutLastItems?.find((entry: SalutModel) => entry.entornAppId === id) ?? null;
        },
        [salutLastItems]
    );

    const renderItemStateChip = React.useCallback(
        (id: GridRowId, salutField: keyof SalutModel) => {
            const salutItem: SalutModel | null = findSalutItem(id);
            if (salutItem == null) {
                return undefined;
            }
            return (
                <ItemStateChip
                    salutField={salutField}
                    salutStatEnum={salutItem[salutField]}
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
                            <SalutGenericTooltip title={t('page.salut.integracions.integracioUpCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorByIntegracio(SalutModel.INTEGRACIO_UP_COUNT), color: ChipColor.WHITE,
                                        "& .MuiChip-label": {
                                            // fontSize: "0.7rem !important",
                                        }}}
                                    label={salutItem.integracioUpCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.integracions.integracioDownCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorByIntegracio(SalutModel.INTEGRACIO_DOWN_COUNT), color: ChipColor.WHITE,
                                        "& .MuiChip-label": {
                                            // fontSize: "0.7rem !important",
                                        }}}
                                    label={salutItem.integracioDownCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.integracions.integracioDesconegutCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorByIntegracio(SalutModel.INTEGRACIO_DESCONEGUT_COUNT), color: ChipColor.WHITE }}
                                    label={salutItem.integracioDesconegutCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
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
                            <SalutGenericTooltip title={t('page.salut.subsistemes.subsistemaUpCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorBySubsistema(SalutModel.SUBSISTEMA_UP_COUNT), color: ChipColor.WHITE }}
                                    label={salutItem.subsistemaUpCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.subsistemes.subsistemaDownCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorBySubsistema(SalutModel.SUBSISTEMA_DOWN_COUNT), color: ChipColor.WHITE }}
                                    label={salutItem.subsistemaDownCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
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
                            <SalutGenericTooltip title={t('page.salut.msgs.missatgeErrorCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_ERROR_COUNT), color: ChipColor.WHITE }}
                                    label={salutItem.missatgeErrorCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.msgs.missatgeWarnCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_WARN_COUNT), color: ChipColor.WHITE }}
                                    label={salutItem.missatgeWarnCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.msgs.missatgeInfoCount')}>
                                <Chip
                                    sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_INFO_COUNT), color: ChipColor.WHITE }}
                                    label={salutItem.missatgeInfoCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
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

    const treeDataRenderCell = useTreeDataEntornAppRenderCell();
    const getTreeDataPath = React.useCallback(
        (row: any) => [row.app.id, row.entorn.description],
        []
    );
    const groupingColDefAdditionalProps = React.useMemo(
        () => ({ renderCell: treeDataRenderCell }),
        [treeDataRenderCell]
    );
    const { dataGridProps: treeDataGridProps } = useTreeData(
        getTreeDataPath,
        t('page.salut.apps.column.group'),
        1,
        true,
        groupingColDefAdditionalProps);

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
