import { useEffect } from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { WidgetCreationWizard } from './WidgetCreationWizard';

const mocks = vi.hoisted(() => ({
    temporalMessageShowMock: vi.fn(),
    createDashboardItemMock: vi.fn(),
    createDashboardTitolMock: vi.fn(),
    hasVisualOverridesTitolMock: vi.fn(),
    useDashboardPlantillaMock: vi.fn(),
    useFormContextMock: vi.fn(),
    translations: {
        page: {
            widget: {
                form: {
                    preview: 'Previsualització',
                },
                wizard: {
                    title: 'Crear un component nou',
                    steps: {
                        type: 'Tipus',
                        content: 'Contingut',
                        indicators: 'Indicadors',
                        dimensions: 'Dimensions',
                        period: 'Període',
                        visual: 'Visualització',
                    },
                    types: {
                        titol: { label: 'Títol', description: 'Descripció títol' },
                        simple: { label: 'Simple', description: 'Descripció simple' },
                        grafic: { label: 'Gràfic', description: 'Descripció gràfic' },
                        taula: { label: 'Taula', description: 'Descripció taula' },
                    },
                    help: {
                        type: 'Ajuda tipus',
                        appEntorn: 'Ajuda app entorn',
                        content: 'Ajuda contingut',
                        indicators: 'Ajuda indicadors',
                        dimensions: 'Ajuda dimensions',
                        period: 'Ajuda període',
                        visual: 'Ajuda visualització',
                    },
                    visual: {
                        personalitzat: 'Personalitzat',
                        personalitzatHelp: 'Ajuda personalitzat',
                    },
                    actions: {
                        cancel: 'Cancel·lar',
                        back: 'Enrere',
                        next: 'Següent',
                        finish: 'Crear',
                        validationError: 'Hi ha camps amb errors',
                    },
                },
            },
            dashboards: {
                action: {
                    addWidget: {
                        success: 'Widget afegit correctament',
                    },
                },
            },
        },
    },
}));

mocks.useFormContextMock.mockReturnValue({
    data: { aplicacio: { id: 3 }, titol: 'El meu widget' },
    apiRef: { current: { setFieldValue: vi.fn() } },
});

vi.mock('reactlib', () => ({
    FormField: ({ name, label, disabled }: { name: string; label?: string; disabled?: boolean }) => (
        <div data-testid={`field-${name}`} data-disabled={disabled ? 'true' : 'false'}>{label || name}</div>
    ),
    MuiFilter: ({ children }: any) => <div data-testid="app-entorn-picker">{children}</div>,
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useFormContext: () => mocks.useFormContextMock(),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboardItem') {
            return { create: mocks.createDashboardItemMock };
        }
        if (resourceName === 'dashboardTitol') {
            return { create: mocks.createDashboardTitolMock };
        }
        return {};
    },
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: (selector: any) => selector(mocks.translations) }),
}));

vi.mock('../../../lib/components/mui/form/MuiForm.tsx', () => ({
    default: ({ children, apiRef }: any) => {
        if (apiRef) {
            apiRef.current = {
                save: vi.fn().mockResolvedValue({ id: 123 }),
                getData: vi.fn().mockReturnValue({ posX: 0, width: 3, height: 3 }),
            };
        }
        return <div data-testid="mui-form">{children}</div>;
    },
}));

vi.mock('./EstadisticaSimpleWidgetForm.tsx', () => ({
    default: ({ mode }: { mode?: string }) => <div data-testid="simple-widget-form">{mode}</div>,
    hasVisualOverrides: () => false,
    SIMPLE_OVERRIDE_FIELDS: ['colorText'],
}));
vi.mock('./EstadisticaGraficWidgetForm.tsx', () => ({
    default: ({ mode }: { mode?: string }) => <div data-testid="grafic-widget-form">{mode}</div>,
    hasVisualOverrides: () => false,
    GRAFIC_OVERRIDE_FIELDS: ['colorText'],
}));
vi.mock('./EstadisticaTaulaWidgetForm.tsx', () => ({
    default: ({ mode }: { mode?: string }) => <div data-testid="taula-widget-form">{mode}</div>,
    hasVisualOverrides: () => false,
    TAULA_OVERRIDE_FIELDS: ['colorTextTaula'],
}));

vi.mock('./EstadisticaWidgetFormFields.tsx', () => ({
    TitleDescriptionFields: () => <div data-testid="title-description-fields" />,
    DimensionsFields: () => <div data-testid="dimensions-fields" />,
    PeriodFields: () => <div data-testid="period-fields" />,
    FieldHelp: ({ text }: { text: string }) => <div data-testid="field-help">{text}</div>,
    PersonalitzatFields: ({ hasOverrides, onExpandedChange }: { hasOverrides: boolean; onExpandedChange?: (expanded: boolean) => void }) => {
        // Reprodueix el comportament real: en muntar-se, si ja hi ha overrides es desplega automàticament.
        useEffect(() => {
            if (hasOverrides) onExpandedChange?.(true);
        }, [hasOverrides]);
        return <div data-testid="personalitzat-fields" data-has-overrides={hasOverrides ? 'true' : 'false'} />;
    },
    VoraGraphicalFormEditor: () => <div data-testid="vora-graphical-form-editor" />,
    hasVisualOverridesTitol: (data: any) => mocks.hasVisualOverridesTitolMock(data),
    TITOL_OVERRIDE_FIELDS: ['colorTitol'],
}));

vi.mock('./WidgetPreview.tsx', () => ({
    WidgetPreview: ({ widgetType }: { widgetType: string }) => <div data-testid="widget-preview">{widgetType}</div>,
}));

vi.mock('./dashboardPlantillaHook.ts', () => ({
    useDashboardPlantilla: (id: number) => mocks.useDashboardPlantillaMock(id),
}));

describe('WidgetCreationWizard', () => {
    const defaultDashboard = {
        id: 1,
        titol: 'Dashboard Test',
        plantilla: { id: 10 },
        entorn: { id: 5 },
        aplicacio: { id: 3 },
    };

    beforeEach(() => {
        mocks.createDashboardItemMock.mockResolvedValue({ id: 123 });
        mocks.createDashboardTitolMock.mockResolvedValue({ id: 456 });
        mocks.hasVisualOverridesTitolMock.mockReturnValue(false);
        mocks.useDashboardPlantillaMock.mockReturnValue({ plantilla: null, loading: false });
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 3 },
                titol: 'El meu widget',
                indicador: { id: 1 },
                titolIndicador: 'Indicador X',
                tipusIndicador: 'SUM',
                dimensionsValor: [{ id: 1 }],
                periodeMode: 'PRESET',
                presetPeriode: 'LAST_30_DAYS',
            },
            apiRef: { current: { setFieldValue: vi.fn() } },
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('WidgetCreationWizard_alObrir_mostraLes4TargetesDeTipus', () => {
        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        expect(screen.getByText('Títol')).toBeInTheDocument();
        expect(screen.getByText('Simple')).toBeInTheDocument();
        expect(screen.getByText('Gràfic')).toBeInTheDocument();
        expect(screen.getByText('Taula')).toBeInTheDocument();
    });

    it('WidgetCreationWizard_senseTipusSeleccionat_botoSeguentDeshabilitat', () => {
        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        expect(screen.getByRole('button', { name: 'Següent' })).toBeDisabled();
    });

    it('WidgetCreationWizard_quanEsTriaSimpleIDadesValides_permetAvancarAIndicadors', () => {
        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByText('Simple'));
        expect(screen.getByTestId('title-description-fields')).toBeInTheDocument();

        const nextButton = screen.getByRole('button', { name: 'Següent' });
        expect(nextButton).not.toBeDisabled();
        fireEvent.click(nextButton);

        expect(screen.getByTestId('simple-widget-form')).toHaveTextContent('indicators');
    });

    it('WidgetCreationWizard_quanAplicacioIEntornEstanFixatsAlDashboard_esMostrenDesactivats', () => {
        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByText('Simple'));

        expect(screen.getByTestId('field-app')).toHaveAttribute('data-disabled', 'true');
        expect(screen.getByTestId('field-entorn')).toHaveAttribute('data-disabled', 'true');
    });

    it('WidgetCreationWizard_quanNoHiHaAplicacioNiEntornFixats_esPodenSeleccionar', () => {
        render(
            <WidgetCreationWizard
                open
                dashboard={{ id: 2, titol: 'Dashboard sense app' }}
                dashboardId="2"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByText('Simple'));

        expect(screen.getByTestId('field-app')).toHaveAttribute('data-disabled', 'false');
        expect(screen.getByTestId('field-entorn')).toHaveAttribute('data-disabled', 'false');
    });

    it('WidgetCreationWizard_quanEsTriaTitol_mostraPassesDeTitol', () => {
        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByText('Títol'));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));

        expect(screen.getByTestId('field-titol')).toBeInTheDocument();
        expect(screen.getByTestId('field-subtitol')).toBeInTheDocument();
    });

    it('WidgetCreationWizard_quanEsCreaUnTitolAmbOverrides_mostraLaPrevisualitzacioIGuardaPersonalitzatCert', async () => {
        // La passa de visualització d'un títol ha de mostrar la previsualització, permetre triar el
        // tipus de títol, i desar `personalitzat` segons si l'usuari ha emplenat camps propis.
        mocks.hasVisualOverridesTitolMock.mockReturnValue(true);

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByText('Títol'));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // type -> content
        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // content -> visual

        expect(screen.getByTestId('widget-preview')).toHaveTextContent('TITOL');
        expect(screen.getByTestId('field-tipusTitol')).toBeInTheDocument();
        expect(screen.getByTestId('personalitzat-fields')).toBeInTheDocument();
        // Amb overrides ja detectats, la secció es desplega automàticament i mostra la posició/separació
        // del subtítol i l'editor gràfic de vores (rectangle + modal), no els camps plans antics.
        expect(screen.getByTestId('field-posicioSubtitol')).toBeInTheDocument();
        expect(screen.getByTestId('field-separacioSubtitol')).toBeInTheDocument();
        expect(screen.getByTestId('vora-graphical-form-editor')).toBeInTheDocument();
        expect(screen.queryByTestId('field-mostrarVora')).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Crear' }));

        await waitFor(() => {
            expect(mocks.createDashboardTitolMock).toHaveBeenCalledWith({
                data: expect.objectContaining({ personalitzat: true }),
            });
        });
    });

    it('WidgetCreationWizard_quanEsPremCrearAlDarrerPas_creaDashboardItemITanca', async () => {
        const onCreated = vi.fn();
        const onClose = vi.fn();

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                initialWidgetType="SIMPLE"
                initialEntornId={5}
                initialAplicacio={{ id: 3 }}
                onClose={onClose}
                onCreated={onCreated}
            />
        );

        // Type -> Indicators -> Dimensions -> Period -> Visual
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));

        fireEvent.click(screen.getByRole('button', { name: 'Crear' }));

        await waitFor(() => {
            expect(mocks.createDashboardItemMock).toHaveBeenCalledWith({
                data: expect.objectContaining({
                    dashboard: { id: '1' },
                    widget: { id: 123 },
                    entornId: 5,
                }),
            });
            expect(onCreated).toHaveBeenCalled();
            expect(onClose).toHaveBeenCalled();
        });
    });

    it('WidgetCreationWizard_quanElDesatFallaPerUnCampDeIndicadors_hiTornaEnLlocDeQuedarBloquejat', async () => {
        // Reprodueix el bug reportat: un 422 no ha de deixar l'usuari bloquejat sense saber quin camp corregir.
        mocks.createDashboardItemMock.mockRejectedValueOnce({
            message: 'HTTP Error 422',
            errors: [{ field: 'indicador', message: 'Camp obligatori' }],
        });

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                initialWidgetType="SIMPLE"
                initialEntornId={5}
                initialAplicacio={{ id: 3 }}
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        expect(screen.getByTestId('personalitzat-fields')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Crear' }));

        await waitFor(() => {
            expect(screen.getByTestId('simple-widget-form')).toHaveTextContent('indicators');
        });
        expect(screen.queryByTestId('personalitzat-fields')).not.toBeInTheDocument();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Hi ha camps amb errors', 'error');
    });

    it('WidgetCreationWizard_quanFaltaUnCampObligatoriALaPassaDIndicadors_noEsPotAvancar', () => {
        // No s'ha de poder passar de fase si hi ha camps obligatoris no emplenats (bug reportat).
        mocks.useFormContextMock.mockReturnValue({
            data: { aplicacio: { id: 3 }, titol: 'El meu widget', indicador: { id: 1 } }, // falten titolIndicador i tipusIndicador
            apiRef: { current: { setFieldValue: vi.fn() } },
        });

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                initialWidgetType="SIMPLE"
                initialEntornId={5}
                initialAplicacio={{ id: 3 }}
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        expect(screen.getByTestId('simple-widget-form')).toHaveTextContent('indicators');
        expect(screen.getByRole('button', { name: 'Següent' })).toBeDisabled();
    });

    it('WidgetCreationWizard_quanFaltaElPeriode_noEsPotAvancarALaPassaDeVisualitzacio', () => {
        // Comprova que la passa de període també bloqueja l'avanç si no s'ha indicat cap valor.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 3 },
                titol: 'El meu widget',
                indicador: { id: 1 },
                titolIndicador: 'Indicador X',
                tipusIndicador: 'SUM',
                dimensionsValor: [{ id: 1 }],
                // periodeMode buit
            },
            apiRef: { current: { setFieldValue: vi.fn() } },
        });

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                initialWidgetType="SIMPLE"
                initialEntornId={5}
                initialAplicacio={{ id: 3 }}
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // indicators
        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // dimensions
        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // period
        expect(screen.getByTestId('period-fields')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Següent' })).toBeDisabled();
    });

    it('WidgetCreationWizard_quanNoHiHaDimensionsValor_permetAvancarDeLaPassaDeDimensions', () => {
        // La dimensió de filtre és opcional: si no se n'indica cap, es mostren totes les dades.
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 3 },
                titol: 'El meu widget',
                indicador: { id: 1 },
                titolIndicador: 'Indicador X',
                tipusIndicador: 'SUM',
                // dimensionsValor buit/absent
            },
            apiRef: { current: { setFieldValue: vi.fn() } },
        });

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                initialWidgetType="SIMPLE"
                initialEntornId={5}
                initialAplicacio={{ id: 3 }}
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // type -> indicators
        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // indicators -> dimensions
        fireEvent.click(screen.getByRole('button', { name: 'Següent' })); // dimensions -> period
        expect(screen.getByTestId('period-fields')).toBeInTheDocument();
    });

    it('WidgetCreationWizard_quanEsGraficGaugeDosIndicadorsSenseIndicadorMax_noEsPotAvancar', () => {
        mocks.useFormContextMock.mockReturnValue({
            data: {
                aplicacio: { id: 3 },
                titol: 'El meu widget',
                tipusGrafic: 'GAUGE_CHART',
                tipusDades: 'DOS_INDICADORS',
                indicador: { id: 1 },
                agregacio: 'SUM',
                tempsAgrupacio: 'MES',
                // falten indicadorMax, agregacioMax i tipusValors
            },
            apiRef: { current: { setFieldValue: vi.fn() } },
        });

        render(
            <WidgetCreationWizard
                open
                dashboard={defaultDashboard}
                dashboardId="1"
                initialWidgetType="GRAFIC"
                initialEntornId={5}
                initialAplicacio={{ id: 3 }}
                onClose={vi.fn()}
                onCreated={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', { name: 'Següent' }));
        expect(screen.getByTestId('grafic-widget-form')).toHaveTextContent('indicators');
        expect(screen.getByRole('button', { name: 'Següent' })).toBeDisabled();
    });
});
