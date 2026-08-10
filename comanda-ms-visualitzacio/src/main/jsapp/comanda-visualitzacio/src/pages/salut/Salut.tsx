import { Activity, FunctionComponent, useCallback, useEffect, useRef, useState, useTransition } from 'react';
import { SalutEstatEnum, SalutModel } from '../../types/salut.model';
import { springFilterBuilder, useResourceApiService } from 'reactlib';
import dayjs from 'dayjs';
import SalutToolbar, {
    agrupacioFromMinutes,
    GroupingEnum,
    salutEntornAppFilterBuilder,
    SalutFilterDataType,
    useSalutToolbarState,
} from '../../components/salut/SalutToolbar';
import { useTranslation } from 'react-i18next';
import { useSseContext } from '../../components/SseProvider';
import { SalutLlistat } from '../../components/salut/SalutPrincipalWidgets';
import { useParams } from 'react-router-dom';
import SalutAppInfo from './SalutAppInfo';
import { ItemStateChip } from '../../components/salut/SalutItemStateChip';
import { AppModel, EntornAppModel } from '../../types/app.model.tsx';
import { EntornModel } from '../../types/entorn.model.tsx';
import { useSalutLlistatExpansionState } from './salutState.ts';
import { ISO_DATE_FORMAT } from '../../util/dateUtils.ts';
import { filterNumericObjectKeys } from '../../util/objectUtils.ts';
import { SalutField } from '../../components/salut/SalutChipTooltip';
import { useAppInfoData } from './dataFetching';
import { Box } from '@mui/material';
import PageTitle from '../../components/PageTitle';

// es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem
export type SalutInformeEstatItem = {
    data: string;
    upPercent: number;
    warnPercent: number;
    degradedPercent: number;
    downPercent: number;
    errorPercent: number;
    maintenancePercent: number;
    unknownPercent: number;
    alwaysUp: boolean;
    alwaysDown: boolean;
};

export type SalutData = {
    estats: {
        [entornAppId: number]: SalutInformeEstatItem[];
    };
    entornApps: EntornAppModel[];
    salutLastItems: SalutModel[];
    groupedApp?: AppModel;
    groupedEntorn?: EntornModel;
};

const normalizeAppsFromEntornApps = (
    entornApps: EntornAppModel[],
    apps?: AppModel[]
): AppModel[] => {
    const appsById = new Map<number, AppModel>();

    apps?.forEach(app => {
        if (app.id != null) {
            appsById.set(app.id, app);
        }
    });

    entornApps.forEach(entornApp => {
        const appId = entornApp.app?.id;
        if (appId == null || appsById.has(appId)) {
            return;
        }
        const fallbackName = entornApp.app?.description ?? String(appId);
        appsById.set(
            appId,
            new AppModel({
                id: appId,
                codi: fallbackName,
                nom: fallbackName,
            })
        );
    });

    return Array.from(appsById.values());
};

const normalizeEntornsFromEntornApps = (
    entornApps: EntornAppModel[],
    entorns?: EntornModel[]
): EntornModel[] => {
    const entornsById = new Map<number, EntornModel>();

    entorns?.forEach(entorn => {
        if (entorn.id != null) {
            entornsById.set(entorn.id, entorn);
        }
    });

    entornApps.forEach(entornApp => {
        const entornId = entornApp.entorn?.id;
        if (entornId == null || entornsById.has(entornId)) {
            return;
        }
        const fallbackName = entornApp.entorn?.description ?? String(entornId);
        entornsById.set(
            entornId,
            new EntornModel({
                id: entornId,
                codi: fallbackName,
                nom: fallbackName,
            })
        );
    });

    return Array.from(entornsById.values());
};

const splitSalutDataIntoGroups = ({
    estats,
    salutLastItems,
    groupBy,
    apps,
    entorns,
    entornApps,
}: {
    estats: SalutData['estats'];
    salutLastItems: SalutData['salutLastItems'];
    entornApps: EntornAppModel[];
    groupBy: GroupingEnum;
    apps?: AppModel[];
    entorns?: EntornModel[];
}) => {
    const groups: SalutData[] = [];

    const generateGroup = ({
        groupedApp,
        groupedEntorn,
        entornApps,
    }: Pick<SalutData, 'groupedApp' | 'groupedEntorn' | 'entornApps'>) => {
        const filteredEntornAppIds = entornApps.map(({ id }) => id as number);
        return {
            groupedApp,
            groupedEntorn,
            entornApps,
            estats: filterNumericObjectKeys(estats, key =>
                filteredEntornAppIds.includes(Number(key))
            ),
            salutLastItems: salutLastItems.filter(({ entornAppId }) =>
                filteredEntornAppIds.includes(entornAppId)
            ),
        };
    };

    if (groupBy === GroupingEnum.APPLICATION) {
        if (apps == null)
            throw new Error('[splitSalutDataIntoGroups] apps is required when groupBy is APP');

        const appIds = apps.map(({ id }) => id as number);
        appIds.forEach(appId => {
            const filteredEntornApps = entornApps.filter(({ app }) => app.id === appId);

            groups.push(
                generateGroup({
                    groupedApp: apps.find(({ id }) => id === appId),
                    entornApps: filteredEntornApps,
                })
            );
        });
    } else if (groupBy === GroupingEnum.ENVIRONMENT) {
        if (entorns == null)
            throw new Error(
                '[splitSalutDataIntoGroups] entorns is required when groupBy is ENTORN'
            );

        const entornIds = entorns.map(({ id }) => id as number);
        entornIds.forEach(entornId => {
            const filteredEntornApps = entornApps.filter(({ entorn }) => entorn.id === entornId);

            groups.push(
                generateGroup({
                    groupedEntorn: entorns.find(({ id }) => id === entornId),
                    entornApps: filteredEntornApps,
                })
            );
        });
    } else if (groupBy === GroupingEnum.NONE) {
        groups.push({
            entornApps,
            estats,
            salutLastItems,
        });
    }

    return groups;
};

const DEBOUNCE_MS = 300;

type SalutChangedPayload = {
    entornAppId: number;
    appEstat: SalutEstatEnum;
    appLatencia?: number;
    bdEstat: SalutEstatEnum;
    bdLatencia?: number;
    peticioError?: boolean;
    integracioUpCount?: number;
    integracioWarnCount?: number;
    integracioDownCount?: number;
    integracioDesconegutCount?: number;
    subsistemaUpCount?: number;
    subsistemaWarnCount?: number;
    subsistemaDownCount?: number;
    subsistemaDesconegutCount?: number;
    missatgeErrorCount?: number;
    missatgeWarnCount?: number;
    missatgeInfoCount?: number;
    estatItemsPerAgrupacio?: Record<string, SalutInformeEstatItem[]>;
};

const useSalutData = ({
    groupBy,
    dataRangeMinutes,
    additionalFilter,
    filterData,
}: {
    groupBy: GroupingEnum;
    dataRangeMinutes: number;
    additionalFilter?: string;
    filterData?: SalutFilterDataType;
}) => {
    const { isReady: salutApiIsReady, artifactReport: salutApiReport } =
        useResourceApiService('salut');
    const { isReady: entornAppApiIsReady, find: entornAppFind } =
        useResourceApiService('entornApp');
    const { isReady: appApiIsReady, find: appFind } = useResourceApiService('app');
    const { isReady: entornApiIsReady, find: entornFind } = useResourceApiService('entorn');
    const ready = salutApiIsReady && entornAppApiIsReady && appApiIsReady && entornApiIsReady;
    const [salutData, setSalutData] = useState<{
        lastRefresh?: Date;
        apps?: AppModel[];
        entorns?: EntornModel[];
        groups: SalutData[];
        grupsDates?: string[];
        agrupacio?: string;
        initialized: boolean;
        loading: boolean;
        error?: unknown;
    }>({ groups: [], initialized: false, loading: false });
    const [, startTransition] = useTransition();
    const requestSequence = useRef<number>(0);
    const filterDataApp = filterData?.app;
    const filterDataEntorn = filterData?.entorn;
    const filterDataEstatsSalut = filterData?.estatsSalut;
    const request = useCallback(async () => {
        if (!ready) {
            console.error('APIs not ready');
            return;
        }

        setSalutData(prevState => ({ ...prevState, loading: true, error: undefined }));
        const sequence = ++requestSequence.current;

        try {
            const dataReferencia = dayjs().format(ISO_DATE_FORMAT);
            const agrupacio = agrupacioFromMinutes(dataRangeMinutes);
            const appsIds = filterDataApp?.map(({id}) => (id))
            const entornsIds = filterDataEntorn?.map(({id}) => (id))
            const hasEstatFilter = filterDataEstatsSalut && filterDataEstatsSalut.length > 0;
            let entornAppIdsWithSalut;

            if (hasEstatFilter) {
                const salutLastItemsResponse = await salutApiReport(null, {
                    code: 'salut_last',
                    data: additionalFilter ?? '',
                });

                const salutLastItems = (salutLastItemsResponse as SalutModel[])
                    .filter(item => {
                        if (!filterDataEstatsSalut || filterDataEstatsSalut.length === 0) {
                            return true; //Sols filtrar si hi ha filtre d'estat
                        }
                        //Incluirem sols el que tengun l'estat a filtrar
                        return filterDataEstatsSalut.includes(item?.appEstat);
                    })
                    .map(item => new SalutModel(item));

                entornAppIdsWithSalut = hasEstatFilter ?
                    salutLastItems.map(item => item.entornAppId).filter((id, index, self) => self.indexOf(id) === index) :
                    null;

                if (hasEstatFilter && entornAppIdsWithSalut!.length === 0) {//Si no hi ha salut, no retornarem res
                    setSalutData({
                        lastRefresh: new Date(),
                        apps: [],
                        entorns: [],
                        groups: [],
                        grupsDates: [],
                        agrupacio,
                        error: undefined,
                        initialized: true,
                        loading: false,
                    });
                    return;
                }
            }

            const [
                activeEntornAppsResponse,
                activeAppsResponse,
                entornsResponse,
                grupsDatesResponse,
            ] = await Promise.all([
                entornAppFind({
                    unpaged: true,
                    filter: springFilterBuilder.and(
                        springFilterBuilder.eq('activa', true),
                        springFilterBuilder.eq('app.activa', true),
                        springFilterBuilder.inn('app.id', appsIds),
                        springFilterBuilder.inn('entorn.id', entornsIds),
                        entornAppIdsWithSalut ? springFilterBuilder.inn('id', entornAppIdsWithSalut) : null,
                    ),
                }),
                appFind({
                    unpaged: true,
                    filter: springFilterBuilder.and(
                        springFilterBuilder.eq('activa', true),
                        springFilterBuilder.inn('id', appsIds),
                    ),
                }),
                entornFind({
                    unpaged: true,
                    filter: springFilterBuilder.and(
                        springFilterBuilder.inn('id', entornsIds),
                    ),
                }),
                salutApiReport(null, {
                    code: 'grups_dates',
                    data: {
                        dataReferencia,
                        agrupacio,
                    },
                }),
            ]);

            const reportData = {
                dataReferencia,
                agrupacio,
                entornAppIdList: activeEntornAppsResponse.rows.map(({ id }) => id),
            };

            if (sequence !== requestSequence.current) return;
            const [estatsResponse, salutLastItemsResponse] = await Promise.all([
                salutApiReport(null, { code: 'estats', data: reportData }),
                salutApiReport(null, {
                    code: 'salut_last',
                    data: additionalFilter ?? '', // El backend lanza un 500 si se envía un body vacío
                }),
            ]);

            const visibleEntornApps = activeEntornAppsResponse.rows as EntornAppModel[];
            const visibleEntornAppIds = new Set(
                visibleEntornApps
                    .map(({ id }) => id as number | undefined)
                    .filter((id): id is number => id != null)
            );
            const salutLastItems = (salutLastItemsResponse as SalutModel[])
                .map(item => new SalutModel(item))
                .filter(item => visibleEntornAppIds.has(item.entornAppId));
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const { ...rawEstats } = (estatsResponse as any[])[0];
            const estats = filterNumericObjectKeys(rawEstats, key =>
                visibleEntornAppIds.has(Number(key))
            );
            const apps = normalizeAppsFromEntornApps(
                visibleEntornApps,
                activeAppsResponse?.rows as AppModel[]
            );
            const entorns = normalizeEntornsFromEntornApps(
                visibleEntornApps,
                entornsResponse?.rows as EntornModel[]
            );

            if (sequence !== requestSequence.current) return;

            // S'aplica com a transició perquè el canvi de dades (que pot afegir/llevar grups sencers
            // en crear/eliminar entorns-app) no bloquegi ni faci parpellejar la interfície: React manté
            // el contingut anterior visible fins que el nou està preparat.
            startTransition(() => {
                setSalutData({
                    lastRefresh: new Date(),
                    apps,
                    entorns,
                    groups: splitSalutDataIntoGroups({
                        estats,
                        salutLastItems,
                        groupBy,
                        apps,
                        entorns,
                        entornApps: visibleEntornApps,
                    }),
                    grupsDates: (grupsDatesResponse as { data: string }[]).map(item => item.data),
                    agrupacio,
                    error: undefined,
                    initialized: true,
                    loading: false,
                });
            });
        } catch (e) {
            if (sequence !== requestSequence.current) return;

            // TODO Mostrar error en la UI
            // No es reinicialitzen groups/apps/entorns: si ja hi havia dades carregades (p.ex. un
            // refresc en segon pla per un canvi d'entorn-app), es mantenen visibles en lloc de
            // deixar la pantalla en blanc per un error puntual.
            setSalutData(prevState => ({
                ...prevState,
                lastRefresh: new Date(),
                error: e,
                initialized: true,
                loading: false,
            }));
        }
    }, [
        additionalFilter,
        appFind,
        dataRangeMinutes,
        entornAppFind,
        entornFind,
        filterDataApp,
        filterDataEntorn,
        filterDataEstatsSalut,
        groupBy,
        ready,
        salutApiReport,
    ]);

    const applyBatchedSseUpdates = useCallback((payloads: Map<number, SalutChangedPayload>) => {
        setSalutData(prevState => {
            const agrupacio = prevState.agrupacio;
            const newGroups = prevState.groups.map(group => {
                const newSalutLastItems = [...group.salutLastItems];
                let salutChanged = false;
                const newEstats = { ...group.estats };
                let estatsChanged = false;

                for (const [entornAppId, payload] of payloads) {
                    const itemIndex = newSalutLastItems.findIndex(
                        item => item.entornAppId === entornAppId
                    );
                    if (itemIndex !== -1) {
                        newSalutLastItems[itemIndex] = new SalutModel({
                            ...newSalutLastItems[itemIndex],
                            ...payload,
                        } as SalutModel);
                        salutChanged = true;
                    }
                    if (agrupacio && payload.estatItemsPerAgrupacio &&
                            group.entornApps.some(ea => ea.id === entornAppId)) {
                        const items = payload.estatItemsPerAgrupacio[agrupacio];
                        if (items) {
                            newEstats[entornAppId] = items;
                            estatsChanged = true;
                        }
                    }
                }
                if (!salutChanged && !estatsChanged) return group;
                return {
                    ...group,
                    ...(salutChanged && { salutLastItems: newSalutLastItems }),
                    ...(estatsChanged && { estats: newEstats }),
                };
            });

            let newGrupsDates = prevState.grupsDates;
            if (agrupacio) {
                for (const payload of payloads.values()) {
                    if (payload.estatItemsPerAgrupacio) {
                        const items = payload.estatItemsPerAgrupacio[agrupacio];
                        if (items && items.length > 0) {
                            const candidateDates = items.map(item => item.data as string);
                            const prev = prevState.grupsDates;
                            const changed = !prev
                                || prev.length !== candidateDates.length
                                || prev.some((d, i) => d !== candidateDates[i]);
                            if (changed) {
                                newGrupsDates = candidateDates;
                            }
                            break;
                        }
                    }
                }
            }

            return {
                ...prevState,
                lastRefresh: new Date(),
                grupsDates: newGrupsDates,
                groups: newGroups,
            };
        });
    }, []);

    useEffect(() => {
        if (!ready) {
            return;
        }
        request();
    }, [ready, request]);
    return { ...salutData, refresh: request, applyBatchedSseUpdates, ready };
};

const SALUT_CHANGED_EVENT_TYPE = 'salut.changed';
const ENTORN_APP_CHANGED_EVENT_TYPE = 'entornApp.changed';
const APP_CHANGED_EVENT_TYPE = 'app.changed';
const ENTORN_CHANGED_EVENT_TYPE = 'entorn.changed';
const SSE_FALLBACK_INTERVAL_MS = 60 * 1000;

const Salut: FunctionComponent = () => {
    const { id } = useParams();
    const { t } = useTranslation();
    const toolbarState = useSalutToolbarState();
    const salutLlistatState = useSalutLlistatExpansionState();
    const additionalFilter = salutEntornAppFilterBuilder(toolbarState.filterData);
    const dataRangeMinutes = dayjs.duration(toolbarState.dataRangeDuration).asMinutes();
    const salutData = useSalutData({
        groupBy: toolbarState.grouping,
        dataRangeMinutes,
        filterData: toolbarState.filterData,
        additionalFilter,
    });
    const appInfoData = useAppInfoData(id, dataRangeMinutes);
    const salutInitialLoading = !salutData.initialized;

    const salutDataRefresh = salutData.refresh;
    const appInfoDataRefresh = appInfoData.refresh;
    const refreshAll = useCallback(() => {
        salutDataRefresh();
        appInfoDataRefresh();
    }, [salutDataRefresh, appInfoDataRefresh]);

    const { subscribe, status: sseStatus } = useSseContext();
    const applyBatchedSseUpdates = salutData.applyBatchedSseUpdates;

    const pendingPayloads = useRef<Map<number, SalutChangedPayload>>(new Map());
    const flushTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const pendingAppInfoRefresh = useRef(false);

    const flushPendingUpdates = useCallback(() => {
        if (pendingPayloads.current.size > 0) {
            applyBatchedSseUpdates(new Map(pendingPayloads.current));
            pendingPayloads.current.clear();
        }
    }, [applyBatchedSseUpdates]);

    useEffect(() => {
        const unsubscribe = subscribe(SALUT_CHANGED_EVENT_TYPE, event => {
            const payload = event.payload as SalutChangedPayload | null;
            if (payload?.entornAppId != null) {
                pendingPayloads.current.set(payload.entornAppId, payload);
                if (id != null && String(payload.entornAppId) === id) {
                    pendingAppInfoRefresh.current = true;
                }
                if (flushTimeoutRef.current !== null) {
                    clearTimeout(flushTimeoutRef.current);
                }
                flushTimeoutRef.current = setTimeout(() => {
                    flushPendingUpdates();
                    if (pendingAppInfoRefresh.current) {
                        appInfoDataRefresh();
                        pendingAppInfoRefresh.current = false;
                    }
                    flushTimeoutRef.current = null;
                }, DEBOUNCE_MS);
            }
        });
        return () => {
            unsubscribe();
            if (flushTimeoutRef.current !== null) {
                clearTimeout(flushTimeoutRef.current);
                flushTimeoutRef.current = null;
            }
        };
    }, [subscribe, flushPendingUpdates, id, appInfoDataRefresh]);

    // Alta/baixa/canvi d'un entorn-app, d'una app o d'un entorn: un pedaç puntual no basta
    // perquè pot fer aparèixer o desaparèixer un grup sencer, cal refer tota la llista.
    const fullRefreshTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    useEffect(() => {
        const handleRefreshEvent = () => {
            if (fullRefreshTimeoutRef.current !== null) {
                clearTimeout(fullRefreshTimeoutRef.current);
            }
            fullRefreshTimeoutRef.current = setTimeout(() => {
                refreshAll();
                fullRefreshTimeoutRef.current = null;
            }, DEBOUNCE_MS);
        };

        const unsubscribeEntornApp = subscribe(ENTORN_APP_CHANGED_EVENT_TYPE, handleRefreshEvent);
        const unsubscribeApp = subscribe(APP_CHANGED_EVENT_TYPE, handleRefreshEvent);
        const unsubscribeEntorn = subscribe(ENTORN_CHANGED_EVENT_TYPE, handleRefreshEvent);

        return () => {
            unsubscribeEntornApp();
            unsubscribeApp();
            unsubscribeEntorn();
            if (fullRefreshTimeoutRef.current !== null) {
                clearTimeout(fullRefreshTimeoutRef.current);
                fullRefreshTimeoutRef.current = null;
            }
        };
    }, [subscribe, refreshAll]);

    useEffect(() => {
        if (sseStatus !== 'disconnected') return;
        const interval = setInterval(refreshAll, SSE_FALLBACK_INTERVAL_MS);
        return () => clearInterval(interval);
    }, [sseStatus, refreshAll]);

    const isAppInfoRouteActive = id != null;
    const appInfoToolbarProps = isAppInfoRouteActive
        ? {
              title:
                  appInfoData.entornApp != null
                      ? `${appInfoData.entornApp.app.description} - ${appInfoData.entornApp.entorn.description}`
                      : '',

              subtitle: appInfoData.entornApp?.versio
                  ? 'v' + appInfoData.entornApp.versio
                  : undefined,
              state: appInfoData.salutCurrentApp?.appEstat ? (
                  <ItemStateChip
                      sx={{ ml: 1 }}
                      salutField={SalutField.APP_ESTAT}
                      salutStatEnum={appInfoData.salutCurrentApp.appEstat}
                  />
              ) : undefined,
              goBackActive: true,
              groupingActive: false,
              hideFilter: true,
              hideAppVersioning: true,
          }
        : null;

    return (
        <Box
            sx={{
                display: 'flex',
                flexDirection: 'column',
                height: '100%',
            }}
        >
            <SalutToolbar
                title={t($ => $.page.salut.title)}
                ready={salutData.ready}
                onRefreshClick={refreshAll}
                appDataLoading={salutData.loading}
                lastRefresh={salutData.lastRefresh}
                groupingActive
                {...toolbarState}
                {...appInfoToolbarProps}
            />
            <Activity mode={!isAppInfoRouteActive ? 'visible' : 'hidden'}>
                <Box sx={{ p: 2, flex: 1, overflowY: 'auto', scrollbarGutter: 'stable' }}>
                    <PageTitle title={t($ => $.page.salut.title)} />
                    <SalutLlistat
                        apps={salutData.apps}
                        entorns={salutData.entorns}
                        salutGroups={salutData.groups}
                        agrupacio={salutData.agrupacio}
                        springFilter={additionalFilter}
                        grupsDates={salutData.grupsDates}
                        loading={salutInitialLoading}
                        {...salutLlistatState}
                    />
                </Box>
            </Activity>
            {isAppInfoRouteActive && (
                <SalutAppInfo
                    appInfoData={appInfoData}
                    ready={appInfoData.ready}
                    grupsDates={salutData.grupsDates}
                />
            )}
        </Box>
    );
};
export default Salut;
