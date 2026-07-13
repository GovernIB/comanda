import React from 'react';
import {
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Icon,
    Skeleton,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import estils from './WidgetEstils';
import { useWidgetTheme } from './useWidgetTheme';
import { WidgetContainer, WidgetHeader, WidgetFooter, WidgetErrorDisplay } from './WidgetLayout';

// Interfaces
interface ColumnaEstil {
    codiColumna: string;
    colorText?: string;
    colorFons?: string;
    negreta?: boolean;
    cursiva?: boolean;
    rangsValors?: RangValor[];
}

interface RangValor {
    valorMin: number;
    valorMax: number;
    colorText?: string;
    colorFons?: string;
    negreta?: boolean;
    cursiva?: boolean;
}

interface CellaDestacada {
    codiColumna: string;
    valorDimensio: string;
    colorText?: string;
    colorFons?: string;
    negreta?: boolean;
    cursiva?: boolean;
    iconaPrefix?: string;
    iconaSufix?: string;
}

export interface TaulaWidgetVisualizationProps {
    // Widget data
    entornCodi?: string;
    titol?: string;
    descripcio?: string;
    columnes?: { id: string; label: string; format?: (value: any) => string }[];
    files?: any[];
    destacat?: boolean,

    // Atributs visuals
    colorText?: string;
    colorFons?: string;
    mostrarVora?: boolean;
    colorVora?: string;
    ampleVora?: number;

    // Taula
    colorTextTaula?: string;
    colorFonsTaula?: string;
    mostrarCapcalera?: boolean;
    colorCapcalera?: string;
    colorFonsCapcalera?: string;
    mostrarAlternancia?: boolean;
    colorAlternancia?: string;
    mostrarVoraTaula?: boolean;
    colorVoraTaula?: string;
    ampleVoraTaula?: number;
    mostrarSeparadorHoritzontal?: boolean;
    colorSeparadorHoritzontal?: string;
    ampleSeparadorHoritzontal?: number;
    mostrarSeparadorVertical?: boolean;
    colorSeparadorVertical?: string;
    ampleSeparadorVertical?: number;
    paginada?: boolean;

    columnesEstils?: ColumnaEstil[];
    cellesDestacades?: CellaDestacada[];

    // Additional props
    loading?: boolean;
    preview?: boolean;
    error?: boolean;
    errorMsg?: string;
    errorTrace?: string;
    onClick?: () => void;

    midaFontTitol?: number;
    midaFontDescripcio?: number;

    // Dashboard context
    dashboardEntornCodi?: string;
}

const TaulaWidgetVisualization: React.FC<TaulaWidgetVisualizationProps> = (props) => {
    const {
        titol,
        descripcio,
        columnes = generateSampleColumns(),
        files = generateSampleData(),
        entornCodi,
        mostrarVora = false,
        ampleVora = 1,
        mostrarCapcalera = true,
        mostrarAlternancia = false,
        colorAlternancia,
        mostrarVoraTaula = false,
        ampleVoraTaula = 1,
        mostrarSeparadorHoritzontal = true,
        ampleSeparadorHoritzontal = 1,
        mostrarSeparadorVertical = false,
        ampleSeparadorVertical = 1,
        columnesEstils = [],
        cellesDestacades = [],
        loading = false,
        preview = false,
        error = false,
        errorMsg,
        errorTrace,
        onClick,
        dashboardEntornCodi,
    } = props;

    const theme = useTheme();

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
        ampleVora,
    });

    const textTaulaColor = props.colorTextTaula || props.colorText || theme.palette.text.primary;
    const taulaBgColor = props.colorFonsTaula || props.colorFons || 'transparent';
    const textHeaderColor = props.colorCapcalera || props.colorTextTaula || props.colorText || theme.palette.text.primary;
    const headerBgColor = props.colorFonsCapcalera || props.colorFonsTaula || props.colorFons;
    const voraTaulaColor = props.colorVoraTaula || theme.palette.divider;
    const horDividerColor = props.colorSeparadorHoritzontal || props.colorVoraTaula || theme.palette.divider;
    const verDividerColor = props.colorSeparadorVertical || props.colorVoraTaula || theme.palette.divider;

    const getCellStyle = (columnId: string, rowIndex: number, value: any, rowData: any) => {
        let style: React.CSSProperties = {};

        const columnStyle = columnesEstils.find(col => col.codiColumna === columnId);
        if (columnStyle) {
            // Aplicar estils de columnes
            if (columnStyle.colorText) style.color = columnStyle.colorText;
            if (columnStyle.colorFons) style.backgroundColor = columnStyle.colorFons;
            if (columnStyle.negreta) style.fontWeight = 'bold';
            if (columnStyle.cursiva) style.fontStyle = 'italic';

            // Comprovar si el valor es troba en algun rang
            if (typeof value === 'number' && columnStyle.rangsValors && columnStyle.rangsValors.length > 0) {
                const matchingRange = columnStyle.rangsValors.find(
                    range => value >= range.valorMin && value <= range.valorMax
                );
                if (matchingRange) {
                    if (matchingRange.colorText) style.color = matchingRange.colorText;
                    if (matchingRange.colorFons) style.backgroundColor = matchingRange.colorFons;
                    if (matchingRange.negreta) style.fontWeight = 'bold';
                    if (matchingRange.cursiva) style.fontStyle = 'italic';
                }
            }
        }

        // Comprovar si la cel·la té una configuració específica
        const dimensionValue = rowData.dimensio || rowData.name || '';
        const highlightedCell = cellesDestacades.find(
            cell => cell.codiColumna === columnId && cell.valorDimensio === dimensionValue
        );

        if (highlightedCell) {
            if (highlightedCell.colorText) style.color = highlightedCell.colorText;
            if (highlightedCell.colorFons) style.backgroundColor = highlightedCell.colorFons;
            if (highlightedCell.negreta) style.fontWeight = 'bold';
            if (highlightedCell.cursiva) style.fontStyle = 'italic';
        }

        // Aplicar estils alternats a files
        if (mostrarAlternancia && rowIndex % 2 === 1 && !style.backgroundColor) {
            style.backgroundColor = colorAlternancia;
        }

        return style;
    };

    // Function to render cell content with optional icons
    const renderCellContent = (columnId: string, value: any, rowData: any) => {
        const dimensionValue = rowData.dimensio || rowData.name || '';
        const highlightedCell = cellesDestacades.find(
            cell => cell.codiColumna === columnId && cell.valorDimensio === dimensionValue
        );

        if (highlightedCell) {
            return (
                <>
                    {highlightedCell.iconaPrefix && (
                        <Icon sx={{fontSize: 'small', mr: 0.5}}>{highlightedCell.iconaPrefix}</Icon>
                    )}
                    {value}
                    {highlightedCell.iconaSufix && (
                        <Icon sx={{fontSize: 'small', ml: 0.5}}>{highlightedCell.iconaSufix}</Icon>
                    )}
                </>
            );
        }

        return value;
    };

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
                    <Box sx={estils.tableContainerBox}>
                        {loading ? (
                            <Box sx={{ width: '100%', height: '100%', minHeight: 200 }}>
                                <Skeleton variant="rectangular" width="100%" height={40} sx={{ mb: 1 }} />
                                {[...Array(5)].map((_, index) => (
                                    <Skeleton key={index} variant="rectangular" width="100%" height={30} sx={{ mb: 1 }} />
                                ))}
                            </Box>
                        ) : (
                            <TableContainer sx={{ height: '100%' }}>
                                <Table
                                    stickyHeader
                                    size={preview ? "small" : "medium"}
                                    sx={estils.tableContainer(mostrarVoraTaula, ampleVoraTaula, voraTaulaColor, taulaBgColor)}
                                >
                                    {mostrarCapcalera && (
                                        <TableHead>
                                            <TableRow
                                                sx={estils.tableHeader(
                                                    textHeaderColor,
                                                    headerBgColor,
                                                    horDividerColor,
                                                    mostrarSeparadorHoritzontal ? Number(ampleSeparadorHoritzontal) + 1 : 1,
                                                    mostrarSeparadorVertical,
                                                    verDividerColor,
                                                    ampleSeparadorVertical
                                                )}
                                            >
                                                {columnes.map((column) => (
                                                    <TableCell
                                                        key={column.id}
                                                        align="left"
                                                        sx={{
                                                            fontWeight: '600',
                                                            color: textHeaderColor,
                                                            backgroundColor: headerBgColor,
                                                        }}
                                                    >
                                                        {column.label}
                                                    </TableCell>
                                                ))}
                                            </TableRow>
                                        </TableHead>
                                    )}
                                    <TableBody>
                                        {files.map((row, rowIndex) => (
                                            <TableRow
                                                key={rowIndex}
                                                sx={estils.tableRow(
                                                    textTaulaColor,
                                                    taulaBgColor,
                                                    mostrarSeparadorHoritzontal,
                                                    horDividerColor,
                                                    ampleSeparadorHoritzontal,
                                                    mostrarSeparadorVertical,
                                                    verDividerColor,
                                                    ampleSeparadorVertical
                                                )}
                                            >
                                                {columnes.map((column) => {
                                                    const value = row[column.id];
                                                    const formattedValue = column.format ? column.format(value) : value;

                                                    return (
                                                        <TableCell
                                                            key={column.id}
                                                            align="left"
                                                            sx={{
                                                                color: textTaulaColor,
                                                                backgroundColor: taulaBgColor,
                                                                ...getCellStyle(column.id, rowIndex, value, row),
                                                            }}
                                                        >
                                                            {renderCellContent(column.id, formattedValue, row)}
                                                        </TableCell>
                                                    );
                                                })}
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        )}
                    </Box>

                    <WidgetFooter
                        descripcio={descripcio}
                        textColor={textColor}
                        loading={loading}
                    />
                </>
            )}
        </WidgetContainer>
    );
};

const generateSampleColumns = (): { id: string; label: string; format?: (value: any) => string }[] => {
    return [
        {id: 'name', label: 'Nom'},
        {id: 'valor1', label: 'Valor 1'},
        {id: 'valor2', label: 'Valor 2'},
        {id: 'valor3', label: 'Valor 3'},
    ];
};

const generateSampleData = () => {
    return [
        {name: 'Fila 1', dimensio: 'dim1', valor1: 100, valor2: 200, valor3: 300},
        {name: 'Fila 2', dimensio: 'dim2', valor1: 150, valor2: 250, valor3: 350},
        {name: 'Fila 3', dimensio: 'dim3', valor1: 200, valor2: 300, valor3: 400},
        {name: 'Fila 4', dimensio: 'dim4', valor1: 250, valor2: 350, valor3: 450},
        {name: 'Fila 5', dimensio: 'dim5', valor1: 300, valor2: 400, valor3: 500},
    ];
};

export default TaulaWidgetVisualization;
