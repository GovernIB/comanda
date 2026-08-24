import { beforeEach, describe, expect, it } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import i18n from '../../i18n/i18n';
import {
    columnesDimensio,
    columnesIndicador,
    getColumnesDimensioValor,
    useColumnesDimensioValor,
} from './advancedSearchColumns';

describe('advancedSearchColumns', () => {
    beforeEach(async () => {
        await i18n.changeLanguage('ca');
    });

    it('advancedSearchColumns_quanEsConsultenLesColumnesIndicador_mantenenLEsquemaEsperat', () => {
        // Comprova que la cerca avançada d'indicadors manté el contracte de tres columnes principals.
        expect(columnesIndicador).toEqual([
            { field: 'codi', flex: 1 },
            { field: 'nom', flex: 2 },
            { field: 'descripcio', flex: 3 },
        ]);
    });

    it('advancedSearchColumns_quanEsConsultenLesColumnesDimensio_mantenenElsCampsEspecifics', () => {
        // Verifica que les dimensions tenen els camps addicionals que fan servir els formularis.
        expect(columnesDimensio).toEqual(
            expect.arrayContaining([
                { field: 'entornAppId', flex: 1 },
                { field: 'descripcio', flex: 3 },
            ])
        );
    });

    it('advancedSearchColumns_quanEsGenerenColumnesDimensioValorAmbT_tradueixCorrectamentEnTotsElsIdiomes', async () => {
        // Verifica que la fàbrica de columnes tradueix el headerName segons l'idioma actiu (ca, es, en)
        await i18n.changeLanguage('ca');
        expect(getColumnesDimensioValor(i18n.t)).toEqual([
            { field: 'codiNom', flex: 1 },
            { field: 'dimensio.description', headerName: 'Dimensió', flex: 2 },
        ]);

        await i18n.changeLanguage('es');
        expect(getColumnesDimensioValor(i18n.t)).toEqual([
            { field: 'codiNom', flex: 1 },
            { field: 'dimensio.description', headerName: 'Dimensión', flex: 2 },
        ]);

        await i18n.changeLanguage('en');
        expect(getColumnesDimensioValor(i18n.t)).toEqual([
            { field: 'codiNom', flex: 1 },
            { field: 'dimensio.description', headerName: 'Dimension', flex: 2 },
        ]);
    });

    it('advancedSearchColumns_quanCanviaLIdioma_elHookActualitzaElsHeadersSenseRecarregar', async () => {
        // Verifica que el hook useColumnesDimensioValor reacciona als canvis d'idioma de i18next
        await i18n.changeLanguage('ca');
        const { result } = renderHook(() => useColumnesDimensioValor());

        expect(result.current[1].headerName).toBe('Dimensió');

        await act(async () => {
            await i18n.changeLanguage('es');
        });
        expect(result.current[1].headerName).toBe('Dimensión');

        await act(async () => {
            await i18n.changeLanguage('en');
        });
        expect(result.current[1].headerName).toBe('Dimension');

        await act(async () => {
            await i18n.changeLanguage('ca');
        });
        expect(result.current[1].headerName).toBe('Dimensió');
    });
});

