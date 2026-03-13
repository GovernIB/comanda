import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Dimensions from './Dimensions';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    findMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                dimensions: {
                    title: 'Dimensions',
                    values: 'Valors',
                    column: {
                        entornApp: 'Entorn app',
                    },
                },
            },
            components: {
                clear: 'Netejar',
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
        filter,
        toolbarAdditionalRow,
        rowAdditionalActions,
        columns,
    }: {
        title: string;
        filter?: string;
        toolbarAdditionalRow?: React.ReactNode;
        rowAdditionalActions?: Array<{ label: string; linkTo?: string }>;
        columns: Array<{ field: string }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="filter-value">{filter}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            <div data-testid="row-link">{rowAdditionalActions?.[0]?.linkTo}</div>
            <div>{toolbarAdditionalRow}</div>
        </section>
    ),
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name, label, optionsRequest }: { name: string; label?: string; optionsRequest?: (q: string) => Promise<{ options: Array<{ description?: string }> }> }) => (
        <div>
            <span data-testid={`field-${name}`}>{label ?? name}</span>
            {optionsRequest ? <button onClick={async () => {
                const result = await optionsRequest('entorn');
                const descriptions = result.options.map((option) => option.description).join(',');
                document.body.setAttribute('data-dimension-options', descriptions);
            }}>Carrega opcions dimensions</button> : null}
        </div>
    ),
    springFilterBuilder: {
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        like: (field: string, value: unknown) => `${field}~${String(value)}`,
        and: (...parts: Array<string | undefined | false>) => parts.filter(Boolean).join(' && '),
    },
    useFilterApiRef: () => ({
        current: {
            clear: mocks.clearMock,
        },
    }),
    useFormApiRef: () => ({ current: {} }),
    useResourceApiService: () => ({
        isReady: true,
        find: mocks.findMock,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Dimensions', () => {
    beforeEach(() => {
        vi.spyOn(console, 'log').mockImplementation(() => undefined);
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 9, entornAppDescription: 'Dimensió entorn' }],
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
        document.body.removeAttribute('data-dimension-options');
    });

    it('Dimensions_quanEsRenderitza_mostraElGridElFiltreIElLinkAlsValors', async () => {
        // Comprova que la pàgina de dimensions mostra les columnes principals i l'acció per veure els valors.
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('page-title')).toHaveTextContent('Dimensions');
        expect(screen.getByRole('heading', { name: 'Dimensions' })).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('entornAppId=0');
        expect(screen.getByTestId('columns')).toHaveTextContent('codi,nom,descripcio');
        expect(screen.getByTestId('row-link')).toHaveTextContent('valor/{{id}}');
    });

    it('Dimensions_quanEsCarreguenLesOpcionsDelFiltre_utilitzaElsEntornsRecuperats', async () => {
        // Verifica que el filtre de dimensions ofereix els entorns carregats del backend.
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Carrega opcions dimensions' }));

        await waitFor(() => {
            expect(document.body.getAttribute('data-dimension-options')).toContain('Dimensió entorn');
        });
    });

    it('Dimensions_quanEsPremNetejar_esborraElFiltreActiu', async () => {
        // Comprova que el botó de neteja del filtre crida el `clear` del filtre persistent.
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByTitle('Netejar'));

        expect(mocks.clearMock).toHaveBeenCalled();
    });
});
