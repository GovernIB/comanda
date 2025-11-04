package es.caib.comanda.acl.back.config;

import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració de HAL-FORMS per al microservei ACL.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class AclHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				"es.caib.comanda.acl.back.controller"
		};
	}

}
