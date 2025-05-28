import Grid from "@mui/material/Grid2";
import {Divider, Box} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useState, useEffect } from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import SimpleWidgetVisualization from "./SimpleWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";

const EstadisticaSimpleWidgetForm: React.FC = () => {
    const { data } = useFormContext();
    const [previewData, setPreviewData] = useState({
        title: 'Títol del widget',
        value: 1234,
        unit: 'unitat',
        icona: 'trending_up',
        colorText: undefined,
        colorFons: undefined,
        colorIcona: undefined,
        colorFonsIcona: undefined,
        destacat: false,
        colorTextDestacat: undefined,
        colorFonsDestacat: undefined,
        borde: false,
        colorBorde: undefined,
    });

    // Watch for changes in form data to update preview
    useEffect(() => {
        if (data) {
            setPreviewData({
                title: data.titol || 'Títol del widget',
                value: 1234, // Sample value for preview
                unit: data.unitat || 'unitat',
                icona: data.atributsVisuals?.icona || 'trending_up',
                colorText: data.atributsVisuals?.colorText,
                colorFons: data.atributsVisuals?.colorFons,
                colorIcona: data.atributsVisuals?.colorIcona,
                colorFonsIcona: data.atributsVisuals?.colorFonsIcona,
                destacat: data.atributsVisuals?.destacat || false,
                colorTextDestacat: data.atributsVisuals?.colorTextDestacat,
                colorFonsDestacat: data.atributsVisuals?.colorFonsDestacat,
                borde: data.atributsVisuals?.borde || false,
                colorBorde: data.atributsVisuals?.colorBorde,
            });
        }
    }, [data]);

    return (
        <Box sx={{ display: 'flex', width: '100%' }}>
            <Box sx={{ flex: '1 1 auto' }}>
                <EstadisticaWidgetFormFields>
                    <Grid size={6}><FormField name="indicador" /></Grid>
                    <Grid size={6}><FormField name="unitat" /></Grid>
                </EstadisticaWidgetFormFields>
            </Box>

            <Box sx={{ width: '400px', ml: 2, display: 'flex', flexDirection: 'column' }}>
                {/* Preview */}
                <Box sx={{ mb: 2, height: '250px' }}>
                    <SimpleWidgetVisualization
                        preview={true}
                        {...previewData}
                    />
                </Box>

                {/* Visual attributes panel */}
                <Box sx={{ flex: 1 }}>
                    <VisualAttributesPanel
                        widgetType="simple"
                        title="Configuració visual"
                    />
                </Box>
            </Box>
        </Box>
    );
}

export default EstadisticaSimpleWidgetForm;
