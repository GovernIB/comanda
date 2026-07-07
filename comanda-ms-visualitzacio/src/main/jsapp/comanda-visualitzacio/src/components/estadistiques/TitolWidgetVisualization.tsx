import React from 'react';
import { Box, Typography } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import estils from './WidgetEstils';
import { useWidgetTheme } from './useWidgetTheme';

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
    mostrarVora?: boolean;
    mostrarVoraBottom?: boolean;
    colorVora?: string;
    ampleVora?: number;
}

const TitolWidgetVisualization: React.FC<TitolWidgetVisualizationProps> = (props) => {
    const {
        titol,
        subtitol,
        midaFontTitol,
        midaFontSubtitol,
        colorSubtitol,
        onClick,
        mostrarVora,
        mostrarVoraBottom = true,
    } = props;

    const theme = useTheme();

    const {
        textColor,
        backgroundColor,
        voraColor,
        bg,
        voraAmple,
    } = useWidgetTheme({
        colorText: props.colorTitol,
        colorFons: props.colorFons,
        colorVora: props.colorVora,
        mostrarVora,
        ampleVora: props.ampleVora,
    });

    const titleEstils = {
        ...estils.titleText,
        fontSize: midaFontTitol ? `${midaFontTitol}px` : estils.titleText.fontSize,
    };

    const subtitolEstils = {
        ...estils.descText(colorSubtitol || textColor),
        fontSize: midaFontSubtitol ? `${midaFontSubtitol}px` : estils.descText(colorSubtitol || textColor).fontSize,
    };
    const borderStyle = {
        ...estils.paperContainer(
            backgroundColor,
            bg === 'none' ? backgroundColor : bg,
            textColor,
            mostrarVora && !mostrarVoraBottom,
            voraAmple,
            voraColor,
            onClick,
            theme
        ),
        borderBottom: mostrarVora || mostrarVoraBottom ? `${voraAmple}px solid ${voraColor}` : 'none',
        borderRadius: !mostrarVoraBottom ? '.6rem' : 'none',
    };

    return (
        <Box onClick={onClick} sx={borderStyle}>
            <Typography sx={titleEstils}>{titol}</Typography>
            <Typography sx={subtitolEstils}>{subtitol}</Typography>
        </Box>
    );
};
export default TitolWidgetVisualization;
