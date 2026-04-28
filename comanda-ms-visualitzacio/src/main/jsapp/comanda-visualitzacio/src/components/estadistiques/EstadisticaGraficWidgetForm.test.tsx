import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaGraficWidgetForm from './EstadisticaGraficWidgetForm';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    form: {
                        grafic: 'Widget gràfic',
                        configVisual: 'Configuració visual',
                        preview: 'Previsualització',
                        configGeneral: 'Configuració general',
                        graficBar: 'Config barres',
                        graficLin: 'Config línies',
                        configFont: 'Configuració de fonts',
                    },
                    atributsVisuals: {
                        colorText: 'Color text',
                        colorFons: 'Color fons',
                        mostrarVora: 'Mostrar vora',
                        colorVora: 'Color vora',
                        ampleVora: 'Ample vora',
                        mostrarReticula: 'Mostrar retícula',
                        barStacked: 'Apilar barres',
                        barHorizontal: 'Barres horitzontals',
                        lineShowPoints: 'Mostrar punts',
                        area: 'Àrea',
                        lineSmooth: 'Línia suau',
                        lineWidth: 'Ample línia',
                        midaFontTitol: 'Mida títol',
                        midaFontDescripcio: 'Mida descripció',
                    },
                    grafic: {
                        indicadors: 'Indicadors',
                    },
                },
            },
        })
    ),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, disabled }: { name: string; disabled?: boolean }) => (
        <div data-testid={`field-${name}`} data-disabled={disabled ? 'true' : 'false'}>
            {name}
        </div>
    ),
    useFormContext: () => mocks.useFormContextMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('./EstadisticaWidgetFormFields', () => ({
    default: ({ children }: { children: React.ReactNode }) => <div data-testid="widget-form-fields">{children}</div>,
}));

vi.mock('./GraficWidgetVisualization', () => ({
    default: (props: Record<string, unknown>) => <div data-testid="grafic-preview">{String(props.tipusGrafic)}</div>,
}));

vi.mock('./VisualAttributesPanel', () => ({
    default: ({ title, children }: { title: string; children: React.ReactNode }) => (
        <div data-testid="visual-panel">
            <div>{title}</div>
            {children}
        </div>
    ),
}));

vi.mock('../ColorPaletteSelector', () => ({
    default: () => <div data-testid="color-palette-selector">palette</div>,
}));

vi.mock('./ColumnesTable.tsx', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`columnes-table-${name}`}>{name}</div>,
}));

vi.mock('../FormFieldCustomAdvancedSearch', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`advanced-search-${name}`}>{name}</div>,
}));

describe('EstadisticaGraficWidgetForm', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaGraficWidgetForm_quanEsMuntaSenseLineWidth_inicialitzaElValorPerDefecte', () => {
        // Comprova que el formulari fixa `lineWidth` a 2 la primera vegada si no hi havia valor.
        mocks.useFormContextMock.mockReturnValue({
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaGraficWidgetForm />);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('lineWidth', 2);
    });

    it('EstadisticaGraficWidgetForm_quanEsBarChartAmbUnIndicador_mostraElsCampsEspecifics', () => {
        // Verifica que el mode de gràfic de barres exposa els camps de tipus i configuració propis.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                tipusGrafic: 'BAR_CHART',
                tipusDades: 'UN_INDICADOR',
                mostrarVora: true,
                agregacio: 'SUM',
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaGraficWidgetForm />);

        expect(screen.getByText('Widget gràfic')).toBeInTheDocument();
        expect(screen.getByTestId('advanced-search-indicador')).toBeInTheDocument();
        expect(screen.getByTestId('field-barStacked')).toBeInTheDocument();
        expect(screen.getByTestId('field-barHorizontal')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorVora')).toBeInTheDocument();
        expect(screen.getByTestId('field-unitatAgregacio')).toHaveAttribute('data-disabled', 'true');
        expect(screen.getByTestId('grafic-preview')).toHaveTextContent('BAR_CHART');
    });

    it('EstadisticaGraficWidgetForm_quanEsVarisIndicadors_mostraLaTaulaDIndicadors', () => {
        // Comprova que el mode de diversos indicadors delega la configuració a `ColumnesTable`.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                tipusGrafic: 'LINE_CHART',
                tipusDades: 'VARIS_INDICADORS',
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaGraficWidgetForm />);

        expect(screen.getByTestId('columnes-table-indicadorsInfo')).toBeInTheDocument();
        expect(screen.getByTestId('color-palette-selector')).toBeInTheDocument();
    });
});
