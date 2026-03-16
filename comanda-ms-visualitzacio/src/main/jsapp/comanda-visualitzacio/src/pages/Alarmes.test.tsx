import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Alarmes from './Alarmes';

const mocks = vi.hoisted(() => ({
    execMock: vi.fn(),
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
    useMuiActionReportLogic: () => ({
        available: true,
        formDialogComponent: <div>Diàleg alarma</div>,
        exec: mocks.execMock,
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
            <button onClick={() => rowAdditionalActions?.[0]?.onClick('alarma-1')}>
                {rowAdditionalActions?.[0]?.label}
            </button>
            <div data-testid="hidden-active">
                {String(rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: false }))}
            </div>
            <div data-testid="hidden-cleared">
                {String(rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: true }))}
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
        expect(screen.getAllByText('Diàleg alarma')).toHaveLength(2);
        expect(screen.getByTestId('hidden-active')).toHaveTextContent('false');
        expect(screen.getByTestId('hidden-cleared')).toHaveTextContent('true');
        expect(screen.getByTestId('estat-finalitzada')).toHaveTextContent('Finalitzada');
        expect(screen.getByTestId('estat-esborrada')).toHaveTextContent('Finalitzada i esborrada');
    });

    it('Alarmes_quanEsPremElToggle_canviaElFiltreAMostrarTotes', () => {
        // Verifica que el botó de filtre alterna entre només actives i totes les alarmes.
        render(<Alarmes />);

        fireEvent.click(screen.getByTitle('Mostrant només actives'));

        expect(screen.getByTestId('filter-value')).toHaveTextContent("estat in('ACTIVA', 'ESBORRADA')");
    });

    it('Alarmes_quanEsPremLaccioDeFila_executaLaccióDeTancarAlarma', () => {
        // Comprova que l'acció de fila reutilitza la lògica d'acció/report configurada per al mòdul.
        render(<Alarmes />);

        fireEvent.click(screen.getByRole('button', { name: 'Esborrar alarma' }));

        expect(mocks.execMock).toHaveBeenCalledWith('alarma-1');
    });
});
