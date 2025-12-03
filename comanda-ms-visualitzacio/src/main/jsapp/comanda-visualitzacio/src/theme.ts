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
