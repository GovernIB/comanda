import { Routes, Route } from 'react-router-dom';
import Salut from './pages/Salut';
import SalutAppInfo from './pages/SalutAppInfo';
import NotFoundPage from './pages/NotFound';
import Apps, { AppForm } from './pages/Apps';

const AppRoutes: React.FC = () => {
    return <Routes>
        <Route index element={<Salut />} />
        <Route path="/appinfo/:code" element={<SalutAppInfo />} />
        <Route path="app">
            <Route index element={<Apps />} />
            <Route path="form">
                <Route index element={<AppForm />} />
                <Route path=":id" element={<AppForm />} />
            </Route>
        </Route>
        <Route path="*" element={<NotFoundPage />} />
    </Routes>;
}

export default AppRoutes;