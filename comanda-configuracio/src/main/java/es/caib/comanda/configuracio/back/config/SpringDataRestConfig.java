package es.caib.comanda.configuracio.back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Configuració de Spring Data REST.
 * 
 * @author Límit Tecnologies
 */
@Configuration
public class SpringDataRestConfig implements RepositoryRestConfigurer {

	@Override
	public void configureRepositoryRestConfiguration(
			RepositoryRestConfiguration config,
			CorsRegistry cors) {
		// Deshabilita l'exposició per defecte dels repositoris Spring Data
		config.disableDefaultExposure();
	}

}
