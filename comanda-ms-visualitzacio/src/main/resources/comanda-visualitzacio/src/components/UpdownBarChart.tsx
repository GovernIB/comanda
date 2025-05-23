import * as React from 'react';
import { BarChart } from '@mui/x-charts/BarChart';
import { useTheme } from '@mui/material/styles';
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

const getEstatsMaxData = (estats: any) => {
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

const UpdownBarChart: React.FC<UpdownBarChartProps> = (props) => {
    const {
        dataInici,
        agrupacio,
        estats
    } = props;
    const theme = useTheme();
    const estatsMaxData = getEstatsMaxData(estats);
    const baseDataGroups = generateDataGroups(dataInici, estatsMaxData, agrupacio);

    const calculateSeries = (
        baseDataGroups: string[],
        estats: Record<string, any[]>,
        agrupacio: string,
        percentKey: "upPercent" | "warnPercent" | "degradedPercent" | "mantenancePercent" | "downPercent"
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

            return valueCount !== 0 ? valueSum / valueCount : 0.0;
        });
    };

    const seriesUp = calculateSeries(baseDataGroups, estats, agrupacio, "upPercent");
    const seriesWarn = calculateSeries(baseDataGroups, estats, agrupacio, "warnPercent");
    const seriesDegraded = calculateSeries(baseDataGroups, estats, agrupacio, "degradedPercent");
    const seriesMantenance = calculateSeries(baseDataGroups, estats, agrupacio, "mantenancePercent");
    const seriesDown = calculateSeries(baseDataGroups, estats, agrupacio, "downPercent");

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
        data: seriesMantenance,
        label: 'down',
        stack: 'total',
        color: theme.palette.primary.main
    }, {
        data: seriesDown,
        label: 'mantenance',
        stack: 'total',
        color: theme.palette.error.main
    }];
    return estats != null && <BarChart
        xAxis={[{ scaleType: 'band', data: dataGroups }]}
        series={series} />;
}

export default UpdownBarChart;