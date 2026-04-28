import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import AppMenu from './AppMenu';
import type { MenuEntry } from '../../lib/components/mui/Menu';

const mocks = vi.hoisted(() => ({
    sideMenuMock: vi.fn(({ onClose }: { onClose: () => void }) => (
        <div data-testid="side-menu-mock">
            <button onClick={onClose}>Tancar menú</button>
        </div>
    )),
}));

vi.mock('./SideMenu.tsx', () => ({
    default: (props: { onClose: () => void }) => mocks.sideMenuMock(props),
}));

const menuEntries: MenuEntry[] = [{ id: 'opcio', title: 'Opció', to: '/opcio', icon: 'apps' }];

describe('AppMenu', () => {
    it('AppMenu_quanEsPremLaIcona_renderitzaElMenuLateral', () => {
        // Comprova que el component mostra el menú lateral després de prémer el botó principal.
        render(<AppMenu menuEntries={menuEntries} />);

        fireEvent.click(screen.getByRole('button', { name: 'open menu' }));

        expect(screen.getByTestId('side-menu-mock')).toBeInTheDocument();
    });

    it('AppMenu_quanElMenuDemanaTancarSe_ocultaElMenuLateral', () => {
        // Verifica que el component retira el menú lateral quan el fill invoca el tancament.
        render(<AppMenu menuEntries={menuEntries} />);

        fireEvent.click(screen.getByRole('button', { name: 'open menu' }));
        fireEvent.click(screen.getByRole('button', { name: 'Tancar menú' }));

        expect(screen.queryByTestId('side-menu-mock')).not.toBeInTheDocument();
    });
});
