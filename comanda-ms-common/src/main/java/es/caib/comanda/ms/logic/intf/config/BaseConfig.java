package es.caib.comanda.ms.logic.intf.config;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Propietats de configuració de l'aplicació.
 * 
 * @author Límit Tecnologies
 */
public class BaseConfig {

	public static final String APP_NAME = "comanda";
	public static final String DB_PREFIX = "com_";
	public static final String BASE_PACKAGE = "es.caib." + APP_NAME;
	public static final String PROPERTY_PREFIX = BASE_PACKAGE + ".";
	public static final String PROPERTY_PREFIX_FRONT = PROPERTY_PREFIX + "front.";

	public static final String APP_URL = PROPERTY_PREFIX + ".app.url";

	public static final String ROLE_ADMIN = "COM_ADMIN";
	public static final String ROLE_CONSULTA = "COM_CONSULTA";

	public static final String API_PATH = "/api";
	public static final String PING_PATH = "/ping";
	public static final String AUTH_TOKEN_PATH = "/authToken";
	public static final String SYSENV_PATH = "/sysenv";
	public static final String MANIFEST_PATH = "/manifest";

	public static final String DEFAULT_LOCALE = "ca";

	public static final String PROP_DEFAULT_AUDITOR = PROPERTY_PREFIX + "default.auditor";
	public static final String PROP_HTTP_HEADER_ANSWERS = PROPERTY_PREFIX + "http.header.answers";
	public static final String PROP_PERSIST_CONTAINER_TRANSACTIONS_DISABLED = PROPERTY_PREFIX + "persist.container-transactions-disabled";
	public static final String PROP_PERSIST_TRANSACTION_MANAGER_ENABLED = PROPERTY_PREFIX + "persist.transaction-manager.enabled";
	public static final String PROP_SCHEDULER_APP_INFO_CRON = PROPERTY_PREFIX + "scheduler.app.info.cron";
	public static final String PROP_SCHEDULER_SALUT_INFO_CRON = PROPERTY_PREFIX + "scheduler.salut.info.cron";
	public static final String PROP_SCHEDULER_ESTADISTIQUES_INFO_CRON = PROPERTY_PREFIX + "scheduler.estadistiqeus.info.cron";
	public static final String PROP_SCHEDULER_LEADER = PROPERTY_PREFIX + "scheduler.leader";
	public static final String PROP_HTTPAUTH_KEYCLOAK_BASE_URL = PROPERTY_PREFIX + "httpauth.keycloak.base.url";
	public static final String PROP_HTTPAUTH_KEYCLOAK_REALM = PROPERTY_PREFIX + "httpauth.keycloak.realm";
	public static final String PROP_HTTPAUTH_KEYCLOAK_CLIENT_ID = PROPERTY_PREFIX + "httpauth.keycloak.client.id";
	public static final String PROP_HTTPAUTH_USERNAME = PROPERTY_PREFIX + "httpauth.username";
	public static final String PROP_HTTPAUTH_PASSWORD = PROPERTY_PREFIX + "httpauth.password";
	public static final String PROP_SECURITY_MAPPABLE_ROLES = PROPERTY_PREFIX + "security.mappableRoles";
	public static final String PROP_SECURITY_NAME_ATTRIBUTE_KEY = PROPERTY_PREFIX + "security.nameAttributeKey";

	public static final String PROP_FRONT_API_URL = PROPERTY_PREFIX_FRONT + "api.url";
	public static final String PROP_FRONT_AUTH_PROVIDER_URL = PROPERTY_PREFIX_FRONT + "auth.provider.url";
	public static final String PROP_FRONT_AUTH_PROVIDER_REALM = PROPERTY_PREFIX_FRONT + "auth.provider.realm";
	public static final String PROP_FRONT_AUTH_PROVIDER_CLIENTID = PROPERTY_PREFIX_FRONT + "auth.provider.clientid";

	public static final Map<String, String> REACT_APP_PROPS_MAP = Map.ofEntries(
			new AbstractMap.SimpleEntry<>(PROP_FRONT_API_URL, "REACT_APP_API_URL"),
			new AbstractMap.SimpleEntry<>(PROP_FRONT_AUTH_PROVIDER_URL, "REACT_APP_AUTH_PROVIDER_URL"),
			new AbstractMap.SimpleEntry<>(PROP_FRONT_AUTH_PROVIDER_REALM, "REACT_APP_AUTH_PROVIDER_REALM"),
			new AbstractMap.SimpleEntry<>(PROP_FRONT_AUTH_PROVIDER_CLIENTID, "REACT_APP_AUTH_PROVIDER_CLIENTID"));

	public static final Map<String, String> VITE_PROPS_MAP = Map.ofEntries(
			new AbstractMap.SimpleEntry<>(PROP_FRONT_API_URL, "VITE_API_URL"),
			new AbstractMap.SimpleEntry<>(PROP_FRONT_AUTH_PROVIDER_URL, "VITE_AUTH_PROVIDER_URL"),
			new AbstractMap.SimpleEntry<>(PROP_FRONT_AUTH_PROVIDER_REALM, "VITE_AUTH_PROVIDER_REALM"),
			new AbstractMap.SimpleEntry<>(PROP_FRONT_AUTH_PROVIDER_CLIENTID, "VITE_AUTH_PROVIDER_CLIENTID"));

}
