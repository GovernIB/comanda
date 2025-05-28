import Grid from "@mui/material/Grid2";
import {Divider, Box} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import GraficWidgetVisualization from "./GraficWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";

const EstadisticaGraficWidgetForm: React.FC = () => {
    const { data } = useFormContext();
    const [previewData, setPreviewData] = useState({
        title: 'Títol del gràfic',
        tipusGrafic: 'BAR_CHART',
        llegendaX: 'Eix X',
        llegendaY: 'Eix Y',
        colorsPaleta: '#1f77b4,#ff7f0e,#2ca02c,#d62728,#9467bd,#8c564b',
        mostrarReticula: true,
        barStacked: false,
        barHorizontal: false,
        lineShowPoints: true,
        lineSmooth: false,
        lineWidth: 2,
        pieDonut: false,
        pieShowLabels: true,
    });

    // Watch for changes in form data to update preview
    useEffect(() => {
        if (data) {
            setPreviewData({
                title: data.titol || 'Títol del gràfic',
                tipusGrafic: data.tipusGrafic || 'BAR_CHART',
                llegendaX: data.llegendaX || 'Eix X',
                llegendaY: data.llegendaY || 'Eix Y',
                colorsPaleta: data.atributsVisuals?.colorsPaleta || '#1f77b4,#ff7f0e,#2ca02c,#d62728,#9467bd,#8c564b',
                mostrarReticula: data.atributsVisuals?.mostrarReticula !== undefined ? data.atributsVisuals.mostrarReticula : true,
                barStacked: data.atributsVisuals?.barStacked || false,
                barHorizontal: data.atributsVisuals?.barHorizontal || false,
                lineShowPoints: data.atributsVisuals?.lineShowPoints !== undefined ? data.atributsVisuals.lineShowPoints : true,
                lineSmooth: data.atributsVisuals?.lineSmooth || false,
                lineWidth: data.atributsVisuals?.lineWidth || 2,
                pieDonut: data.atributsVisuals?.pieDonut || false,
                pieShowLabels: data.atributsVisuals?.pieShowLabels !== undefined ? data.atributsVisuals.pieShowLabels : true,
                gaugeMin: data.atributsVisuals?.gaugeMin,
                gaugeMax: data.atributsVisuals?.gaugeMax,
                gaugeColors: data.atributsVisuals?.gaugeColors,
                gaugeRangs: data.atributsVisuals?.gaugeRangs,
                heatmapColors: data.atributsVisuals?.heatmapColors,
                heatmapMinValue: data.atributsVisuals?.heatmapMinValue,
                heatmapMaxValue: data.atributsVisuals?.heatmapMaxValue,
            });
        }
    }, [data]);

    return (
        <Box sx={{ display: 'flex', width: '100%' }}>
            <Box sx={{ flex: '1 1 auto' }}>
                <EstadisticaWidgetFormFields>
                    <Grid container spacing={2} >
                        <Grid size={12}><FormField name="indicador" /></Grid>
                        <Grid size={4}><FormField name="tipusGrafic" /></Grid>
                        <Grid size={4}><FormField name="tipusValors" /></Grid>
                        <Grid size={4}><FormField name="tempsAgrupacio" /></Grid>
                        <Grid size={12}><FormField name="dimensioDescomposicio" /></Grid>
                        <Grid size={6}><FormField name="llegendaX" /></Grid>
                        <Grid size={6}><FormField name="llegendaY" /></Grid>
                    </Grid>
                </EstadisticaWidgetFormFields>
            </Box>

            <Box sx={{ width: '500px', ml: 2, display: 'flex', flexDirection: 'column' }}>
                 Preview
                <Box sx={{ mb: 2, height: '300px' }}>
                    <GraficWidgetVisualization
                        preview={true}
                        {...previewData}
                    />
                </Box>

                {/* Visual attributes panel */}
                <Box sx={{ flex: 1 }}>
                    <VisualAttributesPanel 
                        widgetType="grafic"
                        title="Configuració visual"
                    />
                </Box>
            </Box>
        </Box>
    );
}

export default EstadisticaGraficWidgetForm;
