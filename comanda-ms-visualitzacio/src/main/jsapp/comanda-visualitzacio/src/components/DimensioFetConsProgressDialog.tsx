import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Dialog from '@mui/material/Dialog';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import LinearProgress from '@mui/material/LinearProgress';
import Typography from '@mui/material/Typography';
import { useSseContext } from './SseProvider.tsx';

const FET_CONS_PROGRESS_EVENT_TYPE = 'dimensio.fetCons.progress';

type FetConsProgressPayload = {
    dimensioId?: string | number;
    processats?: number;
    total?: number;
};

type DimensioFetConsProgressDialogProps = {
    /** Indica si la modal de progrés s'ha de mostrar */
    open: boolean;
    /** Identificador de la dimensió que s'està actualitzant, per a filtrar els events SSE rebuts */
    dimensioId?: string | number | null;
};

const DimensioFetConsProgressDialog: React.FC<DimensioFetConsProgressDialogProps> = ({ open, dimensioId }) => {
    const { t } = useTranslation();
    const { subscribe } = useSseContext();
    const [progress, setProgress] = React.useState<{ processats: number; total: number } | null>(null);

    React.useEffect(() => {
        if (!open) {
            setProgress(null);
            return;
        }
        return subscribe(FET_CONS_PROGRESS_EVENT_TYPE, event => {
            const payload = event.payload as FetConsProgressPayload | undefined;
            if (payload != null && String(payload.dimensioId) === String(dimensioId)) {
                setProgress({ processats: payload.processats ?? 0, total: payload.total ?? 0 });
            }
        });
    }, [open, dimensioId, subscribe]);

    if (!open) {
        return null;
    }

    const determinate = progress != null && progress.total > 0;
    const value = determinate ? Math.min(100, Math.round((progress!.processats / progress!.total) * 100)) : undefined;

    return (
        <Dialog open={open} maxWidth="sm" fullWidth disableEscapeKeyDown>
            <DialogTitle>{t($ => $.page.dimensions.action.refreshCons.progress.title)}</DialogTitle>
            <DialogContent>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, py: 2 }}>
                    <LinearProgress variant={determinate ? 'determinate' : 'indeterminate'} value={value} />
                    <Typography variant="body2" color="text.secondary">
                        {determinate
                            ? t($ => $.page.dimensions.action.refreshCons.progress.processed, {
                                processats: progress!.processats,
                                total: progress!.total,
                            })
                            : t($ => $.page.dimensions.action.refreshCons.progress.waiting)}
                    </Typography>
                </Box>
            </DialogContent>
        </Dialog>
    );
};

export default DimensioFetConsProgressDialog;
