import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    MuiDataGrid,
    FormField,
} from 'reactlib';
import BlockIcon from '@mui/icons-material/Block';
import LogoUpload from "../components/LogoUpload.tsx";
import PageTitle from '../components/PageTitle.tsx';
import useReadOnlyGestor from '../hooks/useReadOnlyGestor.ts';
import { ScreenFrame } from '../lot2';
import useScreenGridProps from '../hooks/useScreenGridProps.tsx';
import { screenIcons } from '../util/screenIcons.ts';

const Integracions: React.FC = () => {
    const { t } = useTranslation();
    const screenGridProps = useScreenGridProps(screenIcons.integracio, t($ => $.page.integracions.title));
    const readOnlyGestor = useReadOnlyGestor();
    const columns = [
        {
            field: 'logo',
            flex: 1,
            renderCell: (params: { value?: string }) => {
                const value = params.value; // Obtenir el valor de la cel·la
                return value ? (
                    <img
                        src={`data:image/png;base64,${value}`}
                        alt={t($ => $.page.apps.logoAlt)}
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
    return (
        <>
            <PageTitle title={t($ => $.page.integracions.title)} />
            <ScreenFrame>
                <MuiDataGrid
                    {...screenGridProps}
                    resourceName="integracio"
                    columns={columns}
                    paginationActive
                    popupEditActive
                    toolbarHideCreate
                    rowHideDeleteButton
                    rowHideUpdateButton={readOnlyGestor}
                    popupEditFormContent={
                        <Grid container spacing={2}>
                            <Grid size={12}><FormField name="codi" disabled={true} /></Grid>
                            <Grid size={12}><FormField name="nom" disabled={true} /></Grid>
                            <Grid size={12}><LogoUpload name="logo" /></Grid>
                        </Grid>
                    }
                />
            </ScreenFrame>
        </>
    );
};

export default Integracions;
