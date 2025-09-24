import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    FormPage,
    GridPage,
    MuiDataGrid,
    MuiForm,
    MuiFilter,
    FormField,
    useFormApiRef,
} from 'reactlib';

const dataGridColumns = [{
    field: 'nom',
    flex: 1,
}];

export const EntornAppSelector : React.FC<any> = (props) => {
    const { onEntornAppChange, validationErrors } = props;
    return <MuiFilter
        resourceName="entornApp"
        code="salut_entornApp_filter"
        commonFieldComponentProps={{ size: 'small' }}
        validationErrors={validationErrors}
        springFilterBuilder={data => {
            onEntornAppChange(data?.entornApp)
            return '';
        }}>
            <FormField name={'entornApp'} />
    </MuiFilter>;
}

export const AlarmaForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const formApiRef = useFormApiRef();
    const [validationErrors, setValidationErrors] = React.useState<any>();
    const handleEntornAppChange = (entornApp: any) => {
        formApiRef.current.setFieldValue('entornAppId', entornApp?.id);
    }
    const handleValidationErrorsChange = (_id: any, validationErrors?: any[]) => {
        const entornAppValidationError = validationErrors?.find(e => e.field === 'entornAppId');
        setValidationErrors(entornAppValidationError ? [{
            field: 'entornApp',
            code: entornAppValidationError.code,
            message: entornAppValidationError.message,
        }] : null);
    }
    return <FormPage>
        <MuiForm
            id={id}
            title={id ? t('page.alarmaConfig.update') : t('page.alarmaConfig.create')}
            resourceName="alarmaConfig"
            goBackLink="/alarma"
            createLink="form/{{id}}"
            apiRef={formApiRef}
            onValidationErrorsChange={handleValidationErrorsChange}>
            <Grid container spacing={1}>
                <Grid size={12}>
                    <EntornAppSelector
                        onEntornAppChange={handleEntornAppChange}
                        validationErrors={validationErrors} />
                </Grid>
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