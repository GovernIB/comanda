package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.ms.logic.helper.ParametresHelper;
import es.caib.comanda.salut.persist.repository.SalutDetallRepository;
import es.caib.comanda.salut.persist.repository.SalutHistRepository;
import es.caib.comanda.salut.persist.repository.SalutIntegracioRepository;
import es.caib.comanda.salut.persist.repository.SalutMissatgeRepository;
import es.caib.comanda.salut.persist.repository.SalutRepository;
import es.caib.comanda.salut.persist.repository.SalutSubsistemaRepository;
import es.caib.comanda.model.v1.salut.EstatSalutEnum;
import es.caib.comanda.salut.logic.helper.SalutInfoHelper;
import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SalutInfoHelperPrivateMethodsTest {

    @Mock private SalutRepository salutRepository;
    @Mock private SalutIntegracioRepository salutIntegracioRepository;
    @Mock private SalutSubsistemaRepository salutSubsistemaRepository;
    @Mock private SalutMissatgeRepository salutMissatgeRepository;
    @Mock private SalutDetallRepository salutDetallRepository;
    @Mock private SalutHistRepository salutHistRepository;
    @Mock private SalutClientHelper salutClientHelper;
    @Mock private RestTemplate restTemplate;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MetricsHelper metricsHelper;
    @Mock private SalutPurgeHelper salutPurgeService;
    @Mock private ParametresHelper parametresHelper;

    @InjectMocks
    private SalutInfoHelper helper;

    private Object invokePrivate(String name, Class<?>[] types, Object... args) throws Exception {
        Method m = SalutInfoHelper.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(helper, args);
    }

    @Test
    void toSalutEstat_quanRepCadaValor_mappejaElSalutEstatCorrecte() throws Exception {
        for (EstatSalutEnum e : EstatSalutEnum.values()) {
            Object res = invokePrivate("toSalutEstat", new Class[]{EstatSalutEnum.class}, e);
            assertThat(res).isNotNull();
            assertThat(res.toString()).isEqualTo(e.name());
        }

        Object unknownRes = invokePrivate("toSalutEstat", new Class[]{EstatSalutEnum.class}, new Object[]{null});
        assertThat(unknownRes).isEqualTo(SalutEstat.UNKNOWN);
    }

    @Test
    void toLocalDateTime_quanRepDate_laConverteixIAdmetNull() {
        Date now = new Date();
        LocalDateTime ldt = helper.toLocalDateTime(now);
        assertThat(ldt).isNotNull();
        assertThat(helper.toLocalDateTime(null)).isNull();
    }

    @Test
    void isFirstMinuteHelpers_quanRepDiversesDates_retornenElValorEsperat() throws Exception {
        LocalDateTime h0m0 = LocalDateTime.of(2025, Month.AUGUST, 1, 0, 0);
        LocalDateTime h1m0 = LocalDateTime.of(2025, Month.AUGUST, 1, 1, 0);
        LocalDateTime h1m1 = LocalDateTime.of(2025, Month.AUGUST, 1, 1, 1);

        assertThat((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, h0m0)).isTrue();
        assertThat((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, h1m0)).isTrue();
        assertThat((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, h1m1)).isFalse();
        assertThat((Boolean) invokePrivate("isFirstMinuteOfHour", new Class[]{LocalDateTime.class}, new Object[]{null})).isFalse();

        assertThat((Boolean) invokePrivate("isFirstMinuteOfDay", new Class[]{LocalDateTime.class}, h0m0)).isTrue();
        assertThat((Boolean) invokePrivate("isFirstMinuteOfDay", new Class[]{LocalDateTime.class}, h1m0)).isFalse();
        assertThat((Boolean) invokePrivate("isFirstMinuteOfDay", new Class[]{LocalDateTime.class}, new Object[]{null})).isFalse();
    }
}
