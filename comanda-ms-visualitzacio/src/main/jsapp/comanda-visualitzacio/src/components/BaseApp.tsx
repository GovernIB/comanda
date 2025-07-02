import React, {useState, useEffect} from 'react';
import {AccessTime, CalendarMonth} from '@mui/icons-material';
import dayjs from 'dayjs';
import {
    useNavigate,
    useLocation,
    Link as RouterLink,
    LinkProps as RouterLinkProps,
} from 'react-router-dom';
import i18n from '../i18n/i18n';
import { useTranslation } from 'react-i18next';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import 'dayjs/locale/ca';
import 'dayjs/locale/es';
import HeaderLanguageSelector from "./HeaderLanguageSelector";
import Button from '@mui/material/Button';
import AppMenu from "./AppMenu";
import drassana from '../assets/drassana.png';

import {
    MuiBaseApp,
    MenuEntry,
    useBaseAppContext,
} from 'reactlib';
import Footer from "./Footer.tsx";

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
    appMenuEntries?: MenuEntryWithResource[];
    appbarBackgroundColor?: string;
    appbarBackgroundImg?: string;
    appbarStyle?: any;
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

// Hora del sistema
// Component separat per al rellotge del sistema per evitar re-renders innecesaris
const SystemTimeDisplay = React.memo(() => {
    const [currentTime, setCurrentTime] = useState(dayjs());

    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentTime(dayjs());
        }, 1000);
        return () => clearInterval(timer);
    }, []);

    return (
        <div
            style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                marginLeft: '20px',
                color: 'inherit',
                fontSize: '11px'
            }}
        >
            <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                <CalendarMonth sx={{ fontSize: '14px' }}/>
                <span>{currentTime.format('DD/MM/YYYY')}</span>
            </div>
            <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                <AccessTime  sx={{ fontSize: '14px' }}/>
                <span>{currentTime.format('HH:mm:ss')}</span>
            </div>
        </div>
    );
});

const generateSystemTimeItems = () => {
    return [
        <SystemTimeDisplay key="system_time" />
    ];
}

// Entrades independents del menú (sempre visibles si hi ha baseAppMenuEntries)
const generateMenuItems = (appMenuEntries: MenuEntry[] | undefined) => {
    return appMenuEntries?.length
        ? appMenuEntries.map((entry) => (
            <Button
                className="appMenuItem"
                key={entry.id}
                color="inherit"
                component={Link}
                to={entry.to} // Navegació amb React Router
            >
                {entry.title}
            </Button>
        ))
        : [];
}
// Selector d'idioma (només si hi ha idiomes disponibles)
const generateLanguageItems = (availableLanguages: string[] | undefined) => {
    return availableLanguages?.length
        ? [
            <HeaderLanguageSelector
                sx={{ml: '42px'}}
                key="sel_lang"
                languages={availableLanguages}
            />,
        ]
        : [];
}
// Menú general
const generateAppMenu = (menuEntries: MenuEntry[] | undefined) => {
    return menuEntries?.length
        ? [<AppMenu key="app_menu" menuEntries={menuEntries} />]
        : [];
}

const generateFooter = () => {
    return (
        <>
            <div style={{ height: '36px', width: '100%' }} />
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

export const BaseApp: React.FC<BaseAppProps> = (props) => {
    const {
        code,
        logo,
        logoStyle,
        title,
        version,
        availableLanguages,
        menuEntries,
        appMenuEntries,
        appbarBackgroundColor,
        appbarBackgroundImg,
        appbarStyle,
        children
    } = props;
    const navigate = useNavigate();
    const location = useLocation();
    const i18nHandleLanguageChange = (language?: string) => {
        i18n.changeLanguage(language);
    }
    const i18nAddResourceBundleCallback = (language: string, namespace: string, bundle: any) => {
        i18n.addResourceBundle(language, namespace, bundle);
    }
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
            ...generateMenuItems(appMenuEntries), // Menú
            ...generateSystemTimeItems(), // Hora del sistema
            ...generateLanguageItems(availableLanguages), // Idioma
            ...generateAppMenu(menuEntries) // Menú lateral
        ]}
        footer={generateFooter()}
        persistentSession
        persistentLanguage
        i18nUseTranslation={useTranslation}
        i18nCurrentLanguage={i18n.language}
        i18nHandleLanguageChange={i18nHandleLanguageChange}
        i18nAddResourceBundleCallback={i18nAddResourceBundleCallback}
        routerGoBack={goBack}
        routerNavigate={navigate}
        routerUseLocationPath={useLocationPath}
        routerAnyHistoryEntryExist={anyHistoryEntryExist}
        linkComponent={Link}
    >
        <CustomLocalizationProvider>
            {children}
        </CustomLocalizationProvider>
    </MuiBaseApp>;
}

export default BaseApp;
