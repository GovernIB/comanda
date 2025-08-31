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
    useResourceApiService
} from 'reactlib';

type DimensionsFilterProps = { onSpringFilterChange: (springFilter?: string) => void };
const DimensionsFilter = (props: DimensionsFilterProps) => {
    const { onSpringFilterChange } = props;
    const { t } = useTranslation();
    const { isReady: entornAppApiIsReady, find: entornAppGetAll } = useResourceApiService('entornApp');
    const filterApiRef = useFilterApiRef();
    const formApiRef = useFormApiRef();
    type EntornAppItem = { id: string | number; entornAppDescription?: string };
    const [entornApp, setEntornApp] = useState<EntornAppItem[] | null>([]);

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
            resourceName="dimensio"
            code="dimensioFilter"
            persistentState
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
                            required={false}
                            optionsRequest={(q) => {
                                const opts = (entornApp ?? []).map((ea: EntornAppItem) => ({
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

const Dimensions: React.FC = () => {
    const { t } = useTranslation();
    const [filter, setFilter] = React.useState<string | undefined>(springFilterBuilder.eq('entornAppId', 0));

    const columns: MuiDataGridColDef[] = [
        { field: 'codi', flex: 1 },
        { field: 'nom', flex: 2 },
        { field: 'descripcio', flex: 4 },
        { field: 'agrupableCount', headerName: t('page.dimensions.column.agrupacions'), flex: 1 },
    ];
    const filterElement = <DimensionsFilter onSpringFilterChange={setFilter}/>;

    return (
        <GridPage>
            <MuiGrid
                title={t('page.dimensions.title')}
                resourceName="dimensio"
                columns={columns}
                toolbarType="upper"
                paginationActive
                toolbarHideQuickFilter
                toolbarAdditionalRow={filterElement}
                filter={filter}
                rowAdditionalActions={[{
                    label: t('page.dimensions.values'),
                    icon: 'list',
                    linkTo: 'valor/{{id}}'
                }]}
                readOnly
            />
        </GridPage>
    );
};

export default Dimensions;
