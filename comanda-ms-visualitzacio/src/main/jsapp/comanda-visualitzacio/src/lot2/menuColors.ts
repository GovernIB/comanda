import { alpha, createTheme, type Theme } from '@mui/material/styles';
import type { MenuColors, MuiBaseAppAppearance } from 'reactlib';
import { HEADER_HEIGHT, ICON_SIZE, MENU_WIDTH_COLLAPSED } from './layout';

/**
 * Estil del menú lateral (el `MenuEstil` de Notib):
 *  - `theme` (TEMA): fa servir directament la paleta activa;
 *  - `inverse` (TEMA_INVERTIT): pren els colors del tema contrari (clar <-> fosc), de manera que el
 *    menú es veu igual que amb `theme` a l'altre tema;
 *  - `footer` (PEU): sempre el gris fosc del peu de l'aplicació.
 * Tots mantenen l'accent en el color base de l'aplicació.
 */
export type Lot2MenuAppearance = 'theme' | 'inverse' | 'footer';

/** Gris del peu de Drassana (fons de `DrassanaFooter` i del menú amb estil `footer`). */
export const DRASSANA_FOOTER_BACKGROUND = '#5F5D5D';

/**
 * Colors del menú lateral per a `MuiBaseAppAppearance.menuColors` (getMenuColorSet de Notib).
 * Amb `theme` no es fixa cap color: la llibreria ja fa servir els del tema actiu.
 *
 * El tema contrari es construeix amb la paleta per defecte de MUI i el mateix color base, que és
 * el que fan els temes clar i fosc de `createLot2Themes` (no en redefineixen fons, text ni divisors).
 */
export const lot2MenuColors = (theme: Theme, appearance: Lot2MenuAppearance): MenuColors => {
    const accent = theme.palette.primary.main;
    if (appearance === 'footer') {
        return {
            background: DRASSANA_FOOTER_BACKGROUND,
            textPrimary: '#F6F6F6',
            textSecondary: '#E5E5E5',
            divider: '#807D7D',
            accent,
            selectedBackground: alpha(accent, 0.25),
            hoverBackground: 'rgba(255, 255, 255, 0.08)',
        };
    }
    if (appearance !== 'inverse') {
        return {};
    }
    const { palette } = createTheme({
        palette: {
            mode: theme.palette.mode === 'dark' ? 'light' : 'dark',
            primary: { main: accent },
        },
    });
    return {
        background: palette.background.paper,
        textPrimary: palette.text.primary,
        textSecondary: palette.action.active,
        divider: palette.divider,
        accent: palette.primary.main,
        selectedBackground: alpha(palette.primary.main, palette.mode === 'dark' ? 0.2 : 0.1),
        hoverBackground: palette.action.hover,
    };
};

/**
 * Mides del marc de Notib per a `MuiBaseAppAppearance`: capçalera de 74px, menú plegat de 74px
 * (icones centrades a la columna de 37px), icones de 24px i entrada seleccionada marcada amb fons.
 * `Lot2BaseApp` hi afegeix els `menuColors`.
 */
export const LOT2_APPEARANCE: MuiBaseAppAppearance = {
    headerHeight: HEADER_HEIGHT,
    menuCollapsedWidth: MENU_WIDTH_COLLAPSED,
    menuIconSize: ICON_SIZE,
    menuSelectedIndicator: 'background',
};
