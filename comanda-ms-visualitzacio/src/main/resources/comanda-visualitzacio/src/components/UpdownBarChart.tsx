import * as React from 'react';
import {BarChart} from '@mui/x-charts/BarChart';
import {useTheme} from '@mui/material/styles';
import {
    generateDataGroups,
    isDataInGroup,
    toXAxisDataGroups
} from '../util/dataGroup';

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
    percentKey: "upPercent" | "warnPercent" | "degradedPercent" | "maintenancePercent" | "downPercent"
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

        if (percentKey === "degradedPercent" || percentKey === "maintenancePercent" || percentKey === "downPercent") {
            valueSum = -valueSum;
        }
        return valueCount !== 0 ? valueSum / valueCount : 0.0;
    });
};


const UpdownBarChart: React.FC<UpdownBarChartProps> = (props) => {
    const {
        dataInici,
        agrupacio,
        estats
    } = props;
    const theme = useTheme();
    const estatsMaxData = getEstatsMaxData(estats);
    const baseDataGroups = generateDataGroups(dataInici, estatsMaxData, agrupacio);

    const seriesUp = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "upPercent");
    const seriesWarn = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "warnPercent");
    const seriesDegraded = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "degradedPercent");
    const seriesMaintenance = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "maintenancePercent");
    const seriesDown = calculateEstatsSeries(baseDataGroups, estats, agrupacio, "downPercent");

    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);
    const series = [{
        data: seriesUp,
        label: 'up',
        stack: 'total',
        color: theme.palette.success.main
    }, {
        data: seriesWarn,
        label: 'warn',
        stack: 'total',
        color: theme.palette.warning.light
    }, {
        data: seriesDegraded,
        label: 'degraded',
        stack: 'total',
        color: theme.palette.warning.dark
    }, {
        data: seriesMaintenance,
        label: 'maintenance',
        stack: 'total',
        color: theme.palette.primary.main
    }, {
        data: seriesDown,
        label: 'down',
        stack: 'total',
        color: theme.palette.error.main
    }];

    return estats != null && <BarChart
        xAxis={[{ scaleType: 'band', data: dataGroups }]}
        series={series}
        borderRadius={6}
        grid={{ horizontal: true, vertical: true }}
    />;
}

export default UpdownBarChart;
