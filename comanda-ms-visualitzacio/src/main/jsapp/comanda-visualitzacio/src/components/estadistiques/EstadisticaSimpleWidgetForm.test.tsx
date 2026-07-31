import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaSimpleWidgetForm, { hasVisualOverrides } from './EstadisticaSimpleWidgetForm';

const mocks = vi.hoisted(() => ({
    useFormContextMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    form: {
                        simple: 'Widget simple',
                        configVisual: 'Configuració visual',
                        preview: 'Previsualització',
                        configGeneral: 'Configuració general',
                        configFont: 'Configuració de fonts',
                        help: {
                            tipusIndicador: 'Ajuda tipus indicador',
                            periodeIndicador: 'Ajuda període indicador',
                        },
                    },
                    atributsVisuals: {
                        colorText: 'Color text',
                        colorFons: 'Color fons',
                        icona: 'Icona',
                        colorIcona: 'Color icona',
                        colorFonsIcona: 'Color fons icona',
                        colorTextDestacat: 'Color destacat',
                        mostrarVora: 'Mostrar vora',
                        colorVora: 'Color vora',
                        ampleVora: 'Ample vora',
                        midaFontTitol: 'Mida títol',
                        midaFontDescripcio: 'Mida descripció',
                        midaFontValor: 'Mida valor',
                        midaFontUnitats: 'Mida unitats',
                        midaFontCanviPercentual: 'Mida canvi',
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
    FieldHelp: ({ text }: { text: string }) => <div data-testid="field-help">{text}</div>,
}));

vi.mock('./WidgetPreview', () => ({
    WidgetPreview: ({ widgetType, widgetData }: { widgetType: string; widgetData: any }) => (
        <div data-testid="widget-preview">
            {widgetType} - {widgetData.titol}
        </div>
    ),
}));

vi.mock('./VisualAttributesPanel', () => ({
    default: ({ title, children }: { title: string; children: React.ReactNode }) => (
        <div data-testid="visual-panel">
            <div>{title}</div>
            {children}
        </div>
    ),
}));

vi.mock('../IconAutocompleteSelect', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`icon-select-${name}`}>{name}</div>,
}));

vi.mock('../FormFieldCustomAdvancedSearch', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`advanced-search-${name}`}>{name}</div>,
}));

describe('EstadisticaSimpleWidgetForm', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaSimpleWidgetForm_quanEsRenderitza_mostraElsBlocsPrincipalsIElPreview', () => {
        // Comprova que el formulari simple renderitza el bloc de camps i la previsualització lateral.
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 7 }, titol: 'Resum principal', tipusIndicador: 'SUM' },
        });

        render(<EstadisticaSimpleWidgetForm />);

        expect(screen.getByTestId('widget-form-fields')).toBeInTheDocument();
        expect(screen.getByText('Widget simple')).toBeInTheDocument();
        expect(screen.getByTestId('advanced-search-indicador')).toBeInTheDocument();
        expect(screen.getByTestId('widget-preview')).toHaveTextContent('SIMPLE - Resum principal');
        expect(screen.getByTestId('visual-panel')).toBeInTheDocument();
    });

    it('EstadisticaSimpleWidgetForm_quanHiHaIconaIVora_mostraElsCampsCondicionals', () => {
        // Verifica que amb icona i vora activades apareixen els camps visuals addicionals.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                icona: 'Add',
                mostrarVora: true,
                tipusIndicador: 'AVERAGE',
            },
        });

        render(<EstadisticaSimpleWidgetForm />);

        expect(screen.getByTestId('field-colorIcona')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorFonsIcona')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorVora')).toBeInTheDocument();
        expect(screen.getByTestId('field-ampleVora')).toBeInTheDocument();
        expect(screen.getByTestId('field-periodeIndicador')).toHaveAttribute('data-disabled', 'false');
    });
});

describe('hasVisualOverrides', () => {
    it('hasVisualOverrides_quanNoHiHaCapCampVisualEmplenat_retornaFals', () => {
        // Reprodueix el bug reportat: sense cap camp visual emplenat, no s'ha de considerar personalitzat.
        expect(hasVisualOverrides({ aplicacio: { id: 7 }, titol: 'Resum', indicador: { id: 1 } })).toBe(false);
    });

    it('hasVisualOverrides_quanEsSeleccionaUnaIcona_retornaCert', () => {
        // Triar una icona s'ha de considerar una personalització del widget.
        expect(hasVisualOverrides({ icona: 'Add' })).toBe(true);
    });

    it('hasVisualOverrides_quanHiHaUnColorEmplenat_retornaCert', () => {
        expect(hasVisualOverrides({ colorText: '#ff0000' })).toBe(true);
    });
});
