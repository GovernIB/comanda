import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Salut from './pages/Salut';
import SalutAppInfo from './pages/SalutAppInfo';
import NotFoundPage from './pages/NotFound';
import Apps, { AppForm } from './pages/Apps';
import Entorns from './pages/Entorns';
import Monitors from './pages/Monitor';

const AppRoutes: React.FC = () => {
    return (
        <Routes>
            <Route index element={<Salut />} />
            <Route path="/appinfo/:id" element={<SalutAppInfo />} />
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
                {/* <Route path=":id" element={<MonitorForm />} />
                <Route path="form">
                    <Route index element={<MonitorForm />} />
                    <Route path=":id" element={<MonitorForm />} />
                </Route> */}
            </Route>
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
};

export default AppRoutes;
