import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Collapse from '@mui/material/Collapse';
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
import {
    useResourceApiService,
    dateFormatLocale,
} from 'reactlib';
import { toReportInterval } from '../components/SalutToolbar';
import UpdownBarChart, { getEstatsMaxData } from '../components/UpdownBarChart';
import { generateDataGroups, isDataInGroup, toXAxisDataGroups } from '../util/dataGroup';
import { ErrorBoundary } from 'react-error-boundary';
import {
    ChartsXAxis,
    ChartsYAxis,
    LinePlot,
    LineSeriesType,
    ChartContainer,
    ChartsTooltip,
} from '@mui/x-charts';
import {ENUM_APP_ESTAT_PREFIX, getColorByMissatge, getColorByNivellEnum, getColorByStatEnum, NivellEnum, SalutEstatEnum, SalutModel, TITLE} from "../types/salut.model.tsx";
import {ChipColor} from "../util/colorUtil.ts";
import {SalutGenericTooltip} from "../components/SalutChipTooltip.tsx";
import {ItemStateChip} from "../components/SalutItemStateChip.tsx";
import { Alert } from '@mui/material';
import { useCallback, useEffect } from 'react';

export const ErrorBoundaryFallback = () => {
    const { t } = useTranslation();
    return <Typography sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
    }} color="error">{t('page.salut.latencia.error')}</Typography>
}

interface AppDataState {
    loading: boolean | null; // Null indica que no se ha hecho ninguna petición aún
    entornApp: any;
    estats: Record<string, any> | null;
    latencies: any[] | null;
    salutCurrentApp: SalutModel | null;
    reportParams: any;
    error?: any;
    grupsDates: string[] | null;
}

const appDataStateInitialValue: AppDataState = {
    loading: null,
    entornApp: null,
    estats: null,
    latencies: null,
    salutCurrentApp: null,
    reportParams: null,
    grupsDates: null,
};

const useAppEstatLabel = () => {
  const { t } = useTranslation();

  return (estat?: SalutEstatEnum) => {
      return t(ENUM_APP_ESTAT_PREFIX + estat + TITLE, { defaultValue: estat });
  };
};

export const useAppInfoData = (id: any, dataRangeMinutes: number ) => {
    const {
        isReady: entornAppApiIsReady,
        getOne: entornAppGetOne,
    } = useResourceApiService('entornApp');
    const {
        isReady: salutApiIsReady,
        find: salutApiFind,
        artifactReport: salutApiReport,
    } = useResourceApiService('salut');
    const [appDataState, setAppDataState] = React.useState<AppDataState>(appDataStateInitialValue);
    const ready = entornAppApiIsReady && salutApiIsReady;
    // TODO Considerar implementar bloqueig o cancelar peticions antigues si se fa una nova
    const refresh = useCallback(async () => {
        if (id == null) {
            setAppDataState(appDataStateInitialValue);
            return;
        }

        const reportParams = toReportInterval(dataRangeMinutes);
        if (ready) {
            setAppDataState((prevState) => ({
                ...prevState,
                loading: true,
                error: undefined,
            }));
            try {
                const entornApp = await entornAppGetOne(id);
                const entornAppId = entornApp.id;
                const reportData = {
                    ...reportParams,
                    entornAppId,
                };
                const grupDatesReportItems = await salutApiReport(null, {
                    code: 'grups_dates',
                    data: {
                        dataReferencia: reportData.dataFi,
                        agrupacio: reportData.agrupacio,
                    },
                });
                const estatReportItems = await salutApiReport(null, {
                    code: 'estat',
                    data: reportData,
                });
                const latenciaReportItems = await salutApiReport(null, {
                    code: 'latencia',
                    data: reportData,
                });
                const findArgs = {
                    page: 0,
                    size: 1,
                    sorts: ['data,desc'],
                    perspectives: [
                        'SAL_INTEGRACIONS',
                        'SAL_SUBSISTEMES',
                        'SAL_CONTEXTS',
                        'SAL_MISSATGES',
                        'SAL_DETALLS',
                    ],
                    filter: 'entornAppId : ' + entornAppId,
                };
                const { rows } = await salutApiFind(findArgs);
                const salutCurrentApp: SalutModel = rows?.[0];
                setAppDataState((state) => ({
                    ...state,
                    loading: false,
                    entornApp,
                    estats: { [entornAppId]: estatReportItems },
                    latencies: latenciaReportItems as any[],
                    salutCurrentApp,
                    reportParams,
                    grupsDates: (grupDatesReportItems as { data: string }[]).map((item) => item.data),
                }));
            } catch (e) {
                // TODO Mostrar error en la UI
                setAppDataState({
                    ...appDataStateInitialValue,
                    loading: false,
                    error: e,
                })
            }
        }
    }, [dataRangeMinutes, ready, entornAppGetOne, id, salutApiReport, salutApiFind]);

    useEffect(() => {
        if (!ready) {
            return;
        }
        refresh();
    }, [ready, refresh]);

    return {
        ready,
        refresh,
        ...appDataState,
    };
}

const AppInfo: React.FC<any> = (props: {salutCurrentApp: SalutModel, entornApp: any}) => {
    const {
        salutCurrentApp: app,
        entornApp: entornApp,
    } = props;
    const { t } = useTranslation();
    const revisio = entornApp && <Typography>{entornApp.revisioSimplificat}</Typography>;
    const jdk = entornApp && <Typography>{entornApp.jdkVersion}</Typography>;
    const data = app && <Typography>{dateFormatLocale(app.data, true)}</Typography>;
    const bdEstat = app && <ItemStateChip salutField={SalutModel.BD_ESTAT} salutStatEnum={app.bdEstat} />;
    const appLatencia = app && <Typography>{app.appLatencia != null ? app.appLatencia + ' ms' : t('page.salut.nd')}</Typography>;
    const missatges = app && <>
        <SalutGenericTooltip title={t('page.salut.msgs.missatgeErrorCount')}>
            <Chip
                sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_ERROR_COUNT), color: ChipColor.WHITE }}
                label={app.missatgeErrorCount}
                size="small"
            />
        </SalutGenericTooltip>
        &nbsp;/&nbsp;
        <SalutGenericTooltip title={t('page.salut.msgs.missatgeWarnCount')}>
            <Chip
                sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_WARN_COUNT), color: ChipColor.WHITE }}
                label={app.missatgeWarnCount}
                size="small"
            />
        </SalutGenericTooltip>
        &nbsp;/&nbsp;
        <SalutGenericTooltip title={t('page.salut.msgs.missatgeInfoCount')}>
            <Chip
                sx={{ bgcolor: getColorByMissatge(SalutModel.MISSATGE_INFO_COUNT), color: ChipColor.WHITE }}
                label={app.missatgeInfoCount}
                size="small"
            />
        </SalutGenericTooltip>
    </>;
    return <Card variant="outlined" sx={{ height: '300px' }}>
        <CardContent sx={{ height: '100%' }}>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.info.title')}</Typography>
            <Table size="small">
                <TableBody>
                    <TableRow key={1}>
                        <TableCell>{t('page.salut.info.revisio')}</TableCell>
                        <TableCell>{revisio}</TableCell>
                    </TableRow>
                    <TableRow key={2}>
                        <TableCell>{t('page.salut.info.jdk.versio')}</TableCell>
                        <TableCell>{jdk}</TableCell>
                    </TableRow>
                    <TableRow key={3}>
                        <TableCell>{t('page.salut.info.data')}</TableCell>
                        <TableCell>{data}</TableCell>
                    </TableRow>
                    <TableRow key={4}>
                        <TableCell>{t('page.salut.info.bdEstat')}</TableCell>
                        <TableCell>{bdEstat}</TableCell>
                    </TableRow>
                    <TableRow key={5}>
                        <TableCell>{t('page.salut.info.appLatencia')}</TableCell>
                        <TableCell>{appLatencia}</TableCell>
                    </TableRow>
                    <TableRow key={6}>
                        <TableCell>{t('page.salut.info.missatges')}</TableCell>
                        <TableCell>{missatges}</TableCell>
                    </TableRow>
                </TableBody>
            </Table>
        </CardContent>
    </Card>;
}

const LatenciaLineChart: React.FC<any> = (props) => {
    const { dataInici, agrupacio, latencies, estats, grupsDates } = props;
    const { t } = useTranslation();
    if (!latencies || latencies.length === 0) {
        return <Card variant="outlined" sx={{ height: '300px' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">{t('page.salut.latencia.title')}</Typography>
                <Typography
                    sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'center',
                        alignItems: 'center',
                    }}
                >
                    {t('page.salut.estatLatencia.noInfo')}
                </Typography>
            </CardContent>
        </Card>;
    }
    const estatsMaxData = getEstatsMaxData(estats);
    // const baseDataGroups = generateDataGroups(dataInici, estatsMaxData, agrupacio);
    const baseDataGroups = grupsDates;
    const seriesDataLatencia = baseDataGroups.map((group) => {
        return latencies.find((latenciaData: any) => {
            if (!latenciaData || !latenciaData.data) return false;
            return isDataInGroup(latenciaData.data, group, agrupacio);
        })?.latenciaMitja;
    });
    const lineSeries: LineSeriesType[] = [
        {
            data: seriesDataLatencia,
            type: 'line',
            showMark: true,
            valueFormatter: (v) => (v == null ? '' : `${v} ms`),
        },
    ];
    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    return <Card variant="outlined" sx={{ height: '300px' }}>
        <CardContent sx={{ height: '100%' }}>
            <Typography gutterBottom variant="h5" component="div">
                {t('page.salut.latencia.title')}
            </Typography>
            <ChartContainer
                series={[...lineSeries]}
                xAxis={[{
                    scaleType: 'band',
                    data: dataGroups,
                    id: 'latencia-x-axis-id',
                    // TODO Fer un formatter generic per a totes les agrupacions
                    valueFormatter: (value: string) => agrupacio === 'HORA' ? value.substring(3) : value
                }]}
                yAxis={[{ label: ' ms', id: 'latencia-y-axis-id', }]}>
                <LinePlot />
                <MarkPlot />
                <ChartsTooltip />
                <ChartsYAxis axisId="latencia-y-axis-id" />
                <ChartsXAxis axisId="latencia-x-axis-id" />
            </ChartContainer>
        </CardContent>
    </Card>;
}

const EstatsBarCard: React.FC<any> = (props) => {
    const { grupsDates, dataInici, agrupacio, estats } = props;
    const { t } = useTranslation();
    const hasData = estats && Object.keys(estats).length > 0;
    return <Card variant="outlined" sx={{ height: '300px' }}>
        <CardContent sx={{ height: '100%' }}>
            <Typography gutterBottom variant="h5" component="div">
                {t('page.salut.estats.title')}
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
                    {t('page.salut.estatLatencia.noInfo')}
                </Typography>
            ) : (
                <UpdownBarChart dataInici={dataInici} agrupacio={agrupacio} estats={estats} grupsDates={grupsDates} />
            )}
        </CardContent>
    </Card>;
}

const PeticionsOkError: React.FC<any> = (props) => {
    const { ok, error } = props;
    const theme = useTheme();
    return <>
        <span style={{ color: theme.palette.success.main }}>{ok ?? 0}</span>
        &nbsp;/&nbsp;
        <span style={{ color: theme.palette.error.main }}>{error ?? 0}</span>
    </>
}

const IntegracioRow: React.FC<any> = (props) => {
    const { integracio, fills, key } = props;
    const { t } = useTranslation();
    const getAppEstatLabel = useAppEstatLabel();
    const [open, setOpen] = React.useState<boolean>(false);
    return <>
        <TableRow key={key}>
            <TableCell sx={{width: '50px'}}>
                {fills?.length ? <IconButton size="small" onClick={() => setOpen(!open)}>
                    {open ? <Icon>keyboard_arrow_up</Icon> : <Icon>keyboard_arrow_down</Icon>}
                </IconButton> : null}
                {integracio.logo && <img src={`data:image/png;base64,${integracio.logo}`} alt="logo" style={{ maxHeight: '32px' }}/>}
            </TableCell>
            <TableCell>{integracio.nom}</TableCell>
            <TableCell>
                <Chip label={getAppEstatLabel(integracio.estat)} size="small" sx={{backgroundColor: getColorByStatEnum(integracio.estat as SalutEstatEnum), color: 'white'}}/>
            </TableCell>
            <TableCell>
                <PeticionsOkError ok={integracio.totalOk} error={integracio.totalError} />
            </TableCell>
            <TableCell>{integracio.totalTempsMig != null ? integracio.totalTempsMig + ' ms' : t('page.salut.nd')}</TableCell>
            <TableCell>
                <PeticionsOkError ok={integracio.peticionsOkUltimPeriode} error={integracio.peticionsErrorUltimPeriode} />
            </TableCell>
            <TableCell>{integracio.tempsMigUltimPeriode != null ? integracio.tempsMigUltimPeriode + ' ms' : t('page.salut.nd')}</TableCell>
            <TableCell>
                {integracio.endpoint && <IconButton
                    component="a"
                    href={integracio.endpoint}
                    target="_blank"
                    size="small">
                        <Icon>launch</Icon>
                </IconButton>}
            </TableCell>
        </TableRow>
        {fills?.length ? <TableRow>
            <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={6}>
                <Collapse in={open} timeout="auto" unmountOnExit>
                    {fills.map((f: any, key2: number) => <IntegracioRow
                        integracio={f}
                        key={key + '_' + key2} />)}
                </Collapse>
            </TableCell>
        </TableRow> : null}
    </>;
}

const Integracions: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const integracions = salutCurrentApp?.integracions;
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.integracions.title')}</Typography>
            {!integracions?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                {t('page.salut.integracions.noInfo')}
            </Typography>}
            {integracions?.length > 0 && <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell sx={{width: '50px'}}></TableCell>
                        <TableCell>{t('page.salut.integracions.column.nom')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.estat')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.peticionsTotals')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.tempsMigTotal')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.peticionsPeriode')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.tempsMigPeriode')}</TableCell>
                        <TableCell></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {integracions.filter((i: any) => i.pare == null).map((i: any, key: number) => <IntegracioRow
                    integracio={i}
                    fills={integracions.filter((i2: any) => i2.pare?.id === i.id)}
                    key={key} />)}
                </TableBody>
            </Table>}
        </CardContent>
    </Card>;
}

const Subsistemes: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const subsistemes = salutCurrentApp?.subsistemes;
    const getAppEstatLabel = useAppEstatLabel();
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.subsistemes.title')}</Typography>
            {!subsistemes?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                {t('page.salut.subsistemes.noInfo')}
            </Typography>}
            {subsistemes?.length > 0 && <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>{t('page.salut.subsistemes.column.codi')}</TableCell>
                        <TableCell>{t('page.salut.subsistemes.column.nom')}</TableCell>
                        <TableCell>{t('page.salut.subsistemes.column.estat')}</TableCell>
                        <TableCell>{t('page.salut.subsistemes.column.peticionsTotals')}</TableCell>
                        <TableCell>{t('page.salut.subsistemes.column.tempsMigTotal')}</TableCell>
                        <TableCell>{t('page.salut.subsistemes.column.peticionsPeriode')}</TableCell>
                        <TableCell>{t('page.salut.subsistemes.column.tempsMigPeriode')}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {subsistemes.map((s: any, key: number) => <TableRow key={key}>
                        <TableCell>{s.codi}</TableCell>
                        <TableCell>{s.nom}</TableCell>
                        <TableCell>
                            <Chip label={getAppEstatLabel(s.estat)} size="small" sx={{backgroundColor: getColorByStatEnum(s.estat as SalutEstatEnum), color: 'white'}} />
                        </TableCell>
                        <TableCell>
                            <PeticionsOkError ok={s.totalOk} error={s.totalError} />
                        </TableCell>
                        <TableCell>{s.totalTempsMig != null ? s.totalTempsMig + ' ms' : t('page.salut.nd')}</TableCell>
                        <TableCell>
                            <PeticionsOkError ok={s.peticionsOkUltimPeriode} error={s.peticionsErrorUltimPeriode} />
                        </TableCell>
                        <TableCell>{s.tempsMigUltimPeriode != null ? s.tempsMigUltimPeriode + ' ms' : t('page.salut.nd')}</TableCell>
                    </TableRow>)}
                </TableBody>
            </Table>}
        </CardContent>
    </Card>;
}

const Contexts: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const contexts = salutCurrentApp?.contexts;
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.contexts.title')}</Typography>
            {!contexts?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                {t('page.salut.contexts.noInfo')}
            </Typography>}
            {contexts?.length > 0 && <Table size="small">
            <TableHead>
                    <TableRow>
                        <TableCell>{t('page.salut.contexts.column.codi')}</TableCell>
                        <TableCell>{t('page.salut.contexts.column.nom')}</TableCell>
                        <TableCell>{t('page.salut.contexts.column.path')}</TableCell>
                        <TableCell>{t('page.salut.contexts.column.api')}</TableCell>
                        <TableCell>{t('page.salut.contexts.column.manuals')}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {contexts.map((s: any, key: number) => <TableRow key={key}>
                        <TableCell>{s.codi}</TableCell>
                        <TableCell>{s.nom}</TableCell>
                        <TableCell>{s.path && <Button href={s.path} target="_blank" rel="noopener noreferrer" sx={{textTransform: 'none'}}>{s.path}</Button>}</TableCell>
                        <TableCell>{s.api && <Button href={s.api} target="_blank" rel="noopener noreferrer" sx={{textTransform: 'none'}}>API</Button>}</TableCell>
                        <TableCell>
                            {s.manuals && s.manuals.map((manual: any, index: number) => (
                                <Button key={index} href={manual.path} target="_blank" rel="noopener noreferrer" sx={{textTransform: 'none'}}>{manual.nom}</Button>
                            ))}
                        </TableCell>
                    </TableRow>)}
                </TableBody>
            </Table>}
        </CardContent>
    </Card>;
}

const Missatges: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const missatges = salutCurrentApp?.missatges;
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.missatges.title')}</Typography>
            {!missatges?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                {t('page.salut.missatges.noInfo')}
            </Typography>}
            {missatges?.length > 0 && <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>{t('page.salut.missatges.column.data')}</TableCell>
                        <TableCell>{t('page.salut.missatges.column.nivell')}</TableCell>
                        <TableCell>{t('page.salut.missatges.column.missatge')}</TableCell>
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
    </Card>;
}

const DetallInfo: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const detalls = salutCurrentApp?.detalls;
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.detalls.title')}</Typography>
            {!detalls?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                {t('page.salut.detalls.noInfo')}
            </Typography>}
            {detalls?.length > 0 && <Table size="small">
                <TableBody>
                    {detalls.map((d: any) => <TableRow key={d.id}>
                        <TableCell sx={{minWidth: '165px'}}>{d.nom}</TableCell>
                        <TableCell>{d.valor}</TableCell>
                    </TableRow>)}
                </TableBody>
            </Table>}
        </CardContent>
    </Card>;
}

const SalutAppInfo: React.FC<{ appInfoData: AppDataState; ready: boolean, grupsDates?: string[] }> = ({
    appInfoData,
    grupsDates,
    ready,
}) => {
    const { t } = useTranslation();
    const { salutCurrentApp, entornApp, loading, reportParams, estats, latencies } =
        appInfoData;
    const dataLoaded = ready && loading != null && !loading;

    if (dataLoaded && salutCurrentApp == null)
        return <Alert severity="warning">{t('page.salut.info.noInfo')}</Alert>;

    if (dataLoaded && salutCurrentApp?.peticioError)
        return (
            <>
                <Grid container spacing={2} sx={{ mb: 2 }}>
                    <Grid size={{ sm: 12, lg: 3 }}>
                        <AppInfo salutCurrentApp={salutCurrentApp} entornApp={entornApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 9 }}>
                        <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                            {reportParams != null && estats != null && grupsDates != null && (
                                <EstatsBarCard
                                    dataInici={reportParams.dataInici}
                                    agrupacio={reportParams.agrupacio}
                                    estats={estats}
                                    grupsDates={grupsDates}
                                />
                            )}
                        </ErrorBoundary>
                    </Grid>
                </Grid>
                <Alert severity="error">{t('page.salut.info.downAlert')}</Alert>
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
                <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                    {reportParams != null && estats != null && grupsDates != null && (
                        <EstatsBarCard
                            dataInici={reportParams.dataInici}
                            agrupacio={reportParams.agrupacio}
                            estats={estats}
                            grupsDates={grupsDates}
                        />
                    )}
                </ErrorBoundary>
            </Grid>
            {salutCurrentApp?.peticioError ? (
                <Grid>
                    <Alert severity="error">{t('page.salut.info.downAlert')}</Alert>
                </Grid>
            ) : (
                <>
                    <Grid size={{ sm: 12, lg: 3 }}>
                        <DetallInfo salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 9 }}>
                        <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                            {reportParams != null && latencies != null && estats != null && (
                                <>
                                    <LatenciaLineChart
                                        dataInici={reportParams.dataInici}
                                        agrupacio={reportParams.agrupacio}
                                        latencies={latencies}
                                        estats={estats}
                                        grupsDates={grupsDates}
                                    />
                                </>
                            )}
                        </ErrorBoundary>
                    </Grid>
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Integracions salutCurrentApp={salutCurrentApp} />
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
