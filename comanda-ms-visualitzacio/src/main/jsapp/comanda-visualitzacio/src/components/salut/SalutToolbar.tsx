import * as React from 'react';
import { useTranslation } from 'react-i18next';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select, { SelectChangeEvent } from '@mui/material/Select';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Typography from '@mui/material/Typography';
import { useTheme } from '@mui/material/styles';
import {
    FilterApiRef,
    FormField,
    MuiDialog,
    MuiFilter,
    springFilterBuilder,
    Toolbar,
    useBaseAppContext,
    useFilterApiRef,
    useFormContext,
} from 'reactlib';
import { Box, InputLabel } from '@mui/material';
import Grid from '@mui/material/Grid';
import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import { useId } from 'react';
import { SalutEstatEnum, useSalutEstatTranslation } from '../../types/salut.model';

export type SalutToolbarProps = {
    title: string;
    subtitle?: string;
    state?: React.ReactElement;
    hideFilter?: boolean;
    groupingActive?: boolean;
    ready: boolean;
    onRefreshClick: () => void;
    goBackActive?: boolean;
    appDataLoading?: boolean;
    dataRangeDuration: DataRangeDurationType;
    setDataRangeDuration: (duration: DataRangeDurationType) => void;
    refreshDuration: RefreshDurationType;
    setRefreshDuration: (duration: RefreshDurationType) => void;
    filterData: SalutFilterDataType;
    setFilterData: (data: SalutFilterDataType) => void;
    grouping: GroupingEnum;
    setGrouping: (grouping: GroupingEnum) => void;
    lastRefresh?: Date;
    nextRefresh?: Date;
}

export const agrupacioFromMinutes = (
    intervalMinutes: number
) => {
    if (intervalMinutes <= 15) {
        return 'MINUT';
    } else if (intervalMinutes <= 60) {
        return 'MINUTS_HORA';
    } else if (intervalMinutes <= 24 * 60) {
        return 'HORA';
    } else if (intervalMinutes <= 24 * 60 * 7) {
        return 'DIA_SETMANA';
    } else {
        return 'DIA_MES';
    }
};

export enum GroupingEnum {
    APPLICATION = "APPLICATION",
    ENVIRONMENT = "ENVIRONMENT",
    NONE = "NONE",
}

const isValidGrouping = (grouping: string): grouping is GroupingEnum => {
    return Object.values(GroupingEnum).includes(grouping as GroupingEnum);
};

const getInitialGrouping = () => {
    const storedValue = localStorage.getItem('groupingForViewSelect');
    if (!storedValue || !isValidGrouping(storedValue)) {
        return GroupingEnum.APPLICATION;
    }
    return storedValue;
};

export const getIdList = (a: any[] = []) => {
    return a?.map((uo: any) => uo.id) ?? [];
};


/**
 * Filtro para mostrar las agrupaciones de gráficos en la vista de Salut. Valores posibles: {@link GroupingEnum}:
 * @param props
 * @constructor
 */
const GroupForViewSelect = (props: { disabled?: boolean; onChange: (grouping: GroupingEnum) => void; value: GroupingEnum }) => {
    const { value, onChange, disabled } = props;
    const { t } = useTranslation();
    const labelId = useId();
    const selectId = useId();

    const handleChange = (event: SelectChangeEvent) => {
        const selectedGrouping = event.target.value;
        if (isValidGrouping(selectedGrouping))
            onChange(selectedGrouping);
        else
            console.error('Invalid grouping:', selectedGrouping);
    }

    return (
        <FormControl sx={{ ml: 2, width: '12rem' }}>
            <InputLabel id={labelId}>{t($ => $.page.salut.groupingSelect.label)}</InputLabel>
            <Select
                label={t($ => $.page.salut.groupingSelect.label)}
                labelId={labelId}
                id={selectId}
                value={value}
                size="small"
                disabled={disabled}
                onChange={handleChange}
            >
                <MenuItem value={GroupingEnum.APPLICATION}>
                    {t($ => $.page.salut.groupingSelect.BY_APPLICATION)}
                </MenuItem>
                <MenuItem value={GroupingEnum.ENVIRONMENT}>
                    {t($ => $.page.salut.groupingSelect.BY_ENVIRONMENT)}
                </MenuItem>
                <MenuItem value={GroupingEnum.NONE}>
                    {t($ => $.page.salut.groupingSelect.NONE)}
                </MenuItem>
            </Select>
        </FormControl>
    );
}

const getInitialRefreshDuration = () => {
    const storedValue = localStorage.getItem('refreshTimeoutSelect');
    if (!storedValue || !isValidRefreshDuration(storedValue)) {
        return 'PT5M';
    }
    return storedValue;
};

type RefreshDurationType = 'PT1M' | 'PT5M' | 'PT10M' | 'PT30M' | 'PT1H';
const isValidRefreshDuration = (duration: string): duration is RefreshDurationType => {
    return ['PT1M', 'PT5M', 'PT10M', 'PT30M', 'PT1H'].includes(duration);
};

const RefreshTimeoutSelect: React.FC<{
    value: RefreshDurationType;
    disabled?: boolean;
    onChange: (duration: RefreshDurationType) => void;
}> = (props) => {
    const { value, onChange, disabled } = props;
    const { t } = useTranslation();

    const handleChange = (event: SelectChangeEvent) => {
        const value = event.target.value;
        if (isValidRefreshDuration(value)) {
            onChange(value);
        } else {
            console.error('Invalid refresh duration:', value);
        }
    };

    return (
        <FormControl
            title={t($ => $.page.salut.refreshperiod.title)}
        >
            <Select
                value={value}
                size="small"
                disabled={disabled}
                onChange={handleChange}
                startAdornment={
                    <InputAdornment position="start">
                        <Icon>update</Icon>
                    </InputAdornment>
                }
                sx={{ mr: 1 }}
                slotProps={{
                    input: {
                        'aria-label': t($ => $.page.salut.refreshperiod.title)
                    }
                }}
            >
                <MenuItem value={'PT1M'}>{t($ => $.page.salut.refreshperiod.PT1M)}</MenuItem>
                <MenuItem value={'PT5M'}>{t($ => $.page.salut.refreshperiod.PT5M)}</MenuItem>
                <MenuItem value={'PT10M'}>{t($ => $.page.salut.refreshperiod.PT10M)}</MenuItem>
                <MenuItem value={'PT30M'}>{t($ => $.page.salut.refreshperiod.PT30M)}</MenuItem>
                <MenuItem value={'PT1H'}>{t($ => $.page.salut.refreshperiod.PT1H)}</MenuItem>
            </Select>
        </FormControl>
    );
};

// Get the stored value from localStorage or use initialValue
const getInitialDateRangeDuration = () => {
    const storedValue = localStorage.getItem('appDataRangeSelect');
    if (!storedValue || !isValidDataRangeDuration(storedValue)) {
        return 'PT15M';
    }
    return storedValue;
};

type DataRangeDurationType = 'PT15M' | 'PT1H' | 'P1D' | 'P7D' | 'P1M';
const isValidDataRangeDuration = (duration: string): duration is DataRangeDurationType => {
    return ['PT15M', 'PT1H', 'P1D', 'P7D', 'P1M'].includes(duration);
};
const AppDataRangeSelect = (props: {
    disabled?: boolean;
    onChange: (duration: DataRangeDurationType) => void;
    value: DataRangeDurationType;
}) => {
    const { value, onChange, disabled } = props;
    const { t } = useTranslation();

    const handleChange = (event: SelectChangeEvent) => {
        const value = event.target.value;
        if (isValidDataRangeDuration(value)) {
            onChange(value);
        } else {
            console.error('Invalid data range duration:', value);
        }
    };

    return (
        <FormControl
            title={t($ => $.page.salut.timerange.title)}
        >
            <Select
                value={value}
                size="small"
                disabled={disabled}
                onChange={handleChange}
                startAdornment={
                    <InputAdornment position="start">
                        <Icon>date_range</Icon>
                    </InputAdornment>
                }
                sx={{ mr: 1 }}
                slotProps={{
                    input: {
                        'aria-label': t($ => $.page.salut.timerange.title)
                    }
                }}
            >
                <MenuItem value={'PT15M'}>{t($ => $.page.salut.timerange.PT15M)}</MenuItem>
                <MenuItem value={'PT1H'}>{t($ => $.page.salut.timerange.PT1H)}</MenuItem>
                <MenuItem value={'P1D'}>{t($ => $.page.salut.timerange.P1D)}</MenuItem>
                <MenuItem value={'P7D'}>{t($ => $.page.salut.timerange.P7D)}</MenuItem>
                <MenuItem value={'P1M'}>{t($ => $.page.salut.timerange.P1M)}</MenuItem>
            </Select>
        </FormControl>
    );
};

function formatTimeDifference(otherDate: Date) {
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


const useTimeUntilNextRefreshFormatted = (nextRefresh?: Date | null) => {
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

const SalutEntornAppFilterForm: React.FC = () => {
    const { data } = useFormContext();

    return <Grid container spacing={1}>
        <Grid size={12}>
            <FormField name="app" componentProps={{ size: 'small', }} multiple optionsUnpaged
                        advancedSearchColumns={[{
                            field: 'codi',
                            flex: 0.5,
                        }, {
                            field: 'nom',
                            flex: 2,
                        }]}
                       filter={
                        springFilterBuilder.and(
                            springFilterBuilder.eq('activa', true),
                            springFilterBuilder.exists(
                                springFilterBuilder.and(springFilterBuilder.inn('entornApps.entorn.id', getIdList(data?.entorn)))
                            )
                        )}
            />
        </Grid>
        <Grid size={12}>
            <FormField name="entorn" componentProps={{ size: 'small', }} multiple optionsUnpaged
                        advancedSearchColumns={[{
                            field: 'codi',
                            flex: 0.5,
                        }, {
                            field: 'nom',
                            flex: 2,
                        }]}
                       filter={springFilterBuilder.exists(
                           springFilterBuilder.and(springFilterBuilder.inn('entornAppEntities.app.id', getIdList(data?.app)))
                       )}
            />
        </Grid>
        <Grid size={12}>
            <FormField name="estatsSalut" componentProps={{ size: 'small', }} multiple/>
        </Grid>
    </Grid>
}

export const salutEntornAppFilterBuilder = (data: SalutFilterDataType) => {
    if (data == null) return '';
    return springFilterBuilder.and(
        springFilterBuilder.inn('app.id', getIdList(data?.app)),
        springFilterBuilder.inn('entorn.id', getIdList(data?.entorn)),
    )
}

const SalutEntornAppFilter: React.FC<{
    initialData: SalutFilterDataType;
    setData: (newData: SalutFilterDataType) => void;
    apiRef: FilterApiRef;
}> = (props) => {
    const { initialData, setData, apiRef } = props;

    return (
        <MuiFilter
            resourceName="entornApp"
            code="salut_entornApp_filter"
            springFilterBuilder={(data: SalutFilterDataType) => {
                setData(data);
                return salutEntornAppFilterBuilder(data);
            }}
            initialData={initialData}
            apiRef={apiRef}
            buttonControlled
        >
            <SalutEntornAppFilterForm />
        </MuiFilter>
    );
};

const FILTER_DATA_LOCALSTORAGE_KEY = 'filterDataSalut';

const getInitialFilterData = () => {
    const storedValue = localStorage.getItem(FILTER_DATA_LOCALSTORAGE_KEY);
    return storedValue ? JSON.parse(storedValue) : {};
};

export type SalutFilterDataType = {
    app?: [{
        id: string;
        description: string;
    }];
    entorn?: [{
        id: string;
        description: string;
    }];
    estatsSalut?: SalutEstatEnum[];
};

const useSalutEntornAppFilter = ({
    filterData,
    setFilterData,
}: {
    filterData: SalutFilterDataType;
    setFilterData: (data: SalutFilterDataType) => void;
}) => {
    const { t } = useTranslation();

    const [open, setOpen] = React.useState<boolean>(false);
    const filterRef = useFilterApiRef();

    const cercar = () => {
        filterRef?.current?.filter?.();
        setOpen(false);
    };
    const netejar = () => {
        filterRef?.current?.clear?.();
    };

    const handleOpen = () => {
        setOpen(true);
    };

    const handleClose = () => {
        setOpen(false);
    };

    const dialog = (
        <MuiDialog
            open={open}
            closeCallback={handleClose}
            title={t($ => $.page.salut.filtrar)}
            componentProps={{ fullWidth: true, maxWidth: 'sm' }}
            buttons={[
                {
                    value: 'clear',
                    text: t($ => $.components.clear),
                    componentProps: {
                        variant: 'outlined',
                        sx: { borderRadius: '4px' },
                    },
                },
                {
                    value: 'search',
                    text: t($ => $.components.search),
                    icon: 'filter_alt',
                    componentProps: {
                        variant: 'contained',
                        sx: { borderRadius: '4px' },
                    },
                },
            ]}
            buttonCallback={(value: unknown): void => {
                if (value === 'clear') netejar();
                if (value === 'search') cercar();
            }}
        >
            <SalutEntornAppFilter
                initialData={filterData}
                setData={setFilterData}
                apiRef={filterRef}
            />
        </MuiDialog>
    );

    return {
        handleOpen,
        handleClose,
        dialog,
    };
};

export const useSalutToolbarState = () => {
    const [dataRangeDuration, setDataRangeDuration] = React.useState<DataRangeDurationType>(
        getInitialDateRangeDuration
    );
    const [refreshDuration, setRefreshDuration] = React.useState<RefreshDurationType>(getInitialRefreshDuration);
    const [filterData, setFilterData] = React.useState<SalutFilterDataType>(getInitialFilterData);
    const [grouping, setGrouping] = React.useState<GroupingEnum>(getInitialGrouping());
    return {
        dataRangeDuration,
        setDataRangeDuration: (duration: DataRangeDurationType) => {
            localStorage.setItem('appDataRangeSelect', duration);
            setDataRangeDuration(duration);
        },
        refreshDuration,
        setRefreshDuration: (duration: RefreshDurationType) => {
            localStorage.setItem('refreshTimeoutSelect', duration);
            setRefreshDuration(duration);
        },
        filterData,
        setFilterData: (data: SalutFilterDataType) => {
            localStorage.setItem(FILTER_DATA_LOCALSTORAGE_KEY, JSON.stringify(data));
            setFilterData(data);
        },
        grouping,
        setGrouping: (grouping: GroupingEnum) => {
            localStorage.setItem('groupingForViewSelect', grouping);
            setGrouping(grouping);
        },
    };
};

export const SalutToolbar: React.FC<SalutToolbarProps> = (props) => {
    const {
        title,
        subtitle,
        hideFilter,
        groupingActive,
        state,
        ready,
        onRefreshClick,
        goBackActive,
        appDataLoading,
        dataRangeDuration,
        setDataRangeDuration,
        refreshDuration,
        setRefreshDuration,
        lastRefresh,
        nextRefresh,
        filterData,
        setFilterData,
        grouping,
        setGrouping,
    } = props;
    const { t } = useTranslation();
    const { goBack } = useBaseAppContext();
    const theme = useTheme();
    const timeUntilNextRefreshFormatted = useTimeUntilNextRefreshFormatted(nextRefresh);

    const { handleOpen, dialog } = useSalutEntornAppFilter({ filterData, setFilterData });
    const springFilter = salutEntornAppFilterBuilder(filterData);

    const { tTitle } = useSalutEstatTranslation();
    const computedSubtitle = React.useMemo(() => {
        if (subtitle != null) return subtitle;
        const apps = filterData?.app?.map(a => a.description).join(", ");
        const entorns = filterData?.entorn?.map(e => e.description).join(", ");
        const estats = filterData?.estatsSalut?.map(estat => tTitle(estat)).join(", ");
        const parts = [apps, entorns, estats].filter(Boolean);
        if (parts.length === 0) {
            return t($ => $.page.salut.senseFiltres);
        }
        return parts.join(" - ");
    }, [filterData, subtitle, t]);

    const toolbarElementsWithPositions = [
        {
            position: 2,
            element: (
                <Box sx={{ mr: 2, minWidth: { xs: '50px', sm: '135px' } }}>
                    {lastRefresh != null && (
                        <Typography sx={{ display: 'block' }} variant="caption">
                            {t($ => $.page.salut.refresh.last)}:{' '}
                            <b>{lastRefresh.toLocaleTimeString()}</b>
                        </Typography>
                    )}
                    {nextRefresh != null &&
                        nextRefresh > new Date() &&
                        timeUntilNextRefreshFormatted && (
                            <Typography sx={{ display: 'block' }} variant="caption">
                                {t($ => $.page.salut.refresh.next)}:{' '}
                                <b>{timeUntilNextRefreshFormatted}</b>
                            </Typography>
                        )}
                </Box>
            ),
        },
        {
            position: 2,
            element: (
                <Box
                    sx={{
                        display: 'flex',
                        flexWrap: 'wrap',
                        gap: 1,
                    }}
                >
                    <RefreshTimeoutSelect
                        value={refreshDuration}
                        onChange={setRefreshDuration}
                        disabled={!ready}
                    />
                    <AppDataRangeSelect
                        value={dataRangeDuration}
                        onChange={setDataRangeDuration}
                        disabled={!ready}
                    />
                </Box>
            ),
        },
        {
            position: 2,
            element: (
                <IconButton
                    onClick={onRefreshClick}
                    title={t($ => $.page.salut.refrescar)}
                    disabled={!ready}
                    loading={appDataLoading}
                >
                    <Icon>refresh</Icon>
                </IconButton>
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
    if (goBackActive) {
        toolbarElementsWithPositions.unshift({
            position: 0,
            element: (
                <IconButton
                    onClick={() => goBack('/')}
                    sx={{ mr: 1 }}
                    title={t($ => $.page.salut.goBack)}
                >
                    <Icon>arrow_back</Icon>
                </IconButton>
            ),
        });
    }
    toolbarElementsWithPositions.unshift({
        position: 1,
        element: (
            <>
                {!hideFilter &&
                    <IconButton
                        onClick={() => handleOpen()}
                        title={t($ => $.page.salut.filtrar)}
                    >
                        {springFilter ? (
                            <FilterAltIcon fontSize="small" />
                        ) : (
                            <FilterAltOutlinedIcon fontSize="small" />
                        )}
                    </IconButton>
                }
                <Typography
                    variant="caption"
                    sx={{
                        position: 'relative',
                        // top: '4px',
                        color: theme.palette.text.disabled,
                        mx: 1,
                    }}
                >
                    {computedSubtitle}
                </Typography>
                {state}
            </>
        ),
    });
    if (groupingActive) {
        toolbarElementsWithPositions.unshift({
            position: 1,
            element: (
                <Box
                    sx={{
                        display: 'flex',
                        flexWrap: 'wrap',
                        gap: 1,
                        maxWidth: { sm: '210px', lg: '400px' },
                    }}
                    minWidth={{ xs: '110px' }}
                >
                    <GroupForViewSelect value={grouping} onChange={setGrouping} disabled={!ready} />
                </Box>
            ),
        });
    }

    return (
        <>
            <Toolbar
                title={title}
                elementsWithPositions={toolbarElementsWithPositions}
                upperToolbar
                sx={{
                    backgroundColor: theme.palette.mode === 'dark' ? theme.palette.grey['900'] : theme.palette.grey['200'],
                }}
            />
            {dialog}
        </>
    );
}

export default SalutToolbar;
