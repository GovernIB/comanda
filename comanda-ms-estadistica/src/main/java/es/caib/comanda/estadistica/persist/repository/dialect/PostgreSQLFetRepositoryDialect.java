package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementació PostgreSQL de FetRepositoryDialect.
 * Proporciona les consultes SQL específiques per a la base de dades PostgreSQL.
 */
@Component
public class PostgreSQLFetRepositoryDialect implements FetRepositoryDialect {

    /**
     * Genera la consulta SQL per obtenir dades basades en el codi d'entorn de l'aplicació (entornAppId), un rang de dates i un valor de dimensió específic.
     * La consulta limita els resultats basant-se en els paràmetres proporcionats.
     *
     * @return Una cadena que conté la consulta SQL per obtenir els resultats filtrats segons entornAppId, rang de dates i valor de dimensió.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        return "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND (f.dimensions_json->>:dimensioCodi) = :dimensioValor)";
    }

    /**
     * Genera la consulta SQL per obtenir fets basats en l'entornAppId, un rang de dates i múltiples valors de dimensions.
     * Aquesta consulta selecciona els resultats de la taula `cmd_est_fet` filtrats per condicions específiques.
     *
     * Els filtres inclouen:
     * - Identificador de l'entorn d'aplicació (entornAppId).
     * - Rang de dates utilitzant les columnes de la taula de temps associada (cmd_est_temps).
     * - Valor específic de dimensions expressat com una estructura JSON.
     *
     * @return Una cadena amb la consulta SQL per obtenir els resultats filtrats segons entornAppId, rang de dates i múltiples valors de dimensió.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery() {
        return "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND (f.dimensions_json->>:dimensioCodi) IN (:dimensioValors)";
    }

    /**
     * Genera una consulta SQL per obtenir dades basades en l'entorn d'aplicació (entornAppId), una data concreta i diverses
     * dimensions indicades (amb els seus valors associats).
     * La consulta inclou condicions bàsiques per entornAppId i la data proporcionada, i opcionalment, afegeix condicions
     * addicionals basades en dimensions específiques passades com a paràmetre.
     *
     * @param dimensionsFiltre Un mapa que representa les dimensions i els seus valors per aplicar com a filtre. Cada clau
     *                         és el codi d'una dimensió, i el valor associat és una llista que conté els valors possibles
     *                         per aquesta dimensió. Si no hi ha dimensions o valors específics, només s'apliquen les
     *                         condicions base.
     * @return Una cadena amb la consulta SQL generada amb les condicions proporcionades. Inclou sempre els filtres per
     *         entornAppId i data, i opcionalment les condicions definides per les dimensions.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        String query = "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data = :data ";

        String conditions = generateDimensionConditions(dimensionsFiltre);
        return query + conditions;
    }

    /**
     * Genera una consulta SQL per obtenir fets en base a l'identificador de l'entorn d'aplicació (entornAppId), un rang de
     * dates i els valors de diverses dimensions especificades.
     * La consulta inclou condicions específiques per filtrar les dades segons els criteris proporcionats mitjançant el mapa
     * de dimensions.
     *
     * @param dimensionsFiltre Un mapa que conté les dimensions i els seus valors de filtre. Cada clau correspon a un codi
     *                         de dimensió, mentre que el valor associat és una llista que inclou els possibles valors per
     *                         aquesta dimensió. Si el mapa és null o buit, es retorna una consulta que només inclou els
     *                         filtres d'entornAppId i el rang de dates.
     * @return Una cadena que representa la consulta SQL completa, amb els filtres per entornAppId, dates i, opcionalment,
     *         les condicions generades per les dimensions indicades.
     */
    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        String query = "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi ";

        String conditions = generateDimensionConditions(dimensionsFiltre);
        return query + conditions;
    }

    /**
     * Genera una consulta SQL per obtenir un valor agregat d'un indicador específic basat en l'entornAppId, 
     * un rang de dates específic, valors dimensionals i un tipus d'agregació.
     * Aquesta consulta aplica l'agregació directament a la base de dades, optimitzant el rendiment.
     *
     * @param dimensionsFiltre Un mapa on cada clau representa el codi d'una dimensió i el valor és una llista de valors
     *                         a filtrar. Si el mapa és null o buit, es generen només les condicions per entornAppId i rang
     *                         de dates.
     * @param indicadorCodi El codi de l'indicador sobre el qual s'aplicarà l'agregació.
     * @param agregacio El tipus d'agregació a aplicar (COUNT, SUM, AVERAGE, etc.).
     * @return Una cadena de text que representa la consulta SQL generada per obtenir el valor agregat.
     */
    @Override
    public String getValorSimpleAgregatQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        String querySelect = "";
        switch (agregacio) {
            case AVERAGE:
                querySelect = "SELECT AVG(sum_fets) as result FROM (SELECT " + getGrupping(unitatAgregacio) + "  SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) as sum_fets";
                break;
            case FIRST_SEEN:
                querySelect = "SELECT MIN(t.data) as result";
                break;
            case LAST_SEEN:
                querySelect = "SELECT MAX(t.data) as result";
                break;
            default:
                querySelect = "SELECT SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) as result";
        }

        querySelect += " FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi ";

        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryAggregationConditions = generateAggregationConditions(indicadorCodi, agregacio, unitatAgregacio);
        return querySelect + queryConditions + queryAggregationConditions + " LIMIT 1";
    }

    @Override
    public String getValorTaulaAgregatQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio) {
        return "";
    }

    /**
     * Genera les condicions dimensionals per a una consulta SQL en funció dels valors proporcionats al mapa dimensionsFiltre.
     * Per cada clau (codi de dimensió) al mapa, genera una condició SQL que verifica el valor (o valors) associats.
     * Si dimensionsFiltre és nul o buit, retorna una cadena buida.
     *
     * @param dimensionsFiltre Mapa on les claus són codis de les dimensions i els valors són llistes de valors per a cada dimensió
     * @return Cadena de text que representa les condicions SQL generades per a les dimensions específiques
     */
    private String generateDimensionConditions(Map<String, List<String>> dimensionsFiltre) {
        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) {
            return "";
        }

        return dimensionsFiltre.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> {
                    String codi = entry.getKey();
                    List<String> valors = entry.getValue();

                    if (valors.size() == 1) {
                        return "AND (f.dimensions_json->>'" + codi + "') = '" + valors.get(0) + "'";
                    } else {
                        String valorsStr = valors.stream()
                                .map(valor -> "'" + valor + "'")
                                .collect(Collectors.joining(","));
                        return "AND (f.dimensions_json->>'" + codi + "') IN (" + valorsStr + ")";
                    }
                })
                .collect(Collectors.joining(" "));
    }

    /**
     * Retorna la funció d'agregació SQL corresponent al tipus d'agregació especificat.
     *
     * @param agregacio El tipus d'agregació a aplicar.
     * @param indicadorCodi El codi de l'indicador sobre el qual s'aplicarà l'agregació.
     * @return La funció d'agregació SQL corresponent.
     */
    private String getAggregationFunction(TableColumnsEnum agregacio, String indicadorCodi, PeriodeUnitat unitatAgregacio) {
        switch (agregacio) {
            case AVERAGE:
                return "AVG(sum_fets) FROM (SELECT " + getGrupping(unitatAgregacio) + "  SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) as sum_fets";
            case FIRST_SEEN:
                return "(SELECT t2.data FROM cmd_est_fet f2 " +
                       "JOIN cmd_est_temps t2 ON f2.temps_id = t2.id " +
                       "WHERE f2.entorn_app_id = f.entorn_app_id " +
                       "ORDER BY t2.data ASC LIMIT 1)";
            case LAST_SEEN:
                return "(SELECT t2.data FROM cmd_est_fet f2 " +
                       "JOIN cmd_est_temps t2 ON f2.temps_id = t2.id " +
                       "WHERE f2.entorn_app_id = f.entorn_app_id " +
                       "ORDER BY t2.data DESC LIMIT 1)";
            case PERCENTAGE:
            case COUNT:
            case SUM:
            default:
                return "SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric)";
        }
    }

    private String generateAggregationConditions(String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {

        switch (agregacio) {
            case AVERAGE:
                return "GROUP BY " + getGrupping(unitatAgregacio) + ")";
            case FIRST_SEEN:
            case LAST_SEEN:
            case COUNT:
            case SUM:
            default:
                return "";
        }
    }

    private static String getGrupping(PeriodeUnitat unitatAgregacio) {
        String grupping = "t.data";
        if (unitatAgregacio != null) {
            switch (unitatAgregacio) {
                case SETMANA:
                    grupping = "t.anualitat, t.setmana";
                    break;
                case MES:
                    grupping = "t.anualitat, t.mes";
                    break;
                case TRIMESTRE:
                    grupping = "t.anualitat, t.trimestre";
                    break;
                case ANY:
                    grupping = "t.anualitat";
                    break;
            }
        }
        return grupping;
    }
}
