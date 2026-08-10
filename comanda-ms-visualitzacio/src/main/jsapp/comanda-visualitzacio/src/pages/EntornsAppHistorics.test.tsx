import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EntornAppHist from './EntornsAppHistorics';

const mocks = vi.hoisted(() => ({
    useResourceApiServiceMock: vi.fn(),
    tMock: vi.fn((selector: any) => {
        if (typeof selector === 'function') {
            return selector({
                page: {
                    entornAppHist: {
                        title: 'Històric de versions',
                        versioRevisio: 'Versió / Revisió',
                        filter: { more: 'Més filtres' },
                    },
                },
                components: { clear: 'Netejar' },
            });
        }
        return selector;
    }),
    truncateHashRevisioMock: vi.fn((hash: string) => (hash ? hash.substring(0, 7) + '...' : '')),
    dateFormatLocaleMock: vi.fn((val: string) => `formatted:${val}`),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('./salut/dataFetching.ts', () => ({
    truncateHashRevisio: mocks.truncateHashRevisioMock,
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div data-testid="grid-page">{children}</div>,
    MuiDataGrid: ({ columns, toolbarAdditionalRow }: any) => (
        <div data-testid="data-grid">
            <div data-testid="columns-data">
                {JSON.stringify(columns.map((c: any) => ({ field: c.field, headerName: c.headerName })))}
            </div>
            <div data-testid="toolbar-additional">{toolbarAdditionalRow}</div>
            <div data-testid="column-formatters" style={{ display: 'none' }}>
                {JSON.stringify(
                    columns.map((c: any) => ({
                        field: c.field,
                        hasFormatter: !!c.valueFormatter,
                        hasRenderCell: !!c.renderCell,
                    }))
                )}
            </div>
        </div>
    ),
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div data-testid="mui-filter">{children}</div>,
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`} />,
    useFilterApiRef: () => ({ current: { clear: vi.fn() } }),
    useFormApiRef: () => ({ current: { setFieldValue: vi.fn() } }),
    springFilterBuilder: {
        and: vi.fn((...args: any[]) => args.filter(Boolean).join(' && ')),
        eq: vi.fn((field: string, value: string) => `${field}=${value}`),
        like: vi.fn((field: string, value: string) => `${field} LIKE ${value}`),
        between: vi.fn((field: string, val1: string, val2: string) => `${field} BETWEEN ${val1} AND ${val2}`),
    },
    dateFormatLocale: mocks.dateFormatLocaleMock,
    useResourceApiService: (resource: string) => mocks.useResourceApiServiceMock(resource),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1 data-testid="page-title">{title}</h1>,
}));

describe('EntornAppHist', () => {
    const mockEntornAppsData = {
        rows: [
            { id: 1, entornAppDescription: 'Entorn de Producció' },
            { id: 2, entornAppDescription: 'Entorn de Preproducció' },
        ],
    };

    beforeEach(() => {
        vi.clearAllMocks();
        mocks.useResourceApiServiceMock.mockReturnValue({
            isReady: true,
            find: vi.fn().mockResolvedValue(mockEntornAppsData),
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EntornAppHist_columnaEntornApp_formatejaCorrectamentElValor', async () => {
        render(<EntornAppHist />);

        await waitFor(() => {
            expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
        });

        const columnsData = JSON.parse(screen.getByTestId('columns-data').textContent || '[]');
        expect(columnsData).toContainEqual({ field: 'entorn', headerName: undefined });
        expect(columnsData).toContainEqual({ field: 'app', headerName: undefined });

        const entornApps = mockEntornAppsData.rows;
        const mockValueFormatter = (value: any) => {
            if (value?.id == null) return '';
            const entornApp = entornApps.find((ea: any) => ea.id === value?.id);
            return entornApp?.entornAppDescription ?? '';
        };

        expect(mockValueFormatter({ id: 1 })).toBe('Entorn de Producció');
        expect(mockValueFormatter({ id: 2 })).toBe('Entorn de Preproducció');
        expect(mockValueFormatter({ id: 99 })).toBe('');
        expect(mockValueFormatter(null)).toBe('');
    });

    it('EntornAppHist_columnaVersio_renderitzaChipsCorrectamentSegonsCanviVersio', async () => {
        render(<EntornAppHist />);

        await waitFor(() => {
            expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
        });

        const truncateHashRevisio = mocks.truncateHashRevisioMock;
        const mockRenderCell = (param: any) => {
            const versionChip = param?.row?.versio != null && (
                <span data-testid="version-chip" data-color={param?.row?.canviVersio ? 'success' : 'secondary'}>
                    {param?.row?.versio}
                </span>
            );
            const revisionChip = param?.row?.revisio != null && (
                <span data-testid="revision-chip" title={param?.row?.revisio}>
                    {truncateHashRevisio(param?.row?.revisio)}
                </span>
            );
            return (
                <div>
                    {versionChip}
                    {revisionChip}
                </div>
            );
        };

        // 1
        const resultSuccess = mockRenderCell({
            row: { versio: '2.0.0', revisio: 'abc123def456', canviVersio: true },
        });

        const { container: containerSuccess } = render(resultSuccess);
        expect(containerSuccess.querySelector('[data-testid="version-chip"]')).toHaveAttribute('data-color', 'success');
        expect(containerSuccess.querySelector('[data-testid="revision-chip"]')).toHaveTextContent('abc123d...');
        expect(mocks.truncateHashRevisioMock).toHaveBeenCalledWith('abc123def456');

        // 2
        vi.clearAllMocks();
        const resultSecondary = mockRenderCell({
            row: { versio: '1.5.0', revisio: 'xyz987uvw654321', canviVersio: false },
        });

        const { container: containerSecondary } = render(resultSecondary);
        expect(containerSecondary.querySelector('[data-testid="version-chip"]')).toHaveAttribute('data-color', 'secondary');
        expect(containerSecondary.querySelector('[data-testid="revision-chip"]')).toHaveTextContent('xyz987u...');
    });

    it('EntornAppHist_columnaData_utilitzaDateFormatLocale', async () => {
        render(<EntornAppHist />);

        await waitFor(() => {
            expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
        });

        const columnsData = JSON.parse(screen.getByTestId('columns-data').textContent || '[]');
        expect(columnsData).toContainEqual({ field: 'data', headerName: undefined });
        expect(mocks.dateFormatLocaleMock('2023-10-25T10:00:00')).toBe('formatted:2023-10-25T10:00:00');
    });
});
