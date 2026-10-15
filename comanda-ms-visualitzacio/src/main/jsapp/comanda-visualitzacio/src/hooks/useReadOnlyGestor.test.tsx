import { renderHook } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import useReadOnlyGestor from './useReadOnlyGestor';

vi.mock('../components/UserContext.ts', () => ({
    useIsUserAdmin: vi.fn(),
}));

import { useIsUserAdmin } from '../components/UserContext.ts';

beforeEach(() => {
    vi.clearAllMocks();
});

afterEach(() => {
    vi.clearAllMocks();
});

describe('useReadOnlyGestor', () => {
    it('retorna undefined quan l\'usuari és administrador', () => {
        vi.mocked(useIsUserAdmin).mockReturnValue(true);

        const { result } = renderHook(() => useReadOnlyGestor());

        expect(result.current).toBeUndefined();
    });

    it('retorna true quan l\'usuari NO és administrador', () => {
        vi.mocked(useIsUserAdmin).mockReturnValue(false);

        const { result } = renderHook(() => useReadOnlyGestor());

        expect(result.current).toBe(true);
    });

    it('retorna true quan l\'estat d\'admin és fals (ex: null o undefined)', () => {
        vi.mocked(useIsUserAdmin).mockReturnValue(null as any);

        const { result } = renderHook(() => useReadOnlyGestor());

        expect(result.current).toBe(true);
    });
});
