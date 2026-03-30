import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaTaulaWidgetForm from './EstadisticaTaulaWidgetForm';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    form: {
                        taula: 'Widget taula',
                        configVisual: 'Configuració visual',
                        preview: 'Previsualització',
                        configGeneral: 'Configuració general',
                        configTaula: 'Configuració de taula',
                    },
                    taula: {
                        tableCols: 'Columnes de taula',
                    },
                    atributsVisuals: {
                        colorText: 'Color text',
                        colorFons: 'Color fons',
                        mostrarVora: 'Mostrar vora',
                        colorVora: 'Color vora',
                        ampleVora: 'Ample vora',
                        colorTextTaula: 'Color text taula',
                        colorFonsTaula: 'Color fons taula',
                        mostrarCapcalera: 'Mostrar capçalera',
                        colorCapcalera: 'Color capçalera',
                        colorFonsCapcalera: 'Color fons capçalera',
                        mostrarAlternancia: 'Mostrar alternança',
                        colorAlternancia: 'Color alternança',
                        mostrarVoraTaula: 'Mostrar vora taula',
                        colorVoraTaula: 'Color vora taula',
                        ampleVoraTaula: 'Ample vora taula',
                    },
                },
            },
        })
    ),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    useFormContext: () => mocks.useFormContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('./EstadisticaWidgetFormFields', () => ({
    default: ({ children }: { children: React.ReactNode }) => <div data-testid="widget-form-fields">{children}</div>,
}));

vi.mock('./TaulaWidgetVisualization', () => ({
    default: () => <div data-testid="taula-preview">preview</div>,
}));

vi.mock('./VisualAttributesPanel', () => ({
    default: ({ title, children }: { title: string; children: React.ReactNode }) => (
        <div data-testid="visual-panel">
            <div>{title}</div>
            {children}
        </div>
    ),
}));

vi.mock('./ColumnesTable', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`columnes-table-${name}`}>{name}</div>,
}));

describe('EstadisticaTaulaWidgetForm', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaTaulaWidgetForm_quanEsMuntaSenseDefaults_estableixElsCheckboxosInicials', () => {
        // Comprova que el formulari inicialitza els defaults de capçalera i separador horitzontal.
        mocks.useFormContextMock.mockReturnValue({
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('mostrarCapcalera', true);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('mostrarSeparadorHoritzontal', true);
    });

    it('EstadisticaTaulaWidgetForm_quanEsRenderitza_mostraLaTaulaDeColumnesIElPreview', () => {
        // Verifica que el formulari de taula renderitza el configurador de columnes i la previsualització lateral.
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 7 }, columnes: [{ titol: 'Columna 1' }] },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        expect(screen.getByText('Widget taula')).toBeInTheDocument();
        expect(screen.getByTestId('columnes-table-columnes')).toBeInTheDocument();
        expect(screen.getByTestId('taula-preview')).toBeInTheDocument();
        expect(screen.getByTestId('visual-panel')).toBeInTheDocument();
    });

    it('EstadisticaTaulaWidgetForm_quanHiHaOpcionsActivades_mostraElsCampsVisualsCondicionals', () => {
        // Comprova que amb les opcions activades apareixen els camps condicionals de capçalera, alternança i vores.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                mostrarVora: true,
                mostrarCapcalera: true,
                mostrarAlternancia: true,
                mostrarVoraTaula: true,
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        expect(screen.getByTestId('field-colorVora')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorCapcalera')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorAlternancia')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorVoraTaula')).toBeInTheDocument();
    });
});
