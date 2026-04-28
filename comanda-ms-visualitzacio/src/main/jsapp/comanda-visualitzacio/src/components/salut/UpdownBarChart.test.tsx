import { render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import UpdownBarChart, { calculateEstatsSeries } from './UpdownBarChart';

const mocks = vi.hoisted(() => ({
    barChartMock: vi.fn(),
    toXAxisDataGroupsMock: vi.fn((groups: string[]) => groups.map(group => `axis-${group}`)),
    isDataInGroupMock: vi.fn(() => true),
    tMock: vi.fn((key: string) => `tr:${key}`),
    getColorByStatEnumMock: vi.fn((state: string) => `color-${state}`),
}));

vi.mock('@mui/x-charts/BarChart', () => ({
    BarChart: (props: Record<string, unknown>) => {
        mocks.barChartMock(props);
        return <div data-testid="bar-chart">chart</div>;
    },
}));

vi.mock('../../util/dataGroup', () => ({
    toXAxisDataGroups: (groups: string[], agrupacio: string) =>
        (mocks.toXAxisDataGroupsMock as any)(groups, agrupacio),
    isDataInGroup: (data: string, group: string, agrupacio: string) =>
        (mocks.isDataInGroupMock as any)(data, group, agrupacio),
}));

vi.mock('../../hooks/useTranslationStringKey', () => ({
    default: () => ({ t: mocks.tMock }),
}));

vi.mock('../../types/salut.model.tsx', () => ({
    ENUM_APP_ESTAT_PREFIX: 'enum.appEstat.',
    TITLE: '.title',
    SalutEstatEnum: {
        UP: 'UP',
        WARN: 'WARN',
        DEGRADED: 'DEGRADED',
        ERROR: 'ERROR',
        DOWN: 'DOWN',
        MAINTENANCE: 'MAINTENANCE',
        UNKNOWN: 'UNKNOWN',
    },
    useGetColorByStatEnum: () => mocks.getColorByStatEnumMock,
}));

vi.mock('../../util/muiWorkarounds', () => ({
    MUI_AXIS_WORKAROUND_HEIGHT: 24,
}));

describe('calculateEstatsSeries', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('calculateEstatsSeries_quanHiHaDadesCalculaLaMitjanaPerCadaGrup', () => {
        // Comprova que la funció calcula la mitjana de percentatges entre totes les apps del grup.
        const result = calculateEstatsSeries(
            ['2026-03-13'],
            {
                1: [{ data: '2026-03-13', upPercent: 80 }],
                2: [{ data: '2026-03-13', upPercent: 40 }],
            } as any,
            'DIA',
            'upPercent'
        );

        expect(result).toEqual([60]);
    });

    it('calculateEstatsSeries_quanNoHiHaEstats_retornaZeros', () => {
        // Verifica que sense estats carregats la sèrie torna zero per a cada grup base.
        const result = calculateEstatsSeries(['2026-03-13', '2026-03-14'], {} as any, 'DIA', 'upPercent');

        expect(result).toEqual([0, 0]);
    });

    it('calculateEstatsSeries_quanTrobaUnValorNoNumeric_llencaUnaExcepcio', () => {
        // Comprova que els percentatges invàlids fallen ràpid per no dibuixar dades inconsistents.
        expect(() =>
            calculateEstatsSeries(
                ['2026-03-13'],
                {
                    1: [{ data: '2026-03-13', upPercent: Number.NaN }],
                } as any,
                'DIA',
                'upPercent'
            )
        ).toThrow('NaN is not a number');
    });
});

describe('UpdownBarChart', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('UpdownBarChart_quanRepEstats_renderitzaElBarChartAmbLesSeriesILaXAxis', () => {
        // Verifica que el component construeix les set sèries esperades i adapta l'eix X segons l'agrupació.
        render(
            <UpdownBarChart
                agrupacio="HORA"
                grupsDates={['13 10:00']}
                estats={{
                    1: [
                        {
                            data: '13 10:00',
                            upPercent: 70,
                            warnPercent: 5,
                            degradedPercent: 4,
                            maintenancePercent: 3,
                            downPercent: 10,
                            errorPercent: 6,
                            unknownPercent: 2,
                        },
                    ],
                } as any}
            />
        );

        expect(screen.getByTestId('bar-chart')).toBeInTheDocument();
        expect(mocks.barChartMock).toHaveBeenCalledWith(
            expect.objectContaining({
                yAxis: [{ max: 100, valueFormatter: expect.any(Function), }],
                series: expect.arrayContaining([
                    expect.objectContaining({ label: 'tr:enum.appEstat.UP.title', color: 'color-UP' }),
                    expect.objectContaining({ label: 'tr:enum.appEstat.UNKNOWN.title', color: 'color-UNKNOWN' }),
                ]),
                xAxis: [
                    expect.objectContaining({
                        data: ['axis-13 10:00'],
                        height: 24,
                        scaleType: 'band',
                    }),
                ],
            })
        );
    });

    it('UpdownBarChart_quanLesDadesSonNull_fallaAmbUnaExcepcioDelCodiActual', () => {
        // Documenta el comportament actual: el component assumeix un objecte d'estats vàlid i falla si és null.
        expect(() =>
            render(<UpdownBarChart agrupacio="DIA" grupsDates={['2026-03-13']} estats={null as any} />)
        ).toThrow();

        expect(mocks.barChartMock).not.toHaveBeenCalled();
    });
});
