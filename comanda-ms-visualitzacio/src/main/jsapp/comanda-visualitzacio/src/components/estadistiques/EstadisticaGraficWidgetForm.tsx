import Grid from "@mui/material/Grid";
import {Divider, Box, Typography} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useMemo, useEffect, useRef} from "react";
import EstadisticaWidgetFormFields, { FieldHelp } from "./EstadisticaWidgetFormFields";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";
import ColorPaletteSelector from "../ColorPaletteSelector";
import ColumnesTable from "./ColumnesTable.tsx";
import FormFieldCustomAdvancedSearch from '../FormFieldCustomAdvancedSearch';
import { WidgetPreview } from "./WidgetPreview.tsx";

/** Camps que sobreescriuen l'estil de la plantilla (excloent-ne `lineWidth`, que s'inicialitza sempre a 2 en muntar-se) */
export const GRAFIC_OVERRIDE_FIELDS = [
    'colorText', 'colorFons', 'mostrarVora', 'colorVora', 'ampleVora',
    'colorsPaleta', 'mostrarReticula', 'llegendaX',
    'barStacked', 'barHorizontal', 'lineShowPoints', 'area', 'lineSmooth',
    'outerRadius', 'pieDonut', 'innerRadius', 'pieShowLabels', 'labelSize',
    'gaugeMin', 'gaugeMax', 'gaugeRangs', 'heatmapMinValue', 'heatmapMaxValue',
    'midaFontTitol', 'midaFontDescripcio',
];

/** Indica si el widget té algun valor que sobreescrigui la plantilla (per mostrar l'indicador de "personalitzat") */
export const hasVisualOverrides = (data: any): boolean =>
    GRAFIC_OVERRIDE_FIELDS.some(field => data?.[field] !== undefined && data?.[field] !== null && data?.[field] !== '');

type EstadisticaGraficWidgetFormProps = {
    mode?: 'full' | 'stats' | 'indicators' | 'visual';
    dashboardPlantilla?: any;
    destacat?: boolean;
    /** Indica si s'han de mostrar els camps que sobreescriuen la plantilla (per defecte, sí). La previsualització es mostra sempre. */
    showOverrideFields?: boolean;
};

const EstadisticaGraficWidgetForm: React.FC<EstadisticaGraficWidgetFormProps> = ({ mode = 'full', dashboardPlantilla, destacat, showOverrideFields = true }) => {
    const { data, apiRef } = useFormContext();
    const { t } = useTranslation();
    const previewData = useMemo(() =>({
        entornCodi: 'ENT',
        titol: data.titol || t($ => $.page.plantilla.sample.chartTitle),
        descripcio: data.descripcio,
        destacat: data.destacat || destacat,
        colorText: data.colorText,
        colorFons: data.colorFons,
        mostrarVora: data.mostrarVora,
        colorVora: data.colorVora,
        ampleVora: data.ampleVora,
        tipusGrafic: data.tipusGrafic || 'BAR_CHART',
        llegendaX: data.llegendaX,
        // llegendaY: data.llegendaY || 'Eix Y',
        // Sense fallback fix: un valor per defecte aquí faria que sempre "sobreescrigués" els colors
        // de la plantilla a resolveWidgetStyles (només s'aplica la plantilla si el camp és buit).
        colorsPaleta: data.colorsPaleta,
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
        tipusValors: data.tipusValors,
        heatmapColors: data.heatmapColors,
        heatmapMinValue: data.heatmapMinValue,
        heatmapMaxValue: data.heatmapMaxValue,
        midaFontTitol: data.midaFontTitol,
        midaFontDescripcio: data.midaFontDescripcio,
    }), [data, destacat])

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
            return ['UN_INDICADOR_AMB_DESCOMPOSICIO', 'VARIS_INDICADORS'];
        }
        return [];
    }, [isPieTypeVisible, isScatterTypeVisible, isHeatTypeVisible, isBarTypeVisible, isLineTypeVisible, isSparkLineTypeVisible, isGaugeTypeVisible]);

    const handlePaletteChange = (newPalette: string[]) => {
        const paletteString = newPalette.join(',');
        apiRef.current?.setFieldValue('colorsPaleta', paletteString);
    };

    const handleAgruparPerDimensioChange = (value: boolean) => {
        if (value) {
            apiRef.current?.setFieldValue('tempsAgrupacio', undefined);
        }
    }
    const handleTempsAgrupacioChange = (value: string) => {
        if (value) {
            apiRef.current?.setFieldValue('agruparPerDimensioDescomposicio', undefined);
        }
    }

    const initializedRef = useRef(false);
    useEffect(() => {
        if (!initializedRef.current) {
            if (data?.mostrarCapcalera === undefined) {
                apiRef.current?.setFieldValue('lineWidth', 2);
            }
            initializedRef.current = true;
        }
    }, [data]);
    const indicadorDimensioNamedQueries = React.useMemo(() => [`filterByAppGroupByNom:${data?.aplicacio?.id}`], [data?.aplicacio?.id]);

    if (mode === 'stats') {
        return renderStatsFields();
    }

    if (mode === 'indicators') {
        return <Grid container spacing={2}>{renderIndicatorFields()}</Grid>;
    }

    if (mode === 'visual') {
        return renderVisualContent();
    }

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                {renderStatsFields()}
            </Grid>
            <Grid id={'cv'} size={{xs: 12, sm: 4}}>
                <VisualAttributesPanel widgetType="grafic" title={t($ => $.page.widget.form.configVisual)}>
                    {renderVisualContent()}
                </VisualAttributesPanel>
            </Grid>
        </Grid>
    );

    function renderStatsFields() {
        return (
            <EstadisticaWidgetFormFields>
                {renderIndicatorFields()}
            </EstadisticaWidgetFormFields>
        );
    }

    function renderIndicatorFields() {
        return (
            <>
                <Grid size={12}><Divider sx={{ my: 1 }} >{t($ => $.page.widget.form.grafic)}</Divider></Grid>
                {/* 1a fila: tipus de gràfic i tipus de dades */}
                <Grid size={6}><FormField name="tipusGrafic" required/></Grid>
                { isChartTypeSelected && (
                    <>
                        <Grid size={6}>
                            <FormField name="tipusDades" hiddenEnumValues={tipusDadesOcultar} required/>
                            <FieldHelp text={t($ => $.page.widget.form.help.tipusDades)} />
                        </Grid>
                        {/* 2a fila: indicador, títol, tipus i unitat d'agregació */}
                        { (isUnIndicador || isUnIndicadorAmbDescomposicio || isDosIndicadors) && (
                            <>
                                <Grid size={4}>
                                    <FormFieldCustomAdvancedSearch
                                        name="indicador"
                                        namedQueries={indicadorDimensioNamedQueries}
                                        advancedSearchColumns={columnesIndicador}
                                        advancedSearchDataGridProps={{ rowHeight: 30, }}
                                        advancedSearchDialogHeight={500}
                                        required
                                    />
                                </Grid>
                                <Grid size={4}><FormField name="titolIndicador" required={false} /></Grid>
                                <Grid size={2}>
                                    <FormField name="agregacio" hiddenEnumValues={['FIRST_SEEN', 'LAST_SEEN']} required/>
                                    <FieldHelp text={t($ => $.page.widget.form.help.agregacio)} />
                                </Grid>
                                <Grid size={2}>
                                    <FormField name="unitatAgregacio" required={data.agregacio === 'AVERAGE'} disabled={data.agregacio !== 'AVERAGE'}/>
                                    <FieldHelp text={t($ => $.page.widget.form.help.unitatAgregacio)} />
                                </Grid>
                            </>
                        )}
                        { isDosIndicadors && (
                            <>
                                <Grid size={4}>
                                    <FormFieldCustomAdvancedSearch
                                        name="indicadorMax"
                                        namedQueries={indicadorDimensioNamedQueries}
                                        advancedSearchColumns={columnesIndicador}
                                        advancedSearchDataGridProps={{ rowHeight: 30, }}
                                        advancedSearchDialogHeight={500}
                                        required
                                    />
                                </Grid>
                                <Grid size={4}><FormField name="titolIndicadorMax" required={false} /></Grid>
                                <Grid size={2}>
                                    <FormField name="agregacioMax" hiddenEnumValues={['FIRST_SEEN', 'LAST_SEEN']} required/>
                                </Grid>
                                <Grid size={2}>
                                    <FormField name="unitatAgregacioMax" required={data.agregacioMax === 'AVERAGE'} disabled={data.agregacioMax !== 'AVERAGE'}/>
                                </Grid>
                                <Grid size={4}>
                                    <FormField name="tipusValors" required/>
                                </Grid>
                            </>
                        )}
                        {/* 3a fila: agrupació temporal i, si escau, dimensió de descomposició */}
                        <Grid size={isUnIndicadorAmbDescomposicio ? 4 : 12}>
                            <FormField
                                name="tempsAgrupacio"
                                onChange={handleTempsAgrupacioChange}
                                disabled={data.agruparPerDimensioDescomposicio === true}
                                required={!(isUnIndicadorAmbDescomposicio && data.agruparPerDimensioDescomposicio === true)}
                            />
                            <FieldHelp text={t($ => $.page.widget.form.help.tempsAgrupacio)} />
                        </Grid>
                        { isUnIndicadorAmbDescomposicio && (
                            <>
                                <Grid size={4}>
                                    <FormField name="agruparPerDimensioDescomposicio" type={"checkbox"} onChange={handleAgruparPerDimensioChange} />
                                    <FieldHelp text={t($ => $.page.widget.form.help.agruparPerDimensioDescomposicio)} />
                                </Grid>
                                <Grid size={4}>
                                    <FormField name="descomposicioDimensio" namedQueries={indicadorDimensioNamedQueries} required/>
                                    <FieldHelp text={t($ => $.page.widget.form.help.descomposicioDimensio)} />
                                </Grid>
                            </>
                        )}
                        { isVarisIndicadors && (
                            <Grid size={12}>
                                <ColumnesTable name="indicadorsInfo"
                                               label={t($ => $.page.widget.grafic.indicadors)}
                                               value={data.indicadorsInfo}
                                               mostrarUnitat={true}
                                               hiddenAgregacioValues={['FIRST_SEEN', 'LAST_SEEN']}
                                               onChange={(value) => {
                                                   apiRef.current?.setFieldValue('indicadorsInfo', value);
                                               }}
                                />
                            </Grid>
                        )}
                    </>
                )}
            </>
        );
    }

    function renderVisualContent() {
        return (
            <Box sx={{ p: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 2 }}>{t($ => $.page.widget.form.preview)}</Typography>
                <Box sx={{ height: '240px' }}>
                    <WidgetPreview
                            widgetType="GRAFIC"
                            widgetData={previewData}
                            dashboardPlantilla={dashboardPlantilla}
                        />
                </Box>
                {showOverrideFields && renderGraficFormFields()}
            </Box>
        );
    }

    // Render form fields for grafic widget
    function renderGraficFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>{t($ => $.page.widget.form.configGeneral)}</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="colorText" label={t($ => $.page.widget.atributsVisuals.colorText)} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="colorFons" label={t($ => $.page.widget.atributsVisuals.colorFons)} type="color" required={false} /></Grid>
                <Grid size={12}><FormField name="mostrarVora" label={t($ => $.page.widget.atributsVisuals.mostrarVora)} type="checkbox" /></Grid>
                { isMostrarVora && (
                    <>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="colorVora" label={t($ => $.page.widget.atributsVisuals.colorVora)} type="color" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="ampleVora" label={t($ => $.page.widget.atributsVisuals.ampleVora)} type="number" required={false} /></Grid>
                    </>
                )}
                {/*<Grid size={12} sx={{backgroundColor: 'background.paper'}}><FormField name="atributsVisuals.colorsPaleta" label="Colors de la paleta" type="color" /></Grid>*/}
                <Grid size={12} sx={{backgroundColor: 'background.paper'}}><ColorPaletteSelector initialColors={data?.colorsPaleta} onPaletteChange={handlePaletteChange} /></Grid>
                { (isBarTypeVisible || isLineTypeVisible || isScatterTypeVisible) && (
                    <>
                        <Grid size={12}><FormField name="mostrarReticula" label={t($ => $.page.widget.atributsVisuals.mostrarReticula)} type="checkbox" /></Grid>
                        <Grid size={12}><FormField name="llegendaX" /></Grid>
                    </>
                )}
                {isBarTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.graficBar)}</Typography></Grid>
                        <Grid size={6}><FormField name="barStacked" label={t($ => $.page.widget.atributsVisuals.barStacked)} type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="barHorizontal" label={t($ => $.page.widget.atributsVisuals.barHorizontal)} type="checkbox" /></Grid>
                    </>
                )}
                {(isLineTypeVisible || isSparkLineTypeVisible) && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.graficLin)}</Typography></Grid>
                        <Grid size={6}><FormField name="lineShowPoints" label={t($ => $.page.widget.atributsVisuals.lineShowPoints)} type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="area" label={t($ => $.page.widget.atributsVisuals.area)} type="checkbox" /></Grid>
                        <Grid size={6}><FormField name="lineSmooth" label={t($ => $.page.widget.atributsVisuals.lineSmooth)} type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="lineWidth" label={t($ => $.page.widget.atributsVisuals.lineWidth)} type="number" required={false} /></Grid>
                    </>
                )}
                {isPieTypeVisible && (
                    <>
                        <Grid size={6}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.graficPst)}</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="outerRadius" label={t($ => $.page.widget.atributsVisuals.outerRadius)} type="number" required={false} /></Grid>
                        <Grid size={6}><FormField name="pieDonut" label={t($ => $.page.widget.atributsVisuals.pieDonut)} type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="innerRadius" label={t($ => $.page.widget.atributsVisuals.innerRadius)} type="number" required={false} /></Grid>
                        <Grid size={6}><FormField name="pieShowLabels" label={t($ => $.page.widget.atributsVisuals.pieShowLabels)} type="checkbox" /></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="labelSize" label={t($ => $.page.widget.atributsVisuals.labelSize)} type="number" required={false} /></Grid>
                    </>
                )}
                {isGaugeTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.graficGug)}</Typography></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="gaugeMin" label={t($ => $.page.widget.atributsVisuals.gaugeMin)} type="number" required={false} /></Grid>
                        { !isDosIndicadors && (
                            <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="gaugeMax" label={t($ => $.page.widget.atributsVisuals.gaugeMax)} type="number" required={false} /></Grid>
                        )}
                        {/*<Grid size={12}><FormField name="atributsVisuals.gaugeColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={12} sx={{backgroundColor: 'background.paper'}}><FormField name="gaugeRangs" label={t($ => $.page.widget.atributsVisuals.gaugeRangs)} /></Grid>
                    </>
                )}
                {isHeatTypeVisible && (
                    <>
                        <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.graficMap)}</Typography></Grid>
                        {/*<Grid size={12}><FormField name="atributsVisuals.heatmapColors" label="Colors (separats per comes)" /></Grid>*/}
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="heatmapMinValue" label={t($ => $.page.widget.atributsVisuals.heatmapMinValue)} type="number" required={false} /></Grid>
                        <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="heatmapMaxValue" label={t($ => $.page.widget.atributsVisuals.heatmapMaxValue)} type="number" required={false} /></Grid>
                    </>
                )}
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t($ => $.page.widget.form.configFont)}</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="midaFontTitol" label={t($ => $.page.widget.atributsVisuals.midaFontTitol)} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: 'background.paper'}}><FormField name="midaFontDescripcio" label={t($ => $.page.widget.atributsVisuals.midaFontDescripcio)} type="number" required={false} /></Grid>
            </Grid>
        );
    }
}

export default EstadisticaGraficWidgetForm;
