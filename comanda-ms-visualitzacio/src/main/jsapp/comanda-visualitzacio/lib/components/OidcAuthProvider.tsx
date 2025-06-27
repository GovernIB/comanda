import React from 'react';
import { UserManager, UserManagerSettings, User } from 'oidc-client-ts';
import useLogConsole from '../util/useLogConsole';
import { isCurrentPathMatching } from '../util/url';
import AuthContext from './AuthContext';

const LOG_PREFIX = '[OAUTH]';

type AuthProviderProps = React.PropsWithChildren & {
    config: UserManagerSettings;
    mandatory?: boolean;
    offlineToken?: boolean;
    everetAuthPatch?: boolean;
    debug?: boolean;
};

const userManagerNewInstance = (config: UserManagerSettings) => {
    const userManager = new UserManager(config);
    return userManager;
}

export const AuthProvider = (props: AuthProviderProps) => {
    const {
        config,
        mandatory,
        debug,
        children
    } = props;
    const logConsole = useLogConsole(LOG_PREFIX);
    const hasInitialized = React.useRef(false);
    const [isLoading, setIsLoading] = React.useState<boolean>(true);
    const [isAuthenticated, setIsAuthenticated] = React.useState<boolean>(false);
    const tokenRef = React.useRef<string>(undefined);
    const tokenParsedRef = React.useRef<any>(undefined);
    const userManagerRef = React.useRef<UserManager>(undefined);
    const isAuthCallback = isCurrentPathMatching(config?.redirect_uri, true);
    const isAuthSilentRedirect = config?.silent_redirect_uri ? isCurrentPathMatching(config?.silent_redirect_uri, false) : false;
    const processUser = (user: User | null) => {
        if (user != null) {
            tokenRef.current = user.access_token;
            tokenParsedRef.current = user.profile;
            setIsLoading(false);
            setIsAuthenticated(true);
        } else {
            tokenRef.current = undefined;
            tokenParsedRef.current = undefined;
            setIsLoading(true);
            setIsAuthenticated(false);
        }
    }
    React.useEffect(() => {
        if (hasInitialized.current) {
            return; // evitem executar-ho la segona vegada (en dev amb StrictMode)
        }
        hasInitialized.current = true;
        const userManager = userManagerNewInstance(config);
        userManagerRef.current = userManager;
        userManager.startSilentRenew();
        const handleAuthFlow = async () => {
            try {
                if (isAuthSilentRedirect) {
                    debug && logConsole.debug('Callback de la renovació silenciosa');
                    await userManager.signinSilentCallback();
                } else if (isAuthCallback) {
                    debug && logConsole.debug('Callback des del servidor de recursos');
                    await userManager.signinRedirectCallback();
                    window.history.replaceState({}, document.title, '/');
                } else {
                    debug && logConsole.debug('Comprovant si l\'usuari ja està autenticat');
                    const loadedUser = await userManager.getUser();
                    if (loadedUser && !loadedUser.expired) {
                        debug && logConsole.debug('S\'ha trobat un usuari autenticat');
                        processUser(loadedUser);
                    } else {
                        debug && logConsole.debug('Provant renovació silenciosa');
                        try {
                            const user = await userManager.signinSilent();
                            debug && logConsole.debug('Usuari resultant de la renovació silenciosa', user);
                            processUser(user);
                        } catch (error: any) {
                            const isLoginRequired = error.error === 'login_required';
                            if (isLoginRequired && mandatory) {
                                debug && logConsole.debug('Silent renew ha fallat amb login_required. Redirigint a signin.');
                                userManager.signinRedirect();
                            } else {
                                debug && logConsole.debug('Error en silent renew', error);
                                processUser(null);
                            }
                        }
                    }
                }
            } catch (error) {
                logConsole.error('Error durant el flux d\'autenticació', error);
                processUser(null);
            }
        };
        handleAuthFlow();
        return () => {
            userManager.stopSilentRenew();
        }
    }, [config, debug, isAuthCallback, isAuthSilentRedirect, mandatory, logConsole]);
    React.useEffect(() => {
        const userManager = userManagerRef.current;
        if (userManager) {
            const onAccessTokenExpiring = () => {
                debug && logConsole.debug('Renovant token a punt d\'expirar');
                userManagerRef.current?.signinSilent().
                    then(user => {
                        debug && logConsole.debug('Token renovat correctament', user);
                        processUser(user);
                    }).
                    catch(error => {
                        logConsole.error('Error renovant el token', error);
                        // Forçam redirecció cap a la pantalla de login si la renovació falla
                        if (error.error === 'login_required') {
                            mandatory && userManagerRef.current?.signinRedirect();
                        }
                    });
            }
            userManager.events.addAccessTokenExpiring(onAccessTokenExpiring);
            return () => {
                userManager.events.removeAccessTokenExpiring(onAccessTokenExpiring);
            };
        }
    }, [debug, logConsole]);
    const signIn = isLoading ? undefined : () => {
        userManagerRef.current?.signinRedirect();
    }
    const signOut = isLoading ? undefined : () => {
        userManagerRef.current?.signoutRedirect();
    }
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
        config
    }
    const showChildren = !isLoading && !isAuthSilentRedirect && (!mandatory || (mandatory && isAuthenticated));
    return <AuthContext.Provider value={context}>
        {showChildren ? children : null}
    </AuthContext.Provider>;
}
