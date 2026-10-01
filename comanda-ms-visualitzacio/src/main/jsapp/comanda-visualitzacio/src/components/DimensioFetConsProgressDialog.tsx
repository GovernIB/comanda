import * as React from 'react';
import { useTranslation } from 'react-i18next';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
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
    error?: boolean;
};

type DimensioFetConsProgressDialogProps = {
    /** Indica si la modal de progrés s'ha de mostrar */
    open: boolean;
    /** Identificador de la dimensió que s'està actualitzant, per a filtrar els events SSE rebuts */
    dimensioId?: string | number | null;
    /**
     * Event que es llença quan el procés real ha acabat (progrés al 100% o error), sigui quina sigui la
     * crida que hagi obert aquesta modal: si ja hi havia una execució en curs, la crida d'aquest client no
     * ha fet cap feina i la seva pròpia resposta HTTP no reflecteix quan acaba el procés real.
     */
    onComplete?: (error: boolean) => void;
    /** Event quant es vol ocultar el diàleg */
    onHide?: () => void;
};

const DimensioFetConsProgressDialog: React.FC<DimensioFetConsProgressDialogProps> = ({ open, dimensioId, onComplete, onHide }) => {
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
            if (payload == null || String(payload.dimensioId) !== String(dimensioId)) {
                return;
            }
            const processats = payload.processats ?? 0;
            const total = payload.total ?? 0;
            setProgress({ processats, total });
            if (payload.error) {
                onComplete?.(true);
            } else if (total > 0 && processats >= total) {
                onComplete?.(false);
            }
        });
    }, [open, dimensioId, subscribe, onComplete]);

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
            {onHide && (
                <DialogActions>
                    <Button onClick={onHide} variant="text">
                        {t($ => $.page.dimensions.action.refreshCons.progress.hideInBackground)}
                    </Button>
                </DialogActions>
            )}
        </Dialog>
    );
};

export default DimensioFetConsProgressDialog;
