import React from 'react';
import { render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';

const mocks = vi.hoisted(() => ({
    baseAppPropsMock: vi.fn(),
    useUserContextMock: vi.fn(),
    useIsUserAdminMock: vi.fn(),
    useIsUserConsultaMock: vi.fn(),
    useThemeMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                salut: 'Salut',
                estadistiques: 'Estadístiques',
                tasca: 'Tasca',
                avis: 'Avisos',
                monitoritzacio: 'Monitorització',
                monitor: 'Monitor',
                cache: 'Cache',
                broker: 'Broker',
                configuracio: 'Configuració',
                app: 'Apps',
                entorn: 'Entorns',
                versionsEntorn: 'Versions',
                alarmaConfig: 'Alarmes',
                integracio: 'Integracions',
                dimensio: 'Dimensions',
                indicador: 'Indicadors',
                widget: 'Widgets',
                dashboard: 'Dashboards',
                calendari: 'Calendari',
                parametre: 'Paràmetres',
            },
        })
    ),
}));

vi.mock('./components/BaseApp', () => ({
    BaseApp: ({ children, ...props }: { children: React.ReactNode }) => {
        mocks.baseAppPropsMock(props);
        return <div data-testid="base-app">{children}</div>;
    },
}));

vi.mock('./AppRoutes', () => ({
    default: () => <div>AppRoutes mock</div>,
}));

vi.mock('./components/KeepAlive', () => ({
    default: () => <div>KeepAlive mock</div>,
}));

vi.mock('./components/UserContext', () => ({
    useUserContext: () => mocks.useUserContextMock(),
    useIsUserAdmin: () => mocks.useIsUserAdminMock(),
    useIsUserConsulta: () => mocks.useIsUserConsultaMock(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('@mui/material/styles', () => ({
    useTheme: () => mocks.useThemeMock(),
}));

vi.mock('./assets/goib_logo.svg', () => ({
    default: 'logo-clar',
}));

vi.mock('./assets/goib_logo.png', () => ({
    default: 'logo-fosc',
}));

vi.mock('./assets/COM_DRA_COL.svg?react', () => ({
    default: ({ title }: { title?: string }) => <svg data-testid="comanda-logo" aria-label={title} />,
}));

describe('App', () => {
    beforeEach(() => {
        mocks.useUserContextMock.mockReturnValue({
            user: {
                numElementsPagina: '20',
            },
        });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useThemeMock.mockReturnValue({
            palette: {
                mode: 'light',
                divider: '#ccc',
                getContrastText: vi.fn(() => '#111'),
            },
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('App_quanLusuariEsAdmin_configuraElMenuILaPaginacioPerDefecte', () => {
        // Comprova que l’aplicació passa a BaseApp el menú complet i la paginació derivada de la configuració d’usuari.
        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(screen.getByTestId('base-app')).toBeInTheDocument();
        expect(screen.getByText('KeepAlive mock')).toBeInTheDocument();
        expect(screen.getByText('AppRoutes mock')).toBeInTheDocument();
        expect(props.logo).toBe('logo-clar');
        expect(props.appbarBackgroundColor).toBe('#fff');
        expect(props.availableLanguages).toEqual(['ca', 'es']);
        expect(props.headerMenuEntries).toHaveLength(4);
        expect(props.menuEntries).toHaveLength(6);
        expect(props.defaultMuiComponentProps.dataGrid.paginationModel).toEqual({
            page: 0,
            pageSize: 20,
        });
        expect(props.menuEntries[5].children.some((entry: { id: string }) => entry.id === 'estadisticaWidget')).toBe(true);
        expect(props.menuEntries[5].children.some((entry: { id: string }) => entry.id === 'parametre')).toBe(true);
    });

    it('App_quanLusuariNoTePermisosDeMenu_noPassaLesEntradesLaterals', () => {
        // Verifica que per un usuari sense perfil admin ni consulta no s’envien les entrades del menú lateral.
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.menuEntries).toBeUndefined();
        expect(props.headerMenuEntries).toHaveLength(4);
    });

    it('App_quanElTemaEsFosc_usaElLogoFoscISenseColorDeFonsFix', () => {
        // Comprova que en tema fosc es fa servir el logo alternatiu i no es força el color blanc a l’appbar.
        mocks.useThemeMock.mockReturnValue({
            palette: {
                mode: 'dark',
                divider: '#222',
                getContrastText: vi.fn(() => '#fff'),
            },
        });
        mocks.useUserContextMock.mockReturnValue({
            user: {
                numElementsPagina: 'AUTOMATIC',
            },
        });

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.logo).toBe('logo-fosc');
        expect(props.appbarBackgroundColor).toBeUndefined();
        expect(props.defaultMuiComponentProps.dataGrid.paginationModel).toBeUndefined();
    });

    it('App_quanLusuariEsConsulta_habilitaElMenuSenseOpcionsNomesAdmin', () => {
        // Verifica que el perfil consulta veu el menú lateral però sense les entrades reservades a administració.
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(true);

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];
        const configuracioChildren = props.menuEntries[5].children;

        expect(props.menuEntries).toHaveLength(6);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'estadisticaWidget')).toBe(false);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'parametre')).toBe(false);
    });
});
