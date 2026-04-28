import React from 'react';
import { render, screen, within } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { afterEach, describe, expect, it, vi } from 'vitest';
import ResponsiveCardTable from './ResponsiveCardTable';

const mocks = vi.hoisted(() => ({
    useMediaQueryMock: vi.fn(),
}));

vi.mock('@mui/material/useMediaQuery', () => ({
    default: (query: unknown) => mocks.useMediaQueryMock(query),
}));

const renderComponent = (ui: React.ReactElement) =>
    render(<ThemeProvider theme={createTheme()}>{ui}</ThemeProvider>);

describe('ResponsiveCardTable', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('ResponsiveCardTable_quanNoHiHaSeccions_mostraElMissatgeSenseInformacio', () => {
        // Comprova que el component informa correctament quan no hi ha dades a presentar.
        mocks.useMediaQueryMock.mockReturnValue(false);

        renderComponent(
            <ResponsiveCardTable
                title="Resum"
                noInfoMessage="Sense dades disponibles"
                tableSections={[]}
            />
        );

        expect(screen.getByText('Resum')).toBeInTheDocument();
        expect(screen.getByText('Sense dades disponibles')).toBeInTheDocument();
        expect(screen.queryByRole('table')).not.toBeInTheDocument();
    });

    it('ResponsiveCardTable_quanElBreakpointNoSAsssoleix_renderitzaLesFilesEnFormatVertical', () => {
        // Verifica que en pantalles petites cada capçalera es mostra al costat del seu contingut.
        mocks.useMediaQueryMock.mockReturnValue(false);

        renderComponent(
            <ResponsiveCardTable
                title="Resum"
                tableSections={[
                    { id: 'a', headerName: 'Nom', cellContent: <span>Aplicació 1</span> },
                    { id: 'b', headerName: 'Estat', cellContent: <span>Activa</span> },
                ]}
            />
        );

        const table = screen.getByRole('table');
        const rows = within(table).getAllByRole('row');

        expect(rows).toHaveLength(2);
        expect(within(rows[0]).getByText('Nom')).toBeInTheDocument();
        expect(within(rows[0]).getByText('Aplicació 1')).toBeInTheDocument();
        expect(screen.queryByRole('rowgroup', { name: '' })).toBeInTheDocument();
    });

    it('ResponsiveCardTable_quanElBreakpointSAsssoleix_renderitzaCapcaleresIValorsEnFilesSeparades', () => {
        // Comprova que en pantalles grans el component separa capçaleres i valors en capçalera i cos.
        mocks.useMediaQueryMock.mockReturnValue(true);

        renderComponent(
            <ResponsiveCardTable
                title="Resum"
                tableSections={[
                    { id: 'a', headerName: 'Nom', cellContent: <span>Aplicació 1</span> },
                    { id: 'b', headerName: 'Estat', cellContent: <span>Activa</span> },
                ]}
            />
        );

        const table = screen.getByRole('table');
        const rows = within(table).getAllByRole('row');

        expect(rows).toHaveLength(2);
        expect(within(rows[0]).getByText('Nom')).toBeInTheDocument();
        expect(within(rows[0]).getByText('Estat')).toBeInTheDocument();
        expect(within(rows[1]).getByText('Aplicació 1')).toBeInTheDocument();
        expect(within(rows[1]).getByText('Activa')).toBeInTheDocument();
    });
});
