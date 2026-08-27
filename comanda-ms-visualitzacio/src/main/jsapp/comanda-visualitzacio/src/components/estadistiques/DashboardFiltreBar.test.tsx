import React from 'react';
import { render, waitFor } from '@testing-library/react';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import DashboardFiltreBar from './DashboardFiltreBar';
import { DashboardFiltre } from '../../types/dashboardFiltre.model.ts';

const mocks = vi.hoisted(() => ({
    findMock: vi.fn().mockResolvedValue({ rows: [] }),
}));

vi.mock('reactlib', () => ({
    useResourceApiService: () => ({
        isReady: true,
        find: mocks.findMock,
    }),
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<LocalizationProvider dateAdapter={AdapterDayjs}>{ui}</LocalizationProvider>);

const dimensioFiltre: DashboardFiltre = {
    id: 1,
    tipus: 'DIMENSIO',
    dimensioCodi: 'ORG',
    ordre: 1,
};

describe('DashboardFiltreBar', () => {
    beforeEach(() => {
        mocks.findMock.mockResolvedValue({ rows: [] });
    });

    it('DashboardFiltreBar_ambAplicacioId_escalaLaCercaDeValorsPerFilterByAppGroupByValor', async () => {
        renderComponent(
            <DashboardFiltreBar
                filtres={[dimensioFiltre]}
                value={{}}
                onChange={vi.fn()}
                aplicacioId={42}
            />
        );

        await waitFor(() => expect(mocks.findMock).toHaveBeenCalledTimes(1));
        expect(mocks.findMock).toHaveBeenCalledWith(
            expect.objectContaining({ namedQueries: ['filterByAppGroupByValor:42'] })
        );
    });

    it('DashboardFiltreBar_senseAplicacioId_noCercaValorsDeDimensio', async () => {
        renderComponent(
            <DashboardFiltreBar
                filtres={[dimensioFiltre]}
                value={{}}
                onChange={vi.fn()}
                aplicacioId={undefined}
            />
        );

        await new Promise((resolve) => setTimeout(resolve, 0));
        expect(mocks.findMock).not.toHaveBeenCalled();
    });
});
