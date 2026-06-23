package es.caib.comanda.configuracio.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.RestTemplateHelper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Set;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_APP_CACHE;

/**
 * Lògica comuna per a consultar la informació de les apps.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AppInfoHelper {
	@Value("${" + BaseConfig.PROP_STATS_AUTH_USER + ":}")
	private String statsAuthUser;
	@Value("${" + BaseConfig.PROP_STATS_AUTH_PASSWORD + ":}")
	private String statsAuthPassword;

	@Lazy
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
	private final MonitorServiceClient monitorServiceClient;

	private static final RestTemplate restTemplate = RestTemplateHelper.createRestTemplate();
	private final CacheHelper cacheHelper;
    private final AppInfoEntornAppHelper appInfoEntornAppHelper;
    private final AppInfoIntegracionsHelper appInfoIntegracionsHelper;
	private final AppInfoSubsistemesHelper appInfoSubsistemesHelper;
	private final AppInfoContextsHelper appInfoContextsHelper;

	public void refreshAppInfo(AppInfoEntornAppProjection entornApp) {
        Long entornAppId = entornApp.getId();
        log.debug("Refrescant informació de l'entornApp {}", entornAppId);
        String appNom = entornApp.getAppNom();
        String entornNom = entornApp.getEntornNom();
        String entornAppInfoUrl = entornApp.getInfoUrl();

        MonitorApp monitorApp = new MonitorApp(
                entornAppId,
                entornAppInfoUrl,
                monitorServiceClient,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());

        try {
            // Obtenim informació de l'app
            monitorApp.startAction();

            AppInfo appInfo = fetchAppInfo(entornApp);
            // Se separa la crida fetch del guardat per a fer que la transacció a BBDD tingui la mínima durada possible
            appInfoEntornAppHelper.storeAppInfo(appInfo, entornAppId);
            if (appInfo != null) {
                try {
                    appInfoIntegracionsHelper.refreshIntegracions(entornAppId, appInfo.getIntegracions());
                } catch (Exception e) {
                    log.error("Error al actualitzar integracions de l'entornApp {}: {}",
                            entornAppId,
                            e.getLocalizedMessage());
                }
                try {
	                appInfoSubsistemesHelper.refreshSubsistemes(entornAppId, appInfo.getSubsistemes());
                } catch (Exception e) {
                    log.error("Error al actualitzar subsistemes de l'entornApp {}: {}",
                            entornAppId,
                            e.getLocalizedMessage());
                }
                try {
	                appInfoContextsHelper.refreshContexts(entornAppId, appInfo.getContexts());
                } catch (Exception e) {
                    log.error("Error al actualitzar contexts de l'entornApp {}: {}",
                            entornAppId,
                            e.getLocalizedMessage());
                }
            }

            monitorApp.endAction();
        } catch (RestClientException | MalformedURLException ex) {
            log.warn("No s'ha pogut obtenir informació de l'app {}, entorn {}: {}",
                    appNom,
                    entornNom,
                    ex.getLocalizedMessage());
            if (!monitorApp.isFinishedAction()) {
                monitorApp.endAction(ex, null);
            }
        } catch (Exception ex) {
            log.error("Error al recuperar i guardar la informació de l'app {}, entorn {}: {}",
                    appNom,
                    entornNom,
                    ex.getLocalizedMessage());
            monitorApp.endAction(ex, "Error intern de Comanda");
        } finally {
            cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entornAppId.toString());
        }
	}

    private HttpEntity<Void> buildAuthEntityIfNeeded(AppInfoEntornAppProjection entornApp) {
        if (!entornApp.isSalutAuth()) {
            return null;
        }
        if (statsAuthUser == null || statsAuthPassword == null) {
            return null;
        }
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", basicAuthHeader(statsAuthUser, statsAuthPassword));
        return new org.springframework.http.HttpEntity<>(headers);
    }

    private String basicAuthHeader(String user, String password) {
        String token = java.util.Base64.getEncoder().encodeToString((user + ":" + password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "Basic " + token;
    }

	private AppInfo fetchAppInfo(AppInfoEntornAppProjection entornApp) throws MalformedURLException {
		URI uri = buildUriOrNull(entornApp.getInfoUrl());
		if (!isValidUri(uri)) {
			throw new MalformedURLException("URL d'info invàlida o no absoluta");
		}
		HttpEntity<Void> httpEntity = buildAuthEntityIfNeeded(entornApp);
		return restTemplate.exchange(
				entornApp.getInfoUrl(),
				HttpMethod.GET,
				httpEntity,
				AppInfo.class
		).getBody();
	}

    private boolean isValidUri(URI uri) {
        return (uri != null && uri.isAbsolute());
    }

    private URI buildUriOrNull(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;
        try {
            URI uri = URI.create(trimmed);
            return uri;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Trunca un string a una mida màxima, afegint "..." al final si l'string supera la mida màxima.
     *
     * @param str       String a truncar
     * @param maxLength Longitud màxima permesa
     * @return String truncat amb "..." si era més llarg que maxLength
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

	public static Set<ConstraintViolation<Object>> validateObject(Object object) {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			Validator validator = factory.getValidator();
			return validator.validate(object);
		}
	}

    @Getter
    @AllArgsConstructor
    public static class AppInfoEntornAppProjection {
        private Long id;
        private String infoUrl;
        private boolean isSalutAuth;
        private String appNom;
        private String entornNom;
    }

}
