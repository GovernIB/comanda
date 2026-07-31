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
                        help: {
                            tipusDades: 'Ajuda tipus dades',
                            tempsAgrupacio: 'Ajuda temps agrupació',
                            agregacio: 'Ajuda agregació',
                            unitatAgregacio: 'Ajuda unitat agregació',
                            agruparPerDimensioDescomposicio: 'Ajuda agrupar per dimensió',
                        },
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
    FormField: ({ name, disabled, required }: { name: string; disabled?: boolean; required?: boolean }) => (
        <div
            data-testid={`field-${name}`}
            data-disabled={disabled ? 'true' : 'false'}
            data-required={required ? 'true' : 'false'}
        >
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
        <div data-testid="widget-preview" data-colors-paleta={widgetData.colorsPaleta ?? ''}>
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

vi.mock('../ColorPaletteSelector', () => ({
    default: () => <div data-testid="color-palette-selector">palette</div>,
}));

vi.mock('./ColumnesTable.tsx', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`columnes-table-${name}`}>{name}</div>,
}));

vi.mock('../FormFieldCustomAdvancedSearch', () => ({
    default: ({ name, required }: { name: string; required?: boolean }) => (
        <div data-testid={`advanced-search-${name}`} data-required={required ? 'true' : 'false'}>
            {name}
        </div>
    ),
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
        expect(screen.getByTestId('widget-preview')).toHaveTextContent('GRAFIC - Títol del gràfic');
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

    it('EstadisticaGraficWidgetForm_quanNoHiHaColorsPaletaPropia_noEnviaCapValorPerDefecteALaPrevisualitzacio', () => {
        // La previsualització ha de poder aplicar els colors de la plantilla: si aquí s'enviàs un
        // valor per defecte, resolveWidgetStyles el consideraria una sobreescriptura i mai es veurien
        // els colors de la plantilla al gràfic (bug reportat: "no mostra correctament els colors").
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 7 }, tipusGrafic: 'BAR_CHART' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaGraficWidgetForm />);

        expect(screen.getByTestId('widget-preview')).toHaveAttribute('data-colors-paleta', '');
    });

    it('EstadisticaGraficWidgetForm_quanEsUnIndicadorAmbDescomposicio_ordenaCampsEn3FilesIMarcaElsObligatoris', () => {
        // Reprodueix l'ordre demanat: 1a fila (tipus gràfic, tipus dades), 2a fila (indicador, títol,
        // agregació, unitat), 3a fila (temps agrupació, checkbox, dimensió de descomposició). A més,
        // els camps que el backend només valida via el validador de creuament de camps (no @NotNull al
        // model) han de mostrar-se igualment com a obligatoris quan calgui.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                tipusGrafic: 'LINE_CHART',
                tipusDades: 'UN_INDICADOR_AMB_DESCOMPOSICIO',
                agregacio: 'SUM',
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        const { container } = render(<EstadisticaGraficWidgetForm />);

        const relevantTestIds = new Set([
            'field-tipusGrafic',
            'field-tipusDades',
            'advanced-search-indicador',
            'field-titolIndicador',
            'field-agregacio',
            'field-unitatAgregacio',
            'field-tempsAgrupacio',
            'field-agruparPerDimensioDescomposicio',
            'field-descomposicioDimensio',
        ]);
        const order = Array.from(container.querySelectorAll('[data-testid]'))
            .map(el => el.getAttribute('data-testid'))
            .filter((id): id is string => !!id && relevantTestIds.has(id));
        expect(order).toEqual([
            'field-tipusGrafic',
            'field-tipusDades',
            'advanced-search-indicador',
            'field-titolIndicador',
            'field-agregacio',
            'field-unitatAgregacio',
            'field-tempsAgrupacio',
            'field-agruparPerDimensioDescomposicio',
            'field-descomposicioDimensio',
        ]);

        expect(screen.getByTestId('field-tipusGrafic')).toHaveAttribute('data-required', 'true');
        expect(screen.getByTestId('field-tipusDades')).toHaveAttribute('data-required', 'true');
        expect(screen.getByTestId('advanced-search-indicador')).toHaveAttribute('data-required', 'true');
        expect(screen.getByTestId('field-titolIndicador')).toHaveAttribute('data-required', 'false');
        expect(screen.getByTestId('field-agregacio')).toHaveAttribute('data-required', 'true');
        expect(screen.getByTestId('field-unitatAgregacio')).toHaveAttribute('data-required', 'false');
        expect(screen.getByTestId('field-tempsAgrupacio')).toHaveAttribute('data-required', 'true');
        expect(screen.getByTestId('field-descomposicioDimensio')).toHaveAttribute('data-required', 'true');
    });

    it('EstadisticaGraficWidgetForm_quanSActivaAgruparPerDimensio_tempsAgrupacioDeixaDeSerObligatoriIUnitatSiEsAverage', () => {
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 7 },
                tipusGrafic: 'LINE_CHART',
                tipusDades: 'UN_INDICADOR_AMB_DESCOMPOSICIO',
                agregacio: 'AVERAGE',
                agruparPerDimensioDescomposicio: true,
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaGraficWidgetForm />);

        expect(screen.getByTestId('field-unitatAgregacio')).toHaveAttribute('data-required', 'true');
        expect(screen.getByTestId('field-tempsAgrupacio')).toHaveAttribute('data-required', 'false');
        expect(screen.getByTestId('field-tempsAgrupacio')).toHaveAttribute('data-disabled', 'true');
    });
});
