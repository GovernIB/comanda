import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
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
import { MarkPlot } from '@mui/x-charts/LineChart';
import {
    BasePage,
    useResourceApiService,
    dateFormatLocale,
} from 'reactlib';
import SalutToolbar from '../components/SalutToolbar';
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
}

const appDataStateInitialValue: AppDataState = {
    loading: null,
    entornApp: null,
    estats: null,
    latencies: null,
    salutCurrentApp: null,
    reportParams: null,
};

const useAppEstatLabel = () => {
  const { t } = useTranslation();

  return (estat?: SalutEstatEnum) => {
      return t(ENUM_APP_ESTAT_PREFIX + estat + TITLE, { defaultValue: estat });
  };
};

const useAppData = (id: any) => {
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
    const refresh = (dataInici: string, dataFi: string, agrupacio: string) => {
        const reportParams = {
            dataInici,
            dataFi,
            agrupacio,
        };
        if (ready) {
            setAppDataState((prevState) => ({
                ...prevState,
                loading: true,
            }));
            (async function () {
                const entornApp = await entornAppGetOne(id);
                const entornAppId = entornApp.id;
                const reportData = {
                    ...reportParams,
                    entornAppId,
                };
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
                }));
            })();
        }
    }
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
    const { dataInici, agrupacio, latencies, estats } = props;
    const { t } = useTranslation();

    if (!latencies || latencies.length === 0)
        return (
            <Card variant="outlined" sx={{ height: '300px' }}>
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
            </Card>
        );

    const estatsMaxData = getEstatsMaxData(estats);
    const baseDataGroups = generateDataGroups(dataInici, estatsMaxData, agrupacio);

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

    return (
        <Card variant="outlined" sx={{ height: '300px' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">
                    {t('page.salut.latencia.title')}
                </Typography>
                <ChartContainer
                    series={[...lineSeries]}
                    xAxis={[{ scaleType: 'band', data: dataGroups, id: 'latencia-x-axis-id', }]}
                    yAxis={[{ label: ' ms', id: 'latencia-y-axis-id', }]}
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
}

const EstatsBarCard: React.FC<any> = (props) => {
    const { dataInici, agrupacio, estats } = props;
    const { t } = useTranslation();

    const hasData = estats && Object.keys(estats).length > 0;

    return (
        <Card variant="outlined" sx={{ height: '300px' }}>
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
                    <UpdownBarChart dataInici={dataInici} agrupacio={agrupacio} estats={estats} />
                )}
            </CardContent>
        </Card>
    );
}

const Integracions: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const integracions = salutCurrentApp?.integracions;
    const getAppEstatLabel = useAppEstatLabel();
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.integracions.title')}</Typography>
            {!integracions?.length && <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                {t('page.salut.integracions.noInfo')}
            </Typography>}
            {integracions?.length > 0 && <Table size="small">
                <TableHead>
                    <TableRow>
                        {/*<TableCell>{t('page.salut.integracions.column.codi')}</TableCell>*/}
                        <TableCell sx={{width: '50px'}}></TableCell>
                        <TableCell>{t('page.salut.integracions.column.nom')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.estat')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.latencia')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.peticions')}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {integracions.map((i: any, key: number) => <TableRow key={key}>
                        {/*<TableCell>{i.codi}</TableCell>*/}
                        <TableCell sx={{width: '50px'}}>{i.logo && <img src={`data:image/png;base64,${i.logo}`} alt="logo" style={{ maxHeight: '32px' }}/>}</TableCell>
                        <TableCell>{i.nom}</TableCell>
                        <TableCell>
                            <Chip label={getAppEstatLabel(i.estat)} size="small" sx={{backgroundColor: getColorByStatEnum(i.estat as SalutEstatEnum), color: 'white'}}/>
                        </TableCell>
                        <TableCell>{i.latencia != null ? i.latencia + ' ms' : t('page.salut.nd')}</TableCell>
                        <TableCell>
                            <Chip label={i.totalOk} size="small" sx={{ minWidth: '35px', textAlign: 'center', mr:1, backgroundColor: getColorByStatEnum(i.estat as SalutEstatEnum), color: 'white' }} />
                            <Chip label={i.totalError} size="small" sx={{ minWidth: '35px', textAlign: 'center', backgroundColor: getColorByStatEnum(i.estat as SalutEstatEnum), color: 'white' }} />
                        </TableCell>
                    </TableRow>)}
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
                        <TableCell>{t('page.salut.subsistemes.column.latencia')}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {subsistemes.map((s: any, key: number) => <TableRow key={key}>
                        <TableCell>{s.codi}</TableCell>
                        <TableCell>{s.nom}</TableCell>
                        <TableCell>
                            <Chip label={getAppEstatLabel(s.estat)} size="small" sx={{backgroundColor: getColorByStatEnum(s.estat as SalutEstatEnum), color: 'white'}} />
                        </TableCell>
                        <TableCell>{s.latencia} ms</TableCell>
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

const SalutAppInfo: React.FC = () => {
    const { id } = useParams();
    const {
        ready,
        loading,
        refresh: entornAppDataRefresh,
        entornApp,
        estats,
        latencies,
        salutCurrentApp,
        reportParams,
    } = useAppData(id);
    const dataLoaded = ready && loading != null && !loading;
    const toolbarState = salutCurrentApp?.appEstat
        ? <ItemStateChip
            sx={{ ml: 1 }}
            salutField={SalutModel.APP_ESTAT}
            salutStatEnum={salutCurrentApp.appEstat} />
        : undefined;
    const toolbar = <SalutToolbar
        title={entornApp != null ? `${entornApp.app.description} - ${entornApp.entorn.description}` : ""}
        subtitle={entornApp?.versio ? 'v' + entornApp?.versio : undefined}
        hideFilter
        state={toolbarState}
        ready={ready}
        onRefresh={entornAppDataRefresh}
        goBackActive
        appDataLoading={!dataLoaded}
    />;
    return (
        <BasePage toolbar={toolbar}>
            {(salutCurrentApp == null || entornApp == null) ? (
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
                    <Grid size={{ sm: 12, lg: 3 }}>
                        <AppInfo salutCurrentApp={salutCurrentApp} entornApp={entornApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 9 }}>
                        <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                            {reportParams != null && estats != null && (
                                <EstatsBarCard
                                    dataInici={reportParams.dataInici}
                                    agrupacio={reportParams.agrupacio}
                                    estats={estats}
                                />
                            )}
                        </ErrorBoundary>
                    </Grid>
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
                </Grid>
            )}
        </BasePage>
    );
}

export default SalutAppInfo;
