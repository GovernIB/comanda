import { ThemeProvider } from '@emotion/react';
import { FC } from 'react';
import { useUserContext } from './UserContext';
import { darkTheme, lightTheme } from '../theme';

const MuiThemeProvider: FC<{ children?: React.ReactNode }> = ({ children }) => {
    const { user } = useUserContext();
    const theme = user?.temaObscur ? darkTheme : lightTheme ;
    return <ThemeProvider theme={theme}>{children}</ThemeProvider>;
};

export default MuiThemeProvider;
