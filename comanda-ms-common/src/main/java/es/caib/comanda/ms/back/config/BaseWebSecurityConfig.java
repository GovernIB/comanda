package es.caib.comanda.ms.back.config;

import es.caib.comanda.base.config.BaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.j2ee.J2eePreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuració de Spring Security.
 * 
 * @author Límit Tecnologies
 */
@Slf4j
@EnableWebSecurity
@RequiredArgsConstructor
public abstract class BaseWebSecurityConfig {

	@Value("${jwt.auth.converter.principal-claim:preferred_username}")
	private String principalClaim;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		if (isWebContainerAuthActive()) {
			log.info("Web container auth active");
			http.addFilterBefore(
					webContainerProcessingFilter(),
					BasicAuthenticationFilter.class);
			http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}
		if (isOauth2ResourceServerActive()) {
			log.info("OAUTH2 resource server active");
			http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthConverter());
			http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		}
		var auth = http.authorizeHttpRequests().requestMatchers(internalPublicRequestMatchers()).permitAll();
		customHttpSecurityConfiguration(http);
		if (isAllRequestsUnauthenticatedByDefault()) {
			auth.anyRequest().permitAll();
		}
		if (isAllRequestsAuthenticatedByDefault()) {
			auth.anyRequest().authenticated();
		}
		if (isAllRequestsDeniedByDefault()) {
			auth.anyRequest().denyAll();
		}
		return http.build();
	}

	/**
	 * Configuració de seguretat personalitzada.
	 *
	 * @param http
	 *            l'objecte de configuració.
	 * @throws Exception
	 *            si es produeix alguna excepció fent la configuració.
	 */
	protected void customHttpSecurityConfiguration(HttpSecurity http) throws Exception {
		http.cors();
		http.csrf().disable();
		http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
		if (isWebContainerAuthActive()) {
			// Això és per a que funcioni correctament l'autenticació amb el provider de JBoss i les aplicacions React
			http.sessionManagement(session ->
					session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		}
	}

	/**
	 * Indica si l'autenticació de contenidor web (JBoss) està activa.
	 *
	 * @return true si està activa o false en cas contrari.
	 */
	protected boolean isWebContainerAuthActive() {
		return false;
	}
	/**
	 * Indica si l'autenticació amb servidor de recursos OAUTH (Keycloak) està activa.
	 *
	 * @return true si està activa o false en cas contrari.
	 */
	protected boolean isOauth2ResourceServerActive() {
		return true;
	}

	/**
	 * Configura el comportament per defecte de les URLs que no coinicideixen amb cap Matcher: NO requerir autenticació.
	 *
	 * @return true si no es requereix autenticació o false en cas contrari.
	 */
	protected boolean isAllRequestsUnauthenticatedByDefault() {
		return false;
	}
	/**
	 * Configura el comportament per defecte de les URLs que no coinicideixen amb cap Matcher: requerir autenticació.
	 *
	 * @return true si es requereix autenticació o false en cas contrari.
	 */
	protected boolean isAllRequestsAuthenticatedByDefault() {
		return true;
	}
	/**
	 * Configura el comportament per defecte de les URLs que no coinicideixen amb cap Matcher: denegar accés.
	 *
	 * @return true si es denega l'accés o false en cas contrari.
	 */
	protected boolean isAllRequestsDeniedByDefault() {
		return false;
	}

	/**
	 * Retorna els rols permesos per a filtrar-los. Es crida en cada petició.
	 *
	 * @return la llista de rols permesos.
	 */
	protected Set<String> getAllowedRoles() {
		return null;
	}

	/**
	 * Matchers per a les peticions internes que es configuren per a no requerir autenticació.
	 *
	 * @return la llista matchers.
	 */
	protected RequestMatcher[] internalPublicRequestMatchers() {
		return new RequestMatcher[] {
				new AntPathRequestMatcher(BaseConfig.API_PATH),
				new AntPathRequestMatcher(BaseConfig.PING_PATH),
				new AntPathRequestMatcher(BaseConfig.AUTH_TOKEN_PATH),
				new AntPathRequestMatcher(BaseConfig.SYSENV_PATH),
				new AntPathRequestMatcher(BaseConfig.MANIFEST_PATH)
		};
	}

	@Bean
	public J2eePreAuthenticatedProcessingFilter webContainerProcessingFilter() {
		J2eePreAuthenticatedProcessingFilter preAuthenticatedProcessingFilter = new J2eePreAuthenticatedProcessingFilter();
		preAuthenticatedProcessingFilter.setAuthenticationDetailsSource(getPreauthFilterAuthenticationDetailsSource());
		final List<AuthenticationProvider> providers = new ArrayList<>(1);
		PreAuthenticatedAuthenticationProvider preauthAuthProvider = new PreAuthenticatedAuthenticationProvider();
		preauthAuthProvider.setPreAuthenticatedUserDetailsService(getPreauthAuthenticationUserDetailsService());
		providers.add(preauthAuthProvider);
		preAuthenticatedProcessingFilter.setAuthenticationManager(new ProviderManager(providers));
		preAuthenticatedProcessingFilter.setContinueFilterChainOnUnsuccessfulAuthentication(false);
		return preAuthenticatedProcessingFilter;
	}

	protected AuthenticationDetailsSource<HttpServletRequest, ?> getPreauthFilterAuthenticationDetailsSource() {
		return new J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource();
	}

	protected AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> getPreauthAuthenticationUserDetailsService() {
		return new PreAuthenticatedGrantedAuthoritiesUserDetailsService();
	}

	protected Converter<Jwt, AbstractAuthenticationToken> jwtAuthConverter() {
		return jwt -> {
			Collection<GrantedAuthority> grantedAuthorities = Stream.concat(
					new JwtGrantedAuthoritiesConverter().convert(jwt).stream(),
					extractJwtGrantedAuthorities(jwt).stream()).collect(Collectors.toSet());
			filterAllowedGrantedAuthorities(grantedAuthorities);
			return new JwtAuthenticationToken(
					jwt,
					grantedAuthorities,
					getJwtPrincipalName(jwt));
		};
	}

	protected String getJwtPrincipalName(Jwt jwt) {
		String claimName = principalClaim != null ? principalClaim : JwtClaimNames.SUB;
		return jwt.getClaim(claimName);
	}

	protected Collection<? extends GrantedAuthority> extractJwtGrantedAuthorities(Jwt jwt) {
		Set<String> roles = new HashSet<>();
		// Recuperam els rols a nivell de REALM
		Map<String, Object> realmAccess = jwt.getClaim("realm_access");
		if (realmAccess != null && !realmAccess.isEmpty()) {
			List<String> realmRoles = ((List<String>)realmAccess.get("roles"));
			if (realmRoles != null && !realmRoles.isEmpty()) {
				roles.addAll(realmRoles);
			}
		}
		// Obtenim el clientId (al claim "azp")
		String clientId = jwt.getClaim("azp");
		// Recuperam els rols del client
		if (clientId != null && !clientId.isEmpty()) {
			Map<String, Object> resourceAccess = (Map<String, Object>)jwt.getClaims().get("resource_access");
			if (resourceAccess != null && !resourceAccess.isEmpty()) {
				Map<String, Object> clientAccess = (Map<String, Object>)resourceAccess.get(clientId);
				if (clientAccess != null && !clientAccess.isEmpty()) {
					List<String> clientRoles = ((List<String>)clientAccess.get("roles"));
					if (clientRoles != null && !clientRoles.isEmpty()) {
						roles.addAll(clientRoles);
					}
				}
			}
		}
		return roles.stream().
				map(SimpleGrantedAuthority::new).
				collect(Collectors.toSet());
	}

	protected void filterAllowedGrantedAuthorities(Collection<? extends GrantedAuthority> grantedAuthorities) {
		Set<String> allowedRoles = getAllowedRoles();
		if (allowedRoles != null) {
			grantedAuthorities.removeIf(a -> !allowedRoles.contains(a.getAuthority()));
		}
	}

}
