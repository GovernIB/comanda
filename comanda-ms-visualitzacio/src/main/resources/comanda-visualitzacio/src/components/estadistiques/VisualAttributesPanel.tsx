import React, { useState } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Collapse from '@mui/material/Collapse';
import { useTheme } from '@mui/material/styles';
import {FormField, useFormContext} from 'reactlib';

// Define the props for the VisualAttributesPanel component
export interface VisualAttributesPanelProps {
  title?: string;
  widgetType: 'simple' | 'grafic' | 'taula';
  children?: React.ReactNode;
}

/**
 * Component for displaying an expandable panel for visual attributes configuration.
 * Can be used in both widget forms and dashboard configuration.
 */
const VisualAttributesPanel: React.FC<VisualAttributesPanelProps> = (props) => {
  const {
    title = 'Atributs visuals',
    widgetType,
    children,
  } = props;

  const { data } = useFormContext();

  const theme = useTheme();

  // Render the appropriate form fields based on widget type
  const renderFormFields = () => {
    switch (widgetType) {
      case 'simple':
        return renderSimpleFormFields();
      case 'grafic':
        return renderGraficFormFields();
      case 'taula':
        return renderTaulaFormFields();
      default:
        return null;
    }
  };

  // Render form fields for simple widget
  const renderSimpleFormFields = () => {
    return (
      <Box sx={{ p: 2 }}>
        <Typography variant="subtitle2" sx={{ mb: 2 }}>Configuració general</Typography>
        <FormField name="atributsVisuals.icona" label="Icona" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Colors</Typography>
        <FormField name="atributsVisuals.colorText" label="Color del text" type="color" value={data?.atributsVisuals?.colorText || '#000000'} required={false} />
        <FormField name="atributsVisuals.colorFons" label="Color de fons" type="color" value={data?.atributsVisuals?.colorText || '#FFFFFF'} required={false} />
        <FormField name="atributsVisuals.colorIcona" label="Color de la icona" type="color" value={data?.atributsVisuals?.colorText || '#000000'} required={false} />
        <FormField name="atributsVisuals.colorFonsIcona" label="Color de fons de la icona" type="color" value={data?.atributsVisuals?.colorText || '#FFFFFF'} required={false} />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Destacat</Typography>
        <FormField name="atributsVisuals.destacat" label="Destacat" type="checkbox" />
        <FormField
          name="atributsVisuals.colorTextDestacat"
          label="Color del text destacat"
          type="color"
          value={data?.atributsVisuals?.colorText || '#FFFFFF'}
          required={false}
          disabled={!data?.atributsVisuals?.destacat}
        />
        <FormField
          name="atributsVisuals.colorFonsDestacat"
          label="Color de fons destacat"
          type="color"
          value={data?.atributsVisuals?.colorText || '#000000'}
          required={false}
          disabled={!data?.atributsVisuals?.destacat}
        />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Vora</Typography>
        <FormField name="atributsVisuals.borde" label="Mostrar vora" type="checkbox" />
        <FormField
          name="atributsVisuals.colorBorde"
          label="Color de la vora"
          type="color"
          value={data?.atributsVisuals?.colorText || '#CCCCCC'}
          required={false}
          disabled={!data?.atributsVisuals?.borde}
        />
      </Box>
    );
  };

  // Render form fields for grafic widget
  const renderGraficFormFields = () => {
    return (
      <Box sx={{ p: 2 }}>
        <Typography variant="subtitle2" sx={{ mb: 2 }}>Configuració general</Typography>
        <FormField name="atributsVisuals.colorsPaleta" label="Colors de la paleta" />
        <FormField name="atributsVisuals.mostrarReticula" label="Mostrar retícula" type="checkbox" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de barres</Typography>
        <FormField name="atributsVisuals.barStacked" label="Barres apilades" type="checkbox" />
        <FormField name="atributsVisuals.barHorizontal" label="Barres horitzontals" type="checkbox" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de línies</Typography>
        <FormField name="atributsVisuals.lineShowPoints" label="Mostrar punts" type="checkbox" />
        <FormField name="atributsVisuals.lineSmooth" label="Línies suaus" type="checkbox" />
        <FormField name="atributsVisuals.lineWidth" label="Amplada de línia" type="number" required={false} />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de pastís</Typography>
        <FormField name="atributsVisuals.pieDonut" label="Tipus donut" type="checkbox" />
        <FormField name="atributsVisuals.pieShowLabels" label="Mostrar etiquetes" type="checkbox" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de gauge</Typography>
        <FormField name="atributsVisuals.gaugeMin" label="Valor mínim" type="number" required={false} />
        <FormField name="atributsVisuals.gaugeMax" label="Valor màxim" type="number" required={false} />
        <FormField name="atributsVisuals.gaugeColors" label="Colors (separats per comes)" />
        <FormField name="atributsVisuals.gaugeRangs" label="Rangs (separats per comes)" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Gràfic de heatmap</Typography>
        <FormField name="atributsVisuals.heatmapColors" label="Colors (separats per comes)" />
        <FormField name="atributsVisuals.heatmapMinValue" label="Valor mínim" type="number" required={false} />
        <FormField name="atributsVisuals.heatmapMaxValue" label="Valor màxim" type="number" required={false} />
      </Box>
    );
  };

  // Render form fields for taula widget
  const renderTaulaFormFields = () => {
    return (
      <Box sx={{ p: 2 }}>
        <Typography variant="subtitle2" sx={{ mb: 2 }}>Configuració general</Typography>
        <FormField name="atributsVisuals.mostrarCapcalera" label="Mostrar capçalera" type="checkbox" />
        <FormField name="atributsVisuals.mostrarBordes" label="Mostrar vores" type="checkbox" />
        <FormField name="atributsVisuals.mostrarAlternancia" label="Mostrar alternança de files" type="checkbox" />
        <FormField
          name="atributsVisuals.colorAlternancia"
          label="Color d'alternança"
          type="color"
          required={false}
          disabled={!data?.atributsVisuals?.mostrarAlternancia}
        />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Estils de columnes</Typography>
        <Typography variant="body2" color="text.secondary">
          Els estils de columnes es configuren a través d'una estructura complexa.
          Utilitzeu el botó "Afegir estil de columna" per afegir-ne un de nou.
        </Typography>

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Cel·les destacades</Typography>
        <Typography variant="body2" color="text.secondary">
          Les cel·les destacades es configuren a través d'una estructura complexa.
          Utilitzeu el botó "Afegir cel·la destacada" per afegir-ne una de nova.
        </Typography>
      </Box>
    );
  };

  return (
    <Box sx={{ position: 'relative', height: '100%', display: 'flex', minWidth: '40px', minHeight: '100px' }}>
      {/* Main panel */}
      <Paper
        elevation={1}
        sx={{
          display: 'flex',
          flexDirection: 'column',
          height: '100%',
          overflow: 'hidden',
          width: '100%',
          border: `1px solid ${theme.palette.divider}`,
          position: 'relative', // Ensure proper stacking context
          m: 0, // No margin
          p: 0, // No padding
        }}
      >
        {/* Header */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            p: 2,
            backgroundColor: theme.palette.background.default,
          }}
        >
          <Typography variant="subtitle1">{title}</Typography>
        </Box>

        <Divider />

        {/* Content - always expanded */}
        <Box sx={{ overflow: 'auto' }}>
          {children || renderFormFields()}
        </Box>
      </Paper>
    </Box>
  );
};

export default VisualAttributesPanel;
