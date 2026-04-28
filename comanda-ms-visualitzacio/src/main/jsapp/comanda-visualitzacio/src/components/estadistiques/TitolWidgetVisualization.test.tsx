import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import TitolWidgetVisualization from './TitolWidgetVisualization';

describe('TitolWidgetVisualization', () => {
    it('TitolWidgetVisualization_quanEsRenderitza_mostraTitolISubtitol', () => {
        // Comprova que el component mostra el títol i el subtítol informats.
        render(
            <ThemeProvider theme={createTheme()}>
                <TitolWidgetVisualization
                    titol="Indicador principal"
                    subtitol="Detall resumit"
                    mostrarVora={false}
                    mostrarVoraBottom={false}
                />
            </ThemeProvider>
        );

        expect(screen.getByText('Indicador principal')).toBeInTheDocument();
        expect(screen.getByText('Detall resumit')).toBeInTheDocument();
    });

    it('TitolWidgetVisualization_quanRepOnClick_invocaElCallbackEnClicar', () => {
        // Verifica que el component exposa la interacció principal quan es prem el contenidor.
        const onClick = vi.fn();
        render(
            <ThemeProvider theme={createTheme()}>
                <TitolWidgetVisualization
                    titol="Indicador principal"
                    mostrarVora={true}
                    mostrarVoraBottom={true}
                    onClick={onClick}
                />
            </ThemeProvider>
        );

        fireEvent.click(screen.getByText('Indicador principal'));

        expect(onClick).toHaveBeenCalledTimes(1);
    });

    it('TitolWidgetVisualization_quanNoRepSubtitol_manteElContenidorSenseFallades', () => {
        // Comprova que el component continua renderitzant correctament encara que no hi hagi subtítol.
        render(
            <ThemeProvider theme={createTheme()}>
                <TitolWidgetVisualization
                    titol="Indicador principal"
                    mostrarVora={true}
                    mostrarVoraBottom={false}
                    colorFons="#ffffff"
                />
            </ThemeProvider>
        );

        expect(screen.getByText('Indicador principal')).toBeInTheDocument();
    });
});
