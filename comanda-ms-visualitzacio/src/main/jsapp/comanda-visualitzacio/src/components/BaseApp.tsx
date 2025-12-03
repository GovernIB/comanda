import React from 'react';
import {
    Link as RouterLink,
    LinkProps as RouterLinkProps,
    useLocation,
    useNavigate
} from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Button from '@mui/material/Button';
import useMediaQuery from '@mui/material/useMediaQuery';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import {
    DefaultMuiComponentProps,
    MenuEntry,
    MuiBaseApp,
    useBaseAppContext,
    useResourceApiContext,
} from 'reactlib';
import { DataFormDialogApi } from '../../lib/components/mui/datacommon/DataFormDialog';
import Alarms from './Alarms';
import AppMenu from './AppMenu';
import Footer from './Footer';
import SystemTimeDisplay from './SystemTimeDisplay';
import { useUserContext } from './UserContext';
import HeaderLanguageSelector from './HeaderLanguageSelector';
import { UserProfileFormDialog, UserProfileFormDialogButton } from './UserProfileFormDialog';
import RoleSelector from './RoleSelector';
import i18n from '../i18n/i18n';
import drassana from '../assets/drassana.png';
import 'dayjs/locale/ca';
import 'dayjs/locale/es';
import { useTheme } from '@mui/material/styles';

export type MenuEntryWithResource = MenuEntry & {
    resourceName?: string;
}

export type HeaderBackgroundModuleItem = {
    color?: string;
    image?: string;
}

export type BaseAppProps = React.PropsWithChildren & {
    code: string;
    logo?: string;
    logoStyle?: any;
    title: string | React.ReactElement;
    version: string;
    availableLanguages?: string[];
    menuEntries?: MenuEntryWithResource[];
    headerMenuEntries?: MenuEntryWithResource[];
    caibMenuEntries?: MenuEntryWithResource[];
    appbarBackgroundColor?: string;
    appbarBackgroundImg?: string;
    appbarStyle?: any;
    defaultMuiComponentProps?: DefaultMuiComponentProps;
};

const Link = React.forwardRef<HTMLAnchorElement, RouterLinkProps>((itemProps, ref) => {
    return <RouterLink ref={ref} {...itemProps} role={undefined} />;
});

const useLocationPath = () => {
    const location = useLocation();
    return location.pathname;
}

const CustomLocalizationProvider = ({ children }: React.PropsWithChildren) => {
    const { currentLanguage } = useBaseAppContext();
    const adapterLocale = React.useMemo(() => {
        const languageTwoChars = currentLanguage?.substring(0, 2).toLowerCase();
        switch (languageTwoChars) {
            case 'ca':
            case 'es':
            case 'en':
                return languageTwoChars;
            default:
                return 'ca';
        }
    }, [currentLanguage]);
    const adapter = AdapterDayjs;
    return <LocalizationProvider dateAdapter={adapter} adapterLocale={adapterLocale}>
        {children}
    </LocalizationProvider>;
}

// Entrades independents del menú (sempre visibles si hi ha baseAppMenuEntries)
const generateMenuItems = (appMenuEntries: MenuEntryWithResource[] | undefined) => {
    const theme = useTheme();
    const { indexState: apiIndex } = useResourceApiContext();
    const filteredAppMenuEntries = appMenuEntries?.filter(e => e.resourceName == null || apiIndex?.links.has(e.resourceName));
    return filteredAppMenuEntries?.length
        ? filteredAppMenuEntries.map((entry) => (
            <Button
                className="appMenuItem"
                key={entry.id}
                component={Link}
                to={entry.to ?? ''} // Navegació amb React Router
                sx={{
                    color: theme.palette.text.primary,
                    display: { xs: 'none', md: 'inline' },
                    mr: 1,
                    textTransform: 'none',
                    '&:hover': {
                        textDecoration: 'underline',
                        '--variant-containedBg': '#fff',
                        '--variant-textBg': '#fff',
                        '--variant-outlinedBg': '#fff',
                    }
                }}>
                {entry.title}
            </Button>
        ))
        : [];
}
// Selector d'idioma (només si hi ha idiomes disponibles)
const generateLanguageItems = (availableLanguages: string[] | undefined) => {
    const theme = useTheme();
    const isLgUp = useMediaQuery(theme.breakpoints.up("lg"));
    return availableLanguages?.length && isLgUp && false
        ? [
            <HeaderLanguageSelector
                sx={{ ml: { xs: 1, md: '42px' } }}
                key="sel_lang"
                languages={availableLanguages}
            />,
        ]
        : [];
}
// Menú general
const generateAppMenu = (menuEntries: MenuEntryWithResource[] | undefined) => {
    const { indexState: apiIndex } = useResourceApiContext();
    const filteredMenuEntries = menuEntries?.filter(e => e.resourceName == null || apiIndex?.links.has(e.resourceName));
    return filteredMenuEntries?.length
        ? [<AppMenu key="app_menu" menuEntries={filteredMenuEntries} />]
        : [];
}

const useBaseAppMenuEntries = (menuEntries?: MenuEntryWithResource[]) => {
    const [processedMenuEntries, setprocessedMenuEntries] = React.useState<MenuEntry[]>();
    const { isReady: apiIsReady, indexState: apiIndex } = useResourceApiContext();
    React.useEffect(() => {
        if (apiIsReady) {
            const apiLinks = apiIndex?.links.getAll();
            const resourceNames = apiLinks?.map((l: any) => l.rel);
            const processedMenuEntries = menuEntries?.
                filter(e => e?.resourceName == null || resourceNames?.includes(e.resourceName)).
                map(e => {
                    const { resourceName, ...otherProps } = e;
                    return otherProps;
                });
            setprocessedMenuEntries(processedMenuEntries);
        }
    }, [apiIsReady, apiIndex]);
    return processedMenuEntries;
}

const generateFooter = () => {
    return (
        <>
            <div style={{ height: '36px', flexShrink: 0, width: '100%' }} />
            <Footer
                title="COMANDA"
                backgroundColor="#5F5D5D"
                logos={[drassana]}
                style={{ position: 'fixed', height: '36px', bottom: 0, width: '100%' }}
                // style={{display: 'flex', flexDirection: 'row', flexShrink: 0, position: 'sticky', height: '36px', bottom: 0, width: '100%'}}
            />
        </>
    );
};

const useI18nUseTranslation = (ns?: string) => {
    // @ts-expect-error baseReact defineix el namespace com a string, però comanda només suporta actualment 'translations'
    return useTranslation(ns);
}

const useI18n = () => {
    const { user } = useUserContext();
    const currentUserLanguage = user?.idioma.toLowerCase();
    const i18nHandleLanguageChange = (language?: string) => {
        i18n.changeLanguage(language);
    }
    const i18nAddResourceBundleCallback = (language: string, namespace: string, bundle: any) => {
        i18n.addResourceBundle(language, namespace, bundle);
    }
    return {
        i18nUseTranslation: useI18nUseTranslation,
        i18nCurrentLanguage: currentUserLanguage ?? i18n.language,
        i18nHandleLanguageChange,
        i18nAddResourceBundleCallback,
    }
}

export const BaseApp: React.FC<BaseAppProps> = (props) => {
    const {
        code,
        logo,
        logoStyle,
        title,
        version,
        availableLanguages,
        menuEntries,
        headerMenuEntries,
        caibMenuEntries,
        appbarBackgroundColor,
        appbarBackgroundImg,
        appbarStyle,
        defaultMuiComponentProps,
        children
    } = props;
    const navigate = useNavigate();
    const location = useLocation();
    const baseAppMenuEntries = useBaseAppMenuEntries(menuEntries);
    const { user, currentRole } = useUserContext();
    const userDialogApiRef = React.useRef<DataFormDialogApi | undefined>(undefined);
    const { indexState } = useResourceApiContext();
    const {
        i18nUseTranslation,
        i18nCurrentLanguage,
        i18nHandleLanguageChange,
        i18nAddResourceBundleCallback,
    } = useI18n();
    const anyHistoryEntryExist = () => location.key !== 'default';
    const goBack = (fallback?: string) => {
        if (anyHistoryEntryExist()) {
            navigate(-1);
        } else if (fallback != null) {
            navigate(fallback);
        } else {
            console.warn('[BACK] No s\'ha pogut tornar enrere, ni s\'ha especificat una ruta alternativa ni existeix una entrada prèvia a l\'historial de navegació');
        }
    }
    const showAlarms = indexState?.links?.has('alarma');
    return <MuiBaseApp
        code={code}
        headerTitle={title}
        headerVersion={version}
        headerLogo={logo}
        headerLogoStyle={logoStyle}
        headerAppbarBackgroundColor={appbarBackgroundColor}
        headerAppbarBackgroundImg={appbarBackgroundImg}
        headerAppbarStyle={appbarStyle}
        headerAdditionalComponents={[
            ...generateMenuItems(headerMenuEntries), // Menú
            <SystemTimeDisplay key="system_time" />, // Hora del sistema
            ...(showAlarms ? [<Alarms key="alarms" />] : []), // Alarmes actives
            ...generateLanguageItems(availableLanguages), // Idioma
            ...generateAppMenu(caibMenuEntries), // Menú lateral
        ]}
        headerAdditionalAuthComponents={[
            <UserProfileFormDialogButton onClick={() => userDialogApiRef.current?.show(
                user?.id,
            )} />,
            <RoleSelector />
        ]}
        headerAuthBadgeIcon={currentRole === 'COM_ADMIN' ? 'settings' : undefined}
        footer={generateFooter()}
        persistentSession
        persistentLanguage
        i18nUseTranslation={i18nUseTranslation}
        i18nCurrentLanguage={i18nCurrentLanguage}
        i18nHandleLanguageChange={i18nHandleLanguageChange}
        i18nAddResourceBundleCallback={i18nAddResourceBundleCallback}
        routerGoBack={goBack}
        routerNavigate={navigate}
        routerUseLocationPath={useLocationPath}
        routerAnyHistoryEntryExist={anyHistoryEntryExist}
        linkComponent={Link}
        menuEntries={baseAppMenuEntries}
        defaultMuiComponentProps={defaultMuiComponentProps}
        fixedContentExpandsToAvailableHeightEnabled
        marginsDisabled>
        <UserProfileFormDialog dialogApiRef={userDialogApiRef} />
        <CustomLocalizationProvider>
            {children}
        </CustomLocalizationProvider>
    </MuiBaseApp>;
}

export default BaseApp;
