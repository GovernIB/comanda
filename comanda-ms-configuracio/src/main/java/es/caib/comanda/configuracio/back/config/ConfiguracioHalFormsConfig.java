package es.caib.comanda.configuracio.back.config;

import es.caib.comanda.configuracio.back.controller.AppController;
import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Josep Gayà
 */
@Configuration
public class ConfiguracioHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				AppController.class.getPackageName()
		};
	}

}
