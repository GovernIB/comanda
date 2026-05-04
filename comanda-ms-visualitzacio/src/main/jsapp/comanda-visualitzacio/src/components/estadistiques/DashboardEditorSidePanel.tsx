import * as React from 'react';
import {
    Box,
    Button,
    Divider,
    FormControl,
    Icon,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    Stack,
    ToggleButton,
    Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid';
import {
    FormField,
    useBaseAppContext,
    useConfirmDialogButtons,
    useFormContext,
    useResourceApiService,
} from 'reactlib';
import MuiForm from '../../../lib/components/mui/form/MuiForm.tsx';
import type { FormApi } from '../../../lib/components/form/FormContext.tsx';
import EstadisticaSimpleWidgetForm from './EstadisticaSimpleWidgetForm.tsx';
import EstadisticaGraficWidgetForm from './EstadisticaGraficWidgetForm.tsx';
import EstadisticaTaulaWidgetForm from './EstadisticaTaulaWidgetForm.tsx';

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
      };

type DashboardEditorSidePanelProps = {
    dashboard: any;
    dashboardId: string;
    selection: DashboardEditorSelection;
    onSelectionChange: (selection: DashboardEditorSelection) => void;
    onSaved: () => void;
    onDeleted: () => void;
};

const widgetTypeConfig: Record<
    DashboardWidgetType,
    {
        label: string;
        resourceName: string;
        FormComponent: React.FC<{ mode?: 'full' | 'stats' | 'visual' }>;
    }
> = {
    SIMPLE: {
        label: 'Simple',
        resourceName: 'estadisticaSimpleWidget',
        FormComponent: EstadisticaSimpleWidgetForm,
    },
    GRAFIC: {
        label: 'Gràfic',
        resourceName: 'estadisticaGraficWidget',
        FormComponent: EstadisticaGraficWidgetForm,
    },
    TAULA: {
        label: 'Taula',
        resourceName: 'estadisticaTaulaWidget',
        FormComponent: EstadisticaTaulaWidgetForm,
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

/** Pont invisible que llegeix personalitzat del context del formulari dashboardItem i notifica el pare */
const PersonalitzatBridge: React.FC<{ onChange: (v: boolean) => void }> = ({ onChange }) => {
    const { data } = useFormContext();
    React.useEffect(() => {
        onChange(!!data?.personalitzat);
    }, [data?.personalitzat]);
    return null;
};

const DashboardItemFormFields = () => {
    const { data, apiRef } = useFormContext();
    const isPersonalitzat = !!data?.personalitzat;
    return (
        <Grid container spacing={1.5}>
            <Grid size={12}>
                <ToggleButton
                    value="personalitzat"
                    selected={isPersonalitzat}
                    onChange={() => apiRef?.current?.setFieldValue('personalitzat', !isPersonalitzat)}
                    size="small"
                    sx={{ width: '100%', justifyContent: 'flex-start', gap: 1 }}
                >
                    <Icon sx={{ fontSize: '1rem' }}>tune</Icon>
                    Personalitzat
                </ToggleButton>
            </Grid>
            {!isPersonalitzat && (
                <>
                    <Grid size={12}>
                        <FormField name="destacat" label="Mostrar com a destacat" type="checkbox" />
                    </Grid>
                    <Grid size={12}>
                        <FormField name="plantilla" label="Plantilla" />
                    </Grid>
                </>
            )}
            <Grid size={6}>
                <FormField name="posX" label="Posició X" type="number" />
            </Grid>
            <Grid size={6}>
                <FormField name="posY" label="Posició Y" type="number" required={false} />
            </Grid>
            <Grid size={6}>
                <FormField name="width" label="Amplada" type="number" />
            </Grid>
            <Grid size={6}>
                <FormField name="height" label="Alçada" type="number" />
            </Grid>
        </Grid>
    );
};

const DashboardTitleFields = () => {
    const { data } = useFormContext();
    return (
        <Stack spacing={2}>
            <PanelSection title="Configuració de dades">
                <Grid container spacing={1.5}>
                    <Grid size={12}>
                        <FormField name="titol" label="Text del títol" />
                    </Grid>
                </Grid>
            </PanelSection>
            <PanelSection title="Configuració gràfica">
                <Grid container spacing={1.5}>
                    <Grid size={12}>
                        <FormField name="destacat" label="Mostrar com a destacat" type="checkbox" />
                    </Grid>
                    <Grid size={12}>
                        <FormField name="plantilla" label="Plantilla" />
                    </Grid>
                    <Grid size={12}>
                        <FormField name="tipusTitol" label="Tipus de títol" />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="posX" label="Posició X" type="number" />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="posY" label="Posició Y" type="number" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="width" label="Amplada" type="number" />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="height" label="Alçada" type="number" />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="midaFontTitol" label="Mida del text" type="number" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="colorTitol" label="Color del text" type="color" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="colorFons" label="Color de fons" type="color" required={false} />
                    </Grid>
                    <Grid size={6}>
                        <FormField name="mostrarVora" label="Mostrar vora" type="checkbox" />
                    </Grid>
                    {data?.mostrarVora && (
                        <>
                            <Grid size={6}>
                                <FormField name="colorVora" label="Color de vora" type="color" required={false} />
                            </Grid>
                            <Grid size={6}>
                                <FormField name="ampleVora" label="Amplada de vora" type="number" required={false} />
                            </Grid>
                        </>
                    )}
                </Grid>
            </PanelSection>
        </Stack>
    );
};

const EmptyPanel = () => (
    <Box sx={{ p: 2, color: 'text.secondary' }}>
        <Typography variant="body2">
            Seleccionau un element del canvas o creau un widget nou per editar-ne les propietats.
        </Typography>
    </Box>
);

const WidgetTypeSelector = ({
    value,
    onChange,
}: {
    value?: DashboardWidgetType;
    onChange: (value: DashboardWidgetType) => void;
}) => (
    <FormControl fullWidth size="small">
        <InputLabel id="dashboard-widget-type-label">Tipus de widget</InputLabel>
        <Select
            labelId="dashboard-widget-type-label"
            value={value ?? ''}
            label="Tipus de widget"
            onChange={event => onChange(event.target.value as DashboardWidgetType)}
        >
            {Object.entries(widgetTypeConfig).map(([type, config]) => (
                <MenuItem key={type} value={type}>
                    {config.label}
                </MenuItem>
            ))}
        </Select>
    </FormControl>
);

export const DashboardEditorSidePanel: React.FC<DashboardEditorSidePanelProps> = ({
    dashboard,
    dashboardId,
    selection,
    onSelectionChange,
    onSaved,
    onDeleted,
}) => {
    const widgetFormApiRef = React.useRef<FormApi | any>({});
    const dashboardItemFormApiRef = React.useRef<FormApi | any>({});
    const titleFormApiRef = React.useRef<FormApi | any>({});
    const { temporalMessageShow, messageDialogShow } = useBaseAppContext();
    const confirmDialogButtons = useConfirmDialogButtons();
    const { create: createDashboardItem, delete: deleteDashboardItem } =
        useResourceApiService('dashboardItem');
    const { delete: deleteDashboardTitol } = useResourceApiService('dashboardTitol');
    const [saving, setSaving] = React.useState(false);

    const selectionKey = React.useMemo(() => {
        if (selection.kind === 'widget') {
            return `widget-${selection.mode}-${selection.widgetType ?? 'none'}-${selection.mode === 'edit' ? selection.dashboardItemId : 'new'}`;
        }
        if (selection.kind === 'title') {
            return `title-${selection.mode}-${selection.mode === 'edit' ? selection.dashboardTitolId : 'new'}`;
        }
        return 'none';
    }, [selection]);

    const handleSave = async () => {
        if (selection.kind === 'none') {
            return;
        }
        setSaving(true);
        try {
            if (selection.kind === 'widget') {
                if (!selection.widgetType) {
                    temporalMessageShow(null, 'Seleccionau el tipus de widget', 'warning');
                    return;
                }
                const savedWidget = await widgetFormApiRef.current?.save();
                if (selection.mode === 'create') {
                    const dashboardItemData = dashboardItemFormApiRef.current?.getData() ?? {};
                    await createDashboardItem({
                        data: {
                            ...dashboardItemData,
                            dashboard: { id: dashboardId },
                            widget: { id: savedWidget?.id },
                            entornId: selection.entornId ?? dashboard?.entorn?.id,
                        },
                    });
                } else {
                    await dashboardItemFormApiRef.current?.save();
                }
            } else if (selection.kind === 'title') {
                await titleFormApiRef.current?.save();
            }
            onSaved();
        } catch (error: any) {
            if (error?.message) {
                temporalMessageShow(null, error.message, 'error');
            }
        } finally {
            setSaving(false);
        }
    };

    const confirmDelete = (action: () => Promise<void>) => {
        messageDialogShow(
            'Confirmació',
            'Estau segur que voleu esborrar aquest element del dashboard?',
            confirmDialogButtons,
            { maxWidth: 'sm', fullWidth: true }
        ).then((value: any) => {
            if (!value) {
                return;
            }
            action()
                .then(() => {
                    temporalMessageShow(null, 'Element eliminat', 'success');
                    onDeleted();
                })
                .catch((error: any) => {
                    temporalMessageShow(null, error?.message ?? 'No s’ha pogut eliminar', 'error');
                });
        });
    };

    const handleDelete = () => {
        if (selection.kind === 'widget' && selection.mode === 'edit') {
            confirmDelete(() => deleteDashboardItem(selection.dashboardItemId));
        } else if (selection.kind === 'title' && selection.mode === 'edit') {
            confirmDelete(() => deleteDashboardTitol(selection.dashboardTitolId));
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
                    Propietats
                </Typography>
            </Box>
            <Divider />
            <Box sx={{ flex: 1, minHeight: 0, overflow: 'auto', p: 2 }}>
                {selection.kind === 'none' ? (
                    <EmptyPanel />
                ) : selection.kind === 'widget' ? (
                    <WidgetEditor
                        key={selectionKey}
                        dashboard={dashboard}
                        dashboardId={dashboardId}
                        selection={selection}
                        widgetFormApiRef={widgetFormApiRef}
                        dashboardItemFormApiRef={dashboardItemFormApiRef}
                        onSelectionChange={onSelectionChange}
                    />
                ) : (
                    <TitleEditor
                        key={selectionKey}
                        dashboard={dashboard}
                        dashboardId={dashboardId}
                        selection={selection}
                        titleFormApiRef={titleFormApiRef}
                    />
                )}
            </Box>
            <Divider />
            <Box sx={{ p: 1.5, display: 'flex', gap: 1, justifyContent: 'space-between' }}>
                <Button
                    variant="outlined"
                    color="error"
                    startIcon={<Icon>delete</Icon>}
                    onClick={handleDelete}
                    disabled={selection.kind === 'none' || saving}
                >
                    Eliminar
                </Button>
                <Button
                    variant="contained"
                    startIcon={<Icon>save</Icon>}
                    onClick={handleSave}
                    disabled={selection.kind === 'none' || saving}
                >
                    Desar
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
};

const WidgetEditor: React.FC<WidgetEditorProps> = ({
    dashboard,
    dashboardId,
    selection,
    widgetFormApiRef,
    dashboardItemFormApiRef,
    onSelectionChange,
}) => {
    const widgetType = selection.widgetType;
    const config = widgetType ? widgetTypeConfig[widgetType] : undefined;
    const FormComponent = config?.FormComponent;
    const [isPersonalitzat, setIsPersonalitzat] = React.useState(false);
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
                    <Stack spacing={2}>
                        <PanelSection title="Configuració estadística">
                            <FormComponent mode="stats" />
                        </PanelSection>
                        <PanelSection title="Configuració gràfica">
                            <MuiForm
                                resourceName="dashboardItem"
                                id={selection.mode === 'edit' ? selection.dashboardItemId : undefined}
                                additionalData={
                                    selection.mode === 'create'
                                        ? {
                                              dashboard: { id: dashboardId },
                                              entornId: selection.entornId ?? dashboard?.entorn?.id,
                                              ...defaultDashboardItemData,
                                          }
                                        : undefined
                                }
                                apiRef={dashboardItemFormApiRef}
                                hiddenToolbar
                                formBlockerDisabled
                                componentProps={{ sx: { m: 0, mt: 0 } }}
                            >
                                <PersonalitzatBridge onChange={setIsPersonalitzat} />
                                <DashboardItemFormFields />
                            </MuiForm>
                            {isPersonalitzat && (
                                <>
                                    <Divider sx={{ my: 1.5 }} />
                                    <FormComponent mode="visual" />
                                </>
                            )}
                        </PanelSection>
                    </Stack>
                </MuiForm>
            ) : (
                <Typography variant="body2" color="text.secondary">
                    Seleccionau un tipus per veure les propietats configurables.
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
};

const TitleEditor: React.FC<TitleEditorProps> = ({
    dashboard,
    dashboardId,
    selection,
    titleFormApiRef,
}) => (
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
        <DashboardTitleFields />
    </MuiForm>
);

export default DashboardEditorSidePanel;
