import { describe, it, expect } from 'vitest';
import { mergeSequentialStringArrays } from './stringUtils';

describe('mergeSequentialStringArrays (strict log merge)', () => {
    it('merges when overlap is perfectly sequential', () => {
        const baseline = ['a', 'b', 'c'];
        const incoming = ['b', 'c', 'd'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['a', 'b', 'c', 'd']);
    });

    it('fails merge when overlap contains a mismatch', () => {
        const baseline = ['a', 'b', 'c'];
        const incoming = ['b', 't', 'c'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['b', 't', 'c']);
    });

    it('fails merge when overlap diverges midway', () => {
        const baseline = ['a', 'b', 'c', 'd'];
        const incoming = ['b', 'c', 'x'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['b', 'c', 'x']);
    });

    it('returns incoming when no overlap start exists', () => {
        const baseline = ['a', 'b', 'c'];
        const incoming = ['x', 'y'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['x', 'y']);
    });

    it('merges when incoming is a full suffix', () => {
        const baseline = ['a', 'b', 'c'];
        const incoming = ['c'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['a', 'b', 'c']);
    });

    it('returns baseline when incoming is empty', () => {
        const baseline = ['a', 'b'];
        const incoming: string[] = [];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['a', 'b']);
    });

    it('returns incoming when baseline is empty', () => {
        const baseline: string[] = [];
        const incoming = ['a', 'b'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['a', 'b']);
    });
});
