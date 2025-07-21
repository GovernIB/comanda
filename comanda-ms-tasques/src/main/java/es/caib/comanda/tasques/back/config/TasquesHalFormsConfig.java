package es.caib.comanda.tasques.back.config;

import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.tasques.back.controller.TascaController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class TasquesHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				TascaController.class.getPackageName()
		};
	}

}
