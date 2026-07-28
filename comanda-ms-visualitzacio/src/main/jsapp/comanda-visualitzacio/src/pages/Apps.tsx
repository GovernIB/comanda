import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    FormField,
    FormPage,
    FormTabsValue,
    MuiActionReportButton,
    MuiDataGrid,
    MuiDataGridColDef,
    MuiForm,
    MuiFormTabContent,
    MuiFormTabs,
    springFilterBuilder,
    useBaseAppContext,
    useCloseDialogButtons,
    useFormContext,
    useMuiContentDialog,
    useMuiDataGridApiRef,
    useResourceApiService,
} from 'reactlib';
import {
    FormControl,
    FormControlLabel,
    FormGroup,
    FormLabel,
    IconButton,
    Radio,
    RadioGroup,
    Typography,
} from '@mui/material';
import LogoUpload from "../components/LogoUpload";
import { ReactElementWithPosition } from '../../lib/util/reactNodePosition.ts';
import BlockIcon from "@mui/icons-material/Block";
import FasesCompactacio from "../components/FasesCompactacio";
import UrlPingAdornment from '../components/UrlPingAdornment';
import { useAclPermissionManager } from '../components/AclPermissionManager';
import {iniciaDescargaJSON} from "../util/commonsActions";
import {DataCommonAdditionalAction} from "../../lib/components/mui/datacommon/MuiDataCommon";
import Cancel from '@mui/icons-material/Cancel';
import CheckCircle from '@mui/icons-material/CheckCircle';
import useReordering from '../hooks/reordering.tsx';
import PageTitle from '../components/PageTitle.tsx';
import { StacktraceBlock } from '../components/RickTextDetail.tsx';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';
import notNull from '../util/arrayUtils.ts';
import { useIsUserAdmin } from '../components/UserContext.ts';
import { getErrorMessage } from '../util/exceptionUtils.ts';
import * as z from 'zod';
import { AppType } from '../models/app.schema';
import ParameterExistsAdornment from '../components/ParameterExistsAdornment.tsx';
import Divider from '@mui/material/Divider';
import InputAdornment from '@mui/material/InputAdornment';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';

const PingUrlActionResponse = z.object({
    success: z.boolean(),
    validationError: z.boolean().optional(),
    message: z.string(),
});

export type PingUrlResult = { status: string; elements?: { contentValue: React.JSX.Element }[] } | false;
export type ExistsParameterResult = { exists: boolean };

const useActions = (refresh?: () => void) => {
    const { artifactAction: apiAction } = useResourceApiService('entornApp');
    const { artifactReport: apiReport } = useResourceApiService('app');
    const { temporalMessageShow } = useBaseAppContext();
    const { t } = useTranslation();

    const pingUrl = React.useCallback(async (
        additionalData: Record<string, unknown>,
        expectedResponseTypeEnum: string,
    ): Promise<PingUrlResult> => {
        try {
            const actionResponse = await apiAction(null, { code: 'pingUrl', data: {...additionalData, expectedResponseTypeEnum} });
            const data = PingUrlActionResponse.parse(actionResponse);
            refresh?.();
            if (data?.validationError) {
                const elementsDetail = [
                    {
                        contentValue: (
                            <StacktraceBlock
                                title={t($ => $.page.apps.ping.validationTrace)}
                                value={data?.message}
                            />
                        ),
                    },
                ];
                return {
                    status: 'warning',
                    elements: elementsDetail,
                };
            } else {
                temporalMessageShow(null, data.message, data.success ? 'success' : 'error');
            }
            return {status :data.success ? 'success' : 'error'};
        } catch (error) {
            temporalMessageShow(null, getErrorMessage(error), 'error');
            return false;
        }
    }, [apiAction, refresh, t, temporalMessageShow]);

    const existsParameter = React.useCallback(async (
        parameterValue: string,
    ): Promise<ExistsParameterResult> => {
        try {
            const actionResponse = await apiAction(null, {
                code: 'existsParameter',
                data: { parameterValue }
            });
            return actionResponse?.exists;
        } catch (error) {
            temporalMessageShow(null, getErrorMessage(error), 'error');
            return {"exists" : false};
        }
    }, [apiAction, temporalMessageShow]);

    const report = (id: string | number, code: string, mssg: string, fileType: string) => {
        apiReport(id, {code, fileType: fileType as any})
            .then((result) => {
                iniciaDescargaJSON(result);
                temporalMessageShow(null, mssg, 'success');
            })
            .catch((error) => {
                temporalMessageShow(null, error.message, 'error');
            });
    }
    const appExport = (id: string | number) => report(id, 'app_export', t($ => $.page.apps.action.export), 'JSON')

    const toogleActiva = (id: string | number) => {
        apiAction(id, { code: "toogle_activa" })
            .then(() => {
                refresh?.();
                temporalMessageShow(null, t($ => $.page.appsEntorns.action.toolbarActiva.ok), 'success');
            })
            .catch((error) => {
                if (error?.message) {
                    temporalMessageShow(null, error?.message, 'error');
                }
            });
    }

    return {
        pingUrl,
        appExport,
        toogleActiva,
        existsParameter,
    };
};

const CustomPasswordFormField: React.FC<{
    name: string;
    additionalEndAdornment?: React.ReactNode;
    disablePasswordMode?: boolean;
}> = ({ name, additionalEndAdornment, disablePasswordMode }) => {
    const { t } = useTranslation();
    const [showPassword, setShowPassword] = React.useState(false);

    const handleClickShowPassword = () => setShowPassword(show => !show);
    return (
        <FormField
            name={name}
            componentProps={{
                type: disablePasswordMode || showPassword ? 'text' : 'password',
                placeholder: t($ => $.page.apps.fields.contrasenyaAuthPlaceholder),
                slotProps: {
                    inputLabel: { shrink: true },
                    input: {
                        endAdornment: (
                            <InputAdornment position="end">
                                {additionalEndAdornment}
                                {!disablePasswordMode && (
                                    <IconButton
                                        aria-label={
                                            showPassword
                                                ? t($ => $.page.apps.fields.hidePassword)
                                                : t($ => $.page.apps.fields.showPassword)
                                        }
                                        onClick={handleClickShowPassword}
                                        edge="end"
                                    >
                                        {showPassword ? <VisibilityOff /> : <Visibility />}
                                    </IconButton>
                                )}
                            </InputAdornment>
                        ),
                    },
                },
            }}
        />
    );
};

const AppEntornForm: React.FC = () => {
    const { t } = useTranslation();
    const isCurrentUserAdmin = useIsUserAdmin();
    const { data } = useFormContext();
    const { id: appId } = useParams();
    const entornFilter = springFilterBuilder.not(springFilterBuilder.exists(springFilterBuilder.eq("entornAppEntities.app.id", appId)));
    const { pingUrl, existsParameter } = useActions();
    const closeDialogButton = useCloseDialogButtons();
    const [detailDialogShow, detailDialogComponent] = useMuiContentDialog(closeDialogButton);

    return (
        <><Grid container spacing={2}>
            <Grid size={9}>
                <FormField name="entorn" disabled={data?.id != null} readOnly={data?.id != null} filter={entornFilter}/>
            </Grid>
            <Grid size={3}>
                <FormField name="activa" />
            </Grid>
            <Grid size={12}>
                <Divider sx={{ my: 1 }}>{t($ => $.page.apps.fields.salutDivider)}</Divider>
            </Grid>
            <Grid size={12}>
                <FormField name="infoUrl" componentProps={isCurrentUserAdmin && {slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.infoUrl} formData={data} onClick={(formData) => pingUrl(formData, 'INFO')} dialogShow={detailDialogShow} />}}}} />
            </Grid>
            <Grid size={12}>
                <FormField name="salutUrl" componentProps={isCurrentUserAdmin && {slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.salutUrl} formData={data} onClick={(formData) => pingUrl(formData, 'SALUT')} dialogShow={detailDialogShow} />}}}} />
            </Grid>
            <Grid size={12}>
                <FormField name="logsUrl" componentProps={isCurrentUserAdmin && {slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.logsUrl} formData={data} onClick={(formData) => pingUrl(formData, 'LOGS')} dialogShow={detailDialogShow} />}}}} />
            </Grid>
            <Grid size={12} sx={{ p: 1, pt: 0 }}>
                <FormControl component="fieldset">
                    <FormLabel component="legend">{t($ => $.page.apps.fields.salutAuthLegend)}</FormLabel>
                    <FormGroup aria-label="position" row>
                        <FormField name="salutAuth" type="checkbox" label={t($ => $.page.apps.fields.auth)} />
                    </FormGroup>
                </FormControl>
            </Grid>
            <Grid size={12}>
                <Divider sx={{ my: 1 }}>{t($ => $.page.apps.fields.estadistiquesDivider)}</Divider>
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaInfoUrl" componentProps={isCurrentUserAdmin && {slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.estadisticaInfoUrl} formData={data} onClick={(formData) => pingUrl(formData, 'ESTADISTICA_INFO')} dialogShow={detailDialogShow} />}}}} />
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaUrl" componentProps={isCurrentUserAdmin && {slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.estadisticaUrl} formData={data} onClick={(formData) => pingUrl(formData, 'ESTADISTICA')} dialogShow={detailDialogShow} />}}}} />
            </Grid>
            <Grid size={5} sx={{ p: 1, pt: 0 }}>
                <FormControl component="fieldset">
                    <FormLabel component="legend">{t($ => $.page.apps.fields.estadisticaAuthLegend)}</FormLabel>
                    <FormGroup aria-label="position" row>
                        <FormField name="estadisticaAuth" type="checkbox" label={t($ => $.page.apps.fields.auth)} />
                    </FormGroup>
                </FormControl>
            </Grid>
            <Grid size={4}>
                <FormField name="estadisticaCron" />
            </Grid>
            <Grid size={3}>
                <FormField name="compactable" type="checkbox" label={t($ => $.page.apps.fields.compactable)} />
            </Grid>
            {data?.compactable === true && (
                <>
                    {/*<Grid size={{xs:12, md:4}}>*/}
                    {/*    <FormField name="compactacioSetmanalMesos" type="number" required={false} label={t('page.apps.fields.compactacioSetmanalMesos')} componentProps={{ title: t('page.apps.tooltips.compactacioSetmanes') }} />*/}
                    {/*</Grid>*/}
                    <Grid size={{xs:12, md:6}}>
                        <FormField name="compactacioMensualMesos" type="number" required={false} label={t($ => $.page.apps.fields.compactacioMensualMesos)} componentProps={{ title: t($ => $.page.apps.tooltips.compactacioMesos) }} />
                    </Grid>
                    <Grid size={{xs:12, md:6}}>
                        <FormField name="eliminacioMesos" type="number" required={false} label={t($ => $.page.apps.fields.eliminacioMesos)} componentProps={{ title: t($ => $.page.apps.tooltips.borratMesos) }} />
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
                <Divider sx={{ my: 1 }}>{t($ => $.page.apps.fields.authDivider)}</Divider>
            </Grid>
            <Grid size={9}>
                <FormField
                    name="nomUsuariAuth"
                    componentProps={{
                        placeholder: t($ => $.page.apps.fields.nomUsuariAuthPlaceholder),
                        slotProps: {
                            inputLabel: { shrink: true },
                            ...(isCurrentUserAdmin && {
                                input: {
                                    endAdornment: (
                                        <ParameterExistsAdornment
                                            value={data?.nomUsuariAuth}
                                            onClick={existsParameter}
                                            disabled={!data?.parametreAuth}
                                        />
                                    ),
                                },
                            }),
                        },
                    }}
                />
            </Grid>
            <Grid size={3}>
                <FormField name="parametreAuth" />
            </Grid>
            <Grid size={12}>
                <CustomPasswordFormField
                    name="contrasenyaAuth"
                    disablePasswordMode={data?.parametreAuth}
                    additionalEndAdornment={
                        isCurrentUserAdmin ? (
                            <ParameterExistsAdornment
                                value={data?.contrasenyaAuth}
                                onClick={existsParameter}
                                disabled={!data?.parametreAuth}
                            />
                        ) : null
                    }
                />
            </Grid>
            <Grid size={12}>
                <Divider sx={{ my: 1 }}>{t($ => $.page.apps.fields.altresDivider)}</Divider>
            </Grid>
            <Grid size={12}>
                <FormField name="alarmesEmail" />
            </Grid>
        </Grid>
        {detailDialogComponent}</>
    );
};

const entornAppColumns: MuiDataGridColDef[] = [
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
        renderCell: (params) => {
            if (params?.row?.activa) {
                return <CheckCircle color="success" />
            } else {
                return <Cancel color="error"/>
            }
        }
    },
];

const AppsEntorns: React.FC<{ appNom?: string }> = ({ appNom }) => {
    const { t } = useTranslation();
    const { id: appId } = useParams();

    const apiRef = useMuiDataGridApiRef();
    const refresh = () => {
        apiRef?.current?.refresh?.()
    }
    const { toogleActiva } = useActions(refresh)
    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclPermissionManager('ENTORN_APP');
    const gestorReadOnly = useReadOnlyGestor();
    const actions = [
        {
            label: t($ => $.page.appsEntorns.action.toolbarActiva.permisos),
            icon: "lock",
            onClick: (id: string | number, row: { entorn?: { description?: string } }) => permissionShow(id, row?.entorn?.description ?? '')
        },
        {
            label: t($ => $.page.appsEntorns.action.toolbarActiva.activar),
            icon: "check_circle",
            showInMenu: true,
            onClick: toogleActiva,
            hidden: (row: any): boolean => Boolean(row?.activa || gestorReadOnly),
        },
        gestorReadOnly ? {
            label: t($ => $.components.details),
            icon: 'info',
            clickShowUpdateDialog: true,
        } : null,
        {
            label: t($ => $.page.appsEntorns.action.toolbarActiva.desactivar),
            icon: "cancel",
            showInMenu: true,
            onClick: toogleActiva,
            hidden: (row: any): boolean => Boolean(!row?.activa || gestorReadOnly),
        },
    ].filter(notNull);
    return (
        <>
            <MuiDataGrid
                apiRef={apiRef}
                title={t($ => $.page.appsEntorns.title)}
                resourceName="entornApp"
                fixedFilter={`app.id : ${appId}`}
                columns={entornAppColumns}
                paginationActive
                popupEditActive
                popupEditFormContent={<AppEntornForm />}
                popupEditFormDialogResourceTitle={t($ => $.page.appsEntorns.resourceTitle)+` (${appNom})`}
                formAdditionalData={{
                    app: { id: appId },
                }}
                rowAdditionalActions={actions}
                rowActionsColumnProps={{ flex: .3 }}
                rowHideUpdateButton={gestorReadOnly}
                rowHideDeleteButton={gestorReadOnly}
            />
            {permissionComponent}
        </>
    );
};

export const AppForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const [appNom, setAppNom] = React.useState<string>()
    const gestorReadOnly = useReadOnlyGestor();
    const formTabs: FormTabsValue[] = [
        {
            label: t($ => $.page.apps.general),
        },
        {
            label: t($ => $.page.apps.entornApp),
        },
    ];

    const formTitle = id ? (t($ => $.page.apps.update)+` (${appNom})`) : t($ => $.page.apps.create);

    return (
        <MuiForm
            key={id} // TODO No debería ser necesario, parece un bug de la librería
            id={id}
            title={formTitle}
            resourceName="app"
            goBackLink="/app"
            createLink="form/{{id}}"
            onDataChange={(data) => setAppNom(data?.nom)}
        >
            <PageTitle title={formTitle} />
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
                                    editable={!gestorReadOnly}
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
                    <AppsEntorns appNom={appNom} />
                </MuiFormTabContent>
            </MuiFormTabs>
        </MuiForm>
    );
};

const appColumns: MuiDataGridColDef[] = [
    {
        field: 'logo',
        flex: 1,
        renderCell: (params) => {
            const value = params.value;
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

const AppImportFormContent = () => {
    const { t } = useTranslation();
    const { temporalMessageShow } = useBaseAppContext();
    const { data, apiRef, fieldErrors } = useFormContext();
    const jsonContentValidationError = fieldErrors?.find((err) => err.field === 'jsonContent');

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        try {
            const text = await file.text();
            apiRef.current?.setFieldValue('jsonContent', text);
        } catch {
            temporalMessageShow("", t($ => $.page.apps.import.parseError), 'error');
        }
    };

    React.useEffect(() => {
        if (jsonContentValidationError?.code === 'NotNull')
            temporalMessageShow(
                null,
                t($ => $.page.apps.import.noFile),
                'error'
            );
        else if (jsonContentValidationError?.message)
            temporalMessageShow(
                null,
                jsonContentValidationError.message,
                'error'
            );
    }, [jsonContentValidationError]);

    return <>
        <input type="file" accept="application/json" onChange={handleFileChange} />
        {data?.importedAppCodes?.length && (
            <>
                <Typography variant="body2" sx={{ mt: 2 }}>
                    {t($ => $.page.apps.import.detectedCodes)} {data.importedAppCodes.join(', ')}
                </Typography>
                {data?.importedAppExists && (
                    <FormControl sx={{ mt: 2 }}>
                        <Typography variant="body2" sx={{ mb: 1 }}>
                            {t($ => $.page.apps.import.conflict)}
                        </Typography>
                        <RadioGroup
                            value={data?.decision || ''}
                            onChange={(e) =>{
                                apiRef.current?.setFieldValue('decision', e.target.value);
                            }}
                        >
                            <FormControlLabel value="OVERWRITE" control={<Radio />} label={t($ => $.page.apps.import.overwrite)} />
                            <FormControlLabel value="COMBINE" control={<Radio />} label={t($ => $.page.apps.import.combine)} />
                            <FormControlLabel value="SKIP" control={<Radio />} label={t($ => $.page.apps.import.skip)} />
                        </RadioGroup>
                    </FormControl>
                )}
            </>
        )}</>
}

const Apps: React.FC = () => {
    const { t } = useTranslation();
    const { temporalMessageShow } = useBaseAppContext();
    const navigate = useNavigate();
    const gridApiRef = useMuiDataGridApiRef();
    const { appExport } = useActions();
    const {
        show: appPermissionShow,
        component: appPermissionComponent
    } = useAclPermissionManager('APP');
    const gestorReadOnly = useReadOnlyGestor();
    const appActions: DataCommonAdditionalAction[] = [
        {
            label: t($ => $.components.permisos.title),
            icon: 'lock',
            onClick: (id: AppType['id'], row: AppType) => appPermissionShow(id, row.nom),
        },
        gestorReadOnly ? {
            label: t($ => $.components.details),
            icon: 'info',
            onClick: (id: AppType['id']) => navigate(`form/${id}`),
        } : null,
        {
            label: t($ => $.page.apps.action.export),
            icon: 'download',
            showInMenu: true,
            onClick: appExport,
        },
    ].filter(notNull);
    const { dataGridProps, loadingElement } = useReordering("app");
    const toolbarElementsWithPositions: ReactElementWithPosition[] = [
        {
            position: 1,
            element: loadingElement,
        },
        {
            position: 2,
            element: (
                <MuiActionReportButton
                    action="app_import"
                    resourceName="app"
                    icon={"upload"}
                    title={t($ => $.page.apps.action.import)}
                    formDialogContent={<AppImportFormContent />}
                    onSuccess={() => {
                        temporalMessageShow(null, t($ => $.page.apps.import.success), 'success');
                        gridApiRef?.current?.refresh?.();
                    }}
                />
            ),
        },
    ];
    return (
        <>
            <PageTitle title={t($ => $.page.apps.title)} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={t($ => $.page.apps.title)}
                resourceName="app"
                columns={appColumns}
                toolbarType="upper"
                paginationActive
                //readOnly
                toolbarCreateLink="form"
                rowUpdateLink="form/{{id}}"
                rowAdditionalActions={appActions}
                toolbarElementsWithPositions={toolbarElementsWithPositions}
                rowHideUpdateButton={gestorReadOnly}
                rowHideDeleteButton={gestorReadOnly}
                {...(!gestorReadOnly ? dataGridProps : {})}
            />
            {appPermissionComponent}
        </>
    );
};

export default Apps;
