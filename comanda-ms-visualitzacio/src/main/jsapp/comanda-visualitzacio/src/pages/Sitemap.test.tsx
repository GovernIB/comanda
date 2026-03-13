import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import Sitemap from './Sitemap';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                menu: {
                    salut: 'Salut',
                    dashboard: 'Dashboard',
                    estadistiques: 'Estadístiques',
                    app: 'Aplicacions',
                    entorn: 'Entorns',
                    versionsEntorn: 'Versions entorn',
                    monitor: 'Monitor',
                    cache: 'Cachés',
                    integracio: 'Integracions',
                    dimensio: 'Dimensions',
                    indicador: 'Indicadors',
                    widget: 'Widgets',
                    calendari: 'Calendari',
                    tasca: 'Tasca',
                    avis: 'Avisos',
                    broker: 'Broker',
                    parametre: 'Paràmetres',
                    accessibilitat: 'Accessibilitat',
                },
                page: {
                    sitemap: {
                        title: 'Mapa del lloc',
                        subtitle: 'Accedeix a totes les seccions principals',
                    },
                },
            }),
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Sitemap', () => {
    it('Sitemap_quanEsRenderitza_mostraElTitolISetLinksPrincipals', () => {
        // Comprova que la pàgina del mapa del lloc mostra el títol i les rutes principals de navegació.
        render(
            <MemoryRouter>
                <Sitemap />
            </MemoryRouter>
        );

        expect(screen.getByTestId('page-title')).toHaveTextContent('Mapa del lloc');
        expect(screen.getByText('Accedeix a totes les seccions principals')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Salut' })).toHaveAttribute('href', '/');
        expect(screen.getByRole('link', { name: 'Dashboard' })).toHaveAttribute('href', '/dashboard');
        expect(screen.getByRole('link', { name: 'Accessibilitat' })).toHaveAttribute(
            'href',
            '/accessibilitat'
        );
    });
});
