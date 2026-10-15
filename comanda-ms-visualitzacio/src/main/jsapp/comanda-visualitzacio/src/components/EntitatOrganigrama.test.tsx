import { act, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi, beforeEach } from 'vitest';
import { EntitatOrganigrama, useOrganigramaDialog } from './EntitatOrganigrama';

const defaultUnitats = [
    { id: 1, codi: 'ROOT', codiNom: 'Arrel', codiUnitatSuperior: null, numPermisos: 0 },
    { id: 2, codi: 'CHILD1', codiNom: 'Fill 1', codiUnitatSuperior: 'ROOT', numPermisos: 2 },
    { id: 3, codi: 'CHILD2', codiNom: 'Fill 2', codiUnitatSuperior: 'ROOT', numPermisos: 0 },
];

const mocks = vi.hoisted(() => ({
    tMock: vi.fn((selector: any, options?: Record<string, unknown>) => {
        const template = selector({
            page: {
                unitatOrganitzativa: { acl: { perm0Allowed: 'Permís 0' } },
                entitats: { action: { organigrama: { ko: 'Error carregant organigrama', title: 'Organigrama' } } },
            },
            components: { permisos: { title: 'Gestionar permisos' } }
        });
        return Object.entries(options ?? {}).reduce(
            (text: string, [key, value]) => text.split(`{{${key}}}`).join(String(value)),
            template
        );
    }),
    apiActionMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    permissionShowMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: () => ({
        isReady: true,
        artifactAction: mocks.apiActionMock,
    }),
    useBaseAppContext: () => ({
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    FormField: ({ name, label }: any) => <div data-testid={`field-${name}`}>{label || name}</div>,
    MuiDialog: ({ open, children, title }: any) => open ? (
        <div data-testid="dialog">
            <h2>{title}</h2>
            {children}
        </div>
    ) : null,
}));

vi.mock('./AclPermissionManager.tsx', () => ({
    useAclCustomPermissionManager: () => ({
        show: mocks.permissionShowMock,
        component: <div data-testid="permission-component" />,
    }),
}));

vi.mock('@mui/x-tree-view', () => ({
    SimpleTreeView: ({ children, defaultExpandedItems }: any) => (
        <div data-testid="tree-view" data-expanded={defaultExpandedItems?.join(',')}>
            {children}
        </div>
    ),
    TreeItem: ({ itemId, label, children }: any) => (
        <div data-testid={`tree-item-${itemId}`}>
            {label}
            {children}
        </div>
    ),
}));

beforeEach(() => {
    vi.clearAllMocks();
    mocks.apiActionMock.mockResolvedValue(defaultUnitats);
});

afterEach(() => {
    vi.clearAllMocks();
});

describe('EntitatOrganigrama', () => {
    it('mostraSpinnerInicialMentresCarrega', () => {
        mocks.apiActionMock.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
        render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} />);

        expect(screen.getByRole('progressbar')).toBeInTheDocument();
        expect(screen.queryByTestId('tree-view')).not.toBeInTheDocument();
    });

    it('quanCarregaCorrectament_renderitzaLArbreJerarquic', async () => {
        render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} />);

        await waitFor(() => {
            expect(mocks.apiActionMock).toHaveBeenCalledWith(5, { code: 'ORGANIGRAMA' });
        });

        expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
        expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        expect(screen.getByText('Arrel')).toBeInTheDocument();
        expect(screen.getByText('Fill 1')).toBeInTheDocument();
        expect(screen.getByTestId('tree-item-ROOT')).toBeInTheDocument();
        expect(screen.getByTestId('tree-item-CHILD1')).toBeInTheDocument();
    });

    it('quanAPIRetornaBuit_mostraErrorINoRenderitzaNodes', async () => {
        mocks.apiActionMock.mockResolvedValue([]);
        render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} />);

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Error carregant organigrama',
                'error'
            );
        });

        expect(screen.queryByTestId('tree-item-ROOT')).not.toBeInTheDocument();
    });

    it('quanAPIFalla_mostraErrorTecnical', async () => {
        mocks.apiActionMock.mockRejectedValue(new Error('Error de xarxa'));
        render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} />);

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Error de xarxa',
                'error'
            );
        });

        expect(screen.queryByTestId('tree-item-ROOT')).not.toBeInTheDocument();
    });

    it('quanEsPassaPropPermisos_renderitzaBotóAmbBadge', async () => {
        const mockPermisos = vi.fn();
        render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} permisos={mockPermisos} />);

        await waitFor(() => {
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });

        const child1Item = screen.getByTestId('tree-item-CHILD1');
        const button = child1Item.querySelector('button');

        expect(button).toBeInTheDocument();
        expect(button).toHaveAttribute('title', 'Gestionar permisos');
        expect(child1Item).toHaveTextContent('2');
    });

    it('quanEsClicaPermisos_cridaLaFuncioAmbLesDadesDelNode', async () => {
        const mockPermisos = vi.fn();
        render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} permisos={mockPermisos} />);

        await waitFor(() => {
            expect(screen.getByTestId('tree-view')).toBeInTheDocument();
        });

        const child1Item = screen.getByTestId('tree-item-CHILD1');
        const button = child1Item.querySelector('button');

        act(() => {
            button?.click();
        });

        expect(mockPermisos).toHaveBeenCalledWith(2, expect.objectContaining({
            id: 2,
            codi: 'CHILD1',
            codiNom: 'Fill 1'
        }));
    });

    it('quanCanviaReloadToken_tornaACarregarDadesSenseDesmuntarLArbre', async () => {
        const { rerender } = render(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={0} />);

        await waitFor(() => {
            expect(mocks.apiActionMock).toHaveBeenCalledTimes(1);
        });

        rerender(<EntitatOrganigrama entitatId={5} codi="ROOT" reloadToken={1} />);

        await waitFor(() => {
            expect(mocks.apiActionMock).toHaveBeenCalledTimes(2);
        });
        expect(screen.getByTestId('tree-view')).toBeInTheDocument();
    });
});

describe('useOrganigramaDialog', () => {
    const TestWrapper = () => {
        const { handleOpen, handleClose, dialog } = useOrganigramaDialog();
        return (
            <>
                <button onClick={() => handleOpen(10, 'DIR3-CODI')}>Obrir Diàleg</button>
                <button onClick={handleClose}>Tancar Diàleg</button>
                {dialog}
            </>
        );
    };

    it('inicialment_estaTancat', () => {
        render(<TestWrapper />);
        expect(screen.queryByTestId('dialog')).not.toBeInTheDocument();
    });

    it('quanSObre_passaLesDadesCorrectesAlComponent', async () => {
        render(<TestWrapper />);

        act(() => {
            screen.getByText('Obrir Diàleg').click();
        });

        await waitFor(() => {
            expect(screen.getByTestId('dialog')).toBeInTheDocument();
        });
        expect(screen.getByText('Organigrama')).toBeInTheDocument();
    });

    it('quanEsObre_inclouElComponentDePermisos', async () => {
        render(<TestWrapper />);

        act(() => {
            screen.getByText('Obrir Diàleg').click();
        });

        await waitFor(() => {
            expect(screen.getByTestId('dialog')).toBeInTheDocument();
        });
        expect(screen.getByTestId('permission-component')).toBeInTheDocument();
    });
});
