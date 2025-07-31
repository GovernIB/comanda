import * as React from 'react';
import {useTranslation} from 'react-i18next';
import {useParams} from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {FormField, FormPage, GridPage, MuiForm, MuiFormTabContent, MuiGrid, springFilterBuilder, useBaseAppContext, useFormContext, useResourceApiService} from 'reactlib';
import {Box, Tab, Tabs} from '@mui/material';
import LogoUpload from "../components/LogoUpload";
import BlockIcon from "@mui/icons-material/Block";
import UrlPingAdornment from '../components/UrlPingAdornment';
import {iniciaDescargaJSON} from "../util/commonsActions";
import {DataCommonAdditionalAction} from "../../lib/components/mui/datacommon/MuiDataCommon";

const useActions = (refresh?: () => void) => {
    const { artifactAction: apiAction } = useResourceApiService('entornApp');
    const { artifactReport: apiReport } = useResourceApiService('app');
    const { temporalMessageShow } = useBaseAppContext();
    const { t } = useTranslation();

    const pingUrl = React.useCallback(async (additionalData: any): Promise<boolean> => {
        try {
            const data = await apiAction(null, { code: 'pingUrl', data: additionalData });
            refresh?.();
            temporalMessageShow(null, data.message, data.success ? 'success' : 'error');
            return data.success;
        } catch (error: any) {
            temporalMessageShow(null, error.message, 'error');
            return false;
        }
    }, [apiAction, refresh, temporalMessageShow]);

    const report = (id:any, code:any, mssg:any, fileType:any) => {
        apiReport(id, {code, fileType})
            .then((result) => {
                iniciaDescargaJSON(result);
                temporalMessageShow(null, mssg, 'success');
            })
            .catch((error) => {
                temporalMessageShow(null, error.message, 'error');
            });
    }
    const appExport = (id:any) => report(id, 'app_export', t('page.apps.action.export'), 'JSON')
    const appActions: DataCommonAdditionalAction[] = [
        {
            title: t('page.apps.action.export'),
            icon: 'download',
            showInMenu: true,
            onClick: appExport,
        },
    ]

    return {
        pingUrl,
        appActions,
    };
};

const AppEntornForm: React.FC = () => {
    const { data } = useFormContext();
    const { id: appId } = useParams();
    const entornFilter = springFilterBuilder.not(springFilterBuilder.exists(springFilterBuilder.eq("entornAppEntities.app.id", appId)));
    const { pingUrl } = useActions();

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="entorn" disabled={data?.id != null} filter={entornFilter}/>
            </Grid>
            <Grid size={12}>
                <FormField name="infoUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.infoUrl} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={{xs:12, md:4}}>
                <FormField name="infoInterval" />
            </Grid>

            <Grid size={12}>
                <FormField name="salutUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.salutUrl} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={{xs:12, md:4}}>
                <FormField name="salutInterval" />
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaInfoUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.estadisticaInfoUrl} onClick={pingUrl}/>}}}} />
            </Grid>

            <Grid size={12}>
                <FormField name="estadisticaUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.estadisticaUrl} onClick={pingUrl}/>}}}} />
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
    const { appActions } = useActions();
    
    const columns = [
        {
            field: 'logo',
            flex: 1,
            renderCell: (params: any) => {
                const value = params.value; // Obtenir el valor de la cel·la
                return value ? (
                    <img
                        src={`data:image/png;base64,${value}`}
                        alt="logo"
                        style={{ maxHeight: '32px' }}
                    />
                ) : (
                    <span role="img" aria-label="block" style={{ fontSize: '24px' }}>
                    <BlockIcon style={{ fontSize: '20px', color: 'gray' }} />
                </span>
                );
            }
            ,
        },
        {
            field: 'codi',
            flex: 2,
        },
        {
            field: 'nom',
            flex: 7,
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
                rowAdditionalActions={appActions}
            />
        </GridPage>
    );
};

export default Apps;
