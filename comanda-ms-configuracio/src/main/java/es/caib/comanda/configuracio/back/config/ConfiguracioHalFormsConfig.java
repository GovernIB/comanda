package es.caib.comanda.configuracio.back.config;

import es.caib.comanda.configuracio.back.controller.AppController;
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
public class ConfiguracioHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				AppController.class.getPackageName()
		};
	}

}
