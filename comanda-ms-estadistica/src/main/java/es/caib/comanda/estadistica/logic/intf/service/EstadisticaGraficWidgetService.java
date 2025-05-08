package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.EstadisticaGraficWidget;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;

/**
 * Interface per a la gestió de recursos del tipus EstadisticaGraficWidget.
 *
 * El servei EstadisticaGraficWidgetService permet la consulta i gestió de widgets gràfics estadístics definits dins
 * d'un sistema, oferint funcionalitats per obtenir recursos individuals o en forma paginada, així com altres operacions
 * relacionades. Aquesta interfície extèn ReadonlyResourceService, el qual defineix els mètodes bàsics per a la
 * manipulació de recursos només lectura.
 *
 * Relacions:
 * - La implementació d'aquesta interfície ha d'estar alineada amb les entitats i regles locals dels recursos
 *   EstadisticaGraficWidget i la seva representació.
 *
 * Funcionalitats principals:
 * - Consulta d'un únic widget gràfic estadístic donada la seva identificació.
 * - Execució de consultes paginades per filtrar i obtenir múltiples instàncies de recursos EstadisticaGraficWidget.
 * - Suport per gestionar artefactes, formularis i informes relacionats amb aquests widgets gràfics.
 *
 * Ús:
 * S'utilitza en contextos on es requereix visualitzar, gestionar o accedir de manera controlada a widgets gràfics que
 * mostren estadístiques o indicadors segons uns criteris definits.
 *
 * @param <R> Classe del recurs EstadisticaGraficWidget.
 * @param <ID> Tipus de la clau primària (Long) associada al recurs.
 * @author Límit Tecnologies
 */
public interface EstadisticaGraficWidgetService extends ReadonlyResourceService<EstadisticaGraficWidget, Long> {
}
