import { FormField, useFormContext, useResourceApiService } from 'reactlib';
import Grid from "@mui/material/Grid";
import * as React from "react";
import Divider from "@mui/material/Divider";
import Icon from "@mui/material/Icon";
import Tooltip from "@mui/material/Tooltip";
import ToggleButton from "@mui/material/ToggleButton";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import { useTheme } from "@mui/material/styles";
import { useColumnesDimensioValor } from '../sharedAdvancedSearch/advancedSearchColumns';
import { useTranslation } from "react-i18next";
import FormFieldAdvancedSearchFilters from '../FormFieldAdvancedSearchFilters.tsx';
import { Box } from '@mui/material';
import { findOptions } from '../../util/requestUtils.ts';

/** Camps que sobreescriuen l'estil de la plantilla per a un títol */
export const TITOL_OVERRIDE_FIELDS = [
    'midaFontTitol', 'colorTitol', 'midaFontSubtitol', 'colorSubtitol',
    'colorFons', 'posicioSubtitol', 'separacioSubtitol',
    'mostrarVoraTop', 'colorVoraTop', 'ampleVoraTop',
    'mostrarVoraRight', 'colorVoraRight', 'ampleVoraRight',
    'mostrarVoraBottom', 'colorVoraBottom', 'ampleVoraBottom',
    'mostrarVoraLeft', 'colorVoraLeft', 'ampleVoraLeft',
];

/**
 * Valor de referència d'alguns camps de vora/subtítol quan NO s'han personalitzat: aquestes columnes tenen
 * un valor per defecte a la base de dades (vegeu el canvi de Liquibase que les va introduir), de manera que
 * TOTS els títols persistits ja en porten un valor concret. Sense aquesta referència, hasVisualOverridesTitol
 * els confondria amb una personalització real encara que l'usuari no hagi tocat res.
 */
const TITOL_OVERRIDE_FIELD_DEFAULTS: Record<string, unknown> = {
    posicioSubtitol: 'SOTA',
    separacioSubtitol: 0,
    mostrarVoraTop: false,
    ampleVoraTop: 1,
    mostrarVoraRight: false,
    ampleVoraRight: 1,
    mostrarVoraBottom: true,
    ampleVoraBottom: 1,
    mostrarVoraLeft: false,
    ampleVoraLeft: 1,
};

/**
 * Indica si el títol té algun valor propi que sobreescrigui la plantilla (per mostrar l'indicador de
 * "personalitzat"). `initialData` (opcional, el valor tal com es va carregar en obrir l'edició) permet
 * detectar un canvi real encara que el valor final coincideixi amb el de referència: per exemple, revertir
 * posicioSubtitol a 'SOTA' des d'un valor personalitzat anterior ('COSTAT') és un canvi genuí, encara que
 * 'SOTA' sigui el valor per defecte de la columna i, sense aquesta comparació, passaria desapercebut.
 */
export const hasVisualOverridesTitol = (data: any, initialData?: any): boolean =>
    TITOL_OVERRIDE_FIELDS.some(field => {
        const value = data?.[field];
        if (value === undefined || value === null || value === '') return false;
        if (value !== TITOL_OVERRIDE_FIELD_DEFAULTS[field]) return true;
        return initialData != null && value !== initialData?.[field];
    });

export type VoraCostat = 'Top' | 'Right' | 'Bottom' | 'Left';
const voraCostats: VoraCostat[] = ['Top', 'Right', 'Bottom', 'Left'];

/** Costat de vora independent (Top/Right/Bottom/Left): checkbox "mostrar" i, si és cert, color i gruix. */
export const VoraCostatFields: React.FC<{ costat: VoraCostat; showLabel: string; colorLabel: string; widthLabel: string }> = ({
    costat,
    showLabel,
    colorLabel,
    widthLabel,
}) => {
    const { data } = useFormContext();
    const mostrarField = `mostrarVora${costat}`;
    return (
        <Grid container spacing={1.5}>
            <Grid size={12}>
                <FormField name={mostrarField} label={showLabel} type="checkbox" />
            </Grid>
            {data?.[mostrarField] && (
                <>
                    <Grid size={6}>
                        <FormField name={`colorVora${costat}`} label={colorLabel} type="color" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name={`ampleVora${costat}`} label={widthLabel} type="number" required={false} />
                    </Grid>
                </>
            )}
        </Grid>
    );
};

const useVoraCostatLabels = () => {
    const { t } = useTranslation();
    const labels: Record<VoraCostat, { show: string; color: string; width: string; zone: string }> = {
        Top: {
            show: t($ => $.page.widget.editor.showBorderTop),
            color: t($ => $.page.widget.editor.borderColorTop),
            width: t($ => $.page.widget.editor.borderWidthTop),
            zone: t($ => $.page.widget.editor.voraTop),
        },
        Right: {
            show: t($ => $.page.widget.editor.showBorderRight),
            color: t($ => $.page.widget.editor.borderColorRight),
            width: t($ => $.page.widget.editor.borderWidthRight),
            zone: t($ => $.page.widget.editor.voraRight),
        },
        Bottom: {
            show: t($ => $.page.widget.editor.showBorderBottom),
            color: t($ => $.page.widget.editor.borderColorBottom),
            width: t($ => $.page.widget.editor.borderWidthBottom),
            zone: t($ => $.page.widget.editor.voraBottom),
        },
        Left: {
            show: t($ => $.page.widget.editor.showBorderLeft),
            color: t($ => $.page.widget.editor.borderColorLeft),
            width: t($ => $.page.widget.editor.borderWidthLeft),
            zone: t($ => $.page.widget.editor.voraLeft),
        },
    };
    return labels;
};

/**
 * Editor gràfic de les 4 vores independents d'un títol: un rectangle amb una zona clicable a cada costat
 * que obre una modal amb els camps (mostrar/color/gruix) d'aquell costat concret. Lligat directament als
 * camps del formulari (mostrarVoraTop, colorVoraTop, ampleVoraTop, ...), reutilitzable tant a l'assistent
 * de creació com al panell lateral d'edició del dashboard.
 */
export const VoraGraphicalFormEditor: React.FC = () => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    const theme = useTheme();
    const labels = useVoraCostatLabels();
    const [openCostat, setOpenCostat] = React.useState<VoraCostat | null>(null);

    const borderFor = (costat: VoraCostat) => {
        const mostrar = Boolean(data?.[`mostrarVora${costat}`]);
        const color = data?.[`colorVora${costat}`] || theme.palette.divider;
        const ample = Number(data?.[`ampleVora${costat}`]) || 1;
        return mostrar ? `${ample}px solid ${color}` : `1px dashed ${theme.palette.divider}`;
    };

    const zoneSx = { position: 'absolute' as const, cursor: 'pointer' };

    return (
        <>
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 1.5 }}>
                <Box
                    sx={{
                        width: 180,
                        height: 96,
                        position: 'relative',
                        borderTop: borderFor('Top'),
                        borderRight: borderFor('Right'),
                        borderBottom: borderFor('Bottom'),
                        borderLeft: borderFor('Left'),
                        bgcolor: 'background.paper',
                    }}
                >
                    {voraCostats.map((costat) => (
                        <Tooltip key={costat} title={labels[costat].zone}>
                            <Box
                                data-testid={`vora-zone-${costat}`}
                                onClick={() => setOpenCostat(costat)}
                                sx={{
                                    ...zoneSx,
                                    ...(costat === 'Top' ? { top: 0, left: 14, right: 14, height: 16 } : {}),
                                    ...(costat === 'Right' ? { top: 14, right: 0, bottom: 14, width: 16 } : {}),
                                    ...(costat === 'Bottom' ? { bottom: 0, left: 14, right: 14, height: 16 } : {}),
                                    ...(costat === 'Left' ? { top: 14, left: 0, bottom: 14, width: 16 } : {}),
                                }}
                            />
                        </Tooltip>
                    ))}
                </Box>
            </Box>
            <Dialog open={openCostat != null} onClose={() => setOpenCostat(null)} maxWidth="xs" fullWidth>
                <DialogTitle>{openCostat && labels[openCostat].zone}</DialogTitle>
                <DialogContent>
                    {openCostat && (
                        <VoraCostatFields
                            costat={openCostat}
                            showLabel={labels[openCostat].show}
                            colorLabel={labels[openCostat].color}
                            widthLabel={labels[openCostat].width}
                        />
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenCostat(null)}>{t($ => $.common.cancel)}</Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

/** Icona d'ajuda amb un tooltip explicatiu, per a conceptes que poden no ser obvis per a un usuari sense coneixements tècnics */
export const FieldHelp: React.FC<{ text: string }> = ({ text }) => (
    <Tooltip title={text} arrow>
        <Icon
            fontSize="small"
            sx={{ verticalAlign: 'middle', ml: 0.5, color: 'text.secondary', fontSize: '1rem', cursor: 'help' }}
        >
            info
        </Icon>
    </Tooltip>
);

export type PersonalitzatFieldsProps = {
    personalitzatLabel: string;
    personalitzatHelp: string;
    personalitzatBadge: string;
    /** Indica si el widget té algun valor propi que sobreescrigui la plantilla (calculat pel pare a partir de les dades del widget, no d'aquest formulari). Només llavors es mostra la marca. */
    hasOverrides: boolean;
    destacatLabel?: string;
    plantillaLabel?: string;
    /** Notifica el pare si la secció de personalització està desplegada, perquè pugui mostrar (o no) els camps que sobreescriuen la plantilla al component de visualització. */
    onExpandedChange?: (expanded: boolean) => void;
    /** Etiqueta/tooltip del botó per eliminar el disseny personalitzat. Si no es passa junt amb overrideFields, el botó no es mostra. */
    resetLabel?: string;
    /** Noms dels camps propis del component (Simple/Gràfic/Taula/Títol) que s'han de buidar en eliminar el disseny personalitzat. */
    overrideFields?: string[];
    /**
     * apiRef del formulari on viuen realment els camps d'overrideFields, si és diferent del formulari on
     * es renderitza aquest component (p.ex. als widgets Simple/Gràfic/Taula, on PersonalitzatFields viu al
     * formulari de dashboardItem però els camps visuals viuen al formulari específic del widget). Si no es
     * passa, s'utilitza l'apiRef del propi context de formulari.
     */
    resetApiRef?: React.RefObject<any>;
};

/**
 * Camps de la passa "Visualització": plantilla, destacat i, al final, l'interruptor de personalització.
 * Desplegar/plegar la secció de personalització només mostra o amaga els camps que sobreescriuen la
 * plantilla, no els esborra: un cop se n'ha definit algun, es manté aplicat encara que es plegui la secció.
 * La marca de "personalitzat" només es mostra si `hasOverrides` és cert (és a dir, si l'usuari ha
 * emplenat algun valor concret), no simplement perquè la secció s'hagi desplegat alguna vegada.
 */
export const PersonalitzatFields: React.FC<PersonalitzatFieldsProps> = ({
    personalitzatLabel,
    personalitzatHelp,
    personalitzatBadge,
    hasOverrides,
    destacatLabel,
    plantillaLabel,
    onExpandedChange,
    resetLabel,
    overrideFields,
    resetApiRef,
}) => {
    const { apiRef: localApiRef } = useFormContext();
    const [expanded, setExpanded] = React.useState(false);
    const initializedRef = React.useRef(false);

    const handleReset = (event: React.MouseEvent) => {
        event.stopPropagation();
        const targetApiRef = resetApiRef ?? localApiRef;
        overrideFields?.forEach(field => targetApiRef?.current?.setFieldValue?.(field, undefined));
    };

    React.useEffect(() => {
        if (!initializedRef.current && hasOverrides) {
            setExpanded(true);
            initializedRef.current = true;
        }
    }, [hasOverrides]);

    React.useEffect(() => {
        onExpandedChange?.(expanded);
    }, [expanded]);

    return (
        <Grid container spacing={1.5}>
            <Grid size={12}>
                <FormField name="plantilla" label={plantillaLabel} />
            </Grid>
            <Grid size={12}>
                <FormField name="destacat" label={destacatLabel} type="checkbox" />
            </Grid>
            <Grid size={12} sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <ToggleButton
                    value="personalitzat"
                    selected={expanded}
                    onChange={() => setExpanded(prevExpanded => !prevExpanded)}
                    size="small"
                    sx={{ flex: 1, justifyContent: 'flex-start', gap: 1 }}
                >
                    <Icon sx={{ fontSize: '1rem' }}>tune</Icon>
                    {personalitzatLabel}
                    {hasOverrides && (
                        <Tooltip title={personalitzatBadge} arrow>
                            <Box
                                data-testid="personalitzat-badge"
                                sx={{
                                    width: 8,
                                    height: 8,
                                    borderRadius: '50%',
                                    backgroundColor: 'warning.main',
                                    ml: 1,
                                }}
                            />
                        </Tooltip>
                    )}
                </ToggleButton>
                {hasOverrides && overrideFields && overrideFields.length > 0 && (
                    <Tooltip title={resetLabel} arrow>
                        <IconButton size="small" aria-label={resetLabel} onClick={handleReset}>
                            <Icon sx={{ fontSize: '1.1rem' }}>restore</Icon>
                        </IconButton>
                    </Tooltip>
                )}
                <FieldHelp text={personalitzatHelp} />
            </Grid>
        </Grid>
    );
};

/** Camps d'aplicació, títol i descripció del widget (pas "Tipus i aplicació" de l'assistent) */
export const AppTitleFields: React.FC = () => {
    const { isReady: appIsReady, find: appFind } = useResourceApiService("app");

    if (!appIsReady)
        return null;

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField
                    name="aplicacio"
                    optionsRequest={(quickFilter: string) =>
                        findOptions(appFind, 'nom', quickFilter, 'activa:true')
                    }
                />
            </Grid>
            <Grid size={12}>
                <FormField name="titol" />
            </Grid>
            <Grid size={12}>
                <FormField name="descripcio" type="textarea" />
            </Grid>
        </Grid>
    );
};

/** Camps de títol i descripció del widget, sense el selector d'aplicació (l'assistent gestiona l'aplicació/entorn per separat) */
export const TitleDescriptionFields: React.FC = () => (
    <Grid container spacing={2}>
        <Grid size={12}>
            <FormField name="titol" />
        </Grid>
        <Grid size={12}>
            <FormField name="descripcio" type="textarea" />
        </Grid>
    </Grid>
);

/** Camp de dimensions (filtratge de la informació), pas "Dimensions" de l'assistent */
export const DimensionsFields: React.FC = () => {
    const { data } = useFormContext();
    const columnesDimensioValor = useColumnesDimensioValor();
    if (data?.aplicacio == null)
        return null;
    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormFieldAdvancedSearchFilters
                    name="dimensionsValor"
                    advancedSearchFilterResourceName="dimensio"
                    advancedSearchFilterCode="filterByDimensio"
                    advancedSearchFilterContent={
                        <Box sx={{ my: 1 }}>
                            <FormField
                                componentProps={{
                                    size: 'small',
                                }}
                                name="dimensio"
                                namedQueries={[`filterByAppGroupByNom:${data.aplicacio.id}`]}
                            />
                        </Box>
                    }
                    advancedSearchFilterBuilder={data =>
                        data.dimensio ? `dimensio.nom : '${data.dimensio.description}'` : undefined
                    }
                    advancedSearchDataGridProps={{
                        rowHeight: 30,
                    }}
                    advancedSearchDialogHeight={500}
                    multiple
                    advancedSearchColumns={columnesDimensioValor}
                    namedQueries={[
                        `filterByAppGroupByValor:${data.aplicacio.id}`,
                    ]}
                />
            </Grid>
        </Grid>
    );
};

/** Camps de període de temps, pas "Període" de l'assistent */
export const PeriodFields: React.FC = () => {
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

    if (data?.aplicacio == null)
        return null;

    return (
        <Grid container spacing={2}>
            {/* Periodo base, depenent des tipus apareixeran o no els camps de abaix */}
            <Grid size={12}>
                <FormField name="periodeMode" />
            </Grid>

            {/* Camps PRESET */}
            {periodeMode === 'PRESET' && (
                <Grid size={12}>
                    <FormField name="presetPeriode" />
                </Grid>
            )}
            {isPresetCountVisible && (
                <Grid size={12}>
                    <FormField name="presetCount" />
                </Grid>
            )}

            {/* Camps RELATIU */}
            {isRelatiuVisible && (
                <>
                    <Grid size={12}>
                        <FormField name="relatiuPuntReferencia" />
                        <FieldHelp text={t($ => $.page.widget.form.help.relatiuPuntReferencia)} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="relatiuCount" />
                        <FieldHelp text={t($ => $.page.widget.form.help.relatiuCount)} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="relatiueUnitat" />
                        <FieldHelp text={t($ => $.page.widget.form.help.relatiueUnitat)} />
                    </Grid>
                    <Grid size={12}>
                        <FormField name="relatiuAlineacio" />
                        <FieldHelp text={t($ => $.page.widget.form.help.relatiuAlineacio)} />
                    </Grid>
                </>
            )}

            {/* Camps ABSOLUT */}
            {isAbsolutVisible && (
                <Grid size={12}>
                    <FormField name="absolutTipus" />
                    <FieldHelp text={t($ => $.page.widget.form.help.absolutTipus)} />
                </Grid>
            )}

            {/* Camps DATE_RANGE fields */}
            {isDateRangeVisible && (
                <>
                    <Grid size={6}>
                        <FormField name="absolutDataInici" />
                        <FieldHelp text={t($ => $.page.widget.form.help.absolutDataInici)} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="absolutDataFi" />
                        <FieldHelp text={t($ => $.page.widget.form.help.absolutDataFi)} />
                    </Grid>
                </>
            )}

            {/* Camps SPECIFIC_PERIOD_OF_YEAR fields */}
            {isSpecificPeriodVisible && (
                <>
                    <Grid size={8}>
                        <FormField name="absolutAnyReferencia" />
                        <FieldHelp text={t($ => $.page.widget.form.help.absolutAnyReferencia)} />
                    </Grid>
                    <Grid size={4}>
                        <FormField
                            name="absolutAnyValor"
                            label={t($ => $.page.widget.form.any)}
                            disabled={!isAbsolutAnyValorVisible}
                        />
                    </Grid>
                    <Grid size={12}>
                        <FormField name="absolutPeriodeUnitat" />
                        <FieldHelp text={t($ => $.page.widget.form.help.absolutPeriodeUnitat)} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="absolutPeriodeInici" />
                        <FieldHelp text={t($ => $.page.widget.form.help.absolutPeriodeInici)} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="absolutPeriodeFi" />
                        <FieldHelp text={t($ => $.page.widget.form.help.absolutPeriodeFi)} />
                    </Grid>
                </>
            )}
        </Grid>
    );
};

const EstadisticaWidgetFormFields: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    const { isReady: appIsReady } = useResourceApiService("app");

    if (!appIsReady)
        return;

    return (
        <>
            <Grid container spacing={2}>
                <Grid size={12}>
                    <AppTitleFields />
                </Grid>
                {data?.aplicacio != null &&
                    <>
                        <Grid size={12}>
                            <DimensionsFields />
                        </Grid>

                        <Grid size={12}>
                            <Divider sx={{ my: 1 }}>{t($ => $.page.widget.form.periode)}</Divider>
                        </Grid>

                        <Grid size={12}>
                            <PeriodFields />
                        </Grid>

                        {children}
                    </>
                }
            </Grid>
        </>
    );
}

export default EstadisticaWidgetFormFields;
