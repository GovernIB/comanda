import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Tab from '@mui/material/Tab';
import Tabs from '@mui/material/Tabs';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { useTheme } from '@mui/material/styles';
import { MarkPlot } from '@mui/x-charts/LineChart';
import { dateFormatLocale } from 'reactlib';
import UpdownBarChart from '../../components/salut/UpdownBarChart';
import { isDataInGroup, toXAxisDataGroups } from '../../util/dataGroup';
import { ErrorBoundary } from 'react-error-boundary';
import {
    ChartContainer,
    ChartsTooltip,
    ChartsXAxis,
    ChartsYAxis,
    LinePlot,
    LineSeriesType,
} from '@mui/x-charts';
import {
    useGetColorByIntegracio,
    useGetColorByNivellEnum,
    useGetColorBySubsistema,
    NivellEnum,
    SalutIntegracioModel,
    SalutModel,
} from '../../types/salut.model.tsx';
import { SalutField } from '../../components/salut/SalutChipTooltip.tsx';
import { ItemStateChip } from '../../components/salut/SalutItemStateChip.tsx';
import { Alert, Tooltip } from '@mui/material';
import { SalutData } from './Salut.tsx';
import { AppDataState, SalutInformeLatenciaItem } from './dataFetching';
import { SalutErrorBoundaryFallback } from '../../components/salut/SalutErrorBoundaryFallback';
import { EntornAppModel } from '../../types/app.model';
import SalutChip from '../../components/salut/SalutChip';
import ResponsiveCardTable from '../../components/salut/ResponsiveCardTable';
import { MUI_AXIS_WORKAROUND_HEIGHT } from '../../util/muiWorkarounds';
import LogsViewer from './LogsViewer';

const AppInfo: React.FC<{ salutCurrentApp: SalutModel; entornApp: EntornAppModel }> = props => {
    const { salutCurrentApp: app, entornApp: entornApp } = props;
    const { t } = useTranslation();
    const versio = entornApp && <Typography>{entornApp.versio}</Typography>;
    const revisio = entornApp && <Typography>{entornApp.revisioSimplificat}</Typography>;
    const jdk = entornApp && <Typography>{entornApp.jdkVersion}</Typography>;
    const data = app && <Typography>{dateFormatLocale(app.data, true)}</Typography>;

    const tableSections = [
        {
            id: 'versio',
            headerName: t($ => $.page.salut.info.versio),
            cellContent: versio,
        },
        {
            id: 'revisio',
            headerName: t($ => $.page.salut.info.revisio),
            cellContent: revisio,
        },
        {
            id: 'jdk',
            headerName: t($ => $.page.salut.info.jdk.versio),
            cellContent: jdk,
        },
        {
            id: 'data',
            headerName: t($ => $.page.salut.info.data),
            cellContent: data,
        },
    ];

    return (
        <ResponsiveCardTable
            title={t($ => $.page.salut.info.title)}
            tableSections={tableSections}
            breakpoint="lg"
        />
    );
};

const LatenciaLineChart: React.FC<{
    agrupacio: string;
    latencies: SalutInformeLatenciaItem[];
    grupsDates: string[];
}> = props => {
    const { agrupacio, latencies, grupsDates: baseDataGroups } = props;
    const { t } = useTranslation();
    if (!latencies || latencies.length === 0) {
        return (
            <Card variant="outlined" sx={{ height: '100%' }}>
                <CardContent sx={{ height: '100%' }}>
                    <Typography gutterBottom variant="h5" component="div">
                        {t($ => $.page.salut.latencia.title)}
                    </Typography>
                    <Typography
                        sx={{
                            height: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'center',
                            alignItems: 'center',
                        }}
                    >
                        {t($ => $.page.salut.estatLatencia.noInfo)}
                    </Typography>
                </CardContent>
            </Card>
        );
    }
    const seriesDataLatencia = baseDataGroups.map(group => {
        return (
            latencies.find(latenciaData => {
                if (!latenciaData || !latenciaData.data) return false;
                return isDataInGroup(latenciaData.data, group, agrupacio);
            })?.latenciaMitja ?? null
        );
    });
    const lineSeries: LineSeriesType[] = [
        {
            data: seriesDataLatencia,
            type: 'line',
            showMark: true,
            valueFormatter: v => (v == null ? '' : `${v} ms`),
        },
    ];
    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    return (
        <Card variant="outlined" sx={{ height: '340px' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.latencia.title)}
                </Typography>
                <ChartContainer
                    series={[...lineSeries]}
                    xAxis={[
                        {
                            height: MUI_AXIS_WORKAROUND_HEIGHT,
                            scaleType: 'band',
                            data: dataGroups,
                            id: 'latencia-x-axis-id',
                            // TODO Fer un formatter generic per a totes les agrupacions
                            valueFormatter: (value: string) =>
                                agrupacio === 'HORA' ? value.substring(3) : value,
                        },
                    ]}
                    yAxis={[{ label: ' ms', id: 'latencia-y-axis-id' }]}
                >
                    <LinePlot />
                    <MarkPlot />
                    <ChartsTooltip />
                    <ChartsYAxis axisId="latencia-y-axis-id" />
                    <ChartsXAxis axisId="latencia-x-axis-id" />
                </ChartContainer>
            </CardContent>
        </Card>
    );
};

const EstatsBarCard: React.FC<{
    agrupacio: string;
    estats: SalutData['estats'];
    grupsDates: string[];
}> = props => {
    const { grupsDates, agrupacio, estats } = props;
    const { t } = useTranslation();
    const hasData = estats && Object.keys(estats).length > 0;
    return (
        <Card variant="outlined" sx={{ height: '340px' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.estats.title)}
                </Typography>
                {!hasData ? (
                    <Typography
                        sx={{
                            height: '100%',
                            display: 'flex',
                            flexDirection: 'column',
                            justifyContent: 'center',
                            alignItems: 'center',
                        }}
                    >
                        {t($ => $.page.salut.estatLatencia.noInfo)}
                    </Typography>
                ) : (
                    <UpdownBarChart agrupacio={agrupacio} estats={estats} grupsDates={grupsDates} />
                )}
            </CardContent>
        </Card>
    );
};

const PeticionsOkError: React.FC<{ ok?: number; error?: number }> = props => {
    const { ok, error } = props;
    const theme = useTheme();
    return (
        <>
            <span style={{ color: theme.palette.success.main }}>{ok ?? 0}</span>
            &nbsp;/&nbsp;
            <span style={{ color: theme.palette.error.main }}>{error ?? 0}</span>
        </>
    );
};

const IntegracioRow: React.FC<{
    integracio: SalutIntegracioModel;
    fills?: SalutIntegracioModel[];
    padLeft?: boolean;
    toggleOpen: () => void;
    open: boolean;
}> = props => {
    const { integracio, fills, padLeft, toggleOpen, open } = props;
    const { t } = useTranslation();
    const displayName = integracio.nom ?? integracio.codi;
    return (
        <>
            <TableRow>
                <TableCell padding="none" align="center">
                    {fills?.length ? (
                        <IconButton size="small" onClick={toggleOpen}>
                            {open ? (
                                <Icon>keyboard_arrow_up</Icon>
                            ) : (
                                <Icon>keyboard_arrow_down</Icon>
                            )}
                        </IconButton>
                    ) : null}
                </TableCell>
                <TableCell>
                    <Box
                        sx={{
                            display: 'flex',
                            alignItems: 'center',
                            height: '100%',
                            gap: '10px',
                            paddingLeft: padLeft ? '20px' : '0px',
                        }}
                    >
                        {integracio.logo && (
                            <img
                                src={`data:image/png;base64,${integracio.logo}`}
                                alt="logo"
                                style={{ height: '32px' }}
                            />
                        )}
                        {integracio.endpoint ? (
                            <Tooltip title={integracio.endpoint}><span>{displayName}</span></Tooltip>
                        ) : (
                            displayName
                        )}
                    </Box>
                </TableCell>
                <TableCell>
                    <ItemStateChip
                        sx={{ ml: 1 }}
                        salutField={SalutField.INTEGRACIO_ESTAT}
                        salutStatEnum={integracio.estat}
                    />
                </TableCell>
                <TableCell>
                    <PeticionsOkError ok={integracio.totalOk} error={integracio.totalError} />
                </TableCell>
                <TableCell>
                    {integracio.totalTempsMig != null
                        ? integracio.totalTempsMig + ' ms'
                        : t($ => $.page.salut.nd)}
                </TableCell>
                <TableCell>
                    <PeticionsOkError
                        ok={integracio.peticionsOkUltimPeriode}
                        error={integracio.peticionsErrorUltimPeriode}
                    />
                </TableCell>
                <TableCell>
                    {integracio.tempsMigUltimPeriode != null
                        ? integracio.tempsMigUltimPeriode + ' ms'
                        : t($ => $.page.salut.nd)}
                </TableCell>
                {/*<TableCell>*/}
                {/*    {integracio.endpoint && <IconButton*/}
                {/*        component="a"*/}
                {/*        href={integracio.endpoint}*/}
                {/*        target="_blank"*/}
                {/*        size="small">*/}
                {/*            <Icon>launch</Icon>*/}
                {/*    </IconButton>}*/}
                {/*</TableCell>*/}
            </TableRow>
            {/*<Collapse in={open} timeout="auto" unmountOnExit>*/}
            {open && fills?.length
                ? fills.map((childIntegracio) => (
                      <IntegracioRow
                          padLeft
                          integracio={childIntegracio}
                          key={`integracioRowChild-${childIntegracio.id}`}
                          // IntegracioRow children don't have an expansion state
                          toggleOpen={() => {}}
                          open={false}
                      />
                  ))
                : undefined}
            {/*</Collapse>*/}
        </>
    );
};

const Integracions: React.FC<{
    salutCurrentApp: SalutModel;
    integracionsExpandState: string[];
    toggleIntegracioExpand: (id: string) => void;
}> = props => {
    const { salutCurrentApp, integracionsExpandState, toggleIntegracioExpand } = props;
    const { t } = useTranslation();
    const integracions = salutCurrentApp?.integracions;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.integracions.title)}
                </Typography>
                {!integracions?.length && (
                    <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                        {t($ => $.page.salut.integracions.noInfo)}
                    </Typography>
                )}
                {!!integracions?.length && (
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell></TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.integracions.column.nom)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.integracions.column.estat)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.integracions.column.peticionsTotals)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.integracions.column.tempsMigTotal)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.integracions.column.peticionsPeriode)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.integracions.column.tempsMigPeriode)}
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {integracions
                                .filter(i => i.pare == null)
                                .map(i => (
                                    <IntegracioRow
                                        open={integracionsExpandState.includes(i.codi)}
                                        toggleOpen={() => toggleIntegracioExpand(i.codi)}
                                        integracio={i}
                                        fills={integracions.filter(i2 => i2.pare?.id === i.id)}
                                        key={`integracioRow-${i.id}`}
                                    />
                                ))}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
};

const Subsistemes: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
    const { t } = useTranslation();
    const subsistemes = salutCurrentApp.subsistemes;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.subsistemes.title)}
                </Typography>
                {!subsistemes?.length && (
                    <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                        {t($ => $.page.salut.subsistemes.noInfo)}
                    </Typography>
                )}
                {!!subsistemes?.length && (
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    {t($ => $.page.salut.subsistemes.column.codi)}
                                </TableCell>
                                <TableCell>{t($ => $.page.salut.subsistemes.column.nom)}</TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.subsistemes.column.estat)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.subsistemes.column.peticionsTotals)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.subsistemes.column.tempsMigTotal)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.subsistemes.column.peticionsPeriode)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.subsistemes.column.tempsMigPeriode)}
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {subsistemes.map((s, key: number) => (
                                <TableRow key={key}>
                                    <TableCell>{s.codi}</TableCell>
                                    <TableCell>{s.nom}</TableCell>
                                    <TableCell>
                                        <ItemStateChip
                                            sx={{ ml: 1 }}
                                            salutField={SalutField.INTEGRACIO_ESTAT}
                                            salutStatEnum={s.estat}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <PeticionsOkError ok={s.totalOk} error={s.totalError} />
                                    </TableCell>
                                    <TableCell>
                                        {s.totalTempsMig != null
                                            ? s.totalTempsMig + ' ms'
                                            : t($ => $.page.salut.nd)}
                                    </TableCell>
                                    <TableCell>
                                        <PeticionsOkError
                                            ok={s.peticionsOkUltimPeriode}
                                            error={s.peticionsErrorUltimPeriode}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        {s.tempsMigUltimPeriode != null
                                            ? s.tempsMigUltimPeriode + ' ms'
                                            : t($ => $.page.salut.nd)}
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
};

const Contexts: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
    const { t } = useTranslation();
    const contexts = salutCurrentApp.contexts;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.contexts.title)}
                </Typography>
                {!contexts?.length && (
                    <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                        {t($ => $.page.salut.contexts.noInfo)}
                    </Typography>
                )}
                {contexts?.length && (
                    <>
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        {t($ => $.page.salut.contexts.column.nom)}
                                    </TableCell>
                                    <TableCell>
                                        {t($ => $.page.salut.contexts.column.path)}
                                    </TableCell>
                                    <TableCell>
                                        {t($ => $.page.salut.contexts.column.api)}
                                    </TableCell>
                                    {/*<TableCell>*/}
                                    {/*    {t($ => $.page.salut.contexts.column.manuals)}*/}
                                    {/*</TableCell>*/}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {contexts.map((s, key: number) => (
                                    <TableRow key={key}>
                                        <TableCell>{s.nom}</TableCell>
                                        <TableCell>
                                            {s.path && (
                                                <Button
                                                    href={s.path}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    sx={{ textTransform: 'none' }}
                                                >
                                                    {s.path}
                                                </Button>
                                            )}
                                        </TableCell>
                                        <TableCell>
                                            {s.api && (
                                                <IconButton
                                                    component="a"
                                                    href={s.api}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    size="small"
                                                >
                                                    <Icon>launch</Icon>
                                                </IconButton>
                                            )}
                                        </TableCell>
                                        {/*<TableCell>*/}
                                        {/*    {s.manuals &&*/}
                                        {/*        s.manuals.map((manual, index: number) => (*/}
                                        {/*            <Button*/}
                                        {/*                key={index}*/}
                                        {/*                href={manual.path ?? ''}*/}
                                        {/*                target="_blank"*/}
                                        {/*                rel="noopener noreferrer"*/}
                                        {/*                sx={{*/}
                                        {/*                    textTransform: 'none',*/}
                                        {/*                    display: 'block',*/}
                                        {/*                }}*/}
                                        {/*            >*/}
                                        {/*                {manual.nom}*/}
                                        {/*            </Button>*/}
                                        {/*        ))}*/}
                                        {/*</TableCell>*/}
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        <Box sx={{
                            mt: 2,
                            display: 'flex',
                        }}>
                            {contexts.map((s, key: number) =>
                                s.manuals?.map((manual, index: number) => (
                                    <Button
                                        key={`manual${key}-${index}`}
                                        href={manual.path ?? ''}
                                        target="_blank"
                                        rel="noopener noreferrer"
                                        sx={{
                                            textTransform: 'none',
                                            display: 'block',
                                        }}
                                    >
                                        {manual.nom}
                                    </Button>
                                ))
                            )}
                        </Box>
                    </>
                )}
            </CardContent>
        </Card>
    );
};

const Missatges: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
    const getColorByNivellEnum = useGetColorByNivellEnum();
    const { t } = useTranslation();
    const missatges = salutCurrentApp.missatges;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.missatges.title)}
                </Typography>
                {!missatges?.length && (
                    <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                        {t($ => $.page.salut.missatges.noInfo)}
                    </Typography>
                )}
                {!!missatges?.length && (
                    <Table size="small">
                        <TableHead>
                            <TableRow>
                                <TableCell>{t($ => $.page.salut.missatges.column.data)}</TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.missatges.column.nivell)}
                                </TableCell>
                                <TableCell>
                                    {t($ => $.page.salut.missatges.column.missatge)}
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {missatges.map((m, key: number) => (
                                <TableRow key={key}>
                                    <TableCell>
                                        <Typography>{dateFormatLocale(m.data, true)}</Typography>
                                    </TableCell>
                                    <TableCell>
                                        <Chip
                                            label={m.nivell}
                                            size="small"
                                            sx={{
                                                backgroundColor: getColorByNivellEnum(
                                                    m.nivell as NivellEnum
                                                ),
                                                color: 'white',
                                            }}
                                        />
                                    </TableCell>
                                    <TableCell>{m.missatge}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
};

// const EstatInfo: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
//     const { t } = useTranslation();
//     const { tTitle: tSalutEstatTitle } = useSalutEstatTranslation();
//     const integracionsEstat = getLowestCommonIntegracioEstat(salutCurrentApp);
//     const integracionsEstatChip = salutCurrentApp && (
//         <SalutChip
//             icon={getMaterialIconByState(integracionsEstat)}
//             label={tSalutEstatTitle(integracionsEstat)}
//             backgroundColor={getColorByStatEnum(integracionsEstat)}
//         />
//     );
//     const subsistemesEstat = getLowestCommonSubsistemesEstat(salutCurrentApp);
//     const subsistemesEstatChip = salutCurrentApp && (
//         <SalutChip
//             icon={getMaterialIconByState(subsistemesEstat)}
//             label={tSalutEstatTitle(subsistemesEstat)}
//             backgroundColor={getColorByStatEnum(subsistemesEstat)}
//         />
//     );
//
//     const missatges = salutCurrentApp && <SalutMissatgesChips salutItem={salutCurrentApp} />;
//     const integracions = salutCurrentApp && <SalutIntegracionsChips salutItem={salutCurrentApp} />;
//     const subsistemes = salutCurrentApp && <SalutSubsistemesChips salutItem={salutCurrentApp} />;
//
//     const tableSections = [
//
//         {
//             id: 'integracionsEstat',
//             headerName: t($ => $.page.salut.info.integracions),
//             cellContent: integracionsEstatChip,
//         },
//         {
//             id: 'subsistemesEstat',
//             headerName: t($ => $.page.salut.info.subsistemes),
//             cellContent: subsistemesEstatChip,
//         },
//
//         {
//             id: 'integracions',
//             headerName: t($ => $.page.salut.info.integracions),
//             cellContent: integracions,
//         },
//         {
//             id: 'subsistemes',
//             headerName: t($ => $.page.salut.info.subsistemes),
//             cellContent: subsistemes,
//         },
//         {
//             id: 'missatges',
//             headerName: t($ => $.page.salut.info.missatges),
//             cellContent: missatges,
//         },
//     ];
//
//     return (
//         <ResponsiveCardTable
//             title={t($ => $.page.salut.estats.title)}
//             noInfoMessage={t($ => $.page.salut.detalls.noInfo)}
//             tableSections={tableSections}
//             breakpoint="xl"
//         />
//     );
// };

const DetallInfo: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
    const { t } = useTranslation();
    const detalls = salutCurrentApp.detalls;
    const bdEstat = salutCurrentApp && (
        <ItemStateChip salutField={SalutField.BD_ESTAT} salutStatEnum={salutCurrentApp.bdEstat} />
    );

    const appLatencia = salutCurrentApp && (
        <Typography>
            {salutCurrentApp.appLatencia != null
                ? salutCurrentApp.appLatencia + ' ms'
                : t($ => $.page.salut.nd)}
        </Typography>
    );

    const tableSections = [
        ...(detalls ?? []).map(detall => ({
            id: detall.id,
            headerName: detall.nom,
            cellContent: detall.valor,
        })),
        {
            id: 'bdEstat',
            headerName: t($ => $.page.salut.info.bdEstat),
            cellContent: bdEstat,
        },
        {
            id: 'appLatencia',
            headerName: t($ => $.page.salut.info.appLatencia),
            cellContent: appLatencia,
        },
    ];
    return (
        <ResponsiveCardTable
            title={t($ => $.page.salut.detalls.title)}
            noInfoMessage={t($ => $.page.salut.detalls.noInfo)}
            tableSections={tableSections}
            breakpoint="xl"
        />
    );
};

interface SalutAppInfoTabProps {
    salutCurrentApp: SalutModel;
    entornApp: EntornAppModel;
    dataLoaded: boolean;
}

const WarningNoInfo = () => {
    const { t } = useTranslation();
    return <Alert severity="warning">{t($ => $.page.salut.info.noInfo)}</Alert>;
};
const DownAlert = () => {
    const { t } = useTranslation();
    return <Alert severity="error">{t($ => $.page.salut.info.downAlert)}</Alert>;
};

const TabEntorn: React.FC<SalutAppInfoTabProps> = ({ salutCurrentApp, entornApp }) => {
    return (
        <Grid container spacing={2} sx={{ mb: 2 }}>
            <Grid size={{ sm: 12, lg: 12 }}>
                <AppInfo salutCurrentApp={salutCurrentApp} entornApp={entornApp} />
            </Grid>
            {salutCurrentApp.peticioError ? (
                <Grid size={{ sm: 12, lg: 12 }}>
                    <DownAlert />
                </Grid>
            ) : (
                <>
                    <Grid size={{ sm: 12, lg: 12 }}>
                        <Contexts salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 12 }}>
                        <Missatges salutCurrentApp={salutCurrentApp} />
                    </Grid>
                </>
            )}
        </Grid>
    );
};

const TabEstatActual: React.FC<SalutAppInfoTabProps> = ({ salutCurrentApp }) => {
    if (salutCurrentApp.peticioError) {
        return (
            <Grid>
                <DownAlert />
            </Grid>
        );
    }
    return (
        <Grid container spacing={2} sx={{ mb: 2 }}>
            <Grid size={{ sm: 12, lg: 12 }}>
                <DetallInfo salutCurrentApp={salutCurrentApp} />
            </Grid>
            <Grid size={{ sm: 12, lg: 12 }}>
                <Subsistemes salutCurrentApp={salutCurrentApp} />
            </Grid>
            {/*<Grid size={{ sm: 12, lg: 12 }}>*/}
            {/*    <EstatInfo salutCurrentApp={salutCurrentApp} />*/}
            {/*</Grid>*/}
        </Grid>
    );
};

type TabIntegracionsOtherProps = {
    integracionsExpandState: string[];
    toggleIntegracioExpand: (id: string) => void;
};

const TabIntegracions: React.FC<
    SalutAppInfoTabProps & { otherProps: TabIntegracionsOtherProps }
> = ({ salutCurrentApp, otherProps: { integracionsExpandState, toggleIntegracioExpand } }) => {
    if (salutCurrentApp.peticioError) {
        return (
            <Grid>
                <DownAlert />
            </Grid>
        );
    }
    return (
        <Box
            sx={{
                height: 'auto',
            }}
        >
            <Integracions
                salutCurrentApp={salutCurrentApp}
                toggleIntegracioExpand={toggleIntegracioExpand}
                integracionsExpandState={integracionsExpandState}
            />
        </Box>
    );
};

type TabHistoricOtherProps = Pick<AppDataState, 'agrupacio' | 'estats' | 'latencies'> & {
    grupsDates?: string[];
};

const TabHistoric: React.FC<SalutAppInfoTabProps & { otherProps: TabHistoricOtherProps }> = ({
    otherProps: { agrupacio, estats, grupsDates, latencies },
}) => {
    if (agrupacio == null || estats == null || grupsDates == null) return;

    return (
        <Grid container gap={2}>
            <Grid size={12}>
                <ErrorBoundary fallback={<SalutErrorBoundaryFallback />}>
                    <EstatsBarCard agrupacio={agrupacio} estats={estats} grupsDates={grupsDates} />
                </ErrorBoundary>
            </Grid>
            <Grid size={12}>
                <ErrorBoundary fallback={<SalutErrorBoundaryFallback />}>
                    {latencies != null && (
                        <>
                            <LatenciaLineChart
                                agrupacio={agrupacio}
                                latencies={latencies}
                                grupsDates={grupsDates}
                            />
                        </>
                    )}
                </ErrorBoundary>
            </Grid>
        </Grid>
    );
};

const TabLogs = ({ entornApp }: { entornApp: EntornAppModel | null }) => {
    if (entornApp == null)
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100%',
                }}
            >
                <CircularProgress size={100} />
            </Box>
        );

    return <LogsViewer entornAppId={entornApp.id} />;
};

/**
 * Component wrapper per als tabs que depenen del valor de salutCurrentApp
 * Accepta una sèrie de props, gestiona els casos d'error i renderitza el component childrenTabComponent
 * amb les props validades per a la interfície SalutAppInfoTabProps i a més passa sense tractar
 * les props especificades al genèric T.
 */
function TabSalutCurrentApp<T>({
    salutCurrentApp,
    entornApp,
    dataLoaded,
    childrenTabComponent: ChildrenTabComponent,
    childrenTabOtherProps,
}: {
    salutCurrentApp: SalutModel | null;
    entornApp: EntornAppModel | null;
    dataLoaded: boolean;
    childrenTabComponent: React.FC<SalutAppInfoTabProps & { otherProps: T }>;
    childrenTabOtherProps: T;
}) {
    if (dataLoaded && salutCurrentApp == null) return <WarningNoInfo />;
    if (salutCurrentApp == null || entornApp == null)
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100%',
                }}
            >
                <CircularProgress size={100} />
            </Box>
        );
    return (
        <ChildrenTabComponent
            salutCurrentApp={salutCurrentApp}
            entornApp={entornApp}
            dataLoaded={dataLoaded}
            otherProps={childrenTabOtherProps}
        />
    );
}

const SalutAppInfo: React.FC<{
    appInfoData: AppDataState;
    ready: boolean;
    grupsDates?: string[];
}> = ({ appInfoData, grupsDates, ready }) => {
    const { t } = useTranslation();
    const getColorBySubsistema = useGetColorBySubsistema();
    const getColorByIntegracio = useGetColorByIntegracio();
    const { salutCurrentApp, entornApp, loading, agrupacio, estats, latencies } = appInfoData;
    const [integracionsExpandState, setIntegracionsExpandState] = React.useState<string[]>([]);
    const toggleIntegracioExpand = (id: string) => {
        if (integracionsExpandState.includes(id))
            setIntegracionsExpandState(integracionsExpandState.filter((i: string) => i !== id));
        else setIntegracionsExpandState([...integracionsExpandState, id]);
    };
    const dataLoaded = ready && loading != null && !loading;

    const [tabValue, setTabValue] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTabValue(newValue);
    };

    const tabs = [
        {
            id: 'entorn',
            label: t($ => $.page.salut.tabs.entorn),
            icon: <Icon>info</Icon>,
        },
        {
            id: 'estatActual',
            label: (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {t($ => $.page.salut.tabs.estatActual)}
                    {!!salutCurrentApp?.subsistemaDownCount && (
                        <SalutChip
                            label={salutCurrentApp.subsistemaDownCount}
                            tooltip={t($ => $.page.salut.subsistemes.subsistemaDownCount)}
                            backgroundColor={getColorBySubsistema(SalutModel.SUBSISTEMA_DOWN_COUNT)}
                        />
                    )}
                </Box>
            ),
            icon: <Icon>troubleshoot</Icon>,
        },
        {
            id: 'integracions',
            label: (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    {t($ => $.page.salut.tabs.integracions)}
                    {!!salutCurrentApp?.integracioDownCount && (
                        <SalutChip
                            label={salutCurrentApp.integracioDownCount}
                            tooltip={t($ => $.page.salut.integracions.integracioDownCount)}
                            backgroundColor={getColorByIntegracio(SalutModel.INTEGRACIO_DOWN_COUNT)}
                        />
                    )}
                </Box>
            ),
            icon: <Icon>account_tree</Icon>,
        },
        {
            id: 'historic',
            label: t($ => $.page.salut.tabs.historic),
            icon: <Icon>timeline</Icon>,
        },
        {
            id: 'logs',
            label: t($ => $.page.salut.tabs.logs),
            icon: <Icon>notes</Icon>,
            disabled: true,
        },
    ];

    return (
        <>
            <Tabs
                value={tabValue}
                onChange={handleChange}
                sx={{
                    mx: 2,
                    borderBottom: 1,
                    borderColor: 'divider',
                }}
            >
                {tabs.map(tab => (
                    <Tab
                        key={'salutAppInfoTabs-' + tab.id}
                        iconPosition="start"
                        label={tab.label}
                        icon={tab.icon}
                    />
                ))}
            </Tabs>
            <Box
                sx={{
                    p: 2,
                    height: '100%',
                }}
            >
                {tabValue === 0 && (
                    <TabSalutCurrentApp
                        salutCurrentApp={salutCurrentApp}
                        entornApp={entornApp}
                        dataLoaded={dataLoaded}
                        childrenTabComponent={TabEntorn}
                        childrenTabOtherProps={{}}
                    />
                )}
                {tabValue === 1 && (
                    <TabSalutCurrentApp
                        salutCurrentApp={salutCurrentApp}
                        entornApp={entornApp}
                        dataLoaded={dataLoaded}
                        childrenTabComponent={TabEstatActual}
                        childrenTabOtherProps={{}}
                    />
                )}
                {tabValue === 2 && (
                    <TabSalutCurrentApp<TabIntegracionsOtherProps>
                        salutCurrentApp={salutCurrentApp}
                        entornApp={entornApp}
                        dataLoaded={dataLoaded}
                        childrenTabComponent={TabIntegracions}
                        childrenTabOtherProps={{
                            integracionsExpandState: integracionsExpandState,
                            toggleIntegracioExpand: toggleIntegracioExpand,
                        }}
                    />
                )}
                {tabValue === 3 && (
                    <TabSalutCurrentApp<TabHistoricOtherProps>
                        salutCurrentApp={salutCurrentApp}
                        entornApp={entornApp}
                        dataLoaded={dataLoaded}
                        childrenTabComponent={TabHistoric}
                        childrenTabOtherProps={{
                            agrupacio,
                            estats,
                            grupsDates,
                            latencies,
                        }}
                    />
                )}
                {tabValue === 4 && <TabLogs entornApp={entornApp} />}
            </Box>
        </>
    );
};

export default SalutAppInfo;
