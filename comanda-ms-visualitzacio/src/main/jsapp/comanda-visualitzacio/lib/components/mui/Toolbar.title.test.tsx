import { render } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { Toolbar } from './Toolbar';

const headings = (c: HTMLElement) => c.querySelectorAll('h1, h2, h3, h4, h5, h6');

describe('Toolbar títol', () => {
    it('per defecte el títol de text és un h6', () => {
        const { container } = render(<Toolbar title="Títol" />);
        const h6 = container.querySelector('h6');
        expect(h6?.textContent).toBe('Títol');
        expect(h6?.classList.contains('MuiTypography-h6')).toBe(true);
    });
    it("amb title='' no es pinta cap encapçalament", () => {
        const { container } = render(<Toolbar title="" />);
        expect(headings(container).length).toBe(0);
    });
    it("amb title='' i subtítol no es pinta cap encapçalament però sí el subtítol", () => {
        const { container } = render(<Toolbar title="" subtitle="Sub" />);
        expect(headings(container).length).toBe(0);
        expect(container.textContent).toContain('Sub');
    });
    it('sense title no es pinta cap encapçalament', () => {
        const { container } = render(<Toolbar subtitle="Sub" />);
        expect(headings(container).length).toBe(0);
    });
    it("titleComponent='h2' pinta un h2 amb l'aspecte de h6", () => {
        const { container } = render(<Toolbar title="Títol" titleComponent="h2" />);
        expect(container.querySelector('h6')).toBeNull();
        const h2 = container.querySelector('h2');
        expect(h2?.textContent).toBe('Títol');
        expect(h2?.classList.contains('MuiTypography-h6')).toBe(true);
    });
    it("titleComponent='h2' també amb subtítol", () => {
        const { container } = render(<Toolbar title="Títol" subtitle="Sub" titleComponent="h2" />);
        expect(container.querySelector('h2')?.textContent).toBe('Títol');
        expect(container.querySelector('h6')).toBeNull();
    });
    it("amb title='' l'element de la posició 1 continua abans de l'espai flexible", () => {
        const { container } = render(
            <Toolbar
                title=""
                elementsWithPositions={[{ position: 1, element: <span data-testid="p1" /> }]}
            />
        );
        expect(headings(container).length).toBe(0);
        const children = Array.from(container.querySelector('.MuiToolbar-root')!.children);
        const p1 = children.findIndex((c) => c.getAttribute('data-testid') === 'p1');
        const spacer = children.findIndex((c) => (c as HTMLElement).style.flexGrow === '1');
        expect(p1).toBeGreaterThanOrEqual(0);
        expect(spacer).toBeGreaterThan(p1);
    });
    it('un títol només amb espais tampoc no pinta cap encapçalament', () => {
        const { container } = render(<Toolbar title="   " subtitle="Sub" />);
        expect(headings(container).length).toBe(0);
        const { container: c2 } = render(<Toolbar title="  " />);
        expect(headings(c2).length).toBe(0);
    });
    it('el contenidor del títol amb subtítol no ocupa el 100% (si la barra parteix files, no empeny els controls)', () => {
        const { container } = render(<Toolbar title="Títol" subtitle="Sub" />);
        const wrapper = container.querySelector('h6')!.parentElement as HTMLElement;
        expect(wrapper.style.width).toBe('');
        expect(wrapper.style.minWidth).toBe('0px');
        expect(wrapper.style.flex).toBe('1 1 auto');
        expect(wrapper.textContent).toContain('Sub');
    });
});
