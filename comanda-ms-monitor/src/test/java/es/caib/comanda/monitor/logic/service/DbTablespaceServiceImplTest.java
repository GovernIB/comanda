package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTablespace;
import es.caib.comanda.monitor.logic.intf.model.db.TablespaceDto;
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
@DisplayName("Tests per a DbTablespaceServiceImpl")
class DbTablespaceServiceImplTest {

    @Mock
    private DbMetricsServiceImpl dbMetricsService;

    @InjectMocks
    private DbTablespaceServiceImpl dbTablespaceService;

    private TablespaceDto tablespaceDto1;
    private TablespaceDto tablespaceDto2;

    @BeforeEach
    void setUp() {
        tablespaceDto1 = new TablespaceDto();
        tablespaceDto1.setNom("USERS");
        tablespaceDto1.setTotalMb(1000.0);
        tablespaceDto1.setMaxMb(2000.0);
        tablespaceDto1.setUsatMb(500.0);
        tablespaceDto1.setLliureMb(500.0);
        tablespaceDto1.setPctUsat(50.0);

        tablespaceDto2 = new TablespaceDto();
        tablespaceDto2.setNom("SYSTEM");
        tablespaceDto2.setTotalMb(2000.0);
    }

    // ========================================================================
    // 1. TESTOS PER A entityRepositoryFindOne
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindOne: retorna l'entitat quan l'ID coincideix")
    void entityRepositoryFindOne_quanIdCoincideix_llavorsRetornaEntitat() {
        // Arrange
        when(dbMetricsService.getTablespaces()).thenReturn(List.of(tablespaceDto1, tablespaceDto2));

        // Act
        Optional<NoDatabaseResourceEntity<DbTablespace, String>> result = dbTablespaceService.entityRepositoryFindOne("USERS");

        // Assert
        assertThat(result).isPresent();
        DbTablespace resource = result.get().getResource();
        assertThat(resource.getId()).isEqualTo("USERS");
        assertThat(resource.getTotalMb()).isEqualTo(1000.0);
        assertThat(resource.getPctUsat()).isEqualTo(50.0);
        verify(dbMetricsService, times(1)).getTablespaces();
    }

    @Test
    @DisplayName("entityRepositoryFindOne: retorna Optional buit quan l'ID no existeix")
    void entityRepositoryFindOne_quanIdNoExisteix_llavorsRetornaEmpty() {
        // Arrange
        when(dbMetricsService.getTablespaces()).thenReturn(List.of(tablespaceDto1));

        // Act
        Optional<NoDatabaseResourceEntity<DbTablespace, String>> result = dbTablespaceService.entityRepositoryFindOne("NO_EXISTEIX");

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
        when(dbMetricsService.getTablespaces()).thenReturn(List.of(tablespaceDto1, tablespaceDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTablespace, String>> result = dbTablespaceService.entityRepositoryFindEntities(
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
        when(dbMetricsService.getTablespaces()).thenReturn(List.of(tablespaceDto1, tablespaceDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTablespace, String>> result = dbTablespaceService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResource().getId()).isEqualTo("SYSTEM");
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
        when(dbMetricsService.getTablespaces()).thenReturn(List.of(tablespaceDto1, tablespaceDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTablespace, String>> result = dbTablespaceService.entityRepositoryFindEntities(
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
        when(dbMetricsService.getTablespaces()).thenReturn(Collections.emptyList());

        // Act
        Page<NoDatabaseResourceEntity<DbTablespace, String>> result = dbTablespaceService.entityRepositoryFindEntities(
            "filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
