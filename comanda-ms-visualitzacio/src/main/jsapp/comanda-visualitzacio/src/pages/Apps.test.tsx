import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Apps, { AppForm } from './Apps';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    setMarginsDisabledMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    refreshMock: vi.fn(),
    artifactActionMock: vi.fn(),
    artifactReportMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    permissionShowMock: vi.fn(),
    iniciaDescargaJSONMock: vi.fn(),
    useFormContextValue: {
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
        fieldErrors: [],
    } as any,
    optionalDataGridContextValue: {
        rows: [],
    } as any,
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                apps: {
                    title: 'Aplicacions',
                    general: 'General',
                    entornApp: 'Entorns',
                    update: 'Editar aplicació',
                    create: 'Crear aplicació',
                    action: {
                        export: 'Exportar',
                        import: 'Importar',
                    },
                    import: {
                        success: 'Importació correcta',
                        parseError: 'Error parsejant JSON',
                        noFile: 'Falta fitxer',
                        detectedCodes: 'Codis detectats:',
                        conflict: 'Hi ha conflicte',
                        overwrite: 'Sobreescriure',
                        combine: 'Combinar',
                        skip: 'Ometre',
                    },
                    fields: {
                        salutAuthLegend: 'Auth salut',
                        estadisticaAuthLegend: 'Auth estadística',
                        auth: 'Autenticació',
                        compactable: 'Compactable',
                        compactacioMensualMesos: 'Compactació mensual',
                        eliminacioMesos: 'Eliminació',
                    },
                    tooltips: {
                        compactacioMesos: 'Tooltip compactació',
                        borratMesos: 'Tooltip eliminació',
                    },
                },
                appsEntorns: {
                    title: 'Entorns de l aplicació',
                    resourceTitle: 'Entorn',
                    action: {
                        toolbarActiva: {
                            permisos: 'Permisos',
                            activar: 'Activar',
                            desactivar: 'Desactivar',
                            ok: 'Canvi correcte',
                        },
                    },
                },
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
    useParams: () => mocks.useParamsMock(),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, label }: { name: string; label?: string }) => (
        <div data-testid={`field-${name}`}>{label ?? name}</div>
    ),
    FormPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiActionReportButton: ({
        title,
        onSuccess,
        formDialogContent,
    }: {
        title: string;
        onSuccess?: () => void;
        formDialogContent?: React.ReactNode;
    }) => (
        <div>
            <button type="button" onClick={onSuccess}>
                {title}
            </button>
            {formDialogContent}
        </div>
    ),
    MuiDataGrid: ({
        title,
        rowAdditionalActions,
        toolbarElementsWithPositions,
        popupEditFormContent,
    }: {
        title: string;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id?: unknown) => void }>;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        popupEditFormContent?: React.ReactNode;
    }) => (
        <section>
            <h2>{title}</h2>
            {rowAdditionalActions?.map((action) => (
                <button
                    key={action.label}
                    type="button"
                    onClick={() =>
                        (action.onClick as any)?.(undefined, {
                            entorn: { description: 'PRO' },
                            activa: false,
                        })
                    }
                >
                    {action.label}
                </button>
            ))}
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            {popupEditFormContent}
        </section>
    ),
    MuiForm: ({
        title,
        children,
        goBackLink,
    }: {
        title: string;
        children: React.ReactNode;
        goBackLink: string;
    }) => (
        <form data-back-link={goBackLink}>
            <h1>{title}</h1>
            {children}
        </form>
    ),
    MuiFormTabContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    MuiFormTabs: ({
        tabs,
        children,
    }: {
        tabs: Array<{ label: string }>;
        children: React.ReactNode;
    }) => (
        <div>
            {tabs.map((tab) => (
                <span key={tab.label}>{tab.label}</span>
            ))}
            {children}
        </div>
    ),
    springFilterBuilder: {
        not: vi.fn((value: string) => `NOT(${value})`),
        exists: vi.fn((value: string) => `EXISTS(${value})`),
        eq: vi.fn((field: string, value: string) => `${field}=${value}`),
    },
    useBaseAppContext: () => ({
        setMarginsDisabled: mocks.setMarginsDisabledMock,
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useFormContext: () => mocks.useFormContextValue,
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshMock,
        },
    }),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'entornApp') {
            return {
                artifactAction: mocks.artifactActionMock,
            };
        }
        return {
            artifactReport: mocks.artifactReportMock,
        };
    },
}));

vi.mock('../../lib/util/reactNodePosition.ts', () => ({}));

vi.mock('../../lib/components/mui/datagrid/DataGridContext', () => ({
    useOptionalDataGridContext: () => mocks.optionalDataGridContextValue,
}));

vi.mock('../components/LogoUpload', () => ({
    default: ({ name }: { name: string }) => <div data-testid={`logo-${name}`}>{name}</div>,
}));

vi.mock('../components/FasesCompactacio', () => ({
    default: () => <div>Fases compactació</div>,
}));

vi.mock('../components/UrlPingAdornment', () => ({
    default: () => <div>Ping URL</div>,
}));

vi.mock('../components/AclPermissionManager', () => ({
    useAclPermissionManager: () => ({
        show: mocks.permissionShowMock,
        component: <div>Gestor permisos</div>,
    }),
}));

vi.mock('../util/commonsActions', () => ({
    iniciaDescargaJSON: (...args: unknown[]) => mocks.iniciaDescargaJSONMock(...args),
}));

vi.mock('../../lib/components/mui/datacommon/MuiDataCommon', () => ({}));

vi.mock('../hooks/reordering.tsx', () => ({
    default: () => ({
        dataGridProps: { disableRowSelectionOnClick: true },
        loadingElement: <div>Reordenant</div>,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('AppForm', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.useFormContextValue = {
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };
    });

    it('AppForm_quanEsRenderitzaPerEditar_mostraElTitolIActivaElsMargesReduits', () => {
        // Comprova que el formulari d'edició mostra el títol correcte i desactiva els marges mentre és visible.
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        expect(screen.getByRole('heading', { name: 'Editar aplicació' })).toBeInTheDocument();
        expect(screen.getByTestId('page-title')).toHaveTextContent('Editar aplicació');
        expect(screen.getByText('General')).toBeInTheDocument();
        expect(screen.getByText('Entorns')).toBeInTheDocument();
        expect(mocks.setMarginsDisabledMock).toHaveBeenCalledWith(true);
        expect(screen.getByText('Permisos')).toBeInTheDocument();
        expect(screen.getByText('Activar')).toBeInTheDocument();
        expect(screen.getByText('Desactivar')).toBeInTheDocument();
    });

    it('AppForm_quanEsRenderitzaPerCrear_mostraElsCampsPrincipals', () => {
        // Verifica que en mode alta el formulari manté els camps base i el component del logo.
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<AppForm />);

        expect(screen.getByRole('heading', { name: 'Crear aplicació' })).toBeInTheDocument();
        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-nom')).toBeInTheDocument();
        expect(screen.getAllByTestId('field-activa').length).toBeGreaterThan(0);
        expect(screen.getByTestId('logo-logo')).toBeInTheDocument();
    });

    it('AppForm_quanEsRenderitzaElPopupDEntorn_mostraElsCampsEspecificsDelEntorn', () => {
        // Comprova que el popup d’entorn mostra els camps funcionals propis de configuració i autenticació.
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.useFormContextValue = {
            data: {
                compactable: false,
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };

        render(<AppForm />);

        expect(screen.getByTestId('field-infoUrl')).toBeInTheDocument();
        expect(screen.getByTestId('field-salutUrl')).toBeInTheDocument();
        expect(screen.getByTestId('field-logsUrl')).toBeInTheDocument();
        expect(screen.getByText('Auth salut')).toBeInTheDocument();
        expect(screen.getByText('Auth estadística')).toBeInTheDocument();
        expect(screen.getByTestId('field-compactable')).toBeInTheDocument();
    });

    it('AppForm_quanEsRenderitzaMantéElGoBackEsperat', () => {
        // Comprova que el formulari manté l'enllaç de retorn configurat cap al llistat d'aplicacions.
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        const { container } = render(<AppForm />);

        expect(container.querySelector('form')?.getAttribute('data-back-link')).toBe('/app');
    });
});

describe('Apps', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.optionalDataGridContextValue = { rows: [] };
        mocks.useFormContextValue = {
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };
    });

    it('Apps_quanEsRenderitza_mostraLaGraellaAmbLesAccionsDimportacioIExportacio', () => {
        // Comprova que la pàgina principal d'aplicacions exposa la graella i les accions de toolbar esperades.
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Aplicacions');
        expect(screen.getByRole('heading', { name: 'Aplicacions' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Exportar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Importar' })).toBeInTheDocument();
        expect(screen.getByText('Reordenant')).toBeInTheDocument();
    });

    it('Apps_quanFinalitzaLaImportacio_refrescaLaGraellaIMostraMissatge', () => {
        // Verifica que l'acció d'importació executa el callback d'èxit, refresca la graella i mostra notificació.
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        fireEvent.click(screen.getByRole('button', { name: 'Importar' }));

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Importació correcta',
            'success'
        );
        expect(mocks.refreshMock).toHaveBeenCalled();
    });

    it('Apps_quanSexportaUnaApp_descarregaElJsonIMostraMissatge', async () => {
        // Comprova que l'acció d'exportació delega en l'API, baixa el JSON i notifica l'èxit.
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.artifactReportMock.mockResolvedValue({ fitxer: 'app.json' });

        render(<Apps />);

        fireEvent.click(screen.getByRole('button', { name: 'Exportar' }));

        await waitFor(() => {
            expect(mocks.artifactReportMock).toHaveBeenCalledWith(undefined, {
                code: 'app_export',
                fileType: 'JSON',
            });
        });

        expect(mocks.iniciaDescargaJSONMock).toHaveBeenCalledWith({ fitxer: 'app.json' });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Exportar', 'success');
    });

    it('Apps_quanFallaLexportacio_mostraLErrorRetornatPerLApi', async () => {
        // Verifica que l’error de l’exportació es propaga com a missatge temporal.
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.artifactReportMock.mockRejectedValueOnce(new Error('Export KO'));

        render(<Apps />);

        fireEvent.click(screen.getByRole('button', { name: 'Exportar' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Export KO',
                'error'
            );
        });
    });

    it('Apps_quanElJsonImportatTeConflictes_preseleccionaCombinar', async () => {
        // Comprova que el formulari d'importació detecta codis existents i preselecciona l'estratègia de combinar.
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.optionalDataGridContextValue = {
            rows: [{ codi: 'APP1' }],
        };
        mocks.useFormContextValue = {
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };

        render(<Apps />);

        const input = document.querySelector('input[type="file"]') as HTMLInputElement;
        const file = new File([JSON.stringify([{ codi: 'APP1' }])], 'apps.json', {
            type: 'application/json',
        });

        Object.defineProperty(input, 'files', {
            value: [file],
            configurable: true,
        });
        fireEvent.change(input);

        await waitFor(() => {
            expect(mocks.setFieldValueMock).toHaveBeenCalledWith('decision', 'COMBINE');
        });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith(
            'jsonContent',
            JSON.stringify([{ codi: 'APP1' }])
        );
    });

    it('Apps_quanElJsonImportatEsInvalid_mostraLErrorDeParseig', async () => {
        // Comprova que el formulari d'importació informa quan el JSON pujat no es pot parsejar.
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        const input = document.querySelector('input[type="file"]') as HTMLInputElement;
        const file = new File(['{ invalid json'], 'apps.json', {
            type: 'application/json',
        });

        Object.defineProperty(input, 'files', {
            value: [file],
            configurable: true,
        });
        fireEvent.change(input);

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                '',
                'Error parsejant JSON',
                'error'
            );
        });
    });

    it('Apps_quanElJsonImportatNoTeConflictes_detectaElsCodisSenseMostrarDecisio', async () => {
        // Verifica que la importació mostra els codis detectats però no força cap decisió si no hi ha conflictes.
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.optionalDataGridContextValue = {
            rows: [{ codi: 'APP1' }],
        };
        mocks.useFormContextValue = {
            data: {
                jsonContent: JSON.stringify([{ codi: 'APP2' }]),
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };

        render(<Apps />);

        expect(screen.getByText(/Codis detectats: APP2/)).toBeInTheDocument();
        expect(screen.queryByText('Hi ha conflicte')).not.toBeInTheDocument();
    });

    it('Apps_quanFaltaElFitxerDimportacio_mostraLErrorDeValidacio', () => {
        // Comprova que el formulari mostra l’error específic quan el fitxer és obligatori i no s’ha informat.
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.useFormContextValue = {
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [{ field: 'jsonContent', code: 'NotNull' }],
        };

        render(<Apps />);

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Falta fitxer',
            'error'
        );
    });

    it('Apps_quanHiHaUnErrorDeValidacioPersonalitzat_mostraElMissatgeDelCamp', () => {
        // Verifica que qualsevol error funcional del camp jsonContent es mostra tal com arriba de validació.
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.useFormContextValue = {
            data: {},
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [{ field: 'jsonContent', message: 'JSON invàlid segons l esquema' }],
        };

        render(<Apps />);

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'JSON invàlid segons l esquema',
            'error'
        );
    });

    it('AppForm_quanEsPremPermisosObriElGestorAssociatAlEntorn', () => {
        // Comprova que l’acció de permisos delega al gestor ACL amb la descripció de l’entorn seleccionat.
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        fireEvent.click(screen.getByRole('button', { name: 'Permisos' }));

        expect(mocks.permissionShowMock).toHaveBeenCalledWith(undefined, 'PRO');
    });

    it('AppForm_quanEsCanviaLEstatDEntorn_refrescaILlançaElMissatgeDexit', async () => {
        // Verifica que activar o desactivar un entorn reutilitza l’acció comuna i refresca la graella.
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.artifactActionMock.mockResolvedValue({});

        render(<AppForm />);

        fireEvent.click(screen.getByRole('button', { name: 'Activar' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith(undefined, {
                code: 'toogle_activa',
            });
        });

        expect(mocks.refreshMock).toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Canvi correcte',
            'success'
        );
    });

    it('AppForm_quanFallaElCanviDEstat_mostraLErrorDeLApi', async () => {
        // Comprova que el toggle d’activa mostra el missatge d’error quan l’API el retorna.
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.artifactActionMock.mockRejectedValueOnce({ message: 'No s ha pogut canviar' });

        render(<AppForm />);

        fireEvent.click(screen.getByRole('button', { name: 'Desactivar' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'No s ha pogut canviar',
                'error'
            );
        });
    });
});
