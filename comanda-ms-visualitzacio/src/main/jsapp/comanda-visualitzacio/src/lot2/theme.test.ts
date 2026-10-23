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
