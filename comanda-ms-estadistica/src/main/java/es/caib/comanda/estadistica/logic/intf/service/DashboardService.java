package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

/**
 * Servei de gestió i consulta per a quadres de comandament (Dashboard).
 *
 * Aquesta interfície extén ReadonlyResourceService per proporcionar funcionalitats bàsiques de consulta en mode només lectura, com ara:
 * - Recuperació d'un quadre de comandament específic per identificador.
 * - Consultes paginades de llista de quadres de comandament.
 * - Gestió d'artefactes relacionats amb quadres de comandament (consultes, formularis i generació d'informes).
 *
 * Un quadre de comandament conté elements visuals que ofereixen informació rellevant a nivell estadístic o reportat,
 * i està estructurat per representar de manera intuïtiva dades crítiques als usuaris finals.
 *
 * Aquest servei és útil dins del context de sistemes on es requereixi gestionar la visualització de dades analítiques o resums
 * de continguts vinculats a indicadors de rendiment, mètriques o estadístiques clau.
 *
 * Les implementacions d'aquesta interfície han de respectar els requisits de només consulta establerts per l'especificació
 * base de ReadonlyResourceService.
 *
 * @author Límit Tecnologies
 */
public interface DashboardService extends MutableResourceService<Dashboard, Long> {
}
