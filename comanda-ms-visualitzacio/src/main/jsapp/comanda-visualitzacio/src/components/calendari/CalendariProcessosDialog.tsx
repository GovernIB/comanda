import React, { useState } from 'react';
import {
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Button,
    Typography,
    Box,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Chip,
    LinearProgress,
    IconButton,
    Icon,
    CircularProgress,
    FormControlLabel,
    Switch,
    Tooltip,
} from '@mui/material';
import dayjs from 'dayjs';
import { useTranslation } from 'react-i18next';
import { ProcesBaixaPrioritat } from './CalendariTypes';

export interface CalendariProcessosDialogProps {
    open: boolean;
    onClose: () => void;
    processos: ProcesBaixaPrioritat[];
    isLoading?: boolean;
    onRefresh: () => void;
    onCancelProces: (id: string) => Promise<boolean>;
    entornAppId?: number | string;
}

const CalendariProcessosDialog: React.FC<CalendariProcessosDialogProps> = ({
    open,
    onClose,
    processos,
    isLoading = false,
    onRefresh,
    onCancelProces,
    entornAppId,
}) => {
    const { t } = useTranslation();
    const [mostrarTots, setMostrarTots] = useState(false);
    const [cancelingId, setCancelingId] = useState<string | null>(null);

    const filteredProcessos = React.useMemo(() => {
        if (mostrarTots || !entornAppId) {
            return processos;
        }
        return processos.filter((p) => String(p.entornAppId) === String(entornAppId));
    }, [processos, mostrarTots, entornAppId]);

    const getEstatChip = (estat: string, missatge?: string) => {
        let chip: React.ReactElement;
        switch (estat) {
            case 'EN_EXECUCIO':
                chip = (
                    <Chip
                        size="small"
                        color="warning"
                        icon={<CircularProgress size={12} color="inherit" />}
                        label={t($ => $.calendari.estat_en_execucio)}
                    />
                );
                break;
            case 'FINALITZAT':
                chip = (
                    <Chip
                        size="small"
                        color="success"
                        icon={<Icon sx={{ fontSize: '16px !important' }}>check_circle</Icon>}
                        label={t($ => $.calendari.estat_finalitzat)}
                    />
                );
                break;
            case 'ERROR':
                chip = (
                    <Chip
                        size="small"
                        color="error"
                        icon={<Icon sx={{ fontSize: '16px !important' }}>error</Icon>}
                        label={t($ => $.calendari.estat_error)}
                    />
                );
                break;
            case 'CANCELAT':
                chip = (
                    <Chip
                        size="small"
                        variant="outlined"
                        icon={<Icon sx={{ fontSize: '16px !important' }}>cancel</Icon>}
                        label={t($ => $.calendari.estat_cancelat)}
                    />
                );
                break;
            case 'PENDENT':
            default:
                chip = (
                    <Chip
                        size="small"
                        color="info"
                        icon={<Icon sx={{ fontSize: '16px !important' }}>hourglass_empty</Icon>}
                        label={t($ => $.calendari.estat_pendent)}
                    />
                );
                break;
        }

        if (missatge) {
            return (
                <Tooltip
                    title={missatge}
                    arrow
                    slotProps={{
                        tooltip: {
                            sx: {
                                maxHeight: 200,
                                overflowY: 'auto',
                                whiteSpace: 'pre-line',
                            },
                        },
                    }}
                >
                    <span style={{ display: 'inline-block' }}>{chip}</span>
                </Tooltip>
            );
        }
        return chip;
    };

    const getVelocitatChip = (pausaMs?: number) => {
        if (pausaMs === undefined || pausaMs === null) {
            return (
                <Chip
                    size="small"
                    color="error"
                    label={t($ => $.calendari.prioritat_alta)}
                    data-testid="chip-prioritat-alta"
                />
            );
        }
        if (pausaMs >= 600000) {
            return (
                <Chip
                    size="small"
                    color="success"
                    label={t($ => $.calendari.prioritat_baixa)}
                    data-testid="chip-prioritat-baixa"
                />
            );
        }
        if (pausaMs >= 180000) {
            return (
                <Chip
                    size="small"
                    color="warning"
                    label={t($ => $.calendari.prioritat_mitja)}
                    data-testid="chip-prioritat-mitja"
                />
            );
        }
        return (
            <Chip
                size="small"
                color="error"
                label={t($ => $.calendari.prioritat_alta)}
                data-testid="chip-prioritat-alta"
            />
        );
    };

    const handleCancel = async (id: string) => {
        try {
            setCancelingId(id);
            await onCancelProces(id);
        } finally {
            setCancelingId(null);
        }
    };

    if (!open) return null;

    return (
        <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
            <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', pb: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Icon color="primary">schedule</Icon>
                    <Typography variant="h6">{t($ => $.calendari.titol_processos_baixa_prioritat)}</Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    {entornAppId && (
                        <FormControlLabel
                            control={
                                <Switch
                                    size="small"
                                    checked={mostrarTots}
                                    onChange={(e) => setMostrarTots(e.target.checked)}
                                    data-testid="switch-mostrar-tots"
                                />
                            }
                            label={
                                <Typography variant="caption">
                                    {mostrarTots
                                        ? t($ => $.calendari.filtre_tots_entorns)
                                        : t($ => $.calendari.filtre_entorn_actual)}
                                </Typography>
                            }
                        />
                    )}
                    <IconButton
                        size="small"
                        onClick={onRefresh}
                        disabled={isLoading}
                        title={t($ => $.calendari.refrescar)}
                        data-testid="btn-refresh-processos"
                    >
                        {isLoading ? <CircularProgress size={18} /> : <Icon>refresh</Icon>}
                    </IconButton>
                </Box>
            </DialogTitle>
            <DialogContent dividers>
                {filteredProcessos.length === 0 ? (
                    <Box textAlign="center" py={4}>
                        <Icon sx={{ fontSize: 48, color: 'text.disabled', mb: 1 }}>inbox</Icon>
                        <Typography color="text.secondary">
                            {t($ => $.calendari.sense_processos)}
                        </Typography>
                    </Box>
                ) : (
                    <TableContainer component={Paper} variant="outlined">
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell><strong>{t($ => $.calendari.entorn)}</strong></TableCell>
                                    <TableCell><strong>{t($ => $.calendari.periode)}</strong></TableCell>
                                    <TableCell align="center"><strong>{t($ => $.calendari.velocitat)}</strong></TableCell>
                                    <TableCell align="center"><strong>{t($ => $.calendari.estat)}</strong></TableCell>
                                    <TableCell sx={{ minWidth: 200 }}><strong>{t($ => $.calendari.progres)}</strong></TableCell>
                                    <TableCell align="center"><strong>{t($ => $.calendari.accions)}</strong></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {filteredProcessos.map((p) => {
                                    const percent = p.percentatge ?? 0;
                                    const canCancel = p.estat === 'EN_EXECUCIO' || p.estat === 'PENDENT';
                                    const isCanceling = cancelingId === p.id;
                                    return (
                                        <TableRow key={p.id}>
                                            <TableCell>
                                                <Typography variant="body2" fontWeight={500}>
                                                    {p.entornAppNom || `Entorn ${p.entornAppId}`}
                                                </Typography>
                                                {p.dataIniciExecucio && (
                                                    <Typography variant="caption" color="text.secondary" display="block">
                                                        {dayjs(p.dataIniciExecucio).format('DD/MM/YYYY HH:mm:ss')}
                                                    </Typography>
                                                )}
                                            </TableCell>
                                            <TableCell>
                                                <Typography variant="body2">
                                                    {dayjs(p.dataInici).format('DD/MM/YYYY')} - {dayjs(p.dataFi).format('DD/MM/YYYY')}
                                                </Typography>
                                                <Typography variant="caption" color="text.secondary">
                                                    {p.totalDies} {p.totalDies === 1 ? t($ => $.calendari.dia) : t($ => $.calendari.dies)}
                                                </Typography>
                                            </TableCell>
                                            <TableCell align="center">
                                                {getVelocitatChip(p.pausaMs)}
                                            </TableCell>
                                            <TableCell align="center">
                                                {getEstatChip(p.estat, p.missatge)}
                                            </TableCell>
                                            <TableCell>
                                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                                    <Box sx={{ width: '100%', mr: 1 }}>
                                                        <LinearProgress
                                                            variant="determinate"
                                                            value={percent}
                                                            color={p.estat === 'ERROR' ? 'error' : p.estat === 'CANCELAT' ? 'inherit' : 'primary'}
                                                            sx={{ height: 8, borderRadius: 4 }}
                                                        />
                                                    </Box>
                                                    <Typography variant="caption" sx={{ minWidth: 45, fontWeight: 500 }}>
                                                        {percent}%
                                                    </Typography>
                                                </Box>
                                                <Typography variant="caption" color="text.secondary" display="block">
                                                    {p.diesProcessats} / {p.totalDies} {p.totalDies === 1 ? t($ => $.calendari.dia) : t($ => $.calendari.dies)}
                                                    {p.diesAmbErrors > 0 && ` (${p.diesAmbErrors} ${t($ => $.calendari.errors)})`}
                                                    {p.diaActual && ` — ${t($ => $.calendari.processant)} ${dayjs(p.diaActual).format('DD/MM/YYYY')}`}
                                                </Typography>
                                                {p.missatge && (
                                                    <Typography
                                                        variant="caption"
                                                        color={p.estat === 'ERROR' ? 'error.main' : (p.diesAmbErrors > 0 ? 'warning.main' : 'text.secondary')}
                                                        display="block"
                                                        sx={{
                                                            wordBreak: 'break-word',
                                                            whiteSpace: 'pre-line',
                                                            maxHeight: 120,
                                                            overflowY: 'auto',
                                                            mt: 0.5,
                                                            fontWeight: p.estat === 'ERROR' || p.diesAmbErrors > 0 ? 500 : 400
                                                        }}
                                                        data-testid={`msg-proces-${p.id}`}
                                                    >
                                                        {p.missatge}
                                                    </Typography>
                                                )}
                                            </TableCell>
                                            <TableCell align="center">
                                                {canCancel && (
                                                    <Button
                                                        size="small"
                                                        color="error"
                                                        variant="outlined"
                                                        disabled={isCanceling}
                                                        onClick={() => handleCancel(p.id)}
                                                        data-testid={`btn-cancel-${p.id}`}
                                                        sx={{ textTransform: 'none', py: 0.2, px: 1, minWidth: 0 }}
                                                    >
                                                        {isCanceling ? <CircularProgress size={14} color="inherit" /> : t($ => $.calendari.cancelar_proces)}
                                                    </Button>
                                                )}
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </DialogContent>
            <DialogActions>
                <Button onClick={onClose}>{t($ => $.calendari.tancar)}</Button>
            </DialogActions>
        </Dialog>
    );
};

export default CalendariProcessosDialog;
