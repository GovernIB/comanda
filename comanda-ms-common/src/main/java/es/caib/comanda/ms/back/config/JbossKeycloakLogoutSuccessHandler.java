package es.caib.comanda.ms.back.config;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.IDToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Tanca la sessió SSO de Keycloak quan l'autenticació la fa l'adaptador Keycloak de JBoss/Undertow: redirigeix el
 * navegador a l'"end_session_endpoint" de l'IdP amb "id_token_hint", "client_id" i "post_logout_redirect_uri"
 * (RP-initiated logout; Keycloak >= 18 els necessita per tancar la sessió sense demanar confirmació).
 * <p>
 * Mateix disseny que Notib i Pinbal:
 * <ul>
 *     <li>NO es crida {@code request.logout()}: a l'adaptador d'Undertow fa un logout backchannel amb el
 *     refresh_token que mata la sessió SSO abans que hi arribi el redirect; com que no passa pel navegador, la
 *     cookie SSO de l'IdP no s'esborra i el redirect posterior rep "Session not active". En tornar a entrar,
 *     l'usuari es reautenticaria en silenci.</li>
 *     <li>L'issuer es llegeix del claim "iss" de l'id_token i el client_id del claim "azp": si es prenen de la
 *     configuració i no coincideixen amb qui ha creat la sessió, l'IdP respon "Session not active". Les propietats
 *     {@code es.caib.comanda.auth.url} i {@code es.caib.comanda.auth.realm} només són el fallback de l'issuer quan
 *     no hi ha KeycloakSecurityContext; el client_id no té fallback (com a Pinbal).</li>
 * </ul>
 *
 * @author Límit Tecnologies
 */
public class JbossKeycloakLogoutSuccessHandler implements LogoutSuccessHandler {

	private final String authUrl;
	private final String authRealm;
	private final Function<String, String> endSessionEndpointResolver;

	public JbossKeycloakLogoutSuccessHandler(String authUrl, String authRealm) {
		this(authUrl, authRealm, OidcDiscoveryHelper::getEndSessionEndpoint);
	}

	JbossKeycloakLogoutSuccessHandler(
			String authUrl,
			String authRealm,
			Function<String, String> endSessionEndpointResolver) {
		this.authUrl = authUrl;
		this.authRealm = authRealm;
		this.endSessionEndpointResolver = endSessionEndpointResolver;
	}

	@Override
	public void onLogoutSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException {
		// El KeycloakSecurityContext s'ha de llegir abans d'invalidar la sessió
		KeycloakSecurityContext keycloakSecurityContext = getKeycloakSecurityContext(request);
		IDToken idToken = keycloakSecurityContext != null ? keycloakSecurityContext.getIdToken() : null;
		String idTokenHint = keycloakSecurityContext != null ? keycloakSecurityContext.getIdTokenString() : null;
		HttpSession session = request.getSession(false);
		if (session != null) {
			try {
				session.invalidate();
			} catch (IllegalStateException ex) {
				// Ja invalidada
			}
		}
		String issuerUrl = idToken != null && idToken.getIssuer() != null ? idToken.getIssuer() : getConfiguredIssuerUrl();
		if (issuerUrl == null) {
			response.sendRedirect(request.getContextPath() + "/");
			return;
		}
		String clientId = idToken != null ? idToken.getIssuedFor() : null;
		String endSessionEndpoint = endSessionEndpointResolver.apply(issuerUrl);
		if (endSessionEndpoint == null) {
			endSessionEndpoint = issuerUrl + "/protocol/openid-connect/logout";
		}
		String postLogoutRedirectUri = getBaseUrl(request) + request.getContextPath() + "/";
		StringBuilder logoutUrl = new StringBuilder(endSessionEndpoint).
				append("?post_logout_redirect_uri=").append(URLEncoder.encode(postLogoutRedirectUri, StandardCharsets.UTF_8));
		if (idTokenHint != null) {
			logoutUrl.append("&id_token_hint=").append(URLEncoder.encode(idTokenHint, StandardCharsets.UTF_8));
		}
		if (clientId != null) {
			logoutUrl.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
		}
		response.sendRedirect(logoutUrl.toString());
	}

	private String getConfiguredIssuerUrl() {
		if (authUrl == null || authRealm == null) {
			return null;
		}
		String authUrlSenseBarra = authUrl.endsWith("/") ? authUrl.substring(0, authUrl.length() - 1) : authUrl;
		return authUrlSenseBarra + "/realms/" + authRealm;
	}

	private static KeycloakSecurityContext getKeycloakSecurityContext(HttpServletRequest request) {
		Object keycloakSecurityContext = request.getAttribute(KeycloakSecurityContext.class.getName());
		return keycloakSecurityContext instanceof KeycloakSecurityContext
				? (KeycloakSecurityContext) keycloakSecurityContext
				: null;
	}

	private static String getBaseUrl(HttpServletRequest request) {
		int port = request.getServerPort();
		String portSuffix = (port == 80 || port == 443) ? "" : ":" + port;
		return request.getScheme() + "://" + request.getServerName() + portSuffix;
	}

}
