import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import Badge from '@mui/material/Badge';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { useResourceApiService } from 'reactlib';

const SEGONS_REFRESC = 30;

export const Alarms = () => {
    const { isReady: apiIsReady, find: apiFind } = useResourceApiService('alarma');
    const [count, setCount] = React.useState<number>();
    React.useEffect(() => {
        const fetchAlarms = async () => {
            const response = await apiFind({ filter: "estat:'ACTIVA'", unpaged: true });
            setCount(response.rows?.length ?? 0);
        };
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