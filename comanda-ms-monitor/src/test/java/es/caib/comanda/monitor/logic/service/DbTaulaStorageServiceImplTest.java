package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTaulaStorage;
import es.caib.comanda.monitor.logic.intf.model.db.TaulaStorageDto;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DbTaulaStorageServiceImpl")
class DbTaulaStorageServiceImplTest {

    @Mock
    private DbMetricsServiceImpl dbMetricsService;

    @InjectMocks
    private DbTaulaStorageServiceImpl dbTaulaStorageService;

    private TaulaStorageDto storageDto1;
    private TaulaStorageDto storageDto2;

    @BeforeEach
    void setUp() {
        storageDto1 = new TaulaStorageDto();
        storageDto1.setTaula("TAULA_1");
        storageDto1.setNumFiles(5);
        storageDto1.setBytesReservats(1000000L);
        storageDto1.setBytesEstimats(800000L);
        storageDto1.setUltimaAnalisi(new Date(2023, 10, 25, 10, 0));

        storageDto2 = new TaulaStorageDto();
        storageDto2.setTaula("TAULA_2");
        storageDto2.setNumFiles(3);
        storageDto2.setBytesReservats(2000000L);
        storageDto2.setBytesEstimats(1500000L);
        storageDto2.setUltimaAnalisi(new Date(2023, 10, 24, 8, 30));
    }

    // ========================================================================
    // 1. TESTOS PER A entityRepositoryFindOne
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindOne: retorna l'entitat quan l'ID coincideix")
    void entityRepositoryFindOne_quanIdCoincideix_llavorsRetornaEntitat() {
        // Arrange
        when(dbMetricsService.getStorage()).thenReturn(List.of(storageDto1, storageDto2));

        // Act
        Optional<NoDatabaseResourceEntity<DbTaulaStorage, String>> result =
            dbTaulaStorageService.entityRepositoryFindOne("TAULA_1");

        // Assert
        assertThat(result).isPresent();
        DbTaulaStorage resource = result.get().getResource();
        assertThat(resource.getId()).isEqualTo("TAULA_1");
        assertThat(resource.getNumFiles()).isEqualTo(5);
        assertThat(resource.getBytesReservats()).isEqualTo(1000000L);
        assertThat(resource.getBytesEstimats()).isEqualTo(800000L);
        assertThat(resource.getUltimaAnalisi()).isEqualTo(new Date(2023, 10, 25, 10, 0));
        verify(dbMetricsService, times(1)).getStorage();
    }

    @Test
    @DisplayName("entityRepositoryFindOne: retorna Optional buit quan l'ID no existeix")
    void entityRepositoryFindOne_quanIdNoExisteix_llavorsRetornaEmpty() {
        // Arrange
        when(dbMetricsService.getStorage()).thenReturn(List.of(storageDto1));

        // Act
        Optional<NoDatabaseResourceEntity<DbTaulaStorage, String>> result =
            dbTaulaStorageService.entityRepositoryFindOne("NO_EXISTEIX");

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
        when(dbMetricsService.getStorage()).thenReturn(List.of(storageDto1, storageDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaStorage, String>> result =
            dbTaulaStorageService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

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
        when(dbMetricsService.getStorage()).thenReturn(List.of(storageDto1, storageDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaStorage, String>> result =
            dbTaulaStorageService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResource().getId()).isEqualTo("TAULA_2");
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
        when(dbMetricsService.getStorage()).thenReturn(List.of(storageDto1, storageDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaStorage, String>> result =
            dbTaulaStorageService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

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
        when(dbMetricsService.getStorage()).thenReturn(Collections.emptyList());

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaStorage, String>> result =
            dbTaulaStorageService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
