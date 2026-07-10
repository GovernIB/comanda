type WidgetType = 'SIMPLE' | 'GRAFIC' | 'TAULA' | 'TITOL';
type PaletteGroupType = 'LIGHT' | 'LIGHT_HIGHLIGHTED' | 'DARK' | 'DARK_HIGHLIGHTED';

interface PaletteColor {
    id?: number;
    posicio: number;
    valor: string;
}

interface Paleta {
    id: number;
    nom: string;
    descripcio?: string;
    colors: PaletteColor[];
    clientId?: string;
}

interface PaletteGroup {
    id: number;
    groupType: PaletteGroupType;
    widgetPalette?: { id: number; description?: string };
    chartPalette?: { id: number; description?: string };
    widgetPaletteClientId?: string;
    chartPaletteClientId?: string;
}

interface StyleProperty {
    id: number;
    scope: string;
    propertyName: string;
    valueType: 'COLOR' | 'BOOLEAN' | 'NUMBER' | 'TEXT' | 'ICON';
    paletteRole?: 'WIDGET' | 'CHART';
    paletteIndex?: number;
    scalarValue?: string;
    defaultProperty?: boolean;
    ordre?: number;
    optional?: boolean;
}

interface Plantilla {
    id: number;
    nom: string;
    paletes?: Paleta[];
    paletteGroups?: PaletteGroup[];
    styleProperties?: StyleProperty[];
    [key: string]: any;
}

/** Retorna els estils combinats de plantilla del dashboard amb els propis del widget. **/
export const resolveWidgetStyles = (
    widget: any,
    widgetType: WidgetType,
    plantilla: Plantilla | null | undefined,
    temaFosc: boolean = false,
): Record<string, any> => {
    const widgetAtributs = widget.atributsVisuals || widget;
    if (!plantilla) {
        return widgetAtributs;
    }
    const themeGroup = calculateThemeGroup(temaFosc, widget?.destacat);
    const scopes = getRelevantScopes(widgetType, widget?.tipusTitol);
    const plantillaStyles = getPlantillaStylesForWidget(plantilla, scopes, themeGroup);
    const finalStyles = { ...plantillaStyles };
    Object.keys(widgetAtributs).forEach(key => {
        const widgetValue = widgetAtributs[key];
        if (widgetValue !== undefined && 
            widgetValue !== null && 
            widgetValue !== '') {
            finalStyles[key] = widgetValue;
        }
    });
    return finalStyles;
};

const calculateThemeGroup = (temaFosc: boolean, widgetDestacat: boolean): PaletteGroupType => {
    if (temaFosc) {
        return widgetDestacat ? 'DARK_HIGHLIGHTED' : 'DARK';
    }
    return widgetDestacat ? 'LIGHT_HIGHLIGHTED' : 'LIGHT';
};

const getPlantillaStylesForWidget = (
    plantilla: Plantilla,
    relevantScopes: string[],
    themeGroup: PaletteGroupType
): Record<string, any> => {
    const styles: Record<string, any> = {};
    const styleProperties = plantilla.styleProperties || [];
    const paletes = plantilla.paletes || [];
    const paletteGroups = plantilla.paletteGroups || [];
    const relevantProperties = styleProperties.filter(prop =>
        relevantScopes.includes(prop.scope)
    );
    relevantProperties.forEach(prop => {
        const value = resolvePropertyValue(prop, themeGroup, paletes, paletteGroups);
        if (value !== undefined) {
            styles[prop.propertyName] = value;
        }
    });
    if (relevantScopes.includes('GRAFIC')) {
        const chartColors = getChartColors(themeGroup, paletes, paletteGroups);
        if (chartColors.length > 0) {
            styles.colorsPaleta = chartColors.join(',');
        }
    }
    return styles;
};

const mapTipusTitolToScope = (tipusTitol?: string): string => {
    switch (tipusTitol) {
        case 'TIPUS_1': return 'TITOL_1';
        case 'TIPUS_2': return 'TITOL_2';
        case 'TIPUS_3': return 'TITOL_3';
        default: return 'TITOL_1';
    }
};

export const getRelevantScopes = (widgetType: WidgetType, tipusTitol?: string): string[] => {
    switch (widgetType) {
        case 'SIMPLE':
            return ['COMMON', 'SIMPLE'];
        case 'GRAFIC':
            return ['COMMON', 'GRAFIC'];
        case 'TAULA':
            return ['COMMON', 'TAULA'];
        case 'TITOL':
            const scope = mapTipusTitolToScope(tipusTitol);
            return ['COMMON', scope];
        default:
            return ['COMMON'];
    }
};

const resolvePropertyValue = (
    prop: StyleProperty,
    themeGroup: PaletteGroupType,
    paletes: Paleta[],
    paletteGroups: PaletteGroup[]
): any => {
    if (prop.valueType === 'COLOR') {
        const paletteRole = prop.paletteRole || 'WIDGET';
        const paletteIndex = prop.paletteIndex;
        if (paletteIndex === undefined) return undefined;
        const group = paletteGroups.find(g => g.groupType === themeGroup);
        if (!group) return undefined;
        const paletteId = paletteRole === 'WIDGET' 
            ? (group.widgetPaletteClientId || group.widgetPalette?.id?.toString())
            : (group.chartPaletteClientId || group.chartPalette?.id?.toString());
        if (!paletteId) return undefined;
        const palette = paletes.find(p => 
            p.clientId === paletteId || p.id.toString() === paletteId
        );
        if (!palette) return undefined;
        const color = palette.colors.find(c => c.posicio === paletteIndex);
        return color?.valor;
    } else {
        if (prop.scalarValue === undefined) return undefined;
        switch (prop.valueType) {
            case 'BOOLEAN':
                return prop.scalarValue === 'true';
            case 'NUMBER':
                const num = Number(prop.scalarValue);
                return isNaN(num) ? undefined : num;
            case 'TEXT':
            case 'ICON':
            default:
                return prop.scalarValue;
        }
    }
};

const getChartColors = (
    themeGroup: PaletteGroupType,
    paletes: Paleta[],
    paletteGroups: PaletteGroup[]
): string[] => {
    const group = paletteGroups.find(g => g.groupType === themeGroup);
    if (!group) return [];
    const paletteId = group.chartPaletteClientId || group.chartPalette?.id?.toString();
    if (!paletteId) return [];
    const palette = paletes.find(p => 
        p.clientId === paletteId || p.id.toString() === paletteId
    );
    if (!palette) return [];
    return palette.colors
        .sort((a, b) => a.posicio - b.posicio)
        .map(c => c.valor);
};