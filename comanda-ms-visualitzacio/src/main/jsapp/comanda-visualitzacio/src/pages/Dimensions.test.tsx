import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Dimensions from './Dimensions';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    findMock: vi.fn(),
    showMock: vi.fn(), // Nou: per mockejar l'obertura del diàleg
    artifactActionMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    formContextData: { tipus: undefined as string | undefined },
    mockRowTipus: 'ORGAN_GESTOR' as string,
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
                        changeTipus: {
                            label: 'Canviar tipus',
                            ok: 'Tipus cambiat',
                            field: {
                                tipus: 'Tipus de dimensió',
                                entitatValorTipus: 'Camp de mapeig',
                            },
                        },
                        desmarcar: {
                            label: 'NO_ORGAN_GESTOR',
                            ok: 'Tipus cambiat a null',
                        },
                        updateEntitats: {
                            label: 'UPDATE_ENTITATS',
                            ok: 'Entitats actualitzades',
                        },
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
        rowAdditionalActions?: Array<{ label: string; linkTo?: string; onClick?: (id: string, row: any) => void; showInMenu?: boolean; hidden?: boolean | ((row: any) => boolean) }>;
        columns: Array<{ field: string }>;
    }) => {
        // Simulem una fila per passar-la als onClick i poder provar lògica que depèn de 'row'
        const mockRow = { id: '15', entornAppId: 99, tipus: mocks.mockRowTipus };
        return (
            <section>
                <h2>{title}</h2>
                <div data-testid="filter-value">{filter}</div>
                <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
                <div data-testid="row-link">{rowAdditionalActions?.find(a => a.linkTo)?.linkTo}</div>
                <div>{toolbarAdditionalRow}</div>
                {rowAdditionalActions?.filter(a => a.onClick).map((action) => (
                    <button
                        key={action.label}
                        data-in-menu={String(!!action.showInMenu)}
                        data-hidden={String(typeof action.hidden === 'function' ? action.hidden(mockRow) : !!action.hidden)}
                        onClick={() => action.onClick?.(mockRow.id, mockRow)}
                        type="button"
                    >
                        {action.label}
                    </button>
                ))}
            </section>
        )
    },
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name, label, optionsRequest }: { name: string; label?: string; optionsRequest?: (q: string) => Promise<{ options: Array<{ description?: string }> }> }) => (
        <div>
            <span data-testid={`field-${name}`}>{label ?? name}</span>
            {optionsRequest ? (
                <button onClick={async () => {
                    const result = await optionsRequest('entorn');
                    const descriptions = result.options.map((option) => option.description).join(',');
                    document.body.setAttribute('data-dimension-options', descriptions);
                }}>
                    Carrega opcions dimensions
                </button>
            ) : null}
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
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: vi.fn(),
        },
    }),
    useMuiFormDialogApiRef: () => ({
        current: {
            show: mocks.showMock,
        },
    }),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
        messageDialogShow: mocks.messageDialogShowMock,
    }),
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useFormDialogButtons: () => [
        { value: false, text: 'Cancel·lar' },
        { value: true, text: 'Desar' },
    ],
    useFormContext: () => ({ data: mocks.formContextData }),
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

vi.mock('../components/FormActionDialog.tsx', () => ({
    default: ({ children, formDialogButtons }: { children: React.ReactNode; formDialogButtons?: Array<{ text: string }> }) => (
        <div>
            {children}
            {formDialogButtons && (
                <div data-testid="dialog-buttons">{formDialogButtons.map((b) => b.text).join(',')}</div>
            )}
        </div>
    ),
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
        mocks.formContextData.tipus = undefined;
        mocks.mockRowTipus = 'ORGAN_GESTOR';
    });

    it('Dimensions_quanEsRenderitza_mostraElGridElFiltreIElLinkAlsValors', async () => {
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('page-title')).toHaveTextContent('Dimensions');
        expect(screen.getByRole('heading', { name: 'Dimensions' })).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent('entornAppId=0');
        // Corregit: s'ha afegit 'tipus' a les columnes esperades
        expect(screen.getByTestId('columns')).toHaveTextContent('codi,nom,descripcio,tipus');
        expect(screen.getByTestId('row-link')).toHaveTextContent('valor/{{id}}');
    });

    it('Dimensions_quanEsCarreguenLesOpcionsDelFiltre_utilitzaElsEntornsRecuperats', async () => {
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
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        fireEvent.click(screen.getByTitle('Netejar'));

        expect(mocks.clearMock).toHaveBeenCalled();
    });

    it('Dimensions_quanEsPremAccioFET_CONS_cridaApiActionIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'FET_CONS' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'FET_CONS' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('15', {
                code: 'FET_CONS',
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Consergeria actualitzada', 'success');
        });
    });

    it('Dimensions_quanEsPremAccioCanviarTipus_obreElDialogAmbLesDadesActualsPrecarregades', async () => {
        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Canviar tipus' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Canviar tipus' }));

        await waitFor(() => {
            // Verifiquem que s'obre el diàleg amb l'id, l'entornAppId i el tipus/mapeig actuals de la fila,
            // per poder editar el mapeig sense haver de tornar a triar el tipus.
            expect(mocks.showMock).toHaveBeenCalledWith('15', {
                entornAppId: 99,
                tipus: 'ORGAN_GESTOR',
                entitatValorTipus: undefined,
                dimensioId: '15',
            });
        });
    });

    it('Dimensions_laAccioCanviarTipus_esMostraAlMenuINoEstaOcultaEncaraQueLaDimensioJaTinguiTipus', async () => {
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        const button = screen.getByRole('button', { name: 'Canviar tipus' });

        // mockRow ja té tipus='ORGAN_GESTOR': l'acció ha d'estar disponible igualment (per editar el mapeig)
        expect(button).toHaveAttribute('data-in-menu', 'true');
        expect(button).toHaveAttribute('data-hidden', 'false');
    });

    it('Dimensions_laAccioDesmarcar_esMostraAlMenu', async () => {
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByRole('button', { name: 'NO_ORGAN_GESTOR' })).toHaveAttribute('data-in-menu', 'true');
    });

    it('Dimensions_laAccioFET_CONS_esMostraAlMenu', async () => {
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByRole('button', { name: 'FET_CONS' })).toHaveAttribute('data-in-menu', 'true');
    });

    it('Dimensions_quanLaDimensioEsConselleria_ocultaCanviarTipusIDesmarcar', async () => {
        mocks.mockRowTipus = 'CONSELLERIA';

        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByRole('button', { name: 'Canviar tipus' })).toHaveAttribute('data-hidden', 'true');
        expect(screen.getByRole('button', { name: 'NO_ORGAN_GESTOR' })).toHaveAttribute('data-hidden', 'true');
    });

    it('Dimensions_elDialegDeCanviarTipus_mostraLesEtiquetesDelsCampsIElBotoDesar', async () => {
        mocks.formContextData.tipus = 'ENTITAT';

        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('field-tipus')).toHaveTextContent('Tipus de dimensió');
        expect(screen.getByTestId('field-entitatValorTipus')).toHaveTextContent('Camp de mapeig');
        expect(screen.getByTestId('dialog-buttons')).toHaveTextContent('Desar');
    });

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

    it('Dimensions_quanEsPremAccioActualitzaEntitats_cridaApiActionAmbUPDATE_ENTITATSIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'UPDATE_ENTITATS' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'UPDATE_ENTITATS' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('15', {
                code: 'UPDATE_ENTITATS',
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Entitats actualitzades', 'success');
        });
    });

    it('Dimensions_quanElTipusDelFormulariEsEntitat_mostraElCampEntitatValorTipus', async () => {
        mocks.formContextData.tipus = 'ENTITAT';

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByTestId('field-entitatValorTipus')).toBeInTheDocument();
        });
    });

    it('Dimensions_quanElTipusDelFormulariNoEsEntitat_noMostraElCampEntitatValorTipus', async () => {
        mocks.formContextData.tipus = 'ORGAN_GESTOR';

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Dimensions' })).toBeInTheDocument();
        });

        expect(screen.queryByTestId('field-entitatValorTipus')).not.toBeInTheDocument();
    });
});
