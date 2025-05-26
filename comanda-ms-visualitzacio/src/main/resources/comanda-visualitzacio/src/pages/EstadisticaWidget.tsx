import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Grid from '@mui/material/Grid2';
import {
    GridPage,
    MuiGrid,
    FormField,
    useFormContext,
} from 'reactlib';
import { Box, Divider, Tab, Tabs } from '@mui/material';

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

const EstadisticaWidgetFormFields: React.FC = () => {
    const { data } = useFormContext();
    return <>
        <Grid size={4}><FormField name="titol" /></Grid>
        <Grid size={8}><FormField name="descripcio" type="textarea"/></Grid>
        <Grid size={4}><FormField name="entornAppId" /></Grid>
        <Grid size={4}><FormField name="aplicacioNom" disabled /></Grid>
        <Grid size={4}><FormField name="entornNom" disabled /></Grid>

        {/* Lista de dimensiones, Convertir en llista de relacions o en tab apart. 
        <Grid size={12}><FormField name="dimensionsValor" type="multiselect" /></Grid>*/}

        {/* Periodo base, depenent des tipus apareixeran o no els camps de abaix */}
        <Grid size={4}><FormField name="periodeMode" /></Grid>

        {/* Campos PRESET */}
        { data?.periodeMode === "PRESET" && 
        <><Grid size={4}><FormField name="presetPeriode" /></Grid>
          <Grid size={4}><FormField name="presetCount" /></Grid></>}

        {/* Campos RELATIVE */}
        { data?.periodeMode === "RELATIU" && 
        <><Grid size={4}><FormField name="relatiuPuntReferencia" /></Grid>
        <Grid size={4}><FormField name="relatiuCount" /></Grid>
        <Grid size={4}><FormField name="relatiueUnitat" /></Grid>
        <Grid size={4}><FormField name="relatiuAlineacio" /></Grid></>}
        
        {/* Campos ABSOLUTE */}
        { data?.periodeMode === "ABSOLUT" && 
        <><Grid size={4}><FormField name="absolutTipus" /></Grid>
        <Grid size={4}><FormField name="absolutDataInici" type="date" /></Grid>
        <Grid size={4}><FormField name="absolutDataFi" type="date" /></Grid>
        <Grid size={4}><FormField name="absolutAnyReferencia" /></Grid>
        <Grid size={4}><FormField name="absolutAnyValor" /></Grid>
        <Grid size={4}><FormField name="absolutPeriodeUnitat" /></Grid>
        <Grid size={4}><FormField name="absolutPeriodeInici" /></Grid>
        <Grid size={4}><FormField name="absolutPeriodeFi" /></Grid></>}
    </>;
}

const EstadisticaGraficWidgetForm: React.FC = () => {
    return <Grid container spacing={2} > 
        <EstadisticaWidgetFormFields/>
        <Grid size={12}> <Divider sx={{ my: 1 }} /> </Grid>
        <Grid size={4}><FormField name="indicador" /></Grid>
        <Grid size={4}><FormField name="tipusGrafic" /></Grid>
        <Grid size={4}><FormField name="tipusValors" /></Grid>
        <Grid size={4}><FormField name="tempsAgrupacio" /></Grid>
        <Grid size={4}><FormField name="dimensioDescomposicio" /></Grid>
        <Grid size={4}><FormField name="llegendaX" /></Grid>
        <Grid size={4}><FormField name="llegendaY" /></Grid>
    </Grid>;
}

const EstadisticaGraficWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    return <MuiGrid
            title={t('page.estadisticaGraficWidget.title')}
            resourceName="estadisticaGraficWidget"
            columns={columns}
            toolbarType="upper"
            paginationActive
            popupEditActive
            popupEditFormContent={<EstadisticaGraficWidgetForm/>}
        />;
}

const EstadisticaSimpleWidgetForm: React.FC = () => {
    return <Grid container spacing={2}>
        <EstadisticaWidgetFormFields />
        <Grid size={12}><Divider sx={{ my: 1 }} /></Grid>
        <Grid size={6}><FormField name="indicador" /></Grid>
        <Grid size={6}><FormField name="unitat" /></Grid>
    </Grid>;
};

const EstadisticaSimpleWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    return <MuiGrid
        title={t('page.estadisticaSimpleWidget.title')}
        resourceName="estadisticaSimpleWidget"
        columns={columns}
        toolbarType="upper"
        paginationActive
        popupEditActive
        popupEditFormContent={<EstadisticaSimpleWidgetForm />}
    />;
};

const EstadisticaTaulaWidgetForm: React.FC = () => {
    return <Grid container spacing={2}>
        <EstadisticaWidgetFormFields />
        <Grid size={12}><Divider sx={{ my: 1 }} /></Grid>
        <Grid size={6}><FormField name="dimensioAgrupacio" /></Grid>
        <Grid size={6}><FormField name="titolAgrupament" /></Grid>
        <Grid size={12}><FormField name="columnes" type="multiselect" /></Grid>
    </Grid>;
};

const EstadisticaTaulaWidgetGrid: React.FC = () => {
    const { t } = useTranslation();
    return <MuiGrid
        title={t('page.estadisticaTaulaWidget.title')}
        resourceName="estadisticaTaulaWidget"
        columns={columns}
        toolbarType="upper"
        paginationActive
        popupEditActive
        popupEditFormContent={<EstadisticaTaulaWidgetForm />}
    />;
};

const estadisticaWidget: React.FC = () => {
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };
    return <GridPage disableMargins>
        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
            <Tabs value={tab} onChange={handleChange}>
                <Tab label={t('page.estadisticaTaulaWidget.grafic')} />
                <Tab label={t('page.estadisticaTaulaWidget.simple')} />
                <Tab label={t('page.estadisticaTaulaWidget.taula')} />
            </Tabs>
        </Box>
        {tab === 0 && (<EstadisticaGraficWidgetGrid />)}
        {tab === 1 && (<EstadisticaSimpleWidgetGrid />)}
        {tab === 2 && (<EstadisticaTaulaWidgetGrid />)}
    </GridPage>;
};

export default estadisticaWidget;
