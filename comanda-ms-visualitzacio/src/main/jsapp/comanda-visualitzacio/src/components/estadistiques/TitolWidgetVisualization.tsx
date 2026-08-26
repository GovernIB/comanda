import React from 'react';
import { Box, Typography } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import estils from './WidgetEstils';
import { useWidgetTheme } from './useWidgetTheme';

export type PosicioSubtitol = 'SOTA' | 'COSTAT';

export interface TitolWidgetVisualizationProps {
    titol: string;
    subtitol?: string;
    midaFontTitol?: number;
    midaFontSubtitol?: number;
    colorTitol?: string;
    colorSubtitol?: string;
    colorFons?: string;
    destacat?: boolean;
    onClick?: () => void;
    posicioSubtitol?: PosicioSubtitol;
    separacioSubtitol?: number;
    mostrarVoraTop?: boolean;
    colorVoraTop?: string;
    ampleVoraTop?: number;
    mostrarVoraRight?: boolean;
    colorVoraRight?: string;
    ampleVoraRight?: number;
    mostrarVoraBottom?: boolean;
    colorVoraBottom?: string;
    ampleVoraBottom?: number;
    mostrarVoraLeft?: boolean;
    colorVoraLeft?: string;
    ampleVoraLeft?: number;
}

/** Resol el CSS d'un costat de vora independent: "none" si no s'ha de mostrar, o "<gruix>px solid <color>". **/
export const resolveVoraCostat = (
    mostrar: boolean | undefined,
    color: string | undefined,
    ample: number | undefined,
    colorPerDefecte: string
): string => (mostrar ? `${ample ?? 1}px solid ${color || colorPerDefecte}` : 'none');

/** Resol l'estil del contenidor (flexDirection/alignItems) segons si el subtítol va a sota o al costat del títol. **/
export const resolveDireccioSubtitol = (posicioSubtitol: PosicioSubtitol): { display: 'flex'; flexDirection: 'column' | 'row'; alignItems?: string } =>
    posicioSubtitol === 'COSTAT'
        ? { display: 'flex', flexDirection: 'row', alignItems: 'baseline' }
        : { display: 'flex', flexDirection: 'column' };

/** Marge entre títol i subtítol: a l'esquerra si van en línia (COSTAT), a sobre si van apilats (SOTA). **/
export const resolveSeparacioSubtitolEstils = (posicioSubtitol: PosicioSubtitol, separacioSubtitol: number) =>
    posicioSubtitol === 'COSTAT' ? { ml: `${separacioSubtitol}px` } : { mt: `${separacioSubtitol}px` };

/**
 * El títol té width:100% per defecte (apilat, SOTA) perquè pugui truncar-se amb el·lipsi. En COSTAT això
 * faria que el títol ocupàs tota la fila i empenyés el subtítol fins al marge dret del component, en lloc
 * d'anar-hi enganxat amb el marge de resolveSeparacioSubtitolEstils.
 **/
export const resolveTitolEstilsOverride = (posicioSubtitol: PosicioSubtitol) =>
    posicioSubtitol === 'COSTAT' ? { width: 'auto', flexShrink: 0, flexGrow: 0 } : {};

/** Anul·la el minWidth/flexGrow de descText (pensats per texts apilats a tot l'ample) quan el subtítol va al costat. **/
export const resolveSubtitolEstilsOverride = (posicioSubtitol: PosicioSubtitol) =>
    posicioSubtitol === 'COSTAT' ? { minWidth: 0, flexGrow: 0 } : {};

/**
 * Separació entre el text i les vores esquerra/inferior quan estan actives, perquè el text no hi quedi
 * enganxat. Només cal quan la vora corresponent es mostra: sense vora no hi ha línia amb la qual xocar.
 **/
export const resolveVoraSpacingEstils = (mostrarVoraLeft: boolean, mostrarVoraBottom: boolean) => ({
    pl: mostrarVoraLeft ? 2 : 0,
    pb: mostrarVoraBottom ? 2 : 0,
});

const TitolWidgetVisualization: React.FC<TitolWidgetVisualizationProps> = (props) => {
    const {
        titol,
        subtitol,
        midaFontTitol,
        midaFontSubtitol,
        colorSubtitol,
        onClick,
        posicioSubtitol = 'SOTA',
        separacioSubtitol = 0,
        mostrarVoraTop = false,
        colorVoraTop,
        ampleVoraTop,
        mostrarVoraRight = false,
        colorVoraRight,
        ampleVoraRight,
        mostrarVoraBottom = true,
        colorVoraBottom,
        ampleVoraBottom,
        mostrarVoraLeft = false,
        colorVoraLeft,
        ampleVoraLeft,
    } = props;

    const theme = useTheme();

    const {
        textColor,
        backgroundColor,
        bg,
    } = useWidgetTheme({
        colorText: props.colorTitol,
        colorFons: props.colorFons,
    });

    const titleEstils = {
        ...estils.titleText,
        fontSize: midaFontTitol ? `${midaFontTitol}px` : estils.titleText.fontSize,
        ...resolveTitolEstilsOverride(posicioSubtitol),
    };

    const subtitolEstils = {
        ...estils.descText(colorSubtitol || textColor),
        fontSize: midaFontSubtitol ? `${midaFontSubtitol}px` : estils.descText(colorSubtitol || textColor).fontSize,
        ...resolveSubtitolEstilsOverride(posicioSubtitol),
        ...resolveSeparacioSubtitolEstils(posicioSubtitol, separacioSubtitol),
    };

    const voraDivider = theme.palette.divider;

    const borderStyle = {
        ...estils.paperContainer(
            backgroundColor,
            bg === 'none' ? backgroundColor : bg,
            textColor,
            false,
            0,
            voraDivider,
            onClick,
            theme
        ),
        // Els títols del dashboard no han de tenir cantonades arrodonides, independentment de quines
        // vores es mostrin.
        borderRadius: 'none',
        borderTop: resolveVoraCostat(mostrarVoraTop, colorVoraTop, ampleVoraTop, voraDivider),
        borderRight: resolveVoraCostat(mostrarVoraRight, colorVoraRight, ampleVoraRight, voraDivider),
        borderBottom: resolveVoraCostat(mostrarVoraBottom, colorVoraBottom, ampleVoraBottom, voraDivider),
        borderLeft: resolveVoraCostat(mostrarVoraLeft, colorVoraLeft, ampleVoraLeft, voraDivider),
        ...resolveVoraSpacingEstils(mostrarVoraLeft, mostrarVoraBottom),
        // El contingut s'alinea a la part de sota: si el component ocupa molt d'espai vertical, l'espai
        // buit queda a la part de dalt i el text es manté sempre a prop de la vora inferior (si n'hi ha).
        justifyContent: 'flex-end',
    };

    const contentStyle = resolveDireccioSubtitol(posicioSubtitol);

    return (
        <Box onClick={onClick} sx={borderStyle}>
            <Box sx={contentStyle}>
                <Typography sx={titleEstils}>{titol}</Typography>
                <Typography sx={subtitolEstils}>{subtitol}</Typography>
            </Box>
        </Box>
    );
};
export default TitolWidgetVisualization;
