import { renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { UsuariModel } from '../types/usuari.model';
import useHasSalutAccess from './useHasSalutAccess';

interface Mocks {
    entornAppFindMock: ReturnType<typeof vi.fn>;
    isReady: boolean;
    user: UsuariModel | null;
    isUserAdmin: boolean;
    isUserConsulta: boolean;
    isUserUsuari: boolean;
}

const mocks = vi.hoisted((): Mocks => ({
    entornAppFindMock: vi.fn(),
    isReady: true,
    user: { id: 1, nom: 'Test User' } as unknown as UsuariModel,
    isUserAdmin: false,
    isUserConsulta: false,
    isUserUsuari: false,
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'entornApp') {
            return {
                isReady: mocks.isReady,
                find: mocks.entornAppFindMock,
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
    useIsUserConsulta: () => mocks.isUserConsulta,
    useIsUserUsuari: () => mocks.isUserUsuari,
}));

describe('useHasSalutAccess', () => {
    beforeEach(() => {
        mocks.isReady = true;
        mocks.user = { id: 1, nom: 'Test User' } as unknown as UsuariModel;
        mocks.isUserAdmin = false;
        mocks.isUserConsulta = false;
        mocks.isUserUsuari = false;
        mocks.entornAppFindMock.mockReset();
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('quanUsuariEsAdmin_retornaTrueDirectamentSenseConsultarApi', async () => {
        mocks.isUserAdmin = true;
        mocks.isUserUsuari = false;

        const { result } = renderHook(() => useHasSalutAccess());

        await waitFor(() => expect(result.current).toBe(true));
        expect(mocks.entornAppFindMock).not.toHaveBeenCalled();
    });

    it('quanUsuariEsConsulta_retornaTrueDirectamentSenseConsultarApi', async () => {
        mocks.isUserConsulta = true;
        mocks.isUserUsuari = false;

        const { result } = renderHook(() => useHasSalutAccess());

        await waitFor(() => expect(result.current).toBe(true));
        expect(mocks.entornAppFindMock).not.toHaveBeenCalled();
    });

    it('quanUsuariEsUsuariITePermisos_retornaTrue', async () => {
        mocks.isUserUsuari = true;
        mocks.entornAppFindMock.mockResolvedValue({ rows: [{ id: 1, activa: true }] });

        const { result } = renderHook(() => useHasSalutAccess());

        await waitFor(() => expect(result.current).toBe(true));
        expect(mocks.entornAppFindMock).toHaveBeenCalledWith({
            page: 0,
            size: 1,
            namedQueries: ['permis_salut'],
            filter: 'activa:true and app.activa:true',
        });
    });

    it('quanUsuariEsUsuariINoTePermisos_retornaFalse', async () => {
        mocks.isUserUsuari = true;
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });

        const { result } = renderHook(() => useHasSalutAccess());

        await waitFor(() => expect(result.current).toBe(false));
        expect(mocks.entornAppFindMock).toHaveBeenCalledWith({
            page: 0,
            size: 1,
            namedQueries: ['permis_salut'],
            filter: 'activa:true and app.activa:true',
        });
    });

    it('quanLaConsultaFalla_retornaFalse', async () => {
        mocks.isUserUsuari = true;
        mocks.entornAppFindMock.mockRejectedValue(new Error('Network error'));

        const { result } = renderHook(() => useHasSalutAccess());

        await waitFor(() => expect(result.current).toBe(false));
    });

    it('quanUsuariNoEstaLlest_retornaUndefined', () => {
        mocks.user = null;
        mocks.isUserUsuari = true;

        const { result } = renderHook(() => useHasSalutAccess());

        expect(result.current).toBeUndefined();
        expect(mocks.entornAppFindMock).not.toHaveBeenCalled();
    });
});
