import { describe, expect, it } from 'vitest';
import { resolveWidgetStyles } from './dashboardStyleResolver';

describe('resolveWidgetStyles', () => {
    const mockPlantilla = {
        id: 1,
        nom: 'Plantilla Test',
        paletes: [
            {
                id: 10,
                nom: 'Paleta Widget Light',
                colors: [
                    { posicio: 0, valor: '#FF0000' },
                    { posicio: 1, valor: '#00FF00' },
                ],
            },
            {
                id: 20,
                nom: 'Paleta Chart Light',
                colors: [
                    { posicio: 0, valor: '#0000FF' },
                    { posicio: 1, valor: '#FFFF00' },
                    { posicio: 2, valor: '#FF00FF' },
                ],
            },
            {
                id: 30,
                nom: 'Paleta Widget Dark',
                colors: [
                    { posicio: 0, valor: '#AA0000' },
                ],
            },
        ],
        paletteGroups: [
            {
                id: 100,
                groupType: 'LIGHT',
                widgetPalette: { id: 10 },
                chartPalette: { id: 20 },
            },
            {
                id: 101,
                groupType: 'DARK',
                widgetPalette: { id: 30 },
            },
            {
                id: 102,
                groupType: 'LIGHT_HIGHLIGHTED',
                widgetPalette: { id: 10 },
                chartPalette: { id: 20 },
            },
        ],
        styleProperties: [
            {
                id: 1,
                scope: 'COMMON',
                propertyName: 'colorFons',
                valueType: 'COLOR',
                paletteRole: 'WIDGET',
                paletteIndex: 0,
            },
            {
                id: 2,
                scope: 'SIMPLE',
                propertyName: 'midaFontTitol',
                valueType: 'NUMBER',
                scalarValue: '16',
            },
            {
                id: 3,
                scope: 'GRAFIC',
                propertyName: 'lineWidth',
                valueType: 'NUMBER',
                scalarValue: '2',
            },
            {
                id: 4,
                scope: 'COMMON',
                propertyName: 'mostrarVora',
                valueType: 'BOOLEAN',
                scalarValue: 'true',
            },
            {
                id: 5,
                scope: 'TAULA',
                propertyName: 'mostrarCapcalera',
                valueType: 'BOOLEAN',
                scalarValue: 'false',
            },
            {
                id: 6,
                scope: 'TITOL_1',
                propertyName: 'tipusTitol',
                valueType: 'TEXT',
                scalarValue: 'TIPUS_1',
            },
        ],
    };

    it('resolveWidgetStyles_quanNoHiHaPlantilla_retornaAtributsWidget', () => {
        const widget = {
            tipus: 'SIMPLE',
            atributsVisuals: {
                colorFons: '#123456',
                midaFontTitol: 20,
            },
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', null, false);

        expect(result).toEqual({
            colorFons: '#123456',
            midaFontTitol: 20,
        });
    });

    it('resolveWidgetStyles_quanNoHiHaAtributsVisuals_usaObjecteWidgetDirectament', () => {
        const widget = {
            tipus: 'SIMPLE',
            colorFons: '#123456',
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', mockPlantilla as any, false);

        expect(result.colorFons).toBe('#123456');
    });

    it('resolveWidgetStyles_quanHiHaPlantilla_aplicaEstilsDePlantilla', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: false,
            atributsVisuals: {},
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', mockPlantilla as any, false);

        expect(result.colorFons).toBe('#FF0000');
        expect(result.midaFontTitol).toBe(16);
        expect(result.mostrarVora).toBe(true);
    });

    it('resolveWidgetStyles_quanWidgetTeAtributs_propisSobreescriuenPlantilla', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: false,
            atributsVisuals: {
                colorFons: '#CUSTOM',
                midaFontTitol: 24,
            },
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', mockPlantilla as any, false);

        expect(result.colorFons).toBe('#CUSTOM');
        expect(result.midaFontTitol).toBe(24);
        expect(result.mostrarVora).toBe(true);
    });

    it('resolveWidgetStyles_quanAtributWidgetEsBuitNoSobreescriu', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: false,
            atributsVisuals: {
                colorFons: '',
                midaFontTitol: null,
                mostrarVora: undefined,
            },
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', mockPlantilla as any, false);

        expect(result.colorFons).toBe('#FF0000');
        expect(result.midaFontTitol).toBe(16);
        expect(result.mostrarVora).toBe(true);
    });

    it('resolveWidgetStyles_quanTemaFosc_usaPaletaDark', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: false,
            atributsVisuals: {},
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', mockPlantilla as any, true);

        expect(result.colorFons).toBe('#AA0000');
    });

    it('resolveWidgetStyles_quanWidgetDestacatTemaClar_usaLightHighlighted', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: true,
            atributsVisuals: {},
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', mockPlantilla as any, false);

        expect(result.colorFons).toBe('#FF0000');
    });

    it('resolveWidgetStyles_quanEsGRAFIC_inclouColorsPaleta', () => {
        const widget = {
            tipus: 'GRAFIC',
            destacat: false,
            atributsVisuals: {},
        };

        const result = resolveWidgetStyles(widget, 'GRAFIC', mockPlantilla as any, false);

        expect(result.colorsPaleta).toBe('#0000FF,#FFFF00,#FF00FF');
        expect(result.lineWidth).toBe(2);
    });

    it('resolveWidgetStyles_quanEsTAULA_aplicaEstilsTaula', () => {
        const widget = {
            tipus: 'TAULA',
            destacat: false,
            atributsVisuals: {},
        };

        const result = resolveWidgetStyles(widget, 'TAULA', mockPlantilla as any, false);

        expect(result.colorFons).toBe('#FF0000');
        expect(result.mostrarCapcalera).toBe(false);
    });

    it('resolveWidgetStyles_quanEsTITOL_aplicaEstilsTitol', () => {
        const widget = {
            tipus: 'TITOL',
            destacat: false,
            atributsVisuals: {},
        };

        const result = resolveWidgetStyles(widget, 'TITOL', mockPlantilla as any, false);

        expect(result.tipusTitol).toBe('TIPUS_1');
        expect(result.colorFons).toBe('#FF0000');
    });

    it('resolveWidgetStyles_quanPlantillaNoTeStyleProperties_retornaObjecteBuit', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: false,
            atributsVisuals: {},
        };

        const plantillaBuida = {
            id: 2,
            nom: 'Plantilla Buida',
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', plantillaBuida as any, false);

        expect(result).toEqual({});
    });

    it('resolveWidgetStyles_quanPaletteGroupNoExisteix_noAplicaColors', () => {
        const widget = {
            tipus: 'SIMPLE',
            destacat: false,
            atributsVisuals: {},
        };

        const plantillaSenseDark = {
            id: 3,
            nom: 'Plantilla Sense Dark',
            paletteGroups: [
                {
                    id: 200,
                    groupType: 'LIGHT',
                    widgetPalette: { id: 10 },
                },
            ],
            styleProperties: [
                {
                    id: 1,
                    scope: 'COMMON',
                    propertyName: 'colorFons',
                    valueType: 'COLOR',
                    paletteRole: 'WIDGET',
                    paletteIndex: 0,
                },
            ],
            paletes: [],
        };

        const result = resolveWidgetStyles(widget, 'SIMPLE', plantillaSenseDark as any, true);

        expect(result.colorFons).toBeUndefined();
    });
});