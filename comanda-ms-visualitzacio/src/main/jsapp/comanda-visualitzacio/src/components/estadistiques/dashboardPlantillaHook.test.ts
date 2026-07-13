import { renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { useDashboardPlantilla, useEntornCodi } from './dashboardPlantillaHook';

const mocks = vi.hoisted(() => ({
    getPlantillaMock: vi.fn(),
    getEntornMock: vi.fn(),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'plantilla') {
            return { isReady: true, getOne: mocks.getPlantillaMock, };
        }
        if (resourceName === 'entorn') {
            return { isReady: true, getOne: mocks.getEntornMock, };
        }
        return { isReady: true, getOne: vi.fn() };
    },
}));

describe('useDashboardPlantilla', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('useDashboardPlantilla_quanNoHiHaPlantillaId_retornaNull', () => {
        const { result } = renderHook(() => useDashboardPlantilla(undefined));

        expect(result.current.plantilla).toBeNull();
        expect(result.current.loading).toBe(false);
        expect(mocks.getPlantillaMock).not.toHaveBeenCalled();
    });

    it('useDashboardPlantilla_quanHiHaPlantillaId_carregaLaPlantilla', async () => {
        const mockPlantilla = { id: 10, nom: 'Plantilla Test' };
        mocks.getPlantillaMock.mockResolvedValue(mockPlantilla);

        const { result } = renderHook(() => useDashboardPlantilla(10));

        expect(result.current.loading).toBe(true);

        await waitFor(() => {
            expect(result.current.plantilla).toEqual(mockPlantilla);
            expect(result.current.loading).toBe(false);
        });

        expect(mocks.getPlantillaMock).toHaveBeenCalledWith(10);
    });

    it('useDashboardPlantilla_quanLaPeticioFalla_retornaNull', async () => {
        mocks.getPlantillaMock.mockRejectedValue(new Error('API Error'));
        vi.spyOn(console, 'error').mockImplementation(() => {});

        const { result } = renderHook(() => useDashboardPlantilla(10));

        await waitFor(() => {
            expect(result.current.plantilla).toBeNull();
            expect(result.current.loading).toBe(false);
        });
    });
});

describe('useEntornCodi', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('useEntornCodi_quanNoHiHaEntornId_retornaUndefined', () => {
        const { result } = renderHook(() => useEntornCodi(undefined));

        expect(result.current.entornCodi).toBeUndefined();
        expect(result.current.loading).toBe(false);
        expect(mocks.getEntornMock).not.toHaveBeenCalled();
    });

    it('useEntornCodi_quanHiHaEntornId_carregaElCodi', async () => {
        mocks.getEntornMock.mockResolvedValue({ id: 5, codi: 'ENT_TEST' });

        const { result } = renderHook(() => useEntornCodi(5));

        expect(result.current.loading).toBe(true);

        await waitFor(() => {
            expect(result.current.entornCodi).toBe('ENT_TEST');
            expect(result.current.loading).toBe(false);
        });

        expect(mocks.getEntornMock).toHaveBeenCalledWith(5);
    });

    it('useEntornCodi_quanLaPeticioFalla_retornaUndefined', async () => {
        mocks.getEntornMock.mockRejectedValue(new Error('API Error'));
        vi.spyOn(console, 'error').mockImplementation(() => {});

        const { result } = renderHook(() => useEntornCodi(5));

        await waitFor(() => {
            expect(result.current.entornCodi).toBeUndefined();
            expect(result.current.loading).toBe(false);
        });
    });
});