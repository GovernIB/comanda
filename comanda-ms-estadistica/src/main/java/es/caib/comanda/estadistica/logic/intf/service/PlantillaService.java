package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.paleta.Plantilla;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

/**
 * Servei que gestiona les operacions relacionades amb les plantilla d'una aplicació.
 *
 * Aquesta interfície defineix els mètodes necessaris per a realitzar consultes de lectura sobre les entitats de tipus Plantilla,
 * incloent-hi la recuperació d'una plantilla específica mitjançant el seu identificador i la cerca paginada de plantilla amb criteris.
 *
 * Plantilla representa una estructura organitzativa per a categoritzar dades dins d'un entorn d'aplicació.
 * Aquesta interfície amplia `MutableResourceService`, que proporciona funcionalitats generals per a recursos.
 *
 * L'implementació d'aquesta interfície ha d'incloure la lògica d'accés i manipulació de dades segons les regles i especificitats del domini.
 *
 * @author Límit Tecnologies
 */
public interface PlantillaService extends MutableResourceService<Plantilla, Long> {
}
