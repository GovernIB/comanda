import { fireEvent, render, screen, within } from '@testing-library/react';
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
                        help: {
                            dimensioAgrupacio: 'Ajuda agrupació',
                        },
                    },
                    taula: {
                        tableCols: 'Columnes de taula',
                        amagarFilesZero: 'Amagar files a zero',
                        senseOrdenacio: 'Sense ordenacio',
                        direccioOrdenacio: 'Direccio',
                        limitResultats: 'Limit resultats',
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
                plantilla: {
                    sample: {
                        tableDescription: 'Descripcio de la taula',
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
    FieldHelp: ({ text }: { text: string }) => <div data-testid="field-help">{text}</div>,
}));

vi.mock('./WidgetPreview', () => ({
    WidgetPreview: ({ widgetType, widgetData }: { widgetType: string; widgetData: any }) => (
        <div data-testid="widget-preview">
            {widgetType} - {widgetData.descripcio}
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
        expect(screen.getByTestId('widget-preview')).toHaveTextContent('TAULA - Descripcio de la taula');
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

    it('EstadisticaTaulaWidgetForm_senseColumnaOrdenacio_noMostraDireccioNiLimit', () => {
        // Sense cap columna d'ordenació triada, els camps de direcció i límit no s'han de mostrar.
        mocks.useFormContextMock.mockReturnValue({
            data: { columnes: [{ titol: 'Visites' }, { titol: 'Sessions' }] },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        expect(screen.getByText('Sense ordenacio')).toBeInTheDocument();
        expect(screen.queryByTestId('field-direccioOrdenacio')).not.toBeInTheDocument();
        expect(screen.queryByTestId('field-limitResultats')).not.toBeInTheDocument();
    });

    it('EstadisticaTaulaWidgetForm_enTriarUnaColumnaDOrdenacio_mostraDireccioILimitIEstableixDescPerDefecte', () => {
        // En triar una columna per ordenar, apareixen direcció i límit, i la direcció es fixa a DESC si no n'hi havia cap.
        mocks.useFormContextMock.mockReturnValue({
            data: { columnes: [{ titol: 'Visites' }, { titol: 'Sessions' }] },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        fireEvent.mouseDown(screen.getByRole('combobox'));
        const listbox = screen.getByRole('listbox');
        fireEvent.click(within(listbox).getByText('Sessions'));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('columnaOrdenacio', 1);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('direccioOrdenacio', 'DESC');
    });

    it('EstadisticaTaulaWidgetForm_ambColumnaOrdenacioTriada_mostraDireccioILimit', () => {
        mocks.useFormContextMock.mockReturnValue({
            data: { columnes: [{ titol: 'Visites' }], columnaOrdenacio: 0, direccioOrdenacio: 'ASC' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        expect(screen.getByTestId('field-direccioOrdenacio')).toBeInTheDocument();
        expect(screen.getByTestId('field-limitResultats')).toBeInTheDocument();
    });

    it('EstadisticaTaulaWidgetForm_enTreureLaColumnaDOrdenacio_esbrraDireccioILimit', () => {
        // En tornar a "Sense ordenació" s'han de netejar direcció i límit, ja que sense columna no tenen sentit.
        mocks.useFormContextMock.mockReturnValue({
            data: { columnes: [{ titol: 'Visites' }], columnaOrdenacio: 0, direccioOrdenacio: 'ASC', limitResultats: 5 },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        });

        render(<EstadisticaTaulaWidgetForm />);

        fireEvent.mouseDown(screen.getByRole('combobox'));
        const listbox = screen.getByRole('listbox');
        fireEvent.click(within(listbox).getByText('Sense ordenacio'));

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('columnaOrdenacio', null);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('direccioOrdenacio', null);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('limitResultats', null);
    });
});
