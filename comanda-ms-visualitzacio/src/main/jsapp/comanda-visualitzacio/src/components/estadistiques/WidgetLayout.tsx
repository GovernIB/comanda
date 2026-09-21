import React from 'react';
import { Box, Typography, Chip, Icon, Skeleton, Paper } from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { useTheme } from '@mui/material/styles';
import estils, { PERCENTAGE_TRIANGLE_MARGIN } from './WidgetEstils';
import { createTransparentColor } from '../../util/colorUtil';
import { SalutErrorBoundaryFallback } from '../salut/SalutErrorBoundaryFallback';

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
    const theme = useTheme();

    const descEstils = {
        ...estils.footerDescText(textColor),
        fontSize: midaFontDescripcio ? `${midaFontDescripcio}px` : estils.footerDescText(textColor).fontSize,
    };

    const canviPercentualEstils = {
        ...estils.percText(textColor),
        fontSize: midaFontCanviPercentual ? `${midaFontCanviPercentual}px` : estils.percText(textColor).fontSize,
    };

    const canviPercentualNumber = Number(canviPercentual);
    const trendColor = canviPercentualNumber > 0 ? theme.palette.success.main : theme.palette.error.main;

    return (
        <Box sx={{
            ...estils.footerContainer,
            justifyContent: descripcio ? 'space-between' : 'flex-end',
        }}>
            {loading ? (
                <>
                    <Skeleton width="60%" height={24} />
                    {canviPercentual && <Skeleton width="20%" height={24} />}
                </>
            ) : (
                <>
                    {descripcio && <Typography sx={descEstils}>{descripcio}</Typography>}
                    {canviPercentual && (
                        <Box sx={{ display: 'flex', alignItems: 'center', flexShrink: 0 }}>
                            {canviPercentualNumber !== 0 && (
                                <Icon sx={{
                                    color: trendColor,
                                    fontSize: canviPercentualEstils.fontSize,
                                    marginRight: PERCENTAGE_TRIANGLE_MARGIN,
                                }}>
                                    {canviPercentualNumber > 0 ? 'arrow_drop_up' : 'arrow_drop_down'}
                                </Icon>
                            )}
                            <Typography sx={canviPercentualEstils}>{canviPercentual}%</Typography>
                        </Box>
                    )}
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
    // Ha de veure's igual que un error de renderitzat del gràfic (missatge genèric + icona amb modal
    // de detall), per això es delega directament al mateix component.
    return (
        <Box sx={{ flex: 1, p: 2, overflow: 'auto' }}>
            <SalutErrorBoundaryFallback error={{ message: errorMsg, stack: errorTrace }} />
        </Box>
    );
});

export const WidgetNoAccessDisplay: React.FC = React.memo(() => {
    const theme = useTheme();

    return (
        <Box sx={{ flex: 1, p: 2, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1, textAlign: 'center' }}>
                <LockOutlinedIcon sx={{ fontSize: '2rem', color: theme.palette.text.secondary }} />
                <Typography sx={{ fontSize: '0.8rem', color: theme.palette.text.secondary }}>
                    No teniu permisos per veure dades en aquest widget
                </Typography>
            </Box>
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
