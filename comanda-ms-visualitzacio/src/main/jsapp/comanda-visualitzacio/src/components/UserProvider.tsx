import { useResourceApiService } from 'reactlib';
import { FC, PropsWithChildren, useEffect, useState } from 'react';
import { UserContext, UserContextType } from './UserContext';
import { UsuariModel } from '../types/usuari.model';

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
    const { find, isReady } = useResourceApiService('usuari');
    const [user, setUser] = useState<UsuariModel>();
    const [loading, setLoading] = useState<boolean>(false);
    useEffect(() => {
        if (!isReady) return;
        setLoading(true);
        find({ page: 0, size: 1 }).then((response) => {
            setUser(response.rows[0]);
            setLoading(false);
        });
    }, [find, isReady]);
    const contextValue: UserContextType = {
        user,
        setUser,
        loading,
    };
    if (loading) return;

    return <UserContext.Provider value={contextValue}>{children}</UserContext.Provider>;
};

export default UserProvider;
