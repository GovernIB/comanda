import React from 'react';
import { useTranslation } from 'react-i18next';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Icon from '@mui/material/Icon';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import { useAuthButtonContext } from 'reactlib';
import { useUserContext } from './UserContext';

function RoleSelector() {
    const { t } = useTranslation();
    const { apiRef: authButtonApiRef } = useAuthButtonContext();
    const { user, currentRole, setCurrentRole } = useUserContext();
    const userRoles = user?.rols as string[];
    const [open, setOpen] = React.useState<boolean>(false);
    const handleSelectOnChange = (event: any) => {
        setCurrentRole(event.target.value as string);
        //authButtonApiRef.current?.close();
    }
    return userRoles ? <MenuItem onClick={()=>setOpen(prev=>!prev)}>
        <ListItemIcon>
            <Icon fontSize="small">badge</Icon>
        </ListItemIcon>
        <FormControl size="small" sx={{ width: '100%' }}>
            <Select
                open={open}
                size="small"
                variant="standard"
                disableUnderline
                fullWidth
                value={currentRole}
                onChange={handleSelectOnChange}>
                {userRoles.map(r => <MenuItem key={r} value={r}>
                    <ListItemText>{r === 'COM_ADMIN' ? t($ => $.enum.userRole.COM_ADMIN) : t($ => $.enum.userRole.COM_CONSULTA)}</ListItemText>
                </MenuItem>)}
            </Select>
        </FormControl>
    </MenuItem> : null;
}

export default RoleSelector;
