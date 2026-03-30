import { describe, expect, it } from 'vitest';
import { filterNumericObjectKeys, numericObjectKeys } from './objectUtils';

describe('numericObjectKeys', () => {
    it('numericObjectKeys_quanRepUnObjecteNumeric_retornaLesClausComANombres', () => {
        // Comprova que les claus string d'un objecte indexat es transformen a nombres.
        expect(numericObjectKeys({ 1: 'a', 2: 'b', 7: 'c' })).toEqual([1, 2, 7]);
    });
});

describe('filterNumericObjectKeys', () => {
    it('filterNumericObjectKeys_quanLaClauCompleixElFiltre_laMantéAlResultat', () => {
        // Verifica que el filtre conserva només les claus que compleixen la condició indicada.
        const result = filterNumericObjectKeys({ 1: 'a', 2: 'b', 10: 'c' }, key => Number(key) >= 2);

        expect(result).toEqual({ 2: 'b', 10: 'c' });
    });

    it('filterNumericObjectKeys_quanCapClauCompleixElFiltre_retornaUnObjecteBuit', () => {
        // Comprova el comportament quan cap entrada supera el predicat de filtratge.
        const result = filterNumericObjectKeys({ 1: 'a', 2: 'b' }, key => Number(key) > 10);

        expect(result).toEqual({});
    });
});
