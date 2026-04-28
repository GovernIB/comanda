import { renderHook } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { describe, expect, it, vi } from 'vitest';
import { AppModel, EntornAppModel } from './app.model';
import { EntornModel } from './entorn.model';
import { LanguageEnum, MenuEstil, NUM_ELEMENT_PAGE_OPTIONS, TemaAplicacio, UsuariModel } from './usuari.model';
import {
    AvisTipusEnum,
    getMaterialIconByState,
    NivellEnum,
    SalutDetall,
    SalutEstatEnum,
    SalutInformeEstatItemModel,
    SalutIntegracioModel,
    SalutMissatge,
    SalutModel,
    SalutSubsistema,
    TascaEstatEnum,
    TascaPrioritatEnum,
    useGetColorByAvisTipus,
    useGetColorByIntegracio,
    useGetColorByMissatge,
    useGetColorByNivellEnum,
    useGetColorByStatEnum,
    useGetColorBySubsistema,
    useGetColorByTascaEstat,
    useGetColorByTascaPrioritat,
    useSalutEstatTranslation,
} from './salut.model';

vi.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (selector: any) =>
            selector({
                enum: {
                    appEstat: {
                        UP: { title: 'UP title', tooltip: 'UP tooltip' },
                        WARN: { title: 'WARN title', tooltip: 'WARN tooltip' },
                        DEGRADED: { title: 'DEGRADED title', tooltip: 'DEGRADED tooltip' },
                        DOWN: { title: 'DOWN title', tooltip: 'DOWN tooltip' },
                        MAINTENANCE: { title: 'MAINTENANCE title', tooltip: 'MAINTENANCE tooltip' },
                        UNKNOWN: { title: 'UNKNOWN title', tooltip: 'UNKNOWN tooltip' },
                        ERROR: { title: 'ERROR title', tooltip: 'ERROR tooltip' },
                    },
                },
            }),
    }),
}));

function withTheme(mode: 'light' | 'dark' = 'light') {
    const theme = createTheme({ palette: { mode } });
    return ({ children }: { children: React.ReactNode }) => (
        <ThemeProvider theme={theme}>{children}</ThemeProvider>
    );
}

describe('Models', () => {
    it('Models_quanEsConstrueixenModelsBase_copienLesPropietatsEsperades', () => {
        // Comprova que els models principals del directori types copien correctament les dades del constructor.
        const app = new AppModel({
            id: 1,
            codi: 'APP',
            nom: 'Aplicació',
            activa: true,
            links: [],
        });
        const entorn = new EntornModel({
            id: 2,
            codi: 'PRO',
            nom: 'Producció',
            links: [],
        });
        const entornApp = new EntornAppModel({
            id: 3,
            app: { id: 1, description: 'Aplicació' },
            entorn: { id: 2, description: 'Producció' },
            versio: '1.0.0',
            links: [],
        });
        const usuari = new UsuariModel({
            id: 4,
            codi: 'u1',
            nom: 'Usuari 1',
            nif: undefined,
            email: 'test@example.com',
            emailAlternatiu: undefined,
            idioma: LanguageEnum.CA,
            temaAplicacio: TemaAplicacio.OBSCUR,
            estilMenu: MenuEstil.TEMA,
            rols: ['ADMIN'],
            alarmaMail: true,
            alarmaMailAgrupar: false,
            numElementsPagina: NUM_ELEMENT_PAGE_OPTIONS[1],
            links: [],
        });

        expect(app.nom).toBe('Aplicació');
        expect(entorn.codi).toBe('PRO');
        expect(entornApp.versio).toBe('1.0.0');
        expect(usuari.email).toBe('test@example.com');
        expect(UsuariModel.CODI).toBe('codi');
    });

    it('Models_quanEsConstrueixenModelsDeSalut_copienLesPropietatsEsperades', () => {
        // Verifica que els models de salut reutilitzables conserven els camps de negoci principals.
        const salut = new SalutModel({
            id: 1,
            links: [],
            entornAppId: 7,
            data: '2026-03-13',
            versio: '1.2.3',
            appEstat: SalutEstatEnum.UP,
            bdEstat: SalutEstatEnum.WARN,
        });
        const integracio = new SalutIntegracioModel({
            id: 2,
            links: [],
            codi: 'REST',
            estat: SalutEstatEnum.DOWN,
            totalOk: 1,
            totalError: 2,
        });
        const subsistema = new SalutSubsistema({
            id: 3,
            links: [],
            codi: 'SUB',
            estat: SalutEstatEnum.UNKNOWN,
            totalOk: 0,
            totalError: 1,
        });
        const missatge = new SalutMissatge({
            id: 4,
            links: [],
            data: '2026-03-13',
            nivell: NivellEnum.ERROR,
            missatge: 'Error',
        });
        const detall = new SalutDetall({
            id: 5,
            links: [],
            codi: 'versio',
            nom: 'Versió',
            valor: '1.2.3',
        });
        const informe = new SalutInformeEstatItemModel({
            alwaysUp: true,
            alwaysDown: false,
            upPercent: 100,
            warnPercent: 0,
            degradedPercent: 0,
            downPercent: 0,
            errorPercent: 0,
            maintenancePercent: 0,
            unknownPercent: 0,
        });

        expect(salut.entornAppId).toBe(7);
        expect(integracio.estat).toBe(SalutEstatEnum.DOWN);
        expect(subsistema.codi).toBe('SUB');
        expect(missatge.nivell).toBe(NivellEnum.ERROR);
        expect(detall.valor).toBe('1.2.3');
        expect(informe.alwaysUp).toBe(true);
    });
});

describe('Salut hooks', () => {
    it('SalutHooks_quanEsTraduiexUnEstat_retornaElTitolIElTooltipCorrectes', () => {
        // Comprova que el hook públic de traducció resol el títol i el tooltip segons l'estat rebut.
        const { result } = renderHook(() => useSalutEstatTranslation());

        expect(result.current.tTitle(SalutEstatEnum.UP)).toBe('UP title');
        expect(result.current.tTooltip(SalutEstatEnum.ERROR)).toBe('ERROR tooltip');
    });

    it('SalutHooks_quanEsCalculenColors_enModeClarRetornenElMapaEsperat', () => {
        // Verifica diversos hooks públics de color sobre el tema clar.
        const wrapper = withTheme('light');
        const { result: estatHook } = renderHook(() => useGetColorByStatEnum(), { wrapper });
        const { result: nivellHook } = renderHook(() => useGetColorByNivellEnum(), { wrapper });
        const { result: integracioHook } = renderHook(() => useGetColorByIntegracio(), { wrapper });
        const { result: subsistemaHook } = renderHook(() => useGetColorBySubsistema(), { wrapper });
        const { result: missatgeHook } = renderHook(() => useGetColorByMissatge(), { wrapper });
        const { result: avisHook } = renderHook(() => useGetColorByAvisTipus(), { wrapper });
        const { result: prioritatHook } = renderHook(() => useGetColorByTascaPrioritat(), { wrapper });
        const { result: estatTascaHook } = renderHook(() => useGetColorByTascaEstat(), { wrapper });

        expect(estatHook.current(SalutEstatEnum.UP)).toBe('#72bd75');
        expect(estatHook.current(SalutEstatEnum.ERROR)).toBe('#EF9A9A');
        expect(nivellHook.current(NivellEnum.WARN)).toBe('#efe271');
        expect(integracioHook.current(SalutModel.INTEGRACIO_DOWN_COUNT)).toBe('#e36161');
        expect(subsistemaHook.current(SalutModel.SUBSISTEMA_DESCONEGUT_COUNT)).toBe('#9c9c9c');
        expect(missatgeHook.current(SalutModel.MISSATGE_INFO_COUNT)).toBe('#90CAF9');
        expect(avisHook.current(AvisTipusEnum.CRITIC)).toBe('#e36161');
        expect(prioritatHook.current(TascaPrioritatEnum.ALTA)).toBe('#FFCC80');
        expect(estatTascaHook.current(TascaEstatEnum.FINALITZADA)).toBe('#72bd75');
    });

    it('SalutHooks_quanEsCalculenColorsAmbValorsDesconeguts_retornenElsDefaults', () => {
        // Comprova les branques per defecte dels hooks de color i el mapatge d'icones per estat.
        const wrapper = withTheme('dark');
        const { result: avisHook } = renderHook(() => useGetColorByAvisTipus(), { wrapper });
        const { result: prioritatHook } = renderHook(() => useGetColorByTascaPrioritat(), {
            wrapper,
        });
        const { result: estatTascaHook } = renderHook(() => useGetColorByTascaEstat(), {
            wrapper,
        });

        expect(avisHook.current('DESCONegut')).toBeTruthy();
        expect(prioritatHook.current('ALTRE')).toBeTruthy();
        expect(estatTascaHook.current('ALTRE')).toBeTruthy();
        expect(getMaterialIconByState(SalutEstatEnum.MAINTENANCE).props.children).toBe(
            'build_circle'
        );
        expect(getMaterialIconByState(SalutEstatEnum.ERROR).props.children).toBe('error');
    });
});
