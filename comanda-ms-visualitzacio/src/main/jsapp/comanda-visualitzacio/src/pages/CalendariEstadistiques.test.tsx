import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import dayjs from 'dayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import CalendariEstadistiques, { CalendarStatusButton, useEntornAppData } from './CalendariEstadistiques';

vi.mock('@mui/x-date-pickers/DatePicker', () => ({
    DatePicker: ({ label, value, onChange }: any) => (
        <div>
            <label htmlFor={`input-${label}`}>{label}</label>
            <input
                id={`input-${label}`}
                aria-label={label}
                value={value ? dayjs(value).format('YYYY-MM-DD') : ''}
                onChange={(e) => onChange(e.target.value ? dayjs(e.target.value) : null)}
            />
        </div>
    ),
}));

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
                obtenir_dades_carregant: 'Obtenint dades',
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
                carregant: "Carregant",
                carregant_dades: 'Carregant dades',
                success_obtenir_dades: 'Dades carregades',
                error_obtenir_dades: 'Error obtenint dades',
                error_dades_disponibles: 'Error dates disponibles',
                error_dades_dia: 'Error dades dia',
                error_titol: 'Error del calendari',
                data: 'Data',
                missatge: 'Missatge',
                traca: 'Traça',
                baixa_prioritat: 'Baixa prioritat',
                baixa_prioritat_descripcio: 'Recupera les dades progressivament en segon pla sense sobrecarregar el sistema',
                processos_baixa_prioritat: 'Processos de baixa prioritat',
                success_baixa_prioritat: 'Procés de baixa prioritat iniciat',
                proces_cancelat: 'Procés cancel·lat correctament',
                estat_en_execucio: 'En execució',
                estat_finalitzat: 'Finalitzat',
                estat_error: 'Error',
                estat_cancelat: 'Cancel·lat',
                estat_pendent: 'Pendent',
                cancelar_proces: 'Cancel·lar procés',
                sense_processos: 'No hi ha cap procés de baixa prioritat',
                refrescar: 'Refrescar',
                mostrar_tots_processos: 'Mostrar tots els processos',
                dies_processats: 'Dies processats',
                estat: 'Estat',
                progres: 'Progrés',
                dia_actual: 'Dia actual',
                accions: 'Accions',
                entorn: 'Entorn',
                periode: 'Període',
                error_cancelar_proces: "No s'ha pogut cancel·lar el procés (potser ja ha finalitzat)",
                processant_segon_pla: 'En segon pla...',
                processant_segon_pla_tooltip: 'Hi ha un procés de recuperació en segon pla per a aquest dia',
                velocitat_recuperacio: 'Velocitat de recuperació',
                velocitat: 'Velocitat',
                prioritat_baixa: 'Baixa (10 minuts)',
                prioritat_baixa_desc: 'Mínim impacte en el servidor. Pausa de 10 minuts entre dies.',
                prioritat_mitja: 'Mitja (3 minuts)',
                prioritat_mitja_desc: 'Impacte moderat. Pausa de 3 minuts entre dies.',
                prioritat_alta: 'Alta (Sense retard)',
                prioritat_alta_desc: 'Recuperació ràpida amb pausa mínima (500 ms). Major impacte en el servidor.',
            },
            common: {
                estat: 'Estat',
                actions: 'Accions',
                close: 'Tancar',
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
                <button
                    type="button"
                    onClick={() =>
                        props.eventClick({
                            event: {
                                startStr: '2026-03-17',
                                display: 'block',
                                extendedProps: {
                                    esDisponible: false,
                                    hasError: false,
                                    isLoading: false,
                                    isBackgroundProcessing: true,
                                },
                            },
                        })
                    }
                >
                    Event background processing
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
            screen.getByRole('button', { name: 'Obtenint dades', })
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

    it('CalendarStatusButton_quanEstaEnProcessamentEnSegonPla_mostraLestatEnSegonPlaIDesactivaElBoto', () => {
        mocks.useMediaQueryMock.mockReturnValue(false);

        render(
            <CalendarStatusButton
                hasError={false}
                isLoading={false}
                esDisponible={false}
                isBackgroundProcessing
            />
        );

        const button = screen.getByRole('button', { name: 'En segon pla...' });
        expect(button).toBeInTheDocument();
        expect(button).toBeDisabled();
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });

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

it('CalendariEstadistiques_quanNoHiHaEntornSeleccionat_mostraMissatgeAjudaIAmagaCalendari', async () => {
        // Verifica que quan no hi ha un entorn seleccionat, es mostra el missatge d'ajuda
        mocks.entornAppFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await waitFor(() => {
            expect(mocks.entornAppFindMock).toHaveBeenCalled();
        });

        expect(screen.getByText('Selecciona primer l entorn')).toBeInTheDocument();

        expect(screen.queryByRole('button', { name: 'Carregar mes actual' })).not.toBeInTheDocument();
        expect(screen.queryByRole('button', { name: 'Carregar interval' })).not.toBeInTheDocument();
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });

        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
        });
        fireEvent.click(screen.getByRole('button', { name: 'Carregar mes actual' }));

        const dialog = await screen.findByRole('dialog');
        expect(dialog).toBeInTheDocument();
        const checkbox = screen.getByTestId('baixa-prioritat-checkbox') as HTMLInputElement;
        expect(checkbox.checked).toBe(true);

        fireEvent.click(within(dialog).getByRole('button', { name: 'Carregar' }));

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(
                null,
                expect.objectContaining({
                    code: 'obtenir_per_interval',
                    data: expect.objectContaining({
                        entornAppId: '7',
                        baixaPrioritat: true,
                        pausaMs: 180000,
                    }),
                })
            );
        });
    });

    it('CalendariEstadistiques_quanEsSeleccionaPausaPersonalitzada_enviaPausaCorrecta', async () => {
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.fetActionMock.mockResolvedValue({ success: true, message: 'OK' });
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
        });

        fireEvent.click(screen.getByRole('button', { name: 'Carregar interval' }));

        const dialog = await screen.findByRole('dialog');
        expect(dialog).toBeInTheDocument();

        fireEvent.change(screen.getByLabelText('Data inici'), { target: { value: '2026-03-01' } });
        fireEvent.change(screen.getByLabelText('Data fi'), { target: { value: '2026-03-10' } });

        // Activa baixa prioritat
        const checkbox = screen.getByTestId('baixa-prioritat-checkbox');
        fireEvent.click(checkbox);

        // Selecciona 'Baixa (10 minuts)' -> 600000
        const radio10m = screen.getByDisplayValue('600000');
        fireEvent.click(radio10m);

        fireEvent.click(within(dialog).getByRole('button', { name: 'Carregar' }));

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(
                null,
                expect.objectContaining({
                    code: 'obtenir_per_interval',
                    data: expect.objectContaining({
                        entornAppId: '7',
                        baixaPrioritat: true,
                        pausaMs: 600000,
                    }),
                })
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => expect(mocks.fetReportMock).toHaveBeenCalled());
        mocks.fetActionMock.mockClear();

        fireEvent.click(screen.getByRole('button', { name: 'Event carregant' }));

        expect(mocks.fetActionMock).not.toHaveBeenCalled();
    });

    it('CalendariEstadistiques_quanEsPremUnEventEnSegonPla_noRepeteixLaPeticio', async () => {
        // Verifica que els dies marcats com a processant en segon pla ignoren clics per evitar peticions duplicades.
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(<CalendariEstadistiques />);

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => expect(mocks.fetReportMock).toHaveBeenCalled());
        mocks.fetActionMock.mockClear();

        fireEvent.click(screen.getByRole('button', { name: 'Event background processing' }));

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

        await screen.findByRole('option', { name: 'App Demo - PRO' });
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

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        await waitFor(() => {
            expect(mocks.dimensioFindMock).toHaveBeenCalled();
            expect(mocks.indicadorFindMock).toHaveBeenCalled();
        });

        expect(screen.getByTestId('calendar-events-count')).toHaveTextContent('0');
    });

    it('CalendariEstadistiques_quanEsCarregaIntervalAmbBaixaPrioritat_enviaElParametreICarregaProcessos', async () => {
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.fetActionMock.mockResolvedValue({ success: true, message: 'Procés iniciat en segon pla' });
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
        });

        // Clic a Carregar interval per obrir el diàleg
        fireEvent.click(screen.getByRole('button', { name: 'Carregar interval' }));

        // Fixar les dates inici i fi
        fireEvent.change(screen.getByLabelText('Data inici'), { target: { value: '01/01/2026' } });
        fireEvent.change(screen.getByLabelText('Data fi'), { target: { value: '10/01/2026' } });

        // Comprovar que el checkbox de baixa prioritat és visible
        const checkbox = screen.getByTestId('baixa-prioritat-checkbox');
        expect(checkbox).toBeInTheDocument();
        expect(checkbox).not.toBeChecked();

        // Marcar baixa prioritat
        fireEvent.click(checkbox);
        expect(checkbox).toBeChecked();

        // Clic a Carregar dins el diàleg
        fireEvent.click(screen.getByRole('button', { name: 'Carregar' }));

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(null, {
                code: 'obtenir_per_interval',
                data: expect.objectContaining({
                    entornAppId: '7',
                    baixaPrioritat: true,
                }),
            });
        });

        expect(mocks.showMessageMock).toHaveBeenCalledWith(null, 'Procés iniciat en segon pla', 'success');
    });

    it('CalendariEstadistiques_quanHiHaProcessosEnExecucio_mostraComptadorIObreElDialeg', async () => {
        const mockProcessos = [
            {
                id: 'proc-1',
                entornAppId: 7,
                entornAppNom: 'App Demo - PRO',
                dataInici: '2026-01-01',
                dataFi: '2026-01-10',
                estat: 'EN_EXECUCIO',
                diesProcessats: 3,
                totalDies: 10,
                percentatge: 30,
                diaActual: '2026-01-04',
                dataCreacio: '2026-03-29T10:00:00Z',
            },
        ];

        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockImplementation((_id: unknown, args: { code: string }) => {
            if (args.code === 'processos_baixa_prioritat') {
                return Promise.resolve(mockProcessos);
            }
            return Promise.resolve([]);
        });
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        const btnProcessos = await screen.findByTestId('btn-processos-baixa-prioritat');
        expect(btnProcessos).toBeInTheDocument();
        expect(btnProcessos).toHaveTextContent('1');

        fireEvent.click(btnProcessos);

        const dialog = screen.getByRole('dialog');
        expect(dialog).toBeInTheDocument();
        expect(within(dialog).getByText('App Demo - PRO')).toBeInTheDocument();
    });

    it('CalendariEstadistiques_quanEsCancelaUnProces_cridaLaccioDeCancelacioIRefresca', async () => {
        const mockProcessos = [
            {
                id: 'proc-to-cancel',
                entornAppId: 7,
                entornAppNom: 'App Demo - PRO',
                dataInici: '2026-01-01',
                dataFi: '2026-01-10',
                estat: 'EN_EXECUCIO',
                diesProcessats: 2,
                totalDies: 10,
                percentatge: 20,
                diaActual: '2026-01-03',
                dataCreacio: '2026-03-29T10:00:00Z',
            },
        ];

        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockImplementation((_id: unknown, args: { code: string }) => {
            if (args.code === 'processos_baixa_prioritat') {
                return Promise.resolve(mockProcessos);
            }
            return Promise.resolve([]);
        });
        mocks.fetActionMock.mockResolvedValue(true);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        const btnProcessos = await screen.findByTestId('btn-processos-baixa-prioritat');
        fireEvent.click(btnProcessos);

        const cancelBtn = await screen.findByTestId('btn-cancel-proc-to-cancel');
        fireEvent.click(cancelBtn);

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(null, {
                code: 'cancelar_baixa_prioritat',
                data: 'proc-to-cancel',
            });
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            'Procés cancel·lat correctament',
            'success'
        );
    });

    it('CalendariEstadistiques_quanFallaCancelacioProces_mostraAvisWarning', async () => {
        const mockProcessos = [
            {
                id: 'proc-failed-cancel',
                entornAppId: 7,
                dataInici: '2026-05-01',
                dataFi: '2026-05-10',
                estat: 'EN_EXECUCIO',
                totalDies: 10,
                diesProcessats: 10,
                diesAmbDades: 10,
                diesAmbErrors: 0,
                percentatge: 100,
            },
        ];

        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockImplementation((_: any, args: any) => {
            if (args.code === 'processos_baixa_prioritat') {
                return Promise.resolve(mockProcessos);
            }
            return Promise.resolve([]);
        });
        mocks.fetActionMock.mockResolvedValue(false);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        const btnProcessos = await screen.findByTestId('btn-processos-baixa-prioritat');
        fireEvent.click(btnProcessos);

        const cancelBtn = await screen.findByTestId('btn-cancel-proc-failed-cancel');
        fireEvent.click(cancelBtn);

        await waitFor(() => {
            expect(mocks.fetActionMock).toHaveBeenCalledWith(null, {
                code: 'cancelar_baixa_prioritat',
                data: 'proc-failed-cancel',
            });
        });

        expect(mocks.temporalMessageShowMock).toHaveBeenCalledWith(
            null,
            "No s'ha pogut cancel·lar el procés (potser ja ha finalitzat)",
            'warning'
        );
    });

    it('CalendariEstadistiques_quanHiHaProcessosActius_passaDatesEnSegonPlaAlHook', async () => {
        const mockProcessos = [
            {
                id: 'proc-running',
                entornAppId: 7,
                dataInici: '2026-05-01',
                dataFi: '2026-05-03',
                estat: 'EN_EXECUCIO',
                totalDies: 3,
                diesProcessats: 1,
                diesAmbDades: 1,
                diesAmbErrors: 0,
                percentatge: 33,
            },
        ];

        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockImplementation((_: any, args: any) => {
            if (args?.code === 'processos_baixa_prioritat') {
                return Promise.resolve(mockProcessos);
            }
            return Promise.resolve([]);
        });
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        await waitFor(() => {
            expect(mocks.useCalendarEventsMock).toHaveBeenCalledWith(
                expect.objectContaining({
                    backgroundProcessingDates: ['2026-05-01', '2026-05-02', '2026-05-03'],
                })
            );
        });
    });

    it('CalendariEstadistiques_quanEsCancelaElDialeg_reiniciaElCheckboxDeBaixaPrioritat', async () => {
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });
        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'dates_disponibles',
                data: '7',
            });
        });

        // Obre diàleg
        const btnInterval = await screen.findByRole('button', { name: 'Carregar interval' });
        fireEvent.click(btnInterval);

        const checkbox = screen.getByTestId('baixa-prioritat-checkbox');
        expect(checkbox).not.toBeChecked();

        // Marca baixa prioritat
        fireEvent.click(checkbox);
        expect(checkbox).toBeChecked();

        // Cancel·la diàleg
        fireEvent.click(screen.getByRole('button', { name: 'Cancel lar' }));

        await waitFor(() => {
            expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
        });

        // Torna a obrir el diàleg
        const btnIntervalReobert = await screen.findByRole('button', { name: 'Carregar interval' });
        fireEvent.click(btnIntervalReobert);

        const checkboxReobert = screen.getByTestId('baixa-prioritat-checkbox');
        expect(checkboxReobert).not.toBeChecked();
    });

    it('CalendariEstadistiques_quanEsSeleccionaUnEntorn_carregaProcessosFiltratsPerAquestEntorn', async () => {
        mocks.entornAppFindMock.mockResolvedValue({
            rows: [{ id: 7, app: { description: 'App Demo' }, entorn: { description: 'PRO' } }],
        });
        mocks.fetReportMock.mockResolvedValue([]);
        mocks.dimensioFindMock.mockResolvedValue({ rows: [] });
        mocks.indicadorFindMock.mockResolvedValue({ rows: [] });
        mocks.useCalendarEventsMock.mockReturnValue([]);

        render(
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <CalendariEstadistiques />
            </LocalizationProvider>
        );

        await screen.findByRole('option', { name: 'App Demo - PRO' });
        fireEvent.change(screen.getByLabelText('Seleccionar entorn app'), {
            target: { value: '7' },
        });

        await waitFor(() => {
            expect(mocks.fetReportMock).toHaveBeenCalledWith(null, {
                code: 'processos_baixa_prioritat',
                data: { entornAppId: 7, },
            });
        });
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
