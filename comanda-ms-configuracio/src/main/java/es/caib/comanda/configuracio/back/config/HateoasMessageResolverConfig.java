package es.caib.comanda.configuracio.back.config;

import es.caib.comanda.ms.back.config.BaseHateoasMessageResolverConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració del MessageResolver per a spring-hateoas.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class HateoasMessageResolverConfig extends BaseHateoasMessageResolverConfig {

	@Override
	protected String getBasename() {
		return "comanda.configuracio-rest-messages";
	}

}
