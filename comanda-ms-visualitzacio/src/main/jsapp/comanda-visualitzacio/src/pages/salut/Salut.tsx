import { FunctionComponent, useCallback, useEffect, useState } from 'react';
import { SalutModel } from '../../types/salut.model';
import { springFilterBuilder, useResourceApiService } from 'reactlib';
import { BaseEntity } from '../../types/base-entity.model';
import dayjs from 'dayjs';
import SalutToolbar, {
    agrupacioFromMinutes,
    GroupingEnum,
    salutEntornAppFilterBuilder,
    SalutFilterDataType,
    useSalutToolbarState,
} from '../../components/salut/SalutToolbar';
import useInterval from '../../hooks/useInterval';
import { useTranslation } from 'react-i18next';
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
import useDisableMargins from '../../hooks/useDisableMargins';
import PageTitle from '../../components/PageTitle';

// es.caib.comanda.salut.logic.intf.model.SalutInformeEstatItem
type SalutInformeEstatItem = {
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
                links: null,
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
                links: null,
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
    // TODO Considerar implementar bloqueig o cancelar peticions antigues si se fa una nova
    const request = useCallback(async () => {
        if (!ready) {
            console.error('APIs not ready');
            return;
        }

        setSalutData(prevState => ({ ...prevState, loading: true, error: undefined }));

        try {
            const dataReferencia = dayjs().format(ISO_DATE_FORMAT);
            const agrupacio = agrupacioFromMinutes(dataRangeMinutes);
            const appsIds = filterData?.app?.map(({id}) => (id))
            const entornsIds = filterData?.entorn?.map(({id}) => (id))

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
            // eslint-disable-next-line @typescript-eslint/no-explicit-any,@typescript-eslint/no-unused-vars
            const { [BaseEntity.LINKS]: _links, ...rawEstats } = (estatsResponse as any[])[0];
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
        } catch (e) {
            // TODO Mostrar error en la UI
            setSalutData({
                lastRefresh: new Date(),
                groups: [],
                error: e,
                initialized: true,
                loading: false,
            });
        }
    }, [
        additionalFilter,
        appFind,
        dataRangeMinutes,
        entornAppFind,
        entornFind,
        filterData?.app,
        filterData?.entorn,
        groupBy,
        ready,
        salutApiReport,
    ]);

    useEffect(() => {
        if (!ready) {
            return;
        }
        request();
    }, [ready, request]);
    return { ...salutData, refresh: request, ready };
};

const Salut: FunctionComponent = () => {
    useDisableMargins();
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

    const [nextRefresh, setNextRefresh] = useState<Date>();
    const updateNextRefresh = () => {
        const nextRequestDate = new Date();
        nextRequestDate.setTime(
            nextRequestDate.getTime() +
                dayjs.duration(toolbarState.refreshDuration).asMilliseconds()
        );
        setNextRefresh(nextRequestDate);
    };
    const salutDataRefresh = salutData.refresh;
    const appInfoDataRefresh = appInfoData.refresh;
    const refreshAll = useCallback(() => {
        salutDataRefresh();
        appInfoDataRefresh();
    }, [salutDataRefresh, appInfoDataRefresh]);
    useInterval({
        tick: () => {
            updateNextRefresh();
            refreshAll();
        },
        init: updateNextRefresh,
        timeout: dayjs.duration(toolbarState.refreshDuration).asMilliseconds(),
    });

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
                nextRefresh={nextRefresh}
                groupingActive
                {...toolbarState}
                {...appInfoToolbarProps}
            />
            {!isAppInfoRouteActive && (
                <Box sx={{ p: 2 }}>
                    <PageTitle title={t($ => $.page.salut.title)} />
                    <SalutLlistat
                        apps={salutData.apps}
                        entorns={salutData.entorns}
                        salutGroups={salutData.groups}
                        agrupacio={salutData.agrupacio}
                        springFilter={additionalFilter}
                        grupsDates={salutData.grupsDates}
                        {...salutLlistatState}
                    />
                </Box>
            )}
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
