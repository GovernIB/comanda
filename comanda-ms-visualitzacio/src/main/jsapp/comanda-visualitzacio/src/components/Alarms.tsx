import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import Badge from '@mui/material/Badge';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';

export const Alarms = () => {
    const [count, setCount] = React.useState<number>(2);
    const icon = <Icon>notifications</Icon>;
    return <IconButton sx={{ mr: 2 }} to="alarmes" component={RouterLink}>
        <Badge badgeContent={count} color="error" overlap="circular">{icon}</Badge>
    </IconButton>;
}

export default Alarms;