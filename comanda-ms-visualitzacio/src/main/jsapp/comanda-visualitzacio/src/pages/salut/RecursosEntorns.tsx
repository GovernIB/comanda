import React, { useMemo, useState } from 'react';
import { 
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
    Paper, TableSortLabel, Box, LinearProgress, Typography, IconButton,
    CircularProgress
} from '@mui/material';
import Icon from '@mui/material/Icon';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { SalutModel } from '../../types/salut.model';
import { EntornAppModel } from '../../types/app.model.tsx';
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

interface RecursosEntornsProps {
    salutGroups: any[]; 
    loading: boolean;
}

const RecursosEntorns: React.FC<RecursosEntornsProps> = ({ salutGroups, loading }) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [orderBy, setOrderBy] = useState<keyof RecursRowData>('memoriaPercent');
    const [order, setOrder] = useState<Order>('desc'); 

    const rows = useMemo<RecursRowData[]>(() => {
        const flattened: RecursRowData[] = [];
        salutGroups.forEach(group => {
            const entornAppsMap = new Map<number, EntornAppModel>();
            group.entornApps?.forEach((ea: EntornAppModel) => {
                if (ea.id) entornAppsMap.set(ea.id, ea);
            });
            group.salutLastItems?.forEach((item: SalutModel) => {
                const entornApp = entornAppsMap.get(item.entornAppId);
                const memTotal = item.detalls?.find(d => d.codi === 'MET')?.valor;
                const memDisp = item.detalls?.find(d => d.codi === 'MED')?.valor;
                const discTotal = item.detalls?.find(d => d.codi === 'EDT')?.valor;
                const discDisp = item.detalls?.find(d => d.codi === 'EDL')?.valor;
                flattened.push({
                    id: String(item.entornAppId),
                    appName: entornApp?.app?.description || String(item.entornAppId),
                    entornName: entornApp?.entorn?.description || '',
                    memoriaTotal: memTotal || 'N/A',
                    memoriaDisponible: memDisp || 'N/A',
                    memoriaPercent: calcularPercentatgeUs(memTotal, memDisp),
                    discTotal: discTotal || 'N/A',
                    discDisponible: discDisp || 'N/A',
                    discPercent: calcularPercentatgeUs(discTotal, discDisp),
                });
            });
        });
        return flattened;
    }, [salutGroups]);

    const handleRequestSort = (property: keyof RecursRowData) => {
        const isAsc = orderBy === property && order === 'asc';
        setOrder(isAsc ? 'desc' : 'asc');
        setOrderBy(property);
    };

    const sortedRows = useMemo(() => {
        return [...rows].sort((a, b) => {
            if (b[orderBy] < a[orderBy]) return order === 'asc' ? 1 : -1;
            if (b[orderBy] > a[orderBy]) return order === 'asc' ? -1 : 1;
            return 0;
        });
    }, [rows, order, orderBy]);

    if (loading && rows.length === 0) {
        return <Box sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', height: '100%',}}>
                <CircularProgress size={100} />
            </Box>;
    }

    return (
        <TableContainer component={Paper} sx={{ mt: 2, mx: 2 }}>
            <Table size="small" aria-label="taula de recursos">
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
                    {sortedRows.map((row) => (
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
        </TableContainer>
    );
};

export default RecursosEntorns;