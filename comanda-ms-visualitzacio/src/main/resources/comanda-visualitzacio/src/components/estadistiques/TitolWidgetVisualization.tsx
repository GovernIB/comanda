import React from "react";
import { Box, Typography } from '@mui/material';
import estils from "./WidgetEstils.ts";
import {createTransparentColor, isWhiteColor} from "../../util/colorUtil.ts";
import {useTheme} from "@mui/material/styles";

export interface TitolWidgetVisualizationProps {
    titol: string;
    subtitol?: string;
    midaFontTitol?: number;
    midaFontSubtitol?: number;
    colorTitol?: string;
    colorSubtitol?: string;
    colorFons?: string;
    onClick?: () => void;
    mostrarVora:boolean;
    colorVora?: string;
    ampleVora?: number;
}

const useWidgetColors = (props: TitolWidgetVisualizationProps, theme: any) => {
    const {
        colorTitol = theme.palette.text.primary,
        colorFons,
        colorVora = theme.palette.divider,
    } = props;

    const backgroundColor = colorFons || theme.palette.background.paper;

    return {
        textColor: colorTitol,
        backgroundColor: backgroundColor,
        voraColor: colorVora,
        isWhiteBackground: !colorFons || isWhiteColor(backgroundColor),
    };
}

const TitolWidgetVisualization: React.FC<TitolWidgetVisualizationProps> = (props) => {
    const {
        titol = 'Títol...',
        subtitol = 'Subtítol...',
        midaFontTitol,
        midaFontSubtitol,
        colorSubtitol,
        colorFons,
        onClick,
        mostrarVora,
        ampleVora,
    } = props;

    const theme = useTheme();
    const {textColor, backgroundColor, voraColor, isWhiteBackground} = useWidgetColors(props, theme);
    const bg = isWhiteBackground ? 'none' : `linear-gradient(to bottom, ${colorFons}, ${createTransparentColor(backgroundColor, 0.75)})`;
    const voraAmple = ampleVora || (mostrarVora ? 1 : 0);

    return (
        <Box onClick={onClick} sx={estils.paperContainer(backgroundColor, bg, textColor, mostrarVora, voraAmple, voraColor, onClick, theme)}>
            <Typography color={textColor} fontSize={`${midaFontTitol}px`}>{titol}</Typography>
            <Typography color={colorSubtitol} fontSize={`${midaFontSubtitol}px`}>{subtitol}</Typography>
        </Box>
    );
}
export default TitolWidgetVisualization;
