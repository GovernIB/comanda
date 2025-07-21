package es.caib.comanda.avisos.back.config;

import es.caib.comanda.avisos.back.controller.AvisController;
import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class AvisosHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				AvisController.class.getPackageName()
		};
	}

}
