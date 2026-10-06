package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
    @Mock private DashboardRepository dashboardRepository;
    @Mock private EstadisticaClientHelper estadisticaClientHelper;
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

        verifyNoInteractions(dashboardItemRepository, dashboardRepository, estadisticaWidgetRepository,
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

    @Test
    @DisplayName("netejaPerEntornAppEsborrat: esborra fets, dashboards, elements, widgets sense ús i catàleg en ordre")
    void netejaPerEntornAppEsborrat_quanWidgetsSenseAltresUsos_llavorsEsborraTot() {
        Long entornAppId = 42L, appId = 5L, entornId = 7L;
        EstadisticaWidgetEntity<?> widgetDashboard = widget(1L, appId);
        EstadisticaWidgetEntity<?> widgetAltreDashboard = widget(2L, appId);
        EstadisticaWidgetEntity<?> widgetCataleg = widget(3L, appId);
        DashboardEntity dashboard = dashboard(100L);
        DashboardItemEntity elementDashboard = item(10L, dashboard, widgetDashboard, entornId);
        dashboard.setItems(List.of(elementDashboard));
        DashboardItemEntity elementAltreDashboard = item(20L, dashboard(200L), widgetAltreDashboard, entornId);

        when(dashboardRepository.findByAppIdAndEntornId(appId, entornId)).thenReturn(List.of(dashboard));
        when(dashboardItemRepository.findByEntornIdAndWidgetAppId(entornId, appId)).thenReturn(List.of(elementDashboard, elementAltreDashboard));
        when(estadisticaWidgetRepository.findIdsByCatalegEntornAppId(entornAppId)).thenReturn(List.of(3L));
        when(estadisticaWidgetRepository.findById(1L)).thenReturn(Optional.of(widgetDashboard));
        when(estadisticaWidgetRepository.findById(2L)).thenReturn(Optional.of(widgetAltreDashboard));
        when(estadisticaWidgetRepository.findById(3L)).thenReturn(Optional.of(widgetCataleg));
        when(dashboardItemRepository.findByWidgetId(any())).thenReturn(Collections.emptyList());

        estadisticaNetejaService.netejaPerEntornAppEsborrat(entornAppId, appId, entornId);

        InOrder inOrder = inOrder(fetRepository, dashboardItemRepository, dashboardRepository, estadisticaWidgetRepository,
                indicadorTaulaRepository, indicadorFormulaTermeRepository, indicadorRepository,
                dimensioValorRepository, dimensioRepository, cacheHelper);
        inOrder.verify(fetRepository).deleteByEntornAppId(entornAppId);
        // Els elements del dashboard esborrat s'esborren en cascada amb el dashboard
        inOrder.verify(dashboardItemRepository).deleteAll(List.of(elementAltreDashboard));
        inOrder.verify(dashboardRepository).deleteAll(List.of(dashboard));
        inOrder.verify(estadisticaWidgetRepository).delete(widgetDashboard);
        inOrder.verify(estadisticaWidgetRepository).delete(widgetAltreDashboard);
        inOrder.verify(estadisticaWidgetRepository).delete(widgetCataleg);
        inOrder.verify(estadisticaWidgetRepository).clearIndicadorByEntornAppId(entornAppId);
        inOrder.verify(estadisticaWidgetRepository).clearTaulaDimensioAgrupacioByEntornAppId(entornAppId);
        inOrder.verify(estadisticaWidgetRepository).clearGraficDescomposicioDimensioByEntornAppId(entornAppId);
        inOrder.verify(estadisticaWidgetRepository).deleteWidgetDimValorByEntornAppId(entornAppId);
        inOrder.verify(indicadorTaulaRepository).deleteByIndicadorEntornAppId(entornAppId);
        inOrder.verify(indicadorFormulaTermeRepository).deleteByEntornAppId(entornAppId);
        inOrder.verify(indicadorRepository).clearCompactacioByEntornAppId(entornAppId);
        inOrder.verify(indicadorRepository).deleteByEntornAppId(entornAppId);
        inOrder.verify(dimensioValorRepository).deleteByDimensioEntornAppId(entornAppId);
        inOrder.verify(dimensioRepository).deleteByEntornAppId(entornAppId);
        inOrder.verify(cacheHelper).evictCache(HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE);
    }

    @Test
    @DisplayName("netejaPerEntornAppEsborrat: un widget usat en un dashboard d'un altre entorn no s'esborra i es reapunta al catàleg equivalent")
    void netejaPerEntornAppEsborrat_quanWidgetUsatEnAltreEntorn_llavorsEsReapunta() {
        Long entornAppId = 42L, appId = 5L, entornId = 7L, altreEntornId = 8L, altreEntornAppId = 43L;
        DimensioEntity dimensio = dimensio("ORG", entornAppId);
        DimensioEntity dimensioAltre = dimensio("ORG", altreEntornAppId);
        DimensioValorEntity valor = dimensioValor(dimensio, "A");
        DimensioValorEntity valorAltre = dimensioValor(dimensioAltre, "A");
        IndicadorEntity indicador = indicador("IND", entornAppId);
        IndicadorEntity indicadorAltre = indicador("IND", altreEntornAppId);
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setId(1L);
        widget.setAppId(appId);
        widget.setDimensioAgrupacio(dimensio);
        widget.setDimensionsValor(new ArrayList<>(List.of(valor)));
        IndicadorTaulaEntity columna = new IndicadorTaulaEntity();
        columna.setIndicador(indicador);
        columna.setWidget(widget);

        when(dashboardRepository.findByAppIdAndEntornId(appId, entornId)).thenReturn(Collections.emptyList());
        when(dashboardItemRepository.findByEntornIdAndWidgetAppId(entornId, appId)).thenReturn(Collections.emptyList());
        when(estadisticaWidgetRepository.findIdsByCatalegEntornAppId(entornAppId)).thenReturn(List.of(1L));
        when(estadisticaWidgetRepository.findById(1L)).thenReturn(Optional.of(widget));
        when(dashboardItemRepository.findByWidgetId(1L)).thenReturn(List.of(item(30L, dashboard(300L), widget, altreEntornId)));
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(appId, altreEntornId)).thenReturn(EntornApp.builder().id(altreEntornAppId).build());
        when(indicadorTaulaRepository.findByWidgetIdAndIndicadorEntornAppId(1L, entornAppId)).thenReturn(List.of(columna));
        when(indicadorRepository.findByCodiAndEntornAppId("IND", altreEntornAppId)).thenReturn(Optional.of(indicadorAltre));
        when(dimensioRepository.findByCodiAndEntornAppId("ORG", altreEntornAppId)).thenReturn(Optional.of(dimensioAltre));
        when(dimensioValorRepository.findByDimensioAndValor(dimensioAltre, "A")).thenReturn(Optional.of(valorAltre));

        estadisticaNetejaService.netejaPerEntornAppEsborrat(entornAppId, appId, entornId);

        verify(estadisticaWidgetRepository, never()).delete(any());
        assertSame(indicadorAltre, columna.getIndicador());
        assertSame(dimensioAltre, widget.getDimensioAgrupacio());
        assertEquals(List.of(valorAltre), widget.getDimensionsValor());
        verify(indicadorRepository).deleteByEntornAppId(entornAppId);
        verify(dimensioRepository).deleteByEntornAppId(entornAppId);
    }

    @Test
    @DisplayName("netejaPerEntornAppEsborrat: si no hi ha entornApp alternatiu, el widget usat es manté però no es reapunta")
    void netejaPerEntornAppEsborrat_quanSenseEntornAppAlternatiu_llavorsNoEsReapunta() {
        Long entornAppId = 42L, appId = 5L, entornId = 7L, altreEntornId = 8L;
        EstadisticaWidgetEntity<?> widget = widget(1L, appId);

        when(dashboardRepository.findByAppIdAndEntornId(appId, entornId)).thenReturn(Collections.emptyList());
        when(dashboardItemRepository.findByEntornIdAndWidgetAppId(entornId, appId)).thenReturn(Collections.emptyList());
        when(estadisticaWidgetRepository.findIdsByCatalegEntornAppId(entornAppId)).thenReturn(List.of(1L));
        when(estadisticaWidgetRepository.findById(1L)).thenReturn(Optional.of(widget));
        when(dashboardItemRepository.findByWidgetId(1L)).thenReturn(List.of(item(30L, dashboard(300L), widget, altreEntornId)));
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(appId, altreEntornId)).thenReturn(null);

        estadisticaNetejaService.netejaPerEntornAppEsborrat(entornAppId, appId, entornId);

        verify(estadisticaWidgetRepository, never()).delete(any());
        verify(indicadorTaulaRepository, never()).findByWidgetIdAndIndicadorEntornAppId(any(), any());
        verify(estadisticaWidgetRepository).clearTaulaDimensioAgrupacioByEntornAppId(entornAppId);
        verify(indicadorTaulaRepository).deleteByIndicadorEntornAppId(entornAppId);
        verify(indicadorRepository).deleteByEntornAppId(entornAppId);
    }

    @Test
    @DisplayName("netejaPerEntornAppEsborrat: sense app ni entorn no es toquen dashboards però s'esborra el catàleg")
    void netejaPerEntornAppEsborrat_quanSenseAppNiEntorn_llavorsNoToquaDashboards() {
        Long entornAppId = 42L;
        when(estadisticaWidgetRepository.findIdsByCatalegEntornAppId(entornAppId)).thenReturn(Collections.emptyList());

        estadisticaNetejaService.netejaPerEntornAppEsborrat(entornAppId, null, null);

        verifyNoInteractions(dashboardRepository);
        verify(dashboardItemRepository, never()).findByEntornIdAndWidgetAppId(any(), any());
        verify(fetRepository).deleteByEntornAppId(entornAppId);
        verify(indicadorRepository).deleteByEntornAppId(entornAppId);
        verify(dimensioRepository).deleteByEntornAppId(entornAppId);
    }

    private static EstadisticaWidgetEntity<?> widget(Long id, Long appId) {
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setId(id);
        widget.setAppId(appId);
        return widget;
    }

    private static DashboardEntity dashboard(Long id) {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(id);
        return dashboard;
    }

    private static DashboardItemEntity item(Long id, DashboardEntity dashboard, EstadisticaWidgetEntity<?> widget, Long entornId) {
        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(id);
        item.setDashboard(dashboard);
        item.setWidget(widget);
        item.setEntornId(entornId);
        return item;
    }

    private static DimensioEntity dimensio(String codi, Long entornAppId) {
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi(codi);
        dimensio.setEntornAppId(entornAppId);
        return dimensio;
    }

    private static DimensioValorEntity dimensioValor(DimensioEntity dimensio, String valor) {
        DimensioValorEntity dimensioValor = new DimensioValorEntity();
        dimensioValor.setDimensio(dimensio);
        dimensioValor.setValor(valor);
        return dimensioValor;
    }

    private static IndicadorEntity indicador(String codi, Long entornAppId) {
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setCodi(codi);
        indicador.setEntornAppId(entornAppId);
        return indicador;
    }

}
