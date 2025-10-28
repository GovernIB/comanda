import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    FormField,
    useResourceApiService,
} from 'reactlib';
import { GridRowOrderChangeParams } from '@mui/x-data-grid-pro';

const Entorns: React.FC = () => {
    const { t } = useTranslation();
    const {patch} = useResourceApiService('entorn');
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
    const onRowOrderChange = (params:GridRowOrderChangeParams) => {
        patch(params?.row?.id, {data: {ordre: params?.targetIndex + 1 }})
    }
    return <GridPage>
        <MuiGrid
            title={t('page.entorns.title')}
            resourceName="entorn"
            columns={columns}
            toolbarType="upper"
            paginationActive
            popupEditActive
            popupEditFormContent={<Grid container spacing={2}>
                <Grid size={4}><FormField name="codi" /></Grid>
                <Grid size={8}></Grid>
                <Grid size={12}><FormField name="nom" /></Grid>
            </Grid>}
            rowReordering
            onRowOrderChange={onRowOrderChange}
        />
    </GridPage>;
};

export default Entorns;
