import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { BaseAppContext } from '../BaseAppContext';
import { toToolbarIcon } from './ToolbarIcon';

const Link = React.forwardRef<HTMLAnchorElement, any>(({ to, ...props }, ref) => (
    <a ref={ref} href={to} {...props} />
));
const baseApp: any = { t: (k: string) => k, getLinkComponent: () => Link };

const renderIcon = (element: React.ReactElement) =>
    render(<BaseAppContext.Provider value={baseApp}>{element}</BaseAppContext.Provider>);

describe('toToolbarIcon per defecte', () => {
    it("és un botó d'icona amb el títol com a title", () => {
        const onClick = vi.fn();
        renderIcon(toToolbarIcon('refresh', { title: 'Refrescar', onClick }));
        const button = screen.getByTitle('Refrescar');
        expect(button.className).toContain('MuiIconButton-root');
        expect(button.textContent).toBe('refresh');
        fireEvent.click(button);
        expect(onClick).toHaveBeenCalledTimes(1);
    });
    it("variant 'icon' és igual que per defecte", () => {
        renderIcon(toToolbarIcon('refresh', { title: 'Refrescar', variant: 'icon' }));
        expect(screen.getByTitle('Refrescar').className).toContain('MuiIconButton-root');
    });
});

describe("toToolbarIcon amb variant 'button'", () => {
    it('és un botó amb la icona al principi i el títol com a text', () => {
        const onClick = vi.fn();
        renderIcon(toToolbarIcon('refresh', { title: 'Refrescar', onClick, variant: 'button' }));
        const button = screen.getByRole('button', { name: /Refrescar/ });
        expect(button.className).toContain('MuiButton-root');
        expect(button.className).not.toContain('MuiIconButton-root');
        expect(button.querySelector('.MuiButton-startIcon')?.textContent).toBe('refresh');
        expect(button.textContent).toContain('Refrescar');
        fireEvent.click(button);
        expect(onClick).toHaveBeenCalledTimes(1);
    });
    it('desactivat', () => {
        const onClick = vi.fn();
        renderIcon(
            toToolbarIcon('refresh', { title: 'Refrescar', onClick, disabled: true, variant: 'button' })
        );
        const button = screen.getByRole('button', { name: /Refrescar/ });
        expect((button as HTMLButtonElement).disabled).toBe(true);
    });
    it("amb linkTo és un enllaç", () => {
        renderIcon(toToolbarIcon('add', { title: 'Nou', linkTo: '/nou', variant: 'button' }));
        const link = screen.getByRole('link', { name: /Nou/ });
        expect(link.getAttribute('href')).toBe('/nou');
        expect(link.className).toContain('MuiButton-root');
    });
    it('amb badge el mostra a la icona', () => {
        renderIcon(toToolbarIcon('filter_list', { title: 'Filtre', badge: 3, variant: 'button' }));
        const button = screen.getByRole('button', { name: /Filtre/ });
        expect(button.querySelector('.MuiBadge-badge')?.textContent).toBe('3');
    });
});
