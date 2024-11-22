import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@emotion/react';
import { CssBaseline } from '@mui/material';
import { LicenseInfo } from '@mui/x-license';
import theme from './theme';
import App from './App.tsx'
import {
    envVar,
    KeycloakAuthProvider as AuthProvider,
    ResourceApiProvider
} from 'reactlib';

LicenseInfo.setLicenseKey('89030bade1b4837f62ee60358d25fa78Tz04MjczMyxFPTE3Mzc2MzAzMDcwMDAsUz1wcm8sTE09c3Vic2NyaXB0aW9uLEtWPTI=');

export const envVars = {
    VITE_API_URL: import.meta.env.VITE_API_URL,
    VITE_API_PUBLIC_URL: import.meta.env.VITE_API_PUBLIC_URL,
    VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
    VITE_API_SUFFIX: import.meta.env.VITE_API_SUFFIX,
    VITE_AUTH_KEYCLOAK_URL: import.meta.env.VITE_AUTH_KEYCLOAK_URL,
    VITE_AUTH_KEYCLOAK_REALM: import.meta.env.VITE_AUTH_KEYCLOAK_REALM,
    VITE_AUTH_KEYCLOAK_CLIENTID: import.meta.env.VITE_AUTH_KEYCLOAK_CLIENTID,
}

const getAuthConfig = () => ({
    url: envVar('VITE_AUTH_KEYCLOAK_URL', envVars),
    realm: envVar('VITE_AUTH_KEYCLOAK_REALM', envVars),
    clientId: envVar('VITE_AUTH_KEYCLOAK_CLIENTID', envVars),
});

const getEnvApiUrl = () => {
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
}

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <AuthProvider config={getAuthConfig()} mandatory everetAuthPatch>
            <ResourceApiProvider apiUrl={getEnvApiUrl()} userSessionActive /*defaultUserSession={{ i: 443, e: 987 }}*/>
                <ThemeProvider theme={theme}>
                    <CssBaseline />
                    <BrowserRouter basename={import.meta.env.BASE_URL}>
                        <App />
                    </BrowserRouter>
                </ThemeProvider>
            </ResourceApiProvider>
        </AuthProvider>
    </React.StrictMode>,
);
