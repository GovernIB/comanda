import { Theme } from "@mui/material/styles";

export const estils = {
    /*styles.paperContainer(bgColor, bg, colors.textColor, mostrarVora, voraAmple, colors.voraColor, onClick, theme)*/
    paperContainer: (bgColor: string, bg: string, textColor: string,
                     mostrarVora: boolean, voraAmple: number, voraColor: string,
                     onClick: (() => void) | undefined, theme: Theme) => ({
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        borderRadius: '.6rem',
        backgroundColor: bgColor,
        background: bg,
        color: textColor,
        border: mostrarVora ? `${voraAmple}px solid ${voraColor}` : 'none',
        cursor: onClick ? 'pointer' : 'default',
        height: '100%',
        transition: 'all 0.2s ease-in-out',
        '&:hover': {
            boxShadow: onClick ? theme.shadows[4] : undefined,
        },
    }),
    titleContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexGrow: 0,
        flexShrink: 0,
        flexBasis: 'auto',
        pt: 2,
        px: 2,
    },
    titleText: {
        letterSpacing: '0.025em',
        fontWeight: '600',
        fontSize: '1.4em',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        width: '100%',
    },
    contentContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'flex-end',
        justifyContent: 'flex-start',
        flexGrow: 1,
        flexShrink: 1,
        flexBasis: 'auto',
        p: 2,
        pt: 1,
        pb: 0,
    },
    contentText: (preview: boolean) => ({
        // height: preview ? '80px' : '120px', // Altura fixa del contenidor
        display: 'flex',
        flexDirection: 'column', // Els elements s'apilen verticalment
        justifyContent: 'flex-end', // Alinea els elements al final (inferior)
        alignItems: 'flex-start', // Alinea horitzontalment a l'inici (esquerra)
        pr: 1, // Padding intern
        mb: 0, // Margin bottom
    }),
    valueText: (preview: boolean) => ({
        fontSize: preview ? '5em' : '5em',
        fontWeight: '500',
        textAlign: 'center',
        lineHeight: '1',
        zIndex:2,
    }),
    unitText: (preview: boolean) => ({
        fontSize: preview ? '1em' : '1em',
        textAlign: 'center',
        lineHeight: '1',
        zIndex:2,
    }),
    footerContainer: {
        display: 'flex',
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexGrow: 0,
        flexShrink: 0,
        flexBasis: 'auto',
        p: 2,
        py: 1,
    },
    descText: (textColor: string) => ({
        flexGrow: 1,
        flexShrink: 1,
        fontWeight: '500',
        fontSize: '1.0em',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        minWidth: '260px',
        color: textColor,
        zIndex:2,
    }),
    percText: (textColor: string) => ({
        flexShrink: 0,
        fontWeight: '600',
        fontSize: '1.2em',
        overflow: 'visible',
        textAlign: 'right',
        whiteSpace: 'nowrap',
        ml: 2,
        color: textColor,
        zIndex:2,
    }),
    iconContainer: {
    },
    icon: (preview: boolean, color: string, bgColor: string) => ({
        color,
        // width: preview ? '1em' : '1.4em',
        // height: preview ? '1em' : '1.4em',
        // fontSize: preview ? '1.8em' : '1.8em',
        backgroundColor: bgColor,
        borderRadius: '50%',
        // padding: '0.1em',
        // border: 'solid',
        // borderWidth: '0.1em',
        // boxSizing: 'content-box',
        position: 'absolute',
        bottom: '-60px',
        right: '-60px',
        border: 'none',
        fontSize: '200px',
        opacity: '0.3',
    }),
    tableContainerBox: {
        display: 'flex',
        flexDirection: 'column',
        flex: 1,
        px: 2,
        pt: 1,
        pb: 0,
        width: '100%',
        overflow: 'auto',
    },
    tableContainer: (mostrarVora: boolean, voraAmple: number | undefined, voraColor: string, taulaBgColor: string /*, theme: Theme*/) => ({
        borderCollapse: 'separate',
        border: mostrarVora ? `${voraAmple}px solid ${voraColor}` : 'none',
        backgroundColor: taulaBgColor,
    }),
    tableHeader: (textColor: string, bgColor: string,
                  horDividerColor: string, horDividerAmple: number | undefined,
                  verDivider: boolean, verDividerColor: string, verDividerAmple: number | undefined) => ({
        color: textColor,
        fontWeight: '600',
        backgroundColor: bgColor,
        '& > *': {
            borderRight: verDivider ? `${verDividerAmple ?? 1}px solid ${verDividerColor}` : 'none', // Separador de columna
            borderBottom: `${horDividerAmple ?? 1}px solid ${horDividerColor}`, // Separador de fila
        },
        '& > *:last-child': {
            borderRight: "none", // Elimina separador de l'última columna
        },
    }),
    tableRow: (textColor: string, bgColor: string,
               horDivider: boolean, horDividerColor: string, horDividerAmple: number | undefined,
               verDivider: boolean, verDividerColor: string, verDividerAmple: number | undefined) => ({
        color: textColor,
        fontWeight: '400',
        backgroundColor: bgColor,
        '& > *': {
            borderRight: verDivider ? `${verDividerAmple ?? 1}px solid ${verDividerColor}` : 'none', // Separador de columna
            borderBottom: horDivider ? `${horDividerAmple ?? 1}px solid ${horDividerColor}` : 'none', // Separador de fila
        },
        '& > *:last-child': {
            borderRight: "none",
        },
    }),
    entornCodi: {
        fontWeight: 600,
    },
    errorIcon: (theme: Theme) => ({
        color: theme.palette.error.main,
        fontSize: '3rem',
        marginRight: 1
    }),
    errorAccordion: {
        margin: 0,
        boxShadow: 'none',
        width: '100%',
        maxWidth: '100%',
        overflow: 'hidden',
        '&:before': {
            display: 'none',
        },
        position: "relative",
        zIndex: 10,
    },
    errorSummary: (theme: Theme) => ({
        color: theme.palette.error.main,
        fontWeight: 'bold',
        padding: '0 16px',
    }),
    errorDetails: (theme: Theme) => ({
        whiteSpace: 'pre-wrap',
        fontFamily: 'monospace',
        fontSize: '0.875rem',
        backgroundColor: theme.palette.grey[100],
        padding: 2,
        maxHeight: '200px',
        overflow: 'auto',
    })

};

export default estils;
