import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Alarmes from './Alarmes';

const mocks = vi.hoisted(() => ({
    execEsborrarMock: vi.fn(),
    execReactivarMock: vi.fn(),
    refreshMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                alarmes: 'Alarmes',
            },
            page: {
                alarma: {
                    filter: {
                        showOnlyActiveEnabled: 'Mostrant només actives',
                        showOnlyActiveDisabled: 'Mostrant totes',
                    },
                    action: {
                        clear: {
                            label: 'Esborrar alarma',
                        },
                        reactivate: {
                            label: 'Reactivar alarma',
                        },
                    },
                    estats: {
                        finalitzada: 'Finalitzada',
                        finalitzadaEsborrada: 'Finalitzada i esborrada',
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
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.refreshMock,
        },
    }),
    useMuiActionReportLogic: vi.fn((_resourceName, action) => {
        if (action === 'ALARMA_ESBORRAR') {
            return {
                available: true,
                formDialogComponent: <div data-testid="dialog-esborrar">Diàleg esborrar</div>,
                exec: mocks.execEsborrarMock,
            };
        }
        if (action === 'ALARMA_REACTIVAR') {
            return {
                available: true,
                formDialogComponent: <div data-testid="dialog-reactivar">Diàleg reactivar</div>,
                exec: mocks.execReactivarMock,
            };
        }
        return { available: false, formDialogComponent: null, exec: vi.fn() };
    }),
    MuiDataGrid: ({
        title,
        filter,
        toolbarElementsWithPositions,
        rowAdditionalActions,
        columns,
    }: {
        title: string;
        filter: string;
        toolbarElementsWithPositions?: Array<{ element: React.ReactNode }>;
        rowAdditionalActions?: Array<{ label: string; onClick: (id: unknown) => void; hidden?: (row: any) => boolean }>;
        columns: Array<{ field: string; renderCell?: (params: any) => React.ReactNode }>;
    }) => (
        <section>
            <h2>{title}</h2>
            <div data-testid="filter-value">{filter}</div>
            {toolbarElementsWithPositions?.map((entry, index) => (
                <div key={index}>{entry.element}</div>
            ))}
            <button data-testid="btn-esborrar"
                onClick={() => rowAdditionalActions?.[0]?.onClick('alarma-1')}
                disabled={rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: false })}>
                {rowAdditionalActions?.[0]?.label}
            </button>
            <button data-testid="btn-reactivar"
                onClick={() => rowAdditionalActions?.[1]?.onClick('alarma-1')}
                disabled={rowAdditionalActions?.[1]?.hidden?.({ id: '1', dataEsborrat: true })}>
                {rowAdditionalActions?.[1]?.label}
            </button>
            <div data-testid="esborrar-visible-activa">
                {String(!rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: false }))}
            </div>
            <div data-testid="esborrar-visible-esborrada">
                {String(!rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: true }))}
            </div>
            <div data-testid="reactivar-visible-activa">
                {String(!rowAdditionalActions?.[1]?.hidden?.({ id: '1', dataEsborrat: false }))}
            </div>
            <div data-testid="reactivar-visible-esborrada">
                {String(!rowAdditionalActions?.[1]?.hidden?.({ id: '1', dataEsborrat: true }))}
            </div>
            <div data-testid="estat-finalitzada">
                {columns[3]?.renderCell?.({ row: { dataFinalitzacio: '2026-03-13', estat: 'ACTIVA' } })}
            </div>
            <div data-testid="estat-esborrada">
                {columns[3]?.renderCell?.({ row: { dataFinalitzacio: '2026-03-13', estat: 'ESBORRADA' } })}
            </div>
        </section>
    ),
}));

describe('Alarmes', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Alarmes_quanEsRenderitza_mostraElFiltreInicialIElDialegDaccio', () => {
        // Comprova que la pàgina arrenca filtrant només les alarmes actives i mostra els diàlegs d'acció disponibles.
        render(<Alarmes />);

        expect(screen.getByRole('heading', { name: 'Alarmes' })).toBeInTheDocument();
        expect(screen.getByTestId('filter-value')).toHaveTextContent("estat:'ACTIVA'");
        expect(screen.getByTestId('dialog-esborrar')).toBeInTheDocument();
        expect(screen.getByTestId('dialog-reactivar')).toBeInTheDocument();
        expect(screen.getByTestId('esborrar-visible-activa')).toHaveTextContent('true');
        expect(screen.getByTestId('esborrar-visible-esborrada')).toHaveTextContent('false');
        expect(screen.getByTestId('reactivar-visible-activa')).toHaveTextContent('false');
        expect(screen.getByTestId('reactivar-visible-esborrada')).toHaveTextContent('true');
        expect(screen.getByTestId('estat-finalitzada')).toHaveTextContent('Finalitzada');
        expect(screen.getByTestId('estat-esborrada')).toHaveTextContent('Finalitzada i esborrada');
    });

    it('Alarmes_quanEsPremElToggle_canviaElFiltreAMostrarTotes', () => {
        // Verifica que el botó de filtre alterna entre només actives i totes les alarmes.
        render(<Alarmes />);

        fireEvent.click(screen.getByTitle('Mostrant només actives'));

        expect(screen.getByTestId('filter-value')).toHaveTextContent("estat in('ACTIVA', 'ESBORRADA')");
    });

    it('Alarmes_quanEsPremLaccioDesborrar_executaLaccióDesborrarAlarma', () => {
        render(<Alarmes />);

        fireEvent.click(screen.getByTestId('btn-esborrar'));

        expect(mocks.execEsborrarMock).toHaveBeenCalledWith('alarma-1');
        expect(mocks.refreshMock).not.toHaveBeenCalled();
    });

    it('Alarmes_quanEsPremLaccioDeReactivar_executaLaccióDeReactivarAlarma', () => {
        render(<Alarmes />);

        fireEvent.click(screen.getByTestId('btn-reactivar'));

        expect(mocks.execReactivarMock).toHaveBeenCalledWith('alarma-1');
        expect(mocks.refreshMock).not.toHaveBeenCalled();
    });
});
