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

    it('TaulaWidgetVisualization_quanEstaEnModeLoading_mostraSkeletons', () => {
        renderComponent(
            <TaulaWidgetVisualization
                titol="Resum"
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
                loading={true}
            />
        );

        expect(screen.queryByText('Resum')).not.toBeInTheDocument();
        expect(screen.queryByText('Nom')).not.toBeInTheDocument();
        expect(screen.queryByText('Fila A')).not.toBeInTheDocument();
    });

    it('TaulaWidgetVisualization_quanMostrarCapcaleraEsFals_noRenderitzaLaCapcalera', () => {
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
                mostrarCapcalera={false}
            />
        );

        expect(screen.queryByText('Nom')).not.toBeInTheDocument();
        expect(screen.getByText('Fila A')).toBeInTheDocument();
    });

    it('TaulaWidgetVisualization_quanColumnaTeFormat_aplicaElFormat', () => {
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[
                    { id: 'name', label: 'Nom' },
                    { id: 'valor', label: 'Valor', format: (v) => `${v}€` },
                ]}
                files={[{ name: 'Fila A', dimensio: 'fila-a', valor: 42 }]}
            />
        );

        expect(screen.getByText('42€')).toBeInTheDocument();
    });

    it('TaulaWidgetVisualization_quanNoTeFiles_noRenderitzaFiles', () => {
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[]}
            />
        );

        expect(screen.getByText('Nom')).toBeInTheDocument();
        const rows = screen.queryAllByRole('row');
        expect(rows.length).toBe(1);
    });

    it('TaulaWidgetVisualization_quanTeMidaFontTitolIDescripcio_lesAplicaAlTitolIALaDescripcio', () => {
        // Regressió: midaFontTitol/midaFontDescripcio es declaraven a les props però no s'aplicaven mai.
        renderComponent(
            <TaulaWidgetVisualization
                titol="Resum"
                descripcio="Dades agregades"
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
                midaFontTitol={28}
                midaFontDescripcio={18}
            />
        );

        expect(screen.getByText('Resum')).toHaveStyle({ fontSize: '28px' });
        expect(screen.getByText('Dades agregades')).toHaveStyle({ fontSize: '18px' });
    });

    it('TaulaWidgetVisualization_quanTeMidaFontTaula_LAplicaALaCapcaleraIALesFiles', () => {
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
                midaFontTaula={20}
            />
        );

        expect(screen.getByText('Nom')).toHaveStyle({ fontSize: '20px' });
        expect(screen.getByText('Fila A')).toHaveStyle({ fontSize: '20px' });
    });

    it('TaulaWidgetVisualization_quanCompacteEsTrue_usaLaMidaCompactaDeLaTaula', () => {
        // La versió compacta ha d'usar la mida "small" de MUI (marges de cel·la menors).
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
                compacte={true}
            />
        );

        expect(screen.getByText('Fila A').closest('td')).toHaveClass('MuiTableCell-sizeSmall');
    });

    it('TaulaWidgetVisualization_quanCompacteEsFalsIPreviewEsFals_usaLaMidaEstandard', () => {
        renderComponent(
            <TaulaWidgetVisualization
                columnes={[{ id: 'name', label: 'Nom' }]}
                files={[{ name: 'Fila A', dimensio: 'fila-a' }]}
            />
        );

        expect(screen.getByText('Fila A').closest('td')).toHaveClass('MuiTableCell-sizeMedium');
    });

    it('TaulaWidgetVisualization_quanUsaDadesPerDefecte_renderitzaCorrectament', () => {
        renderComponent(
            <TaulaWidgetVisualization
                titol="Taula per defecte"
            />
        );

        expect(screen.getByText('Taula per defecte')).toBeInTheDocument();
        expect(screen.getByText('Nom')).toBeInTheDocument();
        expect(screen.getByText('Valor 1')).toBeInTheDocument();
    });
});
