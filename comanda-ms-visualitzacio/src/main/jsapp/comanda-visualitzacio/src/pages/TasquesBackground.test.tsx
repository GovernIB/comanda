import { render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import TasquesBackground from './TasquesBackground';

const mocks = vi.hoisted(() => ({
    apiDeleteMock: vi.fn(),
    gridRefreshMock: vi.fn(),
    messageDialogShowMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    confirmDialogButtonsMock: { accept: 'Sí', cancel: 'No' },
    tMock: vi.fn((selectorOrKey: any, options?: any) => {
        if (typeof selectorOrKey === 'function') {
            const base = selectorOrKey({
                page: {
                    tasquesBackground: {
                        title: 'Tasques en segon pla',
                        reiniciar: {
                            titol: 'Reiniciar tasca',
                            confirm: 'Estàs segur de reiniciar la tasca {nom}?',
                            success: 'Tasca reiniciada correctament',
                            error: 'Error en reiniciar la tasca',
                            label: 'Reiniciar',
                        },
                        columns: {
                            nom: 'Nom',
                            expressioHuman: 'Expressió',
                            proxExecucio: 'Propera execució',
                            ultimaExecucio: 'Última execució',
                            ultimaEstat: 'Últim estat',
                            ultimaDuracio: 'Última duració',
                        },
                    },
                },
            });
            if (options && typeof base === 'string') {
                return base.replace('{nom}', String(options.nom));
            }
            return base;
        }
        return String(selectorOrKey);
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
        rowAdditionalActions,
    }: {
        title: string;
        columns: Array<Record<string, any>>;
        rowAdditionalActions?: Array<{ label: string; icon: string; onClick: (id: string | number) => void }>;
    }) => {
        const sampleRowId = 123;
        return (
            <section>
                <h2>{title}</h2>
                <span data-testid="column-count">{columns.length}</span>
                <span data-testid="column-header-nom">{columns[0]?.headerName}</span>
                <span data-testid="column-header-proxExecucio">{columns[2]?.headerName}</span>
                <span data-testid="column-header-ultimaExecucio">{columns[3]?.headerName}</span>
                <span data-testid="value-getter-proxExecucio">
                    {columns[2]?.valueGetter?.('2023-10-01T10:00:00Z')?.toISOString() ?? 'sense-valor'}
                </span>
                <span data-testid="value-getter-ultimaExecucio">
                    {columns[3]?.valueGetter?.('2023-10-02T11:00:00Z')?.toISOString() ?? 'sense-valor'}
                </span>
                <span data-testid="value-getter-proxExecucio-null">
                    {columns[2]?.valueGetter?.(null) ?? 'sense-valor'}
                </span>
                {rowAdditionalActions && rowAdditionalActions.length > 0 && (
                    <button
                        data-testid="action-reiniciar"
                        onClick={() => rowAdditionalActions[0].onClick(sampleRowId)}
                    >
                        {rowAdditionalActions[0].label}
                    </button>
                )}
            </section>
        );
    },
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'tascaBackground') {
            return {
                isReady: true,
                delete: mocks.apiDeleteMock,
            };
        }
        return { isReady: false, delete: vi.fn() };
    },
    useMuiDataGridApiRef: () => ({
        current: {
            refresh: mocks.gridRefreshMock,
        },
    }),
    useBaseAppContext: () => ({
        messageDialogShow: mocks.messageDialogShowMock,
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useConfirmDialogButtons: () => mocks.confirmDialogButtonsMock,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

describe('TasquesBackground', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mocks.messageDialogShowMock.mockResolvedValue(true);
        mocks.apiDeleteMock.mockResolvedValue(undefined);
    });

    afterEach(() => {
        vi.clearAllMocks();
    });

    it('TasquesBackground_quanCarrega_construeixLesColumnesCorrectament', async () => {
        render(<TasquesBackground />);

        await waitFor(() => {
            expect(screen.getByTestId('column-count')).toHaveTextContent('6');
        });

        expect(screen.getByRole('heading', { level: 1, name: 'Tasques en segon pla' })).toBeInTheDocument();
        expect(screen.getByTestId('column-header-nom')).toHaveTextContent('Nom');
        expect(screen.getByTestId('column-header-proxExecucio')).toHaveTextContent('Propera execució');
        expect(screen.getByTestId('column-header-ultimaExecucio')).toHaveTextContent('Última execució');
        expect(screen.getByTestId('value-getter-proxExecucio')).toHaveTextContent('2023-10-01T10:00:00.000Z');
        expect(screen.getByTestId('value-getter-ultimaExecucio')).toHaveTextContent('2023-10-02T11:00:00.000Z');
        expect(screen.getByTestId('value-getter-proxExecucio-null')).toHaveTextContent('sense-valor');
    });

    it('TasquesBackground_quanEsFaClickAReiniciar_iEsConfirma_executaLaCridaIActualitza', async () => {
        render(<TasquesBackground />);

        const reiniciarButton = screen.getByTestId('action-reiniciar');
        expect(reiniciarButton).toBeInTheDocument();
        reiniciarButton.click();

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalledWith(
                'Reiniciar tasca',
                'Estàs segur de reiniciar la tasca 123?',
                mocks.confirmDialogButtonsMock,
                { maxWidth: 'sm', fullWidth: true }
            );
        });

        await waitFor(() => {
            expect(mocks.apiDeleteMock).toHaveBeenCalledWith(123);
        });

        await waitFor(() => {
            expect(mocks.gridRefreshMock).toHaveBeenCalled();
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                null,
                'Tasca reiniciada correctament',
                'success'
            );
        });
    });

    it('TasquesBackground_quanEsFaClickAReiniciar_iFallaLaCrida_mostraError', async () => {
        mocks.apiDeleteMock.mockRejectedValue({ message: 'Error de xarxa' });
        render(<TasquesBackground />);

        const reiniciarButton = screen.getByTestId('action-reiniciar');
        reiniciarButton.click();

        await waitFor(() => {
            expect(mocks.apiDeleteMock).toHaveBeenCalledWith(123);
        });

        await waitFor(() => {
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                'Error en reiniciar la tasca',
                'Error de xarxa',
                'error'
            );
        });
    });

    it('TasquesBackground_quanEsFaClickAReiniciar_iEsCancel·la_noExecutaLaCrida', async () => {
        mocks.messageDialogShowMock.mockResolvedValue(false);
        render(<TasquesBackground />);

        const reiniciarButton = screen.getByTestId('action-reiniciar');
        reiniciarButton.click();

        await waitFor(() => {
            expect(mocks.messageDialogShowMock).toHaveBeenCalled();
        });

        expect(mocks.apiDeleteMock).not.toHaveBeenCalled();
        expect(mocks.gridRefreshMock).not.toHaveBeenCalled();
        expect(mocks.temporalMessageShowMock).not.toHaveBeenCalled();
    });
});
