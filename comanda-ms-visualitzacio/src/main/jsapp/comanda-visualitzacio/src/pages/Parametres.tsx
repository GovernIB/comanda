import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid';
import {
    GridPage,
    MuiGrid,
    FormField,
} from 'reactlib';
import DataGridNoRowsOverlay from '../../lib/components/mui/datagrid/DataGridNoRowsOverlay';
import { GridSlots } from '@mui/x-data-grid-pro';

const ParametreForm: React.FC = () => {

    return (
        <Grid container spacing={2}>
            <Grid size={4}><FormField name="codi" readOnly disabled /></Grid>
            <Grid size={8}><FormField name="nom" /></Grid>
            <Grid size={12}><FormField name="descripcio" /></Grid>
            <Grid size={4}><FormField name="tipus" readOnly disabled/></Grid>
            <Grid size={8}><FormField name="valor" /></Grid>
        </Grid>
    );
}

const columns = [
        {
            field: 'nom',
            flex: 1,
        },
        {
            field: 'descripcio',
            flex: 3,
        },
        {
            field: 'valor',
            flex: 1,
        },
        // {
        //     field: 'tipus',
        //     flex: 1,
        // },
        {
            field: 'editable',
            flex: 0.5,
        },
    ];

const Parametres: React.FC = () => {
    const { t } = useTranslation();
    return <GridPage disableMargins>
        <MuiGrid
            title={t('page.parametres.title')}
            resourceName="parametre"
            columns={columns}
            toolbarType="upper"
            paginationActive
            toolbarHideCreate
            rowHideUpdateButton//TODO El boton debe depender del valor de la row
            popupEditActive
            popupEditFormContent={<ParametreForm />}
            rowHideDeleteButton
            treeData
            getTreeDataPath={(row) => [row.grup, row.subGrup, row.codi]}
            defaultGroupingExpansionDepth={-1}
            groupingColDef={{
                flex: 2,
                minWidth: 100,}}
            hideFooter
            slots={{
                noRowsOverlay: DataGridNoRowsOverlay as GridSlots['noRowsOverlay'],
            }}
            // autoHeight
        />
    </GridPage>;
};

export default Parametres;
