import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemText from '@mui/material/ListItemText';
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
    useMuiContentDialog,
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
    return <Typography sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
    }} color="error">Hi ha hagut un error al mostrar el gràfic</Typography>
}

interface AppDataState {
    loading: boolean | null; // Null indica que no se ha hecho ninguna petición aún
    entornApp: any;
    estats: Record<string, any>;
    latencies: Record<string, any>;
    salutCurrentApp: any;
    reportParams: any;
}

const appDataStateInitialValue = {
    loading: null,
    entornApp: null,
    estats: {},
    latencies: {},
    salutCurrentApp: null,
    reportParams: null,
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
            setAppDataState({
                ...appDataStateInitialValue,
                loading: true,
                reportParams,
            });
            let entornAppId: any;
            entornAppGetOne(id).then((entornApp) => {
                setAppDataState((state) => ({
                    ...state,
                    entornApp,
                }))
                entornAppId = entornApp.id;
            }).then(() => {
                const reportData = {
                    ...reportParams,
                    entornAppId,
                };
                return salutApiReport(null, { code: 'estat', data: reportData })
            }).then((items) => {
                setAppDataState((state) => ({
                    ...state,
                    estats: { [entornAppId]: items },
                }))
                const reportData = {
                    ...reportParams,
                    entornAppId
                };
                return salutApiReport(null, { code: 'latencia', data: reportData });
            }).then((items) => {
                setAppDataState((state) => ({
                    ...state,
                    latencies: items,
                }))
                const findArgs = {
                    page: 0,
                    size: 1,
                    sorts: ['data,desc'],
                    perspectives: ['SAL_INTEGRACIONS', 'SAL_SUBSISTEMES', 'SAL_MISSATGES', 'SAL_DETALLS'],
                    filter: 'entornAppId : ' + entornAppId,
                };
                return salutApiFind(findArgs);
            }).then(({ rows }) => {
                const salutCurrentApp = rows?.[0];
                setAppDataState((state) => ({
                    ...state,
                    salutCurrentApp,
                }))
            }).finally(() => {
                setAppDataState((state) => ({
                    ...state,
                    loading: false,
                }))
            });
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
        detailsDialogShow
    } = props;
    const { t } = useTranslation();
    const data = app && <Typography>{dateFormatLocale(app.data, true)}</Typography>;
    const bdEstat = app && <Typography><Chip label={app.bdEstat} size="small" color={app.bdEstat === 'UP' ? 'success' : 'error'} /></Typography>;
    const appLatencia = app && <Typography>{app.appLatencia != null ? app.appLatencia + ' ms' : t('page.salut.nd')}</Typography>;
    const missatges = app && <>
        <Chip label={app.missatgeErrorCount} size="small" color="error" />&nbsp;/&nbsp;
        <Chip label={app.missatgeWarnCount} size="small" color="warning" />&nbsp;/&nbsp;
        <Chip label={app.missatgeInfoCount} size="small" color="info" />
    </>;
    const detalls = app?.detalls;
    const detallsContent = detalls?.length ? <List sx={{ ml: 2 }}>
        {detalls.map((d: any) => <ListItem secondaryAction={d.valor} disablePadding>
            <ListItemText primary={d.nom} sx={{ '& span': { fontWeight: 'bold' } }} />
        </ListItem>)}
    </List> : null;
    const detailsButton = detalls?.length ? <Button
        size="small"
        variant="contained"
        onClick={() => detailsDialogShow(null, detallsContent, undefined, { fullWidth: true, maxWidth: 'md' })}
        sx={{ mt: 1 }}>
        {t('page.salut.info.detalls')}
    </Button> : null;
    return <Card variant="outlined" sx={{ height: '300px' }}>
        <CardContent sx={{ height: '100%' }}>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.info.title')}</Typography>
            <Grid container columnSpacing={1} rowSpacing={1} sx={{ ml: 2 }}>
                <Grid item size={6} >
                    <Typography component="span" sx={{ fontWeight: 'bold' }}>{t('page.salut.info.data')}:</Typography>
                </Grid>
                <Grid item size={6} >
                    <Typography component="span">{data}</Typography>
                </Grid>

                <Grid item size={6} >
                    <Typography component="span" sx={{ fontWeight: 'bold' }}>{t('page.salut.info.bdEstat')}:</Typography>
                </Grid>
                <Grid item size={6} >
                    <Typography component="span">{bdEstat}</Typography>
                </Grid>

                <Grid item size={6} >
                    <Typography component="span" sx={{ fontWeight: 'bold' }}>{t('page.salut.info.appLatencia')}:</Typography>
                </Grid>
                <Grid item size={6} >
                    <Typography component="span">{appLatencia}</Typography>
                </Grid>

                <Grid item size={6} >
                    <Typography component="span" sx={{ fontWeight: 'bold' }}>{t('page.salut.info.missatges')}:</Typography>
                </Grid>
                <Grid item size={6} >
                    <Box display="flex" alignItems="center">
                        {missatges}
                    </Box>
                </Grid>

                {detalls?.length && (
                    <Grid item size={12} sx={{ display: 'flex', justifyContent: 'flex-end' }} >
                        {detailsButton}
                    </Grid>
                )}
            </Grid>
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
                        No hi ha dades per mostrar
                    </Typography>
                </CardContent>
            </Card>
        );

    const latenciaMaxValue = latencies.map((latencia: any) => latencia.latenciaMitja).reduce((accumulator: any, currentValue: any) => {
        return Math.max(accumulator, currentValue);
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
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.integracions.title')}</Typography>
            {integracions && <Table size="small">
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
                            <Chip label={i.estat} size="small" color={getEstatColor(i.estat)} />
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
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.subsistemes.title')}</Typography>
            {subsistemes && <Table size="small">
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
                            <Chip label={s.estat} size="small" color={s.estat === 'UP' ? 'success' : 'error'} />
                        </TableCell>
                        <TableCell>{s.latencia} ms</TableCell>
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
    const { setMarginsDisabled } = useBaseAppContext();
    React.useEffect(() => {
        setMarginsDisabled(true);
        return () => setMarginsDisabled(false);
    }, []);
    const dataLoaded = ready && loading != null && !loading;
    const toolbarState = salutCurrentApp?.appEstat ? <Chip
        label={salutCurrentApp.appEstat}
        size="small"
        color={salutCurrentApp.appEstat === 'UP' ? 'success' : 'error'}
        sx={{ ml: 1 }} /> : undefined;
    const toolbar = <SalutToolbar
        title={entornApp != null ? `${entornApp.app.description} - ${entornApp.entorn.description}` : ""}
        subtitle={entornApp?.versio ? 'v' + entornApp?.versio : undefined}
        state={toolbarState}
        ready={ready}
        onRefresh={entornAppDataRefresh}
        goBackActive />;
    const loadingComponent = loading ? <Box
        sx={{
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: 'calc(100vh - 80px)',
        }}>
        <CircularProgress size={100} />
    </Box> : null;
    const [detailsDialogShow, detailsDialogComponent] = useMuiContentDialog();
    const detailsComponent = (
        <Grid container spacing={2}>
            <Grid size={3}>
                <AppInfo salutCurrentApp={salutCurrentApp} detailsDialogShow={detailsDialogShow} />
            </Grid>
            <Grid size={9}>
                <ErrorBoundary fallback={<ErrorBoundaryFallback />}>
                    {dataLoaded && (
                        <LatenciaEstatsChart
                            dataInici={reportParams.dataInici}
                            agrupacio={reportParams.agrupacio}
                            latencies={latencies}
                            estats={estats}
                        />
                    )}
                </ErrorBoundary>
            </Grid>
            <Grid size={6}>
                <Integracions salutCurrentApp={salutCurrentApp} />
            </Grid>
            <Grid size={6}>
                <Subsistemes salutCurrentApp={salutCurrentApp} />
            </Grid>
        </Grid>
    );
    return <BasePage toolbar={toolbar}>
        {loading ? loadingComponent : detailsComponent}
        {detailsDialogComponent}
    </BasePage>;
}

export default SalutAppInfo;
