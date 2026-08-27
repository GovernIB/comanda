import React from 'react';
import {useTranslation} from 'react-i18next';
import {Box, Skeleton, Typography} from '@mui/material';
import {useTheme} from '@mui/material/styles';
import {
    BarChart,
    ChartsLegend,
    ChartsTooltip,
    Gauge,
    gaugeClasses,
    LineChart,
    LineSeries,
    PieArcLabel,
    PieChart,
    PieSeries,
    SparkLineChart,
    XAxis,
    YAxis,
} from '@mui/x-charts';
import estils from './WidgetEstils';
import {useWidgetTheme} from './useWidgetTheme';
import {WidgetContainer, WidgetErrorDisplay, WidgetFooter, WidgetHeader, WidgetNoAccessDisplay} from './WidgetLayout';

interface ColumnLabel {
    id: string;
    label: string;
}

// Define the props for the GraficWidgetVisualization component
export interface GraficWidgetVisualizationProps {
    // Widget data
    entornCodi?: string;
    titol?: string;
    descripcio?: string;

    tipusGrafic?: 'BAR_CHART' | 'LINE_CHART' | 'PIE_CHART' | 'GAUGE_CHART' | 'HEATMAP_CHART' | 'SCATTER_CHART' | 'SPARK_LINE_CHART';
    labels?: ColumnLabel[];
    dades?: Record<string, unknown>[];
    columnaAgregacio?: string;
    llegendaX?: string;
    destacat?: boolean;

    // Atributs visuals
    colorText?: string;
    colorFons?: string;
    mostrarVora?: boolean;
    colorVora?: string;
    ampleVora?: number;

    // Visual attributes
    colorsPaleta?: string;
    mostrarReticula?: boolean;

    // Bar chart
    barStacked?: boolean;
    barHorizontal?: boolean;

    // Line chart
    lineShowPoints?: boolean;
    lineSmooth?: boolean;
    lineWidth?: number;
    area?: boolean;

    // Pie chart
    pieDonut?: boolean;
    pieShowLabels?: boolean;
    outerRadius?: number;
    innerRadius?: number;
    labelSize?: number;

    // Gauge chart
    gaugeMin?: number;
    gaugeMax?: number;
    gaugeColors?: string;
    gaugeRangs?: string;

    // Heatmap chart
    heatmapColors?: string;
    heatmapMinValue?: number;
    heatmapMaxValue?: number;

    // Additional props
    loading?: boolean;
    preview?: boolean;
    error?: boolean;
    errorMsg?: string;
    errorTrace?: string;
    senseAccesDades?: boolean;
    onClick?: () => void;

    midaFontTitol?: number;
    midaFontDescripcio?: number;

    // Dashboard context
    dashboardEntornCodi?: string;
}

const GraficWidgetVisualization: React.FC<GraficWidgetVisualizationProps> = (props) => {
    const {
        // Widget data
        titol,
        descripcio,
        entornCodi,
        // colorText,
        // colorFons,
        mostrarVora = false,
        // colorVora,
        ampleVora = 1,
        dades = generateSampleData(props.tipusGrafic),
        labels = generateSampleLabels(props.tipusGrafic),
        columnaAgregacio = generateSampleAgregacio(props.tipusGrafic),
        tipusGrafic = 'BAR_CHART',
        llegendaX,

        // Visual attributes
        colorsPaleta = '#1f77b4,#ff7f0e,#2ca02c,#d62728,#9467bd,#8c564b',
        mostrarReticula = false,

        // Bar chart specific
        barStacked = false,
        barHorizontal = false,

        // Line chart specific
        lineShowPoints = true,
        lineSmooth = false,
        lineWidth = 2,
        area = false,

        // Pie chart specific
        pieDonut = false,
        pieShowLabels = true,
        outerRadius,
        innerRadius,
        labelSize,

        // Gauge chart specific
        gaugeMin = 0,
        gaugeMax = 100,
        // gaugeColors = '#d4e6f1,#3498db,#1a5276',
        gaugeRangs = '50,75,100',

        // Heatmap chart specific
        // heatmapColors = '#d4e6f1,#3498db,#1a5276',
        // heatmapMinValue = 0,
        // heatmapMaxValue = 100,

        // Additional props
        loading = false,
        preview = false,
        error = false,
        errorMsg,
        errorTrace,
        senseAccesDades = false,
        onClick,
        dashboardEntornCodi,

        //midaFontTitol,
        //midaFontDescripcio,
    } = props;
    const {t} = useTranslation();
    const theme = useTheme();
    const {
        textColor,
        backgroundColor,
        voraColor,
        isWhiteBackground,
        contrastTextColor,
        bgColor,
        bg,
        voraAmple,
    } = useWidgetTheme({
        colorText: props.colorText,
        colorFons: props.colorFons,
        colorVora: props.colorVora,
        mostrarVora,
        ampleVora,
    });
    const chartTextColor = contrastTextColor;
    const axisStyleProps = {
        tickLabelStyle: {fill: chartTextColor},
        labelStyle: {fill: chartTextColor},
        slotProps: {
            axisLine: {stroke: chartTextColor, style: {stroke: chartTextColor}},
            axisTick: {stroke: chartTextColor, style: {stroke: chartTextColor}},
        },
    };
    // Per defecte, MUI X-Charts amaga (tickLabelInterval: 'auto') les etiquetes de categoria de l'eix que
    // calcula que se solaparien amb l'anterior — amb prou categories (p. ex. un any complet de mesos) això
    // acaba amagant-les TOTES en lloc de només les que no hi caben. Es força que es mostrin totes, inclinades
    // per reduir el solapament visual.
    const bandTickLabelProps = {
        tickLabelInterval: () => true,
        tickLabelStyle: {
            fill: chartTextColor,
            angle: -35,
            textAnchor: 'end' as const
        },
    };
    const chartCommonSx = {
        '& .MuiChartsAxis-line': {stroke: chartTextColor},
        '& .MuiChartsAxis-tick': {stroke: chartTextColor},
        '& .MuiChartsAxis-root line': {stroke: chartTextColor},
        '& .MuiChartsAxis-root path': {stroke: chartTextColor},
        '& .MuiChartsAxis-tickLabel': {fill: chartTextColor},
        '& .MuiChartsAxis-label': {fill: chartTextColor},
        '& .MuiChartsLegend-label': {fill: chartTextColor},
    };

    const paletaColors = colorsPaleta.split(',');
    const chartHeight = preview ? 150 : 300;

    const renderChart = () => {
        switch (tipusGrafic) {
            case 'BAR_CHART':
                return renderBarChart();
            case 'LINE_CHART':
                return renderLineChart();
            case 'PIE_CHART':
                return renderPieChart();
            case 'SCATTER_CHART':
                return renderScatterChart();
            case 'SPARK_LINE_CHART':
                return renderSparkLineChart();
            case 'GAUGE_CHART':
                return renderGaugeChart();
            case 'HEATMAP_CHART':
                return renderHeatmapChart();
            default:
                return renderBarChart();
        }
    };

    // Render a bar chart
    const renderBarChart = () => {
        const discriminador: string = !columnaAgregacio ? 'agregacio' : columnaAgregacio;
        const dataKeys = dades.length > 0
            ? Object.keys(dades[0]).filter(key => key !== discriminador)
            : [];

        const dataset = dades.map(item => ({
            [discriminador]: item[discriminador],
            ...dataKeys.reduce((acc: Record<string, unknown>, key: string) => {
                acc[key] = item[key] || 0;
                return acc;
            }, {}),
        }));

        const series = dataKeys.map((key, index) => ({
            dataKey: key,
            label: labels?.find(label => label.id === key)?.label || key,
            color: paletaColors[index % paletaColors.length],
            stack: barStacked ? 'stack' : undefined,
        }));

        const xAxis: Array<XAxis> = [];
        const yAxisLabel = preview ? t($ => $.page.plantilla.sample.yAxis) : undefined;
        if (barHorizontal) { // Si és horitzontal, l'eix X té valors numèrics
            xAxis.push({
                scaleType: 'linear',
                label: llegendaX || (preview ? t($ => $.page.plantilla.sample.xAxis) : undefined),
                // min: 0,
                // max: 100,
                ...axisStyleProps
            });
        } else { // Si no és horitzontal, l'eix X té categories
            xAxis.push({
                scaleType: 'band',
                // dataKey: discriminador,
                data: dades.map(item => item[discriminador]),
                label: llegendaX || (preview ? t($ => $.page.plantilla.sample.xAxis) : undefined),
                ...axisStyleProps,
                ...bandTickLabelProps,
                tickLabelInterval: () => true,
            });
        }

        const yAxis: Array<YAxis> = [];
        if (barHorizontal) { // Si és horitzontal, l'eix Y té categories
            yAxis.push({
                scaleType: 'band',
                data: dades.map(item => item[discriminador]),
                label: llegendaX || undefined,
                ...axisStyleProps,
                tickLabelInterval: () => true,
            });
        } else { // Si no és horitzontal, l'eix Y té valors numèrics
            yAxis.push({
                scaleType: 'linear',
                label: yAxisLabel,
                // min: 0,
                // max: 100,
                ...axisStyleProps
            });
        }

        const grid = barHorizontal
            ? mostrarReticula ? {vertical: true} : {vertical: false}
            : mostrarReticula ? {horizontal: true} : {horizontal: false};

        console.log(dataset);
        console.log(dataset.map(item => item[discriminador]));
        console.log(series);
        console.log(xAxis);
        console.log(yAxis);

        return (
            <Box sx={{width: '100%', height: chartHeight}}>
                <BarChart
                    sx={chartCommonSx}
                    dataset={dataset}
                    series={series}
                    xAxis={xAxis}
                    yAxis={yAxis}
                    layout={barHorizontal ? 'horizontal' : 'vertical'}
                    grid={grid}
                    height={chartHeight}
                    margin={{top: 10, bottom: 40, left: 20, right: 10}}
                    slotProps={{
                        legend: {
                            sx: {
                                '& .MuiChartsLegend-label': {fill: chartTextColor, color: chartTextColor},
                            },
                        },
                    }}
                >
                    <ChartsTooltip/>
                    <ChartsLegend/>
                </BarChart>
            </Box>
        );
    };

    // Render a line chart
    const renderLineChart = () => {
        const discriminador: string = !columnaAgregacio ? 'agregacio' : columnaAgregacio;
        const dataKeys = dades.length > 0
            ? Object.keys(dades[0]).filter(key => key !== discriminador)
            : [];

        const series: LineSeries[] = dataKeys.map((key, index) => ({
            dataKey: key,
            label: labels?.find((label) => label.id === key)?.label || key,
            color: paletaColors[index % paletaColors.length],
            curve: lineSmooth ? 'natural' : 'linear',
            showMark: lineShowPoints,
            area: area,
        }));

        const xAxisData: ReadonlyArray<XAxis<'band'>> = [{
            dataKey: discriminador,
            scaleType: 'band',
            label: llegendaX,
            ...axisStyleProps,
            ...bandTickLabelProps,
            // data: dades.map(d => d[datakey]),
        }];

        const grid = mostrarReticula ? {horizontal: true} : {horizontal: false};

        return (
            <Box sx={{width: '100%', height: chartHeight}}>
                <LineChart
                    sx={{
                        ...chartCommonSx,
                        '& .MuiLineElement-root': {strokeWidth: +lineWidth},
                    }}
                    xAxis={xAxisData}
                    yAxis={[{scaleType: 'linear', label: preview ? 'Eix Y' : undefined, ...axisStyleProps}]}
                    series={series}
                    dataset={dades}
                    height={chartHeight}
                    grid={grid}
                    // Vegeu comentari equivalent a renderBarChart: cal espai per a les etiquetes de categoria I el títol de l'eix alhora.
                    margin={{top: 10, bottom: 40, left: 20, right: 10}}
                    slotProps={{
                        legend: {
                            sx: {
                                '& .MuiChartsLegend-label': {fill: chartTextColor, color: chartTextColor},
                            },
                        },
                    }}
                >
                    <ChartsTooltip/>
                    <ChartsLegend/>
                </LineChart>
            </Box>
        );
    };

    // Render a pie chart
    const renderPieChart = () => {
        const pieData = dades.map((item, index) => ({
            id: index,
            label: (labels?.find(label => label.id === item.label)?.label || item.label) as string | undefined,
            value: typeof item.value === 'number' ? item.value : 0,
            color: paletaColors[index % paletaColors.length],
        }));

        const radiExterior = outerRadius ? outerRadius : preview ? 60 : 80;
        const radiInterior = innerRadius ? innerRadius : pieDonut ? radiExterior * 0.6 : 0;

        interface ArcLabelParams {
            label?: string;

            [key: string]: any;
        }

        const getArcLabel = (params: ArcLabelParams) => {
            return pieShowLabels ? (params.label ?? '') : '';
        };

        const ContrastPieArcLabel = ({color, ...other}: any) => {
            const labelColor = theme.palette.getContrastText(color);
            return (
                <PieArcLabel
                    {...other}
                    color={color}
                    fill={labelColor}
                    style={{...(other?.style || {}), fill: labelColor, color: labelColor}}
                />
            );
        };

        const series: Readonly<PieSeries[]> = [{
            data: pieData,
            innerRadius: +radiInterior,
            outerRadius: +radiExterior,
            paddingAngle: 1,
            cornerRadius: 4,
            arcLabel: getArcLabel,
            arcLabelMinAngle: 20,
            highlightScope: {fade: 'global', highlight: 'item'},
            faded: {innerRadius: 20, additionalRadius: -15, color: 'gray'},
        }];

        return (
            <Box sx={{width: '100%', height: chartHeight}}>
                <PieChart
                    sx={{
                        ...chartCommonSx,
                        '& .MuiPieArcLabel-root': {fontSize: labelSize ? labelSize + 'px' : '1em'},
                    }}
                    slots={{pieArcLabel: ContrastPieArcLabel}}
                    series={series}
                    height={chartHeight}
                    margin={{top: 10, bottom: 10, left: 10, right: 10}}
                    slotProps={{
                        legend: {
                            sx: {
                                '& .MuiChartsLegend-label': {fill: chartTextColor, color: chartTextColor},
                            },
                        },
                    }}
                >
                    <ChartsTooltip/>
                    <ChartsLegend/>
                </PieChart>
            </Box>
        );
    };

    // Render a gauge chart (Line version) //TODO Modificar back para devolver multiples valores para el valor X
    const renderScatterChart = () => {
        // //Opciones graficas
        // const discriminador: string = !columnaAgregacio ? 'agregacio' : columnaAgregacio;
        // const paletaColors = colorsPaleta.split(',');
        // const grid = mostrarReticula ? { horizontal: true, } : { horizontal: false, };
        // //Valores
        // const indexed = dades.map((d, i) => ({ ...d, index: i }));
        // const keys = Object.keys(indexed[0]).filter(
        //     (k) => k !== discriminador && k !== "index"
        //     );
        // const series = keys.map((key, index) => ({
        //     label: labels?.find((label) => label.id === key)?.label || key,
        //     data: indexed.map((row:any) => ({ x: row.index, y: row[key] })),
        //     color: paletaColors[index % paletaColors.length],
        //     }));
        // //Componente
        // return (
        //     <Box sx={{ width: '100%', height: chartHeight }}>
        //     <ScatterChart
        //         height={chartHeight}
        //         xAxis={[{ label: "Día", min: 0 }]}
        //         yAxis={[{ label: "Valor" }]}
        //         series={series}
        //         grid={grid}
        //     />
        //     </Box>
        // );
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: chartHeight,
                color: theme.palette.text.secondary,
            }}>
                <Typography sx={{color: theme.palette.error.main}}>
                    Scatter Chart (Pendent d'implementació)
                </Typography>
            </Box>
        );
    };

    // Render a spark chart
    const renderSparkLineChart = () => {
        const discriminador: string = !columnaAgregacio ? 'agregacio' : columnaAgregacio;
        const numericKey = dades.length > 0
            ? Object.keys(dades[0]).find((key) => key !== discriminador)
            : null;
        const numericData: number[] = numericKey
            ? dades.map((item) => Number(item[numericKey])).filter((n) => !isNaN(n))
            : [];

        return (
            <Box sx={{width: '100%', height: chartHeight}}>
                <SparkLineChart
                    data={numericData}
                    height={chartHeight}
                    showTooltip={lineShowPoints}
                    showHighlight={lineShowPoints}
                    curve={lineSmooth ? 'monotoneX' : 'linear'}
                    area={area}
                    sx={{
                        '& .MuiLineElement-root': {strokeWidth: +lineWidth},
                    }}
                />
            </Box>);
    };

    // Render a gauge chart
    const renderGaugeChart = () => {
        const valorGauge = Array.isArray(dades) && dades.length > 0 ? Number(dades[0].value) : 0;
        const colors = colorsPaleta ? colorsPaleta.split(',').map(c => c.trim()) : ["#000000"];
        const rangs = gaugeRangs ? gaugeRangs.split(',').map(r => Number(r.trim())).filter(v => !isNaN(v)) : [];
        const getColor = (value: number) => {
            for (let i = 0; i < colors.length && i < rangs.length; i++) {
                if (value < rangs[i]) {
                    return colors[i];
                }
            }
            return colors[colors.length - 1];//Por defecto devolvemos el ultimo valor.
        };

        return (
            <Box sx={{
                width: '100%',
                height: chartHeight,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                overflow: 'hidden',
            }}>
                <Gauge
                    value={valorGauge}
                    valueMin={gaugeMin}
                    valueMax={gaugeMax}
                    sx={() => ({
                        [`& .${gaugeClasses.valueArc}`]: {
                            fill: getColor(valorGauge),
                        },
                        [`& .${gaugeClasses.valueText}`]: {
                            fill: `${chartTextColor} !important`,
                            color: `${chartTextColor} !important`,
                        },
                        [`& .${gaugeClasses.valueText} *`]: {
                            fill: `${chartTextColor} !important`,
                            color: `${chartTextColor} !important`,
                        },
                    })}
                />
            </Box>
        );
    };

    // Render a heatmap chart (simplified version)
    const renderHeatmapChart = () => {
        // const colorArray = heatmapColors.split(',').map(c => c.trim());

        // const xCategories = Array.from(new Set(dades.map(d => d.x)));
        // const yCategories = Array.from(new Set(dades.map(d => d.y)));

        // const heatmapData = dades.map(d => [
        //         xCategories.indexOf(d.x),
        //         yCategories.indexOf(d.y),
        //         d.value
        //     ]);


        // return (
        //     <Box sx={{ width: '100%', height: chartHeight }}>
        //     <Heatmap
        //         height={chartHeight}
        //         xAxis={[{ scaleType: 'band', data: xCategories }]}
        //         yAxis={[{ scaleType: 'band', data: yCategories }]}
        //         series={[
        //         {
        //             type: 'heatmap',
        //             data: heatmapData,
        //             colorMap: {
        //             type: 'continuous',
        //             min: heatmapMinValue,
        //             max: heatmapMaxValue,
        //             colors: colorArray,
        //             },
        //         },
        //         ]}
        //     />
        //     </Box>
        // );
        // };

        // For preview, just show a placeholder
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: chartHeight,
                color: theme.palette.text.secondary,
            }}>
                <Typography sx={{color: theme.palette.error.main}}>
                    Heatmap Chart (Pendent d'implementació)
                </Typography>
            </Box>
        );
    };

    return (
        <WidgetContainer
            bgColor={bgColor}
            bg={bg}
            textColor={textColor}
            mostrarVora={mostrarVora}
            voraAmple={voraAmple}
            voraColor={voraColor}
            onClick={onClick}
        >
            <WidgetHeader
                titol={titol}
                entornCodi={entornCodi}
                loading={loading}
                isWhiteBackground={isWhiteBackground}
                backgroundColor={backgroundColor}
                voraColor={voraColor}
                contrastTextColor={contrastTextColor}
                dashboardEntornCodi={dashboardEntornCodi}
            />

            {error ? (
                <WidgetErrorDisplay errorMsg={errorMsg} errorTrace={errorTrace}/>
            ) : senseAccesDades ? (
                <WidgetNoAccessDisplay/>
            ) : (
                <>
                    <Box sx={estils.tableContainerBox}>
                        {loading ? (
                            <Box sx={{
                                width: '100%',
                                height: chartHeight,
                                display: 'flex',
                                flexDirection: 'column',
                                justifyContent: 'center'
                            }}>
                                <Skeleton variant="rectangular" width="100%" height={chartHeight}/>
                            </Box>
                        ) : (
                            renderChart()
                        )}
                    </Box>

                    <WidgetFooter
                        descripcio={descripcio}
                        textColor={textColor}
                        loading={loading}
                    />
                </>
            )}
        </WidgetContainer>
    );
};

// Helper function to generate sample data based on chart type
const generateSampleData = (chartType?: string): Record<string, unknown>[] => {
    switch (chartType) {
        case 'BAR_CHART':
            return [
                {name: 'Gen', valor1: 400, valor2: 240},
                {name: 'Feb', valor1: 300, valor2: 139},
                {name: 'Mar', valor1: 200, valor2: 980},
                {name: 'Abr', valor1: 278, valor2: 390},
                {name: 'Mai', valor1: 189, valor2: 480},
            ];
        case 'LINE_CHART':
        case 'SPARK_LINE_CHART':
            return [
                {x: 'Gen', y: 2},
                {x: 'Feb', y: 5.5},
                {x: 'Mar', y: 2},
                {x: 'Abr', y: 8.5},
                {x: 'Mai', y: 1.5},
                {x: 'Jun', y: 5},
            ];
        case 'SCATTER_CHART':
            return [
                {name: "A", valor1: 1.645, valor2: 1.797, valor3: 2.0},
                {name: "B", valor1: 1.645, valor2: 1.597, valor3: 1.0},
                {name: "C", valor1: 1.945, valor2: 1.797, valor3: 0.0},
                {name: "D", valor1: 2.045, valor2: 0.997, valor3: -1.0},
                {name: "E", valor1: 2.945, valor2: 0.797, valor3: 0.5},
                {name: "F", valor1: 1.945, valor2: -0.797, valor3: 2.5},
            ];
        case 'PIE_CHART':
            return [
                {label: 'Grup A', value: 400},
                {label: 'Grup B', value: 300},
                {label: 'Grup C', value: 300},
                {label: 'Grup D', value: 200},
            ];
        case 'GAUGE_CHART':
            return [{value: 75}];
        case 'HEATMAP_CHART':
            return [
                {x: 'A', y: 'X', value: 10},
                {x: 'B', y: 'X', value: 20},
                {x: 'A', y: 'Y', value: 30},
                {x: 'B', y: 'Y', value: 40},
            ];
        default:
            return [
                {name: 'Gen', valor1: 400, valor2: 240},
                {name: 'Feb', valor1: 300, valor2: 139},
                {name: 'Mar', valor1: 200, valor2: 980},
                {name: 'Abr', valor1: 278, valor2: 390},
                {name: 'Mai', valor1: 189, valor2: 480},
            ];
    }
};

const generateSampleLabels = (chartType?: string): ColumnLabel[] | undefined => {
    switch (chartType) {
        case 'BAR_CHART':
            return [
                {id: 'name', label: 'Mes'},
                {id: 'valor1', label: 'Valor 1'},
                {id: 'valor2', label: 'Valor 2'},
            ];
        case 'LINE_CHART':
            return [
                {id: 'x', label: 'X'},
                {id: 'y', label: 'Y'},
            ];
        case 'SCATTER_CHART':
            return [
                {id: "name", label: "Dia"},
                {id: "valor1", label: "Valor 1"},
                {id: "valor2", label: "Valor 2"},
                {id: "valor3", label: "Valor 3"}
            ];
        default:
            return undefined;
    }
};
const generateSampleAgregacio = (chartType?: string): string | undefined => {
    switch (chartType) {
        case 'BAR_CHART':
            return 'name';
        case 'LINE_CHART':
        case 'SPARK_LINE_CHART':
            return 'x';
        case 'PIE_CHART':
            return 'name';
        case 'SCATTER_CHART':
            return 'name';
        default:
            return undefined;
    }
};
export default GraficWidgetVisualization;
