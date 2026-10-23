import { describe, expect, it } from 'vitest';
import { countSpringFilterTerms } from '../lot2';
import { sumSpringFilterTerms } from './springFilterSum';

describe('sumSpringFilterTerms', () => {
    it('sumSpringFilterTerms_senseTermes_tornaUndefined', () => {
        expect(sumSpringFilterTerms(undefined, '')).toBeUndefined();
    });

    it('sumSpringFilterTerms_ambDosFiltres_comptaLaSumaDelsTermes', () => {
        const sum = sumSpringFilterTerms("a:1 and b~'x and y'", '(c:1 and d:2) and e:3');
        // 2 termes + 2 termes (els parèntesis interiors compten com un de sol)
        expect(countSpringFilterTerms(sum)).toBe(4);
    });
});
