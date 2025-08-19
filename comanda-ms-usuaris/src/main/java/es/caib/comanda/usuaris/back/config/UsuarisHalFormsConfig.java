package es.caib.comanda.usuaris.back.config;

import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.usuaris.back.controller.UsuariController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@Profile("!back")
public class UsuarisHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				UsuariController.class.getPackageName()
		};
	}

}
