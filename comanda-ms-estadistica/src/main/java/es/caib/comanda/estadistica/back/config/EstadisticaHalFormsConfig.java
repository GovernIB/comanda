package es.caib.comanda.estadistica.back.config;

import es.caib.comanda.estadistica.back.controller.DimensioController;
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
public class EstadisticaHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				DimensioController.class.getPackageName()
		};
	}

}
