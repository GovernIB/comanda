import * as React from 'react';
import {
    Autocomplete,
    Box,
    Button,
    Chip,
    CircularProgress,
    Divider,
    FormControl,
    Icon,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Stack,
    Tab,
    Tabs,
    TextField,
    Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import {
    FormField,
    springFilterBuilder,
    useBaseAppContext,
    useConfirmDialogButtons,
    useFormContext,
    useResourceApiService,
} from 'reactlib';
import { findOptions } from '../../util/requestUtils.ts';
import MuiForm from '../../../lib/components/mui/form/MuiForm.tsx';
import { FormFieldDataActionType } from '../../../lib/components/form/FormContext.tsx';
import type { FormApi } from '../../../lib/components/form/FormContext.tsx';
import EstadisticaSimpleWidgetForm, { hasVisualOverrides as simpleHasVisualOverrides, SIMPLE_OVERRIDE_FIELDS } from './EstadisticaSimpleWidgetForm.tsx';
import EstadisticaGraficWidgetForm, { hasVisualOverrides as graficHasVisualOverrides, GRAFIC_OVERRIDE_FIELDS } from './EstadisticaGraficWidgetForm.tsx';
import EstadisticaTaulaWidgetForm, { hasVisualOverrides as taulaHasVisualOverrides, TAULA_OVERRIDE_FIELDS } from './EstadisticaTaulaWidgetForm.tsx';
import { DimensionsFields, PeriodFields, PersonalitzatFields, VoraGraphicalFormEditor, TITOL_OVERRIDE_FIELDS, hasVisualOverridesTitol } from './EstadisticaWidgetFormFields.tsx';
import { useDashboardPlantilla, useEntornCodi } from './dashboardPlantillaHook.ts';
import { WidgetPreview } from './WidgetPreview.tsx';
import { useTranslation } from 'react-i18next';
import type { DashboardFiltre } from '../../types/dashboardFiltre.model.ts';

export type DashboardWidgetType = 'SIMPLE' | 'GRAFIC' | 'TAULA';

export type DashboardEditorSelection =
    | { kind: 'none' }
    | {
          kind: 'widget';
          mode: 'create';
          widgetType?: DashboardWidgetType;
          entornId?: any;
          aplicacio?: any;
      }
    | {
          kind: 'widget';
          mode: 'edit';
          widgetType: DashboardWidgetType;
          dashboardItemId: any;
          widgetId: any;
      }
    | {
          kind: 'title';
          mode: 'create';
      }
    | {
          kind: 'title';
          mode: 'edit';
          dashboardTitolId: any;
      }
    | {
          kind: 'filtre';
          mode: 'create';
          /** Precalculat pel pare a partir dels filtres existents, perquè el nou en quedi al final. */
          nextOrdre?: number;
      }
    | {
          kind: 'filtre';
          mode: 'edit';
          dashboardFiltreId: any;
      }
    | {
          /**
           * Diversos elements seleccionats alhora (marc de selecció amb el ratolí, vegeu
           * DashboardReactGridLayout). No es mostren propietats editables per a una selecció múltiple.
           */
          kind: 'multi';
          ids: string[];
      };

type DashboardEditorSidePanelProps = {
    dashboard: any;
    dashboardId: string;
    selection: DashboardEditorSelection;
    onSelectionChange: (selection: DashboardEditorSelection) => void;
    /** Si es passa un dashboardItemId, el pare pot refrescar només aquest widget en lloc de tot el dashboard */
    onSaved: (dashboardItemId?: any) => void;
    onDeleted: () => void;
    /** Filtres ja configurats al dashboard, usats per l'editor de filtres per evitar-ne duplicats (vegeu FiltreEditor) */
    dashboardFiltres?: DashboardFiltre[];
    /**
     * Notifica el pare dels canvis en viu (encara no desats) d'un títol existent que s'està editant, perquè
     * pugui reflectir-los al canvas. El pare ha de descartar aquesta previsualització (revertint el canvas
     * a l'últim estat desat) quan la selecció canviï sense haver-se desat.
     */
    onLiveTitleDataChange?: (dashboardTitolId: any, data: any) => void;
};

const widgetTypeConfig: Record<
    DashboardWidgetType,
    {
        resourceName: string;
        FormComponent: React.FC<{ mode?: 'full' | 'stats' | 'indicators' | 'visual', dashboardPlantilla?: any, destacat?: boolean, showOverrideFields?: boolean}>;
        hasVisualOverrides: (data: any) => boolean;
        overrideFields: string[];
    }
> = {
    SIMPLE: {
        resourceName: 'estadisticaSimpleWidget',
        FormComponent: EstadisticaSimpleWidgetForm,
        hasVisualOverrides: simpleHasVisualOverrides,
        overrideFields: SIMPLE_OVERRIDE_FIELDS,
    },
    GRAFIC: {
        resourceName: 'estadisticaGraficWidget',
        FormComponent: EstadisticaGraficWidgetForm,
        hasVisualOverrides: graficHasVisualOverrides,
        overrideFields: GRAFIC_OVERRIDE_FIELDS,
    },
    TAULA: {
        resourceName: 'estadisticaTaulaWidget',
        FormComponent: EstadisticaTaulaWidgetForm,
        hasVisualOverrides: taulaHasVisualOverrides,
        overrideFields: TAULA_OVERRIDE_FIELDS,
    },
};

const defaultDashboardItemData = {
    posX: 0,
    width: 3,
    height: 3,
    destacat: false,
};

const titleDefaultData = {
    posX: 0,
    width: 6,
    height: 1,
    tipusTitol: 'TIPUS_1',
    destacat: false,
};

const PanelSection = ({ title, children }: React.PropsWithChildren<{ title: string }>) => (
    <Box>
        <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 700 }}>
            {title}
        </Typography>
        {children}
    </Box>
);

// TODO Refactoritzar, es fa un us no recomanat de useEffect. Considerar moure el useState a un nivell superior o usar portals
/** Pont invisible que llegeix les dades del formulari del widget (colors, mides, etc.) i notifica el pare */
const WidgetDataBridge: React.FC<{ onChange: (data: any) => void }> = ({ onChange }) => {
    const { data } = useFormContext();
    React.useEffect(() => {
        onChange(data);
    }, [data]);
    return null;
};

// TODO Refactoritzar, es fa un us no recomanat de useEffect. Considerar moure el useState a un nivell superior o usar portals
/** Pont invisible que llegeix destacat del context del formulari dashboardItem i notifica el pare */
const DestacatBridge: React.FC<{ onChange: (v: boolean) => void }> = ({ onChange }) => {
    const { data } = useFormContext();
    React.useEffect(() => {
        onChange(!!data?.destacat);
    }, [data?.destacat, onChange]);
    return null;
};

// TODO Refactoritzar, es fa un us no recomanat de useEffect. Considerar moure el useState a un nivell superior o usar portals
/** Pont invisible que llegeix la plantilla del context del formulari dashboardItem i notifica el pare */
const PlantillaBridge: React.FC<{ onChange: (v: any) => void }> = ({ onChange }) => {
    const { data } = useFormContext();
    React.useEffect(() => {
        onChange(data?.plantilla);
    }, [data?.plantilla, onChange]);
    return null;
};

// TODO Refactoritzar, es fa un us no recomanat de useEffect. Considerar moure el useState a un nivell superior o usar portals
/** Pont invisible que llegeix l'entorn del context del formulari dashboardItem i notifica el pare */
const EntornIdBridge: React.FC<{ onChange: (v: any) => void }> = ({ onChange }) => {
    const { data } = useFormContext();
    React.useEffect(() => {
        onChange(data?.entornId);
    }, [data?.entornId, onChange]);
    return null;
};

/** Capçalera comuna (per sobre de les pestanyes): aplicació i entorn només informatius, títol editable */
const WidgetEditorHeader = ({ aplicacio, entornCodi }: { aplicacio: any; entornCodi?: string }) => {
    const { t } = useTranslation();
    return (
        <Box sx={{ mb: 1 }}>
            {(aplicacio || entornCodi) && (
                <Stack direction="row" spacing={1} sx={{ mb: 1.5, flexWrap: 'wrap' }}>
                    {aplicacio && (
                        <Chip size="small" icon={<Icon>apps</Icon>} label={aplicacio.description ?? aplicacio.nom} />
                    )}
                    {entornCodi && <Chip size="small" icon={<Icon>dns</Icon>} label={entornCodi} />}
                </Stack>
            )}
            <Grid container spacing={1.5}>
                <Grid size={12}>
                    <FormField name="titol" label={t($ => $.page.widget.editor.titleText)} />
                </Grid>
            </Grid>
        </Box>
    );
};

const PositionSizeFields = () => {
    const { t } = useTranslation();
    return (
        <Grid container spacing={1.5}>
            <Grid size={6}>
                <FormField name="posX" label={t($ => $.page.widget.editor.posX)} type="number" />
            </Grid>
            <Grid size={6}>
                <FormField name="posY" label={t($ => $.page.widget.editor.posY)} type="number" required={false} />
            </Grid>
            <Grid size={6}>
                <FormField name="width" label={t($ => $.page.widget.editor.width)} type="number" />
            </Grid>
            <Grid size={6}>
                <FormField name="height" label={t($ => $.page.widget.editor.height)} type="number" />
            </Grid>
        </Grid>
    );
};

const VisualizationTabFields = ({
    hasOverrides,
    onExpandedChange,
    overrideFields,
    resetApiRef,
}: {
    hasOverrides: boolean;
    onExpandedChange: (expanded: boolean) => void;
    overrideFields?: string[];
    resetApiRef?: React.RefObject<any>;
}) => {
    const { t } = useTranslation();
    return (
        <PersonalitzatFields
            personalitzatLabel={t($ => $.page.widget.wizard.visual.personalitzat)}
            personalitzatHelp={t($ => $.page.widget.wizard.visual.personalitzatHelp)}
            personalitzatBadge={t($ => $.page.widget.wizard.visual.personalitzatBadge)}
            resetLabel={t($ => $.page.widget.wizard.visual.resetLabel)}
            overrideFields={overrideFields}
            resetApiRef={resetApiRef}
            hasOverrides={hasOverrides}
            onExpandedChange={onExpandedChange}
        />
    );
};

/**
 * A partir del nom d'un camp amb error de validació, determina a quina pestanya pertany, per poder-hi
 * navegar i que l'usuari vegi l'error en context en lloc de quedar bloquejat sense saber què corregir.
 * Els camps d'indicadors/filtres/període i els de capçalera (títol, descripció, aplicació) queden
 * tots dins la pestanya "Propietats" (índex 0).
 */
const tabIndexForField = (field: string | undefined): number => {
    if (field === 'destacat' || field === 'plantilla' || field === 'personalitzat') return 1;
    if (field === 'posX' || field === 'posY' || field === 'width' || field === 'height') return 2;
    return 0;
};

type DashboardTitleFieldsProps = {
    dashboardPlantilla: any;
    hasOverrides: boolean;
    expanded: boolean;
    onExpandedChange: (expanded: boolean) => void;
};


const DashboardTitleFields = ({ dashboardPlantilla, hasOverrides, expanded, onExpandedChange }: DashboardTitleFieldsProps) => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    return (
        <Stack spacing={2}>
            <PanelSection title={t($ => $.page.widget.editor.configData)}>
                <Grid container spacing={1.5}>
                    <Grid size={12}>
                        <FormField name="titol" label={t($ => $.page.widget.editor.titleText)} />
                    </Grid>
                    <Grid size={12}>
                        <FormField name="subtitol" label={t($ => $.page.widget.editor.subtitleText)} />
                    </Grid>
                </Grid>
            </PanelSection>
            <PanelSection title={t($ => $.page.widget.editor.configVisual)}>
                <Box sx={{ p: 2 }}>
                    <Typography variant="subtitle2" sx={{ mb: 2 }}>
                        {t($ => $.page.widget.form.preview)}
                    </Typography>
                    <Box sx={{ height: '120px' }}>
                        <WidgetPreview
                            widgetType="TITOL"
                            widgetData={data}
                            dashboardPlantilla={dashboardPlantilla}
                        />
                    </Box>
                </Box>
                <Grid container spacing={1.5}>
                    <Grid size={12}>
                        <FormField name="tipusTitol" label={t($ => $.page.widget.editor.titleType)} />
                    </Grid>
                    <Grid size={12}>
                        <PersonalitzatFields
                            personalitzatLabel={t($ => $.page.widget.wizard.visual.personalitzat)}
                            personalitzatHelp={t($ => $.page.widget.wizard.visual.personalitzatHelp)}
                            personalitzatBadge={t($ => $.page.widget.wizard.visual.personalitzatBadge)}
                            destacatLabel={t($ => $.page.widget.editor.showDestacat)}
                            plantillaLabel={t($ => $.page.widget.editor.template)}
                            resetLabel={t($ => $.page.widget.wizard.visual.resetLabel)}
                            overrideFields={TITOL_OVERRIDE_FIELDS}
                            hasOverrides={hasOverrides}
                            onExpandedChange={onExpandedChange}
                        />
                    </Grid>
                    {expanded && (
                        <>
                            <Grid size={6}>
                                <FormField name="midaFontTitol" label={t($ => $.page.widget.editor.textSize)} type="number" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="colorTitol" label={t($ => $.page.widget.editor.textColor)} type="color" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="midaFontSubtitol" label={t($ => $.page.widget.editor.subtitleSize)} type="number" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="colorSubtitol" label={t($ => $.page.widget.editor.subtitleColor)} type="color" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="colorFons" label={t($ => $.page.widget.editor.backgroundColor)} type="color" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="posicioSubtitol" label={t($ => $.page.widget.editor.subtitlePosition)} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="separacioSubtitol" label={t($ => $.page.widget.editor.subtitleSpacing)} type="number" required={false} />
                            </Grid>
                            <Grid size={12}>
                                <Divider sx={{ my: 1 }} />
                            </Grid>
                            <Grid size={12}>
                                <VoraGraphicalFormEditor />
                            </Grid>
                        </>
                    )}
                    <Grid size={12}>
                        <Divider sx={{ my: 1 }} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="posX" label={t($ => $.page.widget.editor.posX)} type="number" />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="posY" label={t($ => $.page.widget.editor.posY)} type="number" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="width" label={t($ => $.page.widget.editor.width)} type="number" />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="height" label={t($ => $.page.widget.editor.height)} type="number" />
                    </Grid>
                </Grid>
            </PanelSection>
        </Stack>
    );
};

/**
 * Camps de configuració general del dashboard (aplicació, entorn i colors de fons per tema clar/fosc),
 * mostrats al panell de propietats quan no hi ha cap element del canvas seleccionat.
 */
const DashboardSettingsFields = () => {
    const { t } = useTranslation();
    const { data } = useFormContext();
    const { isReady: appIsReady, find: appFind } = useResourceApiService('app');
    const { isReady: entornIsReady, find: entornFind } = useResourceApiService('entorn');
    const filterAplicacio = springFilterBuilder.and(
        springFilterBuilder.eq('activa', true),
        springFilterBuilder.exists(springFilterBuilder.and(springFilterBuilder.eq('entornApps.entorn.id', data?.entorn?.id)))
    );
    const filterEntorn = springFilterBuilder.exists(
        springFilterBuilder.and(springFilterBuilder.eq('entornAppEntities.app.id', data?.aplicacio?.id))
    );

    if (!appIsReady || !entornIsReady) {
        return null;
    }

    return (
        <Grid container spacing={2}>
            <Grid size={12}>
                <FormField
                    name="aplicacio"
                    // Igual que al menú lateral: un cop configurada al dashboard, l'aplicació ja no es pot canviar des d'aquí.
                    disabled={data?.aplicacio}
                    optionsRequest={(quickFilter: string) => findOptions(appFind, 'nom', quickFilter, filterAplicacio)}
                />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="entorn"
                    disabled={data?.entorn}
                    optionsRequest={(quickFilter: string) => findOptions(entornFind, 'nom', quickFilter, filterEntorn)}
                />
            </Grid>
            <Grid size={6}>
                <FormField name="colorFonsClar" type="color" required={false} label={t($ => $.page.widget.editor.colorFonsClar)} />
            </Grid>
            <Grid size={6}>
                <FormField name="colorFonsFosc" type="color" required={false} label={t($ => $.page.widget.editor.colorFonsFosc)} />
            </Grid>
        </Grid>
    );
};

type DashboardSettingsEditorProps = {
    dashboardId: string;
    dashboardSettingsFormApiRef: React.RefObject<FormApi | any>;
};

const DashboardSettingsEditor: React.FC<DashboardSettingsEditorProps> = ({ dashboardId, dashboardSettingsFormApiRef }) => (
    <MuiForm
        resourceName="dashboard"
        id={dashboardId}
        apiRef={dashboardSettingsFormApiRef}
        hiddenToolbar
        formBlockerDisabled
        componentProps={{ sx: { m: 0, mt: 0 } }}
    >
        <DashboardSettingsFields />
    </MuiForm>
);

const WidgetTypeSelector = ({
    value,
    onChange,
}: {
    value?: DashboardWidgetType;
    onChange: (value: DashboardWidgetType) => void;
}) => {
    const { t } = useTranslation();
    return (
        <FormControl fullWidth size="small">
            <InputLabel id="dashboard-widget-type-label">{t($ => $.page.widget.editor.widgetType)}</InputLabel>
            <Select
                labelId="dashboard-widget-type-label"
                value={value ?? ''}
                label={t($ => $.page.widget.editor.widgetType)}
                onChange={event => onChange(event.target.value as DashboardWidgetType)}
            >
                <MenuItem value="SIMPLE">{t($ => $.page.widget.wizard.types.simple.label)}</MenuItem>
                <MenuItem value="GRAFIC">{t($ => $.page.widget.wizard.types.grafic.label)}</MenuItem>
                <MenuItem value="TAULA">{t($ => $.page.widget.wizard.types.taula.label)}</MenuItem>
            </Select>
        </FormControl>
    );
};

export const DashboardEditorSidePanel: React.FC<DashboardEditorSidePanelProps> = ({
    dashboard,
    dashboardId,
    selection,
    onSelectionChange,
    onSaved,
    onDeleted,
    dashboardFiltres,
    onLiveTitleDataChange,
}) => {
    const widgetFormApiRef = React.useRef<FormApi | any>({});
    const dashboardItemFormApiRef = React.useRef<FormApi | any>({});
    const titleFormApiRef = React.useRef<FormApi | any>({});
    const filtreFormApiRef = React.useRef<FormApi | any>({});
    const dashboardSettingsFormApiRef = React.useRef<FormApi | any>({});
    // Mateix càlcul que mostra l'indicador de "personalitzat" a TitleEditor (vegeu hasVisualOverridesTitol),
    // reutilitzat en desar per evitar duplicar-hi la lògica de comparació amb l'estat inicial carregat.
    const titleHasOverridesRef = React.useRef(false);
    const { temporalMessageShow, messageDialogShow } = useBaseAppContext();
    const { t } = useTranslation();
    const confirmDialogButtons = useConfirmDialogButtons();
    const { create: createDashboardItem, patch: patchDashboardItem, delete: deleteDashboardItem } =
        useResourceApiService('dashboardItem');
    const { patch: patchDashboardTitol, delete: deleteDashboardTitol } = useResourceApiService('dashboardTitol');
    const { delete: deleteDashboardFiltre } = useResourceApiService('dashboardFiltre');
    const [saving, setSaving] = React.useState(false);
    const [activeTab, setActiveTab] = React.useState(0);

    const selectionKey = React.useMemo(() => {
        if (selection.kind === 'widget') {
            return `widget-${selection.mode}-${selection.widgetType ?? 'none'}-${selection.mode === 'edit' ? selection.dashboardItemId : 'new'}`;
        }
        if (selection.kind === 'title') {
            return `title-${selection.mode}-${selection.mode === 'edit' ? selection.dashboardTitolId : 'new'}`;
        }
        if (selection.kind === 'filtre') {
            return `filtre-${selection.mode}-${selection.mode === 'edit' ? selection.dashboardFiltreId : 'new'}`;
        }
        if (selection.kind === 'multi') {
            return `multi-${selection.ids.join(',')}`;
        }
        return 'none';
    }, [selection]);

    React.useEffect(() => {
        setActiveTab(0);
    }, [selectionKey]);

    const handleSave = async () => {
        if (selection.kind === 'multi') return; // sense propietats editables per a una selecció múltiple
        setSaving(true);
        try {
            if (selection.kind === 'none') {
                await dashboardSettingsFormApiRef.current?.save();
            } else if (selection.kind === 'widget') {
                if (!selection.widgetType) {
                    temporalMessageShow(null, t($ => $.page.widget.editor.selectWidgetType), 'warning');
                    return;
                }
                const savedWidget = await widgetFormApiRef.current?.save();
                const widgetData = widgetFormApiRef.current?.getData() ?? {};
                const config = selection.widgetType ? widgetTypeConfig[selection.widgetType] : undefined;
                const hasOverrides = config ? config.hasVisualOverrides(widgetData) : false;
                if (selection.mode === 'create') {
                    const dashboardItemData = dashboardItemFormApiRef.current?.getData() ?? {};
                    await createDashboardItem({
                        data: {
                            ...dashboardItemData,
                            // El backend només aplica els camps propis del widget per sobre de la
                            // plantilla quan `personalitzat` és cert (vegeu resolveAtributsVisuals).
                            personalitzat: hasOverrides,
                            dashboard: { id: dashboardId },
                            widget: { id: savedWidget?.id },
                            entornId: selection.entornId ?? dashboard?.entorn?.id,
                        },
                    });
                } else {
                    await dashboardItemFormApiRef.current?.save();
                    // Fet en una petició separada (i no via setFieldValue+save) perquè el formulari
                    // de dashboardItem ja ha capturat el seu propi tancament de `data` a `save()`;
                    // afegir-hi aquest camp derivat just abans no es reflectiria a temps (race amb el
                    // useEffect que actualitza apiRef.current).
                    await patchDashboardItem(selection.dashboardItemId, {
                        data: { personalitzat: hasOverrides },
                    });
                }
            } else if (selection.kind === 'title') {
                await titleFormApiRef.current?.save();
                if (selection.mode === 'edit') {
                    // Fet en una petició separada, pel mateix motiu que a l'edició de widgets (vegeu
                    // comentari més amunt): evita la carrera amb el useEffect que actualitza apiRef.current.
                    // S'usa el mateix valor calculat que mostra l'indicador de "personalitzat" (vegeu
                    // TitleEditor), en lloc de recalcular-lo aquí sense l'estat inicial carregat.
                    await patchDashboardTitol(selection.dashboardTitolId, {
                        data: { personalitzat: titleHasOverridesRef.current },
                    });
                }
            } else if (selection.kind === 'filtre') {
                await filtreFormApiRef.current?.save();
            }
            onSaved(
                selection.kind === 'widget' && selection.mode === 'edit'
                    ? selection.dashboardItemId
                    : undefined
            );
        } catch (error: any) {
            const validationFieldErrors = error?.errors ?? error?.validationErrors;
            if (Array.isArray(validationFieldErrors) && validationFieldErrors.length > 0) {
                if (selection.kind === 'widget') {
                    setActiveTab(tabIndexForField(validationFieldErrors[0]?.field));
                }
                temporalMessageShow(null, t($ => $.page.widget.wizard.actions.validationError), 'error');
            } else if (error?.message) {
                temporalMessageShow(null, error.message, 'error');
            }
        } finally {
            setSaving(false);
        }
    };

    const confirmDelete = (action: () => Promise<void>) => {
        messageDialogShow(
            t($ => $.page.widget.editor.confirmTitle),
            t($ => $.page.widget.editor.confirmDelete),
            confirmDialogButtons,
            { maxWidth: 'sm', fullWidth: true }
        ).then((value: any) => {
            if (!value) {
                return;
            }
            action()
                .then(() => {
                    temporalMessageShow(null, t($ => $.page.widget.editor.deleted), 'success');
                    onDeleted();
                })
                .catch((error: any) => {
                    temporalMessageShow(null, error?.message ?? t($ => $.page.widget.editor.deleteError), 'error');
                });
        });
    };

    const handleDelete = () => {
        if (selection.kind === 'multi') return; // sense acció d'esborrat per a una selecció múltiple
        if (selection.kind === 'widget' && selection.mode === 'edit') {
            confirmDelete(() => deleteDashboardItem(selection.dashboardItemId));
        } else if (selection.kind === 'title' && selection.mode === 'edit') {
            confirmDelete(() => deleteDashboardTitol(selection.dashboardTitolId));
        } else if (selection.kind === 'filtre' && selection.mode === 'edit') {
            confirmDelete(() => deleteDashboardFiltre(selection.dashboardFiltreId));
        } else {
            onDeleted();
        }
    };

    return (
        <Paper
            elevation={1}
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                overflow: 'hidden',
                borderRadius: 0,
            }}
        >
            <Box sx={{ px: 2, py: 1.5 }}>
                <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                    {t($ => $.page.widget.editor.properties)}
                </Typography>
            </Box>
            <Divider />
            <Box sx={{ flex: 1, minHeight: 0, overflow: 'auto', p: 2 }}>
                {selection.kind === 'none' ? (
                    <DashboardSettingsEditor
                        key={dashboardId}
                        dashboardId={dashboardId}
                        dashboardSettingsFormApiRef={dashboardSettingsFormApiRef}
                    />
                ) : selection.kind === 'widget' ? (
                    <WidgetEditor
                        key={selectionKey}
                        dashboard={dashboard}
                        dashboardId={dashboardId}
                        selection={selection}
                        widgetFormApiRef={widgetFormApiRef}
                        dashboardItemFormApiRef={dashboardItemFormApiRef}
                        onSelectionChange={onSelectionChange}
                        activeTab={activeTab}
                        onActiveTabChange={setActiveTab}
                    />
                ) : selection.kind === 'title' ? (
                    <TitleEditor
                        key={selectionKey}
                        dashboard={dashboard}
                        dashboardId={dashboardId}
                        selection={selection}
                        titleFormApiRef={titleFormApiRef}
                        onLiveDataChange={onLiveTitleDataChange}
                        onHasOverridesChange={(value) => { titleHasOverridesRef.current = value; }}
                    />
                ) : selection.kind === 'filtre' ? (
                    <FiltreEditor
                        key={selectionKey}
                        dashboard={dashboard}
                        dashboardId={dashboardId}
                        selection={selection}
                        filtreFormApiRef={filtreFormApiRef}
                        dashboardFiltres={dashboardFiltres}
                    />
                ) : (
                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: 'text.secondary' }}>
                        <Typography variant="body2">
                            {t($ => $.page.dashboards.editor.multiSelection.message, { count: selection.ids.length })}
                        </Typography>
                    </Box>
                )}
            </Box>
            <Divider />
            <Box sx={{ p: 1.5, display: 'flex', gap: 1, justifyContent: 'space-between' }}>
                <Button
                    variant="outlined"
                    color="error"
                    startIcon={<Icon>delete</Icon>}
                    onClick={handleDelete}
                    disabled={selection.kind === 'none' || selection.kind === 'multi' || saving}
                >
                    {t($ => $.common.delete)}
                </Button>
                <Button
                    variant="contained"
                    startIcon={<Icon>save</Icon>}
                    onClick={handleSave}
                    disabled={selection.kind === 'multi' || saving}
                >
                    {t($ => $.common.save)}
                </Button>
            </Box>
        </Paper>
    );
};

type WidgetEditorProps = {
    dashboard: any;
    dashboardId: string;
    selection: Extract<DashboardEditorSelection, { kind: 'widget' }>;
    widgetFormApiRef: React.RefObject<FormApi | any>;
    dashboardItemFormApiRef: React.RefObject<FormApi | any>;
    onSelectionChange: (selection: DashboardEditorSelection) => void;
    activeTab: number;
    onActiveTabChange: (tab: number) => void;
};

const WidgetEditor: React.FC<WidgetEditorProps> = ({
    dashboard,
    dashboardId,
    selection,
    widgetFormApiRef,
    dashboardItemFormApiRef,
    onSelectionChange,
    activeTab,
    onActiveTabChange,
}) => {
    const { t } = useTranslation();
    const widgetType = selection.widgetType;
    const config = widgetType ? widgetTypeConfig[widgetType] : undefined;
    const FormComponent = config?.FormComponent;
    const [visualExpanded, setVisualExpanded] = React.useState(false);
    const [destacat, setDestacat] = React.useState(false);
    const [plantilla, setPlantilla] = React.useState<any>(null);
    const [entornId, setEntornId] = React.useState<any>(selection.mode === 'create' ? (selection.entornId ?? dashboard?.entorn?.id) : undefined);
    const [widgetData, setWidgetData] = React.useState<any>({});
    const plantillaId = plantilla?.id || dashboard?.plantilla?.id;
    const { plantilla: plantillaToUse, loading: loadingPlantilla } = useDashboardPlantilla(plantillaId);
    const { entornCodi } = useEntornCodi(entornId);
    const hasOverrides = config ? config.hasVisualOverrides(widgetData) : false;
    return (
        <Stack spacing={2}>
            {selection.mode === 'create' && (
                <WidgetTypeSelector
                    value={widgetType}
                    onChange={value => onSelectionChange({ ...selection, widgetType: value })}
                />
            )}
            {config && FormComponent ? (
                <MuiForm
                    resourceName={config.resourceName}
                    id={selection.mode === 'edit' ? selection.widgetId : undefined}
                    additionalData={selection.mode === 'create' ? { aplicacio: selection.aplicacio ?? dashboard?.aplicacio } : undefined}
                    apiRef={widgetFormApiRef}
                    hiddenToolbar
                    formBlockerDisabled
                    componentProps={{ sx: { m: 0, mt: 0 } }}
                >
                    <WidgetDataBridge onChange={setWidgetData} />
                    <WidgetEditorHeader aplicacio={widgetData?.aplicacio} entornCodi={entornCodi} />
                    <Tabs
                        value={activeTab}
                        onChange={(_event, value) => onActiveTabChange(value)}
                        variant="fullWidth"
                        sx={{ minHeight: 36, mb: 1.5 }}
                    >
                        <Tab label={t($ => $.page.widget.wizard.steps.indicators)} sx={{ minHeight: 36, py: 0.5 }} />
                        <Tab label={t($ => $.page.widget.wizard.steps.visual)} sx={{ minHeight: 36, py: 0.5 }} />
                        <Tab label={t($ => $.page.widget.wizard.steps.position)} sx={{ minHeight: 36, py: 0.5 }} />
                    </Tabs>

                    {activeTab === 0 && (
                        <Stack spacing={1.5}>
                            <FormField name="descripcio" type="textarea" />
                            <FormComponent mode="indicators" />
                            <Divider sx={{ my: 0.5 }}>{t($ => $.page.widget.wizard.steps.dimensions)}</Divider>
                            <DimensionsFields />
                            <Divider sx={{ my: 0.5 }}>{t($ => $.page.widget.form.periode)}</Divider>
                            <PeriodFields />
                        </Stack>
                    )}

                    <MuiForm
                        resourceName="dashboardItem"
                        id={selection.mode === 'edit' ? selection.dashboardItemId : undefined}
                        additionalData={
                            selection.mode === 'create'
                                ? {
                                      dashboard: { id: dashboardId },
                                      entornId: selection.entornId ?? dashboard?.entorn?.id,
                                      plantilla: dashboard?.plantilla,
                                      ...defaultDashboardItemData,
                                  }
                                : undefined
                        }
                        apiRef={dashboardItemFormApiRef}
                        hiddenToolbar
                        formBlockerDisabled
                        componentProps={{ sx: { m: 0, mt: 0 } }}
                    >
                        <DestacatBridge onChange={setDestacat} />
                        <PlantillaBridge onChange={setPlantilla} />
                        <EntornIdBridge onChange={setEntornId} />
                        {activeTab === 1 && (
                            <VisualizationTabFields
                                hasOverrides={hasOverrides}
                                onExpandedChange={setVisualExpanded}
                                overrideFields={config?.overrideFields}
                                resetApiRef={widgetFormApiRef}
                            />
                        )}
                        {activeTab === 2 && <PositionSizeFields />}
                    </MuiForm>

                    {activeTab === 1 && (
                        loadingPlantilla ? (
                            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 3 }}>
                                <CircularProgress size={24} />
                            </Box>
                        ) : (
                            <FormComponent mode="visual"
                                dashboardPlantilla={plantillaToUse}
                                destacat={destacat}
                                showOverrideFields={visualExpanded}/>
                        )
                    )}
                </MuiForm>
            ) : (
                <Typography variant="body2" color="text.secondary">
                    {t($ => $.page.widget.editor.selectType)}
                </Typography>
            )}
        </Stack>
    );
};

type TitleEditorProps = {
    dashboard: any;
    dashboardId: string;
    selection: Extract<DashboardEditorSelection, { kind: 'title' }>;
    titleFormApiRef: React.RefObject<FormApi | any>;
    /** Notifica el pare de l'estat en viu (encara no desat) del títol, perquè el pugui previsualitzar al canvas. */
    onLiveDataChange?: (dashboardTitolId: any, data: any) => void;
    /** Notifica el pare del valor actual de "té personalitzacions", perquè el pugui desar amb el mateix càlcul que mostra el indicador. */
    onHasOverridesChange?: (hasOverrides: boolean) => void;
};

const TitleEditor: React.FC<TitleEditorProps> = ({
    dashboard,
    dashboardId,
    selection,
    titleFormApiRef,
    onLiveDataChange,
    onHasOverridesChange,
}) => {
    const [titleData, setTitleData] = React.useState<any>({});
    const [visualExpanded, setVisualExpanded] = React.useState(false);
    // Instantània del títol tal com es va carregar en obrir l'edició (abans de cap canvi de l'usuari). Cal
    // per detectar reversions a un valor que coincideix amb el de referència (vegeu hasVisualOverridesTitol):
    // p.ex. tornar posicioSubtitol a 'SOTA' des d'un valor personalitzat anterior és un canvi genuí, encara
    // que 'SOTA' sigui el valor per defecte de la columna.
    const initialTitleDataRef = React.useRef<any>(null);
    // La plantilla pròpia del títol té prioritat sobre la del dashboard (igual que als widgets).
    const plantillaId = titleData?.plantilla?.id || dashboard?.plantilla?.id;
    const { plantilla: dashboardPlantilla } = useDashboardPlantilla(plantillaId);
    const hasOverrides = hasVisualOverridesTitol(
        titleData,
        selection.mode === 'edit' ? initialTitleDataRef.current : undefined
    );

    React.useEffect(() => {
        onHasOverridesChange?.(hasOverrides);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [hasOverrides]);

    const handleTitleDataChange = (data: any) => {
        if (selection.mode === 'edit' && initialTitleDataRef.current === null && data?.id != null) {
            initialTitleDataRef.current = data;
        }
        setTitleData(data);
        // Només té sentit previsualitzar al canvas un títol EXISTENT que ja s'hi renderitza; un de nou
        // (mode 'create') encara no té cap element al canvas a sobreescriure.
        if (selection.mode === 'edit') {
            onLiveDataChange?.(selection.dashboardTitolId, data);
        }
    };

    // Si es desmunta l'editor (p.ex. l'usuari deselecciona l'element) sense haver desat, es descarta la
    // previsualització en viu perquè el canvas torni a mostrar l'últim estat desat.
    React.useEffect(() => {
        return () => {
            if (selection.mode === 'edit') {
                onLiveDataChange?.(selection.dashboardTitolId, null);
            }
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    return (
        <MuiForm
            resourceName="dashboardTitol"
            id={selection.mode === 'edit' ? selection.dashboardTitolId : undefined}
            additionalData={
                selection.mode === 'create'
                    ? {
                          dashboard: { id: dashboardId },
                          plantilla: dashboard?.plantilla,
                          ...titleDefaultData,
                      }
                    : undefined
            }
            apiRef={titleFormApiRef}
            hiddenToolbar
            formBlockerDisabled
            componentProps={{ sx: { m: 0, mt: 0 } }}
        >
            <WidgetDataBridge onChange={handleTitleDataChange} />
            <DashboardTitleFields
                dashboardPlantilla={dashboardPlantilla}
                hasOverrides={hasOverrides}
                expanded={visualExpanded}
                onExpandedChange={setVisualExpanded}
            />
        </MuiForm>
    );
};

const filtreDefaultData = {
    tipus: 'DIMENSIO',
    multiple: true,
};

/**
 * Selector del codi de dimensió d'un filtre (camp `dimensioCodi`, un simple String al backend). Es implementat a
 * mà (en lloc d'un `<FormField name="dimensioCodi" />`) per oferir un Autocomplete amb les dimensions disponibles
 * per l'aplicació del dashboard, en comptes de deixar l'usuari escriure el codi de memòria.
 */
const FiltreDimensioCodiField: React.FC<{ aplicacioId: any; label: string; excludeCodis?: string[] }> = ({
    aplicacioId,
    label,
    excludeCodis,
}) => {
    const { dataGetFieldValue, dataDispatchAction, fields, fieldErrors } = useFormContext();
    const { isReady, find } = useResourceApiService('dimensio');
    const [options, setOptions] = React.useState<{ codi: string; nom: string }[]>([]);
    const value = dataGetFieldValue('dimensioCodi');
    const field = fields?.find((f: any) => f.name === 'dimensioCodi');
    const fieldError = fieldErrors?.find((e: any) => e.field === 'dimensioCodi');

    React.useEffect(() => {
        let cancelled = false;
        if (isReady && aplicacioId != null) {
            find({ namedQueries: [`filterByApp:${aplicacioId}`], unpaged: true }).then((response) => {
                if (cancelled) return;
                const rows = (response.rows ?? []) as Array<{ codi?: string; nom?: string }>;
                setOptions(
                    rows
                        .filter((row) => typeof row.codi === 'string' && row.codi.length > 0)
                        .map((row) => ({ codi: row.codi as string, nom: row.nom || (row.codi as string) }))
                );
            });
        }
        return () => {
            cancelled = true;
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isReady, aplicacioId]);

    const selected = options.find((option) => option.codi === value) ?? null;
    // Una dimensió ja usada en un altre filtre del mateix dashboard no es pot tornar a triar (vegeu FiltreEditor),
    // però si ja estava seleccionada (p. ex. en obrir l'edició d'aquest mateix filtre) es manté visible.
    const visibleOptions = options.filter(
        (option) => option.codi === value || !excludeCodis?.includes(option.codi)
    );

    return (
        <Autocomplete
            size="small"
            options={visibleOptions}
            value={selected}
            getOptionLabel={(option) => option.nom}
            isOptionEqualToValue={(option, val) => option.codi === val.codi}
            onChange={(_event, newValue) => {
                dataDispatchAction({
                    type: FormFieldDataActionType.FIELD_CHANGE,
                    payload: { fieldName: 'dimensioCodi', field, value: newValue?.codi },
                });
            }}
            renderInput={(params) => (
                <TextField
                    {...params}
                    label={label}
                    required
                    error={fieldError != null}
                    helperText={fieldError?.message}
                />
            )}
        />
    );
};

type FiltreEditorProps = {
    dashboard: any;
    dashboardId: string;
    selection: Extract<DashboardEditorSelection, { kind: 'filtre' }>;
    filtreFormApiRef: React.RefObject<FormApi | any>;
    /** Filtres ja configurats al dashboard, per impedir-ne duplicats (vegeu comentari a sota) */
    dashboardFiltres?: DashboardFiltre[];
};

const FiltreEditor: React.FC<FiltreEditorProps> = ({
    dashboard,
    dashboardId,
    selection,
    filtreFormApiRef,
    dashboardFiltres,
}) => {
    const { t } = useTranslation();
    const [filtreData, setFiltreData] = React.useState<any>({});
    // Un dashboard només admet un filtre de tipus PERIODE, i cap dimensió es pot repetir en dos filtres DIMENSIO
    // (validat també al backend, vegeu DashboardFiltreServiceImpl); aquí només s'evita que l'usuari pugui triar-ho.
    const otherFiltres = React.useMemo(
        () =>
            (dashboardFiltres ?? []).filter(
                (f) => !(selection.mode === 'edit' && String(f.id) === String(selection.dashboardFiltreId))
            ),
        [dashboardFiltres, selection]
    );
    const periodeJaUsat = otherFiltres.some((f) => f.tipus === 'PERIODE');
    const dimensioCodisUsats = otherFiltres
        .filter((f) => f.tipus === 'DIMENSIO' && f.dimensioCodi)
        .map((f) => f.dimensioCodi as string);
    return (
        <MuiForm
            resourceName="dashboardFiltre"
            id={selection.mode === 'edit' ? selection.dashboardFiltreId : undefined}
            additionalData={
                selection.mode === 'create'
                    ? {
                          dashboard: { id: dashboardId },
                          ordre: selection.nextOrdre ?? 0,
                          ...filtreDefaultData,
                      }
                    : undefined
            }
            apiRef={filtreFormApiRef}
            hiddenToolbar
            formBlockerDisabled
            componentProps={{ sx: { m: 0, mt: 0 } }}
        >
            <WidgetDataBridge onChange={setFiltreData} />
            <Stack spacing={1.5}>
                <FormField
                    name="tipus"
                    label={t($ => $.page.widget.editor.filtre.tipus)}
                    hiddenEnumValues={periodeJaUsat ? ['PERIODE'] : undefined}
                />
                {filtreData?.tipus === 'DIMENSIO' && (
                    <FiltreDimensioCodiField
                        aplicacioId={dashboard?.aplicacio?.id}
                        label={t($ => $.page.widget.editor.filtre.dimensio)}
                        excludeCodis={dimensioCodisUsats}
                    />
                )}
                <FormField name="titol" label={t($ => $.page.widget.editor.filtre.titol)} required={false} />
                {filtreData?.tipus === 'DIMENSIO' && (
                    <FormField
                        name="multiple"
                        type="checkbox"
                        label={t($ => $.page.widget.editor.filtre.multiple)}
                    />
                )}
                <FormField name="ordre" label={t($ => $.page.widget.editor.filtre.ordre)} type="number" />
            </Stack>
        </MuiForm>
    );
};

export default DashboardEditorSidePanel;
