package es.caib.comanda.estadistica.logic.intf.model;

/**
 * Enumeració que defineix els tipus simples de dades utilitzats en el sistema.
 *
 * Aquesta enumeració proporciona una llista estàtica de tipus de valors que poden ser utilitzats
 * en diverses entitats o funcionalitats per representar dades estructurades de forma simple.
 *
 * Tipus inclosos:
 * - TEXT: Representa dades de text lliure.
 * - SENCER: Representa nombres enters.
 * - DECIMAL: Representa nombres decimals.
 * - PERCENTATGE: Representa valors en forma de percentatge.
 * - DATA: Representa dates.
 *
 * Objectiu:
 * - Garantir una definició clara i compartida dels tipus de dades simples dins del sistema.
 * - Facilitar la validació i processament dels valors associats a aquests tipus.
 *
 * Aquesta enumeració pot ser utilitzada per a configuracions, validacions o conversions relacionades amb dades simples.
 *
 * @author Límit Tecnologies
 */
public enum TipusSimpleEnum {
    TEXT,
    SENCER,
    DECIMAL,
    PERCENTATGE,
    DATA;
}
