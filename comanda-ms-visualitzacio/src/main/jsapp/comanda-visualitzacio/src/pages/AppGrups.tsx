import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';
import { FormField, MuiDataGrid, MuiDataGridColDef } from 'reactlib';
import PageTitle from '../components/PageTitle.tsx';

const ColorCell: React.FC<{ value?: string }> = ({ value }) => {
    if (!value) return null;
    return (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box
                sx={{
                    width: 18,
                    height: 18,
                    borderRadius: '4px',
                    backgroundColor: value,
                    border: '1px solid rgba(0,0,0,0.2)',
                    flexShrink: 0,
                }}
            />
            <span>{value}</span>
        </Box>
    );
};

const AppGrups: React.FC = () => {
    const { t } = useTranslation();

    const columns: MuiDataGridColDef[] = React.useMemo(
        () => [
            {
                field: 'nom',
                headerName: t(($) => $.page.appGrups.nom),
                flex: 3,
            },
            {
                field: 'colorClar',
                headerName: t(($) => $.page.appGrups.colorClar),
                flex: 1.5,
                renderCell: (params) => <ColorCell value={params.value} />,
            },
            {
                field: 'colorFosc',
                headerName: t(($) => $.page.appGrups.colorFosc),
                flex: 1.5,
                renderCell: (params) => <ColorCell value={params.value} />,
            },
        ],
        [t]
    );

    return (
        <>
            <PageTitle title={t(($) => $.page.appGrups.title)} />
            <MuiDataGrid
                title={t(($) => $.page.appGrups.title)}
                resourceName="appGrup"
                popupEditFormDialogResourceTitle={t(($) => $.page.appGrups.resourceTitle)}
                columns={columns}
                toolbarType="upper"
                paginationActive
                popupEditActive
                popupEditFormContent={
                    <Grid container spacing={2}>
                        <Grid size={12}>
                            <FormField name="nom" required />
                        </Grid>
                        <Grid size={6}>
                            <FormField name="colorClar" type="color" required={false} />
                        </Grid>
                        <Grid size={6}>
                            <FormField name="colorFosc" type="color" required={false} />
                        </Grid>
                    </Grid>
                }
            />
        </>
    );
};

export default AppGrups;
