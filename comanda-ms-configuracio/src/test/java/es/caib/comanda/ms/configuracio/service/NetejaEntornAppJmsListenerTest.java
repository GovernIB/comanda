package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.configuracio.logic.service.NetejaEntornAppJmsListener;
import es.caib.comanda.ms.logic.intf.event.EntornAppEsborratEvent;
import es.caib.comanda.ms.logic.intf.jms.NetejaEntornAppMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;

import javax.jms.Message;

import static es.caib.comanda.base.config.Cues.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a NetejaEntornAppJmsListener")
class NetejaEntornAppJmsListenerTest {

    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private JmsTemplate jmsTemplate;
    @Mock private Message jmsMessage;

    @InjectMocks
    private NetejaEntornAppJmsListener listener;

    @Test
    @DisplayName("onEntornAppEsborrat: encua el missatge amb l'app, l'entorn i el flag d'entornApp esborrat")
    void onEntornAppEsborrat_encuaMissatgeAmbAppIEntorn() {
        listener.onEntornAppEsborrat(new EntornAppEsborratEvent(42L, 5L, 7L));

        ArgumentCaptor<NetejaEntornAppMessage> captor = ArgumentCaptor.forClass(NetejaEntornAppMessage.class);
        verify(jmsTemplate).convertAndSend(eq(CUA_NETEJA_ENTORN_APP), captor.capture());
        NetejaEntornAppMessage message = captor.getValue();
        assertEquals(42L, message.getEntornAppId());
        assertEquals(5L, message.getAppId());
        assertEquals(7L, message.getEntornId());
        assertTrue(message.isEntornAppEsborrat());
    }

    @Test
    @DisplayName("processaNeteja: envia a estadístiques el missatge ENTORN amb l'app, l'entorn i el flag d'entornApp esborrat")
    void processaNeteja_enviaMissatgeEstadisticaAmbEntornAppEsborrat() throws Exception {
        listener.processaNeteja(new NetejaEntornAppMessage(42L, 5L, 7L, true), jmsMessage);

        verify(jmsTemplate).convertAndSend(eq(CUA_NETEJA_SALUT), any(NetejaEntornAppMessage.class));
        verify(jmsTemplate).convertAndSend(eq(CUA_NETEJA_TASQUES), any(NetejaEntornAppMessage.class));
        verify(jmsTemplate).convertAndSend(eq(CUA_NETEJA_AVISOS), any(NetejaEntornAppMessage.class));
        verify(jmsTemplate).convertAndSend(eq(CUA_NETEJA_ALARMES), any(NetejaEntornAppMessage.class));

        ArgumentCaptor<NetejaEntornAppMessage> messageCaptor = ArgumentCaptor.forClass(NetejaEntornAppMessage.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(jmsTemplate).convertAndSend(eq(CUA_NETEJA_ESTADISTICA), messageCaptor.capture(), postProcessorCaptor.capture());
        NetejaEntornAppMessage message = messageCaptor.getValue();
        assertEquals(42L, message.getEntornAppId());
        assertEquals(5L, message.getAppId());
        assertEquals(7L, message.getEntornId());
        assertTrue(message.isEntornAppEsborrat());

        Message estadisticaJmsMessage = mock(Message.class);
        postProcessorCaptor.getValue().postProcessMessage(estadisticaJmsMessage);
        verify(estadisticaJmsMessage).setStringProperty("TIPUS_MISSATGE", "ENTORN");
        verify(jmsMessage).acknowledge();
    }

}
