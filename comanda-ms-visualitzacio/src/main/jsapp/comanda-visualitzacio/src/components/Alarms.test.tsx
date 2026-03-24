import React from 'react';
import { act } from '@testing-library/react';
import { render, screen, waitFor, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Alarms } from './Alarms';

const mocks = vi.hoisted(() => ({
    reportMock: vi.fn(),
    showTemporalMock: vi.fn(),
    subscribeMock: vi.fn(),
    sseStatus: 'connected' as 'connecting' | 'connected' | 'disconnected',
    componentFactory: () => React.createElement('div', { 'data-testid': 'message-component' }, 'Missatge'),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: ((() => ({
        isReady: true,
        artifactReport: mocks.reportMock,
    })) as unknown) as typeof import('reactlib').useResourceApiService,
}));
vi.mock('./SseProvider', () => ({
    useSseContext: () => ({
        connected: mocks.sseStatus === 'connected',
        status: mocks.sseStatus,
        subscribe: mocks.subscribeMock,
    }),
}));

vi.mock('./MessageShow', () => ({
    useMessage: () => ({
        showTemporal: mocks.showTemporalMock,
        component: mocks.componentFactory(),
    }),
}));
vi.mock('@mui/x-data-grid-pro', () => ({}));
vi.mock('@mui/x-data-grid', () => ({}));
vi.mock('@mui/x-data-grid/locales', () => ({}));

vi.mock('react-i18next', () => ({
    initReactI18next: {
        type: '3rdParty',
        init: () => undefined,
    },
    useTranslation: () => ({
        t: (selector: any, params?: { count?: number }) =>
            selector({
                page: {
                    alarma: {
                        snackbar: {
                            title: 'Alarmes',
                            existingAlarms: `Hi ha ${params?.count ?? 0} alarmes actives`,
                            newAlarms: `Hi ha ${params?.count ?? 0} alarmes noves`,
                        },
                    },
                },
            }),
    }),
}));

describe('Alarms', () => {
    beforeEach(() => {
        mocks.sseStatus = 'connected';
        mocks.subscribeMock.mockImplementation((_eventType: string, _listener: unknown) => vi.fn());
        let intervalId = 1;
        vi.spyOn(globalThis, 'setInterval').mockImplementation(
            ((callback: TimerHandler) => {
                (globalThis as any).__alarmsIntervalCallback__ = callback;
                return intervalId++ as unknown as ReturnType<typeof setInterval>;
            }) as unknown as typeof setInterval
        );
        vi.spyOn(globalThis, 'clearInterval').mockImplementation(() => undefined);
    });

    afterEach(() => {
        delete (globalThis as any).__alarmsIntervalCallback__;
        vi.restoreAllMocks();
        vi.clearAllMocks();
    });

    it('Alarms_quanHiHaAlarmesInicials_mostraElComptadorIElMissatgeInicial', async () => {
        // Comprova que el component informa d'alarmes existents en la primera càrrega i actualitza el badge.
        mocks.reportMock.mockResolvedValue([{ id: 1 }, { id: 2 }]);

        render(
            <MemoryRouter>
                <Alarms />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(mocks.showTemporalMock).toHaveBeenCalledWith(
                'Alarmes',
                'Hi ha 2 alarmes actives',
                'error',
                undefined,
                5000
            );
        });
        expect(screen.getByText('2')).toBeInTheDocument();
        expect(screen.getByTestId('message-component')).toBeInTheDocument();
    });

    it('Alarms_quanLApiEstaPreparada_registraLaRefrescadaPeriodica', async () => {
        // Verifica que el component fa fallback a polling si l'alta al SSE falla.
        mocks.sseStatus = 'disconnected';
        mocks.reportMock.mockResolvedValue([{ id: 1 }]);

        render(
            <MemoryRouter>
                <Alarms />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(globalThis.setInterval).toHaveBeenCalledWith(expect.any(Function), 30_000);
        });
    });

    it('Alarms_quanElSseEsRegistra_noProgramaPolling', async () => {
        // Comprova que amb SSE operatiu no s'activa el polling i que el payload rebut actualitza el badge.
        mocks.reportMock.mockResolvedValue([{ id: 1 }]);
        let sseListener: ((event: { payload?: { id: number }[] }) => void) | undefined;
        mocks.subscribeMock.mockImplementation((_eventType: string, listener: typeof sseListener) => {
            sseListener = listener;
            return vi.fn();
        });

        render(
            <MemoryRouter>
                <Alarms />
            </MemoryRouter>
        );

        await waitFor(() => {
            expect(mocks.subscribeMock).toHaveBeenCalled();
        });
        expect(globalThis.setInterval).not.toHaveBeenCalledWith(expect.any(Function), 30_000);

        act(() => {
            sseListener?.({ payload: [{ id: 1 }, { id: 2 }] });
        });

        await waitFor(() => {
            expect(screen.getByText('2')).toBeInTheDocument();
        });
    });

    it('Alarms_quanRepUnEventSse_actualitzaElNumeroDelBotoDalarmesActives', async () => {
        mocks.reportMock.mockResolvedValue([{ id: 1 }]);
        let sseListener: ((event: { payload?: { id: number }[] }) => void) | undefined;
        mocks.subscribeMock.mockImplementation((_eventType: string, listener: typeof sseListener) => {
            sseListener = listener;
            return vi.fn();
        });

        await act(async () => {
            render(
                <MemoryRouter>
                    <Alarms />
                </MemoryRouter>
            );
        });

        const alarmsButton = screen.getByRole('link');

        await waitFor(() => {
            expect(within(alarmsButton).getByText('1')).toBeInTheDocument();
        });

        act(() => {
            sseListener?.({ payload: [{ id: 1 }, { id: 2 }, { id: 3 }] });
        });

        await waitFor(() => {
            expect(within(alarmsButton).getByText('3')).toBeInTheDocument();
        });
    });
});
