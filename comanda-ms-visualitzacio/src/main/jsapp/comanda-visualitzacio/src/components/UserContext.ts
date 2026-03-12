import { createContext, useContext } from 'react';
import { UsuariModel } from '../types/usuari.model';
import { ROLE_ADMIN, ROLE_CONSULTA } from './UserProvider.tsx';

export type UserContextType = {
    user?: UsuariModel;
    refresh: () => void;
    currentRole?: string;
    setCurrentRole: (currentRole: string | undefined) => void;
};

export const UserContext = createContext<UserContextType | undefined>(undefined);

export const useUserContext = () => {
    const context = useContext(UserContext);
    if (context === undefined) {
        throw new Error('UserContext Provider not found');
    }
    return context;
}

export const useIsUserAdmin = () => useUserContext().currentRole === ROLE_ADMIN;

export const useIsUserConsulta = () => useUserContext().currentRole === ROLE_CONSULTA;
