import { renderHook } from '@testing-library/react';
import { caES, enUS, esES } from '@mui/x-data-grid/locales';
import { describe, expect, it, vi } from 'vitest';
import useDataGridLocale from './useDataGridLocale';

const useTranslationMock = vi.fn();

vi.mock('react-i18next', () => ({
    useTranslation: () => useTranslationMock(),
}));

describe('useDataGridLocale', () => {
    it('useDataGridLocale_quanLidiomaEsCatala_retornaElLocaleDeCa', () => {
        // Comprova que el hook retorna el paquet de textos del DataGrid en català.
        useTranslationMock.mockReturnValue({ i18n: { language: 'ca' } });

        const { result } = renderHook(() => useDataGridLocale());

        expect(result.current).toBe(caES.components.MuiDataGrid.defaultProps.localeText);
    });

    it('useDataGridLocale_quanLidiomaEsCastella_retornaElLocaleDes', () => {
        // Verifica que el hook selecciona correctament els textos en castellà.
        useTranslationMock.mockReturnValue({ i18n: { language: 'es' } });

        const { result } = renderHook(() => useDataGridLocale());

        expect(result.current).toBe(esES.components.MuiDataGrid.defaultProps.localeText);
    });

    it('useDataGridLocale_quanLidiomaNoEsSuportat_retornaElLocaleAnglesPerDefecte', () => {
        // Comprova el comportament per defecte quan l'idioma no té un mapping específic.
        useTranslationMock.mockReturnValue({ i18n: { language: 'fr' } });

        const { result } = renderHook(() => useDataGridLocale());

        expect(result.current).toBe(enUS.components.MuiDataGrid.defaultProps.localeText);
    });
});
