package es.caib.comanda.salut.back.config;

import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.salut.back.controller.SalutController;
import org.springframework.context.annotation.Configuration;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Josep Gayà
 */
@Configuration
public class HalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				SalutController.class.getPackageName()
		};
	}

}
