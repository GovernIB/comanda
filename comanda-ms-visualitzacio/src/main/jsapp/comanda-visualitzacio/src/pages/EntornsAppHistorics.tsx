import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { Box, Chip, CircularProgress, Icon, IconButton, Stack, Tooltip } from '@mui/material';
import {
    GridPage,
    MuiDataGrid,
    MuiFilter,
    FormField,
    useFilterApiRef,
    useFormApiRef,
    springFilterBuilder as builder,
    dateFormatLocale,
    useResourceApiService,
} from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';
import { truncateHashRevisio } from './salut/dataFetching.ts';

type EntornAppHistFilterProps = {
    onSpringFilterChange: (springFilter?: string) => void;
};

const EntornAppHistFilter: React.FC<EntornAppHistFilterProps> = ({ onSpringFilterChange }) => {
    const { t } = useTranslation();
    const [moreFields, setMoreFields] = React.useState<boolean>(false);
    const filterApiRef = useFilterApiRef();
    const formApiRef = useFormApiRef();

    const netejar = () => {
        filterApiRef.current?.clear();
    };

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
            formApiRef={formApiRef}
            resourceName="entornAppHist"
            code="entornAppHist_filter"
            detached
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
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
                    <IconButton onClick={netejar} title={t($ => $.components.clear)}>
                        <Icon>filter_alt_off</Icon>
                    </IconButton>
                    <IconButton
                        onClick={() => setMoreFields(mf => !mf)}
                        title={t($ => $.page.entornAppHist.filter.more)}
                        color={moreFields ? 'primary' : 'default'}
                    >
                        <Icon>filter_list</Icon>
                    </IconButton>
                </Box>
            </Box>
        </MuiFilter>
    );
};

const EntornAppHist: React.FC = () => {
    const { t } = useTranslation();
    const [filter, setFilter] = React.useState<string | undefined>();
    const { isReady: apiIsReadyEntornApp, find: apiFindEntornApp } = useResourceApiService('entornApp');
    const [entornApps, setEntornApps] = React.useState<any[]>();

    React.useEffect(() => {
        if (apiIsReadyEntornApp) {
            apiFindEntornApp({ unpaged: true }).then(response => {
                setEntornApps(response.rows);
            });
        }
    }, [apiIsReadyEntornApp]);

    if (!entornApps) {
        return (
            <GridPage>
                <PageTitle title={t($ => $.page.entornAppHist.title)} />
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        justifyContent: 'center',
                        alignItems: 'center',
                        height: '400px',
                    }}
                >
                    <CircularProgress size={80} />
                </Box>
            </GridPage>
        );
    }

    const columns = [
        {
            field: 'entornApp',
            flex: 1.5,
            minWidth: 150,
            valueFormatter: (value?: any) => {
                if (value?.id == null) return '';
                const entornApp = entornApps.find(ea => ea.id === value?.id);
                return entornApp?.entornAppDescription ?? '';
            },
        },
        {
            field: 'data',
            flex: 1,
            minWidth: 150,
            valueFormatter: (value: any) => dateFormatLocale(value, true),
        },
        {
            field: 'versio',
            flex: 1,
            headerName: t($ => $.page.entornAppHist.versioRevisio),
            sortable: false,
            minWidth: 165,
            renderCell: (param: any) => (
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
    ];

    return (
        <GridPage>
            <PageTitle title={t($ => $.page.entornAppHist.title)} />
            <MuiDataGrid
                title={t($ => $.page.entornAppHist.title)}
                resourceName="entornAppHist"
                columns={columns}
                toolbarType="upper"
                toolbarAdditionalRow={
                    <EntornAppHistFilter onSpringFilterChange={setFilter} />
                }
                filter={filter}
                paginationActive
                readOnly
            />
        </GridPage>
    );
};

export default EntornAppHist;