import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import GraficWidgetVisualization from './GraficWidgetVisualization';

vi.mock('@mui/x-charts', () => ({
    BarChart: () => <div data-testid="bar-chart">BarChart</div>,
    LineChart: () => <div data-testid="line-chart">LineChart</div>,
    PieChart: () => <div data-testid="pie-chart">PieChart</div>,
    ChartsTooltip: () => <div data-testid="charts-tooltip">Tooltip</div>,
    ChartsLegend: () => <div data-testid="charts-legend">Legend</div>,
    SparkLineChart: () => <div data-testid="spark-line-chart">SparkLine</div>,
    Gauge: () => <div data-testid="gauge-chart">Gauge</div>,
    XAxis: () => null,
    YAxis: () => null,
    LineSeries: () => null,
    gaugeClasses: { valueArc: 'valueArc', referenceArc: 'referenceArc' },
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
});
