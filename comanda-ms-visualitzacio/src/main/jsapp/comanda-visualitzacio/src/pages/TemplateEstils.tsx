import * as React from 'react';
import { useTranslation } from 'react-i18next';
import {
    Box,
    Divider,
    Grid,
    Paper,
    Tab,
    Tabs,
    Typography,
    Stack,
    IconButton,
    Tooltip,
} from '@mui/material';
import {
    GridPage,
    MuiDataGrid,
    FormField,
    useFormContext,
    MuiForm,
} from 'reactlib';
import ColorPaletteSelector from '../components/ColorPaletteSelector';
import VisualAttributesPanel from '../components/estadistiques/VisualAttributesPanel';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';

const CoolorsPalette: React.FC<{
    name: string;
    label: string;
    colors: string;
    onChange: (newColors: string) => void;
}> = ({ label, colors, onChange }) => {
    const handlePaletteChange = (newPalette: string[]) => {
        onChange(newPalette.join(','));
    };

    const handleCopy = () => {
        navigator.clipboard.writeText(colors);
    };

    return (
        <Box sx={{ mb: 3 }}>
            <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
                <Typography variant="subtitle1" fontWeight="bold">{label}</Typography>
                <Tooltip title="Copiar colors">
                    <IconButton size="small" onClick={handleCopy}>
                        <ContentCopyIcon fontSize="small" />
                    </IconButton>
                </Tooltip>
            </Stack>
            <Box 
                sx={{ 
                    display: 'flex', 
                    height: 80, 
                    borderRadius: 2, 
                    overflow: 'hidden', 
                    mb: 1,
                    border: '1px solid #ccc'
                }}
            >
                {colors.split(',').filter(c => c).map((color, idx) => (
                    <Box 
                        key={`${color}-${idx}`}
                        sx={{ 
                            flex: 1, 
                            bgcolor: color, 
                            display: 'flex', 
                            alignItems: 'center', 
                            justifyContent: 'center',
                            transition: 'flex 0.2s',
                            '&:hover': {
                                flex: 1.5,
                            }
                        }}
                    >
                        <Typography 
                            variant="caption" 
                            sx={{ 
                                color: (theme) => theme.palette.getContrastText(color),
                                fontWeight: 'bold',
                                opacity: 0,
                                transition: 'opacity 0.2s',
                                '.MuiBox-root:hover &': {
                                    opacity: 1
                                }
                            }}
                        >
                            {color.toUpperCase()}
                        </Typography>
                    </Box>
                ))}
            </Box>
            <ColorPaletteSelector initialColors={colors} onPaletteChange={handlePaletteChange} />
        </Box>
    );
};

const TemplateEstilsForm: React.FC = () => {
    const { data, apiRef } = useFormContext();
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);

    const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };

    const updateEstilsJson = (type: 'simple' | 'grafic' | 'taula', newData: any) => {
        let currentEstils = {};
        try {
            currentEstils = data.estilsDefaultJson ? JSON.parse(data.estilsDefaultJson) : {};
        } catch (e) {
            console.error("Error parsing estilsDefaultJson", e);
        }
        
        // Atès que el merge al backend és per camps, podem barrejar tots els atributs en un únic objecte JSON.
        const updatedEstils = { ...currentEstils, ...newData };
        apiRef.current?.setFieldValue('estilsDefaultJson', JSON.stringify(updatedEstils));
    };

    const getInitialEstils = () => {
        try {
            return data.estilsDefaultJson ? JSON.parse(data.estilsDefaultJson) : {};
        } catch (e) {
            return {};
        }
    };

    return (
        <Box sx={{ width: '100%' }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={tab} onChange={handleTabChange} variant="scrollable" scrollButtons="auto">
                    <Tab label="General" />
                    <Tab label="Paletes de Colors" />
                    <Tab label="Estils Simple" />
                    <Tab label="Estils Gràfic" />
                    <Tab label="Estils Taula" />
                </Tabs>
            </Box>

            <Box sx={{ p: 3 }}>
                {tab === 0 && (
                    <Grid container spacing={2}>
                        <Grid size={12}>
                            <FormField name="nom" label="Nom de la plantilla" />
                        </Grid>
                        <Grid size={12}>
                            <Typography variant="h6" sx={{ mt: 2 }}>Colors Destacats</Typography>
                            <Divider sx={{ mb: 2 }} />
                        </Grid>
                        <Grid size={{ xs: 12, md: 6 }}>
                            <CoolorsPalette 
                                name="destacatsClar" 
                                label="Destacats Clar" 
                                colors={data.destacatsClar || ''} 
                                onChange={(val) => apiRef.current?.setFieldValue('destacatsClar', val)}
                            />
                        </Grid>
                        <Grid size={{ xs: 12, md: 6 }}>
                            <CoolorsPalette 
                                name="destacatsFosc" 
                                label="Destacats Fosc" 
                                colors={data.destacatsFosc || ''} 
                                onChange={(val) => apiRef.current?.setFieldValue('destacatsFosc', val)}
                            />
                        </Grid>
                    </Grid>
                )}

                {tab === 1 && (
                    <Grid container spacing={2}>
                        <Grid size={{ xs: 12, md: 6 }}>
                            <CoolorsPalette 
                                name="colorsClar" 
                                label="Paleta Tema Clar" 
                                colors={data.colorsClar || ''} 
                                onChange={(val) => apiRef.current?.setFieldValue('colorsClar', val)}
                            />
                        </Grid>
                        <Grid size={{ xs: 12, md: 6 }}>
                            <CoolorsPalette 
                                name="colorsFosc" 
                                label="Paleta Tema Fosc" 
                                colors={data.colorsFosc || ''} 
                                onChange={(val) => apiRef.current?.setFieldValue('colorsFosc', val)}
                            />
                        </Grid>
                    </Grid>
                )}

                {(tab === 2 || tab === 3 || tab === 4) && (
                    <Box>
                        <Typography variant="body2" sx={{ mb: 2, fontStyle: 'italic' }}>
                            Configureu els estils que s'aplicaran per defecte als widgets d'aquest tipus que utilitzin aquesta plantilla.
                        </Typography>
                        <MuiForm
                            resourceName="atributsVisuals" // Nom genèric per al formulari intern
                            initialData={{ atributsVisuals: getInitialEstils() }}
                            onDataChange={(newData) => updateEstilsJson(
                                tab === 2 ? 'simple' : (tab === 3 ? 'grafic' : 'taula'),
                                newData.atributsVisuals
                            )}
                        >
                            <VisualAttributesPanel 
                                widgetType={tab === 2 ? 'simple' : (tab === 3 ? 'grafic' : 'taula')} 
                                title={`Atributs per defecte (${tab === 2 ? 'Simple' : (tab === 3 ? 'Gràfic' : 'Taula')})`}
                            />
                        </MuiForm>
                    </Box>
                )}
            </Box>
        </Box>
    );
};

const TemplateEstils: React.FC = () => {
    const { t } = useTranslation();
    const columns = [
        { field: 'nom', flex: 1 },
    ];

    return (
        <GridPage>
            <MuiDataGrid
                title="Plantilles d'estils"
                resourceName="templateEstils"
                columns={columns}
                paginationActive
                popupEditActive
                popupEditFormContent={<TemplateEstilsForm />}
                popupEditFormComponentProps={{
                    maxWidth: 'lg',
                    fullWidth: true
                }}
            />
        </GridPage>
    );
};

export default TemplateEstils;
