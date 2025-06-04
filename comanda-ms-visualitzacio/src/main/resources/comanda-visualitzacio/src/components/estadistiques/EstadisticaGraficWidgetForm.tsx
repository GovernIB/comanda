import Grid from "@mui/material/Grid2";
import {Divider, Box, Typography} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import GraficWidgetVisualization from "./GraficWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";

const EstadisticaGraficWidgetForm: React.FC = () => {
    const { data } = useFormContext();
    const { t } = useTranslation();
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

    // Get current graphic type (BAR_CHART, LINE_CHART, PIE_CHART, SCATTER_CHART, SPARK_LINE_CHART, GAUGE_CHART, HEATMAP_CHART)
    const chartType = data?.tipusGrafic;
    const isBarTypeVisible = chartType === 'BAR_CHART';
    const isLineTypeVisible = chartType === 'LINE_CHART' || chartType === 'SPARK_LINE_CHART';
    const isPieTypeVisible = chartType === 'PIE_CHART';
    const isGaugeTypeVisible = chartType === 'GAUGE_CHART';
    const isHeatTypeVisible = chartType === 'HEATMAP_CHART';

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                        <Grid size={12}><FormField name="indicador" advancedSearchColumns={columnesIndicador}/></Grid>
                        <Grid size={4}><FormField name="tipusGrafic" /></Grid>
                        <Grid size={4}><FormField name="tipusValors" /></Grid>
                        <Grid size={4}><FormField name="tempsAgrupacio" /></Grid>
                        <Grid size={12}><FormField name="descomposicioDimensio" /></Grid>
                        <Grid size={6}><FormField name="llegendaX" /></Grid>
                        <Grid size={6}><FormField name="llegendaY" /></Grid>
                </EstadisticaWidgetFormFields>
            </Grid>

            <Grid id={'cv'} size={{xs: 12, sm: 4}}>
                <VisualAttributesPanel widgetType="grafic" title="Configuració visual">
                    {/* Preview inside the panel */}
                    <Box sx={{ p: 2 }}>
                        <Typography variant="subtitle2" sx={{ mb: 2 }}>Previsualització</Typography>
                        <Box sx={{ height: '240px' }}>
                            <GraficWidgetVisualization
                                preview={true}
                                tipusGrafic={chartType}
                                {...previewData}
                            />
                        </Box>
                        {renderGraficFormFields()}
                    </Box>
                </VisualAttributesPanel>
            </Grid>
        </Grid>
    );

    // Render form fields for grafic widget
    function renderGraficFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Configuració general</Typography></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorsPaleta" label="Colors de la paleta" /></Grid>
                <Grid size={12}><FormField name="atributsVisuals.mostrarReticula" label="Mostrar retícula" type="checkbox" /></Grid>

                {isBarTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de barres</Typography></Grid>
                        <Grid size={6}><FormField name="atributsVisuals.barStacked" label="Barres apilades" type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="atributsVisuals.barHorizontal" label="Barres horitzontals" type="checkbox" /></Grid>
                    </>
                )}

                {isLineTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de línies</Typography></Grid>
                        <Grid size={6}><FormField name="atributsVisuals.lineShowPoints" label="Mostrar punts" type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="atributsVisuals.lineSmooth" label="Línies suaus" type="checkbox" /></Grid>
                        <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.lineWidth" label="Amplada de línia" type="number" required={false} /></Grid>
                    </>
                )}

                {isPieTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de pastís</Typography></Grid>
                        <Grid size={6}><FormField name="atributsVisuals.pieDonut" label="Tipus donut" type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="atributsVisuals.pieShowLabels" label="Mostrar etiquetes" type="checkbox" /></Grid>
                    </>
                )}

                {isGaugeTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de gauge</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeMin" label="Valor mínim" type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeMax" label="Valor màxim" type="number" required={false} /></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.gaugeColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeRangs" label="Rangs (separats per comes)" /></Grid>
                    </>
                )}

                {isHeatTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de heatmap</Typography></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.heatmapColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.heatmapMinValue" label="Valor mínim" type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.heatmapMaxValue" label="Valor màxim" type="number" required={false} /></Grid>
                    </>
                )}
            </Grid>
        );
    }
}

export default EstadisticaGraficWidgetForm;
