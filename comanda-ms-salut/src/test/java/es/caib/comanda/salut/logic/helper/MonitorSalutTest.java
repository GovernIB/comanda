package es.caib.comanda.salut.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorSalutTest {

    @Mock
    private SalutClientHelper salutClientHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Captor
    private ArgumentCaptor<Monitor> monitorCaptor;

    private static final Long ENTORN_APP_ID = 1L;
    private static final String SALUT_URL = "http://test.com/health";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void constructor_quanHiHaUsuariAutenticat_inicialitzaElMonitorAmbLesMetadadesBasiques() {
        when(authentication.getName()).thenReturn("testUser");

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);

        assertThat(monitorSalut.isFinishedAction()).isFalse();
        assertThat(monitorSalut.getStartTime()).isNull();
        assertThat(monitorSalut.getMonitor().getEntornAppId()).isEqualTo(ENTORN_APP_ID);
        assertThat(monitorSalut.getMonitor().getUrl()).isEqualTo(SALUT_URL);
        assertThat(monitorSalut.getMonitor().getModul()).isEqualTo(ModulEnum.SALUT);
        assertThat(monitorSalut.getMonitor().getTipus()).isEqualTo(AccioTipusEnum.SORTIDA);
        assertThat(monitorSalut.getMonitor().getCodiUsuari()).isEqualTo("testUser");
    }

    @Test
    void startAction_quanSiniciaAccio_registraDataIHoraDinici() {
        when(authentication.getName()).thenReturn("testUser");

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);

        monitorSalut.startAction();

        assertThat(monitorSalut.getStartTime()).isNotNull();
        assertThat(monitorSalut.getMonitor().getData()).isNotNull();
    }

    @Test
    void endAction_quanFinalitzaCorrectament_publicaMonitorAmbEstatOk() {
        when(authentication.getName()).thenReturn("testUser");

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);
        monitorSalut.startAction();

        monitorSalut.endAction();

        verify(salutClientHelper).monitorCreate(monitorCaptor.capture());
        assertThat(monitorSalut.isFinishedAction()).isTrue();
        assertThat(monitorCaptor.getValue().getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitorCaptor.getValue().getTempsResposta()).isNotNull().isGreaterThanOrEqualTo(0L);
    }

    @Test
    void endAction_quanHiHaErrorSenseDescripcio_personalitzaElMissatgePerDefecte() {
        when(authentication.getName()).thenReturn("testUser");

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);
        monitorSalut.startAction();
        RuntimeException exception = new RuntimeException("boom");

        monitorSalut.endAction(exception, null);

        verify(salutClientHelper).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isEqualTo("S'ha produït un error obtenint les dades de salut");
        assertThat(monitorCaptor.getValue().getExcepcioMessage()).contains("boom");
        assertThat(monitorCaptor.getValue().getExcepcioStacktrace()).contains("RuntimeException");
    }

    @Test
    void endAction_quanHiHaErrorAmbDescripcio_usaLaDescripcioProporcionada() {
        when(authentication.getName()).thenReturn("testUser");

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);
        monitorSalut.startAction();

        monitorSalut.endAction(new IllegalStateException("ko"), "descripcio controlada");

        verify(salutClientHelper).monitorCreate(monitorCaptor.capture());
        assertThat(monitorCaptor.getValue().getErrorDescripcio()).isEqualTo("descripcio controlada");
    }

    @Test
    void constructor_quanNoHiHaAutenticacio_usaSchedulerComACodiUsuari() {
        when(securityContext.getAuthentication()).thenReturn(null);

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);

        assertThat(monitorSalut.getMonitor().getCodiUsuari()).isEqualTo("SCHEDULER");
    }

    @Test
    void constructor_quanElNomDusariEsBuit_usaSchedulerComACodiUsuari() {
        when(authentication.getName()).thenReturn("");

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);

        assertThat(monitorSalut.getMonitor().getCodiUsuari()).isEqualTo("SCHEDULER");
    }

    @Test
    void constructor_quanElContextDeSeguretatLlanzaExcepcio_usaSchedulerComACodiUsuari() {
        when(securityContext.getAuthentication()).thenThrow(new RuntimeException("security error"));

        MonitorSalut monitorSalut = new MonitorSalut(ENTORN_APP_ID, SALUT_URL, salutClientHelper);

        assertThat(monitorSalut.getMonitor().getCodiUsuari()).isEqualTo("SCHEDULER");
    }
}
