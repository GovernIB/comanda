package es.caib.comanda.ms.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Helper per a la comunicació amb Keycloak.
 * 
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class HttpAuthorizationHeaderHelper {

	@Value("${" + BaseConfig.PROP_HTTPAUTH_PROVIDER_BASE_URL + ":#{null}}")
	private String providerBaseUrl;
	@Value("${" + BaseConfig.PROP_HTTPAUTH_PROVIDER_REALM + ":#{null}}")
	private String providerRealm;
	@Value("${" + BaseConfig.PROP_HTTPAUTH_PROVIDER_CLIENT_ID + ":#{null}}")
	private String providerClientId;
	@Value("${" + BaseConfig.PROP_HTTPAUTH_USERNAME + ":#{null}}")
	private String authUsername;
	@Value("${" + BaseConfig.PROP_HTTPAUTH_PASSWORD + ":#{null}}")
	private String authPassword;

	@Lazy
	private final RestTemplate restTemplate;

	public String getAuthorizationHeader() {
		if (isProviderConfigured()) {
			String accessToken = getAccessTokenWithUsernamePassword(
					authUsername,
					authPassword);
			return accessToken != null ? "Bearer " + accessToken : null;
		} else {
			String credentials = authUsername + ":" + authPassword;
			String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
			return "Basic " + encodedCredentials;
		}
	}

	private boolean isProviderConfigured() {
		return providerBaseUrl != null &&
				providerRealm != null &&
				providerClientId != null;
	}

	private String getAccessTokenWithUsernamePassword(String username, String password) {
		if (isProviderConfigured()) {
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

	private String getAccessToken(
			String grantType,
			String username,
			String password,
			String clientSecret) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", providerClientId);
		map.add("grant_type", grantType);
		if (username != null) map.add("username", username);
		if (password != null) map.add("password", password);
		if (clientSecret != null) map.add("client_secret", clientSecret);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
		ResponseEntity<Map> response = restTemplate.postForEntity(getAuthUrl(), entity, Map.class);
		return response.getBody() != null ? (String)response.getBody().get("access_token") : null;
	}

	private String getAuthUrl() {
		return providerBaseUrl + "/realms/" + providerRealm + "/protocol/openid-connect/token";
	}

}
