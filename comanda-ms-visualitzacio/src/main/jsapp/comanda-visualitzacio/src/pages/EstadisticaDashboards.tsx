import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    FormField,
    envVar,
    useFormContext,
    springFilterBuilder,
    useResourceApiService,
    useBaseAppContext,
    MuiFormDialogApi,
    useMuiDataGridApiRef,
} from 'reactlib';
import { envVars } from '../main';
import {iniciaDescargaJSON} from "../util/commonsActions.ts";
import FormActionDialog from '../components/FormActionDialog.tsx';

const fetchOptions = async (endpoint: string, listKey: string, q?: string, filter?: string) => {
    const url = new URL(`${envVar('VITE_API_URL', envVars)}/${endpoint}`);
    url.searchParams.append('page', 'UNPAGED');
    if (filter) url.searchParams.append('filter', filter);
    if (q) url.searchParams.append('q', q);

    const response = await fetch(url.toString());
    const data = await response.json();
    return {
        options: data._embedded?.[listKey]?.map((item) => ({
            id: item.id,
            description: item.nom,
        })) || [],
        page: data.page,
    };
};

const EstadisticaDashboardForm: React.FC = () => {
    const { data } = useFormContext();
    const filterAplicacio = springFilterBuilder.and(
        springFilterBuilder.eq('activa', true),
        springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id))));
    const filterEntorn = springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', data?.aplicacio?.id)));
    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField name="titol" />
            </Grid>
            <Grid size={12}>
                <FormField name="descripcio" />
            </Grid>
            <Grid size={12}>
                <FormField name="aplicacio" optionsRequest={(q) => fetchOptions('apps', 'appList', q, filterAplicacio)} />
            </Grid>
            <Grid size={12}>
                <FormField name="entorn" optionsRequest={(q) => fetchOptions('entorns', 'entornList', q, filterEntorn)} />
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
        temporalMessageShow(null, t('page.dashboards.cloneDashboard.success'), 'success');
    }
    const formulario =
        <FormActionDialog
            resourceName={"dashboard"}
            action={"clone_dashboard"}
            apiRef={apiRef}
            title={t('page.dashboards.cloneDashboard.title')}
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
    const dashboardExport = (id:any) => report(id, 'dashboard_export', t('page.dashboard.action.export'), 'JSON')

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
        <GridPage disableMargins>
            <MuiGrid
                title={t('page.dashboards.title')}
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
                        title: t('page.dashboards.edit'),
                        icon: 'edit',
                        clickShowUpdateDialog: true,
                    },
                    {
                        title: t('page.dashboards.dashboardView'),
                        icon: 'dashboard',
                        showInMenu: false,
                        linkTo: '{{id}}',
                    },
                    {
                        title: t('page.dashboards.action.export'),
                        icon: 'download',
                        showInMenu: true,
                        onClick: dashboardExport,
                    },
                    {
                        title: t('page.dashboards.cloneDashboard.title'),
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
