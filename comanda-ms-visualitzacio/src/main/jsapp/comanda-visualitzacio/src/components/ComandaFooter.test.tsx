import { act, render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Footer } from './ComandaFooter';

const mocks = vi.hoisted(() => ({
    toolbarBackgroundStyleMock: vi.fn(() => ({ backgroundImage: 'url(test.png)' })),
    tMock: vi.fn((selector: (input: { menu: { sitemap: string; accessibilitat: string } }) => string) =>
        selector({ menu: { sitemap: 'Mapa del lloc', accessibilitat: 'Accessibilitat' } })
    ),
}));

vi.mock('reactlib', () => ({
    toolbarBackgroundStyle: mocks.toolbarBackgroundStyleMock,
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

describe('Footer', () => {
    beforeEach(() => {
        class ResizeObserverMock {
            observe() {}
            disconnect() {}
        }

        vi.stubGlobal('ResizeObserver', ResizeObserverMock);
        window.__MANIFEST__ = undefined;
        mocks.toolbarBackgroundStyleMock.mockClear();
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('Footer_quanJaHiHaManifest_mostraLaVersioElsLinksIElsLogos', async () => {
        // Comprova que el peu de pàgina renderitza la informació de versió i els elements visibles principals.
        window.__MANIFEST__ = {
            'Build-Timestamp': '2026-03-13T08:00:00',
            'Implementation-Vendor': 'CAIB',
            'Implementation-SCM-Branch': 'main',
            'Implementation-SCM-Revision': 'abc123',
            'Implementation-Version': '1.4.0',
            'Manifest-Version': '1.0',
            'Created-By': 'CI',
            'Build-Jdk-Spec': '17',
        };

        await act(async () => {
            render(
                <MemoryRouter>
                    <Footer
                        title="Comanda"
                        logos={['/logo-caib.png']}
                        backgroundColor="#123456"
                        backgroundImg="/fons.png"
                    />
                </MemoryRouter>
            );
        });

        expect(screen.getByText('Comanda v1.4.0')).toBeInTheDocument();
        expect(screen.getByText('(2026-03-13T08:00:00 | Revisió: abc123)')).toBeInTheDocument();
        expect(screen.getByRole('link', { name: 'Mapa del lloc' })).toHaveAttribute('href', '/sitemap');
        expect(screen.getByRole('link', { name: 'Accessibilitat' })).toHaveAttribute('href', '/accessibilitat');
        expect(screen.getByAltText('foot_logo')).toHaveAttribute('src', '/logo-caib.png');
        expect(mocks.toolbarBackgroundStyleMock).toHaveBeenCalledWith('#123456', '/fons.png');
    });

    it('Footer_quanElManifestArribaMesTard_actualitzaLaInformacioDeVersio', async () => {
        // Verifica que el component refresca el DOM quan el manifest apareix després del muntatge.
        vi.useFakeTimers();

        render(
            <MemoryRouter>
                <Footer title="Comanda" />
            </MemoryRouter>
        );

        window.__MANIFEST__ = {
            'Build-Timestamp': '2026-03-13T09:00:00',
            'Implementation-Vendor': 'CAIB',
            'Implementation-SCM-Branch': 'main',
            'Implementation-SCM-Revision': 'def456',
            'Implementation-Version': '2.0.0',
            'Manifest-Version': '1.0',
            'Created-By': 'CI',
            'Build-Jdk-Spec': '17',
        };

        await act(async () => {
            vi.advanceTimersByTime(100);
        });

        expect(screen.getByText('Comanda v2.0.0')).toBeInTheDocument();
        expect(screen.getByText('(2026-03-13T09:00:00 | Revisió: def456)')).toBeInTheDocument();

        vi.useRealTimers();
    });
});
