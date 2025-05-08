package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.Dimensio;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;

/**
 * Servei que gestiona les operacions relacionades amb les dimensions d'una aplicació.
 *
 * Aquesta interfície defineix els mètodes necessaris per a realitzar consultes de lectura sobre les entitats de tipus Dimensio,
 * incloent-hi la recuperació d'una dimensió específica mitjançant el seu identificador i la cerca paginada de dimensions amb criteris.
 *
 * Dimensio representa una estructura organitzativa per a categoritzar dades dins d'un entorn d'aplicació.
 * Aquesta interfície amplia `ReadonlyResourceService`, que proporciona funcionalitats generals per a recursos només de lectura.
 *
 * L'implementació d'aquesta interfície ha d'incloure la lògica d'accés i manipulació de dades segons les regles i especificitats del domini.
 *
 * @author Límit Tecnologies
 */
public interface DimensioService extends ReadonlyResourceService<Dimensio, Long> {
}
