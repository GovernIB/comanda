import { createTheme, alpha, darken, lighten, type Theme } from '@mui/material/styles';
import type {} from '@mui/x-data-grid-pro/themeAugmentation';
// `palette.DataGrid` (headerBg) només és a l'augmentació del paquet base de MUI X
import type {} from '@mui/x-data-grid/themeAugmentation';
import { ICON_SIZE, MENU_COLLAPSED_BELOW, MENU_ICON_COLUMN, MENU_ITEM_PADDING_LEFT } from './layout';

// Estenem les interfícies de MUI per admetre el nou color
declare module '@mui/material/styles' {
  interface Palette {
    customBackground: string;
    greyBackground: string;
    /** Atenuat del color base de l'aplicació: fons de la capçalera de les graelles. */
    primarySoft: string;
  }
  interface PaletteOptions {
    customBackground?: string;
    greyBackground?: string;
    primarySoft?: string;
  }
}

export type Lot2Colors = {
    /** Color base: barra de títol, vora del menú, capçalera de graelles i botons d'acció. */
    main: string;
    /** "Torrat": `primary.dark`; hover, badges, paginació, barres de desplaçament i icona del menú als temes clars. */
    dark: string;
    /** To clar: `primary.light` als temes foscos. Per defecte `lighten(main, 0.3)`. */
    light?: string;
    /** Atenuat opac: `palette.primarySoft`, fons de la capçalera de les graelles al tema clar. */
    soft: string;
};

export type Lot2Themes = { light: Theme; dark: Theme; dracula: Theme };

/**
 * Color "torrat" de la identitat: barres de desplaçament, icona de la barra de títol, icona
 * seleccionada del menú... És `primary.dark` als temes clars; als foscos, un torrat sobre fons fosc no
 * té contrast i es fa servir `primary.light`. Tot el que ha de sortir "torrat" passa per aquí.
 */
export const accentColor = (theme: Theme): string =>
    theme.palette.mode === 'dark' ? theme.palette.primary.light : theme.palette.primary.dark;

/**
 * Color de farciment (fons) dels elements primaris petits amb text a sobre: pàgina seleccionada de la
 * paginació, badges i xips. Als temes clars és el mateix «torrat» (amb text blanc); als foscos el
 * color base de l'aplicació, amb el text que li correspongui per contrast.
 */
export const accentFillColor = (theme: Theme): string =>
    theme.palette.mode === 'dark' ? theme.palette.primary.main : theme.palette.primary.dark;

// Patró de línies en diagonal per al fons
const hatchPattern = (lineColor: string) =>
    `repeating-linear-gradient(45deg, transparent 0, transparent 0.75px, ${lineColor} 1.25px, transparent 1.75px, transparent 3px)`;

// Vora inferior de la capçalera de les graelles: degradat del color base al torrat. El fons de la
// capçalera no es fixa aquí sinó a `palette.DataGrid.headerBg` de cada tema (és la via de MUI X, que
// també acoloreix el filler i el buit de la barra de desplaçament).
//
// Es declara a part, i no dins `baseComponentStyles`, perquè cada tema redefineix
// `MuiDataGrid.styleOverrides` sencer i l'ha de referenciar explícitament.
const gridColumnHeadersOverride = ({ theme }: { theme: Theme }) => ({
    position: 'relative' as const,
    '&::after': {
        content: '""',
        position: 'absolute' as const,
        left: 0,
        right: 0,
        bottom: 0,
        height: '3px',
        background: `linear-gradient(90deg, ${theme.palette.primary.main} 25%, ${accentColor(theme)} 75%)`,
        pointerEvents: 'none' as const,
    },
});

// Barres de desplaçament de tota l'aplicació (i de les graelles, que hereten la propietat) al color
// "torrat" del tema. `scrollbar-color` és herència CSS, per això n'hi ha prou amb posar-lo a `html`.
const scrollbarOverrides = (thumbColor: string) => ({
    html: { scrollbarColor: `${thumbColor} transparent` },
});

// Compartit per tots els temes: els overrides reben el `theme` actiu per paràmetre, per tant no cal
// repetir-los a cada tema.
const baseComponentStyles = {
    MuiTreeItem: {
        styleOverrides: {
            content: ({ theme }: { theme: Theme }) => ({
                '&:hover': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.10),
                },
                '&.Mui-selected': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.15),
                    '&:hover': {
                        backgroundColor: alpha(theme.palette.primary.main, 0.20),
                    },
                    '&.Mui-focused': {
                        backgroundColor: alpha(theme.palette.primary.main, 0.25),
                    },
                },
            }),
        },
    },
    // Menú lateral. La geometria (capçalera de 74px, menú plegat de 74px, icones de 24px, panell
    // flotant dels submenús, colors i marca de l'entrada seleccionada) ja la donen els paràmetres
    // d'aparença de la llibreria (`LOT2_APPEARANCE` i `lot2MenuColors` a menuColors.ts), que també
    // arriben al menú de pantalla estreta pintat en un portal. Aquí només queda el que la llibreria
    // encara no té com a paràmetre: la columna de les icones del menú obert.
    MuiDrawer: {
        styleOverrides: {
            paper: {
                // Contenidor de la consulta de mida: la llista i NO el paper. Un contenidor crea un
                // bloc contenidor per als descendents `position: fixed`, i el panell flotant dels
                // submenús del menú plegat (germà de la llista dins el paper) quedaria tallat.
                '& .MuiList-root': { containerType: 'inline-size' as const },
                // Obert: la llibreria deixa la icona a `padding + marge` (24 + 8 = 32px de vora
                // esquerra), i amb la mida nova el centre cauria a 44px. Es retoca el marge perquè el
                // centre sigui a MENU_ICON_COLUMN, la columna del botó de menú de la capçalera. La
                // consulta fa que no s'apliqui al menú plegat, on la llibreria ja centra la icona
                // (`menuCollapsedWidth`). `menu-item-icon` és la classe pública de la icona del menú.
                [`@container (min-width: ${MENU_COLLAPSED_BELOW + 1}px)`]: {
                    '& .menu-item-icon': {
                        marginLeft: `${MENU_ICON_COLUMN - ICON_SIZE / 2 - MENU_ITEM_PADDING_LEFT}px`,
                    },
                },
            },
        },
    },
    // Identitat: paginació, badges, xips primaris i icona seleccionada del menú en el color "torrat"
    // (veure accentColor). El botó, l'enllaç i la resta de components primaris es queden al base.
    MuiPaginationItem: {
        styleOverrides: {
            root: ({ theme }: { theme: Theme }) => ({
                '&.Mui-selected': {
                    backgroundColor: accentFillColor(theme),
                    color: theme.palette.getContrastText(accentFillColor(theme)),
                    '&:hover': { backgroundColor: accentFillColor(theme), filter: 'brightness(1.15)' },
                },
            }),
        },
    },
    MuiBadge: {
        styleOverrides: {
            badge: ({ theme }: { theme: Theme }) => ({
                '&.MuiBadge-colorPrimary': {
                    backgroundColor: accentFillColor(theme),
                    color: theme.palette.getContrastText(accentFillColor(theme)),
                },
            }),
        },
    },
    MuiChip: {
        styleOverrides: {
            root: ({ theme }: { theme: Theme }) => ({
                '&.MuiChip-colorPrimary:not(.MuiChip-outlined)': {
                    backgroundColor: accentFillColor(theme),
                    color: theme.palette.getContrastText(accentFillColor(theme)),
                },
            }),
        },
    },
    // Pestanya seleccionada als temes foscos en el to clar (accentColor). Al clar es manté el del tema.
    MuiTab: {
        styleOverrides: {
            root: ({ theme }: { theme: Theme }) => ({
                ...(theme.palette.mode === 'dark' && { '&.Mui-selected': { color: accentColor(theme) } }),
            }),
        },
    },
    MuiListItemButton: {
        styleOverrides: {
            root: ({ theme }: { theme: Theme }) => ({
                // `menu-item-icon` és la classe que la llibreria posa al ListItemIcon del menú
                '&.Mui-selected .menu-item-icon': { color: accentColor(theme) },
                // Vora esquerra de l'entrada seleccionada, del color de la barra de títol de les
                // pantalles. La llibreria no la té com a paràmetre (`menuSelectedIndicator` només
                // marca amb fons o amb la línia dels submenús).
                '&.Mui-selected': { boxShadow: `inset 4px 0 0 ${theme.palette.primary.main}` },
                // ÚNIC selector d'estructura que queda: un grup desplegat (seguit del bloc amb les
                // seves entrades) no porta la vora, ja la porta l'entrada seleccionada de dins. La
                // llibreria no marca de cap altra manera que un grup està desplegat; quan ho faci
                // (classe o paràmetre), s'ha de substituir.
                '&.Mui-selected:has(+ .MuiBox-root)': { boxShadow: 'none' },
            }),
        },
    },
};

/**
 * Crea els tres temes (clar, fosc i Dràcula) de l'aplicació a partir de la seva identitat de colors.
 */
export const createLot2Themes = (colors: Lot2Colors): Lot2Themes => {
    const MAIN = colors.main;
    const DARK = colors.dark;
    const LIGHT = colors.light ?? lighten(colors.main, 0.3);
    const SOFT = colors.soft;

    // Files de les graelles als temes foscos (Obscur i Dracula comparteixen el verd base): el hover és un
    // verd suau; la fila seleccionada, un verd fosc, i amb el cursor a sobre una mica més viu.
    const GRID_ROW_HOVER_DARK = alpha(MAIN, 0.2);
    const GRID_ROW_SELECTED_DARK = alpha(darken(MAIN, 0.55), 0.8);
    const GRID_ROW_SELECTED_HOVER_DARK = alpha(darken(MAIN, 0.4), 0.9);

    const LIGHT_CUSTOM_BACKGROUND = '#ffffff';

    const lightTheme = createTheme({
        palette: {
            mode: 'light',
            primary: { main: MAIN, dark: DARK },
            customBackground: LIGHT_CUSTOM_BACKGROUND,
            greyBackground: MAIN,
            primarySoft: SOFT,
            DataGrid: { headerBg: SOFT },
        },
        components: {
            ...baseComponentStyles,
            MuiCssBaseline: {
                styleOverrides: {
                    ...scrollbarOverrides(DARK),
                    body: {
                        backgroundColor: LIGHT_CUSTOM_BACKGROUND,
                        backgroundImage: hatchPattern('#e1e1e1'),
                    },
                },
            },
            MuiDataGrid: {
                styleOverrides: {
                    columnHeaders: gridColumnHeadersOverride,
                },
            },
        },
    });

    const DARK_CUSTOM_BACKGROUND = '#121212';
    // Opac a propòsit: la capçalera de la graella és enganxosa i les files hi passen per sota.
    const DARK_PRIMARY_SOFT = darken(MAIN, 0.7);

    const darkTheme = createTheme({
        palette: {
            mode: 'dark',
            primary: { light: LIGHT, main: MAIN, dark: DARK },
            customBackground: DARK_CUSTOM_BACKGROUND,
            greyBackground: MAIN,
            primarySoft: DARK_PRIMARY_SOFT,
            DataGrid: { headerBg: DARK_PRIMARY_SOFT },
            error: { main: '#c62828', contrastText: '#f8f8f2' },
            warning: { main: '#e65100', contrastText: '#f8f8f2' },
            success: { main: '#1b5e20', contrastText: '#f8f8f2' },
            info: { main: '#01579b', contrastText: '#f8f8f2' },
        },
        components: {
            ...baseComponentStyles,
            MuiCssBaseline: {
                styleOverrides: {
                    ...scrollbarOverrides(LIGHT),
                    body: {
                        backgroundColor: DARK_CUSTOM_BACKGROUND,
                        backgroundImage: hatchPattern('#2c2c2c'),
                    },
                },
            },
            MuiDataGrid: {
                styleOverrides: {
                    root: {
                        '& .MuiDataGrid-row:not(.Mui-selected):hover': {
                            backgroundColor: `${GRID_ROW_HOVER_DARK} !important`,
                        },
                    },
                    row: {
                        '&.Mui-selected': { backgroundColor: `${GRID_ROW_SELECTED_DARK} !important` },
                        '&.Mui-selected:hover': { backgroundColor: `${GRID_ROW_SELECTED_HOVER_DARK} !important` },
                    },
                    columnHeaders: gridColumnHeadersOverride,
                },
            },
        },
    });

    const DRACULA_BACKGROUND = '#282A36';
    // Opac a propòsit: la capçalera de la graella és enganxosa i les files hi passen per sota.
    const DRACULA_PRIMARY_SOFT = darken(MAIN, 0.6);

    // Paleta oficial del tema Dràcula (https://draculatheme.com), amb el color de fons
    // secundari ("paper") i el gris ("greyBackground") ajustats per separar-se de la resta
    // de superfícies i mantenir el contrast WCAG amb el text. El color primari és el de la
    // identitat de l'aplicació, com als altres temes.
    const draculaTheme = createTheme({
        palette: {
            mode: 'dark',
            primary: { light: LIGHT, main: MAIN, dark: DARK },
            secondary: { main: '#F8F8F2' },
            background: { default: DRACULA_BACKGROUND, paper: '#303341' },
            text: { primary: '#F8F8F2', secondary: '#D6D6C2' },
            divider: '#7a7d8b',
            error: { main: '#c62828', contrastText: '#f8f8f2' },
            warning: { main: '#e65100', contrastText: '#f8f8f2' },
            success: { main: '#1b5e20', contrastText: '#f8f8f2' },
            info: { main: '#01579b', contrastText: '#f8f8f2' },
            customBackground: DRACULA_BACKGROUND,
            greyBackground: '#343746',
            primarySoft: DRACULA_PRIMARY_SOFT,
            DataGrid: { headerBg: DRACULA_PRIMARY_SOFT },
        },
        components: {
            ...baseComponentStyles,
            MuiCssBaseline: {
                styleOverrides: {
                    ...scrollbarOverrides(LIGHT),
                    body: {
                        backgroundColor: DRACULA_BACKGROUND,
                        backgroundImage: hatchPattern('#44475a'),
                    },
                },
            },
            MuiButton: {
                styleOverrides: {
                    root: {
                        variants: [
                            {
                                props: ({ color, variant }: any) =>
                                    variant === 'outlined' && color === 'primary',
                                style: {
                                    color: '#F8F8F2',
                                    borderColor: LIGHT,
                                },
                            },
                        ],
                    },
                },
            },
            MuiDataGrid: {
                styleOverrides: {
                    root: {
                        '& .MuiDataGrid-row:not(.Mui-selected):hover': {
                            backgroundColor: `${GRID_ROW_HOVER_DARK} !important`,
                        },
                    },
                    row: {
                        '&.Mui-selected': { backgroundColor: `${GRID_ROW_SELECTED_DARK} !important` },
                        '&.Mui-selected:hover': { backgroundColor: `${GRID_ROW_SELECTED_HOVER_DARK} !important` },
                    },
                    columnHeaders: gridColumnHeadersOverride,
                },
            },
        },
    });

    return { light: lightTheme, dark: darkTheme, dracula: draculaTheme };
};
