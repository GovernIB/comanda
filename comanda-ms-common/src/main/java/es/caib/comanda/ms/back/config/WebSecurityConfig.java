package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configuració de Spring Security.
 *
 * @author Límit Tecnologies
 */
@EnableWebSecurity
public class WebSecurityConfig extends BaseWebSecurityConfig {

	public WebSecurityConfig(JwtAuthConverter jwtAuthConverter) {
		super(jwtAuthConverter);
	}

	protected RequestMatcher[] publicRequestMatchers() {
		return new RequestMatcher[] {
			new AntPathRequestMatcher(BaseConfig.API_PATH + "/**/*")
		};
	}

	protected RequestMatcher[] privateRequestMatchers() {
		return new RequestMatcher[] {
			new AntPathRequestMatcher(BaseConfig.API_PATH + "/**/*")
		};
	}

	@Override
	protected boolean isWebContainerAuthActive() {
		return isJboss();
	}

	@Value("${jboss.home.dir:#{null}}")
	private String jbossHomeDir;
	private boolean isJboss() {
		return jbossHomeDir != null;
	}

}
