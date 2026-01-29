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
import FormFieldAdvancedSearchFilters from "../FormFieldAdvancedSearchFilters.tsx";

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
            return ['UN_INDICADOR', 'DOS_INDICADORS'];
        }
        if (isBarTypeVisible || isLineTypeVisible) {
            return ['DOS_INDICADORS'];
        }
        if (isSparkLineTypeVisible) {
            return ['DOS_INDICADORS', 'UN_INDICADOR_AMB_DESCOMPOSICIO', 'VARIS_INDICADORS'];
        }
        if (isGaugeTypeVisible) {
            return ['UN_INDICADOR', 'VARIS_INDICADORS'];
        }
        return [];
    }, [isPieTypeVisible, isScatterTypeVisible, isHeatTypeVisible, isBarTypeVisible, isLineTypeVisible, isSparkLineTypeVisible, isGaugeTypeVisible]);

    const [appPalette, setAppPalette] = useState(data.colorsPaleta);
    const handlePaletteChange = (newPalette: string[]) => {
        const paletteString = newPalette.join(',');
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
    const indicadorNamedQueries = React.useMemo(() => [`filterByAppGroupByNom:${data?.aplicacio?.id}`], [data?.aplicacio?.id]);

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
                                    <Grid size={4}><FormFieldAdvancedSearchFilters
                                        name="indicador"
                                        namedQueries={indicadorNamedQueries}
                                        advancedSearchColumns={columnesIndicador}
                                        advancedSearchDataGridProps={{ rowHeight: 30, }}
                                        advancedSearchDialogHeight={500}/></Grid>
                                    <Grid size={4}><FormField name="titolIndicador" /></Grid>
                                    <Grid size={2}><FormField name="agregacio" hiddenEnumValues={['FIRST_SEEN', 'LAST_SEEN']}/></Grid>
                                    <Grid size={2}><FormField name="unitatAgregacio" disabled={data.agregacio !== 'AVERAGE'}/></Grid>
                                </>
                            )}
                            { isUnIndicadorAmbDescomposicio && (
                                <>
                                    <Grid size={6}><FormField name="descomposicioDimensio" namedQueries={["groupByNom", `filterByApp:${data?.aplicacio?.id}`]} /></Grid>
                                    <Grid size={6}><FormField name="agruparPerDimensioDescomposicio" type={"checkbox"} onChange={handleAgruparPerDimensioChange} /></Grid>
                                </>
                            )}
                            { isVarisIndicadors && (
                                <Grid size={12}>
                                    <ColumnesTable name="indicadorsInfo"
                                                   label={t('page.widget.grafic.indicadors')}
                                                   value={data.indicadorsInfo}
                                                   mostrarUnitat={true}
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

                            { (isBarTypeVisible || isLineTypeVisible || isScatterTypeVisible) && (
                                <Grid size={12}><FormField name="llegendaX" /></Grid>
                            )}
                            {/*<Grid size={6}><FormField name="llegendaY" /></Grid>*/}
                        </>
                    )}
                </EstadisticaWidgetFormFields>
            </Grid>

            <Grid id={'cv'} size={{xs: 12, sm: 4}}>
                <VisualAttributesPanel widgetType="grafic" title={t('page.widget.form.configVisual')}>
                    {/* Preview inside the panel */}
                    <Box sx={{ p: 2 }}>
                        <Typography variant="subtitle2" sx={{ mb: 2 }}>{t('page.widget.form.preview')}</Typography>
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
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>{t('page.widget.form.configGeneral')}</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorText" label={t('page.widget.atributsVisuals.colorText')} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFons" label={t('page.widget.atributsVisuals.colorFons')} type="color" required={false} /></Grid>
                <Grid size={12}><FormField name="mostrarVora" label={t('page.widget.atributsVisuals.mostrarVora')} type="checkbox" /></Grid>
                { isMostrarVora && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorVora" label={t('page.widget.atributsVisuals.colorVora')} type="color" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleVora" label={t('page.widget.atributsVisuals.ampleVora')} type="number" required={false} /></Grid>
                    </>
                )}
                {/*<Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorsPaleta" label="Colors de la paleta" type="color" /></Grid>*/}
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><ColorPaletteSelector initialColors={appPalette} onPaletteChange={handlePaletteChange} /></Grid>
                { (isBarTypeVisible || isLineTypeVisible || isScatterTypeVisible) && (
                    <Grid size={12}><FormField name="mostrarReticula" label={t('page.widget.atributsVisuals.mostrarReticula')} type="checkbox" /></Grid>)
                }

                {isBarTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t('page.widget.form.graficBar')}</Typography></Grid>
                        <Grid size={6}><FormField name="barStacked" label={t('page.widget.atributsVisuals.barStacked')} type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="barHorizontal" label={t('page.widget.atributsVisuals.barHorizontal')} type="checkbox" /></Grid>
                    </>
                )}

                {(isLineTypeVisible || isSparkLineTypeVisible) && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t('page.widget.form.graficLin')}</Typography></Grid>
                        <Grid size={6}><FormField name="lineShowPoints" label={t('page.widget.atributsVisuals.lineShowPoints')} type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="area" label={t('page.widget.atributsVisuals.area')} type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="lineSmooth" label={t('page.widget.atributsVisuals.lineSmooth')} type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="lineWidth" label={t('page.widget.atributsVisuals.lineWidth')} type="number" required={false} /></Grid>
                    </>
                )}

                {isPieTypeVisible && (
                    <>
                        <Grid size={6}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t('page.widget.form.graficPst')}</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="outerRadius" label={t('page.widget.atributsVisuals.outerRadius')} type="number" required={false} /></Grid>
                        <Grid size={6}><FormField name="pieDonut" label={t('page.widget.atributsVisuals.pieDonut')} type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="innerRadius" label={t('page.widget.atributsVisuals.innerRadius')} type="number" required={false} /></Grid>
                        <Grid size={6}><FormField name="pieShowLabels" label={t('page.widget.atributsVisuals.pieShowLabels')} type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="labelSize" label={t('page.widget.atributsVisuals.labelSize')} type="number" required={false} /></Grid>
                    </>
                )}

                {isGaugeTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t('page.widget.form.graficGug')}</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeMin" label={t('page.widget.atributsVisuals.gaugeMin')} type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeMax" label={t('page.widget.atributsVisuals.gaugeMax')} type="number" required={false} /></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.gaugeColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="gaugeRangs" label={t('page.widget.atributsVisuals.gaugeRangs')} /></Grid>
                    </>
                )}

                {isHeatTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t('page.widget.form.graficMap')}</Typography></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.heatmapColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="heatmapMinValue" label={t('page.widget.atributsVisuals.heatmapMinValue')} type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="heatmapMaxValue" label={t('page.widget.atributsVisuals.heatmapMaxValue')} type="number" required={false} /></Grid>
                    </>
                )}

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.configFont')}</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="midaFontTitol" label={t('page.widget.atributsVisuals.midaFontTitol')} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="midaFontDescripcio" label={t('page.widget.atributsVisuals.midaFontDescripcio')} type="number" required={false} /></Grid>
            </Grid>
        );
    }
}

export default EstadisticaGraficWidgetForm;
