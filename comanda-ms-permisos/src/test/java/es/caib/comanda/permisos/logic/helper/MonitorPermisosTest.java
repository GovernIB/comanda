package es.caib.comanda.permisos.logic.helper;

import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MonitorPermisosTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endAction_marcaEstatOkQuanHiHaUsuariAutenticat() {
        // Comprova que el monitor calcula estat i temps en una finalització correcta.
        PermisosClientHelper helper = mock(PermisosClientHelper.class);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("usuari.test", "pwd"));
        MonitorPermisos monitor = new MonitorPermisos(4L, "/permisos/1", helper);

        monitor.startAction();
        monitor.endAction();

        Monitor monitorOk = (Monitor) ReflectionTestUtils.getField(monitor, "monitor");
        assertThat(monitorOk.getCodiUsuari()).isEqualTo("usuari.test");
        assertThat(monitorOk.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.isFinishedAction()).isTrue();
        verify(helper).monitorCreate(any(Monitor.class));
    }

    @Test
    void endActionAmbError_usaSchedulerIRespectaElMissatgeExplicit() {
        // Exercita els ramals sense usuari autenticat i amb descripció d'error informada explícitament.
        PermisosClientHelper helper = mock(PermisosClientHelper.class);
        MonitorPermisos monitor = new MonitorPermisos(6L, "/permisos/3", helper);

        monitor.startAction();
        monitor.endAction(new IllegalArgumentException("ko"), "error explícit");

        Monitor builtMonitor = (Monitor) ReflectionTestUtils.getField(monitor, "monitor");
        assertThat(builtMonitor.getCodiUsuari()).isEqualTo("SCHEDULER");
        assertThat(builtMonitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(builtMonitor.getErrorDescripcio()).isEqualTo("error explícit");
        verify(helper, Mockito.times(1)).monitorCreate(any(Monitor.class));
    }
}
