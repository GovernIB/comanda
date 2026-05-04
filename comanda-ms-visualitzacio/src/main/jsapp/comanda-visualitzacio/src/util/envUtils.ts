import { envVar } from 'reactlib';

export const envVars = {
    VITE_API_URL: import.meta.env.VITE_API_URL,
    VITE_API_PUBLIC_URL: import.meta.env.VITE_API_PUBLIC_URL,
    VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
    VITE_API_SUFFIX: import.meta.env.VITE_API_SUFFIX,
    VITE_AUTH_PROVIDER_URL: import.meta.env.VITE_AUTH_PROVIDER_URL,
    VITE_AUTH_PROVIDER_REALM: import.meta.env.VITE_AUTH_PROVIDER_REALM,
    VITE_AUTH_PROVIDER_CLIENTID: import.meta.env.VITE_AUTH_PROVIDER_CLIENTID,
};

export const getAuthConfig = () => ({
    url: envVar('VITE_AUTH_PROVIDER_URL', envVars),
    realm: envVar('VITE_AUTH_PROVIDER_REALM', envVars),
    clientId: envVar('VITE_AUTH_PROVIDER_CLIENTID', envVars),
});

export const getEnvApiUrl = () => {
    const envApiPublicUrl = envVar('VITE_API_PUBLIC_URL', envVars);
    const envApiUrl = envVar('VITE_API_URL', envVars);
    if (envApiPublicUrl || envApiUrl) {
        return envApiPublicUrl ?? envApiUrl;
    } else {
        const envApiBaseUrl = envVar('VITE_API_BASE_URL', envVars);
        const envApiSuffix = envVar('VITE_API_SUFFIX', envVars) ?? '/api';
        if (envApiBaseUrl) {
            return envApiBaseUrl + envApiSuffix;
        } else {
            return window.location.protocol + '//' + window.location.host + ':' + window.location.port + envApiSuffix;
        }
    }
};

export const isAuthUrlPresent = () => envVar('VITE_AUTH_PROVIDER_URL', envVars) != null;
