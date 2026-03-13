import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import AppRoutes from './AppRoutes';

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

vi.mock('./components/ProtectedRoute', () => ({
    default: ({
        resourceName,
        children,
    }: {
        resourceName: string;
        children: React.ReactNode;
    }) => <div>{`Protected(${resourceName})`}{children}</div>,
}));

describe('AppRoutes', () => {
    it('AppRoutes_quanEsCarregaLaRutaArrel_protegeixLaVistaDeSalut', () => {
        // Comprova que la ruta arrel passa pel wrapper de permisos de salut.
        render(
            <MemoryRouter initialEntries={['/']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('Protected(salut)')).toBeInTheDocument();
        expect(screen.getByText('Salut page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaUnaRutaProtegidaDalarmes_aplicaElResourceCorrecte', () => {
        // Verifica que la ruta d’alarmes fa servir el recurs de protecció específic del mòdul.
        render(
            <MemoryRouter initialEntries={['/alarmes']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('Protected(alarma)')).toBeInTheDocument();
        expect(screen.getByText('Alarmes page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaDeDashboardAmbId_mostraLEdicioCorrespondent', () => {
        // Comprova que la ruta de dashboard amb id resol la pàgina d’edició.
        render(
            <MemoryRouter initialEntries={['/dashboard/12']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('EstadisticaDashboardEdit page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaDeQueueMessages_mostraLaPaginaDeCua', () => {
        // Verifica que la ruta anidada del broker obri la vista de missatges de cua.
        render(
            <MemoryRouter initialEntries={['/broker/queue/queue-a']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('QueueMessages page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLaRutaDeDimensioValorAmbId_mostraLaPaginaCorrespondent', () => {
        // Comprova que la ruta amb id de dimensió-valor resol el component esperat.
        render(
            <MemoryRouter initialEntries={['/dimensio/valor/7']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('DimensioValor page')).toBeInTheDocument();
    });

    it('AppRoutes_quanLaRutaNoExisteix_mostraLaPagina404', () => {
        // Verifica que qualsevol ruta desconeguda cau al component de not found.
        render(
            <MemoryRouter initialEntries={['/ruta/inexistent']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('NotFound page')).toBeInTheDocument();
    });

    it('AppRoutes_quanEsCarregaLEstadisticaViewOpcional_mostraLaVistaCorrespondent', () => {
        // Comprova que la ruta d’estadístiques amb id opcional continua resolent la vista de dashboard.
        render(
            <MemoryRouter initialEntries={['/estadistiques/5']}>
                <AppRoutes />
            </MemoryRouter>
        );

        expect(screen.getByText('EstadisticaDashboardView page')).toBeInTheDocument();
    });
});
