package es.caib.comanda.ms.back.config;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.util.HttpRequestUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.j2ee.J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Configuració de Spring Security.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Configuration
public class WebSecurityConfig extends BaseWebSecurityConfig {

	private static final String ROLE_PREFIX = "";

	@Value("${" + BaseConfig.PROP_SECURITY_MAPPABLE_ROLES + ":" +
			BaseConfig.ROLE_ADMIN + "," +
			BaseConfig.ROLE_CONSULTA + "}")
	private String mappableRoles;
	@Value("${" + BaseConfig.PROP_SECURITY_ROLE_HTTP_HEADER + ":X-App-Role}")
	private String selectedRoleHttpHeader;
	@Value("${" + BaseConfig.PROP_SECURITY_NAME_ATTRIBUTE_KEY + ":preferred_username}")
	private String nameAttributeKey;

	@Override
	protected void customHttpSecurityConfiguration(HttpSecurity http) throws Exception {
		super.customHttpSecurityConfiguration(http);
		http.authorizeHttpRequests().
				requestMatchers(
						new RequestMatcher[] {
								new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/api-docs/docs"),
                                new AntPathRequestMatcher("/api-docs"),
                                new AntPathRequestMatcher("/api-docs/**"),
								new AntPathRequestMatcher("/apidocs"),
								new AntPathRequestMatcher("/apidocs/*"),
								new AntPathRequestMatcher("/swagger-ui/*")
						}
				).permitAll().
				requestMatchers(
						new AntPathRequestMatcher(BaseConfig.API_PATH + "/**/*")
				).authenticated();
		LogoutHandler logoutHandler = (request, response, authentication) -> {
			try {
				log.info("Logout called");
				Cookie[] cookies = request.getCookies();
				if (cookies != null) {
					for (Cookie cookie: cookies) {
						Cookie deletedCookie = new Cookie(cookie.getName(), "");
						deletedCookie.setPath(cookie.getPath() != null ? cookie.getPath() : "/");
						deletedCookie.setMaxAge(0);
						deletedCookie.setHttpOnly(cookie.isHttpOnly());
						deletedCookie.setSecure(cookie.getSecure());
						response.addCookie(deletedCookie);
					}
				}
				request.logout();
			} catch (ServletException ex) {
				log.error("Error en el logout", ex);
			}
		};
		http.logout(lo -> lo.addLogoutHandler(logoutHandler).
				logoutRequestMatcher(new AntPathRequestMatcher("/logout")).
				invalidateHttpSession(true).
				logoutSuccessUrl("/").
				permitAll(true));
	}

	@Override
	protected boolean isWebContainerAuthActive() {
		return isJboss();
	}
	@Override
	protected boolean isOauth2ResourceServerActive() {
		return !isJboss();
	}

	@Override
	protected Set<String> getAllowedRoles() {
		Optional<HttpServletRequest> optionalRequest = HttpRequestUtil.getCurrentHttpRequest();
		Set<String> allowedRoles = Set.of(mappableRoles.split(","));
		if (optionalRequest.isPresent()) {
			// Si la petició HTTP conté la capçalera amb el rol seleccionat retorna únicament aquest rol en la llista
			// de rols permesos.
			HttpServletRequest request = optionalRequest.get();
			String selectedRole = request.getHeader(selectedRoleHttpHeader);
			if (selectedRole != null) {
				HashSet<String> editableAllowedRoles = new HashSet<>(allowedRoles);
				editableAllowedRoles.removeIf(s -> !s.equals(selectedRole));
				return editableAllowedRoles;
			}
		}
		return allowedRoles;
	}

	@Value("${jboss.home.dir:#{null}}")
	private String jbossHomeDir;
	private boolean isJboss() {
		return jbossHomeDir != null;
	}

	@Override
	protected AuthenticationDetailsSource<HttpServletRequest, ?> getPreauthFilterAuthenticationDetailsSource() {
		J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource authenticationDetailsSource = new J2eeBasedPreAuthenticatedWebAuthenticationDetailsSource() {
			@Override
			public PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails buildDetails(HttpServletRequest context) {
				Collection<String> j2eeUserRoles = getUserRoles(context);
				log.debug("Roles from ServletRequest for {}: {}",
						context.getUserPrincipal().getName(),
						j2eeUserRoles);
				PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails result;
				if (context.getUserPrincipal() instanceof KeycloakPrincipal) {
					KeycloakPrincipal<?> keycloakPrincipal = ((KeycloakPrincipal<?>)context.getUserPrincipal());
					Set<String> roles = new HashSet<>(j2eeUserRoles);
					AccessToken.Access realmAccess = keycloakPrincipal.getKeycloakSecurityContext().getToken().getRealmAccess();
					if (realmAccess != null && realmAccess.getRoles() != null) {
						log.debug("Keycloak token realm roles: {}", realmAccess.getRoles());
						realmAccess.getRoles().stream().map(r -> ROLE_PREFIX + r).forEach(roles::add);
					}
					IDToken idToken = keycloakPrincipal.getKeycloakSecurityContext().getIdToken();
					Collection<? extends GrantedAuthority> grantedAuthorities = j2eeUserRoles2GrantedAuthoritiesMapper.
							getGrantedAuthorities(roles);
					filterAllowedGrantedAuthorities(grantedAuthorities);
					result = new PreauthWebAuthenticationDetails(
							context,
							grantedAuthorities,
							keycloakPrincipal.getKeycloakSecurityContext().getIdTokenString(),
							nameAttributeKey.equals("preferred_username") ?
									idToken.getPreferredUsername() :
									(String)idToken.getOtherClaims().get(nameAttributeKey),
							idToken.getName(),
							idToken.getEmail(),
							(String)idToken.getOtherClaims().get("nif"),
							roles.toArray(new String[0]));
				} else {
					Collection<? extends GrantedAuthority> grantedAuthorities = j2eeUserRoles2GrantedAuthoritiesMapper.
							getGrantedAuthorities(j2eeUserRoles);
					filterAllowedGrantedAuthorities(grantedAuthorities);
					result = new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(
							context,
							grantedAuthorities);
				}
				log.debug("Created WebAuthenticationDetails for {} with roles {}",
						context.getUserPrincipal().getName(),
						result.getGrantedAuthorities());
				return result;
			}
		};
		SimpleAttributes2GrantedAuthoritiesMapper attributes2GrantedAuthoritiesMapper = new SimpleAttributes2GrantedAuthoritiesMapper();
		attributes2GrantedAuthoritiesMapper.setAttributePrefix(ROLE_PREFIX);
		authenticationDetailsSource.setUserRoles2GrantedAuthoritiesMapper(attributes2GrantedAuthoritiesMapper);
		return authenticationDetailsSource;
	}

	@Getter
	public static class PreauthWebAuthenticationDetails extends PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails {
		private final String jwtToken;
		private final String preferredUsername;
		private final String name;
		private final String email;
		private final String nif;
		private final String[] originalRoles;
		public PreauthWebAuthenticationDetails(
				HttpServletRequest request,
				Collection<? extends GrantedAuthority> authorities,
				String jwtToken,
				String preferredUsername,
				String name,
				String email,
				String nif,
				String[] originalRoles) {
			super(request, authorities);
			this.jwtToken = jwtToken;
			this.preferredUsername = preferredUsername;
			this.name = name;
			this.email = email;
			this.nif = nif;
			this.originalRoles = originalRoles;
		}
	}

}
