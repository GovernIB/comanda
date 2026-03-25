import React from 'react';
import { useTranslation } from 'react-i18next';
import { BaseApp, MenuEntryWithResource } from './components/BaseApp';
import logo from './assets/goib_logo.svg';
import logoDark from './assets/goib_logo.png';
import ComandaLogo from './assets/COM_DRA_COL.svg?react';
import AppRoutes from './AppRoutes';
import { useIsUserAdmin, useIsUserUsuari, useUserContext } from './components/UserContext';
import KeepAlive from './components/KeepAlive';
import { useTheme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import { useResourceApiService } from 'reactlib';
import useStatsEnabled from './hooks/useStatsEnabled';
import notNull from './util/arrayUtils';
import { MenuEstil } from './types/usuari.model.tsx';

const APPBAR_HEIGHT = '64px';

export const App: React.FC = () => {
    const { user } = useUserContext();
    const isUserAdmin = useIsUserAdmin();
    const isUserUsuari = useIsUserUsuari();
    const isUserReady = user != null;
    const isLimitedUser = isUserReady && isUserUsuari;
    const { t } = useTranslation();
    const theme = useTheme();
    const statsEnabled = useStatsEnabled() === true;
    const darkThemeActive = theme.palette.mode === "dark";
    const menuAppearance =
        user?.estilMenu === MenuEstil.TEMA_INVERTIT
            ? 'inverse'
            : user?.estilMenu === MenuEstil.PEU
              ? 'footer'
              : 'theme';
    const appbarBackgroundColor = darkThemeActive ? undefined : '#fff';
    const { isReady: entornAppApiIsReady, find: entornAppFind } = useResourceApiService('entornApp');
    const [hasSalutAccess, setHasSalutAccess] = React.useState(false);
    const menuSalut = {
        id: 'salut',
        title: t($ => $.menu.salut),
        to: '/',
        icon: 'monitor_heart',
        resourceName: 'salut',
    };
    const menuEstadistiques = {
        id: 'estadistiques',
        title: t($ => $.menu.estadistiques),
        to: '/estadistiques',
        icon: 'bar_chart',
        resourceName: 'dashboard',
    }
    const menuTasca = {
        id: 'tasca',
        title: t($ => $.menu.tasca),
        to: '/tasca',
        icon: 'assignment',
        resourceName: 'tasca',
    }
    const menuAvis = {
        id: 'avis',
        title: t($ => $.menu.avis),
        to: '/avis',
        icon: 'warning',
        resourceName: 'avis',
    }
    const menuMonitoritzacio = {
        id: 'monitoritzacio',
        title: t($ => $.menu.monitoritzacio),
        // description: t($ => $.menu.monitoritzacioDescription),
        icon: 'monitor',
        resourceName: 'monitor',
        children: [
            {
                id: 'monitor',
                title: t($ => $.menu.monitor),
                to: '/monitor',
                icon: 'monitor',
                resourceName: 'monitor',
            },
            {
                id: 'cache',
                title: t($ => $.menu.cache),
                to: '/cache',
                icon: 'storage',
                resourceName: 'comandaCache',
            },
            {
                id: 'broker',
                title: t($ => $.menu.broker),
                to: '/broker',
                icon: 'send_time_extension',
                resourceName: 'broker',
            }
        ]
    };
    const menuAlarmaConfig = {
        id: 'alarma',
        // title: isUserAdmin ? t($ => $.menu.alarmaConfig) : t($ => $.menu.alarmaConfigConsultor),
        title: t($ => $.menu.alarmaConfig),
        to: '/alarma',
        icon: 'notifications',
        resourceName: 'alarmaConfig',
    };
    const menuConfiguracio = {
        id: 'configuracio',
        title: t($ => $.menu.configuracio),
        description: t($ => $.menu.configuracioDescription),
        icon: 'settings',
        children: ([
            {
                id: 'app',
                title: t($ => $.menu.app),
                to: '/app',
                icon: 'apps',
                resourceName: 'app',
            },
            {
                id: 'entorn',
                title: t($ => $.menu.entorn),
                to: '/entorn',
                icon: 'layers',
                resourceName: 'entorn',
            },
            {
                id: 'versionsEntorn',
                title: t($ => $.menu.versionsEntorn),
                to: '/versionsEntorn',
                icon: 'format_list_numbered_rtl',
                resourceName: 'entornApp',
            },
            menuAlarmaConfig,
            {
                id: 'integracio',
                title: t($ => $.menu.integracio),
                to: '/integracio',
                icon: 'integration_instructions',
                resourceName: 'integracio',
            },
            statsEnabled ? {
                id: 'dimensio',
                title: t($ => $.menu.dimensio),
                to: '/dimensio',
                icon: 'category',
                resourceName: 'dimensio',
            } : null,
            statsEnabled ? {
                id: 'indicador',
                title: t($ => $.menu.indicador),
                to: '/indicador',
                icon: 'insights',
                resourceName: 'indicador',
            } : null,
            statsEnabled && isUserAdmin ? {
                id: 'estadisticaWidget',
                title: t($ => $.menu.widget),
                to: '/estadisticaWidget',
                icon: 'widgets',
                resourceName: 'dashboard',
            } : null,
            statsEnabled ? {
                id: 'dashboard',
                title: t($ => $.menu.dashboard),
                to: '/dashboard',
                icon: 'dashboardCustomize',
                resourceName: 'dashboard',
            } : null,
            statsEnabled ? {
                id: 'calendari',
                title: t($ => $.menu.calendari),
                to: '/calendari',
                icon: 'calendar_month',
                resourceName: 'fet',
            } : null,
            isUserAdmin ? {
                id: 'parametre',
                title: t($ => $.menu.parametre),
                to: '/parametre',
                icon: 'settings',
                resourceName: 'parametre',
            } : null,
        ].filter(notNull))
    };
    const caibMenuEntries: MenuEntryWithResource[] = [
        menuSalut,
        statsEnabled ? menuEstadistiques : null,
        menuTasca,
        menuAvis,
        menuMonitoritzacio,
        menuConfiguracio,
    ].filter(notNull);
    const limitedMenuEntries: MenuEntryWithResource[] = [
        hasSalutAccess ? { ...menuSalut, resourceName: undefined } : null,
        { ...menuTasca, resourceName: undefined },
        { ...menuAvis, resourceName: undefined },
        { ...menuAlarmaConfig, resourceName: undefined },
    ].filter(notNull);
    const visibleMenuEntries = !isUserReady
        ? undefined
        : isLimitedUser
            ? limitedMenuEntries
            : caibMenuEntries;

    React.useEffect(() => {
        if (!isLimitedUser) {
            setHasSalutAccess(false);
            return;
        }
        if (!entornAppApiIsReady) {
            return;
        }
        void entornAppFind({
            page: 0,
            size: 1,
            filter: 'activa:true and app.activa:true',
        }).then(response => {
            setHasSalutAccess((response.rows?.length ?? 0) > 0);
        }).catch(() => {
            setHasSalutAccess(false);
        });
    }, [entornAppApiIsReady, entornAppFind, isLimitedUser]);

    return (
        <BaseApp
            code="com"
            logo={darkThemeActive ? logoDark : logo}
            logoStyle={{
                '& img': { height: '38px', width: '115px' },
                pl: 2,
                pr: 4,
                mr: 4,
                borderRightWidth: "1px",
                borderRightStyle: "solid",
                borderRightColor: theme.palette.divider,
            }}
            title={
                <Box
                    sx={{
                        '& .cls-1': {
                            fill: darkThemeActive ? 'white' : undefined,
                        },
                    }}
                >
                    <ComandaLogo
                        style={{ height: APPBAR_HEIGHT, verticalAlign: 'middle' }}
                        title="Logo de l'aplicació de Comanda"
                    />
                </Box>
            }
            version="0.1"
            availableLanguages={['ca', 'es']}
            menuEntries={visibleMenuEntries}
            menuAppearance={menuAppearance}
            appbarBackgroundColor={appbarBackgroundColor}
            appbarStyle={{
                cssText: `min-height: ${APPBAR_HEIGHT} !important`,
                color: appbarBackgroundColor ? theme.palette.getContrastText(appbarBackgroundColor) : undefined,
            }}
            // appbarBackgroundImg={headerBackground}
            defaultMuiComponentProps={{
                dataGrid: {
                    pageSizeOptions: [10, 20, 50, 100],
                    paginationModel:
                        user?.numElementsPagina != null && user.numElementsPagina !== 'AUTOMATIC'
                            ? {
                                  page: 0,
                                  pageSize: parseInt(user.numElementsPagina.replace('_', '')),
                              }
                            : undefined,
                },
                form: {
                    commonFieldComponentProps: {
                        size: 'small',
                    },
                },
                filter: {
                    commonFieldComponentProps: {
                        size: 'small',
                    },
                },
            }}
        >
            <KeepAlive />
            <AppRoutes />
        </BaseApp>
    );
};

export default App;
