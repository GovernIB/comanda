import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import Sitemap from './Sitemap';
import { useAppEntries } from '../App.tsx';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            typeof selector === 'function'
                ? selector({
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
                  })
                : selector,
    }),
    Trans: ({ i18nKey }: { i18nKey: any }) => (
        <>{typeof i18nKey === 'function' ? i18nKey({}) : i18nKey}</>
    ),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../App.tsx', () => ({
    useAppEntries: vi.fn(() => ({
        caibMenuEntries: [
            { id: 'salut', title: 'Salut', to: '/', icon: 'monitor_heart' },
            { id: 'dashboard', title: 'Dashboard', to: '/dashboard', icon: 'dashboard' },
        ],
    })),
}));

describe('Sitemap', () => {
    it('Sitemap_quanEsRenderitza_mostraElTitolISetLinksPrincipals', () => {
        // Comprova que la pàgina del mapa del lloc mostra el títol i les rutes principals de navegació amb entrades mockejades.
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

    it('Sitemap_quanTeEntradesAmbFills_lesMostraCorrectament', () => {
        vi.mocked(useAppEntries).mockReturnValue({
            caibMenuEntries: [
                {
                    id: 'parent',
                    title: 'Parent',
                    to: '/parent',
                    icon: 'folder',
                    children: [
                        { id: 'child', title: 'Child', to: '/child', icon: 'file' }
                    ]
                }
            ]
        });

        render(
            <MemoryRouter>
                <Sitemap />
            </MemoryRouter>
        );

        expect(screen.getByRole('link', { name: 'Parent' })).toHaveAttribute('href', '/parent');
        expect(screen.getByRole('link', { name: 'Child' })).toHaveAttribute('href', '/child');
    });
});
