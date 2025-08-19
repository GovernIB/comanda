package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperDeletionTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;

    // Constructor requires these too, but not used here; we can provide dummies via additional mocks
    @Mock private es.caib.comanda.salut.logic.helper.SalutClientHelper salutClientHelper;
    @Mock private org.springframework.web.client.RestTemplate restTemplate;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks private SalutInfoHelper helper;

    private Object invokePrivate(String name, Class<?>[] types, Object... args) throws Exception {
        Method m = SalutInfoHelper.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(helper, args);
    }

    @BeforeEach
    void setup() {
        reset(salutRepository, salutIntegracioRepository, salutSubsistemaRepository, salutMissatgeRepository, salutDetallRepository);
    }

    @Test
    void eliminarLlista_null_or_empty_no_ops() throws Exception {
        invokePrivate("eliminarLlista", new Class[]{List.class}, new Object[]{null});
        invokePrivate("eliminarLlista", new Class[]{List.class}, Collections.emptyList());
        verifyNoInteractions(salutIntegracioRepository, salutSubsistemaRepository, salutMissatgeRepository, salutDetallRepository, salutRepository);
    }

    @Test
    void eliminarDadesSalutAntigues_batches_by_500() throws Exception {
        long entornAppId = 1L;
        LocalDateTime data = LocalDateTime.now();
        TipusRegistreSalut tipus = TipusRegistreSalut.MINUT;

        // Helper per provar diversos tamanys
        int[] sizes = new int[]{0,1,499,500,501,1000};
        for (int size : sizes) {
            reset(salutRepository, salutIntegracioRepository, salutSubsistemaRepository, salutMissatgeRepository, salutDetallRepository);
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < size; i++) ids.add((long) i+1);
            when(salutRepository.findIdsByEntornAppIdAndTipusRegistreAndDataBefore(eq(entornAppId), eq(tipus), any(LocalDateTime.class)))
                    .thenReturn(ids);

            invokePrivate("eliminarDadesSalutAntigues", new Class[]{Long.class, TipusRegistreSalut.class, LocalDateTime.class}, entornAppId, tipus, data);

            int expectedBatches = (size + 499) / 500; // ceil
            // Verifica crides per lots
            verify(salutIntegracioRepository, times(expectedBatches)).deleteAllBySalutIdIn(anyList());
            verify(salutSubsistemaRepository, times(expectedBatches)).deleteAllBySalutIdIn(anyList());
            verify(salutMissatgeRepository, times(expectedBatches)).deleteAllBySalutIdIn(anyList());
            verify(salutDetallRepository, times(expectedBatches)).deleteAllBySalutIdIn(anyList());
            verify(salutRepository, times(expectedBatches)).deleteAllById(anyList());
        }
    }
}
