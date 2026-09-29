package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorFormulaTermeRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaNetejaServiceImpl")
class EstadisticaNetejaServiceImplTest {

    @Mock private FetRepository fetRepository;
    @Mock private IndicadorTaulaRepository indicadorTaulaRepository;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private IndicadorFormulaTermeRepository indicadorFormulaTermeRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private DimensioRepository dimensioRepository;
    @Mock private DashboardItemRepository dashboardItemRepository;
    @Mock private EstadisticaWidgetRepository estadisticaWidgetRepository;
    @Mock private CacheHelper cacheHelper;

    @InjectMocks
    private EstadisticaNetejaServiceImpl estadisticaNetejaService;

    @Test
    @DisplayName("netejaPerEntornApp: neteja exclusivament dades operatives (fets), sense tocar widgets ni catàleg")
    void netejaPerEntornApp_quanEntornAppId_llavorsNomesEsborraFets() {
        Long entornAppId = 42L;

        estadisticaNetejaService.netejaPerEntornApp(entornAppId);

        InOrder inOrder = inOrder(fetRepository, cacheHelper);
        inOrder.verify(fetRepository).deleteByEntornAppId(entornAppId);
        inOrder.verify(cacheHelper).evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);

        verifyNoInteractions(dashboardItemRepository, estadisticaWidgetRepository,
                indicadorTaulaRepository, indicadorFormulaTermeRepository, indicadorRepository,
                dimensioValorRepository, dimensioRepository);
    }

    @Test
    @DisplayName("netejaPerApp: neteja estructural d'app esborra fets, widgets, dashboard items i catàleg en ordre")
    void netejaPerApp_quanAppIdIEntornsAmbWidgets_llavorsEsborraEnOrdreEstricte() {
        Long appId = 10L;
        List<Long> entornAppIds = Arrays.asList(42L, 43L);

        estadisticaNetejaService.netejaPerApp(appId, entornAppIds, true, true);

        InOrder inOrder = inOrder(fetRepository, dashboardItemRepository, estadisticaWidgetRepository,
                indicadorTaulaRepository, indicadorFormulaTermeRepository, indicadorRepository,
                dimensioValorRepository, dimensioRepository, cacheHelper);

        // 1. Fets de tots els entorns
        inOrder.verify(fetRepository).deleteByEntornAppId(42L);
        inOrder.verify(fetRepository).deleteByEntornAppId(43L);

        // 2. Dashboard items per widget.appId
        inOrder.verify(dashboardItemRepository).deleteByWidgetAppId(appId);

        // 3. Widget dim valors per widget.appId
        inOrder.verify(estadisticaWidgetRepository).deleteWidgetDimValorByAppId(appId);

        // 4. Indicador taula per widget.appId
        inOrder.verify(indicadorTaulaRepository).deleteByWidgetAppId(appId);

        // 5. Widgets per appId
        inOrder.verify(estadisticaWidgetRepository).deleteByAppId(appId);

        // 6-11. Catàleg per a cada entorn
        inOrder.verify(indicadorTaulaRepository).deleteByIndicadorEntornAppId(42L);
        inOrder.verify(indicadorFormulaTermeRepository).deleteByEntornAppId(42L);
        inOrder.verify(indicadorRepository).clearCompactacioByEntornAppId(42L);
        inOrder.verify(indicadorRepository).deleteByEntornAppId(42L);
        inOrder.verify(dimensioValorRepository).deleteByDimensioEntornAppId(42L);
        inOrder.verify(dimensioRepository).deleteByEntornAppId(42L);

        inOrder.verify(indicadorTaulaRepository).deleteByIndicadorEntornAppId(43L);
        inOrder.verify(indicadorFormulaTermeRepository).deleteByEntornAppId(43L);
        inOrder.verify(indicadorRepository).clearCompactacioByEntornAppId(43L);
        inOrder.verify(indicadorRepository).deleteByEntornAppId(43L);
        inOrder.verify(dimensioValorRepository).deleteByDimensioEntornAppId(43L);
        inOrder.verify(dimensioRepository).deleteByEntornAppId(43L);

        // 12. Invalidar memòria cau de widgets de dashboard
        inOrder.verify(cacheHelper).evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
    }

    @Test
    @DisplayName("netejaPerApp: si esborrarCataleg és fals, només esborra fets de tots els entorns")
    void netejaPerApp_quanNomesDades_llavorsNomesEsborraFets() {
        Long appId = 10L;
        List<Long> entornAppIds = Arrays.asList(42L, 43L);

        estadisticaNetejaService.netejaPerApp(appId, entornAppIds, false, false);

        verify(fetRepository).deleteByEntornAppId(42L);
        verify(fetRepository).deleteByEntornAppId(43L);
        verify(cacheHelper).evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);

        verifyNoInteractions(dashboardItemRepository, estadisticaWidgetRepository,
                indicadorTaulaRepository, indicadorFormulaTermeRepository, indicadorRepository,
                dimensioValorRepository, dimensioRepository);
    }

    @Test
    @DisplayName("netejaPerApp: si esborrarWidgets és fals, desvincula els widgets però no els elimina")
    void netejaPerApp_quanDadesICatalegSenseWidgets_llavorsDesvinculaWidgets() {
        Long appId = 10L;
        List<Long> entornAppIds = Arrays.asList(42L, 43L);

        estadisticaNetejaService.netejaPerApp(appId, entornAppIds, true, false);

        verify(fetRepository).deleteByEntornAppId(42L);
        verify(fetRepository).deleteByEntornAppId(43L);

        // No s'eliminen els widgets ni els dashboard items
        verify(dashboardItemRepository, never()).deleteByWidgetAppId(appId);
        verify(estadisticaWidgetRepository, never()).deleteByAppId(appId);

        // Es desvinculen de les dimensions i indicadors
        verify(estadisticaWidgetRepository).clearIndicadorByEntornAppId(42L);
        verify(estadisticaWidgetRepository).clearIndicadorByEntornAppId(43L);
        verify(estadisticaWidgetRepository).clearTaulaDimensioAgrupacioByEntornAppId(42L);
        verify(estadisticaWidgetRepository).clearTaulaDimensioAgrupacioByEntornAppId(43L);
        verify(estadisticaWidgetRepository).clearGraficDescomposicioDimensioByEntornAppId(42L);
        verify(estadisticaWidgetRepository).clearGraficDescomposicioDimensioByEntornAppId(43L);
        verify(estadisticaWidgetRepository).deleteWidgetDimValorByEntornAppId(42L);
        verify(estadisticaWidgetRepository).deleteWidgetDimValorByEntornAppId(43L);

        // I s'elimina el catàleg
        verify(indicadorRepository).deleteByEntornAppId(42L);
        verify(indicadorRepository).deleteByEntornAppId(43L);

        verify(cacheHelper).evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
    }

}
