package es.caib.comanda.salut.back.config;

import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.salut.back.controller.SalutController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class SalutHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				SalutController.class.getPackageName()
		};
	}

}
