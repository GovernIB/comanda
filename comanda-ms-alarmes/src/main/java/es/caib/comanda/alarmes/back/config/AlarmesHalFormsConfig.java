package es.caib.comanda.alarmes.back.config;

import es.caib.comanda.alarmes.back.cotroller.AlarmaController;
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
public class AlarmesHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				AlarmaController.class.getPackageName()
		};
	}

}
