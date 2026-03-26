import Badge from '@mui/material/Badge';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { MuiDialog, useCloseDialogButtons } from 'reactlib';
import { Box, SxProps } from '@mui/material';
import Alarmes from '../pages/Alarmes.tsx';
import { useAlarmsContext } from './AlarmsContext.ts';
import { useMemo } from 'react';

export type AlarmType = {
    id: number;
    entornAppId: number;
};

export function AlarmsDialog({
    open,
    setOpen,
    filterBy,
}: {
    open: boolean;
    setOpen: (open: boolean) => void;
    filterBy?: { entornAppId?: number | string };
}) {
    const buttons = useCloseDialogButtons();

    return (
        <MuiDialog
            open={open}
            buttonCallback={() => setOpen(false)}
            closeCallback={() => setOpen(false)}
            buttons={buttons}
            componentProps={{
                maxWidth: 'lg',
                fullWidth: true,
            }}
        >
            <Box
                sx={{
                    mt: 3,
                    height: '500px',
                }}
            >
                <Alarmes filterBy={filterBy} />
            </Box>
        </MuiDialog>
    );
}
export const AlarmsButton: React.FC<{
    onClick?: () => void;
    filterBy?: { entornAppId?: number | string };
    sx?: SxProps,
}> = ({ onClick, filterBy, sx }) => {
    const { alarms } = useAlarmsContext();
    const count = useMemo(() => {
        const entornAppIdFilter = filterBy?.entornAppId;
        const filteredAlarms = entornAppIdFilter != null
            ? alarms?.filter(alarm => alarm.entornAppId === entornAppIdFilter)
            : alarms;

        return filteredAlarms?.length ?? 0;
    }, [alarms, filterBy?.entornAppId]);
    const icon = <Icon>notifications</Icon>;
    return (
        <IconButton onClick={onClick} sx={sx}>
            <Badge badgeContent={count} color="error" overlap="circular">
                {icon}
            </Badge>
        </IconButton>
    );
};

export const Alarms = ({ onButtonClick }: { onButtonClick: () => void }) => {
    return <AlarmsButton sx={{ mr: 2 }} onClick={onButtonClick} />;
};

export default Alarms;
