import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import EstadisticaDashboards from './EstadisticaDashboards';

const mocks = vi.hoisted(() => ({
    temporalMessageShowMock: vi.fn(),
    refreshMock: vi.fn(),
    artifactReportMock: vi.fn(),
    appFindMock: vi.fn(),
    entornFindMock: vi.fn(),
    downloadJsonMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    permissionShowMock: vi.fn(),
    formContextData: {
        entorn: { id: 7 },
        aplicacio: { id: 3 },
        conflicts: [] as Array<{ tipo: string; titol: string; overwrite?: string; nouNom?: string }>,
    },
    tMock: vi.fn((selector: any) =>
        selector({
            components: {
                permisos: {
                    title: 'Permisos',
                },
            },
            page: {
                dashboards: {
                    title: 'Dashboards',
                    edit: 'Editar',
                    dashboardView: 'Veure dashboard',
                    acl: {
                        readAllowed: 'Lectura permesa',
                        writeAllowed: 'Escriptura permesa',
                    },
                    action: {
                        export: 'Exportar',
                        import: {
                            label: 'Importar',
                            title: 'Importar dashboard',
                            success: 'Dashboard importat',
                            selectDecision: 'Selecciona una opció',
                            mantenir: "Mantenir l'existent",
                            sobreescriure: 'Sobreescriure',
                            crearNou: 'Crear nou',
                            nouNom: 'Nom nou',
                            dashboardConflicts: 'Conflictes de tauler',
                            widgetConflicts: 'Conflictes de widget',
                        },
                    },
                    cloneDashboard: {
                        title: 'Clonar dashboard',
                        success: 'Dashboard clonat',
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

vi.mock('reactlib', () => ({
    FormField: ({ name, value, onChange, componentProps }: { name: string; value?: any; onChange?: (value: any) => void; componentProps?: any }) => (
        <input
            data-testid={`field-${name}`}
            name={name}
            value={value ?? ''}
            onChange={(e) => onChange?.(e.target.value)}
            {...componentProps}
        />
    ),
    useFormContext: () => ({
        data: mocks.formContextData,
        apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
    }),
    springFilterBuilder: {
        and: (...values: string[]) => values.join(' AND '),
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        exists: (value: string) => `EXISTS(${value})`,
    },
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshMock,
        },
    }),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboard') {
            return {
                artifactReport: mocks.artifactReportMock,
            };
        }
        if (resourceName === 'app') {
            return {
                isReady: true,
                find: mocks.appFindMock,
            };
        }
        if (resourceName === 'entorn') {
            return {
                isReady: true,
                find: mocks.entornFindMock,
            };
        }
        if (resourceName === 'plantilla') {
            return {
                isReady: true,
                find: vi.fn(),
            };
        }
        return {
            isReady: true,
            find: mocks.entornFindMock,
        };
    },
    MuiDataGrid: ({
                      title,
                      columns,
                      rowAdditionalActions,
                      popupEditFormContent,
                      toolbarElementsWithPositions,
                  }: {
        title: string;
        columns?: Array<{ field: string; renderCell?: (params: any) => React.ReactNode }>;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: number, row: any) => void }>;
        popupEditFormContent?: React.ReactNode;
        toolbarElementsWithPositions?: Array<{ position: number; element: React.ReactNode }>;
    }) => {
        const mockRow = { id: 9, titol: 'Dashboard Test', numPermisos: 2 };
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

                {toolbarElementsWithPositions?.map((item) => (
                    <div key={item.position} data-testid={`toolbar-${item.position}`}>
                        {item.element}
                    </div>
                ))}
                {popupEditFormContent}
                {rowAdditionalActions?.map((action) => (
                    <button
                        key={action.label}
                        onClick={() => action.onClick?.(mockRow.id, mockRow)}
                        type="button"
                    >
                        {action.label}
                    </button>
                ))}
            </section>
        );
    },
}));

vi.mock('../components/FormActionDialog.tsx', () => ({
    default: ({ title, children }: { title: string; children: React.ReactNode }) => (
        <div>
            <h3>{title}</h3>
            {children}
        </div>
    ),
}));

vi.mock('../util/requestUtils.ts', () => ({
    findOptions: vi.fn(() => Promise.resolve([])),
}));

vi.mock('../util/commonsActions.ts', () => ({
    iniciaDescargaJSON: (data: unknown) => mocks.downloadJsonMock(data),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

vi.mock('../components/UserContext.ts', () => ({
    useIsUserAdmin: () => true,
    useIsUserUsuari: () => false,
}));

vi.mock('../components/AclPermissionManager.tsx', () => ({
    useAclCustomPermissionManager: () => ({
        show: mocks.permissionShowMock,
        component: <div>Gestor de permisos DASHBOARD</div>,
    }),
}));

vi.mock('@mui/material/IconButton', () => ({
    default: ({ children, title, onClick, ...props }: any) => (
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
}));

vi.mock('@mui/material/Badge', () => ({
    default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

vi.mock('@mui/material/Icon', () => ({
    default: ({ children }: { children: React.ReactNode }) => <span>{children}</span>,
}));

describe('EstadisticaDashboards', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.formContextData.conflicts = [];
    });

    it('EstadisticaDashboards_quanEsRenderitza_mostraLesAccionsIElDialegDeClonat', () => {
        render(<EstadisticaDashboards />);

        expect(screen.getByRole('heading', { level: 1, name: 'Dashboards' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Editar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Veure dashboard' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Exportar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Clonar dashboard' })).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Clonar dashboard' })).toBeInTheDocument();

        // Comprova que els camps es renderitzen tant al formulari d'edició com al de clonatge
        expect(screen.getAllByTestId('field-titol')).toHaveLength(2);
        expect(screen.getAllByTestId('field-aplicacio')).toHaveLength(2);
    });

    it('EstadisticaDashboards_quanSexportaUnDashboard_descarregaElJsonINotificaExit', async () => {
        let resolveExport: (value: any) => void;
        mocks.artifactReportMock.mockReturnValue(new Promise((resolve) => {
            resolveExport = resolve;
        }));

        render(<EstadisticaDashboards />);

        fireEvent.click(screen.getByRole('button', { name: 'Exportar' }));

        // Comprova que es mostra el progress spinner mentre s'està exportant
        expect(screen.getByRole('progressbar', { hidden: true })).toBeInTheDocument();

        resolveExport!({ ok: true });

        await waitFor(() => {
            expect(mocks.artifactReportMock).toHaveBeenCalledWith(9, {
                code: 'dashboard_export',
                fileType: 'JSON',
            });
        });

        await waitFor(() => {
            expect(screen.queryByRole('progressbar', { hidden: true })).not.toBeInTheDocument();
        });

        expect(mocks.downloadJsonMock).toHaveBeenCalledWith({ ok: true });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Exportar',
            'success'
        );
    });

    it('EstadisticaDashboards_quanEsRenderitza_mostraElBotoDImportarAlToolbar', () => {
        render(<EstadisticaDashboards />);

        // Utilitzar getByRole és més robust que getByTitle per a elements interactius
        const importButton = screen.getByRole('button', { name: 'Importar' });
        expect(importButton).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanHiHaConflictesDeTauler_esMostrenIPermetenTriarUnaDecisio', () => {
        mocks.formContextData.conflicts = [{ tipo: 'DashboardExport', titol: 'Tauler X', overwrite: undefined, nouNom: undefined }];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Conflictes de tauler')).toBeInTheDocument();
        expect(screen.getByText('Tauler X')).toBeInTheDocument();

        const overwriteField = screen.getByTestId('field-conflicts[0].overwrite');
        fireEvent.change(overwriteField, { target: { value: 'Sobreescriure' } });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'DashboardExport', titol: 'Tauler X', overwrite: 'Sobreescriure', nouNom: undefined },
        ]);
    });

    it('EstadisticaDashboards_quanElConflicteDeWidgetTeDecisioCrearNou_mostraElCampDeNomNouIPermetEditarLo', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget X', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: '' },
        ];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Widget X')).toBeInTheDocument();

        const nouNomInput = screen.getByTestId('field-conflicts[0].nouNom');
        fireEvent.change(nouNomInput, { target: { value: 'Widget X (2)' } });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget X', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: 'Widget X (2)' },
        ]);
    });

    it('EstadisticaDashboards_quanEsPremPermisos_obriElGestorDePermisos', () => {
        render(<EstadisticaDashboards />);

        fireEvent.click(screen.getByRole('button', { name: 'Permisos' }));

        expect(mocks.permissionShowMock).toHaveBeenCalledWith(9, 'Dashboard Test');
        expect(screen.getByText('Gestor de permisos DASHBOARD')).toBeInTheDocument();
    });
});
