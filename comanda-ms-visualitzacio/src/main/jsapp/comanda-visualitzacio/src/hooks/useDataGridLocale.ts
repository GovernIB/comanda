import { caES, enUS, esES } from '@mui/x-data-grid/locales';
import { useMemo } from 'react';
import { useTranslation } from 'react-i18next';

const useDataGridLocale = () => {
    const { i18n } = useTranslation();

    return useMemo(() => {
        switch (i18n.language) {
            case 'ca':
                return caES.components.MuiDataGrid.defaultProps.localeText;
            case 'es':
                return esES.components.MuiDataGrid.defaultProps.localeText;
            case 'en':
            default:
                return enUS.components.MuiDataGrid.defaultProps.localeText;
        }
    }, [i18n.language]);
};

export default useDataGridLocale;
