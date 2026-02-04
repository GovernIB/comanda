import React from 'react';
import Box from '@mui/material/Box';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import { useTheme } from '@mui/material/styles';
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
            <Grid container spacing={2} sx={{ p: 2 }}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>{t($ => $.page.widget.form.configGeneral)}</Typography></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><IconAutocompleteSelect name="atributsVisuals.icona" label={t($ => $.page.widget.atributsVisuals.icona)}/></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorText" label={t($ => $.page.widget.atributsVisuals.colorText)} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorFons" label={t($ => $.page.widget.atributsVisuals.colorFons)} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorIcona" label={t($ => $.page.widget.atributsVisuals.colorIcona)} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorFonsIcona" label={t($ => $.page.widget.atributsVisuals.colorFonsIcona)} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorTextDestacat" label={t($ => $.page.widget.atributsVisuals.colorTextDestacat)} type="color" required={false} /></Grid>
                <Grid size={6} />
                <Grid size={6}><FormField name="atributsVisuals.mostrarVora" label={t($ => $.page.widget.atributsVisuals.mostrarVora)} type="checkbox" /></Grid>
                <Grid size={6} />
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorVora" label={t($ => $.page.widget.atributsVisuals.colorVora)} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.ampleVora" label={t($ => $.page.widget.atributsVisuals.ampleVora)} type="number" required={false} /></Grid>
            </Grid>
        );
    };

    // Render form fields for grafic widget
    const renderGraficFormFields = () => {
        return (
            <Grid container spacing={2} sx={{ p: 2 }}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mb: 1 }}>{t($ => $.page.widget.form.configGeneral)}</Typography></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorsPaleta" label={t($ => $.page.widget.atributsVisuals.colorsPaleta)} /></Grid>
                <Grid size={12}><FormField name="atributsVisuals.mostrarReticula" label={t($ => $.page.widget.atributsVisuals.mostrarReticula)} type="checkbox" /></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>{t($ => $.page.widget.form.graficBar)}</Typography></Grid>
                <Grid size={6}><FormField name="atributsVisuals.barStacked" label={t($ => $.page.widget.atributsVisuals.barStacked)} type="checkbox" /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.barHorizontal" label={t($ => $.page.widget.atributsVisuals.barHorizontal)} type="checkbox" /></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>{t($ => $.page.widget.form.graficLin)}</Typography></Grid>
                <Grid size={6}><FormField name="atributsVisuals.lineShowPoints" label={t($ => $.page.widget.atributsVisuals.lineShowPoints)} type="checkbox" /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.lineSmooth" label={t($ => $.page.widget.atributsVisuals.lineSmooth)} type="checkbox" /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.lineWidth" label={t($ => $.page.widget.atributsVisuals.lineWidth)} type="number" required={false} /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.area" label={"Àrea"} type="checkbox" /></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>{t($ => $.page.widget.form.graficPst)}</Typography></Grid>
                <Grid size={6}><FormField name="atributsVisuals.pieDonut" label={t($ => $.page.widget.atributsVisuals.pieDonut)} type="checkbox" /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.pieShowLabels" label={t($ => $.page.widget.atributsVisuals.pieShowLabels)} type="checkbox" /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.outerRadius" label={"Radi exterior"} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.innerRadius" label={"Radi interior"} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.labelSize" label={"Mida etiqueta"} type="number" required={false} /></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>{t($ => $.page.widget.form.graficGug)}</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeMin" label={t($ => $.page.widget.atributsVisuals.gaugeMin)} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeMax" label={t($ => $.page.widget.atributsVisuals.gaugeMax)} type="number" required={false} /></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeColors" label={t($ => $.page.widget.atributsVisuals.gaugeColors)} /></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.gaugeRangs" label={t($ => $.page.widget.atributsVisuals.gaugeRangs)} /></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>{t($ => $.page.widget.form.graficMap)}</Typography></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.heatmapColors" label={t($ => $.page.widget.atributsVisuals.gaugeColors)} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.heatmapMinValue" label={t($ => $.page.widget.atributsVisuals.heatmapMinValue)} type="number" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.heatmapMaxValue" label={t($ => $.page.widget.atributsVisuals.heatmapMaxValue)} type="number" required={false} /></Grid>
            </Grid>
        );
    };

    // Render form fields for taula widget
    const renderTaulaFormFields = () => {
        return (
            <Grid container spacing={2} sx={{ p: 2 }}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mb: 1 }}>{t($ => $.page.widget.form.configGeneral)}</Typography></Grid>
                <Grid size={6}><FormField name="atributsVisuals.mostrarCapcalera" label={t($ => $.page.widget.atributsVisuals.mostrarCapcalera)} type="checkbox" /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.mostrarVora" label={t($ => $.page.widget.atributsVisuals.mostrarVora)} type="checkbox" /></Grid>
                <Grid size={12}><FormField name="atributsVisuals.mostrarAlternancia" label={t($ => $.page.widget.atributsVisuals.mostrarAlternancia)} type="checkbox" /></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}>
                    <FormField
                        name="atributsVisuals.colorAlternancia"
                        label={t($ => $.page.widget.atributsVisuals.colorAlternancia)}
                        type="color"
                        required={false}
                        disabled={!data?.atributsVisuals?.mostrarAlternancia}
                    />
                </Grid>
                <Grid size={6}><FormField name="atributsVisuals.mostrarSeparadorHoritzontal" label={"Separador Horitzontal"} type="checkbox" /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorSeparadorHoritzontal" label={"Color Separador Hor."} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.ampleSeparadorHoritzontal" label={"Ample Separador Hor."} type="number" required={false} /></Grid>

                <Grid size={6}><FormField name="atributsVisuals.mostrarSeparadorVertical" label={"Separador Vertical"} type="checkbox" /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorSeparadorVertical" label={"Color Separador Ver."} type="color" required={false} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.ampleSeparadorVertical" label={"Ample Separador Ver."} type="number" required={false} /></Grid>
            </Grid>
        );
    };

    return (
        <Box sx={{ position: 'relative', width: '100%', display: 'flex', minWidth: '40px', minHeight: '100px' }}>
            {/* Main panel */}
            <Paper
                elevation={1}
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    width: '100%',
                    overflow: 'hidden',
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
