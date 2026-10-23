import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Backdrop from '@mui/material/Backdrop';
import CircularProgress from '@mui/material/CircularProgress';
import ImportConflictsForm from '../components/estadistiques/ImportConflictsForm.tsx';
import {
    MuiDataGrid,
    FormField,
    useFormContext,
    springFilterBuilder,
    useResourceApiService,
    useBaseAppContext,
    MuiFormDialogApi,
    useMuiDataGridApiRef,
    DialogButton,
} from 'reactlib';
import { FormFieldDataActionType } from '../../lib/components/form/FormContext.tsx';
import {iniciaDescargaJSON} from "../util/commonsActions.ts";
import FormActionDialog from '../components/FormActionDialog.tsx';
import { findOptions } from '../util/requestUtils.ts';
import PageTitle from '../components/PageTitle.tsx';
import IconButton from "@mui/material/IconButton";
import Icon from "@mui/material/Icon";
import {useAclCustomPermissionManager} from "../components/AclPermissionManager.tsx";
import {useMemo} from "react";
import {useIsUserAdmin, useIsUserUsuari} from "../components/UserContext.ts";
import Badge from "@mui/material/Badge";
import { ScreenFrame } from '../lot2';
import useScreenGridProps from '../hooks/useScreenGridProps.tsx';
import { screenIcons } from '../util/screenIcons.ts';

const EstadisticaDashboardForm: React.FC = () => {
    const { data } = useFormContext();
    const { isReady: appIsReady, find: appFind } = useResourceApiService("app");
    const { isReady: entornIsReady, find: entornFind } = useResourceApiService("entorn");
    const { isReady: plantillaIsReady, find: plantillaFind } = useResourceApiService("plantilla");
    const filterAplicacio = springFilterBuilder.and(
        springFilterBuilder.eq('activa', true),
        springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id))));
    const filterEntorn = springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', data?.aplicacio?.id)));

    if (!appIsReady || !entornIsReady || !plantillaIsReady)
        return;
    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="titol" />
            </Grid>
            <Grid size={12}>
                <FormField name="descripcio" />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="aplicacio"
                    optionsRequest={(quickFilter: string) => findOptions(appFind, 'nom', quickFilter, filterAplicacio)}
                />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="entorn"
                    optionsRequest={(quickFilter: string) => findOptions(entornFind, 'nom', quickFilter, filterEntorn)}
                />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="plantilla"
                    optionsRequest={(quickFilter: string) => findOptions(plantillaFind, 'nom', quickFilter, '')}
                />
            </Grid>
        </Grid>
    );
};

const useCloneDashboardAction = (refresh?: () => void) => {
    const { t } = useTranslation();
    const apiRef = React.useRef<MuiFormDialogApi>(null);
    const {temporalMessageShow} = useBaseAppContext();
    const handleShow = (id:any, row:any) :void => {
        apiRef.current?.show?.(id, row)
    }
    const onSuccess = () :void => {
        refresh?.();
        temporalMessageShow(null, t($ => $.page.dashboards.cloneDashboard.success), 'success');
    }
    const formulario =
        <FormActionDialog
            resourceName={"dashboard"}
            action={"clone_dashboard"}
            apiRef={apiRef}
            title={t($ => $.page.dashboards.cloneDashboard.title)}
            onSuccess={onSuccess}
            initialOnChange={false}
        >
            <EstadisticaDashboardForm/>
        </FormActionDialog>;
    return {
        handleShow,
        content: formulario
    }
}

const DashboardImportFormContent: React.FC = () => {
    const [isAnalyzing, setIsAnalyzing] = React.useState(false);
    const { apiRef, data, fields, dataDispatchAction } = useFormContext();

    const handleFileChange = (fileValue: any) => {
        if (!fileValue) {
            setIsAnalyzing(false);
            apiRef.current?.setFieldValue('conflicts', undefined);
        } else {
            setIsAnalyzing(true);
            apiRef.current?.setFieldValue('conflicts', undefined);
        }
    };

    const handleReanalyze = React.useCallback(() => {
        if (!data?.file) return;
        setIsAnalyzing(true);
        const fileField = fields?.find((f: any) => f.name === 'file');
        dataDispatchAction?.({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: { fieldName: 'file', field: fileField, value: data.file },
        });
    }, [data?.file, fields, dataDispatchAction]);

    React.useEffect(() => {
        if (data?.conflicts !== undefined) {
            setIsAnalyzing(false);
        }
    }, [data]);

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="file" type={"file"} onChange={handleFileChange} />
            </Grid>
            <Grid size={12}>
                <ImportConflictsForm isAnalyzing={isAnalyzing} onReanalyze={handleReanalyze} />
            </Grid>
        </Grid>
    );
};

const useImportDashboardAction = (refresh?: () => void) => {
    const { t } = useTranslation();
    const apiRef = React.useRef<MuiFormDialogApi>(null);
    const { temporalMessageShow } = useBaseAppContext();
    const handleShow = (): void => {
        apiRef.current?.show?.(undefined);
    };
    const onSuccess = (): void => {
        refresh?.();
        temporalMessageShow(null, t($ => $.page.dashboards.action.import.success), 'success');
    };

    const importLoading = (
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2, my: 4 }}>
            <CircularProgress size={48} />
            <Typography variant="h6" color="text.primary">
                {t($ => $.page.dashboards.action.import.importing)}
            </Typography>
        </Box>
    );

    const importDialogButtons: DialogButton[] = React.useMemo(
        () => [
            {
                value: false,
                text: t($ => $.common.cancel),
                componentProps: { variant: 'outlined' },
            },
            {
                value: true,
                text: t($ => $.page.dashboards.action.import.button),
                icon: 'file_upload',
                componentProps: { variant: 'contained' },
            },
        ],
        [t]
    );

    const formulario = (
        <FormActionDialog
            resourceName={"dashboard"}
            action={"dashboard_import"}
            apiRef={apiRef}
            title={t($ => $.page.dashboards.action.import.title)}
            onSuccess={onSuccess}
            initialOnChange={false}
            formDialogLoading={importLoading}
            formDialogButtons={importDialogButtons}
        >
            <DashboardImportFormContent />
        </FormActionDialog>
    );

    return {
        handleShow,
        content: formulario,
    };
};

const useActions = () => {
    const { artifactReport: apiReport } = useResourceApiService('dashboard');
    const { temporalMessageShow } = useBaseAppContext();
    const { t } = useTranslation();
    const [exporting, setExporting] = React.useState(false);

    const report = (id:any, code:any, mssg:any, fileType:any) => {
        setExporting(true);
        apiReport(id, {code, fileType})
            .then((result) => {
                iniciaDescargaJSON(result);
                temporalMessageShow(null, mssg, 'success');
            })
            .catch((error) => {
                temporalMessageShow(null, error.message, 'error');
            })
            .finally(() => {
                setExporting(false);
            });
    }
    const dashboardExport = (id:any) => report(id, 'dashboard_export', t($ => $.page.dashboards.action.export), 'JSON')

    return {
        dashboardExport,
        exporting,
    };
};

const columns = [
    {
        field: 'titol',
        flex: 1,
    },
    {
        field: 'descripcio',
        flex: 3,
    },
    {
        field: 'aplicacio',
        flex: 1,
    },
    {
        field: 'entorn',
        flex: 1,
    },
];

const AclEntryForm: React.FC = () => {
    const { t } = useTranslation();
    return <Grid container spacing={2}>
        <Grid size={4}>
            <FormField name="subjectType" />
        </Grid>
        <Grid size={8}>
            <FormField name="subjectValue" />
        </Grid>
        <Grid size={12}>
            <FormField name="readAllowed" label={t($ => $.page.dashboards.acl.readAllowed)} />
        </Grid>
        <Grid size={12}>
            <FormField name="writeAllowed" label={t($ => $.page.dashboards.acl.writeAllowed)} />
        </Grid>
    </Grid>;
}

const EstadisticaDashboards: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.dashboard, t($ => $.page.dashboards.title));
    const gridApiRef = useMuiDataGridApiRef();
    const isUserAdmin =  useIsUserAdmin()
    const isUserUsuari =  useIsUserUsuari()

    const additionalColumns:any = useMemo(() => [
        ...columns,
        ...(!isUserUsuari ?[{
            field: 'numPermisos',
            headerName: '',
            sortable: false,
            flex: 0.5,
            renderCell: (params:any) => <IconButton
                title={t($ => $.components.permisos.title)}
                onClick={() => permissionShow(params.id, params.row?.titol ?? '')}
            >
                <Badge badgeContent={params.row.numPermisos} color={'primary'}><Icon>lock</Icon></Badge>
            </IconButton>
        }] :[])
    ], [t, columns, isUserUsuari])

    const aclColumns = useMemo(() => [{
        field: 'subjectType',
        sortable: false,
        flex: 2
    }, {
        field: 'subjectValue',
        sortable: false,
        flex: 4
    }, {
        field: 'readAllowed',
        headerName: t($ => $.page.dashboards.acl.readAllowed),
        sortable: false,
        flex: 1
    }, {
        field: 'writeAllowed',
        headerName: t($ => $.page.dashboards.acl.writeAllowed),
        sortable: false,
        flex: 1
    }], [t]);

    const {
        show: permissionShow,
        component: permissionComponent
    } = useAclCustomPermissionManager({
        resourceType: 'DASHBOARD',
        columns: aclColumns,
        formContent: <AclEntryForm/>
    });
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }
    const { dashboardExport, exporting } = useActions();
    const {handleShow: showCloneDashboard, content: contentCloneDashboard} = useCloneDashboardAction(refresh);
    const {handleShow: showImport, content: contentImport} = useImportDashboardAction(refresh);
    return (
        <>
            <PageTitle title={t($ => $.page.dashboards.title)} />
            {exporting && (
                <Backdrop
                    open={exporting}
                    sx={{
                        color: 'common.white',
                        zIndex: (theme) => theme.zIndex.drawer + 1,
                        display: 'flex',
                        flexDirection: 'column',
                        gap: 2
                    }}
                >
                    <CircularProgress color="inherit" />
                    <Typography variant="h6">{t($ => $.page.dashboards.action.export)}</Typography>
                </Backdrop>
            )}
            <ScreenFrame>
                <MuiDataGrid
                    {...screenGridProps}
                    resourceName="dashboard"
                    columns={additionalColumns}
                    apiRef={gridApiRef}
                    perspectives={['PERMIS_NUM']}
                    namedQueries={['WRITE']}
                    paginationActive
                    rowHideUpdateButton
                    popupEditActive
                    popupEditFormContent={<EstadisticaDashboardForm/>}
                    toolbarElementsWithPositions={isUserAdmin ?[
                        {
                            position: 2,
                            element: <IconButton
                                title={t($ => $.page.dashboards.action.import.label)}
                                onClick={showImport}
                            >
                                <Icon>upload</Icon>
                            </IconButton>
                        }
                    ] :[]}
                    rowAdditionalActions={[
                        {
                            label: t($ => $.page.dashboards.edit),
                            icon: 'edit',
                            clickShowUpdateDialog: true,
                        },
                        {
                            label: t($ => $.page.dashboards.dashboardView),
                            icon: 'dashboard',
                            showInMenu: false,
                            linkTo: '{{id}}',
                        },
                        {
                            label: t($ => $.page.dashboards.action.export),
                            icon: 'download',
                            showInMenu: true,
                            report: 'dashboard_export',
                            onClick: dashboardExport,
                        },
                        {
                            label: t($ => $.page.dashboards.cloneDashboard.title),
                            icon: 'file_copy',
                            showInMenu: true,
                            action: "clone_dashboard",
                            onClick: showCloneDashboard,
                        },
                    ]}
                />
            </ScreenFrame>
            {contentCloneDashboard}
            {contentImport}
            {permissionComponent}
        </>
    );
};

export default EstadisticaDashboards;
