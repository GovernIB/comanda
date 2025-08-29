package es.caib.comanda.broker.config;

import es.caib.comanda.base.config.BaseConfig;
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

import javax.annotation.PostConstruct;
import java.util.Set;

import static es.caib.comanda.ms.broker.model.Cues.*;

@Slf4j
@Configuration
public class ArtemisEmbeddedConfig {

    @Value("${" + BaseConfig.PROP_BROKER_PORT + ":61616}")
    private String brokerPort;
    @Value("${" + BaseConfig.PROP_BROKER_USERNAME + ":jms_user}")
    private String brokerUsername;
    @Value("${" + BaseConfig.PROP_BROKER_PASSWORD + ":jms_pass}")
    private String brokerPassword;
    @Value("${" + BaseConfig.PROP_FILES + "}")
    private String propFiles;

    private EmbeddedActiveMQ embedded;

    @PostConstruct
    public void start() throws Exception {
        log.info("Iniciant broker Artemis...");
        ConfigurationImpl config = new ConfigurationImpl()
                .setPersistenceEnabled(true)
                .setJournalDirectory(propFiles + "/broker/journal")
                .setBindingsDirectory(propFiles + "/broker/bindings")
                .setLargeMessagesDirectory(propFiles + "/broker/large-messages")
                .setPagingDirectory(propFiles + "/broker/paging")
                .setSecurityEnabled(true)
                .addAcceptorConfiguration("tcp", "tcp://0.0.0.0:" + brokerPort);
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
        if (!embedded.getActiveMQServer().queueQuery(new SimpleString(CUA_AVISOS)).isExists()) {
            embedded.getActiveMQServer().createQueue(new QueueConfiguration(CUA_AVISOS)
                    .setAddress(CUA_AVISOS)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(true));
        }
        if (!embedded.getActiveMQServer().queueQuery(new SimpleString(CUA_PERMISOS)).isExists()) {
            embedded.getActiveMQServer().createQueue(new QueueConfiguration(CUA_PERMISOS)
                    .setAddress(CUA_PERMISOS)
                    .setRoutingType(RoutingType.ANYCAST)
                    .setDurable(true));
        }
        // Configurar AddressSettings amb TTL per defecte
        AddressSettings addressSettings = new AddressSettings()
                .setMaxSizeBytes(1048576L)          // 1MB límit
                .setPageSizeBytes(512000)    // 500 KB per pàgina
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

    public class CustomSecurityManager implements ActiveMQSecurityManager {
        @Override
        public boolean validateUser(String user, String password) {
            return brokerUsername.equals(user) && brokerPassword.equals(password);
        }
        @Override
        public boolean validateUserAndRole(String user, String password, Set<Role> roles, CheckType checkType) {
            return validateUser(user, password); // ignora els rols
//            return roles.stream()
//                    .anyMatch(role -> role.getName().equals("amq") && checkType.hasRole(role));
        }
    }

}
