package es.caib.comanda.back.config;

import es.caib.comanda.ms.back.config.BaseMessageSourceConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración del MessageSource de l'aplicació.
 * 
 * @author Límit Tecnologies
 */
@Configuration
public class MessageSourceConfig extends BaseMessageSourceConfig {

	@Override
	protected String[] getBasenames() {
		return new String[] {
			getBasename(),
			"comanda.client-messages",
			"comanda.estadistica-messages",
			"comanda.configuracio-messages",
			"comanda.avisos-messages",
			"comanda.tasques-messages",
		};
	}

}
