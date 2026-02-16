import React from 'react';
import { useTranslation } from 'react-i18next';
import {BaseApp, MenuEntryWithResource} from './components/BaseApp';
import logo from './assets/goib_logo.svg';
import logoDark from './assets/goib_logo.png';
import ComandaLogo from './assets/COM_DRA_COL.svg?react';
import AppRoutes from './AppRoutes';
import { useUserContext } from './components/UserContext';
import KeepAlive from './components/KeepAlive';
import { useTheme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import {useResourceApiContext} from "reactlib";

const APPBAR_HEIGHT = '64px';

const filterEntriesByResources = (menuEntries: MenuEntryWithResource[], resourceNames?:string[]): MenuEntryWithResource[] => {
    if (!resourceNames) return menuEntries;
    return menuEntries
        ?.filter(e => e?.resourceName == null || resourceNames?.includes(e.resourceName))
        ?.map(e => {
            if (e?.children && e?.children?.length > 0) {
                return {
                    ...e,
                    children: filterEntriesByResources(e?.children, resourceNames)
                }
            }
            return e
        })
        ?.filter(e => e?.children == null || e?.children?.length > 0 )
}

const useBaseAppMenuEntries = (menuEntries?: MenuEntryWithResource[]) => {
    const { isReady: apiIsReady, indexState: apiIndex } = useResourceApiContext();
    return React.useMemo(() => {
        if (apiIsReady) {
            if (!menuEntries) return [];
            const apiLinks = apiIndex?.links.getAll();
            const resourceNames = apiLinks?.map((l: any) => l.rel);

            return filterEntriesByResources(menuEntries, resourceNames)
                .map(e => {
                    const { resourceName, ...otherProps } = e;
                    return otherProps;
                });
        } else {
            return [];
        }
    }, [apiIsReady, apiIndex]);
}

export const useAppEntries = () => {
    const { t } = useTranslation();
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
        icon: 'settings',
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
    const menuConfiguracio = {
        id: 'configuracio',
        title: t($ => $.menu.configuracio),
        icon: 'settings',
        children: [
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
                icon: 'domain',
                resourceName: 'entorn',
            },
            {
                id: 'versionsEntorn',
                title: t($ => $.menu.versionsEntorn),
                to: '/versionsEntorn',
                icon: 'format_list_numbered_rtl',
                resourceName: 'entornApp',
            },
            {
                id: 'alarma',
                title: t($ => $.menu.alarmaConfig),
                to: '/alarma',
                icon: 'notifications',
                resourceName: 'alarmaConfig',
            },
            {
                id: 'integracio',
                title: t($ => $.menu.integracio),
                to: '/integracio',
                icon: 'integration_instructions',
                resourceName: 'integracio',
            },
            {
                id: 'dimensio',
                title: t($ => $.menu.dimensio),
                to: '/dimensio',
                icon: 'category',
                resourceName: 'dimensio',
            },
            {
                id: 'indicador',
                title: t($ => $.menu.indicador),
                to: '/indicador',
                icon: 'insights',
                resourceName: 'indicador',
            },
            {
                id: 'estadisticaWidget',
                title: t($ => $.menu.widget),
                to: '/estadisticaWidget',
                icon: 'widgets',
                resourceName: 'dashboard',
            },
            {
                id: 'dashboard',
                title: t($ => $.menu.dashboard),
                to: '/dashboard',
                icon: 'dashboardCustomize',
                resourceName: 'dashboard',
            },
            {
                id: 'calendari',
                title: t($ => $.menu.calendari),
                to: '/calendari',
                icon: 'calendar_month',
                resourceName: 'fet',
            },
            {
                id: 'parametre',
                title: t($ => $.menu.parametre),
                to: '/parametre',
                icon: 'settings',
                resourceName: 'parametre',
            }
        ]
    };
    const headerMenuEntries = [
        menuSalut,
        menuEstadistiques,
        menuTasca,
        menuAvis,
    ];
    const caibMenuEntries = [
        menuSalut,
        menuEstadistiques,
        menuTasca,
        menuAvis,
        menuMonitoritzacio,
        menuConfiguracio,
    ];

    return {
        headerMenuEntries: useBaseAppMenuEntries(headerMenuEntries),
        caibMenuEntries: useBaseAppMenuEntries(caibMenuEntries),
    }
}

export const App: React.FC = () => {
    const { user } = useUserContext();
    const theme = useTheme();
    const darkThemeActive = theme.palette.mode === "dark";
    const appbarBackgroundColor = darkThemeActive ? undefined : '#fff';

    const {caibMenuEntries, headerMenuEntries} = useAppEntries();
    return (
        <BaseApp
            code="com"
            logo={darkThemeActive ? logoDark : logo}
            logoStyle={{
                '& img': { height: '38px' },
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
            menuEntries={caibMenuEntries}
            headerMenuEntries={headerMenuEntries}
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
