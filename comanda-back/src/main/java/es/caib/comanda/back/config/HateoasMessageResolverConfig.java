package es.caib.comanda.back.config;

import es.caib.comanda.ms.back.config.BaseHateoasMessageResolverConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració del MessageResolver per a spring-hateoas.
 * 
 * @author Límit Tecnologies
 */
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
