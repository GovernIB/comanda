import React from 'react';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
import Icon from '@mui/material/Icon';
import IconButton from '@mui/material/IconButton';
import Divider from '@mui/material/Divider';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled, useTheme, Theme, CSSObject } from '@mui/material/styles';
import { useBaseAppContext } from '../BaseAppContext';
import { useSmallScreen, useSmallHeader } from '../../util/useSmallScreen';

export type MenuEntry = {
    id: string;
    title?: string;
    description?: string;
    to?: string;
    icon?: string;
    children?: MenuEntry[];
    divider?: boolean;
};

export type MenuProps = {
    title?: string;
    entries?: MenuEntry[];
    level?: number;
    onTitleClose?: () => void;
    shrink?: boolean;
    iconClicked?: boolean;
    drawerWidth?: number;
    footerHeight?: number;
    compactPanelWidth?: number;
    submenuTitelHeight?: number;
    appearance?: 'theme' | 'inverse' | 'footer';
};

type MenuColorSet = {
    background: string;
    textPrimary: string;
    textSecondary: string;
    divider: string;
    accent: string;
    titleBackground: string;
    selectedBackground: string;
    hoverBackground: string;
};

type ListMenuContentProps = MenuProps & {
    onMenuItemClick?: () => void;
    onEntryClick?: (entry: MenuEntry) => void;
    boldPrimary?: boolean;
    colors?: MenuColorSet;
};

type MenuItemProps = React.PropsWithChildren & {
    entry: MenuEntry;
    primary: string;
    to?: string;
    icon?: string;
    level?: number;
    selected?: boolean;
    shrink?: boolean;
    boldPrimary?: boolean;
    onMenuItemClick?: () => void;
    onEntryClick?: (entry: MenuEntry) => void;
    colors?: MenuColorSet;
};

type MenuTitleProps = {
    title: string;
    onClose?: () => void;
};

const openedMixin = (theme: Theme, width: number | string): CSSObject => ({
    width,
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.enteringScreen,
    }),
});

const closedMixin = (theme: Theme): CSSObject => ({
    transition: theme.transitions.create('width', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    width: `calc(${theme.spacing(6)} + 1px)`,
    [theme.breakpoints.up('sm')]: {
        width: `calc(${theme.spacing(7)} + 1px)`,
    },
});

const ShrinkableDrawer = styled(Drawer, {
    shouldForwardProp: (prop) => prop !== 'open' && prop !== 'width',
})(({ theme, open, ...otherProps }) => {
    const width = (otherProps as any)?.['width'];
    const shrinkableStyles = {
        ...(open && {
            ...openedMixin(theme, width),
            '& .MuiDrawer-paper': openedMixin(theme, width),
        }),
        ...(!open && {
            ...closedMixin(theme),
            whiteSpace: 'nowrap',
            '& .MuiDrawer-paper': closedMixin(theme),
        }),
    };
    return {
        flexShrink: 0,
        boxSizing: 'border-box',
        ...shrinkableStyles,
    };
});

const StyledList = styled(List)<{ component?: React.ElementType }>({
    '& .MuiListItemIcon-root': {
        minWidth: 0,
        marginRight: 16,
    },
    '& .MuiSvgIcon-root': {
        fontSize: 20,
    },
    paddingTop: 0,
    paddingBottom: '8px',
    overflow: 'auto',
    overflowX: 'hidden',
});

const DEFAULT_MENU_ICON = 'menu';
const COMPACT_PANEL_WIDTH = 250;
const SUBMENU_TITEL_HEIGHT = 64;

const getMenuColorSet = (
    theme: Theme,
    appearance: MenuProps['appearance']
): MenuColorSet | undefined => {
    // `theme` no sobreescriu colors: el menú reutilitza directament la paleta activa de MUI.
    if (appearance === 'footer') {
        return {
            background: '#5F5D5D',
            textPrimary: '#F6F6F6',
            textSecondary: '#E5E5E5',
            divider: '#807D7D',
            accent: '#FFFFFF',
            titleBackground: '#4A4848',
            selectedBackground: 'rgba(255, 255, 255, 0.12)',
            hoverBackground: 'rgba(255, 255, 255, 0.08)',
        };
    }
    if (appearance !== 'inverse') {
        return undefined;
    }
    if (theme.palette.mode === 'dark') {
        return {
            background: '#FFFFFF',
            textPrimary: '#1F2937',
            textSecondary: '#4B5563',
            divider: '#D1D5DB',
            accent: '#1976D2',
            titleBackground: '#F3F4F6',
            selectedBackground: 'rgba(25, 118, 210, 0.12)',
            hoverBackground: 'rgba(0, 0, 0, 0.04)',
        };
    }
    return {
        background: '#1E293B',
        textPrimary: '#F8FAFC',
        textSecondary: '#CBD5E1',
        divider: '#475569',
        accent: '#60A5FA',
        titleBackground: '#334155',
        selectedBackground: 'rgba(96, 165, 250, 0.18)',
        hoverBackground: 'rgba(255, 255, 255, 0.08)',
    };
};

const isCurrentMenuEntryOrAnyChildrenSelected = (
    menuEntry: MenuEntry,
    locationPath: string
): boolean => {
    // Un pare s'ha de marcar com a seleccionat si apunta a la ruta actual
    // o si qualsevol descendent coincideix amb la navegació actual.
    const anyChildSelected =
        menuEntry.children?.find((e) => isCurrentMenuEntryOrAnyChildrenSelected(e, locationPath)) !=
        null;
    if (menuEntry.to != null) {
        const menuEntryTo = locationPath.startsWith('/')
            ? menuEntry.to.startsWith('/')
                ? menuEntry.to
                : '/' + menuEntry.to
            : menuEntry.to;
        const selected = menuEntryTo === locationPath || locationPath.startsWith(menuEntryTo + '/');
        return selected || anyChildSelected;
    } else {
        return anyChildSelected;
    }
};

const MenuItem: React.FC<MenuItemProps> = (props) => {
    const {
        entry,
        primary,
        to,
        icon,
        level = 0,
        selected,
        shrink,
        boldPrimary = level === 0,
        onMenuItemClick,
        onEntryClick,
        colors,
        children,
    } = props;
    const { getLinkComponent } = useBaseAppContext();
    const [expanded, setExpanded] = React.useState<boolean>(selected ?? false);
    const itemButtonSx = {
        minHeight: 48,
        justifyContent: !shrink ? 'initial' : 'center',
        color: colors?.textPrimary,
        '&.Mui-selected': {
            backgroundColor: colors?.selectedBackground,
            color: colors?.textPrimary,
        },
        '&.Mui-selected:hover': {
            backgroundColor: colors?.selectedBackground,
        },
        '&:hover': {
            backgroundColor: colors?.hoverBackground,
        },
        '& :before':
            level > 0 && !shrink
                ? {
                      content: '""',
                      display: 'block',
                      position: 'absolute',
                      zIndex: '100',
                      top: '0',
                      left: '40px',
                      height: '100%',
                      width: '2px',
                      opacity: '1',
                      background: selected
                          ? colors?.accent ?? 'hsl(210, 100%, 60%)'
                          : colors?.divider ?? 'hsl(215, 15%, 92%)',
                  }
                : undefined,
    };
    const itemIconSx = {
        minWidth: 0,
        ml: !shrink ? 0 : -1,
        mr: !shrink ? 1 : 'auto',
        justifyContent: 'center',
        color: colors?.textPrimary,
    };
    const itemTextSx = {
        opacity: !shrink ? 1 : 0,
        '& span': {
            fontSize: '14px',
            fontWeight: boldPrimary ? 'bold' : undefined,
            color: level > 0 ? colors?.textSecondary ?? 'text.secondary' : colors?.textPrimary,
        },
    };
    const handleMenuItemClick = () => {
        onEntryClick?.(entry);
        // Les entrades amb fills no naveguen directament: obren o tanquen el seu arbre.
        if (children != null) {
            setExpanded((expanded) => !expanded);
        } else {
            onMenuItemClick?.();
        }
    };
    // En mode compacte només es mostra la icona pròpia. En mode normal, els pares
    // passen a mostrar l'estat expandit/col·lapsat.
    const processedIcon = shrink
        ? icon
        : children != null
          ? expanded
              ? 'expand_more'
              : 'chevron_right'
          : icon;
    const iconComponent = processedIcon ? (
        <ListItemIcon sx={itemIconSx}>
            <Icon fontSize={'small'}>{processedIcon}</Icon>
        </ListItemIcon>
    ) : null;
    return (
        <>
            {(!shrink || !children) && (
                <ListItemButton
                    title={shrink ? primary : undefined}
                    selected={selected}
                    to={children == null ? to : undefined}
                    component={
                        children == null ? (to != null ? getLinkComponent() : undefined) : undefined
                    }
                    onClick={handleMenuItemClick}
                    sx={itemButtonSx}
                    style={{
                        paddingLeft: shrink ? '40px' : 24 + 16 * level + (level > 0 ? 8 : 0) + 'px',
                    }}>
                    {iconComponent}
                    <ListItemText primary={primary} sx={itemTextSx} />
                </ListItemButton>
            )}
            {(shrink || expanded) && children}
        </>
    );
};

const ListMenuContent: React.FC<ListMenuContentProps> = (props) => {
    const { entries, level, shrink, onMenuItemClick, boldPrimary, colors } = props;
    const { useLocationPath } = useBaseAppContext();
    const locationPath = useLocationPath();
    return (
        <StyledList sx={{ backgroundColor: colors?.background, color: colors?.textPrimary }}>
            {entries?.map((item, index) => {
                // Cada nivell recalcula la selecció perquè l'estat visual del pare depèn
                // també del subtree, no només de la seva pròpia ruta.
                const selected = isCurrentMenuEntryOrAnyChildrenSelected(item, locationPath);
                const entryComponent = item.divider ? (
                    <Divider key={index} sx={{ borderColor: colors?.divider }} />
                ) : (
                    <MenuItem
                        key={index}
                        entry={item}
                        primary={item.title ?? ''}
                        to={item.to}
                        icon={item.icon}
                        level={level}
                        selected={selected}
                        shrink={shrink}
                        boldPrimary={boldPrimary}
                        colors={colors}
                        onMenuItemClick={onMenuItemClick}
                        onEntryClick={props.onEntryClick}>
                        {item.children?.length ? (
                            <Box>
                                <ListMenuContent
                                    entries={item.children}
                                    level={(level ?? 0) + 1}
                                    shrink={shrink}
                                    boldPrimary={boldPrimary}
                                    colors={colors}
                                    onMenuItemClick={onMenuItemClick}
                                    onEntryClick={props.onEntryClick}
                                />
                            </Box>
                        ) : null}
                    </MenuItem>
                );
                return entryComponent;
            })}
        </StyledList>
    );
};

const MenuTitle: React.FC<MenuTitleProps & { colors?: MenuColorSet }> = (props) => {
    const { title, onClose, colors } = props;
    const theme = useTheme();
    const handleButtonClick = () => onClose?.();
    return (
        <Box>
            <ListItemButton
                sx={{
                    backgroundColor: colors?.titleBackground ?? theme.palette.grey[200],
                    color: colors?.textPrimary,
                    '&:hover': {
                        backgroundColor: colors?.hoverBackground,
                    },
                }}>
                <ListItemIcon sx={{ minWidth: '40px' }}>
                    <IconButton size="small" onClick={handleButtonClick}>
                        <Icon fontSize={'small'} sx={{ color: colors?.textPrimary }}>clear</Icon>
                    </IconButton>
                </ListItemIcon>
                <ListItemText
                    primary={title}
                    sx={{ '& span': { fontWeight: 'bold', color: colors?.textPrimary } }}
                />
            </ListItemButton>
            <Divider sx={{ borderColor: colors?.divider }} />
        </Box>
    );
};

export const Menu: React.FC<MenuProps> = (props) => {
    const {
        title,
        entries,
        onTitleClose,
        shrink,
        iconClicked,
        drawerWidth = 240,
        footerHeight,
        compactPanelWidth = COMPACT_PANEL_WIDTH,
        submenuTitelHeight = SUBMENU_TITEL_HEIGHT,
        appearance = 'theme',
    } = props;
    const smallScreen = useSmallScreen();
    const smallHeader = useSmallHeader();
    const theme = useTheme();
    const colors = getMenuColorSet(theme, appearance);
    const { getLinkComponent, useLocationPath } = useBaseAppContext();
    const locationPath = useLocationPath();
    const [open, setOpen] = React.useState<boolean>(false);
    const [compactPanelEntryId, setCompactPanelEntryId] = React.useState<string>();
    const compactMenuContainerRef = React.useRef<HTMLDivElement | null>(null);
    const compactPanelRef = React.useRef<HTMLDivElement | null>(null);
    const compactMode = !smallScreen && !!shrink;
    const compactMenuWidth = `calc(${theme.spacing(7)} + 1px)`;
    const compactPanelTop = smallHeader ? theme.spacing(7) : theme.spacing(8);
    const compactPanelBottom = `${footerHeight ?? 0}px`;
    const compactPanelEntry = compactMode
        ? entries?.find((entry) => entry.id === compactPanelEntryId && entry.children?.length)
        : undefined;

    React.useEffect(() => {
        // Al mòbil el menú s'obre i es tanca des de la icona de l'header.
        setOpen((o) => !o);
    }, [iconClicked]);
    React.useEffect(() => {
        // En passar a mòbil o escriptori es reinicia l'estat del drawer temporal.
        setOpen(false);
    }, [smallScreen]);
    React.useEffect(() => {
        // El submenú flotant només existeix en mode compacte d'escriptori.
        if (!compactMode) {
            setCompactPanelEntryId(undefined);
        }
    }, [compactMode]);
    React.useEffect(() => {
        if (!compactMode || !compactPanelEntry) {
            return;
        }
        // Si hi ha un panell flotant obert, qualsevol clic fora del panell i fora
        // de la columna d'icones el tanca.
        const handlePointerDownOutside = (event: MouseEvent) => {
            const target = event.target as Node | null;
            if (
                target != null &&
                (compactPanelRef.current?.contains(target) ||
                    compactMenuContainerRef.current?.contains(target))
            ) {
                return;
            }
            setCompactPanelEntryId(undefined);
        };
        document.addEventListener('mousedown', handlePointerDownOutside);
        return () => {
            document.removeEventListener('mousedown', handlePointerDownOutside);
        };
    }, [compactMode, compactPanelEntry]);
    const handleMenuItemClick = () => {
        // En navegar es tanca tant el drawer mòbil com el submenú compacte obert.
        setOpen(false);
        setCompactPanelEntryId(undefined);
    };
    const handleCompactEntryClick = (entry: MenuEntry) => {
        if (!compactMode) {
            return;
        }
        // En compacte, els elements amb fills obren un segon panell lateral en lloc
        // d'expandir-se dins la mateixa columna.
        if (entry.children?.length) {
            setCompactPanelEntryId((current) => (current === entry.id ? undefined : entry.id));
        } else {
            setCompactPanelEntryId(undefined);
        }
    };
    const drawerContent = (
        <>
            <Box sx={{ mt: smallHeader ? 7 : 8 }} />
            {title && <MenuTitle title={title} onClose={onTitleClose} colors={colors} />}
            {compactMode ? (
                <Box ref={compactMenuContainerRef} sx={{ minHeight: 0, height: '100%' }}>
                    {/* Columna principal del menú compacte: només icones. */}
                    <StyledList
                        sx={{
                            width: compactMenuWidth,
                            backgroundColor: colors?.background,
                            color: colors?.textPrimary,
                        }}>
                        {entries?.map((item, index) => {
                            const selected = compactPanelEntryId === item.id;
                            const routeSelected = isCurrentMenuEntryOrAnyChildrenSelected(item, locationPath);
                            if (item.divider) {
                                return <Divider key={index} sx={{ borderColor: colors?.divider }} />;
                            }
                            return (
                                <ListItemButton
                                    key={index}
                                    title={item.title}
                                    selected={selected || routeSelected}
                                    to={item.children?.length ? undefined : item.to}
                                    component={
                                        !item.children?.length && item.to != null
                                            ? getLinkComponent()
                                            : undefined
                                    }
                                    onClick={() => handleCompactEntryClick(item)}
                                    sx={{
                                        minHeight: 48,
                                        justifyContent: 'center',
                                        px: 0,
                                        color: colors?.textPrimary,
                                        '&.Mui-selected': {
                                            backgroundColor: colors?.selectedBackground,
                                        },
                                        '&.Mui-selected:hover': {
                                            backgroundColor: colors?.selectedBackground,
                                        },
                                        '&:hover': {
                                            backgroundColor: colors?.hoverBackground,
                                        },
                                        '& .MuiListItemIcon-root': {
                                            marginRight: 0,
                                        },
                                    }}>
                                    <ListItemIcon
                                        sx={{
                                            minWidth: 0,
                                            justifyContent: 'center',
                                            color: 'inherit',
                                        }}>
                                        <Icon fontSize="small">
                                            {item.icon ?? DEFAULT_MENU_ICON}
                                        </Icon>
                                    </ListItemIcon>
                                </ListItemButton>
                            );
                        })}
                    </StyledList>
                    {compactPanelEntry ? (
                        <Box
                            ref={compactPanelRef}
                            data-testid="compact-floating-panel"
                            sx={(theme) => ({
                                // El submenú flota al costat del rail compacte perquè
                                // no redimensioni el drawer principal.
                                position: 'fixed',
                                top: compactPanelTop,
                                bottom: compactPanelBottom,
                                left: compactMenuWidth,
                                width: compactPanelWidth,
                                borderLeft: `1px solid ${colors?.divider ?? theme.palette.divider}`,
                                borderRight: `1px solid ${colors?.divider ?? theme.palette.divider}`,
                                backgroundColor: colors?.background ?? theme.palette.background.paper,
                                color: colors?.textPrimary ?? theme.palette.text.primary,
                                overflowY: 'auto',
                                zIndex: theme.zIndex.drawer + 1,
                            })}>
                            <Box
                                sx={(theme) => ({
                                    px: 2,
                                    py: 1.7,
                                    backgroundColor: colors?.titleBackground ?? theme.palette.background.paper,
                                    borderBottom: `1px solid ${colors?.divider ?? theme.palette.divider}`,
                                    minHeight: submenuTitelHeight,
                                })}>
                                <Typography
                                    variant="subtitle2"
                                    sx={() => ({
                                        fontWeight: 700,
                                        color: colors?.accent ?? theme.palette.primary.light,
                                    })}>
                                    {compactPanelEntry.title}
                                </Typography>
                                {compactPanelEntry.description ? (
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            mt: 0.25,
                                            color: colors?.textSecondary ?? 'text.secondary',
                                            fontSize: '0.6rem',
                                        }}>
                                        {compactPanelEntry.description}
                                    </Typography>
                                ) : null}
                            </Box>
                            <ListMenuContent
                                entries={compactPanelEntry.children}
                                level={0}
                                shrink={false}
                                boldPrimary={false}
                                colors={colors}
                                onMenuItemClick={handleMenuItemClick}
                            />
                        </Box>
                    ) : null}
                </Box>
            ) : (
                <ListMenuContent
                    entries={entries}
                    shrink={false}
                    colors={colors}
                    onMenuItemClick={handleMenuItemClick}
                />
            )}
            {footerHeight && <Box sx={{ mb: footerHeight + 'px' }} />}
        </>
    );
    return !smallScreen ? (
        <ShrinkableDrawer
            variant={'permanent'}
            open={!shrink}
            {...{ width: drawerWidth }}
            sx={{
                overflow: compactMode ? 'visible' : undefined,
                '& .MuiDrawer-paper': {
                    backgroundColor: colors?.background,
                    color: colors?.textPrimary,
                    borderRightColor: colors?.divider,
                    overflowX: compactMode ? 'visible' : undefined,
                    overflowY: compactMode ? 'visible' : undefined,
                },
            }}>
            {drawerContent}
        </ShrinkableDrawer>
    ) : (
        <Drawer
            open={open}
            onClose={() => setOpen(false)}
            sx={{
                display: { sm: 'block', md: 'none' },
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: drawerWidth,
                    boxSizing: 'border-box',
                    backgroundColor: colors?.background,
                    color: colors?.textPrimary,
                },
            }}>
            {drawerContent}
        </Drawer>
    );
};

export default Menu;
