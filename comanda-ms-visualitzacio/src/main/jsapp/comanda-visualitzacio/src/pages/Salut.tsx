import { FunctionComponent, useCallback, useEffect, useState } from 'react';
import { SalutModel } from '../types/salut.model';
import { BasePage, springFilterBuilder, useResourceApiService } from 'reactlib';
import { BaseEntity } from '../types/base-entity.model';
import dayjs from 'dayjs';
import SalutToolbar, {
    GroupingEnum,
    salutEntornAppFilterBuilder,
    SalutFilterDataType,
    toReportInterval,
    useSalutToolbarState,
} from '../components/SalutToolbar';
import useInterval from '../hooks/useInterval';
import { useTranslation } from 'react-i18next';
import { SalutLlistat } from '../components/SalutPrincipalWidgets';
import { useParams } from 'react-router-dom';
import SalutAppInfo, { useAppInfoData } from './SalutAppInfo';
import { ItemStateChip } from '../components/SalutItemStateChip';

export type AppModel = {
    // TODO
    id: number;
    nom: string;
    codi: string;
    logo?: string | null;
};

export type EntornAppModel = {
    // TODO
    id: number;
    app: {
        id: number;
    };
    entorn: {
        id: number;
    };
};

export type EntornModel = {
    // TODO
    id: number;
    codi: string;
    nom?: string | null;
};

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

const filterObjectKeys = <T extends { [key: number]: unknown }>(
    object: T,
    filterFunc: (key: string) => boolean
) => {
    return Object.keys(object)
        .filter(filterFunc)
        .reduce((acc, key) => {
            acc[Number(key)] = object[Number(key)];
            return acc;
        }, {} as T);
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

    const generateGroup = ({ groupedApp, groupedEntorn, entornApps }: Pick<SalutData, 'groupedApp' | 'groupedEntorn' | 'entornApps'>) => {
        const filteredEntornAppIds = entornApps
            .map(({ id }) => id as number);
        return {
            groupedApp,
            groupedEntorn,
            entornApps,
            estats: filterObjectKeys(estats, (key) => filteredEntornAppIds.includes(Number(key))),
            salutLastItems: salutLastItems.filter(
                ({ entornAppId }) => filteredEntornAppIds.includes(entornAppId) // TODO SalutModel alomejor no deberia anotar los campos NotNull como undefined
            ),
        };
    };

    if (groupBy === GroupingEnum.APPLICATION) {
        if (apps == null)
            throw new Error('[splitSalutDataIntoGroups] apps is required when groupBy is APP');

        const appIds = apps.map(({ id }) => id as number);
        appIds.forEach((appId) => {
            const filteredEntornApps = entornApps
                .filter(({ app }) => app.id === appId);

            groups.push(generateGroup({
                groupedApp: apps.find(({ id }) => id === appId),
                entornApps: filteredEntornApps,
            }));
        });
    } else {
        if (entorns == null)
            throw new Error(
                '[splitSalutDataIntoGroups] entorns is required when groupBy is ENTORN'
            );

        const entornIds = entorns.map(({ id }) => id as number);
        entornIds.forEach((entornId) => {
            const filteredEntornApps = entornApps
                .filter(({ entorn }) => entorn.id === entornId);

            groups.push(generateGroup({
                groupedEntorn: entorns.find(({ id }) => id === entornId),
                entornApps: filteredEntornApps,
            }));
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
        reportInterval?: {
            dataInici: string;
            dataFi: string;
            agrupacio: string;
        };
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

        setSalutData((prevState) => ({ ...prevState, loading: true, error: undefined }));

        try {
            const [activeEntornAppsResponse, activeAppsResponse, entornsResponse] =
                await Promise.all([
                    entornAppFind({
                        unpaged: true,
                        filter: springFilterBuilder.and(
                            springFilterBuilder.eq('activa', true),
                            springFilterBuilder.eq('app.activa', true),
                            filterData?.app != null
                                ? springFilterBuilder.eq('app.id', filterData.app.id)
                                : null,
                            filterData?.entorn != null
                                ? springFilterBuilder.eq('entorn.id', filterData.entorn.id)
                                : null
                        ),
                    }),
                    appFind({
                        unpaged: true,
                        filter: springFilterBuilder.and(
                            springFilterBuilder.eq('activa', true),
                            filterData?.app != null
                                ? springFilterBuilder.eq('id', filterData.app.id)
                                : null
                        ),
                    }),
                    entornFind({
                        unpaged: true,
                        filter: springFilterBuilder.and(
                            filterData?.entorn != null
                                ? springFilterBuilder.eq('id', filterData.entorn.id)
                                : null
                        ),
                    }),
                ]);

            const reportInterval = toReportInterval(dataRangeMinutes);
            const reportData = {
                ...reportInterval,
                entornAppIdList: activeEntornAppsResponse.rows.map(({ id }) => id),
            };

            const [estatsResponse, salutLastItemsResponse] = await Promise.all([
                salutApiReport(null, { code: 'estats', data: reportData }),
                salutApiReport(null, {
                    code: 'salut_last',
                    data: additionalFilter ?? '', // El backend lanza un 500 si se envía un body vacío
                }),
            ]);

            const salutLastItems = (salutLastItemsResponse as SalutModel[]).map(
                (item) => new SalutModel(item)
            );
            // eslint-disable-next-line @typescript-eslint/no-explicit-any,@typescript-eslint/no-unused-vars
            const { [BaseEntity.LINKS]: _links, ...estats } = (estatsResponse as any[])[0];

            setSalutData({
                lastRefresh: new Date(),
                apps: activeAppsResponse?.rows,
                entorns: entornsResponse?.rows,
                groups: splitSalutDataIntoGroups({
                    estats,
                    salutLastItems,
                    groupBy,
                    apps: activeAppsResponse?.rows,
                    entorns: entornsResponse?.rows,
                    entornApps: activeEntornAppsResponse.rows,
                }),
                reportInterval,
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
    const { id } = useParams();
    const { t } = useTranslation();
    const toolbarState = useSalutToolbarState();
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
    const refreshAll = () => {
        salutData.refresh();
        appInfoData.refresh();
    };
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
                      salutField={SalutModel.APP_ESTAT}
                      salutStatEnum={appInfoData.salutCurrentApp.appEstat}
                  />
              ) : undefined,
              goBackActive: true,
              groupingActive: false,
              hideFilter: true,
          }
        : null;

    return (
        <BasePage
            toolbar={
                <SalutToolbar
                    title={t('page.salut.title')}
                    ready={salutData.ready}
                    onRefreshClick={() => refreshAll()}
                    appDataLoading={salutData.loading}
                    lastRefresh={salutData.lastRefresh}
                    nextRefresh={nextRefresh}
                    groupingActive
                    {...toolbarState}
                    {...appInfoToolbarProps}
                />
            }
        >
            {!isAppInfoRouteActive ? (
                <SalutLlistat
                    apps={salutData.apps}
                    entorns={salutData.entorns}
                    salutGroups={salutData.groups}
                    reportInterval={salutData.reportInterval}
                    springFilter={additionalFilter}
                    grupsDates={grupsDates}
                />
            ) : (
                // TODO Persistir estado de expansión al cambiar a AppInfo
                <SalutAppInfo appInfoData={appInfoData} ready={appInfoData.ready} grupsDates={grupsDates} />
            )}
        </BasePage>
    );
};
export default Salut;
