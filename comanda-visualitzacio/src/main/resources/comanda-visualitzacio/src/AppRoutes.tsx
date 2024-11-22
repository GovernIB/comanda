import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/Home';
import NotFoundPage from './pages/NotFound';
import Apps from './pages/Apps';

const AppRoutes: React.FC = () => {
    return <Routes>
        <Route index element={<HomePage />} />
        <Route path="app">
            <Route index element={<Apps />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
    </Routes>;
}

export default AppRoutes;