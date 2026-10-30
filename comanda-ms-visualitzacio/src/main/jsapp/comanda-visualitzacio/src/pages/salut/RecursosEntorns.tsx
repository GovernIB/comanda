import React, { useMemo, useState } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Paper, TableSortLabel, Box, LinearProgress, Typography, IconButton,
    CircularProgress, TablePagination
} from '@mui/material';
import Icon from '@mui/material/Icon';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { SalutModel } from '../../types/salut.model';
import { EntornAppModel } from '../../types/app.model.tsx';
import { useUserContext } from '../../components/UserContext';
import { calcularPercentatgeUs, getColorForPercentage } from '../../util/recursosUtils';

type RecursRowData = {
    id: string;
    appName: string;
    entornName: string;
    memoriaTotal: string;
    memoriaDisponible: string;
    memoriaPercent: number;
    discTotal: string;
    discDisponible: string;
    discPercent: number;
};

type Order = 'asc' | 'desc';

export interface SalutLastItemForRecursos {
    entornAppId: number;
    detalls?: Array<{ codi?: string; valor?: string }>;
}

export interface SalutGroupForRecursos {
    entornApps?: Array<Partial<EntornAppModel>>;
    salutLastItems?: Array<SalutLastItemForRecursos | SalutModel>;
}

interface RecursosEntornsProps {
    salutGroups?: SalutGroupForRecursos[];
    loading: boolean;
}

const RecursosEntorns: React.FC<RecursosEntornsProps> = ({ salutGroups, loading }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { user } = useUserContext();
    const [orderBy, setOrderBy] = useState<keyof RecursRowData>('memoriaPercent');
    const [order, setOrder] = useState<Order>('desc');

    const initialRowsPerPage = useMemo(() => {
        if (user?.numElementsPagina && user.numElementsPagina !== 'AUTOMATIC') {
            return parseInt(user.numElementsPagina.replace('_', ''), 10);
        }
        return 20;
    }, [user?.numElementsPagina]);

    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(initialRowsPerPage);

    const rowsPerPageOptions = useMemo(() => {
        const baseOptions = [10, 20, 50, 100, 200];
        if (!baseOptions.includes(initialRowsPerPage)) {
            return [...baseOptions, initialRowsPerPage].sort((a, b) => a - b);
        }
        return baseOptions;
    }, [initialRowsPerPage]);

    const noDisponible = t($ => $.page.salut.recursos.noDisponible, { defaultValue: 'N/D' });

    const rows = useMemo<RecursRowData[]>(() => {
        const flattened: RecursRowData[] = [];
        (salutGroups ?? []).forEach(group => {
            const entornAppsMap = new Map<number, Partial<EntornAppModel>>();
            group.entornApps?.forEach(ea => {
                if (ea.id) entornAppsMap.set(ea.id, ea);
            });
            group.salutLastItems?.forEach(item => {
                const entornApp = entornAppsMap.get(item.entornAppId);
                const memTotal = item.detalls?.find(d => d.codi === 'MET')?.valor;
                const memDisp = item.detalls?.find(d => d.codi === 'MED')?.valor;
                const discTotal = item.detalls?.find(d => d.codi === 'EDT')?.valor;
                const discDisp = item.detalls?.find(d => d.codi === 'EDL')?.valor;
                flattened.push({
                    id: String(item.entornAppId),
                    appName: entornApp?.app?.description || String(item.entornAppId),
                    entornName: entornApp?.entorn?.description || '',
                    memoriaTotal: memTotal || noDisponible,
                    memoriaDisponible: memDisp || noDisponible,
                    memoriaPercent: calcularPercentatgeUs(memTotal, memDisp),
                    discTotal: discTotal || noDisponible,
                    discDisponible: discDisp || noDisponible,
                    discPercent: calcularPercentatgeUs(discTotal, discDisp),
                });
            });
        });
        return flattened;
    }, [salutGroups, noDisponible]);

    const handleRequestSort = (property: keyof RecursRowData) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const handleChangePage = (_event: React.MouseEvent<HTMLButtonElement> | null, newPage: number) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const sortedRows = useMemo(() => {
        return [...rows].sort((a, b) => {
            const valA = a[orderBy];
            const valB = b[orderBy];
            if (typeof valA === 'string' && typeof valB === 'string') {
                const cmp = valA.localeCompare(valB, undefined, { numeric: true, sensitivity: 'base' });
                return order === 'asc' ? cmp : -cmp;
            }
            if (valB < valA) return order === 'asc' ? 1 : -1;
            if (valB > valA) return order === 'asc' ? -1 : 1;
            return 0;
        });
    }, [rows, order, orderBy]);

    const visibleRows = useMemo(() => {
        return sortedRows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);
    }, [sortedRows, page, rowsPerPage]);

    if (loading && rows.length === 0) {
        return (
            <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <CircularProgress size={100} />
            </Box>
        );
    }

    return (
        <TableContainer component={Paper}>
            <Table size="small" aria-label={t($ => $.page.salut.recursos.ariaTable)}>
                <TableHead sx={{ backgroundColor: 'action.hover' }}>
                    <TableRow>
                        <TableCell sortDirection={orderBy === 'appName' ? order : false}>
                            <TableSortLabel
                                active={orderBy === 'appName'}
                                direction={orderBy === 'appName' ? order : 'asc'}
                                onClick={() => handleRequestSort('appName')}
                            >
                                {t($ => $.page.salut.recursos.columns.aplicacio)}
                            </TableSortLabel>
                        </TableCell>
                        <TableCell sortDirection={orderBy === 'entornName' ? order : false}>
                            <TableSortLabel
                                active={orderBy === 'entornName'}
                                direction={orderBy === 'entornName' ? order : 'asc'}
                                onClick={() => handleRequestSort('entornName')}
                            >
                                {t($ => $.page.salut.recursos.columns.entorn)}
                            </TableSortLabel>
                        </TableCell>
                        <TableCell align="right">{t($ => $.page.salut.recursos.columns.memoriaValors)}</TableCell>
                        <TableCell sortDirection={orderBy === 'memoriaPercent' ? order : false}>
                            <TableSortLabel
                                active={orderBy === 'memoriaPercent'}
                                direction={orderBy === 'memoriaPercent' ? order : 'asc'}
                                onClick={() => handleRequestSort('memoriaPercent')}
                            >
                                {t($ => $.page.salut.recursos.columns.memoriaPercent)}
                            </TableSortLabel>
                        </TableCell>
                        <TableCell align="right">{t($ => $.page.salut.recursos.columns.discValors)}</TableCell>
                        <TableCell sortDirection={orderBy === 'discPercent' ? order : false}>
                            <TableSortLabel
                                active={orderBy === 'discPercent'}
                                direction={orderBy === 'discPercent' ? order : 'asc'}
                                onClick={() => handleRequestSort('discPercent')}
                            >
                                {t($ => $.page.salut.recursos.columns.discPercent)}
                            </TableSortLabel>
                        </TableCell>
                        <TableCell align="center">{t($ => $.page.salut.recursos.columns.detalls)}</TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {visibleRows.map((row) => (
                        <TableRow key={row.id} hover sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                            <TableCell component="th" scope="row">
                                {row.appName}
                            </TableCell>
                            <TableCell>
                                <Typography variant="body2">{row.entornName}</Typography>
                            </TableCell>
                            <TableCell align="right" sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                                {row.memoriaDisponible} / {row.memoriaTotal}
                            </TableCell>
                            <TableCell>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Box sx={{ width: '100%' }}>
                                        <LinearProgress
                                            variant="determinate"
                                            color={getColorForPercentage(row.memoriaPercent)}
                                            value={row.memoriaPercent}
                                            sx={{ height: 8, borderRadius: '4px' }}
                                        />
                                    </Box>
                                    <Typography variant="body2" sx={{ minWidth: 45, textAlign: 'right' }}>
                                        {row.memoriaPercent}%
                                    </Typography>
                                </Box>
                            </TableCell>
                            <TableCell align="right" sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                                {row.discDisponible} / {row.discTotal}
                            </TableCell>
                            <TableCell>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Box sx={{ width: '100%' }}>
                                        <LinearProgress
                                            variant="determinate"
                                            color={getColorForPercentage(row.discPercent)}
                                            value={row.discPercent}
                                            sx={{ height: 8, borderRadius: '4px' }}
                                        />
                                    </Box>
                                    <Typography variant="body2" sx={{ minWidth: 45, textAlign: 'right' }}>
                                        {row.discPercent}%
                                    </Typography>
                                </Box>
                            </TableCell>
                            <TableCell align="center">
                                <IconButton
                                    size="small"
                                    color="primary"
                                    title={t($ => $.page.salut.recursos.columns.detallsTitle)}
                                    onClick={() => navigate(`appinfo/${row.id}`)}
                                >
                                    <Icon fontSize="small">visibility</Icon>
                                </IconButton>
                            </TableCell>
                        </TableRow>
                    ))}
                    {sortedRows.length === 0 && (
                        <TableRow>
                            <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                                <Typography color="text.secondary">
                                    {t($ => $.page.salut.recursos.empty)}
                                </Typography>
                            </TableCell>
                        </TableRow>
                    )}
                </TableBody>
            </Table>
            {sortedRows.length > 0 && (
                <TablePagination
                    component="div"
                    count={sortedRows.length}
                    page={page}
                    onPageChange={handleChangePage}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                    rowsPerPageOptions={rowsPerPageOptions}
                />
            )}
        </TableContainer>
    );
};

export default RecursosEntorns;
