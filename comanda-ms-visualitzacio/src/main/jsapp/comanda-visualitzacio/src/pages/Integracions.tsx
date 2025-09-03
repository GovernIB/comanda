import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiDataGrid,
    FormField,
} from 'reactlib';
import BlockIcon from '@mui/icons-material/Block';
import LogoUpload from "../components/LogoUpload.tsx";

const Integracions: React.FC = () => {
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
            }
            ,
        },
        {
            field: 'codi',
            flex: 2,
        },
        {
            field: 'nom',
            flex: 7,
        },
    ];
    return <GridPage>
        <MuiDataGrid
            title={t('page.integracions.title')}
            resourceName="integracio"
            columns={columns}
            toolbarType="upper"
            paginationActive
            popupEditActive
            toolbarHideCreate
            rowHideDeleteButton
            popupEditFormContent={
                <Grid container spacing={2}>
                    <Grid size={12}><FormField name="codi" disabled={true} /></Grid>
                    <Grid size={12}><FormField name="nom" disabled={true} /></Grid>
                    <Grid size={12}><LogoUpload name="logo" /></Grid>
                </Grid>
            }
        />
    </GridPage>;
};

export default Integracions;
