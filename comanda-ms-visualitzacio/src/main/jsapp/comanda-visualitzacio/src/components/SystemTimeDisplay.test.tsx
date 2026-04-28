import { act, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import SystemTimeDisplay from './SystemTimeDisplay';

describe('SystemTimeDisplay', () => {
    beforeEach(() => {
        vi.useFakeTimers();
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.clearAllMocks();
    });

    it('SystemTimeDisplay_quanEsRenderitza_mostraLaDataIHoraActuals', () => {
        // Comprova que el component pinta la data i l'hora actuals amb el format esperat.
        vi.setSystemTime(new Date('2025-03-13T10:15:30'));

        render(
            <ThemeProvider theme={createTheme()}>
                <SystemTimeDisplay />
            </ThemeProvider>
        );

        expect(screen.getByText('13/03/2025')).toBeInTheDocument();
        expect(screen.getByText('10:15:30')).toBeInTheDocument();
    });

    it('SystemTimeDisplay_quanPassaUnSegon_actualitzaLHoraMostrada', () => {
        // Verifica que el temporitzador intern actualitza l'hora visible cada segon.
        vi.setSystemTime(new Date('2025-03-13T10:15:30'));

        render(
            <ThemeProvider theme={createTheme()}>
                <SystemTimeDisplay />
            </ThemeProvider>
        );

        act(() => {
            vi.advanceTimersByTime(1000);
        });

        expect(screen.getByText('10:15:31')).toBeInTheDocument();
    });
});
