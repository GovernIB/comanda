import {act, fireEvent, render, screen, waitFor} from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import LogsViewer from './LogsViewer';

const mocks = vi.hoisted(() => ({
    artifactReportMock: vi.fn(),
    showTemporalMock: vi.fn(),
    createObjectUrlMock: vi.fn(),
    revokeObjectUrlMock: vi.fn(),
    tMock: vi.fn((selector: any, options?: any) => {
        const translation = selector({
            common: {
                error: 'Error',
            },
            page: {
                salut: {
                    logs: {
                        noSelected: 'Sense fitxer seleccionat',
                        noPreview: 'Sense previsualització',
                        refresh: 'Refrescar',
                        download: 'Descarregar',
                        softWrap: 'Ajustar text',
                        scrollToBottom: 'Anar al final',
                        preview: 'Previsualitzar',
                        search: 'Cercar en els logs...',
                        matches: '{{current}} / {{total}}',
                        lineFetchCount: 'Línies a carregar',
                        logsList: {
                            title: 'Llistat de logs',
                            nom: 'Nom',
                            dataCreacio: 'Creació',
                            dataModificacio: 'Modificació',
                            mida: 'Mida',
                            showPreview: 'Previsualització',
                        },
                    },
                },
            },
        });
        if (options && typeof translation === 'string') {
            return translation.replace('{{current}}', options.current).replace('{{total}}', options.total);
        }
        return translation;
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', () => ({
    dateFormatLocale: (value: string) => `format:${value}`,
    useCloseDialogButtons: () => <button>Tancar</button>,
    useResourceApiService: () => ({
        isReady: true,
        artifactReport: mocks.artifactReportMock,
    }),
    useDebounce: (value: any) => value,
}));

vi.mock('../../components/MessageShow', () => ({
    useMessage: () => ({
        showTemporal: mocks.showTemporalMock,
        component: <div data-testid="message-component">Missatge</div>,
    }),
}));

vi.mock('../../hooks/useDataGridLocale', () => ({
    default: () => ({ noRowsLabel: 'Sense files' }),
}));

vi.mock('../../../lib/components/mui/Dialog', () => ({
    default: ({
        open,
        title,
        children,
    }: {
        open: boolean;
        title: string;
        children: React.ReactNode;
    }) => (open ? <div><h2>{title}</h2>{children}</div> : null),
}));

vi.mock('../../../lib/components/mui/form/FormFieldNumber', () => ({
    default: ({ label, value, onChange, overrideTextFieldProps }: any) => (
        <input
            aria-label={label}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            {...overrideTextFieldProps}
        />
    ),
    FormFieldNumber: ({ label, value, onChange, overrideTextFieldProps }: any) => (
        <input
            aria-label={label}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            {...overrideTextFieldProps}
        />
    ),
}));

vi.mock('@mui/x-data-grid-pro', () => ({
    DataGridPro: ({
        rows,
        columns,
    }: {
        rows: Array<Record<string, unknown>>;
        columns: Array<{ field?: string; getActions?: (params: { row: Record<string, unknown> }) => React.ReactNode[] }>;
    }) => (
        <div>
            {rows.map((row) => (
                <div key={String(row.id)}>
                    <span>{String(row.nom)}</span>
                    {columns
                        .filter((column) => typeof column.getActions === 'function')
                        .flatMap((column) => column.getActions?.({ row }) ?? [])
                        .map((action, index) => <span key={`${String(row.id)}-${index}`}>{action}</span>)}
                </div>
            ))}
        </div>
    ),
    GridActionsCellItem: ({
        label,
        onClick,
    }: {
        label: string;
        onClick: () => void;
    }) => (
        <button type="button" onClick={onClick}>
            {label}
        </button>
    ),
}));

vi.mock('@mui/material', async () => {
    const actual = await vi.importActual<typeof import('@mui/material')>('@mui/material');
    return {
        ...actual,
        Tooltip: ({ children }: { children: React.ReactNode }) => <>{children}</>,
    };
});

vi.mock('@tanstack/react-virtual', () => ({
    useVirtualizer: () => ({
        getVirtualItems: () => [{ key: '0', index: 0, start: 0 }],
        getTotalSize: () => 20,
        measureElement: vi.fn(),
        scrollToIndex: vi.fn(),
    }),
}));

describe('LogsViewer', () => {
    beforeEach(() => {
        mocks.artifactReportMock.mockImplementation((_id: number, args: { code: string; data?: unknown }) => {
            if (args.code === 'llistar_logs') {
                return Promise.resolve([
                    {
                        nom: 'app.log',
                        mida: 1200,
                        dataCreacio: '2026-03-13T10:00:00',
                        dataModificacio: '2026-03-13T10:05:00',
                    },
                ]);
            }
            if (args.code === 'previsualitzar_log') {
                return Promise.resolve([
                    { linia: 'Línia 1' },
                    { linia: 'Línia 2' },
                ]);
            }
            return Promise.resolve({
                fileName: 'app.log',
                blob: new Blob(['contingut'], { type: 'text/plain' }),
            });
        });
        mocks.createObjectUrlMock.mockReturnValue('blob:test');
        mocks.revokeObjectUrlMock.mockImplementation(() => undefined);
        vi.stubGlobal('URL', {
            ...URL,
            createObjectURL: mocks.createObjectUrlMock,
            revokeObjectURL: mocks.revokeObjectUrlMock,
        });
    });

    afterEach(() => {
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('LogsViewer_quanNoHiHaCapFitxerSeleccionat_mostraLEstatBuit', () => {
        // Comprova que el component mostra el placeholder de previsualització buida abans de seleccionar cap log.
        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        expect(screen.getByText('Sense fitxer seleccionat')).toBeInTheDocument();
        expect(screen.getByText('Sense previsualització')).toBeInTheDocument();
    });

    it('LogsViewer_quanEsSeleccionaUnFitxer_carregaLaPrevisualitzacio', async () => {
        // Verifica que en seleccionar un fitxer des del diàleg es carrega i es mostra el contingut previsualitzat.
        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        expect(await screen.findByRole('heading', { name: 'Llistat de logs' })).toBeInTheDocument();

        fireEvent.click(screen.getByRole('button', { name: 'Previsualitzar' }));

        await waitFor(() => {
            expect(mocks.artifactReportMock).toHaveBeenCalledWith(7, {
                code: 'previsualitzar_log',
                data: {
                    fileName: 'app.log',
                    lineCount: 1000,
                },
            });
        });

        expect(await screen.findByText('app.log')).toBeInTheDocument();
        expect(await screen.findByText('Línia 1')).toBeInTheDocument();
    });

    it('LogsViewer_quanEsDescarregaUnFitxer_creaLaUrlTemporalIElDescarrega', async () => {
        // Comprova que l'acció de descàrrega genera una object URL i la revoca després de l'ús.
        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Previsualitzar' }));

        const appendChildSpy = vi.spyOn(document.body, 'appendChild');
        const removeSpy = vi.spyOn(HTMLAnchorElement.prototype, 'remove').mockImplementation(() => undefined);
        const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined);

        fireEvent.click(screen.getByTestId('DownloadIcon').closest('button') as HTMLButtonElement);

        await waitFor(() => {
            expect(mocks.createObjectUrlMock).toHaveBeenCalled();
        });

        expect(appendChildSpy).toHaveBeenCalled();
        expect(clickSpy).toHaveBeenCalled();
        expect(removeSpy).toHaveBeenCalled();
        expect(mocks.revokeObjectUrlMock).toHaveBeenCalledWith('blob:test');
    });

    it('LogsViewer_quanEsPremRefrescar_tornaACarregarLaPrevisualitzacio', async () => {
        // Verifica que el botó de refresc torna a consultar la previsualització del fitxer seleccionat.
        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Previsualitzar' }));

        await waitFor(() => {
            expect(screen.getByText('Línia 1')).toBeInTheDocument();
        });

        const previewCallsBeforeRefresh = mocks.artifactReportMock.mock.calls.filter(
            ([, args]) => (args as { code: string }).code === 'previsualitzar_log'
        ).length;

        fireEvent.click(screen.getByTestId('RefreshIcon').closest('button') as HTMLButtonElement);

        await waitFor(() => {
            const previewCalls = mocks.artifactReportMock.mock.calls.filter(
                ([, args]) => (args as { code: string }).code === 'previsualitzar_log'
            ).length;
            expect(previewCalls).toBeGreaterThan(previewCallsBeforeRefresh);
        });
    });

    it('LogsViewer_quanLaPrevisualitzacioFalla_mostraUnMissatgeTemporalDerror', async () => {
        // Comprova que els errors de previsualització es comuniquen a l'usuari amb un missatge temporal.
        mocks.artifactReportMock.mockImplementation((_id: number, args: { code: string }) => {
            if (args.code === 'llistar_logs') {
                return Promise.resolve([
                    {
                        nom: 'app.log',
                        mida: 1200,
                        dataCreacio: '2026-03-13T10:00:00',
                        dataModificacio: '2026-03-13T10:05:00',
                    },
                ]);
            }
            if (args.code === 'previsualitzar_log') {
                return Promise.reject(new Error('No es pot previsualitzar'));
            }
            return Promise.resolve({
                fileName: 'app.log',
                blob: new Blob(['contingut'], { type: 'text/plain' }),
            });
        });

        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Previsualitzar' }));

        await waitFor(() => {
            expect(mocks.showTemporalMock).toHaveBeenCalledWith(
                'Error',
                'No es pot previsualitzar',
                'error'
            );
        });
    });

    it('LogsViewer_quanSactivaAutoRefresh_programaRefrescosPeriodics', async () => {
        // Comprova que activar el switch d'auto-refresc programa un interval que crida refreshPreview cada 10s.

        // 👇 Mock de setInterval/clearInterval per controlar el temps
        const setIntervalMock = vi.spyOn(globalThis, 'setInterval').mockImplementation(
            (callback: TimerHandler, _timeout?: number) => {
                (globalThis as any).__autoRefreshCallback__ = callback;
                return 123 as unknown as ReturnType<typeof setInterval>;
            }
        );
        const clearIntervalMock = vi.spyOn(globalThis, 'clearInterval').mockImplementation(() => undefined);

        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Previsualitzar' }));

        // Espera que es carregui la previsualització inicial
        await waitFor(() => {
            expect(screen.getByText('Línia 1')).toBeInTheDocument();
        });

        // 👇 Trobar el switch per la seva etiqueta traduïda
        const autoRefreshSwitch = screen.getByRole('switch');
        expect(autoRefreshSwitch).not.toBeChecked();

        // 👇 Activar el switch
        fireEvent.click(autoRefreshSwitch!);
        expect(autoRefreshSwitch).toBeChecked();

        // 👇 Verificar que s'ha programat l'interval amb 10000ms (10 segons)
        expect(setIntervalMock).toHaveBeenCalledWith(expect.any(Function), 10000);

        // 👇 Simular el pas del temps executant manualment el callback de l'interval
        act(() => {
            (globalThis as any).__autoRefreshCallback__?.();
        });

        // 👇 Verificar que refreshPreview s'ha cridat de nou (2 crides: inicial + auto-refresh)
        const previewCalls = mocks.artifactReportMock.mock.calls.filter(
            ([, args]) => (args as { code: string }).code === 'previsualitzar_log'
        );
        expect(previewCalls.length).toBeGreaterThanOrEqual(2);

        // 👇 Desactivar el switch i verificar que es cancel·la l'interval
        fireEvent.click(autoRefreshSwitch);
        expect(autoRefreshSwitch).not.toBeChecked();
        expect(clearIntervalMock).toHaveBeenCalledWith(123);

        // 👇 Neteja dels mocks globals
        setIntervalMock.mockRestore();
        clearIntervalMock.mockRestore();
        delete (globalThis as any).__autoRefreshCallback__;
    });

    it('LogsViewer_quanEsBuscaUnTerme_mostraElsResultats', async () => {
        // Comprova que buscar un terme actualitza les coincidències i permet navegar entre elles.
        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Previsualitzar' }));

        await waitFor(() => {
            expect(screen.getByText('Línia 1')).toBeInTheDocument();
        });

        const searchInput = screen.getByPlaceholderText('Cercar en els logs...');
        fireEvent.change(searchInput, { target: { value: 'Línia' } });

        await waitFor(() => {
            expect(screen.getByText('1 / 2')).toBeInTheDocument();
        });

        const nextButton = screen.getByTestId('KeyboardArrowDownIcon').closest('button') as HTMLButtonElement;
        fireEvent.click(nextButton);

        await waitFor(() => {
            expect(screen.getByText('2 / 2')).toBeInTheDocument();
        });
    });

    it('LogsViewer_quanSmodificaElCompteDeLinies_despresDeDebounce_esCarregaLaPrevisualitzacio', async () => {
        render(<LogsViewer entornAppId={7} preselectedLog={null} />);

        fireEvent.click(screen.getByRole('button', { name: 'Sense fitxer seleccionat' }));
        fireEvent.click(await screen.findByRole('button', { name: 'Previsualitzar' }));

        await waitFor(() => {
            expect(screen.getByText('Línia 1')).toBeInTheDocument();
        });

        const input = screen.getByLabelText('Línies a carregar');
        fireEvent.change(input, { target: { value: '2000' } });

        await waitFor(() => {
            expect(mocks.artifactReportMock).toHaveBeenCalledWith(7, expect.objectContaining({
                code: 'previsualitzar_log',
                data: expect.objectContaining({
                    lineCount: 2000,
                }),
            }));
        }, { timeout: 2000 });
    });
});
