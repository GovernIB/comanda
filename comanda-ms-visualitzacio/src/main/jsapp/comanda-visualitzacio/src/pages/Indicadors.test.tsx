import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Indicadors from './Indicadors';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                indicadors: {
                    title: 'Indicadors',
                    column: {
                        entornApp: 'Entorn app',
                        indicadorMitjana: 'Indicador mitjana',
                    },
                },
            },
            components: {
                clear: 'Netejar',
            },
        })
    ),
    findMock: vi.fn(),
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
        popupEditFormContent,
        columns,
    }: {
        title: string;
        filter?: string;
        toolbarAdditionalRow?: React.ReactNode;
        popupEditFormContent?: React.ReactNode;
        columns: Array<{ field: string }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="filter-value">{filter}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            <div>{toolbarAdditionalRow}</div>
            <div>{popupEditFormContent}</div>
        </section>
    ),
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name, label, optionsRequest }: { name: string; label?: string; optionsRequest?: (q: string) => Promise<{ options: Array<{ description?: string }> }> }) => (
        <div>
            <span data-testid={`field-${name}`}>{label ?? name}</span>
            {optionsRequest ? <button onClick={async () => {
                const result = await optionsRequest('prova');
                const descriptions = result.options.map((option) => option.description).join(',');
                document.body.setAttribute('data-options', descriptions);
            }}>Carrega opcions</button> : null}
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
    useFormContext: () => ({
        data: {
            compactable: true,
            tipusCompactacio: 'MITJANA',
        },
    }),
}));

vi.mock('../components/sharedAdvancedSearch/advancedSearchColumns', () => ({
    columnesIndicador: [{ field: 'codi' }],
}));

vi.mock('../components/FormFieldCustomAdvancedSearch', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`advanced-${name}`}>{name}</div>,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Indicadors', () => {
    beforeEach(() => {
        vi.spyOn(console, 'log').mockImplementation(() => undefined);
        mocks.findMock.mockResolvedValue({
            rows: [{ id: 7, entornAppDescription: 'Entorn prova' }],
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
        document.body.removeAttribute('data-options');
    });

    it('Indicadors_quanEsRenderitza_mostraLaGraellaElFiltreIElFormulariCondicional', async () => {
        // Comprova que la pàgina mostra les columnes, el filtre inicial i el camp avançat quan la compactació és per mitjana.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('page-title')).toHaveTextContent('Indicadors');
        expect(screen.getByRole('heading', { name: 'Indicadors' })).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('entornAppId=0');
        expect(screen.getByTestId('columns')).toHaveTextContent(
            'codi,nom,descripcio,format,compactable,tipusCompactacio,indicadorComptadorPerMitjana.description'
        );
        expect(screen.getByTestId('advanced-indicadorComptadorPerMitjana')).toBeInTheDocument();
    });

    it('Indicadors_quanEsCarreguenOpcionsDelFiltre_retornaElsEntornsDisponibles', async () => {
        // Verifica que el camp de filtre d'entorn app reusa les dades carregades per oferir opcions filtrables.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Carrega opcions' }));

        await waitFor(() => {
            expect(document.body.getAttribute('data-options')).toContain('Entorn prova');
        });
    });

    it('Indicadors_quanEsPremNetejar_executaLaNetejaDelFiltre', async () => {
        // Comprova que el botó de neteja delega correctament al `filterApiRef`.
        render(<Indicadors />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByTitle('Netejar'));

        expect(mocks.clearMock).toHaveBeenCalled();
    });
});
