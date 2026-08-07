package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbIndex;
import es.caib.comanda.monitor.logic.intf.model.db.IndexDto;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.persist.entity.NoDatabaseResourceEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DbIndexServiceImpl")
class DbIndexServiceImplTest {

    @Mock
    private DbMetricsServiceImpl dbMetricsService;

    @InjectMocks
    private DbIndexServiceImpl dbIndexService;

    private IndexDto indexDto1;
    private IndexDto indexDto2;

    @BeforeEach
    void setUp() {
        indexDto1 = new IndexDto();
        indexDto1.setIndexName("IDX_TEST_1");
        indexDto1.setTableName("TAULA_TEST");
        indexDto1.setStatus("VALID");
        indexDto1.setUniqueness("NONUNIQUE");
        indexDto1.setNumRows(1000L);
        indexDto1.setBlevel(2);
        indexDto1.setLeafBlocks(50);

        indexDto2 = new IndexDto();
        indexDto2.setIndexName("idx_test_2"); // Minúscules per provar case-insensitive
        indexDto2.setTableName("TAULA_TEST_2");
    }

    // ========================================================================
    // 1. TESTOS PER A entityRepositoryFindOne
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindOne: retorna l'entitat quan l'ID coincideix (case-insensitive)")
    void entityRepositoryFindOne_quanIdCoincideix_llavorsRetornaEntitat() {
        // Arrange
        when(dbMetricsService.getIndexos()).thenReturn(List.of(indexDto1, indexDto2));

        // Act: Busquem amb minúscules, el DTO té majúscules
        Optional<NoDatabaseResourceEntity<DbIndex, String>> result = dbIndexService.entityRepositoryFindOne("idx_test_1");

        // Assert
        assertThat(result).isPresent();
        DbIndex resource = result.get().getResource();
        assertThat(resource.getId()).isEqualTo("IDX_TEST_1");
        assertThat(resource.getTableName()).isEqualTo("TAULA_TEST");
        assertThat(resource.getNumRows()).isEqualTo(1000L);
        verify(dbMetricsService, times(1)).getIndexos();
    }

    @Test
    @DisplayName("entityRepositoryFindOne: retorna Optional buit quan l'ID no existeix")
    void entityRepositoryFindOne_quanIdNoExisteix_llavorsRetornaEmpty() {
        // Arrange
        when(dbMetricsService.getIndexos()).thenReturn(List.of(indexDto1));

        // Act
        Optional<NoDatabaseResourceEntity<DbIndex, String>> result = dbIndexService.entityRepositoryFindOne("NO_EXISTEIX");

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 2. TESTOS PER A entityRepositoryFindEntities (Lògica de Paginació)
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindEntities: retorna tots els elements quan pageable és unpaged")
    void entityRepositoryFindEntities_quanPageableEsUnpaged_llavorsRetornaTots() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pageable.isUnpaged()).thenReturn(true);
        when(dbMetricsService.getIndexos()).thenReturn(List.of(indexDto1, indexDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbIndex, String>> result = dbIndexService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("entityRepositoryFindEntities: retorna la subllista correcta quan pageable és paged i dins del rang")
    void entityRepositoryFindEntities_quanPageableEsPagedIValid_llavorsRetornaSubllista() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pageable.isUnpaged()).thenReturn(false);
        when(pageable.getOffset()).thenReturn(1L); // Comença al segon element (índex 1)
        when(pageable.getPageSize()).thenReturn(1);
        when(dbMetricsService.getIndexos()).thenReturn(List.of(indexDto1, indexDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbIndex, String>> result = dbIndexService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResource().getId()).isEqualTo("idx_test_2");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("entityRepositoryFindEntities: retorna llista buida quan l'offset supera la mida total")
    void entityRepositoryFindEntities_quanPageableEsPagedIForaDeRang_llavorsRetornaLlistaBuida() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pageable.isUnpaged()).thenReturn(false);
        when(pageable.getOffset()).thenReturn(5L); // Fora de rang (només hi ha 2 elements)
        when(pageable.getPageSize()).thenReturn(10);
        when(dbMetricsService.getIndexos()).thenReturn(List.of(indexDto1, indexDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbIndex, String>> result = dbIndexService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(2); // El total global es manté
    }

    @Test
    @DisplayName("entityRepositoryFindEntities: gestiona correctament quan la llista original és buida")
    void entityRepositoryFindEntities_quanLlistaOriginalEsBuida_llavorsRetornaPaginaBuida() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        when(pageable.isUnpaged()).thenReturn(false);
        when(pageable.getOffset()).thenReturn(0L);
        when(pageable.getPageSize()).thenReturn(10);
        when(dbMetricsService.getIndexos()).thenReturn(Collections.emptyList());

        // Act
        Page<NoDatabaseResourceEntity<DbIndex, String>> result = dbIndexService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ========================================================================
    // 3. TESTOS PER A RebuildActionExecutor
    // ========================================================================

    @Test
    @DisplayName("RebuildActionExecutor.exec: reconstrueix l'índex correctament i retorna l'ID")
    void rebuildActionExecutor_exec_quanEsValid_llavorsReconstrueixIRetornaId() throws ActionExecutionException {
        // Arrange
        DbIndexServiceImpl.RebuildActionExecutor executor = dbIndexService.new RebuildActionExecutor();
        NoDatabaseResourceEntity<DbIndex, String> entity = NoDatabaseResourceEntity.<DbIndex, String>builder()
            .id("IDX_TEST_1")
            .resource(new DbIndex())
            .build();

        // Act
        String result = executor.exec("REBUILD", entity, null);

        // Assert
        assertThat(result).isEqualTo("IDX_TEST_1");
        verify(dbMetricsService, times(1)).rebuildIndex("IDX_TEST_1");
    }

    @Test
    @DisplayName("RebuildActionExecutor.exec: captura IllegalArgumentException i llança ActionExecutionException")
    void rebuildActionExecutor_exec_quanFallaRebuild_llancaActionExecutionException() throws IllegalArgumentException {
        // Arrange
        DbIndexServiceImpl.RebuildActionExecutor executor = dbIndexService.new RebuildActionExecutor();
        NoDatabaseResourceEntity<DbIndex, String> entity = NoDatabaseResourceEntity.<DbIndex, String>builder()
            .id("IDX_INVALID")
            .resource(new DbIndex())
            .build();

        doThrow(new IllegalArgumentException("Índex no trobat")).when(dbMetricsService).rebuildIndex("IDX_INVALID");
        // Act & Assert
        assertThatThrownBy(() -> executor.exec("REBUILD", entity, null))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("Índex no trobat")
            .hasCauseInstanceOf(IllegalArgumentException.class);

        verify(dbMetricsService, times(1)).rebuildIndex("IDX_INVALID");
    }

    @Test
    @DisplayName("RebuildActionExecutor.onChange: s'executa sense errors (mètode buit)")
    void rebuildActionExecutor_onChange_quanEsCrida_llavorsNoFaRes() {
        // Arrange
        DbIndexServiceImpl.RebuildActionExecutor executor = dbIndexService.new RebuildActionExecutor();

        // Act & Assert
        assertDoesNotThrow(() ->
            executor.onChange(1L, null, "fieldName", "fieldValue", Collections.emptyMap(), new String[0], null)
        );
    }
}
