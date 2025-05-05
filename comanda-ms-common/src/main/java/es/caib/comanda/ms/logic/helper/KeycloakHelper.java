package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.*;

/**
 * Helper per a la comunicació amb Keycloak.
 * 
 * @author Límit Tecnologies
 */
@Slf4j
@Component
public class KeycloakHelper {

	@Value("${" + BaseConfig.PROP_KEYCLOAK_BASE_URL + ":#{null}}")
	private String keycloakBaseUrl;
	@Value("${" + BaseConfig.PROP_KEYCLOAK_REALM + ":#{null}}")
	private String keycloakRealm;
	@Value("${" + BaseConfig.PROP_KEYCLOAK_CLIENT_ID + ":#{null}}")
	private String keycloakClientId;

	private RestTemplate restTemplate;

	public String getAccessTokenWithClientCredentials(String clientSecret) {
		if (isKeycloakConfigured()) {
			return getAccessToken(
					"client_credentials",
					null,
					null,
					clientSecret);
		} else {
			log.error("Couldn't get access token: missing keycloak configuration parameters");
			return null;
		}
	}

	public String getAccessTokenWithUsernamePassword(String username, String password) {
		if (isKeycloakConfigured()) {
			try {
				return getAccessToken(
						"password",
						username,
						password,
						null);
			} catch (Exception ex) {
				log.error("Error obtenint token de access Keycloak", ex);
				throw ex;
			}
		} else {
			log.error("Couldn't get access token: missing keycloak configuration parameters");
			return null;
		}
	}

	private boolean isKeycloakConfigured() {
		return keycloakBaseUrl != null &&
				keycloakRealm != null &&
				keycloakClientId != null;
	}

	private String getAccessToken(
			String grantType,
			String username,
			String password,
			String clientSecret) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", keycloakClientId);
		map.add("grant_type", grantType);
		if (username != null) map.add("username", username);
		if (password != null) map.add("password", password);
		if (clientSecret != null) map.add("client_secret", clientSecret);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		ResponseEntity<Map> response = getRestTemplate().postForEntity(getAuthUrl(), entity, Map.class);
		return response.getBody() != null ? (String)response.getBody().get("access_token") : null;
	}

	private String getAuthUrl() {
		return keycloakBaseUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";
	}

	private RestTemplate getRestTemplate() {
		if (restTemplate == null) {
			restTemplate = new RestTemplateBuilder().build();
		}
		return restTemplate;
	}

}
