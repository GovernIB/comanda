import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import RoleSelector from './RoleSelector';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            enum: {
                userRole: {
                    COM_ADMIN: 'Administrador',
                    COM_CONSULTA: 'Consulta',
                },
            },
        })
    ),
    useUserContextMock: vi.fn(),
    setCurrentRoleMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('./UserContext', () => ({
    useUserContext: () => mocks.useUserContextMock(),
}));

vi.mock('./UserProvider.tsx', () => ({
    MAPPABLE_ROLES: ['COM_ADMIN', 'COM_CONSULTA'],
    ROLE_ADMIN: 'COM_ADMIN',
    ROLE_CONSULTA: 'COM_CONSULTA',
}));

describe('RoleSelector', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('RoleSelector_quanNoHiHaRols_noRenderitzaCapOpcio', () => {
        // Comprova que el selector no mostra res si l'usuari no té rols disponibles.
        mocks.useUserContextMock.mockReturnValue({
            user: null,
            currentRole: 'COM_ADMIN',
            setCurrentRole: mocks.setCurrentRoleMock,
        });

        const { container } = render(<RoleSelector />);

        expect(container).toBeEmptyDOMElement();
    });

    it('RoleSelector_quanEsRenderitza_mostraElsRolsMapejables', async () => {
        // Verifica que el component filtra i mostra només els rols compatibles amb el selector.
        mocks.useUserContextMock.mockReturnValue({
            user: { rols: ['COM_ADMIN', 'COM_CONSULTA', 'ALTRE'] },
            currentRole: 'COM_ADMIN',
            setCurrentRole: mocks.setCurrentRoleMock,
        });

        render(<RoleSelector />);

        fireEvent.click(screen.getByRole('menuitem'));

        expect((await screen.findAllByText('Administrador')).length).toBeGreaterThan(0);
        expect(screen.getByText('Consulta')).toBeInTheDocument();
        expect(screen.queryByText('ALTRE')).not.toBeInTheDocument();
    });

    it('RoleSelector_quanEsSeleccionaUnRol_actualitzaElRolActual', async () => {
        // Comprova que seleccionar una opció invoca el canvi de rol al context d'usuari.
        mocks.useUserContextMock.mockReturnValue({
            user: { rols: ['COM_ADMIN', 'COM_CONSULTA'] },
            currentRole: 'COM_ADMIN',
            setCurrentRole: mocks.setCurrentRoleMock,
        });

        render(<RoleSelector />);

        fireEvent.click(screen.getByRole('menuitem'));
        fireEvent.click(await screen.findByText('Consulta'));

        expect(mocks.setCurrentRoleMock).toHaveBeenCalledWith('COM_CONSULTA');
    });
});
