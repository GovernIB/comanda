import React from 'react';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Apps, { AppForm } from './Apps';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    indicadorArtifactActionMock: vi.fn(),
    refreshMock: vi.fn(),
    artifactActionMock: vi.fn(),
    artifactReportMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    entornPermissionShowMock: vi.fn(),
    appPermissionShowMock: vi.fn(),
    iniciaDescargaJSONMock: vi.fn(),
    dialogShowMock: vi.fn(),
    dialogActionShowMock: vi.fn(),
    dialogComponentMock: 'dialog-component',
    isUserAdminMock: vi.fn(() => true),
    readOnlyGestorMock: vi.fn(() => false),
    lastRowAdditionalActions: [] as any[],
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
                    logoAlt: 'Logotip',
                    noLogo: 'Sense logotip',
                    general: 'General',
                    entornApp: 'Entorns',
                    update: 'Editar aplicació',
                    create: 'Crear aplicació',
                    action: {
                        export: 'Exportar',
                        import: 'Importar',
                        netejaEstadistica: 'Neteja estadístiques',
                        sincronitzarCataleg: 'Sincronitzar catàleg',
                    },
                    sincronitzarCataleg: {
                        title: 'Sincronitzar catàleg',
                        confirm: 'Segur que voleu sincronitzar?',
                        success: 'Catàleg sincronitzat correctament',
                    },
                    netejaEstadistica: {
                        label: 'Neteja estadístiques',
                        dialogTitle: 'Reset estadístiques',
                        alertTitle: 'Atenció global',
                        alertDades: 'Esborrarà dades',
                        alertCataleg: 'Esborrarà catàleg',
                        alertWidgets: 'Esborrarà widgets',
                        alertBackupTip: 'Exportar recomanat',
                        abastOptions: {
                            nomesDades: 'Només dades',
                            dadesICataleg: 'Dades i catàleg',
                        },
                        fields: {
                            abast: 'Abast',
                            esborrarWidgets: 'Esborrar widgets',
                            confirmoPerdua: 'Confirmo pèrdua total',
                        },
                        validation: {
                            confirmoRequired: 'Cal confirmar',
                        },
                        confirmButton: 'Reset complet',
                        cancelButton: 'Cancel·lar',
                        success: 'Reset sol·licitat',
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
                        perm2Allowed: "Salut",
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
                        netejaEstadistica: {
                            label: 'Esborrar dades estadístiques',
                            dialogTitle: 'Esborrar dades estadístiques',
                            alertTitle: 'Atenció: Operació irreversible',
                            alertDades: 'Esborrarà totes les dades estadístiques',
                            alertScopeInfo: 'Per esborrar catàleg, anar a App',
                            fields: {
                                esborrarCatalegDisabled: 'Esborrar catàleg (desactivat)',
                                confirmoPerdua: 'Confirmo pèrdua',
                            },
                            validation: {
                                confirmoRequired: 'Cal confirmar',
                            },
                            confirmButton: 'Esborrar dades',
                            cancelButton: 'Cancel·lar',
                            success: 'Neteja sol·licitada correctament',
                        },
                        sincronitzarCataleg: {
                            label: 'Sincronitzar catàleg entorn',
                            title: 'Sincronitzar catàleg',
                            confirm: 'Segur que voleu sincronitzar entorn?',
                            success: 'Catàleg sincronitzat correctament',
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
    FormField: ({ name, label, componentProps, disabled, onChange }: { name: string; label?: string; componentProps?: any; disabled?: boolean; onChange?: any }) => (
        <div data-testid={`field-${name}`} aria-disabled={disabled ? 'true' : undefined}>
            {label ?? name}
            {componentProps?.slotProps?.input?.endAdornment}
            {onChange && (
                <button
                    type="button"
                    data-testid={`change-${name}`}
                    onClick={() => onChange('NOMES_DADES')}
                />
            )}
        </div>
    ),
    FormPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
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
        columns,
        rowAdditionalActions,
        toolbarElementsWithPositions,
        popupEditFormContent,
    }: {
        title: string;
        columns?: Array<{ field: string; renderCell?: (params: any) => React.ReactNode }>;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id?: unknown, row?: any) => void }>;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        popupEditFormContent?: React.ReactNode;
    }) => {
        mocks.lastRowAdditionalActions = rowAdditionalActions ?? [];
        const isAppList = title === 'Aplicacions';
        const mockRow = isAppList
            ? { id: 12, nom: 'Comanda', activa: true, numPermisos: 2 }
            : { id: undefined, entorn: { description: 'PRO' }, activa: false, numPermisos: 1 };

        return (
            <section>
                <h2>{title}</h2>
                {columns?.map((col) => {
                    if (col.renderCell) {
                        return (
                            <div key={col.field} data-testid={`column-render-${col.field}`}>
                                {col.renderCell({ id: mockRow.id, row: mockRow })}
                            </div>
                        );
                    }
                    return null;
                })}

                {rowAdditionalActions?.map((action) => (
                    <button
                        key={action.label}
                        type="button"
                        onClick={() => (action.onClick as any)?.(mockRow.id, mockRow)}
                    >
                        {action.label}
                    </button>
                ))}

                {toolbarElementsWithPositions?.map((entry, index) => (
                    <div key={index}>{entry.element}</div>
                ))}
                {popupEditFormContent}
            </section>
        );
    },
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
        messageDialogShow: mocks.messageDialogShowMock,
    }),
    useConfirmDialogButtons: () => [{ value: false, text: 'Cancel·lar' }, { value: true, text: 'Confirmar' }],
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
        if (resourceName === 'indicador') {
            return {
                artifactAction: mocks.indicadorArtifactActionMock,
            };
        }
        return {
            artifactReport: mocks.artifactReportMock,
        };
    },
    useCloseDialogButtons: () => [{ value: false, text: 'Tancar', componentProps: { variant: 'contained' } }],
    useMuiContentDialog: () => [mocks.dialogShowMock, mocks.dialogComponentMock],
    useMuiFormDialogApiRef: () => ({
        current: {
            show: mocks.dialogActionShowMock,
            close: vi.fn(),
        },
    }),
}));

vi.mock('../components/FormActionDialog.tsx', () => ({
    default: ({ children }: { children: React.ReactNode }) => (
        <div data-testid="form-action-dialog">{children}</div>
    ),
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
    default: () => mocks.readOnlyGestorMock(),
}));

vi.mock('../components/UserContext.ts', () => ({
    useIsUserAdmin: () => mocks.isUserAdminMock(),
}));

vi.mock('@mui/material', async (importOriginal) => {
    const actual = await importOriginal<typeof import('@mui/material')>();
    return {
        ...actual,
        IconButton: ({ children, title, onClick, ...props }: any) => (
            <button
                type="button"
                title={title}
                aria-label={title}
                onClick={onClick}
                {...props}
            >
                {children}
            </button>
        ),
    };
});

vi.mock('@mui/material/Badge', () => ({
    default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

vi.mock('@mui/material/Icon', () => ({
    default: ({ children }: { children: React.ReactNode }) => <span>{children}</span>,
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
        mocks.isUserAdminMock.mockReturnValue(true);
        mocks.readOnlyGestorMock.mockReturnValue(false);
        mocks.lastRowAdditionalActions = [];
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

        expect(screen.getByText('Gestor permisos ENTORN_APP')).toBeInTheDocument();
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

        expect(mocks.entornPermissionShowMock).toHaveBeenCalledWith(undefined, 'PRO');
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

    it('AppForm_quanEsRenderitza_mostraLAccioNetejaEstadisticaIExecutaShowAlClicar', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        const netejaButton = screen.getByText('Esborrar dades estadístiques');
        expect(netejaButton).toBeInTheDocument();

        fireEvent.click(netejaButton);

        expect(mocks.dialogActionShowMock).toHaveBeenCalledWith(undefined, {
            confirmoPerdua: false,
            esborrarCatalegDisabled: false,
            entornDescripcio: 'PRO',
        });
    });

    it('AppForm_quanNoEsAdmin_amagaLAccioNetejaEstadistica', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.isUserAdminMock.mockReturnValue(false);

        render(<AppForm />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.label === 'Esborrar dades estadístiques'
        );
        expect(action).toBeDefined();
        expect(action.hidden()).toBe(true);
    });

    it('AppForm_quanEsGestorReadOnly_amagaLAccioNetejaEstadistica', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.readOnlyGestorMock.mockReturnValue(true);

        render(<AppForm />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.label === 'Esborrar dades estadístiques'
        );
        expect(action).toBeDefined();
        expect(action.hidden()).toBe(true);
    });

    it('AppForm_quanEsRenderitzaElFormulariNeteja_mostraCampConfirmacioICampDesactivatCataleg', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        expect(screen.getByTestId('form-action-dialog')).toBeInTheDocument();
        expect(screen.getByTestId('field-confirmoPerdua')).toBeInTheDocument();
        const disabledField = screen.getByTestId('field-esborrarCatalegDisabled');
        expect(disabledField).toBeInTheDocument();
        expect(disabledField).toHaveAttribute('aria-disabled', 'true');
        expect(screen.queryByTestId('field-abast')).not.toBeInTheDocument();
        expect(screen.queryByTestId('field-esborrarWidgets')).not.toBeInTheDocument();
    });

    it('AppForm_quanEsPremSincronitzarCatalegEntorn_mostraConfirmacioIExecutaAccio', async () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.indicadorArtifactActionMock.mockResolvedValue({ success: true, message: 'Sincronitzat' });

        render(<AppForm />);

        const syncButton = screen.getByRole('button', { name: 'Sincronitzar catàleg entorn' });
        fireEvent.click(syncButton);

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalled();
            expect(mocks.indicadorArtifactActionMock).toHaveBeenCalledWith(null, {
                code: 'sincronitzar_cataleg',
                data: { entornAppId: undefined },
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Sincronitzat', 'success');
        });
    });

    it('AppForm_quanEntornNoEsActiu_amagaSincronitzarCatalegEntorn', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });

        render(<AppForm />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.action === 'sincronitzar_cataleg'
        );
        expect(action).toBeDefined();
        expect(action.hidden({ activa: true })).toBe(false);
        expect(action.hidden({ activa: false })).toBe(true);
    });

    it('AppForm_quanNoEsAdmin_amagaSincronitzarCatalegEntorn', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.isUserAdminMock.mockReturnValue(false);

        render(<AppForm />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.action === 'sincronitzar_cataleg'
        );
        expect(action).toBeDefined();
        expect(action.hidden({ activa: true })).toBe(true);
    });

    it('AppForm_quanEsGestorReadOnly_amagaSincronitzarCatalegEntorn', () => {
        mocks.useParamsMock.mockReturnValue({ id: '12' });
        mocks.readOnlyGestorMock.mockReturnValue(true);

        render(<AppForm />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.action === 'sincronitzar_cataleg'
        );
        expect(action).toBeDefined();
        expect(action.hidden({ activa: true })).toBe(true);
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

    it('Apps_quanEsRenderitzaLaGraella_laColumnaLogoMostraLaTraduccioDinamica', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        expect(screen.getByLabelText('Sense logotip')).toBeInTheDocument();
    });

    it('Apps_quanEsRenderitza_mostraLAccioNetejaEstadisticaIExecutaShowAlClicar', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        const netejaButton = screen.getByRole('button', { name: 'Neteja estadístiques' });
        expect(netejaButton).toBeInTheDocument();

        fireEvent.click(netejaButton);

        expect(mocks.dialogActionShowMock).toHaveBeenCalledWith(12, {
            abast: 'NOMES_DADES',
            esborrarWidgets: false,
            confirmoPerdua: false,
            appNom: 'Comanda',
        });
    });

    it('Apps_quanNoEsAdmin_amagaLAccioNetejaEstadistica', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.isUserAdminMock.mockReturnValue(false);

        render(<Apps />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.label === 'Neteja estadístiques'
        );
        expect(action).toBeDefined();
        expect(action.hidden()).toBe(true);
    });

    it('Apps_quanEsGestorReadOnly_amagaLAccioNetejaEstadistica', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.readOnlyGestorMock.mockReturnValue(true);

        render(<Apps />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.label === 'Neteja estadístiques'
        );
        expect(action).toBeDefined();
        expect(action.hidden()).toBe(true);
    });

    it('Apps_quanEsRenderitzaElFormulariNetejaGlobal_mostraCampsSegonsAbast', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });

        render(<Apps />);

        expect(screen.getByTestId('form-action-dialog')).toBeInTheDocument();
        expect(screen.getByTestId('field-abast')).toBeInTheDocument();
        expect(screen.getByTestId('field-confirmoPerdua')).toBeInTheDocument();
        expect(screen.queryByTestId('field-esborrarWidgets')).not.toBeInTheDocument();
    });

    it('Apps_quanAbastEsCataleg_mostraCampEsborrarWidgetsIRestableixAlCanviar', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.useFormContextValue = {
            data: { abast: 'DADES_I_CATALEG' },
            apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
            fieldErrors: [],
        };

        render(<Apps />);

        expect(screen.getByTestId('field-esborrarWidgets')).toBeInTheDocument();

        const abastChangeBtn = screen.getByTestId('change-abast');
        fireEvent.click(abastChangeBtn);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('esborrarWidgets', false);
    });

    it('Apps_quanEsPremSincronitzarCatalegApp_mostraConfirmacioIExecutaAccio', async () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.indicadorArtifactActionMock.mockResolvedValue({ success: true, message: 'Sincronitzat app' });

        render(<Apps />);

        const syncButton = screen.getByRole('button', { name: 'Sincronitzar catàleg' });
        fireEvent.click(syncButton);

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalled();
            expect(mocks.indicadorArtifactActionMock).toHaveBeenCalledWith(null, {
                code: 'sincronitzar_cataleg',
                data: { appId: 12 },
            });
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Sincronitzat app', 'success');
        });
    });

    it('Apps_quanNoEsAdmin_amagaLAccioSincronitzarCataleg', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.isUserAdminMock.mockReturnValue(false);

        render(<Apps />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.action === 'sincronitzar_cataleg'
        );
        expect(action).toBeDefined();
        expect(action.hidden()).toBe(true);
    });

    it('Apps_quanEsGestorReadOnly_amagaLAccioSincronitzarCataleg', () => {
        mocks.useParamsMock.mockReturnValue({ id: undefined });
        mocks.readOnlyGestorMock.mockReturnValue(true);

        render(<Apps />);

        const action = mocks.lastRowAdditionalActions.find(
            (a: any) => a?.action === 'sincronitzar_cataleg'
        );
        expect(action).toBeDefined();
        expect(action.hidden()).toBe(true);
    });
});

