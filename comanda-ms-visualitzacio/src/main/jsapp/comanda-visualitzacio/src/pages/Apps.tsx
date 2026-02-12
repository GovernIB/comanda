import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    FormField,
    FormPage,
    FormTabsValue,
    GridPage,
    MuiActionReportButton,
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
import { FormControl, FormControlLabel, FormGroup, FormLabel, Radio, RadioGroup, Typography } from '@mui/material';
import LogoUpload from "../components/LogoUpload";
import { ReactElementWithPosition } from '../../lib/util/reactNodePosition.ts';
import { useOptionalDataGridContext } from '../../lib/components/mui/datagrid/DataGridContext';
import BlockIcon from "@mui/icons-material/Block";
import FasesCompactacio from "../components/FasesCompactacio";
import UrlPingAdornment from '../components/UrlPingAdornment';
import { useAclPermissionManager } from '../components/AclPermissionManager';
import {iniciaDescargaJSON} from "../util/commonsActions";
import {DataCommonAdditionalAction} from "../../lib/components/mui/datacommon/MuiDataCommon";
import {Cancel, CheckCircle} from '@mui/icons-material';
import useReordering from '../hooks/reordering.tsx';
import PageTitle from '../components/PageTitle.tsx';

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
    const appExport = (id:any) => report(id, 'app_export', t($ => $.page.apps.action.export), 'JSON')

    const toogleActiva = (id:any) => {
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
            <Grid size={9}>
                <FormField name="entorn" disabled={data?.id != null} readOnly={data?.id != null} filter={entornFilter}/>
            </Grid>
            <Grid size={3}>
                <FormField name="activa" />
            </Grid>
            <Grid size={12}>
                <FormField name="infoUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.infoUrl} formData={data} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={12}>
                <FormField name="salutUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.salutUrl} formData={data} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={12} sx={{ p: 1, pt: 0 }}>
                <FormControl component="fieldset">
                    <FormLabel component="legend">{t('page.apps.fields.salutAuthLegend')}</FormLabel>
                    <FormGroup aria-label="position" row>
                        <FormField name="salutAuth" type="checkbox" label={t('page.apps.fields.auth')} />
                    </FormGroup>
                </FormControl>
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaInfoUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.estadisticaInfoUrl} formData={data} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaUrl" componentProps={{slotProps: {input: {endAdornment: <UrlPingAdornment url={data?.estadisticaUrl} formData={data} onClick={pingUrl}/>}}}} />
            </Grid>
            <Grid size={6} sx={{ p: 1, pt: 0 }}>
                <FormControl component="fieldset">
                    <FormLabel component="legend">{t($ => $.page.apps.fields.estadisticaAuthLegend)}</FormLabel>
                    <FormGroup aria-label="position" row>
                        <FormField name="estadisticaAuth" type="checkbox" label={t($ => $.page.apps.fields.auth)} />
                    </FormGroup>
                </FormControl>
            </Grid>
            <Grid size={6}>
                <FormField name="estadisticaCron" />
            </Grid>
            <Grid size={12}>
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
    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclPermissionManager('ENTORN_APP');
    const actions = [
        {
            label: t($ => $.page.appsEntorns.action.toolbarActiva.permisos),
            icon: "lock",
            onClick: (id: any, row: any) => permissionShow(id, row.entorn.description)
        },
        {
            label: t($ => $.page.appsEntorns.action.toolbarActiva.activar),
            icon: "check_circle",
            showInMenu: true,
            onClick: toogleActiva,
            hidden: (row:any) => row?.activa,
        },
        {
            label: t($ => $.page.appsEntorns.action.toolbarActiva.desactivar),
            icon: "cancel",
            showInMenu: true,
            onClick: toogleActiva,
            hidden: (row:any) => !row?.activa,
        },
    ]
    return (
        <>
            <MuiDataGrid
                apiRef={apiRef}
                title={t($ => $.page.appsEntorns.title)}
                resourceName="entornApp"
                staticFilter={`app.id : ${appId}`}
                columns={columns}
                paginationActive
                popupEditActive
                popupEditFormContent={<AppEntornForm />}
                popupEditFormDialogResourceTitle={t($ => $.page.appsEntorns.resourceTitle)}
                formAdditionalData={{
                    app: { id: appId },
                }}
                rowAdditionalActions={actions}
                rowActionsColumnProps={{ flex: .3 }}
            />
            {permissionComponent}
        </>
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
            label: t($ => $.page.apps.general),
        },
        {
            label: t($ => $.page.apps.entornApp),
        },
    ];

    const formTitle = id ? t($ => $.page.apps.update) : t($ => $.page.apps.create);

    return (
        <MuiForm
            key={id} // TODO No debería ser necesario, parece un bug de la librería
            id={id}
            title={formTitle}
            resourceName="app"
            goBackLink="/app"
            createLink="form/{{id}}"
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

const parseCodesFromJson = (jsonContent: string) => {
    let parsedJson = JSON.parse(jsonContent);
    if (!Array.isArray(parsedJson)) parsedJson = [parsedJson];
    const parsedCodes = (parsedJson || [])
        .map((a: any) => a?.codi)
        .filter((c: any) => typeof c === 'string');
    return parsedCodes;
};

const existsAnyInParsedCodes = (parsedCodes: any[], existingCodes: Set<any>) => {
    return parsedCodes.some((c: any) => existingCodes.has(c));
}

const AppImportFormContent = () => {
    const { t } = useTranslation();
    const { temporalMessageShow } = useBaseAppContext();
    const { data, apiRef, fieldErrors } = useFormContext();
    const jsonContentValidationError = fieldErrors?.find((err) => err.field === 'jsonContent');
    const gridContext = useOptionalDataGridContext();
    const existingCodes = React.useMemo(() => new Set((gridContext?.rows ?? []).map((r: any) => r?.codi).filter(Boolean)), [gridContext?.rows]);
    const parsedCodes = React.useMemo(() => data?.jsonContent ? parseCodesFromJson(data?.jsonContent) : [], [data?.jsonContent]);
    const existsAny = React.useMemo(() => existsAnyInParsedCodes(parsedCodes, existingCodes), [existingCodes, parsedCodes]);

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        try {
            const text = await file.text();

            // Preselect default decision if conflicts
            // Doing existsAnyInJson before setting jsonContent ensures that the json is valid, as JSON.parse has already been called
            if (existsAnyInParsedCodes(parseCodesFromJson(text), existingCodes))
                apiRef.current?.setFieldValue('decision', 'COMBINE');

            apiRef.current?.setFieldValue('jsonContent', text);
        } catch (err: any) {
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
        {parsedCodes.length > 0 && (
            <>
                <Typography variant="body2" sx={{ mt: 2 }}>
                    {t($ => $.page.apps.import.detectedCodes)} {parsedCodes.join(', ')}
                </Typography>
                {existsAny && (
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
    const gridApiRef = useMuiDataGridApiRef();
    const { appExport } = useActions();
    const appActions: DataCommonAdditionalAction[] = [
        {
            label: t($ => $.page.apps.action.export),
            icon: 'download',
            showInMenu: true,
            onClick: appExport,
        },
    ];
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
        <GridPage>
            <PageTitle title={t($ => $.page.apps.title)} />
            <MuiDataGrid
                apiRef={gridApiRef}
                title={t($ => $.page.apps.title)}
                resourceName="app"
                columns={columns}
                toolbarType="upper"
                paginationActive
                //readOnly
                rowDetailLink="/dd"
                toolbarCreateLink="form"
                rowUpdateLink="form/{{id}}"
                rowAdditionalActions={appActions}
                toolbarElementsWithPositions={toolbarElementsWithPositions}
                {...dataGridProps}
            />
        </GridPage>
    );
};

export default Apps;
