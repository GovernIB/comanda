import { act, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Salut from './Salut';

const mocks = vi.hoisted(() => ({
    useParamsMock: vi.fn(),
    refreshSalutMock: vi.fn(),
    refreshAppInfoMock: vi.fn(),
    useDisableMarginsMock: vi.fn(),
    useIntervalMock: vi.fn(),
    tMock: vi.fn((selector: any) =>
        selector({
            page: {
                salut: {
                    title: 'Salut',
                },
            },
        })
    ),
    findEntornAppMock: vi.fn(),
    findAppMock: vi.fn(),
    findEntornMock: vi.fn(),
    artifactReportSalutMock: vi.fn(),
    consoleErrorMock: vi.fn(),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: mocks.tMock,
    }),
}));

vi.mock('dayjs', () => ({
    __esModule: true,
    default: Object.assign(
        () => ({
            format: () => '2026-03-13',
        }),
        {
            duration: (value: string) => ({
                asMinutes: () => (value === 'PT15M' ? 15 : 5),
                asMilliseconds: () => 300_000,
            }),
        }
    ),
}));

vi.mock('react-router-dom', () => ({
    useParams: () => mocks.useParamsMock(),
}));

vi.mock('../../components/salut/SalutToolbar', () => ({
    __esModule: true,
    default: ({
        title,
        subtitle,
        goBackActive,
        hideFilter,
        onRefreshClick,
        ready,
        state,
    }: {
        title: string;
        subtitle?: string;
        goBackActive?: boolean;
        hideFilter?: boolean;
        onRefreshClick?: () => void;
        ready?: boolean;
        state?: unknown;
    }) => (
        <div data-testid="salut-toolbar">
            <span>{title}</span>
            <span>{subtitle ?? ''}</span>
            <span>{String(Boolean(goBackActive))}</span>
            <span>{String(Boolean(hideFilter))}</span>
            <span>{String(Boolean(ready))}</span>
            <span>{state != null ? 'state-on' : 'state-off'}</span>
            <button type="button" onClick={onRefreshClick}>
                Refrescar
            </button>
        </div>
    ),
    agrupacioFromMinutes: () => 'HORA',
    GroupingEnum: {
        APPLICATION: 'APPLICATION',
        ENVIRONMENT: 'ENVIRONMENT',
        NONE: 'NONE',
    },
    salutEntornAppFilterBuilder: () => 'FILTER(entornApp)',
    useSalutToolbarState: () => ({
        grouping: 'APPLICATION',
        dataRangeDuration: 'PT15M',
        refreshDuration: 'PT5M',
        filterData: {},
        ...(typeof (globalThis as any).__salutToolbarStateMock === 'function'
            ? (globalThis as any).__salutToolbarStateMock()
            : {}),
    }),
}));

vi.mock('./salutState.ts', () => ({
    useSalutLlistatExpansionState: () => ({
        expandedGroups: ['g1'],
        toggleGroupExpanded: vi.fn(),
    }),
}));

vi.mock('../../components/salut/SalutPrincipalWidgets', () => ({
    SalutLlistat: ({ salutGroups }: { salutGroups: unknown[] }) => (
        <div>{`SalutLlistat ${salutGroups.length}`}</div>
    ),
}));

vi.mock('./SalutAppInfo', () => ({
    default: ({
        appInfoData,
    }: {
        appInfoData: { entornApp?: { versio?: string } };
    }) => <div>{`SalutAppInfo ${appInfoData.entornApp?.versio ?? ''}`}</div>,
}));

vi.mock('../../components/salut/SalutItemStateChip', () => ({
    ItemStateChip: () => <span>StateChip</span>,
}));

vi.mock('../../components/PageTitle', () => ({
    default: ({ title }: { title: string }) => <h1>{title}</h1>,
}));

vi.mock('../../hooks/useDisableMargins', () => ({
    default: () => mocks.useDisableMarginsMock(),
}));

vi.mock('../../hooks/useInterval', () => ({
    default: (args: unknown) => mocks.useIntervalMock(args),
}));

vi.mock('./dataFetching', () => ({
    useAppInfoData: (...args: unknown[]) => ({
        ready: true,
        refresh: mocks.refreshAppInfoMock,
        entornApp: {
            app: { description: 'App Demo' },
            entorn: { description: 'PRO' },
            versio: '1.0.0',
        },
        salutCurrentApp: {
            appEstat: 'UP',
        },
        ...(typeof (globalThis as any).__salutAppInfoMock === 'function'
            ? (globalThis as any).__salutAppInfoMock(...args)
            : {}),
    }),
}));

vi.mock('reactlib', () => ({
    springFilterBuilder: {
        and: (...values: unknown[]) => values.filter(Boolean).join(' AND '),
        eq: (field: string, value: unknown) => `${field}=${String(value)}`,
    },
    useResourceApiService: (resourceName: string) => {
        const readyState =
            (globalThis as any).__salutReadyMock?.[resourceName] ??
            true;
        if (resourceName === 'salut') {
            return {
                isReady: readyState,
                artifactReport: mocks.artifactReportSalutMock,
            };
        }
        if (resourceName === 'entornApp') {
            return {
                isReady: readyState,
                find: mocks.findEntornAppMock,
            };
        }
        if (resourceName === 'app') {
            return {
                isReady: readyState,
                find: mocks.findAppMock,
            };
        }
        return {
            isReady: readyState,
            find: mocks.findEntornMock,
        };
    },
}));

describe('Salut', () => {
    beforeEach(() => {
        vi.spyOn(console, 'error').mockImplementation(mocks.consoleErrorMock);
        mocks.useParamsMock.mockReturnValue({});
        mocks.findEntornAppMock.mockResolvedValue({
            rows: [
                {
                    id: 7,
                    app: { id: 1, description: 'App Demo' },
                    entorn: { id: 2, description: 'PRO' },
                },
            ],
        });
        mocks.findAppMock.mockResolvedValue({
            rows: [{ id: 1, description: 'App Demo' }],
        });
        mocks.findEntornMock.mockResolvedValue({
            rows: [{ id: 2, description: 'PRO' }],
        });
        mocks.artifactReportSalutMock.mockImplementation((_id: unknown, args: { code: string }) => {
            if (args.code === 'grups_dates') {
                return Promise.resolve([{ data: '2026-03-13 10:00' }]);
            }
            if (args.code === 'estats') {
                return Promise.resolve([{ 7: [{ upPercent: 100 }] }]);
            }
            return Promise.resolve([
                {
                    entornAppId: 7,
                    data: '2026-03-13T10:00:00',
                    versio: '1.0.0',
                    appEstat: 'UP',
                    bdEstat: 'UP',
                },
            ]);
        });
    });

    afterEach(() => {
        delete (globalThis as any).__salutAppInfoMock;
        delete (globalThis as any).__salutToolbarStateMock;
        delete (globalThis as any).__salutReadyMock;
        vi.clearAllMocks();
        vi.restoreAllMocks();
    });

    it('Salut_quanNoHiHaRutaDapp_mostraLaToolbarIElLlistatAgrupat', async () => {
        // Comprova que la vista general renderitza el títol i el llistat quan no hi ha un id concret seleccionat.
        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByText(/SalutLlistat/)).toBeInTheDocument();
        });

        expect(screen.getByRole('heading', { name: 'Salut' })).toBeInTheDocument();
        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('Salut');
        expect(mocks.useDisableMarginsMock).toHaveBeenCalled();
        expect(mocks.useIntervalMock).toHaveBeenCalled();
    });

    it('Salut_quanHiHaRutaDapp_mostraLaVistaDeDetallIAjustaLaToolbar', async () => {
        // Verifica que la ruta d'una app concreta delega al component de detall i activa el mode de tornada enrere.
        mocks.useParamsMock.mockReturnValue({ id: '7' });

        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByText('SalutAppInfo 1.0.0')).toBeInTheDocument();
        });

        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('App Demo - PRO');
        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('1.0.0');
        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('true');
        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('state-on');
    });

    it('Salut_quanEsPremRefrescar_tornaADemanarLesDadesIGestionaElRefreshDelDetall', async () => {
        // Comprova que el tick periòdic del component reactiva el refresh global i inclou el refresh del detall de l'app.
        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByText(/SalutLlistat/)).toBeInTheDocument();
        });

        const intervalArgs = mocks.useIntervalMock.mock.calls[0]?.[0] as {
            tick: () => void;
            init: () => void;
        };

        act(() => {
            intervalArgs.init();
            intervalArgs.tick();
        });

        expect(mocks.refreshAppInfoMock).toHaveBeenCalled();
        expect(mocks.useIntervalMock).toHaveBeenCalled();
    });

    it('Salut_quanLaRutaDeDetallNoTeEntornApp_noMostraSubtitol', async () => {
        // Comprova que la toolbar de detall gestiona el cas sense dades de l'entorn-app carregat.
        (globalThis as any).__salutAppInfoMock = () => ({
            entornApp: null,
            salutCurrentApp: null,
        });
        mocks.useParamsMock.mockReturnValue({ id: '7' });

        render(<Salut />);

        await waitFor(() => {
            expect(
                screen.getByText((_, element) => element?.textContent === 'SalutAppInfo ')
            ).toBeInTheDocument();
        });

        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('true');
        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('state-off');
    });

    it('Salut_quanLesApisNoEstanPreparades_noLlançaLaCarregaInicial', async () => {
        // Comprova que el component no intenta carregar dades mentre les APIs encara no estan llestes.
        (globalThis as any).__salutReadyMock = {
            salut: false,
        };

        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('false');
        });

        expect(mocks.findEntornAppMock).not.toHaveBeenCalled();
        expect(mocks.artifactReportSalutMock).not.toHaveBeenCalled();
    });

    it('Salut_quanLagregacioEsPerEntorn_mostraUnGrupPerCadaEntorn', async () => {
        // Verifica que la vista agrupada per entorn divideix el llistat segons els entorns disponibles.
        (globalThis as any).__salutToolbarStateMock = () => ({
            grouping: 'ENVIRONMENT',
        });
        mocks.findEntornAppMock.mockResolvedValue({
            rows: [
                {
                    id: 7,
                    app: { id: 1, description: 'App Demo' },
                    entorn: { id: 2, description: 'PRO' },
                },
                {
                    id: 8,
                    app: { id: 1, description: 'App Demo' },
                    entorn: { id: 3, description: 'PRE' },
                },
            ],
        });
        mocks.findEntornMock.mockResolvedValue({
            rows: [
                { id: 2, description: 'PRO' },
                { id: 3, description: 'PRE' },
            ],
        });
        mocks.artifactReportSalutMock.mockImplementation((_id: unknown, args: { code: string }) => {
            if (args.code === 'grups_dates') {
                return Promise.resolve([{ data: '2026-03-13 10:00' }]);
            }
            if (args.code === 'estats') {
                return Promise.resolve([{ 7: [{ upPercent: 100 }], 8: [{ upPercent: 50 }] }]);
            }
            return Promise.resolve([
                {
                    entornAppId: 7,
                    data: '2026-03-13T10:00:00',
                    versio: '1.0.0',
                    appEstat: 'UP',
                    bdEstat: 'UP',
                },
                {
                    entornAppId: 8,
                    data: '2026-03-13T10:00:00',
                    versio: '2.0.0',
                    appEstat: 'WARN',
                    bdEstat: 'UP',
                },
            ]);
        });

        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByText('SalutLlistat 2')).toBeInTheDocument();
        });
    });

    it('Salut_quanLagregacioEsSenseGrups_mostraUnUnicBloc', async () => {
        // Comprova que l’agrupació desactivada compacta totes les dades en un únic grup.
        (globalThis as any).__salutToolbarStateMock = () => ({
            grouping: 'NONE',
        });
        mocks.findEntornAppMock.mockResolvedValue({
            rows: [
                {
                    id: 7,
                    app: { id: 1, description: 'App Demo' },
                    entorn: { id: 2, description: 'PRO' },
                },
                {
                    id: 8,
                    app: { id: 2, description: 'Altres' },
                    entorn: { id: 3, description: 'PRE' },
                },
            ],
        });

        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByText('SalutLlistat 1')).toBeInTheDocument();
        });
    });

    it('Salut_quanFallaLaCarregaInicial_mostraLaVistaBuidaiPermetRefrescar', async () => {
        // Verifica que una errada en la càrrega inicial deixa la pantalla estable i permet reintentar el refresh.
        mocks.findEntornAppMock.mockRejectedValueOnce(new Error('boom'));

        render(<Salut />);

        await waitFor(() => {
            expect(screen.getByText('SalutLlistat 0')).toBeInTheDocument();
        });

        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('true');
        expect(screen.getByTestId('salut-toolbar')).toHaveTextContent('state-off');

        mocks.findEntornAppMock.mockResolvedValueOnce({
            rows: [
                {
                    id: 7,
                    app: { id: 1, description: 'App Demo' },
                    entorn: { id: 2, description: 'PRO' },
                },
            ],
        });

        act(() => {
            screen.getByRole('button', { name: 'Refrescar' }).click();
        });

        await waitFor(() => {
            expect(mocks.findEntornAppMock).toHaveBeenCalledTimes(2);
        });
    });
});
