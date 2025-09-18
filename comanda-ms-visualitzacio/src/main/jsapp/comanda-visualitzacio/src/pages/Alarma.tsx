import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    FormPage,
    GridPage,
    MuiDataGrid,
    MuiForm,
    FormField,
} from 'reactlib';

const dataGridColumns = [{
    field: 'nom',
    flex: 1,
}];

export const AlarmaForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    return <FormPage>
        <MuiForm
            id={id}
            title={id ? t('page.alarmaConfig.update') : t('page.alarmaConfig.create')}
            resourceName="alarmaConfig"
            goBackLink="/alarma"
            createLink="form/{{id}}">
            <Grid container spacing={1}>
                <Grid size={2}>
                    <FormField name="tipus" />
                </Grid>
                <Grid size={10}>
                    <FormField name="nom" />
                </Grid>
                <Grid size={12}>
                    <FormField name="missatge" />
                </Grid>
                <Grid size={9}>
                    <FormField name="condicio" />
                </Grid>
                <Grid size={3}>
                    <FormField name="valor" />
                </Grid>
                <Grid size={12}>
                    <FormField name="admin" />
                </Grid>
            </Grid>
        </MuiForm>
    </FormPage>;
}

const Alarma = () => {
    const { t } = useTranslation();
    return <GridPage>
        <MuiDataGrid
            title={t('page.alarmaConfig.title')}
            resourceName="alarmaConfig"
            columns={dataGridColumns}
            toolbarType="upper"
            toolbarCreateLink="form"
            rowUpdateLink="form/{{id}}"
        />
    </GridPage>;
}

export default Alarma;