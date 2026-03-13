import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Alarms } from './Alarms';

const mocks = vi.hoisted(() => ({
    reportMock: vi.fn(),
    showTemporalMock: vi.fn(),
    componentFactory: () => React.createElement('div', { 'data-testid': 'message-component' }, 'Missatge'),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: () => ({
        isReady: true,
        artifactReport: mocks.reportMock,
    }),
}));

vi.mock('./MessageShow', () => ({
    useMessage: () => ({
        showTemporal: mocks.showTemporalMock,
        component: mocks.componentFactory(),
    }),
}));

vi.mock('react-i18next', () => ({
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
        // Verifica que el component programa la refrescada automàtica amb l'interval esperat.
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
});
