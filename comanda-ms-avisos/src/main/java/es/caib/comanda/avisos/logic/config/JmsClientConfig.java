package es.caib.comanda.avisos.logic.config;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

@Configuration
@Profile("!back")
public class JmsClientConfig {

    @Value("${es.caib.comanda.broker.port:61616}")
    private int BROKER_PORT;
    @Value("${es.caib.comanda.broker.user:jmsUser}")
    private String BROKER_USER;
    @Value("${es.caib.comanda.broker.pass:jmsPass}")
    private String BROKER_PASS;


    @Bean
    public ConnectionFactory connectionFactory() {
        String brokerUrl = "tcp://localhost:" + BROKER_PORT;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUser(BROKER_USER);
        connectionFactory.setPassword(BROKER_PASS);
        return connectionFactory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionAcknowledgeMode(javax.jms.Session.CLIENT_ACKNOWLEDGE);
        factory.setConcurrency("5-50");
        factory.setPubSubDomain(false); // false per cues (anycast), true per temes (multicast)
        return factory;
    }

//    @Bean
//    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
//        JmsTemplate template = new JmsTemplate();
//        template.setConnectionFactory(connectionFactory);
//        template.setPubSubDomain(false); // cues
//        template.setDeliveryPersistent(true); // missatges persistents
//        return template;
//    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }
}
