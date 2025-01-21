package es.caib.comanda.configuracio.logic.intf.service;

import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

/**
 * Servei de gestió d'aplicacions.
 *
 * @author Limit Tecnologies
 */
public interface AppService extends MutableResourceService<App, Long> {

	/**
	 * Refresca la informació de totes les aplicacions actives.
	 */
	public void refreshAppInfo();

}
