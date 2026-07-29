import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Dimensions from './Dimensions';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    findMock: vi.fn(),
    artifactActionMock: vi.fn(), // NUEVO: Para mockear artifactAction de 'dimensio'
    temporalMessageShowMock: vi.fn(), // NUEVO: Para mockear temporalMessageShow
    messageDialogShowMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                dimensions: {
                    title: 'Dimensions',
                    values: 'Valors',
                    action: {
                        refreshCons: {
                            label: 'FET_CONS',
                            ok: 'Consergeria actualitzada',
                            title: 'Voleu actualitzar la consergeria?',
                        },
                        marcarOrgan: {
                            label: 'ORGAN_GESTOR',
                            ok: 'Tipus cambiat a ORGAN_GESTOR',
                        },
                        desmarcar: {
                            label: 'NO_ORGAN_GESTOR',
                            ok: 'Tipus cambiat a null',
                        }
                    },
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
        rowAdditionalActions?: Array<{ label: string; linkTo?: string; onClick?: (id: string) => void; hidden?: (row: any) => boolean }>;
        columns: Array<{ field: string }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="filter-value">{filter}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            {/* CORREGIDO: Buscamos la acción que tiene linkTo, en lugar de asumir que es la primera */}
            <div data-testid="row-link">{rowAdditionalActions?.find(a => a.linkTo)?.linkTo}</div>
            <div>{toolbarAdditionalRow}</div>
            {/* Renderizamos las acciones con onClick para poder testearlas */}
            {rowAdditionalActions?.filter(a => a.onClick).map((action) => (
                <button key={action.label} onClick={() => action.onClick?.('15')} type="button">
                    {action.label}
                </button>
            ))}
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

    // NUEVO: Mock para useMuiDataGridApiRef
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: vi.fn(),
        },
    }),

    // NUEVO: Mock para useBaseAppContext
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
        messageDialogShow: mocks.messageDialogShowMock,
    }),

    useConfirmDialogButtons: () => <button>Confirmar</button>,

    // MODIFICADO: Diferenciamos el mock según el recurso solicitado
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dimensio') {
            return {
                artifactAction: mocks.artifactActionMock,
            };
        }
        return {
            isReady: true,
            find: mocks.findMock,
        };
    },
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

    // NUEVO TEST: Para cubrir la nueva funcionalidad de cambio de tipo a ORGAN_GESTOR
    it('Dimensions_quanEsPremAccioORGAN_GESTOR_cridaApiActionIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'ORGAN_GESTOR' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'ORGAN_GESTOR' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('15', {
                code: 'CHANGE_TIPUS',
                data: { tipus: 'ORGAN_GESTOR' }
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Tipus cambiat a ORGAN_GESTOR', 'success');
        });
    });

    // NUEVO TEST: Para cubrir la nueva funcionalidad de cambio de tipo a null (NO_ORGAN_GESTOR)
    it('Dimensions_quanEsPremAccioNO_ORGAN_GESTOR_cridaApiActionIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'NO_ORGAN_GESTOR' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'NO_ORGAN_GESTOR' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('15', {
                code: 'CHANGE_TIPUS',
                data: { tipus: null }
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Tipus cambiat a null', 'success');
        });
    });
});
