import { ThemeProvider } from '@emotion/react';
import { FC } from 'react';
import { useUserContext } from './UserContext';
import { darkTheme, lightTheme } from '../theme';
import { useMediaQuery } from '@mui/material';

const useComandaTheme = () => {
    const { user } = useUserContext();
    const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
    const systemTheme = prefersDarkMode ? darkTheme : lightTheme;
    if (!user || user.temaObscur == null)
        return systemTheme;
    return user.temaObscur ? darkTheme : lightTheme;
}

const MuiThemeProvider: FC<{ children?: React.ReactNode }> = ({ children }) => {
    const theme = useComandaTheme();
    return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};

export default MuiThemeProvider;
