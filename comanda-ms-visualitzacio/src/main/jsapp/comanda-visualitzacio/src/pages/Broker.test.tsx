import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Broker from './Broker';

const mocks = vi.hoisted(() => ({
    requestHrefMock: vi.fn(),
    navigateMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            common: {
                loading: 'Carregant',
                yes: 'Sí',
                no: 'No',
            },
            page: {
                broker: {
                    title: 'Broker',
                    queues: {
                        title: 'Cues',
                    },
                    queue: {
                        address: 'Adreça',
                        routingType: 'Tipus',
                        durable: 'Durable',
                        messageCount: 'Missatges',
                        consumerCount: 'Consumidors',
                        viewMessages: 'Veure missatges',
                    },
                    detail: {
                        title: 'Detall broker',
                        version: 'Versió',
                        name: 'Nom',
                        status: 'Estat',
                        uptime: 'Temps actiu',
                        memoryUsage: 'Memòria',
                        diskUsage: 'Disc',
                        totalConnections: 'Connexions',
                        totalQueues: 'Cues totals',
                        totalMessages: 'Missatges totals',
                    },
                    error: {
                        fetchFailed: 'No s ha pogut carregar el broker',
                    },
                },
            },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('react-router-dom', () => ({
    useNavigate: () => mocks.navigateMock,
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock('../components/ContentDetail', () => ({
    ContentDetail: ({ title, elements }: { title: string; elements: Array<{ label: string; value?: unknown; contentValue?: React.ReactNode }> }) => (
        <section>
            <h3>{title}</h3>
            {elements.map((element) => (
                <div key={element.label}>
                    <span>{element.label}</span>
                    <span>{element.contentValue ?? String(element.value ?? '')}</span>
                </div>
            ))}
        </section>
    ),
}));

vi.mock('../../lib/components/ResourceApiContext', () => ({
    useResourceApiContext: () => ({
        requestHref: mocks.requestHrefMock,
    }),
}));

vi.mock('../util/requestUtils.ts', () => ({
    buildHref: (path: string) => path,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Broker', () => {
    beforeEach(() => {
        mocks.requestHrefMock.mockImplementation((path: string) => {
            if (path === 'broker') {
                return Promise.resolve({
                    data: {
                        version: '1.0.0',
                        name: 'Broker principal',
                        status: 'STARTED',
                        uptime: '1 day',
                        memoryUsage: 1024,
                        diskUsage: 2048,
                        totalConnections: 2,
                        totalQueues: 3,
                        totalMessages: 4,
                    },
                });
            }

            return Promise.resolve({
                data: [
                    {
                        name: 'queue-a',
                        address: 'addr-a',
                        routingType: 'ANYCAST',
                        durable: true,
                        messageCount: 7,
                        consumerCount: 2,
                    },
                ],
            });
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Broker_quanLaCarregaEsCorrecta_mostraElDetallIElLlistatDeCues', async () => {
        // Comprova que la pàgina mostra el resum del broker i les cues disponibles quan la càrrega és correcta.
        render(<Broker />);

        await waitFor(() => {
            expect(screen.getByTestId('page-title')).toHaveTextContent('Broker');
        });

        expect(screen.getByText('Detall broker')).toBeInTheDocument();
        expect(screen.getByText('Broker principal')).toBeInTheDocument();
        expect(screen.getByText('queue-a')).toBeInTheDocument();
        expect(screen.getByText('Cues')).toBeInTheDocument();
        expect(screen.getByText('STARTED')).toBeInTheDocument();
    });

    it('Broker_quanEsPremVeureMissatges_navegaALaRutaDeLaCua', async () => {
        // Verifica que el botó de cada targeta navega al detall de missatges de la cua corresponent.
        render(<Broker />);

        const button = await screen.findByRole('button', { name: 'Veure missatges' });
        fireEvent.click(button);

        expect(mocks.navigateMock).toHaveBeenCalledWith('/broker/queue/queue-a');
    });

    it('Broker_quanLaCarregaFalla_mostraElMissatgeDError', async () => {
        // Comprova que es mostra l'estat d'error quan no es pot recuperar la informació del broker.
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
        mocks.requestHrefMock.mockRejectedValueOnce(new Error('boom'));

        render(<Broker />);

        await waitFor(() => {
            expect(screen.getByText('No s ha pogut carregar el broker')).toBeInTheDocument();
        });

        consoleErrorSpy.mockRestore();
    });
});
