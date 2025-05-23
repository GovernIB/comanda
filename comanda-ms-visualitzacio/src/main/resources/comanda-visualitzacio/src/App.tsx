import React from 'react';
import { useTranslation } from 'react-i18next';
import { BaseApp } from './components/BaseApp';
import logo from './assets/goib_logo.svg';
// import headerBackground from './assets/background.jpg';
import AppRoutes from './AppRoutes';

export const App: React.FC = () => {
    const { t } = useTranslation();
    const menuSalut = {
        id: 'salut',
        title: t('menu.salut'),
        to: '/',
        icon: 'monitor_heart',
        resourceName: 'salut',
    };
    const menuEstadistiques = {
        id: 'estadistiques',
        title: t('menu.estadistiques'),
        to: '/estadistiques',
        icon: 'bar_chart',
        resourceName: 'fet',
    };
    const menuConfiguracio = {
        id: 'configuracio',
        title: t('menu.configuracio'),
        icon: 'settings',
        children: [
            {
                id: 'app',
                title: t('menu.app'),
                to: '/app',
                icon: 'widgets',
                resourceName: 'app',
            },
            {
                id: 'entorn',
                title: t('menu.entorn'),
                to: '/entorn',
                icon: 'domain',
                resourceName: 'entorn',
            },
        ]
    };
    const menuMonitor = {
        id: 'monitor',
        title: t('menu.monitor'),
        to: '/monitor',
        icon: 'monitor',
        resourceName: 'monitor',
    }
    const menuEntries = [
        menuSalut,
        menuEstadistiques,
        menuConfiguracio,
        menuMonitor
    ];
    const appMenuEntries = [
        menuSalut,
        menuEstadistiques
    ];
    return (
        <BaseApp
            code="cmd"
            logo={logo}
            logoStyle={{
                '& img': { height: '38px' },
                pl: 2,
                pr: 4,
                mr: 4,
                borderRight: '2px solid #000',
            }}
            title="Comanda"
            version="0.1"
            availableLanguages={['ca', 'es']}
            menuEntries={menuEntries}
            appMenuEntries={appMenuEntries}
            appbarBackgroundColor="#fff"
            // appbarBackgroundImg={headerBackground}
        >
            <AppRoutes />
        </BaseApp>
    );
};

export default App;
