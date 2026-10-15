import { render, screen, waitFor, act } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import AlarmsProvider from './AlarmsProvider';
import { useAlarmsContext } from './AlarmsContext';
import * as SseProvider from './SseProvider.tsx';

const mocks = vi.hoisted(() => ({
    reportMock: vi.fn(),
    subscribeMock: vi.fn(),
    showTemporalMock: vi.fn(),
    playMock: vi.fn(),
    tMock: vi.fn((selector: any, options?: any) => {
        if (typeof selector === 'function') {
            const base = selector({
                page: {
                    alarma: {
                        snackbar: {
                            title: 'Alarmas',
                            existingAlarms: 'Hay {count} alarmas existentes',
                            newAlarms: 'Hay {count} alarmas nuevas',
                        },
                    },
                },
            });
            if (options && typeof base === 'string') {
                return base.replace('{count}', String(options.count));
            }
            return base;
        }
        return String(selector);
    }),
}));

vi.mock('react-i18next', () => ({ useTranslation: () => ({ t: mocks.tMock }) }));

vi.mock('reactlib', () => ({
    useResourceApiService: (resource: string) => {
        if (resource === 'alarma') {
            return { isReady: true, artifactReport: mocks.reportMock };
        }
        return { isReady: false, artifactReport: vi.fn() };
    },
}));

vi.mock('./SseProvider.tsx', () => ({
    useSseContext: vi.fn(() => ({
        status: 'connected',
        subscribe: vi.fn(() => () => {}),
    })),
}));

vi.mock('./MessageShow.tsx', () => ({
    useMessage: () => ({
        showTemporal: mocks.showTemporalMock,
        component: <div data-testid="message-component" />,
    }),
}));

vi.mock('./Audio.tsx', () => ({
    useAudio: () => ({ play: mocks.playMock, pause: vi.fn(), stop: vi.fn() }),
}));

vi.mock('./UserContext.ts', () => ({
    useUserContext: () => ({ user: { alarma: 'SOUND_05' } }),
}));

const TestConsumer = () => {
    const { alarms } = useAlarmsContext();
    return <span data-testid="alarms-status">{alarms === null ? 'loading' : `loaded:${alarms.length}`}</span>;
};

describe('AlarmsProvider', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mocks.reportMock.mockResolvedValue([]);
        vi.mocked(SseProvider.useSseContext).mockReturnValue({
            status: 'connected',
            subscribe: mocks.subscribeMock,
            connected: true
        });
    });

    afterEach(() => {
        vi.clearAllTimers();
    });

    it('AlarmsProvider_renderizaChildrenYContexto', async () => {
        mocks.reportMock.mockResolvedValue([{ id: 1 }]);
        render(<AlarmsProvider><TestConsumer /></AlarmsProvider>);
        expect(screen.getByTestId('message-component')).toBeInTheDocument();
        await waitFor(() => expect(screen.getByTestId('alarms-status')).toHaveTextContent('loaded:1'));
    });

    it('AlarmsProvider_hacePeticionInicial_cuandoApiEstaLista', async () => {
        mocks.reportMock.mockResolvedValue([{ id: 1 }]);
        render(<AlarmsProvider><TestConsumer /></AlarmsProvider>);
        await waitFor(() => {
            expect(mocks.reportMock).toHaveBeenCalledWith(undefined, { code: 'ALARMA_FIND_ACTIVES' });
        });
    });

    it('AlarmsProvider_muestraMensajeYReproduceAudio_cuandoHayAlarmasNuevas', async () => {
        mocks.reportMock.mockResolvedValue([{ id: 1, nom: 'Alarma 1' }]);

        let sseCallback: (event: { payload: any }) => void = () => {};
        mocks.subscribeMock.mockImplementation((eventType: string, callback: any) => {
            if (eventType === 'alarm.active.changed') sseCallback = callback;
            return () => {};
        });

        render(<AlarmsProvider><TestConsumer /></AlarmsProvider>);
        await waitFor(() => expect(screen.getByTestId('alarms-status')).toHaveTextContent('loaded:1'));

        vi.clearAllMocks();

        act(() => {
            sseCallback({ payload: [{ id: 1, nom: 'Alarma 1' }, { id: 2, nom: 'Alarma 2' }] });
        });

        await waitFor(() => {
            expect(mocks.showTemporalMock).toHaveBeenCalledWith(
                'Alarmas',
                'Hay 1 alarmas nuevas',
                'error',
                undefined,
                5000
            );
            expect(mocks.playMock).toHaveBeenCalled();
        });
    });

    it('AlarmsProvider_seSuscribeAEventosSSE_yActualizaAlarmas', async () => {
        let sseCallback: (event: { payload: any }) => void = () => {};
        mocks.subscribeMock.mockImplementation((eventType: string, callback: any) => {
            if (eventType === 'alarm.active.changed') sseCallback = callback;
            return () => {};
        });

        mocks.reportMock.mockResolvedValue([]);
        render(<AlarmsProvider><TestConsumer /></AlarmsProvider>);
        await waitFor(() => expect(mocks.reportMock).toHaveBeenCalled());
        vi.clearAllMocks();

        act(() => { sseCallback({ payload: [{ id: 99 }] }); });
        await waitFor(() => {
            expect(mocks.showTemporalMock).toHaveBeenCalled();
            expect(mocks.playMock).toHaveBeenCalled();
        });
    });

    it('AlarmsProvider_hacePolling_cuandoSseEstaDesconectado', async () => {
        vi.useFakeTimers();
        vi.mocked(SseProvider.useSseContext).mockReturnValue({
            status: 'disconnected',
            subscribe: mocks.subscribeMock,
            connected: false,
        });
        mocks.reportMock.mockResolvedValue([{ id: 1 }]);
        render(<AlarmsProvider><TestConsumer /></AlarmsProvider>);

        await Promise.resolve();
        expect(mocks.reportMock).toHaveBeenCalledTimes(1);

        mocks.reportMock.mockClear();
        await vi.advanceTimersByTimeAsync(30000);
        expect(mocks.reportMock).toHaveBeenCalledTimes(1);
        vi.useRealTimers();
    });
});
