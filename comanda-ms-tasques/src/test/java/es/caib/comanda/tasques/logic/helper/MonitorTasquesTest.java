package es.caib.comanda.tasques.logic.helper;

import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import org.junit.jupiter.api.BeforeEach;
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
class MonitorTasquesTest {

    @Mock
    private TasquesClientHelper tasquesClientHelper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private MonitorTasques monitorTasques;

    private final Long entornAppId = 1L;
    private final String url = "http://test.url";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void constructor_quanUsuariAutenticat_creaMonitorAmbUsuari() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testUser");

        // Act
        monitorTasques = new MonitorTasques(entornAppId, url, tasquesClientHelper);

        // Assert
        assertThat(monitorTasques.getMonitor().getCodiUsuari()).isEqualTo("testUser");
        assertThat(monitorTasques.getMonitor().getEntornAppId()).isEqualTo(entornAppId);
        assertThat(monitorTasques.getMonitor().getUrl()).isEqualTo(url);
    }

    @Test
    void constructor_quanNoUsuari_creaScheduler() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        monitorTasques = new MonitorTasques(entornAppId, url, tasquesClientHelper);

        // Assert
        assertThat(monitorTasques.getMonitor().getCodiUsuari()).isEqualTo("SCHEDULER");
    }

    @Test
    void endAction_quanTotOk_cridaCreateAmbEstatOk() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        monitorTasques = new MonitorTasques(entornAppId, url, tasquesClientHelper);
        monitorTasques.startAction();

        // Act
        monitorTasques.endAction();

        // Assert
        ArgumentCaptor<Monitor> monitorCaptor = ArgumentCaptor.forClass(Monitor.class);
        verify(tasquesClientHelper).monitorCreate(monitorCaptor.capture());
        
        Monitor capturedMonitor = monitorCaptor.getValue();
        assertThat(capturedMonitor.getEstat()).isEqualTo(EstatEnum.OK);
        assertThat(capturedMonitor.getTempsResposta()).isNotNull();
        assertThat(monitorTasques.isFinishedAction()).isTrue();
    }

    @Test
    void endAction_quanError_cridaCreateAmbEstatError() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        monitorTasques = new MonitorTasques(entornAppId, url, tasquesClientHelper);
        monitorTasques.startAction();
        Exception exception = new RuntimeException("Error de prova");

        // Act
        monitorTasques.endAction(exception, "Descripció error");

        // Assert
        ArgumentCaptor<Monitor> monitorCaptor = ArgumentCaptor.forClass(Monitor.class);
        verify(tasquesClientHelper).monitorCreate(monitorCaptor.capture());
        
        Monitor capturedMonitor = monitorCaptor.getValue();
        assertThat(capturedMonitor.getEstat()).isEqualTo(EstatEnum.ERROR);
        assertThat(capturedMonitor.getErrorDescripcio()).isEqualTo("Descripció error");
        assertThat(capturedMonitor.getExcepcioMessage()).contains("Error de prova");
        assertThat(capturedMonitor.getExcepcioStacktrace()).isNotNull();
    }
}
