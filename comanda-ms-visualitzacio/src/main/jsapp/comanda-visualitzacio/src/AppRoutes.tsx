import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { Navigate, useLocation } from 'react-router-dom';
import { Box, CircularProgress } from '@mui/material';
import { useResourceApiService } from 'reactlib';
import Salut from './pages/salut/Salut';
import NotFoundPage from './pages/NotFound';
import Apps, { AppForm } from './pages/Apps';
import Entorns from './pages/Entorns';
import Monitors from './pages/Monitor';
import Caches from './pages/Caches';
import Integracions from './pages/Integracions';
import Dimensions from './pages/Dimensions';
import DimensioValor from './pages/DimensioValor';
import Indicadors from './pages/Indicadors';
import EstadisticaWidget from './pages/EstadisticaWidget';
import EstadisticaDashboards from './pages/EstadisticaDashboards';
import EstadisticaDashboardEdit from './pages/EstadisticaDashboardEdit';
import EstadisticaDashboardView from './pages/EstadisticaDashboardView';
import VersionsEntorns from './pages/VersionsEntorns';
import Broker from './pages/Broker';
import QueueMessages from './pages/QueueMessages';
import CalendariEstadistiques from './pages/CalendariEstadistiques';
import Tasca from './pages/Tasca';
import Avis from './pages/Avis';
import Alarmes from './pages/Alarmes';
import AlarmaConfig, { AlarmaConfigForm } from './pages/AlarmaConfig';
import Parametres from './pages/Parametres';
import ProtectedRoute from './components/ProtectedRoute';
import Sitemap from './pages/Sitemap';
import Accessibilitat from './pages/accessibilitat/Accessibilitat';
import { useIsUserAdmin, useIsUserConsulta, useIsUserUsuari, useUserContext } from './components/UserContext';

export const DASHBOARDS_PATH = 'dashboard';
export const ESTADISTIQUES_PATH = 'estadistiques';

const LoadingRoute: React.FC = () => (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '50vh' }}>
        <CircularProgress />
    </Box>
);

const useHasSalutAccess = () => {
    const isUserAdmin = useIsUserAdmin();
    const isUserConsulta = useIsUserConsulta();
    const isUserUsuari = useIsUserUsuari();
    const { user } = useUserContext();
    const { isReady: entornAppApiIsReady, find: entornAppFind } = useResourceApiService('entornApp');
    const [hasSalutAccess, setHasSalutAccess] = React.useState<boolean>();

    React.useEffect(() => {
        if (!isUserUsuari && (isUserAdmin || isUserConsulta)) {
            setHasSalutAccess(true);
            return;
        }
        if (user == null || !entornAppApiIsReady) {
            return;
        }
        void entornAppFind({
            page: 0,
            size: 1,
            filter: 'activa:true and app.activa:true',
        }).then(response => {
            setHasSalutAccess((response.rows?.length ?? 0) > 0);
        }).catch(() => {
            setHasSalutAccess(false);
        });
    }, [entornAppApiIsReady, entornAppFind, isUserAdmin, isUserConsulta, isUserUsuari, user]);

    return hasSalutAccess;
};

const HomeRoute: React.FC = () => {
    const hasSalutAccess = useHasSalutAccess();

    if (hasSalutAccess === undefined) {
        return <LoadingRoute />;
    }

    if (!hasSalutAccess) {
        return <Navigate to="/tasca" replace />;
    }

    return <Salut />;
};

const UserRoleRouteGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const isUserAdmin = useIsUserAdmin();
    const isUserConsulta = useIsUserConsulta();
    // const isUserUsuari = useIsUserUsuari();
    const hasSalutAccess = useHasSalutAccess();
    const location = useLocation();

    if (isUserAdmin) {
        return <>{children}</>;
    }

    if (hasSalutAccess === undefined) {
        return <LoadingRoute />;
    }

    const allowedPrefixes = isUserConsulta
        ? [
            '/',
            '/appinfo',
            '/estadistiques',
            '/dashboard',
            '/app',
            '/entorn',
            '/versionsEntorn',
            '/integracio',
            '/dimensio',
            '/indicador',
            '/calendari',
            '/tasca',
            '/avis',
            '/alarma',
            '/alarmes',
            '/sitemap',
            '/accessibilitat',
        ]
        : [
            ...(hasSalutAccess ? ['/', '/appinfo'] : []),
            '/tasca',
            '/avis',
            '/alarma',
            '/alarmes',
            '/sitemap',
            '/accessibilitat',
        ];
    const isAllowedPath = allowedPrefixes.some(path =>
        location.pathname === path || (path !== '/' && location.pathname.startsWith(path + '/'))
    );

    if (isAllowedPath) {
        return <>{children}</>;
    }

    return <Navigate to={hasSalutAccess ? '/' : '/tasca'} replace />;
};

const AppRoutes: React.FC = () => {
    return (
        <UserRoleRouteGuard>
            <Routes>
                <Route index element={<HomeRoute />} />
                <Route path="/appinfo/:id" element={<Salut />} />
                <Route path={DASHBOARDS_PATH}>
                    <Route index element={<EstadisticaDashboards />} />
                    <Route path=":id" element={<EstadisticaDashboardEdit />} />
                </Route>
                <Route path={`${ESTADISTIQUES_PATH}/:id?`} element={<EstadisticaDashboardView />} />
                <Route path="app">
                    <Route index element={<Apps />} />
                    <Route path="form">
                        <Route index element={<AppForm />} />
                        <Route path=":id" element={<AppForm />} />
                    </Route>
                </Route>
                <Route path="entorn">
                    <Route index element={<Entorns />} />
                </Route>
                <Route path="versionsEntorn">
                    <Route index element={<VersionsEntorns />} />
                </Route>
                <Route path="monitor">
                    <Route index element={<Monitors />} />
                </Route>
                <Route path="cache">
                    <Route index element={<Caches />} />
                </Route>
                <Route path="integracio">
                    <Route index element={<Integracions />} />
                </Route>
                <Route path="dimensio">
                    <Route index element={<Dimensions />} />
                    <Route path="valor/:id" element={<DimensioValor />} />
                </Route>
                <Route path="indicador">
                    <Route index element={<Indicadors />} />
                </Route>
                <Route path="estadisticaWidget">
                    <Route index element={<EstadisticaWidget />} />
                </Route>
                <Route path="calendari">
                    <Route index element={<CalendariEstadistiques />} />
                </Route>
                <Route path="tasca">
                    <Route index element={<Tasca />} />
                </Route>
                <Route path="avis">
                    <Route index element={<Avis />} />
                </Route>
                <Route path="alarma">
                    <Route index element={<AlarmaConfig />} />
                    <Route path="form">
                        <Route index element={<AlarmaConfigForm />} />
                        <Route path=":id" element={<AlarmaConfigForm />} />
                    </Route>
                </Route>
                <Route path="alarmes">
                    <Route index element={<ProtectedRoute resourceName="alarma"><Alarmes /></ProtectedRoute>} />
                </Route>
                <Route path="broker">
                    <Route index element={<Broker />} />
                    <Route path="queue/:queueName" element={<QueueMessages />} />
                </Route>
                <Route path="parametre">
                    <Route index element={<Parametres />} />
                </Route>
                <Route path="sitemap" element={<Sitemap />} />
                <Route path="accessibilitat" element={<Accessibilitat />} />
                <Route path="*" element={<NotFoundPage />} />
            </Routes>
        </UserRoleRouteGuard>
    );
};

export default AppRoutes;
