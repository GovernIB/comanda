import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    FormField,
    FormPage,
    GridPage,
    MuiDataGrid,
    MuiForm,
    MuiFormTabContent,
    MuiFormTabs,
    springFilterBuilder,
    useBaseAppContext,
    useFormContext,
    useMuiDataGridApiRef,
    useResourceApiService,
} from 'reactlib';
import LogoUpload from "../components/LogoUpload";
import BlockIcon from "@mui/icons-material/Block";
import FasesCompactacio from "../components/FasesCompactacio";
import UrlPingAdornment from '../components/UrlPingAdornment';
import {iniciaDescargaJSON} from "../util/commonsActions";
import {DataCommonAdditionalAction} from "../../lib/components/mui/datacommon/MuiDataCommon";
// TODO Debería añadirse un export de este tipo
import { FormTabsValue } from '../../lib/components/mui/form/MuiFormTabs.tsx';
import {Cancel, CheckCircle} from '@mui/icons-material';

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

    const toogleActiva = (id:any) => {
        apiAction(id, { code: "toogle_activa" })
            .then(() => {
                refresh?.();
                temporalMessageShow(null, t('page.appsEntorns.action.toolbalActiva.ok'), 'success');
            })
            .catch((error) => {
                error?.message && temporalMessageShow(null, error?.message, 'error');
            });
    }

    return {
        pingUrl,
        appExport,
        toogleActiva,
    };
};

const AppEntornForm: React.FC = () => {
    const { t } = useTranslation();
    const { data } = useFormContext();
    const { id: appId } = useParams();
    const entornFilter = springFilterBuilder.not(springFilterBuilder.exists(springFilterBuilder.eq("entornAppEntities.app.id", appId)));
    const { pingUrl } = useActions();

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="entorn" disabled={data?.id != null} readOnly={data?.id != null} filter={entornFilter}/>
            </Grid>
            <Grid size={12}>
                <FormField name="infoUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.infoUrl} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={12}>
                <FormField name="salutUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.salutUrl} onClick={pingUrl}/>}}}} />
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
                <FormField name="compactable" type="checkbox" label={t('page.apps.fields.compactable')} />
            </Grid>
            {data?.compactable === true && (
                <>
                    <Grid size={{xs:12, md:4}}>
                        <FormField name="compactacioSetmanalMesos" type="number" required={false} label={t('page.apps.fields.compactacioSetmanalMesos')} componentProps={{ title: t('page.apps.tooltips.compactacioSetmanes') }} />
                    </Grid>
                    <Grid size={{xs:12, md:4}}>
                        <FormField name="compactacioMensualMesos" type="number" required={false} label={t('page.apps.fields.compactacioMensualMesos')} componentProps={{ title: t('page.apps.tooltips.compactacioMesos') }} />
                    </Grid>
                    <Grid size={{xs:12, md:4}}>
                        <FormField name="eliminacioMesos" type="number" required={false} label={t('page.apps.fields.eliminacioMesos')} componentProps={{ title: t('page.apps.tooltips.borratMesos') }} />
                    </Grid>
                    <Grid size={12}>
                        {(() => {
                            const s = data?.compactacioSetmanalMesos == null ? null : Number(data?.compactacioSetmanalMesos);
                            const m = data?.compactacioMensualMesos == null ? null : Number(data?.compactacioMensualMesos);
                            const e = data?.eliminacioMesos == null ? null : Number(data?.eliminacioMesos);
                            return <FasesCompactacio s={s} m={m} e={e} />;
                        })()}
                    </Grid>
                </>
            )}
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
            flex: 2,
        },
        {
            field: 'versio',
            flex: 1,
        },
        {
            field: 'activa',
            flex: 0.5,
            renderCell: (params:any) => {
                if (params?.row?.activa) {
                    return <CheckCircle color="success" />
                } else {
                    return <Cancel color="error"/>
                }
            }
        },
    ];

    const apiRef = useMuiDataGridApiRef();
    const refresh = () => {
        apiRef?.current?.refresh?.()
    }
    const { toogleActiva } = useActions(refresh)

    const actions = [
        {
            label: t('page.appsEntorns.action.toolbalActiva.activar'),
            icon: "check_circle",
            showInMenu: true,
            onClick: toogleActiva,
            hidden: (row:any) => row?.activa,
        },
        {
            label: t('page.appsEntorns.action.toolbalActiva.desactivar'),
            icon: "cancel",
            showInMenu: true,
            onClick: toogleActiva,
            hidden: (row:any) => !row?.activa,
        },
    ]
    return (
        <GridPage>
            <MuiDataGrid
                apiRef={apiRef}
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
                rowAdditionalActions={actions}
            />
        </GridPage>
    );
};

export const AppForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const { setMarginsDisabled } = useBaseAppContext();
    React.useEffect(() => {
        setMarginsDisabled(true);
        return () => setMarginsDisabled(false);
    }, []);

    const formTabs: FormTabsValue[] = [
        {
            label: t('page.apps.general'),
        },
        {
            label: t('page.apps.entornApp'),
        },
    ];

    return (
        <MuiForm
            key={id} // TODO No debería ser necesario, parece un bug de la librería
            id={id}
            title={id ? t('page.apps.update') : t('page.apps.create')}
            resourceName="app"
            goBackLink="/app"
            createLink="form/{{id}}"
        >
            <MuiFormTabs tabs={formTabs} tabIndexesWithGrids={[1]}>
                <MuiFormTabContent index={0} showOnCreate>
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
                                <LogoUpload
                                    name="logo"
                                    // label="Logo"
                                />
                            </Grid>
                            <Grid size={12}>
                                <FormField name="activa" />
                            </Grid>
                        </Grid>
                    </FormPage>
                </MuiFormTabContent>
                <MuiFormTabContent
                    index={1}
                >
                    <AppsEntorns />
                </MuiFormTabContent>
            </MuiFormTabs>
        </MuiForm>
    );
};

const Apps: React.FC = () => {
    const { t } = useTranslation();
    const { appExport } = useActions();
    const appActions: DataCommonAdditionalAction[] = [
        {
            label: t('page.apps.action.export'),
            icon: 'download',
            showInMenu: true,
            onClick: appExport,
        },
    ]

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
            },
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
        <GridPage>
            <MuiDataGrid
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
