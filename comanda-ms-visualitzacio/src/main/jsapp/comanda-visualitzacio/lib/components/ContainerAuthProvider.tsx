import React from 'react';
import useLogConsole from '../util/useLogConsole';
import AuthContext from './AuthContext';

const LOG_PREFIX = '[CAUTH]';

type AuthProviderProps = React.PropsWithChildren & {
    /** La url a carregar després de fer logout */
    logoutUrl: string;
    /** Indica que l'autenticació és obligatòria (no es pot veure res si no s'està autenticat) */
    mandatory?: true;
    /** Indica si s'han d'imprimir a la consola missatges de depuració */
    debug?: true;
};

const parseJwt = (token?: string) => {
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
    const { logoutUrl, mandatory, debug, children } = props;
    const [loading, setLoading] = React.useState<boolean>(true);
    const tokenRef = React.useRef<string>(undefined);
    const tokenParsedRef = React.useRef<any>(undefined);
    const logConsole = useLogConsole(LOG_PREFIX);
    const isAuthenticated = !loading && tokenRef.current != null;
    const authSrc = document.head.getElementsByTagName('script')[2].src;
    const signOutUrl = authSrc.replace('/authToken', '/logout');
    React.useEffect(() => {
        fetch(authSrc).
            then(response => response.text()).
            then(text => {
                const match = text.match(/window\.__AUTH_TOKEN__\s*=\s*'([^']+)'/);
                const token = (match ? match[1] : null) ?? undefined;
                tokenRef.current = token;
                tokenParsedRef.current = parseJwt(token);
                setLoading(false);
                debug && logConsole.debug(token != null ? 'Token obtingut: ' + token : 'Usuari no autenticat');
            });
    }, []);
    const signIn = loading ? undefined : () => {};
    const signOut = loading ? undefined : () => {
        fetch(signOutUrl).
            finally(() => {
                debug && logConsole.debug('Signout');
                window.location.href = logoutUrl;
            });
    }
    const context = {
        isLoading: loading,
        isReady: !loading,
        isAuthenticated,
        bearerTokenActive: false,
        getToken: () => tokenRef.current,
        getTokenParsed: () => tokenParsedRef.current,
        getUserId: () => tokenParsedRef.current?.['preferred_username'],
        getUserName: () => tokenParsedRef.current?.['name'],
        getUserEmail: () => tokenParsedRef.current?.['email'],
        signIn,
        signOut,
    };
    const showChildren = !loading && (!mandatory || (mandatory && isAuthenticated));
    return <AuthContext.Provider value={context}>
        {showChildren ? children : null}
    </AuthContext.Provider>;
};
