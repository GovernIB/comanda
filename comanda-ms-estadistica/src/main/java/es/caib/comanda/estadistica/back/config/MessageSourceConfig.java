package es.caib.comanda.estadistica.back.config;

import es.caib.comanda.ms.back.config.BaseMessageSourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuración del MessageSource de l'aplicació.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class MessageSourceConfig extends BaseMessageSourceConfig {

}
