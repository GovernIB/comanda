import React from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Icon from '@mui/material/Icon';
import { useTheme } from '@mui/material/styles';
import { numberFormat, useBaseAppContext } from 'reactlib';

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
    console.log('Icona', icona);
    console.log('SnakeCaseIcona', snakeCaseIcona);
    return (
        <Box sx={styles.titleContainer}>
            <Typography sx={styles.titleText}>{titol}</Typography>
            {icona && (<Box sx={styles.iconContainer}><Icon sx={styles.icon(preview, iconColor, iconBgColor)}>{snakeCaseIcona}</Icon></Box>)}
        </Box>
    );
};

const WidgetContent: React.FC<{ valor: string | number; unitat: string; preview: boolean }> = ({
    valor,
    unitat,
    preview
}) => (
    <Box sx={styles.contentContainer}>
        <Box sx={styles.contentText(preview)}><Typography sx={styles.valueText(preview)}>{valor}</Typography></Box>
        <Box sx={styles.contentText(preview)}><Typography sx={styles.unitText(preview)}>{unitat}</Typography></Box>
    </Box>
);

const WidgetFooter: React.FC<{ descripcio: string; canviPercentual: string; textColor: string; preview: boolean }> = ({
    descripcio,
    canviPercentual,
    textColor,
    preview
}) => (
    <Box sx={styles.footerContainer}>
        <Typography sx={styles.descText(textColor)}>{descripcio}</Typography>
        {canviPercentual && (<Typography sx={styles.percText(textColor)}>{canviPercentual}</Typography>)}
    </Box>
);


// Estils
const styles = {
    titleContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexGrow: 0,
        flexShrink: 0,
        flexBasis: 'auto',
        pt: 2,
        px: 2,
    },
    contentContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'flex-start',
        flexGrow: 1,
        flexShrink: 1,
        flexBasis: 'auto',
        p: 2,
        pt: 1,
        pb: 0,
    },
    footerContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexGrow: 0,
        flexShrink: 0,
        flexBasis: 'auto',
        p: 2,
        py: 1,
    },
    titleText: {
        letterSpacing: '0.025em',
        fontWeight: '600',
        fontSize: '1.4em',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        width: '100%',
    },
    contentText: (preview: boolean) => ({
        height: preview ? '80px' : '120px', // Altura fixa del contenidor
        display: 'flex',
        flexDirection: 'column', // Els elements s'apilen verticalment
        justifyContent: 'flex-end', // Alinea els elements al final (inferior)
        alignItems: 'flex-start', // Alinea horitzontalment a l'inici (esquerra)
        pr: 1, // Padding intern
        mb: 0, // Margin bottom
    }),
    valueText: (preview: boolean) => ({
        fontSize: preview ? '5em' : '5em',
        fontWeight: '500',
        textAlign: 'center',
        lineHeight: '1',
    }),
    unitText: (preview: boolean) => ({
        fontSize: preview ? '1em' : '1em',
        textAlign: 'center',
        lineHeight: '1',
    }),
    descText: (textColor: string) => ({
        flexGrow: 1,
        flexShrink: 1,
        fontWeight: '500',
        fontSize: '1.0em',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: '260px',
        color: textColor,
    }),
    percText: (textColor: string) => ({
        flexShrink: 0,
        fontWeight: '600',
        fontSize: '1.2em',
        overflow: 'visible',
        textAlign: 'right',
        whiteSpace: 'nowrap',
        ml: 2,
        color: textColor,
    }),
    iconContainer: {
    },
    icon: (preview: boolean, color: string, bgColor: string) => ({
        color,
        width: preview ? '1em' : '1.4em',
        height: preview ? '1em' : '1.4em',
        fontSize: preview ? '1.8em' : '1.8em',
        backgroundColor: bgColor,
        borderRadius: '50%',
        padding: '0.1em',
        border: 'solid',
        borderWidth: '0.1em',
        boxSizing: 'content-box',
    }),
};

// Helper function to check if a color is white
const isWhiteColor = (color: string): boolean => {
    return color === '#FFFFFF' || color === '#fff' || color === 'white';
};

// Helper function to create a semi-transparent version of a color
// opacity = 0.8 means 80% opacity (20% transparency)
const createTransparentColor = (color: string, opacity: number = 0.8): string => {
    // If the color is in hex format (#RRGGBB), convert it to rgba
    if (color.startsWith('#')) {
        const r = parseInt(color.slice(1, 3), 16);
        const g = parseInt(color.slice(3, 5), 16);
        const b = parseInt(color.slice(5, 7), 16);
        return `rgba(${r}, ${g}, ${b}, ${opacity})`;
    }
    // If the color is already in rgba format, just adjust the opacity
    if (color.startsWith('rgba')) {
        return color.replace(/[\d.]+\)$/, `${opacity})`);
    }
    // If the color is in rgb format, convert it to rgba
    if (color.startsWith('rgb')) {
        return color.replace('rgb', 'rgba').replace(')', `, ${opacity})`);
    }
    // For named colors or other formats, return as is
    return color;
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
        icona,
        vora = false,
        ampleVora,
        preview = false,
        onClick,
    } = props;

    console.log(props);

    const theme = useTheme();
    const {currentLanguage} = useBaseAppContext();
    const {textColor, backgroundColor, iconColor, iconBgColor, voraColor, highlightTextColor, isWhiteBackground} = useWidgetColors(props, theme);
    const formattedValue = typeof valor === 'number' ? numberFormat(valor, {}, currentLanguage) : valor;

    const bgColor = isWhiteBackground ? backgroundColor + ' !important' : 'transparent';
    const bg = isWhiteBackground ? 'none' : `linear-gradient(to bottom, ${backgroundColor}, ${createTransparentColor(backgroundColor, 0.75)})`;
    const voraAmple = ampleVora || (vora ? 1 : 0);

    return (
        <Paper
            elevation= {2}
            onClick={onClick}
            sx={{
                position: 'relative',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                borderRadius: '.6rem',
                backgroundColor: bgColor,
                background: bg,
                color: textColor,
                border: vora ? `${voraAmple}px solid ${voraColor}` : 'none',
                cursor: onClick ? 'pointer' : 'default',
                // height: '15vh',
                minHeight: '150px',
                transition: 'all 0.2s ease-in-out',
                '&:hover': {
                    boxShadow: onClick ? theme.shadows[4] : undefined,
                },
            }}
        >
            <WidgetTitle titol={titol} icona={icona} iconColor={iconColor} iconBgColor={iconBgColor} preview={preview}/>
            <WidgetContent valor={formattedValue} unitat={unitat} preview={preview}/>
            <WidgetFooter descripcio={descripcio} canviPercentual={canviPercentual} textColor={highlightTextColor} preview={preview}/>
        </Paper>
    );
};

export default SimpleWidgetVisualization;
