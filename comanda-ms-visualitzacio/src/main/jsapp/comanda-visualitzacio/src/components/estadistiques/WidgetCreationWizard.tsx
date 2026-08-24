import * as React from 'react';
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Icon,
    IconButton,
    Paper,
    Step,
    StepLabel,
    Stepper,
    Stack,
    Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import { FormField, MuiFilter, useBaseAppContext, useFormContext, useResourceApiService } from 'reactlib';
import { useTranslation } from 'react-i18next';
import MuiForm from '../../../lib/components/mui/form/MuiForm.tsx';
import type { FormApi } from '../../../lib/components/form/FormContext.tsx';
import EstadisticaSimpleWidgetForm, { hasVisualOverrides as simpleHasVisualOverrides } from './EstadisticaSimpleWidgetForm.tsx';
import EstadisticaGraficWidgetForm, { hasVisualOverrides as graficHasVisualOverrides } from './EstadisticaGraficWidgetForm.tsx';
import EstadisticaTaulaWidgetForm, { hasVisualOverrides as taulaHasVisualOverrides } from './EstadisticaTaulaWidgetForm.tsx';
import { DimensionsFields, PeriodFields, PersonalitzatFields, TitleDescriptionFields, hasVisualOverridesTitol } from './EstadisticaWidgetFormFields.tsx';
import { useDashboardPlantilla } from './dashboardPlantillaHook.ts';
import { WidgetPreview } from './WidgetPreview.tsx';
import type { DashboardWidgetType } from './DashboardEditorSidePanel.tsx';

type WidgetKind = DashboardWidgetType | 'TITOL';

type StepKey = 'type' | 'content' | 'indicators' | 'dimensions' | 'period' | 'visual';

export type WidgetCreationWizardProps = {
    open: boolean;
    dashboard: any;
    dashboardId: string;
    initialWidgetType?: DashboardWidgetType;
    initialEntornId?: any;
    initialAplicacio?: any;
    onClose: () => void;
    onCreated: () => void;
};

const widgetTypeConfig: Record<
    DashboardWidgetType,
    {
        resourceName: string;
        FormComponent: React.FC<{ mode?: 'full' | 'stats' | 'indicators' | 'visual'; dashboardPlantilla?: any; destacat?: boolean; showOverrideFields?: boolean }>;
        hasVisualOverrides: (data: any) => boolean;
    }
> = {
    SIMPLE: { resourceName: 'estadisticaSimpleWidget', FormComponent: EstadisticaSimpleWidgetForm, hasVisualOverrides: simpleHasVisualOverrides },
    GRAFIC: { resourceName: 'estadisticaGraficWidget', FormComponent: EstadisticaGraficWidgetForm, hasVisualOverrides: graficHasVisualOverrides },
    TAULA: { resourceName: 'estadisticaTaulaWidget', FormComponent: EstadisticaTaulaWidgetForm, hasVisualOverrides: taulaHasVisualOverrides },
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

const PERIOD_FIELD_PREFIXES = ['periodeMode', 'presetPeriode', 'presetCount', 'relatiu', 'absolut'];

const isEmpty = (value: any): boolean =>
    value === undefined || value === null || value === '' || (Array.isArray(value) && value.length === 0);

/**
 * Comprova els camps obligatoris de la passa "Informació a mostrar", replicant les regles dels
 * validadors del backend (ValidSimpleWidgetValidator/ValidGraficWidgetValidator/ValidTaulaWidgetValidator)
 * perquè l'usuari no pugui avançar de passa deixant camps obligatoris buits.
 */
const isIndicatorsStepValid = (kind: WidgetKind | undefined, data: any): boolean => {
    if (kind === 'SIMPLE') {
        if (isEmpty(data?.indicador) || isEmpty(data?.titolIndicador) || isEmpty(data?.tipusIndicador)) return false;
        if (data.tipusIndicador === 'AVERAGE' && isEmpty(data?.periodeIndicador)) return false;
        return true;
    }
    if (kind === 'GRAFIC') {
        if (isEmpty(data?.tipusGrafic) || isEmpty(data?.tipusDades)) return false;
        const tipusDades = data.tipusDades;
        if (tipusDades === 'UN_INDICADOR' || tipusDades === 'UN_INDICADOR_AMB_DESCOMPOSICIO' || tipusDades === 'DOS_INDICADORS') {
            if (isEmpty(data?.indicador) || isEmpty(data?.agregacio)) return false;
            if (data.agregacio === 'AVERAGE' && isEmpty(data?.unitatAgregacio)) return false;
        }
        if (tipusDades === 'UN_INDICADOR_AMB_DESCOMPOSICIO' && isEmpty(data?.descomposicioDimensio)) return false;
        const skipTempsAgrupacio = tipusDades === 'UN_INDICADOR_AMB_DESCOMPOSICIO' && data?.agruparPerDimensioDescomposicio === true;
        if (!skipTempsAgrupacio && isEmpty(data?.tempsAgrupacio)) return false;
        if (tipusDades === 'VARIS_INDICADORS') {
            if (isEmpty(data?.indicadorsInfo)) return false;
            for (const ind of data.indicadorsInfo) {
                if (isEmpty(ind?.indicador) || isEmpty(ind?.titol) || isEmpty(ind?.agregacio)) return false;
                if (ind.agregacio === 'AVERAGE' && isEmpty(ind?.unitatAgregacio)) return false;
            }
        }
        return true;
    }
    if (kind === 'TAULA') {
        if (isEmpty(data?.columnes)) return false;
        for (const col of data.columnes) {
            if (isEmpty(col?.indicador) || isEmpty(col?.titol) || isEmpty(col?.agregacio)) return false;
            if (col.agregacio === 'AVERAGE' && isEmpty(col?.unitatAgregacio)) return false;
        }
        if (isEmpty(data?.dimensioAgrupacio)) return false;
        return true;
    }
    return true;
};


/** Comprova els camps obligatoris de la passa "Període", replicant ValidWidgetValidator.validatePeriode. */
const isPeriodStepValid = (data: any): boolean => {
    const mode = data?.periodeMode;
    if (isEmpty(mode)) return false;
    if (mode === 'PRESET') return !isEmpty(data?.presetPeriode);
    if (mode === 'RELATIU') {
        return !isEmpty(data?.relatiuPuntReferencia) && !isEmpty(data?.relatiuCount)
            && !isEmpty(data?.relatiueUnitat) && !isEmpty(data?.relatiuAlineacio);
    }
    if (mode === 'ABSOLUT') {
        if (isEmpty(data?.absolutTipus)) return false;
        if (data.absolutTipus === 'DATE_RANGE') return !isEmpty(data?.absolutDataInici);
        if (data.absolutTipus === 'SPECIFIC_PERIOD_OF_YEAR') {
            if (isEmpty(data?.absolutAnyReferencia)) return false;
            if (data.absolutAnyReferencia === 'SPECIFIC_YEAR' && isEmpty(data?.absolutAnyValor)) return false;
            return !isEmpty(data?.absolutPeriodeUnitat) && !isEmpty(data?.absolutPeriodeInici) && !isEmpty(data?.absolutPeriodeFi);
        }
        return true;
    }
    return true;
};

/**
 * A partir del nom d'un camp amb error de validació (tornat pel backend), determina a quina passa de
 * l'assistent pertany, per poder-hi navegar i que l'usuari vegi l'error en context en lloc de quedar
 * bloquejat sense saber quin camp cal corregir.
 */
const stepKeyForField = (field: string | undefined, isTitol: boolean): StepKey => {
    if (isTitol) {
        if (field === 'titol' || field === 'subtitol') return 'content';
        return 'visual';
    }
    if (!field) return 'indicators';
    if (field === 'titol' || field === 'descripcio' || field === 'aplicacio') return 'type';
    if (field === 'dimensionsValor' || field === 'dimensio') return 'dimensions';
    if (PERIOD_FIELD_PREFIXES.some(prefix => field.startsWith(prefix))) return 'period';
    if (field === 'destacat' || field === 'plantilla' || field === 'personalitzat') return 'visual';
    return 'indicators';
};

const KIND_ICON: Record<WidgetKind, string> = {
    TITOL: 'title',
    SIMPLE: 'description',
    GRAFIC: 'bar_chart_4_bars',
    TAULA: 'table',
};

/** Pont invisible que notifica el pare cada vegada que canvien les dades del formulari actiu */
const FormDataBridge: React.FC<{ onChange: (data: any) => void }> = ({ onChange }) => {
    const { data } = useFormContext();
    React.useEffect(() => {
        onChange(data);
    }, [data]);
    return null;
};

const StepIntro: React.FC<{ text: string }> = ({ text }) => (
    <Alert severity="info" sx={{ mb: 2 }}>
        {text}
    </Alert>
);

type TypeCardProps = {
    kindValue: WidgetKind;
    selected: boolean;
    label: string;
    description: string;
    onClick: () => void;
};

const TypeCard: React.FC<TypeCardProps> = ({ kindValue, selected, label, description, onClick }) => (
    <Paper
        variant="outlined"
        onClick={onClick}
        role="button"
        aria-pressed={selected}
        sx={{
            p: 2,
            cursor: 'pointer',
            flex: '1 1 200px',
            minWidth: 200,
            borderColor: selected ? 'primary.main' : 'divider',
            borderWidth: selected ? 2 : 1,
            backgroundColor: selected ? 'action.selected' : 'background.paper',
            '&:hover': { borderColor: 'primary.main' },
        }}
    >
        <Stack direction="row" spacing={1} alignItems="center">
            <Icon color={selected ? 'primary' : 'inherit'}>{KIND_ICON[kindValue]}</Icon>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                {label}
            </Typography>
        </Stack>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            {description}
        </Typography>
    </Paper>
);

type AppEntornPickerProps = {
    dashboard: any;
    initialAplicacio?: any;
    initialEntornId?: any;
    onChange: (aplicacio: any, entornId: any) => void;
};

/** Selector d'aplicació i entorn. Si el dashboard ja en té un de fixat, es mostra desactivat (només lectura). */
const AppEntornPicker: React.FC<AppEntornPickerProps> = ({ dashboard, initialAplicacio, initialEntornId, onChange }) => {
    const aplicacioFixed = !!dashboard?.aplicacio;
    const entornFixed = !!dashboard?.entorn?.id;
    return (
        <MuiFilter
            initialData={{
                app: dashboard?.aplicacio ?? initialAplicacio,
                entorn: dashboard?.entorn ?? (initialEntornId ? { id: initialEntornId } : undefined),
            }}
            detached
            resourceName="entornApp"
            code="optional_entornApp_filter"
            commonFieldComponentProps={{ size: 'small' }}
            onDataChange={(data: any) => onChange(data?.app, data?.entorn?.id)}
            springFilterBuilder={() => undefined}
        >
            <Grid container spacing={1}>
                <Grid size={6}>
                    <FormField name="entorn" disabled={entornFixed} />
                </Grid>
                <Grid size={6}>
                    <FormField name="app" disabled={aplicacioFixed} />
                </Grid>
            </Grid>
        </MuiFilter>
    );
};

const TitleContentFields: React.FC = () => (
    <Grid container spacing={2}>
        <Grid size={12}>
            <FormField name="titol" />
        </Grid>
        <Grid size={12}>
            <FormField name="subtitol" required={false} />
        </Grid>
    </Grid>
);

type TitleVisualFieldsProps = {
    hasOverrides: boolean;
    expanded: boolean;
    onExpandedChange: (expanded: boolean) => void;
    dashboardPlantilla?: any;
};

const TitleVisualFields: React.FC<TitleVisualFieldsProps> = ({ hasOverrides, expanded, onExpandedChange, dashboardPlantilla }) => {
    const { data } = useFormContext();
    const { t } = useTranslation();
    return (
        <Grid container spacing={1.5}>
            <Grid size={12}>
                <FormField name="tipusTitol" />
            </Grid>
            <Grid size={12}>
                <PersonalitzatFields
                    personalitzatLabel={t($ => $.page.widget.wizard.visual.personalitzat)}
                    personalitzatHelp={t($ => $.page.widget.wizard.visual.personalitzatHelp)}
                    personalitzatBadge={t($ => $.page.widget.wizard.visual.personalitzatBadge)}
                    hasOverrides={hasOverrides}
                    onExpandedChange={onExpandedChange}
                />
            </Grid>
            <Grid size={12}>
                <Typography variant="subtitle2" sx={{ mb: 1 }}>
                    {t($ => $.page.widget.form.preview)}
                </Typography>
                <Box sx={{ height: '120px' }}>
                    <WidgetPreview widgetType="TITOL" widgetData={data} dashboardPlantilla={dashboardPlantilla} />
                </Box>
            </Grid>
            {expanded && (
                <>
                    <Grid size={6}>
                        <FormField name="midaFontTitol" type="number" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="colorTitol" type="color" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="midaFontSubtitol" type="number" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="colorSubtitol" type="color" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="colorFons" type="color" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="mostrarVora" type="checkbox" />
                    </Grid>
                    {data?.mostrarVora && (
                        <>
                            <Grid size={6}>
                                <FormField name="colorVora" type="color" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="ampleVora" type="number" required={false} />
                            </Grid>
                        </>
                    )}
                </>
            )}
        </Grid>
    );
};

export const WidgetCreationWizard: React.FC<WidgetCreationWizardProps> = ({
    open,
    dashboard,
    dashboardId,
    initialWidgetType,
    initialEntornId,
    initialAplicacio,
    onClose,
    onCreated,
}) => {
    const { t } = useTranslation();
    const { temporalMessageShow } = useBaseAppContext();
    const { create: createDashboardItem } = useResourceApiService('dashboardItem');
    const { create: createDashboardTitol } = useResourceApiService('dashboardTitol');

    const [kind, setKind] = React.useState<WidgetKind | undefined>(initialWidgetType);
    const [activeStep, setActiveStep] = React.useState(0);
    const [formData, setFormData] = React.useState<any>({});
    const [visualExpanded, setVisualExpanded] = React.useState(false);
    const [destacat, setDestacat] = React.useState(false);
    const [plantilla, setPlantilla] = React.useState<any>(null);
    const [saving, setSaving] = React.useState(false);

    const [pickedAplicacio, setPickedAplicacio] = React.useState<any>(null);
    const [pickedEntornId, setPickedEntornId] = React.useState<any>(null);

    const widgetFormApiRef = React.useRef<FormApi | any>({});
    const dashboardItemFormApiRef = React.useRef<FormApi | any>({});
    const titleFormApiRef = React.useRef<FormApi | any>({});

    const resolvedAplicacio = dashboard?.aplicacio ?? pickedAplicacio ?? initialAplicacio;
    const resolvedEntornId = dashboard?.entorn?.id ?? pickedEntornId ?? initialEntornId;

    const plantillaId = plantilla?.id || dashboard?.plantilla?.id;
    const { plantilla: plantillaToUse, loading: loadingPlantilla } = useDashboardPlantilla(plantillaId);

    const steps: { key: StepKey; label: string }[] = React.useMemo(() => {
        if (kind === 'TITOL') {
            return [
                { key: 'type', label: t($ => $.page.widget.wizard.steps.type) },
                { key: 'content', label: t($ => $.page.widget.wizard.steps.content) },
                { key: 'visual', label: t($ => $.page.widget.wizard.steps.visual) },
            ];
        }
        return [
            { key: 'type', label: t($ => $.page.widget.wizard.steps.type) },
            { key: 'indicators', label: t($ => $.page.widget.wizard.steps.indicators) },
            { key: 'dimensions', label: t($ => $.page.widget.wizard.steps.dimensions) },
            { key: 'period', label: t($ => $.page.widget.wizard.steps.period) },
            { key: 'visual', label: t($ => $.page.widget.wizard.steps.visual) },
        ];
    }, [kind, t]);

    const activeStepKey = steps[activeStep]?.key;
    const isLastStep = activeStep === steps.length - 1;

    const config = kind && kind !== 'TITOL' ? widgetTypeConfig[kind] : undefined;
    const FormComponent = config?.FormComponent;
    const hasOverrides = config ? config.hasVisualOverrides(formData) : false;
    const hasOverridesTitol = kind === 'TITOL' ? hasVisualOverridesTitol(formData) : false;

    const canAdvance = React.useMemo(() => {
        if (activeStepKey === 'type') {
            if (!kind) return false;
            if (kind === 'TITOL') return true;
            return !!resolvedAplicacio && !!resolvedEntornId && !!(formData?.titol ?? '').trim();
        }
        if (activeStepKey === 'content') {
            return !!(formData?.titol ?? '').trim();
        }
        if (activeStepKey === 'indicators') {
            return isIndicatorsStepValid(kind, formData);
        }
        // La passa "dimensions" (dimensionsValor) és opcional: si no se n'indica cap, es mostren
        // totes les dades, per això no bloqueja mai l'avanç.
        if (activeStepKey === 'period') {
            return isPeriodStepValid(formData);
        }
        return true;
    }, [activeStepKey, kind, formData, resolvedAplicacio, resolvedEntornId]);

    const handleSelectKind = (value: WidgetKind) => {
        if (value !== kind) {
            setKind(value);
            setFormData({});
        }
    };

    const handleNext = () => setActiveStep(step => Math.min(step + 1, steps.length - 1));
    const handleBack = () => setActiveStep(step => Math.max(step - 1, 0));

    const handleClose = () => {
        onClose();
    };

    const handleFinish = async () => {
        setSaving(true);
        try {
            if (kind === 'TITOL') {
                const titleData = titleFormApiRef.current?.getData() ?? {};
                await createDashboardTitol({
                    data: {
                        ...titleData,
                        // El backend només aplica els camps propis del títol per sobre de la plantilla
                        // quan `personalitzat` és cert (vegeu resolveAtributsVisualsTitol al backend).
                        personalitzat: hasOverridesTitol,
                    },
                });
            } else {
                const savedWidget = await widgetFormApiRef.current?.save();
                const dashboardItemData = dashboardItemFormApiRef.current?.getData() ?? {};
                await createDashboardItem({
                    data: {
                        ...dashboardItemData,
                        // El backend només aplica els camps propis del widget per sobre de la plantilla
                        // quan `personalitzat` és cert (vegeu resolveAtributsVisuals al backend).
                        personalitzat: hasOverrides,
                        dashboard: { id: dashboardId },
                        widget: { id: savedWidget?.id },
                        entornId: resolvedEntornId,
                    },
                });
            }
            temporalMessageShow(null, t($ => $.page.dashboards.action.addWidget.success), 'success');
            onCreated();
            onClose();
        } catch (error: any) {
            const validationFieldErrors = error?.errors ?? error?.validationErrors;
            if (Array.isArray(validationFieldErrors) && validationFieldErrors.length > 0) {
                const invalidStepKey = stepKeyForField(validationFieldErrors[0]?.field, kind === 'TITOL');
                const invalidStepIndex = steps.findIndex(step => step.key === invalidStepKey);
                if (invalidStepIndex >= 0) {
                    setActiveStep(invalidStepIndex);
                }
                temporalMessageShow(null, t($ => $.page.widget.wizard.actions.validationError), 'error');
            } else if (error?.message) {
                temporalMessageShow(null, error.message, 'error');
            }
        } finally {
            setSaving(false);
        }
    };

    return (
        <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                {t($ => $.page.widget.wizard.title)}
                <IconButton size="small" onClick={handleClose}>
                    <Icon fontSize="small">close</Icon>
                </IconButton>
            </DialogTitle>
            <DialogContent>
                <Stepper activeStep={activeStep} sx={{ mb: 3 }}>
                    {steps.map(step => (
                        <Step key={step.key}>
                            <StepLabel>{step.label}</StepLabel>
                        </Step>
                    ))}
                </Stepper>

                {activeStepKey === 'type' && (
                    <Box sx={{ mb: 2 }}>
                        <StepIntro text={t($ => $.page.widget.wizard.help.type)} />
                        <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap sx={{ mb: 2 }}>
                            {(['TITOL', 'SIMPLE', 'GRAFIC', 'TAULA'] as WidgetKind[]).map(kindOption => (
                                <TypeCard
                                    key={kindOption}
                                    kindValue={kindOption}
                                    selected={kind === kindOption}
                                    label={t($ => ($.page.widget.wizard.types as any)[kindOption.toLowerCase()].label)}
                                    description={t($ => ($.page.widget.wizard.types as any)[kindOption.toLowerCase()].description)}
                                    onClick={() => handleSelectKind(kindOption)}
                                />
                            ))}
                        </Stack>
                        {kind && kind !== 'TITOL' && (
                            <Box sx={{ mb: 2 }}>
                                <Typography variant="subtitle2" sx={{ mb: 1 }}>
                                    {t($ => $.page.widget.wizard.help.appEntorn)}
                                </Typography>
                                <AppEntornPicker
                                    dashboard={dashboard}
                                    initialAplicacio={initialAplicacio}
                                    initialEntornId={initialEntornId}
                                    onChange={(aplicacio, entornId) => {
                                        setPickedAplicacio(aplicacio);
                                        setPickedEntornId(entornId);
                                        widgetFormApiRef.current?.setFieldValue?.('aplicacio', aplicacio);
                                    }}
                                />
                            </Box>
                        )}
                    </Box>
                )}

                {kind && kind !== 'TITOL' && config && FormComponent && (
                    <MuiForm
                        key={kind}
                        resourceName={config.resourceName}
                        apiRef={widgetFormApiRef}
                        hiddenToolbar
                        formBlockerDisabled
                        additionalData={resolvedAplicacio ? { aplicacio: resolvedAplicacio } : undefined}
                        componentProps={{ sx: { m: 0, mt: 0 } }}
                    >
                        <FormDataBridge onChange={setFormData} />
                        {activeStepKey === 'type' && <TitleDescriptionFields />}
                        {activeStepKey === 'indicators' && (
                            <>
                                <StepIntro text={t($ => $.page.widget.wizard.help.indicators)} />
                                <FormComponent mode="indicators" />
                            </>
                        )}
                        {activeStepKey === 'dimensions' && (
                            <>
                                <StepIntro text={t($ => $.page.widget.wizard.help.dimensions)} />
                                <DimensionsFields />
                            </>
                        )}
                        {activeStepKey === 'period' && (
                            <>
                                <StepIntro text={t($ => $.page.widget.wizard.help.period)} />
                                <PeriodFields />
                            </>
                        )}
                        {activeStepKey === 'visual' && (
                            <>
                                <StepIntro text={t($ => $.page.widget.wizard.help.visual)} />
                                <MuiForm
                                    resourceName="dashboardItem"
                                    apiRef={dashboardItemFormApiRef}
                                    hiddenToolbar
                                    formBlockerDisabled
                                    additionalData={{
                                        dashboard: { id: dashboardId },
                                        entornId: resolvedEntornId,
                                        plantilla: dashboard?.plantilla,
                                        ...defaultDashboardItemData,
                                    }}
                                    componentProps={{ sx: { m: 0, mt: 0 } }}
                                >
                                    <FormDataBridge
                                        onChange={data => {
                                            setDestacat(!!data?.destacat);
                                            setPlantilla(data?.plantilla);
                                        }}
                                    />
                                    <PersonalitzatFields
                                        personalitzatLabel={t($ => $.page.widget.wizard.visual.personalitzat)}
                                        personalitzatHelp={t($ => $.page.widget.wizard.visual.personalitzatHelp)}
                                        personalitzatBadge={t($ => $.page.widget.wizard.visual.personalitzatBadge)}
                                        hasOverrides={hasOverrides}
                                        onExpandedChange={setVisualExpanded}
                                    />
                                </MuiForm>
                                {loadingPlantilla ? (
                                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 3 }}>
                                        <CircularProgress size={24} />
                                    </Box>
                                ) : (
                                    <FormComponent
                                        mode="visual"
                                        dashboardPlantilla={plantillaToUse}
                                        destacat={destacat}
                                        showOverrideFields={visualExpanded}
                                    />
                                )}
                            </>
                        )}
                    </MuiForm>
                )}

                {kind === 'TITOL' && (
                    <MuiForm
                        resourceName="dashboardTitol"
                        apiRef={titleFormApiRef}
                        hiddenToolbar
                        formBlockerDisabled
                        additionalData={{
                            dashboard: { id: dashboardId },
                            plantilla: dashboard?.plantilla,
                            ...titleDefaultData,
                        }}
                        componentProps={{ sx: { m: 0, mt: 0 } }}
                    >
                        <FormDataBridge
                            onChange={data => {
                                setFormData(data);
                                setDestacat(!!data?.destacat);
                                setPlantilla(data?.plantilla);
                            }}
                        />
                        {activeStepKey === 'content' && (
                            <>
                                <StepIntro text={t($ => $.page.widget.wizard.help.content)} />
                                <TitleContentFields />
                            </>
                        )}
                        {activeStepKey === 'visual' && (
                            <>
                                <StepIntro text={t($ => $.page.widget.wizard.help.visual)} />
                                <TitleVisualFields
                                    hasOverrides={hasOverridesTitol}
                                    expanded={visualExpanded}
                                    onExpandedChange={setVisualExpanded}
                                    dashboardPlantilla={plantillaToUse}
                                />
                            </>
                        )}
                    </MuiForm>
                )}
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 2 }}>
                <Button onClick={handleClose} disabled={saving}>
                    {t($ => $.page.widget.wizard.actions.cancel)}
                </Button>
                <Box sx={{ flex: 1 }} />
                <Button onClick={handleBack} disabled={activeStep === 0 || saving}>
                    {t($ => $.page.widget.wizard.actions.back)}
                </Button>
                {isLastStep ? (
                    <Button variant="contained" onClick={handleFinish} disabled={saving}>
                        {saving ? <CircularProgress size={20} /> : t($ => $.page.widget.wizard.actions.finish)}
                    </Button>
                ) : (
                    <Button variant="contained" onClick={handleNext} disabled={!canAdvance}>
                        {t($ => $.page.widget.wizard.actions.next)}
                    </Button>
                )}
            </DialogActions>
        </Dialog>
    );
};

export default WidgetCreationWizard;
