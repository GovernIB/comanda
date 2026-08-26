import { fireEvent, render, screen } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import TitolWidgetVisualization, {
    resolveVoraCostat,
    resolveDireccioSubtitol,
    resolveSeparacioSubtitolEstils,
    resolveTitolEstilsOverride,
    resolveSubtitolEstilsOverride,
    resolveVoraSpacingEstils,
} from './TitolWidgetVisualization';

describe('TitolWidgetVisualization', () => {
    it('TitolWidgetVisualization_quanEsRenderitza_mostraTitolISubtitol', () => {
        // Comprova que el component mostra el títol i el subtítol informats.
        render(
            <ThemeProvider theme={createTheme()}>
                <TitolWidgetVisualization
                    titol="Indicador principal"
                    subtitol="Detall resumit"
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
                    mostrarVoraTop={true}
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
                    mostrarVoraBottom={false}
                    colorFons="#ffffff"
                />
            </ThemeProvider>
        );

        expect(screen.getByText('Indicador principal')).toBeInTheDocument();
    });

    it('TitolWidgetVisualization_quanPosicioSubtitolEsCostat_esRenderitzenAmbdosTextos', () => {
        render(
            <ThemeProvider theme={createTheme()}>
                <TitolWidgetVisualization
                    titol="Indicador principal"
                    subtitol="Detall resumit"
                    posicioSubtitol="COSTAT"
                    separacioSubtitol={16}
                />
            </ThemeProvider>
        );

        expect(screen.getByText('Indicador principal')).toBeInTheDocument();
        expect(screen.getByText('Detall resumit')).toBeInTheDocument();
    });

    describe('resolveVoraCostat', () => {
        it('resolveVoraCostat_quanNoSHaDeMostrar_retornaNone', () => {
            expect(resolveVoraCostat(false, '#ff0000', 3, '#cccccc')).toBe('none');
        });

        it('resolveVoraCostat_quanSHaDeMostrarAmbColorIGruixPropis_elsUtilitza', () => {
            expect(resolveVoraCostat(true, '#ff0000', 3, '#cccccc')).toBe('3px solid #ff0000');
        });

        it('resolveVoraCostat_quanNoHiHaColorPropi_utilitzaElColorPerDefecte', () => {
            expect(resolveVoraCostat(true, undefined, 3, '#cccccc')).toBe('3px solid #cccccc');
        });

        it('resolveVoraCostat_quanNoHiHaGruixPropi_utilitza1PxPerDefecte', () => {
            expect(resolveVoraCostat(true, '#ff0000', undefined, '#cccccc')).toBe('1px solid #ff0000');
        });
    });

    describe('resolveDireccioSubtitol', () => {
        it('resolveDireccioSubtitol_quanEsSota_retornaColumna', () => {
            expect(resolveDireccioSubtitol('SOTA')).toEqual({ display: 'flex', flexDirection: 'column' });
        });

        it('resolveDireccioSubtitol_quanEsCostat_retornaFilaAmbBaseline', () => {
            // Sense display:'flex' explícit, flexDirection/alignItems no tenen efecte (el Box passa a
            // renderitzar-se en block i el subtítol acaba sempre a sota, mai al costat).
            expect(resolveDireccioSubtitol('COSTAT')).toEqual({ display: 'flex', flexDirection: 'row', alignItems: 'baseline' });
        });
    });

    describe('resolveSeparacioSubtitolEstils', () => {
        it('resolveSeparacioSubtitolEstils_quanEsSota_aplicaMarginTop', () => {
            expect(resolveSeparacioSubtitolEstils('SOTA', 8)).toEqual({ mt: '8px' });
        });

        it('resolveSeparacioSubtitolEstils_quanEsCostat_aplicaMarginLeft', () => {
            expect(resolveSeparacioSubtitolEstils('COSTAT', 8)).toEqual({ ml: '8px' });
        });
    });

    describe('resolveTitolEstilsOverride', () => {
        // El títol té width:100% per defecte (apilat, SOTA) perquè pugui truncar-se amb el·lipsi. Quan el
        // subtítol va al costat (COSTAT), aquest 100% faria que el títol ocupàs tota la fila i el subtítol
        // quedàs empès al marge dret del component, en lloc d'anar-hi enganxat.
        it('resolveTitolEstilsOverride_quanEsSota_noSobreescriuRes', () => {
            expect(resolveTitolEstilsOverride('SOTA')).toEqual({});
        });

        it('resolveTitolEstilsOverride_quanEsCostat_amplaAmpladaAutomaticaSenseCreixer', () => {
            expect(resolveTitolEstilsOverride('COSTAT')).toEqual({ width: 'auto', flexShrink: 0, flexGrow: 0 });
        });
    });

    describe('resolveSubtitolEstilsOverride', () => {
        it('resolveSubtitolEstilsOverride_quanEsSota_noSobreescriuRes', () => {
            expect(resolveSubtitolEstilsOverride('SOTA')).toEqual({});
        });

        it('resolveSubtitolEstilsOverride_quanEsCostat_eliminaLAmpladaMinimaINoCreix', () => {
            expect(resolveSubtitolEstilsOverride('COSTAT')).toEqual({ minWidth: 0, flexGrow: 0 });
        });
    });

    describe('resolveVoraSpacingEstils', () => {
        // Sense vores, el text no ha de tenir cap separació addicional respecte de la vora del component.
        it('resolveVoraSpacingEstils_senseVoraEsquerraNiInferior_noAplicaCapPadding', () => {
            expect(resolveVoraSpacingEstils(false, false)).toEqual({ pl: 0, pb: 0 });
        });

        // Amb vora esquerra, cal separació horitzontal perquè el text no toqui la línia de la vora.
        it('resolveVoraSpacingEstils_ambVoraEsquerra_afegeixPaddingLeft', () => {
            expect(resolveVoraSpacingEstils(true, false)).toEqual({ pl: 2, pb: 0 });
        });

        // Amb vora inferior, cal separació vertical perquè el text no toqui la línia de la vora.
        it('resolveVoraSpacingEstils_ambVoraInferior_afegeixPaddingBottom', () => {
            expect(resolveVoraSpacingEstils(false, true)).toEqual({ pl: 0, pb: 2 });
        });

        it('resolveVoraSpacingEstils_ambAmbduesVores_afegeixElsDosPaddings', () => {
            expect(resolveVoraSpacingEstils(true, true)).toEqual({ pl: 2, pb: 2 });
        });
    });
});
