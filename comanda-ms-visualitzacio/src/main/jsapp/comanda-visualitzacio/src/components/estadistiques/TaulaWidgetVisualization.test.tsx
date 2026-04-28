import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import TaulaWidgetVisualization from './TaulaWidgetVisualization';

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('TaulaWidgetVisualization', () => {
    it('TaulaWidgetVisualization_quanEsRenderitza_mostraCapcaleraFilesIDescripcio', () => {
        // Comprova que el component renderitza la taula, la capçalera i el peu descriptiu.
        renderComponent(
            <TaulaWidgetVisualization
                titol="Resum"
                descripcio="Dades agregades"
                entornCodi="PRO"
                columnes={[
                    { id: 'name', label: 'Nom' },
                    { id: 'valor', label: 'Valor' },
                ]}
                files={[
                    { name: 'Fila A', dimensio: 'fila-a', valor: 12 },
                    { name: 'Fila B', dimensio: 'fila-b', valor: 24 },
                ]}
            />
        );

        expect(screen.getByText('Resum')).toBeInTheDocument();
        expect(screen.getByText('PRO')).toBeInTheDocument();
        expect(screen.getByText('Nom')).toBeInTheDocument();
        expect(screen.getByText('Valor')).toBeInTheDocument();
        expect(screen.getByText('Fila A')).toBeInTheDocument();
        expect(screen.getByText('24')).toBeInTheDocument();
        expect(screen.getByText('Dades agregades')).toBeInTheDocument();
    });

    it('TaulaWidgetVisualization_quanHiHaCellaDestacada_mostraLesIconesConfigurades', () => {
        // Verifica que el component injecta icones prefix i sufix quan una cel·la està destacada.
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[
                    { id: 'name', label: 'Nom' },
                    { id: 'valor', label: 'Valor' },
                ]}
                files={[{ name: 'Fila A', dimensio: 'fila-a', valor: 12 }]}
                cellesDestacades={[
                    {
                        codiColumna: 'valor',
                        valorDimensio: 'fila-a',
                        iconaPrefix: 'arrow_upward',
                        iconaSufix: 'check_circle',
                    },
                ]}
            />
        );

        expect(screen.getByText('arrow_upward')).toBeInTheDocument();
        expect(screen.getByText('check_circle')).toBeInTheDocument();
        expect(screen.getByText('12')).toBeInTheDocument();
    });

    it('TaulaWidgetVisualization_quanHiHaError_mostraLEstatDerror', () => {
        // Comprova que el component reemplaça la taula pel bloc d'error quan la càrrega falla.
        renderComponent(
            <TaulaWidgetVisualization
                error={true}
                errorMsg="Error de dades"
                errorTrace="Traça taula"
            />
        );

        expect(screen.getByText('Error de dades')).toBeInTheDocument();
        expect(screen.getByText('Traça taula')).toBeInTheDocument();
    });

    it('TaulaWidgetVisualization_quanRepOnClick_invocaElCallbackEnClicar', () => {
        // Verifica que el contenidor principal pot actuar com a element clicable.
        const onClick = vi.fn();

        renderComponent(
            <TaulaWidgetVisualization
                titol="Resum"
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
                onClick={onClick}
            />
        );

        fireEvent.click(screen.getByText('Resum'));

        expect(onClick).toHaveBeenCalledTimes(1);
    });
});
