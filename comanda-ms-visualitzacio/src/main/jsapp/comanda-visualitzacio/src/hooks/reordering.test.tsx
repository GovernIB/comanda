import { renderHook, act, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import useReordering from './reordering';

const mocks = vi.hoisted(() => ({
    patch: vi.fn(),
    temporalMessageShow: vi.fn(),
    t: vi.fn((selector: (input: { reordering: { errorMessage: string } }) => string) =>
        selector({ reordering: { errorMessage: 'Error reordenant' } })
    ),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: () => ({
        patch: mocks.patch,
    }),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShow,
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.t,
    }),
}));

describe('useReordering', () => {
    beforeEach(() => {
        mocks.patch.mockReset();
        mocks.temporalMessageShow.mockReset();
        mocks.t.mockClear();
    });

    it('useReordering_quanCanviaLordre_delegaUnPatchAmbLordreDelNouDesti', async () => {
        // Comprova que el hook calcula l'ordre de destí i l'envia al backend amb patch.
        mocks.patch.mockResolvedValue(undefined);
        const { result } = renderHook(() => useReordering('dashboardItem'));

        await act(async () => {
            await result.current.dataGridProps.onRowOrderChange?.(
                {
                    row: { id: 9 },
                    targetIndex: 1,
                } as any,
                {} as any,
                {
                    api: {
                        getAllRowIds: () => [10, 20, 30],
                        getRow: (id: number) => ({ id, ordre: id === 20 ? 7 : 99 }),
                    },
                } as any
            );
        });

        expect(mocks.patch).toHaveBeenCalledWith(9, {
            data: {
                ordre: 7,
            },
        });
    });

    it('useReordering_quanElPatchFalla_mostraUnMissatgeTemporalDError', async () => {
        // Verifica que el hook informa l'usuari quan el backend falla durant la reordenació.
        mocks.patch.mockRejectedValue(new Error('ko'));
        const { result } = renderHook(() => useReordering('dashboardItem'));

        await act(async () => {
            await result.current.dataGridProps.onRowOrderChange?.(
                {
                    row: { id: 12 },
                    targetIndex: 0,
                } as any,
                {} as any,
                {
                    api: {
                        getAllRowIds: () => [10],
                        getRow: () => ({ ordre: 5 }),
                    },
                } as any
            );
        });

        await waitFor(() => {
            expect(mocks.temporalMessageShow).toHaveBeenCalledWith('', 'Error reordenant', 'error');
        });
    });
});
