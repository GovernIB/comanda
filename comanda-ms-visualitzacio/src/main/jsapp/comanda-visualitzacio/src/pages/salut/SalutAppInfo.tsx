import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
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
    getColorByMissatge,
    getColorByNivellEnum,
    NivellEnum,
    SalutModel,
} from '../../types/salut.model.tsx';
import { ChipColor } from '../../util/colorUtil.ts';
import { SalutField, SalutGenericTooltip } from '../../components/salut/SalutChipTooltip.tsx';
import { ItemStateChip } from '../../components/salut/SalutItemStateChip.tsx';
import { Alert, Tooltip } from '@mui/material';
import { SalutData } from './Salut.tsx';
import { AppDataState, SalutInformeLatenciaItem } from './dataFetching';
import { SalutErrorBoundaryFallback } from '../../components/salut/SalutErrorBoundaryFallback';

const AppInfo: React.FC<{ salutCurrentApp: SalutModel; entornApp: any }> = props => {
    const { salutCurrentApp: app, entornApp: entornApp } = props;
    const { t } = useTranslation();
    const versio = entornApp && <Typography>{entornApp.versio}</Typography>;
    const revisio = entornApp && <Typography>{entornApp.revisioSimplificat}</Typography>;
    const jdk = entornApp && <Typography>{entornApp.jdkVersion}</Typography>;
    const data = app && <Typography>{dateFormatLocale(app.data, true)}</Typography>;
    const bdEstat = app && <ItemStateChip salutField={SalutField.BD_ESTAT} salutStatEnum={app.bdEstat} />;
    const appLatencia = app && <Typography>{app.appLatencia != null ? app.appLatencia + ' ms' : t($ => $.page.salut.nd)}</Typography>;
    const missatges = app && <>
        <SalutGenericTooltip title={t($ => $.page.salut.msgs.missatgeErrorCount)}>
            <Chip
                sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_ERROR_COUNT), color: ChipColor.WHITE }}
                label={app.missatgeErrorCount}
                size="small"
            />
        </SalutGenericTooltip>
        &nbsp;/&nbsp;
        <SalutGenericTooltip title={t($ => $.page.salut.msgs.missatgeWarnCount)}>
            <Chip
                sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_WARN_COUNT), color: ChipColor.WHITE }}
                label={app.missatgeWarnCount}
                size="small"
            />
        </SalutGenericTooltip>
        &nbsp;/&nbsp;
        <SalutGenericTooltip title={t($ => $.page.salut.msgs.missatgeInfoCount)}>
            <Chip
                sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_INFO_COUNT), color: ChipColor.WHITE }}
                label={app.missatgeInfoCount}
                size="small"
            />
        </SalutGenericTooltip>
    </>;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.info.title)}
                </Typography>
                <Table size="small" sx={{ width: '100%', tableLayout: 'fixed' }}>
                    <TableBody>
                        <TableRow key={1}>
                            <TableCell>{t($ => $.page.salut.info.versio)}</TableCell>
                            <TableCell>{versio}</TableCell>
                        </TableRow>
                        <TableRow key={2}>
                            <TableCell>{t($ => $.page.salut.info.revisio)}</TableCell>
                            <TableCell>{revisio}</TableCell>
                        </TableRow>
                        <TableRow key={3}>
                            <TableCell>{t($ => $.page.salut.info.jdk.versio)}</TableCell>
                            <TableCell>{jdk}</TableCell>
                        </TableRow>
                        <TableRow key={4}>
                            <TableCell>{t($ => $.page.salut.info.data)}</TableCell>
                            <TableCell>{data}</TableCell>
                        </TableRow>
                        <TableRow key={5}>
                            <TableCell>{t($ => $.page.salut.info.bdEstat)}</TableCell>
                            <TableCell>{bdEstat}</TableCell>
                        </TableRow>
                        <TableRow key={6}>
                            <TableCell>{t($ => $.page.salut.info.appLatencia)}</TableCell>
                            <TableCell>{appLatencia}</TableCell>
                        </TableRow>
                        <TableRow key={7}>
                            <TableCell>{t($ => $.page.salut.info.missatges)}</TableCell>
                            <TableCell>{missatges}</TableCell>
                        </TableRow>
                    </TableBody>
                </Table>
            </CardContent>
        </Card>
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
        <Card variant="outlined" sx={{ height: '350px' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">
                    {t($ => $.page.salut.latencia.title)}
                </Typography>
                <ChartContainer
                    series={[...lineSeries]}
                    xAxis={[
                        {
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
        <Card variant="outlined" sx={{ height: '350px' }}>
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

const PeticionsOkError: React.FC<{ ok?: number, error?: number }> = props => {
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

const IntegracioRow: React.FC<any> = props => {
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
                            <Tooltip title={integracio.endpoint}>{displayName}</Tooltip>
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
                ? fills.map((childIntegracio: any) => (
                      <IntegracioRow
                          padLeft
                          integracio={childIntegracio}
                          key={`integracioRowChild-${childIntegracio.id}`}
                      />
                  ))
                : undefined}
            {/*</Collapse>*/}
        </>
    );
};

const Integracions: React.FC<any> = props => {
    const { salutCurrentApp, integracionsExpandState, toggleIntegracioExpand } = props;
    const { t } = useTranslation();
    const integracions = salutCurrentApp?.integracions;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">{t($ => $.page.salut.integracions.title)}</Typography>
                {!integracions?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                    {t($ => $.page.salut.integracions.noInfo)}
                </Typography>}
                {integracions?.length > 0 && <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell></TableCell>
                            <TableCell>{t($ => $.page.salut.integracions.column.nom)}</TableCell>
                            <TableCell>{t($ => $.page.salut.integracions.column.estat)}</TableCell>
                            <TableCell>{t($ => $.page.salut.integracions.column.peticionsTotals)}</TableCell>
                            <TableCell>{t($ => $.page.salut.integracions.column.tempsMigTotal)}</TableCell>
                            <TableCell>{t($ => $.page.salut.integracions.column.peticionsPeriode)}</TableCell>
                            <TableCell>{t($ => $.page.salut.integracions.column.tempsMigPeriode)}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {integracions.filter((i: any) => i.pare == null).map((i: any) => <IntegracioRow
                            open={integracionsExpandState.includes(i.codi)}
                            toggleOpen={() => toggleIntegracioExpand(i.codi)}
                        integracio={i}
                        fills={integracions.filter((i2: any) => i2.pare?.id === i.id)}
                        key={`integracioRow-${i.id}`} />)}
                    </TableBody>
                </Table>}
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
                <Typography gutterBottom variant="h5" component="div">{t($ => $.page.salut.subsistemes.title)}</Typography>
                {!subsistemes?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                    {t($ => $.page.salut.subsistemes.noInfo)}
                </Typography>}
                {subsistemes?.length && <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.codi)}</TableCell>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.nom)}</TableCell>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.estat)}</TableCell>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.peticionsTotals)}</TableCell>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.tempsMigTotal)}</TableCell>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.peticionsPeriode)}</TableCell>
                            <TableCell>{t($ => $.page.salut.subsistemes.column.tempsMigPeriode)}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {subsistemes.map((s, key: number) => <TableRow key={key}>
                            <TableCell>{s.codi}</TableCell>
                            <TableCell>{s.nom}</TableCell>
                            <TableCell>
                                <ItemStateChip sx={{ ml: 1 }} salutField={SalutField.INTEGRACIO_ESTAT} salutStatEnum={s.estat} />
                            </TableCell>
                            <TableCell>
                                <PeticionsOkError ok={s.totalOk} error={s.totalError} />
                            </TableCell>
                            <TableCell>{s.totalTempsMig != null ? s.totalTempsMig + ' ms' : t($ => $.page.salut.nd)}</TableCell>
                            <TableCell>
                                <PeticionsOkError ok={s.peticionsOkUltimPeriode} error={s.peticionsErrorUltimPeriode} />
                            </TableCell>
                            <TableCell>{s.tempsMigUltimPeriode != null ? s.tempsMigUltimPeriode + ' ms' : t($ => $.page.salut.nd)}</TableCell>
                        </TableRow>)}
                    </TableBody>
                </Table>}
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
                <Typography gutterBottom variant="h5" component="div">{t($ => $.page.salut.contexts.title)}</Typography>
                {!contexts?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                    {t($ => $.page.salut.contexts.noInfo)}
                </Typography>}
                {contexts?.length && <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>{t($ => $.page.salut.contexts.column.codi)}</TableCell>
                            <TableCell>{t($ => $.page.salut.contexts.column.nom)}</TableCell>
                            <TableCell>{t($ => $.page.salut.contexts.column.path)}</TableCell>
                            <TableCell>{t($ => $.page.salut.contexts.column.api)}</TableCell>
                            <TableCell>{t($ => $.page.salut.contexts.column.manuals)}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {contexts.map((s, key: number) => <TableRow key={key}>
                            <TableCell>{s.codi}</TableCell>
                            <TableCell>{s.nom}</TableCell>
                            <TableCell>{s.path && <Button href={s.path} target="_blank" rel="noopener noreferrer" sx={{textTransform: 'none'}}>{s.path}</Button>}</TableCell>
                            <TableCell>{s.api && <Button href={s.api} target="_blank" rel="noopener noreferrer" sx={{textTransform: 'none'}}>API</Button>}</TableCell>
                            <TableCell>
                                {s.manuals && s.manuals.map((manual, index: number) => (
                                    <Button key={index} href={manual.path ?? ""} target="_blank" rel="noopener noreferrer" sx={{textTransform: 'none'}}>{manual.nom}</Button>
                                ))}
                            </TableCell>
                        </TableRow>)}
                    </TableBody>
                </Table>}
            </CardContent>
        </Card>
    );
};

const Missatges: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
    const { t } = useTranslation();
    const missatges = salutCurrentApp.missatges;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">{t($ => $.page.salut.missatges.title)}</Typography>
                {!missatges?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                    {t($ => $.page.salut.missatges.noInfo)}
                </Typography>}
                {missatges?.length && <Table size="small">
                    <TableHead>
                        <TableRow>
                            <TableCell>{t($ => $.page.salut.missatges.column.data)}</TableCell>
                            <TableCell>{t($ => $.page.salut.missatges.column.nivell)}</TableCell>
                            <TableCell>{t($ => $.page.salut.missatges.column.missatge)}</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {missatges.map((m: any, key: number) => <TableRow key={key}>
                            <TableCell><Typography>{dateFormatLocale(m.data, true)}</Typography></TableCell>
                            <TableCell><Chip label={m.nivell} size="small" sx={{backgroundColor: getColorByNivellEnum(m.nivell as NivellEnum), color: 'white'}} /></TableCell>
                            <TableCell>{m.missatge}</TableCell>
                        </TableRow>)}
                    </TableBody>
                </Table>}
            </CardContent>
        </Card>
    );
};

const DetallInfo: React.FC<{ salutCurrentApp: SalutModel }> = ({ salutCurrentApp }) => {
    const { t } = useTranslation();
    const detalls = salutCurrentApp.detalls;
    return (
        <Card variant="outlined" sx={{ height: '100%' }}>
            <CardContent>
                <Typography gutterBottom variant="h5" component="div">{t($ => $.page.salut.detalls.title)}</Typography>
                {!detalls?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                    {t($ => $.page.salut.detalls.noInfo)}
                </Typography>}
                {detalls?.length && <Table size="small" sx={{ width: '100%', tableLayout: 'fixed'}}>
                    <TableBody>
                        {detalls.map((d) => <TableRow key={d.id}>
                            <TableCell sx={{minWidth: '165px'}}>{d.nom}</TableCell>
                            <TableCell>{d.valor}</TableCell>
                        </TableRow>)}
                    </TableBody>
                </Table>}
            </CardContent>
        </Card>
    );
};

const SalutAppInfo: React.FC<{
    appInfoData: AppDataState;
    ready: boolean;
    grupsDates?: string[];
}> = ({ appInfoData, grupsDates, ready }) => {
    const { t } = useTranslation();
    const { salutCurrentApp, entornApp, loading, agrupacio, estats, latencies } = appInfoData;
    const [integracionsExpandState, setIntegracionsExpandState] = React.useState<number[]>([]);
    const toggleIntegracioExpand = (id: number) => {
        if (integracionsExpandState.includes(id))
            setIntegracionsExpandState(integracionsExpandState.filter((i: number) => i !== id));
        else setIntegracionsExpandState([...integracionsExpandState, id]);
    };
    const dataLoaded = ready && loading != null && !loading;

    if (dataLoaded && salutCurrentApp == null)
        return <Alert severity="warning">{t($ => $.page.salut.info.noInfo)}</Alert>;

    if (dataLoaded && salutCurrentApp?.peticioError)
        return (
            <>
                <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid size={{ sm: 12, lg: 3 }}>
                        <AppInfo salutCurrentApp={salutCurrentApp} entornApp={entornApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 9 }}>
                        <ErrorBoundary fallback={<SalutErrorBoundaryFallback />}>
                            {agrupacio != null && estats != null && grupsDates != null && (
                                <EstatsBarCard
                                    agrupacio={agrupacio}
                                    estats={estats}
                                    grupsDates={grupsDates}
                                />
                            )}
                        </ErrorBoundary>
                    </Grid>
                </Grid>
                <Alert severity="error">{t($ => $.page.salut.info.downAlert)}</Alert>
            </>
        );

    if (salutCurrentApp == null || entornApp == null)
        return (
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
        );

    return (
        <Grid container spacing={2}>
            <Grid size={{ sm: 12, lg: 3 }}>
                <AppInfo salutCurrentApp={salutCurrentApp} entornApp={entornApp} />
            </Grid>
            <Grid size={{ sm: 12, lg: 9 }}>
                <ErrorBoundary fallback={<SalutErrorBoundaryFallback />}>
                    {agrupacio != null && estats != null && grupsDates != null && (
                        <EstatsBarCard
                            agrupacio={agrupacio}
                            estats={estats}
                            grupsDates={grupsDates}
                        />
                    )}
                </ErrorBoundary>
            </Grid>
            {salutCurrentApp?.peticioError ? (
                <Grid>
                    <Alert severity="error">{t($ => $.page.salut.info.downAlert)}</Alert>
                </Grid>
            ) : (
                <>
                    <Grid size={{ sm: 12, lg: 3 }}>
                        <DetallInfo salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 9 }}>
                        <ErrorBoundary fallback={<SalutErrorBoundaryFallback />}>
                            {agrupacio != null &&
                                latencies != null &&
                                estats != null &&
                                grupsDates != null && (
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
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Integracions
                            salutCurrentApp={salutCurrentApp}
                            toggleIntegracioExpand={toggleIntegracioExpand}
                            integracionsExpandState={integracionsExpandState}
                        />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Subsistemes salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Contexts salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Missatges salutCurrentApp={salutCurrentApp} />
                    </Grid>
                </>
            )}
        </Grid>
    );
};

export default SalutAppInfo;
