package es.caib.comanda.estadistica.logic.intf.model.enumerats;

/**
 * Enumeració que defineix les diferents columnes disponibles en una taula de dades estadístiques.
 *
 * Aquesta enumeració s'utilitza per identificar i categoritzar les diferents columnes que poden estar presents
 * en una taula de dades. Cada valor de l'enumeració representa un tipus específic d'informació estadística o mètrica.
 *
 * Enumerats disponibles:
 * - SUM: Representa el valor total d'una determinada mètrica.
 * - PERCENTAGE: Percentatge associat als valors d'una columna.
 * - COUNT: Nombre total de registres o elements comptabilitzats.
 * - FIRST_SEEN: Data o moment de la primera aparició d'un registre o valor.
 * - LAST_SEEN: Data o moment de la darrera aparició d'un registre o valor.
 * - AVERAGE: Promig dels valors d'una columna o conjunt estadístic.
 *
 * Objectiu: Proporcionar una llista enumerativa que permeti identificar de manera centralitzada i explícita les columnes
 * que poden aparèixer en operacions estadístiques o informes.
 *
 * @author Límit Tecnologies
 */
public enum TableColumnsEnum {
    COUNT,
    SUM,
    AVERAGE,
    PERCENTAGE,
    FIRST_SEEN,
    LAST_SEEN
}
