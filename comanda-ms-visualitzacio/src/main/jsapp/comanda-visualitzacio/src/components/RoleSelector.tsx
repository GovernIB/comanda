import React from 'react';
import { useTranslation } from 'react-i18next';
import MenuItem from '@mui/material/MenuItem';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Icon from '@mui/material/Icon';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import { useUserContext } from './UserContext';
import { MAPPABLE_ROLES, ROLE_ADMIN, ROLE_CONSULTA, ROLE_USER } from './UserProvider.tsx';

function RoleSelector() {
    const { t } = useTranslation();
    // const { apiRef: authButtonApiRef } = useAuthButtonContext();
    const { user, currentRole, setCurrentRole } = useUserContext();
    const userRoles = user?.rols as string[] | undefined;
    const [open, setOpen] = React.useState<boolean>(false);
    const handleSelectOnChange = (event: any) => {
        setCurrentRole(event.target.value as string);
        //authButtonApiRef.current?.close();
    }
    const getRoleTranslation = (role: string) => {
        switch (role) {
            case ROLE_USER:
            return t($ => $.enum.userRole.COM_USER);
            case ROLE_ADMIN:
            return t($ => $.enum.userRole.COM_ADMIN);
            case ROLE_CONSULTA:
            return t($ => $.enum.userRole.COM_CONSULTA);
            default:
            return role;
        }
    };
    return user ? <MenuItem onClick={()=>setOpen(prev=>!prev)}>
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
                {[ROLE_USER, ...(userRoles?.filter(r => MAPPABLE_ROLES.includes(r) && r !== ROLE_USER) ?? [])]
                    .map(r => <MenuItem key={r} value={r}>
                    <ListItemText>{getRoleTranslation(r)}</ListItemText>
                </MenuItem>)}
            </Select>
        </FormControl>
    </MenuItem> : null;
}

export default RoleSelector;
