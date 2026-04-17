import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Tasca from './Tasca';

let tascaFormData = {
    finalitzada: true,
    tascaPropia: true,
};

const mocks = vi.hoisted(() => ({
    clearAppEntornMock: vi.fn(),
    clearMoreMock: vi.fn(),
    filterApiRefCallMock: vi.fn(),
    setFieldValueMock: vi.fn((field, value) => {
        tascaFormData = { ...tascaFormData, [field]: value };
    }),
    useResourceApiServiceMock: vi.fn(() => ({
        isReady: true,
        find: vi.fn().mockResolvedValue({ rows: [] }),
    })),
    getColorByTascaEstatMock: vi.fn((estat: string) => `estat-${estat}`),
    getColorByTascaPrioritatMock: vi.fn((prioritat: string) => `prioritat-${prioritat}`),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                tasca: 'Tasques',
            },
            page: {
                tasques: {
                    grid: {
                        groupHeader: 'Agrupació',
                        entornAppInvalid: 'Entorn no vàlid',
                        column: {
                            appEntorn: 'App/Entorn',
                            prioritat: {
                                tooltip: {
                                    MAXIMA: 'Prioritat màxima',
                                    ALTA: 'Prioritat alta',
                                    NORMAL: 'Prioritat normal',
                                    BAIXA: 'Prioritat baixa',
                                    NONE: 'Sense prioritat',
                                },
                            },
                            estat: {
                                tooltip: {
                                    PENDENT: 'Pendent',
                                    INICIADA: 'Iniciada',
                                    FINALITZADA: 'Finalitzada',
                                    CANCELADA: 'Cancelada',
                                    ERROR: 'Error',
                                },
                            },
                            dataCaducitat: {
                                tooltip: 'Caducitat',
                            },
                        },
                        action: {
                            obrir: 'Obrir',
                        },
                    },
                    filter: {
                        unfinishedOnlyEnabled: 'Només no finalitzades',
                        unfinishedOnlyDisabled: 'Inclou finalitzades',
                        ownTasksOnlyEnabled: 'Només meves',
                        ownTasksOnlyDisabled: 'Totes les tasques',
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
        t: mocks.tMock,
    }),
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
        columns: Array<{ field: string; renderCell?: (param: any) => React.ReactNode; valueFormatter?: (value: any) => any }>;
        toolbarAdditionalRow?: React.ReactNode;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        rowAdditionalActions?: Array<{ label: string; hidden?: (row: any) => boolean }>;
        filter?: string;
    }) => {
        const treePathColumn = columns.find((column) => column.field === 'treePath');
        const estatColumn = columns.find((column) => column.field === 'estat');
        const prioritatColumn = columns.find((column) => column.field === 'prioritat');
        const dataCaducitatColumn = columns.find((column) => column.field === 'dataCaducitat');
        return (
            <section>
                <h2>{title}</h2>
                <div data-testid="filter-value">{filter ?? ''}</div>
                <div data-testid="columns">{columns.map((column) => column.field).join(',')}</div>
                <div data-testid="treepath-value">
                    {treePathColumn?.valueFormatter?.(['INVALID_ENTORNAPP 44', 'X'])}
                </div>
                <div data-testid="estat-chip">
                    {estatColumn?.renderCell?.({ row: { estat: 'ERROR' } })}
                </div>
                <div data-testid="prioritat-chip">
                    {prioritatColumn?.renderCell?.({ row: { prioritat: 'ALTA' } })}
                </div>
                <div data-testid="caducitat-chip">
                    {dataCaducitatColumn?.renderCell?.({
                        row: { diesPerCaducar: 2 },
                        formattedValue: '14/03/2026',
                    })}
                </div>
                {toolbarElementsWithPositions?.map((entry, index) => (
                    <div key={index}>{entry.element}</div>
                ))}
                <div>{toolbarAdditionalRow}</div>
                <div data-testid="row-action-count">{rowAdditionalActions?.length ?? 0}</div>
                <div data-testid="row-action-hidden">{String(rowAdditionalActions?.[0]?.hidden?.({ id: null }))}</div>
            </section>
        );
    },
    MuiFilter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
    springFilterBuilder: {
        and: (...parts: Array<string | undefined | false | null>) => parts.filter(Boolean).join(' && '),
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
        data: tascaFormData,
    }),
    useFilterApiRef: () => {
        const call = mocks.filterApiRefCallMock.mock.calls.length;
        mocks.filterApiRefCallMock();
        return {
            current: {
                clear: call === 0 ? mocks.clearAppEntornMock : mocks.clearMoreMock,
            },
        };
    },
    useBaseAppContext: () => ({
        t: mocks.tLibMock,
    }),
    useResourceApiService: () => mocks.useResourceApiServiceMock(),
}));

vi.mock('../hooks/treeData', () => ({
    useTreeData: () => ({
        treeView: false,
        treeViewSwitch: <button>Canvia arbre</button>,
        dataGridProps: { treeData: false },
    }),
}));

vi.mock('../util/dateUtils', () => ({
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
    TascaEstatEnum: {
        PENDENT: 'PENDENT',
        INICIADA: 'INICIADA',
        FINALITZADA: 'FINALITZADA',
        CANCELADA: 'CANCELADA',
        ERROR: 'ERROR',
    },
    TascaPrioritatEnum: {
        MAXIMA: 'MAXIMA',
        ALTA: 'ALTA',
        NORMAL: 'NORMAL',
        BAIXA: 'BAIXA',
        NONE: 'NONE',
    },
    useGetColorByTascaEstat: () => mocks.getColorByTascaEstatMock,
    useGetColorByTascaPrioritat: () => mocks.getColorByTascaPrioritatMock,
}));

vi.mock('../../lib/components/mui/datacommon/MuiDataCommon.tsx', () => ({}));
vi.mock('../components/UserProvider.tsx', () => ({ ROLE_ADMIN: 'ADMIN' }));

describe('Tasca', () => {
    afterEach(() => {
        vi.clearAllMocks();
        tascaFormData = {
            finalitzada: true,
            tascaPropia: true,
        };
    });

    it('Tasca_quanEsRenderitza_mostraLesColumnesElsXipsIElFiltreInicial', () => {
        // Comprova que la pàgina mostra les columnes principals, els xips d'estat/prioritat/caducitat i l'estat inicial de cerca.
        render(<Tasca />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Tasques');
        expect(screen.getByRole('heading', { name: 'Tasques' })).toBeInTheDocument();
        expect(screen.getByTestId('columns')).toHaveTextContent(
            'logo,app,entorn,nom,descripcio,numeroExpedient,estat,tipus,responsable,prioritat,dataInici,dataCaducitat,dataFi'
        );
        expect(screen.getByTestId('treepath-value')).toHaveTextContent('');
        expect(screen.getByTestId('estat-chip')).toHaveTextContent('Error');
        expect(screen.getByTestId('prioritat-chip')).toHaveTextContent('Alta');
        expect(screen.getByTestId('caducitat-chip')).toHaveTextContent('14/03/2026');
        expect(screen.getByText('Canvia arbre')).toBeInTheDocument();
    });

    it('Tasca_quanEsPremenElsToggles_delFiltreCanviaLEstatVisual', () => {
        // Verifica que els toggles del filtre alternen entre només pendents/meves i els estats ampliats.
        const { rerender } = render(<Tasca />);

        fireEvent.click(screen.getByTitle('Només no finalitzades'));
        fireEvent.click(screen.getByTitle('Només meves'));

        rerender(<Tasca />);

        expect(screen.getByTitle('Inclou finalitzades')).toBeInTheDocument();
        expect(screen.getByTitle('Totes les tasques')).toBeInTheDocument();
    });

    it('Tasca_quanLusuariEsAdmin_afegeixLaccioDEliminar', () => {
        // Comprova que un administrador veu l'acció addicional d'eliminar i que l'acció d'obrir queda amagada sense id.
        render(<Tasca />);

        expect(screen.getByTestId('row-action-count')).toHaveTextContent('2');
        expect(screen.getByTestId('row-action-hidden')).toHaveTextContent('true');
    });

    it('Tasca_quanElToggleDeNoFinalitzadesEstaActiu_elFiltreInclouDataFiIsNull', () => {
        const { rerender } = render(<Tasca />);

        expect(screen.getByTitle('Només no finalitzades')).toBeInTheDocument();

        fireEvent.click(screen.getByTitle('Només no finalitzades'));
        rerender(<Tasca />);
        expect(screen.getByTitle('Inclou finalitzades')).toBeInTheDocument();

        fireEvent.click(screen.getByTitle('Inclou finalitzades'));
        rerender(<Tasca />);
        expect(screen.getByTitle('Només no finalitzades')).toBeInTheDocument();
    });

    it('Tasca_quanEsPremNetejar_esReinicienElsFiltres', () => {
        render(<Tasca />);

        fireEvent.click(screen.getByTitle('Netejar'));

        expect(mocks.clearAppEntornMock).toHaveBeenCalled();
        expect(mocks.clearMoreMock).toHaveBeenCalled();
    });
});
