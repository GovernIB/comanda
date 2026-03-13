import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Caches from './Caches';

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                caches: {
                    title: 'Cachés',
                    buidar: {
                        titol: 'Buidar caché',
                        confirm: 'Vols buidar aquesta caché?',
                        success: 'Caché buidada',
                        error: 'No s ha pogut buidar la caché',
                        label: 'Buidar',
                        totes: {
                            titol: 'Buidar totes',
                            confirm: 'Vols buidar totes les cachés?',
                            success: 'Totes les cachés buidades',
                            error: 'No s han pogut buidar totes les cachés',
                        },
                    },
                },
            },
        })
    ),
    messageDialogShowMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    apiDeleteMock: vi.fn(),
    refreshMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    useResourceApiService: () => ({
        isReady: true,
        delete: mocks.apiDeleteMock,
    }),
    useBaseAppContext: () => ({
        messageDialogShow: mocks.messageDialogShowMock,
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshMock,
        },
    }),
    MuiDataGrid: ({
        title,
        rowAdditionalActions,
        toolbarElementsWithPositions,
    }: {
        title: string;
        rowAdditionalActions?: Array<{ label: string; onClick: (id: unknown) => void }>;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <button onClick={() => rowAdditionalActions?.[0]?.onClick('cache-1')}>
                {rowAdditionalActions?.[0]?.label}
            </button>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
        </section>
    ),
}));

vi.mock('../../lib/components/ResourceApiProvider.tsx', () => ({}));
vi.mock('../../lib/util/reactNodePosition.ts', () => ({}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('Caches', () => {
    beforeEach(() => {
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.apiDeleteMock.mockResolvedValue(undefined);
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Caches_quanEsRenderitza_mostraLaGraellaIAccioGlobalDeBuidat', () => {
        // Comprova que la pàgina presenta la capçalera i l'acció global per buidar totes les cachés.
        render(<Caches />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Cachés');
        expect(screen.getByRole('heading', { name: 'Cachés' })).toBeInTheDocument();
        expect(screen.getByTitle('Buidar totes')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Buidar' })).toBeInTheDocument();
    });

    it('Caches_quanEsBuidaUnaCache_faLaPeticioIndividualIRefrescaLaGraella', async () => {
        // Verifica que l'acció de fila confirma, elimina la caché i refresca la graella.
        render(<Caches />);

        fireEvent.click(screen.getByRole('button', { name: 'Buidar' }));

        await waitFor(() => {
            expect(mocks.apiDeleteMock).toHaveBeenCalledWith('cache-1');
        });

        expect(mocks.refreshMock).toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Caché buidada',
            'success'
        );
    });

    it('Caches_quanEsBuidaTot_faLaPeticioGlobal', async () => {
        // Comprova que l'acció global envia la petició especial per buidar totes les cachés.
        render(<Caches />);

        fireEvent.click(screen.getByTitle('Buidar totes'));

        await waitFor(() => {
            expect(mocks.apiDeleteMock).toHaveBeenCalledWith('TOTES');
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Totes les cachés buidades',
            'success'
        );
    });
});
