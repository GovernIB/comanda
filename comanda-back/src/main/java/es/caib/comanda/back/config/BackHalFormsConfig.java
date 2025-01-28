package es.caib.comanda.back.config;

import es.caib.comanda.configuracio.back.controller.AppController;
import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.salut.back.controller.SalutController;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
public class BackHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				AppController.class.getPackageName(),
				SalutController.class.getPackageName()
		};
	}

}
