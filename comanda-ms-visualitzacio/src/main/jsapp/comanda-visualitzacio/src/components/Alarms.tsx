import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import Badge from '@mui/material/Badge';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { useResourceApiService } from 'reactlib';

const SEGONS_REFRESC = 30;

export const Alarms = () => {
    const { isReady: apiIsReady, artifactReport: report } = useResourceApiService('alarma');
    const [count, setCount] = React.useState<number>();
    const fetchAlarms = async () => {
        const response = await report(undefined, {code: "ALARMA_FIND_ACTIVES"}) as any[];
        setCount(response?.length ?? 0);
    };
    React.useEffect(() => {
        if (apiIsReady) {
            fetchAlarms();
            const interval = setInterval(() => {
                fetchAlarms();
            }, SEGONS_REFRESC * 1000);
            return () => clearInterval(interval);
        }
    }, [apiIsReady]);
    const icon = <Icon>notifications</Icon>;
    return <IconButton sx={{ mr: 2 }} to="alarmes" component={RouterLink}>
        <Badge badgeContent={count} color="error" overlap="circular">{icon}</Badge>
    </IconButton>;
}

export default Alarms;