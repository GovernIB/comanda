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
    useBaseAppContext,
} from 'reactlib';
import SalutToolbar from '../components/SalutToolbar';
import { calculateEstatsSeries, getEstatsMaxData } from '../components/UpdownBarChart';
import { generateDataGroups, isDataInGroup, toXAxisDataGroups } from '../util/dataGroup';
import { ErrorBoundary } from 'react-error-boundary';
import {
    BarPlot,
    BarSeriesType,
    ChartsLegend,
    ChartsXAxis,
    ChartsYAxis,
    LinePlot,
    LineSeriesType,
    ChartContainer,
} from '@mui/x-charts';
import { useTheme } from '@mui/material/styles';

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
    salutCurrentApp: any;
    reportParams: any;
}

const appDataStateInitialValue = {
    loading: null,
    entornApp: null,
    estats: null,
    latencies: null,
    salutCurrentApp: null,
    reportParams: null,
};

const useAppEstatLabel = () => {
  const { t } = useTranslation();

  return (estat?: string) => {
    switch (estat) {
      case 'UP':
        return t('enum.appEstat.UP.title');
      case 'WARN':
        return t('enum.appEstat.WARN');
      case 'DOWN':
        return t('enum.appEstat.DOWN');
      case 'DEGRADED':
        return t('enum.appEstat.DEGRADED');
      case 'MAINTENANCE':
        return t('enum.appEstat.MAINTENANCE');
      case 'UNKNOWN':
        return t('enum.appEstat.UNKNOWN');
      default:
        return estat;
    }
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
                const salutCurrentApp = rows?.[0];
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

const AppInfo: React.FC<any> = (props) => {
    const {
        salutCurrentApp: app,
        entornApp: entornApp,
    } = props;
    const { t } = useTranslation();
    const getAppEstatLabel = useAppEstatLabel();
    const revisio = entornApp && <Typography>{entornApp.revisioSimplificat}</Typography>;
    const jdk = entornApp && <Typography>{entornApp.jdkVersion}</Typography>;
    const data = app && <Typography>{dateFormatLocale(app.data, true)}</Typography>;
    const bdEstat = app && <Typography><Chip label={getAppEstatLabel(app.bdEstat)} size="small" color={app.bdEstat === 'UP' ? 'success' : 'error'} /></Typography>;
    const appLatencia = app && <Typography>{app.appLatencia != null ? app.appLatencia + ' ms' : t('page.salut.nd')}</Typography>;
    const missatges = app && <>
        <Chip label={app.missatgeErrorCount} size="small" color="error" />&nbsp;/&nbsp;
        <Chip label={app.missatgeWarnCount} size="small" color="warning" />&nbsp;/&nbsp;
        <Chip label={app.missatgeInfoCount} size="small" color="info" />
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

const LatenciaEstatsChart: React.FC<any> = (props) => {
    const { dataInici, agrupacio, latencies, estats } = props;
    const { t } = useTranslation();
    const theme = useTheme();

    if (latencies.length === 0)
        return (
            <Card variant="outlined" sx={{ height: '300px' }}>
                <CardContent sx={{ height: '100%' }}>
                    <Typography gutterBottom variant="h5" component="div">{t('page.salut.estatLatencia.title')}</Typography>
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

    const latenciaMaxValue = latencies.map((latencia: any) => latencia.latenciaMitja).reduce((accumulator: any, currentValue: any) => {
        return Math.max(accumulator, currentValue ?? null); // Si uno de los dos parámetros de Math.max es undefined, devuelve NaN
    }, latencies[0].latenciaMitja);

    const mapPercentToLatenciaMaxValue = (percent: number) => (percent / 100) * latenciaMaxValue*1.5;

    const estatsMaxData = getEstatsMaxData(estats);
    const baseDataGroups = generateDataGroups(dataInici, estatsMaxData, agrupacio);
    const seriesUp = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "upPercent").map(mapPercentToLatenciaMaxValue);
    const seriesWarn = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "warnPercent").map(mapPercentToLatenciaMaxValue);
    const seriesDegraded = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "degradedPercent").map(mapPercentToLatenciaMaxValue);
    const seriesMaintenance = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "maintenancePercent").map(mapPercentToLatenciaMaxValue);
    const seriesDown = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "downPercent").map(mapPercentToLatenciaMaxValue);
    const seriesUnknown = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "unknownPercent").map(mapPercentToLatenciaMaxValue);

    const seriesDataLatencia = baseDataGroups.map((group) => {
        return latencies.find((latenciaData: any) => {
            if (!latenciaData || !latenciaData.data) return false;
            return isDataInGroup(latenciaData.data, group, agrupacio);
        })?.latenciaMitja;
    });

    const barSeries: BarSeriesType[] = [
        {
            data: seriesUp,
            label: 'up',
            stack: 'total',
            color: theme.palette.success.main,
            type: 'bar',
        },
        {
            data: seriesWarn,
            label: 'warn',
            stack: 'total',
            color: theme.palette.warning.light,
            type: 'bar',
        },
        {
            data: seriesDegraded,
            label: 'degraded',
            stack: 'total',
            color: theme.palette.warning.dark,
            type: 'bar',
        },
        {
            data: seriesMaintenance,
            label: 'maintenance',
            stack: 'total',
            color: theme.palette.primary.main,
            type: 'bar',
        },
        {
            data: seriesDown,
            label: 'down',
            stack: 'total',
            color: theme.palette.error.main,
            type: 'bar',
        },
        {
            data: seriesUnknown,
            label: 'unknown',
            stack: 'total',
            color: theme.palette.grey[600],
            type: 'bar',
        }
    ];
    const lineSeries: LineSeriesType[] = [
        { data: seriesDataLatencia, type: 'line', showMark: true },
    ];
    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);

    // TODO Añadir efecto "hover" para ver el número exacto de ms para la latencia
    return (
        <Card variant="outlined" sx={{ height: '300px' }}>
            <CardContent sx={{ height: '100%' }}>
                <Typography gutterBottom variant="h5" component="div">
                    {t('page.salut.estatLatencia.title')}
                </Typography>
                <ChartContainer
                    series={[...lineSeries, ...barSeries]}
                    xAxis={[{ scaleType: 'band', data: dataGroups, id: 'latenciaEstat-x-axis-id', }]}
                    yAxis={[{ label: ' ms', id: 'latenciaEstat-y-axis-id', }]}
                >
                    <ChartsLegend />
                    <BarPlot />
                    <LinePlot />
                    <MarkPlot />
                    <ChartsYAxis axisId="latenciaEstat-y-axis-id" />
                    <ChartsXAxis axisId="latenciaEstat-x-axis-id" />
                </ChartContainer>
            </CardContent>
        </Card>
    );
}

const Integracions: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const integracions = salutCurrentApp?.integracions;
    const getEstatColor = (estat: string) => {
        switch (estat) {
            case 'UP': return 'success';
            case 'DOWN': return 'error';
            case 'UNKNOWN': return 'warning';
        }
    }
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
                            <Chip label={getAppEstatLabel(i.estat)} size="small" color={getEstatColor(i.estat)} />
                        </TableCell>
                        <TableCell>{i.latencia != null ? i.latencia + ' ms' : t('page.salut.nd')}</TableCell>
                        <TableCell>
                            <Chip label={i.totalOk} size="small" color={"success"} sx={{ minWidth: '35px', textAlign: 'center', mr:1 }} />
                            <Chip label={i.totalError} size="small" color={"error"} sx={{ minWidth: '35px', textAlign: 'center' }} />
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
                            <Chip label={getAppEstatLabel(s.estat)} size="small" color={s.estat === 'UP' ? 'success' : 'error'} />
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
    const getAppEstatLabel = useAppEstatLabel();
    const dataLoaded = ready && loading != null && !loading;
    const toolbarState = salutCurrentApp?.appEstat ? <Chip
        label={getAppEstatLabel(salutCurrentApp.appEstat)}
        size="small"
        color={salutCurrentApp.appEstat === 'UP' ? 'success' : 'error'}
        sx={{ ml: 1 }} /> : undefined;
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
                            {reportParams != null && latencies != null && estats != null && (
                                <LatenciaEstatsChart
                                    dataInici={reportParams.dataInici}
                                    agrupacio={reportParams.agrupacio}
                                    latencies={latencies}
                                    estats={estats}
                                />
                            )}
                        </ErrorBoundary>
                    </Grid>
                    <Grid size={{ sm: 12, lg: 3 }}>
                        <DetallInfo salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 9 }}>
                        <Contexts salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Integracions salutCurrentApp={salutCurrentApp} />
                    </Grid>
                    <Grid size={{ sm: 12, lg: 6 }}>
                        <Subsistemes salutCurrentApp={salutCurrentApp} />
                    </Grid>
                </Grid>
            )}
        </BasePage>
    );
}

export default SalutAppInfo;
