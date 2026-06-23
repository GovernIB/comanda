import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import VersionsEntorns from './VersionsEntorns';

const mocks = vi.hoisted(() => ({
    entornFindMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                versionsEntorns: {
                    title: 'Versions per entorn',
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

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiDataGrid: ({
        title,
        columns,
    }: {
        title: string;
        columns: Array<Record<string, any>>;
    }) => {
        const sampleRowLatest = {
            entornApps: [
                {
                    entorn: { id: 1 },
                    versio: '1.2.0',
                    revisio: 'abc123def456789',
                },
                {
                    entorn: { id: 2 },
                    versio: '1.10.0',
                    revisio: 'xyz987uvw654321',
                },
            ],
        };
        const sampleRowSuccess = {
            entornApps: [
                {
                    entorn: { id: 1 },
                    versio: '1.10.0',
                    revisio: 'abc123def456789',
                },
                {
                    entorn: { id: 2 },
                    versio: '1.2.0',
                    revisio: 'xyz987uvw654321',
                },
            ],
        };
        return (
            <section>
                <h2>{title}</h2>
                <span data-testid="column-count">{columns.length}</span>
                <span data-testid="column-header">{columns[1]?.headerName}</span>
                <span data-testid="value-getter">
                    {columns[1]?.valueGetter?.(undefined, sampleRowLatest) ?? 'sense-valor'}
                </span>
                <div data-testid="rendered-chip-warning">
                    {columns[1]?.renderCell?.({
                        formattedValue: '1.2.0',
                        row: sampleRowLatest,
                    })}
                </div>
                <div data-testid="rendered-chip-success">
                    {columns[1]?.renderCell?.({
                        formattedValue: '1.10.0',
                        row: sampleRowSuccess,
                    })}
                </div>
                <div data-testid="rendered-chip">
                    {columns[1]?.renderCell?.({
                        formattedValue: '1.2.0',
                        row: sampleRowLatest,
                    })}
                </div>
            </section>
        );
    },
    useResourceApiService: () => ({
        isReady: true,
        find: mocks.entornFindMock,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

describe('VersionsEntorns', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('VersionsEntorns_quanCarregaElsEntorns_construeixLesColumnesIComparaVersions', async () => {
        // Comprova que la pàgina crea una columna per entorn i marca com a warning les versions no capdavanteres.
        mocks.entornFindMock.mockResolvedValue({
            rows: [
                { id: 1, codi: 'PRO', nom: 'Producció' },
                { id: 2, codi: 'PRE', nom: 'Preproducció' },
            ],
        });

        render(<VersionsEntorns />);

        await waitFor(() => {
            expect(screen.getByTestId('column-count')).toHaveTextContent('3');
        });

        expect(screen.getByRole('heading', { level: 1, name: 'Versions per entorn' })).toBeInTheDocument();
        expect(screen.getByTestId('column-header')).toHaveTextContent('PRO (Producció)');
        expect(screen.getByTestId('value-getter')).toHaveTextContent('1.2.0');
        expect(screen.getByTestId('rendered-chip')).toHaveTextContent('1.2.0');
        expect(mocks.entornFindMock).toHaveBeenCalledWith({ unpaged: true });
    });

    it('VersionsEntorns_quanLaVersioNoEsLaUltima_mostraWarning', async () => {
        mocks.entornFindMock.mockResolvedValue({
            rows: [
                { id: 1, codi: 'PRO', nom: 'Producció' },
                { id: 2, codi: 'PRE', nom: 'Preproducció' },
            ],
        });

        render(<VersionsEntorns />);

        await waitFor(() => {
            expect(screen.getByTestId('rendered-chip-warning')).toBeInTheDocument();
        });

        const warningChip = screen.getByTestId('rendered-chip-warning').querySelector('.MuiChip-colorWarning');
        expect(warningChip).toBeInTheDocument();
    });

    it('VersionsEntorns_quanLaVersioEsLaUltima_mostraSuccess', async () => {
        mocks.entornFindMock.mockResolvedValue({
            rows: [
                { id: 1, codi: 'PRO', nom: 'Producció' },
                { id: 2, codi: 'PRE', nom: 'Preproducció' },
            ],
        });

        render(<VersionsEntorns />);

        await waitFor(() => {
            expect(screen.getByTestId('rendered-chip-success')).toBeInTheDocument();
        });

        const successChip = screen.getByTestId('rendered-chip-success').querySelector('.MuiChip-colorSuccess');
        expect(successChip).toBeInTheDocument();
    });

    it('VersionsEntorns_quanHiHaRevisio_mostraElHashTruncat', async () => {
        mocks.entornFindMock.mockResolvedValue({
            rows: [
                { id: 1, codi: 'PRO', nom: 'Producció' },
            ],
        });

        render(<VersionsEntorns />);

        await waitFor(() => {
            expect(screen.getByTestId('rendered-chip')).toBeInTheDocument();
        });

        const renderedContent = screen.getByTestId('rendered-chip').textContent;
        expect(renderedContent).toContain('abc123d');
    });

    it('VersionsEntorns_quanNoHiHaEntorns_noCreaColumnesDinamiques', async () => {
        mocks.entornFindMock.mockResolvedValue({
            rows: [],
        });

        render(<VersionsEntorns />);

        await waitFor(() => {
            expect(screen.getByTestId('column-count')).toHaveTextContent('1');
        });
    });

    it('VersionsEntorns_comparaVersionsSemanticamentCorrectament', async () => {
        mocks.entornFindMock.mockResolvedValue({
            rows: [
                { id: 1, codi: 'PRO', nom: 'Producció' },
                { id: 2, codi: 'PRE', nom: 'Preproducció' },
            ],
        });

        render(<VersionsEntorns />);

        await waitFor(() => {
            expect(screen.getByTestId('rendered-chip-success')).toBeInTheDocument();
        });

        const successChip = screen.getByTestId('rendered-chip-success').querySelector('.MuiChip-colorSuccess');
        expect(successChip).toBeInTheDocument();
    });
});