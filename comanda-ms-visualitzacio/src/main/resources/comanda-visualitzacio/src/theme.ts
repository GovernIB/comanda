import { createTheme } from '@mui/material/styles';

const theme = createTheme({
    typography: {
        fontFamily: "'Noto Sans', sans-serif",
        fontWeightRegular: 400,
        fontWeightMedium: 500,
        fontWeightBold: 700,
    },
    palette: {
        primary: {
            main: '#004B99',
            contrastText: "#fff"
        },
    },
    components: {
        MuiButton: {
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
                    fontSize: '1.5rem',     // Mida de la font
                },
            },
        },
    }
});

export default theme;
