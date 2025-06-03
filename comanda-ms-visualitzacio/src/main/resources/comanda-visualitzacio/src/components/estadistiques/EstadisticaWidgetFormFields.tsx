import {FormField, useFormContext} from "reactlib";
import Grid from "@mui/material/Grid2";
import * as React from "react";
import Divider from "@mui/material/Divider";
import { columnesDimensioValor } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";

const EstadisticaWidgetFormFields: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { data } = useFormContext();
    const { t } = useTranslation();

    // Get the current values for conditional rendering
    const periodeMode = data?.periodeMode;
    const presetPeriode = data?.presetPeriode;
    const absolutTipus = data?.absolutTipus;
    const absolutAnyReferencia = data?.absolutAnyReferencia;

    // Check if presetCount should be visible
    const isPresetCountVisible = periodeMode === 'PRESET' &&
        ['DARRERS_N_DIES', 'DARRERES_N_SETMANES', 'DARRERS_N_MESOS', 'DARRERS_N_TRIMESTRES', 'DARRERS_N_ANYS'].includes(presetPeriode);

    // Check if RELATIU fields should be visible
    const isRelatiuVisible = periodeMode === 'RELATIU';

    // Check if ABSOLUT fields should be visible
    const isAbsolutVisible = periodeMode === 'ABSOLUT';

    // Check which ABSOLUT fields should be visible based on absolutTipus
    const isDateRangeVisible = isAbsolutVisible && absolutTipus === 'DATE_RANGE';
    const isSpecificPeriodVisible = isAbsolutVisible && absolutTipus === 'SPECIFIC_PERIOD_OF_YEAR';

    // Check if absolutAnyValor should be visible
    const isAbsolutAnyValorVisible = isSpecificPeriodVisible && absolutAnyReferencia === 'SPECIFIC_YEAR';

    return <>
        <Grid container spacing={2}>
            <Grid size={12}><FormField name="appId" /></Grid>
            <Grid size={12}><FormField name="aplicacioNom" disabled /></Grid>
            <Grid size={12}><FormField name="titol" /></Grid>
            <Grid size={12}><FormField name="descripcio" type="textarea"/></Grid>

            <Grid size={12}><FormField name="dimensionsValor" multiple advancedSearchColumns={columnesDimensioValor} /></Grid>

            <Grid size={12}><Divider sx={{ my: 1 }} >{t('page.widget.form.periode')}</Divider></Grid>

            {/* Periodo base, depenent des tipus apareixeran o no els camps de abaix */}
            <Grid size={12}><FormField name="periodeMode" /></Grid>

            {/* Camps PRESET */}
            {periodeMode === 'PRESET' && (<Grid size={12}><FormField name="presetPeriode" /></Grid>)}
            {isPresetCountVisible && (<Grid size={12}><FormField name="presetCount" /></Grid>)}

            {/* Camps RELATIU */}
            {isRelatiuVisible && (
                <>
                    <Grid size={12}><FormField name="relatiuPuntReferencia" /></Grid>
                    <Grid size={6}><FormField name="relatiuCount" /></Grid>
                    <Grid size={6}><FormField name="relatiueUnitat" /></Grid>
                    <Grid size={12}><FormField name="relatiuAlineacio" /></Grid>
                </>
            )}

            {/* Camps ABSOLUT */}
            {isAbsolutVisible && (<Grid size={12}><FormField name="absolutTipus" /></Grid>)}

            {/* Camps DATE_RANGE fields */}
            {isDateRangeVisible && (
                <>
                    <Grid size={6}><FormField name="absolutDataInici" /></Grid>
                    <Grid size={6}><FormField name="absolutDataFi" /></Grid>
                </>
            )}

            {/* Camps SPECIFIC_PERIOD_OF_YEAR fields */}
            {isSpecificPeriodVisible && (
                <>
                    <Grid size={8}><FormField name="absolutAnyReferencia" /></Grid>
                    <Grid size={4}><FormField name="absolutAnyValor" disabled={!isAbsolutAnyValorVisible} /></Grid>
                    <Grid size={12}><FormField name="absolutPeriodeUnitat" /></Grid>
                    <Grid size={6}><FormField name="absolutPeriodeInici" /></Grid>
                    <Grid size={6}><FormField name="absolutPeriodeFi" /></Grid>
                </>
            )}

            { children }
        </Grid>
    </>;
}

export default EstadisticaWidgetFormFields;