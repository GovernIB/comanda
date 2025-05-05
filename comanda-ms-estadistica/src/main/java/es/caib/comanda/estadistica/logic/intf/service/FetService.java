package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.Fet;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;

/**
 * Servei de consulta d'informació de estadística: Fets.
 *
 * @author Límit Tecnologies
 */
public interface FetService extends ReadonlyResourceService<Fet, Long> {

    /**
     * Obté informació estadística de totes les aplicacions actives.
     */
    void getEstadisticaInfo();

}
