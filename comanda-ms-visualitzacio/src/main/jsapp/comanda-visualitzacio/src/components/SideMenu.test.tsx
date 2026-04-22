import { fireEvent, render, screen } from '@testing-library/react';
import React from 'react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import SideMenu from './SideMenu';
import type { MenuEntry } from '../../lib/components/mui/Menu';

const mocks = vi.hoisted(() => ({
    useLocationPathMock: vi.fn(),
    useSmallScreenMock: vi.fn(),
    useSmallHeaderMock: vi.fn(),
}));

vi.mock('../../lib/components/BaseAppContext', () => ({
    useBaseAppContext: () => ({
        getLinkComponent: () =>
            React.forwardRef<HTMLAnchorElement, { to?: string; children?: React.ReactNode; onClick?: React.MouseEventHandler<HTMLAnchorElement> }>(
                ({ to, children, onClick, ...rest }, ref) => (
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
                )
            ),
        useLocationPath: () => mocks.useLocationPathMock(),
    }),
}));

vi.mock('../../lib/util/useSmallScreen', () => ({
    useSmallScreen: () => mocks.useSmallScreenMock(),
    useSmallHeader: () => mocks.useSmallHeaderMock(),
}));

describe('SideMenu', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    const baseEntries: MenuEntry[] = [{ id: 'apps', title: 'Apps', to: '/apps', icon: 'apps' }];

    it('SideMenu_quanEsRenderitza_mostraLesEntradesPrincipalsDelMenu', () => {
        // Comprova que el menú lateral mostra les opcions principals i el títol visible.
        mocks.useLocationPathMock.mockReturnValue('/apps');
        mocks.useSmallScreenMock.mockReturnValue(false);
        mocks.useSmallHeaderMock.mockReturnValue(false);

        render(
            <SideMenu
                entries={baseEntries}
                drawerWidth={350}
                onClose={() => undefined}
            />
        );

        expect(screen.getByText('Menú')).toBeInTheDocument();
        expect(screen.getByText('Apps')).toBeInTheDocument();
    });

    it('SideMenu_quanEsPremUnaOpcioAmbRuta_invocaElCallbackDeTancament', () => {
        // Verifica que clicar una opció navegable tanca el menú lateral.
        mocks.useLocationPathMock.mockReturnValue('/home');
        mocks.useSmallScreenMock.mockReturnValue(false);
        mocks.useSmallHeaderMock.mockReturnValue(false);
        const onClose = vi.fn();

        render(
            <SideMenu
                entries={baseEntries}
                drawerWidth={350}
                onClose={onClose}
            />
        );

        fireEvent.click(screen.getByText('Apps'));

        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('SideMenu_quanEsPremEscape_invocaElTancament', () => {
        // Comprova que el menú es pot tancar amb la tecla d'escapament.
        mocks.useLocationPathMock.mockReturnValue('/home');
        mocks.useSmallScreenMock.mockReturnValue(false);
        mocks.useSmallHeaderMock.mockReturnValue(false);
        const onClose = vi.fn();

        render(
            <SideMenu
                entries={baseEntries}
                drawerWidth={350}
                onClose={onClose}
            />
        );

        fireEvent.keyDown(window, { key: 'Escape' });

        expect(onClose).toHaveBeenCalledTimes(1);
    });

    it('SideMenu_quanUnaOpcioTeFills_permetCol·lapsarLaBranca', () => {
        // Verifica que un element pare alterna la visibilitat del seu fill quan es prem.
        mocks.useLocationPathMock.mockReturnValue('/home');
        mocks.useSmallScreenMock.mockReturnValue(false);
        mocks.useSmallHeaderMock.mockReturnValue(false);

        render(
            <SideMenu
                entries={[
                    {
                        id: 'pare',
                        title: 'Pare',
                        icon: 'info',
                        children: [{ id: 'fill', title: 'Fill', to: '/fill', icon: 'info' }],
                    },
                ]}
                drawerWidth={350}
                onClose={() => undefined}
            />
        );

        expect(screen.getByText('Fill')).toBeInTheDocument();

        fireEvent.click(screen.getByText('Pare'));

        expect(screen.queryByText('Fill')).not.toBeInTheDocument();
    });
});
