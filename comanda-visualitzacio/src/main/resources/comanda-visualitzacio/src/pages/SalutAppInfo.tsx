import * as React from 'react';
import { useParams } from 'react-router-dom';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import CircularProgress from '@mui/material/CircularProgress';
import { LineChart } from '@mui/x-charts/LineChart';
import {
    BasePage,
    useResourceApiService,
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
        report: salutApiReport,
    } = useResourceApiService('salut');
    const [loading, setLoading] = React.useState<boolean>();
    const [app, setApp] = React.useState<any>();
    const [latencies, setLatencies] = React.useState<Record<string, any>>({});
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
        reportParams,
    };
}

const Integracions: React.FC<any> = (props) => {
    return null;
}

const LatenciaBarChart: React.FC<any> = (props) => {
    const {
        dataInici,
        dataFi,
        agrupacio,
        latencies
    } = props;
    const baseDataGroups = generateDataGroups(dataInici, dataFi, agrupacio);
    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    return <LineChart
        dataset={latencies}
        series={[{ dataKey: 'latenciaMitja' }]}
        xAxis={[{ scaleType: 'band', data: dataGroups }]}
        yAxis={[{ label: ' ms' }]} />;
}

const SalutAppInfo: React.FC = () => {
    const { id } = useParams();
    const {
        ready,
        loading,
        refresh: appDataRefresh,
        app,
        latencies,
        reportParams,
    } = useAppData(id);
    const dataLoaded = ready && loading != null && !loading;
    const toolbar = <SalutToolbar
        title={app?.nom}
        subtitle={app?.versio ? 'v' + app?.versio : undefined}
        ready={ready}
        onRefresh={appDataRefresh}
        goBackActive />;
    return <BasePage toolbar={toolbar}>
        {loading ?
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    minHeight: 'calc(100vh - 80px)',
                }}>
                <CircularProgress size={100} />
            </Box> : <Grid container spacing={2}>
                <Grid size={12}>
                    
                </Grid>
                <Grid size={12} style={{ height: '200px' }}>
                    {dataLoaded && <LatenciaBarChart
                        dataInici={reportParams?.dataInici}
                        dataFi={reportParams?.dataFi}
                        agrupacio={reportParams?.agrupacio}
                        latencies={latencies} />}
                </Grid>
            </Grid>}
    </BasePage>;
}

export default SalutAppInfo;
