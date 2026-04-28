import { renderHook, act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import useInterval from './useInterval';

describe('useInterval', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.clearAllMocks();
    });

    it('useInterval_quanHiHaTimeoutValid_executaInitIElTickPeriodic', () => {
        // Comprova que el hook executa la inicialització i després el tick segons la freqüència indicada.
        const init = vi.fn();
        const tick = vi.fn();

        renderHook(() => useInterval({ init, tick, timeout: 100 }));

        expect(init).toHaveBeenCalledTimes(1);
        expect(tick).not.toHaveBeenCalled();

        act(() => {
            vi.advanceTimersByTime(250);
        });

        expect(tick).toHaveBeenCalledTimes(2);
    });

    it('useInterval_quanElTimeoutEsNull_noProgramaCapInterval', () => {
        // Verifica que el hook no fa res quan el timeout no està informat.
        const init = vi.fn();
        const tick = vi.fn();

        renderHook(() => useInterval({ init, tick, timeout: null }));

        act(() => {
            vi.advanceTimersByTime(300);
        });

        expect(init).not.toHaveBeenCalled();
        expect(tick).not.toHaveBeenCalled();
    });

    it('useInterval_quanEsDesmunta_eliminaLIntervalActiu', () => {
        // Comprova que el cleanup del hook atura els ticks un cop el component es desmunta.
        const tick = vi.fn();
        const { unmount } = renderHook(() => useInterval({ tick, timeout: 100 }));

        act(() => {
            vi.advanceTimersByTime(100);
        });
        expect(tick).toHaveBeenCalledTimes(1);

        unmount();

        act(() => {
            vi.advanceTimersByTime(300);
        });

        expect(tick).toHaveBeenCalledTimes(1);
    });
});
