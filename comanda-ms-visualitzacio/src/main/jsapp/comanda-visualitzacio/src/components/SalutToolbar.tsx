import * as React from 'react';
import { useTranslation } from 'react-i18next';
import dayjs from 'dayjs';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, { SelectChangeEvent } from '@mui/material/Select';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import {
    FormField, MuiDialog, MuiFilter, springFilterBuilder,
    Toolbar,
    useBaseAppContext, useFilterApiRef, useFormContext,
} from 'reactlib';
import { Box } from '@mui/material';
import Grid from "@mui/material/Grid";
import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined';
import FilterAltIcon from '@mui/icons-material/FilterAlt';

export type SalutToolbarProps = {
    title: string;
    subtitle?: string;
    state?: React.ReactElement;
    hideFilter?: boolean;
    ready: boolean;
    onRefresh: (dataInici: string, dataFi: string, agrupacio: string, springFilter?: string) => void;
    goBackActive?: boolean;
    appDataLoading?: boolean;
}

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

const toReportInterval = (intervalMinutes?: number) => {
    if (intervalMinutes != null && intervalMinutes > 0) {
        const dataFi = dayjs().set('second', 59).set('millisecond', 999);
        const dataInici = dataFi.subtract(intervalMinutes - 1, 'm').set('second', 0).set('millisecond', 0);
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

const RefreshTimeoutSelect: React.FC<any> = (props: { disabled?: boolean; onChange: (minutes: number) => void; initialValue?: string }) => {
    const { onChange, disabled, initialValue = 'PT5M' } = props;
    const { t } = useTranslation();

    // Get the stored value from localStorage or use initialValue
    const getInitialDuration = () => {
        const storedValue = localStorage.getItem('refreshTimeoutSelect');
        return storedValue || initialValue;
    };

    const [duration, setDuration] = React.useState<string>(getInitialDuration());

    const callOnChange = (duration: string) => {
        if (onChange != null) {
            const minutes = dayjs.duration(duration).asMinutes()
            onChange?.(minutes);
        }
    }

    const handleChange = (event: SelectChangeEvent) => {
        const value = event.target.value as string;
        setDuration(value);
        // Store the selected value in localStorage
        localStorage.setItem('refreshTimeoutSelect', value);
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
            disabled={disabled}
            onChange={handleChange}
            startAdornment={<InputAdornment position="start"><Icon>update</Icon></InputAdornment>}
            sx={{ mr: 1 }}>
            <MenuItem value={"PT1M"}>{t('page.salut.refreshperiod.PT1M')}</MenuItem>
            <MenuItem value={"PT5M"}>{t('page.salut.refreshperiod.PT5M')}</MenuItem>
            <MenuItem value={"PT10M"}>{t('page.salut.refreshperiod.PT10M')}</MenuItem>
            <MenuItem value={"PT30M"}>{t('page.salut.refreshperiod.PT30M')}</MenuItem>
            <MenuItem value={"PT1H"}>{t('page.salut.refreshperiod.PT1H')}</MenuItem>
        </Select>
    </FormControl>;
}

const AppDataRangeSelect: React.FC<any> = (props: { disabled?: boolean; onChange: (minutes: number) => void; initialValue?: string }) => {
    const { onChange, disabled, initialValue = 'PT15M' } = props;
    const { t } = useTranslation();

    // Get the stored value from localStorage or use initialValue
    const getInitialDuration = () => {
        const storedValue = localStorage.getItem('appDataRangeSelect');
        return storedValue || initialValue;
    };

    const [duration, setDuration] = React.useState<string>(getInitialDuration());

    const callOnChange = (duration: string) => {
        if (onChange != null) {
            const minutes = dayjs.duration(duration).asMinutes();
            onChange?.(minutes);
        }
    }

    const handleChange = (event: SelectChangeEvent) => {
        const value = event.target.value as string;
        setDuration(value);
        // Store the selected value in localStorage
        localStorage.setItem('appDataRangeSelect', value);
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
            disabled={disabled}
            onChange={handleChange}
            startAdornment={<InputAdornment position="start"><Icon>date_range</Icon></InputAdornment>}
            sx={{ mr: 1 }}>
            <MenuItem value={"PT15M"}>{t('page.salut.timerange.PT15M')}</MenuItem>
            <MenuItem value={"PT1H"}>{t('page.salut.timerange.PT1H')}</MenuItem>
            <MenuItem value={"P1D"}>{t('page.salut.timerange.P1D')}</MenuItem>
            <MenuItem value={"P7D"}>{t('page.salut.timerange.P7D')}</MenuItem>
            <MenuItem value={"P1M"}>{t('page.salut.timerange.P1M')}</MenuItem>
        </Select>
    </FormControl>;
}

function formatTimeDifference(otherDate: any) {
    const now = new Date();
    const diffInMs = Math.abs(now.getTime() - otherDate.getTime());
    const diffInSeconds = Math.floor(diffInMs / 1000);

    if (diffInSeconds < 60) {
        return `${diffInSeconds} s`;
    } else {
        const diffInMinutes = Math.floor(diffInSeconds / 60);
        return `${diffInMinutes} m`;
    }
}


const useTimeUntilNextRefreshFormatted = (nextRefresh: Date | null) => {
    const [timeUntilNextRefreshFormatted, setTimeUntilNextRefreshFormatted] = React.useState<
        string | null
    >(null);
    React.useEffect(() => {
        if (nextRefresh != null) {
            const intervalId = setInterval(() => {
                setTimeUntilNextRefreshFormatted(formatTimeDifference(nextRefresh))
            }, 1000);
            return () => clearInterval(intervalId);
        }
    }, [nextRefresh]);
    return timeUntilNextRefreshFormatted;
};

type UseIntervalOptions = {
    tickCallback: () => void;
    initCallback: () => void;
    refreshTimeoutMs: number | null | undefined;
};

function useInterval({ tickCallback, initCallback, refreshTimeoutMs }: UseIntervalOptions) {
    const savedCallback = React.useRef<() => void | null>(null);

    React.useEffect(() => {
        savedCallback.current = tickCallback;
    }, [tickCallback]);

    React.useEffect(() => {
        if (refreshTimeoutMs && refreshTimeoutMs > 0) {
            initCallback?.();

            function tick() {
                savedCallback.current?.();
            }

            const intervalId = setInterval(tick, refreshTimeoutMs);
            return () => clearInterval(intervalId);
        }
    }, [refreshTimeoutMs]);
}

const SalutEntornAppFilterForm: React.FC<any> = () => {
    const { data } = useFormContext();

    return <Grid container spacing={2}>
        <Grid size={6}>
            <FormField name="app" componentProps={{ size: 'small', }}
                       filter={springFilterBuilder.exists(
                           springFilterBuilder.and(springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id))
                       )}
            />
        </Grid>
        <Grid size={6}>
            <FormField name="entorn" componentProps={{ size: 'small', }}
                       filter={springFilterBuilder.exists(
                           springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', data?.app?.id))
                       )}
            />
        </Grid>
    </Grid>
}

const salutEntornAppFilterBuilder = (data: any) => {
    return springFilterBuilder.and(
        springFilterBuilder.eq('app.id', data.app?.id),
        springFilterBuilder.eq('entorn.id', data.entorn?.id),
    )
}

const SalutEntornAppFilter: React.FC<any> = (props:any) => {
    const { initialData, setData, apiRef, onSpringFilterChange } = props;

    return <MuiFilter
        resourceName={"entornApp"}
        code={"salut_entornApp_filter"}
        springFilterBuilder={(data:any)=> {
            setData(data)
            return salutEntornAppFilterBuilder(data);
        }}
        initialData={initialData}
        apiRef={apiRef}
        onSpringFilterChange={onSpringFilterChange}
        buttonControlled
    >
        <SalutEntornAppFilterForm/>
    </MuiFilter>
}

const FILTER_DATA_LOCALSTORAGE_KEY = 'filterDataSalut';

const useSalutEntornAppFilter = () => {
    const { t } = useTranslation()

    const getInitialData = () => {
        const storedValue = localStorage.getItem(FILTER_DATA_LOCALSTORAGE_KEY);
        return storedValue ? JSON.parse(storedValue) : {};
    }

    const [open, setOpen] = React.useState<boolean>(false);
    const [data, setData] = React.useState<any>(getInitialData);
    const [springFilter, setSpringFilter] = React.useState<string>(
        data != null ? salutEntornAppFilterBuilder(data) : ''
    );
    const filterRef = useFilterApiRef();

    const onFilterDataChange = (data: any) => {
        setData(data);
        localStorage.setItem(FILTER_DATA_LOCALSTORAGE_KEY, JSON.stringify(data));
    }

    const cercar = ()=> {
        filterRef?.current?.filter?.()
    }
    const netejar = ()=> {
        filterRef?.current?.clear?.()
    }

    const handleOpen = () => {
        setOpen(true)
    }

    const handleClose = () => {
        setOpen(false)
    }

    const dialog = <MuiDialog
        open={open}
        closeCallback={handleClose}
        title={t('page.salut.filtrar')}
        componentProps={{fullWidth: true, maxWidth: 'sm'}}
        buttons={[
            {
                value: 'clear',
                text: t('components.clear'),
                componentProps: {
                    variant: "outlined",
                    sx: { borderRadius: '4px' },
                },
            },
            {
                value: 'search',
                text: t('components.search'),
                icon: 'filter_alt',
                componentProps: {
                    variant: "contained",
                    sx: { borderRadius: '4px' },
                },
            },
        ]}
        buttonCallback={(value: any): void => {
            if (value === 'clear') netejar();
            if (value === 'search') cercar();
        }}
    >
        <SalutEntornAppFilter initialData={data} setData={onFilterDataChange} apiRef={filterRef} onSpringFilterChange={setSpringFilter}/>
    </MuiDialog>

    return {
        springFilter,
        handleOpen,
        handleClose,
        dialog,
        data,
    }
}

export const SalutToolbar: React.FC<SalutToolbarProps> = (props) => {
    const {
        title,
        hideFilter,
        state,
        ready,
        onRefresh,
        goBackActive,
        appDataLoading,
    } = props;
    const { t } = useTranslation();
    const { goBack } = useBaseAppContext();
    const theme = useTheme();
    const [refreshTimeoutMinutes, setRefreshTimeoutMinutes] = React.useState<number>();
    const [appDataRangeMinutes, setAppDataRangeMinutes] = React.useState<number>();
    const [lastRefresh, setLastRefresh] = React.useState<Date | null>(null);
    const [nextRefresh, setNextRefresh] = React.useState<Date | null>(null);

    const {springFilter, handleOpen, dialog, data: filterData} = useSalutEntornAppFilter();

    const computedSubtitle = React.useMemo(() => {
        const appName = filterData?.app?.description;
        const envName = filterData?.entorn?.description;
        if (!appName && !envName) return t('page.salut.senseFiltres');
        if (appName && !envName) return appName as string;
        if (!appName && envName) return envName as string;
        return `${appName} - ${envName}`;
    }, [filterData, t]);

    const timeUntilNextRefreshFormatted = useTimeUntilNextRefreshFormatted(nextRefresh);
    const refresh = () => {
        setLastRefresh(new Date());
        const { dataInici, dataFi, agrupacio } = toReportInterval(appDataRangeMinutes);
        if (appDataRangeMinutes != null) onRefresh(dataInici, dataFi, agrupacio, springFilter);
    };
    const updateNextRefresh = (refreshTimeout: number) => {
        const nextRequestDate = new Date();
        nextRequestDate.setTime(nextRequestDate.getTime() + refreshTimeout);
        setNextRefresh(nextRequestDate);
    }
    React.useEffect(() => {
        // Refresca les dades quan es carrega la pàgina i quan es canvien les dates o l'agrupació
        if (ready) {
            refresh();
        }
    }, [ready, appDataRangeMinutes, springFilter]);
    const refreshTimeoutMs = refreshTimeoutMinutes !== undefined ? refreshTimeoutMinutes * 60 * 1000 : null;
    useInterval({
        tickCallback: () => {
            // @ts-expect-error tickCallback només s'executa si refreshTimeoutMs no és null
            updateNextRefresh(refreshTimeoutMs);
            refresh();
        },
        // @ts-expect-error initCallback només s'executa si refreshTimeoutMs no és null
        initCallback: () => updateNextRefresh(refreshTimeoutMs),
        refreshTimeoutMs,
    });
    const toolbarElementsWithPositions = [
        {
            position: 2,
            element: (
                <Box sx={{ mr: 2, minWidth: { xs: '50px', sm: '135px'}, }}>
                    {lastRefresh != null && (
                        <Typography sx={{ display: 'block' }} variant="caption">
                            {t('page.salut.refresh.last')}: <b>{lastRefresh.toLocaleTimeString()}</b>
                        </Typography>
                    )}
                    {nextRefresh != null &&
                        nextRefresh > new Date() &&
                        timeUntilNextRefreshFormatted && (
                            <Typography sx={{ display: 'block' }} variant="caption">
                                {t('page.salut.refresh.next')}: <b>{timeUntilNextRefreshFormatted}</b>
                            </Typography>
                        )}
                </Box>
            ),
        },
        {
            position: 2,
            element: <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, maxWidth:{ sm: '210px', lg: '400px'}}} minWidth={{xs: '110px'}}>
                <RefreshTimeoutSelect onChange={setRefreshTimeoutMinutes} disabled={!ready} />
                <AppDataRangeSelect onChange={setAppDataRangeMinutes} disabled={!ready} />
            </Box>,
        },
        {
            position: 2,
            element: (
                <>
                    {!hideFilter && (
                        <IconButton onClick={() => handleOpen()}>
                            {springFilter ? (
                                <FilterAltIcon fontSize="small" />
                            ) : (
                                <FilterAltOutlinedIcon fontSize="small" />
                            )}
                        </IconButton>
                    )}
                    <IconButton
                        onClick={() => refresh()}
                        title={t('page.salut.refrescar')}
                        disabled={!ready}
                        loading={appDataLoading}
                    >
                        <Icon>refresh</Icon>
                    </IconButton>
                </>
            ),
        },
    ];
    // state != null && toolbarElementsWithPositions.unshift({
    //     position: 1,
    //     element: state
    // });
    // subtitle != null && toolbarElementsWithPositions.unshift({
    //     position: 1,
    //     element: <Typography
    //         variant="caption"
    //         sx={{
    //             position: 'relative',
    //             top: '4px',
    //             color: theme.palette.text.disabled,
    //             ml: 1,
    //         }}>
    //         {subtitle}
    //     </Typography>
    // });
    goBackActive && toolbarElementsWithPositions.unshift({
        position: 0,
        element: <IconButton onClick={() => goBack('/')} sx={{ mr: 1 }}>
            <Icon>arrow_back</Icon>
        </IconButton>
    });
    return <><Toolbar
        title={<>
            {title}{state}
            <Typography
                variant="caption"
                sx={{
                    position: 'relative',
                    // top: '4px',
                    color: theme.palette.text.disabled,
                    ml: 1,
                }}>
                {computedSubtitle}
            </Typography>
        </>}
        elementsWithPositions={toolbarElementsWithPositions}
        upperToolbar />
        {dialog}
    </>;
}

export default SalutToolbar;
