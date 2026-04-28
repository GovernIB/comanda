import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import {
    agrupacioFromMinutes,
    GroupingEnum,
    salutEntornAppFilterBuilder,
    useSalutToolbarState,
} from './SalutToolbar';

const mocks = vi.hoisted(() => ({
    andMock: vi.fn((...parts: string[]) => `AND(${parts.join(',')})`),
    eqMock: vi.fn((field: string, value: unknown) => `EQ(${field},${String(value)})`),
    existsMock: vi.fn((value: string) => `EXISTS(${value})`),
}));

vi.mock('reactlib', () => ({
    springFilterBuilder: {
        and: (...args: unknown[]) => mocks.andMock(...(args as string[])),
        eq: (...args: unknown[]) => mocks.eqMock(args[0] as string, args[1]),
        exists: (...args: unknown[]) => mocks.existsMock(args[0] as string),
        inn: vi.fn((field: string, values: unknown[]) => `IN(${field},${values.join(',')})`),
    },
    Toolbar: () => null,
    MuiDialog: () => null,
    MuiFilter: () => null,
    FormField: () => null,
    useBaseAppContext: () => ({ goBack: vi.fn() }),
    useFilterApiRef: () => ({ current: { filter: vi.fn(), clear: vi.fn() } }),
    useFormContext: () => ({ data: {} }),
}));

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            typeof selector === 'function'
                ? selector({
                      page: {
                          salut: {
                              filtrar: 'Filtrar',
                              senseFiltres: 'Sense filtres',
                              refresh: { last: 'Darrer', next: 'Següent' },
                              refreshperiod: {
                                  title: 'Període de refresc',
                                  PT1M: '1m',
                                  PT5M: '5m',
                                  PT10M: '10m',
                                  PT30M: '30m',
                                  PT1H: '1h',
                              },
                              timerange: {
                                  title: 'Rang temporal',
                                  PT15M: '15m',
                                  PT1H: '1h',
                                  P1D: '1d',
                                  P7D: '7d',
                                  P1M: '1m',
                              },
                              groupingSelect: {
                                  label: 'Agrupació',
                                  BY_APPLICATION: 'Per aplicació',
                                  BY_ENVIRONMENT: 'Per entorn',
                                  NONE: 'Cap',
                              },
                          },
                      },
                      components: { clear: 'Netejar', search: 'Cercar' },
                  })
                : selector,
    }),
    Trans: ({ i18nKey }: { i18nKey: any }) => (
        <>{typeof i18nKey === 'function' ? i18nKey({}) : i18nKey}</>
    ),
}));

describe('agrupacioFromMinutes', () => {
    it('agrupacioFromMinutes_quanLEscalaEsPetita_retornaAgrupacioPerMinut', () => {
        // Comprova que per intervals curts es retorna l'agrupació de menor granularitat.
        expect(agrupacioFromMinutes(10)).toBe('MINUT');
    });

    it('agrupacioFromMinutes_quanLEscalaEsLlarga_retornaAgrupacioPerDiaDeMes', () => {
        // Verifica que per intervals molt llargs es retorna l'agrupació mensual per dies.
        expect(agrupacioFromMinutes(24 * 60 * 8)).toBe('DIA_MES');
    });
});

describe('salutEntornAppFilterBuilder', () => {
    afterEach(() => {
        vi.clearAllMocks();
    });

    it('salutEntornAppFilterBuilder_quanNoHiHaDades_retornaFiltreBuit', () => {
        // Comprova que el constructor de filtre evita generar expressions quan no hi ha dades.
        expect(salutEntornAppFilterBuilder(null as any)).toBe('');
    });

    it('salutEntornAppFilterBuilder_quanHiHaAppIEntorn_construeixElFiltreCompost', () => {
        // Verifica que el constructor composa el filtre amb l'app i l'entorn seleccionats.
        expect(
            salutEntornAppFilterBuilder({
                app: [{ id: '1', description: 'APP' }],
                entorn: [{ id: '2', description: 'PRO' }],
            })
        ).toBe('AND(IN(app.id,1),IN(entorn.id,2))');
    });
});

describe('useSalutToolbarState', () => {
    const storage = new Map<string, string>();

    beforeEach(() => {
        storage.clear();
        vi.stubGlobal('localStorage', {
            getItem: (key: string) => storage.get(key) ?? null,
            setItem: (key: string, value: string) => {
                storage.set(key, value);
            },
            removeItem: (key: string) => {
                storage.delete(key);
            },
            clear: () => {
                storage.clear();
            },
        });
    });

    afterEach(() => {
        storage.clear();
        vi.unstubAllGlobals();
        vi.clearAllMocks();
    });

    it('useSalutToolbarState_quanNoHiHaValorsPersistits_carregaElsValorsPerDefecte', () => {
        // Comprova que el hook inicialitza la toolbar amb els valors persistits per defecte.
        const { result } = renderHook(() => useSalutToolbarState());

        expect(result.current.dataRangeDuration).toBe('PT15M');
        expect(result.current.refreshDuration).toBe('PT5M');
        expect(result.current.grouping).toBe(GroupingEnum.APPLICATION);
        expect(result.current.filterData).toEqual({});
    });

    it('useSalutToolbarState_quanCanvienElsValors_elsPersisteixAlLocalStorage', () => {
        // Verifica que cada setter actualitza tant l'estat React com el valor persistit.
        const { result } = renderHook(() => useSalutToolbarState());

        act(() => {
            result.current.setDataRangeDuration('P1D');
            result.current.setRefreshDuration('PT10M');
            result.current.setGrouping(GroupingEnum.NONE);
            result.current.setFilterData({ app: [{ id: '5', description: 'APP' }] });
        });

        expect(localStorage.getItem('appDataRangeSelect')).toBe('P1D');
        expect(localStorage.getItem('refreshTimeoutSelect')).toBe('PT10M');
        expect(localStorage.getItem('groupingForViewSelect')).toBe('NONE');
        expect(localStorage.getItem('filterDataSalut')).toBe('{"app":[{"id":"5","description":"APP"}]}');
    });

    it('useSalutToolbarState_quanHiHaValorsPersistitsInvalits_recuperaElsDefaults', () => {
        // Comprova que els valors corruptes del localStorage no trenquen el hook i es substitueixen pels defaults.
        localStorage.setItem('appDataRangeSelect', 'INVALID');
        localStorage.setItem('refreshTimeoutSelect', 'INVALID');
        localStorage.setItem('groupingForViewSelect', 'INVALID');

        const { result } = renderHook(() => useSalutToolbarState());

        expect(result.current.dataRangeDuration).toBe('PT15M');
        expect(result.current.refreshDuration).toBe('PT5M');
        expect(result.current.grouping).toBe(GroupingEnum.APPLICATION);
    });
});
