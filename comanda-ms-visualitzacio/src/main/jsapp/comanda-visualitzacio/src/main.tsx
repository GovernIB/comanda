import React from 'react';
import ReactDOM from 'react-dom/client';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { BrowserRouter } from 'react-router-dom';
import { CssBaseline } from '@mui/material';
import { LicenseInfo } from '@mui/x-license';
import '@fontsource/noto-sans/400.css';
import '@fontsource/noto-sans/500.css';
import '@fontsource/noto-sans/700.css';
import App from './App.tsx';
import { OidcAuthProvider, ContainerAuthProvider } from 'reactlib';
import SseProvider from './components/SseProvider.tsx';
import UserProvider from './components/UserProvider';
import MuiThemeProvider from './components/MuiThemeProvider.tsx';
import { HelmetProvider } from '@dr.pogodin/react-helmet';
import AlarmsProvider from './components/AlarmsProvider.tsx';
import { getAuthConfig, getEnvApiUrl, isAuthUrlPresent } from './util/envUtils.ts';
import ParameterProvider from './components/ParameterProvider.tsx';
import ResourceApiWithInterceptorProvider from './components/ResourceApiWithInterceptorProvider.tsx';

dayjs.extend(duration);

LicenseInfo.setLicenseKey('e0bde345c6cb2453171a44e15a0c58f5Tz0xMjQ4NTIsRT0xODAxMDk0Mzk5MDAwLFM9cHJvLExNPXN1YnNjcmlwdGlvbixQVj1pbml0aWFsLEtWPTI=');

const AuthProvider = isAuthUrlPresent() ? OidcAuthProvider : ContainerAuthProvider;

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <HelmetProvider>
            <AuthProvider
                appBaseUrl={import.meta.env.BASE_URL}
                logoutUrl={import.meta.env.BASE_URL}
                config={getAuthConfig()}
                mandatory
                debug={import.meta.env.DEV ? true : undefined}
            >
                <ResourceApiWithInterceptorProvider apiUrl={getEnvApiUrl()} userSessionActive>
                    <ParameterProvider>
                        <UserProvider>
                            <SseProvider>
                                <AlarmsProvider>
                                    <MuiThemeProvider>
                                        <CssBaseline enableColorScheme />
                                        <BrowserRouter basename={import.meta.env.BASE_URL}>
                                            <App />
                                        </BrowserRouter>
                                    </MuiThemeProvider>
                                </AlarmsProvider>
                            </SseProvider>
                        </UserProvider>
                    </ParameterProvider>
                </ResourceApiWithInterceptorProvider>
            </AuthProvider>
        </HelmetProvider>
    </React.StrictMode>
);
