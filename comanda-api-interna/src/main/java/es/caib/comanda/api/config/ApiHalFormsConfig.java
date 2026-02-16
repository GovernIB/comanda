package es.caib.comanda.api.config;

import es.caib.comanda.acl.back.controller.AclEntryController;
import es.caib.comanda.avisos.back.controller.AvisController;
import es.caib.comanda.configuracio.back.controller.AppController;
import es.caib.comanda.estadistica.back.controller.FetController;
import es.caib.comanda.monitor.back.controller.MonitorController;
import es.caib.comanda.ms.back.config.BaseHalFormsConfig;
import es.caib.comanda.salut.back.controller.SalutController;
import es.caib.comanda.tasques.back.controller.TascaController;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuració de HAL-FORMS.
 * 
 * @author Límit Tecnologies
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false)
public class ApiHalFormsConfig extends BaseHalFormsConfig {

	@Override
	protected String[] getControllerPackages() {
		return new String[] {
				AppController.class.getPackageName(),
				SalutController.class.getPackageName(),
				FetController.class.getPackageName(),
				MonitorController.class.getPackageName(),
				TascaController.class.getPackageName(),
				AvisController.class.getPackageName(),
				AclEntryController.class.getPackageName(),
		};
	}

}
