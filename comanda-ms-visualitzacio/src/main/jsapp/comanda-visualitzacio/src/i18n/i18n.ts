import i18next from 'i18next';
import { initReactI18next } from 'react-i18next';
import translationCa from './translationCa';
import translationEn from './translationEn';
import translationEs from './translationEs';
import { LOT2_I18N_NS, lot2Translations } from '../lot2/i18n';

const resources = {
    ca: { translation: translationCa, [LOT2_I18N_NS]: lot2Translations.ca },
    en: { translation: translationEn, [LOT2_I18N_NS]: lot2Translations.en },
    es: { translation: translationEs, [LOT2_I18N_NS]: lot2Translations.es },
};

// Els tipus de traducció de comanda només descriuen el seu espai de noms: els components de `lot2`
// tradueixen amb `useLot2Translation()`, que no depèn d'aquests tipus.
export type caResourceType = { translation: typeof translationCa };

i18next.use(initReactI18next).init({
    resources,
    fallbackLng: 'ca',
    interpolation: {
        escapeValue: false,
    },
});

export default i18next;
