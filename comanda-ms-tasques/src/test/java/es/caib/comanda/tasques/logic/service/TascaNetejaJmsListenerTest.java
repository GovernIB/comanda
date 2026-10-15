package es.caib.comanda.tasques.logic.service;

import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.monitor.AccioTipusEnum;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import es.caib.comanda.tasques.logic.intf.service.TascaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.JMSException;
import javax.jms.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaris per a TascaNetejaJmsListener")
class TascaNetejaJmsListenerTest {

    @Mock private TascaService tascaService;
    @Mock private MonitorServiceClient monitorServiceClient;
    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private NetejaEntornAppMessage message;
    @Mock private Message jmsMessage;

    @InjectMocks
    private TascaNetejaJmsListener listener;

    private static final Long ENTORN_APP_ID = 123L;
    private static final String AUTH_HEADER = "Bearer mock-token";

    @BeforeEach
    void setUp() {
        when(message.getEntornAppId()).thenReturn(ENTORN_APP_ID);
    }

    @Test
    @DisplayName("Ha de processar correctament i fer acknowledge quan no hi ha errors")
    void processaNeteja_Exit_AcknowledgeCalled() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(1);
        // Act
        listener.processaNeteja(message, jmsMessage);
        // Assert
        verify(tascaService).netejaPerEntornApp(ENTORN_APP_ID);
        verify(jmsMessage).acknowledge();
        verify(monitorServiceClient, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("Ha de rellançar RuntimeException quan falla i el deliveryCount és menor de 3")
    void processaNeteja_Fallada_Reintenta_LlencaExcepcio() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(2);
        RuntimeException errorSimulat = new RuntimeException("Error de base de dades");
        doThrow(errorSimulat).when(tascaService).netejaPerEntornApp(ENTORN_APP_ID);
        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            listener.processaNeteja(message, jmsMessage);
        });
        assertTrue(ex.getMessage().contains("es reintentarà"));
        assertEquals(errorSimulat, ex.getCause());
        verify(jmsMessage, never()).acknowledge();
        verify(monitorServiceClient, never()).create(any(), anyString());
    }

    @Test
    @DisplayName("Ha de fer acknowledge i crear monitor quan falla i el deliveryCount és 3 o més")
    void processaNeteja_Fallada_MaximReintents_CreaMonitor() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        Exception errorSimulat = new RuntimeException("Error fatal persistent");
        doThrow(errorSimulat).when(tascaService).netejaPerEntornApp(ENTORN_APP_ID);
        // Act
        listener.processaNeteja(message, jmsMessage);
        // Assert
        verify(jmsMessage).acknowledge();
        ArgumentCaptor<Monitor> monitorCaptor = ArgumentCaptor.forClass(Monitor.class);
        verify(monitorServiceClient).create(monitorCaptor.capture(), eq(AUTH_HEADER));

        Monitor monitorCreat = monitorCaptor.getValue();
        assertEquals(ENTORN_APP_ID, monitorCreat.getEntornAppId());
        assertEquals(ModulEnum.TASCA, monitorCreat.getModul());
        assertEquals(AccioTipusEnum.INTERNA, monitorCreat.getTipus());
        assertEquals(EstatEnum.ERROR, monitorCreat.getEstat());
        assertEquals("netejaEntornApp", monitorCreat.getOperacio());
        assertNotNull(monitorCreat.getData());
        assertTrue(monitorCreat.getErrorDescripcio().contains("Error en la neteja"));
        assertEquals("Error fatal persistent", monitorCreat.getExcepcioMessage());
    }

    @Test
    @DisplayName("Ha de gestionar correctament una JMSException en fer acknowledge després de màxim reintent")
    void processaNeteja_Fallada_MaximReintents_JmsExceptionEnAcknowledge() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(4);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        doThrow(new RuntimeException("Error fatal")).when(tascaService).netejaPerEntornApp(ENTORN_APP_ID);
        doThrow(new JMSException("Error d'Artemis")).when(jmsMessage).acknowledge();
        // Act
        listener.processaNeteja(message, jmsMessage);
        // Assert
        verify(jmsMessage).acknowledge();
        verify(monitorServiceClient).create(any(Monitor.class), eq(AUTH_HEADER));
    }

    @Test
    @DisplayName("Ha de continuar l'execució sense fallar si la creació del monitor falla")
    void processaNeteja_Fallada_MaximReintents_ErrorCreantMonitor() throws JMSException {
        // Arrange
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn(AUTH_HEADER);
        doThrow(new RuntimeException("Error fatal")).when(tascaService).netejaPerEntornApp(ENTORN_APP_ID);
        doThrow(new RuntimeException("El servei de monitor està caigut")).when(monitorServiceClient).create(any(), anyString());
        // Act
        listener.processaNeteja(message, jmsMessage);
        // Assert
        verify(jmsMessage).acknowledge();
        verify(monitorServiceClient).create(any(Monitor.class), eq(AUTH_HEADER));
    }
}
