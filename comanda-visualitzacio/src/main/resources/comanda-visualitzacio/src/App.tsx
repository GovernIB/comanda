import React from 'react';
import { BaseApp } from './components/BaseApp';
import logo from './assets/goib_logo.png';
import headerBackground from './assets/background.jpg';
import AppRoutes from './AppRoutes';

const availableLanguages = [{
    locale: 'ca',
    name: 'Català'
}, {
    locale: 'es',
    name: 'Castellà'
}];

const menuEntries = [{
    id: 'home',
    title: 'Inici',
    to: '/',
    icon: 'home',
}, {
    id: 'app',
    title: 'Aplicacions',
    to: '/app',
    icon: 'widgets',
    resourceName: 'app',
}];

export const App: React.FC = () => {
    return <BaseApp
        code="cmd"
        logo={logo}
        logoStyle={{
            '& img': { height: '38px' },
            pl: 2,
            pr: 4,
            mr: 4,
            borderRight: '2px solid #fff'
        }}
        title="Comanda"
        version="0.1"
        availableLanguages={availableLanguages}
        menuEntries={menuEntries}
        appbarBackgroundColor="#083c6b"
        appbarBackgroundImg={headerBackground}>
        <AppRoutes />
    </BaseApp>;
}

export default App;
