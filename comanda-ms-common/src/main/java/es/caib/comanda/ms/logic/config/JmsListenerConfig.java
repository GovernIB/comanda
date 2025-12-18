package es.caib.comanda.ms.logic.config;

import es.caib.comanda.base.config.BaseConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
public class JmsListenerConfig {

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
	    factory.setMessageConverter(jacksonJmsMessageConverter());
        factory.setSessionAcknowledgeMode(javax.jms.Session.CLIENT_ACKNOWLEDGE);
        factory.setConcurrency("5-50");
        factory.setPubSubDomain(false); // false per cues (anycast), true per temes (multicast)
        return factory;
    }

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        // Configure ObjectMapper to support Java Time (JSR-310) types like OffsetDateTime
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // use ISO-8601 strings
        converter.setObjectMapper(mapper);
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

}
