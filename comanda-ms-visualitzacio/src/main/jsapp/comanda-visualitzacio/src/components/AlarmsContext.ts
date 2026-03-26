import { createContext, useContext } from 'react';
import { AlarmType } from './Alarms.tsx';

export type AlarmsContextType = {
    alarms: AlarmType[] | null,
};

export const AlarmsContext = createContext<AlarmsContextType | undefined>(undefined);

export const useAlarmsContext = () => {
    const context = useContext(AlarmsContext);
    if (context === undefined) {
        throw new Error('AlarmsContext Provider not found');
    }
    return context;
}
