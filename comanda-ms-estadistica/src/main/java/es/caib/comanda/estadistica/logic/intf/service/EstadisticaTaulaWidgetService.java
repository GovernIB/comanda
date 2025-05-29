package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import es.caib.comanda.ms.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

/**
 * Defineix els serveis associats a la gestió dels widgets estadístics de tipus taula.
 *
 * Aquesta interfície proporciona operacions en mode només lectura per accedir i consultar
 * els widgets de tipus estadística taula, definits per la classe `EstadisticaTaulaWidget`.
 *
 * L'extensió de la interfície `ReadonlyResourceService<EstadisticaTaulaWidget, Long>` permet
 * consultes estàndard com la recuperació d'un widget per la seva identificació, l'obtenció de
 * llistes paginades i la gestió d'artefactes associats.
 *
 * Funcionalitats principals:
 * - Consulta d'un element específic pel seu identificador.
 * - Recuperació de llistes paginades d'elements segons filtres i perspectives especificats.
 * - Accés i generació d'informes o artefactes relacionats amb recursos del tipus `EstadisticaTaulaWidget`.
 *
 * Aquesta interfície pot ser implementada per serveis que proporcionen accés a dades
 * persistides de widgets estadístics de taula. Els serveis d'implementació haurien de
 * considerar les validacions, filtres i perspectives definides per a cada consulta.
 *
 * Exemple d'ús:
 * Pot ser implementada per un servei concret que utilitzi la persistència via una base de dades
 * o altra font de dades per proporcionar informació sobre widgets.
 *
 * @throws ResourceNotFoundException si l'element sol·licitat no existeix o no es troba disponible.
 * @throws ArtifactNotFoundException si l'artefacte associat no és accessible.
 * @throws ReportGenerationException si es produeix un error durant la generació d'algun informe.
 *
 * @author Límit Tecnologies
 */
public interface EstadisticaTaulaWidgetService extends MutableResourceService<EstadisticaTaulaWidget, Long> {
}
