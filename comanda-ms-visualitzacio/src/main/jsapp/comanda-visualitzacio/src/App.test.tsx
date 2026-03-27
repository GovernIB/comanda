import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from './App';

const mocks = vi.hoisted(() => ({
    baseAppPropsMock: vi.fn(),
    useUserContextMock: vi.fn(),
    useIsUserAdminMock: vi.fn(),
    useIsUserConsultaMock: vi.fn(),
    useIsUserUsuariMock: vi.fn(),
    useThemeMock: vi.fn(),
    entornAppFindMock: vi.fn(),
    useStatsEnabledMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                salut: 'Salut',
                estadistiques: 'Estadístiques',
                tasca: 'Tasca',
                avis: 'Avisos',
                monitoritzacio: 'Monitorització',
                monitoritzacioDescription: 'Descripció monitorització',
                monitor: 'Monitor',
                cache: 'Cache',
                broker: 'Broker',
                configuracio: 'Configuració',
                configuracioDescription: 'Descripció configuració',
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

vi.mock('./hooks/useStatsEnabled', () => ({
    default: () => mocks.useStatsEnabledMock(),
}));

vi.mock('./components/UserContext', () => ({
    useUserContext: () => mocks.useUserContextMock(),
    useIsUserAdmin: () => mocks.useIsUserAdminMock(),
    useIsUserConsulta: () => mocks.useIsUserConsultaMock(),
    useIsUserUsuari: () => mocks.useIsUserUsuariMock(),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'entornApp') {
            return {
                isReady: true,
                find: mocks.entornAppFindMock,
            };
        }
        return {
            isReady: true,
        };
    },
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
                estilMenu: 'TEMA',
            },
        });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);
        mocks.useThemeMock.mockReturnValue({
            palette: {
                mode: 'light',
                divider: '#ccc',
                getContrastText: vi.fn(() => '#111'),
            },
        });
        mocks.entornAppFindMock.mockResolvedValue({ rows: [{ id: 1 }] });
        mocks.useStatsEnabledMock.mockReturnValue(true);
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
        expect(props.headerMenuEntries).toBeUndefined();
        expect(props.menuEntries).toHaveLength(6);
        expect(props.menuAppearance).toBe('theme');
        expect(props.defaultMuiComponentProps.dataGrid.paginationModel).toEqual({
            page: 0,
            pageSize: 20,
        });
        expect(props.menuEntries[5].children.some((entry: { id: string }) => entry.id === 'estadisticaWidget')).toBe(true);
        expect(props.menuEntries[5].children.some((entry: { id: string }) => entry.id === 'parametre')).toBe(true);
        expect(props.menuEntries[4].description).toBe('Descripció monitorització');
        expect(props.menuEntries[5].description).toBe('Descripció configuració');
    });

    it('App_quanLusuariNoTeRolsFuncionals_mostraElMenuLateralLimitat', async () => {
        // Verifica que un usuari sense rol funcional veu només salut si hi té accés, tasques, avisos i alarmes.
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(true);

        render(<App />);

        await waitFor(() => {
            const props = mocks.baseAppPropsMock.mock.calls[mocks.baseAppPropsMock.mock.calls.length - 1]?.[0];
            expect(props.menuEntries.map((entry: { id: string }) => entry.id)).toEqual(['salut', 'tasca', 'avis', 'alarma']);
        });
    });

    it('App_quanLusuariEsConsulta_noMostraElMenuSuperior', () => {
        // Verifica que un usuari amb el rol consulta no veu el menú superior.
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(true);
        mocks.useIsUserUsuariMock.mockReturnValue(false);

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.headerMenuEntries).toBeUndefined();
        expect(props.menuEntries).toHaveLength(6);
    });

    it('App_quanLusuariNoEstaLlest_noMostraElMenuSuperior', () => {
        // Verifica que si l'usuari no està llest no es mostra cap menú funcional encara.
        mocks.useUserContextMock.mockReturnValue({
            user: null,
        });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.headerMenuEntries).toBeUndefined();
        expect(props.menuEntries).toBeUndefined();
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
                estilMenu: 'TEMA',
            },
        });

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.logo).toBe('logo-fosc');
        expect(props.appbarBackgroundColor).toBeUndefined();
        expect(props.menuAppearance).toBe('theme');
        expect(props.defaultMuiComponentProps.dataGrid.paginationModel).toBeUndefined();
    });

    it('App_quanLusuariEsConsulta_habilitaElMenuSenseOpcionsNomesAdmin', () => {
        // Verifica que el perfil consulta veu el menú lateral però sense les entrades reservades a administració.
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(true);
        mocks.useIsUserUsuariMock.mockReturnValue(false);

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];
        const configuracioChildren = props.menuEntries[5].children;

        expect(props.menuEntries).toHaveLength(6);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'estadisticaWidget')).toBe(false);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'parametre')).toBe(false);
    });

    it('App_quanLusuariNoTeAccesASalut_amagaLentradaSalutDelMenuLimitat', async () => {
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(true);
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });

        render(<App />);

        await waitFor(() => {
            const props = mocks.baseAppPropsMock.mock.calls[mocks.baseAppPropsMock.mock.calls.length - 1]?.[0];
            expect(props.menuEntries.map((entry: { id: string }) => entry.id)).toEqual(['tasca', 'avis', 'alarma']);
        });
    });

    it('App_quanLesEstadistiquesNoEstanActives_amagaElsMenusRelacionats', () => {
        mocks.useStatsEnabledMock.mockReturnValue(false);

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];
        const configuracioChildren = props.menuEntries[4].children;

        expect(props.menuEntries.map((entry: { id: string }) => entry.id)).toEqual([
            'salut',
            'tasca',
            'avis',
            'monitoritzacio',
            'configuracio',
        ]);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'dimensio')).toBe(false);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'indicador')).toBe(false);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'estadisticaWidget')).toBe(false);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'dashboard')).toBe(false);
        expect(configuracioChildren.some((entry: { id: string }) => entry.id === 'calendari')).toBe(false);
    });

    it('App_quanLusuariDemanaMenuInvertit_passaLaparençaInvertida', () => {
        mocks.useUserContextMock.mockReturnValue({
            user: {
                numElementsPagina: '20',
                estilMenu: 'TEMA_INVERTIT',
            },
        });

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.menuAppearance).toBe('inverse');
    });

    it('App_quanLusuariDemanaMenuDelPeu_passaLaparençaDelPeu', () => {
        mocks.useUserContextMock.mockReturnValue({
            user: {
                numElementsPagina: '20',
                estilMenu: 'PEU',
            },
        });

        render(<App />);

        const props = mocks.baseAppPropsMock.mock.calls[0]?.[0];

        expect(props.menuAppearance).toBe('footer');
    });
});
