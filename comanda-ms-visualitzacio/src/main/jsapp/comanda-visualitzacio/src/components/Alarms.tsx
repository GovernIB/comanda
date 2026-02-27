import { useEffect, useEffectEvent, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import Badge from '@mui/material/Badge';
import IconButton from '@mui/material/IconButton';
import Icon from '@mui/material/Icon';
import { useResourceApiService } from 'reactlib';
import { useMessage } from './MessageShow';
import { useTranslation } from 'react-i18next';

const SEGONS_REFRESC = 30;

type AlarmType = {
    id: number;
};

export const Alarms = () => {
    const { t } = useTranslation();
    const { isReady: apiIsReady, artifactReport: report } = useResourceApiService('alarma');
    const { showTemporal: showMessage, component } = useMessage();
    const [alarms, setAlarms] = useState<AlarmType[] | null>(null);
    const count = alarms?.length ?? 0;
    const fetchAlarms = useEffectEvent(async () => {
        const response = (await report(undefined, { code: 'ALARMA_FIND_ACTIVES' })) as AlarmType[];
        const newAlarms = response.filter(alarm => !alarms?.some(a => a.id === alarm.id));

        if (alarms == null) {
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
    useEffect(() => {
        if (apiIsReady) {
            fetchAlarms();
            const interval = setInterval(() => {
                fetchAlarms();
            }, SEGONS_REFRESC * 1000);
            return () => clearInterval(interval);
        }
    }, [apiIsReady]);
    const icon = <Icon>notifications</Icon>;
    return (
        <>
            {component}
            <IconButton sx={{ mr: 2 }} to="alarmes" component={RouterLink}>
                <Badge badgeContent={count} color="error" overlap="circular">
                    {icon}
                </Badge>
            </IconButton>
        </>
    );
};

export default Alarms;
