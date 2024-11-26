import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/Home';
import NotFoundPage from './pages/NotFound';
import Apps, { AppForm } from './pages/Apps';

const AppRoutes: React.FC = () => {
    return <Routes>
        <Route index element={<HomePage />} />
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