package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.client.model.AppRef;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.EntornRef;
import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.salut.logic.event.SalutInfoUpdatedEvent;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutHistRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.noop.NoopTimer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperValidationTest {

    @Mock
    private SalutRepository salutRepository;

    @Mock
    private SalutIntegracioRepository salutIntegracioRepository;

    @Mock
    private SalutSubsistemaRepository salutSubsistemaRepository;

    @Mock
    private SalutMissatgeRepository salutMissatgeRepository;

    @Mock
    private SalutDetallRepository salutDetallRepository;

    @Mock
    private SalutHistRepository salutHistRepository;

    @Mock
    private ParametresHelper parametresHelper;

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private MetricsHelper metricsHelper;

    @Mock
    private SalutPurgeHelper salutPurgeHelper;

    @InjectMocks
    private SalutInfoHelper helper;

    private EntornApp entornApp;

    @BeforeEach
    void setUp() {
        entornApp = EntornApp.builder()
                .id(5L)
                .app(new AppRef(1L, "Salut app"))
                .entorn(new EntornRef(1L, "PRO"))
                .salutUrl("http://localhost/health")
                .activa(true)
                .build();

        NoopTimer timer = new NoopTimer(new Meter.Id("test", Tags.empty(), null, null, Meter.Type.TIMER));
        when(metricsHelper.getSalutInfoGlobalTimer(any(), any())).thenReturn(timer);
//        when(salutHistRepository.findTopByEntornAppIdOrderByDataDescIdDesc(any())).thenReturn(null);
    }

    @Test
    void getSalutInfo_quanLaUrlNoEsAbsoluta_persistixSalutDownIPublicaEventAmbIdentificador() {
        SalutEntity savedEntity = new SalutEntity();
        savedEntity.setId(99L);
        when(salutRepository.save(any(SalutEntity.class))).thenReturn(savedEntity);
        ReflectionTestUtils.setField(entornApp, "salutUrl", " /path/relativa ");

        helper.getSalutInfo(entornApp);

        ArgumentCaptor<SalutEntity> salutCaptor = ArgumentCaptor.forClass(SalutEntity.class);
        ArgumentCaptor<SalutInfoUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(SalutInfoUpdatedEvent.class);
        verify(salutRepository).save(salutCaptor.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        verify(restTemplate, never()).getForObject(any(), any(Class.class));

        assertThat(salutCaptor.getValue().getEntornAppId()).isEqualTo(5L);
        assertThat(salutCaptor.getValue().getAppEstat()).isEqualTo(SalutEstat.DOWN);
        assertThat(salutCaptor.getValue().isPeticioError()).isTrue();
        assertThat(eventCaptor.getValue().getEntornAppId()).isEqualTo(5L);
        assertThat(eventCaptor.getValue().getSalutId()).isEqualTo(99L);
    }

    @Test
    void getSalutInfo_quanLaRespostaEsNull_noPersisteixSalutIPublicaEventSenseIdentificador() {
        when(restTemplate.getForObject(any(), any(Class.class))).thenReturn((SalutInfo) null);

        helper.getSalutInfo(entornApp);

        verify(salutRepository, never()).save(any(SalutEntity.class));
        verify(eventPublisher).publishEvent(any(SalutInfoUpdatedEvent.class));
        verify(metricsHelper).getSalutInfoGlobalTimer(isNull(), isNull());
        verify(metricsHelper).getSalutInfoGlobalTimer("PRO", "Salut app");
    }
}
