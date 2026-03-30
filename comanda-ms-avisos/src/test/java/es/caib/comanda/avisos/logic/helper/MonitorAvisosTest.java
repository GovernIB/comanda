package es.caib.comanda.avisos.logic.helper;

import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitorAvisosTest {

    @Mock
    private AvisClientHelper avisClientHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("MonitorAvisos ha de crear un monitor amb dades per defecte")
    void constructor_haDeCrearMonitorCorrectament() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("usuari1");

        // Act
        MonitorAvisos monitorAvisos = new MonitorAvisos(10L, "http://url", avisClientHelper);

        // Assert
        Monitor monitor = monitorAvisos.getMonitor();
        assertThat(monitor.getEntornAppId()).isEqualTo(10L);
        assertThat(monitor.getUrl()).isEqualTo("http://url");
        assertThat(monitor.getCodiUsuari()).isEqualTo("usuari1");
        assertThat(monitor.getTipus()).isEqualTo(AccioTipusEnum.SORTIDA);
    }

    @Test
    @DisplayName("endAction ha de registrar el monitor amb estat OK")
    void endAction_haDeRegistrarMonitorOK() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        MonitorAvisos monitorAvisos = new MonitorAvisos(10L, "url", avisClientHelper);
        monitorAvisos.startAction();

        // Act
        monitorAvisos.endAction();

        // Assert
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(avisClientHelper).monitorCreate(captor.capture());
        Monitor monitor = captor.getValue();
        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(monitor.getTempsResposta()).isNotNull();
    }

    @Test
    @DisplayName("endAction amb excepció ha de registrar estat ERROR i stacktrace")
    void endAction_ambExcepcio_haDeRegistrarError() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        MonitorAvisos monitorAvisos = new MonitorAvisos(10L, "url", avisClientHelper);
        monitorAvisos.startAction();
        Exception ex = new RuntimeException("Error fatal");

        // Act
        monitorAvisos.endAction(ex, "Descripció error");

        // Assert
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(avisClientHelper).monitorCreate(captor.capture());
        Monitor monitor = captor.getValue();
        assertThat(monitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(monitor.getErrorDescripcio()).isEqualTo("Descripció error");
        assertThat(monitor.getExcepcioMessage()).contains("Error fatal");
        assertThat(monitor.getExcepcioStacktrace()).isNotNull();
    }
}
