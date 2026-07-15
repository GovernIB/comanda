import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import {
    GridPage,
    MuiDataGrid,
    FormField,
    useFormContext,
    springFilterBuilder,
    useResourceApiService,
    useBaseAppContext,
    MuiFormDialogApi,
    useMuiDataGridApiRef,
} from 'reactlib';
import {iniciaDescargaJSON} from "../util/commonsActions.ts";
import FormActionDialog from '../components/FormActionDialog.tsx';
import { findOptions } from '../util/requestUtils.ts';
import PageTitle from '../components/PageTitle.tsx';
import IconButton from "@mui/material/IconButton";
import Icon from "@mui/material/Icon";

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

type Conflicte = { titol: string; overwrite?: string; nouNom?: string };

const ImportConflictRow: React.FC<{
    index: number;
    conflict: any;
    onChange: (changes: object) => void;
}> = ({ index, conflict, onChange }) => {
    const { fields } = useFormContext();

    const fieldOverwrite = fields?.filter(i=>i.name=='overwrite')[0];
    const fieldNouNom = fields?.filter(i=>i.name=='nouNom')[0];

    return (
        <Grid container spacing={1} alignItems="center" sx={{ mb: 1 }}>
            <Grid size={4} sx={{display: 'flex', alignItems: 'center'}}>
                {conflict.tipo == "DashboardExport" && <Icon>dashboard</Icon>}
                {conflict.tipo == "EstadisticaWidgetExport" && <Icon>widgets</Icon>}
                {conflict.tipo == "PlantillaExport" && <Icon>palette</Icon>}
                {conflict.tipo == "PaletaExport" && <Icon>format_color_fill</Icon>}
                <Typography variant="body2" sx={{ml: 2}}>{conflict.titol}</Typography>
            </Grid>
            <Grid size={4}>
                <FormField
                    name={`conflicts[${index}].overwrite`}
                    value={conflict.overwrite}
                    field={fieldOverwrite}
                    onChange={(value)=> onChange({overwrite: value})}
                    componentProps={{ size: "small" }}
                    required
                />
            </Grid>
            {conflict.overwrite === 'CREAR_AMB_ALTRE_NOM' && (
                <Grid size={4}>
                    <FormField
                        name={`conflicts[${index}].nouNom`}
                        field={fieldNouNom}
                        value={conflict.nouNom}
                        onChange={(value) => onChange({nouNom: value})}
                    />
                </Grid>
            )}
        </Grid>
    );
};

const DashboardImportConflictsForm: React.FC = () => {
    const { t } = useTranslation();
    const { data, apiRef } = useFormContext();
    const conflicts: Conflicte[] = data?.conflicts ?? [];

    const updateConflict = (index: number, changes: Partial<Conflicte>) => {
        const updated = conflicts.map((c, i) => (i === index ? { ...c, ...changes } : c));
        apiRef.current?.setFieldValue('conflicts', updated);
    };

    return (
        <>
            {conflicts.length > 0 && (<>
                <Grid size={12}>
                    <FormField name="overwrite" onChange={(value) => {
                        const updated = conflicts.map((c) => ({ ...c, overwrite: value }));
                        apiRef.current?.setFieldValue('conflicts', updated)
                    }} />
                </Grid>
                <Box sx={{ mt: 2 }}>
                    <Typography variant="subtitle2">
                        {t($ => $.page.dashboards.action.import.dashboardConflicts)}
                    </Typography>
                    {conflicts.map((c, i) => (
                        <ImportConflictRow
                            key={i + c.titol}
                            index={i}
                            conflict={c}
                            onChange={(changes) => updateConflict(i, changes)}
                        />
                    ))}
                </Box>
            </>)}
        </>
    );
};

const useImportDashboardAction = (refresh?: () => void) => {
    const { t } = useTranslation();
    const apiRef = React.useRef<MuiFormDialogApi>(null);
    const {temporalMessageShow} = useBaseAppContext();
    const handleShow = () :void => {
        apiRef.current?.show?.(undefined)
    }
    const onSuccess = () :void => {
        refresh?.();
        temporalMessageShow(null, t($ => $.page.dashboards.action.import.success), 'success');
    }
    const formulario =
        <FormActionDialog
            resourceName={"dashboard"}
            action={"dashboard_import"}
            apiRef={apiRef}
            title={t($ => $.page.dashboards.action.import.title)}
            onSuccess={onSuccess}
            initialOnChange={false}
        >
            <Grid container spacing={2}>
                <Grid size={12}>
                    <FormField name="file" type={"file"} />
                </Grid>
                <Grid size={12}>
                    <DashboardImportConflictsForm />
                </Grid>
            </Grid>
        </FormActionDialog>;
    return {
        handleShow,
        content: formulario
    }
}

const useActions = () => {
    const { artifactReport: apiReport } = useResourceApiService('dashboard');
    const { temporalMessageShow } = useBaseAppContext();
    const { t } = useTranslation();

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
    const dashboardExport = (id:any) => report(id, 'dashboard_export', t($ => $.page.dashboards.action.export), 'JSON')

    return {
        dashboardExport,
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

const EstadisticaDashboards: React.FC = () => {
    const { t } = useTranslation();
    const gridApiRef = useMuiDataGridApiRef();
    const refresh = () => {
        gridApiRef?.current?.refresh?.();
    }
    const { dashboardExport } = useActions();
    const {handleShow: showCloneDashboard, content: contentCloneDashboard} = useCloneDashboardAction(refresh);
    const {handleShow: showImport, content: contentImport} = useImportDashboardAction(refresh);
    return (
        <GridPage>
            <PageTitle title={t($ => $.page.dashboards.title)} />
            <MuiDataGrid
                title={t($ => $.page.dashboards.title)}
                resourceName="dashboard"
                columns={columns}
                apiRef={gridApiRef}
                toolbarType="upper"
                paginationActive
                rowHideUpdateButton
                popupEditActive
                popupEditFormContent={<EstadisticaDashboardForm/>}
                toolbarElementsWithPositions={[
                    {
                        position: 2,
                        element: <IconButton
                            title={t($ => $.page.dashboards.action.import.label)}
                            onClick={showImport}
                        >
                            <Icon>download</Icon>
                        </IconButton>
                    }
                ]}
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
            {contentCloneDashboard}
            {contentImport}
        </GridPage>
    );
};

export default EstadisticaDashboards;
