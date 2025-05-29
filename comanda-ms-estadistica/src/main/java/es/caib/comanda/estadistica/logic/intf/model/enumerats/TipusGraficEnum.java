package es.caib.comanda.estadistica.logic.intf.model.enumerats;

/**
 * Enumeració que defineix els diferents tipus de gràfics disponibles per a la representació de dades estadístiques.
 *
 * Aquesta enumeració es pot utilitzar en contextos on sigui necessari especificar el tipus de gràfic que s'utilitzarà
 * per visualitzar dades. Cada valor representa un tipus específic de gràfic, com un gràfic de barres, línies, pastís, etc.
 *
 * Opcions disponibles:
 * - BAR_CHART: Gràfic de barres.
 * - LINE_CHART: Gràfic de línies.
 * - PIE_CHART: Gràfic de pastís.
 * - SCATTER_CHART: Gràfic de dispersió.
 * - SPARK_LINE_CHART: Gràfic de línia simple (sparkline).
 * - GAUGE_CHART: Gràfic d'indicador (gauge).
 * - HEATMAP_CHART: Gràfic de mapes de calor.
 *
 * Utilitzant aquesta enumeració, es garanteix consistència en l'ús de tipus de gràfics dins del sistema, evitant errors de codificació
 * i facilitant la gestió de lògiques associades als gràfics en el codi.
 *
 * @author Límit Tecnologies
 */
public enum TipusGraficEnum {
    BAR_CHART,
    LINE_CHART,
    PIE_CHART,
    SCATTER_CHART,
    SPARK_LINE_CHART,
    GAUGE_CHART,
    HEATMAP_CHART;
}
