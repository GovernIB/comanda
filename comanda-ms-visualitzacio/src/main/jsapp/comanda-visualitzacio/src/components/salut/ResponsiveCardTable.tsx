import * as React from 'react';
import { Breakpoint } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableRow from '@mui/material/TableRow';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';

type TableSection = {
    id: string | number;
    headerName: string;
    cellContent: React.ReactNode;
};
const ResponsiveCardTable: React.FC<{
    title: string;
    noInfoMessage?: string;
    tableSections: TableSection[];
    breakpoint?: Breakpoint;
}> = ({ title, tableSections, noInfoMessage, breakpoint = 'xl' }) => {
    const theme = useTheme();
    const isBreakpointUp = useMediaQuery(theme.breakpoints.up(breakpoint));
    return (
        <Card variant="outlined">
            <CardContent>
                <Typography gutterBottom variant="h5">
                    {title}
                </Typography>
                {!tableSections.length && (
                    <Typography sx={{ display: 'flex', justifyContent: 'center' }}>
                        {noInfoMessage}
                    </Typography>
                )}
                {!isBreakpointUp && tableSections.length && (
                    <Table size="small" sx={{ width: '100%', tableLayout: 'fixed' }}>
                        <TableBody>
                            {tableSections.map(d => (
                                <TableRow key={d.id}>
                                    <TableCell sx={{ minWidth: '165px' }}>{d.headerName}</TableCell>
                                    <TableCell>{d.cellContent}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
                {isBreakpointUp && tableSections.length && (
                    <Table size="small" sx={{ width: '100%', tableLayout: 'fixed' }}>
                        <TableHead>
                            <TableRow>
                                {tableSections.map(d => (
                                    <TableCell key={d.id} sx={{ minWidth: '165px' }}>
                                        {d.headerName}
                                    </TableCell>
                                ))}
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            <TableRow>
                                {tableSections.map(d => (
                                    <TableCell key={d.id} sx={{ wordBreak: 'break-word' }}>{d.cellContent}</TableCell>
                                ))}
                            </TableRow>
                        </TableBody>
                    </Table>
                )}
            </CardContent>
        </Card>
    );
};
export default ResponsiveCardTable;
