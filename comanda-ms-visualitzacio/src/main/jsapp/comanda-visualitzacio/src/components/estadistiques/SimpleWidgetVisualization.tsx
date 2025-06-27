import React from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Icon from '@mui/material/Icon';
import { useTheme } from '@mui/material/styles';
import { numberFormat, useBaseAppContext } from 'reactlib';
import {createTransparentColor, isWhiteColor} from "../../util/colorUtil";
import estils from "./WidgetEstils.ts";
import Chip from "@mui/material/Chip";
import Skeleton from '@mui/material/Skeleton';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';

// Define the props for the SimpleWidgetVisualization component
export interface SimpleWidgetVisualizationProps {
    // Widget data
    entornCodi?: string;
    titol?: string;
    descripcio?: string;

    valor?: number | string;
    unitat?: string;
    canviPercentual?: string;

    // Atributs visuals
    icona?: string | undefined;
    colorText?: string;
    colorFons?: string;
    colorIcona?: string;
    colorFonsIcona?: string;
    colorTextDestacat?: string;
    mostrarVora?: boolean;
    colorVora?: string;
    ampleVora?: number | undefined;

    // Additional props
    loading?: boolean;
    preview?: boolean;
    error?: boolean;
    errorMsg?: string;
    errorTrace?: string;
    onClick?: () => void;

    midaFontTitol?: number,
    midaFontDescripcio?: number,
    midaFontValor?: number,
    midaFontUnitats?: number,
    midaFontCanviPercentual?: number,
}

// Components
const WidgetTitle: React.FC<{
    titol: string;
    entornCodi: string;
    loading?: boolean;
    midaFontTitol?: number;
}> = ({titol, entornCodi, loading, midaFontTitol}) => {

    const titleEstils = {
        ...estils.titleText,
        fontSize: midaFontTitol ?`${midaFontTitol}px` :estils.titleText.fontSize
    }

    return (
        <Box sx={estils.titleContainer}>
            {loading ? (
                <>
                    <Skeleton width="70%" height={32} />
                    <Box sx={estils.iconContainer}>
                        <Skeleton width={40} height={24} />
                    </Box>
                </>
            ) : (
                <>
                    <Typography sx={titleEstils}>{titol}</Typography>
                    <Box sx={estils.iconContainer}>
                        <Chip sx={estils.entornCodi} label={entornCodi} size={"small"} />
                    </Box>
                </>
            )}
        </Box>
    );
};

const WidgetContent: React.FC<{ valor: string | number; unitat: string; preview: boolean; loading?: boolean, midaFontValor?:number, midaFontUnitats?:number }> = ({
    valor,
    unitat,
    preview,
    loading,
    midaFontValor,
    midaFontUnitats,
}) => {
    const valorEstils = {
        ...estils.valueText(preview),
        fontSize: midaFontValor ?`${midaFontValor}px` :estils.valueText(preview).fontSize
    }
    const unitatEstils = {
        ...estils.unitText(preview),
        fontSize: midaFontUnitats ?`${midaFontUnitats}px` :estils.unitText(preview).fontSize
    }

    return <Box sx={estils.contentContainer}>
        {loading ? (
            <>
                <Box sx={{...estils.contentText(preview), width: '10em'}}>
                    <Skeleton width="100%" height={80}/>
                </Box>
                <Box sx={{...estils.contentText(preview), width: '4em'}}>
                    <Skeleton width="100%" height={20}/>
                </Box>
            </>
        ) : (
            <>
                <Box sx={estils.contentText(preview)}><Typography sx={valorEstils}>{valor}</Typography></Box>
                <Box sx={estils.contentText(preview)}><Typography sx={unitatEstils}>{unitat}</Typography></Box>
            </>
        )}
    </Box>
};

const WidgetFooter: React.FC<{ descripcio: string; canviPercentual: string; textColor: string; preview: boolean; loading?: boolean, midaFontDescripcio?: number, midaFontCanviPercentual?: number }> = ({
    descripcio,
    canviPercentual,
    textColor,
    preview,
    loading,
    midaFontDescripcio,
    midaFontCanviPercentual,
}) => {
    const descEstils = {
        ...estils.descText(textColor),
        fontSize: midaFontDescripcio ?`${midaFontDescripcio}px` :estils.descText(textColor).fontSize
    }
    const canviPercentualEstils = {
        ...estils.percText(textColor),
        fontSize: midaFontCanviPercentual ?`${midaFontCanviPercentual}px` :estils.percText(textColor).fontSize
    }
    return <Box sx={estils.footerContainer}>
        {loading ? (
            <>
                <Skeleton width="60%" height={24}/>
                {canviPercentual && <Skeleton width="20%" height={24}/>}
            </>
        ) : (
            <>
                <Typography sx={descEstils}>{descripcio}</Typography>
                {canviPercentual && (<Typography sx={canviPercentualEstils}>{canviPercentual}</Typography>)}
            </>
        )}
    </Box>
};

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
        entornCodi = props.entornCodi || 'DEV',
        icona,
        mostrarVora = false,
        ampleVora,
        preview = false,
        loading = false,
        error = false,
        errorMsg,
        errorTrace,
        onClick,
        midaFontTitol,
        midaFontDescripcio,
        midaFontValor,
        midaFontUnitats,
        midaFontCanviPercentual,
    } = props;

    const theme = useTheme();
    const {currentLanguage} = useBaseAppContext();
    const {textColor, backgroundColor, iconColor, iconBgColor, voraColor, highlightTextColor, isWhiteBackground} = useWidgetColors(props, theme);
    const formattedValue = typeof valor === 'number' ? numberFormat(valor, {}, currentLanguage) : valor;

    const bgColor = isWhiteBackground ? backgroundColor + ' !important' : 'transparent';
    const bg = isWhiteBackground ? 'none' : `linear-gradient(to bottom, ${backgroundColor}, ${createTransparentColor(backgroundColor, 0.75)})`;
    const voraAmple = ampleVora || (mostrarVora ? 1 : 0);

    const camelToSnakeCase = (str: string | undefined) => {
        if (!str) return undefined;
        return str
            .replace(/([A-Z])/g, (letter, index) =>
                index === 0 ? letter.toLowerCase() : `_${letter.toLowerCase()}`)
            .replace(/^_/, ''); // Eliminar _ inicial por si acaso
    };

    const snakeCaseIcona = camelToSnakeCase(icona);

    return (
        <Paper elevation={2} onClick={onClick} sx={estils.paperContainer(bgColor, bg, textColor, mostrarVora, voraAmple, voraColor, onClick, theme)}>
            <WidgetTitle titol={titol} entornCodi={entornCodi} loading={loading} midaFontTitol={midaFontTitol}/>

            {error ? (
                // Error content
                <Box sx={{ flex: 1, p: 2, overflow: auto }}>
                    <Accordion sx={{...estils.errorAccordion, pointerEvents: "auto"}} onMouseDown={(event) => {
                        event.stopPropagation(); // Evita que React-Grid-Layout bloquegi el clic
                    }}>
                        <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={estils.errorSummary(theme)}>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <ErrorOutlineIcon sx={estils.errorIcon(theme)} />
                                <Typography sx={{fontSize: '0.75rem'}}>{errorMsg || 'Error'}</Typography>
                            </Box>
                        </AccordionSummary>
                        <AccordionDetails sx={estils.errorDetails(theme)}>
                            {errorTrace || 'No error trace available'}
                        </AccordionDetails>
                    </Accordion>
                </Box>
            ) : (
                // Normal content
                <>
                    <WidgetContent valor={formattedValue} midaFontValor={midaFontValor} unitat={unitat} midaFontUnitats={midaFontUnitats} preview={preview} loading={loading}/>
                    <WidgetFooter descripcio={descripcio} midaFontDescripcio={midaFontDescripcio} canviPercentual={canviPercentual} midaFontCanviPercentual={midaFontCanviPercentual} textColor={highlightTextColor} preview={preview} loading={loading}/>
                    {!loading && <Icon sx={estils.icon(preview, iconColor, iconBgColor)}>{snakeCaseIcona}</Icon>}
                </>
            )}
        </Paper>
    );
};

export default SimpleWidgetVisualization;
