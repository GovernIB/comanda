import React from 'react';
import useLogConsole from '../util/useLogConsole';
import AuthContext from './AuthContext';

const LOG_PREFIX = '[CAUTH]';
const CHECK_TOKEN_TIMEOUT_MARGIN_SECS = 1;

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

const useTokenWatchTimeout = (callback: () => void, delay: number = 1000) => {
    let timeoutId: any;
    const start = (newDelay?: number) => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(callback, newDelay ?? delay);
    };
    const refresh = (newDelay?: number) => start(newDelay);
    const stop = () => clearTimeout(timeoutId);
    return {
        refresh,
        stop,
    };
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
    const checkToken = () => {
        debug && logConsole.debug('Verificació del token iniciada');
        getToken()
            .then((token) => {
                if (token) {
                    setToken(token, true);
                } else {
                    debug && logConsole.debug('La verificació del token ha retornat <null>');
                    window.location.href = logoutUrl;
                }
            })
            .catch((error) => {
                logConsole.error('Error al verificar el token:', JSON.stringify(error));
                window.location.href = logoutUrl;
            });
    };
    const { refresh: checkTokenRefresh, stop: checkTokenStop } = useTokenWatchTimeout(checkToken);
    const getToken = async () => {
        const response = await fetch(authSrc);
        const text = await response.text();
        const match = text.match(/window\.__AUTH_TOKEN__\s*=\s*'([^']+)'/);
        return match ? match[1] : null;
    };
    const setToken = (token: string, verified?: boolean) => {
        tokenRef.current = token;
        const tokenParsed = parseJwt(token);
        tokenParsedRef.current = tokenParsed;
        const checkTokenTimeout =
            (tokenParsed.exp - Date.now() / 1000 + CHECK_TOKEN_TIMEOUT_MARGIN_SECS) * 1000;
        tokenParsed && checkTokenRefresh(checkTokenTimeout);
        setLoading(false);
        debug && logConsole.debug('Token', verified ? 'verificat:' : 'obtingut:', token);
    };
    React.useEffect(() => {
        debug &&
            logConsole.debug(
                "Inicialitzant proveidor d'autenticació de JBoss",
                authSrc,
                signOutUrl,
                logoutUrl
            );
        if ((window as any).__AUTH_TOKEN__) {
            setToken((window as any).__AUTH_TOKEN__);
        } else {
            debug && logConsole.debug("No s'ha trobat el token, programant consulta periòdica");
            const checkTokenInterval = setInterval(() => {
                if ((window as any).__AUTH_TOKEN__) {
                    debug && logConsole.debug('Consultant si el token ja està disponible');
                    setToken((window as any).__AUTH_TOKEN__);
                    clearInterval(checkTokenInterval);
                }
            }, 100);
            setTimeout(() => {
                clearInterval(checkTokenInterval);
                if (!(window as any).__AUTH_TOKEN__) {
                    debug &&
                        logConsole.debug(
                            'La consulta periòdica no ha pogut obtenir el token. El consultarem directament.'
                        );
                    getToken()
                        .then((token) => {
                            token && setToken(token);
                        })
                        .catch((error) => {
                            logConsole.error('Error al obtenir el token directament', error);
                        });
                }
            }, 1000);
        }
        return () => {
            checkTokenStop();
        };
    }, []);
    // No hi ha cap flux client-side per (re)iniciar sessió: l'autenticació la fa el contenidor (gatekeeper) davant
    // l'aplicació. Redirigim a logoutUrl (com fa checkToken quan detecta que la sessió ja no és vàlida): en tornar
    // a carregar l'aplicació, si ja no hi ha sessió, el gatekeeper redirigirà a la pàgina de login.
    const signIn = loading ? undefined : () => {
        window.location.href = logoutUrl;
    };
    // El logout ha de ser una navegació real (mai fetch()): el backend respon amb un redirect a l'"end_session_endpoint"
    // de l'IdP, que és l'únic que esborra la cookie SSO del seu domini. Amb fetch() aquest redirect no el fa mai el
    // navegador, la sessió SSO queda oberta i en tornar a carregar l'aplicació l'usuari hi torna a entrar en silenci.
    // Un cop tancada, l'IdP torna a l'aplicació (post_logout_redirect_uri).
    const signOut = loading
        ? undefined
        : () => {
              debug && logConsole.debug('Tancament de sessió');
              window.location.href = signOutUrl;
          };
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
    return (
        <AuthContext.Provider value={context}>
            {showChildren ? children : null}
        </AuthContext.Provider>
    );
};
