import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EstadisticaDashboardEdit, { AfegirTitolFormContent } from './EstadisticaDashboardEdit';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    navigateMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    goBackMock: vi.fn(),
    showContentDialogMock: vi.fn(),
    showFormDialogMock: vi.fn(),
    useDashboardMock: vi.fn(),
    useDashboardWidgetsMock: vi.fn(),
    useMapDashboardItemsMock: vi.fn(),
    createDashboardItemMock: vi.fn(),
    patchDashboardItemMock: vi.fn(),
    patchDashboardTitolMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                dashboards: {
                    title: 'Dashboards',
                    components: {
                        llistar: 'Llistar',
                        afegir: 'Afegir',
                    },
                    action: {
                        llistarWidget: { label: 'Llistar widgets' },
                        llistarTitle: { label: 'Llistar títols' },
                        addWidget: {
                            title: 'Afegir widget',
                            label: 'Afegir widget',
                            success: 'Widget afegit',
                            error: 'Error afegint widget',
                        },
                        afegirTitle: {
                            title: 'Afegir títol',
                            label: 'Afegir títol',
                        },
                        patchItem: {
                            success: 'Guardat',
                            error: 'Error guardant',
                            warning: 'Advertiment',
                            saveError: 'Error persistint',
                        },
                    },
                    alert: {
                        tornarLlistat: 'Tornar al llistat',
                        notExists: 'Dashboard inexistent',
                        carregar: 'Error de càrrega',
                    },
                },
                widget: {
                    simple: { tab: { title: 'Simple' }, title: 'Widgets simples' },
                    grafic: { tab: { title: 'Gràfic' }, title: 'Widgets gràfics' },
                    taula: { tab: { title: 'Taula' }, title: 'Widgets taula' },
                    grid: {
                        position: 'Posició',
                        size: 'Mida',
                    },
                    action: {
                        add: { label: 'Afegir' },
                    },
                },
            },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('react-router-dom', () => ({
    useParams: () => mocks.useParamsMock(),
    useNavigate: () => mocks.navigateMock,
}));

vi.mock('reactlib', () => ({
    BasePage: ({
        toolbar,
        children,
    }: {
        toolbar: React.ReactNode;
        children: React.ReactNode;
    }) => (
        <div>
            <div data-testid="toolbar">{toolbar}</div>
            <div>{children}</div>
        </div>
    ),
    MuiDataGrid: ({
        title,
        rowAdditionalActions,
    }: {
        title: string;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: number) => void }>;
    }) => (
        <section>
            <h2>{title}</h2>
            {rowAdditionalActions?.map((action) => (
                <button key={action.label} type="button" onClick={() => action.onClick?.(5)}>
                    {action.label}
                </button>
            ))}
        </section>
    ),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
        t: (key: string) => key,
        goBack: mocks.goBackMock,
    }),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboardItem') {
            return {
                isReady: true,
                patch: mocks.patchDashboardItemMock,
                create: mocks.createDashboardItemMock,
            };
        }
        if (resourceName === 'dashboardTitol') {
            return {
                isReady: true,
                patch: mocks.patchDashboardTitolMock,
            };
        }
        return {
            isReady: true,
            delete: vi.fn(),
        };
    },
    MuiFilter: ({
        children,
        onDataChange,
        onSpringFilterChange,
        additionalData,
    }: {
        children: React.ReactNode;
        onDataChange?: (data: unknown) => void;
        onSpringFilterChange?: (value?: string) => void;
        additionalData?: unknown;
    }) => (
        <div>
            <button
                type="button"
                onClick={() => {
                    onDataChange?.({
                        ...(additionalData as object),
                        entorn: { id: 2 },
                    });
                    onSpringFilterChange?.('appId=1');
                }}
            >
                Aplicar filtre entorn
            </button>
            {children}
        </div>
    ),
    springFilterBuilder: {
        and: (...values: unknown[]) => values.filter(Boolean).join(' AND '),
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        exists: (value: unknown) => `exists(${String(value)})`,
    },
    FormField: ({
        name,
        type,
    }: {
        name: string;
        type?: string;
    }) => <div data-testid={`field-${name}`}>{`${name}:${type ?? 'default'}`}</div>,
    useFormContext: () => ({
        data: {
            titol: 'Títol',
            subtitol: 'Subtítol',
            mostrarVora: true,
        },
    }),
    useMessageDialogButtons: () => <button>Ok</button>,
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: vi.fn(),
        },
    }),
}));

vi.mock('../../lib/components/mui/Dialog.tsx', () => ({
    useContentDialog: () => [mocks.showContentDialogMock, <div key="content-dialog" />],
}));

vi.mock('../../lib/components/mui/form/FormDialog.tsx', () => ({
    useFormDialog: () => [mocks.showFormDialogMock, <div key="form-dialog" />],
}));

vi.mock('../hooks/dashboardRequests.ts', () => ({
    useDashboard: (id: string) => mocks.useDashboardMock(id),
    useDashboardWidgets: (id: string) => mocks.useDashboardWidgetsMock(id),
}));

vi.mock('../components/estadistiques/DashboardReactGridLayout.tsx', () => ({
    DashboardReactGridLayout: ({
        dashboardId,
        editable,
        onGridLayoutItemsChange,
    }: {
        dashboardId: number;
        editable: boolean;
        onGridLayoutItemsChange?: (items: Array<{ id: number; x: number; y: number; w: number; h: number; type?: string }>) => void;
    }) => (
        <div>
            <div>{`DashboardGrid ${dashboardId} ${String(editable)}`}</div>
            <button
                type="button"
                onClick={() =>
                    onGridLayoutItemsChange?.([{ id: 1, x: 1, y: 1, w: 4, h: 4 }])
                }
            >
                Moure layout
            </button>
        </div>
    ),
    useMapDashboardItems: (widgets: unknown[]) => mocks.useMapDashboardItemsMock(widgets),
}));

vi.mock('../components/estadistiques/TitolWidgetVisualization.tsx', () => ({
    default: () => <div>Preview títol</div>,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

vi.mock('../components/CenteredCircularProgress.tsx', () => ({
    default: () => <div>Carregant dashboard</div>,
}));

vi.mock('../components/ButtonMenu.tsx', () => ({
    default: ({
        title,
        children,
    }: {
        title: string;
        children: React.ReactNode;
    }) => (
        <div>
            <button type="button">{title}</button>
            <div>{children}</div>
        </div>
    ),
}));

vi.mock('../AppRoutes.tsx', () => ({
    DASHBOARDS_PATH: 'dashboards',
}));

describe('AfegirTitolFormContent', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('AfegirTitolFormContent_quanMostraVora_renderitzaElsCampsAddicionals', () => {
        // Comprova que el formulari del títol mostra la previsualització i els camps de vora quan estan actius.
        render(<AfegirTitolFormContent />);

        expect(screen.getByText('Preview títol')).toBeInTheDocument();
        expect(screen.getByTestId('field-titol')).toBeInTheDocument();
        expect(screen.getByTestId('field-colorVora')).toHaveTextContent('colorVora:color');
        expect(screen.getByTestId('field-ampleVora')).toBeInTheDocument();
    });
});

describe('EstadisticaDashboardEdit', () => {
    beforeEach(() => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12', aplicacio: { id: 1 }, entorn: { id: 2 } },
            loading: false,
            exception: null,
        });
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1 }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: vi.fn(),
        });
        mocks.useMapDashboardItemsMock.mockReturnValue([{ id: 1, x: 0, y: 0, w: 3, h: 3 }]);
        mocks.createDashboardItemMock.mockResolvedValue(undefined);
        mocks.patchDashboardItemMock.mockResolvedValue(undefined);
        mocks.patchDashboardTitolMock.mockResolvedValue(undefined);
        mocks.showContentDialogMock.mockImplementation(() => undefined);
        mocks.showFormDialogMock.mockResolvedValue(undefined);
        vi.spyOn(console, 'error').mockImplementation(() => {});
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaDashboardEdit_quanElDashboardNoExisteix_mostraLAlerta404', () => {
        // Verifica que la pàgina mostra l'alerta de 404 i permet tornar al llistat de dashboards.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: { status: 404 },
        });

        render(<EstadisticaDashboardEdit />);

        fireEvent.click(screen.getByRole('button', { name: 'Tornar al llistat' }));

        expect(screen.getByText('Dashboard inexistent')).toBeInTheDocument();
        expect(mocks.navigateMock).toHaveBeenCalledWith('/dashboards');
    });

    it('EstadisticaDashboardEdit_quanHiHaDashboard_mostraToolbarIGraellaEditable', async () => {
        // Comprova que la vista d'edició mostra la toolbar principal i el layout editable del dashboard.
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        expect(screen.getByRole('heading', { name: 'Dashboards' })).toBeInTheDocument();
        expect(screen.getByText('Dashboard 12')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Llistar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Afegir' })).toBeInTheDocument();
    });

    it('EstadisticaDashboardEdit_quanHiHaErrorGeneric_mostraLalertaDeCarrega', () => {
        // Verifica que els errors no-404 mostren el missatge genèric de càrrega fallida.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: { status: 500 },
        });

        render(<EstadisticaDashboardEdit />);

        expect(screen.getByText('Error de càrrega')).toBeInTheDocument();
    });

    it('EstadisticaDashboardEdit_quanEsLlistenElsWidgets_obriElDialegDeContingut', async () => {
        // Comprova que el menú de llistar obri el diàleg amb el contingut de widgets del dashboard.
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByText('Llistar widgets'));

        expect(mocks.showContentDialogMock).toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_quanSobraElFormulariDeTitol_obriElDialegIRefrescaEnTancar', async () => {
        // Verifica que l'acció d'afegir títol obri el formulari modal i refresqui els widgets en completar-se.
        const forceRefreshMock = vi.fn();
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1 }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: forceRefreshMock,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByText('Afegir títol'));

        await waitFor(() => {
            expect(mocks.showFormDialogMock).toHaveBeenCalled();
        });

        expect(forceRefreshMock).toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_quanSafaUnWidgetNou_elCreaIMostraExit', async () => {
        // Comprova que des del diàleg d'afegir widget es crea l'element i es notifica l'èxit.
        const forceRefreshMock = vi.fn();
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1 }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: forceRefreshMock,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByText('Afegir widget'));
        fireEvent.click(screen.getByRole('button', { name: 'Aplicar filtre entorn' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Afegir' }));

        await waitFor(() => {
            expect(mocks.createDashboardItemMock).toHaveBeenCalledWith({
                data: {
                    dashboard: { id: '12' },
                    widget: { id: 5 },
                    entornId: 2,
                    posX: 0,
                    width: 3,
                    height: 3,
                },
            });
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Widget afegit', 'success');
        expect(forceRefreshMock).toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_quanCanviaElLayout_guardaElsCanvisINotificaExit', async () => {
        // Verifica que els canvis de posició del layout es persisteixen i mostren missatge d'èxit.
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Moure layout' }));

        await waitFor(() => {
            expect(mocks.patchDashboardItemMock).toHaveBeenCalledWith(1, {
                data: {
                    posX: 1,
                    posY: 1,
                    width: 4,
                    height: 4,
                },
            });
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Guardat', 'success');
    });

    it('EstadisticaDashboardEdit_quanFallaLaCreacioDunWidget_mostraLError', async () => {
        // Comprova que els errors en afegir un widget es comuniquen amb notificació d'error.
        mocks.createDashboardItemMock.mockRejectedValueOnce(new Error('boom'));

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByText('Afegir widget'));
        fireEvent.click(screen.getByRole('button', { name: 'Aplicar filtre entorn' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Afegir' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Error afegint widget',
                'error'
            );
        });
    });

    it('EstadisticaDashboardEdit_quanFallaElGuardatDelLayout_mostraLError', async () => {
        // Verifica que un error en persistir el layout mostra el missatge d'error corresponent.
        mocks.patchDashboardItemMock.mockRejectedValueOnce(new Error('boom'));

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Moure layout' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Error guardant',
                'error'
            );
        });
    });
});
