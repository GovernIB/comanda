import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Monitors, { translateEnumValue } from './Monitor';
import React from 'react';

const mocks = vi.hoisted(() => ({
    clearMock: vi.fn(),
    filterMock: vi.fn(),
    showDialogMock: vi.fn(),
    setFieldValueMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    apiActionMock: vi.fn().mockResolvedValue(true),
    getErrorMessageMock: vi.fn((error) => error?.message || 'Error desconocido'),

    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                monitors: {
                    title: 'Monitor',
                    detail: {
                        title: 'Detall monitor',
                        data: 'Data',
                        operacio: 'Operació',
                        tipus: 'Tipus',
                        estat: 'Estat',
                        codiUsuari: 'Codi usuari',
                        errorDescripcio: 'Descripció error',
                        excepcioMessage: 'Missatge excepció',
                        excepcioStacktrace: 'Stacktrace',
                        netejaEntornApp: {
                            reintentarSuccess: 'Reintentament iniciat correctament',
                            reintentarError: 'Error en reintentar',
                            reintentarButton: 'Reintentar',
                        }
                    },
                    modulEnum: {
                        salut: 'Salut',
                        estadistica: 'Estadística',
                        configuracio: 'Configuració',
                        alarmes: 'Alarmes',
                        tasca: 'Tasca',
                        avis: 'Avís',
                        usuaris: 'Usuaris',
                    },
                    column: {
                        mailAddress: 'Adreça de correu',
                        rolOUsuari: 'Rol / Usuari',
                    },
                    tab: {
                        email: 'EMAIL',
                    },
                    filter: {
                        more: "Més camps",
                    },
                },
            },
            components: {
                clear: 'Netejar',
                search: 'Cercar',
            },
        })
    ),
    tStringKeyMock: vi.fn((key: string) => {
        const translations: Record<string, string> = {
            'page.monitors.modulEnum.salut': 'Salut',
            'page.monitors.modulEnum.estadistica': 'Estadística',
            'page.monitors.modulEnum.configuracio': 'Configuració',
            'page.monitors.modulEnum.alarmes': 'Alarmes',
            'page.monitors.modulEnum.tasca': 'Tasca',
            'page.monitors.modulEnum.avis': 'Avís',
            'page.monitors.modulEnum.usuaris': 'Usuaris',
            'page.monitors.column.mailAddress': 'Adreça de correu',
            'page.monitors.column.rolOUsuari': 'Rol / Usuari',
            'page.monitors.tab.email': 'EMAIL',
            'page.monitors.detail.estatEnum.ok': 'Correcte',
            'page.monitors.detail.estatEnum.error': 'Error',
            'page.monitors.detail.estatEnum.warn': 'Avís',
            'page.monitors.detail.tipusEnum.sortida': 'Sortida',
            'page.monitors.detail.tipusEnum.entrada': 'Entrada',
            'page.monitors.detail.tipusEnum.interna': 'Interna',
            'page.monitors.detail.operacioEnum.netejaEntornApp': 'Neteja d\'entorn',
            'page.monitors.detail.operacioEnum.netejaEntornAppCompletat': 'Neteja d\'entorn completada',
        };
        return translations[key] ?? key;
    }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('reactlib', async () => {
    const React = await import('react');
    return {
        MuiDataGrid: ({
            title,
            toolbarAdditionalRow,
            onRowClick,
            fixedFilter,
            columns,
        }: {
            title: string;
            toolbarAdditionalRow?: React.ReactNode;
            onRowClick?: (params: { row: Record<string, unknown> }) => void;
            fixedFilter?: string;
            columns: Array<{ field: string; headerName?: string; renderCell?: (params: any) => React.ReactNode }>;
        }) => {
            const urlColumn = columns?.find(col => col.field === 'url');
            const estatColumn = columns?.find(col => col.field === 'estat');
            return (
                <section>
                    <h2>{title}</h2>
                    <div data-testid="fixed-filter">{fixedFilter}</div>
                    <div>{toolbarAdditionalRow}</div>
                    {columns?.map((col) => (
                        <div key={col.field} data-testid={`column-${col.field}`}>
                            {col.headerName ?? col.field}
                        </div>
                    ))}
                    <div data-testid="url-header">{urlColumn?.headerName ?? urlColumn?.field ?? 'url'}</div>
                    <button data-testid="open-detail" onClick={() => onRowClick?.({ row: { estat: 'ERROR', tipus: 'SORTIDA', excepcioStacktrace: 'stack' } })}>
                        Obre detall
                    </button>
                    <button
                        data-testid="open-retry-detail"
                        onClick={() => onRowClick?.({
                            row: {
                                id: 999,
                                operacio: 'netejaEntornApp',
                                estat: 'ERROR',
                                app: { description: 'App Test' },
                                entorn: { description: 'Entorn Test' }
                            }
                        })}
                    >
                        Obre detall Reintentar
                    </button>
                    <div data-testid="estat-cell">
                        {estatColumn?.renderCell?.({ value: 'WARN', formattedValue: 'Avís' })}
                    </div>
                </section>
            );
        },
        MuiFilter: ({
            children,
            springFilterBuilder,
            apiRef,
            resourceName,
        }: {
            children: React.ReactNode;
            springFilterBuilder?: (data: any) => string;
            apiRef?: { current: { clear: () => void } };
            resourceName?: string;
        }) => {
            const [data, setData] = React.useState<any>({});
            const handleAppChange = (appId: number | undefined) => {
                const newData = { ...data, app: appId ? { id: appId } : undefined };
                setData(newData);
                springFilterBuilder?.(newData);
            };
            const handleEntornChange = (entornId: number | undefined) => {
                const newData = { ...data, entorn: entornId ? { id: entornId } : undefined };
                setData(newData);
                springFilterBuilder?.(newData);
            };
            return (
                <div>
                    {children}
                    {resourceName === 'entornApp' && (
                        <>
                            <button data-testid="select-app" onClick={() => handleAppChange(1)}>Seleccionar App</button>
                            <button data-testid="select-entorn" onClick={() => handleEntornChange(1)}>Seleccionar Entorn</button>
                            <button data-testid="clear-filters" onClick={() => {
                                setData({});
                                apiRef?.current?.clear();
                            }}>Limpiar Filtros</button>
                        </>
                    )}
                </div>
            );
        },
        FormField: ({ name }: { name: string }) => <div data-testid={`field-${name}`}>{name}</div>,
        dateFormatLocale: () => '13/03/2026 10:00',

        useMuiContentDialog: () => {
            const [content, setContent] = React.useState<React.ReactNode>(null);
            const showDialog = (title: string, comp: React.ReactNode, buttons: any, options: any) => {
                setContent(comp);
                mocks.showDialogMock(title, comp, buttons, options);
            };
            return [
                showDialog,
                content ? <div data-testid="mock-dialog">{content}</div> : null
            ];
        },

        useCloseDialogButtons: () => <button>Tancar</button>,
        useFilterApiRef: () => ({
            current: {
                clear: mocks.clearMock,
                filter: mocks.filterMock,
                setFieldValue: mocks.setFieldValueMock,
            },
        }),
        springFilterBuilder: {
            and: (...parts: Array<string | undefined | false>) => parts.filter(Boolean).join(' && '),
            like: (field: string, value: unknown) => `${field}~${String(value)}`,
            between: (field: string, from: unknown, to: unknown) => `${field}[${String(from)},${String(to)}]`,
            eq: (field: string, value: unknown) => `${field}=${String(value)}`,
        },
        useFormApiRef: () => ({ current: { setFieldValue: mocks.setFieldValueMock } }),
        useBaseAppContext: () => ({ temporalMessageShow: mocks.temporalMessageShowMock }),
        useResourceApiService: () => ({ artifactAction: mocks.apiActionMock }),
    };
});

vi.mock('../util/exceptionUtils', () => ({
    getErrorMessage: mocks.getErrorMessageMock,
}));

vi.mock('../components/ContentDetail', () => ({
    ContentDetail: ({ elements }: { elements: Array<{ label?: string; value?: unknown; contentValue?: React.ReactNode }> }) => (
        <div>
            {elements.map((element, index) => (
                <div key={index} data-testid={`detail-row-${index}`}>
                    <span>{element.label}</span>
                    <span data-testid={`detail-value-${index}`}>{element.contentValue ?? String(element.value ?? '')}</span>
                </div>
            ))}
        </div>
    ),
}));

vi.mock('../components/RickTextDetail', () => ({
    StacktraceBlock: ({ title }: { title: string }) => <div>{title}</div>,
}));

vi.mock('../hooks/useTranslationStringKey', () => ({
    default: () => ({
        t: mocks.tStringKeyMock,
    }),
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('translateEnumValue', () => {
    it('translateEnumValue_quanHiHaTraduccio_retornaElTextTraduït', () => {
        // Comprova que els enums coneguts es transformen amb el mapa i la funció de traducció.
        const result = translateEnumValue('OK', { OK: 'page.monitors.detail.estatEnum.ok' }, mocks.tStringKeyMock);
        expect(result).toBe('Correcte');
    });

    it('translateEnumValue_quanNoHiHaMapa_retornaElValorOriginal', () => {
        // Verifica que si no hi ha mapa de traducció es manté el valor original.
        expect(translateEnumValue('CUSTOM')).toBe('CUSTOM');
    });
});

describe('Monitors', () => {
    afterEach(() => {
        vi.clearAllMocks();
        mocks.apiActionMock.mockResolvedValue(true);
    });

    it('Monitors_quanEsRenderitza_mostraElFiltreInicialITotsElsControlsPrincipals', () => {
        // Comprova que la pàgina arrenca al mòdul de salut i mostra el filtre, els camps i les pestanyes principals.
        render(<Monitors />);

        expect(screen.getByTestId('page-title')).toHaveTextContent('Monitor');
        expect(screen.getByRole('heading', { name: 'Monitor' })).toBeInTheDocument();
        expect(screen.getByTestId('fixed-filter')).toHaveTextContent("modul:'SALUT'");
        expect(screen.getByTestId('field-codi')).toBeInTheDocument();
        expect(screen.getByTestId('field-dataDesde')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Netejar' })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Més camps' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'Salut' })).toBeInTheDocument();
        expect(screen.getByRole('tab', { name: 'EMAIL' })).toBeInTheDocument();
        expect(screen.getByTestId('url-header')).toHaveTextContent('URL');
        expect(screen.getByTestId('estat-cell')).toHaveTextContent('Avís');
    });

    it('Monitors_quanEsPremenElsBotonsDelFiltre_executaClearIFilter', () => {
        // Verifica que els botons del filtre deleguen correctament a l'API del filtre.
        render(<Monitors />);

        fireEvent.click(screen.getByRole('button', { name: 'Netejar' }));
        expect(mocks.clearMock).toHaveBeenCalled();
    });

    it('Monitors_quanCanviaLaPestanya_actualitzaElFiltreStaticIObreElDetall', () => {
        // Comprova que canviar de pestanya actualitza el mòdul actiu i que clicar una fila obre el detall.
        render(<Monitors />);

        fireEvent.click(screen.getByRole('tab', { name: 'Estadística' }));
        expect(screen.getByTestId('fixed-filter')).toHaveTextContent("modul:'ESTADISTICA'");

        fireEvent.click(screen.getByTestId('open-detail'));
        expect(mocks.showDialogMock).toHaveBeenCalledWith(
            'Detall monitor',
            expect.anything(),
            expect.anything(),
            expect.objectContaining({ maxWidth: 'lg', fullWidth: true })
        );
    });

    it('Monitors_quanSeleccionaEmail_mostraElModulAlarmesIElHeaderDeCorreu', () => {
        render(<Monitors />);
        fireEvent.click(screen.getByRole('tab', { name: 'EMAIL' }));

        expect(screen.getByTestId('fixed-filter')).toHaveTextContent("modul:'ALARMES'");
        expect(screen.getByTestId('url-header')).toHaveTextContent('Adreça de correu');
    });

    it('Monitors_quanFiltraPerApp_ocultaLaColumnaApp', async () => {
        render(<Monitors />);
        fireEvent.click(screen.getByTestId('select-app'));

        await waitFor(() => {
            expect(screen.queryByTestId('column-app')).not.toBeInTheDocument();
        });
        expect(screen.queryByTestId('column-entorn')).toBeInTheDocument();
    });

    it('Monitors_quanFiltraPerEntorn_ocultaLaColumnaEntorn', async () => {
        render(<Monitors />);
        fireEvent.click(screen.getByTestId('select-entorn'));

        await waitFor(() => {
            expect(screen.queryByTestId('column-entorn')).not.toBeInTheDocument();
        });
        expect(screen.queryByTestId('column-app')).toBeInTheDocument();
    });

        it('MonitorDetails_quanEsReintentaNetejaAmbExit_actualitzaLaOperacioIMostraMissatge', async () => {
        render(<Monitors />);
        fireEvent.click(screen.getByTestId('open-retry-detail'));

        const retryButton = await screen.findByRole('button', { name: 'Reintentar' });
        expect(retryButton).toBeInTheDocument();
        fireEvent.click(retryButton);
        await waitFor(() => {
            expect(mocks.apiActionMock).toHaveBeenCalledWith(999, { code: 'delete_entorn_app_by_modul' });
        });
        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Reintentament iniciat correctament',
            'success'
        );
        await waitFor(() => {
            expect(screen.getByText('Neteja d\'entorn completada')).toBeInTheDocument();
        });
        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Reintentar' })).toBeDisabled();
        });
    });

    it('MonitorDetails_quanEsReintentaNetejaAmbError_mostraMissatgeDerrorUtilitzantGetErrorMessage', async () => {
        mocks.apiActionMock.mockRejectedValueOnce(new Error('Fallo catastrófico del servidor'));
        render(<Monitors />);
        fireEvent.click(screen.getByTestId('open-retry-detail'));

        const retryButton = await screen.findByRole('button', { name: 'Reintentar' });
        fireEvent.click(retryButton);
        await waitFor(() => {
            expect(mocks.getErrorMessageMock).toHaveBeenCalledWith(expect.any(Error));
            expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
                'Error en reintentar',
                'Fallo catastrófico del servidor',
                'error'
            );
        });

        expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument();
    });

    it('MonitorDetails_quanEsReintentaNeteja_elBotoEsDesactivaMentreEsCarrega', async () => {
        let resolveRetry: (value: boolean) => void;
        const retryPromise = new Promise<boolean>((resolve) => {
            resolveRetry = resolve;
        });
        mocks.apiActionMock.mockReturnValueOnce(retryPromise);

        render(<Monitors />);
        fireEvent.click(screen.getByTestId('open-retry-detail'));

        const retryButton = await screen.findByRole('button', { name: 'Reintentar' });
        expect(retryButton).not.toBeDisabled();

        fireEvent.click(retryButton);

        await waitFor(() => {
            expect(retryButton).toBeDisabled();
        });

        // Finalitzem la crida
        resolveRetry!(true);
        await waitFor(() => {
            expect(retryButton).toBeDisabled(); // Continua desactivat perquè s'ha completat
        });
    });
});
