import * as React from 'react';
import {
    Box,
    Typography,
    Divider,
    Grid,
    Tab,
    Tabs,
} from '@mui/material';
import { FormField, MuiForm } from 'reactlib';
import { useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import EstadisticaSimpleWidgetForm from './EstadisticaSimpleWidgetForm.tsx';
import EstadisticaGraficWidgetForm from './EstadisticaGraficWidgetForm.tsx';
import EstadisticaTaulaWidgetForm from './EstadisticaTaulaWidgetForm.tsx';
import SimpleWidgetVisualization from './SimpleWidgetVisualization.tsx';
import GraficWidgetVisualization from './GraficWidgetVisualization.tsx';
import TaulaWidgetVisualization from './TaulaWidgetVisualization.tsx';

type DashboardRightBarProps = {
    selectedItem?: any;
    selectedItemType?: 'WIDGET' | 'TITOL';
    onUpdate: (data: any) => void;
    templates: any[];
};

const DashboardRightBar: React.FC<DashboardRightBarProps> = ({
    selectedItem,
    selectedItemType,
    onUpdate,
    templates,
}) => {
    const { t } = useTranslation();
    const [tab, setTab] = useState(0);

    const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };

    if (!selectedItem) {
        return (
            <Box sx={{ width: 300, borderLeft: 1, borderColor: 'divider', p: 2, bgcolor: 'background.paper' }}>
                <Typography variant="body1">Selecciona un element per configurar-lo</Typography>
            </Box>
        );
    }

    const widget = selectedItem.widget;
    const widgetType = selectedItem.tipus; // SIMPLE, GRAFIC, TAULA

    return (
        <Box sx={{ width: 300, borderLeft: 1, borderColor: 'divider', height: '100%', overflowY: 'auto', bgcolor: 'background.paper' }}>
            <Box sx={{ p: 2, pb: 0 }}>
                <Typography variant="h6" gutterBottom>
                    {selectedItemType === 'TITOL' ? 'Configuració Títol' : `Configuració ${widgetType}`}
                </Typography>
                <Tabs value={tab} onChange={handleTabChange} variant="fullWidth" sx={{ borderBottom: 1, borderColor: 'divider' }}>
                    <Tab label="Dades" />
                    <Tab label="Estils" />
                </Tabs>
            </Box>

            <Box sx={{ p: 2 }}>
                {tab === 0 && (
                    <MuiForm
                        resourceName={selectedItemType === 'TITOL' ? 'dashboardTitol' : 
                            (widgetType === 'SIMPLE' ? 'estadisticaSimpleWidget' : 
                            (widgetType === 'GRAFIC' ? 'estadisticaGraficWidget' : 'estadisticaTaulaWidget'))}
                        initialData={selectedItemType === 'TITOL' ? selectedItem : widget}
                        onDataChange={(newData) => {
                            if (selectedItemType === 'TITOL') {
                                onUpdate(newData);
                            } else {
                                onUpdate({ ...selectedItem, widget: newData });
                            }
                        }}
                    >
                        {selectedItemType === 'TITOL' ? (
                            <Grid container spacing={2}>
                                <Grid item xs={12}><FormField name="titol" label="Títol" /></Grid>
                                <Grid item xs={12}><FormField name="subtitol" label="Subtítol" /></Grid>
                            </Grid>
                        ) : (
                            <>
                                {widgetType === 'SIMPLE' && <EstadisticaSimpleWidgetForm />}
                                {widgetType === 'GRAFIC' && <EstadisticaGraficWidgetForm />}
                                {widgetType === 'TAULA' && <EstadisticaTaulaWidgetForm />}
                            </>
                        )}
                    </MuiForm>
                )}

                {tab === 1 && (
                    <MuiForm
                        resourceName={selectedItemType === 'TITOL' ? 'dashboardTitol' : 'dashboardItem'}
                        initialData={selectedItem}
                        onDataChange={onUpdate}
                    >
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <Typography variant="subtitle2" gutterBottom>Previsualització</Typography>
                                <Box sx={{ height: 150, mb: 2, border: '1px dashed #ccc', display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
                                    {selectedItemType === 'WIDGET' && (
                                        <>
                                            {widgetType === 'SIMPLE' && <SimpleWidgetVisualization preview {...widget} {...selectedItem.atributsVisuals} />}
                                            {widgetType === 'GRAFIC' && <GraficWidgetVisualization preview {...widget} {...selectedItem.atributsVisuals} />}
                                            {widgetType === 'TAULA' && <TaulaWidgetVisualization preview {...widget} {...selectedItem.atributsVisuals} />}
                                        </>
                                    )}
                                    {selectedItemType === 'TITOL' && (
                                        <Typography>{selectedItem.titol}</Typography>
                                    )}
                                </Box>
                            </Grid>
                            
                            <Grid item xs={12}>
                                <FormField name="templateId" label="Plantilla" type="select" options={templates.map(t => ({ value: t.id, label: t.nom }))} />
                            </Grid>
                            
                            <Grid item xs={12}>
                                <FormField name="estilsCustom" label="Estils personalitzats" type="checkbox" />
                            </Grid>
                            
                            {selectedItem.estilsCustom && (
                                <Grid item xs={12}>
                                     <Typography variant="subtitle2" sx={{ mt: 1 }}>Atributs Visuals Custom</Typography>
                                     <FormField name="atributsVisuals.colorFons" label="Color Fons" type="color" />
                                     <FormField name="atributsVisuals.colorText" label="Color Text" type="color" />
                                     <FormField name="atributsVisuals.colorVora" label="Color Vora" type="color" />
                                     <FormField name="atributsVisuals.ampleVora" label="Ample Vora" type="number" />
                                     <FormField name="atributsVisuals.mostrarVora" label="Mostrar Vora" type="checkbox" />
                                     
                                     {selectedItemType === 'WIDGET' && (
                                         <>
                                             <Typography variant="subtitle2" sx={{ mt: 2 }}>Estils Destacats</Typography>
                                             <FormField name="templateHighlight" label="Utilitzar colors destacats de la plantilla" type="checkbox" />
                                             <Divider sx={{ my: 2 }} />
                                             <FormField name="atributsVisuals.midaFontTitol" label="Mida Font Títol" type="number" />
                                             <FormField name="atributsVisuals.midaFontDescripcio" label="Mida Font Descripció" type="number" />
                                         </>
                                     )}
                                </Grid>
                            )}
                        </Grid>
                    </MuiForm>
                )}
            </Box>
        </Box>
    );
};

export default DashboardRightBar;
