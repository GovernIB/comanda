package es.caib.comanda.ms.logic.intf.config;

/**
 * Propietats de configuració de l'aplicació.
 * 
 * @author Límit Tecnologies
 */
public class BaseConfig {

	public static final String APP_NAME = "comanda";
	public static final String DB_PREFIX = "cmd_";
	public static final String BASE_PACKAGE = "es.caib." + APP_NAME;
	public static final String PROPERTY_PREFIX = BASE_PACKAGE + ".";

	public static final String APP_URL = PROPERTY_PREFIX + ".app.url";

	public static final String ROLE_ADMIN = "CMD_ADMIN";
	public static final String ROLE_CONSULTA = "CMD_CONSULTA";

	public static final String API_PATH = "/api";

	public static final String DEFAULT_LOCALE = "ca";

	public static final String PROP_DEFAULT_AUDITOR = PROPERTY_PREFIX + "default.auditor";
	public static final String PROP_HTTP_HEADER_ANSWERS = PROPERTY_PREFIX + "http.header.answers";
	public static final String PROP_PERSIST_CONTAINER_TRANSACTIONS_DISABLED = PROPERTY_PREFIX + "persist.container-transactions-disabled";
	public static final String PROP_PERSIST_TRANSACTION_MANAGER_ENABLED = PROPERTY_PREFIX + "persist.transaction-manager.enabled";
	public static final String PROP_SCHEDULER_APP_INFO_CRON = PROPERTY_PREFIX + "scheduler.app.info.cron";
	public static final String PROP_SCHEDULER_SALUT_INFO_CRON = PROPERTY_PREFIX + "scheduler.salut.info.cron";
	public static final String PROP_KEYCLOAK_BASE_URL = PROPERTY_PREFIX + "keycloak.base.url";
	public static final String PROP_KEYCLOAK_REALM = PROPERTY_PREFIX + "keycloak.realm";
	public static final String PROP_KEYCLOAK_CLIENT_ID = PROPERTY_PREFIX + "keycloak.client.id";

}
