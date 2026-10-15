import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import MonitorDb, { formatBytes, formatNumber, pctColor } from './MonitorDb';

const mockFetch = vi.fn();
global.fetch = mockFetch;

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) => {
            if (typeof selector === 'function') {
                const mockData = {
                    page: {
                        monitorDb: {
                            title: 'Monitor BD',
                            lastUpdate: 'Última actualització',
                            refresh: 'Actualitzar',
                            tabs: { overview: 'Resum', storage: 'Emmagatzematge', activity: 'Activitat', sql: 'Top SQL', locks: 'Bloquejos', indexes: 'Índexos' },
                            overview: { activeSessions: 'Sessions Actives', activeSessionsTooltip: 'T', cacheHitRatio: 'Hit Ratio', cacheHitRatioTooltip: 'T', totalTables: 'Taules', totalTablesTooltip: 'T', totalReserved: 'Espai Reservat', totalReservedTooltip: 'T', activeLocks: 'Bloquejos Actius', activeLocksTooltip: 'T', invalidIndexes: 'Índexos Invàlids', invalidIndexesTooltip: 'T' },
                            health: { title: 'Salut', tablespace: 'Tablespace', usedMb: 'U', totalMb: 'T', maxMb: 'M', freeMb: 'F', pctUsed: '%' },
                            storage: { table: 'T', numRows: 'N', bytesReserved: 'B', estimatedData: 'E', pctUsed: '%', lastAnalyzed: 'L' },
                            activity: { table: 'T', physicalReads: 'P', logicalReads: 'L', cacheHitRatio: 'H', bufferWaits: 'B', rowLockWaits: 'R' },
                            sql: { sqlText: 'S', totalTime: 'T', executions: 'E', msPerExec: 'M', buffersPerExec: 'B' },
                            locks: { sid: 'S', username: 'U', status: 'S', objectName: 'O', objectType: 'O', lockMode: 'L', lockRequest: 'L', blocking: 'B' },
                            indexes: { indexName: 'I', tableName: 'T', status: 'S', uniqueness: 'U', numRows: 'N', lastAnalyzed: 'L', blevel: 'B', leafBlocks: 'L', rebuild: 'Reconstruir', rebuildSuccess: 'Reconstruït {name}', rebuildError: 'Error reconstruint {name}' }
                        },
                        common: { yes: 'Sí', no: 'No', save: 'Guardar' }
                    }
                };
                return selector(mockData);
            }
            return selector;
        }
    }),
}));

vi.mock('reactlib', () => ({
    useAuthContext: () => ({ getToken: () => 'mock-token-123' }),
    useResourceApiContext: () => ({ apiUrl: 'https://api.mock.com' }),
    useMuiDataGridApiRef: () => ({ current: { refresh: vi.fn() } }),
    MuiDataGrid: ({ resourceName, rowAdditionalActions }: any) => (
        <div data-testid={`datagrid-${resourceName}`} data-actions={JSON.stringify(rowAdditionalActions)}>
            Grid Mock: {resourceName}
        </div>
    ),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: any) => <h1 data-testid="page-title">{title}</h1>,
}));

beforeEach(() => {
    vi.clearAllMocks();
    mockFetch.mockResolvedValue({
        ok: true,
        json: () => Promise.resolve({
            sessionsActives: 5,
            sessionsTotal: 10,
            hitRatioCache: 95.5,
            taulesCom: 150,
            totalBytesReservats: 1500000000,
            ultimaActualitzacio: '2023-10-27T10:00:00Z',
            sessionsDisponibles: true,
            activitatDisponible: true,
            sqlDisponible: true,
            tablespacesDisponibles: true,
            bloqueigsDisponibles: true,
            bloqueigsActius: 1,
            indexosInvalidsCount: 0,
        }),
    });
});

afterEach(() => {
    vi.clearAllMocks();
});

describe('Helpers de format', () => {
    it('formatBytes converteix correctament les unitats', () => {
        expect(formatBytes(0)).toBe('0 B');
        expect(formatBytes(500)).toBe('500 B');
        expect(formatBytes(1500)).toBe('1.5 KB');
        expect(formatBytes(1500000)).toBe('1.4 MB');
        expect(formatBytes(1500000000)).toBe('1.40 GB');
    });

    it('formatNumber aplica el format local ca-ES', () => {
        expect(formatNumber(1234567)).toBe('1.234.567');
    });

    it('pctColor retorna el color semàntic correcte', () => {
        expect(pctColor(90)).toBe('error');
        expect(pctColor(85)).toBe('warning');
        expect(pctColor(74)).toBe('success');
    });
});

describe('MonitorDb Component', () => {
    it('renderitza l\'estructura base i el títol de la pàgina', async () => {
        render(<MonitorDb />);
        expect(screen.getByTestId('page-title')).toHaveTextContent('Monitor BD');
        expect(screen.getByText('Resum')).toBeInTheDocument();
        expect(screen.getByText('Emmagatzematge')).toBeInTheDocument();

        await screen.findByText('5 / 10');
    });

    it('crida a l\'API d\'overview amb els headers correctes en muntar', async () => {
        render(<MonitorDb />);

        await waitFor(() => {
            expect(mockFetch).toHaveBeenCalledWith(
                'https://api.mock.com/db-metrics/overview',
                expect.objectContaining({
                    credentials: 'include',
                    headers: { Authorization: 'Bearer mock-token-123' },
                })
            );
        });

        await screen.findByText('5 / 10');
    });

    it('mostra les dades de l\'overview correctament quan l\'API té èxit', async () => {
        render(<MonitorDb />);

        expect(await screen.findByText('5 / 10')).toBeInTheDocument();
        expect(screen.getByText('95.5%')).toBeInTheDocument();
        expect(screen.getByText('1.40 GB')).toBeInTheDocument();
        expect(screen.getByText('150')).toBeInTheDocument();
    });

    it('mostra un Alert d\'error quan la crida a overview falla', async () => {
        mockFetch.mockRejectedValueOnce(new Error('Error de xarxa'));

        render(<MonitorDb />);

        expect(await screen.findByText('Error: Error de xarxa')).toBeInTheDocument();
    });

    it('canvia de pestanya i renderitza el DataGrid corresponent', async () => {
        render(<MonitorDb />);

        await screen.findByText('5 / 10');

        expect(screen.queryByTestId('datagrid-dbTablespace')).toBeInTheDocument();

        fireEvent.click(screen.getByText('Emmagatzematge'));
        expect(screen.getByTestId('datagrid-dbTaulaStorage')).toBeInTheDocument();

        fireEvent.click(screen.getByText('Índexos'));
        expect(screen.getByTestId('datagrid-dbIndex')).toBeInTheDocument();
    });

    it('la pestanya d\'Índexos passa les accions addicionals (REBUILD) correctament al Grid', async () => {
        render(<MonitorDb />);

        await screen.findByText('5 / 10');
        fireEvent.click(screen.getByText('Índexos'));
        const grid = screen.getByTestId('datagrid-dbIndex');
        const actions = JSON.parse(grid.getAttribute('data-actions') || '[]');

        expect(actions).toHaveLength(1);
        expect(actions[0].action).toBe('REBUILD');
        expect(actions[0].title).toBe('Reconstruir');
    });
});
