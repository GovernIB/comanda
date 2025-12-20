package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.v1.salut.EstatSalut;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.model.v1.salut.InformacioSistema;
import es.caib.comanda.model.v1.salut.SalutInfo;
import es.caib.comanda.salut.logic.helper.MetricsHelper;
import es.caib.comanda.salut.logic.helper.SalutClientHelper;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.persist.entity.SalutDetallEntity;
import es.caib.comanda.salut.persist.entity.SalutEntity;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperInformacioSistemaTest {

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

    @Captor private ArgumentCaptor<SalutDetallEntity> detallCaptor;

    private Method crearSalutMethod;

    @BeforeEach
    void setup() throws Exception {
        crearSalutMethod = SalutInfoHelper.class.getDeclaredMethod("crearSalut", SalutInfo.class, Long.class, LocalDateTime.class);
        crearSalutMethod.setAccessible(true);
    }

    @Test
    void crearSalut_persists_detalls_from_informacioSistema_object() throws Exception {
        Long entornAppId = 9L;
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        EstatSalut appEstat = EstatSalut.builder().estat(EstatSalutEnum.UP).latencia(20).build();
        EstatSalut bdEstat = EstatSalut.builder().estat(EstatSalutEnum.UP).latencia(10).build();

        InformacioSistema sys = InformacioSistema.builder()
                .processadors(8)
                .carregaSistema("1.23")
                .cpuSistema("12.50 %")
                .memoriaTotal("16.0 GB")
                .memoriaDisponible("8.5 GB")
                .espaiDiscTotal("500.0 GB")
                .espaiDiscLliure("120.0 GB")
                .sistemaOperatiu("Linux (5.15.0)")
                .dataArrencada("25/11/2025 08:12")
                .tempsFuncionant("3 dies, 04:21:10")
                .build();

        SalutInfo info = SalutInfo.builder()
                .codi("APP")
                .data(OffsetDateTime.now())
                .estatGlobal(appEstat)
                .estatBaseDeDades(bdEstat)
                .informacioSistema(sys)
                .build();

        when(salutRepository.save(any(SalutEntity.class))).thenAnswer(inv -> {
            SalutEntity e = inv.getArgument(0);
            if (e.getId() == null) e.setId(2L);
            return e;
        });
        when(salutDetallRepository.save(any(SalutDetallEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Long id = (Long) crearSalutMethod.invoke(helper, info, entornAppId, now);
        assertNotNull(id);

        // Com a mínim comprovar que es creen detalls principals
        verify(salutDetallRepository, times(10)).save(detallCaptor.capture());
        SalutDetallEntity d = detallCaptor.getValue();
        assertNotNull(d.getCodi());
        assertNotNull(d.getNom());
        assertNotNull(d.getValor());
    }
}
