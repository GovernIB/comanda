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
        const sampleRow = {
            entornApps: [
                {
                    entorn: { id: 1 },
                    versio: '1.2.0',
                },
                {
                    entorn: { id: 2 },
                    versio: '1.10.0',
                },
            ],
        };
        return (
            <section>
                <h2>{title}</h2>
                <span data-testid="column-count">{columns.length}</span>
                <span data-testid="column-header">{columns[1]?.headerName}</span>
                <span data-testid="value-getter">
                    {columns[1]?.valueGetter?.(undefined, sampleRow) ?? 'sense-valor'}
                </span>
                <div data-testid="rendered-chip">
                    {columns[1]?.renderCell?.({
                        formattedValue: '1.2.0',
                        row: sampleRow,
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
});
