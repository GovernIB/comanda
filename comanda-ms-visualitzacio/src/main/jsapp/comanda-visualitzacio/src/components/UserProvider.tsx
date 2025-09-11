import { useResourceApiService } from 'reactlib';
import { FC, PropsWithChildren, useEffect, useState } from 'react';
import { User, UserContext } from './UserContext';

const UserProvider: FC<PropsWithChildren> = ({ children }) => {
    const { find, isReady } = useResourceApiService('usuari');
    const [user, setUser] = useState<User>();
    useEffect(() => {
        if (!isReady) return;
        find({ page: 0, size: 1 }).then((response) => {
            setUser(response.rows[0]);
        });
    }, [find, isReady]);
    return <UserContext.Provider value={user}>{children}</UserContext.Provider>;
};

export default UserProvider;
