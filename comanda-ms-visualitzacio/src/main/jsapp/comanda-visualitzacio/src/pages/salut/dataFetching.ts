import { useResourceApiService, springFilterBuilder } from 'reactlib';
import * as React from 'react';
import { useCallback, useEffect } from 'react';
import dayjs from 'dayjs';
import { ISO_DATE_FORMAT } from '../../util/dateUtils';
import { agrupacioFromMinutes } from '../../components/salut/SalutToolbar';
import { SalutModel } from '../../types/salut.model';
import { EntornAppModel } from '../../types/app.model';

// es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem
export type SalutInformeLatenciaItem = {
    data: string;
    latenciaMitja: number;
};
export interface AppDataState {
    loading: boolean | null; // Null indica que no se ha hecho ninguna petición aún
    entornApp: EntornAppModel | null;
    estats: Record<string, any> | null;
    latencies: SalutInformeLatenciaItem[] | null;
    salutCurrentApp: SalutModel | null;
    agrupacio?: string;
    error?: any;
    grupsDates: string[] | null;
}

const appDataStateInitialValue: AppDataState = {
    loading: null,
    entornApp: null,
    estats: null,
    latencies: null,
    salutCurrentApp: null,
    grupsDates: null,
};

export const useAppInfoData = (id: any, dataRangeMinutes: number) => {
    const { isReady: entornAppApiIsReady, getOne: entornAppGetOne } =
        useResourceApiService('entornApp');
    const {
        isReady: salutApiIsReady,
        find: salutApiFind,
        artifactReport: salutApiReport,
    } = useResourceApiService('salut');
    const [appDataState, setAppDataState] = React.useState<AppDataState>(appDataStateInitialValue);
    const ready = entornAppApiIsReady && salutApiIsReady;
    // TODO Considerar implementar bloqueig o cancelar peticions antigues si se fa una nova
    const refresh = useCallback(async () => {
        if (id == null) {
            setAppDataState(appDataStateInitialValue);
            return;
        }

        if (ready) {
            setAppDataState(prevState => ({
                ...prevState,
                loading: true,
                error: undefined,
            }));
            try {
                const entornApp = await entornAppGetOne(id);
                const entornAppId = entornApp.id;
                const dataReferencia = dayjs().format(ISO_DATE_FORMAT);
                const agrupacio = agrupacioFromMinutes(dataRangeMinutes);
                const reportData = {
                    dataReferencia,
                    agrupacio,
                    entornAppId,
                };
                const grupDatesReportItems = await salutApiReport(null, {
                    code: 'grups_dates',
                    data: {
                        dataReferencia,
                        agrupacio,
                    },
                });
                const estatReportItems = await salutApiReport(null, {
                    code: 'estat',
                    data: reportData,
                });
                const latenciaReportItems = await salutApiReport(null, {
                    code: 'latencia',
                    data: reportData,
                });
                const findArgs = {
                    page: 0,
                    size: 1,
                    sorts: ['data,desc'],
                    perspectives: [
                        'SAL_INTEGRACIONS',
                        'SAL_SUBSISTEMES',
                        'SAL_CONTEXTS',
                        'SAL_MISSATGES',
                        'SAL_DETALLS',
                    ],
                    filter: springFilterBuilder.and(
                        springFilterBuilder.eq('tipusRegistre', `'MINUT'`),
                        springFilterBuilder.eq('entornAppId', `'${entornAppId}'`)
                    ),
                };
                const { rows } = await salutApiFind(findArgs);
                const salutCurrentApp: SalutModel = rows?.[0];
                setAppDataState(state => ({
                    ...state,
                    loading: false,
                    entornApp,
                    estats: { [entornAppId]: estatReportItems },
                    latencies: latenciaReportItems as any[],
                    salutCurrentApp,
                    agrupacio,
                    grupsDates: (grupDatesReportItems as { data: string }[]).map(item => item.data),
                }));
            } catch (e) {
                // TODO Mostrar error en la UI
                setAppDataState({
                    ...appDataStateInitialValue,
                    loading: false,
                    error: e,
                });
            }
        }
    }, [dataRangeMinutes, ready, entornAppGetOne, id, salutApiReport, salutApiFind]);

    useEffect(() => {
        if (!ready) {
            return;
        }
        refresh();
    }, [ready, refresh]);

    return {
        ready,
        refresh,
        ...appDataState,
    };
};
