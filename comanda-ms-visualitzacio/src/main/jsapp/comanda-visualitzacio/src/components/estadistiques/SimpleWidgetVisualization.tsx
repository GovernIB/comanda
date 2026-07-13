import React from 'react';
import { Icon, Box, Typography, Skeleton } from '@mui/material';
import { numberFormat, useBaseAppContext } from 'reactlib';
import { useWidgetTheme } from './useWidgetTheme';
import { 
    WidgetContainer, 
    WidgetHeader, 
    WidgetFooter, 
    WidgetErrorDisplay 
} from './WidgetLayout';
import estils from './WidgetEstils';
import { camelToSnakeCase } from '../../util/stringUtils';

// Define the props for the SimpleWidgetVisualization component
export interface SimpleWidgetVisualizationProps {
    // Widget data
    entornCodi?: string;
    titol?: string;
    descripcio?: string;

    valor?: number | string;
    unitat?: string;
    canviPercentual?: string;
    destacat?: boolean;

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

    midaFontTitol?: number;
    midaFontDescripcio?: number;
    midaFontValor?: number;
    midaFontUnitats?: number;
    midaFontCanviPercentual?: number;

    // Dashboard context
    dashboardEntornCodi?: string;
}

const SimpleWidgetVisualization: React.FC<SimpleWidgetVisualizationProps> = (props) => {
    const {
        titol = '',
        valor = 0,
        unitat = '',
        descripcio = '',
        canviPercentual = '',
        entornCodi = '',
        icona,
        mostrarVora = false,
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
        dashboardEntornCodi,
    } = props;

    const { currentLanguage } = useBaseAppContext();

    const {
        textColor,
        backgroundColor,
        voraColor,
        isWhiteBackground,
        contrastTextColor,
        bgColor,
        bg,
        voraAmple,
    } = useWidgetTheme({
        colorText: props.colorText,
        colorFons: props.colorFons,
        colorVora: props.colorVora,
        mostrarVora,
        ampleVora: props.ampleVora,
    });

    const iconColor = props.colorIcona || '#888888';
    const iconBgColor = props.colorFonsIcona || 'transparent';
    const highlightTextColor = props.colorTextDestacat || '#004B99';

    const parsedValue = (typeof valor === 'string' && valor.trim() !== '') ? Number(valor) : valor;
    const formattedValue = typeof parsedValue === 'number' && !isNaN(parsedValue)
        ? numberFormat(parsedValue, {}, currentLanguage)
        : (typeof valor === 'string' ? valor : String(valor || ''));

    const snakeCaseIcona = camelToSnakeCase(icona);

    return (
        <WidgetContainer
            bgColor={bgColor}
            bg={bg}
            textColor={textColor}
            mostrarVora={mostrarVora}
            voraAmple={voraAmple}
            voraColor={voraColor}
            onClick={onClick}
        >
            <WidgetHeader
                titol={titol}
                entornCodi={entornCodi}
                loading={loading}
                midaFontTitol={midaFontTitol}
                isWhiteBackground={isWhiteBackground}
                backgroundColor={backgroundColor}
                voraColor={voraColor}
                contrastTextColor={contrastTextColor}
                dashboardEntornCodi={dashboardEntornCodi}
            />

            {error ? (
                <WidgetErrorDisplay errorMsg={errorMsg} errorTrace={errorTrace} />
            ) : (
                <>
                    {/* Content */}
                    <Box sx={estils.contentContainer}>
                        {loading ? (
                            <>
                                <Box sx={{...estils.contentText(preview), width: '10em'}}>
                                    <Skeleton width="100%" height={80} />
                                </Box>
                                <Box sx={{...estils.contentText(preview), width: '4em'}}>
                                    <Skeleton width="100%" height={20} />
                                </Box>
                            </>
                        ) : (
                            <>
                                <Box sx={estils.contentText(preview)}>
                                    <Typography sx={{
                                        ...estils.valueText(preview),
                                        fontSize: midaFontValor ? `${midaFontValor}px` : estils.valueText(preview).fontSize,
                                    }}>
                                        {formattedValue}
                                    </Typography>
                                </Box>
                                <Box sx={estils.contentText(preview)}>
                                    <Typography sx={{
                                        ...estils.unitText(preview),
                                        fontSize: midaFontUnitats ? `${midaFontUnitats}px` : estils.unitText(preview).fontSize,
                                    }}>
                                        {unitat}
                                    </Typography>
                                </Box>
                            </>
                        )}
                    </Box>

                    <WidgetFooter
                        descripcio={descripcio}
                        textColor={highlightTextColor}
                        loading={loading}
                        midaFontDescripcio={midaFontDescripcio}
                        canviPercentual={canviPercentual}
                        midaFontCanviPercentual={midaFontCanviPercentual}
                    />

                    {!loading && snakeCaseIcona &&(
                        <Icon sx={estils.icon(preview, iconColor, iconBgColor)}>
                            {snakeCaseIcona}
                        </Icon>
                    )}
                </>
            )}
        </WidgetContainer>
    );
};

export default React.memo(SimpleWidgetVisualization);