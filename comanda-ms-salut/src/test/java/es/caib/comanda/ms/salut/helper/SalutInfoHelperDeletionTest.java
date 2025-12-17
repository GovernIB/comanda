package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.helper.SalutPurgeHelper;
import es.caib.comanda.salut.logic.intf.model.TipusRegistreSalut;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.anyLong;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperDeletionTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private SalutClientHelper salutClientHelper;
    @Mock private RestTemplate restTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MetricsHelper metricsHelper;
    @Mock private SalutPurgeHelper salutPurgeService;

    @InjectMocks private SalutInfoHelper helper;

    @BeforeEach
    void setup() {
        reset(salutRepository, salutIntegracioRepository, salutSubsistemaRepository, salutMissatgeRepository, salutDetallRepository);
    }

    @Test
    void eliminarLlista_null_or_empty_no_ops() throws Exception {
        // Helper ha de delegar al servei de purga
        helper.eliminarDadesSalutAntigues(1L, TipusRegistreSalut.MINUT, LocalDateTime.now());
        verify(salutPurgeService, times(1)).eliminarDadesSalutAntigues(anyLong(), any(), any(LocalDateTime.class));
    }
}
