import React from 'react';
import ReactDOM from 'react-dom/client';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from '@emotion/react';
import { CssBaseline } from '@mui/material';
import { LicenseInfo } from '@mui/x-license';
import theme from './theme';
import "@fontsource/noto-sans/400.css";
import "@fontsource/noto-sans/500.css";
import "@fontsource/noto-sans/700.css";
import App from './App.tsx'
import {
    envVar,
    KeycloakAuthProvider,
    ContainerAuthProvider,
    ResourceApiProvider
} from 'reactlib';
import { HelmetProvider } from '@dr.pogodin/react-helmet';

dayjs.extend(duration);

LicenseInfo.setLicenseKey('d7a3848ee04d821438959d61037634beTz0xMDY1ODQsRT0xNzY5Mjk5MTk5MDAwLFM9cHJvLExNPXN1YnNjcmlwdGlvbixQVj1pbml0aWFsLEtWPTI=');

export const envVars = {
    VITE_API_URL: import.meta.env.VITE_API_URL,
    VITE_API_PUBLIC_URL: import.meta.env.VITE_API_PUBLIC_URL,
    VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
    VITE_API_SUFFIX: import.meta.env.VITE_API_SUFFIX,
    VITE_AUTH_PROVIDER_URL: import.meta.env.VITE_AUTH_PROVIDER_URL,
    VITE_AUTH_PROVIDER_REALM: import.meta.env.VITE_AUTH_PROVIDER_REALM,
    VITE_AUTH_PROVIDER_CLIENTID: import.meta.env.VITE_AUTH_PROVIDER_CLIENTID,
}

const getAuthConfig = () => ({
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
}

const isAuthUrlPresent = envVar('VITE_AUTH_PROVIDER_URL', envVars) != null;
const AuthProvider = isAuthUrlPresent ? KeycloakAuthProvider : ContainerAuthProvider;

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <HelmetProvider>
            <AuthProvider logoutUrl={import.meta.env.BASE_URL} config={getAuthConfig()} mandatory>
                <ResourceApiProvider apiUrl={getEnvApiUrl()} userSessionActive>
                    <ThemeProvider theme={theme}>
                        <CssBaseline />
                        <BrowserRouter basename={import.meta.env.BASE_URL}>
                            <App />
                        </BrowserRouter>
                    </ThemeProvider>
                </ResourceApiProvider>
            </AuthProvider>
        </HelmetProvider>
    </React.StrictMode>
);
