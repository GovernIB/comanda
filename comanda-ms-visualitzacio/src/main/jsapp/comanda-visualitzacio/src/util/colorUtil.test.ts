import { describe, expect, it } from 'vitest';
import { createTransparentColor, isWhiteColor } from './colorUtil';

describe('createTransparentColor', () => {
    it('createTransparentColor_quanRepUnHex_retornaUnRgbaAmbLOpacitatIndicada', () => {
        // Comprova que un color hexadecimal es converteix correctament a format rgba.
        expect(createTransparentColor('#112233', 0.4)).toBe('rgba(17, 34, 51, 0.4)');
    });

    it('createTransparentColor_quanRepUnRgba_substitueixNomesLOpacitat', () => {
        // Verifica que un color rgba existent manté els canals i només canvia l'opacitat.
        expect(createTransparentColor('rgba(10, 20, 30, 0.8)', 0.25)).toBe('rgba(10, 20, 30, 0.25)');
    });

    it('createTransparentColor_quanRepUnRgb_afegeixLOpacitatAlFinal', () => {
        // Comprova la conversió de rgb a rgba afegint l'opacitat especificada.
        expect(createTransparentColor('rgb(10, 20, 30)', 0.6)).toBe('rgba(10, 20, 30, 0.6)');
    });

    it('createTransparentColor_quanRepUnColorNominal_retornaElValorOriginal', () => {
        // Verifica que els colors nominals o formats desconeguts es retornen sense modificar.
        expect(createTransparentColor('blue', 0.2)).toBe('blue');
    });
});

describe('isWhiteColor', () => {
    it('isWhiteColor_quanRepUnaVariantDeBlanc_retornaTrue', () => {
        // Comprova que el helper identifica correctament les variants admeses de color blanc.
        expect(isWhiteColor('#FFFFFF')).toBe(true);
        expect(isWhiteColor('#fff')).toBe(true);
        expect(isWhiteColor('white')).toBe(true);
    });

    it('isWhiteColor_quanRepUnColorNoBlanc_retornaFalse', () => {
        // Verifica que qualsevol altre color no es considera blanc.
        expect(isWhiteColor('#000000')).toBe(false);
    });
});
