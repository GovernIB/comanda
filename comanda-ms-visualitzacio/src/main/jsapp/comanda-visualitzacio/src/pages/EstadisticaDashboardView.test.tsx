import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EstadisticaDashboardView from './EstadisticaDashboardView';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    navigateMock: vi.fn(),
    findDashboardMock: vi.fn(),
    setItemMock: vi.fn(),
    removeItemMock: vi.fn(),
    getItemMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                dashboards: {
                    title: 'Dashboards',
                    action: {
                        select: {
                            title: 'Seleccionar dashboard',
                        },
                    },
                    alert: {
                        tornarTauler: 'Tornar al tauler',
                        notExists: 'El dashboard no existeix',
                        carregar: 'No s ha pogut carregar el dashboard',
                        notDefined: 'No hi ha dashboards definits',
                    },
                },
            },
        })
    ),
    useDashboardMock: vi.fn(),
    useDashboardWidgetsMock: vi.fn(),
    useMapDashboardItemsMock: vi.fn(),
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
        filter,
        onRowClick,
    }: {
        title: string;
        filter?: string;
        onRowClick?: () => void;
    }) => (
        <section>
            <h2>{title}</h2>
            <span data-testid="dashboard-filter">{filter ?? ''}</span>
            <button type="button" onClick={onRowClick}>
                Seleccionar fila
            </button>
        </section>
    ),
    useCloseDialogButtons: () => <button>Tancar</button>,
    useResourceApiService: () => ({
        isReady: true,
        find: mocks.findDashboardMock,
    }),
}));

vi.mock('../hooks/dashboardRequests.ts', () => ({
    useDashboard: (dashboardId: string | null) => mocks.useDashboardMock(dashboardId),
    useDashboardWidgets: (dashboardId: string | null) => mocks.useDashboardWidgetsMock(dashboardId),
}));

vi.mock('../components/estadistiques/DashboardReactGridLayout.tsx', () => ({
    DashboardReactGridLayout: ({
        dashboardId,
        gridLayoutItems,
    }: {
        dashboardId: number;
        gridLayoutItems: unknown[];
    }) => (
        <div>{`Grid ${dashboardId} (${gridLayoutItems.length})`}</div>
    ),
    useMapDashboardItems: (widgets: unknown[]) => mocks.useMapDashboardItemsMock(widgets),
}));

vi.mock('../../lib/components/mui/Dialog.tsx', () => ({
    default: ({
        open,
        children,
    }: {
        open: boolean;
        children: React.ReactNode;
    }) => (open ? <div data-testid="dialog-select">{children}</div> : null),
}));

vi.mock('../AppRoutes.tsx', () => ({
    ESTADISTIQUES_PATH: 'estadistiques',
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

vi.mock('../components/CenteredCircularProgress.tsx', () => ({
    default: () => <div>Carregant dashboard</div>,
}));

describe('EstadisticaDashboardView', () => {
    beforeEach(() => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.getItemMock.mockReturnValue(null);
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12' },
            loading: false,
            exception: null,
        });
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1 }],
            loadingWidgetPositions: false,
        });
        mocks.useMapDashboardItemsMock.mockReturnValue([{ i: '1' }]);
        mocks.findDashboardMock.mockResolvedValue({
            rows: [{ id: 99, titol: 'Primer dashboard' }],
        });
        vi.stubGlobal('localStorage', {
            getItem: mocks.getItemMock,
            setItem: mocks.setItemMock,
            removeItem: mocks.removeItemMock,
        });
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('EstadisticaDashboardView_quanHiHaDashboard_mostraLaToolbarIElGrid', async () => {
        // Comprova que la vista principal renderitza el dashboard seleccionat i desa l'últim id visitat.
        render(<EstadisticaDashboardView />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Dashboards' })).toBeInTheDocument();
        });

        expect(screen.getByRole('button', { name: /Dashboard 12/i })).toBeInTheDocument();
        expect(screen.getByText('Grid 12 (1)')).toBeInTheDocument();
        expect(mocks.setItemMock).toHaveBeenCalledWith('lastViewedDashboardId', '12');
    });

    it('EstadisticaDashboardView_quanEsPremLaToolbar_obreElDialegDeSeleccioAmbFiltre', async () => {
        // Verifica que la selecció de dashboard obre el diàleg i exclou el dashboard actual del grid.
        render(<EstadisticaDashboardView />);

        fireEvent.click(screen.getByRole('button', { name: /Dashboard 12/i }));

        expect(await screen.findByTestId('dialog-select')).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Seleccionar dashboard' })).toBeInTheDocument();
        expect(screen.getByTestId('dashboard-filter')).toHaveTextContent('id ! 12');
    });

    it('EstadisticaDashboardView_quanElDashboardNoExisteix_mostraLAlertaIResetejaLaSeleccio', async () => {
        // Comprova que un 404 mostra l'avís i permet tornar al tauler per defecte netejant el localStorage.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: { status: 404 },
        });

        render(<EstadisticaDashboardView />);

        fireEvent.click(await screen.findByRole('button', { name: 'Tornar al tauler' }));

        expect(screen.getByText('El dashboard no existeix')).toBeInTheDocument();
        expect(mocks.removeItemMock).toHaveBeenCalledWith('lastViewedDashboardId');
        expect(mocks.navigateMock).toHaveBeenCalledWith('/estadistiques');
    });

    it('EstadisticaDashboardView_quanHiHaErrorGeneric_mostraLAlertaDError', () => {
        // Verifica que qualsevol error diferent de 404 mostra el missatge genèric de càrrega.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: { status: 500 },
        });

        render(<EstadisticaDashboardView />);

        expect(screen.getByText(/No s ha pogut carregar el dashboard/i)).toBeInTheDocument();
    });

    it('EstadisticaDashboardView_quanNoHiHaIdNiDashboards_mostraLAvísDeBuit', async () => {
        // Comprova que si no hi ha id ni dashboards disponibles es mostra l'estat buit de la vista.
        mocks.useParamsMock.mockReturnValue({});
        mocks.getItemMock.mockReturnValue(null);
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: null,
        });
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: undefined,
            loadingWidgetPositions: false,
        });
        mocks.findDashboardMock.mockResolvedValue({ rows: [] });

        render(<EstadisticaDashboardView />);

        await waitFor(() => {
            expect(screen.getByText('No hi ha dashboards definits')).toBeInTheDocument();
        });
    });
});
