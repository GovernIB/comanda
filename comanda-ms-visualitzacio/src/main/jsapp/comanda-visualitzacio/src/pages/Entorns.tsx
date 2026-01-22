import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import { GridPage, FormField, MuiDataGrid } from 'reactlib';
import useReordering from '../hooks/reordering.tsx';
import PageTitle from '../components/PageTitle.tsx';

const columns = [
    {
        field: 'codi',
        flex: 1,
    },
    {
        field: 'nom',
        flex: 3,
    },
];

const Entorns: React.FC = () => {
    const { t } = useTranslation();
    const { dataGridProps, loadingElement } = useReordering('entorn');
    return (
        <GridPage>
            <PageTitle title={t(($) => $.page.entorns.title)} />
            <MuiDataGrid
                title={t(($) => $.page.entorns.title)}
                resourceName="entorn"
                columns={columns}
                toolbarType="upper"
                paginationActive
                popupEditActive
                popupEditFormContent={
                    <Grid container spacing={2}>
                        <Grid size={4}>
                            <FormField name="codi" />
                        </Grid>
                        <Grid size={8}></Grid>
                        <Grid size={12}>
                            <FormField name="nom" />
                        </Grid>
                    </Grid>
                }
                toolbarElementsWithPositions={[
                    {
                        position: 1,
                        element: loadingElement,
                    },
                ]}
                {...dataGridProps}
            />
        </GridPage>
    );
};

export default Entorns;
