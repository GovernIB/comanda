import React from 'react';
import Keycloak, { KeycloakError } from 'keycloak-js';
import useLogConsole, { LogConsoleType } from '../util/useLogConsole';
import AuthContext, { AuthConfig } from './AuthContext';

const LOG_PREFIX = '[KAUTH]';

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
     * comprovació periòdica de la sessió SSO (checkLoginIframe, per defecte cada 5 s) a un iframe ocult. Sense
     * això, quan aquesta comprovació detecta la sessió com a "canviada", keycloak-js fa un redirect de tota la
     * finestra cap a l'endpoint /auth i tornada, que es percep com un refresc de la interfície sense interacció
     * de l'usuari. Molt recomanable configurar-ho sempre que s'usi 'check-sso'.
     */
    silentCheckSsoRedirectUri?: string;
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
    const { config, mandatory, offlineAccess, forceCheckSso, silentCheckSsoRedirectUri, debug, children } = props;
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
