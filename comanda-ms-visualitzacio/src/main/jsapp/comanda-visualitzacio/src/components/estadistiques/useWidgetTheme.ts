import { useMemo } from 'react';
import { useTheme } from '@mui/material/styles';
import { createTransparentColor, isLightColor } from '../../util/colorUtil';

interface WidgetThemeColors {
    textColor: string;
    backgroundColor: string;
    voraColor: string;
    isWhiteBackground: boolean;
    contrastTextColor: string;
    bgColor: string;
    bg: string;
    voraAmple: number;
}

interface WidgetThemeOptions {
    colorText?: string;
    colorFons?: string;
    colorVora?: string;
    mostrarVora?: boolean;
    ampleVora?: number;
}

export const useWidgetTheme = (options: WidgetThemeOptions): WidgetThemeColors => {
    const theme = useTheme();
    
    return useMemo(() => {
        const {
            colorText,
            colorFons,
            colorVora,
            mostrarVora = false,
            ampleVora = 1,
        } = options;

        const textColor = colorText || theme.palette.text.primary;
        const backgroundColor = colorFons || theme.palette.background.paper;
        const voraColor = colorVora || theme.palette.divider;
        const isWhiteBackground = !colorFons || isLightColor(backgroundColor);
        const contrastTextColor = isWhiteBackground ? '#000000' : '#FFFFFF';
        
        const bgColor = isWhiteBackground ? backgroundColor + ' !important' : 'transparent';
        const bg = isWhiteBackground 
            ? 'none' 
            : `linear-gradient(to bottom, ${backgroundColor}, ${createTransparentColor(backgroundColor, 0.75)})`;
        
        const voraAmple = (mostrarVora ? ampleVora : 0);

        return {
            textColor,
            backgroundColor,
            voraColor,
            isWhiteBackground,
            contrastTextColor,
            bgColor,
            bg,
            voraAmple,
        };
    }, [
        options.colorText,
        options.colorFons,
        options.colorVora,
        options.mostrarVora,
        options.ampleVora,
        theme.palette.text.primary,
        theme.palette.background.paper,
        theme.palette.divider,
    ]);
};