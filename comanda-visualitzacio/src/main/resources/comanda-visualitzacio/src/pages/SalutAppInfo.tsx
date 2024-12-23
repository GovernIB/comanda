import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
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
import Alert from '@mui/material/Alert';
import Icon from '@mui/material/Icon';
import { LineChart } from '@mui/x-charts/LineChart';
import {
    BasePage,
    useResourceApiService,
    dateFormatLocale,
} from 'reactlib';
import SalutToolbar from '../components/SalutToolbar';
import {
    generateDataGroups,
    toXAxisDataGroups
} from '../util/dataGroup';

const useAppData = (id: any) => {
    const {
        isReady: appApiIsReady,
        getOne: appGetOne,
    } = useResourceApiService('app');
    const {
        isReady: salutApiIsReady,
        find: salutApiFind,
        report: salutApiReport,
    } = useResourceApiService('salut');
    const [loading, setLoading] = React.useState<boolean>();
    const [app, setApp] = React.useState<any>();
    const [latencies, setLatencies] = React.useState<Record<string, any>>({});
    const [salutCurrentApp, setSalutCurrentApp] = React.useState<any>();
    const [reportParams, setReportParams] = React.useState<any>();
    const refresh = (dataInici: string, dataFi: string, agrupacio: string) => {
        const reportParams = {
            dataInici,
            dataFi,
            agrupacio,
        };
        setReportParams(reportParams);
        if (appApiIsReady && salutApiIsReady) {
            setLoading(true);
            appGetOne(id).
                then((app) => {
                    setApp(app);
                    return app;
                }).
                then((app) => new Promise((resolve, reject) => {
                    const reportData = {
                        ...reportParams,
                        appCodi: app.codi
                    };
                    salutApiReport({ code: 'latencia', data: reportData }).
                        then(items => {
                            setLatencies(items);
                            resolve(items);
                        }).
                        catch(reject);
                })).
                then(() => new Promise((resolve, reject) => {
                    const findArgs = {
                        page: 0,
                        size: 1,
                        sorts: ['data,desc'],
                        perspectives: ['SAL_INTEGRACIONS', 'SAL_SUBSISTEMES'],
                    };
                    salutApiFind(findArgs).
                        then(({ rows }) => {
                            const salutCurrentApp = rows?.[0];
                            setSalutCurrentApp(salutCurrentApp);
                            resolve(salutCurrentApp);
                        }).
                        catch(reject);
                })).
                finally(() => {
                    setLoading(false);
                });
        }
    }
    return {
        ready: appApiIsReady && salutApiIsReady,
        loading,
        refresh,
        app,
        latencies,
        salutCurrentApp,
        reportParams,
    };
}

const AppInfo: React.FC<any> = (props) => {
    const { salutCurrentApp: app } = props;
    const { t } = useTranslation();
    const data = app && <Typography>{dateFormatLocale(app.data, true)}</Typography>;
    const bdEstat = app && <Typography><Chip label={app.bdEstat} size="small" color={app.bdEstat === 'UP' ? 'success' : 'error'} /></Typography>;
    const appLatencia = app && <Typography>{app.appLatencia != null ? app.appLatencia + ' ms' : t('page.salut.nd')}</Typography>;
    const missatges = app && <>
        <Chip label={app.missatgeErrorCount} size="small" color="error" />&nbsp;/&nbsp;
        <Chip label={app.missatgeWarnCount} size="small" color="warning" />&nbsp;/&nbsp;
        <Chip label={app.missatgeInfoCount} size="small" color="info" />
    </>;
    return <Card variant="outlined" sx={{ height: '300px' }}>
        <CardContent sx={{ height: '100%' }}>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.info.title')}</Typography>
            <List sx={{ ml: 2 }}>
                <ListItem secondaryAction={data} disablePadding>
                    <ListItemText primary={t('page.salut.info.data')} sx={{ '& span': { fontWeight: 'bold' } }} />
                </ListItem>
                <ListItem secondaryAction={bdEstat} disablePadding>
                    <ListItemText primary={t('page.salut.info.bdEstat')} sx={{ '& span': { fontWeight: 'bold' } }} />
                </ListItem>
                <ListItem secondaryAction={appLatencia} disablePadding>
                    <ListItemText primary={t('page.salut.info.appLatencia')} sx={{ '& span': { fontWeight: 'bold' } }} />
                </ListItem>
                <ListItem secondaryAction={missatges} disablePadding>
                    <ListItemText primary={t('page.salut.info.missatges')} sx={{ '& span': { fontWeight: 'bold' } }} />
                </ListItem>
            </List>
        </CardContent>
    </Card>;
}

const LatenciaBarChart: React.FC<any> = (props) => {
    const {
        dataInici,
        dataFi,
        agrupacio,
        latencies
    } = props;
    const { t } = useTranslation();
    const baseDataGroups = generateDataGroups(dataInici, dataFi, agrupacio);
    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    return <Card variant="outlined" sx={{ height: '300px' }}>
        <CardContent sx={{ height: '100%' }}>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.latencia.title')}</Typography>
            <LineChart
                dataset={latencies}
                series={[{ dataKey: 'latenciaMitja' }]}
                xAxis={[{ scaleType: 'band', data: dataGroups }]}
                yAxis={[{ label: ' ms' }]} />
        </CardContent>
    </Card>;
}

const Integracions: React.FC<any> = (props) => {
    const { salutCurrentApp } = props;
    const { t } = useTranslation();
    const integracions = salutCurrentApp?.integracions;
    return <Card variant="outlined" sx={{ height: '100%' }}>
        <CardContent>
            <Typography gutterBottom variant="h5" component="div">{t('page.salut.integracions.title')}</Typography>
            {integracions && <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell>{t('page.salut.integracions.column.codi')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.nom')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.estat')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.latencia')}</TableCell>
                        <TableCell>{t('page.salut.integracions.column.peticions')}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {integracions.map((i: any, key: number) => <TableRow key={key}>
                        <TableCell>{i.codi}</TableCell>
                        <TableCell>{i.nom}</TableCell>
                        <TableCell>
                            <Chip label={i.estat} size="small" color={i.estat === 'UP' ? 'success' : 'error'} />
                        </TableCell>
                        <TableCell>{i.latencia != null ? i.latencia + ' ms' : t('page.salut.nd')}</TableCell>
                        <TableCell>{i.latencia} ms</TableCell>
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
    const { t } = useTranslation();
    const {
        ready,
        loading,
        refresh: appDataRefresh,
        app,
        latencies,
        salutCurrentApp,
        reportParams,
    } = useAppData(id);
    const dataLoaded = ready && loading != null && !loading;
    const appStateUp = salutCurrentApp?.appEstat === 'UP';
    const toolbarState = salutCurrentApp?.appEstat ? <Chip
        label={salutCurrentApp.appEstat}
        size="small"
        color={salutCurrentApp.appEstat === 'UP' ? 'success' : 'error'}
        sx={{ ml: 1 }} /> : undefined;
    const toolbar = <SalutToolbar
        title={app?.nom}
        subtitle={app?.versio ? 'v' + app?.versio : undefined}
        state={toolbarState}
        ready={ready}
        onRefresh={appDataRefresh}
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
    const detailsComponent = appStateUp ? <Grid container spacing={2}>
        <Grid size={6}>
            <AppInfo salutCurrentApp={salutCurrentApp} />
        </Grid>
        <Grid size={6}>
            {dataLoaded && <LatenciaBarChart
                dataInici={reportParams?.dataInici}
                dataFi={reportParams?.dataFi}
                agrupacio={reportParams?.agrupacio}
                latencies={latencies} />}
        </Grid>
        <Grid size={6}>
            <Integracions salutCurrentApp={salutCurrentApp} />
        </Grid>
        <Grid size={6}>
            <Subsistemes salutCurrentApp={salutCurrentApp} />
        </Grid>
        <Grid size={12}>

        </Grid>
    </Grid> : <Alert icon={<Icon>warning</Icon>} severity="warning">
        {t('page.salut.noDetailsIfDown')}
    </Alert>;
    return <BasePage toolbar={toolbar}>
        {loading ? loadingComponent : detailsComponent}
    </BasePage>;
}

export default SalutAppInfo;
