package es.caib.comanda.avisos.logic.service;

import es.caib.comanda.avisos.logic.intf.service.AvisService;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a AvisNetejaJmsListener")
class AvisNetejaJmsListenerTest {

    @Mock
    private AvisService avisService;

    @Mock
    private MonitorServiceClient monitorServiceClient;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private NetejaEntornAppMessage message;

    @Mock
    private Message jmsMessage;

    @InjectMocks
    private AvisNetejaJmsListener avisNetejaJmsListener;

    private static final String AUTH_HEADER = "Bearer token";
    private static final Long ENTORN_APP_ID = 100L;

    @BeforeEach
    void setUp() {
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        lenient().when(message.getEntornAppId()).thenReturn(ENTORN_APP_ID);
    }

    // ========================================================================
    // 1. CAMÍ D'ÈXIT (HAPPY PATH)
    // ========================================================================

    @Test
    @DisplayName("processaNeteja: executa la neteja i fa acknowledge quan té èxit al primer intent")
    void processaNeteja_quanExecucioExitosa_LlavorsFaAcknowledge() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(1);

        // Act
        avisNetejaJmsListener.processaNeteja(message, jmsMessage);

        // Assert
        verify(avisService, times(1)).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage, times(1)).acknowledge();
        verify(monitorServiceClient, never()).create(any(), anyString());
    }

    // ========================================================================
    // 2. CAMÍ DE REINTENT (DELIVERY COUNT < 3)
    // ========================================================================

    @Test
    @DisplayName("processaNeteja: llança RuntimeException per forçar reintent quan falla i deliveryCount < 3")
    void processaNeteja_quanFallaIReintentDisponible_LlavorsLlancaExcepcioINoFaAcknowledge() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(2);
        RuntimeException expectedException = new RuntimeException("Error de base de dades");
        doThrow(expectedException).when(avisService).netejaPerEntornApp(ENTORN_APP_ID);

        // Act & Assert
        assertThatThrownBy(() -> avisNetejaJmsListener.processaNeteja(message, jmsMessage))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error en neteja, es reintentarà")
            .hasCause(expectedException);

        verify(avisService, times(1)).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage, never()).acknowledge(); // No s'ha de fer acknowledge per permetre el reintert d'Artemis
        verify(monitorServiceClient, never()).create(any(), anyString());
    }

    // ========================================================================
    // 3. CAMÍ D'ERROR FINAL (DELIVERY COUNT >= 3)
    // ========================================================================

    @Test
    @DisplayName("processaNeteja: fa acknowledge i crea entrada al monitor quan falla i s'assoleix el límit de 3 intents")
    void processaNeteja_quanFallaILimitReintentsAssolit_LlavorsFaAcknowledgeICreaMonitor() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        RuntimeException expectedException = new RuntimeException("Error persistent");
        doThrow(expectedException).when(avisService).netejaPerEntornApp(ENTORN_APP_ID);

        // Act
        // No ha de llançar excepció cap enfora, ja que s'ha gestionat el límit de reintents
        assertThatCode(() -> avisNetejaJmsListener.processaNeteja(message, jmsMessage)).doesNotThrowAnyException();

        // Assert
        verify(avisService, times(1)).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage, times(1)).acknowledge(); // S'ha de fer acknowledge per treure'l de la cua

        verify(monitorServiceClient, times(1)).create(
            argThat(monitor ->
                monitor.getEntornAppId().equals(ENTORN_APP_ID) &&
                    monitor.getModul() == ModulEnum.AVIS &&
                    monitor.getExcepcioMessage().equals("Error persistent")
            ),
            eq(AUTH_HEADER)
        );
    }

    // ========================================================================
    // 4. CASOS LÍMIT I GESTIÓ D'EXCEPCIONS EN LA GESTIÓ D'ERRORS
    // ========================================================================

    @Test
    @DisplayName("processaNeteja: gestiona correctament una JMSException en fer acknowledge al límit de reintents")
    void processaNeteja_quanFallaAcknowledgeAlLimitReintents_LlavorsRegistraErrorICreaMonitorIgualment() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        RuntimeException serviceException = new RuntimeException("Error persistent");
        JMSException jmsException = new JMSException("Error de connexió JMS");

        doThrow(serviceException).when(avisService).netejaPerEntornApp(ENTORN_APP_ID);
        doThrow(jmsException).when(jmsMessage).acknowledge();

        // Act
        // No ha de propagar ni la RuntimeException original ni la JMSException
        assertThatCode(() -> avisNetejaJmsListener.processaNeteja(message, jmsMessage)).doesNotThrowAnyException();

        // Assert
        verify(avisService, times(1)).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage, times(1)).acknowledge();

        // Assegurem que malgrat la JMSException, es va intentar crear l'entrada al monitor
        verify(monitorServiceClient, times(1)).create(any(Monitor.class), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("crearEntradaMonitorError: no propaga l'excepció si el MonitorServiceClient falla")
    void crearEntradaMonitorError_quanClientMonitorFalla_LlavorsNoPropagaExcepcio() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        RuntimeException serviceException = new RuntimeException("Error persistent");
        RuntimeException monitorException = new RuntimeException("Monitor API down");

        doThrow(serviceException).when(avisService).netejaPerEntornApp(ENTORN_APP_ID);
        doThrow(monitorException).when(monitorServiceClient).create(any(Monitor.class), eq(AUTH_HEADER));

        // Act
        // La fallada del monitor no ha d'impedir que el mètode acabi netament
        assertThatCode(() -> avisNetejaJmsListener.processaNeteja(message, jmsMessage)).doesNotThrowAnyException();

        // Assert
        verify(avisService, times(1)).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage, times(1)).acknowledge();
        verify(monitorServiceClient, times(1)).create(any(Monitor.class), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("processaNeteja: gestiona correctament un deliveryCount exacte de 4 (més del límit)")
    void processaNeteja_quanDeliveryCountEs4_LlavorsEsComportaComAlLimitDe3() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(4);
        RuntimeException expectedException = new RuntimeException("Error persistent");
        doThrow(expectedException).when(avisService).netejaPerEntornApp(ENTORN_APP_ID);

        // Act
        assertThatCode(() -> avisNetejaJmsListener.processaNeteja(message, jmsMessage)).doesNotThrowAnyException();

        // Assert
        verify(avisService, times(1)).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage, times(1)).acknowledge();
        verify(monitorServiceClient, times(1)).create(any(Monitor.class), eq(AUTH_HEADER));
    }
}
