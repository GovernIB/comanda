import { renderHook, waitFor, act } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useDashboard, useDashboardWidgets } from './dashboardRequests';

const mocks = vi.hoisted(() => ({
    dashboardService: {
        isReady: true,
        getOne: vi.fn(),
        artifactReport: vi.fn(),
    },
    dashboardItemService: {
        isReady: true,
        artifactReport: vi.fn(),
    },
}));

vi.mock('reactlib', () => ({
    useResourceApiService: (resourceName: string) => {
        if (resourceName === 'dashboard') {
            return mocks.dashboardService;
        }
        if (resourceName === 'dashboardItem') {
            return mocks.dashboardItemService;
        }
        throw new Error(`Resource desconegut: ${resourceName}`);
    },
}));

vi.mock('../components/estadistiques/DashboardReactGridLayout', () => ({
    horizontalSubdivisions: 10,
}));

describe('useDashboard', () => {
    beforeEach(() => {
        mocks.dashboardService.isReady = true;
        mocks.dashboardService.getOne.mockReset();
        mocks.dashboardService.artifactReport.mockReset();
        mocks.dashboardItemService.isReady = true;
        mocks.dashboardItemService.artifactReport.mockReset();
    });

    it('useDashboard_quanLaPeticioEsCorrecta_retornaElDashboardICessaLaCarrega', async () => {
        // Comprova que el hook carrega el dashboard i deixa l'estat final sense loading.
        mocks.dashboardService.getOne.mockResolvedValue({ id: 7, nom: 'Principal' });

        const { result } = renderHook(() => useDashboard(7));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
            expect(result.current.dashboard).toEqual({ id: 7, nom: 'Principal' });
        });
    });

    it('useDashboard_quanLaPeticioFalla_exposaLexcepcioIResetejaElDashboard', async () => {
        // Verifica que el hook propaga l'error a l'estat i elimina el dashboard de la resposta.
        const exception = new Error('boom');
        mocks.dashboardService.getOne.mockRejectedValue(exception);

        const { result } = renderHook(() => useDashboard(9));

        await waitFor(() => {
            expect(result.current.loading).toBe(false);
            expect(result.current.dashboard).toBeNull();
            expect(result.current.exception).toBe(exception);
        });
    });

    it('useDashboard_quanEsForcaRefresh_tornaACarregarElDashboard', async () => {
        // Necessari perquè canvis fets al propi dashboard (p.ex. el color de fons) es reflecteixin
        // sense haver de refrescar la pàgina.
        mocks.dashboardService.getOne
            .mockResolvedValueOnce({ id: 7, colorFonsClar: '#111111' })
            .mockResolvedValueOnce({ id: 7, colorFonsClar: '#222222' });

        const { result } = renderHook(() => useDashboard(7));

        await waitFor(() => {
            expect(result.current.dashboard).toEqual({ id: 7, colorFonsClar: '#111111' });
        });

        act(() => {
            result.current.forceRefresh();
        });

        await waitFor(() => {
            expect(result.current.dashboard).toEqual({ id: 7, colorFonsClar: '#222222' });
        });
        expect(mocks.dashboardService.getOne).toHaveBeenCalledTimes(2);
    });
});

describe('useDashboardWidgets', () => {
    beforeEach(() => {
        mocks.dashboardService.isReady = true;
        mocks.dashboardService.artifactReport.mockReset();
        mocks.dashboardItemService.isReady = true;
        mocks.dashboardItemService.artifactReport.mockReset();
    });

    it('useDashboardWidgets_quanCarregaWidgets_elsOrdenaICarregaLesDadesDelsItems', async () => {
        // Comprova que el hook ordena els widgets per posició i enriqueix els no-títol amb les dades de l'item.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 2, tipus: 'GRAFIC', posX: 2, posY: 1 },
            { dashboardItemId: 1, tipus: 'TITOL', posX: 0, posY: 0 },
            { dashboardItemId: 3, tipus: 'TAULA', posX: 1, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport
            .mockResolvedValueOnce([{ dashboardItemId: 2, titol: 'Widget 2', error: true }])
            .mockResolvedValueOnce([{ dashboardItemId: 3, titol: 'Widget 3', error: false }]);

        const { result } = renderHook(() => useDashboardWidgets(12));

        await waitFor(() => {
            expect(result.current.loadingWidgetPositions).toBe(false);
            expect(result.current.loadingWidgetData).toBe(false);
            expect(result.current.dashboardWidgets?.map((widget: any) => widget.dashboardItemId)).toEqual([1, 3, 2]);
            expect(result.current.errorDashboardWidgets).toHaveLength(1);
            expect(result.current.errorDashboardWidgets?.[0].dashboardItemId).toBe(2);
        });
    });

    it('useDashboardWidgets_quanLaPeticioDUnWidgetFalla_elMarcaComAErroniEnLlocDePerdreLoSilenciosament', async () => {
        // Regressió: si la petició HTTP d'un widget falla (rebutja la promesa, no una resposta 200 amb
        // error:true), el widget ha de quedar marcat com a erroni perquè es pugui mostrar el missatge, en
        // lloc de quedar-se penjat sense actualitzar-se mai.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'GRAFIC', posX: 0, posY: 0 },
            { dashboardItemId: 2, tipus: 'TAULA', posX: 1, posY: 0 },
        ]);
        const exception = Object.assign(new Error('ORA-00904'), { description: 'identificador no vàlid' });
        mocks.dashboardItemService.artifactReport
            .mockRejectedValueOnce(exception)
            .mockResolvedValueOnce([{ dashboardItemId: 2, titol: 'Widget 2', error: false }]);

        const { result } = renderHook(() => useDashboardWidgets(30));

        await waitFor(() => {
            expect(result.current.loadingWidgetData).toBe(false);
            expect(result.current.errorDashboardWidgets).toHaveLength(1);
            expect(result.current.errorDashboardWidgets?.[0]).toMatchObject({
                dashboardItemId: 1,
                error: true,
                errorMsg: 'ORA-00904',
                errorTrace: 'identificador no vàlid',
            });
        });
    });

    it('useDashboardWidgets_quanTraceEnabledEsCert_demanaLaTracaAlBackend', async () => {
        // La pantalla de disseny ha de poder demanar la traça real de l'error de backend.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'GRAFIC', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([{ dashboardItemId: 1, titol: 'Widget 1' }]);

        const { result } = renderHook(() => useDashboardWidgets(40, false, undefined, true));

        await waitFor(() => {
            expect(result.current.loadingWidgetData).toBe(false);
        });
        expect(mocks.dashboardItemService.artifactReport).toHaveBeenCalledWith(1, {
            code: 'widget_data',
            data: { temaFosc: false, filtreSeleccio: undefined },
            urlData: { trace: 'true' },
        });
    });

    it('useDashboardWidgets_quanTraceEnabledEsFals_noDemanaLaTracaAlBackend', async () => {
        // La visualització del dashboard mai no ha de demanar ni mostrar la traça real.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'GRAFIC', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([{ dashboardItemId: 1, titol: 'Widget 1' }]);

        const { result } = renderHook(() => useDashboardWidgets(41));

        await waitFor(() => {
            expect(result.current.loadingWidgetData).toBe(false);
        });
        expect(mocks.dashboardItemService.artifactReport).toHaveBeenCalledWith(1, {
            code: 'widget_data',
            data: { temaFosc: false, filtreSeleccio: undefined },
        });
    });

    it('useDashboardWidgets_quanLaPeticioFallaAmbTracaReal_lUsaComAErrorTraceEnLlocDeLaDescripcio', async () => {
        // Si el backend retorna una traça real (stackTrace), s'ha de mostrar en lloc del missatge curt.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'GRAFIC', posX: 0, posY: 0 },
        ]);
        const exception = Object.assign(new Error('ORA-00904'), {
            description: 'identificador no vàlid',
            stackTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
        });
        mocks.dashboardItemService.artifactReport.mockRejectedValueOnce(exception);

        const { result } = renderHook(() => useDashboardWidgets(42, false, undefined, true));

        await waitFor(() => {
            expect(result.current.errorDashboardWidgets?.[0]).toMatchObject({
                errorMsg: 'ORA-00904',
                errorTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
            });
        });
    });

    it('useDashboardWidgets_quanElBackendRetornaErrorTraceEnUna200IioTraceEnabledEsFals_lElimina', async () => {
        // Cas real: un error de dades/SQL dins la generació del widget NO llença excepció, torna una
        // resposta 200 amb error:true i errorTrace ja ple (traça Java sencera). La visualització no l'ha
        // de mostrar mai, encara que el backend l'hagi enviat.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'GRAFIC', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([
            {
                dashboardItemId: 1,
                error: true,
                errorMsg: 'Error processing item 1: ORA-00904',
                errorTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
            },
        ]);

        const { result } = renderHook(() => useDashboardWidgets(50));

        await waitFor(() => {
            expect(result.current.errorDashboardWidgets).toHaveLength(1);
        });
        expect(result.current.errorDashboardWidgets?.[0]).toMatchObject({
            error: true,
            errorMsg: 'Error processing item 1: ORA-00904',
        });
        expect(result.current.errorDashboardWidgets?.[0].errorTrace).toBeUndefined();
    });

    it('useDashboardWidgets_quanElBackendRetornaErrorTraceEnUna200IioTraceEnabledEsCert_laMante', async () => {
        // La pantalla de disseny sí que ha de veure la traça real quan el backend ja la retorna en un 200.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'GRAFIC', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([
            {
                dashboardItemId: 1,
                error: true,
                errorMsg: 'Error processing item 1: ORA-00904',
                errorTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
            },
        ]);

        const { result } = renderHook(() => useDashboardWidgets(51, false, undefined, true));

        await waitFor(() => {
            expect(result.current.errorDashboardWidgets).toHaveLength(1);
        });
        expect(result.current.errorDashboardWidgets?.[0]).toMatchObject({
            error: true,
            errorMsg: 'Error processing item 1: ORA-00904',
            errorTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
        });
    });

    it('useDashboardWidgets_quanEsForcaRefresh_tornaALlançarLaCarrega', async () => {
        // Verifica que el callback de refresc reutilitza la mateixa lògica i repeteix les peticions.
        mocks.dashboardService.artifactReport.mockResolvedValue([]);

        const { result } = renderHook(() => useDashboardWidgets(15));

        await waitFor(() => {
            expect(mocks.dashboardService.artifactReport).toHaveBeenCalledTimes(1);
        });

        act(() => {
            result.current.forceRefresh();
        });

        await waitFor(() => {
            expect(mocks.dashboardService.artifactReport).toHaveBeenCalledTimes(2);
        });
    });

    it('useDashboardWidgets_quanEsRefrescaUnWidget_nomesActualitzaAquellItemSenseRecarregarLesPosicions', async () => {
        // Comprova que refreshWidget actualitza només el widget indicat, sense tornar a demanar les posicions.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'SIMPLE', posX: 0, posY: 0 },
            { dashboardItemId: 2, tipus: 'GRAFIC', posX: 1, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport
            .mockResolvedValueOnce([{ dashboardItemId: 1, titol: 'Widget 1 vell' }])
            .mockResolvedValueOnce([{ dashboardItemId: 2, titol: 'Widget 2' }]);

        const { result } = renderHook(() => useDashboardWidgets(20));

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.map((widget: any) => widget.titol)).toEqual([
                'Widget 1 vell',
                'Widget 2',
            ]);
        });

        mocks.dashboardItemService.artifactReport.mockResolvedValueOnce([
            { dashboardItemId: 1, titol: 'Widget 1 actualitzat' },
        ]);
        mocks.dashboardService.artifactReport.mockClear();

        act(() => {
            result.current.refreshWidget(1);
        });

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.map((widget: any) => widget.titol)).toEqual([
                'Widget 1 actualitzat',
                'Widget 2',
            ]);
        });
        expect(mocks.dashboardService.artifactReport).not.toHaveBeenCalled();
    });

    it('useDashboardWidgets_quanEsRefrescaUnWidgetAmbCampBuidat_elCampNoConservaElValorAnterior', async () => {
        // Regressió: el backend omet els camps null, així que si el widget es refresca després de buidar la
        // descripció, la resposta ja no en porta i s'havia de conservar l'antiga per error.
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'TAULA', posX: 3, posY: 4, width: 5, height: 6 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValueOnce([
            { dashboardItemId: 1, titol: 'Widget 1', descripcio: 'Descripció vella' },
        ]);

        const { result } = renderHook(() => useDashboardWidgets(21));

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]?.descripcio).toBe('Descripció vella');
        });

        mocks.dashboardItemService.artifactReport.mockResolvedValueOnce([
            { dashboardItemId: 1, titol: 'Widget 1' },
        ]);

        act(() => {
            result.current.refreshWidget(1);
        });

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]?.descripcio).toBeUndefined();
        });
        // Les dades d'identitat i posició no s'han de perdre.
        expect(result.current.dashboardWidgets?.[0]).toMatchObject({
            dashboardItemId: 1, tipus: 'TAULA', posX: 3, posY: 4, width: 5, height: 6, titol: 'Widget 1',
        });
    });

    it('useDashboardWidgets_quanEsActualitzaElLayout_canviaNomesLaPosicioIMidaDelsElementsIndicats', async () => {
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'SIMPLE', posX: 0, posY: 0, width: 3, height: 3 },
            { dashboardTitolId: 7, tipus: 'TITOL', posX: 0, posY: 5, width: 10, height: 2 },
            { dashboardItemId: 2, tipus: 'GRAFIC', posX: 4, posY: 0, width: 3, height: 3 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([{ titol: 'Widget' }]);

        const { result } = renderHook(() => useDashboardWidgets(22));

        await waitFor(() => {
            expect(result.current.dashboardWidgets).toHaveLength(3);
        });

        act(() => {
            result.current.updateWidgetsLayout([
                { id: '1', x: 8, y: 9, w: 5, h: 6 },
                { id: '7', x: 1, y: 5, w: 10, h: 2 },
            ]);
        });

        const byId = (id: number, key: 'dashboardItemId' | 'dashboardTitolId' = 'dashboardItemId') =>
            result.current.dashboardWidgets?.find((widget: any) => widget[key] === id);
        expect(byId(1)).toMatchObject({ posX: 8, posY: 9, width: 5, height: 6 });
        expect(byId(7, 'dashboardTitolId')).toMatchObject({ posX: 1, posY: 5 });
        expect(byId(2)).toMatchObject({ posX: 4, posY: 0, width: 3, height: 3 });
    });

    it('useDashboardWidgets_quanRefreshWidgetFalla_elMarcaComAErroniSenseAfectarElsAltres', async () => {
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'SIMPLE', posX: 0, posY: 0 },
            { dashboardItemId: 2, tipus: 'GRAFIC', posX: 1, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport
            .mockResolvedValueOnce([{ dashboardItemId: 1, titol: 'Widget 1' }])
            .mockResolvedValueOnce([{ dashboardItemId: 2, titol: 'Widget 2' }]);

        const { result } = renderHook(() => useDashboardWidgets(31));

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.map((widget: any) => widget.titol)).toEqual([
                'Widget 1',
                'Widget 2',
            ]);
        });

        const exception = Object.assign(new Error('ORA-00904'), { description: 'identificador no vàlid' });
        mocks.dashboardItemService.artifactReport.mockRejectedValueOnce(exception);

        act(() => {
            result.current.refreshWidget(2);
        });

        await waitFor(() => {
            const widget2 = result.current.dashboardWidgets?.find((w: any) => w.dashboardItemId === 2);
            expect(widget2).toMatchObject({ error: true, errorMsg: 'ORA-00904', errorTrace: 'identificador no vàlid' });
            const widget1 = result.current.dashboardWidgets?.find((w: any) => w.dashboardItemId === 1);
            expect(widget1).toMatchObject({ titol: 'Widget 1' });
        });
    });

    it('useDashboardWidgets_quanRefreshWidgetAmbTraceEnabled_demanaLaTracaAlBackend', async () => {
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'SIMPLE', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([{ dashboardItemId: 1, titol: 'Widget 1' }]);

        const { result } = renderHook(() => useDashboardWidgets(43, false, undefined, true));

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]).toMatchObject({ titol: 'Widget 1' });
        });

        mocks.dashboardItemService.artifactReport.mockClear();
        mocks.dashboardItemService.artifactReport.mockResolvedValueOnce([
            { dashboardItemId: 1, titol: 'Widget 1 actualitzat' },
        ]);

        act(() => {
            result.current.refreshWidget(1);
        });

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]).toMatchObject({ titol: 'Widget 1 actualitzat' });
        });
        expect(mocks.dashboardItemService.artifactReport).toHaveBeenCalledWith(1, {
            code: 'widget_data',
            data: { temaFosc: false, filtreSeleccio: undefined },
            urlData: { trace: 'true' },
        });
    });

    it('useDashboardWidgets_quanRefreshWidgetRetornaErrorTraceEnUna200IioTraceEnabledEsFals_lElimina', async () => {
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'SIMPLE', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([{ dashboardItemId: 1, titol: 'Widget 1' }]);

        const { result } = renderHook(() => useDashboardWidgets(45));

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]).toMatchObject({ titol: 'Widget 1' });
        });

        mocks.dashboardItemService.artifactReport.mockResolvedValueOnce([
            {
                dashboardItemId: 1,
                error: true,
                errorMsg: 'Error processing item 1: ORA-00904',
                errorTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
            },
        ]);

        act(() => {
            result.current.refreshWidget(1);
        });

        await waitFor(() => {
            const widget1 = result.current.dashboardWidgets?.find((w: any) => w.dashboardItemId === 1);
            expect(widget1).toMatchObject({ error: true, errorMsg: 'Error processing item 1: ORA-00904' });
            expect(widget1?.errorTrace).toBeUndefined();
        });
    });

    it('useDashboardWidgets_quanRefreshWidgetFallaAmbTracaReal_lUsaComAErrorTrace', async () => {
        mocks.dashboardService.artifactReport.mockResolvedValue([
            { dashboardItemId: 1, tipus: 'SIMPLE', posX: 0, posY: 0 },
        ]);
        mocks.dashboardItemService.artifactReport.mockResolvedValue([{ dashboardItemId: 1, titol: 'Widget 1' }]);

        const { result } = renderHook(() => useDashboardWidgets(44, false, undefined, true));

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]).toMatchObject({ titol: 'Widget 1' });
        });

        const exception = Object.assign(new Error('ORA-00904'), {
            description: 'identificador no vàlid',
            stackTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
        });
        mocks.dashboardItemService.artifactReport.mockRejectedValueOnce(exception);

        act(() => {
            result.current.refreshWidget(1);
        });

        await waitFor(() => {
            expect(result.current.dashboardWidgets?.[0]).toMatchObject({
                error: true,
                errorMsg: 'ORA-00904',
                errorTrace: 'oracle.jdbc.OracleDatabaseException: ORA-00904\n\tat ...',
            });
        });
    });
});
