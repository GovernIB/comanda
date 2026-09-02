import {fireEvent, render, screen} from '@testing-library/react';
import {createTheme, ThemeProvider} from '@mui/material/styles';
import {describe, expect, it, vi} from 'vitest';
import GraficWidgetVisualization from './GraficWidgetVisualization';

type MockAxis = { scaleType?: string; tickLabelInterval?: (value: unknown, index: number) => boolean };

vi.mock('@mui/x-charts', () => ({
    BarChart: ({margin, xAxis}: { margin?: { bottom?: number }; xAxis?: MockAxis[] }) => {
        const bandAxis = xAxis?.find((axis) => axis.scaleType === 'band');
        return (
            <div
                data-testid="bar-chart"
                data-margin-bottom={margin?.bottom}
                data-band-tick-label-interval={bandAxis ? String(bandAxis.tickLabelInterval?.('x', 0)) : undefined}
            >
                BarChart
            </div>
        );
    },
    LineChart: ({margin, xAxis}: { margin?: { bottom?: number }; xAxis?: MockAxis[] }) => {
        const bandAxis = xAxis?.find((axis) => axis.scaleType === 'band');
        return (
            <div
                data-testid="line-chart"
                data-margin-bottom={margin?.bottom}
                data-band-tick-label-interval={bandAxis ? String(bandAxis.tickLabelInterval?.('x', 0)) : undefined}
            >
                LineChart
            </div>
        );
    },
    PieChart: () => <div data-testid="pie-chart">PieChart</div>,
    ChartsTooltip: () => <div data-testid="charts-tooltip">Tooltip</div>,
    ChartsLegend: () => <div data-testid="charts-legend">Legend</div>,
    SparkLineChart: () => <div data-testid="spark-line-chart">SparkLine</div>,
    Gauge: ({value, valueMin, valueMax, text, sx}: { value?: number; valueMin?: number; valueMax?: number; text?: (params: { value: number | null }) => string; sx?: () => Record<string, { fill?: string }> }) => {
        const style = sx ? sx() : undefined;
        const arcFill = style?.['& .valueArc']?.fill;
        return (
            <div data-testid="gauge-chart" data-value={value} data-value-min={valueMin} data-value-max={valueMax} data-text={text ? text({ value: value ?? null }) : undefined} data-value-arc-fill={arcFill}>Gauge</div>
        );
    },
    XAxis: () => null,
    YAxis: () => null,
    LineSeries: () => null,
    gaugeClasses: {valueArc: 'valueArc', referenceArc: 'referenceArc'},
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('GraficWidgetVisualization', () => {
    it('GraficWidgetVisualization_quanEsRenderitzaEnModeBar_mostraElGraficIElTextBase', () => {
        // Comprova que el component mostra el gràfic corresponent i les dades textuals bàsiques.
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic principal"
                descripcio="Descripció resum"
                entornCodi="PRO"
                tipusGrafic="BAR_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByText('Gràfic principal')).toBeInTheDocument();
        expect(screen.getByText('PRO')).toBeInTheDocument();
        expect(screen.getByTestId('bar-chart')).toBeInTheDocument();
        expect(screen.getByText('Descripció resum')).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_enModeBar_reservaProuMarInferiorPerEtiquetesDeCategoriaIEixTitol', () => {
        // Regressió: amb poc marge inferior, MUI X-Charts amaga les etiquetes de categoria de l'eix X
        // (només queda visible el títol genèric de l'eix), tot i que les dades de cada categoria s'hi passen.
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic principal"
                tipusGrafic="BAR_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        const marginBottom = Number(screen.getByTestId('bar-chart').getAttribute('data-margin-bottom'));
        expect(marginBottom).toBeGreaterThanOrEqual(20);
    });

    it('GraficWidgetVisualization_enModeLine_reservaProuMarInferiorPerEtiquetesDeCategoriaIEixTitol', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic línia"
                tipusGrafic="LINE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        const marginBottom = Number(screen.getByTestId('line-chart').getAttribute('data-margin-bottom'));
        expect(marginBottom).toBeGreaterThanOrEqual(20);
    });

    it('GraficWidgetVisualization_enModeBar_forcaMostrarTotesLesEtiquetesDeCategoria', () => {
        // Regressió: per defecte MUI X-Charts amaga (tickLabelInterval: 'auto') les etiquetes que calcula que
        // se solaparien amb l'anterior — amb moltes categories (p. ex. un any de mesos) això acaba amagant-les
        // TOTES en comptes de només les que no hi caben. S'ha de forçar que es mostrin totes.
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic principal"
                tipusGrafic="BAR_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByTestId('bar-chart').getAttribute('data-band-tick-label-interval')).toBe('true');
    });

    it('GraficWidgetVisualization_enModeLine_forcaMostrarTotesLesEtiquetesDeCategoria', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic línia"
                tipusGrafic="LINE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByTestId('line-chart').getAttribute('data-band-tick-label-interval')).toBe('true');
    });

    it('GraficWidgetVisualization_quanCanviaElTipus_renderitzaElComponentDeGraficEsperat', () => {
        // Verifica que el component selecciona el render correcte segons el tipus de gràfic.
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic línia"
                tipusGrafic="LINE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByTestId('line-chart')).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanHiHaError_mostraLEstatDerror', () => {
        // Comprova que el component substitueix el gràfic pel bloc d'error quan la càrrega falla.
        renderComponent(
            <GraficWidgetVisualization
                error={true}
                errorMsg="Error del gràfic"
                errorTrace="Traça del gràfic"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByText('Error del gràfic')).toBeInTheDocument();
        expect(screen.getByText('Traça del gràfic')).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanRepOnClick_invocaElCallbackEnClicar', () => {
        // Verifica que el contenidor principal permet reaccionar a clics externs.
        const onClick = vi.fn();
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic clicable"
                tipusGrafic="PIE_CHART"
                onClick={onClick}
                mostrarVora={false}
                ampleVora={1}
            />
        );

        fireEvent.click(screen.getByText('Gràfic clicable'));

        expect(onClick).toHaveBeenCalledTimes(1);
    });

    it('GraficWidgetVisualization_quanEsRenderitzaEnModePie_mostraElPieChart', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic circular"
                tipusGrafic="PIE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByTestId('pie-chart')).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanEsRenderitzaEnModeGauge_mostraElGauge', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic gauge"
                tipusGrafic="GAUGE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByTestId('gauge-chart')).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanEsGaugeSenseDadesIAmbRangsPerDefecte_usaElPrimerColorDeLaPaleta', () => {
        // El valor de mostra (75) coincidia exactament amb el segon llindar per defecte de
        // gaugeRangs ('50,75,100'), fent que getColor saltés sempre al tercer color de la
        // paleta i el primer (i el segon) mai es mostressin a la previsualització.
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic gauge"
                tipusGrafic="GAUGE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        const gauge = screen.getByTestId('gauge-chart');
        expect(gauge).toHaveAttribute('data-value-arc-fill', '#1f77b4');
    });

    it('GraficWidgetVisualization_quanEsGaugeAmbDosIndicadorsIModeNumeric_elMaximVeDelSegonIndicador', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic gauge"
                tipusGrafic="GAUGE_CHART"
                tipusValors="NUMERIC"
                dades={[{ value: 40, max: 200 }]}
                mostrarVora={false}
                ampleVora={1}
            />
        );

        const gauge = screen.getByTestId('gauge-chart');
        expect(gauge).toHaveAttribute('data-value', '40');
        expect(gauge).toHaveAttribute('data-value-max', '200');
    });

    it('GraficWidgetVisualization_quanEsGaugeAmbDosIndicadorsIModePercentatge_calculaElPercentatge', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic gauge"
                tipusGrafic="GAUGE_CHART"
                tipusValors="PERCENTAGE"
                dades={[{ value: 40, max: 200 }]}
                mostrarVora={false}
                ampleVora={1}
            />
        );

        const gauge = screen.getByTestId('gauge-chart');
        expect(gauge).toHaveAttribute('data-value', '20');
        expect(gauge).toHaveAttribute('data-value-min', '0');
        expect(gauge).toHaveAttribute('data-value-max', '100');
    });

    it('GraficWidgetVisualization_quanEsGaugePercentatgeSenseMaxim_noDividiuPerZeroIMostraZero', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic gauge"
                tipusGrafic="GAUGE_CHART"
                tipusValors="PERCENTAGE"
                dades={[{ value: 40 }]}
                mostrarVora={false}
                ampleVora={1}
            />
        );

        const gauge = screen.getByTestId('gauge-chart');
        expect(gauge).toHaveAttribute('data-value', '0');
    });

    it('GraficWidgetVisualization_quanEsRenderitzaEnModeSparkLine_mostraElSparkLine', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic sparkline"
                tipusGrafic="SPARK_LINE_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByTestId('spark-line-chart')).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanEsRenderitzaEnModeScatter_mostraMissatgePendent', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic scatter"
                tipusGrafic="SCATTER_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByText(/Scatter Chart \(Pendent d'implementació\)/)).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanEsRenderitzaEnModeHeatmap_mostraMissatgePendent', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic heatmap"
                tipusGrafic="HEATMAP_CHART"
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.getByText(/Heatmap Chart \(Pendent d'implementació\)/)).toBeInTheDocument();
    });

    it('GraficWidgetVisualization_quanEstaEnModeLoading_mostraSkeleton', () => {
        renderComponent(
            <GraficWidgetVisualization
                titol="Gràfic carregant"
                tipusGrafic="BAR_CHART"
                loading={true}
                mostrarVora={false}
                ampleVora={1}
            />
        );

        expect(screen.queryByText('Gràfic carregant')).not.toBeInTheDocument();
        expect(screen.queryByTestId('bar-chart')).not.toBeInTheDocument();
    });

});
