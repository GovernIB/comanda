import Grid from "@mui/material/Grid";
import {Box, Typography} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from './EstadisticaWidgetFormFields';
import TaulaWidgetVisualization from "./TaulaWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesDimensio } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";

const EstadisticaTaulaWidgetForm: React.FC = () => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    const [previewData, setPreviewData] = useState({
        title: 'Títol de la taula',
        mostrarCapcalera: true,
        mostrarBordes: true,
        mostrarAlternancia: true,
        colorAlternancia: '#f5f5f5',
        columnesEstils: [],
        cellesDestacades: [],
    });


    // Watch for changes in form data to update preview
    useEffect(() => {
        if (data) {
            setPreviewData({
                title: data.titol || 'Títol de la taula',
                mostrarCapcalera: data.atributsVisuals?.mostrarCapcalera !== undefined ? data.atributsVisuals.mostrarCapcalera : true,
                mostrarBordes: data.atributsVisuals?.mostrarBordes !== undefined ? data.atributsVisuals.mostrarBordes : true,
                mostrarAlternancia: data.atributsVisuals?.mostrarAlternancia !== undefined ? data.atributsVisuals.mostrarAlternancia : true,
                colorAlternancia: data.atributsVisuals?.colorAlternancia || '#f5f5f5',
                columnesEstils: data.atributsVisuals?.columnesEstils || [],
                cellesDestacades: data.atributsVisuals?.cellesDestacades || [],
            });
        }
    }, [data]);

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                        <Grid size={12}><FormField name="dimensioAgrupacio" advancedSearchColumns={columnesDimensio} /></Grid>
                        <Grid size={12}><FormField name="titolAgrupament" /></Grid>
                        <Grid size={12}><FormField name="columnes" multiple  /></Grid> {/** Canviar a un component per a crear els IndicadorTaula com objecte, que es procesara després */}
                </EstadisticaWidgetFormFields>
            </Grid>

            <Grid id={'cv'} size={{xs: 12, sm: 4}}>
                <VisualAttributesPanel widgetType="taula" title="Configuració visual">
                    {/* Preview inside the panel */}
                    <Box sx={{ p: 2 }}>
                        <Typography variant="subtitle2" sx={{ mb: 2 }}>Previsualització</Typography>
                        <Box sx={{ height: '240px' }}>
                            <TaulaWidgetVisualization
                                preview={true}
                                {...previewData}
                            />
                        </Box>
                        {renderTaulaFormFields()}
                    </Box>
                </VisualAttributesPanel>
            </Grid>
        </Grid>
    );

    // Render form fields for taula widget
    function renderTaulaFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Configuració general</Typography></Grid>
                <Grid size={6}><FormField name="atributsVisuals.mostrarCapcalera" label="Mostrar capçalera" type="checkbox" /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.mostrarBordes" label="Mostrar vores" type="checkbox" /></Grid>
                <Grid size={6}><FormField name="atributsVisuals.mostrarAlternancia" label="Mostrar alternança de files" type="checkbox" /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="atributsVisuals.colorAlternancia" label="Color d'alternança" type="color" value={data?.atributsVisuals?.colorAlternancia || '#f5f5f5'} required={false} disabled={!data?.atributsVisuals?.mostrarAlternancia}
                /></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Estils de columnes</Typography></Grid>
                <Grid size={12}><Typography variant="body2" color="text.secondary">
                    Els estils de columnes es configuren a través d'una estructura complexa.
                    Utilitzeu el botó "Afegir estil de columna" per afegir-ne un de nou.
                </Typography></Grid>

                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Cel·les destacades</Typography></Grid>
                <Grid size={12}><Typography variant="body2" color="text.secondary">
                    Les cel·les destacades es configuren a través d'una estructura complexa.
                    Utilitzeu el botó "Afegir cel·la destacada" per afegir-ne una de nova.
                </Typography></Grid>
            </Grid>
        );
    }
}

export default EstadisticaTaulaWidgetForm;
