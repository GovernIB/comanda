import React from 'react';
import { useTheme } from '@mui/material/styles';
import { MuiBaseApp } from 'reactlib';
import type { DefaultMuiComponentProps } from 'reactlib';
import { DrassanaFooter } from './DrassanaFooter';
import { APPBAR_PADDING_LEFT, FOOTER_HEIGHT, MAIN_GAP, MAIN_PADDING } from './layout';
import { lot2DefaultMuiComponentProps } from './lot2ComponentProps';
import { LOT2_APPEARANCE, lot2MenuColors, type Lot2MenuAppearance } from './menuColors';

// `MuiBaseAppProps` no s'exporta des de reactlib.
type MuiBaseAppProps = React.ComponentProps<typeof MuiBaseApp>;

export type Lot2BaseAppProps = Omit<MuiBaseAppProps, 'appearance' | 'footer' | 'footerHeight'> & {
    /** Logo de la capçalera (tema clar). */
    logo: string;
    /** Logo de la capçalera per als temes foscos. Per defecte, `logo`. */
    logoDark?: string;
    /** Estil del menú lateral. Per defecte, `theme`. */
    menuAppearance?: Lot2MenuAppearance;
    /**
     * Peu de pàgina. Per defecte, `<DrassanaFooter />` amb `headerTitle` com a títol quan és un text;
     * si `headerTitle` és un element, el peu per defecte no té títol i l'aplicació l'ha de passar
     * (`footer={<DrassanaFooter title="..." />}`). `false` no en mostra cap.
     */
    footer?: React.ReactElement | false;
};

/**
 * Marc de l'aplicació (BaseApp de Notib): l'arrel ocupa tota l'alçada de la finestra (capçalera dalt,
 * peu baix) i només l'àrea de contingut (`<main>`) fa scroll, amb el padding i la separació entre
 * elements de Notib. Substitueix els marges de la llibreria (`marginsDisabled`).
 */
const LOT2_CONTENT_ROOT_STYLE: React.CSSProperties = { height: '100vh' };
const LOT2_CONTENT_MAIN_STYLE: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    gap: MAIN_GAP + 'px',
    padding: MAIN_PADDING + 'px',
    boxSizing: 'border-box',
    minHeight: 0,
    overflowY: 'auto',
};

/** Combina les props per defecte de `lot2` amb les de l'aplicació: per a cada component guanya l'aplicació. */
const mergeDefaultMuiComponentProps = (
    lot2Props: DefaultMuiComponentProps,
    appProps?: DefaultMuiComponentProps
): DefaultMuiComponentProps => {
    if (appProps == null) return lot2Props;
    const keys = new Set([...Object.keys(lot2Props), ...Object.keys(appProps)]) as Set<
        keyof DefaultMuiComponentProps
    >;
    const merged: DefaultMuiComponentProps = {};
    keys.forEach((key) => {
        merged[key] = { ...lot2Props[key], ...appProps[key] } as any;
    });
    return merged;
};

/**
 * `MuiBaseApp` amb l'aspecte de Lot 2: capçalera i menú plegat de 74px, colors del menú segons
 * `menuAppearance`, marc de contingut de Notib, peu de Drassana i props per defecte dels components
 * de `lot2`. La resta de props (capçalera, extensions de l'usuari, i18n, router...) passen tal qual a
 * `MuiBaseApp`; quan l'aplicació indica `headerAppbarStyle`, `contentRootStyle`, `contentMainStyle`
 * o `defaultMuiComponentProps`, es combinen amb els de `lot2` i guanya l'aplicació.
 */
export const Lot2BaseApp: React.FC<Lot2BaseAppProps> = (props) => {
    const {
        logo,
        logoDark,
        menuAppearance = 'theme',
        footer,
        headerTitle,
        headerAppbarStyle,
        contentRootStyle,
        contentMainStyle,
        defaultMuiComponentProps,
        marginsDisabled = true,
        children,
        ...otherProps
    } = props;
    const theme = useTheme();
    const appearance = React.useMemo(
        () => ({ ...LOT2_APPEARANCE, menuColors: lot2MenuColors(theme, menuAppearance) }),
        [theme, menuAppearance]
    );
    const mergedDefaultMuiComponentProps = React.useMemo(
        () => mergeDefaultMuiComponentProps(lot2DefaultMuiComponentProps(theme), defaultMuiComponentProps),
        [theme, defaultMuiComponentProps]
    );
    const footerComponent =
        footer === false ? undefined : (
            // El peu va per flux, no fixat: l'arrel ocupa l'alçada de la finestra, així que queda
            // sempre a baix sense tapar el contingut.
            <div style={{ height: FOOTER_HEIGHT + 'px', overflow: 'hidden' }}>
                {footer ?? (
                    <DrassanaFooter title={typeof headerTitle === 'string' ? headerTitle : undefined} />
                )}
            </div>
        );
    return (
        <MuiBaseApp
            {...otherProps}
            headerTitle={headerTitle}
            headerLogo={theme.palette.mode === 'dark' && logoDark != null ? logoDark : logo}
            // El botó de menú duu un marge esquerre de -12px: amb aquest padding queda centrat a la
            // columna d'icones del menú plegat.
            headerAppbarStyle={{ paddingLeft: APPBAR_PADDING_LEFT + 'px', ...headerAppbarStyle }}
            appearance={appearance}
            defaultMuiComponentProps={mergedDefaultMuiComponentProps}
            marginsDisabled={marginsDisabled}
            contentRootStyle={{ ...LOT2_CONTENT_ROOT_STYLE, ...contentRootStyle }}
            contentMainStyle={{ ...LOT2_CONTENT_MAIN_STYLE, ...contentMainStyle }}
            footer={footerComponent}
            footerHeight={footerComponent != null ? FOOTER_HEIGHT : 0}>
            {children}
        </MuiBaseApp>
    );
};

export default Lot2BaseApp;
