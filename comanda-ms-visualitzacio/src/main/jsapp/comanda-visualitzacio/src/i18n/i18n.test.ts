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
