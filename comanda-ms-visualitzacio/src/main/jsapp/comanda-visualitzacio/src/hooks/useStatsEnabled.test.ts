import { renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import useStatsEnabled from './useStatsEnabled';

const mocks = vi.hoisted(() => ({
    findMock: vi.fn(),
    isReady: true,
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'parametre') {
            return {
                isReady: mocks.isReady,
                find: mocks.findMock,
            };
        }
        return {
            isReady: true,
        };
    },
}));

describe('useStatsEnabled', () => {
    beforeEach(() => {
        mocks.isReady = true;
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('useStatsEnabled_quanElParametreBooleanEsTrue_retornaTrue', async () => {
        mocks.findMock.mockResolvedValue({ rows: [{ valorBoolean: true, valor: 'false' }] });

        const { result } = renderHook(() => useStatsEnabled());

        await waitFor(() => expect(result.current).toBe(true));
        expect(mocks.findMock).toHaveBeenCalledWith({
            page: 0,
            size: 1,
            filter: "codi:'es.caib.comanda.stats.enabled'",
        });
    });

    it('useStatsEnabled_quanElParametreTextualEsTrue_retornaTrue', async () => {
        mocks.findMock.mockResolvedValue({ rows: [{ valor: ' TRUE ' }] });

        const { result } = renderHook(() => useStatsEnabled());

        await waitFor(() => expect(result.current).toBe(true));
    });

    it('useStatsEnabled_quanElParametreEsFalse_retornaFalse', async () => {
        mocks.findMock.mockResolvedValue({ rows: [{ valorBoolean: false }] });

        const { result } = renderHook(() => useStatsEnabled());

        await waitFor(() => expect(result.current).toBe(false));
    });

    it('useStatsEnabled_quanLaCarregaFalla_retornaFalse', async () => {
        mocks.findMock.mockRejectedValue(new Error('load error'));

        const { result } = renderHook(() => useStatsEnabled());

        await waitFor(() => expect(result.current).toBe(false));
    });
});
