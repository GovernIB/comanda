import Grid from "@mui/material/Grid";
import {FormField, useFormContext, springFilterBuilder as builder } from "reactlib";
import * as React from "react";
import {useState, useEffect, useMemo} from "react";
import EstadisticaWidgetFormFields from "./EstadisticaWidgetFormFields";
import SimpleWidgetVisualization, {SimpleWidgetVisualizationProps} from "./SimpleWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { Divider, Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import IconAutocompleteSelect from "../IconAutocompleteSelect.tsx";
import {FormFieldDataActionType} from "../../../lib/components/form/FormContext.tsx";


const EstadisticaSimpleWidgetForm: React.FC = () => {
    const { data, dataDispatchAction, isReady } = useFormContext();
    const { t } = useTranslation();
    const previewData: SimpleWidgetVisualizationProps = useMemo((): SimpleWidgetVisualizationProps =>({
        titol: data.titol || 'Títol del widget',
        valor: 1234, // Sample value for preview
        unitat: data.unitat || 'unitat',
        descripcio: data.descripcio || 'descripcio del widget',
        canviPercentual: data.canviPercentual || '12.34%',
        icona: data.icona,
        colorText: data.colorText,
        colorFons: data.colorFons,
        colorIcona: data.colorIcona,
        colorFonsIcona: data.colorFonsIcona,
        colorTextDestacat: data.colorTextDestacat,
        mostrarVora: data.mostrarVora || false,
        colorVora: data.colorVora,
        ampleVora: data.ampleVora,
    }), [data]);

    const isMostrarVora: boolean = data?.mostrarVora;
    const isIcona: boolean = !!data?.icona;


    // useEffect(() => {
    //     dataDispatchAction({
    //         type: FormFieldDataActionType.FIELD_CHANGE,
    //         payload: { fieldName: "atributsVisuals", value: {
    //                 'icona': data.icona,
    //                 'colorText': data.colorText,
    //                 'colorFons': data.colorFons,
    //                 'colorIcona': data.colorIcona,
    //                 'colorFonsIcona': data.colorFonsIcona,
    //                 'colorTextDestacat': data.colorTextDestacat,
    //                 'mostrarVora': data.mostrarVora,
    //                 'colorVora': data.colorVora,
    //                 'ampleVora': data.ampleVora,
    //             } }
    //     });
    //     dataDispatchAction({
    //         type: FormFieldDataActionType.FIELD_CHANGE,
    //         payload: { fieldName: "indicadorAgregacio", value: {
    //                 'indicador': data.indicador,
    //                 'titolIndicador': data.titolIndicador,
    //                 'tipusIndicador': data.tipusIndicador,
    //                 'periodeIndicador': data.periodeIndicador,
    //             } }
    //     })
    // }, [isReady]);

    const generateOnChange = (name: string, fieldName: string)  => ((value: any) => {
        // dataDispatchAction({
        //     type: FormFieldDataActionType.FIELD_CHANGE,
        //     payload: { fieldName: fieldName, value: {
        //             ...data[fieldName],
        //             [name]: value,
        //         } }
        // })
    })

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                    <Grid size={12}><Divider sx={{ my: 1 }} >{t('page.widget.form.simple')}</Divider></Grid>
                    <Grid size={6}><FormField name="unitat" /></Grid>
                    <Grid size={6}><FormField name="compararPeriodeAnterior" /></Grid>
                    <Grid size={12}><FormField name="indicador" advancedSearchColumns={columnesIndicador} onChange={generateOnChange("indicador", "indicadorAgregacio")} /></Grid>
                    <Grid size={12}><FormField name="titolIndicador" onChange={generateOnChange("titolIndicador", "indicadorAgregacio")} /></Grid>
                    <Grid size={6}><FormField name="tipusIndicador" onChange={generateOnChange("tipusIndicador", "indicadorAgregacio")} /></Grid>
                    <Grid size={6}><FormField name="periodeIndicador" onChange={generateOnChange("periodeIndicador", "indicadorAgregacio")} disabled={data.tipusIndicador !== 'AVERAGE'} /></Grid>
                </EstadisticaWidgetFormFields>
            </Grid>

            <Grid id={'cv'} size={{xs: 12, sm: 4}} sx={{backgroundColor: '#f5f5f5'}}>
                <VisualAttributesPanel widgetType="simple" title="Configuració visual">
                    {/* Preview inside the panel */}
                    <Box sx={{ p: 2 }}>
                        <Typography variant="subtitle2" sx={{ mb: 2 }}>Previsualització</Typography>
                        <Box sx={{ height: '190px' }}>
                            <SimpleWidgetVisualization
                                preview={true}
                                {...previewData}
                            />
                        </Box>
                        {renderSimpleFormFields()}
                    </Box>
                </VisualAttributesPanel>
            </Grid>
        </Grid>
    );

    // Render form fields for simple widget
    function renderSimpleFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>Configuració general</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorText" label="Color del text" type="color" required={false} onChange={generateOnChange("colorText", "atributsVisuals")} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFons" label="Color de fons" type="color" required={false} onChange={generateOnChange("colorFons", "atributsVisuals")} /></Grid>
                <Grid size={12} sx={{backgroundColor: '#FFFFFF'}}><IconAutocompleteSelect name="icona" label={"Icona"} onChange={generateOnChange("icona", "atributsVisuals")} /></Grid>
                { isIcona && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorIcona" label="Color de la icona" type="color" required={false} onChange={generateOnChange("colorIcona", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFonsIcona" label="Color de fons de la icona" type="color" required={false} onChange={generateOnChange("colorFonsIcona", "atributsVisuals")} /></Grid>
                    </>
                )}
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorTextDestacat" label="Color del text destacat" type="color" required={false} onChange={generateOnChange("colorTextDestacat", "atributsVisuals")} /></Grid>
                <Grid size={6} />
                <Grid size={12}><FormField name="mostrarVora" label="Mostrar vora" type="checkbox" onChange={generateOnChange("mostrarVora", "atributsVisuals")} /></Grid>
                { isMostrarVora && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorVora" label="Color de la vora" type="color" required={false} onChange={generateOnChange("colorVora", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleVora" label="Ample de la vora" type="number" required={false} onChange={generateOnChange("ampleVora", "atributsVisuals")} /></Grid>
                    </>
                )}
            </Grid>
        );
    }
}

export default EstadisticaSimpleWidgetForm;
