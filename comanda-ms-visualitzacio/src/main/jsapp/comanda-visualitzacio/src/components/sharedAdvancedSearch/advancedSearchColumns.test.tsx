import { describe, expect, it } from 'vitest';
import {
    columnesDimensio,
    columnesDimensioValor,
    columnesIndicador,
} from './advancedSearchColumns';

describe('advancedSearchColumns', () => {
    it('advancedSearchColumns_quanEsConsultenLesColumnesIndicador_mantenenLEsquemaEsperat', () => {
        // Comprova que la cerca avançada d'indicadors manté el contracte de tres columnes principals.
        expect(columnesIndicador).toEqual([
            { field: 'codi', flex: 1 },
            { field: 'nom', flex: 2 },
            { field: 'descripcio', flex: 3 },
        ]);
    });

    it('advancedSearchColumns_quanEsConsultenLesColumnesDimensio_iValor_mantenenElsCampsEspecifics', () => {
        // Verifica que les dimensions i els valors tenen els camps addicionals que fan servir els formularis.
        expect(columnesDimensio).toEqual(
            expect.arrayContaining([
                { field: 'entornAppId', flex: 1 },
                { field: 'descripcio', flex: 3 },
            ])
        );
        expect(columnesDimensioValor).toEqual([
            { field: 'valor', flex: 1 },
            { field: 'dimensio.description', headerName: 'Dimensio', flex: 2 },
        ]);
    });
});
