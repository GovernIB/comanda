import { act, renderHook } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import useAppliedSpringFilter from './useAppliedSpringFilter';

describe('useAppliedSpringFilter', () => {
    it('useAppliedSpringFilter_quanCanviaElFiltre_elRecordaIAvisaElCallback', () => {
        const onChange = vi.fn();
        const { result } = renderHook(() => useAppliedSpringFilter(onChange));
        expect(result.current.appliedSpringFilter).toBeUndefined();

        act(() => result.current.handleSpringFilterChange('a:1'));

        expect(result.current.appliedSpringFilter).toBe('a:1');
        expect(onChange).toHaveBeenCalledWith('a:1');
    });

    it('useAppliedSpringFilter_senseCallback_nomesElRecorda', () => {
        const { result } = renderHook(() => useAppliedSpringFilter());
        act(() => result.current.handleSpringFilterChange('b:2'));
        expect(result.current.appliedSpringFilter).toBe('b:2');
    });
});
