package es.caib.comanda.ms.persist.config;

import es.caib.comanda.base.config.PropertyConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Optional;

/**
 * Configuració per a les entitats de base de dades auditables.
 *
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!visualizacio")
@EnableJpaAuditing
public class AuditingConfig {

	@Value("${" + PropertyConfig.PROP_PERSIST_DEFAULT_AUDITOR + ":unknown}")
	private String defaultAuditor;

	@Bean
	public AuditorAware<? extends Serializable> auditorProvider() {
		return () -> {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated()) {
				return Optional.of(authentication.getName());
			}
			return Optional.ofNullable(defaultAuditor);
		};
	}

}
