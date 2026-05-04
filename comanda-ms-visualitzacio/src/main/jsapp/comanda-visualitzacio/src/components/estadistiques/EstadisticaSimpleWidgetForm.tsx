import Grid from '@mui/material/Grid';
import { FormField, useFormContext } from 'reactlib';
import * as React from 'react';
import { useMemo } from 'react';
import EstadisticaWidgetFormFields from './EstadisticaWidgetFormFields';
import SimpleWidgetVisualization, {
    SimpleWidgetVisualizationProps,
} from './SimpleWidgetVisualization';
import VisualAttributesPanel from './VisualAttributesPanel';
import { columnesIndicador } from '../sharedAdvancedSearch/advancedSearchColumns';
import { Divider, Box, Typography } from '@mui/material';
import { useTranslation } from 'react-i18next';
import IconAutocompleteSelect from '../IconAutocompleteSelect';
import FormFieldCustomAdvancedSearch from '../FormFieldCustomAdvancedSearch';

type EstadisticaSimpleWidgetFormProps = {
    mode?: 'full' | 'stats' | 'visual';
};

const EstadisticaSimpleWidgetForm: React.FC<EstadisticaSimpleWidgetFormProps> = ({ mode = 'full' }) => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    const previewData: SimpleWidgetVisualizationProps = useMemo(
        (): SimpleWidgetVisualizationProps => ({
            titol: data.titol || 'Títol del widget',
            valor: 1234, // Sample value for preview
            unitat: data.unitat || 'unitat',
            descripcio: data.descripcio || 'descripcio del widget',
            canviPercentual: data.canviPercentual || '12.34',
            icona: data.icona,
            colorText: data.colorText,
            colorFons: data.colorFons,
            colorIcona: data.colorIcona,
            colorFonsIcona: data.colorFonsIcona,
            colorTextDestacat: data.colorTextDestacat,
            mostrarVora: data.mostrarVora || false,
            colorVora: data.colorVora,
            ampleVora: data.ampleVora,
            midaFontTitol: data.midaFontTitol,
            midaFontDescripcio: data.midaFontDescripcio,
            midaFontValor: data.midaFontValor,
            midaFontUnitats: data.midaFontUnitats,
            midaFontCanviPercentual: data.midaFontCanviPercentual,
        }),
        [data]
    );

    const isMostrarVora: boolean = data?.mostrarVora;
    const isIcona: boolean = !!data?.icona;
    const indicadorNamedQueries = React.useMemo(() => [`filterByAppGroupByNom:${data?.aplicacio?.id}`], [data?.aplicacio?.id]);

    if (mode === 'stats') {
        return renderStatsFields();
    }

    if (mode === 'visual') {
        return renderVisualContent();
    }

    return (
        <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 8 }}>
                {renderStatsFields()}
            </Grid>
            <Grid id={'cv'} size={{ xs: 12, sm: 4 }} sx={{ backgroundColor: 'background.default' }}>
                <VisualAttributesPanel
                    widgetType="simple"
                    title={t($ => $.page.widget.form.configVisual)}
                >
                    {renderVisualContent()}
                </VisualAttributesPanel>
            </Grid>
        </Grid>
    );

    function renderStatsFields() {
        return (
            <EstadisticaWidgetFormFields>
                <Grid size={12}>
                    <Divider sx={{ my: 1 }}>{t($ => $.page.widget.form.simple)}</Divider>
                </Grid>
                <Grid size={12}>
                    <IconAutocompleteSelect
                        name="icona"
                        label={t($ => $.page.widget.atributsVisuals.icona)}
                    />
                </Grid>
                <Grid size={6}>
                    <FormField name="unitat" />
                </Grid>
                <Grid size={6}>
                    <FormField name="compararPeriodeAnterior" />
                </Grid>
                <Grid size={12}>
                    <FormFieldCustomAdvancedSearch
                        name="indicador"
                        namedQueries={indicadorNamedQueries}
                        advancedSearchColumns={columnesIndicador}
                        advancedSearchDataGridProps={{ rowHeight: 30 }}
                        advancedSearchDialogHeight={500}
                    />
                </Grid>
                <Grid size={12}>
                    <FormField
                        name="titolIndicador"
                    />
                </Grid>
                <Grid size={6}>
                    <FormField
                        name="tipusIndicador"
                    />
                </Grid>
                <Grid size={6}>
                    <FormField
                        name="periodeIndicador"
                        disabled={data.tipusIndicador !== 'AVERAGE'}
                    />
                </Grid>
            </EstadisticaWidgetFormFields>
        );
    }

    function renderVisualContent() {
        return (
            <Box sx={{ p: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 2 }}>
                    {t($ => $.page.widget.form.preview)}
                </Typography>
                <Box sx={{ height: '190px' }}>
                    <SimpleWidgetVisualization preview={true} {...previewData} />
                </Box>
                {renderSimpleFormFields()}
            </Box>
        );
    }

    // Render form fields for simple widget
    function renderSimpleFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}>
                    <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>
                        {t($ => $.page.widget.form.configGeneral)}
                    </Typography>
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="colorText"
                        label={t($ => $.page.widget.atributsVisuals.colorText)}
                        type="color"
                        required={false}
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="colorFons"
                        label={t($ => $.page.widget.atributsVisuals.colorFons)}
                        type="color"
                        required={false}
                    />
                </Grid>
                {isIcona && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorIcona"
                                label={t($ => $.page.widget.atributsVisuals.colorIcona)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorFonsIcona"
                                label={t($ => $.page.widget.atributsVisuals.colorFonsIcona)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="colorTextDestacat"
                        label={t($ => $.page.widget.atributsVisuals.colorTextDestacat)}
                        type="color"
                        required={false}
                    />
                </Grid>
                <Grid size={6} />
                <Grid size={12}>
                    <FormField
                        name="mostrarVora"
                        label={t($ => $.page.widget.atributsVisuals.mostrarVora)}
                        type="checkbox"
                    />
                </Grid>
                {isMostrarVora && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorVora"
                                label={t($ => $.page.widget.atributsVisuals.colorVora)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="ampleVora"
                                label={t($ => $.page.widget.atributsVisuals.ampleVora)}
                                type="number"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                <Grid size={12}>
                    <Typography variant="subtitle2" sx={{ mt: 3, mb: 2 }}>
                        {t($ => $.page.widget.form.configFont)}
                    </Typography>
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="midaFontTitol"
                        label={t($ => $.page.widget.atributsVisuals.midaFontTitol)}
                        type="number"
                        required={false}
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="midaFontDescripcio"
                        label={t($ => $.page.widget.atributsVisuals.midaFontDescripcio)}
                        type="number"
                        required={false}
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="midaFontValor"
                        label={t($ => $.page.widget.atributsVisuals.midaFontValor)}
                        type="number"
                        required={false}
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="midaFontUnitats"
                        label={t($ => $.page.widget.atributsVisuals.midaFontUnitats)}
                        type="number"
                        required={false}
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="midaFontCanviPercentual"
                        label={t($ => $.page.widget.atributsVisuals.midaFontCanviPercentual)}
                        type="number"
                        required={false}
                    />
                </Grid>
            </Grid>
        );
    }
};

export default EstadisticaSimpleWidgetForm;
