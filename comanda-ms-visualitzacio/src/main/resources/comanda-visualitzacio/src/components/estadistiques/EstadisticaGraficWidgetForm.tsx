import Grid from "@mui/material/Grid";
import {Divider, Box, Typography} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import {useState, useMemo, useEffect} from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import GraficWidgetVisualization from "./GraficWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";
import ColorPaletteSelector from "../ColorPaletteSelector";
import ColumnesTable from "./ColumnesTable.tsx";
import {FormFieldDataActionType, FormFieldError} from "../../../lib/components/form/FormContext.tsx";

const EstadisticaGraficWidgetForm: React.FC = () => {
    const { data, dataDispatchAction } = useFormContext();
    const { t } = useTranslation();
    const previewData = useMemo(() =>({
        title: data.titol || 'Títol del gràfic',
        tipusGrafic: data.tipusGrafic || 'BAR_CHART',
        llegendaX: data.llegendaX || 'Eix X',
        llegendaY: data.llegendaY || 'Eix Y',
        colorsPaleta: data.colorsPaleta || '#1f77b4,#ff7f0e,#2ca02c,#d62728,#9467bd,#8c564b',
        mostrarReticula: data.mostrarReticula !== undefined ? data.mostrarReticula : true,
        barStacked: data.barStacked || false,
        barHorizontal: data.barHorizontal || false,
        lineShowPoints: data.lineShowPoints !== undefined ? data.lineShowPoints : true,
        lineSmooth: data.lineSmooth || false,
        lineWidth: data.lineWidth || 2,
        pieDonut: data.pieDonut || false,
        pieShowLabels: data.pieShowLabels !== undefined ? data.pieShowLabels : true,
        gaugeMin: data.gaugeMin,
        gaugeMax: data.gaugeMax,
        gaugeColors: data.gaugeColors,
        gaugeRangs: data.gaugeRangs,
        heatmapColors: data.heatmapColors,
        heatmapMinValue: data.heatmapMinValue,
        heatmapMaxValue: data.heatmapMaxValue,
    }), [data])

    // Get current graphic type (BAR_CHART, LINE_CHART, PIE_CHART, SCATTER_CHART, SPARK_LINE_CHART, GAUGE_CHART, HEATMAP_CHART)
    const chartType = data?.tipusGrafic;
    const isChartTypeSelected = chartType ?? false;
    const isPieTypeVisible = chartType === 'PIE_CHART';
    const isBarTypeVisible = chartType === 'BAR_CHART';
    const isLineTypeVisible = chartType === 'LINE_CHART';
    const isScatterTypeVisible = chartType === 'SCATTER_CHART';
    const isSparkLineTypeVisible = chartType === 'SPARK_LINE_CHART';
    const isGaugeTypeVisible = chartType === 'GAUGE_CHART';
    const isHeatTypeVisible = chartType === 'HEATMAP_CHART';
    const dataType: string = data?.tipusDades;
    const isUnIndicador: boolean = dataType === 'UN_INDICADOR';
    const isUnIndicadorAmbDescomposicio: boolean = dataType === 'UN_INDICADOR_AMB_DESCOMPOSICIO';
    const isVarisIndicadors: boolean = dataType === 'VARIS_INDICADORS';
    const isDosIndicadors: boolean = dataType === 'DOS_INDICADORS';
    const tipusDadesOcultar = useMemo((): string[] => {
        if (isPieTypeVisible || isScatterTypeVisible || isHeatTypeVisible) {
            console.log("['UN_INDICADOR', 'DOS_INDICADORS']");
            return ['UN_INDICADOR', 'DOS_INDICADORS'];
        }
        if (isBarTypeVisible || isLineTypeVisible) {
            console.log("['DOS_INDICADORS']");
            return ['DOS_INDICADORS'];
        }
        if (isSparkLineTypeVisible) {
            console.log("['DOS_INDICADORS', 'UN_INDICADOR_AMB_DESCOMPOSICIO', 'VARIS_INDICADORS']");
            return ['DOS_INDICADORS', 'UN_INDICADOR_AMB_DESCOMPOSICIO', 'VARIS_INDICADORS'];
        }
        if (isGaugeTypeVisible) {
            console.log("['UN_INDICADOR', 'VARIS_INDICADORS']");
            return ['UN_INDICADOR', 'VARIS_INDICADORS'];
        }
        return [];
    }, [isPieTypeVisible, isScatterTypeVisible, isHeatTypeVisible, isBarTypeVisible, isLineTypeVisible, isSparkLineTypeVisible, isGaugeTypeVisible]);

    const [appPalette, setAppPalette] = useState(data.colorsPaleta);
    const handlePaletteChange = (newPalette: string[]) => {
        const paletteString = newPalette.join(',');
        console.log('La paleta ha canviat:', newPalette);
        setAppPalette(paletteString);
        // Aquí pots fer qualsevol cosa amb la nova paleta,
        // com desar-la en una base de dades, en l'estat global, etc.
        // colorsPaleta
        dataDispatchAction({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: { fieldName: 'colorsPaleta', value: paletteString, }
        })
        // dataDispatchAction({
        //     type: FormFieldDataActionType.FIELD_CHANGE,
        //     payload: { fieldName: 'atributsVisuals', value: {
        //             ...data['atributsVisuals'],
        //             colorsPaleta: paletteString,
        //         } }
        // })
    };

    const generateOnChange = (name: string, fieldName: string)  => ((value: any) => {
        console.log(name, value);
        // dataDispatchAction({
        //     type: FormFieldDataActionType.FIELD_CHANGE,
        //     payload: { fieldName: fieldName, value: {
        //             ...data[fieldName],
        //             [name]: value,
        //         } }
        // })
    })

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                    <Grid size={4}><FormField name="tipusGrafic" /></Grid>
                    { isChartTypeSelected && (
                        <>
                            <Grid size={4}><FormField name="tipusDades" hiddenEnumValues={tipusDadesOcultar}/></Grid>
                            <Grid size={4}><FormField name="tempsAgrupacio" /></Grid>
                            { (isUnIndicador || isUnIndicadorAmbDescomposicio || isDosIndicadors) && (
                                <>
                                    <Grid size={4}><FormField name="indicador" advancedSearchColumns={columnesIndicador}/></Grid>
                                    <Grid size={6}><FormField name="titolIndicador" /></Grid>
                                    <Grid size={2}><FormField name="agregacio" hiddenEnumValues={['FIRST_SEEN', 'LAST_SEEN']}/></Grid>
                                </>
                            )}
                            { isUnIndicadorAmbDescomposicio && (<Grid size={12}><FormField name="descomposicioDimensio" /></Grid>) }
                            { isVarisIndicadors && (
                                <Grid size={12}>
                                    <ColumnesTable name="columnes"
                                                   label="Columnes de la taula"
                                                   value={data.indicadorsInfo}
                                                   mostrarUnitat={false}
                                                   hiddenAgregacioValues={['FIRST_SEEN', 'LAST_SEEN']}
                                                   onChange={(value) => {
                                        dataDispatchAction({
                                            type: FormFieldDataActionType.FIELD_CHANGE,
                                            payload: { fieldName: "indicadorsInfo", value }
                                        });
                                    }} />
                                </Grid>
                            )}
                            {/*<Grid size={4}><FormField name="tipusValors" /></Grid>*/}

                            <Grid size={6}><FormField name="llegendaX" /></Grid>
                            <Grid size={6}><FormField name="llegendaY" /></Grid>
                        </>
                    )}
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
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>Configuració general</Typography></Grid>
                {/*<Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorsPaleta" label="Colors de la paleta" type="color" /></Grid>*/}
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><ColorPaletteSelector initialColors={appPalette} onPaletteChange={handlePaletteChange} /></Grid>
                <Grid size={12}><FormField name="mostrarReticula" label="Mostrar retícula" type="checkbox" onChange={generateOnChange("mostrarReticula", "atributsVisuals")} /></Grid>

                {isBarTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de barres</Typography></Grid>
                        <Grid size={6}><FormField name="barStacked" label="Barres apilades" type="checkbox" onChange={generateOnChange("barStacked", "atributsVisuals")} /></Grid>
                        <Grid size={6}><FormField name="barHorizontal" label="Barres horitzontals" type="checkbox" onChange={generateOnChange("barHorizontal", "atributsVisuals")} /></Grid>
                    </>
                )}

                {(isLineTypeVisible || isSparkLineTypeVisible) && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de línies</Typography></Grid>
                        <Grid size={6}><FormField name="lineShowPoints" label="Mostrar punts" type="checkbox" onChange={generateOnChange("lineShowPoints", "atributsVisuals")} /></Grid>
                        <Grid size={6}><FormField name="lineSmooth" label="Línies suaus" type="checkbox" onChange={generateOnChange("lineSmooth", "atributsVisuals")} /></Grid>
                        <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="lineWidth" label="Amplada de línia" type="number" required={false} onChange={generateOnChange("lineWidth", "atributsVisuals")} /></Grid>
                    </>
                )}

                {isPieTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de pastís</Typography></Grid>
                        <Grid size={6}><FormField name="pieDonut" label="Tipus donut" type="checkbox" onChange={generateOnChange("pieDonut", "atributsVisuals")} /></Grid>
                        <Grid size={6}><FormField name="pieShowLabels" label="Mostrar etiquetes" type="checkbox" onChange={generateOnChange("pieShowLabels", "atributsVisuals")} /></Grid>
                    </>
                )}

                {isGaugeTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de gauge</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeMin" label="Valor mínim" type="number" required={false} onChange={generateOnChange("gaugeMin", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeMax" label="Valor màxim" type="number" required={false} onChange={generateOnChange("gaugeMax", "atributsVisuals")} /></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.gaugeColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeRangs" label="Rangs (separats per comes)" onChange={generateOnChange("gaugeRangs", "atributsVisuals")} /></Grid>
                    </>
                )}

                {isHeatTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de heatmap</Typography></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.heatmapColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="heatmapMinValue" label="Valor mínim" type="number" required={false} onChange={generateOnChange("heatmapMinValue", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="heatmapMaxValue" label="Valor màxim" type="number" required={false} onChange={generateOnChange("heatmapMaxValue", "atributsVisuals")} /></Grid>
                    </>
                )}
            </Grid>
        );
    }
}

export default EstadisticaGraficWidgetForm;
