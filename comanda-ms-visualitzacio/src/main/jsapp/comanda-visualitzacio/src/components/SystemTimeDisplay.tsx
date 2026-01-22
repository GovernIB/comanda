import React from 'react';
import { AccessTime, CalendarMonth } from '@mui/icons-material';
import dayjs from 'dayjs';
import { useTheme } from '@mui/material/styles';

export const SystemTimeDisplay = React.memo(() => {
    const theme = useTheme();
    const [currentTime, setCurrentTime] = React.useState(dayjs());
    React.useEffect(() => {
        const timer = setInterval(() => {
            setCurrentTime(dayjs());
        }, 1000);
        return () => clearInterval(timer);
    }, []);
    return (
        <div
            style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                marginLeft: '20px',
                color: theme.palette.text.primary,
                fontSize: '11px',
                marginRight: '16px',
            }}>
            <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                <CalendarMonth sx={{ fontSize: '14px' }}/>
                <span>{currentTime.format('DD/MM/YYYY')}</span>
            </div>
            <div style={{display: 'flex', alignItems: 'center', gap: '4px'}}>
                <AccessTime  sx={{ fontSize: '14px' }}/>
                <span>{currentTime.format('HH:mm:ss')}</span>
            </div>
        </div>
    );
});

export default SystemTimeDisplay;
