package es.caib.comanda.api.config;

import es.caib.comanda.ms.back.config.BaseHateoasMessageResolverConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració del MessageResolver per a spring-hateoas.
 * 
 * @author Límit Tecnologies
 */
@Profile("!openapi")
@Configuration
public class HateoasMessageResolverConfig extends BaseHateoasMessageResolverConfig {

	@Override
	protected String[] getBasenames() {
		return new String[] {
			"comanda.configuracio-rest-messages",
			"comanda.estadistica-rest-messages",
			"comanda.monitor-rest-messages",
		};
	}

}
