package es.caib.comanda.alarmes.logic.intf.service;

import es.caib.comanda.alarmes.logic.intf.model.Alarma;
import es.caib.comanda.alarmes.logic.intf.model.Alarma.AlarmaReduidaResource;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

import java.util.List;

/**
 * Servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
public interface AlarmaService extends MutableResourceService<Alarma, Long> {

	/**
	 * Tasca periòdica que comprova si s'han de crear alarmes.
	 */
	void comprovacioScheduledTask();

	void enviamentsAgrupatsScheduledTask();

    List<AlarmaReduidaResource> findActiveAlarmIdsForSubscriber(String currentUser, boolean isAdmin);

}
