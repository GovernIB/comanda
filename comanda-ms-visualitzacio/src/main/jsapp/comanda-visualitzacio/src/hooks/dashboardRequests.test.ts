import { renderHook, waitFor, act } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useDashboard, useDashboardWidgets } from './dashboardRequests';

const mocks = vi.hoisted(() => ({
    dashboardService: {
        isReady: true,
        getOne: vi.fn(),
        artifactReport: vi.fn(),
    },
    dashboardItemService: {
        isReady: true,
        artifactReport: vi.fn(),
    },
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboard') {
            return mocks.dashboardService;
        }
        if (resourceName === 'dashboardItem') {
            return mocks.dashboardItemService;
        }
        throw new Error(`Resource desconegut: ${resourceName}`);
    },
}));

vi.mock('../components/estadistiques/DashboardReactGridLayout', () => ({
    horizontalSubdivisions: 10,
}));

describe('useDashboard', () => {
    beforeEach(() => {
        mocks.dashboardService.isReady = true;
        mocks.dashboardService.getOne.mockReset();
        mocks.dashboardService.artifactReport.mockReset();
        mocks.dashboardItemService.isReady = true;
        mocks.dashboardItemService.artifactReport.mockReset();
    });

    it('useDashboard_quanLaPeticioEsCorrecta_retornaElDashboardICessaLaCarrega', async () => {
        // Comprova que el hook carrega el dashboard i deixa l'estat final sense loading.
        mocks.dashboardService.getOne.mockResolvedValue({ id: 7, nom: 'Principal' });

        const { result } = renderHook(() => useDashboard(7));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
            expect(result.current.dashboard).toEqual({ id: 7, nom: 'Principal' });
        });
    });

    it('useDashboard_quanLaPeticioFalla_exposaLexcepcioIResetejaElDashboard', async () => {
        // Verifica que el hook propaga l'error a l'estat i elimina el dashboard de la resposta.
        const exception = new Error('boom');
        mocks.dashboardService.getOne.mockRejectedValue(exception);

        const { result } = renderHook(() => useDashboard(9));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
            expect(result.current.dashboard).toBeNull();
            expect(result.current.exception).toBe(exception);
        });
    });
});

describe('useDashboardWidgets', () => {
    beforeEach(() => {
        mocks.dashboardService.isReady = true;
        mocks.dashboardService.artifactReport.mockReset();
        mocks.dashboardItemService.isReady = true;
        mocks.dashboardItemService.artifactReport.mockReset();
    });

    it('useDashboardWidgets_quanCarregaWidgets_elsOrdenaICarregaLesDadesDelsItems', async () => {
        // Comprova que el hook ordena els widgets per posició i enriqueix els no-títol amb les dades de l'item.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 2, tipus: 'GRAFIC', posX: 2, posY: 1 },
            { dashboardItemId: 1, tipus: 'TITOL', posX: 0, posY: 0 },
            { dashboardItemId: 3, tipus: 'TAULA', posX: 1, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport
            .mockResolvedValueOnce([{ dashboardItemId: 2, titol: 'Widget 2', error: true }])
            .mockResolvedValueOnce([{ dashboardItemId: 3, titol: 'Widget 3', error: false }]);

        const { result } = renderHook(() => useDashboardWidgets(12));

        await waitFor(() => {
            expect(result.current.loadingWidgetPositions).toBe(false);
            expect(result.current.loadingWidgetData).toBe(false);
            expect(result.current.dashboardWidgets?.map((widget: any) => widget.dashboardItemId)).toEqual([1, 3, 2]);
            expect(result.current.errorDashboardWidgets).toHaveLength(1);
            expect(result.current.errorDashboardWidgets?.[0].dashboardItemId).toBe(2);
        });
    });

    it('useDashboardWidgets_quanEsForcaRefresh_tornaALlançarLaCarrega', async () => {
        // Verifica que el callback de refresc reutilitza la mateixa lògica i repeteix les peticions.
        mocks.dashboardService.artifactReport.mockResolvedValue([]);

        const { result } = renderHook(() => useDashboardWidgets(15));

        await waitFor(() => {
            expect(mocks.dashboardService.artifactReport).toHaveBeenCalledTimes(1);
        });

        act(() => {
            result.current.forceRefresh();
        });

        await waitFor(() => {
            expect(mocks.dashboardService.artifactReport).toHaveBeenCalledTimes(2);
        });
    });
});
