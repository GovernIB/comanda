package es.caib.comanda.broker.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import javax.annotation.PostConstruct;
import java.util.Set;

import static es.caib.comanda.ms.broker.model.Cues.*;

@Slf4j
@Configuration
public class ArtemisEmbeddedConfig {

    @Value("${es.caib.comanda.broker.port:61616}")
    private String BROKER_PORT;
    @Value("${es.caib.comanda.broker.user:jmsUser}")
    private String BROKER_USER;
    @Value("${es.caib.comanda.broker.pass:jmsPass}")
    private String BROKER_PASS;
    @Value("${es.caib.comanda.fitxers:comanda_files}")
    private String comandaFiles;

    private EmbeddedActiveMQ embedded;

    @PostConstruct
    public void start() throws Exception {

        log.info("Iniciant broker Artemis...");
        ConfigurationImpl config = new ConfigurationImpl()
                .setPersistenceEnabled(true)
                .setJournalDirectory(comandaFiles + "/broker")
                .setSecurityEnabled(true)
                .addAcceptorConfiguration("tcp", "tcp://0.0.0.0:" + BROKER_PORT);

        embedded = new EmbeddedActiveMQ();
        embedded.setConfiguration(config);
        // Assignem el SecurityManager personalitzat
        embedded.setSecurityManager(new CustomSecurityManager());

        embedded.start();

        // Defineix cues durables
        if (!embedded.getActiveMQServer().queueQuery(new SimpleString(CUA_TASQUES)).isExists()) {
            embedded.getActiveMQServer().createQueue(new QueueConfiguration(CUA_TASQUES)
                    .setAddress(CUA_TASQUES)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(true));
        }
        if (!embedded.getActiveMQServer().queueQuery(new SimpleString(CUA_TASQUES)).isExists()) {
            embedded.getActiveMQServer().createQueue(new QueueConfiguration(CUA_AVISOS)
                    .setAddress(CUA_AVISOS)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(true));
        }
        if (!embedded.getActiveMQServer().queueQuery(new SimpleString(CUA_TASQUES)).isExists()) {
            embedded.getActiveMQServer().createQueue(new QueueConfiguration(CUA_PERMISOS)
                    .setAddress(CUA_PERMISOS)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(true));
        }
        if (!embedded.getActiveMQServer().queueQuery(new SimpleString(CUA_TASQUES)).isExists()) {
            embedded.getActiveMQServer().createQueue(new QueueConfiguration(CUA_INTEGRACIONS)
                    .setAddress(CUA_INTEGRACIONS)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(true));
        }

        // Configurar AddressSettings amb TTL per defecte
        AddressSettings addressSettings = new AddressSettings()
                .setMaxSizeBytes(1048576L)          // 1MB límit
                .setMaxExpiryDelay(300000L)         // TTL per defecte: 300 segons
                .setDefaultLastValueQueue(false)    // comportament estàndard
                .setDefaultExclusiveQueue(false)    // permet múltiples consumidors
                .setDefaultNonDestructive(false)    // missatges s'esborren quan es consumeixen
                .setAutoCreateQueues(true)          // crear cues dinàmicament
                .setAutoCreateAddresses(true);      // crear adreces automàticament

        embedded.getActiveMQServer()
                .getAddressSettingsRepository()
                .addMatch("#", addressSettings);

        // Defineix rols
        Role permissiu = new Role("any", true, true, true, true, true, true, true, true);
        Set<Role> rols = Set.of(permissiu);

        // Aplica a totes les cues
        embedded.getActiveMQServer()
                .getSecurityRepository()
                .addMatch("#", rols);

    }

    @Bean
    public ActiveMQServer activeMQServer() {
        return embedded.getActiveMQServer();
    }

    @Bean
    public ActiveMQSecurityManager securityManager() {
        return new CustomSecurityManager();
    }

//    @Bean
//    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
//        JmsTemplate template = new JmsTemplate();
//        template.setConnectionFactory(connectionFactory);
//        template.setPubSubDomain(false); // cues
//        template.setDeliveryPersistent(true); // missatges persistents
//        return template;
//    }

    @Profile("!back")
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    public class CustomSecurityManager implements ActiveMQSecurityManager {

        @Override
        public boolean validateUser(String user, String password) {
            return BROKER_USER.equals(user) && BROKER_PASS.equals(password);
        }

        @Override
        public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
            return validateUser(user, password); // ignora els rols
//            if (!validateUser(user, password)) return false;
//
//            return roles.stream()
//                    .anyMatch(role -> role.getName().equals("amq") && checkType.hasRole(role));
        }

    }

}
