package es.caib.comanda.permisos.back.config;

import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.permisos.back.controller.PermisController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class PermisosHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				PermisController.class.getPackageName()
		};
	}

}
