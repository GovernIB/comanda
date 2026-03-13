import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { afterEach, describe, expect, it, vi } from 'vitest';
import SimpleWidgetVisualization from './SimpleWidgetVisualization';

const mocks = vi.hoisted(() => ({
    useBaseAppContextMock: vi.fn(),
    numberFormatMock: vi.fn((value: number, _options: object, _language?: string) => `fmt:${value}`),
}));

vi.mock('reactlib', () => ({
    useBaseAppContext: () => mocks.useBaseAppContextMock(),
    numberFormat: (value: number, options: object, language?: string) =>
        mocks.numberFormatMock(value, options, language),
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('SimpleWidgetVisualization', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
        });
    });

    it('SimpleWidgetVisualization_quanEsRenderitzaSenseError_mostraValorDescripcioIIcona', () => {
        // Comprova que el component mostra les dades principals i formata els valors numèrics.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
        });

        renderComponent(
            <SimpleWidgetVisualization
                titol="Peticions"
                entornCodi="PRO"
                valor={42}
                unitat="ms"
                descripcio="Latència mitjana"
                canviPercentual="12"
                icona="CheckCircle"
            />
        );

        expect(screen.getByText('Peticions')).toBeInTheDocument();
        expect(screen.getByText('PRO')).toBeInTheDocument();
        expect(screen.getByText('fmt:42')).toBeInTheDocument();
        expect(screen.getByText('Latència mitjana')).toBeInTheDocument();
        expect(screen.getByText('12%')).toBeInTheDocument();
        expect(screen.getByText('check_circle')).toBeInTheDocument();
    });

    it('SimpleWidgetVisualization_quanHiHaError_mostraLacordioAmbElDetall', () => {
        // Verifica que el component substitueix el contingut normal per l'estat d'error expandible.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
        });

        renderComponent(
            <SimpleWidgetVisualization
                titol="Peticions"
                error={true}
                errorMsg="No s'han pogut carregar les dades"
                errorTrace="Traça tècnica"
            />
        );

        expect(screen.getByText("No s'han pogut carregar les dades")).toBeInTheDocument();
        expect(screen.getByText('Traça tècnica')).toBeInTheDocument();
    });

    it('SimpleWidgetVisualization_quanRepOnClick_invocaElCallbackEnClicar', () => {
        // Comprova que el contenidor principal propaga el clic quan rep una acció.
        mocks.useBaseAppContextMock.mockReturnValue({
            currentLanguage: 'ca',
        });
        const onClick = vi.fn();

        renderComponent(
            <SimpleWidgetVisualization
                titol="Peticions"
                valor={7}
                onClick={onClick}
            />
        );

        fireEvent.click(screen.getByText('Peticions'));

        expect(onClick).toHaveBeenCalledTimes(1);
    });
});
