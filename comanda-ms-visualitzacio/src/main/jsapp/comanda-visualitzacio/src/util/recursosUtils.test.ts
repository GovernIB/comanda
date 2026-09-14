import { describe, it, expect } from 'vitest';
import { 
    parseStorageSizeToMB, 
    calcularPercentatgeUs, 
    getColorForPercentage 
} from './recursosUtils';

describe('recursosUtils', () => {
    describe('parseStorageSizeToMB', () => {
        it('hauria de convertir GB a MB correctament', () => {
            expect(parseStorageSizeToMB('3 GB')).toBe(3072);
            expect(parseStorageSizeToMB('3.4 GB')).toBe(3481.6);
            expect(parseStorageSizeToMB('10GB')).toBe(10240);
        });

        it('hauria de gestionar comes decimals (format europeu)', () => {
            expect(parseStorageSizeToMB('3,4 GB')).toBe(3481.6);
            expect(parseStorageSizeToMB('1.024,5 MB')).toBe(1024.5);
        });

        it('hauria de retornar 0 si el valor és null, undefined o buit', () => {
            expect(parseStorageSizeToMB(null)).toBe(0);
            expect(parseStorageSizeToMB(undefined)).toBe(0);
            expect(parseStorageSizeToMB('')).toBe(0);
            expect(parseStorageSizeToMB('N/A')).toBe(0);
        });

        it('hauria de assumir MB si no hi ha unitat', () => {
            expect(parseStorageSizeToMB('1024')).toBe(1024);
        });
    });

    describe('calcularPercentatgeUs', () => {
        it('hauria de calcular correctament el percentatge d\'ús normal', () => {
            expect(calcularPercentatgeUs('3400 MB', '3000 MB')).toBe(12);
            expect(calcularPercentatgeUs('100 MB', '25 MB')).toBe(75);
        });

        it('hauria de retornar 0 si el total és 0 (evita divisió per zero)', () => {
            expect(calcularPercentatgeUs('0 MB', '0 MB')).toBe(0);
            expect(calcularPercentatgeUs(undefined, '100 MB')).toBe(0);
        });

        it('hauria de retornar 100 si el disponible és 0', () => {
            expect(calcularPercentatgeUs('100 MB', '0 MB')).toBe(100);
        });

        it('hauria de clampar a 0 si el disponible és major que el total', () => {
            expect(calcularPercentatgeUs('100 MB', '150 MB')).toBe(0);
        });
    });

    describe('getColorForPercentage', () => {
        it('hauria de retornar "success" per percentatges de 0 a 69', () => {
            expect(getColorForPercentage(0)).toBe('success');
            expect(getColorForPercentage(69)).toBe('success');
        });

        it('hauria de retornar "warning" per percentatges de 70 a 89', () => {
            expect(getColorForPercentage(70)).toBe('warning');
            expect(getColorForPercentage(89)).toBe('warning');
        });

        it('hauria de retornar "error" per percentatges de 90 o superiors', () => {
            expect(getColorForPercentage(90)).toBe('error');
            expect(getColorForPercentage(100)).toBe('error');
        });
    });
});