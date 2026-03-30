import { describe, expect, it } from 'vitest';
import {
    formatDate,
    formatEndOfDay,
    formatIso,
    formatStartOfDay,
    ISO_DATE_FORMAT,
} from './dateUtils';

describe('formatDate', () => {
    it('formatDate_quanRepUnaDataValida_retornaElFormatPerDefecte', () => {
        // Comprova que la utilitat formata la data amb el patró per defecte.
        expect(formatDate('2025-03-12T10:15:30')).toBe('12/03/2025 10:15:30');
    });

    it('formatDate_quanRepUnFormatCustom_retornaLaDataEnAquestFormat', () => {
        // Verifica que es respecta el format explícitament indicat pel consumidor.
        expect(formatDate('2025-03-12T10:15:30', 'YYYY/MM/DD')).toBe('2025/03/12');
    });

    it('formatDate_quanNoRepData_retornaNull', () => {
        // Comprova el comportament defensiu quan no arriba cap data d'entrada.
        expect(formatDate('')).toBeNull();
    });
});

describe('formatIso', () => {
    it('formatIso_quanRepUnaData_retornaElFormatIsoDefinitPelModul', () => {
        // Verifica que la utilitat usa el patró ISO intern compartit pel front.
        expect(formatIso('2025-03-12T10:15:30')).toBe('2025-03-12T10:15:30');
        expect(ISO_DATE_FORMAT).toBe('YYYY-MM-DDTHH:mm:ss');
    });
});

describe('formatStartOfDay', () => {
    it('formatStartOfDay_quanRepUnaData_retornaLiniciDelDia', () => {
        // Comprova que la utilitat fixa l'hora al primer segon del dia.
        expect(formatStartOfDay('2025-03-12T10:15:30')).toBe('2025-03-12T00:00:00');
    });
});

describe('formatEndOfDay', () => {
    it('formatEndOfDay_quanRepUnaData_retornaElFinalDelDia', () => {
        // Verifica que la utilitat fixa l'hora al darrer segon del dia.
        expect(formatEndOfDay('2025-03-12T10:15:30')).toBe('2025-03-12T23:59:59');
    });
});
