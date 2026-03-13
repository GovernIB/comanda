import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import CalendariEstadistiques, { CalendarStatusButton, useEntornAppData } from './CalendariEstadistiques';

const mocks = vi.hoisted(() => ({
    useMediaQueryMock: vi.fn(),
    alertMock: vi.fn(),
    fullCalendarProps: undefined as any,
    entornAppFindMock: vi.fn(),
    fetActionMock: vi.fn(),
    fetReportMock: vi.fn(),
    dimensioFindMock: vi.fn(),
    indicadorFindMock: vi.fn(),
    temporalMessageShowMock: vi.fn(),
    showMessageMock: vi.fn(),
    useCalendarEventsMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            menu: {
                calendari: 'Calendari',
            },
            calendari: {
                error_dades: 'Error de dades',
                dades_disponibles: 'Dades disponibles',
                obtenir_dades: 'Obtenir dades',
                error_dades_tooltip: 'No s han pogut carregar les dades',
                dades_disponibles_tooltip: 'Ja hi ha dades disponibles',
                obtenir_dades_tooltip: 'Encara no hi ha dades disponibles',
                seleccionar_entorn_app: 'Seleccionar entorn app',
                seleccionar: 'Seleccionar',
                seleccionar_entorn_app_primer: 'Selecciona primer l entorn',
                carregar_interval: 'Carregar interval',
                carregar_mes_actual: 'Carregar mes actual',
                data_inici: 'Data inici',
                data_fi: 'Data fi',
                cancelar: 'Cancel lar',
                carregar: 'Carregar',
                tancar: 'Tancar',
                today: 'Avui',
                carregant_dades: 'Carregant dades',
                success_obtenir_dades: 'Dades carregades',
                error_obtenir_dades: 'Error obtenint dades',
                error_dades_disponibles: 'Error dates disponibles',
                error_dades_dia: 'Error dades dia',
                error_titol: 'Error del calendari',
                data: 'Data',
                missatge: 'Missatge',
                traca: 'Traça',
            },
        })
    ),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('@mui/material', async () => {
    const actual = await vi.importActual<typeof import('@mui/material')>('@mui/material');
    return {
        ...actual,
        useMediaQuery: (...args: unknown[]) => mocks.useMediaQueryMock(...args),
        FormControl: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
        InputLabel: ({ children, id }: { children: React.ReactNode; id?: string }) => (
            <label htmlFor={id}>{children}</label>
        ),
        Select: ({
            children,
            labelId,
            value,
            onChange,
            label,
        }: {
            children: React.ReactNode;
            labelId?: string;
            value: string | number;
            onChange?: (event: { target: { value: string } }) => void;
            label?: string;
        }) => (
            <select
                aria-label={label}
                id={labelId}
                value={String(value)}
                onChange={(event) => onChange?.({ target: { value: event.target.value } })}
            >
                {children}
            </select>
        ),
        MenuItem: ({
            children,
            value,
            onClick,
        }: {
            children: React.ReactNode;
            value?: string | number;
            onClick?: () => void;
        }) => (
            <option value={String(value ?? '')} onClick={onClick}>
                {children}
            </option>
        ),
        Tooltip: ({ children }: { children: React.ReactNode }) => <>{children}</>,
    };
});

vi.mock('@fullcalendar/react', () => ({
    default: (props: any) => {
        mocks.fullCalendarProps = props;
        return (
            <div>
                <div data-testid="calendar-events-count">{props.events.length}</div>
                <button type="button" onClick={() => props.customButtons.monthButton.click()}>
                    Carregar mes actual
                </button>
                <button type="button" onClick={() => props.customButtons.intervalButton.click()}>
                    Carregar interval
                </button>
                <button
                    type="button"
                    onClick={() =>
                        props.eventClick({
                            event: {
                                startStr: '2026-03-13',
                                display: 'block',
                                extendedProps: {
                                    esDisponible: true,
                                    hasError: false,
                                    isLoading: false,
                                },
                            },
                        })
                    }
                >
                    Event disponible
                </button>
                <button
                    type="button"
                    onClick={() =>
                        props.eventClick({
                            event: {
                                startStr: '2026-03-14',
                                display: 'block',
                                extendedProps: {
                                    esDisponible: false,
                                    hasError: false,
                                    isLoading: false,
                                },
                            },
                        })
                    }
                >
                    Event buit
                </button>
                <button
                    type="button"
                    onClick={() =>
                        props.eventClick({
                            event: {
                                startStr: '2026-03-15',
                                display: 'background',
                                extendedProps: {
                                    esDisponible: false,
                                    hasError: false,
                                    isLoading: false,
                                },
                            },
                        })
                    }
                >
                    Event background
                </button>
                <button
                    type="button"
                    onClick={() =>
                        props.eventClick({
                            event: {
                                startStr: '2026-03-16',
                                display: 'block',
                                extendedProps: {
                                    esDisponible: false,
                                    hasError: false,
                                    isLoading: true,
                                },
                            },
                        })
                    }
                >
                    Event carregant
                </button>
                <button
                    type="button"
                    onClick={() =>
                        props.eventClick({
                            event: {
                                startStr: '2026-03-14',
                                display: 'block',
                                extendedProps: {
                                    esDisponible: false,
                                    hasError: true,
                                    isLoading: false,
                                },
                            },
                        })
                    }
                >
                    Event error
                </button>
            </div>
        );
    },
}));

vi.mock('@fullcalendar/daygrid', () => ({
    default: {},
}));

vi.mock('@fullcalendar/interaction', () => ({
    default: {},
}));

vi.mock('reactlib', () => ({
    GridPage: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    Toolbar: ({
        title,
        elementsWithPositions,
    }: {
        title: string;
        elementsWithPositions?: Array<{ element: React.ReactNode }>;
    }) => (
        <div>
            <h2>{title}</h2>
            {elementsWithPositions?.map((item, index) => (
                <div key={index}>{item.element}</div>
            ))}
        </div>
    ),
    useBaseAppContext: () => ({
        currentLanguage: 'ca',
        temporalMessageShow: mocks.temporalMessageShowMock,
    }),
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'entornApp') {
            return {
                isReady: true,
                find: mocks.entornAppFindMock,
            };
        }
        if (resourceName === 'fet') {
            return {
                isReady: true,
                artifactAction: mocks.fetActionMock,
                artifactReport: mocks.fetReportMock,
            };
        }
        if (resourceName === 'dimensio') {
            return {
                isReady: true,
                find: mocks.dimensioFindMock,
            };
        }
        if (resourceName === 'indicador') {
            return {
                isReady: true,
                find: mocks.indicadorFindMock,
            };
        }
        return {
            isReady: false,
            find: vi.fn(),
            artifactAction: vi.fn(),
            artifactReport: vi.fn(),
        };
    },
    springFilterBuilder: {
        and: vi.fn(),
        eq: vi.fn((field: string, value: unknown) => `${field}=${String(value)}`),
    },
}));

vi.mock('../components/MessageShow.tsx', () => ({
    useMessage: () => ({
        show: mocks.showMessageMock,
        component: <div data-testid="message-component" />,
    }),
}));

vi.mock('../components/calendari/UseCalendarEventsProps.ts', () => ({
    useCalendarEvents: (args: unknown) => mocks.useCalendarEventsMock(args),
}));

vi.mock('../components/calendari/CalendariDadesDialog.tsx', () => ({
    default: ({
        dadesDiaModalOpen,
        currentDataDia,
    }: {
        dadesDiaModalOpen: boolean;
        currentDataDia: string;
    }) =>
        dadesDiaModalOpen ? (
            <div data-testid="dades-dia-dialog">{`Dades dia ${currentDataDia}`}</div>
        ) : null,
}));

vi.mock('../components/PageTitle.tsx', () => ({
    default: ({ title }: { title: string }) => <div data-testid="page-title">{title}</div>,
}));

describe('CalendarStatusButton', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('CalendarStatusButton_quanHiHaError_mostraLestatDError', () => {
        // Comprova que el botó mostra el text d'error i queda habilitat quan no està carregant.
        mocks.useMediaQueryMock.mockReturnValue(false);

        render(<CalendarStatusButton hasError isLoading={false} esDisponible={false} />);

        expect(screen.getByRole('button', { name: 'Error de dades' })).toBeInTheDocument();
        expect(screen.getByText('Error de dades')).toBeInTheDocument();
    });

    it('CalendarStatusButton_quanEstaCarregant_desactivaElBoto', () => {
        // Verifica que durant la càrrega el botó queda deshabilitat.
        mocks.useMediaQueryMock.mockReturnValue(false);
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
        const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => undefined);

        render(<CalendarStatusButton hasError={false} isLoading esDisponible={false} />);

        expect(
            screen.getByRole('button', {
                name: 'Obtenir dades',
            })
        ).toBeDisabled();

        consoleErrorSpy.mockRestore();
        consoleWarnSpy.mockRestore();
    });

    it('CalendarStatusButton_quanLaPantallaEsPetita_amagaLetiquetaTextual', () => {
        // Comprova que en pantalles petites només es renderitza la icona i no el text del botó.
        mocks.useMediaQueryMock.mockReturnValue(true);

        render(<CalendarStatusButton hasError={false} isLoading={false} esDisponible />);

        expect(screen.getByRole('button')).toBeInTheDocument();
        expect(screen.queryByText('Dades disponibles')).not.toBeInTheDocument();
    });
});

describe('CalendariEstadistiques', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('CalendariEstadistiques_quanEsSeleccionaUnEntorn_carregaDatesIDadesRelacionades', async () => {
        // Comprova que en seleccionar un entorn-app es consulten les dates disponibles, dimensions i indicadors.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [
                {
                    id: 7,
                    app: { description: 'App Demo' },
                    entorn: { description: 'PRO' },
                },
            ],
        });
        mocks.fetReportMock.mockResolvedValue([{ data: '2026-03-13' }]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [{ id: 1, nom: 'Dim 1' }] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [{ id: 2, nom: 'Ind 1' }] });
        mocks.useCalendarEventsMock.mockReturnValue([{ id: 'evt-1' }]);

        render(<CalendariEstadistiques />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
        });

        expect(mocks.dimensioFindMock).toHaveBeenCalledWith({
            unpaged: true,
            filter: 'entornAppId=7',
        });
        expect(mocks.indicadorFindMock).toHaveBeenCalledWith({
            unpaged: true,
            filter: 'entornAppId=7',
        });
        expect(screen.getByTestId('calendar-events-count')).toHaveTextContent('1');
    });

    it('CalendariEstadistiques_quanNoHiHaEntornSeleccionat_iEsPremCarregarMes_mostraUnAlert', async () => {
        // Verifica que el botó de carregar el mes actual obliga primer a seleccionar un entorn-app.
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);
        vi.stubGlobal('alert', mocks.alertMock);

        render(<CalendariEstadistiques />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        act(() => {
            fireEvent.click(screen.getByRole('button', { name: 'Carregar mes actual' }));
        });

        expect(mocks.alertMock).toHaveBeenCalledWith('Selecciona primer l entorn');
    });

    it('CalendariEstadistiques_quanEsPremUnDiaAmbDades_obriElDialegDelDia', async () => {
        // Comprova que el clic sobre un dia amb dades demana el detall i obri el diàleg del dia.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [
                {
                    id: 7,
                    app: { description: 'App Demo' },
                    entorn: { description: 'PRO' },
                },
            ],
        });
        mocks.fetReportMock.mockImplementation((_id: unknown, args: { code: string }) => {
            if (args.code === 'dates_disponibles') {
                return Promise.resolve([{ data: '2026-03-13' }]);
            }
            if (args.code === 'dades_dia') {
                return Promise.resolve([{ valor: 12 }]);
            }
            return Promise.resolve([]);
        });
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([{ id: 'evt-1' }]);

        render(<CalendariEstadistiques />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        fireEvent.click(screen.getByRole('button', { name: 'Event disponible' }));

        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dades_dia',
                data: {
                    entornAppId: '7',
                    dataInici: '2026-03-13',
                },
            });
        });

        expect(screen.getByTestId('dades-dia-dialog')).toHaveTextContent('Dades dia 2026-03-13');
    });

    it('CalendariEstadistiques_quanEsPremUnDiaSenseDades_lesObteIActualitzaLesDates', async () => {
        // Verifica que un dia sense dades llança l'acció de càrrega i refresca les dates disponibles si l'operació va bé.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [
                {
                    id: 7,
                    app: { description: 'App Demo' },
                    entorn: { description: 'PRO' },
                },
            ],
        });
        mocks.fetActionMock.mockResolvedValue({
            success: true,
            diesAmbDades: {
                '2026-03-14': true,
            },
        });
        mocks.fetReportMock.mockResolvedValue([{ data: '2026-03-14' }]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([{ id: 'evt-1' }]);

        render(<CalendariEstadistiques />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        fireEvent.click(screen.getByRole('button', { name: 'Event buit' }));

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(null, {
                code: 'obtenir_per_data',
                data: {
                    entornAppId: '7',
                    dataInici: '2026-03-14',
                },
            });
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(null, 'Dades carregades', 'success');
    });

    it('CalendariEstadistiques_quanEsCarregaElMesActual_usaLaccioPerInterval', async () => {
        // Verifica que el botó del mes actual calcula l'interval i llança l'acció massiva de càrrega.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.fetActionMock.mockResolvedValue({ success: true });
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        fireEvent.click(screen.getByRole('button', { name: 'Carregar mes actual' }));

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(
                null,
                expect.objectContaining({ code: 'obtenir_per_interval' })
            );
        });
    });

    it('CalendariEstadistiques_quanEsPremUnEventBackground_noFaCapAccio', async () => {
        // Comprova que els events de fons sense dades no disparen cap acció.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await waitFor(() => expect(mocks.entornAppFindMock).toHaveBeenCalled());
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => expect(mocks.fetReportMock).toHaveBeenCalled());
        mocks.fetActionMock.mockClear();

        fireEvent.click(screen.getByRole('button', { name: 'Event background' }));

        expect(mocks.fetActionMock).not.toHaveBeenCalled();
    });

    it('CalendariEstadistiques_quanEsPremUnEventCarregant_noRepeteixLaPeticio', async () => {
        // Verifica que els dies marcats com a carregant ignoren clics addicionals.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await waitFor(() => expect(mocks.entornAppFindMock).toHaveBeenCalled());
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => expect(mocks.fetReportMock).toHaveBeenCalled());
        mocks.fetActionMock.mockClear();

        fireEvent.click(screen.getByRole('button', { name: 'Event carregant' }));

        expect(mocks.fetActionMock).not.toHaveBeenCalled();
    });

    it('CalendariEstadistiques_quanEsPremUnEventAmbError_mostraElDialegDeDetall', async () => {
        // Comprova que un error guardat per una data es pot consultar des del calendari.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);
        mocks.fetActionMock.mockRejectedValueOnce(new Error('boom'));

        render(<CalendariEstadistiques />);

        await waitFor(() => expect(mocks.entornAppFindMock).toHaveBeenCalled());
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => expect(mocks.fetReportMock).toHaveBeenCalled());

        fireEvent.click(screen.getByRole('button', { name: 'Event buit' }));
        await waitFor(() => expect(mocks.fetActionMock).toHaveBeenCalled());

        fireEvent.click(screen.getByRole('button', { name: 'Event error' }));

        expect(screen.getByText('Error del calendari')).toBeInTheDocument();
        expect(screen.getByText('Error obtenint dades')).toBeInTheDocument();
    });

    it('CalendariEstadistiques_quanFallenDimensionsIIndicadors_manteLaVistaOperativa', async () => {
        // Verifica que els errors de recursos auxiliars no trenquen la pantalla principal del calendari.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockRejectedValueOnce(new Error('dim-error'));
        mocks.indicadorFindMock.mockRejectedValueOnce(new Error('ind-error'));
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await waitFor(() => expect(mocks.entornAppFindMock).toHaveBeenCalled());
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        await waitFor(() => {
            expect(mocks.dimensioFindMock).toHaveBeenCalled();
            expect(mocks.indicadorFindMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('calendar-events-count')).toHaveTextContent('0');
    });
});

describe('useEntornAppData', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('useEntornAppData_quanLapiNoEstaLlesta_noFaCapConsulta', async () => {
        // Comprova que el hook no intenta carregar dades mentre l'API encara no està preparada.
        const getAll = vi.fn();
        const { result } = renderHook(() => useEntornAppData(false, getAll));

        await waitFor(() => {
            expect(result.current).toEqual([]);
        });

        expect(getAll).not.toHaveBeenCalled();
    });

    it('useEntornAppData_quanLaConsultaVaBe_retornaLesFilesObtingudes', async () => {
        // Verifica que el hook desa la llista retornada pel servei quan la consulta és correcta.
        const rows = [{ id: 1, nom: 'Entorn 1' }, { id: 2, nom: 'Entorn 2' }];
        const getAll = vi.fn().mockResolvedValue({ rows });
        const { result } = renderHook(() => useEntornAppData(true, getAll));

        await waitFor(() => {
            expect(result.current).toEqual(rows);
        });

        expect(getAll).toHaveBeenCalledWith({
            unpaged: true,
            filter: 'activa : true AND app.activa : true',
        });
    });

    it('useEntornAppData_quanLaConsultaFalla_retornaUnaLlistaBuida', async () => {
        // Comprova que el hook es recupera d'un error i torna una col·lecció buida.
        const getAll = vi.fn().mockRejectedValue(new Error('boom'));
        const { result } = renderHook(() => useEntornAppData(true, getAll));

        await waitFor(() => {
            expect(result.current).toEqual([]);
        });
    });
});
