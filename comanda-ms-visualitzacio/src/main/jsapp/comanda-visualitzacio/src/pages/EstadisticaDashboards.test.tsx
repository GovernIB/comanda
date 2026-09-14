import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
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
    dataDispatchActionMock: vi.fn(),
    permissionShowMock: vi.fn(),
    formContextData: {
        entorn: { id: 7 },
        aplicacio: { id: 3 },
        conflicts: [] as Array<{
            tipo: string;
            titol: string;
            overwrite?: string;
            nouNom?: string;
            suggerenciaNouNom?: string;
            bloquejant?: boolean;
            missatgeError?: string;
            codi?: string;
        }>,
        file: undefined as any,
    },
    tMock: vi.fn((selector: any, options?: any) => {
        const res = selector({
            common: {
                cancel: 'Cancel·lar',
            },
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
                            button: 'Importar',
                            conflictsTitle: 'Conflictes',
                            success: 'Dashboard importat',
                            selectDecision: 'Selecciona una opció',
                            mantenir: "Mantenir l'existent",
                            sobreescriure: 'Sobreescriure',
                            crearNou: 'Crear nou',
                            nouNom: 'Nom nou',
                            dashboardConflicts: 'Conflictes de tauler',
                            widgetConflicts: 'Conflictes de widget',
                            analyzing: 'Analitzant fitxer...',
                            noConflicts: 'Sense conflictes',
                            importing: 'Important dashboard...',
                            blockingChip: 'Bloquejant',
                            blockingErrorTitle: "S'han detectat dependències no resoltes",
                            blockingErrorDescription: 'No es pot importar el tauler de control perquè conté dependències que no existeixen a la destinació.',
                            reanalyze: 'Tornar a analitzar',
                            groups: {
                                dashboard: 'Taulers de control',
                                widget: 'Widgets',
                                indicador: 'Indicadors',
                                dimensio: 'Dimensions',
                                plantilla: 'Plantilles',
                                paleta: 'Paletes',
                                other: 'Altres elements',
                            },
                            bulkActions: {
                                selectedCount: '{{count}} seleccionats',
                                useExisting: 'Emprar existent',
                                createWithAnotherName: 'Crear amb un altre nom',
                                overwrite: 'Sobrescriure',
                                selectAll: 'Seleccionar-ho tot',
                                deselectAll: 'Deseleccionar',
                            },
                            warningExistingTitle: 'Elements compartits entre taulers de control',
                            warningExistingDescription: "Els elements amb l'opció \"Emprar existent\" quedaran vinculats entre els taulers. Les modificacions que s'hi facin posteriorment afectaran automàticament tots els taulers de control que els utilitzin.",
                        },
                    },
                    cloneDashboard: {
                        title: 'Clonar dashboard',
                        success: 'Dashboard clonat',
                    },
                },
            },
        });
        if (typeof res === 'string' && options) {
            return Object.entries(options).reduce(
                (str, [k, v]) => str.replace(`{{${k}}}`, String(v)),
                res
            );
        }
        return res;
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    FormField: ({ name, value, onChange, componentProps, hiddenEnumValues }: { name: string; value?: any; onChange?: (value: any) => void; componentProps?: any; hiddenEnumValues?: string[] }) => (
        <input
            data-testid={`field-${name}`}
            name={name}
            defaultValue={value}
            data-hidden-enum-values={hiddenEnumValues ? hiddenEnumValues.join(',') : undefined}
            onChange={(e) => onChange?.(e.target.value)}
            {...componentProps}
        />
    ),
    useFormContext: () => ({
        data: mocks.formContextData,
        fields: [
            { name: 'overwrite' },
            { name: 'nouNom' },
            { name: 'file', onChangeActive: true },
        ],
        apiRef: { current: { setFieldValue: mocks.setFieldValueMock } },
        dataDispatchAction: mocks.dataDispatchActionMock,
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
    default: ({ children, ...props }: any) => <span {...props}>{children}</span>,
}));

describe('EstadisticaDashboards', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.formContextData.conflicts = [];
        mocks.formContextData.file = undefined;
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

        expect(screen.getByText('Conflictes')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'info' })).toBeInTheDocument();
        expect(screen.getByText('Tauler X')).toBeInTheDocument();

        const overwriteField = screen.getByTestId('field-conflicts[0].overwrite');
        fireEvent.change(overwriteField, { target: { value: 'Sobreescriure' } });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'DashboardExport', titol: 'Tauler X', overwrite: 'Sobreescriure', nouNom: undefined },
        ]);
    });

    it('EstadisticaDashboards_quanElConflicteDeWidgetTeDecisioCrearNou_mostraElCampDeNomNouIPermetEditarLo', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget X', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: '', suggerenciaNouNom: 'Widget X (5)' },
        ];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Widget X')).toBeInTheDocument();

        const nouNomInput = screen.getByTestId('field-conflicts[0].nouNom');
        expect(nouNomInput).toHaveAttribute('placeholder', 'Widget X (5)');

        fireEvent.change(nouNomInput, { target: { value: 'Widget X (2)' } });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget X', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: 'Widget X (2)', suggerenciaNouNom: 'Widget X (5)' },
        ]);
    });

    it('EstadisticaDashboards_quanEsPremPermisos_obriElGestorDePermisos', () => {
        render(<EstadisticaDashboards />);

        fireEvent.click(screen.getByRole('button', { name: 'Permisos' }));

        expect(mocks.permissionShowMock).toHaveBeenCalledWith(9, 'Dashboard Test');
        expect(screen.getByText('Gestor de permisos DASHBOARD')).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanSAnalitzaElFitxer_mostraElSpinnerDAnàlisi', () => {
        render(<EstadisticaDashboards />);

        const fileInput = screen.getByTestId('field-file');
        fireEvent.change(fileInput, { target: { value: 'test.json' } });

        expect(screen.getByText('Analitzant fitxer...')).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanElFitxerNoTeConflictes_mostraAlertaSenseConflictes', () => {
        mocks.formContextData.file = { name: 'dashboards.json', content: 'abc' } as any;
        mocks.formContextData.conflicts = [];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Sense conflictes')).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanEsNetejaElFitxer_eliminaElsConflictes', () => {
        render(<EstadisticaDashboards />);

        const fileInput = screen.getByTestId('field-file');
        fireEvent.change(fileInput, { target: { value: 'test.json' } });
        mocks.setFieldValueMock.mockClear();

        fireEvent.change(fileInput, { target: { value: '' } });

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', undefined);
    });

    it('EstadisticaDashboards_quanHiHaDiversosTipusDeConflictes_esMostrenAgrupatsEnElTreeView', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'DashboardExport', titol: 'Dashboard 1' },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1' },
            { tipo: 'IndicadorExport', titol: 'Indicador 1' },
            { tipo: 'PlantillaExport', titol: 'Plantilla 1' },
            { tipo: 'PaletaExport', titol: 'Paleta 1' },
        ];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Taulers de control')).toBeInTheDocument();
        expect(screen.getByText('Widgets')).toBeInTheDocument();
        expect(screen.getByText('Indicadors')).toBeInTheDocument();
        expect(screen.getByText('Plantilles')).toBeInTheDocument();
        expect(screen.getByText('Paletes')).toBeInTheDocument();

        expect(screen.getByText('Dashboard 1')).toBeInTheDocument();
        expect(screen.getByText('Widget 1')).toBeInTheDocument();
        expect(screen.getByText('Indicador 1')).toBeInTheDocument();
        expect(screen.getByText('Plantilla 1')).toBeInTheDocument();
        expect(screen.getByText('Paleta 1')).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanSutilitzenAccionsMassives_aplicaEmprarExistentATotsElsSeleccionats', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'IndicadorExport', titol: 'Indicador 1', overwrite: undefined },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: undefined },
            { tipo: 'PlantillaExport', titol: 'Plantilla 1', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        // Seleccionar-ho tot
        const selectAllButton = screen.getByRole('button', { name: 'Seleccionar-ho tot' });
        fireEvent.click(selectAllButton);

        // Clicar a Emprar existent
        const bulkUseExistingBtn = screen.getByRole('button', { name: /Emprar existent/i });
        expect(bulkUseExistingBtn).not.toBeDisabled();
        fireEvent.click(bulkUseExistingBtn);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'IndicadorExport', titol: 'Indicador 1', overwrite: 'EMPRAR_EXISTENT' },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: 'EMPRAR_EXISTENT' },
            { tipo: 'PlantillaExport', titol: 'Plantilla 1', overwrite: 'EMPRAR_EXISTENT' },
        ]);
    });

    it('EstadisticaDashboards_quanSutilitzenAccionsMassives_aplicaCrearAmbAltreNomATotsElsSeleccionats', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'DashboardExport', titol: 'Dashboard 1', overwrite: undefined },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        // Seleccionar-ho tot
        const selectAllButton = screen.getByRole('button', { name: 'Seleccionar-ho tot' });
        fireEvent.click(selectAllButton);

        // Clicar a Crear amb un altre nom
        const bulkCreateBtn = screen.getByRole('button', { name: /Crear amb un altre nom/i });
        expect(bulkCreateBtn).not.toBeDisabled();
        fireEvent.click(bulkCreateBtn);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'DashboardExport', titol: 'Dashboard 1', overwrite: 'CREAR_AMB_ALTRE_NOM' },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: 'CREAR_AMB_ALTRE_NOM' },
        ]);
    });

    it('EstadisticaDashboards_quanEsPremDeseleccionar_deshabilitaElsBotonsDAccioMassiva', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        const selectAllButton = screen.getByRole('button', { name: 'Seleccionar-ho tot' });
        const deselectAllButton = screen.getByRole('button', { name: 'Deseleccionar' });
        const bulkUseExistingBtn = screen.getByRole('button', { name: /Emprar existent/i });
        const bulkCreateBtn = screen.getByRole('button', { name: /Crear amb un altre nom/i });

        // Inicialment sense selecció
        expect(bulkUseExistingBtn).toBeDisabled();
        expect(bulkCreateBtn).toBeDisabled();

        // Seleccionar tot
        fireEvent.click(selectAllButton);
        expect(bulkUseExistingBtn).not.toBeDisabled();
        expect(bulkCreateBtn).not.toBeDisabled();

        // Deseleccionar tot
        fireEvent.click(deselectAllButton);
        expect(bulkUseExistingBtn).toBeDisabled();
        expect(bulkCreateBtn).toBeDisabled();
    });

    it('EstadisticaDashboards_quanEsSeleccionaUnGrup_seleccionaTotsElsFillsIActualitzaElCompte', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'DashboardExport', titol: 'Dashboard A', overwrite: undefined },
            { tipo: 'DashboardExport', titol: 'Dashboard B', overwrite: undefined },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        // El grup de Taulers de control té 2 fills
        const groupTreeItem = screen.getByRole('treeitem', { name: /Taulers de control/i });
        const groupInput = groupTreeItem.querySelector('input.PrivateSwitchBase-input') as HTMLInputElement;
        expect(groupInput).not.toBeNull();

        // Clic al checkbox del grup
        fireEvent.click(groupInput);

        // El compte de seleccionats ha de ser 2
        expect(screen.getByText('2 seleccionats')).toBeInTheDocument();

        // Ambdós fills han d'estar seleccionats
        const childA = within(groupTreeItem).getByRole('treeitem', { name: /Dashboard A/i });
        const childB = within(groupTreeItem).getByRole('treeitem', { name: /Dashboard B/i });
        expect(childA).toHaveAttribute('aria-checked', 'true');
        expect(childB).toHaveAttribute('aria-checked', 'true');

        // Deseleccionar un fill
        const childAInput = childA.querySelector('input.PrivateSwitchBase-input') as HTMLInputElement;
        fireEvent.click(childAInput);

        // El compte ha de baixar a 1
        expect(screen.getByText('1 seleccionats')).toBeInTheDocument();
        expect(childA).toHaveAttribute('aria-checked', 'false');
        expect(childB).toHaveAttribute('aria-checked', 'true');

        // El grup ha de tenir l'estat indeterminat
        expect(groupTreeItem).toHaveAttribute('aria-checked', 'mixed');

        // Tornar a seleccionar el fill
        fireEvent.click(childAInput);
        expect(screen.getByText('2 seleccionats')).toBeInTheDocument();
        expect(groupTreeItem).toHaveAttribute('aria-checked', 'true');
    });

    it('EstadisticaDashboards_quanHiHaUnTipusDeConflicteDesconegut_mostraEtiquetaAltresElements', () => {
        mocks.formContextData.conflicts = [
            { tipo: '', titol: 'Element Desconegut', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Altres elements')).toBeInTheDocument();
        expect(screen.getByText('Element Desconegut')).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanHiHaConflictesAmbDecisioEmprarExistent_mostraAlertDAvisPlegable', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget Existent', overwrite: 'EMPRAR_EXISTENT' },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget Nou', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: 'Widget Nou (1)' },
        ];

        const { rerender } = render(<EstadisticaDashboards />);

        // Ha de mostrar el títol de l'alerta d'avís
        const alertTitle = screen.getByText('Elements compartits entre taulers de control');
        expect(alertTitle).toBeInTheDocument();

        // Inicialment el botó mostra expand_more
        expect(screen.getByText('expand_more')).toBeInTheDocument();

        // En fer clic sobre el botó d'expandir, canvia a expand_less
        const expandBtn = screen.getByRole('button', { name: 'expand_more' });
        fireEvent.click(expandBtn);
        expect(screen.getByText('expand_less')).toBeInTheDocument();

        // En fer clic sobre el títol, es torna a plegar
        fireEvent.click(alertTitle);
        expect(screen.getByText('expand_more')).toBeInTheDocument();

        // Si tots els conflictes passen a CREAR_AMB_ALTRE_NOM, l'alerta d'avís desapareix
        mocks.formContextData.conflicts = [
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget Existent', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: 'Widget Existent (1)' },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget Nou', overwrite: 'CREAR_AMB_ALTRE_NOM', nouNom: 'Widget Nou (1)' },
        ];
        rerender(<EstadisticaDashboards />);

        expect(screen.queryByText('Elements compartits entre taulers de control')).not.toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanHiHaConflicteBloquejant_mostraAlertaErrorIIndicadorBloquejantSenseAccions', () => {
        mocks.formContextData.conflicts = [
            {
                tipo: 'IndicadorExport',
                titol: 'TAS_CREADES_TOTAL',
                bloquejant: true,
                missatgeError: "Indicador amb codi TAS_CREADES_TOTAL no trobat per a l'aplicació Gestor de tasques a Preproducció",
            },
        ];

        render(<EstadisticaDashboards />);

        // Alerta de dependències no resoltes
        expect(screen.getByText("S'han detectat dependències no resoltes")).toBeInTheDocument();
        expect(screen.getByText('No es pot importar el tauler de control perquè conté dependències que no existeixen a la destinació.')).toBeInTheDocument();

        // Chip de bloquejant i missatge d'error
        expect(screen.getByText('Bloquejant')).toBeInTheDocument();
        expect(screen.getByText("Indicador amb codi TAS_CREADES_TOTAL no trobat per a l'aplicació Gestor de tasques a Preproducció")).toBeInTheDocument();

        // No s'ha de mostrar el selector d'overwrite per a l'element bloquejant
        expect(screen.queryByTestId('field-conflicts[0].overwrite')).not.toBeInTheDocument();

        // L'element del TreeView NO ha d'estar desactivat (sense efecte difuminat)
        const treeItem = screen.getByTestId('conflict-item-0');
        expect(treeItem).not.toHaveAttribute('aria-disabled', 'true');

        // L'element bloquejant NO ha de tenir checkbox
        expect(treeItem.querySelector('input.PrivateSwitchBase-input')).toBeNull();
    });

    it('EstadisticaDashboards_quanEsPremTornarAAnalitzar_disparaAccioFieldChangePerAlFitxer', () => {
        mocks.formContextData.file = new File(['{}'], 'dashboard.json', { type: 'application/json' });
        mocks.formContextData.conflicts = [
            {
                tipo: 'IndicadorExport',
                titol: 'TAS_CREADES_TOTAL',
                bloquejant: true,
                missatgeError: 'Error de dependència',
            },
        ];

        render(<EstadisticaDashboards />);

        const reanalyzeButton = screen.getByRole('button', { name: /Tornar a analitzar/i });
        expect(reanalyzeButton).toBeInTheDocument();

        fireEvent.click(reanalyzeButton);

        expect(mocks.dataDispatchActionMock).toHaveBeenCalledWith(
            expect.objectContaining({
                type: 'FIELD_CHANGE',
                payload: expect.objectContaining({
                    fieldName: 'file',
                    value: mocks.formContextData.file,
                }),
            })
        );
    });

    it('EstadisticaDashboards_quanNoHiHaConflictesBloquejants_noMostraBotoTornarAAnalitzar', () => {
        mocks.formContextData.file = new File(['{}'], 'dashboard.json', { type: 'application/json' });
        mocks.formContextData.conflicts = [
            {
                tipo: 'EstadisticaWidgetExport',
                titol: 'Widget 1',
                bloquejant: false,
            },
        ];

        render(<EstadisticaDashboards />);

        expect(screen.queryByRole('button', { name: /Tornar a analitzar/i })).toBeNull();
    });

    it('EstadisticaDashboards_quanHiHaConflictesBloquejantsINoBloquejants_seleccionarTotIgnoraElsBloquejants', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'DashboardExport', titol: 'Dashboard A', overwrite: undefined },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget A', overwrite: undefined },
            {
                tipo: 'IndicadorExport',
                titol: 'IND_BLOQ',
                bloquejant: true,
                missatgeError: 'Indicador bloquejant',
            },
        ];

        render(<EstadisticaDashboards />);

        // Prémer seleccionar-ho tot
        const selectAllButton = screen.getByRole('button', { name: /Seleccionar-ho tot/i });
        fireEvent.click(selectAllButton);

        // Només 2 elements (els no bloquejants) han de quedar seleccionats
        expect(screen.getByText('2 seleccionats')).toBeInTheDocument();

        // L'element bloquejant no té checkbox seleccionable i no està desactivat
        const blockingTreeItem = screen.getByTestId('conflict-item-2');
        expect(blockingTreeItem.querySelector('input.PrivateSwitchBase-input')).toBeNull();
        expect(blockingTreeItem).not.toHaveAttribute('aria-disabled', 'true');
    });

    it('EstadisticaDashboards_quanHiHaConflicteDeDimensio_mostraElGrupDeDimensions', () => {
        mocks.formContextData.conflicts = [
            {
                tipo: 'DimensioExport',
                titol: 'DIM_ESTAT',
                bloquejant: true,
                missatgeError: 'Dimensió no trobada',
            },
        ];

        render(<EstadisticaDashboards />);

        expect(screen.getByText('Dimensions')).toBeInTheDocument();
        expect(screen.getByText('DIM_ESTAT')).toBeInTheDocument();
        expect(screen.getByText('Dimensió no trobada')).toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanConflicteEsIndicador_ocultaCrearAmbAltreNomIConfiguraHiddenEnumValues', () => {
        mocks.formContextData.conflicts = [
            {
                tipo: 'IndicadorExport',
                titol: 'IND_FORMULA',
                overwrite: 'EMPRAR_EXISTENT',
            },
        ];

        render(<EstadisticaDashboards />);

        const overwriteField = screen.getByTestId('field-conflicts[0].overwrite');
        expect(overwriteField).toHaveAttribute('data-hidden-enum-values', 'CREAR_AMB_ALTRE_NOM');
        expect(screen.queryByTestId('field-conflicts[0].nouNom')).not.toBeInTheDocument();
    });

    it('EstadisticaDashboards_quanConflicteEsDashboard_ocultaEmprarExistentISobrescriure', () => {
        mocks.formContextData.conflicts = [
            {
                tipo: 'DashboardExport',
                titol: 'Tauler 1',
                overwrite: 'CREAR_AMB_ALTRE_NOM',
            },
        ];

        render(<EstadisticaDashboards />);

        const overwriteField = screen.getByTestId('field-conflicts[0].overwrite');
        expect(overwriteField).toHaveAttribute('data-hidden-enum-values', 'EMPRAR_EXISTENT,SOBRESCRIURE');
    });

    it('EstadisticaDashboards_quanConflicteEsWidget_ocultaSobrescriure', () => {
        mocks.formContextData.conflicts = [
            {
                tipo: 'EstadisticaWidgetExport',
                titol: 'Widget 1',
                overwrite: 'EMPRAR_EXISTENT',
            },
        ];

        render(<EstadisticaDashboards />);

        const overwriteField = screen.getByTestId('field-conflicts[0].overwrite');
        expect(overwriteField).toHaveAttribute('data-hidden-enum-values', 'SOBRESCRIURE');
    });

    it('EstadisticaDashboards_quanHiHaIndicadors_mostraBotoSobrescriureIActualitzaConflictesMassivament', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'IndicadorExport', titol: 'Indicador 1', overwrite: undefined },
            { tipo: 'IndicadorExport', titol: 'Indicador 2', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        const selectAllButton = screen.getByRole('button', { name: 'Seleccionar-ho tot' });
        fireEvent.click(selectAllButton);

        const bulkOverwriteBtn = screen.getByRole('button', { name: /Sobrescriure/i });
        expect(bulkOverwriteBtn).not.toBeDisabled();
        fireEvent.click(bulkOverwriteBtn);

        expect(mocks.setFieldValueMock).toHaveBeenCalledWith('conflicts', [
            { tipo: 'IndicadorExport', titol: 'Indicador 1', overwrite: 'SOBRESCRIURE' },
            { tipo: 'IndicadorExport', titol: 'Indicador 2', overwrite: 'SOBRESCRIURE' },
        ]);
    });

    it('EstadisticaDashboards_quanSeleccioEsMixta_deshabilitaBotonsIMostraIconaInfoAmbMissatge', () => {
        mocks.formContextData.conflicts = [
            { tipo: 'DashboardExport', titol: 'Dashboard 1', overwrite: undefined },
            { tipo: 'EstadisticaWidgetExport', titol: 'Widget 1', overwrite: undefined },
            { tipo: 'IndicadorExport', titol: 'Indicador 1', overwrite: undefined },
        ];

        render(<EstadisticaDashboards />);

        const selectAllButton = screen.getByRole('button', { name: 'Seleccionar-ho tot' });
        const deselectAllButton = screen.getByRole('button', { name: 'Deseleccionar' });
        const bulkUseExistingBtn = screen.getByRole('button', { name: /Emprar existent/i });
        const bulkCreateBtn = screen.getByRole('button', { name: /Crear amb un altre nom/i });
        const bulkOverwriteBtn = screen.getByRole('button', { name: /Sobrescriure/i });

        // Inicialment sense selecció: la icona info no es mostra
        expect(screen.queryByTestId('mixed-selection-info')).toBeNull();

        // Seleccionar-ho tot: selecció mixta per a tots els botons (Emprar existent, Crear amb un altre nom, Sobrescriure)
        fireEvent.click(selectAllButton);
        expect(bulkUseExistingBtn).toBeDisabled();
        expect(bulkCreateBtn).toBeDisabled();
        expect(bulkOverwriteBtn).toBeDisabled();

        // La icona d'informació s'ha de mostrar
        const infoIcon = screen.getByTestId('mixed-selection-info');
        expect(infoIcon).toBeInTheDocument();

        // Deseleccionar tot: la icona s'amaga
        fireEvent.click(deselectAllButton);
        expect(screen.queryByTestId('mixed-selection-info')).toBeNull();

        // Seleccionar només l'indicador: tant Emprar existent com Sobrescriure han d'estar habilitats; Crear amb un altre nom no és aplicable a indicadors
        const groupIndicadors = screen.getByRole('treeitem', { name: /Indicadors/i });
        const indicadorItem = within(groupIndicadors).getByRole('treeitem', { name: /Indicador 1/i });
        const indicadorInput = indicadorItem.querySelector('input.PrivateSwitchBase-input') as HTMLInputElement;
        fireEvent.click(indicadorInput);

        expect(bulkUseExistingBtn).not.toBeDisabled();
        expect(bulkCreateBtn).toBeDisabled();
        expect(bulkOverwriteBtn).not.toBeDisabled();
        expect(screen.queryByTestId('mixed-selection-info')).toBeNull();

        // Seleccionar a més el widget:
        // - Sobrescriure esdevé mixt (indicador sí, widget no) -> deshabilitat
        // - Crear amb un altre nom esdevé mixt (widget sí, indicador no) -> deshabilitat
        // - Emprar existent és permès a ambdós -> habilitat
        const widgetInput = screen.getByTestId('field-conflicts[1].overwrite').closest('li')!.querySelector('input.PrivateSwitchBase-input') as HTMLInputElement;
        fireEvent.click(widgetInput);

        expect(bulkUseExistingBtn).not.toBeDisabled();
        expect(bulkCreateBtn).toBeDisabled();
        expect(bulkOverwriteBtn).toBeDisabled();
        expect(screen.getByTestId('mixed-selection-info')).toBeInTheDocument();
    });
});
