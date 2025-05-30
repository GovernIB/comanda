package es.caib.comanda.estadistica.logic.intf.service;


import es.caib.comanda.estadistica.logic.intf.model.estadistiques.DimensioValor;
import es.caib.comanda.ms.logic.intf.service.ReadonlyResourceService;

/**
 * Servei que gestiona les operacions relacionades amb els valors de dimensions.
 *
 * Aquesta interfície defineix els mètodes necessaris per a realitzar consultes de lectura sobre les entitats de tipus DimensioValor,
 * que representen valors específics associats a una dimensió concreta dins del sistema.
 *
 * Un DimensioValor vincula un text descriptiu amb una Dimensio, permetent categoritzar o agrupar dades segons estructures definides.
 * Aquesta interfície amplia ReadonlyResourceService, que proporciona funcionalitats generals per a recursos només de lectura.
 *
 * Les implementacions d’aquest servei han de garantir una gestió eficient i coherent dels valors dins del context del model de dades.
 *
 * Relació:
 * - Cada DimensioValor està enllaçat a una única Dimensio mitjançant una referència.
 *
 * @author Límit Tecnologies
 */
public interface DimensioValorService extends ReadonlyResourceService<DimensioValor, Long> {
}
