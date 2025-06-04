import Grid from "@mui/material/Grid2";
import {FormField, useFormContext, springFilterBuilder as builder } from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import SimpleWidgetVisualization from "./SimpleWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { Divider, Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";


const EstadisticaSimpleWidgetForm: React.FC = () => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    const [previewData, setPreviewData] = useState({
        title: 'Títol del widget',
        value: 1234,
        unit: 'unitat',
        icona: 'trending_up',
        colorText: undefined,
        colorFons: undefined,
        colorIcona: undefined,
        colorFonsIcona: undefined,
        destacat: false,
        colorTextDestacat: undefined,
        colorFonsDestacat: undefined,
        borde: false,
        colorBorde: undefined,
    });

    // Watch for changes in form data to update preview
    useEffect(() => {
        if (data) {
            setPreviewData({
                title: data.titol || 'Títol del widget',
                value: 1234, // Sample value for preview
                unit: data.unitat || 'unitat',
                icona: data.atributsVisuals?.icona || 'trending_up',
                colorText: data.atributsVisuals?.colorText,
                colorFons: data.atributsVisuals?.colorFons,
                colorIcona: data.atributsVisuals?.colorIcona,
                colorFonsIcona: data.atributsVisuals?.colorFonsIcona,
                destacat: data.atributsVisuals?.destacat || false,
                colorTextDestacat: data.atributsVisuals?.colorTextDestacat,
                colorFonsDestacat: data.atributsVisuals?.colorFonsDestacat,
                borde: data.atributsVisuals?.borde || false,
                colorBorde: data.atributsVisuals?.colorBorde,
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

            <Grid id={'cv'} size={{xs: 12, sm: 4}}>
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
                <Grid size={12}><FormField name="atributsVisuals.icona" label="Icona" /></Grid>

                {/*<Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Colors</Typography></Grid>*/}
                <Grid size={6}><FormField name="atributsVisuals.colorText" label="Color del text" type="color" value={data?.atributsVisuals?.colorText || '#000000'} required={false} /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.colorFons" label="Color de fons" type="color" value={data?.atributsVisuals?.colorFons || '#FFFFFF'} required={false} /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.colorIcona" label="Color de la icona" type="color" value={data?.atributsVisuals?.colorIcona || '#000000'} required={false} /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.colorFonsIcona" label="Color de fons de la icona" type="color" value={data?.atributsVisuals?.colorFonsIcona || '#FFFFFF'} required={false} /></Grid>

                {/*<Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Destacat</Typography></Grid>*/}
                <Grid size={12}><FormField name="atributsVisuals.destacat" label="Destacat" type="checkbox" /></Grid>
                <Grid size={6}><FormField
                    name="atributsVisuals.colorTextDestacat"
                    label="Color del text destacat"
                    type="color"
                    value={data?.atributsVisuals?.colorTextDestacat || '#FFFFFF'}
                    required={false}
                    disabled={!data?.atributsVisuals?.destacat}
                /></Grid>
                    <Grid size={6}><FormField
                    name="atributsVisuals.colorFonsDestacat"
                    label="Color de fons destacat"
                    type="color"
                    value={data?.atributsVisuals?.colorFonsDestacat || '#000000'}
                    required={false}
                    disabled={!data?.atributsVisuals?.destacat}
                /></Grid>

                {/*<Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Vora</Typography></Grid>*/}
                <Grid size={12}><FormField name="atributsVisuals.borde" label="Mostrar vora" type="checkbox" /></Grid>
                <Grid size={6}><FormField
                    name="atributsVisuals.colorBorde"
                    label="Color de la vora"
                    type="color"
                    value={data?.atributsVisuals?.colorBorde || '#CCCCCC'}
                    required={false}
                    disabled={!data?.atributsVisuals?.borde}
                /></Grid>
            </Grid>
        );
    }
}

export default EstadisticaSimpleWidgetForm;
