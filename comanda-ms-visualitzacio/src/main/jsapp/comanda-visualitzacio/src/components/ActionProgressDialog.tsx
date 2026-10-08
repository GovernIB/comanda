import * as React from 'react';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import LinearProgress from '@mui/material/LinearProgress';
import Typography from '@mui/material/Typography';
import { useSseContext } from './SseProvider.tsx';

export type ActionProgressPayload = {
    id?: string | number;
    resourceId?: string | number;
    dimensioId?: string | number;
    entitatId?: string | number;
    processats?: number;
    total?: number;
    error?: boolean;
    [key: string]: unknown;
};

export type ActionProgressDialogProps<T extends ActionProgressPayload = ActionProgressPayload> = {
    /** Indica si la modal de progrés s'ha de mostrar */
    open: boolean;
    /** Identificador del recurs que s'està processant */
    itemId?: string | number | null;
    /** Tipus d'esdeveniment SSE al qual subscriure's */
    eventType: string;
    /** Títol del diàleg */
    title: string;
    /** Missatge d'espera mentre no s'ha rebut cap progrés determinat */
    waitingMessage: string;
    /** Missatge formattat amb els processats sobre el total */
    processedMessage: (processats: number, total: number) => string;
    /** Text del botó per ocultar el diàleg en segon pla */
    hideButtonLabel?: string;
    /** Selector opcional per obtenir l'identificador del recurs des del payload */
    getItemId?: (payload: T) => string | number | undefined;
    /** Callback invocat quan el procés ha acabat (èxit o error) */
    onComplete?: (error: boolean) => void;
    /** Callback invocat quan l'usuari prem per ocultar el diàleg */
    onHide?: () => void;
};

/**
 * Diàleg genèric de progrés per a accions de llarga durada notificades via Server-Sent Events (SSE).
 * Gestiona la subscripció al canal SSE, l'estat dels comptadors i la resolució automàtica per onComplete
 * tant si s'arriba al 100% com si el total és 0 (llista buida).
 */
const ActionProgressDialog = <T extends ActionProgressPayload = ActionProgressPayload>({
    open,
    itemId,
    eventType,
    title,
    waitingMessage,
    processedMessage,
    hideButtonLabel,
    getItemId,
    onComplete,
    onHide,
}: ActionProgressDialogProps<T>) => {
    const { subscribe } = useSseContext();
    const [progress, setProgress] = React.useState<{ processats: number; total: number } | null>(null);
    const onCompleteRef = React.useRef(onComplete);
    onCompleteRef.current = onComplete;
    const getItemIdRef = React.useRef(getItemId);
    getItemIdRef.current = getItemId;
    const completedRef = React.useRef(false);

    React.useEffect(() => {
        if (!open) {
            setProgress(null);
            completedRef.current = false;
            return;
        }
        return subscribe(eventType, event => {
            const payload = event.payload as T | undefined;
            if (payload == null) {
                return;
            }
            const payloadId = getItemIdRef.current
                ? getItemIdRef.current(payload)
                : (payload.dimensioId ?? payload.entitatId ?? payload.resourceId ?? payload.id);
            if (itemId != null) {
                if (payloadId == null || String(payloadId) !== String(itemId)) {
                    return;
                }
            } else if (payloadId != null) {
                return;
            }

            if (payload.error) {
                if (!completedRef.current) {
                    completedRef.current = true;
                    onCompleteRef.current?.(true);
                }
                return;
            }

            if (payload.total == null || payload.processats == null) {
                return;
            }

            const processats = payload.processats;
            const total = payload.total;
            setProgress({ processats, total });

            if (processats >= total) {
                // >= total fins i tot quan total === 0 (llista buida o sense elements a processar):
                // garanteix que la modal no es quedi bloquejada per sempre esperant un progrés que mai no arribarà.
                if (!completedRef.current) {
                    completedRef.current = true;
                    onCompleteRef.current?.(false);
                }
            }
        });
    }, [open, itemId, eventType, subscribe]);

    if (!open) {
        return null;
    }

    const determinate = progress != null && progress.total > 0;
    const value = determinate
        ? Math.max(0, Math.min(100, Math.round((progress!.processats / progress!.total) * 100)))
        : undefined;

    return (
        <Dialog open={open} maxWidth="sm" fullWidth disableEscapeKeyDown>
            <DialogTitle>{title}</DialogTitle>
            <DialogContent>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1, py: 2 }}>
                    <LinearProgress variant={determinate ? 'determinate' : 'indeterminate'} value={value} />
                    <Typography variant="body2" color="text.secondary">
                        {determinate
                            ? processedMessage(progress!.processats, progress!.total)
                            : waitingMessage}
                    </Typography>
                </Box>
            </DialogContent>
            {onHide && (
                <DialogActions>
                    <Button onClick={onHide} variant="text">
                        {hideButtonLabel}
                    </Button>
                </DialogActions>
            )}
        </Dialog>
    );
};

export default ActionProgressDialog;
