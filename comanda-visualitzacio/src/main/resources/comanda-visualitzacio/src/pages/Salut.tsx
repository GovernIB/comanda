import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Button from '@mui/material/Button';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import { Gauge, gaugeClasses } from '@mui/x-charts/Gauge';
import { BarChart } from '@mui/x-charts/BarChart';
import { useTheme } from '@mui/material/styles';
import {
    BasePage,
    useResourceApiService,
    useBaseAppContext,
    dateFormatLocale,
} from 'reactlib';
import {
    generateDataGroups,
    isDataInGroup,
    toXAxisDataGroups
} from '../util/dataGroup';

import SalutToolbar from '../components/SalutToolbar';

const useAppData = () => {
    const {
        isReady: appApiIsReady,
        find: appApiFind,
    } = useResourceApiService('app');
    const {
        isReady: salutApiIsReady,
        report: salutApiReport,
    } = useResourceApiService('salut');
    const [loading, setLoading] = React.useState<boolean>();
    const [apps, setApps] = React.useState<any[]>();
    const [estats, setEstats] = React.useState<Record<string, any>>({});
    const [salutLastItems, setSalutLastItems] = React.useState<any[]>();
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
            appApiFind({ unpaged: true }).
                then((response) => {
                    setApps(response.rows);
                }).
                then(() => new Promise((resolve, reject) => {
                    salutApiReport({ code: 'salut_last' }).
                        then(items => {
                            setSalutLastItems(items);
                            resolve(items);
                        }).
                        catch(reject);
                })).
                then((updownItems) => new Promise((resolve, reject) => {
                    const ps: Promise<any>[] = (updownItems as any[])?.map((i: any) => {
                        const reportData = {
                            ...reportParams,
                            appCodi: i.codi
                        };
                        return salutApiReport({ code: 'estat', data: reportData }).
                            then(ii => setEstats(e => ({ ...e, [i.codi]: ii })));
                    });
                    Promise.all(ps).then(resolve).catch(reject);
                })).
                then(() => {
                    setLoading(false);
                });
        }
    }
    return {
        ready: appApiIsReady && salutApiIsReady,
        loading,
        refresh,
        apps,
        salutLastItems,
        estats,
        reportParams,
    };
}

const UpdownGaugeChart: React.FC<any> = (props: { salutLastItems: any[] }) => {
    const { salutLastItems } = props;
    const theme = useTheme();
    const upCount = salutLastItems?.filter(i => i.appUp).length;
    const upPercent = salutLastItems?.length ? (upCount / salutLastItems.length) * 100 : 0;
    return <Gauge
        value={upPercent}
        sx={() => ({
            [`& .${gaugeClasses.valueText}`]: {
                fontSize: 30,
                transform: 'translate(0px, 0px)',
            },
            [`& .${gaugeClasses.valueArc}`]: {
                fill: theme.palette.success.main,
            },
            [`& .${gaugeClasses.referenceArc}`]: {
                fill: theme.palette.error.main,
            },
        })}
        text={({ value }) => `${(value ?? 0) * salutLastItems.length / 100} / ${salutLastItems.length}`} />;
}

const UpdownBarChart: React.FC<any> = (props) => {
    const {
        dataInici,
        dataFi,
        agrupacio,
        estats
    } = props;
    const theme = useTheme();
    const baseDataGroups = generateDataGroups(dataInici, dataFi, agrupacio);
    const seriesUp = baseDataGroups.map(g => {
        let up = 0;
        const estatApps = Object.keys(estats);
        estatApps?.forEach((a: any) => {
            const estatForGroup = estats[a].find((ea: any) => isDataInGroup(ea.data, g, agrupacio));
            up = up + (estatForGroup?.alwaysUp ? 1 : 0);
        });
        return up;
    });
    const seriesUpDown = baseDataGroups.map(g => {
        let upDown = 0;
        const estatApps = Object.keys(estats);
        estatApps?.forEach((a: any) => {
            const estatForGroup = estats[a].find((ea: any) => isDataInGroup(ea.data, g, agrupacio));
            upDown = upDown + (estatForGroup != null && !estatForGroup?.alwaysUp && !estatForGroup?.alwaysDown ? 1 : 0);
        });
        return upDown;
    });
    const seriesDown = baseDataGroups.map(g => {
        let down = 0;
        const estatApps = Object.keys(estats);
        estatApps?.forEach((a: any) => {
            const estatForGroup = estats[a].find((ea: any) => isDataInGroup(ea.data, g, agrupacio));
            down = down + (estatForGroup?.alwaysDown ? 1 : 0);
        });
        return down;
    });
    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    const series = [{
        data: seriesUp,
        label: 'up',
        stack: 'total',
        color: theme.palette.success.main
    }, {
        data: seriesUpDown,
        label: 'up/down',
        stack: 'total',
        color: theme.palette.warning.main
    }, {
        data: seriesDown,
        label: 'down',
        stack: 'total',
        color: theme.palette.error.main
    }];
    return <BarChart
        xAxis={[{ scaleType: 'band', data: dataGroups }]}
        series={series} />;
}

const ItemStateChip: React.FC<any> = (props: { up: boolean, date: string }) => {
    const { up, date } = props;
    return <>
        {up ? <Chip label="UP" size="small" color="success" /> : <Chip label="DOWN" size="small" color="error" />}
        <br />
        <Typography variant="caption">{date}</Typography>
    </>;
}

const AppDataTable: React.FC<any> = (props: { apps: any[], salutLastItems: any[] }) => {
    const { apps, salutLastItems } = props;
    const { t } = useTranslation();
    const { getLinkComponent } = useBaseAppContext();
    return <Table sx={{ minWidth: 650 }} aria-label="simple table">
        <TableHead>
            <TableRow>
                <TableCell>{t('page.salut.apps.column.estat')}</TableCell>
                <TableCell>{t('page.salut.apps.column.codi')}</TableCell>
                <TableCell>{t('page.salut.apps.column.nom')}</TableCell>
                <TableCell>{t('page.salut.apps.column.versio')}</TableCell>
                <TableCell>{t('page.salut.apps.column.bd')}</TableCell>
                <TableCell>{t('page.salut.apps.column.latencia')}</TableCell>
                <TableCell>{t('page.salut.apps.column.integ')}</TableCell>
                <TableCell>{t('page.salut.apps.column.subsis')}</TableCell>
                <TableCell>{t('page.salut.apps.column.msgs')}</TableCell>
                <TableCell></TableCell>
            </TableRow>
        </TableHead>
        <TableBody>
            {apps?.map(app => {
                const appUpdownItem = salutLastItems?.find(i => i.codi === app.codi);
                return <TableRow
                    key={app.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                    <TableCell component="th" scope="row">
                        <ItemStateChip up={appUpdownItem?.appUp} date={dateFormatLocale(appUpdownItem.data, true)} />
                    </TableCell>
                    <TableCell component="th" scope="row">{app.codi}</TableCell>
                    <TableCell component="th" scope="row">{app.nom}</TableCell>
                    <TableCell component="th" scope="row">{app.versio}</TableCell>
                    <TableCell component="th" scope="row">
                        <ItemStateChip up={appUpdownItem?.bdUp} date={dateFormatLocale(appUpdownItem.data, true)} />
                    </TableCell>
                    <TableCell component="th" scope="row">
                        {appUpdownItem.appLatencia != null ? appUpdownItem.appLatencia + ' ms' : t('page.salut.nd')}
                    </TableCell>
                    <TableCell component="th" scope="row">
                        <Chip label={appUpdownItem.integracioUpCount} size="small" color="success" />&nbsp;/&nbsp;
                        <Chip label={appUpdownItem.integracioDownCount} size="small" color="error" />
                    </TableCell>
                    <TableCell component="th" scope="row">
                        <Chip label={appUpdownItem.subsistemaUpCount} size="small" color="success" />&nbsp;/&nbsp;
                        <Chip label={appUpdownItem.subsistemaDownCount} size="small" color="error" />
                    </TableCell>
                    <TableCell component="th" scope="row">
                        <Chip label={appUpdownItem.missatgeErrorCount} size="small" color="error" />&nbsp;/&nbsp;
                        <Chip label={appUpdownItem.missatgeWarnCount} size="small" color="warning" />&nbsp;/&nbsp;
                        <Chip label={appUpdownItem.missatgeInfoCount} size="small" color="info" />
                    </TableCell>
                    <TableCell component="th" scope="row">
                        <Button
                            variant="contained"
                            size="small"
                            component={getLinkComponent()}
                            to={'appinfo/' + app.id}>{t('page.salut.apps.detalls')}</Button>
                    </TableCell>
                </TableRow>;
            })}
        </TableBody>
    </Table>;
}

const Salut: React.FC = () => {
    const { t } = useTranslation();
    const {
        ready,
        loading,
        refresh: appDataRefresh,
        apps,
        salutLastItems,
        estats,
        reportParams,
    } = useAppData();
    const dataLoaded = ready && loading != null && !loading;
    const toolbar = <SalutToolbar
        title={t('page.salut.title')}
        ready={ready}
        onRefresh={appDataRefresh} />;
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
                <Grid size={3}>
                    {dataLoaded && <UpdownGaugeChart
                        salutLastItems={salutLastItems} />}
                </Grid>
                <Grid size={9} style={{ height: '200px' }}>
                    {dataLoaded && <UpdownBarChart
                        dataInici={reportParams?.dataInici}
                        dataFi={reportParams?.dataFi}
                        agrupacio={reportParams?.agrupacio}
                        estats={estats} />}
                </Grid>
                <Grid size={12}>
                    {dataLoaded && <AppDataTable
                        apps={apps}
                        salutLastItems={salutLastItems} />}
                </Grid>
            </Grid>}
    </BasePage>;
}

export default Salut;
