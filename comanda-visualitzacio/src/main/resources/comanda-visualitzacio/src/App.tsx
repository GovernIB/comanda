import React from 'react';
import { useTranslation } from 'react-i18next';
import { BaseApp } from './components/BaseApp';
import logo from './assets/goib_logo.png';
import headerBackground from './assets/background.jpg';
import AppRoutes from './AppRoutes';


export const App: React.FC = () => {
    const { t } = useTranslation();
    const menuEntries = [{
        id: 'salut',
        title: t('menu.salut'),
        to: '/',
        icon: 'monitor_heart',
    }, {
        id: 'app',
        title: t('menu.app'),
        to: '/app',
        icon: 'widgets',
        resourceName: 'app',
    }];
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
        availableLanguages={['ca', 'es']}
        menuEntries={menuEntries}
        appbarBackgroundColor="#083c6b"
        appbarBackgroundImg={headerBackground}>
        <AppRoutes />
    </BaseApp>;
}

export default App;
