import React from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Icon from '@mui/material/Icon';
import {useTheme} from '@mui/material/styles';
import {createTransparentColor, isWhiteColor} from "../../util/colorUtil";
import estils from './WidgetEstils';
import Chip from "@mui/material/Chip";
import Skeleton from '@mui/material/Skeleton';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';

// Define the column style interface
interface ColumnaEstil {
    codiColumna: string;
    colorText?: string;
    colorFons?: string;
    negreta?: boolean;
    cursiva?: boolean;
    rangsValors?: RangValor[];
}

// Define the value range interface
interface RangValor {
    valorMin: number;
    valorMax: number;
    colorText?: string;
    colorFons?: string;
    negreta?: boolean;
    cursiva?: boolean;
}

// Define the highlighted cell interface
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

interface TaulaWidgetColors {
    textColor: string;
    backgroundColor: string;
    voraColor: string;
    textTaulaColor: string;
    taulaBgColor: string;
    textHeaderColor: string;
    headerBgColor?: string;
    alternanciaColor: string;
    voraTaulaColor: string;
    horDividerColor: string;
    verDividerColor: string;
    isWhiteBackground: boolean;
}

// Define the props for the TaulaWidgetVisualization component
export interface TaulaWidgetVisualizationProps {
    // Widget data
    entornCodi?: string;
    titol?: string;
    descripcio?: string;

    columnes?: { id: string; label: string; format?: (value: any) => string }[];
    files?: any[];

    // Atributs visuals
    // Contenidor
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

    midaFontTitol?: number,
    midaFontDescripcio?: number,
}

const useWidgetColors = (props: TaulaWidgetVisualizationProps, theme: any): TaulaWidgetColors => {
    const {
        colorText,
        colorFons,
        colorVora,
        colorTextTaula,
        colorFonsTaula,
        colorCapcalera,
        colorFonsCapcalera,
        colorAlternancia,
        colorVoraTaula,
        colorSeparadorHoritzontal,
        colorSeparadorVertical
    } = props;

    const colors = {
        text: colorText || theme.palette.text.primary,
        background: colorFons || theme.palette.background.paper,
        vora: colorVora || theme.palette.divider,
        textTaula: colorTextTaula || colorText || theme.palette.text.primary,
        taulaBg: colorFonsTaula || colorFons || 'transparent',
        textHeader: colorCapcalera || colorTextTaula || colorText || theme.palette.text.primary,
        headerBg: colorFonsCapcalera || colorFonsTaula || colorFons,
        alternancia: colorAlternancia || colorFonsTaula || colorFons || 'transparent',
        voraTaula: colorVoraTaula || theme.palette.divider,
        horDivider: colorSeparadorHoritzontal || colorVoraTaula || theme.palette.divider,
        verDivider: colorSeparadorVertical || colorVoraTaula || theme.palette.divider,
    };

    return {
        textColor: colors.text,
        backgroundColor: colors.background,
        voraColor: colors.vora,
        textTaulaColor: colors.textTaula,
        taulaBgColor: colors.taulaBg,
        textHeaderColor: colors.textHeader,
        headerBgColor: colors.headerBg,
        alternanciaColor: colors.alternancia,
        voraTaulaColor: colors.voraTaula,
        horDividerColor: colors.horDivider,
        verDividerColor: colors.verDivider,
        isWhiteBackground: !colorFons || isWhiteColor(colors.background),
    };
};

/**
 * Component for visualizing a table widget with configurable visual attributes.
 * Can be used both for dashboard display and for preview in configuration forms.
 */
const TaulaWidgetVisualization: React.FC<TaulaWidgetVisualizationProps> = (props) => {
    const {
        // Widget data
        titol = 'Títol de la taula',
        descripcio,
        columnes = generateSampleColumns(),
        files = generateSampleData(),
        entornCodi = 'DEV',

        // Atributs visuals
        // Contenidor
        // colorText,
        // colorFons,
        mostrarVora = false,
        // colorVora,
        ampleVora = 1,
        // Taula
        // colorTextTaula,
        // colorFonsTaula,
        mostrarCapcalera = true,
        // colorCapcalera,
        // colorFonsCapcalera,
        mostrarAlternancia = false,
        colorAlternancia,
        mostrarVoraTaula = false,
        // colorVoraTaula,
        ampleVoraTaula = 1,
        mostrarSeparadorHoritzontal = true,
        // colorSeparadorHoritzontal,
        ampleSeparadorHoritzontal = 1,
        mostrarSeparadorVertical = false,
        // colorSeparadorVertical,
        ampleSeparadorVertical = 1,
        // paginada = false,
        // colorAlternancia = '#f5f5f5',
        columnesEstils = [],
        cellesDestacades = [],

        // Additional props
        loading = false,
        preview = false,
        error = false,
        errorMsg,
        errorTrace,
        onClick,

        midaFontTitol,
        midaFontDescripcio,
    }
        = props;

    const theme = useTheme();
    const colors: TaulaWidgetColors = useWidgetColors(props, theme);

    const bgColor = colors.isWhiteBackground ? colors.backgroundColor + ' !important' : 'transparent';
    const bg = colors.isWhiteBackground ? 'none' : `linear-gradient(to bottom, ${colors.backgroundColor}, ${createTransparentColor(colors.backgroundColor, 0.75)})`;
    const voraAmple = ampleVora || (mostrarVora ? 1 : 0);

    // Function to get cell style based on column styles, value ranges, and highlighted cells
    const getCellStyle = (columnId: string, rowIndex: number, value: any, rowData: any) => {
        let style: React.CSSProperties = {};

        // Find column style
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

    const titleEstils = {
        ...estils.titleText,
        fontSize: midaFontTitol ?`${midaFontTitol}px` :estils.titleText.fontSize
    }
    const descEstils = {
        ...estils.descText(colors.textColor),
        fontSize: midaFontDescripcio ?`${midaFontDescripcio}px` :estils.descText(colors.textColor).fontSize
    }

    return (
        <Paper elevation={2} onClick={onClick} sx={estils.paperContainer(bgColor, bg, colors.textColor, mostrarVora, voraAmple, colors.voraColor, onClick, theme)}>
            {/* Titol */}
            <Box sx={estils.titleContainer} >
                {loading ? (
                    <>
                        <Skeleton width="70%" height={32} />
                        <Box sx={estils.iconContainer}>
                            <Skeleton width={40} height={24} />
                        </Box>
                    </>
                ) : (
                    <>
                        <Typography sx={titleEstils} >{titol}</Typography>
                        <Box sx={estils.iconContainer}>
                            <Chip sx={estils.entornCodi} label={entornCodi} size={"small"} />
                        </Box>
                    </>
                )}
            </Box>

            {error ? (
                // Error content
                <Box sx={{ flex: 1, p: 2, overflow: "auto" }}>
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
                    {/* Table */}
                    <Box sx={ estils.tableContainerBox } >
                        {loading ? (
                            <Box sx={{ width: '100%', height: '100%', minHeight: 200 }}>
                                {/* Table header skeleton */}
                                <Skeleton variant="rectangular" width="100%" height={40} sx={{ mb: 1 }} />

                                {/* Table rows skeletons */}
                                {[...Array(5)].map((_, index) => (
                                    <Skeleton key={index} variant="rectangular" width="100%" height={30} sx={{ mb: 1 }} />
                                ))}
                            </Box>
                        ) : (
                            <TableContainer sx={{height: '100%'}}>
                                <Table
                                    stickyHeader
                                    size={preview ? "small" : "medium"}
                                    sx={ estils.tableContainer(mostrarVoraTaula, ampleVoraTaula, colors.voraTaulaColor, colors.taulaBgColor) }
                                >
                                    {mostrarCapcalera && (
                                        <TableHead>
                                            <TableRow sx={estils.tableHeader(colors.textHeaderColor, colors.headerBgColor,
                                                colors.horDividerColor, mostrarSeparadorHoritzontal ? Number(ampleSeparadorHoritzontal) + 1 : 1,
                                                mostrarSeparadorVertical, colors.verDividerColor, ampleSeparadorVertical)}>
                                                {columnes.map((column) => (
                                                    <TableCell
                                                        key={column.id}
                                                        align="left"
                                                        sx={{
                                                            fontWeight: '600',
                                                            color: colors.textHeaderColor,
                                                            backgroundColor: colors.headerBgColor,
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
                                            <TableRow key={rowIndex}
                                                    sx={estils.tableRow(colors.textTaulaColor, colors.taulaBgColor,
                                                        mostrarSeparadorHoritzontal, colors.horDividerColor, ampleSeparadorHoritzontal,
                                                        mostrarSeparadorVertical, colors.verDividerColor, ampleSeparadorVertical)}>
                                                {columnes.map((column) => {
                                                    const value = row[column.id];
                                                    const formattedValue = column.format ? column.format(value) : value;

                                                    return (
                                                        <TableCell
                                                            key={column.id}
                                                            align="left"
                                                            sx={{color:colors.textTaulaColor,
                                                                backgroundColor:colors.taulaBgColor,
                                                                ...getCellStyle(column.id, rowIndex, value, row)}}
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

                    {/*Peu*/}
                    <Box sx={estils.footerContainer}>
                        {loading ? (
                            <Skeleton width="60%" height={24} />
                        ) : (
                            <Typography sx={descEstils}>{descripcio}</Typography>
                        )}
                    </Box>
                </>
            )}
        </Paper>
    );
};

// Helper function to generate sample columns
const generateSampleColumns = (): { id: string; label: string; format?: (value: any) => string }[] => {
    return [
        {id: 'name', label: 'Nom'},
        {id: 'valor1', label: 'Valor 1'},
        {id: 'valor2', label: 'Valor 2'},
        {id: 'valor3', label: 'Valor 3'},
    ];
};

// Helper function to generate sample data
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
