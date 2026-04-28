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

    it('merges on false overlap fail', () => {
        const baseline = ['a', 'b', 'c', 'd', 'e', 'a', 'b', '1', '2', '3'];
        const incoming = ['a', 'b', '1', '2', '3', '4', '5', '6'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['a', 'b', 'c', 'd', 'e', 'a', 'b', '1', '2', '3', '4', '5', '6']);
    })

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

    it('handles multiple partial matches correctly', () => {
        const baseline = ['a', 'b', 'c', 'a', 'b', 'd'];
        const incoming = ['a', 'b', 'd', 'e'];
        // first 'a', 'b' is at index 0, but next is 'c' instead of 'd'.
        // second 'a', 'b' is at index 3, next is 'd', which matches.

        const result = mergeSequentialStringArrays(baseline, incoming);
        expect(result).toEqual(['a', 'b', 'c', 'a', 'b', 'd', 'e']);
    });

    it('returns incoming if it is completely contained in baseline but not at the end', () => {
        const baseline = ['a', 'b', 'c', 'd', 'e'];
        const incoming = ['b', 'c'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['b', 'c']);
    });

    it('prefers extension over containment if both are present', () => {
        // 'a', 'b' is contained at index 0
        // 'a', 'b' also starts an extension at index 2
        const baseline = ['a', 'b', 'x', 'a', 'b'];
        const incoming = ['a', 'b', 'c'];

        const result = mergeSequentialStringArrays(baseline, incoming);
        expect(result).toEqual(['a', 'b', 'x', 'a', 'b', 'c']);
    });

    it('returns baseline merged with incoming if incoming extends baseline', () => {
        const baseline = ['a', 'b', 'c'];
        const incoming = ['a', 'b', 'c', 'd'];

        const result = mergeSequentialStringArrays(baseline, incoming);

        expect(result).toEqual(['a', 'b', 'c', 'd']);
    });
});
