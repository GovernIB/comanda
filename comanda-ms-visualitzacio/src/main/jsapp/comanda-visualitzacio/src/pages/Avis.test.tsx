import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Avis from './Avis';

let avisFormData = {
    avisPropi: true,
};

const mocks = vi.hoisted(() => ({
    setFieldValueMock: vi.fn((field, value) => {
        avisFormData = { ...avisFormData, [field]: value };
    }),
    clearAppEntornMock: vi.fn(),
    clearMoreMock: vi.fn(),
    filterApiRefCallMock: vi.fn(),
    getColorByAvisTipusMock: vi.fn((tipus: string) => `color-${tipus}`),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                avis: 'Avisos',
            },
            page: {
                message: {
                    grid: {
                        timestamp: 'Data',
                    },
                },
                avisos: {
                    grid: {
                        groupHeader: 'Grup',
                        column: {
                            appEntorn: 'App/Entorn',
                            global: 'Global',
                            globalTooltip: 'Afecta tots els entorns',
                            tipus: {
                                tooltip: {
                                    NOTICIA: 'Notícia',
                                    INFO: 'Informació',
                                    ALERTA: 'Alerta',
                                    ERROR: 'Error',
                                    CRITIC: 'Crític',
                                },
                            },
                        },
                    },
                    action: {
                        obrir: 'Obrir',
                        llegit: { label: 'Llegit' },
                        nollegit: { label: 'No llegit' },
                    },
                    filter: {
                        ownAvisOnlyEnabled: 'Només meus',
                        ownAvisOnlyDisabled: 'Tots els avisos',
                        more: 'Més filtres',
                    },
                },
            },
            components: {
                clear: 'Netejar',
            },
        })
    ),
    tLibMock: vi.fn((key: string) => {
        const values: Record<string, string> = {
            'datacommon.delete.label': 'Eliminar',
        };
        return values[key] ?? key;
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            typeof selector === 'function' ? mocks.tMock(selector) : selector,
    }),
    Trans: ({ i18nKey }: { i18nKey: any }) => (
        <>{typeof i18nKey === 'function' ? mocks.tMock(i18nKey) : i18nKey}</>
    ),
}));

vi.mock('reactlib', () => ({
    MuiDataGrid: ({
        title,
        columns,
        toolbarAdditionalRow,
        toolbarElementsWithPositions,
        rowAdditionalActions,
        filter,
    }: {
        title: string;
        columns: Array<{ field: string; renderCell?: (param: any) => React.ReactNode }>;
        toolbarAdditionalRow?: React.ReactNode;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        rowAdditionalActions?: Array<{ label: string; hidden?: (row: any) => boolean }>;
        filter?: string;
    }) => (
        <section>
            <div>{title}</div>
            <div data-testid="filter-value">{filter ?? ''}</div>
            <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
            <div data-testid="tipus-cell">
                {columns.find((column) => column.field === 'tipus')?.renderCell?.({
                    row: { tipus: 'INFO', global: true },
                })}
            </div>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            <div>{toolbarAdditionalRow}</div>
            <div data-testid="row-action-count">{rowAdditionalActions?.length ?? 0}</div>
            <div data-testid="row-action-hidden">{String(rowAdditionalActions?.[0]?.hidden?.({ url: null }))}</div>
        </section>
    ),
    MuiFilter: ({ children, resourceName, onDataChange }: { children: React.ReactNode; resourceName?: string; onDataChange?: (data: Record<string, unknown>) => void }) => (
        <div>
            <button type="button" onClick={() => onDataChange?.(resourceName === 'entornApp' ? { app: { id: 1 } } : { nom: 'a', tipus: 'b' })}>
                aplica-{resourceName}
            </button>
            {children}
        </div>
    ),
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    springFilterBuilder: {
        and: (...parts: Array<string | undefined | false | null>) => parts.filter((part) => Boolean(part) && !String(part).includes('undefined')).join(' and '),
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        like: (field: string, value: unknown) => `${field}~${String(value)}`,
        gte: (field: string, value: unknown) => `${field}>=${String(value)}`,
        lte: (field: string, value: unknown) => `${field}<=${String(value)}`,
    },
    useFormApiRef: () => ({
        current: {
            setFieldValue: mocks.setFieldValueMock,
        },
    }),
    useFormContext: () => ({
        apiRef: {
            current: {
                setFieldValue: mocks.setFieldValueMock,
            },
        },
        data: avisFormData,
    }),
    useFilterApiContext: () => ({ current: { clear: mocks.clearAppEntornMock } }),
    useFilterApiRef: () => {
        const call = mocks.filterApiRefCallMock.mock.calls.length;
        mocks.filterApiRefCallMock();
        return {
            current: {
                clear: call === 0 ? mocks.clearAppEntornMock : mocks.clearMoreMock,
            },
        };
    },
    useResourceApiContext: () => ({
        indexState: {
            links: {
                has: (rel: string) => rel === 'avis',
            },
        },
    }),
    useResourceApiService: (resourceName: string) => ({
        isReady: true,
        resourceName,
        find: vi.fn().mockResolvedValue({ rows: [] }),
    }),
    useBaseAppContext: () => ({
        t: mocks.tLibMock,
    }),
    useMuiDataGridApiRef: () => ({
        current: {},
    }),
}));

vi.mock('../hooks/treeData', () => ({
    useTreeData: () => ({
        treeView: false,
        treeViewSwitch: <button>Canvia arbre</button>,
        dataGridProps: { treeData: false },
    }),
}));

vi.mock('../util/dateUtils.ts', () => ({
    formatStartOfDay: (value: string) => `START(${value})`,
    formatEndOfDay: (value: string) => `END(${value})`,
}));

vi.mock('@mui/x-data-grid-pro', () => ({
    useGridApiRef: () => ({
        current: {},
    }),
}));

vi.mock('../components/UserContext', () => ({
    useUserContext: () => ({
        user: { codi: 'u001' },
        currentRole: 'ADMIN',
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

vi.mock('../components/salut/SalutChip.tsx', () => ({
    default: ({ label, tooltip }: { label: string; tooltip?: string }) => (
        <div>
            <span>{label}</span>
            <span>{tooltip}</span>
        </div>
    ),
}));

vi.mock('../types/salut.model.tsx', () => ({
    AvisTipusEnum: {
        NOTICIA: 'NOTICIA',
        INFO: 'INFO',
        ALERTA: 'ALERTA',
        ERROR: 'ERROR',
        CRITIC: 'CRITIC',
    },
    useGetColorByAvisTipus: () => mocks.getColorByAvisTipusMock,
}));

vi.mock('../components/UserProvider.tsx', () => ({
    ROLE_ADMIN: 'ADMIN',
}));

vi.mock('../../lib/components/mui/datacommon/MuiDataCommon.tsx', () => ({}));

// Els botons i el recompte de filtres de lot2 demanen els textos a l'espai de noms `lot2`: es fixen aquí.
vi.mock('../lot2/i18n', async (importOriginal) => ({
    ...(await importOriginal<typeof import('../lot2/i18n')>()),
    useLot2Translation: () => ({
        t: (key: string, options?: { count?: number }) => {
            if (key === 'neteja') return 'Netejar';
            if (key === 'obrirFiltreAvancat') return 'Més filtres';
            return options?.count != null ? `${key}:${options.count}` : key;
        },
    }),
}));

describe('Avis', () => {
    afterEach(() => {
        vi.clearAllMocks();
        avisFormData = {
            avisPropi: true,
        };
    });

    it('Avis_quanEsRenderitza_mostraLaGraellaElsXipsIElFiltreInicial', () => {
        // Comprova que la pàgina d'avisos mostra les columnes base, el filtre i els xips de tipus/global.
        const { container } = render(<Avis />);

        // El títol de la graella és un ScreenTitle dins el ScreenFrame de la pàgina.
        const frame = container.querySelector('.screen-frame');
        expect(frame).not.toBeNull();
        const screenTitle = (frame as HTMLElement).querySelector('.screen-title');
        expect(screenTitle).not.toBeNull();
        expect(screenTitle).toHaveTextContent('Avisos');

        expect(screen.getByTestId('page-title')).toHaveTextContent('Avisos');
        expect(screen.getByRole('heading', { name: 'Avisos' })).toBeInTheDocument();
        expect(screen.getByTestId('columns')).toHaveTextContent('logo,app,entorn,nom,descripcio,tipus,responsable,dataInici');
        expect(screen.getByTestId('tipus-cell')).toHaveTextContent('Info');
        expect(screen.getByTestId('tipus-cell')).toHaveTextContent('Global');
        expect(screen.getByText('Canvia arbre')).toBeInTheDocument();
    });

    it('Avis_quanEsPremenElsTogglesIDeNeteja_actualitzaLEstatDelFiltreVisual', () => {
        // Verifica que els toggles del filtre canvien d'estat i que el control de neteja continua disponible.
        const { rerender } = render(<Avis />);

        fireEvent.click(screen.getByTitle('Només meus'));
        rerender(<Avis />);
        expect(screen.getByTitle('Tots els avisos')).toBeInTheDocument();
    });

    it('Avis_quanLusuariEsAdmin_afegeixLaccioDEliminar', () => {
        // Comprova que un administrador veu l'acció addicional d'eliminar al grid.
        render(<Avis />);

        expect(screen.getByTestId('row-action-count')).toHaveTextContent('4');
        expect(screen.getByTestId('row-action-hidden')).toHaveTextContent('true');
    });

    it('Avis_quanEsPremNetejar_sinvocaElsClearsDelFiltre', () => {
        render(<Avis />);
        fireEvent.click(screen.getByTitle('Netejar'));
        expect(mocks.clearAppEntornMock).toHaveBeenCalled();
        expect(mocks.clearMoreMock).toHaveBeenCalled();
    });

    it('Avis_quanLaFilaEsGlobal_mostraElChipGlobal', () => {
        render(<Avis />);

        const tipusCell = screen.getByTestId('tipus-cell');
        expect(tipusCell).toHaveTextContent('Global');
    });

    it('Avis_quanEsAplicaranElsDosFiltres_elRecompteEsLaSumaDelsTermes', () => {
        // FilterCount mostra la SUMA dels termes aplicats pels dos filtres (1 de l'entorn-app i 2 de l'ampliat).
        const { container } = render(<Avis />);

        fireEvent.click(screen.getByRole('button', { name: 'aplica-entornApp' }));
        fireEvent.click(screen.getByRole('button', { name: 'aplica-avis' }));

        expect(container.querySelector('.screen-title')).toHaveTextContent('filtresAplicats:3');
    });
});
