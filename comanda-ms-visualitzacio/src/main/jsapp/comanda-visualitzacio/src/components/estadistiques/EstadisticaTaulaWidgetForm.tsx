import Grid from '@mui/material/Grid';
import { Box, MenuItem, Select, Typography } from '@mui/material';
import { FormField, useFormContext } from 'reactlib';
import * as React from 'react';
import { useEffect, useMemo, useRef } from 'react';
import EstadisticaWidgetFormFields, { FieldHelp } from './EstadisticaWidgetFormFields';
import VisualAttributesPanel from './VisualAttributesPanel';
import { columnesDimensio } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from 'react-i18next';
import Button from '@mui/material/Button';
import EditIcon from '@mui/icons-material/Edit';
import ColumnesTable from './ColumnesTable';
import Divider from '@mui/material/Divider';
import { WidgetPreview } from './WidgetPreview';
import { TaulaWidgetVisualizationProps } from './TaulaWidgetVisualization';

/** Camps que sobreescriuen l'estil de la plantilla (excloent `mostrarCapcalera`/`mostrarSeparadorHoritzontal`, que s'inicialitzen sempre a `true` en muntar-se) */
export const TAULA_OVERRIDE_FIELDS = [
    'colorText', 'colorFons', 'mostrarVora', 'colorVora', 'ampleVora',
    'colorTextTaula', 'colorFonsTaula', 'colorCapcalera', 'colorFonsCapcalera',
    'mostrarAlternancia', 'colorAlternancia',
    'mostrarVoraTaula', 'colorVoraTaula', 'ampleVoraTaula',
    'colorSeparadorHoritzontal', 'ampleSeparadorHoritzontal',
    'mostrarSeparadorVertical', 'colorSeparadorVertical', 'ampleSeparadorVertical',
    'midaFontTitol', 'midaFontDescripcio', 'midaFontTaula', 'compacte',
];

/** Indica si el widget té algun valor que sobreescrigui la plantilla (per mostrar l'indicador de "personalitzat") */
export const hasVisualOverrides = (data: any): boolean =>
    TAULA_OVERRIDE_FIELDS.some(field => data?.[field] !== undefined && data?.[field] !== null && data?.[field] !== '');

type EstadisticaTaulaWidgetFormProps = {
    mode?: 'full' | 'stats' | 'indicators' | 'visual';
    dashboardPlantilla?: any;
    destacat?: boolean;
    /** Indica si s'han de mostrar els camps que sobreescriuen la plantilla (per defecte, sí). La previsualització es mostra sempre. */
    showOverrideFields?: boolean;
};

const EstadisticaTaulaWidgetForm: React.FC<EstadisticaTaulaWidgetFormProps> = ({ mode = 'full', dashboardPlantilla, destacat, showOverrideFields = true }) => {
    const { data, apiRef } = useFormContext();
    const { t } = useTranslation();
    const previewData: TaulaWidgetVisualizationProps = useMemo(
        (): TaulaWidgetVisualizationProps => ({
            entornCodi: 'ENT',
            titol: data.titol,
            descripcio: data.descripcio || t($ => $.page.plantilla.sample.tableDescription),
            destacat: data.destacat || destacat,
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
            midaFontTaula: data.midaFontTaula,
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
            compacte: data.compacte,
            columnesEstils: data.columnesEstils || [],
            cellesDestacades: data.cellesDestacades || [],
            midaFontTitol: data.midaFontTitol,
            midaFontDescripcio: data.midaFontDescripcio,
        }),
        [data, destacat]
    );

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
                apiRef.current?.setFieldValue('mostrarCapcalera', true);
            }
            if (data?.mostrarSeparadorHoritzontal === undefined) {
                apiRef.current?.setFieldValue('mostrarSeparadorHoritzontal', true);
            }
            initializedRef.current = true;
        }
    }, [data]);
    const dimensioNamedQueries = React.useMemo(() => [`filterByAppGroupByNom:${data?.aplicacio?.id}`], [data?.aplicacio?.id]);

    if (mode === 'stats') {
        return renderStatsFields();
    }

    if (mode === 'indicators') {
        return <Grid container spacing={2}>{renderIndicatorFields()}</Grid>;
    }

    if (mode === 'visual') {
        return renderVisualContent();
    }

    return (
        <Grid container spacing={2}>
            <Grid size={{ xs: 12, sm: 8 }}>
                {renderStatsFields()}
            </Grid>
            <Grid id={'cv'} size={{ xs: 12, sm: 4 }}>
                <VisualAttributesPanel
                    widgetType="taula"
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
                {renderIndicatorFields()}
            </EstadisticaWidgetFormFields>
        );
    }

    function renderIndicatorFields() {
        return (
            <>
                <Grid size={12}>
                    <Divider sx={{ my: 1 }}>{t($ => $.page.widget.form.taula)}</Divider>
                </Grid>
                <Grid size={12}>
                    <ColumnesTable
                        name="columnes"
                        label={t($ => $.page.widget.taula.tableCols)}
                        value={data.columnes}
                        mostrarUnitat={true}
                        onChange={value => {
                            apiRef.current?.setFieldValue('columnes', value);
                        }}
                    />
                </Grid>
                <Grid size={12}>
                    <Divider sx={{ my: 1 }} />
                </Grid>
                <Grid size={6}>
                    <FormField
                        name="dimensioAgrupacio"
                        namedQueries={dimensioNamedQueries}
                        advancedSearchColumns={columnesDimensio}
                    />
                    <FieldHelp text={t($ => $.page.widget.form.help.dimensioAgrupacio)} />
                </Grid>
                <Grid size={6}>
                    <FormField name="titolAgrupament" />
                </Grid>
                <Grid size={12}>
                    <FormField
                        name="amagarFilesZero"
                        label={t($ => $.page.widget.taula.amagarFilesZero)}
                        type="checkbox"
                    />
                </Grid>
                <Grid size={4}>
                    <Select
                        fullWidth
                        size="small"
                        displayEmpty
                        value={data?.columnaOrdenacio ?? ''}
                        onChange={(event) => {
                            const value = event.target.value === '' ? null : Number(event.target.value);
                            apiRef.current?.setFieldValue('columnaOrdenacio', value);
                            if (value == null) {
                                apiRef.current?.setFieldValue('direccioOrdenacio', null);
                                apiRef.current?.setFieldValue('limitResultats', null);
                            } else if (data?.direccioOrdenacio == null) {
                                apiRef.current?.setFieldValue('direccioOrdenacio', 'DESC');
                            }
                        }}
                    >
                        <MenuItem value="">{t($ => $.page.widget.taula.senseOrdenacio)}</MenuItem>
                        {(data?.columnes ?? []).map((columna: any, index: number) => (
                            <MenuItem key={index} value={index}>
                                {columna?.titol || `Col ${index + 1}`}
                            </MenuItem>
                        ))}
                    </Select>
                </Grid>
                {data?.columnaOrdenacio != null && (
                    <>
                        <Grid size={4}>
                            <FormField
                                name="direccioOrdenacio"
                                label={t($ => $.page.widget.taula.direccioOrdenacio)}
                            />
                        </Grid>
                        <Grid size={4}>
                            <FormField
                                name="limitResultats"
                                label={t($ => $.page.widget.taula.limitResultats)}
                                type="number"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
            </>
        );
    }

    function renderVisualContent() {
        return (
            <Box sx={{ p: 2 }}>
                <Typography variant="subtitle2" sx={{ mb: 2 }}>
                    {t($ => $.page.widget.form.preview)}
                </Typography>
                <Box sx={{ height: '240px' }}>
                    <WidgetPreview
                        widgetType="TAULA"
                        widgetData={previewData}
                        dashboardPlantilla={dashboardPlantilla}
                    />
                </Box>
                {showOverrideFields && renderTaulaFormFields()}
            </Box>
        );
    }

    // Render form fields for taula widget
    function renderTaulaFormFields() {
        return (
            <Grid container spacing={2}>
                <Grid size={12}>
                    <Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>
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
                    <Typography variant="subtitle2" sx={{ mt: 3, mb: 0 }}>
                        {t($ => $.page.widget.form.configTaula)}
                    </Typography>
                </Grid>
                <Grid size={12}>
                    <FormField
                        name="compacte"
                        label={t($ => $.page.widget.atributsVisuals.compacte)}
                        type="checkbox"
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="colorTextTaula"
                        label={t($ => $.page.widget.atributsVisuals.colorTextTaula)}
                        type="color"
                        required={false}
                    />
                </Grid>
                <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                    <FormField
                        name="colorFonsTaula"
                        label={t($ => $.page.widget.atributsVisuals.colorFonsTaula)}
                        type="color"
                        required={false}
                    />
                </Grid>
                <Grid size={12}>
                    <FormField
                        name="mostrarCapcalera"
                        label={t($ => $.page.widget.atributsVisuals.mostrarCapcalera)}
                        type="checkbox"
                    />
                </Grid>
                {isMostrarCapcalera && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorCapcalera"
                                label={t($ => $.page.widget.atributsVisuals.colorCapcalera)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorFonsCapcalera"
                                label={t($ => $.page.widget.atributsVisuals.colorFonsCapcalera)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                <Grid size={6}>
                    <FormField
                        name="mostrarAlternancia"
                        label={t($ => $.page.widget.atributsVisuals.mostrarAlternancia)}
                        type="checkbox"
                    />
                </Grid>
                {isMostrarAlternancia && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorAlternancia"
                                label={t($ => $.page.widget.atributsVisuals.colorAlternancia)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                <Grid size={12}>
                    <FormField
                        name="mostrarVoraTaula"
                        label={t($ => $.page.widget.atributsVisuals.mostrarVoraTaula)}
                        type="checkbox"
                    />
                </Grid>
                {isMostrarVoraTaula && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorVoraTaula"
                                label={t($ => $.page.widget.atributsVisuals.colorVoraTaula)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="ampleVoraTaula"
                                label={t($ => $.page.widget.atributsVisuals.ampleVoraTaula)}
                                type="number"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                <Grid size={12}>
                    <FormField
                        name="mostrarSeparadorHoritzontal"
                        label={t($ => $.page.widget.atributsVisuals.mostrarSeparadorHoritzontal)}
                        type="checkbox"
                    />
                </Grid>
                {isMostrarSeparadorHoritzontal && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorSeparadorHoritzontal"
                                label={t(
                                    $ => $.page.widget.atributsVisuals.colorSeparadorHoritzontal
                                )}
                                type="color"
                                required={false}
                            />
                        </Grid>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="ampleSeparadorHoritzontal"
                                label={t(
                                    $ => $.page.widget.atributsVisuals.ampleSeparadorHoritzontal
                                )}
                                type="number"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                <Grid size={12}>
                    <FormField
                        name="mostrarSeparadorVertical"
                        label={t($ => $.page.widget.atributsVisuals.mostrarSeparadorVertical)}
                        type="checkbox"
                    />
                </Grid>
                {isMostrarSeparadorVertical && (
                    <>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="colorSeparadorVertical"
                                label={t($ => $.page.widget.atributsVisuals.colorSeparadorVertical)}
                                type="color"
                                required={false}
                            />
                        </Grid>
                        <Grid size={6} sx={{ backgroundColor: 'background.paper' }}>
                            <FormField
                                name="ampleSeparadorVertical"
                                label={t($ => $.page.widget.atributsVisuals.ampleSeparadorVertical)}
                                type="number"
                                required={false}
                            />
                        </Grid>
                    </>
                )}
                {/*<Grid size={12}><FormField name="paginada" label="Taula paginada" type="checkbox" /></Grid>*/}
                <Grid
                    size={12}
                    sx={{
                        display: 'flex',
                        flexDirection: 'row',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                    }}
                >
                    <Typography variant="subtitle2" sx={{ mt: 2, mb: 0 }}>
                        Estils de columnes
                    </Typography>
                    <Button variant="contained" startIcon={<EditIcon />} disabled>
                        Configura estils
                    </Button>
                </Grid>
                <Grid size={12}>
                    <Typography variant="body2" color="text.secondary">
                        Els estils de columnes es configuren a través d'una estructura complexa.
                        Utilitzeu el botó "Afegir estil de columna" per afegir-ne un de nou.
                    </Typography>
                    <Typography variant="body2" color="#b71c1c">
                        (Pendent de desenvolupament)
                    </Typography>
                </Grid>
                <Grid
                    size={12}
                    sx={{
                        display: 'flex',
                        flexDirection: 'row',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                    }}
                >
                    <Typography variant="subtitle2" sx={{ mt: 2, mb: 0 }}>
                        Cel·les destacades
                    </Typography>
                    <Button variant="contained" startIcon={<EditIcon />} disabled>
                        Configura cel·les
                    </Button>
                </Grid>
                <Grid size={12}>
                    <Typography variant="body2" color="text.secondary">
                        Les cel·les destacades es configuren a través d'una estructura complexa.
                        Utilitzeu el botó "Afegir cel·la destacada" per afegir-ne una de nova.
                    </Typography>
                    <Typography variant="body2" color="#b71c1c">
                        (Pendent de desenvolupament)
                    </Typography>
                </Grid>
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
                        name="midaFontTaula"
                        label={t($ => $.page.widget.atributsVisuals.midaFontTaula)}
                        type="number"
                        required={false}
                    />
                </Grid>
            </Grid>
        );
    }
};

export default EstadisticaTaulaWidgetForm;
