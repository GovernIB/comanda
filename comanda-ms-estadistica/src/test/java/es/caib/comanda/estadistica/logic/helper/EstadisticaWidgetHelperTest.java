package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaWidgetHelper")
class EstadisticaWidgetHelperTest {

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private DimensioValorRepository dimensioValorRepository;

    @Mock
    private CacheHelper cacheHelper;

    @Mock
    private DashboardItemRepository dashboardItemRepository;

    @InjectMocks
    private EstadisticaWidgetHelper estadisticaWidgetHelper;

    private EstadisticaSimpleWidgetEntity entity;
    private EstadisticaSimpleWidget resource;

    @BeforeEach
    void setUp() {
        entity = new EstadisticaSimpleWidgetEntity();
        resource = new EstadisticaSimpleWidget();
    }

    // ========================================================================
    // 1. TESTOS PER A upsertDimensionsValors
    // ========================================================================

    @Test
    @DisplayName("upsertDimensionsValors: sincronitza correctament els valors de dimensió quan tot és vàlid")
    void upsertDimensionsValors_quanTotEsValid_llavorsSincronitzaCorrectament() {
        // Arrange
        DimensioValorEntity dimVal1 = new DimensioValorEntity();
        dimVal1.setId(1L);
        DimensioValorEntity dimVal2 = new DimensioValorEntity();
        dimVal2.setId(2L);

        entity.setDimensionsValor(new ArrayList<>());
        resource.setDimensionsValor(List.of(
            ResourceReference.toResourceReference(1L, "Desc1"),
            ResourceReference.toResourceReference(2L, "Desc2")
        ));

        when(dimensioValorRepository.findById(1L)).thenReturn(Optional.of(dimVal1));
        when(dimensioValorRepository.findById(2L)).thenReturn(Optional.of(dimVal2));

        // Act
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);

        // Assert
        assertThat(entity.getDimensionsValor()).hasSize(2);
        assertThat(entity.getDimensionsValor()).containsExactly(dimVal1, dimVal2);
    }

    @Test
    @DisplayName("upsertDimensionsValors: inicialitza la llista de l'entitat quan és null")
    void upsertDimensionsValors_quanEntityDimensionsEsNull_llavorsInicialitzaLlista() {
        // Arrange
        entity.setDimensionsValor(null);
        resource.setDimensionsValor(Collections.emptyList());

        // Act
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);

        // Assert
        assertThat(entity.getDimensionsValor()).isNotNull();
        assertThat(entity.getDimensionsValor()).isEmpty();
    }

    @Test
    @DisplayName("upsertDimensionsValors: tracta dimensions del resource com a buides quan són null")
    void upsertDimensionsValors_quanResourceDimensionsEsNull_llavorsTractaComBuida() {
        // Arrange
        entity.setDimensionsValor(new ArrayList<>());
        resource.setDimensionsValor(null);

        // Act
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);

        // Assert
        assertThat(entity.getDimensionsValor()).isEmpty();
        verify(dimensioValorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertDimensionsValors: filtra IDs nuls i entitats no trobades al repositori")
    void upsertDimensionsValors_quanHiHaIdsNulsONoExisteixen_llavorsFiltraCorrectament() {
        // Arrange
        entity.setDimensionsValor(new ArrayList<>());
        resource.setDimensionsValor(List.of(
            ResourceReference.toResourceReference(99L, "NoExisteix")
        ));

        when(dimensioValorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        estadisticaWidgetHelper.upsertDimensionsValors(entity, resource);

        // Assert
        assertThat(entity.getDimensionsValor()).isEmpty();
        verify(dimensioValorRepository, times(1)).findById(99L);
    }

    // ========================================================================
    // 2. TESTOS PER A afterConversionGetAppNom
    // ========================================================================

    @Test
    @DisplayName("afterConversionGetAppNom: assigna el nom de l'aplicació quan es troba")
    void afterConversionGetAppNom_quanAppExisteix_llavorsAssignaNom() {
        // Arrange
        entity.setAppId(10L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 10L);
        ReflectionTestUtils.setField(app, "nom", "App Test");
        when(estadisticaClientHelper.appFindById(10L)).thenReturn(app);

        // Act
        estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource);

        // Assert
        assertThat(resource.getAplicacio()).isNotNull();
        assertThat(resource.getAplicacio().getId()).isEqualTo(10L);
        assertThat(resource.getAplicacio().getDescription()).isEqualTo("App Test");
    }

    @Test
    @DisplayName("afterConversionGetAppNom: no fa res quan l'aplicació no es troba (és null)")
    void afterConversionGetAppNom_quanAppEsNull_llavorsNoFaRes() {
        // Arrange
        entity.setAppId(10L);
        when(estadisticaClientHelper.appFindById(10L)).thenReturn(null);

        // Act
        estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource);

        // Assert
        assertThat(resource.getAplicacio()).isNull();
    }

    @Test
    @DisplayName("afterConversionGetAppNom: captura l'excepció i no falla quan el client llança error")
    void afterConversionGetAppNom_quanClientLlancaExcepcio_llavorsNoFalla() {
        // Arrange
        entity.setAppId(10L);
        when(estadisticaClientHelper.appFindById(10L)).thenThrow(new RuntimeException("Error de xarxa"));

        // Act & Assert
        assertThatCode(() -> estadisticaWidgetHelper.afterConversionGetAppNom(entity, resource))
            .doesNotThrowAnyException();
        assertThat(resource.getAplicacio()).isNull();
    }

    // ========================================================================
    // 3. TESTOS PER A afterConversionGetDimensions
    // ========================================================================

    @Test
    @DisplayName("afterConversionGetDimensions: mapeja correctament les dimensions de l'entitat al resource")
    void afterConversionGetDimensions_quanEntityTeDimensions_llavorsMapejaCorrectament() {
        // Arrange
        DimensioValorEntity dimValEntity = new DimensioValorEntity();
        dimValEntity.setId(5L);

        DimensioValor dimValResource = new DimensioValor();
        dimValResource.setValor("Descripció Test");
        dimValResource.setDimensio(ResourceReference.toResourceReference(0L, "Dimensio"));

        entity.setDimensionsValor(Collections.singletonList(dimValEntity));
        when(resourceEntityMappingHelper.entityToResource(dimValEntity, DimensioValor.class)).thenReturn(dimValResource);

        // Act
        estadisticaWidgetHelper.afterConversionGetDimensions(entity, resource);

        // Assert
        assertThat(resource.getDimensionsValor()).hasSize(1);
        ResourceReference<DimensioValor, Long> ref = resource.getDimensionsValor().get(0);
        assertThat(ref.getId()).isEqualTo(5L);
        assertThat(ref.getDescription()).isEqualTo("Dimensio [Descripció Test]");
    }

    // ========================================================================
    // 4. TESTOS PER A clearDashboardWidgetCacheByWidget
    // ========================================================================

    @Test
    @DisplayName("clearDashboardWidgetCacheByWidget: esborra la caché per a cada dashboard item associat")
    void clearDashboardWidgetCacheByWidget_quanHiHaItems_llavorsEsborraCachéDeCadaUn() {
        // Arrange
        Long widgetId = 100L;
        DashboardItemEntity item1 = new DashboardItemEntity();
        item1.setId(1L);
        DashboardItemEntity item2 = new DashboardItemEntity();
        item2.setId(2L);

        when(dashboardItemRepository.findByWidgetId(widgetId)).thenReturn(List.of(item1, item2));

        // Act
        estadisticaWidgetHelper.clearDashboardWidgetCacheByWidget(widgetId);

        // Assert
        verify(cacheHelper, times(1)).evictCacheItemByPrefix(eq(DASHBOARD_WIDGET_CACHE), eq("1_"));
        verify(cacheHelper, times(1)).evictCacheItemByPrefix(eq(DASHBOARD_WIDGET_CACHE), eq("2_"));
    }

    @Test
    @DisplayName("clearDashboardWidgetCacheByWidget: no fa res quan la llista d'items és null")
    void clearDashboardWidgetCacheByWidget_quanItemsSonNull_llavorsNoFaRes() {
        // Arrange
        when(dashboardItemRepository.findByWidgetId(anyLong())).thenReturn(null);

        // Act
        estadisticaWidgetHelper.clearDashboardWidgetCacheByWidget(100L);

        // Assert
        verify(cacheHelper, never()).evictCacheItemByPrefix(anyString(), anyString());
    }

    @Test
    @DisplayName("clearDashboardWidgetCacheByWidget: no fa res quan la llista d'items és buida")
    void clearDashboardWidgetCacheByWidget_quanItemsSonBuits_llavorsNoFaRes() {
        // Arrange
        when(dashboardItemRepository.findByWidgetId(anyLong())).thenReturn(Collections.emptyList());

        // Act
        estadisticaWidgetHelper.clearDashboardWidgetCacheByWidget(100L);

        // Assert
        verify(cacheHelper, never()).evictCacheItemByPrefix(anyString(), anyString());
    }

    @Test
    @DisplayName("clearDashboardWidgetCacheByWidget: captura l'excepció i no falla quan el repositori llança error")
    void clearDashboardWidgetCacheByWidget_quanRepositoriLlancaExcepcio_llavorsNoFalla() {
        // Arrange
        when(dashboardItemRepository.findByWidgetId(anyLong())).thenThrow(new RuntimeException("Error de BD"));

        // Act & Assert
        assertThatCode(() -> estadisticaWidgetHelper.clearDashboardWidgetCacheByWidget(100L))
            .doesNotThrowAnyException();
        verify(cacheHelper, never()).evictCacheItemByPrefix(anyString(), anyString());
    }

    // ========================================================================
    // 5. TESTOS PER A clearDashboardWidgetCache
    // ========================================================================

    @Test
    @DisplayName("clearDashboardWidgetCache: esborra la caché utilitzant el prefix correcte de l'ID")
    void clearDashboardWidgetCache_quanEsCrida_llavorsEsborraCachéAmbPrefixCorrecte() {
        // Arrange
        Long dashboardItemId = 42L;

        // Act
        estadisticaWidgetHelper.clearDashboardWidgetCache(dashboardItemId);

        // Assert
        verify(cacheHelper, times(1)).evictCacheItemByPrefix(eq(DASHBOARD_WIDGET_CACHE), eq("42_"));
    }
}
