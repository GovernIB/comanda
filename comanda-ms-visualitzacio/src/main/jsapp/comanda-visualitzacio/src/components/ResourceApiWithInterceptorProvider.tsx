import {
    ResourceApiProvider,
    ResourceApiProviderProps,
    AuthContext,
    KettingResponseInterceptor,
} from 'reactlib';
import { use, useCallback, useRef } from 'react';

const ResourceApiWithInterceptorProvider = (props: ResourceApiProviderProps) => {
    const { children, ...resourceApiProviderProps } = props;
    const authContext = use(AuthContext);

    const getTokenParsed = authContext?.getTokenParsed;
    const signOut = authContext?.signOut;
    const sessionRedirectTriggeredRef = useRef<boolean>(false);
    const responseInterceptor = useCallback<KettingResponseInterceptor>(response => {
        const exp = getTokenParsed?.()?.exp;
        const isSessionInvalid = !getTokenParsed?.() || (exp != null && exp * 1000 <= Date.now());
        if (
            signOut &&
            !sessionRedirectTriggeredRef.current &&
            (response.status === 401 || (response.status === 403 && isSessionInvalid))
        ) {
            sessionRedirectTriggeredRef.current = true;
            signOut();
        }
        return response;
    }, [getTokenParsed, signOut]);
    return (
        <ResourceApiProvider
            kettingResponseInterceptor={responseInterceptor}
            {...resourceApiProviderProps}
        >
            {children}
        </ResourceApiProvider>
    );
};

export default ResourceApiWithInterceptorProvider;
