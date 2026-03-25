package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.ms.back.config.WebSecurityConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;


/**
 * Mètodes per a interactuar amb l'usuari autenticat.
 *
 * @author Límit Tecnologies
 */
@Component
public class AuthenticationHelper {

	/**
	 * Retorna el nom de l'usuari actual.
	 *
	 * @return el nom de l'usuari actual.
	 */
	public String getCurrentUserName() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getName();
	}

	public String[] getCurrentUserRoles() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getAuthorities().stream().
				map(GrantedAuthority::getAuthority).
				toArray(String[]::new);
	}

	
	public String[] getCurrentUserRealmRoles() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return new String[0];
		}
		Jwt jwt = extractJwt(auth);
		if (jwt != null) {
			return extractRealmRoles(jwt.getClaim("realm_access"));
		}
		if (auth.getDetails() instanceof WebSecurityConfig.PreauthWebAuthenticationDetails) {
			String[] originalRoles = ((WebSecurityConfig.PreauthWebAuthenticationDetails) auth.getDetails()).getOriginalRoles();
			return originalRoles != null ? Arrays.copyOf(originalRoles, originalRoles.length) : new String[0];
		}
		return new String[0];
	}

	private Jwt extractJwt(Authentication auth) {
		if (auth instanceof JwtAuthenticationToken) {
			return ((JwtAuthenticationToken) auth).getToken();
		}
		if (auth.getPrincipal() instanceof Jwt) {
			return (Jwt) auth.getPrincipal();
		}
		if (auth.getCredentials() instanceof Jwt) {
			return (Jwt) auth.getCredentials();
		}
		return null;
	}

	private String[] extractRealmRoles(Map<String, Object> realmAccess) {
		if (realmAccess == null || realmAccess.isEmpty()) {
			return new String[0];
		}
		Object roles = realmAccess.get("roles");
		if (!(roles instanceof Collection<?>)) {
			return new String[0];
		}
		return ((Collection<?>) roles).stream().
				filter(Objects::nonNull).
				map(String::valueOf).
				toArray(String[]::new);
	}

	/**
	 * Retorna true si l'usuari actual te el rol especificat.
	 *
	 * @param role
	 *            el rol a verificar.
	 * @return true si l'usuari actual te el rol especificat i false en cas contrari.
	 */
	public boolean isCurrentUserInRole(String role) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth.getAuthorities().stream().
				anyMatch(ga -> ga.getAuthority().equals(role));
	}

	/**
	 * Retorna true si l'usuari de l'objecte d'autenticació te el rol especificat.
	 *
	 * @param auth
	 *            l'objecte d'autenticació.
	 * @param role
	 *            el rol a verificar.
	 * @return true si l'usuari actual te el rol especificat i false en cas contrari.
	 */
	public boolean isCurrentUserInRole(Authentication auth, String role) {
		boolean isInRole = false;
		for (GrantedAuthority ga: auth.getAuthorities()) {
			if (ga != null && ga.getAuthority().equals(role)) {
				isInRole = true;
				break;
			}
		}
		return isInRole;
	}

}
