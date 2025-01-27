package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
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
 * @author Limit Tecnologies
 */
@EnableWebSecurity
public class WebSecurityConfig {

	protected final JwtAuthConverter jwtAuthConverter;

	public WebSecurityConfig(JwtAuthConverter jwtAuthConverter) {
		this.jwtAuthConverter = jwtAuthConverter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		if (jwtAuthConverter != null) {
			http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthConverter);
		}
		if (isPermitAllRequestsByDefault()) {
			http.authorizeHttpRequests().
			requestMatchers(new AntPathRequestMatcher("/sysenv")).permitAll().
			requestMatchers(new AntPathRequestMatcher("/manifest")).permitAll().
			requestMatchers(publicRequestMatchers()).permitAll().
			requestMatchers(privateRequestMatchers()).authenticated().
			anyRequest().permitAll();
		} else {
			http.authorizeHttpRequests().
			requestMatchers(new AntPathRequestMatcher("/sysenv")).permitAll().
			requestMatchers(new AntPathRequestMatcher("/manifest")).permitAll().
			requestMatchers(publicRequestMatchers()).permitAll().
			requestMatchers(privateRequestMatchers()).authenticated().
			anyRequest().denyAll();
		}
		customHttpSecurityConfiguration(http);
		return http.build();
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

	protected void customHttpSecurityConfiguration(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.csrf().disable();
		http.cors();
	}

	protected boolean isPermitAllRequestsByDefault() {
		return true;
	}

}
