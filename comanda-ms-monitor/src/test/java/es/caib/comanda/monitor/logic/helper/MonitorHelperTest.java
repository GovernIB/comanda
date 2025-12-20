package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.monitor.persist.repository.MonitorRepository;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setup() {
        // de moment, res
    }

    @Test
    void buidat_invalidRetention_doesNothing_whenNull() {
        monitorHelper.buidat(null);
        verifyNoInteractions(monitorRepository);
    }

    @Test
    void buidat_invalidRetention_doesNothing_whenNegative() {
        monitorHelper.buidat(-1);
        verifyNoInteractions(monitorRepository);
    }

    @Test
    void buidat_batches_and_deletes_until_empty() {
        when(monitorRepository.findIdsBeforeDate(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(Arrays.asList(1L, 2L))
                .thenReturn(Collections.singletonList(3L))
                .thenReturn(Collections.emptyList());

        monitorHelper.buidat(5);

        verify(monitorRepository, times(3)).findIdsBeforeDate(any(LocalDateTime.class), any(Pageable.class));
        verify(monitorRepository).deleteAllByIdInBatch(Arrays.asList(1L, 2L));
        verify(monitorRepository).deleteAllByIdInBatch(Collections.singletonList(3L));
        verifyNoMoreInteractions(monitorRepository);
    }
}
