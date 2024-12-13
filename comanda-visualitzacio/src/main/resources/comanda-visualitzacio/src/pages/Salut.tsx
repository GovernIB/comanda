import * as React from 'react';
import dayjs from 'dayjs';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid2';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, { SelectChangeEvent } from '@mui/material/Select';
import InputAdornment from '@mui/material/InputAdornment';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import CircularProgress from '@mui/material/CircularProgress';
import { Gauge, gaugeClasses } from '@mui/x-charts/Gauge';
import { BarChart } from '@mui/x-charts/BarChart';
import {
    BasePage,
    Toolbar,
    useResourceApiService,
    useBaseAppContext,
} from 'reactlib';

const agrupacioFromMinutes = (intervalMinutes: number) => {
    if (intervalMinutes <= 60) {
        return 'MINUT';
    } else if (intervalMinutes <= 24 * 60) {
        return 'HORA';
    } else if (intervalMinutes <= 24 * 60 * 30 * 2) {
        return 'DIA';
    } else if (intervalMinutes <= 24 * 60 * 30 * 12 * 2) {
        return 'MES';
    } else {
        return 'ANY';
    }
}

const useReportInterval = (intervalMinutes?: number) => {
    if (intervalMinutes != null) {
        const dataRef = dayjs();
        const dataInici = dataRef.subtract(intervalMinutes, 'm').set('second', 0).set('millisecond', 0).subtract(1, 'minute');
        const dataFi = dataRef.set('second', 0).set('millisecond', 0).subtract(1, 'minute');
        const dataIniciFormat = dataInici.format('YYYY-MM-DDTHH:mm:ss');
        const dataFiFormat = dataFi.format('YYYY-MM-DDTHH:mm:ss');
        const agrupacio = agrupacioFromMinutes(intervalMinutes);
        return {
            dataInici: dataIniciFormat,
            dataFi: dataFiFormat,
            agrupacio,
        };
    } else {
        return {
            dataInici: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
            dataFi: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
            agrupacio: 'MINUT',
        };
    }
}

const generateDataGroups = (dataInici: string, dataFi: string, agrupacio: string) => {
    const dataGroups: string[] = [];
    let djs = dayjs(dataInici);
    const dataFiJs = dayjs(dataFi);
    do {
        dataGroups.push(djs.format('YYYY-MM-DDTHH:mm:ss'));
        if (agrupacio === 'ANY') {
            djs = djs.add(1, 'year');
        } else if (agrupacio === 'MES') {
            djs = djs.add(1, 'month');
        } else if (agrupacio === 'DIA') {
            djs = djs.add(1, 'day');
        } else if (agrupacio === 'HORA') {
            djs = djs.add(1, 'hour');
        } else if (agrupacio === 'MINUT') {
            djs = djs.add(1, 'minute');
        }
    } while (djs.isBefore(dataFiJs));
    return dataGroups;
}

const isDataInGroup = (data: string, group: string, agrupacio: string) => {
    if (agrupacio === 'ANY') {
        return data.substring(0, 4) === group.substring(0, 4);
    } else if (agrupacio === 'MES') {
        return data.substring(0, 7) === group.substring(0, 7);
    } else if (agrupacio === 'DIA') {
        return data.substring(0, 10) === group.substring(0, 10);
    } else if (agrupacio === 'HORA') {
        return data.substring(0, 13) === group.substring(0, 13);
    } else if (agrupacio === 'MINUT') {
        return data.substring(0, 16) === group.substring(0, 16);
    }
}

const generateUpdownBarChartData = (
    dataInici: string,
    dataFi: string,
    agrupacio: string,
    estats: Record<string, any>) => {
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
    const series = [{
        data: seriesUp,
        label: 'up',
        stack: 'total',
        color: '#59a14f'
    }, {
        data: seriesUpDown,
        label: 'up/down',
        stack: 'total',
        color: '#f28e2c'
    }, {
        data: seriesDown,
        label: 'down',
        stack: 'total',
        color: '#e15759'
    }];
    const dataGroups = baseDataGroups?.map(g => {
        if (agrupacio === 'ANY') {
            return g.substring(0, 4);
        } else if (agrupacio === 'MES') {
            return g.substring(0, 7);
        } else if (agrupacio === 'DIA') {
            return g.substring(0, 10);
        } else if (agrupacio === 'HORA') {
            return g.substring(11, 13) + ':00';
        } else if (agrupacio === 'MINUT') {
            return g.substring(11, 16);
        }
    });
    return {
        dataGroups,
        series,
    };
}

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
    const refresh = (dataInici: string, dataFi: string, agrupacio: string) => {
        const reportData = {
            dataInici,
            dataFi,
            agrupacio,
        };
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
                        const currentData = {
                            ...reportData,
                            appCodi: i.codi
                        };
                        return salutApiReport({ code: 'estat', data: currentData }).
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
    };
}

/*const HomeDashboards = () => {
    return <Grid container spacing={2}>
        <Grid size={3}>
            <DashboardWidget
                resourceName="app"
                to="/"
                icon="home" />
        </Grid>
        <Grid size={3}>
            <DashboardWidget
                resourceName="app"
                to="/"
                icon="home" />
        </Grid>
        <Grid size={3}>
            <DashboardWidget
                resourceName="app"
                to="/ape"
                icon="home" />
        </Grid>
        <Grid size={3}>
            <DashboardWidget
                resourceName="app"
                to="/api"
                icon="home" />
        </Grid>
    </Grid>;
}*/

const RefreshTimeoutSelect: React.FC<any> = (props: { onChange: (minutes: number) => void }) => {
    const { onChange } = props;
    const [duration, setDuration] = React.useState<string>('PT1M');
    const callOnChange = (duration: string) => {
        if (onChange != null) {
            const minutes = dayjs.duration(duration).asMinutes()
            onChange?.(minutes);
        }
    }
    const handleChange = (event: SelectChangeEvent) => {
        const value = event.target.value as string;
        setDuration(value);
        callOnChange(value);
    }
    React.useEffect(() => {
        callOnChange(duration);
    }, []);
    return <FormControl>
        <Select
            labelId="range-select-label"
            id="range-select"
            value={duration}
            size="small"
            onChange={handleChange}
            startAdornment={<InputAdornment position="start"><Icon>update</Icon></InputAdornment>}
            sx={{ mr: 1, width: '10em' }}>
            <MenuItem value={"PT1M"}>1 minut</MenuItem>
            <MenuItem value={"PT5M"}>5 minuts</MenuItem>
            <MenuItem value={"PT10M"}>10 minuts</MenuItem>
            <MenuItem value={"PT30M"}>Mitja hora</MenuItem>
            <MenuItem value={"PT1H"}>1 hora</MenuItem>
        </Select>
    </FormControl>;
}

const AppDataRangeSelect: React.FC<any> = (props: { onChange: (minutes: number) => void }) => {
    const { onChange } = props;
    const [duration, setDuration] = React.useState<string>('PT15M');
    const callOnChange = (duration: string) => {
        if (onChange != null) {
            const minutes = dayjs.duration(duration).asMinutes();
            onChange?.(minutes);
        }
    }
    const handleChange = (event: SelectChangeEvent) => {
        const value = event.target.value as string;
        setDuration(value);
        callOnChange(value);
    }
    React.useEffect(() => {
        callOnChange(duration);
    }, []);
    return <FormControl>
        <Select
            labelId="range-select-label"
            id="range-select"
            value={duration}
            size="small"
            onChange={handleChange}
            startAdornment={<InputAdornment position="start"><Icon>date_range</Icon></InputAdornment>}
            sx={{ mr: 1, width: '20em' }}>
            <MenuItem value={"PT15M"}>Darrers 15 minuts</MenuItem>
            <MenuItem value={"PT1H"}>Darrera hora</MenuItem>
            <MenuItem value={"P1D"}>Darrer dia</MenuItem>
            <MenuItem value={"P7D"}>Darrera setmana</MenuItem>
            <MenuItem value={"P1M"}>Darrer mes</MenuItem>
        </Select>
    </FormControl>;
}

const UpdownGaugeChart: React.FC<any> = (props: { salutLastItems: any[] }) => {
    const { salutLastItems } = props;
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
                fill: '#59a14f',
            },
            [`& .${gaugeClasses.referenceArc}`]: {
                fill: '#e15759',
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
    const { dataGroups, series } = generateUpdownBarChartData(dataInici, dataFi, agrupacio, estats);
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
    const { getLinkComponent } = useBaseAppContext();
    return <Table sx={{ minWidth: 650 }} aria-label="simple table">
        <TableHead>
            <TableRow>
                <TableCell>Estat</TableCell>
                <TableCell>Codi</TableCell>
                <TableCell>Nom</TableCell>
                <TableCell>Versió</TableCell>
                <TableCell>Base de dades</TableCell>
                <TableCell>Latència</TableCell>
                <TableCell>Integracions</TableCell>
                <TableCell>Subsistemes</TableCell>
                <TableCell>Missatges</TableCell>
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
                        <ItemStateChip up={appUpdownItem?.appUp} date={appUpdownItem.data} />
                    </TableCell>
                    <TableCell component="th" scope="row">{app.codi}</TableCell>
                    <TableCell component="th" scope="row">{app.nom}</TableCell>
                    <TableCell component="th" scope="row">{app.versio}</TableCell>
                    <TableCell component="th" scope="row">
                        <ItemStateChip up={appUpdownItem?.bdUp} date={appUpdownItem.data} />
                    </TableCell>
                    <TableCell component="th" scope="row">
                        {appUpdownItem.appLatencia != null ? appUpdownItem.appLatencia + ' ms' : 'N/D'}
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
                            to={'appinfo/' + app.codi}>Detalls</Button>
                    </TableCell>
                </TableRow>;
            })}
        </TableBody>
    </Table>;
}

const Salut: React.FC = () => {
    const [refreshTimeoutMinutes, setRefreshTimeoutMinutes] = React.useState<number>();
    const [appDataRangeMinutes, setAppDataRangeMinutes] = React.useState<number>();
    const {
        ready,
        loading,
        refresh: appDataRefresh,
        apps,
        salutLastItems,
        estats,
    } = useAppData();
    const {
        dataInici,
        dataFi,
        agrupacio,
    } = useReportInterval(appDataRangeMinutes);
    const dataLoaded = ready && loading != null && !loading;
    const refresh = () => {
        appDataRangeMinutes != null && appDataRefresh(dataInici, dataFi, agrupacio);
    }
    const handleRefreshClick = () => {
        refresh();
    }
    React.useEffect(() => {
        if (ready) {
            refresh();
        }
    }, [ready]);
    React.useEffect(() => {
        if (refreshTimeoutMinutes) {
            const timeoutMs = refreshTimeoutMinutes * 60 * 1000;
            const intervalId = setInterval(() => {
                refresh();
            }, timeoutMs);
            return () => {
                clearInterval(intervalId);
            }
        }
    }, [refreshTimeoutMinutes, dataInici, dataFi, agrupacio]);
    React.useEffect(() => {
        refresh();
    }, [appDataRangeMinutes]);
    const toolbarElementsWithPositions = [{
        position: 2,
        element: <RefreshTimeoutSelect onChange={setRefreshTimeoutMinutes} />
    }, {
        position: 2,
        element: <AppDataRangeSelect onChange={setAppDataRangeMinutes} />
    }, {
        position: 2,
        element: <IconButton onClick={handleRefreshClick}>
            <Icon>refresh</Icon>
        </IconButton>
    }];
    const toolbar = <Toolbar
        title="Salut"
        elementsWithPositions={toolbarElementsWithPositions}
        upperToolbar />;
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
                        dataInici={dataInici}
                        dataFi={dataFi}
                        agrupacio={agrupacio}
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
