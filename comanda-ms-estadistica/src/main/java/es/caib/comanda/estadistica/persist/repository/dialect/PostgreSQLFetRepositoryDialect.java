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
    public String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {

        String querySelect = getSimpleQuerySelect(agregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(agregacio, unitatAgregacio);

        return "SELECT " + querySelect +
                " FROM ( " +
                "    SELECT " +
                (TableColumnsEnum.AVERAGE. equals(agregacio) ? "" : "t.data as data, ") +
                "        SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) AS sum_fets " +
                "    FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                queryConditions +
                queryGrouping +
                ")";
    }

    @Override
    public String getGraficUnIndicadorQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, PeriodeUnitat tempsAgregacio) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        String querySelect = getGraficQuerySelect(indicadorAgregacio);
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGraficGroupConditions(tempsAgregacio);


        return "SELECT agrupacio, " +
                "      SUM(sum_fets) AS total_sum," +
                querySelect +
                " FROM ( " +
                "    SELECT " +
                queryAgrupacio + " AS agrupacio," +
                "        SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) AS sum_fets " +
                "    FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                queryConditions +
                "GROUP BY " + queryGrouping +
                ") " +
                "ORDER BY " + queryGrouping;
    }

    @Override
    public String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGraficGroupConditions(tempsAgregacio);
        String queryDescomposicio = " JSON_VALUE(f.dimensions_json->>'" + dimensioDescomposicioCodi + "') ";


        return  "SELECT " +
                queryAgrupacio + " AS agrupacio," +
                queryDescomposicio + "AS descomposicio," +
                "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"" + indicadorCodi + "\"'))) AS sum_fets " +
                "    FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                queryConditions +
                "GROUP BY " + queryGrouping + "," + queryDescomposicio +
                "ORDER BY " + queryGrouping + ", descomposicio";
    }

    @Override
    public String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi) {

        String indicadorCodi = indicadorAgregacio.getIndicadorCodi();
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryDescomposicio = " JSON_VALUE(f.dimensions_json->>'" + dimensioDescomposicioCodi + "') ";


        return "SELECT " +
                queryDescomposicio + " AS agrupacio," +
                "        SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) AS sum_fets " +
                "    FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                queryConditions +
                "GROUP BY " + queryDescomposicio +
                "GROUP BY agrupacio";
    }

    @Override
    public String getGraficVarisIndicadorsQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio) {

        IndicadorAgregacio indicadorCodi = indicadorsAgregacio.get(0);
        boolean isAnyAverageQuery = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()));

        PeriodeUnitat avgUnitat = indicadorsAgregacio.get(0).getUnitatAgregacio();
        if (isAnyAverageQuery) {
            boolean thereAreDifferentUnitatAgregacio = indicadorsAgregacio.stream()
                    .skip(1) // Ignora el primer element
                    .anyMatch(indicador -> !indicador.getUnitatAgregacio().equals(avgUnitat));

            // Si hi ha columnes tipus AVERAGE amb diferents períodes, les separam per unitatAgregacio i fem UNION
            if (thereAreDifferentUnitatAgregacio) {
                List<List<IndicadorAgregacio>> indicadorsAgregacioByPeriode = indicadorsAgregacio.stream()
                        .collect(Collectors.groupingBy(IndicadorAgregacio::getUnitatAgregacio))
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                return indicadorsAgregacioByPeriode.stream()
                        .map(listaIndicadors -> getGraficVarisIndicadorsQuery(dimensionsFiltre, listaIndicadors, tempsAgregacio))
                        .collect(Collectors.joining(" UNION "));
            }
        }

        String querySelect = getTaulaQuerySelect(indicadorsAgregacio);
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        String subQuerySelects = getTaulaSubQuerySelects(indicadorsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGraficGroupConditions(tempsAgregacio);


        return  "SELECT agrupacio, " + querySelect +
                " FROM ( " +
                "    SELECT " +
                (isAnyAverageQuery ? "" : generateGraficGroupConditions(avgUnitat) + ", ") +
                queryAgrupacio + " AS agrupacio," +
                subQuerySelects +
                "    FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                queryConditions +
                "GROUP BY " + queryGrouping +
                ") " +
                "GROUP BY agrupacio " +
                "ORDER BY " + queryGrouping;
    }

    private String generateGraficAgrupacioConditions(PeriodeUnitat tempsAgregacio) {
        switch (tempsAgregacio) {
            case SETMANA: return "t.setmana || + '/' || t.anualitat";
            case MES: return "t.mes || + '/' || t.anualitat";
            case TRIMESTRE: return "t.trimestre || + '/' || t.anualitat";
            case ANY: return "t.anualitat";
            default: return "t.dia || '/' || t.mes || + '/' || t.anualitat";
        }
    }

    private String generateGraficGroupConditions(PeriodeUnitat tempsAgregacio) {
        switch (tempsAgregacio) {
            case SETMANA: return "t.anualitat, t.setmana";
            case MES: return "t.anualitat, t.mes";
            case TRIMESTRE: return "t.anualitat, t.trimestre";
            case ANY: return "t.anualitat";
            default: return "t.anualitat, t.mes, t.dia";
        }
    }

    @Override
    public String getTaulaQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, String dimensioAgrupacioCode) {
        boolean isAnyAverageQuery = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio()));
        boolean isAnyDataQuery = indicadorsAgregacio.stream().anyMatch(ind -> TableColumnsEnum.FIRST_SEEN.equals(ind.getAgregacio()) || TableColumnsEnum.LAST_SEEN.equals(ind.getAgregacio()));
        boolean isAverageAndDataQuery = isAnyAverageQuery && isAnyDataQuery;

        if (isAverageAndDataQuery) {
            // Dividim la consulta i fem UNION posteriorment
            List<IndicadorAgregacio> indicadorsAverage = indicadorsAgregacio.stream().filter(ind -> TableColumnsEnum.AVERAGE.equals(ind.getAgregacio())).collect(Collectors.toList());
            List<IndicadorAgregacio> indicadorsNotAverage = indicadorsAgregacio.stream().filter(ind -> !TableColumnsEnum.AVERAGE.equals(ind.getAgregacio())).collect(Collectors.toList());

            return getTaulaQuery(dimensionsFiltre, indicadorsAverage, dimensioAgrupacioCode) + " UNION " + getTaulaQuery(dimensionsFiltre, indicadorsNotAverage, dimensioAgrupacioCode);
        }

        if (isAnyAverageQuery) {
            PeriodeUnitat primeraUnitat = indicadorsAgregacio.get(0).getUnitatAgregacio();
            boolean thereAreDifferentUnitatAgregacio = indicadorsAgregacio.stream()
                    .skip(1) // Ignora el primer element
                    .anyMatch(indicador -> !indicador.getUnitatAgregacio().equals(primeraUnitat));

            // Si hi ha columnes tipus AVERAGE amb diferents períodes, les separam per unitatAgregacio i fem UNION
            if (thereAreDifferentUnitatAgregacio) {
                List<List<IndicadorAgregacio>> indicadorsAgregacioByPeriode = indicadorsAgregacio.stream()
                        .collect(Collectors.groupingBy(IndicadorAgregacio::getUnitatAgregacio))
                        .values()
                        .stream()
                        .collect(Collectors.toList());

                return indicadorsAgregacioByPeriode.stream()
                        .map(listaIndicadors -> getTaulaQuery(dimensionsFiltre, listaIndicadors, dimensioAgrupacioCode))
                        .collect(Collectors.joining(" UNION "));
            }
        }

        IndicadorAgregacio primerIndicador = indicadorsAgregacio.get(0);
        String querySelect = getTaulaQuerySelect(indicadorsAgregacio);
        String subQuerySelects = getTaulaSubQuerySelects(indicadorsAgregacio);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryGrouping = generateGroupConditions(primerIndicador.getAgregacio(), primerIndicador.getUnitatAgregacio());
        String queryAgrupacio = " JSON_VALUE(f.dimensions_json->>'" + dimensioAgrupacioCode + "') ";


        return "SELECT agrupacio, " + querySelect +
                " FROM ( " +
                "    SELECT " +
                (isAnyAverageQuery ? "t.data as data, " : "") +
                queryAgrupacio + " AS agrupacio," +
                subQuerySelects +
                "    FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                queryConditions +
                queryGrouping + "," + queryAgrupacio +
                ") " +
                "GROUP BY agrupacio";
    }

    private String getTaulaSubQuerySelects(List<IndicadorAgregacio> indicadorsAgregacio) {
        // Només volem un select per indicadorCodi
        return indicadorsAgregacio.stream()
                .map(IndicadorAgregacio::getIndicadorCodi)
                .distinct()
                .map(indicadorCodi -> "SUM((f.indicadors_json->>'" + indicadorCodi + "')::numeric) AS sum_fets " + getIndicadorSuffix(indicadorCodi))
                .collect(Collectors.joining(", "));
    }

    private String getTaulaQuerySelect(List<IndicadorAgregacio> indicadorsAgregacio) {
        return indicadorsAgregacio.stream()
                .map(ind -> getSimpleQuerySelect(ind.getAgregacio(), ind.getIndicadorCodi()) )
                .collect(Collectors.joining(","));
    }

    private String getGraficQuerySelect(IndicadorAgregacio indicadorAgregacio) {
        return getSimpleQuerySelect(indicadorAgregacio.getAgregacio(), indicadorAgregacio.getIndicadorCodi());
    }

    public String getSimpleQuerySelect(TableColumnsEnum agregacio) {
        return getSimpleQuerySelect(agregacio, null);
    }
    public String getSimpleQuerySelect(TableColumnsEnum agregacio, String indicadorCodi) {
        String suffix = getIndicadorSuffix(indicadorCodi);
        switch (agregacio) {
            case AVERAGE:
                return "AVG(sum_fets" + suffix + ") AS average_result" + suffix;
            case FIRST_SEEN:
                return "CASE WHEN SUM(sum_fets" + suffix + ") > 0 THEN MIN(data) ELSE NULL END AS first_seen" + suffix;
            case LAST_SEEN:
                return "CASE WHEN SUM(sum_fets" + suffix + ") > 0 THEN MAX(data) ELSE NULL END AS last_seen" + suffix;
            default:
                return "SUM(sum_fets" + suffix + ") AS total_sum" + suffix;
        }
    }

    private static String getIndicadorSuffix(String indicadorCodi) {
        return indicadorCodi != null ? "_" + indicadorCodi : "";
    }

    /*
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

    private String generateGroupConditions(TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {

        switch (agregacio) {
            case AVERAGE:
                return "GROUP BY " + getGrupping(unitatAgregacio) + ")";
            default:
                return "GROUP BY t.data ";
        }
    }

    private static String getGrupping(PeriodeUnitat unitatAgregacio) {
        if (unitatAgregacio != null) {
            switch (unitatAgregacio) {
                case DIA: return "t.anualitat, t.mes, t.dia";
                case SETMANA: return "t.anualitat, t.setmana";
                case MES: return "t.anualitat, t.mes";
                case TRIMESTRE: return "t.anualitat, t.trimestre";
                case ANY: return "t.anualitat";
            }
        }
        return "t.data";
    }

}
