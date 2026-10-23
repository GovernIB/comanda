import * as React from 'react';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import AlarmaConfig, { AlarmaConfigForm, EntornAppSelector } from './AlarmaConfig';
import { MuiDataGridColDef } from 'reactlib';

const mocks = vi.hoisted(() => ({
    setFieldValueMock: vi.fn(),
    artifactActionMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    refreshMock: vi.fn(),
    tMock: vi.fn((selector: any) => {
        if (typeof selector !== 'function') return selector;
        return selector({
            enum: {
                appEstat: {
                    UP: { title: 'Activa' },
                    WARN: { title: 'Avís' },
                    DEGRADED: { title: 'Degradada' },
                    DOWN: { title: 'Caiguda' },
                    MAINTENANCE: { title: 'Manteniment' },
                    UNKNOWN: { title: 'Desconegut' },
                    ERROR: { title: 'Error' },
                },
            },
            components: {
                clear: 'Netejar',
            },
            page: {
                alarma: {
                    snackbar: {
                        title: 'Alarmes',
                    },
                },
                alarmaConfig: {
                    title: 'Configuració d alarmes',
                    toolbarCreateLabel: 'Nova alarma',
                    update: 'Editar configuració',
                    create: 'Crear configuració',
                    condicio: {
                        title: 'Condició',
                        subtitle: 'Condició de dispar',
                        add: 'Afegir condició',
                        scope: 'Àmbit',
                        scopeOptions: {
                            aplicacio: 'Aplicació',
                            subsistema: 'Subsistema',
                            integracio: 'Integració',
                            sistema: 'Sistema',
                        },
                        code: 'Codi',
                        metric: 'Mètrica',
                        metricOptions: {
                            estat: 'Estat',
                            latencia: 'Latència',
                            carregaMitjanaSistema: 'Càrrega mitjana del sistema',
                            memoriaDisponible: 'Memòria disponible',
                            espaiDiscLliure: 'Espai de disc lliure',
                        },
                        comparator: 'Comparador',
                        comparatorOptions: {
                            major: 'Major',
                            majorIgual: 'Major o igual',
                            menor: 'Menor',
                            menorIgual: 'Menor o igual',
                            igual: 'Igual',
                            diferent: 'Diferent',
                            en: 'En',
                        },
                        value: 'Valor',
                        valueMs: 'Valor (ms)',
                        operator: 'Lògica entre condicions',
                    },
                    periode: {
                        switch: 'Configurar període',
                        title: 'Període',
                        subtitle: 'Límit temporal',
                    },
                    periodeInactiu: {
                        switch: 'Configurar període inactiu',
                        title: 'Període inactiu',
                        subtitle: 'Interval sense activitat',
                    },
                    filter: {
                        showOnlyOwnEnabled: 'Mostrar només les meves alarmes',
                        showOnlyOwnDisabled: 'Mostra només les alarmes d\'administrador',
                        more: 'Més filtres',
                        entornApp: "Entorn d'aplicació",
                    },
                },
            },
        });
    }),
    tLibMock: vi.fn((key: string) => {
        const values: Record<string, string> = {
            'datacommon.delete.label': 'Eliminar',
            'datacommon.delete.single.label': 'Eliminar configuració',
            'datacommon.delete.single.confirm': 'Confirmes l eliminació?',
            'datacommon.delete.single.success': 'Eliminada',
            'datacommon.delete.single.error': 'No s ha pogut eliminar',
        };
        return values[key] ?? key;
    }),
    getOneEntornAppMock: vi.fn(),
    findEntornAppsMock: vi.fn(),
    getOneAlarmaConfigMock: vi.fn(),
    navigateMock: vi.fn(),
    confirmMock: vi.fn(() => true),
    lastGridProps: {} as Record<string, unknown>,
}));

// Dades del formulari tal com les veu el contingut (context del MuiForm o del FormDialog de la graella).
const formData = {
    entornAppId: 7,
    tipus: 'APP_LATENCIA',
    periodeValor: 5,
    periodeUnitat: 'MINUTS',
    inactiuDesde: '01:00:00',
    inactiuFins: '03:00:00',
    admin: false,
    regla: {
        tipusNode: 'GRUP',
        operador: 'AND',
        fills: [{
            tipusNode: 'CONDICIO',
            ambit: 'APLICACIO',
            metrica: 'ESTAT',
            comparador: 'EN',
            valorsText: ['DOWN']
        }]
    }
};

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('react-router-dom', () => ({
    useParams: () => ({
        id: '44',
    }),
    useNavigate: () => mocks.navigateMock,
}));

vi.mock('reactlib', async (importOriginal) => {
    const original = await importOriginal<typeof import('reactlib')>();

    return {
    ...original,
    FormPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiDataGrid: (props: {
        title: string;
        filter?: string;
        toolbarAdditionalRow?: React.ReactNode;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode; position: string }>;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: unknown) => void }>;
        columns?: MuiDataGridColDef[];
        toolbarCreateLink?: string;
        popupEditActive?: boolean;
        popupEditFormContent?: React.ReactElement;
        popupEditFormDialogOnClose?: (reason?: string) => boolean;
    }) => {
        const {
            title,
            filter,
            toolbarAdditionalRow,
            toolbarElementsWithPositions,
            rowAdditionalActions,
            columns,
            toolbarCreateLink,
            popupEditActive,
            popupEditFormContent,
            popupEditFormDialogOnClose,
        } = props;
        mocks.lastGridProps = props;
        const [dialogOpen, setDialogOpen] = React.useState(false);
        // Mateix contracte que la llibreria: amb toolbarCreateLink el botó de crear navega; amb
        // popupEditActive obre el FormDialog, que es tanca si popupEditFormDialogOnClose no ho impedeix.
        const handleCreate = () => {
            if (toolbarCreateLink) {
                mocks.navigateMock(toolbarCreateLink);
            } else if (popupEditActive) {
                setDialogOpen(true);
            }
        };
        const handleDialogClose = () => {
            const doClose = popupEditFormDialogOnClose != null ? popupEditFormDialogOnClose('escapeKeyDown') : true;
            if (doClose) setDialogOpen(false);
        };
        return (
        <section>
            <button onClick={handleCreate}>grid-create</button>
            {dialogOpen && (
                <div role="dialog">
                    {popupEditFormContent}
                    <button onClick={handleDialogClose}>dialog-close</button>
                </div>
            )}
            <div>{title}</div>
            <div data-testid="filter-value">{filter ?? ''}</div>
            {toolbarAdditionalRow}
            {toolbarElementsWithPositions?.map((item, index) => (
                <div key={index} data-testid={`toolbar-element-${index}`}>
                    {item.element}
                </div>
            ))}
            {columns?.map((col: any, index: number) => (
                <div key={index} data-testid={`column-${col.field}`}>
                    {col.field}
                </div>
            ))}
            <button onClick={() => rowAdditionalActions?.[0]?.onClick?.('cfg-1')}>
                {rowAdditionalActions?.[0]?.label}
            </button>
        </section>
        );
    },
    MuiForm: ({
        title,
        children,
    }: {
        title: string;
        children: React.ReactNode;
    }) => (
        <form>
            <h2>{title}</h2>
            {children}
        </form>
    ),
    MuiFilter: ({
        children,
        onDataChange,
        onSpringFilterChange,
        springFilterBuilder: springFilterBuilderProp,
        apiRef,
    }: {
        children: React.ReactNode;
        onDataChange?: (data: any) => void;
        onSpringFilterChange?: (springFilter?: string) => void,
        springFilterBuilder?: (data: any) => string;
        apiRef?: { current: { setFieldValue: (field: string, value: any) => void } };
    }) => {
        const data = { entornApp: { id: 9 } };
        React.useEffect(() => {
            onDataChange?.(data);
            apiRef?.current?.setFieldValue?.('entornApp', { id: 7, description: 'Entorn 7' });
        }, []);
        React.useEffect(() => {
            const filter = springFilterBuilderProp?.(data);
            onSpringFilterChange?.(filter || undefined);
        }, [springFilterBuilderProp]);

        return <div>{children}</div>;
    },
    FormField: ({ name, disabled }: { name: string; disabled?: boolean }) => (
        <div data-testid={`field-${name}`}>{`${name}:${String(disabled ?? false)}`}</div>
    ),
    useFormContext: () => ({
        id: '44',
        isReady: true,
        data: formData,
        fieldErrors: [{ field: 'entornAppId', code: 'NotNull', message: 'Obligatori' }],
        apiRef: {
            current: {
                setFieldValue: mocks.setFieldValueMock,
            }
        }
    }),
    useFormApiRef: () => ({
        current: {
            setFieldValue: mocks.setFieldValueMock,
            save: vi.fn(),
        },
    }),
    useResourceApiService: (resource: string) => {
        if (resource === 'entornApp') {
            return {
                isReady: true,
                getOne: mocks.getOneEntornAppMock,
                find: mocks.findEntornAppsMock,
            };
        }
        return {
            isReady: true,
            artifactAction: mocks.artifactActionMock,
            getOne: mocks.getOneAlarmaConfigMock,
            find: vi.fn().mockResolvedValue({ rows: [] }),
        };
    },
    useBaseAppContext: () => ({
        messageDialogShow: mocks.messageDialogShowMock,
        temporalMessageShow: mocks.temporalMessageShowMock,
        t: mocks.tLibMock,
    }),
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useCloseDialogButtons: () => <button>Tancar</button>,
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshMock,
        },
    }),
    useMuiDataGridProps: (props: any) => props,
    useFilterApiContext: () => ({ current: { clear: vi.fn() } }),
    useFilterApiRef: () => ({
        current: {
            clear: vi.fn(),
            setFieldValue: mocks.setFieldValueMock,
        },
    }),
}});

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../components/UserContext', () => ({
    useIsUserAdmin: () => true,
    useUserContext: () => ({
        user: { codi: 'u001' },
    }),
}));

vi.mock('../components/CenteredCircularProgress.tsx', () => ({
    default: () => <div>Carregant configuració</div>,
}));

// Els botons de filtre de lot2 demanen els textos a l'espai de noms `lot2`: es fixen aquí.
vi.mock('../lot2/i18n', async (importOriginal) => ({
    ...(await importOriginal<typeof import('../lot2/i18n')>()),
    useLot2Translation: () => ({ t: (key: string) => (key === 'neteja' ? 'Netejar' : key) }),
}));

describe('EntornAppSelector', () => {
    beforeEach(() => {
        mocks.getOneEntornAppMock.mockResolvedValue({
            id: 7,
            entornAppDescription: 'Entorn 7',
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EntornAppSelector_quanRepId_carregaLEntornIMapejaElValorAlFormulari', async () => {
        // Comprova que el selector carrega l'entorn existent i el trasllada al formulari intern.
        render(
            <EntornAppSelector
                id={7}
                onEntornAppChange={vi.fn()}
                validationErrors={[]}
            />
        );

        await waitFor(() => {
            expect(mocks.getOneEntornAppMock).toHaveBeenCalledWith(7);
        });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('entornApp', {
            id: 7,
            description: 'Entorn 7',
        });
    });
});

describe('AlarmaConfigForm', () => {
    beforeEach(() => {
        mocks.getOneEntornAppMock.mockResolvedValue({
            id: 7,
            entornAppDescription: 'Entorn 7',
        });
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('AlarmaConfigForm_quanRepDadesDeLatencia_mostraLEditorDeReglesIAvisos', async () => {
        // Verifica que el formulari mostra el nou editor de regles, els avisos i el bloc de període quan hi ha dades associades.
        render(<AlarmaConfigForm id="44" />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Editar configuració' })).toBeInTheDocument();
            expect(screen.getByText('Condició')).toBeInTheDocument();
            expect(screen.getByText('Període')).toBeInTheDocument();
            expect(screen.getByText('Afegir condició')).toBeInTheDocument();
            expect(screen.getByTestId('field-periodeValor')).toBeInTheDocument();
            expect(screen.getByTestId('field-admin')).toHaveTextContent('admin:true');
        });
    });

    it('AlarmaConfigForm_quanCanviaLEntorn_actualitzaLEntornAppIdAlFormulariExtern', async () => {
        // Comprova que el selector d'entorn propaga l'identificador seleccionat al formulari principal.
        render(<AlarmaConfigForm id="44" />);

        await waitFor(() => {
            expect(mocks.setFieldValueMock).toHaveBeenCalledWith('entornAppId', 9);
        });
    });

    it('AlarmaConfigForm_quanEsDesactivaElToggleDePeriode_esborraElsValorsDelsCampsPeriode', async () => {
        render(<AlarmaConfigForm id="44" />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Editar configuració' })).toBeInTheDocument();
        });

        const periodeToggle = screen.getByLabelText(/Configurar període(?!.*inactiu)/);
        expect(periodeToggle).toBeChecked();
        fireEvent.click(periodeToggle);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('periodeValor', null);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('periodeUnitat', null);
        expect(periodeToggle).not.toBeChecked();
    });

    it('AlarmaConfigForm_quanEsDesactivaElToggleDePeriodeInactiu_esborraElsValorsDelsCampsInactiu', async () => {
        render(<AlarmaConfigForm id="44" />);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Editar configuració' })).toBeInTheDocument();
        });

        const periodeInactiuToggle = screen.getByLabelText(/Configurar període inactiu/);
        expect(periodeInactiuToggle).toBeChecked();
        fireEvent.click(periodeInactiuToggle);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('inactiuDesde', null);
        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('inactiuFins', null);
        expect(periodeInactiuToggle).not.toBeChecked();
    });

    it('AlarmaConfigForm_quanEsRenderitza_mostraAmbdosTogglesDePeriode', async () => {
        render(<AlarmaConfigForm id="44" />);

        await waitFor(() => {
            expect(screen.getByLabelText(/Configurar període$/)).toBeInTheDocument();
            expect(screen.getByLabelText(/Configurar període inactiu/)).toBeInTheDocument();
        });
    });

    it('AlarmaConfigForm_quanTeId_correuGenericDepenDeAdmin', async () => {
        render(<AlarmaConfigForm id="44" />);

        await waitFor(() => {
            expect(screen.getByTestId('field-admin')).toBeInTheDocument();
            expect(screen.getByTestId('field-correuGeneric')).toBeInTheDocument();
        });

        expect(screen.getByTestId('field-admin')).toHaveTextContent('admin:true');
        expect(screen.getByTestId('field-correuGeneric')).toHaveTextContent('correuGeneric:true');
    });
});

describe('AlarmaConfig', () => {
    beforeEach(() => {
        mocks.findEntornAppsMock.mockResolvedValue({ rows: [{ id: 1, entornAppDescription: 'Entorn 1' }] });
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.artifactActionMock.mockResolvedValue(undefined);
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('AlarmaConfig_quanEsRenderitza_mostraElGridIElFiltreInicialDeLusuari', async () => {
        // Comprova que la pàgina arrenca filtrant per l'usuari actual i mostra el toggle de Mostrar només les meves alarmes.
        const { container } = render(<AlarmaConfig />);
        await screen.findByRole('heading', { name: 'Configuració d alarmes' });

        // El títol de la graella és un ScreenTitle dins el ScreenFrame de la pàgina.
        const frame = container.querySelector('.screen-frame');
        expect(frame).not.toBeNull();
        const screenTitle = (frame as HTMLElement).querySelector('.screen-title');
        expect(screenTitle).not.toBeNull();
        expect(screenTitle).toHaveTextContent('Configuració d alarmes');
        expect(mocks.lastGridProps.toolbarCreateLabel).toBe('Nova alarma');
        // La graella conserva l'alçada automàtica d'abans (el contingut del marc fa scroll).
        expect(mocks.lastGridProps.autoHeight).toBe(true);

        await waitFor(() => {
            expect(screen.getByRole('heading', { name: 'Configuració d alarmes' })).toBeInTheDocument();
            expect(
                screen.getByTitle("Mostrar només les meves alarmes")
            ).toBeInTheDocument();
        });
    });

    it('AlarmaConfig_quanEsPremElToggle_canviaElFiltreIQuanSElimina_refrescaElGrid', async () => {
        // Verifica que el toggle d'usuari alterna el filtre i que l'acció d'eliminar executa l'artifact corresponent.
        render(<AlarmaConfig />);

        await waitFor(() => {
            expect(screen.getByTitle('Mostrar només les meves alarmes')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByTitle('Mostrar només les meves alarmes'));
        expect(screen.getByTestId('filter-value')).not.toHaveTextContent('');
        expect(screen.getByTitle('Mostra només les alarmes d\'administrador')).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith('cfg-1', { code: 'delete_alarmaConfig' });
        });

        expect(mocks.refreshMock).toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Eliminada', 'success');
    });

    it('AlarmaConfig_quanEsDesactivaElFiltreDeNomésMeva_mostraLaColumnaDeTipusUsuari', async () => {
        render(<AlarmaConfig />);

        await waitFor(() => {
            expect(screen.queryByTestId('column-tipusUsuariAlarma')).not.toBeInTheDocument();
            expect(screen.queryByTestId('column-createdByFullName')).not.toBeInTheDocument();
        });

        fireEvent.click(screen.getByTitle('Mostrar només les meves alarmes'));

        await waitFor(() => {
            expect(screen.getByTestId('column-tipusUsuariAlarma')).toBeInTheDocument();
            expect(screen.getByTestId('column-createdByFullName')).toBeInTheDocument();
        });
    });

    it('AlarmaConfig_quanEsPremElToggle_canviaElFiltreIQuanSElimina_refrescaElGrid', async () => {
        render(<AlarmaConfig />);
        await waitFor(() => {
            expect(screen.getByTitle('Mostrar només les meves alarmes')).toBeInTheDocument();
        });
        fireEvent.click(screen.getByTitle('Mostrar només les meves alarmes'));
        expect(screen.getByTitle("Mostra només les alarmes d'administrador")).toBeInTheDocument();
    });
});

describe('AlarmaConfig en modal', () => {
    beforeEach(() => {
        mocks.findEntornAppsMock.mockResolvedValue({ rows: [{ id: 1, entornAppDescription: 'Entorn 1' }] });
        mocks.getOneEntornAppMock.mockResolvedValue({ id: 7, entornAppDescription: 'Entorn 7' });
        vi.stubGlobal('confirm', mocks.confirmMock);
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('crear una alarma obre un diàleg i no navega', async () => {
        render(<AlarmaConfig />);
        await screen.findByRole('heading', { name: 'Configuració d alarmes' });

        fireEvent.click(screen.getByRole('button', { name: 'grid-create' }));

        const dialog = await screen.findByRole('dialog');
        expect(within(dialog).getByTestId('field-nom')).toBeInTheDocument();
        expect(dialog).toHaveTextContent('Condició');
        expect(mocks.navigateMock).not.toHaveBeenCalled();
        expect(mocks.lastGridProps.toolbarCreateLink).toBeUndefined();
        expect(mocks.lastGridProps.rowUpdateLink).toBeUndefined();
    });

    it('tancar el diàleg amb canvis el tanca sense demanar confirmació (comportament de FormDialog)', async () => {
        // El FormDialog de base-react no té avís de canvis pendents en tancar (el FormBlocker de la
        // pàgina només actua en navegar). Aquest test fixa el comportament actual; l'avís queda pendent.
        render(<AlarmaConfig />);
        await screen.findByRole('heading', { name: 'Configuració d alarmes' });
        fireEvent.click(screen.getByRole('button', { name: 'grid-create' }));
        await screen.findByRole('dialog');

        fireEvent.click(screen.getByRole('button', { name: 'dialog-close' }));

        await waitFor(() => {
            expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        });
        expect(mocks.confirmMock).not.toHaveBeenCalled();
        expect(mocks.lastGridProps.popupEditFormDialogOnClose).toBeUndefined();
    });
});
