import { createContext, useContext } from 'react';
import { UsuariModel } from '../types/usuari.model';

export type UserContextType = {
    user: UsuariModel | undefined;
    setUser: (user: UsuariModel) => void;
    loading: boolean;
};

export const UserContext = createContext<UserContextType | undefined>(undefined);

export const useUserContext = () => {
    const context = useContext(UserContext);
    if (context === undefined) {
        throw new Error('UserContext Provider not found');
    }
    return context;
}
