import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    FormField,
} from 'reactlib';

const EstadisticaDashboards: React.FC = () => {
    const { t } = useTranslation();
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
