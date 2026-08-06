package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.intf.model.scheduler.TascaBackground;
import es.caib.comanda.ms.logic.helper.SchedulerTaskRegistryService;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a TascaBackgroundServiceImpl")
class TascaBackgroundServiceImplTest {

    @Mock
    private SchedulerTaskRegistryService schedulerTaskRegistry;

    @InjectMocks
    private TascaBackgroundServiceImpl tascaBackgroundService;

    private SchedulerTaskRegistryService.SchedulerTaskEntry mockEntry;
    private static final String TASK_ID = "task-123";

    @BeforeEach
    void setUp() {
        // Preparació d'una entrada de tasca mockejada per a les proves
        mockEntry = mock(SchedulerTaskRegistryService.SchedulerTaskEntry.class);
        lenient().when(mockEntry.getId()).thenReturn(TASK_ID);
        lenient().when(mockEntry.getNom()).thenReturn("Tasca de Prova");
        lenient().when(mockEntry.getDescripcio()).thenReturn("Descripció de la tasca");
        lenient().when(mockEntry.getExpressioHuman()).thenReturn("Cada dia a les 10:00");
        lenient().when(mockEntry.getUltimaExecucio()).thenReturn(LocalDateTime.now().minusHours(1));
        lenient().when(mockEntry.getUltimaEstat()).thenReturn("SUCCESS");
        lenient().when(mockEntry.getUltimaDuracio()).thenReturn(1500L);
        lenient().when(mockEntry.getProxExecucio()).thenReturn(LocalDateTime.now().plusHours(23));
    }

    // ========================================================================
    // 1. TESTOS PER A getOne
    // ========================================================================

    @Test
    @DisplayName("getOne: retorna la tasca mapejada correctament quan l'ID existeix")
    void getOne_quanIdExisteix_llavorsRetornaTascaResource() {
        // Arrange
        when(schedulerTaskRegistry.getById(TASK_ID)).thenReturn(Optional.of(mockEntry));

        // Act
        TascaBackground result = tascaBackgroundService.getOne(TASK_ID, new String[0]);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(TASK_ID);
        assertThat(result.getNom()).isEqualTo("Tasca de Prova");
        assertThat(result.getDescripcio()).isEqualTo("Descripció de la tasca");
        assertThat(result.getExpressioHuman()).isEqualTo("Cada dia a les 10:00");
        assertThat(result.getUltimaEstat()).isEqualTo("SUCCESS");

        verify(schedulerTaskRegistry, times(1)).getById(TASK_ID);
    }

    @Test
    @DisplayName("getOne: llança ResourceNotFoundException quan l'ID no existeix")
    void getOne_quanIdNoExisteix_llancaResourceNotFoundException() {
        // Arrange
        when(schedulerTaskRegistry.getById(TASK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tascaBackgroundService.getOne(TASK_ID, new String[0]))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(TASK_ID);

        verify(schedulerTaskRegistry, times(1)).getById(TASK_ID);
    }

    // ========================================================================
    // 2. TESTOS PER A findPage
    // ========================================================================

    @Test
    @DisplayName("findPage: retorna una pàgina amb totes les tasques mapejades")
    void findPage_quanHiHaTasques_llavorsRetornaPaginaCorrecta() {
        // Arrange
        SchedulerTaskRegistryService.SchedulerTaskEntry entry2 = mock(SchedulerTaskRegistryService.SchedulerTaskEntry.class);
        when(entry2.getId()).thenReturn("task-456");

        List<SchedulerTaskRegistryService.SchedulerTaskEntry> entries = List.of(mockEntry, entry2);
        when(schedulerTaskRegistry.getAll()).thenReturn(entries);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<TascaBackground> result = tascaBackgroundService.findPage(
            "filtre", "filter", new String[0], new String[0], pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(TASK_ID);
        assertThat(result.getContent().get(1).getId()).isEqualTo("task-456");
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(schedulerTaskRegistry, times(1)).getAll();
    }

    @Test
    @DisplayName("findPage: retorna una pàgina buida quan no hi ha tasques registrades")
    void findPage_quanNoHiHaTasques_llavorsRetornaPaginaBuida() {
        // Arrange
        when(schedulerTaskRegistry.getAll()).thenReturn(Collections.emptyList());
        Pageable pageable = PageRequest.of(0, 10);

        // Act
        Page<TascaBackground> result = tascaBackgroundService.findPage(
            null, null, null, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(schedulerTaskRegistry, times(1)).getAll();
    }

    // ========================================================================
    // 3. TESTOS PER A delete
    // ========================================================================

    @Test
    @DisplayName("delete: crida a trigger correctament quan la tasca existeix")
    void delete_quanTascaExisteix_llavorsCridaTrigger() {
        // Arrange
        when(schedulerTaskRegistry.getById(TASK_ID)).thenReturn(Optional.of(mockEntry));

        // Act
        tascaBackgroundService.delete(TASK_ID, new HashMap<>());

        // Assert
        verify(schedulerTaskRegistry, times(1)).getById(TASK_ID);
        verify(schedulerTaskRegistry, times(1)).trigger(TASK_ID);
    }

    @Test
    @DisplayName("delete: llança ResourceNotFoundException i no crida trigger quan la tasca no existeix")
    void delete_quanTascaNoExisteix_llancaExcepcioINoCridaTrigger() {
        // Arrange
        when(schedulerTaskRegistry.getById(TASK_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tascaBackgroundService.delete(TASK_ID, new HashMap<>()))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining(TASK_ID);

        verify(schedulerTaskRegistry, times(1)).getById(TASK_ID);
        verify(schedulerTaskRegistry, never()).trigger(anyString());
    }
}
