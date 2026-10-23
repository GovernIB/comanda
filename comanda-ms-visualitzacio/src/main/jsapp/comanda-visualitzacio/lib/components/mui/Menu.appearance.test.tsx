import { render } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { Menu } from './Menu';
import { BaseAppContext } from '../BaseAppContext';
import { MuiBaseAppContext, MuiBaseAppAppearance } from './MuiBaseAppContext';
import * as smallScreen from '../../util/useSmallScreen';

const baseCtx: any = {
    getLinkComponent: () => 'a',
    useLocationPath: () => '/p/a',
};

const entries: any[] = [
    {
        id: 'p',
        title: 'Pare',
        icon: 'home',
        children: [
            { id: 'a', title: 'A', icon: 'star', to: '/p/a' },
            { id: 'b', title: 'B', icon: 'star', to: '/p/b' },
        ],
    },
];

const renderMenu = (appearance: MuiBaseAppAppearance, shrink = false) =>
    render(
        <BaseAppContext.Provider value={baseCtx}>
            <MuiBaseAppContext.Provider value={{ defaultMuiComponentProps: {}, appearance }}>
                <Menu entries={entries} shrink={shrink} />
            </MuiBaseAppContext.Provider>
        </BaseAppContext.Provider>
    );

const cssText = () =>
    Array.from(document.querySelectorAll('style'))
        .map((s) => s.textContent)
        .join('\n');
// Regles :before dels botons renderitzats (els estils d'emotion s'acumulen entre tests)
const lineRules = () => {
    const classes = Array.from(document.querySelectorAll('.MuiListItemButton-root')).flatMap((e) =>
        Array.from(e.classList).filter((c) => c.startsWith('css-'))
    );
    return (cssText().match(/[^}]*:before\{[^}]*\}/g) ?? []).filter((r) =>
        classes.some((c) => r.includes(`.${c}>`))
    );
};

afterEach(() => vi.restoreAllMocks());

describe('Menu aparença per defecte', () => {
    it('amplada plegada, icona i línia com fins ara', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        const { container } = renderMenu({}, true);
        const paper = container.querySelector('.MuiDrawer-paper') as HTMLElement;
        expect(paper.className).toBeTruthy();
        expect(cssText()).toContain('width:calc(48px + 1px)');
        expect(cssText()).toContain('width:calc(56px + 1px)');
        expect(cssText()).toMatch(/\.MuiSvgIcon-root\{[^}]*font-size:20px/);
    });
    it('la línia del subelement seleccionat és hsl(210, 100%, 60%)', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        renderMenu({});
        const rules = lineRules().join('\n');
        expect(rules).toContain('background:hsl(210, 100%, 60%)');
        expect(rules).toContain('background:hsl(215, 15%, 92%)');
    });
});

describe('Menu amb aparença', () => {
    const appearance: MuiBaseAppAppearance = {
        menuCollapsedWidth: 74,
        menuIconSize: 24,
        menuSelectedColor: '#f00',
        menuSelectedIndicator: 'background',
        menuColors: { background: '#111', selectedBackground: '#222' },
    };
    it('amplada plegada, mida de les icones i colors', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        const { container } = renderMenu(appearance, true);
        const paper = container.querySelector('.MuiDrawer-paper') as HTMLElement;
        expect(getComputedStyle(paper).width).toBe('74px');
        expect(getComputedStyle(paper).backgroundColor).toBe('rgb(17, 17, 17)');
        const icon = container.querySelector('.MuiIcon-root') as HTMLElement;
        expect(getComputedStyle(icon).fontSize).toBe('24px');
    });
    it("amb indicador 'background' no hi ha línia i el seleccionat té fons", () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        const { container } = renderMenu(appearance);
        expect(lineRules().join('')).toBe('');
        const selected = container.querySelector(
            '.Mui-selected.MuiListItemButton-root'
        ) as HTMLElement;
        expect(selected).toBeTruthy();
        expect(getComputedStyle(selected).backgroundColor).toBe('rgb(34, 34, 34)');
    });
    it("amb indicador 'none' no hi ha línia", () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        renderMenu({ menuSelectedIndicator: 'none' });
        expect(lineRules().join('')).toBe('');
    });
    it('menuSelectedColor en mode línia', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        renderMenu({ menuSelectedColor: '#f00' });
        expect(lineRules().join('')).toContain('background:#f00');
    });
    it('menuColors: text, icones, hover i divisor', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        const { container } = renderMenu({
            menuColors: { textPrimary: '#fff', textSecondary: '#ccc', hoverBackground: '#333' },
        });
        const paper = container.querySelector('.MuiDrawer-paper') as HTMLElement;
        expect(getComputedStyle(paper).color).toBe('rgb(255, 255, 255)');
        const ic = container.querySelector('.menu-item-icon') as HTMLElement;
        expect(getComputedStyle(ic).color).toBe('rgb(204, 204, 204)');
        expect(cssText()).toMatch(/:hover\{[^}]*background-color:#333/);
    });
    it('sense menuColors el fons del paper és el de MUI', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        const { container } = renderMenu({});
        const paper = container.querySelector('.MuiDrawer-paper') as HTMLElement;
        expect(getComputedStyle(paper).backgroundColor).not.toBe('rgb(17, 17, 17)');
    });

    describe('icona de l\'entrada seleccionada', () => {
        // Tema amb un override que pinta la icona seleccionada de vermell (com el de lot2): l'accent l'ha de guanyar.
        const themeWithOverride = createTheme({
            components: {
                MuiListItemButton: {
                    styleOverrides: { root: { '&.Mui-selected .menu-item-icon': { color: '#f00' } } },
                },
            },
        });
        const renderWithTheme = (a: MuiBaseAppAppearance) =>
            render(
                <ThemeProvider theme={themeWithOverride}>
                    <BaseAppContext.Provider value={baseCtx}>
                        <MuiBaseAppContext.Provider value={{ defaultMuiComponentProps: {}, appearance: a }}>
                            <Menu entries={entries} />
                        </MuiBaseAppContext.Provider>
                    </BaseAppContext.Provider>
                </ThemeProvider>
            );
        const iconOf = (c: HTMLElement, title: string) =>
            Array.from(c.querySelectorAll('.MuiListItemButton-root'))
                .find((b) => b.textContent?.includes(title))!
                .querySelector('.menu-item-icon') as HTMLElement;
        it("amb accent, la icona seleccionada pren l'accent (i guanya l'override del tema)", () => {
            vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
            const { container } = renderWithTheme({
                menuColors: { textSecondary: '#ccc', accent: '#0f0' },
            });
            expect(getComputedStyle(iconOf(container, 'A')).color).toBe('rgb(0, 255, 0)');
            expect(getComputedStyle(iconOf(container, 'B')).color).toBe('rgb(204, 204, 204)');
        });
        it("sense accent, la icona seleccionada no canvia (la pinta el tema)", () => {
            vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
            const { container } = renderWithTheme({ menuColors: { textSecondary: '#ccc' } });
            expect(getComputedStyle(iconOf(container, 'A')).color).toBe('rgb(255, 0, 0)');
        });
    });
});
