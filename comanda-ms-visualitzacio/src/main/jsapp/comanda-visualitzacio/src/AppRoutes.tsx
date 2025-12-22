import React from 'react';
import { Routes, Route } from 'react-router-dom';
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
import TemplateEstils from './pages/TemplateEstils';
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

export const DASHBOARDS_PATH = 'dashboard';
export const ESTADISTIQUES_PATH = 'estadistiques';

const AppRoutes: React.FC = () => {
    return (
        <Routes>
            <Route index element={<ProtectedRoute resourceName="salut"><Salut /></ProtectedRoute>} />
            <Route path="/appinfo/:id" element={<ProtectedRoute resourceName="salut"><Salut /></ProtectedRoute>} />
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
            <Route path="templateEstils">
                <Route index element={<TemplateEstils />} />
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
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
};

export default AppRoutes;
