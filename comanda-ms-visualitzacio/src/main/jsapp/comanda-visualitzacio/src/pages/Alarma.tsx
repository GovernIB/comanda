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
    useResourceApiService,
} from 'reactlib';

const dataGridColumns = [{
    field: 'nom',
    flex: 1,
}];

export const EntornAppSelector : React.FC<any> = (props) => {
    const { id, onEntornAppChange, validationErrors } = props;
    const formApiRef = useFormApiRef();
    const { isReady: apiIsReady, getOne: apiGetOne } = useResourceApiService('entornApp');
    const [entornApp, setEntornApp] = React.useState<any>();
    React.useEffect(() => {
        if (apiIsReady && id != null) {
            apiGetOne(id).then(value => {
                const entornApp = { id: value.id, description: value.entornAppDescription };
                setEntornApp(entornApp)
                formApiRef.current.setFieldValue('entornApp', entornApp);
            });
        }
    }, [apiIsReady, id]);
    return (!id || entornApp != null) && <MuiFilter
        resourceName="entornApp"
        code="salut_entornApp_filter"
        commonFieldComponentProps={{ size: 'small' }}
        validationErrors={validationErrors}
        initialData={{ entornApp: entornApp }}
        springFilterBuilder={() => ''}
        onDataChange={data => onEntornAppChange(data?.entornApp)}
        formApiRef={formApiRef}>
            <FormField name="entornApp"/>
    </MuiFilter>;
}

export const AlarmaForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const formApiRef = useFormApiRef();
    const [entornAppId, setEntornAppId] = React.useState<any>();
    const [validationErrors, setValidationErrors] = React.useState<any>();
    const handleDataChange = (data: any) => {
        setEntornAppId(data?.entornAppId);
    }
    const handleValidationErrorsChange = (_id: any, validationErrors?: any[]) => {
        const entornAppValidationError = validationErrors?.find(e => e.field === 'entornAppId');
        setValidationErrors(entornAppValidationError ? [{
            field: 'entornApp',
            code: entornAppValidationError.code,
            message: entornAppValidationError.message,
        }] : null);
    }
    const handleEntornAppChange = (entornApp: any) => {
        formApiRef.current.setFieldValue('entornAppId', entornApp?.id);
    }
    return <FormPage>
        <MuiForm
            id={id}
            title={id ? t('page.alarmaConfig.update') : t('page.alarmaConfig.create')}
            resourceName="alarmaConfig"
            goBackLink="/alarma"
            createLink="form/{{id}}"
            apiRef={formApiRef}
            onDataChange={handleDataChange}
            onValidationErrorsChange={handleValidationErrorsChange}>
            <Grid container spacing={1}>
                <Grid size={12}>
                    <EntornAppSelector
                        id={entornAppId}
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