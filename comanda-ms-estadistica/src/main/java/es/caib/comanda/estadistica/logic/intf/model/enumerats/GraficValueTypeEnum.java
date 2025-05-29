package es.caib.comanda.estadistica.logic.intf.model.enumerats;

/**
 * Enum que defineix els tipus de valors disponibles per a representar dades en gràfics.
 *
 * Aquest enum categoritza les dades estadístiques en tres tipus principals:
 * - NO_MOSTRAR: Indica que el valor no es mostrarà en el gràfic.
 * - NUMERIC: Representa un valor numèric sense format addicional, idoni per a valors quantitatius.
 * - PERCENTAGE: Representa una proporció o percentatge, proporcionant un context percentual per als valors.
 *
 * Objectiu:
 * - Estandarditzar la representació de valors dins de gràfics i altres components visuals que requereixen dades estadístiques
 *   amb formats i interpretacions específiques.
 *
 * Aquest tipus s'empra dins del sistema per assegurar que els valors mostren informació clara i coherent en diferents contexts.
 *
 * @author Límit Tecnologies
 */
public enum GraficValueTypeEnum {
    NO_MOSTRAR,
    NUMERIC,
    PERCENTAGE;
}
