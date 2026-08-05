package es.caib.comanda.estadistica.logic.intf.model.dashboard;

/**
 * Tipus de filtre configurable a la capçalera d'un dashboard.
 *
 * - DIMENSIO: l'usuari selecciona un o més valors d'una dimensió (identificada per {@code dimensioCodi}).
 * - PERIODE: l'usuari selecciona un període que sobreescriu el període propi de cada widget del dashboard.
 *
 * @author Límit Tecnologies
 */
public enum DashboardFiltreTipus {
    DIMENSIO,
    PERIODE
}
