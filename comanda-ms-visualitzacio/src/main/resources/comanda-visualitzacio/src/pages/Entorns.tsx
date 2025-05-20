import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid2';
import {
    GridPage,
    MuiGrid,
    FormField,
} from 'reactlib';

const Entorns: React.FC = () => {
    const { t } = useTranslation();
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
    return <GridPage disableMargins>
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
        />
    </GridPage>;
};

export default Entorns;
