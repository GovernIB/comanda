import React from 'react';
import { useTranslation } from 'react-i18next';
import { BaseApp } from './components/BaseApp';
import logo from './assets/goib_logo.svg';
import comandaLogo from './assets/COM_DRA_COL.svg';
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
    const menuEstadistiquesReduit = {
        id: 'estadistiques',
        title: t('menu.estadistiques'),
        to: '/estadistiques',
        icon: 'bar_chart',
        resourceName: 'dashboard',
    }
    const menuEstadistiques = {
        id: 'estadistiques',
        title: t('menu.estadistiques'),
        icon: 'bar_chart',
        children: [
            {
                id: 'estadistiques',
                title: t('menu.estadistiques'),
                to: '/estadistiques',
                icon: 'bar_chart',
                resourceName: 'dashboard',
            },
            {
                id: 'estadisticaWidget',
                title: t('menu.widget'),
                to: '/estadisticaWidget',
                icon: 'widgets',
                resourceName: 'dashboard',
            },
            {
                id: 'dashboard',
                title: t('menu.dashboard'),
                to: '/dashboard',
                icon: 'dashboardCustomize',
                resourceName: 'dashboard',
            },
            {
                id: 'calendari',
                title: t('menu.calendari'),
                to: '/calendari',
                icon: 'calendar_month',
                resourceName: 'fet',
            },
        ],
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
                icon: 'apps',
                resourceName: 'app',
            },
            {
                id: 'entorn',
                title: t('menu.entorn'),
                to: '/entorn',
                icon: 'domain',
                resourceName: 'entorn',
            },
            {
                id: 'versionsEntorn',
                title: t('menu.versionsEntorn'),
                to: '/versionsEntorn',
                icon: 'format_list_numbered_rtl',
                resourceName: 'entornApp',
            },
            {
                id: 'integracio',
                title: t('menu.integracio'),
                to: '/integracio',
                icon: 'integration_instructions',
                resourceName: 'integracio',
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
    const menuCache = {
        id: 'cache',
        title: t('menu.cache'),
        to: '/cache',
        icon: 'storage',
        resourceName: 'comandaCache',
    }
    const menuBroker = {
        id: 'broker',
        title: t('menu.broker'),
        to: '/broker',
        icon: 'send_time_extension',
        resourceName: 'broker',
    }
    const menuEntries = [
        menuSalut,
        menuEstadistiques,
        menuConfiguracio,
        menuMonitor,
        menuCache,
        menuBroker
    ];
    const appMenuEntries = [
        menuSalut,
        menuEstadistiquesReduit
    ];
    return (
        <BaseApp
            code="com"
            logo={logo}
            logoStyle={{
                '& img': { height: '38px' },
                pl: 2,
                pr: 4,
                mr: 4,
                borderRight: '2px solid #000',
            }}
            title={<img style={{ height: '64px', verticalAlign: 'middle' }} src={comandaLogo} alt="Logo de l'aplicació de Comanda" />}
            version="0.1"
            availableLanguages={['ca', 'es']}
            menuEntries={menuEntries}
            appMenuEntries={appMenuEntries}
            appbarBackgroundColor="#fff"
            appbarStyle={{ cssText: 'min-height: 64px !important; background-color: #fff !important' }}
            // appbarBackgroundImg={headerBackground}
        >
            <AppRoutes />
        </BaseApp>
    );
};

export default App;
