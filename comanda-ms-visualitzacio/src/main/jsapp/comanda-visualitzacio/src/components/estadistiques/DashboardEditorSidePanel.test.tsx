import {fireEvent, render, screen, waitFor} from '@testing-library/react';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {DashboardEditorSelection, DashboardEditorSidePanel} from './DashboardEditorSidePanel';

const mocks = vi.hoisted(() => ({
    temporalMessageShowMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    createDashboardItemMock: vi.fn(),
    patchDashboardItemMock: vi.fn(),
    patchDashboardTitolMock: vi.fn(),
    deleteDashboardItemMock: vi.fn(),
    deleteDashboardTitolMock: vi.fn(),
    hasVisualOverridesTitolMock: vi.fn(),
    useDashboardPlantillaMock: vi.fn(),
    // Referència estable perquè els bridges (useEffect amb [data] com a dependència) no entrin en bucle infinit.
    formContextValue: {data: {} as any, apiRef: {current: {setFieldValue: vi.fn()}}},
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    form: {
                        preview: 'Previsualització',
                        periode: 'Període',
                        configVisual: 'Configuració gràfica',
                    },
                    editor: {
                        properties: 'Propietats',
                        configData: 'Configuració de dades',
                        configVisual: 'Configuració gràfica',
                        titleText: 'Text del títol',
                        titleType: 'Tipus de títol',
                        showDestacat: 'Mostrar com a destacat',
                        template: 'Plantilla',
                        textSize: 'Mida del text',
                        textColor: 'Color del text',
                        subtitleSize: 'Mida del subtítol',
                        subtitleColor: 'Color del subtítol',
                        backgroundColor: 'Color de fons',
                        showBorder: 'Mostrar vora',
                        borderColor: 'Color de vora',
                        borderWidth: 'Amplada de vora',
                        posX: 'Posició X',
                        posY: 'Posició Y',
                        width: 'Amplada',
                        height: 'Alçada',
                        widgetType: 'Tipus de widget',
                        empty: 'Seleccionau un element del canvas o creau un widget nou per editar-ne les propietats.',
                        selectWidgetType: 'Seleccionau el tipus de widget',
                        confirmTitle: 'Confirmació',
                        confirmDelete: 'Estau segur que voleu esborrar aquest element del dashboard?',
                        deleted: 'Element eliminat',
                        deleteError: 'No s’ha pogut eliminar',
                        selectType: 'Seleccionau un tipus per veure les propietats configurables.',
                    },
                    wizard: {
                        steps: {
                            indicators: 'Propietats',
                            dimensions: 'Filtres',
                            visual: 'Visualització',
                            position: 'Posició i mida',
                        },
                        visual: {
                            personalitzat: 'Personalitzat',
                            personalitzatHelp: 'Ajuda personalitzat',
                            personalitzatBadge: 'Ajuda badge personalitzat',
                        },
                        actions: {
                            validationError: 'Hi ha camps amb errors',
                        },
                        types: {
                            simple: { label: 'Simple' },
                            grafic: { label: 'Gràfic' },
                            taula: { label: 'Taula' },
                        },
                    },
                },
            },
            common: {
                delete: 'Eliminar',
                save: 'Desar',
            },
        })
    ),
}));

vi.mock('reactlib', () => ({
    FormField: ({name, label}: { name: string; label?: string }) => (
        <div data-testid={`field-${name}`}>{label || name}</div>
    ),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
        messageDialogShow: mocks.messageDialogShowMock,
    }),
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useFormContext: () => mocks.formContextValue,
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboardItem') {
            return {
                create: mocks.createDashboardItemMock,
                patch: mocks.patchDashboardItemMock,
                delete: mocks.deleteDashboardItemMock,
            };
        }
        if (resourceName === 'dashboardTitol') {
            return {
                patch: mocks.patchDashboardTitolMock,
                delete: mocks.deleteDashboardTitolMock,
            };
        }
        return {};
    },
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({t: mocks.tMock}),
}));

vi.mock('../../../lib/components/mui/form/MuiForm.tsx', () => ({
    default: ({children, apiRef}: any) => {
        if (apiRef) {
            apiRef.current = {
                save: vi.fn().mockResolvedValue({id: 123}),
                getData: vi.fn().mockReturnValue({posX: 0, width: 3, height: 3}),
            };
        }
        return <div data-testid="mui-form">{children}</div>;
    },
}));

vi.mock('./EstadisticaSimpleWidgetForm.tsx', () => ({
    default: ({mode}: { mode?: string }) => (
        <div data-testid="simple-widget-form">{mode}</div>
    ),
    hasVisualOverrides: () => false,
}));

vi.mock('./EstadisticaGraficWidgetForm.tsx', () => ({
    default: ({mode}: { mode?: string }) => (
        <div data-testid="grafic-widget-form">{mode}</div>
    ),
    hasVisualOverrides: () => false,
}));

vi.mock('./EstadisticaTaulaWidgetForm.tsx', () => ({
    default: ({mode}: { mode?: string }) => (
        <div data-testid="taula-widget-form">{mode}</div>
    ),
    hasVisualOverrides: () => false,
}));

vi.mock('./dashboardPlantillaHook.ts', () => ({
    useDashboardPlantilla: (id: number) => mocks.useDashboardPlantillaMock(id),
    useEntornCodi: () => ({entornCodi: undefined, loading: false}),
}));

vi.mock('./EstadisticaWidgetFormFields.tsx', () => ({
    DimensionsFields: () => <div data-testid="dimensions-fields"/>,
    PeriodFields: () => <div data-testid="period-fields"/>,
    PersonalitzatFields: ({hasOverrides}: { hasOverrides: boolean }) => (
        <div data-testid="personalitzat-fields" data-has-overrides={hasOverrides ? 'true' : 'false'}/>
    ),
    FieldHelp: ({text}: { text: string }) => <div data-testid="field-help">{text}</div>,
    hasVisualOverridesTitol: (data: any) => mocks.hasVisualOverridesTitolMock(data),
}));

vi.mock('./WidgetPreview.tsx', () => ({
    WidgetPreview: ({widgetType}: { widgetType: string }) => (
        <div data-testid="widget-preview">{widgetType}</div>
    ),
}));

describe('DashboardEditorSidePanel', () => {
    const defaultDashboard = {
        id: 1,
        titol: 'Dashboard Test',
        plantilla: {id: 10},
        entorn: {id: 5},
        aplicacio: {id: 3},
    };

    beforeEach(() => {
        mocks.createDashboardItemMock.mockResolvedValue({id: 123});
        mocks.patchDashboardItemMock.mockResolvedValue(undefined);
        mocks.patchDashboardTitolMock.mockResolvedValue(undefined);
        mocks.deleteDashboardItemMock.mockResolvedValue(undefined);
        mocks.deleteDashboardTitolMock.mockResolvedValue(undefined);
        mocks.hasVisualOverridesTitolMock.mockReturnValue(false);
        mocks.useDashboardPlantillaMock.mockReturnValue({
            plantilla: null,
            loading: false,
        });
        mocks.messageDialogShowMock.mockResolvedValue(true);
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('DashboardEditorSidePanel_quanSelectionEsNone_mostraMissatgeBuit', () => {
        const selection: DashboardEditorSelection = {kind: 'none'};
        const onSelectionChange = vi.fn();
        const onSaved = vi.fn();
        const onDeleted = vi.fn();

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={onSelectionChange}
                onSaved={onSaved}
                onDeleted={onDeleted}
            />
        );

        expect(screen.getByText(/Seleccionau un element del canvas/)).toBeInTheDocument();
    });

    it('DashboardEditorSidePanel_quanSelectionEsWidgetSimple_mostraFormulariSimple', () => {
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'edit',
            widgetType: 'SIMPLE',
            dashboardItemId: 10,
            widgetId: 20,
        };

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        expect(screen.getByTestId('simple-widget-form')).toHaveTextContent('indicators');

        fireEvent.click(screen.getByRole('tab', {name: 'Visualització'}));

        expect(screen.getByTestId('simple-widget-form')).toHaveTextContent('visual');
    });

    it('DashboardEditorSidePanel_quanSelectionEsWidgetGrafic_mostraFormulariGrafic', () => {
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'edit',
            widgetType: 'GRAFIC',
            dashboardItemId: 10,
            widgetId: 20,
        };

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        expect(screen.getByTestId('grafic-widget-form')).toHaveTextContent('indicators');

        fireEvent.click(screen.getByRole('tab', {name: 'Visualització'}));

        expect(screen.getByTestId('grafic-widget-form')).toHaveTextContent('visual');
    });

    it('DashboardEditorSidePanel_quanSelectionEsTitol_mostraFormulariTitol', () => {
        const selection: DashboardEditorSelection = {
            kind: 'title',
            mode: 'edit',
            dashboardTitolId: 15,
        };

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        expect(screen.getByTestId('field-titol')).toBeInTheDocument();
        expect(screen.getByTestId('field-tipusTitol')).toBeInTheDocument();
        expect(screen.getByTestId('widget-preview')).toHaveTextContent('TITOL');
        expect(screen.getByTestId('personalitzat-fields')).toBeInTheDocument();
    });

    it('DashboardEditorSidePanel_quanEsPremDesarAmbTitolExistent_actualitzaElFlagPersonalitzat', async () => {
        // El backend només aplica els camps propis del títol per sobre de la plantilla si `personalitzat`
        // és cert: cal que es desi sempre, calculat a partir dels overrides reals del títol.
        mocks.hasVisualOverridesTitolMock.mockReturnValue(true);
        const selection: DashboardEditorSelection = {
            kind: 'title',
            mode: 'edit',
            dashboardTitolId: 15,
        };

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', {name: /Desar/i}));

        await waitFor(() => {
            expect(mocks.patchDashboardTitolMock).toHaveBeenCalledWith(15, {
                data: {personalitzat: true},
            });
        });
    });

    it('DashboardEditorSidePanel_quanEsPremDesarAmbWidgetNou_creaDashboardItem', async () => {
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'create',
            widgetType: 'SIMPLE',
            entornId: 5,
        };
        const onSaved = vi.fn();

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={onSaved}
                onDeleted={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', {name: /Desar/i}));

        await waitFor(() => {
            expect(mocks.createDashboardItemMock).toHaveBeenCalledWith({
                data: expect.objectContaining({
                    dashboard: {id: '1'},
                    widget: {id: 123},
                    entornId: 5,
                    personalitzat: false,
                }),
            });
            expect(onSaved).toHaveBeenCalled();
        });
    });

    it('DashboardEditorSidePanel_quanEsPremDesarAmbWidgetExistent_actualitzaElFlagPersonalitzat', async () => {
        // El backend només aplica els camps propis del widget per sobre de la plantilla si `personalitzat`
        // és cert: cal que es desi sempre, calculat a partir dels overrides reals del widget.
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'edit',
            widgetType: 'SIMPLE',
            dashboardItemId: 55,
            widgetId: 20,
        };

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        fireEvent.click(screen.getByRole('button', {name: /Desar/i}));

        await waitFor(() => {
            expect(mocks.patchDashboardItemMock).toHaveBeenCalledWith(55, {
                data: {personalitzat: false},
            });
        });
    });

    it('DashboardEditorSidePanel_quanElDesatFallaPerUnCampDUnaAltraPestanya_hiNavega', async () => {
        // Reprodueix el bug reportat: si el 422 ve d'un camp d'una pestanya no activa, cal navegar-hi.
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'create',
            widgetType: 'SIMPLE',
            entornId: 5,
        };

        mocks.createDashboardItemMock.mockRejectedValueOnce({
            message: 'HTTP Error 422',
            errors: [{field: 'destacat', message: 'Camp invàlid'}],
        });

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        expect(screen.queryByTestId('personalitzat-fields')).not.toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', {name: /Desar/i}));

        await waitFor(() => {
            expect(screen.getByRole('tab', {name: 'Visualització'})).toHaveAttribute('aria-selected', 'true');
        });
        expect(screen.getByTestId('personalitzat-fields')).toBeInTheDocument();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Hi ha camps amb errors', 'error');
    });

    it('DashboardEditorSidePanel_quanEsPremEliminarAmbWidgetExistents_esborraDashboardItem', async () => {
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'edit',
            widgetType: 'SIMPLE',
            dashboardItemId: 10,
            widgetId: 20,
        };
        const onDeleted = vi.fn();

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={onDeleted}
            />
        );

        fireEvent.click(screen.getByRole('button', {name: /Eliminar/i}));

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalled();
            expect(mocks.deleteDashboardItemMock).toHaveBeenCalledWith(10);
            expect(onDeleted).toHaveBeenCalled();
        });
    });

    it('DashboardEditorSidePanel_quanEsPremEliminarAmbTitolExistents_esborraDashboardTitol', async () => {
        const selection: DashboardEditorSelection = {
            kind: 'title',
            mode: 'edit',
            dashboardTitolId: 15,
        };
        const onDeleted = vi.fn();

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={vi.fn()}
                onSaved={vi.fn()}
                onDeleted={onDeleted}
            />
        );

        fireEvent.click(screen.getByRole('button', {name: /Eliminar/i}));

        await waitFor(() => {
            expect(mocks.deleteDashboardTitolMock).toHaveBeenCalledWith(15);
            expect(onDeleted).toHaveBeenCalled();
        });
    });

    it('DashboardEditorSidePanel_quanEsCanviaTipusWidget_enModeCreate_actualitzaSelection', () => {
        const selection: DashboardEditorSelection = {
            kind: 'widget',
            mode: 'create',
            widgetType: 'SIMPLE',
        };
        const onSelectionChange = vi.fn();

        render(
            <DashboardEditorSidePanel
                dashboard={defaultDashboard}
                dashboardId="1"
                selection={selection}
                onSelectionChange={onSelectionChange}
                onSaved={vi.fn()}
                onDeleted={vi.fn()}
            />
        );

        const select = screen.getByRole('combobox');
        fireEvent.mouseDown(select);

        const graficOption = screen.getByText('Gràfic');
        fireEvent.click(graficOption);

        expect(onSelectionChange).toHaveBeenCalledWith(
            expect.objectContaining({
                widgetType: 'GRAFIC',
            })
        );
    });
});
