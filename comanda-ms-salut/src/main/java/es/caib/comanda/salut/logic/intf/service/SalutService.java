package es.caib.comanda.salut.logic.intf.service;

import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;
import es.caib.comanda.salut.logic.intf.model.Salut;

/**
 * Servei de consulta d'informació de salut.
 *
 * @author Límit Tecnologies
 */
public interface SalutService extends ReadonlyResourceService<Salut, Long> {

	/**
	 * Obté informació de salut de totes les aplicacions actives.
	 */
	public void getSalutInfo();

}
