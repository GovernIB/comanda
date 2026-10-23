import { describe, expect, it } from 'vitest';
import { COMANDA_COLORS, darkTheme, draculaTheme, lightTheme } from './theme';

// Luminància relativa i contrast segons WCAG 2.x (https://www.w3.org/TR/WCAG21/#dfn-relative-luminance)
const relativeLuminance = (hex: string): number => {
    const value = hex.replace('#', '');
    const full = value.length === 3 ? value.split('').map((c) => c + c).join('') : value;
    const [r, g, b] = [0, 2, 4].map((i) => parseInt(full.substring(i, i + 2), 16) / 255).map((c) =>
        c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4)
    );
    return 0.2126 * r + 0.7152 * g + 0.0722 * b;
};

const contrastRatio = (a: string, b: string): number => {
    const [l1, l2] = [relativeLuminance(a), relativeLuminance(b)].sort((x, y) => y - x);
    return (l1 + 0.05) / (l2 + 0.05);
};

describe('theme de comanda', () => {
    it('el to clar és el rosa de comanda', () => {
        expect(COMANDA_COLORS.light).toBe('#cb6c88');
    });

    it('el color base té un contrast amb blanc de com a mínim 4.5:1', () => {
        expect(contrastRatio(COMANDA_COLORS.main, '#fff')).toBeGreaterThanOrEqual(4.5);
    });

    it('el torrat també té un contrast amb blanc de com a mínim 4.5:1 (badges i paginació al tema clar)', () => {
        expect(contrastRatio(COMANDA_COLORS.dark, '#fff')).toBeGreaterThanOrEqual(4.5);
    });

    it('els tres temes tenen el primari derivat de COMANDA_COLORS', () => {
        for (const theme of [lightTheme, darkTheme, draculaTheme]) {
            expect(theme.palette.primary.main).toBe(COMANDA_COLORS.main);
            expect(theme.palette.primary.dark).toBe(COMANDA_COLORS.dark);
        }
        expect(darkTheme.palette.primary.light).toBe(COMANDA_COLORS.light);
        expect(draculaTheme.palette.primary.light).toBe(COMANDA_COLORS.light);
        expect(lightTheme.palette.primarySoft).toBe(COMANDA_COLORS.soft);
    });

    it('els botons primaris (crear) porten text blanc sobre el color base', () => {
        for (const theme of [lightTheme, darkTheme, draculaTheme]) {
            expect(theme.palette.primary.contrastText).toBe('#fff');
        }
    });

    it("als temes foscos, l'accent (to clar) es distingeix del fons (≥ 3:1, components no textuals)", () => {
        expect(contrastRatio(COMANDA_COLORS.light!, darkTheme.palette.background.default)).toBeGreaterThanOrEqual(3);
        expect(contrastRatio(COMANDA_COLORS.light!, draculaTheme.palette.background.default)).toBeGreaterThanOrEqual(3);
        expect(contrastRatio(COMANDA_COLORS.light!, draculaTheme.palette.background.paper)).toBeGreaterThanOrEqual(3);
    });
});
