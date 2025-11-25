import React from 'react';
import { useResourceApiService, useResourceApiContext } from 'reactlib';
import { UserContext } from './UserContext';
import { UsuariModel } from '../types/usuari.model';

const ROLE_DEFAULT_ORDER = ['COM_CONSULTA', 'COM_ADMIN'];

const UserProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
    const {
        isReady: apiIsReady,
        find: apiFind,
    } = useResourceApiService('usuari');
    const {
        setHttpHeaders: apiSetHttpHeaders,
        refreshApiIndex: apiRefreshIndex
    } = useResourceApiContext();
    const [user, setUser] = React.useState<UsuariModel>();
    const [currentRole, setCurrentRole] = React.useState<string | undefined>();
    const refresh = () => {
        if (apiIsReady) {
            apiFind({ page: 0, size: 1 }).
            then((response) => {
                const user = response.rows[0];
                setUser(user);
                const userRoles = user?.rols as string[];
                if (!currentRole) {
                    const currentRole = ROLE_DEFAULT_ORDER.find(role => userRoles?.includes(role));
                    setCurrentRole(currentRole);
                }
            }).
            catch(error => console.log('Error obtenint usuari', error));
        }
    }
    React.useEffect(() => {
        refresh();
    }, [apiIsReady]);
    React.useEffect(() => {
        if (currentRole) {
            apiSetHttpHeaders([{ 'X-App-Role': currentRole }]);
            apiRefreshIndex();
        }
    }, [currentRole]);
    const contextValue = {
        user,
        refresh,
        currentRole,
        setCurrentRole,
    };
    return <UserContext.Provider value={contextValue}>
        {children}
    </UserContext.Provider>;
};

export default UserProvider;
