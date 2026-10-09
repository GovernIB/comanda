import React from 'react';
import Keycloak, { KeycloakError } from 'keycloak-js';
import useLogConsole, { LogConsoleType } from '../util/useLogConsole';
import AuthContext, { AuthConfig } from './AuthContext';

const LOG_PREFIX = '[KAUTH]';
// Per defecte, cada quant temps forçam una renovació del token mentre la finestra estigui oberta i autenticada
// (vegeu el useEffect de manteniment de sessió a AuthProvider). 5 minuts és prudencialment curt respecte als
// "SSO Session Idle" habituals (sovint 15-30 min), per evitar que la sessió es doni per inactiva a Keycloak.
const DEFAULT_SESSION_KEEPALIVE_INTERVAL_MS = 5 * 60 * 1000;

type KeycloakAuthProviderProps = React.PropsWithChildren & {
    /** La configuració necessària per a crear la instància del Keycloak */
    config: AuthConfig;
    /** Indica que l'autenticació és obligatòria (no es pot veure res si no s'està autenticat) */
    mandatory?: true;
    /** Indica que s'ha d'activar l'access token offline */
    offlineAccess?: true;
    /** Indica si s'ha de forçar el valor 'check-sso' a l'onLoad */
    forceCheckSso?: true;
    /**
     * URL (relativa o absoluta) d'una pàgina estàtica mínima (vegeu public/silent-check-sso.html) que confina la
     * comprovació SSO inicial (feta durant `init()` quan s'usa 'check-sso') a un iframe ocult, evitant que aquesta
     * comprovació puntual faci un redirect de tota la finestra cap a l'endpoint /auth i tornada. Molt recomanable
     * configurar-ho sempre que s'usi 'check-sso'.
     *
     * @warning Això NO evita el refresc de tota la interfície causat per checkLoginIframe (vegeu més avall):
     * és una comprovació diferent, feta només una vegada a l'inici.
     */
    silentCheckSsoRedirectUri?: string;
    /**
     * Interval (en ms) amb què es força una renovació del token mentre l'aplicació estigui oberta i autenticada,
     * encara que l'access token vigent no hagi caducat (vegeu el comentari de DEFAULT_SESSION_KEEPALIVE_INTERVAL_MS).
     * Sense això, si l'"Access Token Lifespan" del realm és igual o més llarg que el "SSO Session Idle", pot passar
     * que no es faci cap petició a Keycloak durant tot aquest temps: la sessió es dona per inactiva i caduca, i la
     * següent renovació (a onTokenExpired) falla amb un redirect complet de finestra (pèrdua de l'estat de la UI
     * no persistit) encara que l'usuari hagi tingut la pantalla oberta tota l'estona.
     */
    sessionKeepAliveIntervalMs?: number;
    /** Indica si s'han d'imprimir a la consola missatges de depuració */
    debug?: true;
};

const kcInit = async (
    keycloak: Keycloak,
    mandatory: boolean | undefined,
    offlineAccess: boolean | undefined,
    forceCheckSso: boolean | undefined,
    silentCheckSsoRedirectUri: string | undefined,
    debug: boolean | undefined,
    logConsole: LogConsoleType
) => {
    try {
        const isAuthenticated = await keycloak.init({
            onLoad: forceCheckSso ? 'check-sso' : mandatory ? 'login-required' : 'check-sso',
            scope: offlineAccess ? 'offline_access' : undefined,
            silentCheckSsoRedirectUri,
            // Sense això, si el navegador bloqueja les cookies de tercers, keycloak-js sobreescriu en
            // silenci silentCheckSsoRedirectUri a 'false' i torna a caure en el redirect de finestra
            // completa que precisament volem evitar.
            silentCheckSsoFallback: silentCheckSsoRedirectUri ? false : undefined,
            // Per defecte keycloak-js comprova cada 5 s (checkLoginIframe) si la sessió SSO ha "canviat" mitjançant
            // un iframe ocult. Aquesta comprovació és poc fiable (falsos positius per rotació normal de l'estat de
            // la sessió, bloqueig de cookies de tercers...) i, quan detecta un "canvi", crida internament
            // kc.clearToken(), que -amb onLoad 'login-required'- fa un kc.login() immediat, és a dir, un redirect
            // de tota la finestra cap a l'endpoint /auth i tornada. Com que no hi ha cap interacció de l'usuari,
            // això es percep com un "refresc" periòdic i espontani de tota la interfície, amb la conseqüent pèrdua
            // de qualsevol estat no persistit (filtres, pàgina, mida de pàgina, amplada/ordre de columnes...).
            // El refresc del token ja el gestiona onTokenExpired/updateToken() més avall, així que no necessitam
            // aquesta comprovació addicional.
            checkLoginIframe: false,
            enableLogging: debug,
        });
        debug && logConsole.debug('Initialized', '(isAuthenticated=' + isAuthenticated + ')');
    } catch (error) {
        logConsole.error('Failed to initialize adapter:', error);
    }
    return keycloak;
};

const kcNewInstance = (
    authConfig: any,
    mandatory: boolean | undefined,
    offlineToken: boolean | undefined,
    forceCheckSso: boolean | undefined,
    silentCheckSsoRedirectUri: string | undefined,
    setIsLoading: React.Dispatch<React.SetStateAction<boolean>>,
    setIsAuthenticated: React.Dispatch<React.SetStateAction<boolean>>,
    setToken: (token: string | undefined) => void,
    setTokenParsed: (tokenParsed: any | undefined) => void,
    debug: boolean | undefined,
    logConsole: LogConsoleType
) => {
    const keycloak = new Keycloak(authConfig);
    kcInit(keycloak, mandatory, offlineToken, forceCheckSso, silentCheckSsoRedirectUri, debug, logConsole);
    keycloak.onReady = (isAuthenticated) => {
        debug && logConsole.debug('Callback onReady', isAuthenticated);
        setIsLoading(false);
    };
    keycloak.onAuthSuccess = () => {
        debug && logConsole.debug('Callback onAuthSuccess');
        setIsAuthenticated(true);
        setToken(keycloak.token);
        setTokenParsed(keycloak.tokenParsed);
    };
    keycloak.onAuthError = (errorData: KeycloakError) => {
        logConsole.error(
            'Callback onAuthError',
            '[' + errorData?.error + ']',
            errorData?.error_description
        );
        setIsAuthenticated(false);
        setToken(undefined);
        setTokenParsed(undefined);
    };
    keycloak.onAuthRefreshSuccess = () => {
        debug && logConsole.debug('Callback onAuthRefreshSuccess');
        setIsAuthenticated(keycloak.authenticated ?? false);
        setToken(keycloak.token);
        setTokenParsed(keycloak.tokenParsed);
    };
    keycloak.onAuthRefreshError = () => {
        logConsole.error('Callback onAuthRefreshError');
        setIsAuthenticated(false);
        setToken(undefined);
        setTokenParsed(undefined);
    };
    keycloak.onAuthLogout = () => {
        debug && logConsole.debug('Callback onAuthLogout');
        setIsAuthenticated(false);
        setToken(undefined);
        setTokenParsed(undefined);
    };
    keycloak.onTokenExpired = async () => {
        try {
            debug && logConsole.debug('Callback onTokenExpired, refreshing token');
            const refreshed = await keycloak.updateToken(-1);
            if (refreshed) {
                debug && logConsole.debug('Callback onTokenExpired, token was refreshed');
            } else {
                debug && logConsole.debug('Callback onTokenExpired, token is still valid');
            }
        } catch (error) {
            logConsole.error('Callback onTokenExpired, failed to refresh the token:', error);
        }
    };
    return keycloak;
};

export const AuthProvider = (props: KeycloakAuthProviderProps) => {
    const {
        config,
        mandatory,
        offlineAccess,
        forceCheckSso,
        silentCheckSsoRedirectUri,
        sessionKeepAliveIntervalMs,
        debug,
        children,
    } = props;
    const logConsole = useLogConsole(LOG_PREFIX);
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const [isAuthenticated, setIsAuthenticated] = React.useState<boolean>(false);
    const tokenRef = React.useRef<string>(undefined);
    const tokenParsedRef = React.useRef<any>(undefined);
    const keycloakRef = React.useRef<Keycloak>(undefined);
    React.useEffect(() => {
        if (!keycloakRef.current) {
            debug && logConsole.debug('Nova instància de Keycloak', config);
            const keycloak = kcNewInstance(
                config,
                mandatory,
                offlineAccess,
                forceCheckSso,
                silentCheckSsoRedirectUri,
                setIsLoading,
                setIsAuthenticated,
                (token: string | undefined) => (tokenRef.current = token),
                (tokenParsed: any | undefined) => (tokenParsedRef.current = tokenParsed),
                debug,
                logConsole
            );
            keycloakRef.current = keycloak;
        }
    }, []);
    React.useEffect(() => {
        if (forceCheckSso && !isLoading && mandatory && !isAuthenticated) {
            keycloakRef.current?.login();
        }
    }, [forceCheckSso, isLoading, mandatory, isAuthenticated]);
    React.useEffect(() => {
        if (!isAuthenticated) {
            return;
        }
        const intervalMs = sessionKeepAliveIntervalMs ?? DEFAULT_SESSION_KEEPALIVE_INTERVAL_MS;
        const intervalId = setInterval(() => {
            debug && logConsole.debug('Manteniment de sessió: forçant renovació periòdica del token');
            keycloakRef.current?.updateToken(-1)?.catch((error) => {
                logConsole.error('Manteniment de sessió: no s\'ha pogut renovar el token', error);
            });
        }, intervalMs);
        return () => clearInterval(intervalId);
    }, [isAuthenticated, sessionKeepAliveIntervalMs, debug, logConsole]);
    const signIn = isLoading
        ? undefined
        : () => {
              keycloakRef.current?.login();
          };
    const signOut = isLoading
        ? undefined
        : () => {
              keycloakRef.current?.logout();
          };
    const context = {
        isLoading,
        isReady: !isLoading,
        isAuthenticated,
        bearerTokenActive: true,
        getToken: () => tokenRef.current,
        getTokenParsed: () => tokenParsedRef.current,
        getUserId: () => tokenParsedRef.current?.['preferred_username'],
        getUserName: () => tokenParsedRef.current?.['name'],
        getUserEmail: () => tokenParsedRef.current?.['email'],
        signIn,
        signOut,
    };
    const showChildren = !isLoading && (!mandatory || (mandatory && isAuthenticated));
    return (
        <AuthContext.Provider value={context}>
            {showChildren ? children : null}
        </AuthContext.Provider>
    );
};
