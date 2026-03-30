import { act, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import UserProvider, { ROLE_ADMIN, ROLE_CONSULTA, ROLE_USER } from './UserProvider';
import { useUserContext } from './UserContext';

const mocks = vi.hoisted(() => ({
    useResourceApiServiceMock: vi.fn(),
    useResourceApiContextMock: vi.fn(),
    findMock: vi.fn(),
    setHttpHeadersMock: vi.fn(),
    refreshApiIndexMock: vi.fn(),
    storage: {} as Record<string, string>,
}));

vi.mock('reactlib', () => ({
    useResourceApiService: () => mocks.useResourceApiServiceMock(),
    useResourceApiContext: () => mocks.useResourceApiContextMock(),
}));

const Consumer = () => {
    const { user, currentRole, setCurrentRole, refresh } = useUserContext();
    return (
        <div>
            <div data-testid="user-id">{String(user?.id ?? '')}</div>
            <div data-testid="current-role">{String(currentRole ?? '')}</div>
            <button type="button" onClick={() => setCurrentRole(ROLE_ADMIN)}>
                canvia-rol
            </button>
            <button type="button" onClick={() => refresh()}>
                refresca
            </button>
        </div>
    );
};

describe('UserProvider', () => {
    beforeEach(() => {
        mocks.storage = {};
        vi.stubGlobal('localStorage', {
            getItem: vi.fn((key: string) => mocks.storage[key] ?? null),
            setItem: vi.fn((key: string, value: string) => {
                mocks.storage[key] = value;
            }),
            clear: vi.fn(() => {
                mocks.storage = {};
            }),
        });
        mocks.findMock.mockReset();
        mocks.setHttpHeadersMock.mockReset();
        mocks.refreshApiIndexMock.mockReset();
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: mocks.findMock,
        });
        mocks.useResourceApiContextMock.mockReturnValue({
            setHttpHeaders: mocks.setHttpHeadersMock,
            refreshApiIndex: mocks.refreshApiIndexMock,
        });
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('UserProvider_quanCarregaLUsuari_seleccionaElRolDesatAlLocalStorage', async () => {
        // Comprova que el provider prioritza el rol desat si encara és vàlid per a l'usuari.
        localStorage.setItem('comanda_userRole', ROLE_ADMIN);
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 9, rols: [ROLE_CONSULTA, ROLE_ADMIN] }],
        });

        render(
            <UserProvider>
                <Consumer />
            </UserProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('user-id')).toHaveTextContent('9');
        });

        expect(screen.getByTestId('current-role')).toHaveTextContent(ROLE_ADMIN);
        expect(mocks.setHttpHeadersMock).toHaveBeenCalledWith([{ 'X-App-Role': ROLE_ADMIN }]);
        expect(mocks.refreshApiIndexMock).toHaveBeenCalled();
    });

    it('UserProvider_quanNoHiHaRolDesat_usaLordrePerDefecte', async () => {
        // Verifica que el rol per defecte segueix la prioritat `consulta` abans que `admin`.
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 3, rols: [ROLE_ADMIN, ROLE_CONSULTA] }],
        });

        render(
            <UserProvider>
                <Consumer />
            </UserProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('current-role')).toHaveTextContent(ROLE_CONSULTA);
        });
    });

    it('UserProvider_quanLusuariNoTeRolsFuncionals_usaUsuariPerDefecte', async () => {
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 4, rols: [] }],
        });

        render(
            <UserProvider>
                <Consumer />
            </UserProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('current-role')).toHaveTextContent(ROLE_USER);
        });

        await waitFor(() => {
            expect(mocks.setHttpHeadersMock).toHaveBeenCalledWith([{ 'X-App-Role': ROLE_USER }]);
        });
    });

    it('UserProvider_quanElRolDesatEsUsuari_elRecuperaEncaraQueNoSiguiUnRolFuncional', async () => {
        localStorage.setItem('comanda_userRole', ROLE_USER);
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 10, rols: [ROLE_CONSULTA, ROLE_ADMIN] }],
        });

        render(
            <UserProvider>
                <Consumer />
            </UserProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('current-role')).toHaveTextContent(ROLE_USER);
        });
    });

    it('UserProvider_quanEsCanviaElRol_lactualitzaILDesaAlLocalStorage', async () => {
        // Comprova que el canvi manual de rol persisteix el valor i refresca la capçalera HTTP.
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 5, rols: [ROLE_CONSULTA, ROLE_ADMIN] }],
        });

        render(
            <UserProvider>
                <Consumer />
            </UserProvider>
        );

        await waitFor(() => {
            expect(screen.getByTestId('current-role')).toHaveTextContent(ROLE_CONSULTA);
        });

        await act(async () => {
            screen.getByRole('button', { name: 'canvia-rol' }).click();
        });

        expect(screen.getByTestId('current-role')).toHaveTextContent(ROLE_ADMIN);
        expect(localStorage.getItem('comanda_userRole')).toBe(ROLE_ADMIN);
        expect(mocks.setHttpHeadersMock).toHaveBeenLastCalledWith([{ 'X-App-Role': ROLE_ADMIN }]);
    });
});
