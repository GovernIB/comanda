import {Box, Chip, Grid, IconButton, Paper, Stack, Tab, Tabs, TextField, Typography} from "@mui/material";
import {FormField, MuiDataGrid, useFormContext} from "reactlib";
import Card from "@mui/material/Card";
import CardHeader from "@mui/material/CardHeader";
import CardContent from "@mui/material/CardContent";
import SimpleWidgetVisualization from "../components/estadistiques/SimpleWidgetVisualization.tsx";
import {useCallback, useMemo, useState} from "react";
import * as React from "react";
import {useTranslation} from "react-i18next";
import {useTheme} from "@mui/material/styles";
import IconAutocompleteSelect from "../components/IconAutocompleteSelect.tsx";
import Button from "@mui/material/Button";
import EditIcon from "@mui/icons-material/Edit";
import TaulaWidgetVisualization from "../components/estadistiques/TaulaWidgetVisualization.tsx";
import GraficWidgetVisualization from "../components/estadistiques/GraficWidgetVisualization.tsx";
import InputAdornment from "@mui/material/InputAdornment";
import Icon from "@mui/material/Icon";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import DeleteIcon from "@mui/icons-material/Delete";

const ColorPaletteSelector = ({ value, onChange }: { value: string[], onChange: (newPalette: string[]) => void }) => {
    const { t } = useTranslation();
    const [newColor, setNewColor] = useState('#000000'); // Valor per defecte negre per al nou color

    // Maneja l'afegit d'un nou color a la paleta
    const handleAddColor = useCallback(() => {
        // Evita duplicats si ja hi ha un color igual (opcional)
        if (!value.includes(newColor)) {
            const updatedColors = [...value, newColor];
            onChange(updatedColors);
            setNewColor('#000000'); // Reseteja el selector de color per al següent afegit
        } else {
            alert(t($ => $.page.widget.editorPaleta.exist)); // O mostra un missatge més amigable
        }
    }, [t, value, newColor, onChange]);

    // Maneja l'eliminació d'un color de la paleta
    const handleDeleteColor = useCallback( (colorToDelete: string) => {
        const updatedColors = value.filter((color) => color !== colorToDelete);
        onChange(updatedColors);
    }, [value, onChange]);

    const fileInputRef = React.useRef<HTMLInputElement>(undefined);
    const endAdornment = <>
        <InputAdornment position="end">
            <IconButton onClick={() => (fileInputRef.current as any)?.querySelector('input').click()} size="small">
                <Icon fontSize="small">palette</Icon>
            </IconButton>
        </InputAdornment>
    </>;
    const inputProps = {
        endAdornment,
        ref: fileInputRef,
    };

    // Renderitzat del component
    return (
        <Box sx={{ py: 1, px: 2, border: '1px solid #ccc', borderRadius: 2, bgcolor: 'background.paper' }}>
            <Typography variant="subtitle2" gutterBottom>{t($ => $.page.widget.editorPaleta.title)}</Typography>
            {/* Secció per afegir nous colors */}
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
                <TextField
                    type="color"
                    label={t($ => $.page.widget.editorPaleta.color)}
                    value={newColor}
                    onChange={(e) => setNewColor(e.target.value)}
                    fullWidth
                    sx={{ width: 200, height: 40,
                        '& input': { opacity: newColor ? undefined : '0' },}} // Estils per fer l'input de color més compacte
                    inputProps={{ style: { padding: 8, border: 'none' } }}
                    slotProps={{
                        input: inputProps,
                    }}
                />
                <TextField
                    type="text"
                    value={newColor}
                    onChange={(e) => setNewColor(e.target.value)}
                    size="small"
                    label={t($ => $.page.widget.editorPaleta.hex)}
                    sx={{ flexGrow: 1 }}
                />
                <Button
                    variant="contained"
                    onClick={handleAddColor}
                    startIcon={<AddCircleOutlineIcon />}
                    disabled={!newColor || value.includes(newColor)} // Deshabilita si el color està buit o duplicat
                >
                    {t($ => $.page.widget.action.add.label)}
                </Button>
            </Stack>
            {/* Visualització de la paleta de colors actual */}
            <Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>
                {t($ => $.page.widget.editorPaleta.palet)}
            </Typography>
            {value.length === 0 ? (
                <Typography variant="body2" color="text.secondary">
                    {t($ => $.page.widget.editorPaleta.empty)}
                </Typography>
            ) : (
                <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                    {value.map((color: string) => (
                        <Chip
                            key={color} // Utilitzem el color com a key (assumint que són únics)
                            label={color.toUpperCase()}
                            sx={{
                                bgcolor: color,
                                fontSize: '0.75rem',
                                color: (theme) => theme.palette.getContrastText(color),
                                border: color.toLowerCase() === '#ffffff' ? '1px solid #ccc' : 'none',
                                '& .MuiChip-deleteIcon': {
                                    color: (theme) => theme.palette.getContrastText(color),
                                    opacity: 0.5,
                                },
                                '& .MuiChip-deleteIcon:hover': {
                                    color: (theme) => theme.palette.getContrastText(color),
                                    opacity: 0.75,
                                },
                            }}
                            deleteIcon={<DeleteIcon />}
                            onDelete={() => handleDeleteColor(color)}
                        />
                    ))}
                </Stack>
            )}
        </Box>
    );
}

const PaletaField = (props:any) => {
    const {name:key, ...other} = props
    const {data, apiRef} = useFormContext()

    return <FormField
        name={"colors" + (data?.colors?.[key] ?`#${key}`:'')}
        value={data?.colors?.[key]}
        // field={fieldTipusDocument}
        type={'color'}
        onChange={(value)=>{
            apiRef?.current?.setFieldValue('colors', {
                ...data?.colors,
                [key]: value,
            })
        }}
        required
        {...other}
    />
}
const ColorField = (props:any) => {
    const {name:key, ...other} = props
    const {data} = useFormContext()

    const colorKey = `${key}_${data?.paleta}`;

    return <PaletaField
        name={colorKey}
        required={false}
        {...other}
    />
}

const PaleteCard = (props:any) => {
    const { t } = useTranslation();
    const {title, cardKey} = props
    const {data, apiRef} = useFormContext()

    const colorKey = 'colorText_' + cardKey;
    const backKey = 'colorFons_' + cardKey;
    const selected = cardKey == data?.paleta

    const color = data?.colors?.[colorKey]
    const backgroundColor = data?.colors?.[backKey]

    return <Card
        onClick={() => apiRef?.current?.setFieldValue('paleta', cardKey) }
        sx={{
            width: '100%',
            color: color,
            backgroundColor: backgroundColor,
            border: `2px solid ${selected ? '#0070f3' : 'undefined'}`,
        }}>
        <CardHeader title={title}/>
        <CardContent>
            <Grid container columnSpacing={1} rowSpacing={1}>
                <PaletaField
                    name={colorKey}
                    label={t($ => $.page.widget.atributsVisuals.colorText)}
                    componentProps={{
                        sx: {
                            '& .MuiInputAdornment-positionEnd .MuiIcon-root, & .MuiInputAdornment-positionEnd .MuiSvgIcon-root': {
                                color: color,
                            },
                            '& .MuiOutlinedInput-root': {
                                '& fieldset': { borderColor: color },
                                '&:hover fieldset': { borderColor: color },
                            },
                            '& .MuiInputLabel-root': {
                                color: color,
                            },
                        },
                    }}
                />
                <PaletaField
                    name={backKey}
                    label={t($ => $.page.widget.atributsVisuals.colorFons)}
                    componentProps={{
                        sx: {
                            '& .MuiInputAdornment-positionEnd .MuiIcon-root, & .MuiInputAdornment-positionEnd .MuiSvgIcon-root': {
                                color: color,
                            },
                            '& .MuiOutlinedInput-root': {
                                '& fieldset': { borderColor: color },
                                '&:hover fieldset': { borderColor: color },
                            },
                            '& .MuiInputLabel-root': {
                                color: color,
                            },
                        },
                    }}
                />
            </Grid>
        </CardContent>
    </Card>
}

const PlantillaForm = () => {
    const { t } = useTranslation();
    const [tab, setTab] = React.useState(0);
    const handleChange = (_event: React.SyntheticEvent, newValue: number) => {
        setTab(newValue);
    };

    const paletes: any[] = [
        {
            title: "Tema clar",
            key: "clar",
        },
        {
            title: "Destacat clar",
            key: "dest",
        },
        {
            title: "Tema fosc",
            key: "obs",
        },
        {
            title: "Destacat fosc",
            key: "obs_dest",
        },
    ]

    return <>
        <Grid container columnSpacing={1} rowSpacing={1}>
            <Grid size={12}><FormField name={'nom'} required/></Grid>

            {paletes.map((paleta) => <Grid container size={3}>
                <PaleteCard title={paleta?.title} cardKey={paleta?.key}/>
            </Grid>)}

            <Grid size={12} container columnSpacing={1} rowSpacing={1} mt={1}>
                <Grid container size={2.5}>
                    <CommonTab/>
                </Grid>
                <Grid container size={9.5}>
                    <Tabs value={tab} onChange={handleChange}>
                        {/*<Tab label={'common'} />*/}
                        <Tab label={t($ => $.page.widget.simple.tab.title)} />
                        <Tab label={t($ => $.page.widget.grafic.tab.title)} />
                        <Tab label={t($ => $.page.widget.taula.tab.title)} />
                    </Tabs>
                    <Box>
                        {/*{tab === 0 && (<CommonTab/>)}*/}
                        {tab === 0 && (<WidgetSimpleTab/>)}
                        {tab === 1 && (<WidgetGraficTab/>)}
                        {tab === 2 && (<WidgetTaulaTab/>)}
                    </Box>
                </Grid>
            </Grid>
        </Grid>
    </>
}

const columns = [
    {
        field: 'nom',
        flex: 1,
    },
]

export const Plantilla = () => {
    return (
        <Box sx={{ height: '100%' }}>
            <MuiDataGrid
                resourceName={'plantilla'}
                columns={columns}
                toolbarType="upper"
                popupEditCreateActive
                popupEditActive
                popupEditFormContent={<PlantillaForm/>}
                popupEditFormDialogComponentProps={{ fullWidth: true, maxWidth: 'xl' }}
                formAdditionalData={(row:any) => ({
                    ...(!row?.id && {
                        colors: {
                            ['colorText_clar']: '#ef6c00',
                            ['colorFons_clar']: 'white',
                            ['colorText_dest']: 'white',
                            ['colorFons_dest']: '#ef6c00',
                            ['colorText_obs']: 'white',
                            ['colorFons_obs']: 'grey',
                            ['colorText_obs_dest']: 'white',
                            ['colorFons_obs_dest']: 'darkgrey',
                        }
                    }),
                    tipusGrafic: 'BAR_CHART',
                    paleta: 'clar',
                })}
            />
        </Box>
    )
}

const CommonTab = () => {
    const { t } = useTranslation();
    const {data} = useFormContext()
    const theme = useTheme();

    return <Paper
        elevation={1}
        sx={{
            width: '100%',
            height: '100%',
            overflow: 'hidden',
            border: `1px solid ${theme.palette.divider}`,
            position: 'relative',
        }}
    >
        <Grid container columnSpacing={1} rowSpacing={1} p={1}>
            <Grid size={12}>
                <Typography variant="subtitle2" sx={{ mt: 1 }}>
                    {t($ => $.page.widget.form.configVisual)}
                </Typography>
            </Grid>
            <Grid size={12}>
                <FormField
                    name="mostrarVora"
                    label={t($ => $.page.widget.atributsVisuals.mostrarVora)}
                    type="checkbox"
                />
            </Grid>
            {data?.mostrarVora && (
                <>
                    <Grid size={12}>
                        <ColorField
                            name="colorVora"
                            label={t($ => $.page.widget.atributsVisuals.colorVora)}
                        />
                    </Grid>
                    <Grid size={12}>
                        <FormField
                            name="ampleVora"
                            label={t($ => $.page.widget.atributsVisuals.ampleVora)}
                            type="number"
                            required={false}
                        />
                    </Grid>
                </>
            )}
            <Grid size={12}><Typography variant="subtitle2" sx={{ mt: 1 }}>{t($ => $.page.widget.form.configFont)}</Typography></Grid>
            <Grid size={12}>
                <FormField
                    name="midaFontTitol"
                    label={t($ => $.page.widget.atributsVisuals.midaFontTitol)}
                    type="number"
                    required={false}
                />
            </Grid>
            <Grid size={12}>
                <FormField
                    name="midaFontDescripcio"
                    label={t($ => $.page.widget.atributsVisuals.midaFontDescripcio)}
                    type="number"
                    required={false}
                />
            </Grid>
        </Grid>
    </Paper>
}

const WidgetSimpleTab = () => {
    const { t } = useTranslation();
    const {data} = useFormContext()
    const theme = useTheme();

    const getColor = (key:string) => {
        const colorKey = `${key}_${data?.paleta}`;
        return data?.colors?.[colorKey] || data?.colors?.[key] || undefined
    }

    const previewData = useMemo(() => ({
        ...data,
        colorVora: getColor('colorVora'),
        colorIcona: getColor('colorIcona'),
        colorFonsIcona: getColor('colorFonsIcona'),
        colorTextDestacat: getColor('colorTextDestacat'),
        colorText: getColor('colorText'),
        colorFons: getColor('colorFons'),
    }), [data, getColor]);

    return <Grid container columnSpacing={1} rowSpacing={1}>
        <Grid size={8}>
            <Paper
                elevation={1}
                sx={{
                    width: '100%',
                    height: '100%',
                    overflow: 'hidden',
                    border: `1px solid ${theme.palette.divider}`,
                    position: 'relative',
                }}
            >
                <Grid container columnSpacing={1} rowSpacing={1} p={1}>
                    <Grid size={12}>
                        <Typography variant="subtitle2">
                            {t($ => $.page.widget.form.configGeneral)}
                        </Typography>
                    </Grid>
                    <Grid size={12}>
                        <IconAutocompleteSelect
                            name="icona"
                            label={t($ => $.page.widget.atributsVisuals.icona)}
                        />
                    </Grid>
                    {data?.icona && (
                        <>
                            <Grid size={6}>
                                <ColorField
                                    name={'colorIcona'}
                                    label={t($ => $.page.widget.atributsVisuals.colorIcona)}
                                />
                            </Grid>
                            <Grid size={6}>
                                <ColorField
                                    name="colorFonsIcona"
                                    label={t($ => $.page.widget.atributsVisuals.colorFonsIcona)}
                                />
                            </Grid>
                        </>
                    )}
                    <Grid size={6}>
                        <ColorField
                            name="colorTextDestacat"
                            label={t($ => $.page.widget.atributsVisuals.colorTextDestacat)}
                        />
                    </Grid>
                    <Grid size={6} />
                    <Grid size={12}>
                        <Typography variant="subtitle2" sx={{ mt: t }}>
                            {t($ => $.page.widget.form.configFont)}
                        </Typography>
                    </Grid>
                    <Grid size={6}>
                        <FormField
                            name="midaFontValor"
                            label={t($ => $.page.widget.atributsVisuals.midaFontValor)}
                            type="number"
                            required={false}
                        />
                    </Grid>
                    <Grid size={6}>
                        <FormField
                            name="midaFontUnitats"
                            label={t($ => $.page.widget.atributsVisuals.midaFontUnitats)}
                            type="number"
                            required={false}
                        />
                    </Grid>
                    <Grid size={6}>
                        <FormField
                            name="midaFontCanviPercentual"
                            label={t($ => $.page.widget.atributsVisuals.midaFontCanviPercentual)}
                            type="number"
                            required={false}
                        />
                    </Grid>
                </Grid>
            </Paper>
        </Grid>
        <Grid size={4}>
            <Box sx={{ height: '190px' }}>
                <SimpleWidgetVisualization preview={true} {...previewData}/>
            </Box>
        </Grid>
    </Grid>
}
const WidgetGraficTab = () => {
    const { t } = useTranslation();
    const {data, apiRef} = useFormContext()
    const theme = useTheme();

    const getColor = (key:string) => {
        const colorKey = `${key}_${data?.paleta}`;
        return data?.colors?.[colorKey] || data?.colors?.[key] || undefined
    }

    const previewData = useMemo(() => ({
        ...data,
        tipusGrafic: data.tipusGrafic || 'BAR_CHART',
        colorVora: getColor('colorVora'),
        colorsPaleta: getColor('colorsPaleta'),
        colorText: getColor('colorText'),
        colorFons: getColor('colorFons'),
    }), [data, getColor]);

    const chartType = data?.tipusGrafic;
    const isPieTypeVisible = chartType === 'PIE_CHART';
    const isBarTypeVisible = chartType === 'BAR_CHART';
    const isLineTypeVisible = chartType === 'LINE_CHART';
    const isScatterTypeVisible = chartType === 'SCATTER_CHART';
    const isSparkLineTypeVisible = chartType === 'SPARK_LINE_CHART';
    const isGaugeTypeVisible = chartType === 'GAUGE_CHART';
    const isHeatTypeVisible = chartType === 'HEATMAP_CHART';

    const handlePaletteChange = (newPalette: string[]) => {
        const paletteString = newPalette.join(',');
        const colorKey = `colorsPaleta_${data?.paleta}`
        apiRef?.current?.setFieldValue('colors', {
            ...data?.colors,
            [colorKey]: paletteString,
        })
    };

    const initialColors: string = useMemo(() => getColor('colorsPaleta'), [data.colors, getColor])

    return <Grid container columnSpacing={1} rowSpacing={1}>
        <Grid size={8}>
            <Paper
                elevation={1}
                sx={{
                    width: '100%',
                    height: '100%',
                    overflow: 'hidden',
                    border: `1px solid ${theme.palette.divider}`,
                    position: 'relative',
                }}
            >
                <Grid container columnSpacing={1} rowSpacing={1} p={1}>
                    <Grid size={12}>
                        <Typography variant="subtitle2">
                            {t($ => $.page.widget.form.configGeneral)}
                        </Typography>
                    </Grid>
                    <Grid size={12}><ColorPaletteSelector value={initialColors?.split(',') || []} onChange={handlePaletteChange} /></Grid>
                    <Grid size={12}><FormField name="tipusGrafic" required/></Grid>
                    { (isBarTypeVisible || isLineTypeVisible || isScatterTypeVisible) && (
                        <Grid size={12}><FormField name="mostrarReticula" label={t($ => $.page.widget.atributsVisuals.mostrarReticula)} type="checkbox" /></Grid>)
                    }
                    {isBarTypeVisible && (
                        <>
                            <Grid size={12}><Typography variant="subtitle2">{t($ => $.page.widget.form.graficBar)}</Typography></Grid>
                            <Grid size={6}><FormField name="barStacked" label={t($ => $.page.widget.atributsVisuals.barStacked)} type="checkbox" /></Grid>
                            <Grid size={6}><FormField name="barHorizontal" label={t($ => $.page.widget.atributsVisuals.barHorizontal)} type="checkbox" /></Grid>
                        </>
                    )}
                    {(isLineTypeVisible || isSparkLineTypeVisible) && (
                        <>
                            <Grid size={12}><Typography variant="subtitle2">{t($ => $.page.widget.form.graficLin)}</Typography></Grid>
                            <Grid size={6}><FormField name="lineShowPoints" label={t($ => $.page.widget.atributsVisuals.lineShowPoints)} type="checkbox" /></Grid>
                            <Grid size={6}><FormField name="area" label={t($ => $.page.widget.atributsVisuals.area)} type="checkbox" /></Grid>
                            <Grid size={6}><FormField name="lineSmooth" label={t($ => $.page.widget.atributsVisuals.lineSmooth)} type="checkbox" /></Grid>
                            <Grid size={6}><FormField name="lineWidth" label={t($ => $.page.widget.atributsVisuals.lineWidth)} type="number" required={false} /></Grid>
                        </>
                    )}
                    {isPieTypeVisible && (
                        <>
                            <Grid size={6}><Typography variant="subtitle2">{t($ => $.page.widget.form.graficPst)}</Typography></Grid>
                            <Grid size={6}><FormField name="outerRadius" label={t($ => $.page.widget.atributsVisuals.outerRadius)} type="number" required={false} /></Grid>
                            <Grid size={6}><FormField name="pieDonut" label={t($ => $.page.widget.atributsVisuals.pieDonut)} type="checkbox" /></Grid>
                            <Grid size={6}><FormField name="innerRadius" label={t($ => $.page.widget.atributsVisuals.innerRadius)} type="number" required={false} /></Grid>
                            <Grid size={6}><FormField name="pieShowLabels" label={t($ => $.page.widget.atributsVisuals.pieShowLabels)} type="checkbox" /></Grid>
                            <Grid size={6}><FormField name="labelSize" label={t($ => $.page.widget.atributsVisuals.labelSize)} type="number" disabled={!data?.pieShowLabels} required={false} /></Grid>
                        </>
                    )}
                    {isGaugeTypeVisible && (
                        <>
                            <Grid size={12}><Typography variant="subtitle2">{t($ => $.page.widget.form.graficGug)}</Typography></Grid>
                            <Grid size={6}><FormField name="gaugeMin" label={t($ => $.page.widget.atributsVisuals.gaugeMin)} type="number" required={false} /></Grid>
                            <Grid size={6}><FormField name="gaugeMax" label={t($ => $.page.widget.atributsVisuals.gaugeMax)} type="number" required={false} /></Grid>
                            <Grid size={12}><FormField name="gaugeRangs" label={t($ => $.page.widget.atributsVisuals.gaugeRangs)} /></Grid>
                        </>
                    )}
                    {isHeatTypeVisible && (
                        <>
                            <Grid size={12}><Typography variant="subtitle2">{t($ => $.page.widget.form.graficMap)}</Typography></Grid>
                            <Grid size={6}><FormField name="heatmapMinValue" label={t($ => $.page.widget.atributsVisuals.heatmapMinValue)} type="number" required={false} /></Grid>
                            <Grid size={6}><FormField name="heatmapMaxValue" label={t($ => $.page.widget.atributsVisuals.heatmapMaxValue)} type="number" required={false} /></Grid>
                        </>
                    )}
                </Grid>
            </Paper>
        </Grid>
        <Grid size={4}>
            <Box sx={{ height: '260px' }}>
                <GraficWidgetVisualization preview={true} {...previewData}/>
            </Box>
        </Grid>
    </Grid>
}
const WidgetTaulaTab = () => {
    const { t } = useTranslation();
    const {data} = useFormContext()
    const theme = useTheme();

    const getColor = (key:string) => {
        const colorKey = `${key}_${data?.paleta}`;
        return data?.colors?.[colorKey] || data?.colors?.[key] || undefined
    }

    const previewData = useMemo(() => ({
        ...data,
        colorVora: getColor('colorVora'),
        colorTextTaula: getColor('colorTextTaula'),
        colorFonsTaula: getColor('colorFonsTaula'),
        colorCapcalera: getColor('colorCapcalera'),
        colorFonsCapcalera: getColor('colorFonsCapcalera'),
        colorAlternancia: getColor('colorAlternancia'),
        colorVoraTaula: getColor('colorVoraTaula'),
        colorSeparadorHoritzontal: getColor('colorSeparadorHoritzontal'),
        colorSeparadorVertical: getColor('colorSeparadorVertical'),
        colorText: getColor('colorText'),
        colorFons: getColor('colorFons'),
    }), [data, getColor]);

    return <Grid container columnSpacing={1} rowSpacing={1}>
        <Grid size={8}>
            <Paper
                elevation={1}
                sx={{
                    width: '100%',
                    height: '100%',
                    overflow: 'hidden',
                    border: `1px solid ${theme.palette.divider}`,
                    position: 'relative',
                }}
            >
                <Grid container columnSpacing={1} rowSpacing={1} p={1}>
                    <Grid size={12}>
                        <Typography variant="subtitle2">
                            {t($ => $.page.widget.form.configTaula)}
                        </Typography>
                    </Grid>
                    <Grid size={6}>
                        <ColorField
                            name="colorTextTaula"
                            label={t($ => $.page.widget.atributsVisuals.colorTextTaula)}
                        />
                    </Grid>
                    <Grid size={6}>
                        <ColorField
                            name="colorFonsTaula"
                            label={t($ => $.page.widget.atributsVisuals.colorFonsTaula)}
                        />
                    </Grid>
                    <Grid size={12}>
                        <FormField
                            name="mostrarCapcalera"
                            label={t($ => $.page.widget.atributsVisuals.mostrarCapcalera)}
                            type="checkbox"
                        />
                    </Grid>
                    {data?.mostrarCapcalera && (
                        <>
                            <Grid size={6}>
                                <ColorField
                                    name="colorCapcalera"
                                    label={t($ => $.page.widget.atributsVisuals.colorCapcalera)}
                                />
                            </Grid>
                            <Grid size={6}>
                                <ColorField
                                    name="colorFonsCapcalera"
                                    label={t($ => $.page.widget.atributsVisuals.colorFonsCapcalera)}
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
                    {data?.mostrarAlternancia && (
                        <>
                            <Grid size={6}>
                                <ColorField
                                    name="colorAlternancia"
                                    label={t($ => $.page.widget.atributsVisuals.colorAlternancia)}
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
                    {data?.mostrarVoraTaula && (
                        <>
                            <Grid size={6}>
                                <ColorField
                                    name="colorVoraTaula"
                                    label={t($ => $.page.widget.atributsVisuals.colorVoraTaula)}
                                />
                            </Grid>
                            <Grid size={6}>
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
                    {data?.mostrarSeparadorHoritzontal && (
                        <>
                            <Grid size={6}>
                                <ColorField
                                    name="colorSeparadorHoritzontal"
                                    label={t($ => $.page.widget.atributsVisuals.colorSeparadorHoritzontal)}
                                />
                            </Grid>
                            <Grid size={6}>
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
                    {data?.mostrarSeparadorVertical && (
                        <>
                            <Grid size={6}>
                                <ColorField
                                    name="colorSeparadorVertical"
                                    label={t($ => $.page.widget.atributsVisuals.colorSeparadorVertical)}
                                />
                            </Grid>
                            <Grid size={6}>
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
                        <Typography variant="subtitle2" sx={{ mt: 1 }}>
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
                        <Typography variant="subtitle2" sx={{ mt: 1 }}>
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
                        <Typography variant="subtitle2" sx={{ mt: 1 }}>
                            {t($ => $.page.widget.form.configFont)}
                        </Typography>
                    </Grid>
                </Grid>
            </Paper>
        </Grid>
        <Grid size={4}>
            <Box sx={{ minHeight: '240px' }}>
                <TaulaWidgetVisualization preview={true} {...previewData}/>
            </Box>
        </Grid>
    </Grid>
}