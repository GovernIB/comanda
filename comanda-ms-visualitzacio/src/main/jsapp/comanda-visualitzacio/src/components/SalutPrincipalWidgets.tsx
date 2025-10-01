import Skeleton from '@mui/material/Skeleton';
import Box from '@mui/material/Box';
import estils from './estadistiques/WidgetEstils.ts';
import Typography from '@mui/material/Typography';
import Chip from '@mui/material/Chip';
import * as React from 'react';
import Grid from '@mui/material/Grid';
import { useTranslation } from 'react-i18next';
import {
    dateFormatLocale,
    MuiDataGridColDef,
    useBaseAppContext,
} from 'reactlib';
import { Button, Paper, styled } from '@mui/material';
import { SalutGenericTooltip } from './SalutChipTooltip.tsx';
import {
    ENUM_APP_ESTAT_PREFIX,
    getColorByIntegracio,
    getColorByMissatge,
    getColorByStatEnum,
    getColorBySubsistema,
    SalutEstatEnum,
    SalutModel,
    TITLE,
} from '../types/salut.model.tsx';
import { ChipColor } from '../util/colorUtil.ts';
import { ItemStateChip } from './SalutItemStateChip.tsx';
import { DataGridPro, GridRowId, GridSlots } from '@mui/x-data-grid-pro';
import { PieChart, useDrawingArea } from '@mui/x-charts';
import DataGridNoRowsOverlay from '../../lib/components/mui/datagrid/DataGridNoRowsOverlay.tsx';
import UpdownBarChart from './UpdownBarChart.tsx';
import { SalutData } from '../pages/Salut.tsx';
import { ErrorBoundaryFallback } from '../pages/SalutAppInfo.tsx';
import { ErrorBoundary } from 'react-error-boundary';
import IconButton from '@mui/material/IconButton';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowUpIcon from '@mui/icons-material/KeyboardArrowUp';
import useSizeTracker from '../hooks/useSizeTracker';
import {AppModel, EntornAppModel} from "../types/app.model.tsx";
import {EntornModel} from "../types/entorn.model.tsx";

const StyledText = styled('text')(({ theme }) => ({
    fill: theme.palette.text.primary,
    textAnchor: 'middle',
    dominantBaseline: 'central',
    fontSize: 50,
    fontWeight: 'bold',
}));

function PieCenterLabel({ children }: { children: React.ReactNode }) {
    const { width, height, left, top } = useDrawingArea();
    return (
        <StyledText x={left + width / 2} y={top + height / 2}>
            {children}
        </StyledText>
    );
}

const UpdownPieChart: React.FC<{ salutLastItems: SalutModel[] }> = (props) => {
    const { salutLastItems } = props;
    const { t } = useTranslation();

    const upValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.UP
    ).length;
    const warnValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.WARN
    ).length;
    const errorValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.ERROR
    ).length;
    const downValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.DOWN
    ).length;
    const degradedValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.DEGRADED
    ).length;
    const maintenanceValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.MAINTENANCE
    ).length;
    const unknownValue = salutLastItems.filter(
        (salutItem) => salutItem.appEstat === SalutEstatEnum.UNKNOWN
    ).length;

    return (
        <PieChart
            slotProps={{
                legend: {
                    sx: {
                        gap: 1,
                    },
                },
            }}
            series={[
                {
                    innerRadius: 50,
                    // outerRadius: 100,
                    // paddingAngle: 1,
                    highlightScope: { fade: 'global', highlight: 'item' },
                    highlighted: {
                        additionalRadius: 1,
                    },
                    // cornerRadius: 5,
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
                            id: SalutEstatEnum.ERROR,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.ERROR + TITLE)} (${errorValue})`,
                            value: errorValue,
                            color: getColorByStatEnum(SalutEstatEnum.ERROR),
                        },
                        {
                            id: SalutEstatEnum.DOWN,
                            label: `${t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DOWN + TITLE)} (${downValue})`,
                            value: downValue,
                            color: getColorByStatEnum(SalutEstatEnum.DOWN),
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
        >
            <PieCenterLabel> {salutLastItems.length}</PieCenterLabel>
        </PieChart>
    );
};

const AppDataTable: React.FC<{
    salutLastItems: SalutModel[];
    apps?: AppModel[];
    entorns?: EntornModel[];
    entornApps: EntornAppModel[];
    groupedApp?: AppModel;
    groupedEntorn?: EntornModel;
}> = (props) => {
    const { salutLastItems, apps, entorns, entornApps, groupedApp, groupedEntorn } = props;
    const { t } = useTranslation();
    const { getLinkComponent } = useBaseAppContext();

    const findSalutItem: (id: GridRowId) => SalutModel | null = React.useCallback(
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
                    sx={{ mr: 1 }}
                    salutField={salutField}
                    salutStatEnum={salutItem[salutField] as SalutEstatEnum}
                />
            );
        },
        [findSalutItem]
    );

    const columns: MuiDataGridColDef[] = React.useMemo(
        () => [
            {
                flex: 0.5,
                field: 'nomLogo',
                headerName: t('page.salut.apps.column.group'),
                minWidth: 150,
                valueGetter: (_value, row) => {
                    const isGroupedByApp = groupedApp != null;
                    const entorn =
                        entorns != null && entorns.find((entorn) => entorn.id === row.entorn.id);

                    if (isGroupedByApp && entorn) return entorn.nom ?? entorn.codi;
                },
                renderCell: (params) => {
                    const isGroupedByEntorn = groupedEntorn != null;

                    const app = apps != null && apps.find((app) => app.id === params.row.app.id);
                    if (isGroupedByEntorn && app)
                        return (
                            <Box
                                sx={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '8px',
                                }}
                            >
                                {app.logo && (
                                    <img
                                        src={'data:image/png;base64,' + app.logo}
                                        alt="Logo"
                                        style={{ height: '48px' }}
                                    />
                                )}
                                {app.nom}
                            </Box>
                        );
                },
            },
            {
                flex: 0.3,
                field: 'estat',
                headerName: t('page.salut.apps.column.estat'),
                minWidth: 100,
                renderCell: ({ id }) => renderItemStateChip(id, SalutModel.APP_ESTAT),
            },
            {
                flex: 0.3,
                field: 'infoData',
                headerName: t('page.salut.apps.column.infoData'),
                description: t('page.salut.apps.column.infoDataDescription'),
                minWidth: 150,
                renderCell: ({ id }) => {
                    const salutItem: SalutModel | null = findSalutItem(id);
                    if (salutItem == null) {
                        return '';
                    }
                    return salutItem?.data
                        ? dateFormatLocale(salutItem?.data, true)
                        : t('page.salut.nd');
                },
            },
            {
                flex: 0.3,
                field: 'versio',
                headerName: t('page.salut.apps.column.versio'),
                minWidth: 100,
            },
            {
                flex: 0.3,
                field: 'revisioSimplificat',
                headerName: t('page.salut.apps.column.revisio'),
                minWidth: 100,
            },
            {
                flex: 0.3,
                field: 'bd',
                headerName: t('page.salut.apps.column.bd'),
                minWidth: 100,
                renderCell: ({ id }) => renderItemStateChip(id, SalutModel.BD_ESTAT),
            },
            {
                flex: 0.3,
                field: 'latencia',
                headerName: t('page.salut.apps.column.latencia'),
                minWidth: 100,
                valueGetter: (_value, row) => {
                    const salutItem: SalutModel | null = findSalutItem(row.id);
                    if (salutItem == null) {
                        return '';
                    }
                    return salutItem?.appLatencia != null
                        ? salutItem.appLatencia + ' ms'
                        : t('page.salut.nd');
                },
            },
            {
                flex: 0.1,
                field: SalutModel.INTEGRACIONS,
                headerName: t('page.salut.apps.column.integ'),
                minWidth: 130,
                renderCell: ({ id }) => {
                    const salutItem: SalutModel | null = findSalutItem(id);

                    if (!salutItem) {
                        return null;
                    }

                    return (
                        <>
                            <SalutGenericTooltip
                                title={t('page.salut.integracions.integracioUpCount')}
                            >
                                <Chip
                                    sx={{
                                        bgcolor: getColorByIntegracio(
                                            SalutModel.INTEGRACIO_UP_COUNT
                                        ),
                                        color: ChipColor.WHITE,
                                        '& .MuiChip-label': {
                                            // fontSize: "0.7rem !important",
                                        },
                                    }}
                                    label={salutItem.integracioUpCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip
                                title={t('page.salut.integracions.integracioDownCount')}
                            >
                                <Chip
                                    sx={{
                                        bgcolor: getColorByIntegracio(
                                            SalutModel.INTEGRACIO_DOWN_COUNT
                                        ),
                                        color: ChipColor.WHITE,
                                        '& .MuiChip-label': {
                                            // fontSize: "0.7rem !important",
                                        },
                                    }}
                                    label={salutItem.integracioDownCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip
                                title={t('page.salut.integracions.integracioDesconegutCount')}
                            >
                                <Chip
                                    sx={{
                                        bgcolor: getColorByIntegracio(
                                            SalutModel.INTEGRACIO_DESCONEGUT_COUNT
                                        ),
                                        color: ChipColor.WHITE,
                                    }}
                                    label={salutItem.integracioDesconegutCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                        </>
                    );
                },
            },
            {
                flex: 0.2,
                field: SalutModel.SUBSISTEMES,
                headerName: t('page.salut.apps.column.subsis'),
                minWidth: 100,
                renderCell: ({ id }) => {
                    const salutItem: SalutModel | null = findSalutItem(id);

                    if (!salutItem) {
                        return null;
                    }
                    return (
                        <>
                            <SalutGenericTooltip
                                title={t('page.salut.subsistemes.subsistemaUpCount')}
                            >
                                <Chip
                                    sx={{
                                        bgcolor: getColorBySubsistema(
                                            SalutModel.SUBSISTEMA_UP_COUNT
                                        ),
                                        color: ChipColor.WHITE,
                                    }}
                                    label={salutItem.subsistemaUpCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip
                                title={t('page.salut.subsistemes.subsistemaDownCount')}
                            >
                                <Chip
                                    sx={{
                                        bgcolor: getColorBySubsistema(
                                            SalutModel.SUBSISTEMA_DOWN_COUNT
                                        ),
                                        color: ChipColor.WHITE,
                                    }}
                                    label={salutItem.subsistemaDownCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                        </>
                    );
                },
            },
            {
                flex: 0.1,
                field: 'msgs',
                headerName: t('page.salut.apps.column.msgs'),
                minWidth: 130,
                renderCell: ({ id }) => {
                    const salutItem: SalutModel | null = findSalutItem(id);

                    if (!salutItem) {
                        return null;
                    }
                    return (
                        <>
                            <SalutGenericTooltip title={t('page.salut.msgs.missatgeErrorCount')}>
                                <Chip
                                    sx={{
                                        bgcolor: getColorByMissatge(
                                            SalutModel.MISSATGE_ERROR_COUNT
                                        ),
                                        color: ChipColor.WHITE,
                                    }}
                                    label={salutItem.missatgeErrorCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.msgs.missatgeWarnCount')}>
                                <Chip
                                    sx={{
                                        bgcolor: getColorByMissatge(SalutModel.MISSATGE_WARN_COUNT),
                                        color: ChipColor.WHITE,
                                    }}
                                    label={salutItem.missatgeWarnCount}
                                    size="small"
                                />
                            </SalutGenericTooltip>
                            &nbsp;/&nbsp;
                            <SalutGenericTooltip title={t('page.salut.msgs.missatgeInfoCount')}>
                                <Chip
                                    sx={{
                                        bgcolor: getColorByMissatge(SalutModel.MISSATGE_INFO_COUNT),
                                        color: ChipColor.WHITE,
                                    }}
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
                renderCell: (params) =>
                    params.rowNode.type !== 'group' && (
                        <Button
                            variant="contained"
                            size="small"
                            component={getLinkComponent()}
                            to={'appinfo/' + params.id}
                        >
                            {t('page.salut.apps.detalls')}
                        </Button>
                    ),
            },
        ],
        [
            apps,
            entorns,
            findSalutItem,
            getLinkComponent,
            groupedApp,
            groupedEntorn,
            renderItemStateChip,
            t,
        ]
    );

    return (
        <DataGridPro
            columns={columns}
            rows={entornApps}
            hideFooter
            slots={{
                noRowsOverlay: DataGridNoRowsOverlay as GridSlots['noRowsOverlay'],
            }}
        />
    );
};

export const SalutWidgetTitle: React.FC<{
    app?: AppModel;
    entorn?: EntornModel;
    loading?: boolean;
    midaFontTitol?: number;
}> = ({ app, entorn, loading, midaFontTitol }) => {
    const titleEstils = {
        ...estils.titleText,
        fontSize: midaFontTitol ? `${midaFontTitol}px` : estils.titleText.fontSize,
    };

    return (
        <Box>
            {loading ? (
                <>
                    <Skeleton width="70%" height={32} />
                    <Box sx={estils.iconContainer}>
                        <Skeleton width={40} height={24} />
                    </Box>
                </>
            ) : (
                <>
                    {
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                gap: '8px',
                            }}
                        >
                            {app?.logo && (
                                <img
                                    src={'data:image/png;base64,' + app.logo}
                                    alt="Logo"
                                    style={{ height: '38px' }}
                                />
                            )}
                            {app && <Typography sx={titleEstils}>{app.nom}</Typography>}
                            {entorn && <Typography sx={titleEstils}>{entorn.nom}</Typography>}
                        </Box>
                    }
                </>
            )}
        </Box>
    );
};

export const SalutWidgetContent: React.FC<{
    salutLastItems: SalutModel[];
    reportParams: {
        dataInici: string;
        dataFi: string;
        agrupacio: string;
    };
    estats: SalutData['estats'];
    loading?: boolean;
    groupedApp?: AppModel;
    groupedEntorn?: EntornModel;
    apps?: AppModel[];
    entorns?: EntornModel[];
    entornApps: EntornAppModel[];
    grupsDates: string[];
}> = ({ salutLastItems, reportParams, estats, loading, groupedApp, groupedEntorn, entornApps, apps, entorns, grupsDates }) => {
    const [open, setOpen] = React.useState(false);
    const { size: trackedGridSize, refCallback: trackedGridRef } = useSizeTracker(100);

    if (loading)
        return (
            <>
                <Box sx={{ ...estils.contentText(true), width: '10em' }}>
                    <Skeleton width="100%" height={80} />
                </Box>
                <Box sx={{ ...estils.contentText(true), width: '4em' }}>
                    <Skeleton width="100%" height={20} />
                </Box>
            </>
        );

    return (
        <Box
            sx={{
                height: !open ? '220px' : trackedGridSize?.height + 'px',
                transition: 'height 0.3s ease-in-out',
                overflow: 'hidden',
                position: 'relative',
            }}
        >
            <Grid ref={trackedGridRef} container spacing={2}>
                <Grid
                    size={{ xs: 12, sm: 11, md: 11, lg: 3 }}
                    sx={{ display: 'flex', flexDirection: 'column', height: '200px' }}
                >
                    <SalutWidgetTitle app={groupedApp} entorn={groupedEntorn} />
                    <Box
                        sx={{
                            height: '100%',
                            minHeight: 0,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                        }}
                    >
                        <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                            <UpdownPieChart salutLastItems={salutLastItems} />
                        </ErrorBoundary>
                    </Box>
                </Grid>
                <Grid size={{ xs: 12, sm: 12, md: 12, lg: 9 }} sx={{ height: '200px' }}>
                    <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                        <UpdownBarChart
                            dataInici={reportParams.dataInici}
                            agrupacio={reportParams.agrupacio}
                            estats={estats}
                            grupsDates={grupsDates}
                        />
                    </ErrorBoundary>
                </Grid>
                {open && (
                    <Grid size={12}>
                        <AppDataTable
                            groupedApp={groupedApp}
                            groupedEntorn={groupedEntorn}
                            apps={apps}
                            entorns={entorns}
                            entornApps={entornApps}
                            salutLastItems={salutLastItems}
                        />
                    </Grid>
                )}
                <Grid
                    size={12}
                    sx={{
                        display: 'flex',
                        justifyContent: 'center',
                        position: 'absolute',
                        bottom: '0px',
                        width: '100%',
                    }}
                >
                    <IconButton onClick={() => setOpen(!open)}>
                        {open ? <KeyboardArrowUpIcon /> : <KeyboardArrowDownIcon />}
                    </IconButton>
                </Grid>
                {/* Botón de expansión duplicado para ocupar la altura del botón original con position: absolute */}
                <Grid size={12}>
                    <IconButton>
                        <KeyboardArrowUpIcon
                            sx={{
                                visibility: 'hidden',
                            }}
                        />
                    </IconButton>
                </Grid>
            </Grid>
        </Box>
    );
};

export const SalutLlistat = ({
    salutGroups,
    grupsDates,
    reportInterval,
    apps,
    entorns,
}: {
    apps?: AppModel[];
    entorns?: EntornModel[];
    salutGroups: SalutData[];
    grupsDates?: string[];
    reportInterval?: {
        dataInici: string;
        dataFi: string;
        agrupacio: string;
    };
    springFilter?: string;
}) => {
    if (!salutGroups.length || grupsDates == null || reportInterval == null) return;

    return (
        <>
            {salutGroups.map((salutGroup, index) => {
                return (
                    <Paper key={index} elevation={1} sx={{ px: 2, pt: 1, marginBottom: 1 }}>
                        <SalutWidgetContent
                            salutLastItems={salutGroup.salutLastItems}
                            reportParams={reportInterval}
                            estats={salutGroup.estats}
                            groupedApp={salutGroup.groupedApp}
                            groupedEntorn={salutGroup.groupedEntorn}
                            apps={apps}
                            entorns={entorns}
                            entornApps={salutGroup.entornApps}
                            grupsDates={grupsDates}
                        />
                    </Paper>
                );
            })}
        </>
    );
};
