import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiDataGrid,
    FormField,
    useFormContext,
} from 'reactlib';

const TemplateEstilsForm: React.FC = () => {
    const { data } = useFormContext();
    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="nom" />
            </Grid>
            <Grid size={12}>
                <FormField name="colorsClar" label="Colors Clar (separats per coma)" />
            </Grid>
            <Grid size={12}>
                <FormField name="colorsFosc" label="Colors Fosc (separats per coma)" />
            </Grid>
            <Grid size={12}>
                <FormField name="destacatsClar" label="Destacats Clar (separats per coma)" />
            </Grid>
            <Grid size={12}>
                <FormField name="destacatsFosc" label="Destacats Fosc (separats per coma)" />
            </Grid>
            <Grid size={12}>
                <FormField name="estilsDefaultJson" label="Estils Default (JSON)" multiline rows={4} />
            </Grid>
        </Grid>
    );
};

const TemplateEstils: React.FC = () => {
    const { t } = useTranslation();
    const columns = [
        { field: 'nom', flex: 1 },
    ];

    return (
        <GridPage>
            <MuiDataGrid
                title="Plantilles d'estils"
                resourceName="templateEstils"
                columns={columns}
                paginationActive
                popupEditActive
                popupEditFormContent={<TemplateEstilsForm />}
            />
        </GridPage>
    );
};

export default TemplateEstils;
