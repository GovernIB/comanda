package es.caib.comanda.monitor.logic.service;

import es.caib.comanda.monitor.logic.intf.model.db.DbTaulaActivitat;
import es.caib.comanda.monitor.logic.intf.model.db.TaulaActivitatDto;
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
@DisplayName("Tests per a DbTaulaActivitatServiceImpl")
class DbTaulaActivitatServiceImplTest {

    @Mock
    private DbMetricsServiceImpl dbMetricsService;

    @InjectMocks
    private DbTaulaActivitatServiceImpl dbTaulaActivitatService;

    private TaulaActivitatDto activitatDto1;
    private TaulaActivitatDto activitatDto2;

    @BeforeEach
    void setUp() {
        activitatDto1 = new TaulaActivitatDto();
        activitatDto1.setTaula("TAULA_A");
        activitatDto1.setLecturesFisiques(100L);
        activitatDto1.setLecturesLogiques(500L);
        activitatDto1.setEsperesBuffer(10L);
        activitatDto1.setEsperesFila(5L);

        activitatDto2 = new TaulaActivitatDto();
        activitatDto2.setTaula("TAULA_B");
        activitatDto2.setLecturesFisiques(200L);
        activitatDto2.setLecturesLogiques(1000L);
        activitatDto2.setEsperesBuffer(20L);
        activitatDto2.setEsperesFila(15L);
    }

    // ========================================================================
    // 1. TESTOS PER A entityRepositoryFindOne
    // ========================================================================

    @Test
    @DisplayName("entityRepositoryFindOne: retorna l'entitat quan l'ID coincideix")
    void entityRepositoryFindOne_quanIdCoincideix_llavorsRetornaEntitat() {
        // Arrange
        when(dbMetricsService.getActivitat()).thenReturn(List.of(activitatDto1, activitatDto2));

        // Act
        Optional<NoDatabaseResourceEntity<DbTaulaActivitat, String>> result =
            dbTaulaActivitatService.entityRepositoryFindOne("TAULA_A");

        // Assert
        assertThat(result).isPresent();
        DbTaulaActivitat resource = result.get().getResource();
        assertThat(resource.getId()).isEqualTo("TAULA_A");
        assertThat(resource.getLecturesFisiques()).isEqualTo(100L);
        assertThat(resource.getLecturesLogiques()).isEqualTo(500L);
        assertThat(resource.getEsperesBuffer()).isEqualTo(10L);
        assertThat(resource.getEsperesFila()).isEqualTo(5L);
        verify(dbMetricsService, times(1)).getActivitat();
    }

    @Test
    @DisplayName("entityRepositoryFindOne: retorna Optional buit quan l'ID no existeix")
    void entityRepositoryFindOne_quanIdNoExisteix_llavorsRetornaEmpty() {
        // Arrange
        when(dbMetricsService.getActivitat()).thenReturn(List.of(activitatDto1));

        // Act
        Optional<NoDatabaseResourceEntity<DbTaulaActivitat, String>> result =
            dbTaulaActivitatService.entityRepositoryFindOne("NO_EXISTEIX");

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
        when(dbMetricsService.getActivitat()).thenReturn(List.of(activitatDto1, activitatDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaActivitat, String>> result =
            dbTaulaActivitatService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

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
        when(dbMetricsService.getActivitat()).thenReturn(List.of(activitatDto1, activitatDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaActivitat, String>> result =
            dbTaulaActivitatService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getResource().getId()).isEqualTo("TAULA_B");
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
        when(dbMetricsService.getActivitat()).thenReturn(List.of(activitatDto1, activitatDto2));

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaActivitat, String>> result =
            dbTaulaActivitatService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

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
        when(dbMetricsService.getActivitat()).thenReturn(Collections.emptyList());

        // Act
        Page<NoDatabaseResourceEntity<DbTaulaActivitat, String>> result =
            dbTaulaActivitatService.entityRepositoryFindEntities("filtre", "filter", new String[0], pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
