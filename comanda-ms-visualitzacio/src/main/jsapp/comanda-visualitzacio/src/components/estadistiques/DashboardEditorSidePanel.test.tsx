import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { DashboardEditorSidePanel, DashboardEditorSelection } from './DashboardEditorSidePanel';

const mocks = vi.hoisted(() => ({
    temporalMessageShowMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    createDashboardItemMock: vi.fn(),
    deleteDashboardItemMock: vi.fn(),
    deleteDashboardTitolMock: vi.fn(),
    useDashboardPlantillaMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                widget: {
                    form: {
                        preview: 'Previsualització',
                    },
                },
            },
        })
    ),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, type, label }: { name: string; type?: string; label?: string }) => (
        <div data-testid={`field-${name}`}>{label || name}</div>
    ),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
        messageDialogShow: mocks.messageDialogShowMock,
    }),
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useFormContext: () => ({
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
    }),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboardItem') {
            return {
                create: mocks.createDashboardItemMock,
                delete: mocks.deleteDashboardItemMock,
            };
        }
        if (resourceName === 'dashboardTitol') {
            return {
                delete: mocks.deleteDashboardTitolMock,
            };
        }
        return {};
    },
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
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
    default: ({ mode }: { mode?: string }) => (
        <div data-testid="simple-widget-form">{mode}</div>
    ),
}));

vi.mock('./EstadisticaGraficWidgetForm.tsx', () => ({
    default: ({ mode }: { mode?: string }) => (
        <div data-testid="grafic-widget-form">{mode}</div>
    ),
}));

vi.mock('./EstadisticaTaulaWidgetForm.tsx', () => ({
    default: ({ mode }: { mode?: string }) => (
        <div data-testid="taula-widget-form">{mode}</div>
    ),
}));

vi.mock('./dashboardPlantillaHook.ts', () => ({
    useDashboardPlantilla: (id: number) => mocks.useDashboardPlantillaMock(id),
}));

vi.mock('./WidgetPreview.tsx', () => ({
    WidgetPreview: ({ widgetType }: { widgetType: string }) => (
        <div data-testid="widget-preview">{widgetType}</div>
    ),
}));

describe('DashboardEditorSidePanel', () => {
    const defaultDashboard = {
        id: 1,
        titol: 'Dashboard Test',
        plantilla: { id: 10 },
        entorn: { id: 5 },
        aplicacio: { id: 3 },
    };

    beforeEach(() => {
        mocks.createDashboardItemMock.mockResolvedValue({ id: 123 });
        mocks.deleteDashboardItemMock.mockResolvedValue(undefined);
        mocks.deleteDashboardTitolMock.mockResolvedValue(undefined);
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
        const selection: DashboardEditorSelection = { kind: 'none' };
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

        expect(screen.getByTestId('simple-widget-form')).toBeInTheDocument();
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

        expect(screen.getByTestId('grafic-widget-form')).toBeInTheDocument();
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
        // expect(screen.getByTestId('widget-preview')).toHaveTextContent('TITOL');
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

        fireEvent.click(screen.getByRole('button', { name: /Desar/i }));

        await waitFor(() => {
            expect(mocks.createDashboardItemMock).toHaveBeenCalledWith({
                data: expect.objectContaining({
                    dashboard: { id: '1' },
                    widget: { id: 123 },
                    entornId: 5,
                }),
            });
            expect(onSaved).toHaveBeenCalled();
        });
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

        fireEvent.click(screen.getByRole('button', { name: /Eliminar/i }));

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

        fireEvent.click(screen.getByRole('button', { name: /Eliminar/i }));

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