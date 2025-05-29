package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

/**
 * Servei per gestionar widgets simples d'estadístiques.
 *
 * Aquesta interfície defineix les operacions disponibles per interactuar amb entitats de tipus EstadisticaSimpleWidget,
 * utilitzant funcions predeterminades de lectura i consulta avançada proporcionades mitjançant la implementació de
 * ReadonlyResourceService.
 *
 * Responsabilitats:
 * - Consultar un widget simple d'estadística segons la seva identificació única.
 * - Gestionar consultes paginades de widgets disponibles amb opcions avançades de filtratge, ordenació i perspectives.
 * - Proporcionar informació relacionada amb artifacts específics i generar informes associats.
 *
 * Ús previst:
 * Aquesta interfície està pensada per a serveis que gestionin la representació i configuració dels widgets simples
 * d'estadística de manera organitzada i eficient en aplicacions basades en dades.
 *
 * Propietats i dependències:
 * - Estén l'interfície ReadonlyResourceService, proporcionant així mecanismes generals per a la manipulació en mode
 **/
public interface EstadisticaSimpleWidgetService extends MutableResourceService<EstadisticaSimpleWidget, Long> {
}
