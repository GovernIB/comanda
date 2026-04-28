import { createTheme } from '@mui/material/styles';
import { lighten } from '@mui/material';

export const lightTheme = createTheme({
    typography: {
        fontFamily: "'Noto Sans', sans-serif",
        fontWeightRegular: 400,
        fontWeightMedium: 500,
        fontWeightBold: 700,
    },
    palette: {
        mode: 'light',
        primary: {
            main: '#004B99',
            contrastText: '#fff',
        },
        secondary: {
            main: '#2E2E2E',
        },
    },
});

export const darkTheme = createTheme({
    typography: {
        fontFamily: "'Noto Sans', sans-serif",
        fontWeightRegular: 400,
        fontWeightMedium: 500,
        fontWeightBold: 700,
    },
    palette: {
        mode: 'dark',
        primary: {
            main: lighten('#004B99', 0.2),
            contrastText: '#fff',
        },
        secondary: {
            main: '#F6F6F6',
        },
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    variants: [
                        {
                            props: ({ color, variant }) =>
                                variant === 'outlined' && color === 'primary',
                            style: props => ({
                                color: props.theme.palette.secondary.main,
                                borderColor: props.theme.palette.secondary.main,
                            }),
                        },
                    ],
                },
            },
        },
    },
});

export const draculaTheme = createTheme({
    typography: {
        fontFamily: "'Noto Sans', sans-serif",
        fontWeightRegular: 400,
        fontWeightMedium: 500,
        fontWeightBold: 700,
    },
    palette: {
        mode: 'dark',
        primary: {
            main: '#BD93F9',
            contrastText: '#282A36',
        },
        secondary: {
            main: '#F8F8F2',
        },
        background: {
            default: '#282A36',
            paper: '#303341',
        },
        text: {
            primary: '#F8F8F2',
            secondary: '#D6D6C2',
        },
        error: {
            main: '#FF5555',
        },
        warning: {
            main: '#FFB86C',
        },
        success: {
            main: '#50FA7B',
        },
        info: {
            main: '#8BE9FD',
        },
        divider: '#44475A',
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    variants: [
                        {
                            props: ({ color, variant }) =>
                                variant === 'outlined' && color === 'primary',
                            style: {
                                color: '#F8F8F2',
                                borderColor: '#BD93F9',
                            },
                        },
                    ],
                },
            },
        },
    },
});
