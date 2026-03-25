import { useEffect, useEffectEvent, useState } from 'react';
import Badge from '@mui/material/Badge';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import {MuiDialog, useCloseDialogButtons, useResourceApiService} from 'reactlib';
import { useMessage } from './MessageShow';
import { useTranslation } from 'react-i18next';
import { useSseContext } from './SseProvider';
import { Box } from '@mui/material';
import Alarmes from '../pages/Alarmes.tsx';

const SEGONS_REFRESC = 30;
const ACTIVE_ALARMS_CHANGED_EVENT_TYPE = 'alarm.active.changed';

type AlarmType = {
    id: number;
};

export function AlarmsDialog({ open, setOpen }: { open: boolean, setOpen: (open: boolean) => void }){
    const buttons = useCloseDialogButtons();

    return (
        <MuiDialog
            open={open}
            buttonCallback={() => setOpen(false)}
            closeCallback={() => setOpen(false)}
            buttons={buttons}
            componentProps={{
                maxWidth: 'md',
            }}
        >
            <Box
                sx={{
                    mt: 3,
                    height: '500px',
                    width: '600px',
                }}
            >
                <Alarmes />
            </Box>
        </MuiDialog>
    );
}

export const Alarms = ({ onButtonClick }: { onButtonClick: () => void }) => {
    const { t } = useTranslation();
    const { status: sseStatus, subscribe } = useSseContext();
    const { isReady: apiIsReady, artifactReport: report } = useResourceApiService('alarma');
    const { showTemporal: showMessage, component } = useMessage();
    const [alarms, setAlarms] = useState<AlarmType[] | null>(null);
    const count = alarms?.length ?? 0;

    const applyAlarms = useEffectEvent((response: AlarmType[]) => {
        const newAlarms = response.filter(alarm => !alarms?.some(a => a.id === alarm.id));

        if (alarms == null && response.length > 0) {
            showMessage(
                t($ => $.page.alarma.snackbar.title),
                t($ => $.page.alarma.snackbar.existingAlarms, { count: response.length}),
                'error',
                undefined,
                5000
            );
        } else if (newAlarms.length > 0) {
            showMessage(
                t($ => $.page.alarma.snackbar.title),
                t($ => $.page.alarma.snackbar.newAlarms, { count: newAlarms.length }),
                'error',
                undefined,
                5000
            );
        }
        setAlarms(response);
    });
    const fetchAlarms = useEffectEvent(async () => {
        const response = (await report(undefined, { code: 'ALARMA_FIND_ACTIVES' })) as AlarmType[];
        applyAlarms(response);
    });

    useEffect(() => {
        if (apiIsReady) {
            fetchAlarms();
        }
    }, [apiIsReady]);

    useEffect(() => {
        return subscribe(ACTIVE_ALARMS_CHANGED_EVENT_TYPE, (event) => {
            if (event.payload != null) {
                applyAlarms(event.payload as AlarmType[]);
            }
        });
    }, [applyAlarms, subscribe]);

    useEffect(() => {
        if (!apiIsReady || sseStatus !== 'disconnected') {
            return;
        }

        const interval = setInterval(() => {
            fetchAlarms();
        }, SEGONS_REFRESC * 1000);

        return () => {
            clearInterval(interval);
        };
    }, [apiIsReady, fetchAlarms, sseStatus]);
    const icon = <Icon>notifications</Icon>;
    return (
        <>
            {component}
            <IconButton sx={{ mr: 2 }} onClick={() => onButtonClick()}>
                <Badge badgeContent={count} color="error" overlap="circular">
                    {icon}
                </Badge>
            </IconButton>
        </>
    );
};

export default Alarms;
