package es.caib.comanda.broker.controller;

import es.caib.comanda.broker.dto.BrokerInfoDTO;
import es.caib.comanda.broker.dto.MessageInfoDTO;
import es.caib.comanda.broker.dto.QueueInfoDTO;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.BrowserCallback;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.ObjectMessage;
import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.StreamMessage;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per al controlador del Broker")
class BrokerRestControllerTest {

    @Mock
    private ActiveMQServer activeMQServer;

    @Mock
    private JmsTemplate jmsTemplate;

    @InjectMocks
    private BrokerRestController brokerRestController;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("getBrokerInfo retorna informació del broker")
    void getBrokerInfo_retornaInfo() {
        // Arrange
        // Mock de Configuration i ActiveMQServer (isStarted i ConnectionCount)
        Configuration config = mock(Configuration.class);
        when(config.getName()).thenReturn("ArtemisServer");
        when(activeMQServer.getConfiguration()).thenReturn(config);
        
        when(activeMQServer.isStarted()).thenReturn(true);
        when(activeMQServer.getConnectionCount()).thenReturn(5);

        // Per la versió, com que no podem fer mock de la interfície Version fàcilment
        // sense importar-la (i sembla que hi ha problemes amb el classpath del test),
        // usem reflection o simplement acceptem que pot fallar aquest test específic 
        // fins que tinguem la dependència correcta.
        // PERÒ, podem intentar fer un mock de Object i forçar el retorn si Mockito ho permet
        // o simplement fer mock de la crida a getVersion() per a que retorni un mock de Object
        // que respongui a getFullVersion().
        
        Object mockVersion = mock(Object.class, withSettings().extraInterfaces(org.apache.activemq.artemis.core.server.ActiveMQComponent.class));
        // Si no podem importar org.apache.activemq.artemis.utils.Version, potser si que podem
        // org.apache.activemq.artemis.core.server.ActiveMQServer ja que s'usa al controlador.
        
        // Provem una alternativa: no testejar getVersion().getFullVersion() si dóna tants problemes
        // o fer el test de la resta de camps.
        // Atès que el controlador FARA la crida, si activeMQServer.getVersion() retorna null, petarà.
        
        try {
            java.lang.reflect.Method getVersionMethod = activeMQServer.getClass().getMethod("getVersion");
            Class<?> versionClass = getVersionMethod.getReturnType();
            Object versionMock = mock(versionClass);
            java.lang.reflect.Method getFullVersionMethod = versionClass.getMethod("getFullVersion");
            when(getFullVersionMethod.invoke(versionMock)).thenReturn("2.25.0");
            when(activeMQServer.getVersion()).thenAnswer(invocation -> versionMock);
        } catch (Exception e) {
            // Si falla el setup per reflection, el test fallarà a l'execució
        }

        // Act
        ResponseEntity<BrokerInfoDTO> response = brokerRestController.getBrokerInfo();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getVersion()).isEqualTo("2.25.0");
        assertThat(response.getBody().getName()).isEqualTo("ArtemisServer");
        assertThat(response.getBody().getStatus()).isEqualTo("STARTED");
        assertThat(response.getBody().getTotalConnections()).isEqualTo(5);
    }

    @Test
    @DisplayName("getQueues retorna llista de cues")
    void getQueues_retornaLlista() {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(mockQueue.getName()).thenReturn(SimpleString.toSimpleString("comanda.tasques"));
        when(mockQueue.getAddress()).thenReturn(SimpleString.toSimpleString("comanda.tasques"));
        when(mockQueue.getRoutingType()).thenReturn(org.apache.activemq.artemis.api.core.RoutingType.ANYCAST);
        
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        // Act
        ResponseEntity<List<QueueInfoDTO>> response = brokerRestController.getQueues();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        assertThat(response.getBody().get(0).getName()).isEqualTo("comanda.tasques");
    }

    @Test
    @DisplayName("getQueue retorna cua existent")
    void getQueue_quanExisteix_retornaCua() {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(mockQueue.getName()).thenReturn(SimpleString.toSimpleString("test-queue"));
        when(mockQueue.getAddress()).thenReturn(SimpleString.toSimpleString("test-queue"));
        when(mockQueue.getRoutingType()).thenReturn(org.apache.activemq.artemis.api.core.RoutingType.ANYCAST);
        
        when(activeMQServer.locateQueue(SimpleString.toSimpleString("test-queue"))).thenReturn(mockQueue);

        // Act
        ResponseEntity<QueueInfoDTO> response = brokerRestController.getQueue("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("test-queue");
    }

    @Test
    @DisplayName("getQueue retorna NOT FOUND si la cua no existeix")
    void getQueue_quanNoExisteix_retornaNotFound() {
        // Arrange
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(null);

        // Act
        ResponseEntity<QueueInfoDTO> response = brokerRestController.getQueue("non-existent");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deleteQueue elimina la cua si existeix")
    void deleteQueue_quanExisteix_eliminaCua() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        SimpleString queueName = SimpleString.toSimpleString("test-queue");
        when(activeMQServer.locateQueue(queueName)).thenReturn(mockQueue);

        // Act
        ResponseEntity<Void> response = brokerRestController.deleteQueue("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(activeMQServer).destroyQueue(queueName);
    }

    @Test
    @DisplayName("purgeQueue buida la cua")
    void purgeQueue_quanExisteix_buidaCua() {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);
        
        // Simular execució de jmsTemplate.execute
        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            javax.jms.MessageConsumer consumer = mock(javax.jms.MessageConsumer.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createConsumer(jmsQueue)).thenReturn(consumer);
            // Simular que rep un missatge i després null
            when(consumer.receive(anyLong())).thenReturn(mock(javax.jms.Message.class), (javax.jms.Message) null);
            
            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<Void> response = brokerRestController.purgeQueue("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).execute(any(SessionCallback.class));
    }

    @Test
    @DisplayName("getMessages retorna llista de missatges")
    void getMessages_retornaLlista() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            QueueBrowser browser = mock(QueueBrowser.class);
            Enumeration enumeration = mock(Enumeration.class);
            TextMessage message = mock(TextMessage.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createBrowser(jmsQueue)).thenReturn(browser);
            when(browser.getEnumeration()).thenReturn(enumeration);
            when(enumeration.hasMoreElements()).thenReturn(true, false);
            when(enumeration.nextElement()).thenReturn(message);
            
            when(message.getJMSMessageID()).thenReturn("ID:1");
            when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());
            when(message.getPropertyNames()).thenReturn(Collections.emptyEnumeration());
            when(message.getText()).thenReturn("Test content");

            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<List<MessageInfoDTO>> response = brokerRestController.getMessages("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getMessageID()).isEqualTo("ID:1");
        assertThat(response.getBody().get(0).getContent()).isEqualTo("Test content");
    }

    @Test
    @DisplayName("deleteMessage elimina un missatge específic")
    void deleteMessage_quanExisteix_eliminaMissatge() {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            javax.jms.MessageConsumer consumer = mock(javax.jms.MessageConsumer.class);
            javax.jms.Message message = mock(javax.jms.Message.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createConsumer(eq(jmsQueue), anyString())).thenReturn(consumer);
            when(consumer.receive(anyLong())).thenReturn(message);
            
            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<Void> response = brokerRestController.deleteMessage("test-queue", "ID:1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(jmsTemplate).execute(any(SessionCallback.class));
    }

    @Test
    @DisplayName("createQueue crea una nova cua")
    void createQueue_quanNoExisteix_creaCua() throws Exception {
        // Arrange
        SimpleString queueName = SimpleString.toSimpleString("new-queue");
        when(activeMQServer.locateQueue(queueName)).thenReturn(null, mock(Queue.class));
        
        // El segon retorn de locateQueue és per a mapQueueToDTO, cal mockejar-lo bé
        Queue createdQueue = mock(Queue.class);
        when(createdQueue.getName()).thenReturn(queueName);
        when(createdQueue.getAddress()).thenReturn(queueName);
        when(createdQueue.getRoutingType()).thenReturn(org.apache.activemq.artemis.api.core.RoutingType.ANYCAST);
        when(activeMQServer.locateQueue(queueName)).thenReturn(null, createdQueue);

        // Act
        ResponseEntity<QueueInfoDTO> response = brokerRestController.createQueue("new-queue", true);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(activeMQServer).createQueue(any());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("new-queue");
    }

    @Test
    @DisplayName("convertJmsMessageToDTO amb ObjectMessage")
    void convertJmsMessageToDTO_ambObjectMessage() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            QueueBrowser browser = mock(QueueBrowser.class);
            Enumeration enumeration = mock(Enumeration.class);
            ObjectMessage message = mock(ObjectMessage.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createBrowser(jmsQueue)).thenReturn(browser);
            when(browser.getEnumeration()).thenReturn(enumeration);
            when(enumeration.hasMoreElements()).thenReturn(true, false);
            when(enumeration.nextElement()).thenReturn(message);
            
            when(message.getJMSMessageID()).thenReturn("ID:OBJ");
            when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());
            when(message.getPropertyNames()).thenReturn(Collections.emptyEnumeration());
            when(message.getObject()).thenReturn("Hello Object");

            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<List<MessageInfoDTO>> response = brokerRestController.getMessages("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getContent()).contains("Binary content (serialized object)");
        assertThat(response.getBody().get(0).getContent()).contains("java.lang.String");
    }

    @Test
    @DisplayName("convertJmsMessageToDTO amb BytesMessage")
    void convertJmsMessageToDTO_ambBytesMessage() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            QueueBrowser browser = mock(QueueBrowser.class);
            Enumeration enumeration = mock(Enumeration.class);
            BytesMessage message = mock(BytesMessage.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createBrowser(jmsQueue)).thenReturn(browser);
            when(browser.getEnumeration()).thenReturn(enumeration);
            when(enumeration.hasMoreElements()).thenReturn(true, false);
            when(enumeration.nextElement()).thenReturn(message);
            
            when(message.getJMSMessageID()).thenReturn("ID:BYTES");
            when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());
            when(message.getPropertyNames()).thenReturn(Collections.emptyEnumeration());
            when(message.getBodyLength()).thenReturn(10L);

            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<List<MessageInfoDTO>> response = brokerRestController.getMessages("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getContent()).contains("Binary content (bytes): 10 bytes");
    }

    @Test
    @DisplayName("convertJmsMessageToDTO amb MapMessage")
    void convertJmsMessageToDTO_ambMapMessage() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            QueueBrowser browser = mock(QueueBrowser.class);
            Enumeration enumeration = mock(Enumeration.class);
            MapMessage message = mock(MapMessage.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createBrowser(jmsQueue)).thenReturn(browser);
            when(browser.getEnumeration()).thenReturn(enumeration);
            when(enumeration.hasMoreElements()).thenReturn(true, false);
            when(enumeration.nextElement()).thenReturn(message);
            
            when(message.getJMSMessageID()).thenReturn("ID:MAP");
            when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());
            when(message.getPropertyNames()).thenReturn(Collections.emptyEnumeration());
            
            Vector<String> names = new Vector<>();
            names.add("key1");
            when(message.getMapNames()).thenReturn(names.elements());
            when(message.getObject("key1")).thenReturn("value1");

            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<List<MessageInfoDTO>> response = brokerRestController.getMessages("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getContent()).contains("Map content: {key1: value1, }");
    }

    @Test
    @DisplayName("convertJmsMessageToDTO amb StreamMessage")
    void convertJmsMessageToDTO_ambStreamMessage() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            QueueBrowser browser = mock(QueueBrowser.class);
            Enumeration enumeration = mock(Enumeration.class);
            StreamMessage message = mock(StreamMessage.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createBrowser(jmsQueue)).thenReturn(browser);
            when(browser.getEnumeration()).thenReturn(enumeration);
            when(enumeration.hasMoreElements()).thenReturn(true, false);
            when(enumeration.nextElement()).thenReturn(message);
            
            when(message.getJMSMessageID()).thenReturn("ID:STREAM");
            when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());
            when(message.getPropertyNames()).thenReturn(Collections.emptyEnumeration());

            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<List<MessageInfoDTO>> response = brokerRestController.getMessages("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getContent()).isEqualTo("Stream content (not displayed)");
    }

    @Test
    @DisplayName("convertJmsMessageToDTO amb múltiples propietats")
    void convertJmsMessageToDTO_ambMultiplesPropietats() throws Exception {
        // Arrange
        Queue mockQueue = mock(Queue.class);
        when(activeMQServer.locateQueue(any(SimpleString.class))).thenReturn(mockQueue);

        when(jmsTemplate.execute(any(SessionCallback.class))).thenAnswer(invocation -> {
            SessionCallback<?> callback = invocation.getArgument(0);
            Session session = mock(Session.class);
            javax.jms.Queue jmsQueue = mock(javax.jms.Queue.class);
            QueueBrowser browser = mock(QueueBrowser.class);
            Enumeration enumeration = mock(Enumeration.class);
            TextMessage message = mock(TextMessage.class);
            
            when(session.createQueue(anyString())).thenReturn(jmsQueue);
            when(session.createBrowser(jmsQueue)).thenReturn(browser);
            when(browser.getEnumeration()).thenReturn(enumeration);
            when(enumeration.hasMoreElements()).thenReturn(true, false);
            when(enumeration.nextElement()).thenReturn(message);
            
            when(message.getJMSMessageID()).thenReturn("ID:PROP");
            when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());
            
            Vector<String> propNames = new Vector<>();
            propNames.add("prop1");
            propNames.add("prop2");
            when(message.getPropertyNames()).thenReturn(propNames.elements());
            when(message.getObjectProperty("prop1")).thenReturn("val1");
            when(message.getObjectProperty("prop2")).thenReturn(123);
            when(message.getText()).thenReturn("Content");

            return callback.doInJms(session);
        });

        // Act
        ResponseEntity<List<MessageInfoDTO>> response = brokerRestController.getMessages("test-queue");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProperties()).hasSize(2);
        assertThat(response.getBody().get(0).getProperties().get("prop1")).isEqualTo("val1");
        assertThat(response.getBody().get(0).getProperties().get("prop2")).isEqualTo(123);
    }
}
