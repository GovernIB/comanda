import React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Button from '@mui/material/Button';
import { StacktraceBlock } from '../RickTextDetail';

type SalutErrorBoundaryFallbackProps = {
    error?: Partial<Error>;
    /** Missatge a mostrar; per defecte, el d'error en mostrar un gràfic. */
    message?: string;
};

export const SalutErrorBoundaryFallback: React.FC<SalutErrorBoundaryFallbackProps> = ({ error, message }) => {
    const { t } = useTranslation();
    const [detailsOpen, setDetailsOpen] = React.useState(false);

    return (
        <>
            <Box
                sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                    <Typography color="error">
                        {message ?? t($ => $.page.salut.latencia.error)}
                    </Typography>
                    <IconButton
                        size="small"
                        color="error"
                        title={t($ => $.page.salut.latencia.errorDetailsButton)}
                        onClick={(event) => {
                            event.stopPropagation();
                            setDetailsOpen(true);
                        }}
                        onMouseDown={(event) => event.stopPropagation()}
                        sx={{ pointerEvents: 'auto' }}
                    >
                        <Icon fontSize="small">info_outline</Icon>
                    </IconButton>
                </Box>
            </Box>
            <Dialog open={detailsOpen} onClose={() => setDetailsOpen(false)} fullWidth maxWidth="md">
                <DialogTitle>{t($ => $.page.salut.latencia.errorDetailsTitle)}</DialogTitle>
                <DialogContent>
                    <StacktraceBlock
                        title={t($ => $.common.error)}
                        value={error?.stack || error?.message || t($ => $.page.widget.noErrorTrace)}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDetailsOpen(false)}>
                        {t($ => $.page.salut.latencia.errorDetailsClose)}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};
