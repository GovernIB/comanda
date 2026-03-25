import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Menu, { type MenuEntry } from './Menu';

const mocks = vi.hoisted(() => ({
    useLocationPathMock: vi.fn(),
    useSmallScreenMock: vi.fn(),
    useSmallHeaderMock: vi.fn(),
}));

vi.mock('../BaseAppContext', () => ({
    useBaseAppContext: () => ({
        getLinkComponent: () =>
            React.forwardRef<
                HTMLAnchorElement,
                { to?: string; children?: React.ReactNode; onClick?: React.MouseEventHandler<HTMLAnchorElement> }
            >(({ to, children, onClick, ...rest }, ref) => (
                <a
                    href={to}
                    ref={ref}
                    onClick={(event) => {
                        event.preventDefault();
                        onClick?.(event);
                    }}
                    {...rest}>
                    {children}
                </a>
            )),
        useLocationPath: () => mocks.useLocationPathMock(),
    }),
}));

vi.mock('../../util/useSmallScreen', () => ({
    useSmallScreen: () => mocks.useSmallScreenMock(),
    useSmallHeader: () => mocks.useSmallHeaderMock(),
}));

describe('Menu', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    const compactEntries: MenuEntry[] = [
        { id: 'home', title: 'Inici', to: '/home', icon: 'home' },
        {
            id: 'config',
            title: 'Configuració',
            description: 'Opcions de configuració',
            children: [
                { id: 'users', title: 'Usuaris', to: '/users', icon: 'group' },
                {
                    id: 'advanced',
                    title: 'Avançat',
                    children: [{ id: 'logs', title: 'Logs', to: '/logs' }],
                },
            ],
        },
    ];

    const renderMenu = (
        entries: MenuEntry[],
        shrink = true,
        appearance: 'theme' | 'inverse' | 'footer' = 'theme'
    ) => {
        mocks.useLocationPathMock.mockReturnValue('/home');
        mocks.useSmallScreenMock.mockReturnValue(false);
        mocks.useSmallHeaderMock.mockReturnValue(false);

        return render(
            <Menu
                entries={entries}
                shrink={shrink}
                drawerWidth={320}
                footerHeight={36}
                appearance={appearance}
            />
        );
    };

    it('Menu_quanEstaCompactat_mostraNomesIconesDePrimerNivellIAfegeixIconaPerDefecte', () => {
        renderMenu(compactEntries);

        expect(screen.getByTitle('Inici')).toBeInTheDocument();
        expect(screen.getByTitle('Configuració')).toBeInTheDocument();
        expect(screen.queryByText('Usuaris')).not.toBeInTheDocument();
        expect(screen.getByText('menu')).toBeInTheDocument();
        expect(
            getComputedStyle(
                screen.getByTitle('Configuració').querySelector('.MuiListItemIcon-root') as Element
            ).marginRight
        ).toBe('0px');
    });

    it('Menu_quanEsPremUnSubmenuCompactat_obreElPanellAmbDescripcioIDesplegablesInterns', () => {
        renderMenu(compactEntries);

        fireEvent.click(screen.getByTitle('Configuració'));

        expect(screen.getByTestId('compact-floating-panel')).toHaveStyle({
            position: 'fixed',
            left: 'calc(56px + 1px)',
            top: '64px',
            bottom: '36px',
        });
        expect(screen.getByText('Configuració')).toBeInTheDocument();
        expect(screen.getByText('Opcions de configuració')).toBeInTheDocument();
        expect(screen.getByText('Usuaris')).toBeInTheDocument();
        expect(screen.getByText('Avançat')).toBeInTheDocument();
        expect(screen.queryByText('Logs')).not.toBeInTheDocument();

        fireEvent.click(screen.getByText('Avançat'));

        expect(screen.getByText('Logs')).toBeInTheDocument();
    });

    it('Menu_quanEsPassenMesuresDelPanell_usaElsValorsConfigurats', () => {
        mocks.useLocationPathMock.mockReturnValue('/home');
        mocks.useSmallScreenMock.mockReturnValue(false);
        mocks.useSmallHeaderMock.mockReturnValue(false);

        render(
            <Menu
                entries={compactEntries}
                shrink
                drawerWidth={320}
                footerHeight={36}
                compactPanelWidth={310}
                submenuTitelHeight={88}
            />
        );

        fireEvent.click(screen.getByTitle('Configuració'));

        expect(screen.getByTestId('compact-floating-panel')).toHaveStyle({
            width: '310px',
        });
        expect(screen.getByText('Configuració').parentElement).toHaveStyle({
            minHeight: '88px',
        });
    });

    it('Menu_quanEsNavegaDesDelSubmenuCompactat_tancaElPanellFlotant', () => {
        renderMenu(compactEntries);

        fireEvent.click(screen.getByTitle('Configuració'));

        expect(screen.getByTestId('compact-floating-panel')).toBeInTheDocument();

        fireEvent.click(screen.getByText('Usuaris'));

        expect(screen.queryByTestId('compact-floating-panel')).not.toBeInTheDocument();
    });

    it('Menu_quanEsFaClickForaDelPanellCompactat_tancaElSubmenu', () => {
        renderMenu(compactEntries);

        fireEvent.click(screen.getByTitle('Configuració'));

        expect(screen.getByTestId('compact-floating-panel')).toBeInTheDocument();

        fireEvent.mouseDown(document.body);

        expect(screen.queryByTestId('compact-floating-panel')).not.toBeInTheDocument();
    });

    it('Menu_quanUnaEntradaCompactadaNoEsSubmenu_manteLaNavegacioAmbLaRutaActual', () => {
        renderMenu(compactEntries);

        const link = screen.getByTitle('Inici').closest('a');

        expect(link).toHaveAttribute('href', '/home');
    });

    it('Menu_quanNoEstaCompactat_manteElComportamentOriginalDelSubmenu', () => {
        renderMenu(compactEntries, false);

        expect(screen.getByText('Inici')).toBeInTheDocument();
        expect(screen.getByText('Configuració')).toBeInTheDocument();
        expect(screen.queryByText('Usuaris')).not.toBeInTheDocument();
        expect(
            getComputedStyle(
                screen
                    .getByText('Inici')
                    .closest('.MuiListItemButton-root')
                    ?.querySelector('.MuiListItemIcon-root') as Element
            ).marginRight
        ).toBe('16px');

        fireEvent.click(screen.getByText('Configuració'));

        expect(screen.getByText('Usuaris')).toBeInTheDocument();
    });

    it('Menu_quanUsaElsColorsDelPeu_aplicaLaPaletaPersonalitzada', () => {
        renderMenu(compactEntries, true, 'footer');

        fireEvent.click(screen.getByTitle('Configuració'));

        expect(screen.getByTestId('compact-floating-panel')).toHaveStyle({
            backgroundColor: 'rgb(95, 93, 93)',
            color: 'rgb(246, 246, 246)',
        });
    });
});
