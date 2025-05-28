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
import { useTheme } from '@mui/material/styles';

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

// Define the props for the TaulaWidgetVisualization component
export interface TaulaWidgetVisualizationProps {
  // Widget data
  title?: string;
  data?: any[];
  columns?: { id: string; label: string; format?: (value: any) => string }[];
  
  // Visual attributes
  mostrarCapcalera?: boolean;
  mostrarBordes?: boolean;
  mostrarAlternancia?: boolean;
  colorAlternancia?: string;
  columnesEstils?: ColumnaEstil[];
  cellesDestacades?: CellaDestacada[];
  
  // Additional props
  loading?: boolean;
  preview?: boolean;
  onClick?: () => void;
}

/**
 * Component for visualizing a table widget with configurable visual attributes.
 * Can be used both for dashboard display and for preview in configuration forms.
 */
const TaulaWidgetVisualization: React.FC<TaulaWidgetVisualizationProps> = (props) => {
  const {
    // Widget data
    title = 'Títol de la taula',
    data = generateSampleData(),
    columns = generateSampleColumns(),
    
    // Visual attributes
    mostrarCapcalera = true,
    mostrarBordes = true,
    mostrarAlternancia = true,
    colorAlternancia = '#f5f5f5',
    columnesEstils = [],
    cellesDestacades = [],
    
    // Additional props
    loading = false,
    preview = false,
    onClick,
  } = props;
  
  const theme = useTheme();
  
  // Function to get cell style based on column styles, value ranges, and highlighted cells
  const getCellStyle = (columnId: string, rowIndex: number, value: any, rowData: any) => {
    let style: React.CSSProperties = {};
    
    // Find column style
    const columnStyle = columnesEstils.find(col => col.codiColumna === columnId);
    
    if (columnStyle) {
      // Apply basic column style
      if (columnStyle.colorText) style.color = columnStyle.colorText;
      if (columnStyle.colorFons) style.backgroundColor = columnStyle.colorFons;
      if (columnStyle.negreta) style.fontWeight = 'bold';
      if (columnStyle.cursiva) style.fontStyle = 'italic';
      
      // Check if value is in any range (for numeric values)
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
    
    // Check if cell is highlighted
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
    
    // Apply alternating row style if enabled
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
            <Icon sx={{ fontSize: 'small', mr: 0.5 }}>{highlightedCell.iconaPrefix}</Icon>
          )}
          {value}
          {highlightedCell.iconaSufix && (
            <Icon sx={{ fontSize: 'small', ml: 0.5 }}>{highlightedCell.iconaSufix}</Icon>
          )}
        </>
      );
    }
    
    return value;
  };
  
  return (
    <Paper
      elevation={preview ? 1 : 2}
      onClick={onClick}
      sx={{
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        borderRadius: '.6rem',
        cursor: onClick ? 'pointer' : 'default',
        height: '100%',
        minHeight: preview ? '200px' : '350px',
        transition: 'all 0.2s ease-in-out',
        '&:hover': {
          boxShadow: onClick ? theme.shadows[4] : undefined,
        },
      }}
    >
      {/* Title */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          pt: 1,
          px: 2,
        }}
      >
        <Typography
          sx={{
            letterSpacing: '0.025em',
            fontWeight: '500',
            fontSize: '1.2rem',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
            width: '100%',
          }}
        >
          {title}
        </Typography>
      </Box>
      
      {/* Table */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          flex: 1,
          p: 2,
          width: '100%',
          overflow: 'auto',
        }}
      >
        <TableContainer sx={{ height: '100%' }}>
          <Table 
            size={preview ? "small" : "medium"}
            sx={{ 
              borderCollapse: 'collapse',
              '& th, & td': {
                border: mostrarBordes ? `1px solid ${theme.palette.divider}` : 'none',
              }
            }}
          >
            {mostrarCapcalera && (
              <TableHead>
                <TableRow>
                  {columns.map((column) => (
                    <TableCell
                      key={column.id}
                      align="left"
                      sx={{
                        fontWeight: 'bold',
                        backgroundColor: theme.palette.background.default,
                      }}
                    >
                      {column.label}
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
            )}
            <TableBody>
              {data.map((row, rowIndex) => (
                <TableRow key={rowIndex}>
                  {columns.map((column) => {
                    const value = row[column.id];
                    const formattedValue = column.format ? column.format(value) : value;
                    
                    return (
                      <TableCell
                        key={column.id}
                        align="left"
                        sx={getCellStyle(column.id, rowIndex, value, row)}
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
      </Box>
    </Paper>
  );
};

// Helper function to generate sample columns
const generateSampleColumns = () => {
  return [
    { id: 'name', label: 'Nom' },
    { id: 'valor1', label: 'Valor 1' },
    { id: 'valor2', label: 'Valor 2' },
    { id: 'valor3', label: 'Valor 3' },
  ];
};

// Helper function to generate sample data
const generateSampleData = () => {
  return [
    { name: 'Fila 1', dimensio: 'dim1', valor1: 100, valor2: 200, valor3: 300 },
    { name: 'Fila 2', dimensio: 'dim2', valor1: 150, valor2: 250, valor3: 350 },
    { name: 'Fila 3', dimensio: 'dim3', valor1: 200, valor2: 300, valor3: 400 },
    { name: 'Fila 4', dimensio: 'dim4', valor1: 250, valor2: 350, valor3: 450 },
    { name: 'Fila 5', dimensio: 'dim5', valor1: 300, valor2: 400, valor3: 500 },
  ];
};

export default TaulaWidgetVisualization;