import { createContext, useContext } from 'react';

export type User = {
    id: number;
    codi: string;
    nom: string;
    nif?: string;
    email?: string;
    emailAlternatiu?: string;
    idioma: string;
    rols?: string[];
    numElementsPagina: number;
};

export const UserContext = createContext<User | undefined>(undefined);

export const useUserContext = () => {
    return useContext(UserContext);
}
