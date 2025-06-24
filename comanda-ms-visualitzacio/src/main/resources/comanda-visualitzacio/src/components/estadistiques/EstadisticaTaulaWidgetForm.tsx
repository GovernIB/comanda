import Grid from "@mui/material/Grid";
import {Box, Typography} from "@mui/material";
import {FormField, useFormContext} from "reactlib";
import * as React from "react";
import { useEffect, useMemo, useRef } from "react";
import EstadisticaWidgetFormFields from './EstadisticaWidgetFormFields';
import TaulaWidgetVisualization, {TaulaWidgetVisualizationProps} from "./TaulaWidgetVisualization";
import VisualAttributesPanel from "./VisualAttributesPanel";
import { columnesDimensio } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";
import {FormFieldDataActionType} from "../../../lib/components/form/FormContext.tsx";
import Button from "@mui/material/Button";
import EditIcon from '@mui/icons-material/Edit';
import ColumnesTable from './ColumnesTable';
import Divider from "@mui/material/Divider";

const EstadisticaTaulaWidgetForm: React.FC = () => {
    const { data, dataDispatchAction } = useFormContext();
    const { t } = useTranslation();
    const previewData: TaulaWidgetVisualizationProps = useMemo((): TaulaWidgetVisualizationProps =>({
        titol: data.titol,
        descripcio: data.descripcio,
        // columnes: [{}, {}, {}],
        // files: [{}, {}, {}],
        // Visual attributes
        // Contenidor
        colorText: data.colorText,
        colorFons: data.colorFons,
        mostrarVora: data.mostrarVora,
        colorVora: data.colorVora,
        ampleVora: data.ampleVora,
        // Taula
        colorTextTaula: data.colorTextTaula,
        colorFonsTaula: data.colorFonsTaula,
        mostrarCapcalera: data.mostrarCapcalera ?? true,
        colorCapcalera: data.colorCapcalera,
        colorFonsCapcalera: data.colorFonsCapcalera,
        mostrarAlternancia: data.mostrarAlternancia,
        colorAlternancia: data.colorAlternancia,
        mostrarVoraTaula: data.mostrarVoraTaula,
        colorVoraTaula: data.colorVoraTaula,
        ampleVoraTaula: data.ampleVoraTaula,
        mostrarSeparadorHoritzontal: data.mostrarSeparadorHoritzontal ?? true,
        colorSeparadorHoritzontal: data.colorSeparadorHoritzontal,
        ampleSeparadorHoritzontal: data.ampleSeparadorHoritzontal,
        mostrarSeparadorVertical: data.mostrarSeparadorVertical,
        colorSeparadorVertical: data.colorSeparadorVertical,
        ampleSeparadorVertical: data.ampleSeparadorVertical,
        paginada: data.paginada,
        columnesEstils: data.columnesEstils || [],
        cellesDestacades: data.cellesDestacades || [],
    }), [data]);

    const generateOnChange = (name: string, fieldName: string)  => ((value: any) => {
        // dataDispatchAction({
        //     type: FormFieldDataActionType.FIELD_CHANGE,
        //     payload: { fieldName: fieldName, value: {
        //             ...data[fieldName],
        //             [name]: value,
        //         } }
        // })
    })

    // Get the current values for conditional rendering
    const isMostrarVora: boolean = data?.mostrarVora;
    const isMostrarCapcalera: boolean = data?.mostrarCapcalera ?? true;
    const isMostrarAlternancia: boolean = data?.mostrarAlternancia;
    const isMostrarVoraTaula: boolean = data?.mostrarVoraTaula;
    const isMostrarSeparadorHoritzontal: boolean = data?.mostrarSeparadorHoritzontal ?? true;
    const isMostrarSeparadorVertical: boolean = data?.mostrarSeparadorVertical;

    // TODO: Hi ha alguna altre manera per definir valors predefinits pels camps?
    // Set default values for checkboxes when component mounts
    const initializedRef = useRef(false);
    useEffect(() => {
        if (!initializedRef.current) {
            if (data?.mostrarCapcalera === undefined) {
                dataDispatchAction({
                    type: FormFieldDataActionType.FIELD_CHANGE,
                    payload: { fieldName: "mostrarCapcalera", value: true }
                });
            }
            if (data?.mostrarSeparadorHoritzontal === undefined) {
                dataDispatchAction({
                    type: FormFieldDataActionType.FIELD_CHANGE,
                    payload: { fieldName: "mostrarSeparadorHoritzontal", value: true }
                });
            }
            initializedRef.current = true;
        }
    }, [data, dataDispatchAction]);

    return (
        <Grid container spacing={2}>
            <Grid size={{xs: 12, sm: 8}}>
                <EstadisticaWidgetFormFields>
                    <Grid size={12}><Divider sx={{ my: 1 }} >{t('page.widget.form.taula')}</Divider></Grid>
                    <Grid size={6}><FormField name="dimensioAgrupacio" advancedSearchColumns={columnesDimensio} /></Grid>
                    <Grid size={6}><FormField name="titolAgrupament" /></Grid>
                    <Grid size={12}>
                        <ColumnesTable name="columnes"
                                       label="Columnes de la taula"
                                       value={data.columnes}
                                       mostrarUnitat={true}
                                       onChange={(value) => {
                            dataDispatchAction({
                                type: FormFieldDataActionType.FIELD_CHANGE,
                                payload: { fieldName: "columnes", value }
                            });
                        }} />
                    </Grid>
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
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>Configuració general</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorText" label="Color de text" type="color" required={false} onChange={generateOnChange("colorText", "atributsVisuals")} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFons" label="Color de fons" type="color" required={false} onChange={generateOnChange("colorFons", "atributsVisuals")} /></Grid>
                <Grid size={12}><FormField name="mostrarVora" label="Mostrar vora" type="checkbox" onChange={generateOnChange("mostrarVora", "atributsVisuals")} /></Grid>
                { isMostrarVora && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorVora" label="Color de la vora" type="color" required={false} onChange={generateOnChange("colorVora", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleVora" label="Ample de la vora" type="number" required={false} onChange={generateOnChange("ampleVora", "atributsVisuals")} /></Grid>
                    </>
                )}
                <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>Configuració taula</Typography></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorTextTaula" label="Color de text de la taula" type="color" required={false} onChange={generateOnChange("colorTextTaula", "atributsVisuals")} /></Grid>
                <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFonsTaula" label="Color de fons de la taula" type="color" required={false} onChange={generateOnChange("colorFonsTaula", "atributsVisuals")} /></Grid>
                <Grid size={12}><FormField name="mostrarCapcalera" label="Mostrar capçalera" type="checkbox" onChange={generateOnChange("mostrarCapcalera", "atributsVisuals")} /></Grid>
                { isMostrarCapcalera && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorCapcalera" label="Color de text de la capçalera" type="color" required={false} onChange={generateOnChange("colorCapcalera", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorFonsCapcalera" label="Color de fons de la capçalera" type="color" required={false} onChange={generateOnChange("colorFonsCapcalera", "atributsVisuals")} /></Grid>
                    </>
                )}
                <Grid size={6}><FormField name="mostrarAlternancia" label="Mostrar alternança de files" type="checkbox" onChange={generateOnChange("mostrarAlternancia", "atributsVisuals")} /></Grid>
                { isMostrarAlternancia && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorAlternancia" label="Color d'alternança" type="color" required={false} onChange={generateOnChange("colorAlternancia", "atributsVisuals")} /></Grid>
                    </>
                )}
                <Grid size={12}><FormField name="mostrarVoraTaula" label="Mostrar vora de taula" type="checkbox" onChange={generateOnChange("mostrarVoraTaula", "atributsVisuals")} /></Grid>
                { isMostrarVoraTaula && (
                <>
                    <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorVoraTaula" label="Color de la vora" type="color" required={false} onChange={generateOnChange("colorVoraTaula", "atributsVisuals")} /></Grid>
                    <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleVoraTaula" label="Ample de la vora" type="number" required={false} onChange={generateOnChange("ampleVoraTaula", "atributsVisuals")} /></Grid>
                </>
                )}
                <Grid size={12}><FormField name="mostrarSeparadorHoritzontal" label="Mostrar separador horitzontal" type="checkbox" onChange={generateOnChange("mostrarSeparadorHoritzontal", "atributsVisuals")} /></Grid>
                { isMostrarSeparadorHoritzontal && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorSeparadorHoritzontal" label="Color del separador" type="color" required={false} onChange={generateOnChange("colorSeparadorHoritzontal", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleSeparadorHoritzontal" label="Ample del separador" type="number" required={false} onChange={generateOnChange("ampleSeparadorHoritzontal", "atributsVisuals")} /></Grid>
                    </>
                )}
                <Grid size={12}><FormField name="mostrarSeparadorVertical" label="Mostrar separador vertical" type="checkbox" onChange={generateOnChange("mostrarSeparadorVertical", "atributsVisuals")} /></Grid>
                { isMostrarSeparadorVertical && (
                    <>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="colorSeparadorVertical" label="Color del separador" type="color" required={false} onChange={generateOnChange("colorSeparadorVertical", "atributsVisuals")} /></Grid>
                        <Grid size={6} sx={{backgroundColor: '#FFFFFF'}}><FormField name="ampleSeparadorVertical" label="Ample del separador" type="number" required={false} onChange={generateOnChange("ampleSeparadorVertical", "atributsVisuals")} /></Grid>
                    </>
                )}
                {/*<Grid size={12}><FormField name="paginada" label="Taula paginada" type="checkbox" onChange={generateOnChange("paginada", "atributsVisuals")} /></Grid>*/}

                <Grid size={12} sx={{display: 'flex', flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'}}>
                    <Typography variant="subtitle2" sx={{ mt: 2, mb: 0 }}>Estils de columnes</Typography>
                    <Button variant="contained" startIcon={<EditIcon />} disabled>Configura estils</Button>
                </Grid>
                <Grid size={12}>
                    <Typography variant="body2" color="text.secondary">Els estils de columnes es configuren a través d'una estructura complexa. Utilitzeu el botó "Afegir estil de columna" per afegir-ne un de nou.</Typography>
                    <Typography variant="body2" color="#b71c1c">(Pendent de desenvolupament)</Typography>
                </Grid>

                <Grid size={12} sx={{display: 'flex', flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center'}}>
                    <Typography variant="subtitle2" sx={{ mt: 2, mb: 0 }}>Cel·les destacades</Typography>
                    <Button variant="contained" startIcon={<EditIcon />} disabled>Configura cel·les</Button>
                </Grid>
                <Grid size={12}>
                    <Typography variant="body2" color="text.secondary">Les cel·les destacades es configuren a través d'una estructura complexa. Utilitzeu el botó "Afegir cel·la destacada" per afegir-ne una de nova.</Typography>
                    <Typography variant="body2" color="#b71c1c">(Pendent de desenvolupament)</Typography>
                </Grid>
            </Grid>
        );
    }
}

export default EstadisticaTaulaWidgetForm;
