import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { Box, Chip, Stack, Tooltip } from '@mui/material';
import {
    MuiDataGrid,
    MuiFilter,
    FormField,
    useFilterApiRef,
    springFilterBuilder as builder,
    dateFormatLocale,
    MuiDataGridColDef,
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';
import { truncateHashRevisio } from './salut/dataFetching.ts';
import { FilterAdvancedButton, FilterClearButton, FilterCount, ScreenFrame } from '../lot2';
import useScreenGridProps from '../hooks/useScreenGridProps.tsx';
import { screenIcons } from '../util/screenIcons.ts';
import useAppliedSpringFilter from '../hooks/useAppliedSpringFilter.ts';

type EntornAppHistFilterProps = {
    onSpringFilterChange: (springFilter?: string) => void;
};

const EntornAppHistFilter: React.FC<EntornAppHistFilterProps> = ({ onSpringFilterChange }) => {
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const filterApiRef = useFilterApiRef();
    const { appliedSpringFilter, handleSpringFilterChange } = useAppliedSpringFilter(onSpringFilterChange);

    const springFilterBuilder = (data: any): string => {
        return builder.and(
            data?.entornApp && builder.eq('entornApp.id', data?.entornApp?.id),
            data?.app && builder.exists(builder.eq('entornApp.app.id', data?.app?.id)),
            data?.entorn && builder.exists(builder.eq('entornApp.entorn.id', data?.entorn?.id)),
            data?.versio && builder.like('versio', data?.versio),
            data?.revisio && builder.like('revisio', data?.revisio),
            data?.canviVersio && builder.eq('canviVersio', `'${data?.canviVersio}'`),
            builder.between("data", `'${data?.dataDesde}'`, `'${data?.dataFins}'`),
        ) || '';
    };

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="entornAppHist"
            code="entornAppHist_filter"
            detached
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={handleSpringFilterChange}
            springFilterBuilder={springFilterBuilder}
        >
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={{ xs: 12, sm: 6}}>
                        <FormField name="app"
                            filter={builder.eq('activa', true)}
                            advancedSearchColumns={[{ field: 'codi', flex: 1, }, { field: 'nom', flex: 2, },]}
                        />
                    </Grid>
                    <Grid size={{ xs: 12, sm: 6}}>
                        <FormField name="entorn"
                            advancedSearchColumns={[{ field: 'codi', flex: 1, }, { field: 'nom', flex: 2, },]}
                        />
                    </Grid>
                    {moreFields && (
                        <>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <FormField name="versio" />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 6 }}>
                                <FormField name="revisio" />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 4 }}>
                                <FormField name="canviVersio" />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 4 }}>
                                <FormField name="dataDesde" />
                            </Grid>
                            <Grid size={{ xs: 12, sm: 4 }}>
                                <FormField name="dataFins" />
                            </Grid>
                        </>
                    )}
                </Grid>
                <Box sx={{ display: 'flex', flexDirection: 'row' }}>
                    <FilterClearButton />
                    <FilterAdvancedButton open={moreFields} onClick={() => setMoreFields(mf => !mf)} />
                </Box>
            </Box>
            <FilterCount springFilter={appliedSpringFilter} />
        </MuiFilter>
    );
};

const EntornAppHist: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.historic, t($ => $.page.entornAppHist.title));
    const [filter, setFilter] = React.useState<string | undefined>();
    const columns: MuiDataGridColDef[] = React.useMemo(() => ([
        {
            field: 'app',
            flex: 0.2,
            minWidth: 100,
        },
        {
            field: 'entorn',
            flex: 1,
            minWidth: 150,
        },
        {
            field: 'data',
            flex: 0.5,
            minWidth: 160,
            valueFormatter: (value: unknown) => dateFormatLocale(value, true),
        },
        {
            field: 'versio',
            flex: 0.5,
            headerName: t($ => $.page.entornAppHist.versioRevisio),
            sortable: false,
            minWidth: 165,
            renderCell: (param) => (
                <Stack direction="row" spacing={0.5} alignItems="center" flexWrap="wrap">
                    <>{param?.row?.versio != null && (
                        <Chip label={param?.row?.versio} color={param?.row?.canviVersio ? 'success' : 'secondary'} />
                    )}
                        {param?.row?.revisio != null && (
                            <Tooltip title={param?.row?.revisio} arrow placement="top">
                                <Chip
                                    label={truncateHashRevisio(param?.row?.revisio)}
                                    size="small"
                                    sx={{ bgcolor: 'info.light', color: 'info.contrastText', }}
                                />
                            </Tooltip>
                        )}</>
                </Stack>
            )
        },
    ]), [t]);

    return (
        <>
            <PageTitle title={t($ => $.page.entornAppHist.title)} />
            <ScreenFrame>
                <MuiDataGrid
                    {...screenGridProps}
                    resourceName="entornAppHist"
                    columns={columns}
                    toolbarAdditionalRow={
                        <EntornAppHistFilter onSpringFilterChange={setFilter} />
                    }
                    filter={filter}
                    paginationActive
                    readOnly
                />
            </ScreenFrame>
        </>
    );
};

export default EntornAppHist;
