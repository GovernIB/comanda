import { renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import useMonitorDbEnabled, { resolveBooleanParamValue } from './useMonitorDbEnabled';
import { useResourceApiService } from 'reactlib';

vi.mock('reactlib', () => ({
    useResourceApiService: vi.fn(),
}));

const findMock = vi.fn();

const createMockService = (overrides: Partial<ReturnType<typeof useResourceApiService>> = {}) => ({
    isReady: true,
    find: findMock,
    ...overrides,
} as unknown as ReturnType<typeof useResourceApiService>);

beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(useResourceApiService).mockReturnValue(createMockService());
    findMock.mockResolvedValue({ rows: [] });
});

afterEach(() => {
    vi.clearAllMocks();
});

describe('resolveBooleanParamValue', () => {
    it('retorna false si el parámetro es null o undefined', () => {
        expect(resolveBooleanParamValue(null)).toBe(false);
        expect(resolveBooleanParamValue(undefined)).toBe(false);
    });

    it('prioriza valorBoolean si existe', () => {
        expect(resolveBooleanParamValue({ valorBoolean: true, valor: 'false' })).toBe(true);
        expect(resolveBooleanParamValue({ valorBoolean: false, valor: 'true' })).toBe(false);
    });

    it('lee valor si es un booleano', () => {
        expect(resolveBooleanParamValue({ valor: true })).toBe(true);
        expect(resolveBooleanParamValue({ valor: false })).toBe(false);
    });

    it('lee valor si es un string (ignorando espacios y mayúsculas)', () => {
        expect(resolveBooleanParamValue({ valor: 'true' })).toBe(true);
        expect(resolveBooleanParamValue({ valor: 'TRUE' })).toBe(true);
        expect(resolveBooleanParamValue({ valor: ' true ' })).toBe(true);
        expect(resolveBooleanParamValue({ valor: 'false' })).toBe(false);
        expect(resolveBooleanParamValue({ valor: 'cualquier-cosa' })).toBe(false);
        expect(resolveBooleanParamValue({ valor: '' })).toBe(false);
    });

    it('retorna false para tipos de datos no soportados', () => {
        expect(resolveBooleanParamValue({ valor: 1 })).toBe(false);
        expect(resolveBooleanParamValue({ valor: 0 })).toBe(false);
    });
});

describe('useMonitorDbEnabled', () => {
    it('inicialmente retorna undefined mientras la API no está ready', () => {
        vi.mocked(useResourceApiService).mockReturnValueOnce(
            createMockService({ isReady: false })
        );

        const { result } = renderHook(() => useMonitorDbEnabled());

        expect(result.current).toBeUndefined();
        expect(findMock).not.toHaveBeenCalled();
    });

    it('llama a find con los parámetros exactos cuando isReady es true', async () => {
        renderHook(() => useMonitorDbEnabled());

        await waitFor(() => {
            expect(findMock).toHaveBeenCalledWith({
                page: 0,
                size: 1,
                filter: "codi:'es.caib.comanda.monitor.db.actiu'",
            });
        });
    });

    it('retorna true cuando la API devuelve el parámetro activado', async () => {
        findMock.mockResolvedValue({
            rows: [{ codi: 'es.caib.comanda.monitor.db.actiu', valorBoolean: true }],
        });

        const { result } = renderHook(() => useMonitorDbEnabled());

        await waitFor(() => {
            expect(result.current).toBe(true);
        });
    });

    it('retorna false cuando la API devuelve el parámetro desactivado', async () => {
        findMock.mockResolvedValue({
            rows: [{ codi: 'es.caib.comanda.monitor.db.actiu', valor: 'false' }],
        });

        const { result } = renderHook(() => useMonitorDbEnabled());

        await waitFor(() => {
            expect(result.current).toBe(false);
        });
    });

    it('aplica fallback a false cuando la API no encuentra el parámetro (rows vacío)', async () => {
        const { result } = renderHook(() => useMonitorDbEnabled());

        await waitFor(() => {
            expect(result.current).toBe(false);
        });
    });

    it('aplica fallback seguro a false cuando la llamada a la API falla', async () => {
        findMock.mockRejectedValue(new Error('Error de red o servidor'));

        const { result } = renderHook(() => useMonitorDbEnabled());

        await waitFor(() => {
            expect(result.current).toBe(false);
        });
    });
});
