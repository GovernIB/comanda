import React, { useState } from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import {styled, useTheme} from '@mui/material/styles';
import {FormField, useFormContext} from 'reactlib';
import Grid from "@mui/material/Grid";
import IconAutocompleteSelect from "../IconAutocompleteSelect.tsx";
import {useTranslation} from "react-i18next";

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
  const { t } = useTranslation();

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
      <Grid container spacing={2}>
          <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.configGeneral')}</Typography></Grid>
          <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><IconAutocompleteSelect name="atributsVisuals.icona" label={t('page.widget.atributsVisuals.icona')}/></Grid>

          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorText" label={t('page.widget.atributsVisuals.colorText')} type="color" required={false} /></Grid>
          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorFons" label={t('page.widget.atributsVisuals.colorFons')} type="color" required={false} /></Grid>
          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorIcona" label={t('page.widget.atributsVisuals.colorIcona')} type="color" required={false} /></Grid>
          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorFonsIcona" label={t('page.widget.atributsVisuals.colorFonsIcona')} type="color" required={false} /></Grid>

          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorTextDestacat" label={t('page.widget.atributsVisuals.colorTextDestacat')} type="color" required={false} /></Grid>
          <Grid size={6} />
          <Grid size={6}><FormField name="atributsVisuals.vora" label={t('page.widget.atributsVisuals.mostrarVora')} type="checkbox" /></Grid>
          <Grid size={6} />
          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorVora" label={t('page.widget.atributsVisuals.colorVora')} type="color" required={false} /></Grid>
          <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.ampleVora" label={t('page.widget.atributsVisuals.ampleVora')} type="number" required={false} /></Grid>
      </Grid>
    );
  };

  // Render form fields for grafic widget
  const renderGraficFormFields = () => {
    return (
      <Box sx={{ p: 2 }}>
        <Typography variant="subtitle2" sx={{ mb: 2 }}>{t('page.widget.form.configGeneral')}</Typography>
        <FormField name="atributsVisuals.colorsPaleta" label={t('page.widget.atributsVisuals.colorsPaleta')} />
        <FormField name="atributsVisuals.mostrarReticula" label={t('page.widget.atributsVisuals.mostrarReticula')} type="checkbox" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.graficBar')}</Typography>
        <FormField name="atributsVisuals.barStacked" label={t('page.widget.atributsVisuals.barStacked')} type="checkbox" />
        <FormField name="atributsVisuals.barHorizontal" label={t('page.widget.atributsVisuals.barHorizontal')} type="checkbox" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.graficLin')}</Typography>
        <FormField name="atributsVisuals.lineShowPoints" label={t('page.widget.atributsVisuals.lineShowPoints')} type="checkbox" />
        <FormField name="atributsVisuals.lineSmooth" label={t('page.widget.atributsVisuals.lineSmooth')} type="checkbox" />
        <FormField name="atributsVisuals.lineWidth" label={t('page.widget.atributsVisuals.lineWidth')} type="number" required={false} />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.graficPst')}</Typography>
        <FormField name="atributsVisuals.pieDonut" label={t('page.widget.atributsVisuals.pieDonut')} type="checkbox" />
        <FormField name="atributsVisuals.pieShowLabels" label={t('page.widget.atributsVisuals.pieShowLabels')} type="checkbox" />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.graficGug')}</Typography>
        <FormField name="atributsVisuals.gaugeMin" label={t('page.widget.atributsVisuals.gaugeMin')} type="number" required={false} />
        <FormField name="atributsVisuals.gaugeMax" label={t('page.widget.atributsVisuals.gaugeMax')} type="number" required={false} />
        <FormField name="atributsVisuals.gaugeColors" label={t('page.widget.atributsVisuals.gaugeColors')} />
        <FormField name="atributsVisuals.gaugeRangs" label={t('page.widget.atributsVisuals.gaugeRangs')} />

        <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>{t('page.widget.form.graficMap')}</Typography>
        <FormField name="atributsVisuals.heatmapColors" label={t('page.widget.atributsVisuals.gaugeColors')} />
        <FormField name="atributsVisuals.heatmapMinValue" label={t('page.widget.atributsVisuals.heatmapMinValue')} type="number" required={false} />
        <FormField name="atributsVisuals.heatmapMaxValue" label={t('page.widget.atributsVisuals.heatmapMaxValue')} type="number" required={false} />
      </Box>
    );
  };

  // Render form fields for taula widget
  const renderTaulaFormFields = () => {
    return (
      <Box sx={{ p: 2 }}>
        <Typography variant="subtitle2" sx={{ mb: 2 }}>{t('page.widget.form.configGeneral')}</Typography>
        <FormField name="atributsVisuals.mostrarCapcalera" label={t('page.widget.atributsVisuals.mostrarCapcalera')} type="checkbox" />
        <FormField name="atributsVisuals.mostrarVora" label={t('page.widget.atributsVisuals.mostrarVora')} type="checkbox" />
        <FormField name="atributsVisuals.mostrarAlternancia" label={t('page.widget.atributsVisuals.mostrarAlternancia')} type="checkbox" />
        <FormField
          name="atributsVisuals.colorAlternancia"
          label={t('page.widget.atributsVisuals.colorAlternancia')}
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
          backgroundColor: '#f8f8f8',
        }}
      >
        {/* Header */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            p: 2,
            // backgroundColor: theme.palette.background.default,
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
