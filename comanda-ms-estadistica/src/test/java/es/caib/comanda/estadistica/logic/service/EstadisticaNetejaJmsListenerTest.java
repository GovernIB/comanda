package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.monitor.EstatEnum;
import es.caib.comanda.client.model.monitor.ModulEnum;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaNetejaService;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.jms.NetejaAppMessage;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaNetejaJmsListener")
class EstadisticaNetejaJmsListenerTest {

    @Mock
    private EstadisticaNetejaService estadisticaNetejaService;
    @Mock
    private MonitorServiceClient monitorServiceClient;
    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock
    private Message jmsMessage;

    @InjectMocks
    private EstadisticaNetejaJmsListener jmsListener;

    @Test
    @DisplayName("NetejaAppMessage: quan té èxit, crida el servei i fa acknowledge")
    void processaNeteja_quanAppMessageExit_llavorsExecutaIAcknowledge() throws Exception {
        NetejaAppMessage message = new NetejaAppMessage(10L, List.of(42L, 43L), true, false);
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(1);

        jmsListener.processaNeteja(message, jmsMessage);

        verify(estadisticaNetejaService).netejaPerApp(10L, List.of(42L, 43L), true, false);
        verify(jmsMessage).acknowledge();
        verifyNoInteractions(monitorServiceClient);
    }

    @Test
    @DisplayName("NetejaAppMessage: quan falla amb menys de 3 intents, llança excepció per reintentar")
    void processaNeteja_quanAppMessageErrorMenys3Intents_llavorsReintenta() throws Exception {
        NetejaAppMessage message = new NetejaAppMessage(10L, List.of(42L), false, false);
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(1);
        doThrow(new RuntimeException("DB error")).when(estadisticaNetejaService).netejaPerApp(any(), any(), anyBoolean(), anyBoolean());

        assertThrows(RuntimeException.class, () -> jmsListener.processaNeteja(message, jmsMessage));

        verify(jmsMessage, never()).acknowledge();
        verifyNoInteractions(monitorServiceClient);
    }

    @Test
    @DisplayName("NetejaAppMessage: quan falla al 3r intent, fa acknowledge i notifica al monitor per a cada entorn")
    void processaNeteja_quanAppMessageError3Intents_llavorsNotificaMonitorPerCadaEntorn() throws Exception {
        NetejaAppMessage message = new NetejaAppMessage(10L, List.of(42L, 43L), false, false);
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        doThrow(new RuntimeException("Error en neteja")).when(estadisticaNetejaService).netejaPerApp(any(), any(), anyBoolean(), anyBoolean());

        jmsListener.processaNeteja(message, jmsMessage);

        verify(jmsMessage).acknowledge();
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(monitorServiceClient, times(2)).create(captor.capture(), eq("Bearer token"));

        List<Monitor> monitors = captor.getAllValues();
        assertEquals(42L, monitors.get(0).getEntornAppId());
        assertEquals(ModulEnum.ESTADISTICA, monitors.get(0).getModul());
        assertEquals("netejaEntornApp", monitors.get(0).getOperacio());
        assertEquals(EstatEnum.ERROR, monitors.get(0).getEstat());

        assertEquals(43L, monitors.get(1).getEntornAppId());
        assertEquals(ModulEnum.ESTADISTICA, monitors.get(1).getModul());
    }

    @Test
    @DisplayName("NetejaEntornAppMessage: quan té èxit, crida el servei i fa acknowledge")
    void processaNeteja_quanEntornMessageExit_llavorsExecutaIAcknowledge() throws Exception {
        NetejaEntornAppMessage message = new NetejaEntornAppMessage(42L);
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(1);

        jmsListener.processaNeteja(message, jmsMessage);

        verify(estadisticaNetejaService).netejaPerEntornApp(42L);
        verify(jmsMessage).acknowledge();
        verifyNoInteractions(monitorServiceClient);
    }

    @Test
    @DisplayName("NetejaEntornAppMessage: quan falla al 3r intent, notifica al monitor amb l'entornAppId")
    void processaNeteja_quanEntornMessageError3Intents_llavorsNotificaMonitor() throws Exception {
        NetejaEntornAppMessage message = new NetejaEntornAppMessage(42L);
        when(jmsMessage.getIntProperty("JMSXDeliveryCount")).thenReturn(3);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        doThrow(new RuntimeException("DB down")).when(estadisticaNetejaService).netejaPerEntornApp(42L);

        jmsListener.processaNeteja(message, jmsMessage);

        verify(jmsMessage).acknowledge();
        ArgumentCaptor<Monitor> captor = ArgumentCaptor.forClass(Monitor.class);
        verify(monitorServiceClient, times(1)).create(captor.capture(), eq("Bearer token"));

        Monitor monitor = captor.getValue();
        assertEquals(42L, monitor.getEntornAppId());
        assertEquals(ModulEnum.ESTADISTICA, monitor.getModul());
        assertEquals("netejaEntornApp", monitor.getOperacio());
        assertEquals(EstatEnum.ERROR, monitor.getEstat());
    }
}
