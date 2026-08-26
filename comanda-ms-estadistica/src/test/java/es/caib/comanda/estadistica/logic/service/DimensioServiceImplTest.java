package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EntitatResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Dimensio;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.EntitatValorTipus;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.TipusDimensioEnum;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.FetEntity;
import es.caib.comanda.estadistica.persist.repository.DimensioRepository;
import es.caib.comanda.estadistica.persist.repository.DimensioValorRepository;
import es.caib.comanda.estadistica.persist.repository.FetRepository;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DimensioServiceImpl")
class DimensioServiceImplTest {

    @Mock private SpringFilterHelper springFilterHelper;
    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private FetRepository fetRepository;
    @Mock private EntitatResolverHelper entitatResolverHelper;
    @Mock private DimensioRepository dimensioRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @InjectMocks
    private DimensioServiceImpl dimensioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dimensioService, "resourceEntityMappingHelper", resourceEntityMappingHelper);
        // Inicialitzem el valor injectat per @Value per a les proves
        ReflectionTestUtils.setField(dimensioService, "codiArrel", "ARREL_TEST");
    }

    // ========================================================================
    // 1. TESTOS PER A namedFilterToSpecification
    // ========================================================================

    @Test
    @DisplayName("namedFilterToSpecification: retorna null quan el nom del filtre és null")
    void namedFilterToSpecification_quanNomEsNull_llavorsRetornaNull() {
        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(null);
        // Assert
        assertThat(spec).isNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification: retorna null per a filtres desconeguts")
    void namedFilterToSpecification_quanFiltreDesconegut_llavorsRetornaNull() {
        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification("desconegut");
        // Assert
        assertThat(spec).isNull();
    }

    @Test
    @DisplayName("namedFilterToSpecification: gestiona el filtre per app correctament")
    void namedFilterToSpecification_quanFiltreApp_llavorsRetornaSpecification() {
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
    @DisplayName("namedFilterToSpecification: retorna especificació disjuntiva quan no hi ha entorns")
    void namedFilterToSpecification_quanSenseEntorns_llavorsRetornaDisjunction() {
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
    @DisplayName("namedFilterToSpecification: gestiona filtre sense app id")
    void namedFilterToSpecification_quanSenseAppId_llavorsRetornaDisjunction() {
        // Arrange
        String filterName = Dimensio.NAMED_FILTER_BY_APP_GROUP_BY_NOM + ":";

        // Act
        Specification<DimensioEntity> spec = dimensioService.namedFilterToSpecification(filterName);

        // Assert
        assertThat(spec).isNotNull();
        verifyNoInteractions(estadisticaClientHelper);
    }

    // ========================================================================
    // 2. TESTOS PER A additionalSpringFilter
    // ========================================================================

    @Test
    @DisplayName("additionalSpringFilter: afegeix filtres d'aplicació si s'indiquen a namedQueries")
    void additionalSpringFilter_quanNamedQueriesAmbApp_llavorsAfegeixFiltre() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna null quan no hi ha filtres")
    void additionalSpringFilter_quanSenseFiltres_llavorsRetornaNull() {
        // Act
        String result = dimensioService.additionalSpringFilter("", new String[0]);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("additionalSpringFilter: combina filtre actual amb namedQueries")
    void additionalSpringFilter_quanFiltreINamedQueries_llavorsCombinaFiltres() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"filterByApp:100"};

        // Act
        String result = dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        assertThat(result).isNotNull();
        verify(springFilterHelper).filterByApp(eq(100L), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter: gestiona correctament quan namedQueries és null")
    void additionalSpringFilter_quanNamedQueriesEsNull_llavorsNoFalla() {
        // Act
        String result = dimensioService.additionalSpringFilter("codi:'TEST'", null);

        // Assert
        assertThat(result).isEqualTo("codi : 'TEST'");
        verify(springFilterHelper, never()).filterByApp(anyLong(), anyString());
    }

    @Test
    @DisplayName("additionalSpringFilter: ignora namedQueries que no contenen el filtre d'app")
    void additionalSpringFilter_quanNamedQueryNoConteFiltreApp_llavorsIgnora() {
        // Arrange
        String currentFilter = "codi:'TEST'";
        String[] namedQueries = {"altreFiltre:100"};

        // Act
        String result = dimensioService.additionalSpringFilter(currentFilter, namedQueries);

        // Assert
        assertThat(result).isEqualTo("codi : 'TEST'");
        verify(springFilterHelper, never()).filterByApp(anyLong(), anyString());
    }

    // ========================================================================
    // 3. TESTOS PER A ChangeTipusActionExecutor
    // ========================================================================

    @Test
    @DisplayName("ChangeTipusActionExecutor: canvia el tipus correctament i retorna el recurs")
    void changeTipusActionExecutor_quanEsValid_llavorsCanviaTipusIRetornaRecurs() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);

        Dimensio.ChangeTipusActionForm params = new Dimensio.ChangeTipusActionForm();
        params.setTipus(TipusDimensioEnum.CONSELLERIA);

        Dimensio expectedResource = new Dimensio();
        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(expectedResource);

        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act
        Dimensio result = executor.exec(Dimensio.ACTION_CHANGE_TIPUS, entity, params);

        // Assert
        assertThat(entity.getTipus()).isEqualTo(TipusDimensioEnum.CONSELLERIA);
        assertThat(result).isSameAs(expectedResource);
        verify(resourceEntityMappingHelper).entityToResource(entity, Dimensio.class);
    }

    @Test
    @DisplayName("ChangeTipusActionExecutor: llança ActionExecutionException quan es produeix un error inesperat")
    void changeTipusActionExecutor_quanEsProdueixError_llancaActionExecutionException() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        Dimensio.ChangeTipusActionForm params = new Dimensio.ChangeTipusActionForm();

        when(resourceEntityMappingHelper.entityToResource(any(), any())).thenThrow(new RuntimeException("Error inesperat"));

        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Dimensio.ACTION_CHANGE_TIPUS, entity, params))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("Error inesperat");
    }

    @Test
    @DisplayName("ChangeTipusActionExecutor: persisteix entitatValorTipus quan el tipus és ENTITAT")
    void changeTipusActionExecutor_quanTipusEntitat_llavorsPersisteixEntitatValorTipus() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();

        Dimensio.ChangeTipusActionForm params = new Dimensio.ChangeTipusActionForm();
        params.setTipus(TipusDimensioEnum.ENTITAT);
        params.setEntitatValorTipus(EntitatValorTipus.CODI_DIR3);

        when(dimensioValorRepository.findByDimensio(entity)).thenReturn(Collections.emptyList());
        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(new Dimensio());

        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act
        executor.exec(Dimensio.ACTION_CHANGE_TIPUS, entity, params);

        // Assert
        assertThat(entity.getEntitatValorTipus()).isEqualTo(EntitatValorTipus.CODI_DIR3);
    }

    @Test
    @DisplayName("ChangeTipusActionExecutor: en configurar/editar el tipus ENTITAT, enllaça els valors existents amb les Entitat")
    void changeTipusActionExecutor_quanTipusEntitat_llavorsEnllacaValorsExistents() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setCodi("ENT");

        Dimensio.ChangeTipusActionForm params = new Dimensio.ChangeTipusActionForm();
        params.setTipus(TipusDimensioEnum.ENTITAT);
        params.setEntitatValorTipus(EntitatValorTipus.CODI_DIR3);

        DimensioValorEntity v1 = new DimensioValorEntity();
        v1.setValor("A01");
        DimensioValorEntity v2 = new DimensioValorEntity();
        v2.setValor("A02");

        when(dimensioValorRepository.findByDimensio(entity)).thenReturn(Arrays.asList(v1, v2));
        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(new Dimensio());

        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act
        executor.exec(Dimensio.ACTION_CHANGE_TIPUS, entity, params);

        // Assert
        verify(entitatResolverHelper).resolveOrCreateEntitat(entity, "A01");
        verify(entitatResolverHelper).resolveOrCreateEntitat(entity, "A02");
    }

    @Test
    @DisplayName("ChangeTipusActionExecutor: no intenta enllaçar valors quan el tipus no és ENTITAT")
    void changeTipusActionExecutor_quanTipusNoEsEntitat_llavorsNoEnllacaValors() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();

        Dimensio.ChangeTipusActionForm params = new Dimensio.ChangeTipusActionForm();
        params.setTipus(TipusDimensioEnum.ORGAN_GESTOR);

        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(new Dimensio());

        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act
        executor.exec(Dimensio.ACTION_CHANGE_TIPUS, entity, params);

        // Assert
        verifyNoInteractions(dimensioValorRepository, entitatResolverHelper);
    }

    @Test
    @DisplayName("ChangeTipusActionExecutor: ignora entitatValorTipus quan el tipus no és ENTITAT")
    void changeTipusActionExecutor_quanTipusNoEsEntitat_llavorsIgnoraEntitatValorTipus() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setEntitatValorTipus(EntitatValorTipus.NOM);

        Dimensio.ChangeTipusActionForm params = new Dimensio.ChangeTipusActionForm();
        params.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        params.setEntitatValorTipus(EntitatValorTipus.CODI_DIR3);

        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(new Dimensio());

        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act
        executor.exec(Dimensio.ACTION_CHANGE_TIPUS, entity, params);

        // Assert
        assertThat(entity.getEntitatValorTipus()).isNull();
    }

    @Test
    @DisplayName("ChangeTipusActionExecutor.onChange: s'executa sense errors")
    void changeTipusActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        DimensioServiceImpl.ChangeTipusActionExecutor executor = dimensioService.new ChangeTipusActionExecutor();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            executor.onChange(1L, null, "field", "value", new HashMap<>(), new String[0], new Dimensio.ChangeTipusActionForm())
        );
    }

    // ========================================================================
    // 4. TESTOS PER A FetConsActionExecutor
    // ========================================================================

    @Test
    @DisplayName("FetConsActionExecutor: crea dimensió CONS si no existeix i és ORGAN_GESTOR")
    void fetConsActionExecutor_quanEsOrganGestorSenseCons_llavorsCreaDimensioCons() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Collections.emptyList());
        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", "ARREL_TEST")).thenReturn(Collections.emptyList());

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
    @DisplayName("FetConsActionExecutor: no crea dimensió CONS si ja existeix")
    void fetConsActionExecutor_quanEsOrganGestorAmbCons_llavorsNoCreaDimensio() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        DimensioEntity existingCons = new DimensioEntity();
        existingCons.setTipus(TipusDimensioEnum.CONSELLERIA);
        existingCons.setCodi("CONS");

        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Arrays.asList(existingCons));
        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", "ARREL_TEST")).thenReturn(Collections.emptyList());

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        verify(dimensioRepository, never()).save(any());
    }

    @Test
    @DisplayName("FetConsActionExecutor: actualitza CONS quan hi ha conselleria")
    void fetConsActionExecutor_quanHiHaConselleria_llavorsActualitzaJson() {
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

        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", "ARREL_TEST")).thenReturn(Arrays.asList(fet));
        when(entitatResolverHelper.resolveConselleria(eq(1L), eq("ORG123"), any())).thenReturn("CONS456");

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        assertThat(fet.getDimensionsJson()).containsEntry("CONS", "CONS456");
        verify(fetRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("FetConsActionExecutor: elimina CONS quan no hi ha conselleria")
    void fetConsActionExecutor_quanNoHiHaConselleria_llavorsEliminaCons() {
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

        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", "ARREL_TEST")).thenReturn(Arrays.asList(fet));
        when(entitatResolverHelper.resolveConselleria(eq(1L), eq("ORG123"), any())).thenReturn(null);

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        assertThat(fet.getDimensionsJson()).doesNotContainKey("CONS");
        verify(fetRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("FetConsActionExecutor: no fa res quan no és ORGAN_GESTOR")
    void fetConsActionExecutor_quanNoEsOrganGestor_llavorsNoFaRes() {
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
    @DisplayName("FetConsActionExecutor: no guarda quan no hi ha canvis")
    void fetConsActionExecutor_quanNoHiHaCanvis_llavorsNoGuarda() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST_ORGAN");

        DimensioEntity existingCons = new DimensioEntity();
        existingCons.setTipus(TipusDimensioEnum.CONSELLERIA);
        when(dimensioRepository.findByEntornAppId(1L)).thenReturn(Arrays.asList(existingCons));
        when(fetRepository.findByEntornAppIdAddCons(1L, "TEST_ORGAN", "ARREL_TEST")).thenReturn(Collections.emptyList());

        // Act
        Dimensio result = dimensioService.new FetConsActionExecutor().exec("FET_CONS", entity, null);

        // Assert
        verify(fetRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("FetConsActionExecutor: llança ActionExecutionException quan es produeix un error inesperat")
    void fetConsActionExecutor_quanEsProdueixError_llancaActionExecutionException() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);
        entity.setEntornAppId(1L);
        entity.setCodi("TEST");

        when(dimensioRepository.findByEntornAppId(1L)).thenThrow(new RuntimeException("Error de BD"));

        DimensioServiceImpl.FetConsActionExecutor executor = dimensioService.new FetConsActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec("FET_CONS", entity, null))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("Error de BD");
    }

    @Test
    @DisplayName("FetConsActionExecutor.onChange: s'executa sense errors")
    void fetConsActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        DimensioServiceImpl.FetConsActionExecutor executor = dimensioService.new FetConsActionExecutor();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            executor.onChange(1L, null, "field", "value", new HashMap<>(), new String[0], null)
        );
    }

    // ========================================================================
    // 5. TESTOS PER A UpdateEntitatsActionExecutor
    // ========================================================================

    @Test
    @DisplayName("UpdateEntitatsActionExecutor: llança ActionExecutionException quan la dimensió no és ENTITAT")
    void updateEntitatsActionExecutor_quanNoEsEntitat_llancaActionExecutionException() {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ORGAN_GESTOR);

        DimensioServiceImpl.UpdateEntitatsActionExecutor executor = dimensioService.new UpdateEntitatsActionExecutor();

        // Act & Assert
        assertThatThrownBy(() -> executor.exec(Dimensio.ACTION_UPDATE_ENTITATS, entity, null))
            .isInstanceOf(ActionExecutionException.class);
        verifyNoInteractions(dimensioValorRepository, entitatResolverHelper);
    }

    @Test
    @DisplayName("UpdateEntitatsActionExecutor: crida resolveOrCreateEntitat per cada valor no blanc")
    void updateEntitatsActionExecutor_quanEsEntitat_llavorsResolCadaValorNoBlanc() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ENTITAT);
        entity.setCodi("ENT");

        DimensioValorEntity v1 = new DimensioValorEntity();
        v1.setValor("A01");
        DimensioValorEntity v2 = new DimensioValorEntity();
        v2.setValor(""); // blanc, s'ha d'ignorar
        DimensioValorEntity v3 = new DimensioValorEntity();
        v3.setValor(null); // null, s'ha d'ignorar
        DimensioValorEntity v4 = new DimensioValorEntity();
        v4.setValor("A02");

        when(dimensioValorRepository.findByDimensio(entity)).thenReturn(Arrays.asList(v1, v2, v3, v4));
        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(new Dimensio());

        DimensioServiceImpl.UpdateEntitatsActionExecutor executor = dimensioService.new UpdateEntitatsActionExecutor();

        // Act
        Dimensio result = executor.exec(Dimensio.ACTION_UPDATE_ENTITATS, entity, null);

        // Assert
        assertThat(result).isNotNull();
        verify(entitatResolverHelper).resolveOrCreateEntitat(entity, "A01");
        verify(entitatResolverHelper).resolveOrCreateEntitat(entity, "A02");
        verify(entitatResolverHelper, org.mockito.Mockito.times(2)).resolveOrCreateEntitat(any(), anyString());
    }

    @Test
    @DisplayName("UpdateEntitatsActionExecutor: continua amb els altres valors quan un falla")
    void updateEntitatsActionExecutor_quanUnValorFalla_llavorsContinuaAmbLaResta() throws ActionExecutionException {
        // Arrange
        DimensioEntity entity = new DimensioEntity();
        entity.setTipus(TipusDimensioEnum.ENTITAT);
        entity.setCodi("ENT");

        DimensioValorEntity v1 = new DimensioValorEntity();
        v1.setValor("A01");
        DimensioValorEntity v2 = new DimensioValorEntity();
        v2.setValor("A02");

        when(dimensioValorRepository.findByDimensio(entity)).thenReturn(Arrays.asList(v1, v2));
        when(entitatResolverHelper.resolveOrCreateEntitat(entity, "A01")).thenThrow(new RuntimeException("Error BD"));
        when(resourceEntityMappingHelper.entityToResource(entity, Dimensio.class)).thenReturn(new Dimensio());

        DimensioServiceImpl.UpdateEntitatsActionExecutor executor = dimensioService.new UpdateEntitatsActionExecutor();

        // Act
        Dimensio result = executor.exec(Dimensio.ACTION_UPDATE_ENTITATS, entity, null);

        // Assert
        assertThat(result).isNotNull();
        verify(entitatResolverHelper).resolveOrCreateEntitat(entity, "A02");
    }

    @Test
    @DisplayName("UpdateEntitatsActionExecutor.onChange: s'executa sense errors")
    void updateEntitatsActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        DimensioServiceImpl.UpdateEntitatsActionExecutor executor = dimensioService.new UpdateEntitatsActionExecutor();

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            executor.onChange(1L, null, "field", "value", new HashMap<>(), new String[0], null)
        );
    }
}
