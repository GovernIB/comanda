import * as React from 'react';
import { useState, useEffect } from "react";
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import {
    GridPage,
    MuiGrid,
    MuiDataGridColDef,
    springFilterBuilder,
    MuiFilter,
    FormField,
    useFilterApiRef,
    useFormApiRef,
    useResourceApiService,
    useFormContext
} from 'reactlib';
import FormFieldAdvancedSearchFilters from '../components/FormFieldAdvancedSearchFilters.tsx';
import { columnesIndicador } from '../components/sharedAdvancedSearch/advancedSearchColumns';
import PageTitle from '../components/PageTitle.tsx';

const IndicadorsFilter = (props: any) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const { isReady: entornAppApiIsReady, find: entornAppGetAll } = useResourceApiService('entornApp');
    const filterApiRef = useFilterApiRef();
    const formApiRef = useFormApiRef();
    const [entornApp, setEntornApp] = useState<any[] | null>([]);

    // Al obrir la pàgina carreguem el llistat de EntornApp actius
    useEffect(() => {
        if (entornAppApiIsReady) {
            console.log('EntornApp API ready');
            entornAppGetAll({
                unpaged: true,
                filter: 'activa : true AND app.activa : true',
            }).then(response => {
                console.log('EntornApp API response received:', response.rows.length, 'items');
                setEntornApp(response.rows);
            });
        }
    }, [entornAppApiIsReady, entornAppGetAll]);

    const netejar = () => {
        filterApiRef?.current?.clear();
    }

    return (
        <MuiFilter
            apiRef={filterApiRef}
            resourceName="indicador"
            code="indicadorFilter"
            formApiRef={formApiRef}
            commonFieldComponentProps={{ size: 'small' }}
            onSpringFilterChange={onSpringFilterChange}
            springFilterBuilder={data => {
                // Build Spring filter based on available fields in the artifact
                // Fallback to empty if no values provided
                return springFilterBuilder.and(
                    data?.entornApp && springFilterBuilder.eq('entornAppId', data?.entornApp?.id ?? data?.entornApp),
                    data?.codi && springFilterBuilder.like('codi', data?.codi),
                    data?.nom && springFilterBuilder.like('nom', data?.nom),
                ) || '';
            }}>
            <Box sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
            }}>
                <Grid container spacing={1} sx={{ flexGrow: 1, mr: 1 }}>
                    <Grid size={4}>
                        <FormField
                            name={'entornApp'}
                            type={'reference'}
                            label={t('page.indicadors.column.entornApp')}
                            required={false}
                            optionsRequest={(q) => {
                                const opts = (entornApp ?? []).map((ea: any) => ({
                                    id: ea?.id,
                                    description: ea.entornAppDescription,
                                }));
                                const filtered = q
                                    ? opts.filter(o => o.description?.toLowerCase().includes(q.toLowerCase()))
                                    : opts;
                                return Promise.resolve({ options: filtered });
                            }}
                            componentProps={{ disabled: (entornApp ?? []).length === 0 }}
                        />
                    </Grid>
                    <Grid size={4}><FormField name={'codi'} /></Grid>
                    <Grid size={4}><FormField name={'nom'} /></Grid>
                </Grid>
                <IconButton
                    onClick={netejar}
                    title={t('components.clear')}
                    sx={{ mr: 1 }}>
                    <Icon>filter_alt_off</Icon>
                </IconButton>
            </Box>
        </MuiFilter>
    );
};

const Indicadors: React.FC = () => {
    const { t } = useTranslation();
    const [filter, setFilter] = React.useState<string | undefined>(springFilterBuilder.eq('entornAppId', 0));

    const columns: MuiDataGridColDef[] = [
        { field: 'codi', flex: 1 },
        { field: 'nom', flex: 2 },
        { field: 'descripcio', flex: 3 },
        { field: 'format', flex: 1 },
        { field: 'compactable', flex: 0.6 },
        { field: 'tipusCompactacio', flex: 1.2 },
        { field: 'indicadorComptadorPerMitjana.description', headerName: t('page.indicadors.column.indicadorMitjana'), flex: 2 },
    ];
    const filterElement = <IndicadorsFilter onSpringFilterChange={setFilter}/>;

    const IndicadorForm: React.FC = () => {
        const { data } = useFormContext();
        return (
        <Grid container spacing={2}>
            <Grid size={6}><FormField name="codi" readOnly disabled /></Grid>
            <Grid size={6}><FormField name="nom" readOnly disabled /></Grid>
            <Grid size={12}><FormField name="descripcio" readOnly disabled /></Grid>
            <Grid size={6}><FormField name="format" readOnly disabled /></Grid>
            <Grid size={12}><FormField name="compactable" /></Grid>
            {data?.compactable === true && (
                <>
                    <Grid size={6}><FormField name="tipusCompactacio" /></Grid>
                    {data?.tipusCompactacio === "MITJANA" && (
                        <Grid size={12}>
                            <FormFieldAdvancedSearchFilters
                                name="indicadorComptadorPerMitjana"
                                namedQueries={["groupByNom"]}
                                advancedSearchColumns={columnesIndicador}
                                advancedSearchDataGridProps={{ rowHeight: 30 }}
                                advancedSearchDialogHeight={500}
                            />
                        </Grid>
                    )}
                </>
            )}
        </Grid>
        );
    };

    return (
        <GridPage>
            <PageTitle title={t('page.indicadors.title')} />
            <MuiGrid
                title={t('page.indicadors.title')}
                resourceName="indicador"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                filter={filter}
                popupEditActive
                toolbarHideCreate
                popupEditFormContent={<IndicadorForm />}
            />
        </GridPage>
    );
};

export default Indicadors;
