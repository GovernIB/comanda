import { renderHook, act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import useSizeTracker from './useSizeTracker';

vi.mock('./useThrottle.ts', () => ({
    default: <T extends (...args: never[]) => void>(callback: T) => callback,
}));

const resizeObserverState = vi.hoisted(() => ({
    callback: null as null | ResizeObserverCallback,
    observe: vi.fn(),
    disconnect: vi.fn(),
}));

class ResizeObserverMock {
    constructor(callback: ResizeObserverCallback) {
        resizeObserverState.callback = callback;
    }

    observe = resizeObserverState.observe;
    disconnect = resizeObserverState.disconnect;
}

describe('useSizeTracker', () => {
    beforeEach(() => {
        resizeObserverState.callback = null;
        resizeObserverState.observe.mockClear();
        resizeObserverState.disconnect.mockClear();
        vi.stubGlobal('ResizeObserver', ResizeObserverMock);
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('useSizeTracker_quanResizeObserverRepUnaMida_actualitzaLEstatAmbAmpladaIAlcada', () => {
        // Comprova que el hook actualitza la mida quan el ResizeObserver notifica un canvi.
        const { result } = renderHook(() => useSizeTracker());
        const node = document.createElement('div');

        act(() => {
            result.current.refCallback(node);
        });

        expect(resizeObserverState.observe).toHaveBeenCalledWith(node);

        act(() => {
            resizeObserverState.callback?.([
                {
                    contentRect: { width: 320, height: 180 },
                } as ResizeObserverEntry,
            ], {} as ResizeObserver);
        });

        expect(result.current.size).toEqual({ width: 320, height: 180 });
    });

    it('useSizeTracker_quanEsFaCleanup_desconnectaLobserverActiu', () => {
        // Verifica que el cleanup retornat pel ref callback desconnecta el ResizeObserver.
        const { result } = renderHook(() => useSizeTracker());
        const node = document.createElement('div');
        const refCallbackWithCleanup = result.current.refCallback as unknown as (
            element: HTMLElement | null
        ) => void | (() => void);

        const cleanup = refCallbackWithCleanup(node);
        if (typeof cleanup === 'function') {
            cleanup();
        }

        expect(resizeObserverState.disconnect).toHaveBeenCalledTimes(1);
    });
});
