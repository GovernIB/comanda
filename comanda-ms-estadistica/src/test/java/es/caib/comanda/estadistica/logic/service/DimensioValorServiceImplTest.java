package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.dir3.SistemaExternException;
import es.caib.comanda.estadistica.logic.helper.EntitatResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.helper.UnitatOrganitzativaHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.UnitatOrganitzativa;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.EntitatEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.UnitatOrganitzativaEntity;
import es.caib.comanda.estadistica.persist.repository.UnitatOrganitzativaRepository;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DimensioValorServiceImpl")
class DimensioValorServiceImplTest {

    @Mock
    private SpringFilterHelper springFilterHelper;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private UnitatOrganitzativaRepository unitatOrganitzativaRepository;

    @Mock
    private UnitatOrganitzativaHelper unitatOrganitzativaHelper;

    @Mock
    private EntitatResolverHelper entitatResolverHelper;

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private DimensioValorServiceImpl dimensioValorService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dimensioValorService, "resourceEntityMappingHelper", resourceEntityMappingHelper);
    }

    // ==========================================
    // Tests existents
    // ==========================================

    @Test
    @DisplayName("namedFilterToSpecification retorna null per a filtres desconeguts")
    void namedFilterToSpecification_quanFiltreDesconegut_retornaNull() {
        // Act
        Specification<DimensioValorEntity> spec = dimensioValorService.namedFilterToSpecification("desconegut");

        // Assert
        assertThat(spec).isNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona el filtre per app correctament")
    void namedFilterToSpecification_quanFiltreApp_retornaSpecification() {
        // Arrange
        String filterName = DimensioValor.NAMED_FILTER_BY_APP_GROUP_BY_VALOR + ":1";
        List<Long> ids = Arrays.asList(10L, 20L);
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(ids);

        // Act
        Specification<DimensioValorEntity> spec = dimensioValorService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("namedFilterToSpecification retorna especificació disjoint quan no hi ha entorns")
    void namedFilterToSpecification_quanSenseEntorns_retornaDisjunction() {
        // Arrange
        String filterName = DimensioValor.NAMED_FILTER_BY_APP_GROUP_BY_VALOR + ":1";
        when(estadisticaClientHelper.getEntornAppsIdByAppId(1L)).thenReturn(Collections.emptyList());

        // Act
        Specification<DimensioValorEntity> spec = dimensioValorService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verify(estadisticaClientHelper).getEntornAppsIdByAppId(1L);
    }

    @Test
    @DisplayName("namedFilterToSpecification gestiona filtre sense app id")
    void namedFilterToSpecification_quanSenseAppId_retornaDisjunction() {
        // Arrange
        String filterName = DimensioValor.NAMED_FILTER_BY_APP_GROUP_BY_VALOR + ":";

        // Act
        Specification<DimensioValorEntity> spec = dimensioValorService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verifyNoInteractions(estadisticaClientHelper);
    }

    @Test
    @DisplayName("additionalSpringFilter afegeix filtres d'aplicació si s'indiquen a namedQueries")
    void additionalSpringFilter_quanNamedQueriesAmbApp_afegeixFiltre() {
        // Arrange
        String currentFilter = "valor:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        dimensioValorService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter retorna null quan no hi ha filtres")
    void additionalSpringFilter_quanSenseFiltres_retornaNull() {
        // Act
        String result = dimensioValorService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("additionalSpringFilter combina filtre actual amb namedQueries")
    void additionalSpringFilter_quanFiltreINamedQueries_combinaFiltres() {
        // Arrange
        String currentFilter = "valor:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        String result = dimensioValorService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        assertThat(result).isNotNull();
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    // ==========================================
    // Tests per a la nova lògica (Objectes reals amb 'new')
    // ==========================================

    @Test
    @DisplayName("namedFilterToSpecification gestiona el filtre per UO nom correctament")
    void namedFilterToSpecification_quanFiltreUoNom_retornaSpecification() {
        // Arrange
        String filterName = DimensioValor.NAMED_FILTER_BY_UO_NOM + ":test";

        // Act
        Specification<DimensioValorEntity> spec = dimensioValorService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification retorna conjunció quan el terme de cerca UO és buit")
    void namedFilterToSpecification_quanTermUoBuit_retornaConjunction() {
        // Arrange
        String filterName = DimensioValor.NAMED_FILTER_BY_UO_NOM + ":";

        // Act
        Specification<DimensioValorEntity> spec = dimensioValorService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("afterConversion mapeja UnitatOrganitzativa i Entitat correctament")
    void afterConversion_mapejaCorrectament() {
        // Arrange
        DimensioValorEntity entity1 = new DimensioValorEntity();
        DimensioEntity dimensio1 = new DimensioEntity();
        dimensio1.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity1.setDimensio(dimensio1);
        entity1.setValor("UO1");

        DimensioValorEntity entity2 = new DimensioValorEntity();
        DimensioEntity dimensio2 = new DimensioEntity();
        dimensio2.setTipus(TipusDimensioEnum.ENTITAT);
        entity2.setDimensio(dimensio2);
        entity2.setValor("ENT1");

        List<DimensioValorEntity> entities = Arrays.asList(entity1, entity2);
        List<DimensioValor> resources = Arrays.asList(new DimensioValor(), new DimensioValor());

        UnitatOrganitzativaEntity uo = new UnitatOrganitzativaEntity();
        uo.setId(10L);
        uo.setCodi("UO1");
        uo.setDenominacio("Unitat 1");

        EntitatEntity entitat = new EntitatEntity();
        entitat.setId(20L);
        entitat.setCodi("ENT1");
        entitat.setNom("Entitat 1");

        when(unitatOrganitzativaRepository.findByCodiIn(anyList())).thenReturn(Collections.singletonList(uo));
        when(entitatResolverHelper.resolveEntitat(dimensio2, "ENT1")).thenReturn(Optional.of(entitat));

        // Act
        dimensioValorService.afterConversion(entities, resources);

        // Assert
        assertThat(resources.get(0).getUnitatOrganitzativa()).isNotNull();
        assertThat(resources.get(0).getUnitatOrganitzativa().getId()).isEqualTo(10L);

        assertThat(resources.get(1).getEntitat()).isNotNull();
        assertThat(resources.get(1).getEntitat().getId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("UOActionExecutor executa correctament per CONSELLERIA i ORGAN_GESTOR")
    void uoActionExecutor_executaCorrectament() throws ActionExecutionException, SistemaExternException {
        // Arrange
        DimensioValorServiceImpl.UOActionExecutor executor = dimensioValorService.new UOActionExecutor();

        DimensioValorEntity entity = new DimensioValorEntity();
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setDimensio(dimensio);
        entity.setValor("UO123");

        UnitatOrganitzativaEntity uoEntity = new UnitatOrganitzativaEntity();
        uoEntity.setId(5L);
        uoEntity.setCodi("UO123");
        uoEntity.setDenominacio("Unitat Test");

        when(unitatOrganitzativaHelper.updateByCodi("UO123")).thenReturn(uoEntity);

        // Act
        Serializable result = executor.exec(DimensioValor.ACTION_UO, entity, null);

        // Assert
        verify(resourceEntityMappingHelper).entityToResource(uoEntity, UnitatOrganitzativa.class);
    }

    @Test
    @DisplayName("UOActionExecutor llança excepció per tipus de dimensió no suportat")
    void uoActionExecutor_llancaExcepcioTipusNoSuportat() {
        // Arrange
        DimensioValorServiceImpl.UOActionExecutor executor = dimensioValorService.new UOActionExecutor();

        DimensioValorEntity entity = new DimensioValorEntity();
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setTipus(TipusDimensioEnum.ENTITAT); // No suportat a UOActionExecutor
        entity.setDimensio(dimensio);

        // Act & Assert (Natiu JUnit 5: Assertions.assertThrows)
        ActionExecutionException exception = assertThrows(ActionExecutionException.class, () -> {
            executor.exec(DimensioValor.ACTION_UO, entity, null);
        });

        assertThat(exception.getMessage()).contains("Tipus de dimensió no trabada");
    }
}
