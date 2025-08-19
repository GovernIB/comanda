package es.caib.comanda.tasques.back.config;

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

	@Override
	protected String[] getBasenames() {
		return new String[] {
				"comanda.tasques-messages",
                "comanda.client-messages",
				"comanda-messages"
		};
	}

}
