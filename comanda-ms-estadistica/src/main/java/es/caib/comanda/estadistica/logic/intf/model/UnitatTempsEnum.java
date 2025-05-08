package es.caib.comanda.estadistica.logic.intf.model;

/**
 * Enumeració que representa unitats de temps utilitzades en un context estadístic o d'anàlisi temporal.
 *
 * Les instàncies d'aquesta enumeració defineixen diferents divisions temporals que poden ser emprades en aplicacions
 * com anàlisi de dades o classificació per períodes. Les unitats inclouen:
 * - ANY: Divisió per anys.
 * - SEMESTRE: Divisió per semestres (mig any).
 * - TRIMESTRE: Divisió per trimestres (tres mesos).
 * - MES: Divisió per mesos.
 * - QUINZENA: Divisió per quinzenes (15 dies).
 * - SETMANA: Divisió per setmanes.
 * - DIA: Divisió per dies.
 * - HORA: Divisió per hores.
 *
 * Aquesta enumeració pot ser utilitzada per estructurar o categoritzar dades estadístiques segons diferents escales temporals.
 *
 * @author Límit Tecnologies
 */
public enum UnitatTempsEnum {
    ANY,
    SEMESTRE,
    TRIMESTRE,
    MES,
    QUINZENA,
    SETMANA,
    DIA,
    HORA;
}
