import { act, renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { useSalutLlistatExpansionState } from './salutState';

describe('useSalutLlistatExpansionState', () => {
    it('useSalutLlistatExpansionState_quanNoShaExpanditRes_retornaFalse', () => {
        // Comprova que l'estat inicial no marca cap grup com expandit.
        const { result } = renderHook(() => useSalutLlistatExpansionState());
        const context = { groupedApp: { id: 1 }, groupedEntorn: { id: 2 } } as any;

        expect(result.current.isExpanded(context)).toBe(false);
    });

    it('useSalutLlistatExpansionState_quanSexpandeixUnGrup_elMarcaComAExpandit', () => {
        // Verifica que activar un context concret el deixa marcat a l'estat intern.
        const { result } = renderHook(() => useSalutLlistatExpansionState());
        const context = { groupedApp: { id: 1 }, groupedEntorn: { id: 2 } } as any;

        act(() => {
            result.current.setExpanded(true, context);
        });

        expect(result.current.isExpanded(context)).toBe(true);
    });

    it('useSalutLlistatExpansionState_quanEsTornaAContraure_lelaiminaDeLEstat', () => {
        // Comprova que desexpandir un context elimina la seva clau del registre intern.
        const { result } = renderHook(() => useSalutLlistatExpansionState());
        const context = { groupedApp: { id: 1 }, groupedEntorn: { id: 2 } } as any;

        act(() => {
            result.current.setExpanded(true, context);
            result.current.setExpanded(false, context);
        });

        expect(result.current.isExpanded(context)).toBe(false);
    });
});
