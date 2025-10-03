import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import Card from '@mui/material/Card';
import CardHeader from '@mui/material/CardHeader';
import CardContent from '@mui/material/CardContent';
import Checkbox from '@mui/material/Checkbox';
import FormControlLabel from '@mui/material/FormControlLabel';
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

const useDataGridColumns = () => {
    const { isReady: apiIsReady, find: apiFind } = useResourceApiService('entornApp');
    const [entornApps, setEntornApps] = React.useState<any[]>();
    React.useEffect(() => {
        if (apiIsReady) {
            apiFind({ unpaged: true }).then(response => {
                setEntornApps(response.rows);
            });
        }
    }, [apiIsReady]);
    return [{
        field: 'entornAppId',
        valueFormatter: (value?: number) => {
            if (value == null) {
                return '';
            }
            const entornApp = entornApps?.find(ea => ea.id === value);
            return entornApp?.entornAppDescription ?? '';
        },
        flex: 1,
    }, {
        field: 'nom',
        flex: 3,
    }, {
        field: 'tipus',
        flex: 1,
    }];
}

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

export const AlarmaConfigForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const formApiRef = useFormApiRef();
    const [entornAppId, setEntornAppId] = React.useState<any>();
    const [validationErrors, setValidationErrors] = React.useState<any>();
    const [condicioValorDisabled, setCondicioValorDisabled] = React.useState<boolean>(true);
    const [periodeShow, setPeriodeShow] = React.useState<boolean>();
    const handleDataChange = (data: any) => {
        setEntornAppId(data?.entornAppId);
        const condicioValorDisabled = data?.tipus !== 'APP_LATENCIA';
        setCondicioValorDisabled(condicioValorDisabled);
        if (periodeShow === undefined) {
            const ps = data?.periodeValor != null || data?.periodeUnitat != null;
            console.log('>>> setPeriodeShow', ps)
            setPeriodeShow(ps);
        }
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
    const handlePeriodeShowChange = (event: any) => {
        setPeriodeShow(event.target.checked);
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
            <Grid container spacing={2}>
                <Grid size={3}>
                    <EntornAppSelector
                        id={entornAppId}
                        onEntornAppChange={handleEntornAppChange}
                        validationErrors={validationErrors} />
                </Grid>
                <Grid size={9}>
                    <FormField name="nom" />
                </Grid>
                <Grid size={12}>
                    <Card variant="outlined">
                        <CardHeader
                            title={t('page.alarmaConfig.condicio.title')}
                            subheader={t('page.alarmaConfig.condicio.subtitle')}
                            slotProps={{
                                title: { variant: 'h6' },
                                subheader: { variant: 'subtitle2', sx: { color: 'text.secondary' } }
                            }}
                            sx={{ mb: -2 }} />
                        <CardContent>
                            <Grid container spacing={1}>
                                <Grid size={3}>
                                    <FormField name="tipus" />
                                </Grid>
                                <Grid size={6}>
                                    <FormField name="condicio" disabled={condicioValorDisabled} />
                                </Grid>
                                <Grid size={3}>
                                    <FormField name="valor" disabled={condicioValorDisabled} />
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>
                <Grid size={12}>
                    <FormField name="missatge" />
                </Grid>
                <Grid size={6}>
                    <FormControlLabel
                        control={<Checkbox size="small" checked={periodeShow ?? false} onChange={handlePeriodeShowChange}/>}
                        label={t('page.alarmaConfig.periode.switch')}
                        sx={{ ml: 1 }} />
                </Grid>
                <Grid size={6}>
                    <FormField name="admin" />
                </Grid>
                {periodeShow && <Grid size={12}>
                    <Card variant="outlined">
                        <CardHeader
                            title={t('page.alarmaConfig.periode.title')}
                            subheader={t('page.alarmaConfig.periode.subtitle')}
                            slotProps={{
                                title: { variant: 'h6' },
                                subheader: { variant: 'subtitle2', sx: { color: 'text.secondary' } }
                            }}
                            sx={{ mb: -2 }} />
                        <CardContent>
                            <Grid container spacing={1}>
                                <Grid size={3}>
                                    <FormField name="periodeValor" />
                                </Grid>
                                <Grid size={9}>
                                    <FormField name="periodeUnitat" />
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Grid>}
            </Grid>
        </MuiForm>
    </FormPage>;
}

const AlarmaConfig = () => {
    const { t } = useTranslation();
    const dataGridColumns = useDataGridColumns();
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

export default AlarmaConfig;