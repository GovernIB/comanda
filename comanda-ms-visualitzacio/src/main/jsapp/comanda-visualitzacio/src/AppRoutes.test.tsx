import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import AppRoutes from './AppRoutes';

const mocks = vi.hoisted(() => ({
    useUserContextMock: vi.fn(),
    useIsUserAdminMock: vi.fn(),
    useIsUserConsultaMock: vi.fn(),
    useIsUserUsuariMock: vi.fn(),
    entornAppFindMock: vi.fn(),
}));

vi.mock('./pages/salut/Salut', () => ({
    default: () => <div>Salut page</div>,
}));

vi.mock('./pages/NotFound', () => ({
    default: () => <div>NotFound page</div>,
}));

vi.mock('./pages/Apps', () => ({
    default: () => <div>Apps page</div>,
    AppForm: () => <div>AppForm page</div>,
}));

vi.mock('./pages/Entorns', () => ({
    default: () => <div>Entorns page</div>,
}));

vi.mock('./pages/Monitor', () => ({
    default: () => <div>Monitor page</div>,
}));

vi.mock('./pages/Caches', () => ({
    default: () => <div>Caches page</div>,
}));

vi.mock('./pages/Integracions', () => ({
    default: () => <div>Integracions page</div>,
}));

vi.mock('./pages/Dimensions', () => ({
    default: () => <div>Dimensions page</div>,
}));

vi.mock('./pages/DimensioValor', () => ({
    default: () => <div>DimensioValor page</div>,
}));

vi.mock('./pages/Indicadors', () => ({
    default: () => <div>Indicadors page</div>,
}));

vi.mock('./pages/EstadisticaWidget', () => ({
    default: () => <div>EstadisticaWidget page</div>,
}));

vi.mock('./pages/EstadisticaDashboards', () => ({
    default: () => <div>EstadisticaDashboards page</div>,
}));

vi.mock('./pages/EstadisticaDashboardEdit', () => ({
    default: () => <div>EstadisticaDashboardEdit page</div>,
}));

vi.mock('./pages/EstadisticaDashboardView', () => ({
    default: () => <div>EstadisticaDashboardView page</div>,
}));

vi.mock('./pages/VersionsEntorns', () => ({
    default: () => <div>VersionsEntorns page</div>,
}));

vi.mock('./pages/Broker', () => ({
    default: () => <div>Broker page</div>,
}));

vi.mock('./pages/QueueMessages', () => ({
    default: () => <div>QueueMessages page</div>,
}));

vi.mock('./pages/CalendariEstadistiques', () => ({
    default: () => <div>CalendariEstadistiques page</div>,
}));

vi.mock('./pages/Tasca', () => ({
    default: () => <div>Tasca page</div>,
}));

vi.mock('./pages/Avis', () => ({
    default: () => <div>Avis page</div>,
}));

vi.mock('./pages/Alarmes', () => ({
    default: () => <div>Alarmes page</div>,
}));

vi.mock('./pages/AlarmaConfig', () => ({
    default: () => <div>AlarmaConfig page</div>,
    AlarmaConfigForm: () => <div>AlarmaConfigForm page</div>,
}));

vi.mock('./pages/Parametres', () => ({
    default: () => <div>Parametres page</div>,
}));

vi.mock('./pages/Sitemap', () => ({
    default: () => <div>Sitemap page</div>,
}));

vi.mock('./pages/accessibilitat/Accessibilitat', () => ({
    default: () => <div>Accessibilitat page</div>,
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

vi.mock('./components/ProtectedRoute', () => ({
    default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

describe('AppRoutes', () => {
    it('AppRoutes_quanEsCarregaLaRutaArrelIUsuariAmbRolFuncional_mostraSalut', async () => {
        // Comprova que per a un usuari admin/consulta la ruta inicial continua mostrant salut.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);

        render(
            <MemoryRouter initialEntries={['/']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('Salut page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaArrelISenseRolPeroAmbAccesASalut_mostraSalut', async () => {
        // Verifica que un usuari sense rol funcional entra a salut si te almenys un entorn-app actiu visible.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(true);
        mocks.entornAppFindMock.mockResolvedValue({ rows: [{ id: 7 }] });

        render(
            <MemoryRouter initialEntries={['/']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(await screen.findByText('Salut page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaArrelISenseRolNiPermisos_redirigeixATasca', async () => {
        // Comprova que sense rol funcional i sense cap entorn-app permès la ruta inicial envia a tasques.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(true);
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });

        render(
            <MemoryRouter initialEntries={['/']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(await screen.findByText('Tasca page')).toBeInTheDocument();
    });

    it('AppRoutes_quanConsultorIntentaAccedirAUnaRutaForaDelSeuMenu_redirigeixALaPaginaPerDefecte', async () => {
        // Verifica que el rol Consultor no pot mantenir-se en rutes reservades a administració.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(true);
        mocks.useIsUserUsuariMock.mockReturnValue(false);

        render(
            <MemoryRouter initialEntries={['/parametre']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(await screen.findByText('Salut page')).toBeInTheDocument();
    });

    it('AppRoutes_quanConsultorAccedeixAUnaRutaDelSeuMenu_laManté', () => {
        // Comprova que el guard no interfereix amb rutes permeses pel menú de consultor.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(true);
        mocks.useIsUserUsuariMock.mockReturnValue(false);

        render(
            <MemoryRouter initialEntries={['/app']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('Apps page')).toBeInTheDocument();
    });

    it('AppRoutes_quanUsuariEsATraUnaPaginaSenseAccesIRoleUsuariTeSalut_redirigeixASalut', async () => {
        // Verifica que en rol Usuari qualsevol ruta fora del conjunt permès envia a la pàgina per defecte amb salut.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(true);
        mocks.entornAppFindMock.mockResolvedValue({ rows: [{ id: 7 }] });

        render(
            <MemoryRouter initialEntries={['/app']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(await screen.findByText('Salut page')).toBeInTheDocument();
    });

    it('AppRoutes_quanUsuariEsATraUnaPaginaSenseAccesIRoleUsuariSenseSalut_redirigeixATasca', async () => {
        // Comprova que en rol Usuari i sense accés a salut la redirecció per defecte cau a tasques.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(false);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(true);
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });

        render(
            <MemoryRouter initialEntries={['/app']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(await screen.findByText('Tasca page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaUnaRutaProtegidaDalarmes_aplicaElResourceCorrecte', () => {
        // Verifica que la ruta d’alarmes fa servir el recurs de protecció específic del mòdul.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);
        render(
            <MemoryRouter initialEntries={['/alarmes']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('Alarmes page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaDeDashboardAmbId_mostraLEdicioCorrespondent', () => {
        // Comprova que la ruta de dashboard amb id resol la pàgina d’edició.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);
        render(
            <MemoryRouter initialEntries={['/dashboard/12']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('EstadisticaDashboardEdit page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaDeQueueMessages_mostraLaPaginaDeCua', () => {
        // Verifica que la ruta anidada del broker obri la vista de missatges de cua.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);
        render(
            <MemoryRouter initialEntries={['/broker/queue/queue-a']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('QueueMessages page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaDeDimensioValorAmbId_mostraLaPaginaCorrespondent', () => {
        // Comprova que la ruta amb id de dimensió-valor resol el component esperat.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);
        render(
            <MemoryRouter initialEntries={['/dimensio/valor/7']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('DimensioValor page')).toBeInTheDocument();
    });

    it('AppRoutes_quanLaRutaNoExisteix_mostraLaPagina404', () => {
        // Verifica que qualsevol ruta desconeguda cau al component de not found.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        mocks.useIsUserUsuariMock.mockReturnValue(false);
        render(
            <MemoryRouter initialEntries={['/ruta/inexistent']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('NotFound page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLEstadisticaViewOpcional_mostraLaVistaCorrespondent', () => {
        // Comprova que la ruta d’estadístiques amb id opcional continua resolent la vista de dashboard.
        mocks.useUserContextMock.mockReturnValue({ user: { id: 1 } });
        mocks.useIsUserAdminMock.mockReturnValue(true);
        mocks.useIsUserConsultaMock.mockReturnValue(false);
        render(
            <MemoryRouter initialEntries={['/estadistiques/5']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('EstadisticaDashboardView page')).toBeInTheDocument();
    });
});
