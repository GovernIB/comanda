import { describe, expect, it } from 'vitest';
import { isDataInGroup, toXAxisDataGroups } from './dataGroup';

describe('isDataInGroup', () => {
    it('isDataInGroup_quanLAgrupacioEsMes_comparaAnyIMes', () => {
        // Comprova que per agrupació mensual només es comparen any i mes.
        expect(isDataInGroup('2025-03-12T10:30:00', '2025-03-01T00:00:00', 'MES')).toBe(true);
    });

    it('isDataInGroup_quanLAgrupacioEsHora_comparaFinsALHora', () => {
        // Verifica que l'agrupació per hora fa matching fins al tram horari.
        expect(isDataInGroup('2025-03-12T10:30:00', '2025-03-12T10:59:59', 'HORA')).toBe(true);
    });

    it('isDataInGroup_quanLesDatesNoPertanyenAlMateixGrup_retornaFalse', () => {
        // Comprova que el helper retorna fals quan les dates cauen en grups diferents.
        expect(isDataInGroup('2025-03-12T10:30:00', '2025-04-01T00:00:00', 'MES')).toBe(false);
    });
});

describe('toXAxisDataGroups', () => {
    it('toXAxisDataGroups_quanLAgrupacioEsAny_retornaNomesLAny', () => {
        // Verifica que els grups d'eix X es redueixen a l'any quan l'agrupació és anual.
        expect(toXAxisDataGroups(['2025-03-12T10:30:00'], 'ANY')).toEqual(['2025']);
    });

    it('toXAxisDataGroups_quanLAgrupacioEsHora_formataLHoraAmbZeros', () => {
        // Comprova el format específic de l'eix X per a agrupacions per hora.
        expect(toXAxisDataGroups(['2025-03-12T10:30:00'], 'HORA')).toEqual(['12T10:00']);
    });

    it('toXAxisDataGroups_quanLAgrupacioEsMinut_retornaHoresIMinuts', () => {
        // Verifica que les agrupacions per minut retenen només hores i minuts.
        expect(toXAxisDataGroups(['2025-03-12T10:30:00'], 'MINUT')).toEqual(['10:30']);
    });
});
