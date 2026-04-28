import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaSimpleWidgetForm from './EstadisticaSimpleWidgetForm';

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
}));

vi.mock('./SimpleWidgetVisualization', () => ({
    default: (props: Record<string, unknown>) => <div data-testid="simple-preview">{String(props.titol)}</div>,
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
        expect(screen.getByTestId('simple-preview')).toHaveTextContent('Resum principal');
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
