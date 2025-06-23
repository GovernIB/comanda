import * as React from 'react';
import {useTranslation} from 'react-i18next';
import {useParams} from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {FormField, FormPage, GridPage, MuiForm, MuiFormTabContent, MuiGrid, springFilterBuilder, useFormContext} from 'reactlib';
import {Box, Tab, Tabs} from '@mui/material';
import LogoUpload from "../components/LogoUpload";

const AppEntornForm: React.FC = () => {
    const { data } = useFormContext();
    const { id: appId } = useParams();
    const entornFilter = springFilterBuilder.not(springFilterBuilder.exists(springFilterBuilder.eq("entornAppEntities.app.id", appId)));

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="entorn" disabled={data?.id != null} filter={entornFilter}/>
            </Grid>
            <Grid size={12}>
                <FormField name="infoUrl" />
            </Grid>
            <Grid size={4}>
                <FormField name="infoInterval" />
            </Grid>


            <Grid size={12}>
                <FormField name="salutUrl" />
            </Grid>
            <Grid size={4}>
                <FormField name="salutInterval" />
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaInfoUrl" />
            </Grid>


            <Grid size={12}>
                <FormField name="estadisticaUrl" />
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaCron" />
            </Grid>
            <Grid size={12}>
                <FormField name="activa" />
            </Grid>
        </Grid>
    );
};

const AppsEntorns: React.FC = () => {
    const { t } = useTranslation();
    const { id: appId } = useParams();
    const columns = [
        {
            field: 'entorn',
            flex: 1,
        },
        {
            field: 'versio',
            flex: 1,
        },
        {
            field: 'activa',
            flex: 0.5,
        },
    ];
    return (
        <GridPage disableMargins>
            <MuiGrid
                title={t('page.appsEntorns.title')}
                resourceName="entornApp"
                staticFilter={`app.id : ${appId}`}
                columns={columns}
                toolbarType="upper"
                paginationActive
                popupEditActive
                popupEditFormContent={<AppEntornForm />}
                popupEditFormDialogResourceTitle={t('page.appsEntorns.resourceTitle')}
                formAdditionalData={{
                    app: { id: appId },
                }}
            />
        </GridPage>
    );
};

export const AppForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const [tabValue, setTabValue] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTabValue(newValue);
    };

    function a11yProps(index: number) {
        return {
            id: `tab-${index}`,
            'aria-controls': `tabpanel-${index}`,
        };
    }

    const isCreation = id == null;

    return (
        <MuiForm
            id={id}
            title={id ? t('page.apps.update') : t('page.apps.create')}
            resourceName="app"
            goBackLink="/app"
            createLink="form/{{id}}"
        >
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs
                    value={tabValue}
                    onChange={handleChange}
                    aria-label="Tabs formulario de aplicacion"
                >
                    <Tab label={t('page.apps.general')} {...a11yProps(0)} />
                    <Tab label={t('page.apps.entornApp')} disabled={isCreation} {...a11yProps(1)} />
                </Tabs>
            </Box>
            <MuiFormTabContent currentIndex={tabValue} index={0} showOnCreate>
                <FormPage>
                    <Grid container spacing={2}>
                        <Grid size={4}>
                            <FormField name="codi" />
                        </Grid>
                        <Grid size={8}></Grid>
                        <Grid size={12}>
                            <FormField name="nom" />
                        </Grid>
                        <Grid size={12}>
                            <FormField name="descripcio" type="textarea" />
                        </Grid>
                        <Grid size={12}>
                            <LogoUpload />
                        </Grid>
                        <Grid size={12}>
                            <FormField name="activa" />
                        </Grid>
                    </Grid>
                </FormPage>
            </MuiFormTabContent>
            <MuiFormTabContent
                currentIndex={tabValue}
                index={1}
            >
                <AppsEntorns />
            </MuiFormTabContent>
        </MuiForm>
    );
};

const Apps: React.FC = () => {
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
        {
            field: 'activa',
            flex: 0.5,
        },
    ];
    return (
        <GridPage disableMargins>
            <MuiGrid
                title={t('page.apps.title')}
                resourceName="app"
                columns={columns}
                toolbarType="upper"
                paginationActive
                //readOnly
                rowDetailLink="/dd"
                toolbarCreateLink="form"
                rowUpdateLink="form/{{id}}"
            />
        </GridPage>
    );
};

export default Apps;
