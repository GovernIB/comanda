import React from 'react';
import useLogConsole from '../util/useLogConsole';
import AuthContext from './AuthContext';

const LOG_PREFIX = '[CAUTH]';

type AuthProviderProps = React.PropsWithChildren & {
    /** Indica si s'han d'imprimir a la consola missatges de depuració */
    debug?: true;
};

const parseJwt = (token: string) => {
    if (token != null) {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } else {
        return token;
    }
};

export const AuthProvider = (props: AuthProviderProps) => {
    const { debug, children } = props;
    const token = (window as any).__TOKEN__;
    const tokenRef = React.useRef<string>(token);
    const tokenParsedRef = React.useRef<any>(parseJwt(token));
    const isLoading = false;
    const isAuthenticated = token != null;
    const logConsole = useLogConsole(LOG_PREFIX);
    debug &&
        logConsole.debug(isAuthenticated ? 'Token obtingut: ' + token : 'Usuari no autenticat');
    const signIn = isLoading ? undefined : () => {};
    const signOut = isLoading ? undefined : () => {};
    const context = {
        isLoading,
        isReady: !isLoading,
        isAuthenticated,
        getToken: () => tokenRef.current,
        getTokenParsed: () => tokenParsedRef.current,
        getUserId: () => tokenParsedRef.current?.['preferred_username'],
        getUserName: () => tokenParsedRef.current?.['name'],
        getUserEmail: () => tokenParsedRef.current?.['email'],
        signIn,
        signOut,
    };
    return <AuthContext.Provider value={context}>{children}</AuthContext.Provider>;
};
