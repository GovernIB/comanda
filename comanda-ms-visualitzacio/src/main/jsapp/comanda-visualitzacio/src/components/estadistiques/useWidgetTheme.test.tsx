import React from 'react';
import { renderHook } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import { useWidgetTheme } from './useWidgetTheme';

vi.mock('../../util/colorUtil', () => ({
    isLightColor: vi.fn((color: string) => {
        // Simula que los colores claros terminan en "light" o son blancos/grises claros
        return color.includes('light') || 
               color === '#FFFFFF' || 
               color === '#ffffff' ||
               color === '#F5F5F5' ||
               color === '#EEEEEE';
    }),
    createTransparentColor: vi.fn((color: string, opacity: number) => {
        return `${color}_transparent_${opacity}`;
    }),
}));

const wrapper = ({ children }: { children: React.ReactNode }) => (
    <ThemeProvider theme={createTheme()}>{children}</ThemeProvider>
);

describe('useWidgetTheme', () => {
    it('useWidgetTheme_quanNoEsPassanOpcions_usaValorsPerDefecteDelTema', () => {
        const { result } = renderHook(() => useWidgetTheme({}), { wrapper });

        expect(result.current.textColor).toBeDefined();
        expect(result.current.backgroundColor).toBeDefined();
        expect(result.current.voraColor).toBeDefined();
        expect(result.current.isWhiteBackground).toBe(true);
        expect(result.current.contrastTextColor).toBe('#000000');
        expect(result.current.voraAmple).toBe(0);
    });

    it('useWidgetTheme_quanEsPassaColorTextPersonalitzat_lutilitza', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorText: '#FF0000' }),
            { wrapper }
        );

        expect(result.current.textColor).toBe('#FF0000');
    });

    it('useWidgetTheme_quanEsPassaColorFonsClar_calculaContrastFosc', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorFons: '#FFFFFF' }),
            { wrapper }
        );

        expect(result.current.backgroundColor).toBe('#FFFFFF');
        expect(result.current.isWhiteBackground).toBe(true);
        expect(result.current.contrastTextColor).toBe('#000000');
        expect(result.current.bg).toBe('none');
    });

    it('useWidgetTheme_quanEsPassaColorFonsFosc_calculaContrastClar', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorFons: '#000000' }),
            { wrapper }
        );

        expect(result.current.backgroundColor).toBe('#000000');
        expect(result.current.isWhiteBackground).toBe(false);
        expect(result.current.contrastTextColor).toBe('#FFFFFF');
        expect(result.current.bg).toContain('linear-gradient');
    });

    it('useWidgetTheme_quanEsPassaColorVoraPersonalitzat_lutilitza', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorVora: '#00FF00' }),
            { wrapper }
        );

        expect(result.current.voraColor).toBe('#00FF00');
    });

    it('useWidgetTheme_quanMostrarVoraEsTrueIampleVoraNoEsDefineix_usa1', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ mostrarVora: true }),
            { wrapper }
        );

        expect(result.current.voraAmple).toBe(1);
    });

    it('useWidgetTheme_quanEsDefineixAmpleVora_lutilitza', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ mostrarVora: true, ampleVora: 3 }),
            { wrapper }
        );

        expect(result.current.voraAmple).toBe(3);
    });

    it('useWidgetTheme_quanMostrarVoraEsFalseIampleVoraNoEsDefineix_usa0', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ mostrarVora: false }),
            { wrapper }
        );

        expect(result.current.voraAmple).toBe(0);
    });

    it('useWidgetTheme_quanFonsEsFosc_generaGradientAmbTransparencia', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorFons: '#123456' }),
            { wrapper }
        );

        expect(result.current.bg).toContain('linear-gradient');
        expect(result.current.bg).toContain('#123456');
        expect(result.current.bg).toContain('_transparent_0.75');
    });

    it('useWidgetTheme_quanFonsEsClar_bgEsNone', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorFons: '#FFFFFF' }),
            { wrapper }
        );

        expect(result.current.bg).toBe('none');
        expect(result.current.bgColor).toBe('#FFFFFF !important');
    });

    it('useWidgetTheme_quanFonsEsFosc_bgColorEsTransparent', () => {
        const { result } = renderHook(
            () => useWidgetTheme({ colorFons: '#000000' }),
            { wrapper }
        );

        expect(result.current.bgColor).toBe('transparent');
    });

    it('useWidgetTheme_quanEsPassenTotesLesOpcions_lesCombinaCorrectament', () => {
        const { result } = renderHook(
            () => useWidgetTheme({
                colorText: '#FF0000',
                colorFons: '#000000',
                colorVora: '#00FF00',
                mostrarVora: true,
                ampleVora: 2,
            }),
            { wrapper }
        );

        expect(result.current.textColor).toBe('#FF0000');
        expect(result.current.backgroundColor).toBe('#000000');
        expect(result.current.voraColor).toBe('#00FF00');
        expect(result.current.isWhiteBackground).toBe(false);
        expect(result.current.contrastTextColor).toBe('#FFFFFF');
        expect(result.current.voraAmple).toBe(2);
    });
});