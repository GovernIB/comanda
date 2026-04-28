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
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                dashboards: {
                    title: 'Dashboards',
                    edit: 'Editar',
                    dashboardView: 'Veure dashboard',
                    action: {
                        export: 'Exportar',
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
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    useFormContext: () => ({
        data: {
            entorn: { id: 7 },
            aplicacio: { id: 3 },
        },
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
        return {
            isReady: true,
            find: mocks.entornFindMock,
        };
    },
    MuiDataGrid: ({
        title,
        rowAdditionalActions,
        popupEditFormContent,
    }: {
        title: string;
        rowAdditionalActions?: Array<{ label: string; onClick?: (id: number) => void }>;
        popupEditFormContent?: React.ReactNode;
    }) => (
        <section>
            <h2>{title}</h2>
            {popupEditFormContent}
            {rowAdditionalActions?.map((action) => (
                <button
                    key={action.label}
                    onClick={() => action.onClick?.(9)}
                    type="button"
                >
                    {action.label}
                </button>
            ))}
        </section>
    ),
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

describe('EstadisticaDashboards', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('EstadisticaDashboards_quanEsRenderitza_mostraLesAccionsIElDialegDeClonat', () => {
        // Comprova que la pàgina exposa les accions de fila principals i el formulari del clonat.
        render(<EstadisticaDashboards />);

        expect(screen.getByRole('heading', { level: 1, name: 'Dashboards' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Editar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Veure dashboard' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Exportar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Clonar dashboard' })).toBeInTheDocument();
        expect(screen.getByRole('heading', { name: 'Clonar dashboard' })).toBeInTheDocument();
        expect(screen.getAllByTestId('field-titol')).toHaveLength(2);
        expect(screen.getAllByTestId('field-aplicacio')).toHaveLength(2);
    });

    it('EstadisticaDashboards_quanSexportaUnDashboard_descarregaElJsonINotificaExit', async () => {
        // Verifica que l'acció d'exportació invoca l'artefacte del dashboard i mostra el missatge d'èxit.
        mocks.artifactReportMock.mockResolvedValue({ ok: true });

        render(<EstadisticaDashboards />);

        fireEvent.click(screen.getByRole('button', { name: 'Exportar' }));

        await waitFor(() => {
            expect(mocks.artifactReportMock).toHaveBeenCalledWith(9, {
                code: 'dashboard_export',
                fileType: 'JSON',
            });
        });

        expect(mocks.downloadJsonMock).toHaveBeenCalledWith({ ok: true });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Exportar',
            'success'
        );
    });
});
