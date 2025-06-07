import React from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Icon from '@mui/material/Icon';
import { useTheme } from '@mui/material/styles';
import { numberFormat, useBaseAppContext } from 'reactlib';
import {createTransparentColor, isWhiteColor} from "../../util/colorUtil";
import estils from "./WidgetEstils.ts";

// Define the props for the SimpleWidgetVisualization component
export interface SimpleWidgetVisualizationProps {
    // Widget data
    titol?: string;
    valor?: number | string;
    unitat?: string;
    descripcio?: string;
    canviPercentual?: string;

    // Visual attributes
    icona?: string | undefined;
    colorText?: string;
    colorFons?: string;
    colorIcona?: string;
    colorFonsIcona?: string;
    colorTextDestacat?: string;
    vora?: boolean;
    colorVora?: string;
    ampleVora?: number | undefined;

    // Additional props
    loading?: boolean;
    preview?: boolean;
    onClick?: () => void;
}

// Components
const WidgetTitle: React.FC<{
    titol: string;
    icona: string | undefined;
    preview: boolean;
    iconColor: string;
    iconBgColor: string;
}> = ({titol, icona, preview, iconColor, iconBgColor}) => {

    const camelToSnakeCase = (str: string | undefined) => {
        if (!str) return undefined;
        return str
            .replace(/([A-Z])/g, (letter, index) =>
                index === 0 ? letter.toLowerCase() : `_${letter.toLowerCase()}`)
            .replace(/^_/, ''); // Eliminar _ inicial por si acaso
    };

    const snakeCaseIcona = camelToSnakeCase(icona);
    return (
        <Box sx={estils.titleContainer}>
            <Typography sx={estils.titleText}>{titol}</Typography>
            {icona && (<Box sx={estils.iconContainer}><Icon sx={estils.icon(preview, iconColor, iconBgColor)}>{snakeCaseIcona}</Icon></Box>)}
        </Box>
    );
};

const WidgetContent: React.FC<{ valor: string | number; unitat: string; preview: boolean }> = ({
    valor,
    unitat,
    preview
}) => (
    <Box sx={estils.contentContainer}>
        <Box sx={estils.contentText(preview)}><Typography sx={estils.valueText(preview)}>{valor}</Typography></Box>
        <Box sx={estils.contentText(preview)}><Typography sx={estils.unitText(preview)}>{unitat}</Typography></Box>
    </Box>
);

const WidgetFooter: React.FC<{ descripcio: string; canviPercentual: string; textColor: string; preview: boolean }> = ({
    descripcio,
    canviPercentual,
    textColor,
    preview
}) => (
    <Box sx={estils.footerContainer}>
        <Typography sx={estils.descText(textColor)}>{descripcio}</Typography>
        {canviPercentual && (<Typography sx={estils.percText(textColor)}>{canviPercentual}</Typography>)}
    </Box>
);

const useWidgetColors = (props: SimpleWidgetVisualizationProps, theme: any) => {
    const {
        colorText,
        colorFons,
        colorIcona,
        colorFonsIcona,
        colorTextDestacat,
        colorVora,
    } = props;

    const colors = {
        text: colorText || theme.palette.text.primary,
        background: colorFons || theme.palette.background.paper,
        icon: colorIcona || '#888888',
        iconBackground: colorFonsIcona || 'transparent',
        highlightText: colorTextDestacat || '#004B99',
        border: colorVora || theme.palette.divider,
    };

    return {
        textColor: colors.text,
        backgroundColor: colors.background,
        iconColor: colors.icon,
        iconBgColor: colors.iconBackground,
        voraColor: colors.border,
        highlightTextColor: colors.highlightText,
        isWhiteBackground: !colorFons || isWhiteColor(colors.background),
    };
};

const SimpleWidgetVisualization: React.FC<SimpleWidgetVisualizationProps> = (props) => {
    const {
        titol = props.titol || 'Títol...',
        valor = props.valor || 0,
        unitat = props.unitat || '',
        descripcio = props.descripcio || 'Descripcio...',
        canviPercentual = props.canviPercentual || '',
        icona,
        vora = false,
        ampleVora,
        preview = false,
        onClick,
    } = props;

    const theme = useTheme();
    const {currentLanguage} = useBaseAppContext();
    const {textColor, backgroundColor, iconColor, iconBgColor, voraColor, highlightTextColor, isWhiteBackground} = useWidgetColors(props, theme);
    const formattedValue = typeof valor === 'number' ? numberFormat(valor, {}, currentLanguage) : valor;

    const bgColor = isWhiteBackground ? backgroundColor + ' !important' : 'transparent';
    const bg = isWhiteBackground ? 'none' : `linear-gradient(to bottom, ${backgroundColor}, ${createTransparentColor(backgroundColor, 0.75)})`;
    const voraAmple = ampleVora || (vora ? 1 : 0);

    return (
        <Paper elevation={2} onClick={onClick} sx={estils.paperContainer(bgColor, bg, textColor, vora, voraAmple, voraColor, onClick, theme)}>
            <WidgetTitle titol={titol} icona={icona} iconColor={iconColor} iconBgColor={iconBgColor} preview={preview}/>
            <WidgetContent valor={formattedValue} unitat={unitat} preview={preview}/>
            <WidgetFooter descripcio={descripcio} canviPercentual={canviPercentual} textColor={highlightTextColor} preview={preview}/>
        </Paper>
    );
};

export default SimpleWidgetVisualization;
