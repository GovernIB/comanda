import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { afterEach, describe, expect, it, vi } from 'vitest';
import VisualAttributesPanel from './VisualAttributesPanel';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    FormField: ({
        name,
        disabled,
        type,
    }: {
        name: string;
        disabled?: boolean;
        type?: string;
    }) => (
        <div data-testid={`form-field-${name}`} data-disabled={disabled ? 'true' : 'false'} data-type={type ?? ''}>
            {name}
        </div>
    ),
    useFormContext: () => mocks.useFormContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (_selector: unknown) => 'translated-label',
    }),
}));

vi.mock('../IconAutocompleteSelect.tsx', () => ({
    default: ({ name, label }: { name: string; label: string }) => (
        <div data-testid={`icon-select-${name}`}>{label}</div>
    ),
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('VisualAttributesPanel', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('VisualAttributesPanel_quanRepChildren_renderitzaElContingutPersonalitzat', () => {
        // Comprova que el panell prioritza el contingut fill en lloc dels camps per defecte.
        mocks.useFormContextMock.mockReturnValue({ data: {} });

        renderComponent(
            <VisualAttributesPanel title="Atributs visuals" widgetType="simple">
                <div>Contingut injectat</div>
            </VisualAttributesPanel>
        );

        expect(screen.getByText('Atributs visuals')).toBeInTheDocument();
        expect(screen.getByText('Contingut injectat')).toBeInTheDocument();
        expect(screen.queryByTestId('icon-select-atributsVisuals.icona')).not.toBeInTheDocument();
    });

    it('VisualAttributesPanel_quanEsDeTipusSimple_renderitzaElsCampsBasicsDelWidget', () => {
        // Verifica que el mode simple exposa els camps de colors, icona i vora.
        mocks.useFormContextMock.mockReturnValue({ data: {} });

        renderComponent(<VisualAttributesPanel widgetType="simple" />);

        expect(screen.getByTestId('icon-select-atributsVisuals.icona')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.colorText')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.colorFons')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.vora')).toBeInTheDocument();
    });

    it('VisualAttributesPanel_quanEsDeTipusGrafic_renderitzaElsCampsPropisDelGrafic', () => {
        // Comprova que el mode gràfic mostra les opcions específiques de paleta, línies i gauge.
        mocks.useFormContextMock.mockReturnValue({ data: {} });

        renderComponent(<VisualAttributesPanel widgetType="grafic" />);

        expect(screen.getByTestId('form-field-atributsVisuals.colorsPaleta')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.lineWidth')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.gaugeMax')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.heatmapMaxValue')).toBeInTheDocument();
    });

    it('VisualAttributesPanel_quanLaTaulaNoMostraAlternancia_desactivaElColorDAlternancia', () => {
        // Verifica que el camp del color d'alternança queda deshabilitat si l'opció no està marcada.
        mocks.useFormContextMock.mockReturnValue({
            data: { atributsVisuals: { mostrarAlternancia: false } },
        });

        renderComponent(<VisualAttributesPanel widgetType="taula" />);

        expect(screen.getByTestId('form-field-atributsVisuals.mostrarAlternancia')).toBeInTheDocument();
        expect(screen.getByTestId('form-field-atributsVisuals.colorAlternancia')).toHaveAttribute(
            'data-disabled',
            'true'
        );
        expect(screen.getByText('Estils de columnes')).toBeInTheDocument();
    });
});
