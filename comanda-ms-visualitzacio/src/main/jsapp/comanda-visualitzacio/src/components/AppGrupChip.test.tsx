import React from 'react';
import { render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it } from 'vitest';
import AppGrupChip from './AppGrupChip';

const renderWithTheme = (ui: React.ReactElement, mode: 'light' | 'dark' = 'light') => {
    const theme = createTheme({ palette: { mode } });
    return render(<ThemeProvider theme={theme}>{ui}</ThemeProvider>);
};

describe('AppGrupChip', () => {
    it('AppGrupChip_quanNoHiHaNom_noRenderitzaRes', () => {
        const { container } = renderWithTheme(<AppGrupChip nom={undefined} />);
        expect(container.firstChild).toBeNull();

        const { container: emptyContainer } = renderWithTheme(<AppGrupChip nom="" />);
        expect(emptyContainer.firstChild).toBeNull();
    });

    it('AppGrupChip_quanHiHaNom_renderitzaElChipAmbElNom', () => {
        renderWithTheme(<AppGrupChip nom="Grup Principal" />);
        expect(screen.getByText('Grup Principal')).toBeInTheDocument();
    });

    it('AppGrupChip_enTemaClar_utilitzaColorClar', () => {
        renderWithTheme(
            <AppGrupChip nom="Grup Clar" colorClar="#e3f2fd" colorFosc="#0d47a1" />,
            'light'
        );
        const chip = screen.getByText('Grup Clar').closest('.MuiChip-root');
        expect(chip).toBeInTheDocument();
    });

    it('AppGrupChip_enTemaFosc_utilitzaColorFosc', () => {
        renderWithTheme(
            <AppGrupChip nom="Grup Fosc" colorClar="#e3f2fd" colorFosc="#0d47a1" />,
            'dark'
        );
        const chip = screen.getByText('Grup Fosc').closest('.MuiChip-root');
        expect(chip).toBeInTheDocument();
    });

    it('AppGrupChip_quanColorFoscNoDefinit_fallbackAColorClar', () => {
        renderWithTheme(
            <AppGrupChip nom="Grup Fallback" colorClar="#ffcc80" />,
            'dark'
        );
        expect(screen.getByText('Grup Fallback')).toBeInTheDocument();
    });

    it('AppGrupChip_ambColorInvalid_noLlançaExcepcio', () => {
        renderWithTheme(
            <AppGrupChip nom="Grup Invalid" colorClar="not-a-color" />,
            'light'
        );
        expect(screen.getByText('Grup Invalid')).toBeInTheDocument();
    });

    it('AppGrupChip_ambMidaMedium_renderitzaCorrectament', () => {
        renderWithTheme(<AppGrupChip nom="Grup Medium" size="medium" />);
        expect(screen.getByText('Grup Medium')).toBeInTheDocument();
    });
});

