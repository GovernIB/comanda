import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Alarmes from './Alarmes';

const mocks = vi.hoisted(() => ({
    execEsborrarMock: vi.fn(),
    execReactivarMock: vi.fn(),
    refreshMock: vi.fn(),
    getTranslations: () => ({
        menu: { alarmes: 'Alarmes' },
        page: {
            alarma: {
                filter: {
                    showOnlyActiveEnabled: 'Mostrant només actives',
                    showOnlyActiveDisabled: 'Mostrant totes',
                },
                action: {
                    clear: { label: 'Esborrar alarma' },
                    clearMultiple: { label: 'Esborrar {{count}} alarmes' },
                    reactivate: { label: 'Reactivar alarma' },
                },
                estats: {
                    finalitzada: 'Finalitzada',
                    finalitzadaEsborrada: 'Finalitzada i esborrada',
                },
            },
        },
    }),
    tMock: vi.fn((selector: any) => selector(mocks.getTranslations())),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({ t: mocks.tMock }),
    Trans: ({ i18nKey, values }: any) => {
        if (typeof i18nKey === 'function') {
            let content = i18nKey(mocks.getTranslations());
            if (values?.count !== undefined) {
                content = content.replace('{{count}}', String(values.count));
            }
            return <>{content}</>;
        }
        return <>{i18nKey}</>;
    },
}));

vi.mock('../../lib/components/mui/datagrid/DataGridContext', () => ({
    DEFAULT_ROW_SELECTION: { ids: new Set<string>() },
}));

vi.mock('reactlib', () => ({
    useMuiDataGridContext: vi.fn(() => ({
        selection: { ids: new Set<string>() },
        apiRef: { current: { refresh: vi.fn() } },
    })),
    useMuiDataGridApiRef: () => ({ current: { refresh: mocks.refreshMock } }),
    springFilterBuilder: {
        and: (...filters: string[]) => filters.filter(f => !!f).join(' AND '),
    },
    MuiActionReportButton: ({
        action,
        selectedCount,
        disabled,
        buttonComponentProps,
        formDialogContent,
    }: any) => {
        const isMultipleDelete = action === 'ALARMA_ESBORRAR_MULTIPLE';
        return (
            <button
                data-testid={isMultipleDelete ? 'btn-esborrar-multiple' : 'btn-action-report'}
                data-action={action}
                disabled={disabled || buttonComponentProps?.disabled}
                title={`Count: ${selectedCount}`}
            >
                {formDialogContent}
            </button>
        );
    },
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
            <div data-testid="toolbar-container">
                {toolbarElementsWithPositions?.map((entry: any, index: number) => (
                    <div key={index} data-testid={`toolbar-element-${index}`}>
                        {entry.element}
                    </div>
                ))}
            </div>
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
            <div data-testid="esborrar-visible-activa">{String(!rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: false }))}</div>
            <div data-testid="esborrar-visible-esborrada">{String(!rowAdditionalActions?.[0]?.hidden?.({ id: '1', dataEsborrat: true }))}</div>
            <div data-testid="reactivar-visible-activa">{String(!rowAdditionalActions?.[1]?.hidden?.({ id: '1', dataEsborrat: false }))}</div>
            <div data-testid="reactivar-visible-esborrada">{String(!rowAdditionalActions?.[1]?.hidden?.({ id: '1', dataEsborrat: true }))}</div>
            <div data-testid="estat-finalitzada">{columns[3]?.renderCell?.({ row: { dataFinalitzacio: '2026-03-13', estat: 'ACTIVA' } })}</div>
            <div data-testid="estat-esborrada">{columns[3]?.renderCell?.({ row: { dataFinalitzacio: '2026-03-13', estat: 'ESBORRADA' } })}</div>
        </section>
    ),
}));

import { useMuiDataGridContext } from 'reactlib';

vi.mock('@mui/x-data-grid-pro', () => ({
    useGridApiRef: () => ({ current: { setRowSelectionModel: vi.fn() } }),
}));

describe('Alarmes', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('Alarmes_quanEsRenderitza_mostraElFiltreInicialIElDialegDaccio', () => {
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
    it('Alarmes_btnEsborrarMultiple_existeixIestaDeshabilitatSenseSeleccio', () => {
        render(<Alarmes />);

        const btnMultiple = screen.getByTestId('btn-esborrar-multiple');
        expect(btnMultiple).toBeInTheDocument();
        expect(btnMultiple).toHaveAttribute('data-action', 'ALARMA_ESBORRAR_MULTIPLE');
        expect(btnMultiple).toBeDisabled();
        expect(btnMultiple).toHaveAttribute('title', 'Count: 0');
    });

    it('Alarmes_btnEsborrarMultiple_shabilitaQuanHiHaSeleccio', () => {
        (useMuiDataGridContext as any).mockReturnValue({
            selection: { ids: new Set(['id-1', 'id-2']) },
            apiRef: { current: { refresh: mocks.refreshMock } },
        });

        render(<Alarmes />);

        const btnMultiple = screen.getByTestId('btn-esborrar-multiple');
        expect(btnMultiple).toBeInTheDocument();
        expect(btnMultiple).not.toBeDisabled();
        expect(btnMultiple).toHaveAttribute('title', 'Count: 2');
        expect(btnMultiple).toHaveTextContent('Esborrar 2 alarmes');
    });

    it('Alarmes_btnEsborrarMultiple_noCridaOnSuccessSiNoHaySeleccion', () => {
        (useMuiDataGridContext as any).mockReturnValue({
            selection: { ids: new Set() },
            apiRef: { current: { refresh: mocks.refreshMock } },
        });

        render(<Alarmes />);
        const btnMultiple = screen.getByTestId('btn-esborrar-multiple');

        fireEvent.click(btnMultiple);

        expect(mocks.refreshMock).not.toHaveBeenCalled();
    });
});
