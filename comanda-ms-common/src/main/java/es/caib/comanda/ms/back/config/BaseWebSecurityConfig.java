package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public abstract class BaseWebSecurityConfig {

	protected final JwtAuthConverter jwtAuthConverter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		if (jwtAuthConverter != null) {
			http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthConverter);
		}
		if (isPermitAllRequestsByDefault()) {
			http.authorizeHttpRequests().
					requestMatchers(internalRequestMatchers()).permitAll().
					requestMatchers(publicRequestMatchers()).permitAll().
					requestMatchers(privateRequestMatchers()).authenticated().
					anyRequest().permitAll();
		} else {
			http.authorizeHttpRequests().
					requestMatchers(internalRequestMatchers()).permitAll().
					requestMatchers(publicRequestMatchers()).permitAll().
					requestMatchers(privateRequestMatchers()).authenticated().
					anyRequest().denyAll();
		}
		customHttpSecurityConfiguration(http);
		return http.build();
	}

	protected RequestMatcher[] internalRequestMatchers() {
		return new RequestMatcher[] {
				new AntPathRequestMatcher("/"),
				new AntPathRequestMatcher("/apidocs"),
				new AntPathRequestMatcher("/apidocs/*"),
				new AntPathRequestMatcher("/swagger-ui/*"),
				new AntPathRequestMatcher(BaseConfig.API_PATH),
				new AntPathRequestMatcher(BaseConfig.PING_PATH),
				new AntPathRequestMatcher(BaseConfig.SYSENV_PATH),
				new AntPathRequestMatcher(BaseConfig.MANIFEST_PATH)
		};
	}

	protected abstract RequestMatcher[] publicRequestMatchers();
	protected abstract RequestMatcher[] privateRequestMatchers();

	protected void customHttpSecurityConfiguration(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.csrf().disable();
		http.cors();
	}

	protected boolean isPermitAllRequestsByDefault() {
		return true;
	}

}
