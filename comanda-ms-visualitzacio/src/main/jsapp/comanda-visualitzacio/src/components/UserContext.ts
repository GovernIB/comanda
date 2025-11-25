import { createContext, useContext } from 'react';
import { UsuariModel } from '../types/usuari.model';

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
