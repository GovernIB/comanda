import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Apps, { AppForm } from './Apps';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    refreshMock: vi.fn(),
    artifactActionMock: vi.fn(),
    artifactReportMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    entornPermissionShowMock: vi.fn(),
    appPermissionShowMock: vi.fn(),
    iniciaDescargaJSONMock: vi.fn(),
    dialogShowMock: vi.fn(),
    dialogComponentMock: 'dialog-component',
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
            components: {
                permisos: {
                    title: 'Permisos',
                },
            },
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
                        contrasenyaAuthPlaceholder: 'Contrasenya',
                        hidePassword: 'Amagar contrasenya',
                        showPassword: 'Mostrar contrasenya',
                    },
                    tooltips: {
                        compactacioMesos: 'Tooltip compactació',
                        borratMesos: 'Tooltip eliminació',
                    },
                    ping: {
                        validationError: 'Error de validació',
                        validationTrace: 'Traça de validació',
                    },
                },
                appsEntorns: {
                    title: 'Entorns de l aplicació',
                    resourceTitle: 'Entorn',
                    acl: {
                        readAllowed: "Salut",
                        perm0Allowed: "Consulta de taulers de control",
                        perm1Allowed: "Disseny de taulers de control",
                    },
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
    useNavigate: () => vi.fn(),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, label, componentProps }: { name: string; label?: string, componentProps?: any }) => (
        <div data-testid={`field-${name}`}>{label ?? name}{componentProps?.slotProps?.input?.endAdornment}</div>
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
        rowAdditionalActions?: Array<{ label: string; onClick?: (id?: unknown, row?: any) => void }>;
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
                        (action.onClick as any)?.(
                            title === 'Aplicacions' ? 12 : undefined,
                            title === 'Aplicacions'
                                ? { nom: 'Comanda', activa: true }
                                : {
                                    entorn: { description: 'PRO' },
                                    activa: false,
                                }
                        )
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
        onDataChange,
    }: {
        title: string;
        children: React.ReactNode;
        goBackLink: string;
        onDataChange?: (data: any) => void;
    }) => {
        React.useEffect(() => {
            onDataChange?.({ nom: 'Comanda' });
        }, [onDataChange]);
        return (
            <form data-back-link={goBackLink}>
                <h1>{title}</h1>
                {children}
            </form>
        );
    },
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
    useCloseDialogButtons: () => [{ value: false, text: 'Tancar', componentProps: { variant: 'contained' } }],
    useMuiContentDialog: () => [mocks.dialogShowMock, mocks.dialogComponentMock],
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
    default: ({ formData, onClick }: { formData?: any; onClick?: (data: any) => Promise<any> }) => (
        <button data-testid="ping-button" onClick={() => onClick?.(formData || {})}>
            Ping URL
        </button>
    ),
}));

vi.mock('../components/AclPermissionManager', () => ({
    useAclCustomPermissionManager: (config: { resourceType: string }) => ({
        show: config.resourceType === 'APP' ? mocks.appPermissionShowMock : mocks.entornPermissionShowMock,
        component: <div>{`Gestor permisos ${config.resourceType}`}</div>,
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

vi.mock('../hooks/useReadOnlyGestor.ts', () => ({
    default: () => false,
}));

vi.mock('../components/UserContext.ts', () => ({
    useIsUserAdmin: () => true,
}));

vi.mock('../components/ParameterExistsAdornment.tsx', () => ({
    default: ({ value, onClick, disabled }: { value?: string; onClick?: (val: string) => Promise<any>; disabled?: boolean }) => (
        <button
            data-testid="parameter-exists-button"
            disabled={disabled || !value}
            onClick={() => onClick?.(value || '')}
        >
            Verificar paràmetre
        </button>
    ),
}));

vi.mock('../util/exceptionUtils.ts', () => ({
    getErrorMessage: (error: any) => error?.message || 'Error desconegut',
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
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        expect(screen.getByRole('heading', { name: 'Editar aplicació (Comanda)' })).toBeInTheDocument();
        expect(screen.getByTestId('page-title')).toHaveTextContent('Editar aplicació (Comanda)');
        expect(screen.getByText('General')).toBeInTheDocument();
        expect(screen.getByText('Entorns')).toBeInTheDocument();
        expect(screen.getAllByRole('button', { name: 'Permisos' })).toHaveLength(1);
        expect(screen.getByText('Activar')).toBeInTheDocument();
        expect(screen.getByText('Desactivar')).toBeInTheDocument();
        expect(screen.getByText('Permisos')).toBeInTheDocument();
    });

    it('AppForm_quanEsRenderitzaPerCrear_mostraElsCampsPrincipals', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<AppForm />);

        expect(screen.getByRole('heading', { name: 'Crear aplicació' })).toBeInTheDocument();
        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-nom')).toBeInTheDocument();
        expect(screen.getAllByTestId('field-activa').length).toBeGreaterThan(0);
        expect(screen.getByTestId('logo-logo')).toBeInTheDocument();
    });

    it('AppForm_quanEsRenderitzaElPopupDEntorn_mostraElsCampsEspecificsDelEntorn', () => {
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
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        const { container } = render(<AppForm />);

        expect(container.querySelector('form')?.getAttribute('data-back-link')).toBe('/app');
    });

    it('AppForm_quanEsPremPingUrl_cridaApiActionIMostraMissatgeExit', async () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.useFormContextValue.data = { infoUrl: 'http://test.com' };
        mocks.artifactActionMock.mockResolvedValue({ success: true, message: 'Ping correcte' });

        render(<AppForm />);

        const pingButtons = screen.getAllByTestId('ping-button');
        fireEvent.click(pingButtons[0]);

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith(null, {
                code: 'pingUrl',
                data: { infoUrl: 'http://test.com', expectedResponseTypeEnum: 'INFO' }
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Ping correcte', 'success');
        });
    });

    it('AppForm_quanEsPremVerificarParametre_cridaApiActionCorrectament', async () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.useFormContextValue.data = { nomUsuariAuth: 'testUser', parametreAuth: true };
        mocks.artifactActionMock.mockResolvedValue({ exists: true });

        render(<AppForm />);

        const verifyButtons = screen.getAllByTestId('parameter-exists-button');
        fireEvent.click(verifyButtons[0]);

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith(null, {
                code: 'existsParameter',
                data: { parameterValue: 'testUser' }
            });
        });
    });

    it('AppForm_quanEsPremPermisosObriElGestorAssociatAlEntorn', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        fireEvent.click(screen.getByRole('button', { name: 'Permisos' }));

        expect(mocks.appPermissionShowMock).toHaveBeenCalledWith(undefined, 'PRO');
    });

    it('AppForm_quanEsCanviaLEstatDEntorn_refrescaILlançaElMissatgeDexit', async () => {
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
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Aplicacions');
        expect(screen.getByRole('heading', { name: 'Aplicacions' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Exportar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Importar' })).toBeInTheDocument();
        expect(screen.getByText('Reordenant')).toBeInTheDocument();
    });

    it('Apps_quanFinalitzaLaImportacio_refrescaLaGraellaIMostraMissatge', () => {
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
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.artifactReportMock.mockResolvedValue({ fitxer: 'app.json' });

        render(<Apps />);

        fireEvent.click(screen.getByRole('button', { name: 'Exportar' }));

        await waitFor(() => {
            expect(mocks.artifactReportMock).toHaveBeenCalledWith(12, {
                code: 'app_export',
                fileType: 'JSON',
            });
        });

        expect(mocks.iniciaDescargaJSONMock).toHaveBeenCalledWith({ fitxer: 'app.json' });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Exportar', 'success');
    });

    it('Apps_quanFallaLexportacio_mostraLErrorRetornatPerLApi', async () => {
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
            expect(mocks.setFieldValueMock).toHaveBeenCalledWith(
                'jsonContent',
                JSON.stringify([{ codi: 'APP1' }])
            );
        });
    });

    it('Apps_quanElJsonImportatEsInvalid_mostraLErrorDeParseig', async () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        const input = document.querySelector('input[type="file"]') as HTMLInputElement;
        const file = new File(['{ invalid json'], 'apps.json', {
            type: 'application/json',
        });

        vi.spyOn(file, 'text').mockRejectedValue(new Error('Read error'));

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
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.optionalDataGridContextValue = {
            rows: [{ codi: 'APP1' }],
        };
        mocks.useFormContextValue = {
            data: {
                importedAppCodes: ['APP2'],
                importedAppExists: false,
            },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };

        render(<Apps />);

        expect(screen.getByText(/Codis detectats: APP2/)).toBeInTheDocument();
        expect(screen.queryByText('Hi ha conflicte')).not.toBeInTheDocument();
    });

    it('Apps_quanFaltaElFitxerDimportacio_mostraLErrorDeValidacio', () => {
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

    it('Apps_quanEsPremPermisosDalAplicacio_obriElGestorAssociatALapp', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        fireEvent.click(screen.getByRole('button', { name: 'Permisos' }));

        expect(mocks.appPermissionShowMock).toHaveBeenCalledWith(12, 'Comanda');
        expect(screen.getByText('Gestor permisos APP')).toBeInTheDocument();
    });
});
