/**
 * 
 */
package es.caib.comanda.ms.back.config;

import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuració de les propietats de l'aplicació a partir de la configuració
 * de les propietats de sistema (System.getProperty).
 * 
 * @author Limit Tecnologies
 */
@Configuration
@PropertySource(ignoreResourceNotFound = true, value = {
	"file://${" + BaseConfig.APP_PROPERTIES + "}",
	"file://${" + BaseConfig.APP_SYSTEM_PROPERTIES + "}"})
public class SystemPropertiesConfig {

}
