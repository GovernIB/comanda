import { render, fireEvent } from '@testing-library/react';
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
    { id: 'h', title: 'Inici', icon: 'home', to: '/h' },
    { id: 'd', icon: '', divider: true },
    {
        id: 'p',
        title: 'Pare',
        icon: 'folder',
        children: [
            { id: 'a', title: 'A', icon: 'star', to: '/p/a' },
            { id: 'd2', icon: '', divider: true },
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

const menuLists = () => Array.from(document.querySelectorAll('ul.MuiList-root'));
const directChildTags = () =>
    menuLists().flatMap((ul) => Array.from(ul.children).map((c) => c.tagName));

afterEach(() => vi.restoreAllMocks());

describe('Menu sense menuListItemSemantics (per defecte)', () => {
    it("els fills de l'ul són com fins ara (cap li)", () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        renderMenu({});
        const tags = directChildTags();
        expect(tags.length).toBeGreaterThan(0);
        expect(tags).not.toContain('LI');
        expect(tags).toContain('HR');
        // El grup desplegat: botó seguit de la Box dels fills, fills directes de l'ul
        const selectedGroup = Array.from(document.querySelectorAll('.Mui-selected')).find((b) =>
            b.textContent?.includes('Pare')
        ) as HTMLElement;
        expect(selectedGroup.parentElement?.tagName).toBe('UL');
        expect(selectedGroup.nextElementSibling?.classList.contains('MuiBox-root')).toBe(true);
    });
});

describe('Menu amb menuListItemSemantics', () => {
    it("tots els fills directes de cada ul són li i el grup desplegat manté el germà", () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        renderMenu({ menuListItemSemantics: true });
        expect(menuLists().length).toBe(2);
        const tags = directChildTags();
        expect(tags.length).toBe(6);
        expect(tags.every((t) => t === 'LI')).toBe(true);
        // Els divisors (hr) van dins un li
        const dividers = Array.from(document.querySelectorAll('.MuiDivider-root'));
        expect(dividers.length).toBe(2);
        expect(dividers.every((d) => d.tagName === 'HR' && d.parentElement?.tagName === 'LI')).toBe(
            true
        );
        // Els li no tenen pic de llista
        const wrapper = menuLists()[0].children[0] as HTMLElement;
        expect(wrapper.style.listStyle).toBe('none');
        // El selector de lot2 '&.Mui-selected:has(+ .MuiBox-root)' continua trobant el germà
        const selectedGroup = Array.from(document.querySelectorAll('.Mui-selected')).find((b) =>
            b.textContent?.includes('Pare')
        ) as HTMLElement;
        expect(selectedGroup.parentElement?.tagName).toBe('LI');
        expect(selectedGroup.nextElementSibling?.classList.contains('MuiBox-root')).toBe(true);
        expect(selectedGroup.matches('.Mui-selected:has(+ .MuiBox-root)')).toBe(true);
    });
    it('també al panell flotant del menú plegat', () => {
        vi.spyOn(smallScreen, 'useSmallScreen').mockReturnValue(false);
        const { container } = renderMenu({ menuListItemSemantics: true }, true);
        const group = Array.from(container.querySelectorAll('.MuiListItemButton-root')).find(
            (b) => b.getAttribute('title') === 'Pare'
        ) as HTMLElement;
        fireEvent.click(group);
        // Columna d'icones + panell flotant
        expect(menuLists().length).toBe(2);
        expect(container.textContent).toContain('B');
        const tags = directChildTags();
        expect(tags.every((t) => t === 'LI')).toBe(true);
    });
});
