import * as React from 'react';
import {BarChart} from '@mui/x-charts/BarChart';
import { isDataInGroup, toXAxisDataGroups} from '../../util/dataGroup';
import {
    ENUM_APP_ESTAT_PREFIX,
    useGetColorByStatEnum,
    SalutEstatEnum,
    TITLE,
} from '../../types/salut.model.tsx';
import { XAxis } from '@mui/x-charts';
import { SalutData } from '../../pages/salut/Salut.tsx';
import { numericObjectKeys } from '../../util/objectUtils.ts';
import useTranslationStringKey from '../../hooks/useTranslationStringKey';
import { MUI_AXIS_WORKAROUND_HEIGHT } from '../../util/muiWorkarounds';

export const calculateEstatsSeries = (
    baseDataGroups: string[],
    estats: SalutData['estats'],
    agrupacio: string,
    percentKey: "upPercent" | "warnPercent" | "degradedPercent" | "maintenancePercent" | "downPercent" | "errorPercent" | "unknownPercent"
): number[] => {
    return baseDataGroups.map((group) => {
        const estatApps = numericObjectKeys(estats);
        if (estatApps.length === 0) {
            return 0;
        }
        let estatPercent: number = 0;
        estatApps.forEach((appKey) => {
            const estatsData = estats[appKey];
            const estat = estatsData.find((e) => e?.data === group);
            if (estat && isDataInGroup(estat.data, group, agrupacio)) {
                if (isNaN(estat[percentKey]))
                    throw new Error(`${estat[percentKey]} is not a number (${group}, ${percentKey})`);
                estatPercent += estat[percentKey];
            }
        });
        return estatPercent / estatApps.length;
    });
};

const UpdownBarChart: React.FC<{
    agrupacio: string;
    estats: SalutData['estats'];
    grupsDates: string[];
}> = React.memo((props) => {
    const { agrupacio, estats, grupsDates: baseDataGroups } = props;
    const { t } = useTranslationStringKey();
    const getColorByStatEnum = useGetColorByStatEnum();

    const seriesUp = calculateEstatsSeries(baseDataGroups, estats, agrupacio, 'upPercent');
    const seriesWarn = calculateEstatsSeries(baseDataGroups, estats, agrupacio, 'warnPercent');
    const seriesDegraded = calculateEstatsSeries(
        baseDataGroups,
        estats,
        agrupacio,
        'degradedPercent'
    );
    const seriesMaintenance = calculateEstatsSeries(
        baseDataGroups,
        estats,
        agrupacio,
        'maintenancePercent'
    );
    const seriesError = calculateEstatsSeries(baseDataGroups, estats, agrupacio, 'errorPercent');
    const seriesDown = calculateEstatsSeries(baseDataGroups, estats, agrupacio, 'downPercent');
    const seriesUnknown = calculateEstatsSeries(
        baseDataGroups,
        estats,
        agrupacio,
        'unknownPercent'
    );

    const dataGroups = toXAxisDataGroups(baseDataGroups, agrupacio);

    const valueFormatter = (value: number | null) => value != null ? `${Math.round(value * 100) / 100}%` : value;

    const series = [
        {
            data: seriesUp,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UP + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.UP),
            valueFormatter,
        },
        {
            data: seriesWarn,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.WARN + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.WARN),
            valueFormatter,
        },
        {
            data: seriesDegraded,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DEGRADED + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.DEGRADED),
            valueFormatter,
        },
        {
            data: seriesError,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.ERROR + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.ERROR),
            valueFormatter,
        },
        {
            data: seriesDown,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.DOWN + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.DOWN),
            valueFormatter,
        },
        {
            data: seriesMaintenance,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.MAINTENANCE + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.MAINTENANCE),
            valueFormatter,
        },
        {
            data: seriesUnknown,
            label: t(ENUM_APP_ESTAT_PREFIX + SalutEstatEnum.UNKNOWN + TITLE),
            stack: 'total',
            color: getColorByStatEnum(SalutEstatEnum.UNKNOWN),
            valueFormatter,
        },
    ];

    const xAxis: ReadonlyArray<XAxis<'band'>> = [
        {
            scaleType: 'band',
            data: dataGroups,
            height: MUI_AXIS_WORKAROUND_HEIGHT,
            // TODO Fer un formatter generic per a totes les agrupacions
            valueFormatter: (value: string) => (agrupacio === 'HORA' ? value.substring(3) : value),
        },
    ];

    return (
        estats != null && (
            <BarChart
                xAxis={xAxis}
                yAxis={[{ max: 100 }]}
                series={series}
                // borderRadius={6}
                grid={{
                    horizontal: true,
                    vertical: true,
                }}
            />
        )
    );
});

export default UpdownBarChart;
