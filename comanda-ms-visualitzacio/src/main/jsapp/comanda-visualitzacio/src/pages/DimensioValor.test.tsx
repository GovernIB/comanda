import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import DimensioValor from './DimensioValor';

const mocks = vi.hoisted(() => ({
    anyHistoryEntryExistMock: vi.fn(),
    goBackMock: vi.fn(),
    getOneMock: vi.fn(),
    artifactActionMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    useReadOnlyGestorMock: vi.fn(() => false),
    getOneDimensioValorMock: vi.fn(),
    updateDimensioValorMock: vi.fn(),
    getOneEntitatMock: vi.fn(),
    updateEntitatMock: vi.fn(),
    findEntitatMock: vi.fn(),
    getOneUOMock: vi.fn(),
    updateUOMock: vi.fn(),
    entitatArtifactActionMock: vi.fn(),
    tMock: vi.fn((selector: any, options?: any) => {
        const res = selector({
            page: {
                dimensions: {
                    valuesTitle: 'Valors dimensió {{nom}}',
                    action: {
                        sincronitzar: {
                            label: 'UO',
                            ok: 'Sincronitzat',
                        },
                        editar: {
                            label: 'Edita',
                            dialogTitle: 'Edita',
                            ok: 'Actualitzat',
                            save: 'Desar',
                        }
                    },
                    editaEntitat: {
                        field: { codi: 'Codi', nom: 'Nom', codiDir3: 'Codi Dir3', cif: 'CIF' },
                    },
                    editaUnitatOrganitzativa: {
                        field: { codi: 'Codi', denominacio: 'Nom', estat: 'Estat' },
                        estatOptions: { V: 'Vigent', E: 'Extingit', A: 'Anulat', T: 'Transitori' },
                    },
                    mapejaEntitat: {
                        field: { entitat: 'Entitat' },
                    },
                },
                unitatOrganitzativa: {
                    acl: { perm0Allowed: 'Permís UO' },
                },
                entitats: {
                    action: {
                        refreshUO: {
                            label: 'Refrescar UO',
                            ok: 'UO refrescada',
                        },
                        organigrama: {
                            label: 'Organigrama',
                            title: 'Organigrama',
                            ko: 'No s\'han trobat unitats',
                        },
                    },
                },
            },
            components: {
                clear: 'Netejar',
                permisos: {
                    title: 'Permisos',
                },
            },
        });
        if (typeof res === 'string' && options?.nom) {
            return res.replace('{{nom}}', options.nom);
        }
        return res;
    }),
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
    MuiDataGrid: ({
                      title,
                      fixedFilter,
                      toolbarElementsWithPositions,
                      toolbarAdditionalRow,
                      columns,
                      rowHideDeleteButton,
                      rowAdditionalActions,
                  }: {
        title: string;
        fixedFilter?: string;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        toolbarAdditionalRow?: React.ReactNode;
        columns: Array<{ field: string }>;
        rowHideDeleteButton?: boolean;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: string) => void; hidden?: boolean; action?: string }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="fixed-filter">{fixedFilter}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            <div data-testid="hide-delete">{String(rowHideDeleteButton)}</div>
            {/* CORREGIT: exposem el camp "action" de cada acció perquè els tests puguin detectar si es fa
                servir un codi d'acció d'un altre recurs (p.ex. REFRESH_UO és de 'entitat', no de 'dimensioValor'),
                cosa que MuiDataGrid amaga silenciosament comprovant els artifacts del recurs actual. */}
            <div data-testid="actions-meta">{JSON.stringify(rowAdditionalActions?.map((a) => ({ label: a.label, action: a.action ?? null })))}</div>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            <div>{toolbarAdditionalRow}</div>
            {/* CORREGIT: Filtrem les accions ocultes per reflectir el comportament real del component */}
            {rowAdditionalActions?.filter(action => !action.hidden).map((action) => (
                <button key={action.label} onClick={() => action.onClick?.('15')} type="button">
                    {action.label}
                </button>
            ))}
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
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: vi.fn(),
        },
    }),
    MuiDialog: ({ open, title, children }: { open: boolean; title: string; children: React.ReactNode }) => (
        open ? (
            <div role="dialog">
                <h3>{title}</h3>
                {children}
            </div>
        ) : null
    ),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dimensioValor') {
            return {
                artifactAction: mocks.artifactActionMock,
                getOne: mocks.getOneDimensioValorMock,
                update: mocks.updateDimensioValorMock,
            };
        }
        if (resourceName === 'entitat') {
            return {
                isReady: true,
                getOne: mocks.getOneEntitatMock,
                update: mocks.updateEntitatMock,
                find: mocks.findEntitatMock,
                artifactAction: mocks.entitatArtifactActionMock,
            };
        }
        if (resourceName === 'unitatOrganitzativa') {
            return {
                isReady: true,
                getOne: mocks.getOneUOMock,
                update: mocks.updateUOMock,
            };
        }
        return {
            isReady: true,
            getOne: mocks.getOneMock,
        };
    },
    useBaseAppContext: () => ({
        goBack: mocks.goBackMock,
        anyHistoryEntryExist: mocks.anyHistoryEntryExistMock,
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
    default: () => mocks.useReadOnlyGestorMock(),
}));

vi.mock('../components/AclPermissionManager.tsx', () => ({
    useAclCustomPermissionManager: () => ({
        show: vi.fn(),
        component: <div>Gestor permisos</div>,
    }),
}));

vi.mock('@mui/x-tree-view', () => ({
    SimpleTreeView: ({ children, defaultExpandedItems }: { children: React.ReactNode; defaultExpandedItems?: string[] }) => (
        <div data-testid="tree-view" data-default-expanded={defaultExpandedItems?.join(',')}>
            {children}
        </div>
    ),
    TreeItem: ({ itemId, label, children }: { itemId: string; label: React.ReactNode; children?: React.ReactNode }) => (
        <div data-testid={`tree-item-${itemId}`}>
            {label}
            {children}
        </div>
    ),
}));

describe('DimensioValor', () => {
    beforeEach(() => {
        // CORREGIT: Afegim 'tipus' perquè l'acció de sincronitzar no estigui oculta per defecte en les proves
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ORGAN_GESTOR' });
        mocks.anyHistoryEntryExistMock.mockReturnValue(true);
    });

    afterEach(() => {
        vi.clearAllMocks();
        mocks.useReadOnlyGestorMock.mockReturnValue(false);
    });

    it('DimensioValor_quanEsCarregaLaDimensio_mostraElTitolIElFiltreStatic', async () => {
        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.getByTestId('page-title')).toHaveTextContent('Valors dimensió Dimensió prova');
        expect(screen.getByTestId('fixed-filter')).toHaveTextContent('dimensio.id=15');
        expect(screen.getByTestId('columns')).toHaveTextContent('codiNom');
        expect(screen.getByTestId('hide-delete')).toHaveTextContent('false');

        expect(mocks.getOneMock).toHaveBeenCalledWith('15');

        // MILLORA: Consultem el botó de tornar de manera més robusta buscant la icona
        const backButton = screen.getAllByRole('button').find(btn => btn.textContent?.includes('arrow_back'));
        expect(backButton).toBeEnabled();
    });

    it('DimensioValor_quanNoHiHaHistorial_deshabilitaElBotoDeTornada', async () => {
        mocks.anyHistoryEntryExistMock.mockReturnValue(false);

        render(<DimensioValor />);

        await waitFor(() => {
            const backButton = screen.getAllByRole('button').find(btn => btn.textContent?.includes('arrow_back'));
            expect(backButton).toBeDisabled();
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

    it('DimensioValor_quanEsPremAccioUO_cridaApiActionIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'UO' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'UO' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('15', { code: 'UO_DIR3' });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Sincronitzat', 'success');
        });
    });

    it('DimensioValor_quanTipusNoEsEntitatNiOrganGestor_ocultaAccioEditar', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ALTRE' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.queryByRole('button', { name: 'Edita' })).not.toBeInTheDocument();
    });

    it('DimensioValor_quanTipusEntitatPeroMapeigNoEsManual_ocultaAccioEditar', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT', entitatValorTipus: 'CODI' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.queryByRole('button', { name: 'Edita' })).not.toBeInTheDocument();
    });

    it('DimensioValor_quanTipusEntitatIResolta_editaCarregaEntitatIDesarLaActualitza', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT', entitatValorTipus: 'MANUAL' });
        mocks.getOneDimensioValorMock.mockResolvedValue({ id: '15', valor: 'A01', entitat: { id: 5, description: 'Ent A' } });
        mocks.getOneEntitatMock.mockResolvedValue({ id: 5, codi: 'A01', nom: 'Entitat A', codiDir3: 'D3', cif: 'Q0700077E' });
        mocks.updateEntitatMock.mockResolvedValue({});

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Edita' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Edita' }));

        await waitFor(() => {
            expect(mocks.getOneDimensioValorMock).toHaveBeenCalledWith('15');
            expect(mocks.getOneEntitatMock).toHaveBeenCalledWith(5);
        });

        const nomInput = await screen.findByLabelText('Nom');
        expect(nomInput).toHaveValue('Entitat A');
        fireEvent.change(nomInput, { target: { value: 'Entitat A Nova' } });

        const cifInput = screen.getByLabelText('CIF');
        expect(cifInput).toHaveValue('Q0700077E');

        fireEvent.click(screen.getByRole('button', { name: 'Desar' }));

        await waitFor(() => {
            expect(mocks.updateEntitatMock).toHaveBeenCalledWith(5, {
                data: { id: 5, codi: 'A01', nom: 'Entitat A Nova', codiDir3: 'D3', cif: 'Q0700077E' },
            });
            expect(mocks.updateDimensioValorMock).not.toHaveBeenCalled();
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Actualitzat', 'success');
        });
    });

    it('DimensioValor_quanTipusEntitatSenseResoldre_editaPermetMapejarManualment', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT', entitatValorTipus: 'MANUAL' });
        mocks.getOneDimensioValorMock.mockResolvedValue({ id: '15', valor: 'X', entitat: null });
        mocks.findEntitatMock.mockResolvedValue({ rows: [{ id: 7, codi: 'B1', nom: 'Entitat B' }] });
        mocks.updateDimensioValorMock.mockResolvedValue({});

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Edita' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Edita' }));

        await waitFor(() => {
            expect(mocks.getOneEntitatMock).not.toHaveBeenCalled();
        });

        const entitatInput = await screen.findByLabelText('Entitat');
        fireEvent.change(entitatInput, { target: { value: 'Ent' } });

        const option = await screen.findByText('Entitat B');
        fireEvent.click(option);

        fireEvent.click(screen.getByRole('button', { name: 'Desar' }));

        await waitFor(() => {
            expect(mocks.updateDimensioValorMock).toHaveBeenCalledWith('15', { data: { entitatMapejada: 7 } });
            expect(mocks.updateEntitatMock).not.toHaveBeenCalled();
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Actualitzat', 'success');
        });
    });

    it('DimensioValor_quanTipusOrganGestor_ocultaAccioEditar', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ORGAN_GESTOR' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.queryByRole('button', { name: 'Edita' })).not.toBeInTheDocument();
    });

    it('DimensioValor_quanTipusConselleria_ocultaAccioEditar', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'CONSELLERIA' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.queryByRole('button', { name: 'Edita' })).not.toBeInTheDocument();
    });

    it('DimensioValor_quanTipusNoEsEntitat_ocultaAccionsRefrescarUOIOrganigrama', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ORGAN_GESTOR' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Valors dimensió Dimensió prova' })).toBeInTheDocument();
        });

        expect(screen.queryByRole('button', { name: 'Refrescar UO' })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Organigrama' })).not.toBeInTheDocument();
    });

    it('DimensioValor_quanTipusEntitat_mostraAccionsRefrescarUOIOrganigrama', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT', entitatValorTipus: 'MANUAL' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Refrescar UO' })).toBeInTheDocument();
            expect(screen.getByRole('button', { name: 'Organigrama' })).toBeInTheDocument();
        });
    });

    it('DimensioValor_quanEsPremRefrescarUO_obteLEntitatVinculadaICridaApiActionIMostraMissatgeExit', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT' });
        mocks.getOneDimensioValorMock.mockResolvedValue({ id: '15', entitat: { id: 5 } });
        mocks.entitatArtifactActionMock.mockResolvedValue({});

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Refrescar UO' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Refrescar UO' }));

        await waitFor(() => {
            expect(mocks.getOneDimensioValorMock).toHaveBeenCalledWith('15');
            expect(mocks.entitatArtifactActionMock).toHaveBeenCalledWith(5, { code: 'REFRESH_UO' });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'UO refrescada', 'success');
        });
    });

    it('DimensioValor_quanEsPremRefrescarUOSenseEntitatVinculada_noCridaApiAction', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT' });
        mocks.getOneDimensioValorMock.mockResolvedValue({ id: '15', entitat: null });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Refrescar UO' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Refrescar UO' }));

        await waitFor(() => {
            expect(mocks.getOneDimensioValorMock).toHaveBeenCalledWith('15');
        });
        expect(mocks.entitatArtifactActionMock).not.toHaveBeenCalled();
    });

    it('DimensioValor_quanEsPremOrganigrama_obreElDialegAmbLEntitatVinculada', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT' });
        mocks.getOneDimensioValorMock.mockResolvedValue({ id: '15', entitat: { id: 5 } });
        mocks.getOneEntitatMock.mockResolvedValue({ id: 5, codiDir3: 'D3' });
        mocks.entitatArtifactActionMock.mockResolvedValue([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null },
        ]);

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Organigrama' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(mocks.getOneDimensioValorMock).toHaveBeenCalledWith('15');
            expect(mocks.getOneEntitatMock).toHaveBeenCalledWith(5);
            expect(mocks.entitatArtifactActionMock).toHaveBeenCalledWith(5, { code: 'ORGANIGRAMA' });
        });

        await waitFor(() => {
            expect(screen.getByRole('dialog')).toBeInTheDocument();
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });
    });

    it('DimensioValor_laAccioRefrescarUO_noEspecificaCapCodiDaccioDunAltreRecurs', async () => {
        // REFRESH_UO és una acció registrada al recurs 'entitat', no a 'dimensioValor'. Si s'especifica com a
        // "action" a MuiDataGrid (resourceName="dimensioValor"), es comprova contra els artifacts del recurs
        // dimensioValor i queda amagada silenciosament encara que "hidden" sigui false.
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT', entitatValorTipus: 'MANUAL' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByTestId('actions-meta')).toBeInTheDocument();
        });

        const actionsMeta = JSON.parse(screen.getByTestId('actions-meta').textContent ?? '[]');
        const refrescarUO = actionsMeta.find((a: any) => a.label === 'Refrescar UO');
        expect(refrescarUO?.action).toBeNull();
    });

    it('DimensioValor_quanFallaObtenirElValorDeDimensioPerRefrescarUO_mostraLErrorDeLApi', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT' });
        mocks.getOneDimensioValorMock.mockRejectedValue({ message: 'Error obtenint valor' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Refrescar UO' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Refrescar UO' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error obtenint valor', 'error');
        });
    });

    it('DimensioValor_quanFallaObtenirElValorDeDimensioPerOrganigrama_mostraLErrorDeLApi', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT' });
        mocks.getOneDimensioValorMock.mockRejectedValue({ message: 'Error obtenint valor' });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Organigrama' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error obtenint valor', 'error');
        });
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('DimensioValor_quanEsPremOrganigramaSenseEntitatVinculada_mostraMissatgeIN0brElDialeg', async () => {
        mocks.getOneMock.mockResolvedValue({ nom: 'Dimensió prova', tipus: 'ENTITAT' });
        mocks.getOneDimensioValorMock.mockResolvedValue({ id: '15', entitat: null });

        render(<DimensioValor />);

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Organigrama' })).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'No s\'han trobat unitats', 'error');
        });
        expect(mocks.getOneEntitatMock).not.toHaveBeenCalled();
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });
});
