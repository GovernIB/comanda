import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router-dom';
import Grid from '@mui/material/Grid';
import {
    FormField,
    GridPage,
    MuiForm,
    MuiFormTabContent,
    MuiGrid,
    springFilterBuilder, useBaseAppContext,
    useFormContext,
} from 'reactlib';
import LogoUpload from '../components/LogoUpload';
import BlockIcon from '@mui/icons-material/Block';
import MuiFormTabs, { FormTabsValue } from '../../lib/components/mui/form/MuiFormTabs.tsx';

const AppEntornForm: React.FC = () => {
    const { data } = useFormContext();
    const { id: appId } = useParams();
    const entornFilter = springFilterBuilder.not(
        springFilterBuilder.exists(springFilterBuilder.eq('entornAppEntities.app.id', appId))
    );

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField
                    name="entorn"
                    disabled={data?.id != null || undefined}
                    filter={entornFilter}
                />
            </Grid>
            <Grid size={12}>
                <FormField name="infoUrl" />
            </Grid>
            <Grid size={4}>
                <FormField name="infoInterval" />
            </Grid>

            <Grid size={12}>
                <FormField name="salutUrl" />
            </Grid>
            <Grid size={4}>
                <FormField name="salutInterval" />
            </Grid>
            <Grid size={12}>
                <FormField name="estadisticaInfoUrl" />
            </Grid>

            <Grid size={12}>
                <FormField name="estadisticaUrl" />
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
    );
};

const AppFormContent = () => (
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
);

export const AppForm: React.FC = () => {
    const { t } = useTranslation();
    const { id } = useParams();
    const { setMarginsDisabled } = useBaseAppContext();
    React.useEffect(() => {
        setMarginsDisabled(true);
        return () => setMarginsDisabled(false);
    }, []);

    const isCreation = id == null;

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
            key={id}
            id={id}
            title={id ? t('page.apps.update') : t('page.apps.create')}
            resourceName="app"
            goBackLink="/app"
            createLink="form/{{id}}"
        >
            {isCreation && <AppFormContent />}
            <MuiFormTabs tabs={formTabs} tabIndexesWithGrids={[1]}>
                <MuiFormTabContent index={0} showOnCreate>
                    <AppFormContent />
                </MuiFormTabContent>
                <MuiFormTabContent index={1}>
                    <AppsEntorns />
                </MuiFormTabContent>
            </MuiFormTabs>
        </MuiForm>
    );
};

const Apps: React.FC = () => {
    const { t } = useTranslation();
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
            />
        </GridPage>
    );
};

export default Apps;
