import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Dimensions from './Dimensions';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    findMock: vi.fn(),
    showMock: vi.fn(), // Nou: per mockejar l'obertura del diàleg
    artifactActionMock: vi.fn(),
    indicadorArtifactActionMock: vi.fn(),
    isUserAdminMock: vi.fn(() => true),
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
                            error: 'Error actualitzant la consergeria',
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
                        sincronitzarCataleg: 'Sincronitzar catàleg',
                        sincronitzarCatalegConfirm: 'Segur que voleu sincronitzar?',
                        sincronitzarCatalegSuccess: 'Catàleg sincronitzat correctament',
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
                      persistentStateActive,
                      persistentStateKey,
                      persistentStateClearPageSortPropsOnTopLevelRouteChange,
                  }: {
        title: string;
        filter?: string;
        toolbarAdditionalRow?: React.ReactNode;
        rowAdditionalActions?: Array<{ label: string; linkTo?: string; onClick?: (id: string, row: any) => void; showInMenu?: boolean; hidden?: boolean | ((row: any) => boolean) }>;
        columns: Array<{ field: string }>;
        persistentStateActive?: boolean;
        persistentStateKey?: string;
        persistentStateClearPageSortPropsOnTopLevelRouteChange?: boolean;
    }) => {
        // Simulem una fila per passar-la als onClick i poder provar lògica que depèn de 'row'
        const mockRow = { id: '15', entornAppId: 99, tipus: mocks.mockRowTipus };
        return (
            <section>
                <h2>{title}</h2>
                <div data-testid="filter-value">{filter}</div>
                <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
                <div data-testid="row-link">{rowAdditionalActions?.find(a => a.linkTo)?.linkTo}</div>
                <div
                    data-testid="datagrid-persistent-state"
                    data-active={String(!!persistentStateActive)}
                    data-key={persistentStateKey ?? ''}
                    data-clear-on-top-level={String(!!persistentStateClearPageSortPropsOnTopLevelRouteChange)}
                />
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
    MuiFilter: ({
                    children,
                    persistentStateActive,
                    persistentStateKey,
                }: {
        children: React.ReactNode;
        persistentStateActive?: boolean;
        persistentStateKey?: string;
    }) => (
        <div>
            <div
                data-testid="filter-persistent-state"
                data-active={String(!!persistentStateActive)}
                data-key={persistentStateKey ?? ''}
            />
            {children}
        </div>
    ),
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
        if (resourceName === 'indicador') {
            return {
                artifactAction: mocks.indicadorArtifactActionMock,
            };
        }
        return {
            isReady: true,
            find: mocks.findMock,
        };
    },
}));

vi.mock('../components/UserContext.ts', () => ({
    useIsUserAdmin: () => mocks.isUserAdminMock(),
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

vi.mock('../components/DimensioFetConsProgressDialog.tsx', () => ({
    default: ({ open, dimensioId, onComplete }: { open: boolean; dimensioId?: any; onComplete?: (error: boolean) => void }) => (
        <div data-testid="fet-cons-progress-dialog" data-open={String(!!open)} data-dimensio-id={String(dimensioId ?? '')}>
            <button onClick={() => onComplete?.(false)}>Simula onComplete èxit</button>
            <button onClick={() => onComplete?.(true)}>Simula onComplete error</button>
        </div>
    ),
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
        mocks.isUserAdminMock.mockReturnValue(true);
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

    it('Dimensions_quanEsRenderitza_activaLaPersistenciaDEstatDelFiltreIDeLaGraella', async () => {
        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        const filterPersistentState = screen.getByTestId('filter-persistent-state');
        expect(filterPersistentState).toHaveAttribute('data-active', 'true');
        expect(filterPersistentState).toHaveAttribute('data-key', 'dimensioFilter');

        const gridPersistentState = screen.getByTestId('datagrid-persistent-state');
        expect(gridPersistentState).toHaveAttribute('data-active', 'true');
        expect(gridPersistentState).toHaveAttribute('data-key', 'dimensio');
        expect(gridPersistentState).toHaveAttribute('data-clear-on-top-level', 'true');
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

    it('Dimensions_quanEsPremAccioFET_CONS_cridaApiAction', async () => {
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
        });
    });

    it('Dimensions_quanEsPremAccioFET_CONS_obreLaModalDeProgresIEsMantéObertaEncaraQueLaCridaHttpResolgui', async () => {
        // La resolució de la crida HTTP, per si sola, no ha de tancar la modal: qui pot no fer cap feina real
        // (perquè ja hi ha una execució en curs) rebria una resposta ràpida sense que el procés real hagi acabat.
        // Només onComplete (senyal SSE real) ha de tancar-la.
        let resolveAction: (value: any) => void = () => undefined;
        mocks.artifactActionMock.mockImplementation(() => new Promise((resolve) => { resolveAction = resolve; }));

        render(<Dimensions />);

        expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'false');

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'FET_CONS' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'FET_CONS' }));

        await waitFor(() => {
            const dialog = screen.getByTestId('fet-cons-progress-dialog');
            expect(dialog).toHaveAttribute('data-open', 'true');
            expect(dialog).toHaveAttribute('data-dimensio-id', '15');
        });

        resolveAction({});

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalled();
        });
        expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'true');
    });

    it('Dimensions_quanLaModalCridaOnCompleteSenseError_tancaLaModalIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'FET_CONS' })).toBeInTheDocument();
        });
        fireEvent.click(screen.getByRole('button', { name: 'FET_CONS' }));

        await waitFor(() => {
            expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'true');
        });

        fireEvent.click(screen.getByRole('button', { name: 'Simula onComplete èxit' }));

        expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'false');
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Consergeria actualitzada', 'success');
    });

    it('Dimensions_quanLaModalCridaOnCompleteAmbError_tancaLaModalIMostraMissatgeError', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'FET_CONS' })).toBeInTheDocument();
        });
        fireEvent.click(screen.getByRole('button', { name: 'FET_CONS' }));

        await waitFor(() => {
            expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'true');
        });

        fireEvent.click(screen.getByRole('button', { name: 'Simula onComplete error' }));

        expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'false');
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error actualitzant la consergeria', 'error');
    });

    it('Dimensions_quanLaCridaHttpFallaAbansQueArribiCapOnComplete_tancaLaModalIMostraLErrorHttp', async () => {
        // Cobreix el cas d'una fallada abans que arrenqui cap procés real (p.ex. error de xarxa o de
        // permisos): mai arribarà cap event SSE, així que la crida HTTP és l'únic senyal disponible.
        mocks.artifactActionMock.mockRejectedValue({ message: 'Error de xarxa' });

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'FET_CONS' })).toBeInTheDocument();
        });
        fireEvent.click(screen.getByRole('button', { name: 'FET_CONS' }));

        await waitFor(() => {
            expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'false');
        });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error de xarxa', 'error');
    });

    it('Dimensions_quanLaCridaHttpFallaDespresQueOnCompleteJaHaTancatLaModal_noDuplicaElMissatge', async () => {
        // Cas de l'execució propietària: onComplete (SSE) i el rebuig de la seva pròpia crida HTTP arriben
        // gairebé alhora pel mateix error real; només s'ha de mostrar un missatge, no dos.
        let rejectAction: (reason: any) => void = () => undefined;
        mocks.artifactActionMock.mockImplementation(() => new Promise((_resolve, reject) => { rejectAction = reject; }));

        render(<Dimensions />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'FET_CONS' })).toBeInTheDocument();
        });
        fireEvent.click(screen.getByRole('button', { name: 'FET_CONS' }));

        await waitFor(() => {
            expect(screen.getByTestId('fet-cons-progress-dialog')).toHaveAttribute('data-open', 'true');
        });

        fireEvent.click(screen.getByRole('button', { name: 'Simula onComplete error' }));
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledTimes(1);

        rejectAction({ message: 'Error de xarxa' });

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalled();
        });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledTimes(1);
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

    it('Dimensions_quanEsAdminIEntornAppSeleccionat_executaSincronitzarCatalegAlConfirmar', async () => {
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.indicadorArtifactActionMock.mockResolvedValue({ success: true, message: 'Catàleg sincronitzat' });

        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        const syncButton = screen.getByTitle('Sincronitzar catàleg');
        expect(syncButton).toBeInTheDocument();
        expect(syncButton).toBeDisabled();

        fireEvent.click(screen.getByTestId('simulate-filter-entornapp'));
        expect(syncButton).toBeEnabled();

        fireEvent.click(syncButton);

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalled();
            expect(mocks.indicadorArtifactActionMock).toHaveBeenCalledWith(null, {
                code: 'sincronitzar_cataleg',
                data: { entornAppId: 9 },
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Catàleg sincronitzat', 'success');
        });
    });

    it('Dimensions_quanNoEsAdmin_noMostraBotoSincronitzarCataleg', async () => {
        mocks.isUserAdminMock.mockReturnValue(false);

        render(<Dimensions />);

        await waitFor(() => {
            expect(mocks.findMock).toHaveBeenCalled();
        });

        expect(screen.queryByTitle('Sincronitzar catàleg')).not.toBeInTheDocument();
    });
});
