import { describe, expect, it } from 'vitest';
import notNull from './arrayUtils';

describe('notNull', () => {
    it('notNull_quanElValorNoEsNull_retornaTrue', () => {
        // Comprova que el predicat considera vàlid qualsevol valor diferent de null.
        expect(notNull('valor')).toBe(true);
        expect(notNull(0)).toBe(true);
    });

    it('notNull_quanElValorEsNull_retornaFalse', () => {
        // Verifica que el helper filtra específicament els valors null.
        expect(notNull(null)).toBe(false);
    });

    it('notNull_quanSusaEnUnFilter_eliminaElsNullsDeLaColleccio', () => {
        // Comprova l'ús habitual del helper com a predicat de filtratge d'arrays.
        const result = ['a', null, 'b', null, 'c'].filter(notNull);

        expect(result).toEqual(['a', 'b', 'c']);
    });
});
