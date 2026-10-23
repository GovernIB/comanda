import { createLot2Themes, type Lot2Colors } from './lot2/theme';

/**
 * Identitat de colors de comanda (rosa fosc) per als temes de `lot2`.
 *  - `main`: contrast amb blanc de 5.64:1 (≥ 4.5, text blanc dels botons i de la barra de títol).
 *  - `light`: to clar als temes foscos (accent del menú, pestanyes, barres de desplaçament).
 */
export const COMANDA_COLORS: Lot2Colors = {
    main: '#A8456A',
    dark: '#7A2E4C',
    light: '#cb6c88',
    soft: '#FAEFF3',
};

const comandaThemes = createLot2Themes(COMANDA_COLORS);

export const lightTheme = comandaThemes.light;
export const darkTheme = comandaThemes.dark;
export const draculaTheme = comandaThemes.dracula;
