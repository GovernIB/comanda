import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import QueueMessages from './QueueMessages';

const mocks = vi.hoisted(() => ({
    requestHrefMock: vi.fn(),
    navigateMock: vi.fn(),
    showDialogMock: vi.fn(),
    getTokenMock: vi.fn(),
    tMock: vi.fn((selector: any, params?: Record<string, string>) =>
        selector({
            common: {
                loading: 'Carregant',
                delete: 'Eliminar',
                cancel: 'Cancel·lar',
                confirm: 'Confirmar',
                yes: 'Sí',
                no: 'No',
                none: 'Cap',
            },
            page: {
                broker: {
                    title: 'Broker',
                },
                queue: {
                    title: `Cua ${params?.name ?? ''}`.trim(),
                    error: {
                        fetchFailed: 'No s ha pogut carregar la cua',
                        purgeFailed: 'No s ha pogut buidar la cua',
                    },
                    actions: {
                        purge: 'Buidar cua',
                    },
                    purge: {
                        title: 'Confirmació de buidatge',
                        confirmation: `Vols buidar ${params?.name ?? ''}?`.trim(),
                    },
                    detail: {
                        title: 'Detall de la cua',
                        name: 'Nom',
                        address: 'Adreça',
                        routingType: 'Tipus',
                        durable: 'Durable',
                        messageCount: 'Missatges',
                        consumerCount: 'Consumidors',
                        deliveringCount: 'Enviant',
                        messagesAdded: 'Afegits',
                        messagesAcknowledged: 'Confirmats',
                        filter: 'Filtre',
                        temporary: 'Temporal',
                        autoCreated: 'Autocreada',
                        purgeOnNoConsumers: 'Buida sense consumidors',
                        maxConsumers: 'Màxim de consumidors',
                    },
                },
                message: {
                    title: 'Missatges',
                    detail: {
                        title: 'Detall del missatge',
                        messageID: 'ID',
                        queueName: 'Cua',
                        timestamp: 'Data',
                        type: 'Tipus',
                        durable: 'Durable',
                        priority: 'Prioritat',
                        size: 'Mida',
                        redelivered: 'Reenviat',
                        deliveryCount: 'Comptador d entrega',
                        expirationTime: 'Caducitat',
                        properties: 'Propietats',
                        content: 'Contingut',
                    },
                    error: {
                        deleteFailed: 'No s ha pogut eliminar el missatge',
                    },
                    grid: {
                        messageID: 'ID',
                        timestamp: 'Data',
                        type: 'Tipus',
                        priority: 'Prioritat',
                        size: 'Mida',
                        actions: 'Accions',
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
    useParams: () => ({ queueName: 'queue-a' }),
    useNavigate: () => mocks.navigateMock,
}));

vi.mock('../hooks/useDataGridLocale', () => ({
    default: () => ({ noRowsLabel: 'Sense files' }),
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    useCloseDialogButtons: () => <button>Tancar</button>,
    useMuiContentDialog: () => [mocks.showDialogMock, <div key="dialog" data-testid="dialog-placeholder" />],
    useAuthContext: () => ({
        getToken: mocks.getTokenMock,
    }),
}));

vi.mock('../components/ContentDetail', () => ({
    ContentDetail: ({ title, elements }: { title: string; elements: Array<{ label: string; value: unknown }> }) => (
        <section>
            <h3>{title}</h3>
            {elements.map((element) => (
                <div key={element.label}>
                    <span>{element.label}</span>
                    <span>{String(element.value ?? '')}</span>
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

vi.mock('@mui/x-data-grid-pro', () => ({
    DataGridPro: ({
        rows,
        onRowClick,
        columns,
    }: {
        rows: Array<Record<string, unknown>>;
        onRowClick?: (params: { row: Record<string, unknown> }) => void;
        columns?: Array<{
            field?: string;
            renderCell?: (params: { row: Record<string, unknown>; value: unknown }) => React.ReactNode;
        }>;
    }) => (
        <div>
            <div data-testid="grid-columns">{columns?.length ?? 0}</div>
            {rows.map((row) => (
                <div key={String(row.messageID)}>
                    <button onClick={() => onRowClick?.({ row })}>{String(row.messageID)}</button>
                    <span>{String(row.type)}</span>
                    {columns
                        ?.filter((column) => typeof column.renderCell === 'function')
                        .map((column, index) => (
                            <div key={`${String(row.messageID)}-${index}`}>
                                {column.renderCell?.({
                                    row,
                                    value: row[column.field as keyof typeof row],
                                })}
                            </div>
                        ))}
                </div>
            ))}
        </div>
    ),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('QueueMessages', () => {
    beforeEach(() => {
        mocks.getTokenMock.mockReturnValue('token-123');
        mocks.requestHrefMock.mockImplementation((path: string) => {
            if (path === 'broker/queues/queue-a') {
                return Promise.resolve({
                    data: {
                        name: 'queue-a',
                        address: 'addr-a',
                        routingType: 'ANYCAST',
                        durable: true,
                        messageCount: 2,
                        consumerCount: 1,
                        deliveringCount: 0,
                        messagesAdded: 10,
                        messagesAcknowledged: 8,
                        filter: '',
                        temporary: false,
                        autoCreated: false,
                        purgeOnNoConsumers: false,
                        maxConsumers: 5,
                    },
                });
            }

            return Promise.resolve({
                data: [
                    {
                        messageID: 'm-1',
                        queueName: 'queue-a',
                        timestamp: '2026-03-13T10:00:00Z',
                        type: 'TEXT',
                        durable: true,
                        priority: 4,
                        size: 128,
                        properties: { origin: 'test' },
                        content: 'missatge 1',
                        redelivered: false,
                        deliveryCount: 1,
                        expirationTime: '',
                    },
                ],
            });
        });
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue({
                ok: true,
            })
        );
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('QueueMessages_quanLaCarregaEsCorrecta_mostraElDetallIlaGraella', async () => {
        // Comprova que la pàgina mostra el detall de la cua i els missatges recuperats del backend.
        render(<QueueMessages />);

        await waitFor(() => {
            expect(screen.getByTestId('page-title')).toHaveTextContent('Cua queue-a');
        });

        expect(screen.getByText('Detall de la cua')).toBeInTheDocument();
        expect(screen.getByText('addr-a')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Buidar cua' })).toBeEnabled();
        expect(screen.getByRole('button', { name: 'm-1' })).toBeInTheDocument();
        expect(screen.getByTestId('grid-columns')).toHaveTextContent('6');
    });

    it('QueueMessages_quanEsFaClickAUnMissatge_obreElDialegDeDetall', async () => {
        // Verifica que clicar una fila reutilitza el diàleg de contingut per mostrar el detall del missatge.
        render(<QueueMessages />);

        const rowButton = await screen.findByRole('button', { name: 'm-1' });
        fireEvent.click(rowButton);

        expect(mocks.showDialogMock).toHaveBeenCalledWith(
            'Detall del missatge',
            expect.anything(),
            expect.anything(),
            expect.objectContaining({ maxWidth: 'lg', fullWidth: true })
        );
    });

    it('QueueMessages_quanFallaLaCarrega_mostraElMissatgeDError', async () => {
        // Comprova que la pàgina informa de l'error quan el backend no pot retornar la informació de la cua.
        mocks.requestHrefMock.mockRejectedValueOnce(new Error('boom'));
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

        render(<QueueMessages />);

        await waitFor(() => {
            expect(screen.getByText('No s ha pogut carregar la cua')).toBeInTheDocument();
        });

        consoleErrorSpy.mockRestore();
    });

    it('QueueMessages_quanSEliminaUnMissatge_faLaPeticioAmbElToken', async () => {
        // Verifica que l'acció d'eliminar envia la capçalera d'autorització i refresca les dades.
        render(<QueueMessages />);

        await screen.findByRole('button', { name: 'm-1' });
        fireEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith('/api/broker/queues/queue-a/messages/m-1', {
                method: 'DELETE',
                headers: {
                    Authorization: 'Bearer token-123',
                },
            });
        });

        expect(mocks.requestHrefMock).toHaveBeenCalledTimes(4);
    });

    it('QueueMessages_quanEsPurgaLaCua_tancaElDialegINavegaDesDelBreadcrumbQuanCal', async () => {
        // Comprova que el diàleg de purga executa la petició corresponent i que el breadcrumb permet tornar enrere.
        render(<QueueMessages />);

        await screen.findByRole('button', { name: 'm-1' });
        fireEvent.click(screen.getByRole('button', { name: 'Buidar cua' }));
        fireEvent.click(screen.getByRole('button', { name: 'Confirmar' }));

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith('/api/broker/queues/queue-a/messages', {
                method: 'DELETE',
                headers: {
                    Authorization: 'Bearer token-123',
                },
            });
        });

        fireEvent.click(screen.getByText('Broker'));
        expect(mocks.navigateMock).toHaveBeenCalledWith('/broker');
    });

    it('QueueMessages_quanFallaLaPurga_mostraLError', async () => {
        // Comprova que els errors en buidar la cua es reflecteixen amb el missatge d'error específic.
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue({
                ok: false,
                status: 500,
            })
        );
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

        render(<QueueMessages />);

        await screen.findByRole('button', { name: 'm-1' });
        fireEvent.click(screen.getByRole('button', { name: 'Buidar cua' }));
        fireEvent.click(screen.getByRole('button', { name: 'Confirmar' }));

        await waitFor(() => {
            expect(screen.getByText('No s ha pogut buidar la cua')).toBeInTheDocument();
        });

        consoleErrorSpy.mockRestore();
    });

    it('QueueMessages_quanNoHiHaMissatges_deshabilitaLaPurga', async () => {
        // Verifica que el botó de buidar cua queda deshabilitat quan la cua està buida.
        mocks.requestHrefMock.mockImplementation((path: string) => {
            if (path === 'broker/queues/queue-a') {
                return Promise.resolve({
                    data: {
                        name: 'queue-a',
                        address: 'addr-a',
                        routingType: 'ANYCAST',
                        durable: true,
                        messageCount: 0,
                        consumerCount: 0,
                        deliveringCount: 0,
                        messagesAdded: 0,
                        messagesAcknowledged: 0,
                        filter: '',
                        temporary: false,
                        autoCreated: false,
                        purgeOnNoConsumers: false,
                        maxConsumers: 5,
                    },
                });
            }
            return Promise.resolve({ data: [] });
        });

        render(<QueueMessages />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Buidar cua' })).toBeDisabled();
        });
    });

    it('QueueMessages_quanSEliminaSenseToken_noEnviaCapCapcaleraDAutoritzacio', async () => {
        // Comprova que la petició d’eliminació funciona també quan no hi ha token d’autenticació disponible.
        mocks.getTokenMock.mockReturnValue(undefined);

        render(<QueueMessages />);

        await screen.findByRole('button', { name: 'm-1' });
        fireEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        await waitFor(() => {
            expect(fetch).toHaveBeenCalledWith('/api/broker/queues/queue-a/messages/m-1', {
                method: 'DELETE',
                headers: undefined,
            });
        });
    });

    it('QueueMessages_quanLaPeticioDEliminarFalla_mostraLErrorEspecific', async () => {
        // Verifica que un error en eliminar un missatge es reflecteix amb el text funcional correcte.
        vi.stubGlobal(
            'fetch',
            vi.fn().mockResolvedValue({
                ok: false,
                status: 500,
            })
        );
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);

        render(<QueueMessages />);

        await screen.findByRole('button', { name: 'm-1' });
        fireEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        await waitFor(() => {
            expect(screen.getByText('No s ha pogut eliminar el missatge')).toBeInTheDocument();
        });

        consoleErrorSpy.mockRestore();
    });
});
