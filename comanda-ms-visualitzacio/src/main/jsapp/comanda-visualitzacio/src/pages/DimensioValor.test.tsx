import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import DimensioValor from './DimensioValor';

const mocks = vi.hoisted(() => ({
    anyHistoryEntryExistMock: vi.fn(),
    goBackMock: vi.fn(),
    getOneMock: vi.fn(),
    useReadOnlyGestorMock: vi.fn(() => false),
    tMock: vi.fn((selector: any) =>
        selector({
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

vi.mock('react-router-dom', () => ({
    useParams: () => ({
        id: '15',
    }),
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiDataGrid: ({
        title,
        staticFilter,
        toolbarElementsWithPositions,
        toolbarAdditionalRow,
        columns,
        rowHideDeleteButton,
    }: {
        title: string;
        staticFilter?: string;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        toolbarAdditionalRow?: React.ReactNode;
        columns: Array<{ field: string }>;
        rowHideDeleteButton?: boolean;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="static-filter">{staticFilter}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            <div data-testid="hide-delete">{String(rowHideDeleteButton)}</div>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            <div>{toolbarAdditionalRow}</div>
        </section>
    ),
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    springFilterBuilder: {
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        like: (field: string, value: unknown) => `${field}~${String(value)}`,
        and: (...parts: Array<string | undefined | false>) => parts.filter(Boolean).join(' && '),
    },
    useFilterApiRef: () => ({
        current: {
            clear: vi.fn(),
        },
    }),
    useFormApiRef: () => ({ current: {} }),
    useResourceApiService: () => ({
        isReady: true,
        getOne: mocks.getOneMock,
    }),
    useBaseAppContext: () => ({
        goBack: mocks.goBackMock,
        anyHistoryEntryExist: mocks.anyHistoryEntryExistMock,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
    default: () => mocks.useReadOnlyGestorMock(),
}));

describe('DimensioValor', () => {
    beforeEach(() => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova' });
        mocks.anyHistoryEntryExistMock.mockReturnValue(true);
    });

    afterEach(() => {
        vi.clearAllMocks();
        mocks.useReadOnlyGestorMock.mockReturnValue(false);
    });

    it('DimensioValor_quanEsCarregaLaDimensio_mostraElTitolIElFiltreStatic', async () => {
        // Comprova que la pàgina carrega el nom de la dimensió i construeix el filtre estàtic amb l'id rebut.
        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.getByTestId('page-title')).toHaveTextContent('Valors dimensió Dimensió prova');
        expect(screen.getByTestId('static-filter')).toHaveTextContent('dimensio.id=15');
        expect(screen.getByTestId('columns')).toHaveTextContent('valor');
        expect(screen.getByTestId('hide-delete')).toHaveTextContent('false');
        expect(mocks.getOneMock).toHaveBeenCalledWith('15');
        expect(screen.getAllByRole('button')[0]).toBeEnabled();
    });

    it('DimensioValor_quanNoHiHaHistorial_deshabilitaElBotoDeTornada', async () => {
        // Verifica que el botó de tornar enrere queda deshabilitat quan no hi ha historial de navegació.
        mocks.anyHistoryEntryExistMock.mockReturnValue(false);

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getAllByRole('button')[0]).toBeDisabled();
        });
    });

    it('DimensioValor_quanGestorEsReadOnly_ocultaLaccioDesborrar', async () => {
        mocks.useReadOnlyGestorMock.mockReturnValue(true);

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.getByTestId('hide-delete')).toHaveTextContent('true');
    });
});
