import {
    Autocomplete,
    Box,
    Button,
    ButtonGroup,
    Checkbox,
    Chip,
    Divider,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    FormControl,
    FormControlLabel,
    Grid,
    IconButton,
    InputLabel,
    MenuItem,
    Paper,
    Select,
    SelectChangeEvent,
    Stack,
    Tab,
    Tabs,
    TextField,
    Tooltip,
    Typography,
} from "@mui/material";
import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import {FormField, MuiDataGrid, useBaseAppContext, useFormContext, useResourceApiService} from "reactlib";
import SimpleWidgetVisualization from "../components/estadistiques/SimpleWidgetVisualization.tsx";
import TaulaWidgetVisualization from "../components/estadistiques/TaulaWidgetVisualization.tsx";
import GraficWidgetVisualization from "../components/estadistiques/GraficWidgetVisualization.tsx";
import TitolWidgetVisualization from "../components/estadistiques/TitolWidgetVisualization.tsx";
import {useMemo, useState} from "react";
import * as React from "react";
import {useTranslation} from "react-i18next";
import {Theme, useTheme} from "@mui/material/styles";
import {darkTheme, lightTheme} from "../theme.ts";

type PaletteGroupType = "LIGHT" | "LIGHT_HIGHLIGHTED" | "DARK" | "DARK_HIGHLIGHTED";
type PaletteRole = "WIDGET" | "CHART";
type WidgetStyleScope = "COMMON" | "SIMPLE" | "GRAFIC" | "TAULA" | "TITOL_1" | "TITOL_2" | "TITOL_3";
type WidgetStyleValueType = "COLOR" | "BOOLEAN" | "NUMBER" | "TEXT" | "ICON";

interface PaletteColor {
    id?: number;
    posicio: number;
    valor: string;
}

interface Palette {
    id?: number;
    clientId?: string;
    nom: string;
    descripcio?: string;
    ordre?: number;
    colors: PaletteColor[];
}

interface PaletteGroup {
    id?: number;
    groupType: PaletteGroupType;
    widgetPalette?: { id?: number; description?: string };
    chartPalette?: { id?: number; description?: string };
    widgetPaletteClientId?: string;
    chartPaletteClientId?: string;
    ordre?: number;
}

interface StyleProperty {
    id?: number;
    scope: WidgetStyleScope;
    propertyName: string;
    label?: string;
    valueType: WidgetStyleValueType;
    paletteRole?: PaletteRole;
    paletteIndex?: number;
    scalarValue?: string;
    defaultProperty?: boolean;
    ordre?: number;
}

interface TemplateData {
    nom?: string;
    paletes?: Palette[];
    paletteGroups?: PaletteGroup[];
    styleProperties?: StyleProperty[];
    tipusGrafic?: string;
    [key: string]: any;
}

const groupLabels: Record<PaletteGroupType, string> = {
    LIGHT: "Tema clar",
    DARK: "Tema fosc",
    LIGHT_HIGHLIGHTED: "Tema clar destacat",
    DARK_HIGHLIGHTED: "Tema fosc destacat",
};

const groupOrder: PaletteGroupType[] = ["LIGHT", "LIGHT_HIGHLIGHTED", "DARK", "DARK_HIGHLIGHTED"];

const defaultPaletteColors = {
    lightWidget: ["#ffffff", "#1f2937", "#d1d5db", "#2563eb", "#16a34a", "#f3f4f6", "#e5e7eb", "#9ca3af"],
    lightChart: ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b"],
    darkWidget: ["#2f343d", "#ffffff", "#6b7280", "#60a5fa", "#86efac", "#374151", "#4b5563", "#9ca3af"],
    darkChart: ["#60a5fa", "#fb923c", "#4ade80", "#f87171", "#c084fc", "#facc15"],
    lightHighlightedWidget: ["#ef6c00", "#ffffff", "#f97316", "#ffffff", "#fff7ed", "#fb923c", "#fed7aa", "#9a3412"],
    lightHighlightedChart: ["#ea580c", "#2563eb", "#16a34a", "#dc2626", "#7c3aed", "#0891b2"],
    darkHighlightedWidget: ["#111827", "#ffffff", "#374151", "#38bdf8", "#a7f3d0", "#1f2937", "#334155", "#cbd5e1"],
    darkHighlightedChart: ["#38bdf8", "#f97316", "#22c55e", "#ef4444", "#a855f7", "#eab308"],
};

const createPalette = (clientId: string, nom: string, colors: string[], ordre: number): Palette => ({
    clientId,
    nom,
    ordre,
    colors: colors.map((valor, posicio) => ({posicio, valor})),
});

const defaultColorsForGroupRole = (groupType: PaletteGroupType, role: PaletteRole) => {
    const suffix = role === "WIDGET" ? "Widget" : "Chart";
    const key = `${groupType === "LIGHT" ? "light" : groupType === "DARK" ? "dark" : groupType === "LIGHT_HIGHLIGHTED" ? "lightHighlighted" : "darkHighlighted"}${suffix}` as keyof typeof defaultPaletteColors;
    return defaultPaletteColors[key];
};

const paletteRoleLabel = (role: PaletteRole) => role === "WIDGET" ? "Widget" : "Grafic";

const defaultPalettes = (): Palette[] => [
    createPalette("light-widget", "Tema clar - widget", defaultPaletteColors.lightWidget, 0),
    createPalette("light-chart", "Tema clar - grafic", defaultPaletteColors.lightChart, 1),
    createPalette("light-highlighted-widget", "Tema clar destacat - widget", defaultPaletteColors.lightHighlightedWidget, 2),
    createPalette("light-highlighted-chart", "Tema clar destacat - grafic", defaultPaletteColors.lightHighlightedChart, 3),
    createPalette("dark-widget", "Tema fosc - widget", defaultPaletteColors.darkWidget, 4),
    createPalette("dark-chart", "Tema fosc - grafic", defaultPaletteColors.darkChart, 5),
    createPalette("dark-highlighted-widget", "Tema fosc destacat - widget", defaultPaletteColors.darkHighlightedWidget, 6),
    createPalette("dark-highlighted-chart", "Tema fosc destacat - grafic", defaultPaletteColors.darkHighlightedChart, 7),
];

const defaultGroups = (): PaletteGroup[] => [
    {groupType: "LIGHT", widgetPaletteClientId: "light-widget", chartPaletteClientId: "light-chart", ordre: 0},
    {groupType: "LIGHT_HIGHLIGHTED", widgetPaletteClientId: "light-highlighted-widget", chartPaletteClientId: "light-highlighted-chart", ordre: 1},
    {groupType: "DARK", widgetPaletteClientId: "dark-widget", chartPaletteClientId: "dark-chart", ordre: 2},
    {groupType: "DARK_HIGHLIGHTED", widgetPaletteClientId: "dark-highlighted-widget", chartPaletteClientId: "dark-highlighted-chart", ordre: 3},
];

const addColorProperty = (properties: StyleProperty[], scope: WidgetStyleScope, propertyName: string, label: string, paletteRole: PaletteRole, paletteIndex: number) => {
    properties.push({
        scope,
        propertyName,
        label,
        valueType: "COLOR",
        paletteRole,
        paletteIndex,
        defaultProperty: true,
        ordre: properties.length,
    });
};

const addScalarProperty = (properties: StyleProperty[], scope: WidgetStyleScope, propertyName: string, label: string, valueType: WidgetStyleValueType, scalarValue: string) => {
    properties.push({
        scope,
        propertyName,
        label,
        valueType,
        scalarValue,
        defaultProperty: true,
        ordre: properties.length,
    });
};

const addTitleStyleProperties = (properties: StyleProperty[], scope: WidgetStyleScope, prefix: string, fontSize: number, underlineWidth: number) => {
    addColorProperty(properties, scope, "colorFons", `${prefix} fons`, "WIDGET", 0);
    addColorProperty(properties, scope, "colorTitol", `${prefix} text`, "WIDGET", 1);
    addScalarProperty(properties, scope, "midaFontTitol", `${prefix} mida font`, "NUMBER", String(fontSize));
    addScalarProperty(properties, scope, "mostrarVora", `${prefix} subratllat`, "BOOLEAN", "true");
    addColorProperty(properties, scope, "colorVora", `${prefix} color subratllat`, "WIDGET", 2);
    addScalarProperty(properties, scope, "ampleVora", `${prefix} gruix subratllat`, "NUMBER", String(underlineWidth));
};

const defaultStyleProperties = (): StyleProperty[] => {
    const properties: StyleProperty[] = [];
    addColorProperty(properties, "COMMON", "colorFons", "Fons", "WIDGET", 0);
    addColorProperty(properties, "COMMON", "colorText", "Text", "WIDGET", 1);
    addColorProperty(properties, "COMMON", "colorVora", "Vora", "WIDGET", 2);
    addScalarProperty(properties, "COMMON", "mostrarVora", "Mostrar vora", "BOOLEAN", "false");
    addScalarProperty(properties, "COMMON", "ampleVora", "Amplada vora", "NUMBER", "1");
    addScalarProperty(properties, "COMMON", "midaFontTitol", "Mida titol", "NUMBER", "22");
    addScalarProperty(properties, "COMMON", "midaFontDescripcio", "Mida descripcio", "NUMBER", "14");

    addColorProperty(properties, "SIMPLE", "colorIcona", "Color icona", "WIDGET", 3);
    addColorProperty(properties, "SIMPLE", "colorFonsIcona", "Fons icona", "WIDGET", 5);
    addColorProperty(properties, "SIMPLE", "colorTextDestacat", "Text destacat", "WIDGET", 4);
    addScalarProperty(properties, "SIMPLE", "midaFontValor", "Mida valor", "NUMBER", "48");
    addScalarProperty(properties, "SIMPLE", "midaFontUnitats", "Mida unitats", "NUMBER", "16");
    addScalarProperty(properties, "SIMPLE", "midaFontCanviPercentual", "Mida canvi", "NUMBER", "18");

    Array.from({length: 6}).forEach((_, index) => {
        addColorProperty(properties, "GRAFIC", `chartSerieColor${index + 1}`, `Serie ${index + 1}`, "CHART", index);
    });
    addScalarProperty(properties, "GRAFIC", "mostrarReticula", "Mostrar reticula", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "barStacked", "Barres apilades", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "barHorizontal", "Barres horitzontals", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "lineShowPoints", "Mostrar punts", "BOOLEAN", "true");
    addScalarProperty(properties, "GRAFIC", "area", "Area", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "lineSmooth", "Linia suau", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "lineWidth", "Amplada linia", "NUMBER", "2");
    addScalarProperty(properties, "GRAFIC", "outerRadius", "Radi exterior", "NUMBER", "100");
    addScalarProperty(properties, "GRAFIC", "pieDonut", "Donut", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "innerRadius", "Radi interior", "NUMBER", "40");
    addScalarProperty(properties, "GRAFIC", "pieShowLabels", "Mostrar etiquetes", "BOOLEAN", "true");
    addScalarProperty(properties, "GRAFIC", "labelSize", "Mida etiqueta", "NUMBER", "12");
    addScalarProperty(properties, "GRAFIC", "gaugeMin", "Gauge minim", "NUMBER", "0");
    addScalarProperty(properties, "GRAFIC", "gaugeMax", "Gauge maxim", "NUMBER", "100");
    addScalarProperty(properties, "GRAFIC", "gaugeRangs", "Rangs gauge", "TEXT", "50,75,100");
    addScalarProperty(properties, "GRAFIC", "heatmapMinValue", "Heatmap minim", "NUMBER", "0");
    addScalarProperty(properties, "GRAFIC", "heatmapMaxValue", "Heatmap maxim", "NUMBER", "100");

    addColorProperty(properties, "TAULA", "colorTextTaula", "Text taula", "WIDGET", 1);
    addColorProperty(properties, "TAULA", "colorFonsTaula", "Fons taula", "WIDGET", 0);
    addScalarProperty(properties, "TAULA", "mostrarCapcalera", "Mostrar capcalera", "BOOLEAN", "true");
    addColorProperty(properties, "TAULA", "colorCapcalera", "Text capcalera", "WIDGET", 1);
    addColorProperty(properties, "TAULA", "colorFonsCapcalera", "Fons capcalera", "WIDGET", 5);
    addScalarProperty(properties, "TAULA", "mostrarAlternancia", "Alternancia", "BOOLEAN", "true");
    addColorProperty(properties, "TAULA", "colorAlternancia", "Color altern", "WIDGET", 6);
    addScalarProperty(properties, "TAULA", "mostrarVoraTaula", "Mostrar vora taula", "BOOLEAN", "false");
    addColorProperty(properties, "TAULA", "colorVoraTaula", "Vora taula", "WIDGET", 2);
    addScalarProperty(properties, "TAULA", "ampleVoraTaula", "Amplada vora taula", "NUMBER", "1");
    addScalarProperty(properties, "TAULA", "mostrarSeparadorHoritzontal", "Separador horitzontal", "BOOLEAN", "true");
    addColorProperty(properties, "TAULA", "colorSeparadorHoritzontal", "Color separador horitzontal", "WIDGET", 2);
    addScalarProperty(properties, "TAULA", "ampleSeparadorHoritzontal", "Amplada separador horitzontal", "NUMBER", "1");
    addScalarProperty(properties, "TAULA", "mostrarSeparadorVertical", "Separador vertical", "BOOLEAN", "false");
    addColorProperty(properties, "TAULA", "colorSeparadorVertical", "Color separador vertical", "WIDGET", 2);
    addScalarProperty(properties, "TAULA", "ampleSeparadorVertical", "Amplada separador vertical", "NUMBER", "1");

    addTitleStyleProperties(properties, "TITOL_1", "Titol 1", 28, 3);
    addTitleStyleProperties(properties, "TITOL_2", "Titol 2", 22, 2);
    addTitleStyleProperties(properties, "TITOL_3", "Titol 3", 18, 1);
    return properties;
};

const templateDefaults = (): TemplateData => ({
    paletes: defaultPalettes(),
    paletteGroups: defaultGroups(),
    styleProperties: defaultStyleProperties(),
    tipusGrafic: "BAR_CHART",
});

const paletteKey = (palette?: Palette) => {
    if (!palette) return "";
    return palette.clientId || (palette.id != null ? String(palette.id) : palette.nom);
};

const paletteOptionValue = (palette?: Palette) => paletteKey(palette);

const propertyKey = (property: StyleProperty) => `${property.scope}:${property.propertyName}`;

type ChartType = "BAR_CHART" | "LINE_CHART" | "PIE_CHART" | "SCATTER_CHART" | "SPARK_LINE_CHART" | "GAUGE_CHART" | "HEATMAP_CHART";

const graphPropertyVisibilityByType: Partial<Record<ChartType, Set<string>>> = {
    BAR_CHART: new Set(["mostrarReticula", "barStacked", "barHorizontal"]),
    LINE_CHART: new Set(["mostrarReticula", "lineShowPoints", "area", "lineSmooth", "lineWidth"]),
    SCATTER_CHART: new Set(["mostrarReticula"]),
    SPARK_LINE_CHART: new Set(["lineShowPoints", "area", "lineSmooth", "lineWidth"]),
    PIE_CHART: new Set(["outerRadius", "pieDonut", "innerRadius", "pieShowLabels", "labelSize"]),
    GAUGE_CHART: new Set(["gaugeMin", "gaugeMax", "gaugeRangs"]),
    HEATMAP_CHART: new Set(["heatmapMinValue", "heatmapMaxValue"]),
};

const graphCommonProperties = new Set<string>(Array.from({length: 6}, (_, index) => `chartSerieColor${index + 1}`));

const shouldShowGraphProperty = (propertyName: string, chartType?: string) => {
    if (!chartType) return true;
    const visibleProperties = graphPropertyVisibilityByType[chartType as ChartType];
    if (!visibleProperties) return true;
    return graphCommonProperties.has(propertyName) || visibleProperties.has(propertyName);
};
const isConfigurableTemplateProperty = (property: StyleProperty) => propertyKey(property) !== "SIMPLE:icona";

const mergeProperties = (properties?: StyleProperty[]) => {
    const merged = new Map<string, StyleProperty>();
    defaultStyleProperties().forEach((property) => merged.set(propertyKey(property), property));
    (properties || []).forEach((property) => {
        if (!property?.scope || !property?.propertyName) return;
        if (!isConfigurableTemplateProperty(property)) return;
        merged.set(propertyKey(property), {
            ...merged.get(propertyKey(property)),
            ...property,
        });
    });
    return Array.from(merged.values()).map((property, ordre) => ({...property, ordre}));
};

const normalizedTemplate = (data?: TemplateData): Required<Pick<TemplateData, "paletes" | "paletteGroups" | "styleProperties">> & TemplateData => {
    const defaults = templateDefaults();
    const paletes = (data?.paletes && data.paletes.length > 0 ? data.paletes : defaults.paletes || []).map((palette, ordre) => ({
        ...palette,
        clientId: palette.clientId || (palette.id != null ? String(palette.id) : `palette-${ordre}`),
        ordre: palette.ordre ?? ordre,
        colors: (palette.colors || []).map((color, posicio) => ({...color, posicio: color.posicio ?? posicio})),
    }));
    return {
        ...data,
        paletes,
        paletteGroups: data?.paletteGroups && data.paletteGroups.length > 0 ? data.paletteGroups : defaults.paletteGroups || [],
        styleProperties: mergeProperties(data?.styleProperties),
    };
};

const paletteByKey = (palettes: Palette[], key?: string) => palettes.find((palette) => paletteKey(palette) === key);

const groupPaletteKey = (group: PaletteGroup, role: PaletteRole) => {
    if (role === "WIDGET") {
        return group.widgetPaletteClientId || (group.widgetPalette?.id != null ? String(group.widgetPalette.id) : "");
    }
    return group.chartPaletteClientId || (group.chartPalette?.id != null ? String(group.chartPalette.id) : "");
};

const paletteForGroup = (data: TemplateData, groupType: PaletteGroupType, role: PaletteRole) => {
    const normalized = normalizedTemplate(data);
    const group = normalized.paletteGroups.find((item) => item.groupType === groupType);
    if (!group) return undefined;
    return paletteByKey(normalized.paletes, groupPaletteKey(group, role));
};

const paletteColor = (palette?: Palette, index?: number) => {
    if (!palette || index == null || index < 0) return undefined;
    return [...palette.colors].sort((a, b) => a.posicio - b.posicio)[index]?.valor;
};

interface PaletteTheme {
    background: string;
    text: string;
    surface: string;
    surfaceText: string;
    fieldBackground: string;
    fieldText: string;
    border: string;
    accent: string;
    accentText: string;
}

const normalizeHexColor = (value?: string) => {
    if (!value || !/^#[0-9a-f]{6}$/i.test(value.trim())) return undefined;
    return value.trim();
};

const hexToRgb = (value?: string) => {
    const color = normalizeHexColor(value);
    if (!color) return undefined;
    const raw = color.slice(1);
    return {
        r: Number.parseInt(raw.slice(0, 2), 16),
        g: Number.parseInt(raw.slice(2, 4), 16),
        b: Number.parseInt(raw.slice(4, 6), 16),
    };
};

const relativeLuminance = (value?: string) => {
    const rgb = hexToRgb(value);
    if (!rgb) return undefined;
    const channel = (component: number) => {
        const normalized = component / 255;
        return normalized <= 0.03928 ? normalized / 12.92 : Math.pow((normalized + 0.055) / 1.055, 2.4);
    };
    return 0.2126 * channel(rgb.r) + 0.7152 * channel(rgb.g) + 0.0722 * channel(rgb.b);
};

const contrastRatio = (left?: string, right?: string) => {
    const leftLum = relativeLuminance(left);
    const rightLum = relativeLuminance(right);
    if (leftLum == null || rightLum == null) return 0;
    const light = Math.max(leftLum, rightLum);
    const dark = Math.min(leftLum, rightLum);
    return (light + 0.05) / (dark + 0.05);
};

const readableTextFor = (background: string, preferred?: string) => {
    const preferredColor = normalizeHexColor(preferred);
    if (preferredColor && contrastRatio(background, preferredColor) >= 4.5) {
        return preferredColor;
    }
    return contrastRatio(background, "#ffffff") >= contrastRatio(background, "#111827") ? "#ffffff" : "#111827";
};

const isDarkGroup = (groupType?: PaletteGroupType) => groupType?.includes("DARK") === true;

const appThemeForGroup = (groupType: PaletteGroupType | undefined, currentTheme: Theme) => {
    const darkGroup = isDarkGroup(groupType);
    if ((currentTheme.palette.mode === "dark") === darkGroup) {
        return currentTheme;
    }
    return darkGroup ? darkTheme : lightTheme;
};

const paletteThemeFor = (groupType: PaletteGroupType | undefined, currentTheme: Theme): PaletteTheme => {
    const appTheme = appThemeForGroup(groupType, currentTheme);
    const background = appTheme.palette.background.default;
    const text = appTheme.palette.text.primary;
    const surface = appTheme.palette.background.paper;
    const surfaceText = appTheme.palette.text.primary;
    const fieldBackground = appTheme.palette.background.paper;
    const fieldText = appTheme.palette.text.primary;
    const border = appTheme.palette.divider;
    const accent = appTheme.palette.primary.main;

    return {
        background,
        text,
        surface,
        surfaceText,
        fieldBackground,
        fieldText,
        border,
        accent,
        accentText: appTheme.palette.primary.contrastText || readableTextFor(accent),
    };
};

const paletteThemeVars = (paletteTheme: PaletteTheme) => ({
    "--plantilla-bg": paletteTheme.background,
    "--plantilla-text": paletteTheme.text,
    "--plantilla-surface": paletteTheme.surface,
    "--plantilla-surface-text": paletteTheme.surfaceText,
    "--plantilla-field-bg": paletteTheme.fieldBackground,
    "--plantilla-field-text": paletteTheme.fieldText,
    "--plantilla-border": paletteTheme.border,
    "--plantilla-accent": paletteTheme.accent,
    "--plantilla-accent-text": paletteTheme.accentText,
} as React.CSSProperties);

const themedTextFieldSx = (paletteTheme: PaletteTheme) => ({
    "& .MuiInputBase-root": {
        bgcolor: paletteTheme.fieldBackground,
        color: paletteTheme.fieldText,
    },
    "& .MuiOutlinedInput-notchedOutline": {
        borderColor: paletteTheme.border,
    },
    "& .MuiInputBase-root:hover .MuiOutlinedInput-notchedOutline": {
        borderColor: paletteTheme.accent,
    },
    "& .MuiInputBase-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
        borderColor: paletteTheme.accent,
    },
    "& .MuiInputLabel-root": {
        color: paletteTheme.surfaceText,
    },
    "& .MuiInputLabel-root.Mui-focused": {
        color: paletteTheme.accent,
    },
    "& .MuiSvgIcon-root": {
        color: paletteTheme.fieldText,
    },
});

const themedFormControlSx = (paletteTheme: PaletteTheme) => ({
    "& .MuiInputLabel-root": {
        color: paletteTheme.surfaceText,
    },
    "& .MuiInputLabel-root.Mui-focused": {
        color: paletteTheme.accent,
    },
    "& .MuiInputBase-root": {
        bgcolor: paletteTheme.fieldBackground,
        color: paletteTheme.fieldText,
    },
    "& .MuiOutlinedInput-notchedOutline": {
        borderColor: paletteTheme.border,
    },
    "& .MuiInputBase-root:hover .MuiOutlinedInput-notchedOutline": {
        borderColor: paletteTheme.accent,
    },
    "& .MuiInputBase-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
        borderColor: paletteTheme.accent,
    },
    "& .MuiSvgIcon-root": {
        color: paletteTheme.fieldText,
    },
});

const colorInputValue = (value: string) => /^#[0-9a-f]{6}$/i.test(value) ? value : "#000000";

const scalarValue = (property: StyleProperty) => {
    if (property.valueType === "BOOLEAN") return property.scalarValue === "true";
    if (property.valueType === "NUMBER") {
        const value = Number(property.scalarValue);
        return Number.isNaN(value) ? undefined : value;
    }
    return property.scalarValue;
};

const propertiesForPreview = (data: TemplateData, groupType: PaletteGroupType, scope: WidgetStyleScope) => {
    const normalized = normalizedTemplate(data);
    const props: Record<string, any> = {};
    const activeScopes = scope === "COMMON" ? ["COMMON", "SIMPLE"] : ["COMMON", scope];
    normalized.styleProperties
        .filter((property) => activeScopes.includes(property.scope))
        .forEach((property) => {
            if (property.valueType === "COLOR") {
                const palette = paletteForGroup(normalized, groupType, property.paletteRole || "WIDGET");
                props[property.propertyName] = paletteColor(palette, property.paletteIndex);
            } else {
                props[property.propertyName] = scalarValue(property);
            }
        });

    if (scope === "GRAFIC") {
        const chartPalette = paletteForGroup(normalized, groupType, "CHART");
        props.colorsPaleta = (chartPalette?.colors || [])
            .sort((a, b) => a.posicio - b.posicio)
            .map((color) => color.valor)
            .join(",");
        props.tipusGrafic = data.tipusGrafic || "BAR_CHART";
        props.descripcio = props.descripcio || "Descripció del gràfic";
        props.llegendaX = props.llegendaX || "Eix X";
    }
    if (scope === "SIMPLE" || scope === "COMMON") {
        props.icona = "AcUnit";
        props.unitat = props.unitat || "u";
        props.canviPercentual = props.canviPercentual || "12.5";
    }
    if (scope === "TAULA") {
        props.descripcio = props.descripcio || "Descripció de la taula";
    }
    return props;
};

const applyTemplateField = (apiRef: any, fieldName: string, value: any) => {
    apiRef?.current?.setFieldValue(fieldName, value);
};

const PaletteBar = ({
    palette,
    selected,
    onClick,
    borderColor,
}: {
    palette?: Palette;
    selected?: boolean;
    onClick?: () => void;
    borderColor?: string;
}) => (
    <ButtonGroup
        fullWidth
        size="small"
        variant="outlined"
        onClick={onClick}
        sx={{
            overflow: "hidden",
            borderRadius: 1,
            border: selected ? "2px solid" : "1px solid",
            borderColor: selected ? borderColor || "primary.main" : borderColor || "divider",
            "& .MuiButtonGroup-grouped": {
                minWidth: 22,
                borderRadius: 0,
                borderColor: borderColor || "divider",
            },
        }}
    >
        {(palette?.colors || []).sort((a, b) => a.posicio - b.posicio).map((color, index) => (
            <Tooltip key={`${paletteKey(palette)}-${index}-${color.valor}`} title={`${index}: ${color.valor}`}>
                <Button
                    aria-label={`${palette?.nom || "Paleta"} ${index}`}
                    sx={{
                        flex: 1,
                        height: 28,
                        bgcolor: color.valor,
                        "&:hover": {bgcolor: color.valor},
                    }}
                />
            </Tooltip>
        ))}
    </ButtonGroup>
);

const PaletteMiniature = ({palette, compact}: { palette?: Palette; compact?: boolean }) => {
    const colors = [...(palette?.colors || [])].sort((a, b) => a.posicio - b.posicio);

    return (
        <Box
            aria-hidden="true"
            sx={{
                display: "flex",
                width: compact ? 54 : 88,
                height: compact ? 18 : 22,
                border: "1px solid",
                borderColor: "divider",
                borderRadius: 0.75,
                overflow: "hidden",
                flexShrink: 0,
                bgcolor: "background.paper",
            }}
        >
            {colors.map((color, index) => (
                <Box
                    key={`${paletteKey(palette)}-${index}-${color.valor}`}
                    data-palette-color={color.valor}
                    sx={{
                        flex: 1,
                        minWidth: 6,
                        bgcolor: color.valor,
                    }}
                />
            ))}
        </Box>
    );
};

const PaletteSelect = ({
    label,
    value,
    palettes,
    onChange,
    paletteTheme,
}: {
    label: string;
    value?: string;
    palettes: Palette[];
    onChange: (value: string) => void;
    paletteTheme?: PaletteTheme;
}) => {
    const selectedPalette = paletteByKey(palettes, value);

    return (
        <Autocomplete
            size="small"
            fullWidth
            options={palettes}
            value={selectedPalette || null}
            getOptionLabel={(palette) => palette.nom || ""}
            isOptionEqualToValue={(option, selected) => paletteKey(option) === paletteKey(selected)}
            onChange={(_event, palette) => {
                if (palette) onChange(paletteOptionValue(palette));
            }}
            renderOption={(props, palette) => {
                const {key, ...optionProps} = props as typeof props & {key: React.Key};

                return (
                    <Box
                        component="li"
                        key={key}
                        {...optionProps}
                        sx={{
                            display: "flex",
                            alignItems: "center",
                            gap: 1,
                            minWidth: 0,
                        }}
                    >
                        <PaletteMiniature palette={palette} />
                        <Typography component="span" variant="body2" noWrap>
                            {palette.nom}
                        </Typography>
                    </Box>
                );
            }}
            renderInput={(params) => (
                <TextField
                    {...params}
                    label={label}
                    sx={paletteTheme ? themedTextFieldSx(paletteTheme) : undefined}
                    InputProps={{
                        ...params.InputProps,
                        startAdornment: selectedPalette ? (
                            <>
                                <PaletteMiniature palette={selectedPalette} compact />
                                {params.InputProps.startAdornment}
                            </>
                        ) : params.InputProps.startAdornment,
                    }}
                />
            )}
        />
    );
};

interface PaletteDialogState {
    mode: "create" | "edit" | "duplicate";
    groupType: PaletteGroupType;
    role: PaletteRole;
    palette: Palette;
    paletteKey?: string;
}

const clonePalette = (palette: Palette): Palette => ({
    ...palette,
    colors: [...(palette.colors || [])]
        .sort((a, b) => a.posicio - b.posicio)
        .map((color, posicio) => ({...color, posicio})),
});

const paletteNames = (palettes: Palette[], excludeKey?: string) => new Set(
    palettes
        .filter((palette) => paletteKey(palette) !== excludeKey)
        .map((palette) => palette.nom?.trim().toLocaleLowerCase())
        .filter(Boolean)
);

const paletteNameExists = (palettes: Palette[], name?: string, excludeKey?: string) => {
    const normalizedName = name?.trim().toLocaleLowerCase();
    return !!normalizedName && paletteNames(palettes, excludeKey).has(normalizedName);
};

const uniquePaletteName = (baseName: string, palettes: Palette[]) => {
    const names = paletteNames(palettes);
    let index = 1;
    let candidate = `${baseName} copia`;
    while (names.has(candidate.trim().toLocaleLowerCase())) {
        index += 1;
        candidate = `${baseName} copia ${index}`;
    }
    return candidate;
};

const paletteSavePayload = (palette: Palette) => ({
    ...palette,
    clientId: undefined,
    key: undefined,
    value: undefined,
    colors: [...(palette.colors || [])]
        .sort((a, b) => a.posicio - b.posicio)
        .map((color, posicio) => ({...color, posicio})),
});

const parseLongId = (value?: string) => {
    if (!value || !/^\d+$/.test(value.trim())) return undefined;
    return Number(value);
};

const PaletteEditorDialog = ({
    state,
    onClose,
    onSave,
    onDuplicate,
    nameExists,
    saving,
}: {
    state?: PaletteDialogState;
    onClose: () => void;
    onSave: (palette: Palette) => void;
    onDuplicate?: (palette: Palette) => void;
    nameExists?: (name?: string) => boolean;
    saving?: boolean;
}) => {
    const [draft, setDraft] = useState<Palette | undefined>(state?.palette);
    const currentTheme = useTheme();

    React.useEffect(() => {
        setDraft(state?.palette ? clonePalette(state.palette) : undefined);
    }, [state]);

    if (!state || !draft) {
        return null;
    }

    const orderedColors = [...draft.colors].sort((a, b) => a.posicio - b.posicio);
    const duplicatedName = nameExists?.(draft.nom) === true;
    const saveDisabled = saving || !draft.nom?.trim() || orderedColors.length === 0 || duplicatedName;

    const setColors = (colors: PaletteColor[]) => {
        setDraft({
            ...draft,
            colors: colors.map((color, posicio) => ({...color, posicio})),
        });
    };

    const moveColor = (index: number, direction: -1 | 1) => {
        const target = index + direction;
        if (target < 0 || target >= orderedColors.length) return;
        const colors = [...orderedColors];
        [colors[index], colors[target]] = [colors[target], colors[index]];
        setColors(colors);
    };

    const updateColor = (index: number, valor: string) => {
        const colors = [...orderedColors];
        colors[index] = {...colors[index], valor};
        setColors(colors);
    };

    const addColor = () => {
        setColors([...orderedColors, {posicio: orderedColors.length, valor: "#000000"}]);
    };

    const deleteColor = (index: number) => {
        setColors(orderedColors.filter((_, posicio) => posicio !== index));
    };

    const paletteTheme = paletteThemeFor(state.groupType, currentTheme);

    return (
        <Dialog
            open
            onClose={onClose}
            fullWidth
            maxWidth="md"
            PaperProps={{
                sx: {
                    bgcolor: paletteTheme.background,
                    color: paletteTheme.text,
                    border: "1px solid",
                    borderColor: paletteTheme.border,
                },
            }}
        >
            <DialogTitle sx={{color: paletteTheme.text, borderBottom: "1px solid", borderColor: paletteTheme.border}}>
                {state.mode === "create" ? "Nova paleta" : state.mode === "duplicate" ? "Duplicar paleta" : "Editar paleta"} · {groupLabels[state.groupType]} · {paletteRoleLabel(state.role)}
            </DialogTitle>
            <DialogContent
                dividers
                data-testid="palette-dialog-theme"
                data-theme-group={state.groupType}
                style={paletteThemeVars(paletteTheme)}
                sx={{
                    bgcolor: paletteTheme.background,
                    color: paletteTheme.text,
                    borderColor: paletteTheme.border,
                }}
            >
                <Stack spacing={1.25} sx={{pt: 0.5}}>
                    <TextField
                        size="small"
                        label="Nom"
                        value={draft.nom || ""}
                        onChange={(event) => setDraft({...draft, nom: event.target.value})}
                        fullWidth
                        error={duplicatedName}
                        helperText={duplicatedName ? "Ja existeix una paleta amb aquest nom" : undefined}
                        sx={themedTextFieldSx(paletteTheme)}
                    />
                    <PaletteBar palette={draft} selected borderColor={paletteTheme.accent} />
                    <Stack spacing={0.75}>
                        {orderedColors.map((color, index) => (
                            <Stack key={`${paletteKey(draft)}-${index}`} direction="row" spacing={1} alignItems="center">
                                <Typography sx={{width: 24, color: paletteTheme.text}} variant="body2">{index}</Typography>
                                <TextField
                                    type="color"
                                    size="small"
                                    value={colorInputValue(color.valor)}
                                    onChange={(event) => updateColor(index, event.target.value)}
                                    sx={{...themedTextFieldSx(paletteTheme), width: 56}}
                                />
                                <TextField
                                    size="small"
                                    value={color.valor}
                                    onChange={(event) => updateColor(index, event.target.value)}
                                    sx={{...themedTextFieldSx(paletteTheme), flex: 1}}
                                />
                                <IconButton size="small" sx={{color: paletteTheme.text}} onClick={() => moveColor(index, -1)} disabled={index === 0}>
                                    <ArrowUpwardIcon fontSize="small" />
                                </IconButton>
                                <IconButton size="small" sx={{color: paletteTheme.text}} onClick={() => moveColor(index, 1)} disabled={index === orderedColors.length - 1}>
                                    <ArrowDownwardIcon fontSize="small" />
                                </IconButton>
                                <IconButton size="small" sx={{color: paletteTheme.text}} onClick={() => deleteColor(index)} disabled={orderedColors.length <= 1}>
                                    <DeleteIcon fontSize="small" />
                                </IconButton>
                            </Stack>
                        ))}
                    </Stack>
                    <Button
                        variant="outlined"
                        startIcon={<AddCircleOutlineIcon />}
                        onClick={addColor}
                        sx={{
                            borderColor: paletteTheme.accent,
                            color: paletteTheme.accent,
                            "&:hover": {
                                borderColor: paletteTheme.accent,
                                bgcolor: paletteTheme.surface,
                            },
                        }}
                    >
                        Afegir color
                    </Button>
                </Stack>
            </DialogContent>
            <DialogActions sx={{bgcolor: paletteTheme.background, borderTop: "1px solid", borderColor: paletteTheme.border}}>
                {state.mode === "edit" && (
                    <Button
                        onClick={() => onDuplicate?.(clonePalette(draft))}
                        startIcon={<ContentCopyIcon />}
                        sx={{mr: "auto", color: paletteTheme.accent}}
                    >
                        Duplicar
                    </Button>
                )}
                <Button onClick={onClose} sx={{color: paletteTheme.text}}>Cancel·lar</Button>
                <Button
                    variant="contained"
                    onClick={() => onSave(clonePalette(draft))}
                    disabled={saveDisabled}
                    sx={{
                        bgcolor: paletteTheme.accent,
                        color: paletteTheme.accentText,
                        "&:hover": {bgcolor: paletteTheme.accent},
                    }}
                >
                    Desar
                </Button>
            </DialogActions>
        </Dialog>
    );
};

const PaletteGroupsEditor = ({selectedGroup, onSelectGroup}: { selectedGroup: PaletteGroupType; onSelectGroup: (group: PaletteGroupType) => void }) => {
    const {data, apiRef} = useFormContext();
    const {create: createPaletteResource, patch: patchPaletteResource, isReady: paletteApiReady} = useResourceApiService("paleta");
    const {temporalMessageShow} = useBaseAppContext();
    const currentTheme = useTheme();
    const template = normalizedTemplate(data);
    const [paletteDialog, setPaletteDialog] = useState<PaletteDialogState>();
    const [savingPalette, setSavingPalette] = useState(false);

    const nextGroupsWithPalette = (groupType: PaletteGroupType, role: PaletteRole, value: string) => (
        template.paletteGroups.map((group) => {
            if (group.groupType !== groupType) return group;
            return role === "WIDGET"
                ? {...group, widgetPaletteClientId: value, widgetPalette: undefined}
                : {...group, chartPaletteClientId: value, chartPalette: undefined};
        })
    );

    const updateGroup = (groupType: PaletteGroupType, role: PaletteRole, value: string) => {
        applyTemplateField(apiRef, "paletteGroups", nextGroupsWithPalette(groupType, role, value));
    };

    const setPalettes = (palettes: Palette[]) => {
        applyTemplateField(apiRef, "paletes", palettes.map((palette, ordre) => ({...palette, ordre})));
    };

    const replacePaletteInGroups = (groupType: PaletteGroupType, role: PaletteRole, oldValue: string, newValue: string) => (
        template.paletteGroups.map((group) => {
            if (group.groupType !== groupType) return group;
            const currentValue = groupPaletteKey(group, role);
            if (currentValue !== oldValue) return group;
            return role === "WIDGET"
                ? {...group, widgetPaletteClientId: newValue, widgetPalette: undefined}
                : {...group, chartPaletteClientId: newValue, chartPalette: undefined};
        })
    );

    const openCreatePalette = (event: React.MouseEvent, groupType: PaletteGroupType, role: PaletteRole) => {
        event.stopPropagation();
        const clientId = `palette-${Date.now()}-${groupType.toLowerCase()}-${role.toLowerCase()}`;
        const baseName = `${groupLabels[groupType]} - ${paletteRoleLabel(role)}`;
        const palette = createPalette(clientId, uniquePaletteName(baseName, template.paletes), defaultColorsForGroupRole(groupType, role), template.paletes.length);
        setPaletteDialog({
            mode: "create",
            groupType,
            role,
            palette,
        });
    };

    const openEditPalette = (event: React.MouseEvent, groupType: PaletteGroupType, role: PaletteRole, palette?: Palette) => {
        event.stopPropagation();
        if (!palette) return;
        setPaletteDialog({
            mode: "edit",
            groupType,
            role,
            paletteKey: paletteKey(palette),
            palette: clonePalette(palette),
        });
    };

    const duplicatePaletteDialog = (palette: Palette) => {
        if (!paletteDialog) return;
        const clientId = `palette-${Date.now()}-${paletteDialog.groupType.toLowerCase()}-${paletteDialog.role.toLowerCase()}-copy`;
        setPaletteDialog({
            mode: "duplicate",
            groupType: paletteDialog.groupType,
            role: paletteDialog.role,
            palette: {
                ...clonePalette(palette),
                id: undefined,
                clientId,
                nom: uniquePaletteName(palette.nom || `${groupLabels[paletteDialog.groupType]} - ${paletteRoleLabel(paletteDialog.role)}`, template.paletes),
                ordre: template.paletes.length,
                colors: palette.colors.map((color) => ({...color, id: undefined})),
            },
        });
    };

    const persistPalette = (palette: Palette, mode: PaletteDialogState["mode"]) => {
        const payload = paletteSavePayload({
            ...palette,
            id: mode === "edit" ? palette.id : undefined,
            ordre: mode === "edit" ? palette.ordre : template.paletes.length,
        });
        const id = payload.id ?? parseLongId(palette.clientId);
        return mode === "edit" && id != null
            ? patchPaletteResource(id, {data: {...payload, id}})
            : createPaletteResource({data: {...payload, id: undefined}});
    };

    const savePaletteDialog = (palette: Palette) => {
        if (!paletteDialog) return;
        if (!paletteApiReady) {
            temporalMessageShow?.(null, "El servei de paletes encara no esta preparat", "error");
            return;
        }
        const key = paletteDialog.paletteKey || paletteKey(palette);
        if (paletteNameExists(template.paletes, palette.nom, paletteDialog.mode === "edit" ? key : undefined)) {
            temporalMessageShow?.(null, "Ja existeix una paleta amb aquest nom", "error");
            return;
        }
        const paletteToSave: Palette = {
            ...palette,
            clientId: palette.clientId || key,
            ordre: paletteDialog.mode === "edit" ? palette.ordre : template.paletes.length,
        };

        setSavingPalette(true);
        persistPalette(paletteToSave, paletteDialog.mode)
            .then((savedPalette: Palette) => {
                const saved = {
                    ...paletteToSave,
                    ...savedPalette,
                    clientId: savedPalette.clientId || (savedPalette.id != null ? String(savedPalette.id) : paletteToSave.clientId),
                    colors: savedPalette.colors || paletteToSave.colors,
                };
                const savedKey = paletteKey(saved);
                if (paletteDialog.mode === "create" || paletteDialog.mode === "duplicate") {
                    setPalettes([...template.paletes, saved]);
                    applyTemplateField(apiRef, "paletteGroups", nextGroupsWithPalette(paletteDialog.groupType, paletteDialog.role, savedKey));
                } else {
                    setPalettes(template.paletes.map((item) => paletteKey(item) === key ? saved : item));
                    applyTemplateField(apiRef, "paletteGroups", replacePaletteInGroups(paletteDialog.groupType, paletteDialog.role, key, savedKey));
                }
                temporalMessageShow?.(null, "Paleta desada", "success");
                setPaletteDialog(undefined);
            })
            .catch((error: any) => {
                temporalMessageShow?.(null, error?.message || "No s'ha pogut desar la paleta", "error");
            })
            .finally(() => setSavingPalette(false));
    };

    return (
        <>
        <Box
            sx={{
                display: "grid",
                gap: 1,
                gridTemplateColumns: {xs: "1fr", sm: "repeat(2, minmax(0, 1fr))", md: "repeat(4, minmax(0, 1fr))"},
            }}
        >
            {groupOrder.map((groupType) => {
                const group = template.paletteGroups.find((item) => item.groupType === groupType) || {groupType};
                const widgetPalette = paletteForGroup(template, groupType, "WIDGET");
                const chartPalette = paletteForGroup(template, groupType, "CHART");
                const groupTheme = paletteThemeFor(groupType, currentTheme);
                const selected = selectedGroup === groupType;
                return (
                    <Paper
                        key={groupType}
                        variant="outlined"
                        data-testid={`palette-group-${groupType}`}
                        data-theme-group={groupType}
                        style={paletteThemeVars(groupTheme)}
                        onClick={() => onSelectGroup(groupType)}
                        sx={{
                            p: 1,
                            bgcolor: groupTheme.background,
                            color: groupTheme.text,
                            borderColor: selected ? groupTheme.accent : groupTheme.border,
                            borderWidth: selected ? 2 : 1,
                            boxShadow: selected ? `0 0 0 1px ${groupTheme.accent}` : "none",
                            minWidth: 0,
                            cursor: "pointer",
                        }}
                    >
                        <Stack spacing={1}>
                            <Stack direction="row" alignItems="center" justifyContent="space-between">
                                <Typography variant="subtitle2" sx={{color: groupTheme.text}}>{groupLabels[groupType]}</Typography>
                                <Chip
                                    size="small"
                                    label={groupType.includes("DARK") ? "Fosc" : "Clar"}
                                    sx={{
                                        bgcolor: groupTheme.surface,
                                        color: groupTheme.surfaceText,
                                        border: "1px solid",
                                        borderColor: groupTheme.border,
                                    }}
                                />
                            </Stack>
                            {(["WIDGET", "CHART"] as PaletteRole[]).map((role) => {
                                const palette = role === "WIDGET" ? widgetPalette : chartPalette;
                                return (
                                    <Stack key={role} spacing={0.75}>
                                        <Stack direction="row" spacing={0.75} alignItems="flex-start">
                                            <Box sx={{flex: 1, minWidth: 0}}>
                                                <PaletteSelect
                                                    label={paletteRoleLabel(role)}
                                                    value={groupPaletteKey(group, role)}
                                                    palettes={template.paletes}
                                                    onChange={(value) => updateGroup(groupType, role, value)}
                                                    paletteTheme={groupTheme}
                                                />
                                            </Box>
                                            <Stack direction="row" spacing={0.25} sx={{pt: 0.25}}>
                                                <Tooltip title={`Crear paleta ${paletteRoleLabel(role).toLowerCase()}`}>
                                                    <IconButton
                                                        size="small"
                                                        sx={{color: groupTheme.accent}}
                                                        aria-label={`Crear paleta ${paletteRoleLabel(role).toLowerCase()} ${groupLabels[groupType]}`}
                                                        onClick={(event) => openCreatePalette(event, groupType, role)}
                                                    >
                                                        <AddCircleOutlineIcon fontSize="small" />
                                                    </IconButton>
                                                </Tooltip>
                                                <Tooltip title={`Editar paleta ${paletteRoleLabel(role).toLowerCase()}`}>
                                                    <span>
                                                        <IconButton
                                                            size="small"
                                                            sx={{color: groupTheme.text}}
                                                            aria-label={`Editar paleta ${paletteRoleLabel(role).toLowerCase()} ${groupLabels[groupType]}`}
                                                            onClick={(event) => openEditPalette(event, groupType, role, palette)}
                                                            disabled={!palette}
                                                        >
                                                            <EditIcon fontSize="small" />
                                                        </IconButton>
                                                    </span>
                                                </Tooltip>
                                            </Stack>
                                        </Stack>
                                        <PaletteBar palette={palette} selected={selected} borderColor={selected ? groupTheme.accent : groupTheme.border} />
                                    </Stack>
                                );
                            })}
                        </Stack>
                    </Paper>
                );
            })}
        </Box>
        <PaletteEditorDialog
            state={paletteDialog}
            onClose={() => setPaletteDialog(undefined)}
            onSave={savePaletteDialog}
            onDuplicate={duplicatePaletteDialog}
            nameExists={(name) => paletteNameExists(template.paletes, name, paletteDialog?.mode === "edit" ? paletteDialog.paletteKey : undefined)}
            saving={savingPalette}
        />
        </>
    );
};

const PalettePositionSelect = ({
    property,
    selectedGroup,
    paletteTheme,
}: {
    property: StyleProperty;
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme;
}) => {
    const {data, apiRef} = useFormContext();
    const template = normalizedTemplate(data);
    const palette = paletteForGroup(template, selectedGroup, property.paletteRole || "WIDGET");
    const colors = [...(palette?.colors || [])].sort((a, b) => a.posicio - b.posicio);

    const updateProperty = (patch: Partial<StyleProperty>) => {
        const next = template.styleProperties.map((item) => propertyKey(item) === propertyKey(property) ? {...item, ...patch} : item);
        applyTemplateField(apiRef, "styleProperties", next);
    };

    return (
        <Stack direction="row" spacing={1} alignItems="center">
            <Chip
                size="small"
                label={property.paletteRole === "CHART" ? "Grafic" : "Widget"}
                sx={{bgcolor: paletteTheme.background, color: paletteTheme.text, border: "1px solid", borderColor: paletteTheme.border}}
            />
            <FormControl fullWidth size="small" sx={themedFormControlSx(paletteTheme)}>
                <InputLabel>{property.label || property.propertyName}</InputLabel>
                <Select
                    label={property.label || property.propertyName}
                    value={property.paletteIndex != null ? String(property.paletteIndex) : ""}
                    onChange={(event: SelectChangeEvent) => updateProperty({paletteIndex: Number(event.target.value)})}
                >
                    {colors.map((color, index) => (
                        <MenuItem key={`${property.propertyName}-${index}`} value={String(index)}>
                            <Stack direction="row" spacing={1} alignItems="center">
                                <Box sx={{width: 22, height: 22, bgcolor: color.valor, border: "1px solid", borderColor: "divider"}} />
                                <span>{index} · {color.valor}</span>
                            </Stack>
                        </MenuItem>
                    ))}
                </Select>
            </FormControl>
        </Stack>
    );
};

const ScalarPropertyField = ({property, paletteTheme}: { property: StyleProperty; paletteTheme: PaletteTheme }) => {
    const {data, apiRef} = useFormContext();
    const template = normalizedTemplate(data);
    const updateProperty = (patch: Partial<StyleProperty>) => {
        const next = template.styleProperties.map((item) => propertyKey(item) === propertyKey(property) ? {...item, ...patch} : item);
        applyTemplateField(apiRef, "styleProperties", next);
    };

    if (property.valueType === "BOOLEAN") {
        return (
            <FormControlLabel
                control={<Checkbox checked={property.scalarValue === "true"} onChange={(event) => updateProperty({scalarValue: event.target.checked ? "true" : "false"})} />}
                label={property.label || property.propertyName}
                sx={{
                    color: paletteTheme.surfaceText,
                    "& .MuiCheckbox-root": {color: paletteTheme.accent},
                }}
            />
        );
    }

    return (
        <TextField
            fullWidth
            size="small"
            type={property.valueType === "NUMBER" ? "number" : "text"}
            label={property.label || property.propertyName}
            value={property.scalarValue || ""}
            onChange={(event) => updateProperty({scalarValue: event.target.value})}
            sx={themedTextFieldSx(paletteTheme)}
        />
    );
};

const StylePropertiesTab = ({scope, selectedGroup, paletteTheme}: { scope: WidgetStyleScope; selectedGroup: PaletteGroupType; paletteTheme: PaletteTheme }) => {
    const {data} = useFormContext();
    const template = normalizedTemplate(data);
    const properties = useMemo(() => {
        const scopedProperties = template.styleProperties.filter((property) => property.scope === scope);
        if (scope !== "GRAFIC") {
            return scopedProperties;
        }
        return scopedProperties.filter((property) => shouldShowGraphProperty(property.propertyName, template.tipusGrafic));
    }, [scope, template.styleProperties, template.tipusGrafic]);

    return (
        <Paper
            variant="outlined"
            sx={{
                p: 1,
                bgcolor: paletteTheme.surface,
                color: paletteTheme.surfaceText,
                borderColor: paletteTheme.border,
            }}
        >
            <Grid container spacing={1}>
                {properties.map((property) => (
                    <Grid key={propertyKey(property)} size={{xs: 12, md: property.valueType === "COLOR" ? 6 : 4}}>
                        {property.valueType === "COLOR"
                            ? <PalettePositionSelect property={property} selectedGroup={selectedGroup} paletteTheme={paletteTheme} />
                            : <ScalarPropertyField property={property} paletteTheme={paletteTheme} />}
                    </Grid>
                ))}
            </Grid>
        </Paper>
    );
};

const titleScopes: WidgetStyleScope[] = ["TITOL_1", "TITOL_2", "TITOL_3"];
const titleScopeLabels: Record<string, string> = {
    TITOL_1: "Titol 1",
    TITOL_2: "Titol 2",
    TITOL_3: "Titol 3",
};

const TitleStylePropertiesTab = ({selectedGroup, paletteTheme}: { selectedGroup: PaletteGroupType; paletteTheme: PaletteTheme }) => (
    <Stack spacing={1}>
        {titleScopes.map((scope) => (
            <Box key={scope}>
                <Typography variant="subtitle2" sx={{mb: 0.75, color: paletteTheme.surfaceText}}>
                    {titleScopeLabels[scope]}
                </Typography>
                <StylePropertiesTab scope={scope} selectedGroup={selectedGroup} paletteTheme={paletteTheme} />
            </Box>
        ))}
    </Stack>
);

const titlePreviewProps = (data: TemplateData, selectedGroup: PaletteGroupType, scope: WidgetStyleScope) => {
    const props = propertiesForPreview(data, selectedGroup, scope);
    return {
        ...props,
        midaFontSubtitol: Number(props.midaFontSubtitol ?? props.midaFontDescripcio) || undefined,
        colorSubtitol: props.colorSubtitol || props.colorTextDestacat || props.colorText,
        titol: titleScopeLabels[scope],
        subtitol: "Subtitol del dashboard",
        mostrarVora: Boolean(props.mostrarVora),
        mostrarVoraBottom: true,
        ampleVora: Number(props.ampleVora) || 1,
    };
};

const Preview = ({tab, selectedGroup, paletteTheme}: { tab: number; selectedGroup: PaletteGroupType; paletteTheme: PaletteTheme }) => {
    const {data} = useFormContext();
    const isTitleTab = tab === 4;
    const scope: WidgetStyleScope = tab === 2 ? "GRAFIC" : tab === 3 ? "TAULA" : tab === 1 ? "SIMPLE" : "COMMON";
    const previewScope = scope === "COMMON" ? "SIMPLE" : scope;
    const props = useMemo(() => propertiesForPreview(data, selectedGroup, scope), [data, selectedGroup, scope]);
    const graphProps = {
        ...props,
        mostrarVora: Boolean(props.mostrarVora),
        ampleVora: Number(props.ampleVora) || 1,
    };

    return (
        <Paper
            variant="outlined"
            sx={{
                p: 1,
                minHeight: 320,
                bgcolor: paletteTheme.background,
                color: paletteTheme.text,
                borderColor: paletteTheme.border,
                position: "sticky",
                top: 8,
            }}
        >
            <Stack spacing={1}>
                <Stack direction="row" spacing={1} alignItems="center">
                    <Typography variant="subtitle2" sx={{color: paletteTheme.text}}>Previsualitzacio</Typography>
                    <Chip
                        size="small"
                        label={selectedGroup}
                        sx={{bgcolor: paletteTheme.surface, color: paletteTheme.surfaceText, border: "1px solid", borderColor: paletteTheme.border}}
                    />
                </Stack>
                {isTitleTab ? (
                    <Stack spacing={1}>
                        {titleScopes.map((titleScope) => (
                            <Box key={titleScope} sx={{height: 76}}>
                                <TitolWidgetVisualization {...titlePreviewProps(data, selectedGroup, titleScope)} />
                            </Box>
                        ))}
                    </Stack>
                ) : (
                    <Box sx={{height: previewScope === "GRAFIC" ? 280 : previewScope === "TAULA" ? 260 : 220}}>
                        {previewScope === "GRAFIC" && <GraficWidgetVisualization preview {...graphProps} />}
                        {previewScope === "TAULA" && <TaulaWidgetVisualization preview {...props} />}
                        {previewScope === "SIMPLE" && <SimpleWidgetVisualization preview {...props} />}
                    </Box>
                )}
            </Stack>
        </Paper>
    );
};

const PlantillaForm = () => {
    const { t } = useTranslation();
    const currentTheme = useTheme();
    const [tab, setTab] = React.useState(0);
    const [selectedGroup, setSelectedGroup] = React.useState<PaletteGroupType>("LIGHT");
    const tabScopes: WidgetStyleScope[] = ["COMMON", "SIMPLE", "GRAFIC", "TAULA"];
    const selectedTheme = paletteThemeFor(selectedGroup, currentTheme);

    React.useEffect(() => {
        const variables = paletteThemeVars(selectedTheme);
        Object.entries(variables).forEach(([key, value]) => document.documentElement.style.setProperty(key, String(value)));
        return () => {
            Object.keys(variables).forEach((key) => document.documentElement.style.removeProperty(key));
        };
    }, [
        selectedTheme.background,
        selectedTheme.text,
        selectedTheme.surface,
        selectedTheme.surfaceText,
        selectedTheme.fieldBackground,
        selectedTheme.fieldText,
        selectedTheme.border,
        selectedTheme.accent,
        selectedTheme.accentText,
    ]);

    return (
        <Box
            data-testid="plantilla-form-theme"
            data-theme-group={selectedGroup}
            style={paletteThemeVars(selectedTheme)}
            sx={{
                bgcolor: selectedTheme.background,
                color: selectedTheme.text,
                p: 1,
                borderRadius: 1,
                "& .MuiInputBase-root": {
                    bgcolor: selectedTheme.fieldBackground,
                    color: selectedTheme.fieldText,
                },
                "& .MuiOutlinedInput-notchedOutline": {
                    borderColor: selectedTheme.border,
                },
                "& .MuiInputLabel-root": {
                    color: selectedTheme.text,
                },
                "& .MuiInputBase-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
                    borderColor: selectedTheme.accent,
                },
                "& .MuiSvgIcon-root": {
                    color: "inherit",
                },
            }}
        >
            <Grid container spacing={1}>
                <Grid size={12}><FormField name="nom" required /></Grid>
                <Grid size={12}>
                    <PaletteGroupsEditor selectedGroup={selectedGroup} onSelectGroup={setSelectedGroup} />
                </Grid>
                <Grid size={{xs: 12, md: 8}}>
                    <Paper
                        variant="outlined"
                        sx={{
                            p: 1,
                            bgcolor: selectedTheme.surface,
                            color: selectedTheme.surfaceText,
                            borderColor: selectedTheme.border,
                        }}
                    >
                        <Tabs
                            value={tab}
                            onChange={(_event, value) => setTab(value)}
                            variant="scrollable"
                            sx={{
                                "& .MuiTab-root": {color: selectedTheme.surfaceText},
                                "& .Mui-selected": {color: selectedTheme.accent},
                                "& .MuiTabs-indicator": {bgcolor: selectedTheme.accent},
                            }}
                        >
                            <Tab label="Comunes" />
                            <Tab label={t($ => $.page.widget.simple.tab.title)} />
                            <Tab label={t($ => $.page.widget.grafic.tab.title)} />
                            <Tab label={t($ => $.page.widget.taula.tab.title)} />
                            <Tab label="Titols" />
                        </Tabs>
                        <Divider sx={{mb: 1, borderColor: selectedTheme.border}} />
                        {tab === 2 && (
                            <Box sx={{mb: 1}}>
                                <FormField name="tipusGrafic" required />
                            </Box>
                        )}
                        {tab === 4
                            ? <TitleStylePropertiesTab selectedGroup={selectedGroup} paletteTheme={selectedTheme} />
                            : <StylePropertiesTab scope={tabScopes[tab]} selectedGroup={selectedGroup} paletteTheme={selectedTheme} />}
                    </Paper>
                </Grid>
                <Grid size={{xs: 12, md: 4}}>
                    <Preview tab={tab} selectedGroup={selectedGroup} paletteTheme={selectedTheme} />
                </Grid>
            </Grid>
        </Box>
    );
};

const columns = [
    {
        field: "nom",
        flex: 1,
    },
];

export const Plantilla = () => (
    <Box sx={{height: "100%"}}>
        <MuiDataGrid
            resourceName="plantilla"
            columns={columns}
            toolbarType="upper"
            popupEditCreateActive
            popupEditActive
            popupEditFormContent={<PlantillaForm />}
            popupEditFormDialogComponentProps={{
                fullWidth: true,
                maxWidth: "xl",
                PaperProps: {
                    sx: {
                        bgcolor: "var(--plantilla-bg, background.paper)",
                        color: "var(--plantilla-text, text.primary)",
                        border: "1px solid",
                        borderColor: "var(--plantilla-border, divider)",
                        "& .MuiDialogTitle-root": {
                            bgcolor: "var(--plantilla-bg, background.paper)",
                            color: "var(--plantilla-text, text.primary)",
                            borderBottom: "1px solid",
                            borderColor: "var(--plantilla-border, divider)",
                        },
                        "& .MuiDialogContent-root": {
                            bgcolor: "var(--plantilla-bg, background.paper)",
                            color: "var(--plantilla-text, text.primary)",
                        },
                        "& .MuiDialogActions-root": {
                            bgcolor: "var(--plantilla-bg, background.paper)",
                            borderTop: "1px solid",
                            borderColor: "var(--plantilla-border, divider)",
                        },
                        "& .MuiDialogActions-root .MuiButton-contained": {
                            bgcolor: "var(--plantilla-accent)",
                            color: "var(--plantilla-accent-text)",
                        },
                        "& .MuiDialogActions-root .MuiButton-contained:hover": {
                            bgcolor: "var(--plantilla-accent)",
                        },
                        "& .MuiDialogActions-root .MuiButton-outlined": {
                            borderColor: "var(--plantilla-accent)",
                            color: "var(--plantilla-accent)",
                        },
                        "& .MuiDialogActions-root .MuiButton-text": {
                            color: "var(--plantilla-text)",
                        },
                    },
                },
            }}
            formAdditionalData={(row: any) => ({
                ...(!row?.id ? templateDefaults() : {}),
                tipusGrafic: row?.tipusGrafic || "BAR_CHART",
            })}
        />
    </Box>
);

export default Plantilla;
