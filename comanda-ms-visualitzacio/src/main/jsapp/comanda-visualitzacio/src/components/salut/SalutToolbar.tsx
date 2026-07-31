import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Menu from '@mui/material/Menu';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
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
    useCloseDialogButtons,
    useFilterApiRef,
    useFormContext,
} from 'reactlib';
import { Box, Button, ButtonGroup } from '@mui/material';
import Grid from '@mui/material/Grid';
import FilterAltOutlinedIcon from '@mui/icons-material/FilterAltOutlined';
import FilterAltIcon from '@mui/icons-material/FilterAlt';
import { SalutEstatEnum, useSalutEstatTranslation } from '../../types/salut.model';
import VersionsEntorns from '../../pages/VersionsEntorns';
import EntornAppHist from '../../pages/EntornsAppHistorics';

export type SalutToolbarProps = {
    title: string;
    subtitle?: string;
    state?: React.ReactElement;
    hideFilter?: boolean;
    hideAppVersioning?: boolean;
    groupingActive?: boolean;
    ready: boolean;
    onRefreshClick: () => void;
    goBackActive?: boolean;
    appDataLoading?: boolean;
    dataRangeDuration: DataRangeDurationType;
    setDataRangeDuration: (duration: DataRangeDurationType) => void;
    filterData: SalutFilterDataType;
    setFilterData: (data: SalutFilterDataType) => void;
    grouping: GroupingEnum;
    setGrouping: (grouping: GroupingEnum) => void;
    lastRefresh?: Date;
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
    return a?.map?.((uo: any) => uo.id) ?? [];
};


/**
 * Filtro para mostrar las agrupaciones de gráficos en la vista de Salut. Valores posibles: {@link GroupingEnum}:
 * @param props
 * @constructor
 */
const GroupForViewSelect = (props: {
    disabled?: boolean;
    onChange: (grouping: GroupingEnum) => void;
    value: GroupingEnum;
}) => {
    const { value, onChange, disabled } = props;
    const { t } = useTranslation();

    return (
        <Box sx={{ ml: 2, display: 'flex', flexDirection: 'column' }}>
            <ButtonGroup
                size="small"
                disabled={disabled}
                aria-label="grouping selection"
                sx={{ flexWrap: 'nowrap' }}
                variant="outlined"
            >
                <Button
                    variant={value === GroupingEnum.APPLICATION ? 'contained' : 'outlined'}
                    onClick={() => onChange(GroupingEnum.APPLICATION)}
                    title={t($ => $.page.salut.groupingSelect.BY_APPLICATION)}
                >
                    <Icon>apps</Icon>
                </Button>
                <Button
                    variant={value === GroupingEnum.ENVIRONMENT ? 'contained' : 'outlined'}
                    onClick={() => onChange(GroupingEnum.ENVIRONMENT)}
                    title={t($ => $.page.salut.groupingSelect.BY_ENVIRONMENT)}
                >
                    <Icon>layers</Icon>
                </Button>
                <Button
                    variant={value === GroupingEnum.NONE ? 'contained' : 'outlined'}
                    onClick={() => onChange(GroupingEnum.NONE)}
                    title={t($ => $.page.salut.groupingSelect.NONE)}
                >
                    <Icon>block</Icon>
                </Button>
            </ButtonGroup>
        </Box>
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


const SalutEntornAppFilterForm: React.FC = () => {
    const { data } = useFormContext();

    return <Grid container spacing={1} sx={{ mt: 1 }}>
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
        // S'usa reset enlloc de clear per a posar a null el filtre sencer, ignorant l'initialData donat
        filterRef?.current?.reset?.();
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
    const [filterData, setFilterData] = React.useState<SalutFilterDataType>(getInitialFilterData);
    const [grouping, setGrouping] = React.useState<GroupingEnum>(getInitialGrouping());

    const handleSetDataRangeDuration = React.useCallback((duration: DataRangeDurationType) => {
        localStorage.setItem('appDataRangeSelect', duration);
        setDataRangeDuration(duration);
    }, []);

    const handleSetFilterData = React.useCallback((data: SalutFilterDataType) => {
        localStorage.setItem(FILTER_DATA_LOCALSTORAGE_KEY, JSON.stringify(data));
        setFilterData(data);
    }, []);

    const handleSetGrouping = React.useCallback((grouping: GroupingEnum) => {
        localStorage.setItem('groupingForViewSelect', grouping);
        setGrouping(grouping);
    }, []);

    return {
        dataRangeDuration,
        setDataRangeDuration: handleSetDataRangeDuration,
        filterData,
        setFilterData: handleSetFilterData,
        grouping,
        setGrouping: handleSetGrouping,
    };
};

export const SalutToolbar: React.FC<SalutToolbarProps> = React.memo((props) => {
    const {
        title,
        subtitle,
        hideFilter,
        hideAppVersioning,
        groupingActive,
        state,
        ready,
        onRefreshClick,
        goBackActive,
        appDataLoading,
        dataRangeDuration,
        setDataRangeDuration,
        lastRefresh,
        filterData,
        setFilterData,
        grouping,
        setGrouping,
    } = props;
    const { t } = useTranslation();
    const { goBack } = useBaseAppContext();
    const theme = useTheme();

    const { handleOpen, dialog } = useSalutEntornAppFilter({ filterData, setFilterData });
    const springFilter = salutEntornAppFilterBuilder(filterData);

    const [versionMenuAnchorEl, setVersionMenuAnchorEl] = React.useState<null | HTMLElement>(null);
    const isVersionMenuOpen = Boolean(versionMenuAnchorEl);

    const handleVersionMenuClick = (event: React.MouseEvent<HTMLButtonElement>) => {
        setVersionMenuAnchorEl(event.currentTarget);
    };

    const handleVersionMenuClose = () => {
        setVersionMenuAnchorEl(null);
    };

    const [openVersionsEntornDialog, setOpenVersionsEntornDialog] = React.useState<boolean>(false);
    const [openEntornAppHistDialog, setOpenEntornAppHistDialog] = React.useState<boolean>(false);

    const handleOpenVersionsEntorn = () => {
        handleVersionMenuClose();
        setOpenVersionsEntornDialog(true);
    };

    const handleOpenEntornAppHist = () => {
        handleVersionMenuClose();
        setOpenEntornAppHistDialog(true);
    };

    const closeDialogButtons = useCloseDialogButtons();

    const { tTitle } = useSalutEstatTranslation();
    const computedSubtitle = React.useMemo(() => {
        if (subtitle != null) return subtitle;
        const apps = filterData?.app?.map?.(a => a.description).join(", ");
        const entorns = filterData?.entorn?.map?.(e => e.description).join(", ");
        const estats = filterData?.estatsSalut?.map?.(estat => tTitle(estat)).join(", ");
        const parts = [apps, entorns, estats].filter(Boolean);
        if (parts.length === 0) {
            return t($ => $.page.salut.senseFiltres);
        }
        return parts.join(" - ");
    }, [filterData, subtitle, t]);
    const getRefreshButtonTitle = () => {
        let title = t($ => $.page.salut.refrescar);
        if (lastRefresh != null)
            title += ` (${t($ => $.page.salut.refresh.last)}: ${lastRefresh.toLocaleTimeString()})`;
        return title;
    };

    const toolbarElementsWithPositions = [
        {
            position: 2,
            element: (
                <AppDataRangeSelect
                    value={dataRangeDuration}
                    onChange={setDataRangeDuration}
                    disabled={!ready}
                />
            ),
        },
        {
            position: 2,
            element: (
                <IconButton
                    onClick={onRefreshClick}
                    title={getRefreshButtonTitle()}
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
                {!hideAppVersioning && (
                    <>
                        <Button
                            startIcon={<Icon>format_list_numbered_rtl</Icon>}
                            variant="outlined"
                            onClick={handleVersionMenuClick}
                            id="version-menu-button"
                            aria-controls={isVersionMenuOpen ? 'version-menu' : undefined}
                            aria-haspopup="true"
                            aria-expanded={isVersionMenuOpen ? 'true' : undefined}
                        >
                            {t($ => $.menu.versions)}
                        </Button>
                        <Menu
                            id="version-menu"
                            anchorEl={versionMenuAnchorEl}
                            open={isVersionMenuOpen}
                            onClose={handleVersionMenuClose}
                            MenuListProps={{
                                'aria-labelledby': 'version-menu-button',
                            }}
                        >
                            <MenuItem onClick={handleOpenVersionsEntorn}>
                                <ListItemIcon>
                                    <Icon>format_list_numbered_rtl</Icon>
                                </ListItemIcon>
                                <ListItemText>{t($ => $.menu.versionsEntorn)}</ListItemText>
                            </MenuItem>
                            <MenuItem onClick={handleOpenEntornAppHist}>
                                <ListItemIcon>
                                    <Icon>update</Icon>
                                </ListItemIcon>
                                <ListItemText>{t($ => $.menu.entornAppHist)}</ListItemText>
                            </MenuItem>
                        </Menu>
                    </>
                )}
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
            <MuiDialog
                open={openVersionsEntornDialog}
                closeCallback={() => setOpenVersionsEntornDialog(false)}
                buttonCallback={() => setOpenVersionsEntornDialog(false)}
                buttons={closeDialogButtons}
                componentProps={{ fullWidth: true, maxWidth: 'lg' }}
            >
                <Box sx={{ mt: 3, minHeight: '400px', height: '600px', display: 'flex', flexDirection: 'column' }}>
                    <VersionsEntorns />
                </Box>
            </MuiDialog>
            <MuiDialog
                open={openEntornAppHistDialog}
                closeCallback={() => setOpenEntornAppHistDialog(false)}
                buttonCallback={() => setOpenEntornAppHistDialog(false)}
                buttons={closeDialogButtons}
                componentProps={{ fullWidth: true, maxWidth: 'lg' }}
            >
                <Box sx={{ mt: 3, minHeight: '400px', height: '600px', display: 'flex', flexDirection: 'column' }}>
                    <EntornAppHist />
                </Box>
            </MuiDialog>
        </>
    );
})
export default SalutToolbar;
