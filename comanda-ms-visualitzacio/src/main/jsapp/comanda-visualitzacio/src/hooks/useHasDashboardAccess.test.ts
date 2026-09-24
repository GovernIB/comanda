import { renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { UsuariModel } from '../types/usuari.model';
import useHasDashboardAccess from './useHasDashboardAccess';

interface Mocks {
    entornAppFindMock: ReturnType<typeof vi.fn>;
    dashboardFindMock: ReturnType<typeof vi.fn>;
    isEntornAppReady: boolean;
    isDashboardReady: boolean;
    user: UsuariModel | null;
    isUserAdmin: boolean;
}

const mocks = vi.hoisted((): Mocks => ({
    entornAppFindMock: vi.fn(),
    dashboardFindMock: vi.fn(),
    isEntornAppReady: true,
    isDashboardReady: true,
    user: { id: 1, nom: 'Test User' } as unknown as UsuariModel,
    isUserAdmin: false,
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'entornApp') {
            return {
                isReady: mocks.isEntornAppReady,
                find: mocks.entornAppFindMock,
            };
        }
        if (resourceName === 'dashboard') {
            return {
                isReady: mocks.isDashboardReady,
                find: mocks.dashboardFindMock,
            };
        }
        return {
            isReady: true,
            find: vi.fn().mockResolvedValue({ rows: [] }),
        };
    },
}));

vi.mock('../components/UserContext', () => ({
    useUserContext: () => ({ user: mocks.user }),
    useIsUserAdmin: () => mocks.isUserAdmin,
}));

describe('useHasDashboardAccess', () => {
    beforeEach(() => {
        mocks.isEntornAppReady = true;
        mocks.isDashboardReady = true;
        mocks.user = { id: 1, nom: 'Test User' } as unknown as UsuariModel;
        mocks.isUserAdmin = false;
        mocks.entornAppFindMock.mockReset();
        mocks.dashboardFindMock.mockReset();
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('quanUsuariEsAdmin_retornaTrueDirectamentSenseConsultarApi', async () => {
        mocks.isUserAdmin = true;

        const { result } = renderHook(() => useHasDashboardAccess());

        await waitFor(() => expect(result.current).toBe(true));
        expect(mocks.entornAppFindMock).not.toHaveBeenCalled();
        expect(mocks.dashboardFindMock).not.toHaveBeenCalled();
    });

    it('quanUsuariNoEsAdminITePermisDissenyAEntornApp_retornaTrue', async () => {
        mocks.isUserAdmin = false;
        mocks.entornAppFindMock.mockResolvedValue({ rows: [{ id: 10, activa: true }] });
        mocks.dashboardFindMock.mockResolvedValue({ rows: [] });

        const { result } = renderHook(() => useHasDashboardAccess());

        await waitFor(() => expect(result.current).toBe(true));
        expect(mocks.entornAppFindMock).toHaveBeenCalledWith({
            page: 0,
            size: 1,
            namedQueries: ['permis_disseny'],
            filter: 'activa:true and app.activa:true',
        });
        expect(mocks.dashboardFindMock).toHaveBeenCalledWith({
            page: 0,
            size: 1,
            namedQueries: ['WRITE'],
        });
    });

    it('quanUsuariNoTePermisDissenyPeroTeDashboardWrite_retornaTrue', async () => {
        mocks.isUserAdmin = false;
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });
        mocks.dashboardFindMock.mockResolvedValue({ rows: [{ id: 5, titol: 'Dashboard 1' }] });

        const { result } = renderHook(() => useHasDashboardAccess());

        await waitFor(() => expect(result.current).toBe(true));
    });

    it('quanUsuariNoTeNiPermisDissenyNiDashboardWrite_retornaFalse', async () => {
        mocks.isUserAdmin = false;
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });
        mocks.dashboardFindMock.mockResolvedValue({ rows: [] });

        const { result } = renderHook(() => useHasDashboardAccess());

        await waitFor(() => expect(result.current).toBe(false));
    });

    it('quanUserIsNull_noFaConsultesIRetornaUndefined', () => {
        mocks.user = null;

        const { result } = renderHook(() => useHasDashboardAccess());

        expect(result.current).toBeUndefined();
        expect(mocks.entornAppFindMock).not.toHaveBeenCalled();
        expect(mocks.dashboardFindMock).not.toHaveBeenCalled();
    });

    it('quanApiNoEstaReady_noFaConsultes', () => {
        mocks.isEntornAppReady = false;

        const { result } = renderHook(() => useHasDashboardAccess());

        expect(result.current).toBeUndefined();
        expect(mocks.entornAppFindMock).not.toHaveBeenCalled();
        expect(mocks.dashboardFindMock).not.toHaveBeenCalled();
    });

    it('quanLaConsultaFalla_retornaFalse', async () => {
        mocks.isUserAdmin = false;
        mocks.entornAppFindMock.mockRejectedValue(new Error('Network error'));
        mocks.dashboardFindMock.mockRejectedValue(new Error('Network error'));

        const { result } = renderHook(() => useHasDashboardAccess());

        await waitFor(() => expect(result.current).toBe(false));
    });
});
