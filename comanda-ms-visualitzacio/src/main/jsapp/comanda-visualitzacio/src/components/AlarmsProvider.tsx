import { AlarmsContext } from './AlarmsContext';
import { AlarmType } from './Alarms.tsx';
import { useCallback, useEffect, useEffectEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useSseContext } from './SseProvider.tsx';
import { useResourceApiService } from 'reactlib';
import { useMessage } from './MessageShow.tsx';

const SEGONS_REFRESC = 30;
const ACTIVE_ALARMS_CHANGED_EVENT_TYPE = 'alarm.active.changed';

const AlarmsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const { t } = useTranslation();
    const { status: sseStatus, subscribe } = useSseContext();
    const { isReady: apiIsReady, artifactReport: report } = useResourceApiService('alarma');
    const { showTemporal: showMessage, component: messageComponent } = useMessage();
    const [alarms, setAlarms] = useState<AlarmType[] | null>(null);

    const applyAlarms = useEffectEvent((response: AlarmType[]) => {
        const newAlarms = response.filter(alarm => !alarms?.some(a => a.id === alarm.id));

        if (alarms == null && response.length > 0) {
            showMessage(
                t($ => $.page.alarma.snackbar.title),
                t($ => $.page.alarma.snackbar.existingAlarms, { count: response.length }),
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

    const fetchAlarms = useCallback(
        () => report(undefined, { code: 'ALARMA_FIND_ACTIVES' }) as Promise<AlarmType[]>,
        [report]
    );

    // Siempre se inicializan las alarmas haciendo una petición inicial
    useEffect(() => {
        if (apiIsReady) {
            setAlarms(null);
            // useResourceApiService cambia la instáncia de artifactReport prematuramente al ejecutar apiSetHttpHeaders
            // (primero se actualiza porque indexState ha cambiado, después porque currentState ha cambiado)
            // Esto causa dos peticiones seguidas de fetchAlarms, una con el rol antiguo y la última con el rol actual,
            // por lo que se debe cancelar la petición anterior si se vuelve a ejecutar fetchAlarms
            let cancelled = false;
            fetchAlarms().then(response => {
                if (cancelled) return;
                applyAlarms(response);
            });
            return () => {
                cancelled = true;
            }
        }
    }, [apiIsReady, fetchAlarms]);

    // Se intenta suscribir al evento SSE
    useEffect(() => {
        return subscribe(ACTIVE_ALARMS_CHANGED_EVENT_TYPE, event => {
            if (event.payload != null) {
                applyAlarms(event.payload as AlarmType[]);
            }
        });
    }, [subscribe]);

    // Si el evento SSE no está disponible, se refrescan las alarmas periódicamente
    useEffect(() => {
        if (!apiIsReady || sseStatus !== 'disconnected') {
            return;
        }

        const interval = setInterval(() => {
            fetchAlarms().then(applyAlarms);
        }, SEGONS_REFRESC * 1000);

        return () => {
            clearInterval(interval);
        };
    }, [apiIsReady, fetchAlarms, sseStatus]);

    return (
        <AlarmsContext.Provider value={{ alarms }}>
            {messageComponent}
            {children}
        </AlarmsContext.Provider>
    );
};

export default AlarmsProvider;
