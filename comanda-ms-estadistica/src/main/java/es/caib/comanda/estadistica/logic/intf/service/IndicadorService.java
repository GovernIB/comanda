package es.caib.comanda.estadistica.logic.intf.service;

import es.caib.comanda.estadistica.logic.intf.model.estadistiques.Indicador;
import es.caib.comanda.ms.logic.intf.service.MutableResourceService;

/**
 * Interfície que defineix els serveis per gestionar recursos de tipus "Indicador".
 *
 * Aquesta interfície extén ReadonlyResourceService, proporcionant funcionalitats de consulta de recursos "Indicador" en mode
 * només lectura.
 *
 * Relació amb la classe "Indicador":
 * - Un "Indicador" representa un element amb atributs estructurats, com el codi, el nom i el format, que permet mesurar
 *   quantitativament o qualitativament diferents aspectes en un context específic.
 *
 * Objectiu:
 * - Facilitar l'accés i la gestió només lectura dels indicadors dins del sistema.
 * - Garantir la consistència i reutilització dels serveis associats als recursos basats en la interfície ReadonlyResourceService.
 *
 * Funcionalitats heretades de ReadonlyResourceService:
 * - Consultar un recurs "Indicador" específic o una llista paginada d'aquests.
 * - Obtenir artefactes als quals l'usuari té accés.
 * - Generar informes utilitzant codis i paràmetres determinats.
 *
 * Aquesta interfície es pot implementar per definir la lògica específica associada a indicadors en diferents contexts.
 *
 * @author Límit Tecnologies
 */
public interface IndicadorService extends MutableResourceService<Indicador, Long> {
}
