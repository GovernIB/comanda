import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
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

const EstadisticaDashboardForm: React.FC = () => {
    const { data } = useFormContext();
    const { isReady: appIsReady, find: appFind } = useResourceApiService("app");
    const { isReady: entornIsReady, find: entornFind } = useResourceApiService("entorn");
    const filterAplicacio = springFilterBuilder.and(
        springFilterBuilder.eq('activa', true),
        springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id))));
    const filterEntorn = springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', data?.aplicacio?.id)));

    if (!appIsReady || !entornIsReady)
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
        </GridPage>
    );
};

export default EstadisticaDashboards;
