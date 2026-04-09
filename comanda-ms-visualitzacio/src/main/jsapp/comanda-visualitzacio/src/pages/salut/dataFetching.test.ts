import { act, renderHook, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useAppInfoData } from './dataFetching';

const mocks = vi.hoisted(() => ({
    useResourceApiServiceMock: vi.fn(),
    useIsUserAdminMock: vi.fn(),
    entornAppGetOneMock: vi.fn(),
    salutFindMock: vi.fn(),
    salutReportMock: vi.fn(),
    agrupacioFromMinutesMock: vi.fn(),
    andMock: vi.fn((...parts: string[]) => parts.join(' && ')),
    eqMock: vi.fn((field: string, value: string) => `${field}=${value}`),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => mocks.useResourceApiServiceMock(resourceName),
    springFilterBuilder: {
        and: (...parts: string[]) => mocks.andMock(...parts),
        eq: (field: string, value: string) => mocks.eqMock(field, value),
    },
}));

vi.mock('../../components/salut/SalutToolbar', () => ({
    agrupacioFromMinutes: (minutes: number) => mocks.agrupacioFromMinutesMock(minutes),
}));

vi.mock('../../components/UserContext.ts', () => ({
    useIsUserAdmin: () => mocks.useIsUserAdminMock(),
}));

vi.mock('../../util/dateUtils', () => ({
    ISO_DATE_FORMAT: 'YYYY-MM-DD',
}));

describe('useAppInfoData', () => {
    beforeEach(() => {
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.agrupacioFromMinutesMock.mockReturnValue('HORA');
        mocks.entornAppGetOneMock.mockResolvedValue({ id: 77, nom: 'App prova' });
        mocks.salutReportMock.mockImplementation((_unused: unknown, request: { code: string }) => {
            if (request.code === 'grups_dates') {
                return Promise.resolve([{ data: '2026-03-12' }, { data: '2026-03-13' }]);
            }
            if (request.code === 'estat') {
                return Promise.resolve([{ data: '2026-03-13', upPercent: 80 }]);
            }
            if (request.code === 'latencia') {
                return Promise.resolve([{ data: '2026-03-13', latenciaMitja: 120 }]);
            }
            return Promise.resolve([]);
        });
        mocks.salutFindMock.mockResolvedValue({
            rows: [{ entornAppId: 77, appEstat: 'UP' }],
        });
        mocks.useResourceApiServiceMock.mockImplementation((resourceName: string) => {
            if (resourceName === 'entornApp') {
                return {
                    isReady: true,
                    getOne: mocks.entornAppGetOneMock,
                };
            }
            return {
                isReady: true,
                find: mocks.salutFindMock,
                artifactReport: mocks.salutReportMock,
            };
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('useAppInfoData_quanNoHiHaId_reinicialitzaLEstat', async () => {
        // Comprova que sense id seleccionat el hook torna a l'estat inicial.
        const { result } = renderHook(() => useAppInfoData(null, 60));

        await waitFor(() => {
            expect(result.current.entornApp).toBeNull();
        });

        expect(result.current.loading).toBeNull();
        expect(result.current.grupsDates).toBeNull();
    });

    it('useAppInfoData_quanLaCarregaEsCorrecta_retornaLesDadesAgregades', async () => {
        // Verifica que el hook combina les respostes de recursos i reports en un únic estat coherent.
        const { result } = renderHook(() => useAppInfoData(77, 60));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        expect(result.current.ready).toBe(true);
        expect(result.current.entornApp).toEqual({ id: 77, nom: 'App prova' });
        expect(result.current.agrupacio).toBe('HORA');
        expect(result.current.grupsDates).toEqual(['2026-03-12', '2026-03-13']);
        expect(result.current.estats).toEqual({ 77: [{ data: '2026-03-13', upPercent: 80 }] });
        expect(result.current.latencies).toEqual([{ data: '2026-03-13', latenciaMitja: 120 }]);
        expect(mocks.salutFindMock).toHaveBeenCalledWith(
            expect.objectContaining({
                perspectives: [
                    'SAL_INTEGRACIONS',
                    'SAL_SUBSISTEMES',
                    'SAL_CONTEXTS',
                    'SAL_MISSATGES',
                    'SAL_DETALLS',
                    'SAL_ULTIM_ESTAT_OPERATIU_INFO',
                ],
                filter: "tipusRegistre='MINUT' && entornAppId='77'",
            })
        );
    });

    it('useAppInfoData_quanLusuariEsAdmin_demanaTambeLaPerspectivaDhistoric', async () => {
        mocks.useIsUserAdminMock.mockReturnValue(true);

        const { result } = renderHook(() => useAppInfoData(77, 60));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        expect(mocks.salutFindMock).toHaveBeenCalledWith(
            expect.objectContaining({
                perspectives: [
                    'SAL_INTEGRACIONS',
                    'SAL_SUBSISTEMES',
                    'SAL_CONTEXTS',
                    'SAL_MISSATGES',
                    'SAL_DETALLS',
                    'SAL_HISTORICS',
                    'SAL_ULTIM_ESTAT_OPERATIU_INFO',
                ],
            })
        );
    });

    it('useAppInfoData_quanHiHaError_guardaleSErrorIAturaLaCarrega', async () => {
        // Comprova que qualsevol error de càrrega es reflecteix a l'estat final del hook.
        const error = new Error('boom');
        mocks.entornAppGetOneMock.mockRejectedValue(error);

        const { result } = renderHook(() => useAppInfoData(77, 60));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        expect(result.current.error).toBe(error);
        expect(result.current.entornApp).toBeNull();
    });

    it('useAppInfoData_quanEsCridaRefresh_tornaAExecutarLaSeqüenciaDeCarrega', async () => {
        // Verifica que el `refresh` manual reaprofita la mateixa seqüència i torna a consultar les dades.
        const { result } = renderHook(() => useAppInfoData(77, 60));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
        });

        const initialCalls = mocks.entornAppGetOneMock.mock.calls.length;

        await act(async () => {
            await result.current.refresh();
        });

        expect(mocks.entornAppGetOneMock.mock.calls.length).toBeGreaterThan(initialCalls);
    });
});
