import Grid from "@mui/material/Grid";
import {FormField, useFormContext, springFilterBuilder as builder } from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import SimpleWidgetVisualization from "./SimpleWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { Divider, Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import IconAutocompleteSelect from "../IconAutocompleteSelect.tsx";


const EstadisticaSimpleWidgetForm: React.FC = () => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    const [previewData, setPreviewData] = useState({
        titol: 'Títol del widget',
        valor: 1234,
        unitat: 'unitat',
        descripcio: 'descripcio del widget',
        canviPercentual: '12.34%',
        icona: 'trending_up',
        colorText: undefined,
        colorFons: undefined,
        colorIcona: undefined,
        colorFonsIcona: undefined,
        colorTextDestacat: undefined,
        vora: false,
        colorVora: undefined,
        ampleVora: undefined,
    });

    // Watch for changes in form data to update preview
    useEffect(() => {
        if (data) {
            setPreviewData({
                titol: data.titol || 'Títol del widget',
                valor: 1234, // Sample value for preview
                unitat: data.unitat || 'unitat',
                descripcio: data.descripcio || 'descripcio del widget',
                canviPercentual: data.canviPercentual || '12.34%',
                icona: data["atributsVisuals.icona"],
                colorText: data["atributsVisuals.colorText"],
                colorFons: data["atributsVisuals.colorFons"],
                colorIcona: data["atributsVisuals.colorIcona"],
                colorFonsIcona: data["atributsVisuals.colorFonsIcona"],
                colorTextDestacat: data["atributsVisuals.colorTextDestacat"],
                vora: data["atributsVisuals.vora"] || false,
                colorVora: data["atributsVisuals.colorVora"],
                ampleVora: data["atributsVisuals.ampleVora"],
            });
        }
    }, [data]);


    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                    <Grid size={12}><Divider sx={{ my: 1 }} >{t('page.widget.form.simple')}</Divider></Grid>
                    <Grid size={6}><FormField name="unitat" /></Grid>
                    <Grid size={6}><FormField name="compararPeriodeAnterior" /></Grid>
                    <Grid size={12}><FormField name="indicador" advancedSearchColumns={columnesIndicador}/></Grid>
                    <Grid size={12}><FormField name="titolIndicador" /></Grid>
                    <Grid size={6}><FormField name="tipusIndicador" /></Grid>
                    <Grid size={6}><FormField name="periodeIndicador" /></Grid>
                </EstadisticaWidgetFormFields>
            </Grid>

            <Grid id={'cv'} size={{xs: 12, sm: 4}} sx={{backgroundColor: '#f5f5f5'}}>
                <VisualAttributesPanel widgetType="simple" title="Configuració visual">
                    {/* Preview inside the panel */}
                    <Box sx={{ p: 2 }}>
                        <Typography variant="subtitle2" sx={{ mb: 2 }}>Previsualització</Typography>
                        <Box sx={{ height: '190px' }}>
                            <SimpleWidgetVisualization
                                preview={true}
                                {...previewData}
                            />
                        </Box>
                        {renderSimpleFormFields()}
                    </Box>
                </VisualAttributesPanel>
            </Grid>
        </Grid>
    );

    // Render form fields for simple widget
    function renderSimpleFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Configuració general</Typography></Grid>
                {/*<Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.icona" label="Icona" /></Grid>*/}
                {/*<Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><IconSelect/></Grid>*/}
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><IconAutocompleteSelect name="atributsVisuals.icona" label={"Icona"}/></Grid>

                {/*<Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Colors</Typography></Grid>*/}
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorText" label="Color del text" type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorFons" label="Color de fons" type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorIcona" label="Color de la icona" type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorFonsIcona" label="Color de fons de la icona" type="color" required={false} /></Grid>

                {/*<Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Destacat</Typography></Grid>*/}
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorTextDestacat" label="Color del text destacat" type="color" required={false} /></Grid>
                <Grid size={6} />
                {/*<Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Vora</Typography></Grid>*/}
                <Grid size={6}><FormField name="atributsVisuals.vora" label="Mostrar vora" type="checkbox" /></Grid>
                <Grid size={6} />
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorVora" label="Color de la vora" type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.ampleVora" label="Ample de la vora" type="number" required={false} /></Grid>
            </Grid>
        );
    }
}

export default EstadisticaSimpleWidgetForm;
