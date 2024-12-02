package es.caib.comanda.configuracio.logic.intf.service;

import es.caib.comanda.configuracio.logic.intf.model.App;

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

	/**
	 * Obté informació de salut de totes les aplicacions actives.
	 */
	public void getSalutInfo();

}
