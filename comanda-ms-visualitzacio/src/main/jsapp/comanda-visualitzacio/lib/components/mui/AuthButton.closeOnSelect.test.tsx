import React from 'react';
import { render, screen, fireEvent, waitFor, within } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import '@testing-library/jest-dom/vitest';
import MenuItem from '@mui/material/MenuItem';
import ListItemText from '@mui/material/ListItemText';
import Select from '@mui/material/Select';
import AuthButton from './AuthButton';
import { AuthContext } from '../AuthContext';
import { BaseAppContext } from '../BaseAppContext';

const authStub: any = {
    isReady: true,
    isAuthenticated: true,
    getTokenParsed: () => ({ name: 'Usuari Prova', preferred_username: 'uprova' }),
    signOut: vi.fn(),
};

const RoleSelect: React.FC<{ onChange: (v: string) => void }> = ({ onChange }) => {
    const [value, setValue] = React.useState('USER');
    return (
        <MenuItem>
            <Select
                value={value}
                variant="standard"
                onChange={(e) => {
                    setValue(e.target.value as string);
                    onChange(e.target.value as string);
                }}>
                <MenuItem value="USER">Usuari</MenuItem>
                <MenuItem value="ADMIN">Administrador</MenuItem>
            </Select>
        </MenuItem>
    );
};

const renderButton = (additionalComponents: React.ReactElement[]) =>
    render(
        <BaseAppContext.Provider value={{ t: (k: string) => k } as any}>
            <AuthContext.Provider value={authStub}>
                <AuthButton additionalComponents={additionalComponents} />
            </AuthContext.Provider>
        </BaseAppContext.Provider>
    );

const openMenu = () => {
    fireEvent.click(screen.getByLabelText('auth menu'));
    return screen.getByRole('menu');
};

const expectMenuClosed = () =>
    waitFor(() => expect(screen.queryByRole('menu', { hidden: true })).not.toBeInTheDocument());

describe('AuthButton tanca el menú en seleccionar una opció', () => {
    it('una opció addicional amb acció tanca el menú', async () => {
        const onProfile = vi.fn();
        renderButton([
            <MenuItem key="p" onClick={onProfile}>
                <ListItemText>Perfil</ListItemText>
            </MenuItem>,
        ]);
        const menu = openMenu();
        fireEvent.click(within(menu).getByText('Perfil'));
        expect(onProfile).toHaveBeenCalled();
        await expectMenuClosed();
    });

    it('obrir un desplegable dins el menú no el tanca, triar-ne un valor sí', async () => {
        const onRole = vi.fn();
        renderButton([<RoleSelect key="r" onChange={onRole} />]);
        const menu = openMenu();
        fireEvent.mouseDown(within(menu).getByRole('combobox'));
        const listbox = await screen.findByRole('listbox');
        // el menú d'usuari queda ocult (aria-hidden) darrere el desplegable però continua obert
        expect(screen.getByRole('menu', { hidden: true })).toBeInTheDocument();
        fireEvent.click(within(listbox).getByText('Administrador'));
        expect(onRole).toHaveBeenCalledWith('ADMIN');
        await expectMenuClosed();
    });

    it("un element informatiu (data-keep-menu-open) no tanca el menú, ni tampoc la capçalera d'usuari", () => {
        renderButton([
            <MenuItem key="info" disableRipple data-keep-menu-open>
                <ListItemText>Darrera connexió</ListItemText>
            </MenuItem>,
        ]);
        const menu = openMenu();
        fireEvent.click(within(menu).getByText('Darrera connexió'));
        fireEvent.click(within(menu).getByText('Usuari Prova'));
        expect(screen.getByRole('menu')).toBeInTheDocument();
    });
});
