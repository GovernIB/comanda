import {
    Autocomplete,
    Box,
    Button,
    ButtonGroup,
    Checkbox,
    Chip,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    Divider,
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
import EditIcon from "@mui/icons-material/Edit";
import {FormField, MuiDataGrid, useBaseAppContext, useFormContext, useResourceApiService} from "reactlib";
import SimpleWidgetVisualization from "../components/estadistiques/SimpleWidgetVisualization.tsx";
import TaulaWidgetVisualization from "../components/estadistiques/TaulaWidgetVisualization.tsx";
import GraficWidgetVisualization from "../components/estadistiques/GraficWidgetVisualization.tsx";
import TitolWidgetVisualization from "../components/estadistiques/TitolWidgetVisualization.tsx";
import * as React from "react";
import {useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {TFunction} from "i18next";
import {Theme, useTheme} from "@mui/material/styles";
import {darkTheme, lightTheme} from "../theme.ts";
import {
    normalizeColors,
    PaletteColor,
    PaletteData,
    PaletteFormContent,
    PaletteTheme,
    useGetPaletteDialogTitle
} from "../components/PaletteFormContent.tsx";

type PaletteGroupType = "LIGHT" | "LIGHT_HIGHLIGHTED" | "DARK" | "DARK_HIGHLIGHTED";
type PaletteRole = "WIDGET" | "CHART";
type WidgetStyleScope = "COMMON" | "SIMPLE" | "GRAFIC" | "TAULA" | "TITOL_1" | "TITOL_2" | "TITOL_3";
type WidgetStyleValueType = "COLOR" | "BOOLEAN" | "NUMBER" | "TEXT" | "ICON";

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
    valueType: WidgetStyleValueType;
    paletteRole?: PaletteRole;
    paletteIndex?: number;
    scalarValue?: string;
    defaultProperty?: boolean;
    ordre?: number;
    optional?: boolean;
}

interface TemplateData {
    nom?: string;
    paletes?: Palette[];
    paletteGroups?: PaletteGroup[];
    styleProperties?: StyleProperty[];
    tipusGrafic?: string;

    [key: string]: unknown;
}

const LABEL_TRANSLATIONS: Record<string, (t: ReturnType<typeof useTranslation>['t']) => string> = {
    colorFons: (t) => t($ => $.page.plantilla.form.colorFons),
    colorText: (t) => t($ => $.page.plantilla.form.colorText),
    colorVora: (t) => t($ => $.page.plantilla.form.colorVora),
    colorTitol: (t) => t($ => $.page.plantilla.form.colorTitol),
    mostrarVora: (t) => t($ => $.page.plantilla.form.mostrarVora),
    ampleVora: (t) => t($ => $.page.plantilla.form.ampleVora),
    midaFontTitol: (t) => t($ => $.page.plantilla.form.midaFontTitol),
    midaFontDescripcio: (t) => t($ => $.page.plantilla.form.midaFontDescripcio),
    colorIcona: (t) => t($ => $.page.plantilla.form.colorIcona),
    colorFonsIcona: (t) => t($ => $.page.plantilla.form.colorFonsIcona),
    colorTextDestacat: (t) => t($ => $.page.plantilla.form.colorTextDestacat),
    midaFontValor: (t) => t($ => $.page.plantilla.form.midaFontValor),
    midaFontUnitats: (t) => t($ => $.page.plantilla.form.midaFontUnitats),
    midaFontCanviPercentual: (t) => t($ => $.page.plantilla.form.midaFontCanviPercentual),
    colorSubtitol: (t) => t($ => $.page.plantilla.form.colorSubtitol),
    midaFontSubtitol: (t) => t($ => $.page.plantilla.form.midaFontSubtitol),
    posicioSubtitol: (t) => t($ => $.page.plantilla.form.posicioSubtitol),
    separacioSubtitol: (t) => t($ => $.page.plantilla.form.separacioSubtitol),
    mostrarVoraTop: (t) => t($ => $.page.plantilla.form.mostrarVoraTop),
    colorVoraTop: (t) => t($ => $.page.plantilla.form.colorVoraTop),
    ampleVoraTop: (t) => t($ => $.page.plantilla.form.ampleVoraTop),
    mostrarVoraRight: (t) => t($ => $.page.plantilla.form.mostrarVoraRight),
    colorVoraRight: (t) => t($ => $.page.plantilla.form.colorVoraRight),
    ampleVoraRight: (t) => t($ => $.page.plantilla.form.ampleVoraRight),
    mostrarVoraBottom: (t) => t($ => $.page.plantilla.form.mostrarVoraBottom),
    colorVoraBottom: (t) => t($ => $.page.plantilla.form.colorVoraBottom),
    ampleVoraBottom: (t) => t($ => $.page.plantilla.form.ampleVoraBottom),
    mostrarVoraLeft: (t) => t($ => $.page.plantilla.form.mostrarVoraLeft),
    colorVoraLeft: (t) => t($ => $.page.plantilla.form.colorVoraLeft),
    ampleVoraLeft: (t) => t($ => $.page.plantilla.form.ampleVoraLeft),
    mostrarReticula: (t) => t($ => $.page.plantilla.form.mostrarReticula),
    barStacked: (t) => t($ => $.page.plantilla.form.barStacked),
    barHorizontal: (t) => t($ => $.page.plantilla.form.barHorizontal),
    lineShowPoints: (t) => t($ => $.page.plantilla.form.lineShowPoints),
    area: (t) => t($ => $.page.plantilla.form.area),
    lineSmooth: (t) => t($ => $.page.plantilla.form.lineSmooth),
    lineWidth: (t) => t($ => $.page.plantilla.form.lineWidth),
    outerRadius: (t) => t($ => $.page.plantilla.form.outerRadius),
    pieDonut: (t) => t($ => $.page.plantilla.form.pieDonut),
    innerRadius: (t) => t($ => $.page.plantilla.form.innerRadius),
    pieShowLabels: (t) => t($ => $.page.plantilla.form.pieShowLabels),
    labelSize: (t) => t($ => $.page.plantilla.form.labelSize),
    gaugeMin: (t) => t($ => $.page.plantilla.form.gaugeMin),
    gaugeMax: (t) => t($ => $.page.plantilla.form.gaugeMax),
    gaugeRangs: (t) => t($ => $.page.plantilla.form.gaugeRangs),
    heatmapMinValue: (t) => t($ => $.page.plantilla.form.heatmapMinValue),
    heatmapMaxValue: (t) => t($ => $.page.plantilla.form.heatmapMaxValue),
    colorTextTaula: (t) => t($ => $.page.plantilla.form.colorTextTaula),
    colorFonsTaula: (t) => t($ => $.page.plantilla.form.colorFonsTaula),
    mostrarCapcalera: (t) => t($ => $.page.plantilla.form.mostrarCapcalera),
    colorCapcalera: (t) => t($ => $.page.plantilla.form.colorCapcalera),
    colorFonsCapcalera: (t) => t($ => $.page.plantilla.form.colorFonsCapcalera),
    mostrarAlternancia: (t) => t($ => $.page.plantilla.form.mostrarAlternancia),
    colorAlternancia: (t) => t($ => $.page.plantilla.form.colorAlternancia),
    mostrarVoraTaula: (t) => t($ => $.page.plantilla.form.mostrarVoraTaula),
    colorVoraTaula: (t) => t($ => $.page.plantilla.form.colorVoraTaula),
    ampleVoraTaula: (t) => t($ => $.page.plantilla.form.ampleVoraTaula),
    mostrarSeparadorHoritzontal: (t) => t($ => $.page.plantilla.form.mostrarSeparadorHoritzontal),
    colorSeparadorHoritzontal: (t) => t($ => $.page.plantilla.form.colorSeparadorHoritzontal),
    ampleSeparadorHoritzontal: (t) => t($ => $.page.plantilla.form.ampleSeparadorHoritzontal),
    mostrarSeparadorVertical: (t) => t($ => $.page.plantilla.form.mostrarSeparadorVertical),
    colorSeparadorVertical: (t) => t($ => $.page.plantilla.form.colorSeparadorVertical),
    ampleSeparadorVertical: (t) => t($ => $.page.plantilla.form.ampleSeparadorVertical),
};

export const getPropertyLabel = (property: StyleProperty, t: ReturnType<typeof useTranslation>['t']): string => {
    return LABEL_TRANSLATIONS[property.propertyName]?.(t) ?? property.propertyName;
};

export const usePaletteGroupTranslation = () => {
    const {t} = useTranslation();
    const tGroup = (group: PaletteGroupType) => {
        switch (group) {
            case "LIGHT":
                return t($ => $.page.plantilla.groups.light);
            case "DARK":
                return t($ => $.page.plantilla.groups.dark);
            case "LIGHT_HIGHLIGHTED":
                return t($ => $.page.plantilla.groups.lightHighlighted);
            case "DARK_HIGHLIGHTED":
                return t($ => $.page.plantilla.groups.darkHighlighted);
        }
    };
    return {tGroup};
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
    {
        groupType: "LIGHT_HIGHLIGHTED",
        widgetPaletteClientId: "light-highlighted-widget",
        chartPaletteClientId: "light-highlighted-chart",
        ordre: 1
    },
    {groupType: "DARK", widgetPaletteClientId: "dark-widget", chartPaletteClientId: "dark-chart", ordre: 2},
    {
        groupType: "DARK_HIGHLIGHTED",
        widgetPaletteClientId: "dark-highlighted-widget",
        chartPaletteClientId: "dark-highlighted-chart",
        ordre: 3
    },
];

const addColorProperty = (properties: StyleProperty[], scope: WidgetStyleScope, propertyName: string, paletteRole: PaletteRole, paletteIndex?: number, optional = false) => {
    properties.push({
        scope,
        propertyName,
        valueType: "COLOR",
        paletteRole,
        paletteIndex,
        optional,
        defaultProperty: true,
        ordre: properties.length,
    });
};

const addScalarProperty = (properties: StyleProperty[], scope: WidgetStyleScope, propertyName: string, valueType: WidgetStyleValueType, scalarValue?: string) => {
    properties.push({
        scope,
        propertyName,
        valueType,
        scalarValue,
        defaultProperty: true,
        ordre: properties.length,
    });
};

const addTitleStyleProperties = (properties: StyleProperty[], scope: WidgetStyleScope, fontSize: number, underlineWidth: number) => {
    addColorProperty(properties, scope, "colorFons", "WIDGET", 0);
    addColorProperty(properties, scope, "colorTitol", "WIDGET", 1);
    addScalarProperty(properties, scope, "midaFontTitol", "NUMBER", String(fontSize));
    addColorProperty(properties, scope, "colorSubtitol", "WIDGET", undefined, true);
    addScalarProperty(properties, scope, "midaFontSubtitol", "NUMBER", undefined);
    addScalarProperty(properties, scope, "posicioSubtitol", "TEXT", "SOTA");
    addScalarProperty(properties, scope, "separacioSubtitol", "NUMBER", "0");
    addScalarProperty(properties, scope, "mostrarVoraTop", "BOOLEAN", "false");
    addColorProperty(properties, scope, "colorVoraTop", "WIDGET", 2);
    addScalarProperty(properties, scope, "ampleVoraTop", "NUMBER", "1");
    addScalarProperty(properties, scope, "mostrarVoraRight", "BOOLEAN", "false");
    addColorProperty(properties, scope, "colorVoraRight", "WIDGET", 2);
    addScalarProperty(properties, scope, "ampleVoraRight", "NUMBER", "1");
    addScalarProperty(properties, scope, "mostrarVoraBottom", "BOOLEAN", "true");
    addColorProperty(properties, scope, "colorVoraBottom", "WIDGET", 2);
    addScalarProperty(properties, scope, "ampleVoraBottom", "NUMBER", String(underlineWidth));
    addScalarProperty(properties, scope, "mostrarVoraLeft", "BOOLEAN", "false");
    addColorProperty(properties, scope, "colorVoraLeft", "WIDGET", 2);
    addScalarProperty(properties, scope, "ampleVoraLeft", "NUMBER", "1");
};

const defaultStyleProperties = (): StyleProperty[] => {
    const properties: StyleProperty[] = [];
    addColorProperty(properties, "COMMON", "colorFons", "WIDGET", 0);
    addColorProperty(properties, "COMMON", "colorText", "WIDGET", 1);
    addColorProperty(properties, "COMMON", "colorVora", "WIDGET", 2);
    addScalarProperty(properties, "COMMON", "mostrarVora", "BOOLEAN", "false");
    addScalarProperty(properties, "COMMON", "ampleVora", "NUMBER", "1");
    addScalarProperty(properties, "COMMON", "midaFontTitol", "NUMBER", "22");
    addScalarProperty(properties, "COMMON", "midaFontDescripcio", "NUMBER", "14");

    addColorProperty(properties, "SIMPLE", "colorIcona", "WIDGET", 3);
    addColorProperty(properties, "SIMPLE", "colorFonsIcona", "WIDGET", 5);
    addColorProperty(properties, "SIMPLE", "colorTextDestacat", "WIDGET", 4);
    addScalarProperty(properties, "SIMPLE", "midaFontValor", "NUMBER", "48");
    addScalarProperty(properties, "SIMPLE", "midaFontUnitats", "NUMBER", "16");
    addScalarProperty(properties, "SIMPLE", "midaFontCanviPercentual", "NUMBER", "18");

    addScalarProperty(properties, "GRAFIC", "mostrarReticula", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "barStacked", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "barHorizontal", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "lineShowPoints", "BOOLEAN", "true");
    addScalarProperty(properties, "GRAFIC", "area", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "lineSmooth", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "lineWidth", "NUMBER", "2");
    addScalarProperty(properties, "GRAFIC", "outerRadius", "NUMBER", "100");
    addScalarProperty(properties, "GRAFIC", "pieDonut", "BOOLEAN", "false");
    addScalarProperty(properties, "GRAFIC", "innerRadius", "NUMBER", "40");
    addScalarProperty(properties, "GRAFIC", "pieShowLabels", "BOOLEAN", "true");
    addScalarProperty(properties, "GRAFIC", "labelSize", "NUMBER", "12");
    addScalarProperty(properties, "GRAFIC", "gaugeMin", "NUMBER", "0");
    addScalarProperty(properties, "GRAFIC", "gaugeMax", "NUMBER", "100");
    addScalarProperty(properties, "GRAFIC", "gaugeRangs", "TEXT", "50,75,100");
    addScalarProperty(properties, "GRAFIC", "heatmapMinValue", "NUMBER", "0");
    addScalarProperty(properties, "GRAFIC", "heatmapMaxValue", "NUMBER", "100");

    addColorProperty(properties, "TAULA", "colorTextTaula", "WIDGET", 1);
    addColorProperty(properties, "TAULA", "colorFonsTaula", "WIDGET", 0);
    addScalarProperty(properties, "TAULA", "mostrarCapcalera", "BOOLEAN", "true");
    addColorProperty(properties, "TAULA", "colorCapcalera", "WIDGET", 1);
    addColorProperty(properties, "TAULA", "colorFonsCapcalera", "WIDGET", 5);
    addScalarProperty(properties, "TAULA", "mostrarAlternancia", "BOOLEAN", "true");
    addColorProperty(properties, "TAULA", "colorAlternancia", "WIDGET", 6);
    addScalarProperty(properties, "TAULA", "mostrarVoraTaula", "BOOLEAN", "false");
    addColorProperty(properties, "TAULA", "colorVoraTaula", "WIDGET", 2);
    addScalarProperty(properties, "TAULA", "ampleVoraTaula", "NUMBER", "1");
    addScalarProperty(properties, "TAULA", "mostrarSeparadorHoritzontal", "BOOLEAN", "true");
    addColorProperty(properties, "TAULA", "colorSeparadorHoritzontal", "WIDGET", 2);
    addScalarProperty(properties, "TAULA", "ampleSeparadorHoritzontal", "NUMBER", "1");
    addScalarProperty(properties, "TAULA", "mostrarSeparadorVertical", "BOOLEAN", "false");
    addColorProperty(properties, "TAULA", "colorSeparadorVertical", "WIDGET", 2);
    addScalarProperty(properties, "TAULA", "ampleSeparadorVertical", "NUMBER", "1");

    addTitleStyleProperties(properties, "TITOL_1", 28, 3);
    addTitleStyleProperties(properties, "TITOL_2", 22, 2);
    addTitleStyleProperties(properties, "TITOL_3", 18, 1);
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

type ChartType =
    "BAR_CHART"
    | "LINE_CHART"
    | "PIE_CHART"
    | "SCATTER_CHART"
    | "SPARK_LINE_CHART"
    | "GAUGE_CHART"
    | "HEATMAP_CHART";

const graphPropertyVisibilityByType: Partial<Record<ChartType, Set<string>>> = {
    BAR_CHART: new Set(["mostrarReticula", "barStacked", "barHorizontal"]),
    LINE_CHART: new Set(["mostrarReticula", "lineShowPoints", "area", "lineSmooth", "lineWidth"]),
    SCATTER_CHART: new Set(["mostrarReticula"]),
    SPARK_LINE_CHART: new Set(["lineShowPoints", "area", "lineSmooth", "lineWidth"]),
    PIE_CHART: new Set(["outerRadius", "pieDonut", "innerRadius", "pieShowLabels", "labelSize"]),
    GAUGE_CHART: new Set(["gaugeMin", "gaugeMax", "gaugeRangs"]),
    HEATMAP_CHART: new Set(["heatmapMinValue", "heatmapMaxValue"]),
};

const shouldShowGraphProperty = (propertyName: string, chartType?: string) => {
    if (!chartType) return true;
    const visibleProperties = graphPropertyVisibilityByType[chartType as ChartType];
    if (!visibleProperties) return true;
    return visibleProperties.has(propertyName);
};
const isConfigurableTemplateProperty = (property: StyleProperty) => propertyKey(property) !== "SIMPLE:icona";

const chartTypesList: ChartType[] = ["BAR_CHART", "PIE_CHART", "GAUGE_CHART", "LINE_CHART", "SPARK_LINE_CHART", "SCATTER_CHART", "HEATMAP_CHART"];

const useChartTypeLabel = () => {
    const {t} = useTranslation();
    return (type: ChartType) => {
        switch (type) {
            case "BAR_CHART":
                return t($ => $.page.plantilla.detail.chartTypeBarChart);
            case "LINE_CHART":
                return t($ => $.page.plantilla.detail.chartTypeLineChart);
            case "PIE_CHART":
                return t($ => $.page.plantilla.detail.chartTypePieChart);
            case "SCATTER_CHART":
                return t($ => $.page.plantilla.detail.chartTypeScatterChart);
            case "SPARK_LINE_CHART":
                return t($ => $.page.plantilla.detail.chartTypeSparkLineChart);
            case "GAUGE_CHART":
                return t($ => $.page.plantilla.detail.chartTypeGaugeChart);
            case "HEATMAP_CHART":
                return t($ => $.page.plantilla.detail.chartTypeHeatmapChart);
        }
    };
};

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
        // Sense tipus de gràfic seleccionat, shouldShowGraphProperty no pot filtrar i mostraria totes les
        // propietats de tots els tipus alhora: cal un valor per defecte sempre present.
        tipusGrafic: data?.tipusGrafic || defaults.tipusGrafic,
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
    return normalizeColors(palette?.colors || [])[index]?.valor;
};

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

const themedTextFieldSx = ({
    "& .MuiInputBase-root": {
        bgcolor: "var(--plantilla-field-bg) !important",
        color: "var(--plantilla-field-text) !important",
    },
    "& .MuiOutlinedInput-notchedOutline": {
        borderColor: "var(--plantilla-border) !important",
    },
    "& .MuiInputBase-root:hover .MuiOutlinedInput-notchedOutline": {
        borderColor: "var(--plantilla-accent)",
    },
    "& .MuiInputBase-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
        borderColor: "var(--plantilla-accent)",
    },
    "& .MuiInputLabel-root": {
        color: "var(--plantilla-surface-text) !important",
    },
    "& .MuiInputLabel-root.Mui-focused": {
        color: "var(--plantilla-accent)",
    },
    "& .MuiSvgIcon-root": {
        color: "var(--plantilla-field-text)",
    },
});

const themedFormControlSx = ({
    "& .MuiInputLabel-root": {
        color: "var(--plantilla-surface-text)",
    },
    "& .MuiInputLabel-root.Mui-focused": {
        color: "var(--plantilla-accent)",
    },
    "& .MuiInputBase-root": {
        bgcolor: "var(--plantilla-field-bg)",
        color: "var(--plantilla-field-text)",
    },
    "& .MuiOutlinedInput-notchedOutline": {
        borderColor: "var(--plantilla-border)",
    },
    "& .MuiInputBase-root:hover .MuiOutlinedInput-notchedOutline": {
        borderColor: "var(--plantilla-accent)",
    },
    "& .MuiInputBase-root.Mui-focused .MuiOutlinedInput-notchedOutline": {
        borderColor: "var(--plantilla-accent)",
    },
    "& .MuiSvgIcon-root": {
        color: "var(--plantilla-field-text)",
    },
});

const scalarValue = (property: StyleProperty) => {
    if (property.valueType === "BOOLEAN") return property.scalarValue === "true";
    if (property.valueType === "NUMBER") {
        const value = Number(property.scalarValue);
        return Number.isNaN(value) ? undefined : value;
    }
    return property.scalarValue;
};

const propertiesForPreview = (data: TemplateData, groupType: PaletteGroupType, scope: WidgetStyleScope, t: TFunction) => {
    const normalized = normalizedTemplate(data);
    const props: Record<string, unknown> = {};
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

    props.entornCodi = props.entornCodi || "DEV";
    if (scope === "GRAFIC") {
        const chartPalette = paletteForGroup(normalized, groupType, "CHART");
        props.colorsPaleta = normalizeColors(chartPalette?.colors || [])
            .map((color) => color.valor)
            .join(",");
        props.tipusGrafic = data.tipusGrafic || "BAR_CHART";
        props.titol = props.titol || t($ => $.page.plantilla.sample.chartTitle);
        props.descripcio = props.descripcio || t($ => $.page.plantilla.sample.chartDescription);
        props.llegendaX = props.llegendaX || t($ => $.page.plantilla.sample.xAxis);
    }
    if (scope === "SIMPLE" || scope === "COMMON") {
        props.titol = props.titol || t($ => $.page.plantilla.sample.simpleTitle);
        props.valor = 1234;
        props.icona = "AcUnit";
        props.unitat = props.unitat || "u";
        props.canviPercentual = props.canviPercentual || "12.5";
        props.descripcio = props.descripcio || t($ => $.page.plantilla.sample.simpleDescription);
    }
    if (scope === "TAULA") {
        props.titol = props.titol || t($ => $.page.plantilla.sample.tableTitle);
        props.descripcio = props.descripcio || t($ => $.page.plantilla.sample.tableDescription);
    }
    return props;
};

const applyTemplateField = (apiRef: any, fieldName: string, value: unknown) => {
    apiRef?.current?.setFieldValue?.(fieldName, value);
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
}) => {
    const {t} = useTranslation();
    return (
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
            {normalizeColors(palette?.colors || []).map((color, index) => (
                <Tooltip key={`${paletteKey(palette)}-${index}-${color.valor}`} title={`${index}: ${color.valor}`}>
                    <Button
                        aria-label={`${palette?.nom || t($ => $.page.plantilla.sample.defaultPaletteName)} ${index}`}
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
};

const PaletteMiniature = ({palette, compact}: { palette?: Palette; compact?: boolean }) => {
    const colors = normalizeColors(palette?.colors || []);

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
    paletteTheme: PaletteTheme;
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
                const {key, ...optionProps} = props as typeof props & { key: React.Key };

                return (
                    <Box sx={{display: "flex", alignItems: "center", gap: 1, minWidth: 0,}}
                         component="li" key={key} {...optionProps} >
                        <PaletteMiniature palette={palette}/>
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
                    sx={paletteTheme ? themedTextFieldSx : undefined}
                    InputProps={{
                        ...params.InputProps,
                        startAdornment: selectedPalette ? (
                            <>
                                <PaletteMiniature palette={selectedPalette} compact/>
                                {params.InputProps.startAdornment}
                            </>
                        ) : params.InputProps.startAdornment,
                    }}
                />
            )}
            slotProps={{
                paper: {
                    sx: {
                        bgcolor: paletteTheme.fieldBackground,
                        color: paletteTheme.fieldText,
                        border: `1px solid ${paletteTheme.border}`,
                    },
                },
                listbox: {
                    sx: {
                        "& .MuiAutocomplete-option": {
                            color: paletteTheme.fieldText,
                            "&[aria-selected='true']": {
                                bgcolor: `${paletteTheme.accent}33`,
                            },
                            "&:hover": {
                                bgcolor: `${paletteTheme.accent}1A`,
                            },
                        },
                    },
                },
            }}
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
    colors: [...normalizeColors(palette?.colors || [])]
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

const uniquePaletteName = (baseName: string, palettes: Palette[], copySuffix: string = 'copia') => {
    const names = paletteNames(palettes);
    let index = 1;
    let candidate = `${baseName} ${copySuffix}`;
    while (names.has(candidate.trim().toLocaleLowerCase())) {
        index += 1;
        candidate = `${baseName} ${copySuffix} ${index}`;
    }
    return candidate;
};

const paletteSavePayload = (palette: Palette) => ({
    ...palette,
    clientId: undefined,
    key: undefined,
    value: undefined,
    colors: normalizeColors(palette?.colors || [])
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
    const {t} = useTranslation();
    const [draft, setDraft] = useState<Palette | undefined>(state?.palette);
    const currentTheme = useTheme();
    const getPaletteDialogTitle = useGetPaletteDialogTitle();

    React.useEffect(() => {
        setDraft(state?.palette ? clonePalette(state.palette) : undefined);
    }, [state]);

    if (!state || !draft) {
        return null;
    }

    const orderedColors = normalizeColors(draft?.colors || []);
    const duplicatedName = nameExists?.(draft.nom) === true;
    const saveDisabled = saving || !draft.nom?.trim() || orderedColors.length === 0 || duplicatedName;

    const paletteTheme = paletteThemeFor(state.groupType, currentTheme);

    const handlePaletteChange = (updated: PaletteData) => {
        setDraft(updated as Palette);
    };

    const mode = state?.mode || 'create';

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
                {getPaletteDialogTitle(mode, draft?.nom)}
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
                <PaletteFormContent
                    palette={draft || {colors: []}}
                    onChange={handlePaletteChange}
                    mode={mode}
                    showDuplicateButton={true}
                    onDuplicate={() => onDuplicate?.(draft)}
                    paletteTheme={paletteTheme}
                />
            </DialogContent>
            <DialogActions
                sx={{bgcolor: paletteTheme.background, borderTop: "1px solid", borderColor: paletteTheme.border}}>
                <Button onClick={onClose} sx={{color: paletteTheme.text}}>{t($ => $.common.cancel)}</Button>
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
                    {t($ => $.common.save)}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

/**
 * Gestiona el diàleg de crear/editar/duplicar una paleta per a un rol (WIDGET o CHART). Independent del
 * layout: s'utilitza tant des dels 4 panells de tema (rol WIDGET) com des del selector de paleta de
 * gràfic de la pestanya de Gràfics (rol CHART).
 */
const usePaletteRoleEditor = () => {
    const {data, apiRef} = useFormContext();
    const {t} = useTranslation();
    const {
        create: createPaletteResource,
        update: updatePaletteResource,
        isReady: paletteApiReady
    } = useResourceApiService("paleta");
    const {temporalMessageShow} = useBaseAppContext();
    const template = normalizedTemplate(data);
    const {tGroup} = usePaletteGroupTranslation();
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
        const baseName = `${tGroup(groupType)} - ${paletteRoleLabel(role)}`;
        const palette = createPalette(clientId, uniquePaletteName(baseName, template.paletes, t($ => $.page.plantilla.detail.copySuffix)), defaultColorsForGroupRole(groupType, role), template.paletes.length);
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
                nom: uniquePaletteName(palette.nom || `${tGroup(paletteDialog.groupType)} - ${paletteRoleLabel(paletteDialog.role)}`, template.paletes, t($ => $.page.plantilla.detail.copySuffix)),
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
            ? updatePaletteResource(id, {data: {...payload, id}})
            : createPaletteResource({data: {...payload, id: undefined}});
    };

    const savePaletteDialog = (palette: Palette) => {
        if (!paletteDialog) return;
        if (!paletteApiReady) {
            temporalMessageShow?.(null, t($ => $.page.plantilla.detail.msgServiceNotReady), "error");
            return;
        }
        const key = paletteDialog.paletteKey || paletteKey(palette);
        if (paletteNameExists(template.paletes, palette.nom, paletteDialog.mode === "edit" ? key : undefined)) {
            temporalMessageShow?.(null, t($ => $.page.plantilla.detail.msgNameExists), "error");
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
                temporalMessageShow?.(null, t($ => $.page.plantilla.detail.msgSaved), "success");
                setPaletteDialog(undefined);
            })
            .catch((error: any) => {
                temporalMessageShow?.(null, error?.message || t($ => $.page.plantilla.detail.msgSaveError), "error");
            })
            .finally(() => setSavingPalette(false));
    };

    const dialog = (
        <PaletteEditorDialog
            state={paletteDialog}
            onClose={() => setPaletteDialog(undefined)}
            onSave={savePaletteDialog}
            onDuplicate={duplicatePaletteDialog}
            nameExists={(name) => paletteNameExists(template.paletes, name, paletteDialog?.mode === "edit" ? paletteDialog.paletteKey : undefined)}
            saving={savingPalette}
        />
    );

    return {template, updateGroup, openCreatePalette, openEditPalette, dialog};
};

/** Selector d'una paleta (widget o gràfic) per a un grup de tema concret, amb botons de crear/editar. */
const PaletteRoleFields = ({
                               groupType,
                               role,
                               group,
                               palette,
                               palettes,
                               selected,
                               groupTheme,
                               onSelectPalette,
                               onCreatePalette,
                               onEditPalette,
                           }: {
    groupType: PaletteGroupType;
    role: PaletteRole;
    group: PaletteGroup;
    palette?: Palette;
    palettes: Palette[];
    selected: boolean;
    groupTheme: PaletteTheme;
    onSelectPalette: (value: string) => void;
    onCreatePalette: (event: React.MouseEvent) => void;
    onEditPalette: (event: React.MouseEvent) => void;
}) => {
    const {t} = useTranslation();
    const {tGroup} = usePaletteGroupTranslation();
    const roleLabel = role === "WIDGET" ? t($ => $.page.plantilla.detail.roleWidget).toLowerCase() : t($ => $.page.plantilla.detail.roleChart).toLowerCase();
    return (
        <Stack spacing={0.75}>
            <Stack direction="row" spacing={0.75} alignItems="flex-start">
                <Box sx={{flex: 1, minWidth: 0}}>
                    <PaletteSelect
                        label={paletteRoleLabel(role)}
                        value={groupPaletteKey(group, role)}
                        palettes={palettes}
                        onChange={onSelectPalette}
                        paletteTheme={groupTheme}
                    />
                </Box>
                <Stack direction="row" spacing={0.25} sx={{pt: 0.25}}>
                    <Tooltip title={`${t($ => $.page.plantilla.action.createPalette)} ${roleLabel}`}>
                        <IconButton
                            size="small"
                            sx={{color: groupTheme.accent}}
                            aria-label={`${t($ => $.page.plantilla.action.createPalette)} ${roleLabel} ${tGroup(groupType)}`}
                            onClick={onCreatePalette}
                        >
                            <AddCircleOutlineIcon fontSize="small"/>
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={`${t($ => $.page.plantilla.action.editPalette)} ${roleLabel}`}>
                        <span>
                            <IconButton
                                size="small"
                                sx={{color: groupTheme.text}}
                                aria-label={`${t($ => $.page.plantilla.action.editPalette)} ${roleLabel} ${tGroup(groupType)}`}
                                onClick={onEditPalette}
                                disabled={!palette}
                            >
                                <EditIcon fontSize="small"/>
                            </IconButton>
                        </span>
                    </Tooltip>
                </Stack>
            </Stack>
            <PaletteBar palette={palette} selected={selected}
                        borderColor={selected ? groupTheme.accent : groupTheme.border}/>
        </Stack>
    );
};

const PaletteGroupsEditor = ({selectedGroup, onSelectGroup}: {
    selectedGroup: PaletteGroupType;
    onSelectGroup: (group: PaletteGroupType) => void
}) => {
    const {t} = useTranslation();
    const currentTheme = useTheme();
    const {tGroup} = usePaletteGroupTranslation();
    const {template, updateGroup, openCreatePalette, openEditPalette, dialog} = usePaletteRoleEditor();

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
                                    <Typography variant="subtitle2"
                                                sx={{color: groupTheme.text}}>{tGroup(groupType)}</Typography>
                                    <Chip
                                        size="small"
                                        label={groupType.includes("DARK") ? t($ => $.page.plantilla.detail.dark) : t($ => $.page.plantilla.detail.light)}
                                        sx={{
                                            bgcolor: groupTheme.surface,
                                            color: groupTheme.surfaceText,
                                            border: "1px solid",
                                            borderColor: groupTheme.border,
                                        }}
                                    />
                                </Stack>
                                <PaletteRoleFields
                                    groupType={groupType}
                                    role="WIDGET"
                                    group={group}
                                    palette={widgetPalette}
                                    palettes={template.paletes}
                                    selected={selected}
                                    groupTheme={groupTheme}
                                    onSelectPalette={(value) => updateGroup(groupType, "WIDGET", value)}
                                    onCreatePalette={(event) => openCreatePalette(event, groupType, "WIDGET")}
                                    onEditPalette={(event) => openEditPalette(event, groupType, "WIDGET", widgetPalette)}
                                />
                            </Stack>
                        </Paper>
                    );
                })}
            </Box>
            {dialog}
        </>
    );
};

/** Selector de la paleta de gràfic del grup de tema actualment seleccionat, mostrat a la pestanya de Gràfics. */
const ChartPaletteSelector = ({selectedGroup}: { selectedGroup: PaletteGroupType }) => {
    const {t} = useTranslation();
    const currentTheme = useTheme();
    const {tGroup} = usePaletteGroupTranslation();
    const {template, updateGroup, openCreatePalette, openEditPalette, dialog} = usePaletteRoleEditor();
    const group = template.paletteGroups.find((item) => item.groupType === selectedGroup) || {groupType: selectedGroup};
    const chartPalette = paletteForGroup(template, selectedGroup, "CHART");
    const groupTheme = paletteThemeFor(selectedGroup, currentTheme);

    return (
        <Box sx={{mb: 1.5}} data-testid="chart-palette-selector">
            <Typography variant="subtitle2" sx={{mb: 0.5, color: groupTheme.text}}>
                {t($ => $.page.plantilla.detail.chartPaletteFor)} · {tGroup(selectedGroup)}
            </Typography>
            <PaletteRoleFields
                groupType={selectedGroup}
                role="CHART"
                group={group}
                palette={chartPalette}
                palettes={template.paletes}
                selected
                groupTheme={groupTheme}
                onSelectPalette={(value) => updateGroup(selectedGroup, "CHART", value)}
                onCreatePalette={(event) => openCreatePalette(event, selectedGroup, "CHART")}
                onEditPalette={(event) => openEditPalette(event, selectedGroup, "CHART", chartPalette)}
            />
            {dialog}
        </Box>
    );
};

/** Pestanyes de tipus de gràfic: cada una filtra, a la graella de propietats, només les del tipus seleccionat. */
const ChartTypeTabs = ({paletteTheme}: { paletteTheme: PaletteTheme }) => {
    const {data, apiRef} = useFormContext();
    const template = normalizedTemplate(data);
    const chartTypeLabel = useChartTypeLabel();
    const activeIndex = Math.max(0, chartTypesList.indexOf(template.tipusGrafic as ChartType));

    return (
        <Tabs
            value={activeIndex}
            onChange={(_event, value) => applyTemplateField(apiRef, "tipusGrafic", chartTypesList[value])}
            variant="scrollable"
            sx={{
                minHeight: 36,
                "& .MuiTab-root": {color: paletteTheme.surfaceText, minHeight: 36},
                "& .Mui-selected": {color: paletteTheme.accent},
                "& .MuiTabs-indicator": {bgcolor: paletteTheme.accent},
            }}
        >
            {chartTypesList.map((type) => (
                <Tab key={type} label={chartTypeLabel(type)}/>
            ))}
        </Tabs>
    );
};

const PalettePositionSelect = ({
                                   property,
                                   selectedGroup,
                                   paletteTheme,
                                   optional = false,
                               }: {
    property: StyleProperty;
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme;
    optional?: boolean;
}) => {
    const {data, apiRef} = useFormContext();
    const {t} = useTranslation();
    const template = normalizedTemplate(data);
    const palette = paletteForGroup(template, selectedGroup, property.paletteRole || "WIDGET");
    const colors = normalizeColors(palette?.colors || []);

    const labelText = getPropertyLabel(property, t);

    const updateProperty = (patch: Partial<StyleProperty>) => {
        const next = template.styleProperties.map((item) => propertyKey(item) === propertyKey(property) ? {...item, ...patch} : item);
        applyTemplateField(apiRef, "styleProperties", next);
    };

    return (
        <FormControl fullWidth size="small" sx={themedFormControlSx}>
            <InputLabel>{labelText}</InputLabel>
            <Select
                label={labelText}
                value={property.paletteIndex != null ? String(property.paletteIndex) : ""}
                onChange={(event: SelectChangeEvent) => {
                    const value = event.target.value;
                    updateProperty({paletteIndex: value === "" ? undefined : Number(value)})
                }}
                MenuProps={{
                    PaperProps: {
                        sx: {
                            bgcolor: paletteTheme.fieldBackground,
                            color: paletteTheme.fieldText,
                            border: `1px solid ${paletteTheme.border}`,
                            "& .MuiMenuItem-root": {
                                color: paletteTheme.fieldText,
                                "&:hover": {
                                    bgcolor: `${paletteTheme.accent}1A`, // Hover suave
                                },
                                "&.Mui-selected": {
                                    bgcolor: `${paletteTheme.accent}33`, // Selección activa
                                },
                            },
                        },
                    },
                }}
            >
                {optional && (<MenuItem value="">
                    <em>{t($ => $.page.plantilla.detail.defaultValue)}</em>
                </MenuItem>)}
                {colors.map((color, index) => (
                    <MenuItem key={`${property.propertyName}-${index}`} value={String(index)}>
                        <Stack direction="row" spacing={1} alignItems="center">
                            <Box sx={{
                                width: 22,
                                height: 22,
                                bgcolor: color.valor,
                                border: "1px solid",
                                borderColor: "divider"
                            }}/>
                            <span>{index} · {color.valor}</span>
                        </Stack>
                    </MenuItem>
                ))}
            </Select>
        </FormControl>
    );
};

const ScalarPropertyField = ({property, paletteTheme}: { property: StyleProperty; paletteTheme: PaletteTheme }) => {
    const {data, apiRef} = useFormContext();
    const {t} = useTranslation();
    const template = normalizedTemplate(data);
    const labelText = getPropertyLabel(property, t);
    const updateProperty = (patch: Partial<StyleProperty>) => {
        const next = template.styleProperties.map((item) => propertyKey(item) === propertyKey(property) ? {...item, ...patch} : item);
        applyTemplateField(apiRef, "styleProperties", next);
    };

    if (property.valueType === "BOOLEAN") {
        return (
            <FormControlLabel
                control={<Checkbox checked={property.scalarValue === "true"}
                                   onChange={(event) => updateProperty({scalarValue: event.target.checked ? "true" : "false"})}/>}
                label={labelText}
                sx={{
                    color: paletteTheme.surfaceText,
                    "& .MuiCheckbox-root": {color: paletteTheme.accent},
                }}
            />
        );
    }

    if (property.propertyName === "posicioSubtitol") {
        return (
            <FormControl
                fullWidth
                size="small"
                sx={themedFormControlSx}
                data-testid={`property-select-${property.scope}-${property.propertyName}`}
            >
                <InputLabel>{labelText}</InputLabel>
                <Select
                    label={labelText}
                    value={property.scalarValue || "SOTA"}
                    onChange={(event: SelectChangeEvent) => updateProperty({scalarValue: event.target.value})}
                    MenuProps={{
                        PaperProps: {
                            sx: {
                                bgcolor: paletteTheme.fieldBackground,
                                color: paletteTheme.fieldText,
                                border: `1px solid ${paletteTheme.border}`,
                            },
                        },
                    }}
                >
                    <MenuItem value="SOTA">{t($ => $.page.plantilla.detail.posicioSubtitolSota)}</MenuItem>
                    <MenuItem value="COSTAT">{t($ => $.page.plantilla.detail.posicioSubtitolCostat)}</MenuItem>
                </Select>
            </FormControl>
        );
    }

    return (
        <TextField
            fullWidth
            size="small"
            type={property.valueType === "NUMBER" ? "number" : "text"}
            label={labelText}
            value={property.scalarValue || ""}
            onChange={(event) => updateProperty({scalarValue: event.target.value})}
            sx={themedTextFieldSx}
        />
    );
};

const voraCostats: Array<"Top" | "Right" | "Bottom" | "Left"> = ["Top", "Right", "Bottom", "Left"];

const isVoraCostatPropertyName = (propertyName: string) =>
    voraCostats.some((costat) => propertyName === `mostrarVora${costat}` || propertyName === `colorVora${costat}` || propertyName === `ampleVora${costat}`);

// Plantilles creades abans del redisseny de vores per costat poden tenir encara files "TITOL_x:mostrarVora"
// (vora única, sense costat) persistides. Ara que la vora es configura gràficament per costat, aquestes
// files antigues ja no s'han de mostrar a la graella d'un títol.
const legacyTitleVoraPropertyNames = new Set(["mostrarVora", "colorVora", "ampleVora"]);
const isLegacyTitleVoraProperty = (scope: WidgetStyleScope, propertyName: string) =>
    scope.startsWith("TITOL") && legacyTitleVoraPropertyNames.has(propertyName);

const useVoraCostatLabel = () => {
    const {t} = useTranslation();
    return (costat: "Top" | "Right" | "Bottom" | "Left") => {
        switch (costat) {
            case "Top":
                return t($ => $.page.plantilla.detail.voraTop);
            case "Right":
                return t($ => $.page.plantilla.detail.voraRight);
            case "Bottom":
                return t($ => $.page.plantilla.detail.voraBottom);
            case "Left":
                return t($ => $.page.plantilla.detail.voraLeft);
        }
    };
};

/** Camps (mostrar/color/gruix) d'un costat de vora concret, reaprofitant els mateixos controls que la resta de propietats. */
const VoraCostatDialogFields = ({
                                    scope,
                                    costat,
                                    selectedGroup,
                                    paletteTheme,
                                }: {
    scope: WidgetStyleScope;
    costat: "Top" | "Right" | "Bottom" | "Left";
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme;
}) => {
    const {data} = useFormContext();
    const template = normalizedTemplate(data);
    const findProperty = (name: string) => template.styleProperties.find((property) => property.scope === scope && property.propertyName === name);
    const mostrarProperty = findProperty(`mostrarVora${costat}`);
    const colorProperty = findProperty(`colorVora${costat}`);
    const ampleProperty = findProperty(`ampleVora${costat}`);
    if (!mostrarProperty || !colorProperty || !ampleProperty) {
        return null;
    }
    const mostrarValue = mostrarProperty.scalarValue === "true";
    return (
        <Stack spacing={1.5} sx={{mt: 1, minWidth: 260}}>
            <ScalarPropertyField property={mostrarProperty} paletteTheme={paletteTheme}/>
            {mostrarValue && (
                <>
                    <PalettePositionSelect property={colorProperty} selectedGroup={selectedGroup}
                                           paletteTheme={paletteTheme}/>
                    <ScalarPropertyField property={ampleProperty} paletteTheme={paletteTheme}/>
                </>
            )}
        </Stack>
    );
};

/**
 * Editor gràfic de les 4 vores independents d'un títol: un rectangle amb una zona clicable a cada costat
 * (superior/dret/inferior/esquerre) que obre una modal per configurar mostrar/color/gruix d'aquell costat.
 */
const VoraGraphicalEditor = ({scope, selectedGroup, paletteTheme}: {
    scope: WidgetStyleScope;
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme
}) => {
    const {data} = useFormContext();
    const {t} = useTranslation();
    const costatLabel = useVoraCostatLabel();
    const [openCostat, setOpenCostat] = useState<"Top" | "Right" | "Bottom" | "Left" | null>(null);
    const previewProps = useMemo(() => propertiesForPreview(data, selectedGroup, scope, t), [data, selectedGroup, scope, t]);

    const borderFor = (costat: "Top" | "Right" | "Bottom" | "Left") => {
        const mostrar = Boolean(previewProps[`mostrarVora${costat}`]);
        const color = (previewProps[`colorVora${costat}`] as string) || paletteTheme.border;
        const ample = Number(previewProps[`ampleVora${costat}`]) || 1;
        return mostrar ? `${ample}px solid ${color}` : `1px dashed ${paletteTheme.border}`;
    };

    const zoneSx = {position: "absolute" as const, cursor: "pointer"};

    return (
        <>
            <Box sx={{display: "flex", justifyContent: "center", py: 1.5}}>
                <Box
                    sx={{
                        width: 180,
                        height: 96,
                        position: "relative",
                        borderTop: borderFor("Top"),
                        borderRight: borderFor("Right"),
                        borderBottom: borderFor("Bottom"),
                        borderLeft: borderFor("Left"),
                        bgcolor: paletteTheme.background,
                    }}
                >
                    <Tooltip title={costatLabel("Top")}>
                        <Box data-testid={`vora-zone-${scope}-Top`} onClick={() => setOpenCostat("Top")}
                             sx={{...zoneSx, top: 0, left: 14, right: 14, height: 16}}/>
                    </Tooltip>
                    <Tooltip title={costatLabel("Right")}>
                        <Box data-testid={`vora-zone-${scope}-Right`} onClick={() => setOpenCostat("Right")}
                             sx={{...zoneSx, top: 14, right: 0, bottom: 14, width: 16}}/>
                    </Tooltip>
                    <Tooltip title={costatLabel("Bottom")}>
                        <Box data-testid={`vora-zone-${scope}-Bottom`} onClick={() => setOpenCostat("Bottom")}
                             sx={{...zoneSx, bottom: 0, left: 14, right: 14, height: 16}}/>
                    </Tooltip>
                    <Tooltip title={costatLabel("Left")}>
                        <Box data-testid={`vora-zone-${scope}-Left`} onClick={() => setOpenCostat("Left")}
                             sx={{...zoneSx, top: 14, left: 0, bottom: 14, width: 16}}/>
                    </Tooltip>
                </Box>
            </Box>
            <Dialog open={openCostat != null} onClose={() => setOpenCostat(null)} maxWidth="xs" fullWidth>
                <DialogTitle>{openCostat && costatLabel(openCostat)}</DialogTitle>
                <DialogContent>
                    {openCostat &&
                        <VoraCostatDialogFields scope={scope} costat={openCostat} selectedGroup={selectedGroup}
                                                paletteTheme={paletteTheme}/>}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenCostat(null)}>{t($ => $.common.cancel)}</Button>
                </DialogActions>
            </Dialog>
        </>
    );
};

const StylePropertiesTab = ({scope, selectedGroup, paletteTheme}: {
    scope: WidgetStyleScope;
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme
}) => {
    const {data} = useFormContext();
    const template = normalizedTemplate(data);
    const properties = useMemo(() => {
        const scopedProperties = template.styleProperties.filter((property) =>
            property.scope === scope &&
            !isVoraCostatPropertyName(property.propertyName) &&
            !isLegacyTitleVoraProperty(property.scope, property.propertyName)
        );
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
                            ? <PalettePositionSelect property={property} selectedGroup={selectedGroup}
                                                     paletteTheme={paletteTheme} optional={property.optional}/>
                            : <ScalarPropertyField property={property} paletteTheme={paletteTheme}/>}
                    </Grid>
                ))}
            </Grid>
        </Paper>
    );
};

export const useTitleScopeTranslation = () => {
    const {t} = useTranslation();
    const tTitleScope = (scope: WidgetStyleScope) => {
        switch (scope) {
            case "TITOL_1":
                return t($ => $.page.plantilla.detail.title1);
            case "TITOL_2":
                return t($ => $.page.plantilla.detail.title2);
            case "TITOL_3":
                return t($ => $.page.plantilla.detail.title3);
            default:
                return scope;
        }
    };
    return {tTitleScope};
};

/** Contingut d'una pestanya de títol individual (TITOL_1, TITOL_2 o TITOL_3): graella de propietats + vores. */
const TitleScopeTab = ({scope, selectedGroup, paletteTheme}: {
    scope: WidgetStyleScope;
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme
}) => {
    const {t} = useTranslation();
    return (
        <Box>
            <StylePropertiesTab scope={scope} selectedGroup={selectedGroup} paletteTheme={paletteTheme}/>
            <Typography variant="caption" sx={{display: "block", mt: 1, mb: 0.5, color: paletteTheme.surfaceText}}>
                {t($ => $.page.plantilla.detail.voresTitle)}
            </Typography>
            <VoraGraphicalEditor scope={scope} selectedGroup={selectedGroup} paletteTheme={paletteTheme}/>
        </Box>
    );
};

const isTitleScope = (scope: WidgetStyleScope) => scope.startsWith("TITOL");

const titlePreviewProps = (data: TemplateData, selectedGroup: PaletteGroupType, scope: WidgetStyleScope, t: TFunction) => {
    const props = propertiesForPreview(data, selectedGroup, scope, t);
    return {
        ...props,
        midaFontSubtitol: Number(props.midaFontSubtitol ?? props.midaFontDescripcio) || undefined,
        colorSubtitol: (props.colorSubtitol || props.colorTextDestacat || props.colorText) as string | undefined,
        titol: scope,
        subtitol: "",
    };
};

const Preview = ({scope, selectedGroup, paletteTheme}: {
    scope: WidgetStyleScope;
    selectedGroup: PaletteGroupType;
    paletteTheme: PaletteTheme
}) => {
    const {data} = useFormContext();
    const {t} = useTranslation();
    const {tGroup} = usePaletteGroupTranslation();
    const {tTitleScope} = useTitleScopeTranslation();

    const previewScope = scope === "COMMON" ? "SIMPLE" : scope;
    const props = useMemo(() => propertiesForPreview(data, selectedGroup, scope, t), [data, selectedGroup, scope, t]);
    const graphProps = {
        ...props,
        mostrarVora: Boolean(props.mostrarVora),
        ampleVora: Number(props.ampleVora) || 1,
    };

    return (
        <Paper
            variant="outlined"
            sx={{
                p: 1, minHeight: 320, bgcolor: paletteTheme.background, color: paletteTheme.text,
                borderColor: paletteTheme.border, position: "sticky", top: 8,
            }}
        >
            <Stack spacing={1}>
                <Stack direction="row" spacing={1} alignItems="center">
                    <Typography variant="subtitle2"
                                sx={{color: paletteTheme.text}}>{t($ => $.page.plantilla.detail.preview)}</Typography>
                    <Chip
                        size="small"
                        label={tGroup(selectedGroup)}
                        sx={{
                            bgcolor: paletteTheme.surface,
                            color: paletteTheme.surfaceText,
                            border: "1px solid",
                            borderColor: paletteTheme.border
                        }}
                    />
                </Stack>
                {isTitleScope(scope) ? (
                    <Box sx={{height: 76}}>
                        <TitolWidgetVisualization
                            {...titlePreviewProps(data, selectedGroup, scope, t)}
                            titol={tTitleScope(scope)}
                            subtitol={t($ => $.page.plantilla.detail.previewSubtitle)}
                        />
                    </Box>
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

const titleScopesList: WidgetStyleScope[] = ["TITOL_1", "TITOL_2", "TITOL_3"];

const PlantillaForm = () => {
    const {t} = useTranslation();
    const currentTheme = useTheme();
    const [tab, setTab] = React.useState(0);
    const [titleTab, setTitleTab] = React.useState(0);
    const [selectedGroup, setSelectedGroup] = React.useState<PaletteGroupType>("LIGHT");
    const tabScopes: WidgetStyleScope[] = ["COMMON", "SIMPLE", "GRAFIC", "TAULA"];
    const {tTitleScope} = useTitleScopeTranslation();
    const selectedTheme = paletteThemeFor(selectedGroup, currentTheme);
    const isTitleTab = tab === 4;
    const activeScope: WidgetStyleScope = isTitleTab ? titleScopesList[titleTab] : tabScopes[tab];

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
                "& .MuiCheckbox-root.Mui-checked": {
                    color: `${selectedTheme.accent} !important`,
                },
                "& .MuiCheckbox-root.Mui-checked:hover": {
                    backgroundColor: `${selectedTheme.accent}1A !important`,
                },
                "& .MuiTab-root.Mui-selected": {
                    color: `${selectedTheme.accent} !important`,
                },
                "& .MuiTabs-indicator": {
                    backgroundColor: `${selectedTheme.accent} !important`,
                },
            }}
        >
            <Grid container spacing={1}>
                <Grid size={12}><FormField name="nom" required/></Grid>
                <Grid size={12}>
                    <PaletteGroupsEditor selectedGroup={selectedGroup} onSelectGroup={setSelectedGroup}/>
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
                            <Tab label={t($ => $.page.plantilla.detail.common)}/>
                            <Tab label={t($ => $.page.widget.simple.tab.title)}/>
                            <Tab label={t($ => $.page.widget.grafic.tab.title)}/>
                            <Tab label={t($ => $.page.widget.taula.tab.title)}/>
                            <Tab label={t($ => $.page.plantilla.detail.title)}/>
                        </Tabs>
                        <Divider sx={{mb: 1, borderColor: selectedTheme.border}}/>
                        {tab === 2 && (
                            <Box sx={{mb: 1}}>
                                <ChartPaletteSelector selectedGroup={selectedGroup}/>
                                <ChartTypeTabs paletteTheme={selectedTheme}/>
                            </Box>
                        )}
                        {isTitleTab && (
                            <Tabs
                                value={titleTab}
                                onChange={(_event, value) => setTitleTab(value)}
                                sx={{
                                    mb: 1,
                                    minHeight: 36,
                                    "& .MuiTab-root": {color: selectedTheme.surfaceText, minHeight: 36},
                                    "& .Mui-selected": {color: selectedTheme.accent},
                                    "& .MuiTabs-indicator": {bgcolor: selectedTheme.accent},
                                }}
                            >
                                <Tab label={tTitleScope("TITOL_1")}/>
                                <Tab label={tTitleScope("TITOL_2")}/>
                                <Tab label={tTitleScope("TITOL_3")}/>
                            </Tabs>
                        )}
                        {isTitleTab
                            ? <TitleScopeTab scope={activeScope} selectedGroup={selectedGroup}
                                             paletteTheme={selectedTheme}/>
                            : <StylePropertiesTab scope={activeScope} selectedGroup={selectedGroup}
                                                  paletteTheme={selectedTheme}/>}
                    </Paper>
                </Grid>
                <Grid size={{xs: 12, md: 4}}>
                    <Preview scope={activeScope} selectedGroup={selectedGroup} paletteTheme={selectedTheme}/>
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
            popupEditFormContent={<PlantillaForm/>}
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
            formAdditionalData={(row: Record<string, unknown> | null | undefined) => ({
                ...(!row?.id ? templateDefaults() : {}),
                tipusGrafic: row?.tipusGrafic || "BAR_CHART",
            })}
        />
    </Box>
);

export default Plantilla;
