import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Salut from './pages/Salut';
import SalutAppInfo from './pages/SalutAppInfo';
import NotFoundPage from './pages/NotFound';
import Apps, { AppForm } from './pages/Apps';
import Entorns from './pages/Entorns';
import Monitors from './pages/Monitor';
import Caches from "./pages/Caches";
import Integracions from "./pages/Integracions";
import EstadisticaWidget from './pages/EstadisticaWidget';
import EstadisticaDashboards from './pages/EstadisticaDashboards';
import EstadisticaDashboardEdit from './pages/EstadisticaDashboardEdit';
import EstadisticaDashboardView from './pages/EstadisticaDashboardView';
import VersionsEntorns from './pages/VersionsEntorns';
import CalendariEstadistiques from './pages/CalendariEstadistiques.tsx';

export const DASHBOARDS_PATH = 'dashboard';
export const ESTADISTIQUES_PATH = 'estadistiques';

const AppRoutes: React.FC = () => {
    return (
        <Routes>
            <Route index element={<Salut />} />
            <Route path="/appinfo/:id" element={<SalutAppInfo />} />
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
            <Route path="estadisticaWidget">
                <Route index element={<EstadisticaWidget />} />
            </Route>
            <Route path="calendari">
                <Route index element={<CalendariEstadistiques />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
};

export default AppRoutes;
