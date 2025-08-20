import * as React from 'react';
import {BarChart} from '@mui/x-charts/BarChart';
import {generateDataGroups, isDataInGroup, toXAxisDataGroups} from '../util/dataGroup';
import {useTranslation} from "react-i18next";
import {ENUM_APP_ESTAT_PREFIX, getColorByStatEnum, SalutEstatEnum} from "../types/salut.model.ts";

export type UpdownBarChartProps = {
    dataInici: string;
    agrupacio: string;
    estats?: any;
}

export const getEstatsMaxData = (estats: any) => {
    let estatsMaxData = estats?.[estats.length - 1]?.data;
    const estatApps = Object.keys(estats);
    estatApps?.forEach((a: any) => {
        const maxData = estats[a][estats[a].length - 1]?.data;
        if (estatsMaxData == null || maxData > estatsMaxData) {
            estatsMaxData = maxData;
        }
    });
    return estatsMaxData;
}

export const calculateEstatsSeries = (
    baseDataGroups: string[],
    estats: Record<string, any[]>,
    agrupacio: string,
    percentKey: "upPercent" | "warnPercent" | "degradedPercent" | "maintenancePercent" | "downPercent" | "errorPercent" | "unknownPercent"
): number[] => {
    return baseDataGroups.map((group) => {
        let valueSum = 0.0;
        let valueCount = 0;

        const estatApps = Object.keys(estats);
        estatApps.forEach((appKey) => {
            const estatForGroup = estats[appKey].find((estat) => isDataInGroup(estat.data, group, agrupacio));
            valueSum += estatForGroup != null ? estatForGroup[percentKey] || 0 : 0;
            valueCount += estatForGroup != null ? 1 : 0;
        });

        // if (valueSum != 0 &&
        //     (percentKey === "degradedPercent" ||
        //      percentKey === "maintenancePercent" ||
        //      percentKey === "downPercent")) {
        //     valueSum = -valueSum;
        // }
        return valueCount !== 0 ? valueSum / valueCount : 0.0;
    });
};


const UpdownBarChart: React.FC<UpdownBarChartProps> = (props) => {
    const {
        dataInici,
        agrupacio,
        estats
    } = props;
    const { t } = useTranslation();
    const estatsMaxData = getEstatsMaxData(estats);
    const baseDataGroups = generateDataGroups(dataInici, estatsMaxData, agrupacio);

    const seriesUp = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "upPercent");
    const seriesWarn = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "warnPercent");
    const seriesDegraded = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "degradedPercent");
    const seriesMaintenance = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "maintenancePercent");
    const seriesDown = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "downPercent");
    const seriesError = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "errorPercent");
    const seriesUnknown = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "unknownPercent");

    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    const series = [
        {
            data: seriesUp,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UP),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.UP),
        },
        {
            data: seriesWarn,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.WARN),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.WARN),
        },
        {
            data: seriesDegraded,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DEGRADED),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.DEGRADED),
        },
        {
            data: seriesDown,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DOWN),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.DOWN),
        },
        {
            data: seriesError,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.ERROR),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.ERROR),
        },
        {
            data: seriesMaintenance,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.MAINTENANCE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.MAINTENANCE),
        },
        {
            data: seriesUnknown,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UNKNOWN),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.UNKNOWN),
        }
    ];

    return estats != null && <BarChart
        xAxis={[{ scaleType: 'band', data: dataGroups }]}
        series={series}
        borderRadius={6}
        grid={{
            horizontal: true,
            // vertical: true TODO No funciona amb versió @mui/x-charts@8.5.1
        }}
    />;
}

export default UpdownBarChart;
