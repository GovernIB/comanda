import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    FormField, useResourceApiService, useBaseAppContext,
} from 'reactlib';
import {iniciaDescargaJSON} from "../util/commonsActions.ts";
import {DataCommonAdditionalAction} from "../../lib/components/mui/datacommon/MuiDataCommon.tsx";

const useActions = (refresh?: () => void) => {
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

const EstadisticaDashboards: React.FC = () => {
    const { t } = useTranslation();
    const { dashboardExport } = useActions();
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
    return (
        <GridPage disableMargins>
            <MuiGrid
                title={t('page.dashboards.title')}
                resourceName="dashboard"
                columns={columns}
                toolbarType="upper"
                paginationActive
                rowHideUpdateButton
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
                ]}
                popupEditActive
                popupEditFormContent={
                    <Grid container spacing={2}>
                        <Grid size={12}>
                            <FormField name="titol" />
                        </Grid>
                        <Grid size={12}>
                            <FormField name="descripcio" />
                        </Grid>
                    </Grid>
                }
            />
        </GridPage>
    );
};

export default EstadisticaDashboards;
