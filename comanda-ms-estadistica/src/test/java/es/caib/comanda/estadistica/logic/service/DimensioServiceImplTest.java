package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EntitatResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DimensioServiceImpl")
class DimensioServiceImplTest {

    @Mock
    private SpringFilterHelper springFilterHelper;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private FetRepository fetRepository;

    @Mock
    private EntitatResolverHelper entitatResolverHelper;

    @Mock
    private DimensioRepository dimensioRepository;

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private DimensioServiceImpl dimensioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dimensioService, "resourceEntityMappingHelper", resourceEntityMappingHelper);
    }

    @Test
    @DisplayName("namedFilterToSpecification retorna null per a filtres desconeguts")
    void namedFilterToSpecification_quanFiltreDesconegut_retornaNull() {
        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification("desconegut");

        // Assert
        assertThat(spec).isNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona el filtre per app correctament")
    void namedFilterToSpecification_quanFiltreApp_retornaSpecification() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":1";
        List<Long> ids = Arrays.asList(10L, 20L);
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(ids);

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("namedFilterToSpecification retorna especificació disjoint quan no hi ha entorns")
    void namedFilterToSpecification_quanSenseEntorns_retornaDisjunction() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":1";
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(Collections.emptyList());

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona filtre sense app id")
    void namedFilterToSpecification_quanSenseAppId_retornaDisjunction() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":";

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verifyNoInteractions(estadisticaClientHelper);
    }

    @Test
    @DisplayName("additionalSpringFilter afegeix filtres d'aplicació si s'indiquen a namedQueries")
    void additionalSpringFilter_quanNamedQueriesAmbApp_afegeixFiltre() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter retorna null quan no hi ha filtres")
    void additionalSpringFilter_quanSenseFiltres_retornaNull() {
        // Act
        String result = dimensioService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("additionalSpringFilter combina filtre actual amb namedQueries")
    void additionalSpringFilter_quanFiltreINamedQueries_combinaFiltres() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        String result = dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        assertThat(result).isNotNull();
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    // ============================================================================
    // TESTS PER A FET_CONS_ACTION_EXECUTOR
    // ============================================================================

    @Test
    @DisplayName("FetConsActionExecutor crea dimensió CONS si no existeix i és ORGAN_GESTOR")
    void fetConsActionExecutor_quanEsOrganGestorSenseCons_creaDimensioCons() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Collections.emptyList());
        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", null)).thenReturn(Collections.emptyList());

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        verify(dimensioRepository).save(argThat(d ->
            d.getCodi().equals("CONS") &&
                d.getNom().equals("Conselleria") &&
                d.getTipus() == TipusDimensioEnum.CONSELLERIA &&
                d.getEntornAppId().equals(1L)
        ));
    }

    @Test
    @DisplayName("FetConsActionExecutor no crea dimensió CONS si ja existeix")
    void fetConsActionExecutor_quanEsOrganGestorAmbCons_noCreaDimensio() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        DimensioEntity existingCons = new DimensioEntity();
        existingCons.setTipus(TipusDimensioEnum.CONSELLERIA);
        existingCons.setCodi("CONS");

        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Arrays.asList(existingCons));
        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", null)).thenReturn(Collections.emptyList());

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        verify(dimensioRepository, never()).save(any());
    }

    @Test
    @DisplayName("FetConsActionExecutor actualitza CONS quan hi ha conselleria")
    void fetConsActionExecutor_quanHiHaConselleria_actualitzaJson() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        DimensioEntity existingCons = new DimensioEntity();
        existingCons.setTipus(TipusDimensioEnum.CONSELLERIA);
        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Arrays.asList(existingCons));

        FetEntity fet = new FetEntity();
        Map<String, String> dimensionsJson = new HashMap<>();
        dimensionsJson.put("TEST_ORGAN", "ORG123");
        fet.setDimensionsJson(dimensionsJson);

        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", null)).thenReturn(Arrays.asList(fet));
        when(entitatResolverHelper.resolveConselleria(eq(1L), eq("ORG123"), any())).thenReturn("CONS456");

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        assertThat(fet.getDimensionsJson()).containsEntry("CONS", "CONS456");
        verify(fetRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("FetConsActionExecutor elimina CONS quan no hi ha conselleria")
    void fetConsActionExecutor_quanNoHiHaConselleria_eliminaCons() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        DimensioEntity existingCons = new DimensioEntity();
        existingCons.setTipus(TipusDimensioEnum.CONSELLERIA);
        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Arrays.asList(existingCons));

        FetEntity fet = new FetEntity();
        Map<String, String> dimensionsJson = new HashMap<>();
        dimensionsJson.put("TEST_ORGAN", "ORG123");
        dimensionsJson.put("CONS", "OLD_CONS");
        fet.setDimensionsJson(dimensionsJson);

        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", null)).thenReturn(Arrays.asList(fet));
        when(entitatResolverHelper.resolveConselleria(eq(1L), eq("ORG123"), any())).thenReturn(null);

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        assertThat(fet.getDimensionsJson()).doesNotContainKey("CONS");
        verify(fetRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("FetConsActionExecutor no fa res quan no és ORGAN_GESTOR")
    void fetConsActionExecutor_quanNoEsOrganGestor_noFaRes() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.CONSELLERIA);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_CONS");

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        verifyNoInteractions(dimensioRepository, fetRepository, entitatResolverHelper);
    }

    @Test
    @DisplayName("FetConsActionExecutor no guarda quan no hi ha canvis")
    void fetConsActionExecutor_quanNoHiHaCanvis_noGuarda() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        DimensioEntity existingCons = new DimensioEntity();
        existingCons.setTipus(TipusDimensioEnum.CONSELLERIA);
        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Arrays.asList(existingCons));
        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", null)).thenReturn(Collections.emptyList());

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        verify(fetRepository, never()).saveAll(anyList());
    }
}
