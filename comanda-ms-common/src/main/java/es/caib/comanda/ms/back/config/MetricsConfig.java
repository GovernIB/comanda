package es.caib.comanda.ms.back.config;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració de les mètriques.
 *
 * @author Limit Tecnologies
 */
@Configuration
public class MetricsConfig {

	@Bean
	public SimpleMeterRegistry meterRegistry() {
		return new SimpleMeterRegistry();
	}

}
