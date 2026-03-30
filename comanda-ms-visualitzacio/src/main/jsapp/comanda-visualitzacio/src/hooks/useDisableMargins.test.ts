import { renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import useDisableMargins from './useDisableMargins';

const setMarginsDisabledMock = vi.fn();

vi.mock('reactlib', () => ({
    useBaseAppContext: () => ({
        setMarginsDisabled: setMarginsDisabledMock,
    }),
}));

describe('useDisableMargins', () => {
    afterEach(() => {
        setMarginsDisabledMock.mockReset();
    });

    it('useDisableMargins_quanEsMunta_activaLaBanderaSegonsElParametre', () => {
        // Comprova que el hook informa el context del valor de marges desactivats en muntar-se.
        renderHook(() => useDisableMargins(true));

        expect(setMarginsDisabledMock).toHaveBeenCalledWith(true);
    });

    it('useDisableMargins_quanEsDesmunta_restableixLesMargesAPerDefecte', () => {
        // Verifica que el cleanup del hook reactiva les marges en desmuntar-se.
        const { unmount } = renderHook(() => useDisableMargins(true));

        unmount();

        expect(setMarginsDisabledMock).toHaveBeenLastCalledWith(false);
    });
});
