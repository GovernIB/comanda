import { useResourceApiService, springFilterBuilder } from 'reactlib';
import * as React from 'react';
import { useCallback, useEffect } from 'react';
import dayjs from 'dayjs';
import { ISO_DATE_FORMAT } from '../../util/dateUtils';
import { agrupacioFromMinutes } from '../../components/salut/SalutToolbar';
import { SalutModel } from '../../types/salut.model';
import { EntornAppModel, IAppContext, IAppIntegracio, IAppSubsistema } from '../../types/app.model';
import { useIsUserAdmin } from '../../components/UserContext.ts';
import { IBaseEntity } from '../../types/base-entity.model.ts';
import { SalutInformeEstatItem } from './Salut.tsx';

// es.caib.comanda.configuracio.logic.intf.model.EntornApp#PERSPECTIVE_DEFAULT_LOGS
const PERSPECTIVE_DEFAULT_LOGS_NAME = 'default_logs';
export type DefaultLogsPerspective = {
    defaultLogs: string[];
}

// es.caib.comanda.configuracio.logic.intf.model.EntornApp#PERSPECTIVE_HISTORICS_VERSIONS
const PERSPECTIVE_HISTORICS_VERSIONS = 'historics_versions';
export type EntornAppHistPerspective = {
    entornAppHistorics: ISalutHistVersion[];
}

// es.caib.comanda.configuracio.logic.intf.model.EntornApp#PERSPECTIVE_INTEGRACIONS_SUBSISTEMES_CONTEXTS
export const PERSPECTIVE_ENTORN_APP_INTEGRACIONS_SUBSISTEMES_CONTEXTS = 'integracions_subsistemes_contexts';
export type IntegracionsSubsistemesContextsPerspective = {
    integracions?   : IAppIntegracio[];
    subsistemes?    : IAppSubsistema[];
    contexts?       : IAppContext[];
}

export interface ISalutHistVersion extends IBaseEntity {
    entornAppId: number;
    data: string;
    versio: string;
    revisio: string;
    canviVersio: boolean;
}

export const truncateHashRevisio = (hash: string | null | undefined, length = 7) => {
    if (!hash) return '';
    return hash.length > length ? `${hash.slice(0, length)}...` : hash;
};

// es.caib.comanda.salut.logic.intf.model.SalutInformeLatenciaItem
export type SalutInformeLatenciaItem = {
    data: string;
    latenciaMitja: number;
};
export interface AppDataState {
    loading: boolean | null; // Null indica que no se ha hecho ninguna petición aún
    refreshInfoLoading: boolean;
    entornApp: (EntornAppModel & DefaultLogsPerspective & EntornAppHistPerspective) | null;
    estats: { [entornAppId: number]: SalutInformeEstatItem[] } | null;
    latencies: SalutInformeLatenciaItem[] | null;
    salutCurrentApp: SalutModel | null;
    agrupacio?: string;
    error?: unknown;
    grupsDates: string[] | null;
}

const appDataStateInitialValue: AppDataState = {
    loading: null,
    refreshInfoLoading: false,
    entornApp: null,
    estats: null,
    latencies: null,
    salutCurrentApp: null,
    grupsDates: null,
};

export const useAppInfoData = (id: number | string | null | undefined, dataRangeMinutes: number) => {
    const isUserAdmin = useIsUserAdmin();
    const {
        isReady: entornAppApiIsReady,
        getOne: entornAppGetOne,
        artifactAction: entornAppApiAction,
    } = useResourceApiService('entornApp');
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
                const entornApp = await entornAppGetOne(id, {
                    perspectives: [PERSPECTIVE_DEFAULT_LOGS_NAME, PERSPECTIVE_HISTORICS_VERSIONS],
                });
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
                        'SAL_HISTORICS',
                        'SAL_ULTIM_ESTAT_OPERATIU_INFO',
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
                    estats: { [entornAppId as number]: estatReportItems as SalutInformeEstatItem[] },
                    latencies: latenciaReportItems as SalutInformeLatenciaItem[],
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
    }, [dataRangeMinutes, ready, entornAppGetOne, id, isUserAdmin, salutApiReport, salutApiFind]);

    const refreshInfo = useCallback(async () => {
        if (id == null || !ready) {
            return;
        }

        setAppDataState(prevState => ({
            ...prevState,
            refreshInfoLoading: true,
            error: undefined,
        }));
        try {
            await entornAppApiAction(id, { code: 'refresh_info' });
            await refresh();
        } catch (e) {
            setAppDataState(prevState => ({
                ...prevState,
                loading: false,
                error: e,
            }));
        } finally {
            setAppDataState(prevState => ({
                ...prevState,
                refreshInfoLoading: false,
            }));
        }
    }, [entornAppApiAction, id, ready, refresh]);

    useEffect(() => {
        if (!ready) {
            return;
        }
        refresh();
    }, [ready, refresh]);

    return {
        ready,
        refresh,
        refreshInfo,
        ...appDataState,
    };
};
