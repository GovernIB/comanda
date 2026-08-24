import React from 'react';
import { useTranslation } from 'react-i18next';
import { Box, Typography, Chip, Skeleton, Paper } from '@mui/material';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import { useTheme } from '@mui/material/styles';
import estils from './WidgetEstils';
import { createTransparentColor } from '../../util/colorUtil';

interface WidgetHeaderProps {
    titol?: string;
    entornCodi?: string;
    loading?: boolean;
    midaFontTitol?: number;
    isWhiteBackground: boolean;
    backgroundColor: string;
    voraColor: string;
    contrastTextColor: string;
    dashboardEntornCodi?: string;
}

export const WidgetHeader: React.FC<WidgetHeaderProps> = React.memo(({
    titol,
    entornCodi,
    loading,
    midaFontTitol,
    isWhiteBackground,
    backgroundColor,
    voraColor,
    contrastTextColor,
    dashboardEntornCodi,
}) => {
    const theme = useTheme();

    const shouldShowEntornCodi = entornCodi && entornCodi !== dashboardEntornCodi;

    const titleEstils = {
        ...estils.titleText,
        fontSize: midaFontTitol ? `${midaFontTitol}px` : estils.titleText.fontSize,
    };

    const entornChipSx = {
        ...estils.entornCodi,
        color: contrastTextColor,
        backgroundColor: isWhiteBackground
            ? theme.palette.grey[200]
            : createTransparentColor(backgroundColor, 0.35),
        border: `1px solid ${voraColor}`,
    };

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
                    {shouldShowEntornCodi && (
                        <Box sx={estils.iconContainer}>
                            <Chip sx={entornChipSx} label={entornCodi} size="small" />
                        </Box>
                    )}
                </>
            )}
        </Box>
    );
});

interface WidgetFooterProps {
    descripcio?: string;
    textColor: string;
    loading?: boolean;
    midaFontDescripcio?: number;
    canviPercentual?: string;
    midaFontCanviPercentual?: number;
}

export const WidgetFooter: React.FC<WidgetFooterProps> = React.memo(({
    descripcio,
    textColor,
    loading,
    midaFontDescripcio,
    canviPercentual,
    midaFontCanviPercentual,
}) => {
    const descEstils = {
        ...estils.descText(textColor),
        fontSize: midaFontDescripcio ? `${midaFontDescripcio}px` : estils.descText(textColor).fontSize,
    };

    const canviPercentualEstils = {
        ...estils.percText(textColor),
        fontSize: midaFontCanviPercentual ? `${midaFontCanviPercentual}px` : estils.percText(textColor).fontSize,
    };

    return (
        <Box sx={estils.footerContainer}>
            {loading ? (
                <>
                    <Skeleton width="60%" height={24} />
                    {canviPercentual && <Skeleton width="20%" height={24} />}
                </>
            ) : (
                <>
                    {descripcio && <Typography sx={descEstils}>{descripcio}</Typography>}
                    {canviPercentual && <Typography sx={canviPercentualEstils}>{canviPercentual}%</Typography>}
                </>
            )}
        </Box>
    );
});

interface WidgetErrorDisplayProps {
    errorMsg?: string;
    errorTrace?: string;
}

export const WidgetErrorDisplay: React.FC<WidgetErrorDisplayProps> = React.memo(({
    errorMsg,
    errorTrace,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();

    return (
        <Box sx={{ flex: 1, p: 2, overflow: 'auto' }}>
            <Accordion
                sx={{...estils.errorAccordion, pointerEvents: 'auto'}}
                onMouseDown={(event) => event.stopPropagation()}
            >
                <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={estils.errorSummary(theme)}>
                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                        <ErrorOutlineIcon sx={estils.errorIcon(theme)} />
                        <Typography sx={{fontSize: '0.75rem'}}>{errorMsg || t($ => $.common.error)}</Typography>
                    </Box>
                </AccordionSummary>
                <AccordionDetails sx={estils.errorDetails(theme)}>
                    {errorTrace || t($ => $.page.widget.noErrorTrace)}
                </AccordionDetails>
            </Accordion>
        </Box>
    );
});

interface WidgetContainerProps {
    children: React.ReactNode;
    bgColor: string;
    bg: string;
    textColor: string;
    mostrarVora?: boolean;
    voraAmple: number;
    voraColor: string;
    onClick?: () => void;
}

export const WidgetContainer: React.FC<WidgetContainerProps> = React.memo(({
    children,
    bgColor,
    bg,
    textColor,
    mostrarVora = false,
    voraAmple,
    voraColor,
    onClick,
}) => {
    const theme = useTheme();

    return (
        <Paper
            elevation={2}
            onClick={onClick}
            sx={estils.paperContainer(bgColor, bg, textColor, mostrarVora, voraAmple, voraColor, onClick, theme)}
        >
            {children}
        </Paper>
    );
});
