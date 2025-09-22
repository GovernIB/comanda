package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.salut.model.EstatSalut;
import es.caib.comanda.ms.salut.model.EstatSalutEnum;
import es.caib.comanda.ms.salut.model.SalutInfo;
import es.caib.comanda.salut.logic.event.SalutInfoUpdatedEvent;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperGetSalutInfoEventTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private SalutClientHelper salutClientHelper;
    @Mock private RestTemplate restTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MetricsHelper metricsHelper;

    @InjectMocks private SalutInfoHelper helper;

    private EntornApp entornApp;

    @Captor private ArgumentCaptor<SalutInfoUpdatedEvent> eventCaptor;

    @BeforeEach
    void setup() {
        SalutInfoHelper.compactacioMap.clear();
        entornApp = EntornApp.builder()
                .id(42L)
                .app(new AppRef(1L, "App"))
                .entorn(new EntornRef(2L, "Entorn"))
                .salutUrl("http://x/health")
                .activa(true)
                .build();

        // Setup metrics mocks
        Timer globalTimer = mock(Timer.class);
        Timer appTimer = mock(Timer.class);
        when(metricsHelper.getSalutInfoGlobalTimer(null, null)).thenReturn(globalTimer);
        when(metricsHelper.getSalutInfoGlobalTimer(
                entornApp.getEntorn().getNom(),
                entornApp.getApp().getNom())).thenReturn(appTimer);
    }

    private SalutInfo sampleInfo() {
        EstatSalut app = EstatSalut.builder().estat(EstatSalutEnum.UP).latencia(10).build();
        EstatSalut bd = EstatSalut.builder().estat(EstatSalutEnum.UP).latencia(5).build();
        return SalutInfo.builder()
                .codi("C")
                .data(new Date())
                .estat(app)
                .bd(bd)
                .build();
    }

    @Test
    void publishEvent_on_success_increments_numeroDiesAgrupacio() {
        when(restTemplate.getForObject(anyString(), eq(SalutInfo.class))).thenReturn(sampleInfo());
        when(salutRepository.save(any(SalutEntity.class))).thenAnswer(inv -> {
            SalutEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(100L);
            return e;
        });

        helper.getSalutInfo(entornApp); // first -> numeroDiesAgrupacio 1
        helper.getSalutInfo(entornApp); // second -> 2

        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
        assertEquals(1, eventCaptor.getAllValues().get(0).getNumeroDiesAgrupacio());
        assertEquals(2, eventCaptor.getAllValues().get(1).getNumeroDiesAgrupacio());
        // entornAppId i salutId no nuls
        assertEquals(entornApp.getId(), eventCaptor.getAllValues().get(0).getEntornAppId());
        assertNotNull(eventCaptor.getAllValues().get(0).getSalutId());
    }

    @Test
    void publishEvent_on_error_path_also_occurs() {
        when(restTemplate.getForObject(anyString(), eq(SalutInfo.class))).thenThrow(new RestClientException("x"));
        when(salutRepository.save(any(SalutEntity.class))).thenAnswer(inv -> {
            SalutEntity e = inv.getArgument(0);
            e.setId(777L);
            return e;
        });

        helper.getSalutInfo(entornApp);

        verify(eventPublisher).publishEvent(eventCaptor.capture());
        SalutInfoUpdatedEvent ev = eventCaptor.getValue();
        assertEquals(entornApp.getId(), ev.getEntornAppId());
        assertEquals(777L, ev.getSalutId());
        assertTrue(ev.getNumeroDiesAgrupacio() >= 1);
    }
}
