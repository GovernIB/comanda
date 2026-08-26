import React from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Entitats from './Entitats';

const mocks = vi.hoisted(() => ({
    artifactActionMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    refreshMock: vi.fn(),
    entitatPermissionShowMock: vi.fn(),
    unitatPermissionShowMock: vi.fn(),
    unitatOnEntryChanged: { current: undefined as ((resourceId: any) => void) | undefined },
    useFormContextValue: {
        data: {},
        apiRef: { current: { setFieldValue: vi.fn() } },
        fieldErrors: [],
    } as any,
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                entitats: {
                    title: 'Entitats',
                    acl: {
                        perm0Allowed: 'Permís 0',
                    },
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
                unitatOrganitzativa: {
                    acl: {
                        perm0Allowed: 'Permís UO',
                    },
                },
            },
            components: {
                permisos: {
                    title: 'Permisos',
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
    FormField: ({ name, label, disabled }: { name: string; label?: string; disabled?: boolean }) => (
        <div data-testid={`field-${name}`} data-disabled={disabled}>
            {label ?? name}
        </div>
    ),
    MuiDataGrid: ({
                      title,
                      rowAdditionalActions,
                      popupEditFormContent,
                      columns,
                  }: {
        title: string;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: unknown, row: any) => void }>;
        popupEditFormContent?: React.ReactNode;
        columns: Array<{ field: string; renderCell?: (params: any) => React.ReactNode }>;
    }) => {
        const mockRow = { id: 15, codi: 'E1', codiDir3: 'D3', numPermisos: 2 };
        return (
            <section>
                <h2>{title}</h2>
                <div data-testid="columns">{columns.map((c) => c.field).join(',')}</div>

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
                        onClick={() => action.onClick?.(mockRow.id, mockRow)}
                    >
                        {action.label}
                    </button>
                ))}
                {popupEditFormContent}
            </section>
        );
    },
    MuiDialog: ({
                    open,
                    title,
                    children,
                    closeCallback,
                }: {
        open: boolean;
        title: string;
        children: React.ReactNode;
        closeCallback?: () => void;
    }) => (
        open ? (
            <div role="dialog" data-testid="organigrama-dialog">
                <h3>{title}</h3>
                <button type="button" onClick={closeCallback}>Tancar</button>
                {children}
            </div>
        ) : null
    ),
    springFilterBuilder: {
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
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
        if (resourceName === 'entitat') {
            return {
                isReady: true,
                artifactAction: mocks.artifactActionMock,
            };
        }
        return {
            isReady: true,
            find: vi.fn(),
        };
    },
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

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../components/AclPermissionManager.tsx', () => ({
    useAclCustomPermissionManager: (config: { resourceType: string, onEntryChanged?: (resourceId: any) => void }) => {
        if (config.resourceType === 'UNITAT') {
            mocks.unitatOnEntryChanged.current = config.onEntryChanged;
        }
        return {
            show: config.resourceType === 'UNITAT' ? mocks.unitatPermissionShowMock : mocks.entitatPermissionShowMock,
            component: <div>{`Gestor permisos ${config.resourceType}`}</div>,
        };
    },
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

describe('Entitats', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.unitatOnEntryChanged.current = undefined;
    });

    it('Entitats_quanEsRenderitza_mostraElTitolLesColumnesILesAccions', () => {
        render(<Entitats />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Entitats');
        expect(screen.getByRole('heading', { name: 'Entitats' })).toBeInTheDocument();
        expect(screen.getByTestId('columns')).toHaveTextContent('codi,nom,codiDir3,cif');
        expect(screen.getByTestId('field-cif')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Permisos' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Refrescar UO' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Organigrama' })).toBeInTheDocument();
        expect(screen.getByText('Gestor permisos ENTITAT')).toBeInTheDocument();
    });

    it('Entitats_quanEsPremPermisos_obreElGestorAmbElCodiDeLaEntitat', () => {
        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Permisos' }));

        expect(mocks.entitatPermissionShowMock).toHaveBeenCalledWith(15, 'E1');
    });

    it('Entitats_quanEsPremRefrescarUO_cridaApiActionIMostraMissatgeExit', async () => {
        mocks.artifactActionMock.mockResolvedValue({});

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Refrescar UO' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledWith(15, { code: 'REFRESH_UO' });
            expect(mocks.refreshMock).toHaveBeenCalled();
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'UO refrescada', 'success');
        });
    });

    it('Entitats_quanFallaRefrescarUO_mostraLErrorDeLApi', async () => {
        mocks.artifactActionMock.mockRejectedValueOnce({ message: 'Error refrescant' });

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Refrescar UO' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Error refrescant', 'error');
        });
    });

    it('Entitats_quanEsPremOrganigrama_obreElDialegAmbElCodiDir3', async () => {
        mocks.artifactActionMock.mockResolvedValue([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null },
        ]);

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(screen.getByTestId('organigrama-dialog')).toBeInTheDocument();
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });
        expect(mocks.artifactActionMock).toHaveBeenCalledWith(15, { code: 'ORGANIGRAMA' });
    });

    it('Entitats_quanNoHiHaUnitatsALOrganigrama_mostraMissatgeError', async () => {
        mocks.artifactActionMock.mockResolvedValue([]);

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'No s\'han trobat unitats', 'error');
        });
    });

    it('Entitats_quanEsCarregaLOrganigrama_mostraUnSpinnerFinsQueArribenLesDades', async () => {
        let resolveAction: (value: any) => void;
        mocks.artifactActionMock.mockReturnValue(new Promise((resolve) => { resolveAction = resolve; }));

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(screen.getByRole('progressbar')).toBeInTheDocument();
        });
        expect(screen.queryByTestId('tree-view')).not.toBeInTheDocument();

        resolveAction!([{ id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null }]);

        await waitFor(() => {
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    });

    it('Entitats_quanEsReobreLOrganigramaDeLaMateixaEntitat_tornaADemanarLesDadesAlBackend', async () => {
        // La cache viu al backend (transparent per al frontend): cada obertura torna a demanar l'acció, ja
        // que el nombre de permisos es calcula en viu i pot haver canviat.
        mocks.artifactActionMock.mockResolvedValue([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null },
        ]);

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));
        await waitFor(() => {
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });
        expect(mocks.artifactActionMock).toHaveBeenCalledTimes(1);

        fireEvent.click(screen.getByRole('button', { name: 'Tancar' }));
        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledTimes(2);
        });
        expect(mocks.artifactActionMock).toHaveBeenNthCalledWith(2, 15, { code: 'ORGANIGRAMA' });
    });

    it('Entitats_quanEsPremPermisosDunNodeDeLOrganigrama_obreElGestorDeUnitats', async () => {
        mocks.artifactActionMock.mockResolvedValue([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null, numPermisos: 1 },
        ]);

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));

        await waitFor(() => {
            expect(screen.getByTestId('tree-item-D3')).toBeInTheDocument();
        });

        const dialog = screen.getByTestId('organigrama-dialog');
        const nodePermissionsButton = dialog.querySelector('button[aria-label="Permisos"]');

        expect(nodePermissionsButton).toBeInTheDocument();
        fireEvent.click(nodePermissionsButton!);

        expect(mocks.unitatPermissionShowMock).toHaveBeenCalledWith(1, 'Unitat arrel');
    });

    it('Entitats_quanEsModifiquenPermisosDunNodeDeLOrganigrama_tornaADemanarLesDadesPerActualitzarElComptador', async () => {
        mocks.artifactActionMock.mockResolvedValue([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null, numPermisos: 1 },
        ]);

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));
        await waitFor(() => {
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });
        expect(mocks.artifactActionMock).toHaveBeenCalledTimes(1);

        // Simulem que useAclCustomPermissionManager avisa que s'ha creat/editat/eliminat un permís de la unitat 1
        act(() => {
            mocks.unitatOnEntryChanged.current?.(1);
        });

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledTimes(2);
        });
    });

    it('Entitats_quanEsModifiquenPermisosDunNodeDeLOrganigrama_noPerdLEstatDelsNodesObertsMentreRecarrega', async () => {
        mocks.artifactActionMock.mockResolvedValueOnce([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null, numPermisos: 1 },
        ]);

        render(<Entitats />);

        fireEvent.click(screen.getByRole('button', { name: 'Organigrama' }));
        await waitFor(() => {
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });

        let resolveReload: (value: any) => void;
        mocks.artifactActionMock.mockReturnValueOnce(new Promise((resolve) => { resolveReload = resolve; }));

        act(() => {
            mocks.unitatOnEntryChanged.current?.(1);
        });

        // Mentre es recarrega en segon pla (per actualitzar el comptador de permisos), l'arbre ha de seguir
        // muntat: no es pot perdre l'estat dels nodes oberts per l'usuari.
        expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();

        resolveReload!([
            { id: 1, codi: 'D3', codiNom: 'Unitat arrel', codiUnitatSuperior: null, numPermisos: 2 },
        ]);

        await waitFor(() => {
            expect(mocks.artifactActionMock).toHaveBeenCalledTimes(2);
        });
        expect(screen.getByTestId('tree-view')).toBeInTheDocument();
    });

    it('Entitats_quanEsRenderitzaElFormulariDEdicio_mostraElsCampsEsperats', () => {
        render(<Entitats />);

        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-nom')).toBeInTheDocument();
        expect(screen.getByTestId('field-codiDir3')).toBeInTheDocument();
        expect(screen.getByTestId('field-cif')).toBeInTheDocument();
    });
});
