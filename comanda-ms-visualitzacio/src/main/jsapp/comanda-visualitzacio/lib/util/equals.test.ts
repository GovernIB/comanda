import { describe, expect, it } from 'vitest';
import { deepEqual } from './equals';

describe('deepEqual', () => {
    it('retorna true per a objectes iguals', () => {
        expect(deepEqual({ a: 1, b: { c: 2 } }, { a: 1, b: { c: 2 } })).toBe(true);
    });
    it('retorna false per a objectes diferents', () => {
        expect(deepEqual({ a: 1, b: { c: 2 } }, { a: 1, b: { c: 3 } })).toBe(false);
    });
});
