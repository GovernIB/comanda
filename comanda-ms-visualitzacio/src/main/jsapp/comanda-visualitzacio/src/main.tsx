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
import { KeycloakAuthProvider, ContainerAuthProvider, ResourceApiProvider } from 'reactlib';
import SseProvider from './components/SseProvider.tsx';
import UserProvider from './components/UserProvider';
import MuiThemeProvider from './components/MuiThemeProvider.tsx';
import { HelmetProvider } from '@dr.pogodin/react-helmet';
import AlarmsProvider from './components/AlarmsProvider.tsx';
import { getAuthConfig, getEnvApiUrl, isAuthUrlPresent } from './util/envUtils.ts';

dayjs.extend(duration);

LicenseInfo.setLicenseKey('e0bde345c6cb2453171a44e15a0c58f5Tz0xMjQ4NTIsRT0xODAxMDk0Mzk5MDAwLFM9cHJvLExNPXN1YnNjcmlwdGlvbixQVj1pbml0aWFsLEtWPTI=');

const AuthProvider = isAuthUrlPresent() ? KeycloakAuthProvider : ContainerAuthProvider;

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <HelmetProvider>
            <AuthProvider logoutUrl={import.meta.env.BASE_URL} config={getAuthConfig()} mandatory>
                <ResourceApiProvider apiUrl={getEnvApiUrl()} userSessionActive>
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
                </ResourceApiProvider>
            </AuthProvider>
        </HelmetProvider>
    </React.StrictMode>
);
