import { renderHook } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import useTranslationStringKey from './useTranslationStringKey';

const { tMock, useTranslationMock } = vi.hoisted(() => {
    const tMock = vi.fn((key: string) => `trad:${key}`);
    const useTranslationMock = vi.fn(() => ({
        t: tMock,
        i18n: { language: 'ca' },
        ready: true,
    }));
    return { tMock, useTranslationMock };
});

vi.mock('react-i18next', () => ({
    useTranslation: useTranslationMock,
}));

describe('useTranslationStringKey', () => {
    it('useTranslationStringKey_quanEsCridaAmbUnaClauString_delegaEnLaTOriginal', () => {
        // Comprova que el hook manté la compatibilitat amb claus de traducció en format string.
        const { result } = renderHook(() => useTranslationStringKey());

        expect(result.current.t('components.clear')).toBe('trad:components.clear');
        expect(tMock).toHaveBeenCalledWith('components.clear');
    });

    it('useTranslationStringKey_quanEsConstrueix_retornaLaRestaDePropietatsDeUseTranslation', () => {
        // Verifica que el hook preserva la resta de dades del contracte de react-i18next.
        const { result } = renderHook(() => useTranslationStringKey());

        expect(result.current.i18n.language).toBe('ca');
        expect(result.current.ready).toBe(true);
    });
});
