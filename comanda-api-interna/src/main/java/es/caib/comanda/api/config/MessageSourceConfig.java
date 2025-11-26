package es.caib.comanda.api.config;

import es.caib.comanda.ms.back.config.BaseMessageSourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuración del MessageSource de l'aplicació.
 * 
 * @author Límit Tecnologies
 */
@Profile("!openapi")
@Configuration
public class MessageSourceConfig extends BaseMessageSourceConfig {

	@Override
	protected String[] getBasenames() {
		return new String[] {
			getBasename(),
			"comanda.client-messages",
			"comanda.estadistica-messages",
			"comanda.configuracio-messages",
		};
	}

}
