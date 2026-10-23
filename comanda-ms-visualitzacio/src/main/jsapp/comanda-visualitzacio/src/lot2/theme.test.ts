import { describe, it, expect } from 'vitest';
import { readdirSync, readFileSync, statSync } from 'fs';
import { join } from 'path';
import { lighten } from '@mui/material/styles';
import { createLot2Themes, accentColor } from './theme';

const NOTIB = { main: '#83cb41', dark: '#497e3a', light: '#a8db7a', soft: '#F2F9EB' };

describe('createLot2Themes', () => {
    it('aplica el color base i els modes', () => {
        const themes = createLot2Themes(NOTIB);
        expect(themes.light.palette.primary.main).toBe('#83cb41');
        expect(themes.light.palette.mode).toBe('light');
        expect(themes.dark.palette.mode).toBe('dark');
        expect(themes.dracula.palette.mode).toBe('dark');
    });

    it('accentColor coincideix amb el de Notib', () => {
        const themes = createLot2Themes(NOTIB);
        // valors de Notib: clar = primary.dark, fosc = primary.light
        expect(accentColor(themes.light)).toBe('#497e3a');
        expect(accentColor(themes.dark)).toBe('#a8db7a');
    });

    it('light per defecte és lighten(main, 0.3)', () => {
        const themes = createLot2Themes({ main: NOTIB.main, dark: NOTIB.dark, soft: NOTIB.soft });
        expect(themes.dark.palette.primary.light).toBe(lighten('#83cb41', 0.3));
    });
});

describe('menú lateral al tema', () => {
    const themes = createLot2Themes(NOTIB);
    const drawerPaper = themes.light.components?.MuiDrawer?.styleOverrides?.paper as Record<string, unknown>;
    const serialize = (value: unknown) => JSON.stringify(value);

    it('el contenidor de la consulta de mida és la llista i no el paper', () => {
        // Al paper, el contenidor tallaria el panell flotant (position: fixed) dels submenús del menú plegat
        expect(drawerPaper).not.toHaveProperty('containerType');
        expect(drawerPaper['& .MuiList-root']).toEqual({ containerType: 'inline-size' });
    });

    it('MuiDrawer no té selectors d\'estructura del DOM de la llibreria', () => {
        const drawer = serialize(themes.light.components?.MuiDrawer);
        expect(drawer).not.toContain(':has(');
        expect(drawer).not.toContain('> div >');
        expect(drawer).not.toContain('> .MuiList-root');
        expect(drawer).not.toContain('first-of-type');
        // ni el marge de la icona plegada, que ara centra la llibreria (menuCollapsedWidth)
        expect(drawer).not.toContain('max-width');
    });

    it("l'única excepció d'estructura és el grup desplegat de MuiListItemButton", () => {
        const root = themes.light.components?.MuiListItemButton?.styleOverrides?.root as (
            props: { theme: typeof themes.light }
        ) => Record<string, unknown>;
        const keys = Object.keys(root({ theme: themes.light }));
        expect(keys.filter(k => k.includes(':has('))).toEqual(['&.Mui-selected:has(+ .MuiBox-root)']);
        expect(keys.some(k => k.includes('::before'))).toBe(false);
        // Al codi font (els overrides amb funció no es poden serialitzar)
        const source = readFileSync(join(__dirname, 'theme.ts'), 'utf8');
        expect(source.match(/'[^'\n]*:has\(/g)).toEqual(["'&.Mui-selected:has("]);
        expect(source).not.toMatch(/'[^'\n]*> div >/);
        expect(source).not.toMatch(/'[^'\n]*first-of-type/);
    });
});

describe('carpeta lot2', () => {
    const walk = (dir: string): string[] =>
        readdirSync(dir).flatMap(f => {
            const p = join(dir, f);
            return statSync(p).isDirectory() ? walk(p) : [p];
        });

    it('cap fitxer no importa de fora de lot2', () => {
        const files = walk(__dirname).filter(f => /\.(ts|tsx)$/.test(f) && !f.endsWith('.test.ts'));
        expect(files.length).toBeGreaterThan(0);
        for (const f of files) {
            expect(readFileSync(f, 'utf8'), f).not.toMatch(/from\s+['"]\.\.\//);
            expect(readFileSync(f, 'utf8'), f).not.toMatch(/@programari-limit\/base-react/);
        }
    });
});
