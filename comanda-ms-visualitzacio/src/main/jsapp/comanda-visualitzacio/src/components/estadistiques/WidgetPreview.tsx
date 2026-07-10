import React from 'react';
import { useDashboardPlantilla } from './dashboardPlantillaHook';
import { resolveWidgetStyles } from './dashboardStyleResolver';
import SimpleWidgetVisualization, { SimpleWidgetVisualizationProps } from './SimpleWidgetVisualization';
import GraficWidgetVisualization, { GraficWidgetVisualizationProps } from './GraficWidgetVisualization';
import TaulaWidgetVisualization, { TaulaWidgetVisualizationProps } from './TaulaWidgetVisualization';
import TitolWidgetVisualization, { TitolWidgetVisualizationProps } from './TitolWidgetVisualization';
import { CircularProgress, Box } from '@mui/material';
import { useTheme } from '@mui/material/styles';

type WidgetPreviewType = 'SIMPLE' | 'GRAFIC' | 'TAULA' | 'TITOL';

interface WidgetPreviewProps {
    widgetType: WidgetPreviewType;
    widgetData: any;
    dashboardPlantilla?: any;
}

export const WidgetPreview: React.FC<WidgetPreviewProps> = ({
    widgetType,
    widgetData,
    dashboardPlantilla,
}) => {
    const theme = useTheme();
    const temaFosc = theme.palette.mode === 'dark';
    const { plantilla: dashboardItemPlantilla, loading: loadingPlantilla } = useDashboardPlantilla(widgetData?.plantilla?.id);
    if (loadingPlantilla) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <CircularProgress size={24} />
            </Box>
        );
    }
    const plantillaToUse = dashboardItemPlantilla || dashboardPlantilla;
    const resolvedStyles = resolveWidgetStyles(widgetData, widgetType, plantillaToUse, temaFosc);
    const finalData = {
        ...widgetData,
        ...resolvedStyles,
        preview: true
    };
    return renderWidget(widgetType, finalData);
};

const renderWidget = (widgetType: WidgetPreviewType, props: any) => {
    switch (widgetType) {
        case 'SIMPLE':
            return <SimpleWidgetVisualization {...(props as SimpleWidgetVisualizationProps)} />;
        case 'GRAFIC':
            return <GraficWidgetVisualization {...(props as GraficWidgetVisualizationProps)} />;
        case 'TAULA':
            return <TaulaWidgetVisualization {...(props as TaulaWidgetVisualizationProps)} />;
        case 'TITOL':
            return <TitolWidgetVisualization {...(props as TitolWidgetVisualizationProps)} />;
        default:
            return null;
    }
};