import { ThemeProvider } from '@emotion/react';
import { FC } from 'react';
import { useUserContext } from './UserContext';
import { darkTheme, draculaTheme, lightTheme } from '../theme';
import { useMediaQuery } from '@mui/material';
import { TemaAplicacio } from '../types/usuari.model.tsx';

const useComandaTheme = () => {
    const { user } = useUserContext();
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
    const systemTheme = prefersDarkMode ? darkTheme : lightTheme;
    if (!user || user.temaAplicacio == null)
        return systemTheme;
    const temaAplicacio = user.temaAplicacio;

    switch (temaAplicacio) {
        case TemaAplicacio.CLAR:
            return lightTheme;
        case TemaAplicacio.OBSCUR:
            return darkTheme;
        case TemaAplicacio.DRACULA:
            return draculaTheme;
        case TemaAplicacio.SISTEMA:
        default:
            return systemTheme;
    }
}

const MuiThemeProvider: FC<{ children?: React.ReactNode }> = ({ children }) => {
    const theme = useComandaTheme();
    return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};

export default MuiThemeProvider;
