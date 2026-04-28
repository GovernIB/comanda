import { renderHook, act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import useThrottle from './useThrottle';

describe('useThrottle', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.clearAllMocks();
    });

    it('useThrottle_quanEsCridaRepetidament_limitaLesExecucionsDurantElTempsDeThrottle', () => {
        // Comprova que el hook executa la primera crida immediatament i compacta les següents dins la finestra temporal.
        const callback = vi.fn();
        const { result } = renderHook(() => useThrottle(callback, 100));

        act(() => {
            result.current('primer' as never);
            result.current('segon' as never);
            result.current('tercer' as never);
        });

        expect(callback).toHaveBeenCalledTimes(1);
        expect(callback).toHaveBeenLastCalledWith('primer');

        act(() => {
            vi.advanceTimersByTime(100);
        });

        expect(callback).toHaveBeenCalledTimes(2);
        expect(callback).toHaveBeenLastCalledWith('tercer');
    });

    it('useThrottle_quanCanviaElCallback_usaLaVersioMesRecent', () => {
        // Verifica que el hook invoca sempre el callback actualitzat després d'un rerender.
        const callbackInicial = vi.fn();
        const callbackNou = vi.fn();
        const { result, rerender } = renderHook(({ callback }) => useThrottle(callback, 50), {
            initialProps: { callback: callbackInicial },
        });

        rerender({ callback: callbackNou });

        act(() => {
            result.current('valor' as never);
        });

        expect(callbackInicial).not.toHaveBeenCalled();
        expect(callbackNou).toHaveBeenCalledWith('valor');
    });
});
