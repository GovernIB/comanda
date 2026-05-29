package es.caib.comanda.base.config;

/**
 * Propietats de configuració de l'aplicació.
 *
 * @author Límit Tecnologies
 */
public class PropertyConfig {

	public static final String PROPERTY_PREFIX = BaseConfig.BASE_PACKAGE;

	public static final String PROP_BACKEND_HTTP_HEADER_ANSWERS = PROPERTY_PREFIX + ".http.header.answers";
	public static final String PROP_PERSIST_DEFAULT_AUDITOR = PROPERTY_PREFIX + ".persist.default.auditor";
	public static final String PROP_PERSIST_CONTAINER_TRANSACTIONS_DISABLED = PROPERTY_PREFIX + ".persist.container-transactions-disabled";
	public static final String PROP_PERSIST_TRANSACTION_MANAGER_ENABLED = PROPERTY_PREFIX + ".persist.transaction-manager.enabled";

}
