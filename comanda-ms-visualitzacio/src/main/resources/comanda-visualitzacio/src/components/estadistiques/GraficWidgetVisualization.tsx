import React from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import {useTheme} from '@mui/material/styles';
import {
    BarChart,
    LineChart,
    PieChart,
    ChartsTooltip,
    ChartsLegend,
    ChartsXAxis,
    ChartsYAxis,
    ChartsGrid,
    BarPlot,
    LinePlot,
    PieArcPlot,
    PieArcLabel,
} from '@mui/x-charts';

interface ColumnLabel {
    id: string;
    label: string;
}

// Define the props for the GraficWidgetVisualization component
export interface GraficWidgetVisualizationProps {
    // Widget data
    title?: string;
    dades?: Record<string, unknown>[];
    labels?: ColumnLabel[];
    columnaAgregacio?: string;
    tipusGrafic?: 'BAR_CHART' | 'LINE_CHART' | 'PIE_CHART' | 'GAUGE_CHART' | 'HEATMAP_CHART' | 'SCATTER_CHART' | 'SPARK_LINE_CHART';
    llegendaX?: string;

    // Visual attributes
    colorsPaleta?: string;  // Colors separated by commas
    mostrarReticula?: boolean;

    // Bar chart specific
    barStacked?: boolean;
    barHorizontal?: boolean;

    // Line chart specific
    lineShowPoints?: boolean;
    lineSmooth?: boolean;
    lineWidth?: number;
    area?: boolean;

    // Pie chart specific
    pieDonut?: boolean;
    pieShowLabels?: boolean;
    outerRadius?: number;
    innerRadius?: number;
    labelSize?: number;

    // Gauge chart specific
    gaugeMin?: number;
    gaugeMax?: number;
    gaugeColors?: string;  // Colors separated by commas
    gaugeRangs?: string;   // Ranges separated by commas

    // Heatmap chart specific
    heatmapColors?: string;  // Colors separated by commas
    heatmapMinValue?: number;
    heatmapMaxValue?: number;

    // Additional props
    loading?: boolean;
    preview?: boolean;
    onClick?: () => void;
}

/**
 * Component for visualizing a graph widget with configurable visual attributes.
 * Can be used both for dashboard display and for preview in configuration forms.
 */
const GraficWidgetVisualization: React.FC<GraficWidgetVisualizationProps> = (props) => {
    const {
        // Widget data
        title = 'Títol del gràfic',
        dades = generateSampleData(props.tipusGrafic),
        labels = generateSampleLabels(props.tipusGrafic),
        columnaAgregacio = generateSampleAgregacio(props.tipusGrafic),
        tipusGrafic = 'BAR_CHART',
        llegendaX,

        // Visual attributes
        colorsPaleta = '#1f77b4,#ff7f0e,#2ca02c,#d62728,#9467bd,#8c564b',
        mostrarReticula = true,

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
        gaugeColors = '#d4e6f1,#3498db,#1a5276',
        gaugeRangs = '0,50,75,100',

        // Heatmap chart specific
        heatmapColors = '#d4e6f1,#3498db,#1a5276',
        heatmapMinValue = 0,
        heatmapMaxValue = 100,

        // Additional props
        loading = false,
        preview = false,
        onClick,
    } = props;

    const theme = useTheme();

    // Parse color palette
    const colors = colorsPaleta.split(',');

    // Determine chart height based on preview mode
    const chartHeight = preview ? 150 : 300;

    // Render the appropriate chart based on the type
    const renderChart = () => {
        console.log('Tipus gràfic', tipusGrafic);
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
        // Extract data keys
        console.log('dades', dades);
        const discriminador: string = !columnaAgregacio ? 'agregacio' : columnaAgregacio;
        console.log('discriminador', discriminador);
        const dataKeys = dades.length > 0
            ? Object.keys(dades[0]).filter(key => key !== discriminador)  // Exclou el discriminador, ja que es fa servir per al `xAxis`.
            : [];
        console.log('dataKeys', dataKeys);
        // Construïm el `dataset` utilitzat pel gràfic. Aquí simplement copiem la matriu de dades.
        // const dataset = [...dades];
        const dataset = dades.map((item) => ({
            [discriminador]: item[discriminador], // La categoria (eix X o Y segons l'orientació)
            ...dataKeys.reduce((acc: Record<string, unknown>, key: string) => {
                acc[key] = item[key] || 0;
                return acc;
            }, {}),
        }));
        console.log('dataset:', dataset);

        // Prepare series for MUI X-Charts
        const series = dataKeys.map((key, index) => ({
            dataKey: key,
            label: labels?.find((label) => label.id === key)?.label || key,
            color: colors[index % colors.length],
            stack: barStacked ? 'stack' : undefined,
        }));
        console.log('series', series);

        // Prepare xAxis categories from data
        const xAxis = barHorizontal
            ? {scaleType: 'linear'} // Si és horitzontal, l'eix X té valors numèrics
            : {scaleType: 'band', data: dades.map((item) => item.name)}; // Si no és horitzontal, l'eix X té categories
        const xAxisData = barHorizontal && llegendaX ? xAxis : {...xAxis, label: llegendaX};

        const yAxis = barHorizontal
            ? {scaleType: 'band', data: dades.map((item) => item.name)} // Si és horitzontal, l'eix Y té categories
            : {scaleType: 'linear'}; // Si no és horitzontal, l'eix Y té valors numèrics
        const yAxisData = barHorizontal && llegendaX ? {...yAxis, label: llegendaX} : yAxis;

        console.log('xAxisData', xAxisData);
        console.log('yAxisData', yAxisData);

        const grid = mostrarReticula
            ? {
                horizontal: true,
                // vertical: true,
            }
            : {
                horizontal: false,
                // vertical: false,
            }

        return (
            <Box sx={{width: '100%', height: chartHeight}}>
                <BarChart
                    dataset={dataset}
                    series={series}
                    xAxis={[xAxisData]}
                    yAxis={[yAxisData]}
                    layout={barHorizontal ? 'horizontal' : 'vertical'}
                    grid={grid}
                    height={chartHeight}
                    margin={{top: 10, bottom: 30, left: 40, right: 10}}
                >
                    <ChartsTooltip/>
                    <ChartsLegend/>
                </BarChart>
            </Box>
        );
    };

    // Render a line chart
    const renderLineChart = () => {
        // Extract data keys
        console.log('dades', dades);
        const datakey: string = !columnaAgregacio ? 'agregacio' : columnaAgregacio;
        console.log('datakey', datakey);
        const dataKeys = dades.length > 0
            ? Object.keys(dades[0]).filter(key => key !== datakey)  // Exclou el discriminador, ja que es fa servir per al `xAxis`.
            : [];
        console.log('dataKeys', dataKeys);
        console.log('Tipus de lineWidth abans del map:', typeof lineWidth);
        // Prepare series for MUI X-Charts
        const series = dataKeys.map((key, index) => ({
            dataKey: key,
            label: labels?.find((label) => label.id === key)?.label || key,
            color: colors[index % colors.length],
            curve: lineSmooth ? 'natural' : 'linear',
            showMark: lineShowPoints,
            area: area,
        }));
        console.log('series', series);

        // Prepare xAxis categories from data
        const xAxisData = [{
            dataKey: datakey,
            scaleType: 'band',
            label: llegendaX,
            // data: dades.map(d => d[datakey]),
        }];
        console.log('xAxisData', xAxisData);

        const grid = mostrarReticula
            ? {
                horizontal: true,
                // vertical: true,
            }
            : {
                horizontal: false,
                // vertical: false,
            }

        return (
            <Box sx={{width: '100%', height: chartHeight}}>
                <LineChart
                    sx={{
                        '& .MuiLineElement-root': {
                            strokeWidth: +lineWidth,
                        },
                    }}
                    xAxis={xAxisData}
                    yAxis={[{scaleType: 'linear'}]}
                    series={series}
                    dataset={dades}
                    height={chartHeight}
                    grid={grid}
                    margin={{top: 10, bottom: 30, left: 40, right: 10}}
                >
                    <ChartsTooltip/>
                    <ChartsLegend/>
                </LineChart>
            </Box>
        );
    };

    // Render a pie chart
    const renderPieChart = () => {
        // For pie chart, transform data if needed
        const pieData = dades.map((item, index) => ({
            id: index,
            label: item.label,
            value: typeof item.value === 'number' ? item.value : 0,
            color: colors[index % colors.length]
        }));

        // Calculate inner radius for donut chart
        const radiExterior = outerRadius ? outerRadius : preview ? 60 : 80;
        const radiInterior = innerRadius? innerRadius : pieDonut ? radiExterior * 0.6 : 0;

        // Define a simple interface for the arc label params
        interface ArcLabelParams {
            label: string;
            [key: string]: any; // Allow any other properties
        }

        // Custom label component for pie chart
        const getArcLabel = (params: ArcLabelParams) => {
            return pieShowLabels ? params.label : '';
        };

        const series = [
            {
                data: pieData,
                innerRadius: +radiInterior,
                outerRadius: +radiExterior,
                paddingAngle: 1,
                cornerRadius: 4,
                arcLabel: getArcLabel,
                arcLabelMinAngle: 20,
                highlightScope: { fade: 'global', highlight: 'item' },
                faded: { innerRadius: 20, additionalRadius: -15, color: 'gray' },
            },
        ];
        console.log('series', series);
        return (
            <Box sx={{ width: '100%', height: chartHeight }}>
                <PieChart
                    sx={{
                        '& .MuiPieArcLabel-root': {
                            fontSize: labelSize ? labelSize + 'px' : '1em',
                        },
                    }}
                    series={series}
                    height={chartHeight}
                    margin={{top: 10, bottom: 10, left: 10, right: 10}}
                >
                    <ChartsTooltip/>
                    <ChartsLegend/>
                </PieChart>
            </Box>
        );
    };

    // Render a gauge chart (simplified version)
    const renderScatterChart = () => {
        // For preview, just show a placeholder
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: chartHeight,
                color: theme.palette.text.secondary
            }}>
                <Typography sx={{color: theme.palette.error.main}}>
                    Scatter Chart (Pendent d'implementació)
                </Typography>
            </Box>
        );
    };

    // Render a gauge chart (simplified version)
    const renderSparkLineChart = () => {
        // For preview, just show a placeholder
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: chartHeight,
                color: theme.palette.text.secondary
            }}>
                <Typography sx={{color: theme.palette.error.main}}>
                    SparkLine Chart (Pendent d'implementació)
                </Typography>
            </Box>
        );
    };

    // Render a gauge chart (simplified version)
    const renderGaugeChart = () => {
        // For preview, just show a placeholder
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: chartHeight,
                color: theme.palette.text.secondary
            }}>
                <Typography sx={{color: theme.palette.error.main}}>
                    Gauge Chart (Pendent d'implementació)
                </Typography>
            </Box>
        );
    };

    // Render a heatmap chart (simplified version)
    const renderHeatmapChart = () => {
        // For preview, just show a placeholder
        return (
            <Box sx={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                height: chartHeight,
                color: theme.palette.text.secondary
            }}>
                <Typography sx={{color: theme.palette.error.main}}>
                    Heatmap Chart (Pendent d'implementació)
                </Typography>
            </Box>
        );
    };

    return (
        <Paper
            elevation={preview ? 1 : 2}
            onClick={onClick}
            sx={{
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                borderRadius: '.6rem',
                cursor: onClick ? 'pointer' : 'default',
                height: '100%',
                minHeight: preview ? '200px' : '350px',
                transition: 'all 0.2s ease-in-out',
                '&:hover': {
                    boxShadow: onClick ? theme.shadows[4] : undefined,
                },
            }}
        >
            {/* Title */}
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    pt: 1,
                    px: 2,
                }}
            >
                <Typography
                    sx={{
                        letterSpacing: '0.025em',
                        fontWeight: '500',
                        fontSize: '1.2rem',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap',
                        width: '100%',
                    }}
                >
                    {title}
                </Typography>
            </Box>

            {/* Chart */}
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    flex: 1,
                    p: 2,
                    width: '100%',
                }}
            >
                {renderChart()}
            </Box>
        </Paper>
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
            return [
                {x: 'Gen', y: 2},
                {x: 'Feb', y: 5.5},
                {x: 'Mar', y: 2},
                {x: 'Abr', y: 8.5},
                {x: 'Mai', y: 1.5},
                {x: 'Jun', y: 5},
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
        default:
            return undefined;
    }
};
const generateSampleAgregacio = (chartType?: string): string | undefined => {
    switch (chartType) {
        case 'BAR_CHART':
            return 'name';
        case 'LINE_CHART':
            return 'x';
        case 'PIE_CHART':
            return 'name';
        default:
            return undefined;
    }
}
export default GraficWidgetVisualization;
