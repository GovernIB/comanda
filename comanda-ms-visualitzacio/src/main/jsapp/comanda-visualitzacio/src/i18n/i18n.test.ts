import { expect, test } from 'vitest';
import i18n from './i18n';

const stringTranslationPath = "components.clear";
const selectorTranslationPath = ($: any) => $.components.clear;

// This test verifies that the i18next configuration remains backwards compatible
// by ensuring both string-based and selector-based translation keys produce
// the same translation output. It specifically validates that useTranslationStringKey
// continues to work with traditional string keys alongside the newer selector syntax.
test('i18n backwards compatible string translation', () => {
    // @ts-ignore
    const translationFromStringKey = i18n.t(stringTranslationPath);
    const translationFromSelectorKey = i18n.t(selectorTranslationPath);
    expect(translationFromStringKey).toBe(translationFromSelectorKey);
})

// Els components de src/lot2 tradueixen amb l'espai de noms 'lot2' (p. ex. els textos del peu).
test('i18n registra els textos de lot2 a l\'espai de noms lot2', () => {
    expect(i18n.getResource('ca', 'lot2', 'footer.sitemap')).toBe('Mapa del lloc web');
    expect(i18n.hasResourceBundle('es', 'lot2')).toBe(true);
    expect(i18n.hasResourceBundle('en', 'lot2')).toBe(true);
})
