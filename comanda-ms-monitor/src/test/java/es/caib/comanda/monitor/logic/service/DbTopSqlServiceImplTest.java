package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTopSql;
import es.caib.comanda.monitor.logic.intf.model.db.TopSqlDto;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DbTopSqlServiceImpl")
class DbTopSqlServiceImplTest {

    @Mock
    private DbMetricsServiceImpl dbMetricsService;

    @InjectMocks
    private DbTopSqlServiceImpl dbTopSqlService;

    private TopSqlDto topSqlDto1;
    private TopSqlDto topSqlDto2;

    @BeforeEach
    void setUp() {
        topSqlDto1 = new TopSqlDto();
        topSqlDto1.setSqlId("SQL_001");
        topSqlDto1.setTempsTotalS(120.5);
        topSqlDto1.setExecucions(1000L);
        topSqlDto1.setMsPerExec(120.5);
        topSqlDto1.setBuffersPerExec(500L);
        topSqlDto1.setSqlText("SELECT * FROM COM_TAULA WHERE ID = :id");

        topSqlDto2 = new TopSqlDto();
        topSqlDto2.setSqlId("SQL_002");
        topSqlDto2.setTempsTotalS(45.2);
        topSqlDto2.setExecucions(500L);
        topSqlDto2.setMsPerExec(90.4);
        topSqlDto2.setBuffersPerExec(250L);
        topSqlDto2.setSqlText("UPDATE COM_TAULA SET NOM = :nom WHERE ID = :id");
    }

    // ========================================================================
    // 1. TESTOS PER A entityRepositoryFindOne
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindOne: retorna l'entitat quan l'ID coincideix")
    void entityRepositoryFindOne_quanIdCoincideix_llavorsRetornaEntitat() {
        // Arrange
        when(dbMetricsService.getTopSql()).thenReturn(List.of(topSqlDto1, topSqlDto2));

        // Act
        Optional<NoDatabaseResourceEntity<DbTopSql, String>> result =
            dbTopSqlService.entityRepositoryFindOne("SQL_001");

        // Assert
        assertThat(result).isPresent();
        DbTopSql resource = result.get().getResource();
        assertThat(resource.getId()).isEqualTo("SQL_001");
        assertThat(resource.getTempsTotalS()).isEqualTo(120.5);
        assertThat(resource.getExecucions()).isEqualTo(1000L);
        assertThat(resource.getMsPerExec()).isEqualTo(120.5);
        assertThat(resource.getBuffersPerExec()).isEqualTo(500L);
        assertThat(resource.getSqlText()).isEqualTo("SELECT * FROM COM_TAULA WHERE ID = :id");
        verify(dbMetricsService, times(1)).getTopSql();
    }

    @Test
    @DisplayName("entityRepositoryFindOne: retorna Optional buit quan l'ID no existeix")
    void entityRepositoryFindOne_quanIdNoExisteix_llavorsRetornaEmpty() {
        // Arrange
        when(dbMetricsService.getTopSql()).thenReturn(List.of(topSqlDto1));

        // Act
        Optional<NoDatabaseResourceEntity<DbTopSql, String>> result =
            dbTopSqlService.entityRepositoryFindOne("SQL_NO_EXISTEIX");

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
        when(dbMetricsService.getTopSql()).thenReturn(List.of(topSqlDto1, topSqlDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTopSql, String>> result =
            dbTopSqlService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

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
        when(dbMetricsService.getTopSql()).thenReturn(List.of(topSqlDto1, topSqlDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTopSql, String>> result =
            dbTopSqlService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResource().getId()).isEqualTo("SQL_002");
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
        when(dbMetricsService.getTopSql()).thenReturn(List.of(topSqlDto1, topSqlDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTopSql, String>> result =
            dbTopSqlService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

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
        when(dbMetricsService.getTopSql()).thenReturn(Collections.emptyList());

        // Act
        Page<NoDatabaseResourceEntity<DbTopSql, String>> result =
            dbTopSqlService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
