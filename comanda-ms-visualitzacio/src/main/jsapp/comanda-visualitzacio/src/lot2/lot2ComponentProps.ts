import type { Theme } from '@mui/material/styles';
import type { DefaultMuiComponentProps, MuiDataGridProps } from 'reactlib';
import { MAIN_PADDING } from './layout';
import { titleBarSx } from './titleBarControlsSx';

/** Marges del contingut de les graelles i formularis de pantalla: MAIN_PADDING, com la resta de l'aplicació. */
const screenMarginsSx = { m: `${MAIN_PADDING}px` };

/**
 * Propietats per defecte dels components de la llibreria (es passen a `MuiBaseApp` com a
 * `defaultMuiComponentProps`). Substitueixen part del CSS que a Notib posava el ScreenFrame sobre el
 * DOM de la llibreria (selectors `:has(...)` sobre la barra d'eines que contenia un ScreenTitle):
 *  - formularis: la barra d'eines es pinta com a barra de títol (`titleBarSx`) i és enganxosa; aquí
 *    el contenidor amb scroll és el ScreenFrame, de manera que s'hi enganxa a dalt de tot (`top: 0`),
 *    damunt de les capçaleres enganxoses de les graelles dels formularis (`zIndex: 3`). Els marges
 *    dels camps passen a MAIN_PADDING;
 *  - graelles: només el botó de crear amb text. La barra de títol i els marges **no** s'apliquen a
 *    totes les graelles (subtaules, diàlegs de selecció...): la graella principal d'una pantalla els
 *    activa amb `{...lot2ScreenGridProps(theme)}`.
 */
export const lot2DefaultMuiComponentProps = (theme: Theme): DefaultMuiComponentProps => ({
    dataGrid: {
        toolbarCreateVariant: 'button',
    },
    form: {
        toolbarSx: {
            ...titleBarSx(theme),
            top: 0,
            zIndex: 3,
        },
        marginsSx: screenMarginsSx,
    },
});

/**
 * Propietats de la graella principal d'una pantalla (la que porta el ScreenTitle com a `title`):
 * barra d'eines superior pintada com a barra de títol i marges de MAIN_PADDING a la graella i a la
 * fila addicional del filtre. A Notib ho feia el CSS del ScreenFrame; aquí és opt-in:
 *
 * ```tsx
 * <MuiDataGrid title={<ScreenTitle icon={...} title={...} />} {...lot2ScreenGridProps(theme)} ... />
 * ```
 */
export const lot2ScreenGridProps = (theme: Theme): Partial<MuiDataGridProps> => ({
    toolbarType: 'upper',
    toolbarSx: titleBarSx(theme),
    marginsSx: screenMarginsSx,
});
