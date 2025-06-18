import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Salut from './pages/Salut';
import SalutAppInfo from './pages/SalutAppInfo';
import NotFoundPage from './pages/NotFound';
import Apps, { AppForm } from './pages/Apps';
import Entorns from './pages/Entorns';
import Monitors from './pages/Monitor';
import EstadisticaWidget from './pages/EstadisticaWidget';
import EstadisticaDashboards from './pages/EstadisticaDashboards.tsx';
import EstadisticaDashboardEdit from './pages/EstadisticaDashboardEdit.tsx';
import EstadisticaDashboardView from './pages/EstadisticaDashboardView.tsx';

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
            <Route path="monitor">
                <Route index element={<Monitors />} />
            </Route>
            <Route path="estadisticaWidget">
                <Route index element={<EstadisticaWidget />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
};

export default AppRoutes;
