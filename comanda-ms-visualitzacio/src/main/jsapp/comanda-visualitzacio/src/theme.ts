import { createTheme } from '@mui/material/styles';

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
    components: {
        /*MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: '0px',
                    '&.appMenuItem': {
                        textTransform: 'none',
                        marginRight: '42px',
                        '&:hover': {
                            textDecoration: 'underline',
                            '--variant-containedBg': '#fff',
                            '--variant-textBg': '#fff',
                            '--variant-outlinedBg': '#fff',
                        }
                    }
                },
            },
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    borderRadius: '0px',
                },
            },
        },
        MuiToolbar: {
            styleOverrides: {
                root: {
                    color: '#000',
                    minHeight: '48px !important',
                    '& input': {
                        height: '1em',
                    },
                    '& .MuiSelect-select': {   // Específicament pels selects
                        height: '1em',
                        minHeight: '1em',
                        padding: '5.5px 14px',
                    },

                },

            },
        },
        MuiAvatar: {
            styleOverrides: {
                root: {
                    backgroundColor: '#CACACA',
                    '&.MuiAvatar-colorDefault': {
                        backgroundColor: '#CACACA', // Assegura't que s'apliqui a aquesta classe específica
                    },
                },
            }
        },
        MuiInputBase: {
            styleOverrides: {
                root: {
                    fontSize: '14px',
                    padding: '0px 0px',
                },
                input: {
                    fontSize: '14px',
                }
            }
        },
        MuiFormLabel: {
            styleOverrides: {
                root: {
                    fontSize: '14px',
                    fontWeight: 200,
                    color: '#666666',
                }
            }
        },
        MuiIcon: {
            styleOverrides: {
                root: {
                    fontSize: '18px', // Mida base
                    // marginRight: '4px',
                },
            }
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    '&.MuiChip-sizeSmall .MuiChip-label': {
                        fontSize: '14px'
                    },
                    '&.MuiChip-sizeMedium .MuiChip-label': {
                        fontSize: '16px'
                    }
                }
            }
        },
        MuiTypography: {
            styleOverrides: {
                h6: {
                    fontSize: '1.4rem',
                },
            },
        },
        MuiFormControlLabel: {
            styleOverrides: {
                label: {
                    fontSize: '14px',
                },
            },
        },*/
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
            main: '#004B99',
            contrastText: '#fff',
        },
        secondary: {
            main: '#F6F6F6',
        },
    },
});
