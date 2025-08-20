package es.caib.comanda.back.config;

import es.caib.comanda.base.config.BaseConfig;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.jms.ConnectionFactory;

@Configuration
public class BackJmsClientConfig {

    @Value("${" + BaseConfig.PROP_BROKER_PORT + ":61616}")
    private int brokerPort;
    @Value("${" + BaseConfig.PROP_BROKER_USERNAME + ":jms_user}")
    private String brokerUsername;
    @Value("${" + BaseConfig.PROP_BROKER_PASSWORD + ":jms_pass}")
    private String brokerPassword;

    @Bean
    public ConnectionFactory connectionFactory() {
        String brokerUrl = "tcp://localhost:" + brokerPort;
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setUser(brokerUsername);
        connectionFactory.setPassword(brokerPassword);
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
