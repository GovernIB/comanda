import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import EstadisticaDashboardEdit from './EstadisticaDashboardEdit';
import translationCa from '../i18n/translationCa';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    navigateMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    goBackMock: vi.fn(),
    showContentDialogMock: vi.fn(),
    showFormDialogMock: vi.fn(),
    useDashboardMock: vi.fn(),
    useDashboardWidgetsMock: vi.fn(),
    useDashboardFiltresMock: vi.fn(),
    useMapDashboardItemsMock: vi.fn(),
    createDashboardItemMock: vi.fn(),
    patchDashboardItemMock: vi.fn(),
    patchDashboardTitolMock: vi.fn(),
    getOneDashboardItemMock: vi.fn(),
    deleteDashboardItemMock: vi.fn(),
    createDashboardTitolMock: vi.fn(),
    getOneDashboardTitolMock: vi.fn(),
    deleteDashboardTitolMock: vi.fn(),
    getOneSimpleWidgetMock: vi.fn(),
    createSimpleWidgetMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    findWidgetsMock: vi.fn(),
    tMock: vi.fn((selector: any, options?: any) => {
        try {
            const mockDict = {
                ...translationCa,
                page: {
                    ...translationCa.page,
                    dashboards: {
                        ...translationCa.page.dashboards,
                        title: 'Dashboards',
                        alert: {
                            tornarLlistat: 'Tornar al llistat',
                            notExists: 'Dashboard inexistent',
                            carregar: 'Error de càrrega',
                        },
                        sideMenu: {
                            filtresTitle: 'Filtres del tauler de control',
                            addFiltre: 'Afegir filtre',
                            noFiltres: 'Cap filtre configurat',
                            periode: 'Període',
                        },
                        editor: {
                            ...translationCa.page.dashboards.editor,
                            expandPanel: 'Expandir panell',
                            collapsePanel: 'Compactar panell',
                        },
                        action: {
                            ...translationCa.page.dashboards.action,
                            patchItem: {
                                success: 'Guardat',
                                error: 'Error guardant',
                                warning: 'Advertiment',
                                saveError: 'Error persistint',
                            },
                        },
                    },
                },
            };
            const res = typeof selector === 'function' ? selector(mockDict) : selector;
            if (typeof res === 'string' && options) {
                return Object.entries(options).reduce(
                    (acc, [k, v]) => acc.replace(new RegExp(`{{${k}}}`, 'g'), String(v)),
                    res
                );
            }
            return res;
        } catch {
            return '';
        }
    }),
}));

vi.mock('react-i18next', () => ({
    initReactI18next: {
        type: '3rdParty',
        init: () => undefined,
    },
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('react-router-dom', () => ({
    useParams: () => mocks.useParamsMock(),
    useNavigate: () => mocks.navigateMock,
}));

vi.mock('reactlib', async (importOriginal) => {
    const original = await importOriginal<typeof import('reactlib')>();

    return {
        ...original,
    BasePage: ({
        toolbar,
        children,
    }: {
        toolbar: React.ReactNode;
        children: React.ReactNode;
    }) => (
        <div>
            <div data-testid="toolbar">{toolbar}</div>
            <div>{children}</div>
        </div>
    ),
    MuiDataGrid: ({
        title,
        rowAdditionalActions,
    }: {
        title: string;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: number) => void }>;
    }) => (
        <section>
            <h2>{title}</h2>
            {rowAdditionalActions?.map((action) => (
                <button key={action.label} type="button" onClick={() => action.onClick?.(5)}>
                    {action.label}
                </button>
            ))}
        </section>
    ),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
        messageDialogShow: mocks.messageDialogShowMock,
        t: (key: string) => key,
        goBack: mocks.goBackMock,
    }),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboardItem') {
            return {
                isReady: true,
                patch: mocks.patchDashboardItemMock,
                create: mocks.createDashboardItemMock,
                find: mocks.findWidgetsMock,
                getOne: mocks.getOneDashboardItemMock,
                delete: mocks.deleteDashboardItemMock,
            };
        }
        if (resourceName === 'dashboardTitol') {
            return {
                isReady: true,
                patch: mocks.patchDashboardTitolMock,
                create: mocks.createDashboardTitolMock,
                find: mocks.findWidgetsMock,
                getOne: mocks.getOneDashboardTitolMock,
                delete: mocks.deleteDashboardTitolMock,
            };
        }
        if (resourceName === 'estadisticaSimpleWidget') {
            return {
                isReady: true,
                getOne: mocks.getOneSimpleWidgetMock,
                create: mocks.createSimpleWidgetMock,
                find: mocks.findWidgetsMock,
            };
        }
        if (resourceName === 'entorn') {
            return {
                isReady: true,
                getOne: vi.fn().mockResolvedValue({ codi: 'ENT_TEST' }),
                find: mocks.findWidgetsMock,
            };
        }
        if (resourceName === 'plantilla') {
            return {
                isReady: true,
                getOne: vi.fn().mockResolvedValue(null),
                find: mocks.findWidgetsMock,
            };
        }
        return {
            isReady: true,
            find: mocks.findWidgetsMock,
            delete: vi.fn(),
            getOne: vi.fn(),
        };
    },
    springFilterBuilder: {
        and: (...values: unknown[]) => values.filter(Boolean).join(' AND '),
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        exists: (value: unknown) => `exists(${String(value)})`,
    },
    FormField: ({
        name,
        type,
    }: {
        name: string;
        type?: string;
    }) => <div data-testid={`field-${name}`}>{`${name}:${type ?? 'default'}`}</div>,
    useFormContext: () => ({
        data: {
            titol: 'Títol',
            subtitol: 'Subtítol',
            mostrarVora: true,
        },
    }),
    useMessageDialogButtons: () => <button>Ok</button>,
    useConfirmDialogButtons: () => <button>Confirmar</button>,
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: vi.fn(),
        },
    }),
    MuiFormDialog: ({ apiRef }: { apiRef?: React.RefObject<any> }) => {
        if (apiRef != null) {
            apiRef.current = {
                show: vi.fn().mockResolvedValue({ id: 5 }),
                close: vi.fn(),
            };
        }
        return null;
    },
}});

vi.mock('../../lib/components/mui/Dialog.tsx', () => ({
    useContentDialog: () => [mocks.showContentDialogMock, <div key="content-dialog" />],
}));

vi.mock('../../lib/components/mui/form/FormDialog.tsx', () => ({
    useFormDialog: () => [mocks.showFormDialogMock, <div key="form-dialog" />],
}));

vi.mock('../hooks/dashboardRequests.ts', () => ({
    useDashboard: (id: string) => mocks.useDashboardMock(id),
    useDashboardWidgets: (id: string) => mocks.useDashboardWidgetsMock(id),
    useDashboardFiltres: (id: string) => mocks.useDashboardFiltresMock(id),
}));

vi.mock('../components/estadistiques/DashboardReactGridLayout.tsx', () => ({
    DashboardReactGridLayout: ({
        dashboardId,
        editable,
        dashboardWidgets,
        onGridLayoutItemsChange,
        onDeleteItem,
        onDuplicateItem,
        backgroundColor,
        largeScreenMode,
    }: {
        dashboardId: number;
        editable: boolean;
        dashboardWidgets?: Array<Record<string, unknown>>;
        onGridLayoutItemsChange?: (items: Array<{ id: number; x: number; y: number; w: number; h: number; type?: string }>) => void;
        onDeleteItem?: (entity: any) => void;
        onDuplicateItem?: (entity: any) => void;
        backgroundColor?: string;
        largeScreenMode?: string;
    }) => (
        <div>
            <div>{`DashboardGrid ${dashboardId} ${String(editable)}`}</div>
            <div data-testid="dashboard-canvas-background-color">{backgroundColor}</div>
            <div data-testid="dashboard-large-screen-mode">{largeScreenMode}</div>
            <div data-testid="dashboard-widgets-json">{JSON.stringify(dashboardWidgets)}</div>
            <button
                type="button"
                onClick={() =>
                    onGridLayoutItemsChange?.([{ id: 1, x: 1, y: 1, w: 4, h: 4 }])
                }
            >
                Moure layout
            </button>
            <button
                type="button"
                onClick={() => onDeleteItem?.({ tipus: 'SIMPLE', dashboardItemId: 1, widgetId: 5 })}
            >
                Eliminar element de test
            </button>
            <button
                type="button"
                onClick={() => onDuplicateItem?.({ tipus: 'SIMPLE', dashboardItemId: 1, widgetId: 5 })}
            >
                Duplicar element de test
            </button>
        </div>
    ),
    useMapDashboardItems: (widgets: unknown[]) => mocks.useMapDashboardItemsMock(widgets),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

vi.mock('../components/CenteredCircularProgress.tsx', () => ({
    default: () => <div>Carregant dashboard</div>,
}));

vi.mock('../components/estadistiques/DashboardEditorSidePanel.tsx', () => ({
    default: ({
        onLiveTitleDataChange,
        onSaved,
    }: {
        onLiveTitleDataChange?: (dashboardTitolId: any, data: any) => void;
        onSaved?: (dashboardItemId?: any) => void;
    }) => (
        <div>
            Editor side panel
            <button type="button" onClick={() => onLiveTitleDataChange?.(2, { colorTitol: '#ff0000' })}>
                Simular edició en viu
            </button>
            <button type="button" onClick={() => onLiveTitleDataChange?.(2, null)}>
                Simular neteja de la previsualització
            </button>
            <button type="button" onClick={() => onSaved?.()}>
                Simular desat de la configuració del dashboard
            </button>
        </div>
    ),
}));

vi.mock('../components/estadistiques/WidgetCreationWizard.tsx', () => ({
    default: ({
        open,
        onCreated,
    }: {
        open: boolean;
        onCreated?: () => void;
    }) =>
        open ? (
            <div>
                <div>Assistent de creació de widgets</div>
                <button type="button" onClick={() => onCreated?.()}>
                    Simular creació completada
                </button>
            </div>
        ) : null,
}));

vi.mock('../AppRoutes.tsx', () => ({
    DASHBOARDS_PATH: 'dashboards',
}));

describe('EstadisticaDashboardEdit', () => {
    beforeEach(() => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12', aplicacio: { id: 1 }, entorn: { id: 2 } },
            loading: false,
            exception: null,
            forceRefresh: vi.fn(),
        });
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1 }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: vi.fn(),
        });
        mocks.useDashboardFiltresMock.mockReturnValue({
            dashboardFiltres: [],
            loading: false,
            forceRefresh: vi.fn(),
        });
        mocks.useMapDashboardItemsMock.mockReturnValue([{ id: 1, x: 0, y: 0, w: 3, h: 3 }]);
        mocks.createDashboardItemMock.mockResolvedValue(undefined);
        mocks.patchDashboardItemMock.mockResolvedValue(undefined);
        mocks.patchDashboardTitolMock.mockResolvedValue(undefined);
        mocks.getOneDashboardItemMock.mockResolvedValue({ id: 1, posX: 0, posY: 3, width: 3, height: 3, entornId: 2, destacat: false });
        mocks.deleteDashboardItemMock.mockResolvedValue(undefined);
        mocks.createDashboardTitolMock.mockResolvedValue({ id: 99 });
        mocks.getOneDashboardTitolMock.mockResolvedValue({ id: 2, titol: 'Títol', posX: 0, posY: 3, width: 6, height: 1 });
        mocks.deleteDashboardTitolMock.mockResolvedValue(undefined);
        mocks.getOneSimpleWidgetMock.mockResolvedValue({ id: 5, titol: 'Widget test', aplicacio: { id: 1 } });
        mocks.createSimpleWidgetMock.mockResolvedValue({ id: 6 });
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.findWidgetsMock.mockResolvedValue({ rows: [{ id: 5, titol: 'Widget test' }] });
        mocks.showContentDialogMock.mockImplementation(() => undefined);
        mocks.showFormDialogMock.mockResolvedValue(undefined);
        vi.spyOn(console, 'error').mockImplementation(() => {});
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaDashboardEdit_quanElDashboardNoExisteix_mostraLAlerta404', () => {
        // Verifica que la pàgina mostra l'alerta de 404 i permet tornar al llistat de dashboards.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: { status: 404 },
        });

        render(<EstadisticaDashboardEdit />);

        fireEvent.click(screen.getByRole('button', { name: 'Tornar al llistat' }));

        expect(screen.getByText('Dashboard inexistent')).toBeInTheDocument();
        expect(mocks.navigateMock).toHaveBeenCalledWith('/dashboards');
    });

    it('EstadisticaDashboardEdit_quanEsEditaUnTitolEnViu_reflecteixElCanviAlCanvasIElRevertEixEnNetejar', async () => {
        // Simula el flux del panell lateral notificant canvis en viu (encara no desats) d'un títol:
        // el canvas els ha de reflectir, i tornar a l'últim estat desat si es descarten (data=null).
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardTitolId: 2, tipus: 'TITOL', titol: 'Títol', colorTitol: '#000000' }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: vi.fn(),
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByTestId('dashboard-widgets-json')).toHaveTextContent('"colorTitol":"#000000"');
        });

        fireEvent.click(screen.getByRole('button', { name: 'Simular edició en viu' }));

        await waitFor(() => {
            expect(screen.getByTestId('dashboard-widgets-json')).toHaveTextContent('"colorTitol":"#ff0000"');
        });

        fireEvent.click(screen.getByRole('button', { name: 'Simular neteja de la previsualització' }));

        await waitFor(() => {
            expect(screen.getByTestId('dashboard-widgets-json')).toHaveTextContent('"colorTitol":"#000000"');
        });
    });

    it('EstadisticaDashboardEdit_quanHiHaDashboard_mostraToolbarIGraellaEditable', async () => {
        // Comprova que la vista d'edició mostra la toolbar principal i el layout editable del dashboard.
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        expect(screen.getByRole('heading', { name: 'Dashboards' })).toBeInTheDocument();
        expect(screen.getByText('Dashboard 12')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Crear component' })).toBeInTheDocument();
        // Al disseny sempre s'aprofita tot l'ample disponible del canvas (a diferència de la visualització,
        // on l'usuari pot triar veure'l a mida real de disseny centrat, vegeu EstadisticaDashboardView).
        expect(screen.getByTestId('dashboard-large-screen-mode')).toHaveTextContent('fit');
    });

    it('EstadisticaDashboardEdit_quanEsDesaLaConfiguracioDelDashboardSenseCapSeleccio_refrescaElDashboard', async () => {
        // Si es canvia p.ex. el color de fons del dashboard i es desa, els canvis s'han d'aplicar sense
        // haver de recarregar la pàgina: cal refrescar el propi dashboard (no només els widgets).
        const forceRefreshDashboard = vi.fn();
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12', aplicacio: { id: 1 }, entorn: { id: 2 } },
            loading: false,
            exception: null,
            forceRefresh: forceRefreshDashboard,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Simular desat de la configuració del dashboard' }));

        expect(forceRefreshDashboard).toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_elPanellEsquerre_noMostraElSelectorDAplicacioIEntorn', async () => {
        // La selecció d'aplicació/entorn s'ha traslladat al panell de propietats (vegeu
        // DashboardEditorSidePanel); el menú lateral esquerre ja no n'ha de mostrar cap selector propi.
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        expect(screen.queryByTestId('field-app')).not.toBeInTheDocument();
        expect(screen.queryByTestId('field-entorn')).not.toBeInTheDocument();
    });

    it('EstadisticaDashboardEdit_quanElDashboardTeColorFonsClar_lAplicaAlCanvasPerDefecte', async () => {
        // Per defecte (tema clar, segons el perfil), s'ha d'aplicar el color de fons clar.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12', aplicacio: { id: 1 }, entorn: { id: 2 }, colorFonsClar: '#123456', colorFonsFosc: '#654321' },
            loading: false,
            exception: null,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        expect(screen.getByTestId('dashboard-canvas-background-color')).toHaveTextContent('#123456');
    });

    it('EstadisticaDashboardEdit_enCanviarElSwitchDeTema_aplicaElColorDeFonsFosc', async () => {
        // El switch de la capçalera commuta la previsualització del disseny entre tema clar i fosc.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12', aplicacio: { id: 1 }, entorn: { id: 2 }, colorFonsClar: '#123456', colorFonsFosc: '#654321' },
            loading: false,
            exception: null,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('switch', { name: 'Mode fosc' }));

        expect(screen.getByTestId('dashboard-canvas-background-color')).toHaveTextContent('#654321');
    });

    it('EstadisticaDashboardEdit_enCanviarElSwitchDeTema_afectaTotaLaPantallaNoNomesElsComponents', async () => {
        // El tema de tota la pantalla de disseny (no només els colors dels widgets) ha de commutar amb el switch.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: { id: 12, titol: 'Dashboard 12', aplicacio: { id: 1 }, entorn: { id: 2 } },
            loading: false,
            exception: null,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        const toolbar = screen.getByTestId('dashboard-editor-toolbar');
        // Tema clar per defecte: la capçalera usa grey[200] (#eeeeee).
        expect(getComputedStyle(toolbar).backgroundColor).toBe('rgb(238, 238, 238)');

        fireEvent.click(screen.getByRole('switch', { name: 'Mode fosc' }));

        // Tema fosc: la capçalera ha de passar a usar grey[900] (#212121), no només els widgets.
        expect(getComputedStyle(toolbar).backgroundColor).toBe('rgb(33, 33, 33)');
    });

    it('EstadisticaDashboardEdit_quanHiHaErrorGeneric_mostraLalertaDeCarrega', () => {
        // Verifica que els errors no-404 mostren el missatge genèric de càrrega fallida.
        mocks.useDashboardMock.mockReturnValue({
            dashboard: null,
            loading: false,
            exception: { status: 500 },
        });

        render(<EstadisticaDashboardEdit />);

        expect(screen.getByText('Error de càrrega')).toBeInTheDocument();
    });

    it('EstadisticaDashboardEdit_quanEsPremCrearComponent_obreLAssistentIRefrescaEnCrear', async () => {
        // Verifica que el botó "Crear component" obre l'assistent de creació i que en completar-se refresca els widgets.
        const forceRefreshMock = vi.fn();
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1 }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: forceRefreshMock,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Crear component' }));

        await waitFor(() => {
            expect(screen.getByText('Assistent de creació de widgets')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByText('Simular creació completada'));

        expect(forceRefreshMock).toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_quanCanviaElLayout_guardaElsCanvisINotificaExit', async () => {
        // Verifica que els canvis de posició del layout es persisteixen i mostren missatge d'èxit.
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Moure layout' }));

        await waitFor(() => {
            expect(mocks.patchDashboardItemMock).toHaveBeenCalledWith(1, {
                data: {
                    posX: 1,
                    posY: 1,
                    width: 4,
                    height: 4,
                },
            });
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Guardat', 'success');
    });

    it('EstadisticaDashboardEdit_quanFallaLaCreacioDunWidget_mostraLError', async () => {
        // Comprova que l'assistent de creació s'obre correctament tot i que la creació posterior pugui fallar
        mocks.createDashboardItemMock.mockRejectedValueOnce(new Error('boom'));

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Crear component' }));

        await waitFor(() => {
            expect(screen.getByText('Assistent de creació de widgets')).toBeInTheDocument();
        });
    });

    it('EstadisticaDashboardEdit_quanFallaElGuardatDelLayout_mostraLError', async () => {
        // Verifica que un error en persistir el layout mostra el missatge d'error corresponent.
        mocks.patchDashboardItemMock.mockRejectedValueOnce(new Error('boom'));

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Moure layout' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Error guardant',
                'error'
            );
        });
    });

    it('EstadisticaDashboardEdit_quanEsContrauUnPanell_esRecordaEntreMuntatges', async () => {
        // L'estat de contret/expandit de cada panell s'ha de recordar entre sessions (localStorage).
        localStorage.clear();
        const { unmount } = render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        const leftToggle = screen.getAllByTitle('Compactar panell')[0];
        fireEvent.click(leftToggle);

        expect(localStorage.getItem('comanda.dashboardEdit.panelCollapsed.left')).toBe('true');
        expect(localStorage.getItem('comanda.dashboardEdit.panelCollapsed.right')).not.toBe('true');

        unmount();
        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });
        // Només queda un botó "Compactar panell" (el dret), ja que l'esquerre s'ha quedat contret.
        expect(screen.getAllByTitle('Compactar panell')).toHaveLength(1);
        expect(screen.getByTitle('Expandir panell')).toBeInTheDocument();
    });

    it('EstadisticaDashboardEdit_quanEsRedimensionaElPanellDret_esGuardaLaMidaAlLocalStorage', async () => {
        // La mida del panell (no només si està contret) també s'ha de recordar entre sessions.
        localStorage.clear();
        const { unmount } = render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        const handle = screen.getByTestId('right-panel-resize-handle');
        fireEvent.mouseDown(handle, { clientX: 500 });
        fireEvent.mouseMove(document, { clientX: 440 }); // arrossegar cap a l'esquerra: panell més ample
        fireEvent.mouseUp(document);

        expect(localStorage.getItem('comanda.dashboardEdit.panelWidth.right')).toBe('500');

        unmount();
        const { container } = render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });
        const restoredHandle = container.querySelector(
            '[data-testid="right-panel-resize-handle"]'
        )?.parentElement as HTMLElement;
        expect(getComputedStyle(restoredHandle).width).toBe('500px');
    });

    it('EstadisticaDashboardEdit_quanEsConfirmaEliminarDesDelMenuContextual_esborraIRefrescaElDashboard', async () => {
        // El menú contextual "Eliminar" ha de demanar confirmació abans d'esborrar.
        const forceRefreshMock = vi.fn();
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1, titol: 'Widget test' }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: forceRefreshMock,
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Eliminar element de test' }));

        await waitFor(() => {
            expect(mocks.deleteDashboardItemMock).toHaveBeenCalledWith(1);
        });
        expect(forceRefreshMock).toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_quanEsCancelaLaConfirmacio_noEsborraRes', async () => {
        mocks.messageDialogShowMock.mockResolvedValue(false);

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Eliminar element de test' }));

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalled();
        });
        expect(mocks.deleteDashboardItemMock).not.toHaveBeenCalled();
    });

    it('EstadisticaDashboardEdit_quanEsPremDuplicarDesDelMenuContextual_creaUnNouWidgetAmbTitolSeqüencial', async () => {
        // "Duplicar" ha de crear un widget nou amb les mateixes dades, afegint un número seqüencial al títol.
        mocks.useDashboardWidgetsMock.mockReturnValue({
            dashboardWidgets: [{ dashboardItemId: 1, titol: 'Widget test' }],
            errorDashboardWidgets: [],
            loadingWidgetPositions: false,
            forceRefresh: vi.fn(),
        });

        render(<EstadisticaDashboardEdit />);

        await waitFor(() => {
            expect(screen.getByText('DashboardGrid 12 true')).toBeInTheDocument();
        });

        fireEvent.click(screen.getByRole('button', { name: 'Duplicar element de test' }));

        await waitFor(() => {
            expect(mocks.createSimpleWidgetMock).toHaveBeenCalledWith({
                data: expect.objectContaining({ titol: 'Widget test (2)', aplicacio: { id: 1 } }),
            });
        });
        expect(mocks.createSimpleWidgetMock.mock.calls[0][0].data.id).toBeUndefined();

        await waitFor(() => {
            expect(mocks.createDashboardItemMock).toHaveBeenCalledWith({
                data: expect.objectContaining({
                    posX: 0,
                    width: 3,
                    height: 3,
                    entornId: 2,
                    dashboard: { id: '12' },
                    widget: { id: 6 },
                }),
            });
        });
        expect(mocks.createDashboardItemMock.mock.calls[0][0].data.posY).toBeUndefined();
        expect(mocks.createDashboardItemMock.mock.calls[0][0].data.id).toBeUndefined();
    });
});
