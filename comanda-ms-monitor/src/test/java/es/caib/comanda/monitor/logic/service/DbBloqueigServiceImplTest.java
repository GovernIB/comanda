package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.BloqueigDto;
import es.caib.comanda.monitor.logic.intf.model.db.DbBloqueig;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DbBloqueigServiceImpl")
class DbBloqueigServiceImplTest {

    @Mock
    private DbMetricsServiceImpl dbMetricsService;

    @InjectMocks
    private DbBloqueigServiceImpl dbBloqueigService;

    private BloqueigDto bloqueigDto1;
    private BloqueigDto bloqueigDto2;

    @BeforeEach
    void setUp() {
        bloqueigDto1 = new BloqueigDto();
        bloqueigDto1.setSid(1L);
        bloqueigDto1.setSerialNum(100L);
        bloqueigDto1.setUsername("usuari1");
        bloqueigDto1.setStatus("ACTIVE");
        bloqueigDto1.setObjectName("Taula1");
        bloqueigDto1.setObjectType("TABLE");
        bloqueigDto1.setLockMode("X");
        bloqueigDto1.setLockRequest("S");
        bloqueigDto1.setBlocking(true);

        bloqueigDto2 = new BloqueigDto();
        bloqueigDto2.setSid(2L);
        bloqueigDto2.setSerialNum(200L);
        bloqueigDto2.setUsername("usuari2");
    }

    // ========================================================================
    // 1. TESTOS PER A entityRepositoryFindOne
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindOne: retorna l'entitat quan l'ID coincideix")
    void entityRepositoryFindOne_quanIdExisteix_llavorsRetornaEntitat() {
        // Arrange
        when(dbMetricsService.getBloqueigs()).thenReturn(List.of(bloqueigDto1, bloqueigDto2));

        // Act
        Optional<NoDatabaseResourceEntity<DbBloqueig, Long>> result = dbBloqueigService.entityRepositoryFindOne(1L);

        // Assert
        assertThat(result).isPresent();
        DbBloqueig resource = result.get().getResource();
        assertThat(resource.getId()).isEqualTo(1L);
        assertThat(resource.getUsername()).isEqualTo("usuari1");
        assertThat(resource.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("entityRepositoryFindOne: retorna Optional buit quan l'ID no existeix")
    void entityRepositoryFindOne_quanIdNoExisteix_llavorsRetornaEmpty() {
        // Arrange
        when(dbMetricsService.getBloqueigs()).thenReturn(List.of(bloqueigDto1));

        // Act
        Optional<NoDatabaseResourceEntity<DbBloqueig, Long>> result = dbBloqueigService.entityRepositoryFindOne(99L);

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
        when(dbMetricsService.getBloqueigs()).thenReturn(List.of(bloqueigDto1, bloqueigDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbBloqueig, Long>> result = dbBloqueigService.entityRepositoryFindEntities(
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
        when(dbMetricsService.getBloqueigs()).thenReturn(List.of(bloqueigDto1, bloqueigDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbBloqueig, Long>> result = dbBloqueigService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResource().getId()).isEqualTo(2L);
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
        when(dbMetricsService.getBloqueigs()).thenReturn(List.of(bloqueigDto1, bloqueigDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbBloqueig, Long>> result = dbBloqueigService.entityRepositoryFindEntities(
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
        when(dbMetricsService.getBloqueigs()).thenReturn(Collections.emptyList());

        // Act
        Page<NoDatabaseResourceEntity<DbBloqueig, Long>> result = dbBloqueigService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
