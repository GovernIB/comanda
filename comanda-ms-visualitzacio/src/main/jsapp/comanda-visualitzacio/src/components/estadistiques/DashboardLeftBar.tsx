import * as React from 'react';
import {
    Box,
    Button,
    Typography,
    List,
    ListItem,
    ListItemButton,
    ListItemIcon,
    ListItemText,
    Divider,
    Icon,
    Accordion,
    AccordionSummary,
    AccordionDetails,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { useTranslation } from 'react-i18next';
import { useResourceApiService } from 'reactlib';
import { useEffect, useState } from 'react';

type DashboardLeftBarProps = {
    dashboardWidgets: any[];
    onSelectWidget: (widgetId: any, type: 'WIDGET' | 'TITOL') => void;
    onAddWidget: (type: 'SIMPLE' | 'GRAFIC' | 'TAULA') => void;
    availableWidgets: any[];
    onAddExistingWidget: (widgetId: any) => void;
    selectedItemId?: any;
};

const DashboardLeftBar: React.FC<DashboardLeftBarProps> = ({
    dashboardWidgets,
    onSelectWidget,
    onAddWidget,
    availableWidgets,
    onAddExistingWidget,
    selectedItemId,
}) => {
    const { t } = useTranslation();

    const widgetsByType = dashboardWidgets.reduce((acc: any, item: any) => {
        const type = item.type || 'WIDGET';
        if (!acc[type]) acc[type] = [];
        acc[type].push(item);
        return acc;
    }, {});

    return (
        <Box sx={{ width: 300, borderRight: 1, borderColor: 'divider', height: '100%', overflowY: 'auto', bgcolor: 'background.paper' }}>
            <Box sx={{ p: 2 }}>
                <Typography variant="h6" gutterBottom>Nou Widget</Typography>
                <Box sx={{ display: 'flex', justifyContent: 'space-around', mb: 2 }}>
                    <Button onClick={() => onAddWidget('SIMPLE')} title="Simple" variant="outlined">
                        <Icon>description</Icon>
                    </Button>
                    <Button onClick={() => onAddWidget('GRAFIC')} title="Gràfic" variant="outlined">
                        <Icon>bar_chart</Icon>
                    </Button>
                    <Button onClick={() => onAddWidget('TAULA')} title="Taula" variant="outlined">
                        <Icon>table_chart</Icon>
                    </Button>
                </Box>
            </Box>
            
            <Divider />

            <Box sx={{ p: 2 }}>
                <Typography variant="subtitle1" gutterBottom>Widgets al Dashboard</Typography>
                {Object.keys(widgetsByType).map(type => (
                    <Accordion key={type} defaultExpanded size="small" sx={{ mb: 1 }}>
                        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                            <Typography variant="body2">{type}</Typography>
                        </AccordionSummary>
                        <AccordionDetails sx={{ p: 0 }}>
                            <List dense>
                                {widgetsByType[type].map((item: any) => (
                                    <ListItem key={item.id} disablePadding>
                                        <ListItemButton 
                                            selected={selectedItemId === item.id}
                                            onClick={() => onSelectWidget(item.id, type as any)}
                                        >
                                            <ListItemText primary={item.titol || item.id} />
                                        </ListItemButton>
                                    </ListItem>
                                ))}
                            </List>
                        </AccordionDetails>
                    </Accordion>
                ))}
            </Box>

            <Divider />

            <Box sx={{ p: 2 }}>
                <Typography variant="subtitle1" gutterBottom>Widgets Disponibles</Typography>
                <List dense>
                    {availableWidgets.map((widget: any) => (
                        <ListItem key={widget.id} disablePadding>
                            <ListItemButton onClick={() => onAddExistingWidget(widget.id)}>
                                <ListItemIcon><Icon>add</Icon></ListItemIcon>
                                <ListItemText primary={widget.titol} secondary={widget.aplicacio?.nom} />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>
            </Box>
        </Box>
    );
};

export default DashboardLeftBar;
