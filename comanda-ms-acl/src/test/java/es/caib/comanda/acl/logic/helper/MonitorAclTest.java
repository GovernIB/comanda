package es.caib.comanda.acl.logic.helper;

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

class MonitorAclTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void endAction_marcaEstatOkQuanHiHaUsuariAutenticat() {
        // Comprova el flux correcte de monitorització quan l'acció acaba bé amb usuari autenticat.
        AclClientHelper helper = mock(AclClientHelper.class);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("acl.user", "pwd"));
        MonitorAcl monitor = new MonitorAcl(2L, "/acl", helper);

        monitor.startAction();
        monitor.endAction();

        Monitor built = (Monitor) ReflectionTestUtils.getField(monitor, "monitor");
        assertThat(built.getCodiUsuari()).isEqualTo("acl.user");
        assertThat(built.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.isFinishedAction()).isTrue();
        verify(helper).monitorCreate(any(Monitor.class));
    }

    @Test
    void endActionAmbError_usaSchedulerQuanNoHiHaUsuariAutenticat() {
        // Verifica el flux d'error quan no hi ha autenticació i s'ha d'atribuir al scheduler.
        AclClientHelper helper = mock(AclClientHelper.class);
        MonitorAcl monitor = new MonitorAcl(3L, "/acl/error", helper);

        monitor.startAction();
        monitor.endAction(new IllegalStateException("ko"), "");

        Monitor built = (Monitor) ReflectionTestUtils.getField(monitor, "monitor");
        assertThat(built.getCodiUsuari()).isEqualTo("SCHEDULER");
        assertThat(built.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(built.getErrorDescripcio()).isNotBlank();
        verify(helper, Mockito.times(1)).monitorCreate(any(Monitor.class));
    }
}
