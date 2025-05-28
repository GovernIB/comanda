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
  title?: string;
  value?: number | string;
  unit?: string;
  
  // Visual attributes
  icona?: string;
  colorText?: string;
  colorFons?: string;
  colorIcona?: string;
  colorFonsIcona?: string;
  destacat?: boolean;
  colorTextDestacat?: string;
  colorFonsDestacat?: string;
  borde?: boolean;
  colorBorde?: string;
  
  // Additional props
  loading?: boolean;
  preview?: boolean;
  onClick?: () => void;
}

/**
 * Component for visualizing a simple widget with configurable visual attributes.
 * Can be used both for dashboard display and for preview in configuration forms.
 */
const SimpleWidgetVisualization: React.FC<SimpleWidgetVisualizationProps> = (props) => {
  const {
    // Widget data
    title = 'Títol del widget',
    value = 0,
    unit = 'unitat',
    
    // Visual attributes
    icona,
    colorText,
    colorFons,
    colorIcona,
    colorFonsIcona,
    destacat = false,
    colorTextDestacat,
    colorFonsDestacat,
    borde = false,
    colorBorde,
    
    // Additional props
    loading = false,
    preview = false,
    onClick,
  } = props;
  
  const theme = useTheme();
  const { currentLanguage } = useBaseAppContext();
  
  // Determine the actual colors to use based on props and theme
  const actualColorText = colorText || theme.palette.text.primary;
  const actualColorFons = colorFons || theme.palette.background.paper;
  const actualColorIcona = colorIcona || theme.palette.primary.main;
  const actualColorFonsIcona = colorFonsIcona || 'transparent';
  const actualColorTextDestacat = colorTextDestacat || theme.palette.primary.contrastText;
  const actualColorFonsDestacat = colorFonsDestacat || theme.palette.primary.main;
  const actualColorBorde = colorBorde || theme.palette.divider;
  
  // Determine the background and text colors based on the destacat flag
  const bgColor = destacat ? actualColorFonsDestacat : actualColorFons;
  const textColor = destacat ? actualColorTextDestacat : actualColorText;
  
  // Format the value if it's a number
  const formattedValue = typeof value === 'number' 
    ? numberFormat(value, {}, currentLanguage) 
    : value;
  
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
        backgroundColor: bgColor,
        color: textColor,
        border: borde ? `1px solid ${actualColorBorde}` : 'none',
        cursor: onClick ? 'pointer' : 'default',
        height: '100%',
        minHeight: preview ? '150px' : '200px',
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
      
      {/* Value and unit */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          flex: 1,
          p: 2,
        }}
      >
        <Typography
          sx={{
            fontSize: preview ? '2rem' : '3rem',
            fontWeight: '700',
            textAlign: 'center',
          }}
        >
          {formattedValue}
        </Typography>
        <Typography
          sx={{
            fontSize: preview ? '0.9rem' : '1.1rem',
            textAlign: 'center',
          }}
        >
          {unit}
        </Typography>
      </Box>
      
      {/* Background icon */}
      {icona && (
        <Box
          sx={{
            position: 'absolute',
            bottom: '10%',
            right: '10%',
            opacity: 0.2,
          }}
        >
          <Icon
            sx={{
              color: actualColorIcona,
              fontSize: preview ? '4rem' : '6rem',
              backgroundColor: actualColorFonsIcona,
              borderRadius: actualColorFonsIcona !== 'transparent' ? '50%' : 0,
              padding: actualColorFonsIcona !== 'transparent' ? 1 : 0,
            }}
          >
            {icona}
          </Icon>
        </Box>
      )}
    </Paper>
  );
};

export default SimpleWidgetVisualization;