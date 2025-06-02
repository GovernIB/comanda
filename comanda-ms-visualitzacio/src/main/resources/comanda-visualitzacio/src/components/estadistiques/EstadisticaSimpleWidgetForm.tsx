import Grid from "@mui/material/Grid2";
import {FormField, useFormContext, springFilterBuilder as builder } from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import SimpleWidgetVisualization from "./SimpleWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';

const EstadisticaSimpleWidgetForm: React.FC = () => {
    const { data } = useFormContext();
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
            <Grid size={{xs: 12, sm:6}}>
                <EstadisticaWidgetFormFields>
                    <Grid size={6}><FormField name="unitat" /></Grid>
                    <Grid size={6}><FormField name="compararPeriodeAnterior" /></Grid>
                    <Grid size={12}><FormField name="indicador" advancedSearchColumns={columnesIndicador}/></Grid>
                    <Grid size={12}><FormField name="titolIndicador" /></Grid>
                    <Grid size={6}><FormField name="tipusIndicador" /></Grid>
                    <Grid size={6}><FormField name="periodeIndicador" /></Grid>
                </EstadisticaWidgetFormFields>
            </Grid>

            <Grid size={{xs: 12, sm:6}}>
                {/* Preview */}
                <Grid size={12}>
                    <SimpleWidgetVisualization
                        preview={true}
                        {...previewData}
                    />
                </Grid>

                {/* Visual attributes panel */}
                <Grid size={12}>
                    <VisualAttributesPanel
                        widgetType="simple"
                        title="Configuració visual"
                    />
                </Grid>
            </Grid> 
        </Grid>
    );
}

export default EstadisticaSimpleWidgetForm;
