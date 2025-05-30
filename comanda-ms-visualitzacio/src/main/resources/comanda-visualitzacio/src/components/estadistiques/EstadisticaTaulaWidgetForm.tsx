import Grid from "@mui/material/Grid2";
import {Box} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from './EstadisticaWidgetFormFields';
import TaulaWidgetVisualization from "./TaulaWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesDimensio } from '../sharedAdvancedSearch/advancedSearchColumns';

const EstadisticaTaulaWidgetForm: React.FC = () => {
    const { data } = useFormContext();
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
        <Box sx={{ display: 'flex', width: '100%' }}>
            <Box sx={{ flex: '1 1 auto' }}>
                <EstadisticaWidgetFormFields>
                        <Grid size={12}><FormField name="dimensioAgrupacio" advancedSearchColumns={columnesDimensio} /></Grid>
                        <Grid size={12}><FormField name="titolAgrupament" /></Grid>
                        <Grid size={12}><FormField name="columnes" multiple  /></Grid> {/** Canviar a un component per a crear els IndicadorTaula com objecte, que es procesara després */}
                </EstadisticaWidgetFormFields>
            </Box>

            <Box sx={{ width: '500px', ml: 2, display: 'flex', flexDirection: 'column' }}>
                {/* Preview */}
                <Box sx={{ mb: 2, height: '300px' }}>
                    <TaulaWidgetVisualization 
                        preview={true}
                        {...previewData}
                    />
                </Box>

                {/* Visual attributes panel */}
                <Box sx={{ flex: 1 }}>
                    <VisualAttributesPanel 
                        widgetType="taula"
                        title="Configuració visual"
                    />
                </Box>
            </Box>
        </Box>
    );
}

export default EstadisticaTaulaWidgetForm;
