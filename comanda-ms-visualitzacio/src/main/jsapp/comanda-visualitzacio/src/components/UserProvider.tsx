import React from 'react';
import { useResourceApiService, useResourceApiContext } from 'reactlib';
import { UserContext } from './UserContext';
import { IUsuari, UsuariModel } from '../types/usuari.model';

export const ROLE_USER = 'COM_USER';
export const ROLE_ADMIN = 'COM_ADMIN';
export const ROLE_CONSULTA = 'COM_CONSULTA';
const ROLE_DEFAULT_ORDER = [ROLE_CONSULTA, ROLE_ADMIN];
export const MAPPABLE_ROLES = [ROLE_USER, ROLE_ADMIN, ROLE_CONSULTA];
const USER_ROLE_LOCAL_STORAGE_KEY = 'comanda_userRole';

const isSelectableRole = (userRoles?: string[], role?: string | null) =>
    role === ROLE_USER || (role != null && userRoles?.includes(role));

const UserProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
    const {
        isReady: apiIsReady,
        find: apiFind,
    } = useResourceApiService('usuari');
    const {
        setHttpHeaders: apiSetHttpHeaders,
        refreshApiIndex: apiRefreshIndex
    } = useResourceApiContext();
    const [persistedUser, setPersistedUser] = React.useState<UsuariModel>();
    const [previewOverrides, setPreviewOverrides] = React.useState<Partial<IUsuari>>();
    const user = React.useMemo(() => {
        if (!persistedUser) {
            return undefined;
        }
        if (!previewOverrides) {
            return persistedUser;
        }
        return new UsuariModel({
            ...persistedUser,
            ...previewOverrides,
        });
    }, [persistedUser, previewOverrides]);
    const [currentRole, setCurrentRole] = React.useState<string | undefined>();
    const refresh = () => {
        if (apiIsReady) {
            apiFind({ page: 0, size: 1 }).
            then((response) => {
                const user = response.rows[0];
                setPersistedUser(user);
                setPreviewOverrides(undefined);
                const userRoles = user?.rols as string[];
                if (!currentRole) {
                    const roleFromLocalStorage = localStorage.getItem(USER_ROLE_LOCAL_STORAGE_KEY);
                    if (isSelectableRole(userRoles, roleFromLocalStorage))
                        setCurrentRole(roleFromLocalStorage ?? undefined);
                    else
                        setCurrentRole(ROLE_DEFAULT_ORDER.find(role => userRoles?.includes(role)) ?? ROLE_USER);
                }
            }).
            catch(error => console.log('Error obtenint usuari', error));
        }
    }
    React.useEffect(() => {
        refresh();
    }, [apiIsReady]);
    React.useEffect(() => {
        if (currentRole != null) {
            apiSetHttpHeaders([{ 'X-App-Role': currentRole }]);
            apiRefreshIndex();
        }
    }, [currentRole]);
    const contextValue = {
        user,
        refresh,
        previewUser: (changes?: Partial<IUsuari>) => setPreviewOverrides(changes),
        clearUserPreview: () => setPreviewOverrides(undefined),
        currentRole,
        setCurrentRole: (newRole?: string) => {
            setCurrentRole(newRole);
            localStorage.setItem(USER_ROLE_LOCAL_STORAGE_KEY, newRole ?? "");
        },
    };
    return <UserContext.Provider value={contextValue}>
        {children}
    </UserContext.Provider>;
};

export default UserProvider;
