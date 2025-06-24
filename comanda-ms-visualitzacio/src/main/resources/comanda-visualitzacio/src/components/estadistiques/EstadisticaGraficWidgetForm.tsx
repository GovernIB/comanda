import Grid from "@mui/material/Grid";
import {Divider, Box, Typography} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import {useState, useMemo, useEffect, useRef} from "react";
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
        titol: data.titol || 'Títol del gràfic',
        descripcio: data.descripcio,
        colorText: data.colorText,
        colorFons: data.colorFons,
        mostrarVora: data.mostrarVora,
        colorVora: data.colorVora,
        ampleVora: data.ampleVora,
        tipusGrafic: data.tipusGrafic || 'BAR_CHART',
        llegendaX: data.llegendaX,
        // llegendaY: data.llegendaY || 'Eix Y',
        colorsPaleta: data.colorsPaleta || '#1f77b4,#ff7f0e,#2ca02c,#d62728,#9467bd,#8c564b',
        mostrarReticula: data.mostrarReticula !== undefined ? data.mostrarReticula : false,
        barStacked: data.barStacked || false,
        barHorizontal: data.barHorizontal || false,
        lineShowPoints: data.lineShowPoints !== undefined ? data.lineShowPoints : false,
        lineSmooth: data.lineSmooth || false,
        lineWidth: data.lineWidth || 2,
        area: data.area || false,
        pieDonut: data.pieDonut || false,
        pieShowLabels: data.pieShowLabels !== undefined ? data.pieShowLabels : false,
        outerRadius: data.outerRadius,
        innerRadius: data.innerRadius,
        labelSize: data.labelSize,
        gaugeMin: data.gaugeMin,
        gaugeMax: data.gaugeMax,
        gaugeColors: data.gaugeColors,
        gaugeRangs: data.gaugeRangs,
        heatmapColors: data.heatmapColors,
        heatmapMinValue: data.heatmapMinValue,
        heatmapMaxValue: data.heatmapMaxValue,
        midaFontTitol: data.midaFontTitol,
        midaFontDescripcio: data.midaFontDescripcio,
    }), [data])

    const isMostrarVora: boolean = data?.mostrarVora;
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
        dataDispatchAction({
            type: FormFieldDataActionType.FIELD_CHANGE,
            payload: { fieldName: 'colorsPaleta', value: paletteString, }
        })
    };

    const handleAgruparPerDimensioChange = (value: boolean) => {
        if (value) {
            dataDispatchAction({
                type: FormFieldDataActionType.FIELD_CHANGE,
                payload: {fieldName: 'tempsAgrupacio', value: undefined,}
            })
        }
    }
    const handleTempsAgrupacioChange = (value: string) => {
        if (value) {
            dataDispatchAction({
                type: FormFieldDataActionType.FIELD_CHANGE,
                payload: {fieldName: 'agruparPerDimensioDescomposicio', value: undefined,}
            })
        }
    }

    const initializedRef = useRef(false);
    useEffect(() => {
        if (!initializedRef.current) {
            if (data?.mostrarCapcalera === undefined) {
                dataDispatchAction({
                    type: FormFieldDataActionType.FIELD_CHANGE,
                    payload: { fieldName: "lineWidth", value: 2 }
                });
            }
            initializedRef.current = true;
        }
    }, [data, dataDispatchAction]);

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                    <Grid size={12}><Divider sx={{ my: 1 }} >{t('page.widget.form.grafic')}</Divider></Grid>
                    <Grid size={4}><FormField name="tipusGrafic" /></Grid>
                    { isChartTypeSelected && (
                        <>
                            <Grid size={4}><FormField name="tipusDades" hiddenEnumValues={tipusDadesOcultar} required/></Grid>
                            <Grid size={4}><FormField name="tempsAgrupacio" onChange={handleTempsAgrupacioChange} disabled={data.agruparPerDimensioDescomposicio === true}/></Grid>
                            { (isUnIndicador || isUnIndicadorAmbDescomposicio || isDosIndicadors) && (
                                <>
                                    <Grid size={4}><FormField name="indicador" advancedSearchColumns={columnesIndicador}/></Grid>
                                    <Grid size={6}><FormField name="titolIndicador" /></Grid>
                                    <Grid size={2}><FormField name="agregacio" hiddenEnumValues={['FIRST_SEEN', 'LAST_SEEN']}/></Grid>
                                </>
                            )}
                            { isUnIndicadorAmbDescomposicio && (
                                <>
                                    <Grid size={6}><FormField name="descomposicioDimensio" /></Grid>
                                    <Grid size={6}><FormField name="agruparPerDimensioDescomposicio" label={"Utilitzar la dimensió de descompasició per agrupar, enlloc d'agrupar per temps?"} type={"checkbox"} onChange={handleAgruparPerDimensioChange} /></Grid>
                                </>
                            )}
                            { isVarisIndicadors && (
                                <Grid size={12}>
                                    <ColumnesTable name="columnes"
                                                   label="Indicadors"
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

                            { (isBarTypeVisible || isLineTypeVisible || isSparkLineTypeVisible || isScatterTypeVisible) && (
                                <Grid size={12}><FormField name="llegendaX" /></Grid>
                            )}
                            {/*<Grid size={6}><FormField name="llegendaY" /></Grid>*/}
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
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>Configuració general</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorText" label="Color de text" type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFons" label="Color de fons" type="color" required={false} /></Grid>
                <Grid size={12}><FormField name="mostrarVora" label="Mostrar vora" type="checkbox" /></Grid>
                { isMostrarVora && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorVora" label="Color de la vora" type="color" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleVora" label="Ample de la vora" type="number" required={false} /></Grid>
                    </>
                )}
                {/*<Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorsPaleta" label="Colors de la paleta" type="color" /></Grid>*/}
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><ColorPaletteSelector initialColors={appPalette} onPaletteChange={handlePaletteChange} /></Grid>
                { (isBarTypeVisible || isLineTypeVisible || isSparkLineTypeVisible || isScatterTypeVisible) && (
                    <Grid size={12}><FormField name="mostrarReticula" label="Mostrar retícula" type="checkbox" /></Grid>)
                }

                {isBarTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de barres</Typography></Grid>
                        <Grid size={6}><FormField name="barStacked" label="Barres apilades" type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="barHorizontal" label="Barres horitzontals" type="checkbox" /></Grid>
                    </>
                )}

                {(isLineTypeVisible || isSparkLineTypeVisible) && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de línies</Typography></Grid>
                        <Grid size={6}><FormField name="lineShowPoints" label="Mostrar punts" type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="area" label="Emplenar area" type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="lineSmooth" label="Línies suaus" type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="lineWidth" label="Amplada de línia" type="number" required={false} /></Grid>
                    </>
                )}

                {isPieTypeVisible && (
                    <>
                        <Grid size={6}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de pastís</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="outerRadius" label="Radi exterior" type="number" required={false} /></Grid>
                        <Grid size={6}><FormField name="pieDonut" label="Tipus donut" type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="innerRadius" label="Radi interior" type="number" required={false} /></Grid>
                        <Grid size={6}><FormField name="pieShowLabels" label="Mostrar etiquetes" type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="labelSize" label="Mida etiquetes" type="number" required={false} /></Grid>
                    </>
                )}

                {isGaugeTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de gauge</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeMin" label="Valor mínim" type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeMax" label="Valor màxim" type="number" required={false} /></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.gaugeColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeRangs" label="Rangs (separats per comes)" /></Grid>
                    </>
                )}

                {isHeatTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>Gràfic de heatmap</Typography></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.heatmapColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="heatmapMinValue" label="Valor mínim" type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="heatmapMaxValue" label="Valor màxim" type="number" required={false} /></Grid>
                    </>
                )}

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Configuració de la mida de font</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="midaFontTitol" label={"Mida de la font del títol"} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="midaFontDescripcio" label={"Mida de la font de la descripció"} type="number" required={false} /></Grid>
            </Grid>
        );
    }
}

export default EstadisticaGraficWidgetForm;
