package es.caib.comanda.ms.persist.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configuració per a les entitats de base de dades auditables.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {

	@Value("${" + BaseConfig.PROP_DEFAULT_AUDITOR + ":unknown}")
	private String defaultAuditor;

	@Bean
	public AuditorAware<String> auditorProvider() {
		return new AuditorAware<String>() {
			@Override
			public Optional<String> getCurrentAuditor() {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null && authentication.isAuthenticated()) {
					return Optional.of(authentication.getName());
				}
				return Optional.ofNullable(defaultAuditor);
			}
		};
	}

}
