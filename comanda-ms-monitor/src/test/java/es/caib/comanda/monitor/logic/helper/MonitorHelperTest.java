package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.monitor.persist.repository.MonitorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitorHelperTest {

    @Mock
    private MonitorRepository monitorRepository;

    @InjectMocks
    private MonitorHelper monitorHelper;

    @Test
    @DisplayName("buidat no fa res si la retenció és nul·la")
    void buidat_quanRetencioNull_noFaRes() {
        // Act
        monitorHelper.buidat(null);

        // Assert
        verifyNoInteractions(monitorRepository);
    }

    @Test
    @DisplayName("buidat no fa res si la retenció és negativa")
    void buidat_quanRetencioNegativa_noFaRes() {
        // Act
        monitorHelper.buidat(-1);

        // Assert
        verifyNoInteractions(monitorRepository);
    }

    @Test
    @DisplayName("buidat esborra dades en batches fins que no n'hi ha més")
    void buidat_esborraDadesEnBatches() {
        // Arrange
        when(monitorRepository.findIdsBeforeDate(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Arrays.asList(1L, 2L))
                .thenReturn(Collections.singletonList(3L))
                .thenReturn(Collections.emptyList());

        // Act
        monitorHelper.buidat(5);

        // Assert
        verify(monitorRepository, times(3)).findIdsBeforeDate(any(LocalDateTime.class), any(Pageable.class));
        verify(monitorRepository).deleteAllByIdInBatch(Arrays.asList(1L, 2L));
        verify(monitorRepository).deleteAllByIdInBatch(Collections.singletonList(3L));
        verifyNoMoreInteractions(monitorRepository);
    }

    @Test
    @DisplayName("buidat no fa res si no troba dades per esborrar")
    void buidat_quanNoHiHaDades_noEsborraRes() {
        // Arrange
        when(monitorRepository.findIdsBeforeDate(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // Act
        monitorHelper.buidat(30);

        // Assert
        verify(monitorRepository, times(1)).findIdsBeforeDate(any(LocalDateTime.class), any(Pageable.class));
        verify(monitorRepository, never()).deleteAllByIdInBatch(any());
    }
}
