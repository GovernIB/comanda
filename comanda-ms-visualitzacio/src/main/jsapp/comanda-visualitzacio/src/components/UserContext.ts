import { createContext, useContext } from 'react';
import { IUsuari, UsuariModel } from '../types/usuari.model';
import { ROLE_ADMIN, ROLE_CONSULTA, ROLE_USER } from './UserProvider.tsx';

export type UserContextType = {
    user?: UsuariModel;
    refresh: () => void;
    previewUser: (changes?: Partial<IUsuari>) => void;
    clearUserPreview: () => void;
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

export const useIsUserUsuari = () => useUserContext().currentRole === ROLE_USER;
