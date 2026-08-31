package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorFormulaTermeResolt;
import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatFiltreSql;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació d'Oracle del FetRepositoryDialect.
 * Proveeix consultes SQL específiques per a la base de dades Oracle.
 * Gestiona la construcció de consultes per obtenir estadístiques i fets amb diferents nivells d'agrupació i filtres dimensionals.
 */
@Component
public class OracleFetRepositoryDialect implements FetRepositoryDialect {

    private static final String BASE_JOIN = " FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id ";
    private static final String BASE_WHERE_ENTORN = " WHERE f.entorn_app_id = :entornAppId ";
    private static final String FILTER_BETWEEN = " AND t.data BETWEEN :dataInici AND :dataFi ";
    private static final String FILTER_DATE = " AND t.data = :data ";
    private static final String BASE_WHERE = BASE_WHERE_ENTORN + FILTER_BETWEEN;
    private static final String SUM_INDICADOR_TEMPLATE = " SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"%s\"'))) AS sum_fets";
    private static final String DIMENSION_VALUE_TEMPLATE = " JSON_VALUE(f.dimensions_json, '$.\"%s\"') ";
    private static final String INDICADOR_VALUE_EXPR_TEMPLATE = "TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"%s\"'))";

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        return "SELECT f.*" + BASE_JOIN + BASE_WHERE + " AND " + getDimensionValueQuery("' || :dimensioCodi || '") + "= :dimensioValor";
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery() {
        return "SELECT f.* " + BASE_JOIN + BASE_WHERE + " AND " + getDimensionValueQuery("' || :dimensioCodi || '") + " IN (:dimensioValor)";
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        return "SELECT f.* " + BASE_JOIN + BASE_WHERE_ENTORN + FILTER_DATE + generateDimensionConditions(dimensionsFiltre);
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        return "SELECT f.* " + BASE_JOIN + BASE_WHERE + generateDimensionConditions(dimensionsFiltre);
    }

    @Override
    public String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio,
                                 PeriodeUnitat unitatAgregacio, SeguretatFiltreSql seguretat) {
        // 1. Filtres
        String queryConditions = generateDimensionConditions(dimensionsFiltre) + generateSecurityCondition(seguretat);

        // Resolem la unitat efectiva (FIRST_SEEN/LAST_SEEN sempre operen a nivell de DIA)
        PeriodeUnitat effectiveUnitat = (agregacio == TableColumnsEnum.FIRST_SEEN || agregacio == TableColumnsEnum.LAST_SEEN)
            ? PeriodeUnitat.DIA
            : unitatAgregacio;

        // 2. Agrupació interna
        String innerGroupingCols = getInnerGroupingColsForSingle(agregacio, effectiveUnitat);
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "" : " GROUP BY " + innerGroupingCols;

        // 3. Càlculs (utilitzem effectiveUnitat per al sufix)
        String querySelect = getSimpleQuerySelect(agregacio, indicadorCodi, effectiveUnitat);
        String innerSumSelect = getSumIndicadorQuery(indicadorCodi) + getIndicadorSuffix(indicadorCodi, effectiveUnitat);

        // 4. Assemblatge
        return "SELECT " + querySelect + " FROM ( SELECT " + innerSelectCols + innerSumSelect +
            BASE_JOIN + BASE_WHERE + queryConditions + innerGroupBy + ")";
    }

    @Override
    public String getGraficUnIndicadorQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio,
              PeriodeUnitat tempsAgregacio, SeguretatFiltreSql seguretat) {
        String queryConditions = generateDimensionConditions(dimensionsFiltre) + generateSecurityCondition(seguretat);
        String innerGroupingCols = getChartInnerGroupingCols(indicadorAgregacio, tempsAgregacio);
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "" : " GROUP BY " + innerGroupingCols;

        String outerGroupingCols = getTimeGroupingColumns(tempsAgregacio, true).replace("t.", "");
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);

        PeriodeUnitat effectiveUnitat = resolveUnitat(indicadorAgregacio, tempsAgregacio);
        TableColumnsEnum realAgregacio = getRealGraficAgregacio(indicadorAgregacio.getAgregacio(), indicadorAgregacio.getUnitatAgregacio(), tempsAgregacio);
        String innerSumSelect = getSumIndicadorQuery(indicadorAgregacio) + getIndicadorSuffix(indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);
        String outerSelect = getSimpleQuerySelect(realAgregacio, indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);

        if (TableColumnsEnum.FIRST_SEEN.equals(indicadorAgregacio.getAgregacio()) ||
            TableColumnsEnum.LAST_SEEN.equals(indicadorAgregacio.getAgregacio())) {

            return "SELECT " + queryAgrupacio + " AS agrupacio, " + outerSelect +
                " FROM ( SELECT " + innerSelectCols + innerSumSelect +
                BASE_JOIN + BASE_WHERE + queryConditions + innerGroupBy + ") " +
                " GROUP BY " + outerGroupingCols + " ORDER BY agrupacio";
        }

        PeriodeUnitat innerUnitat = getChartInnerUnitat(indicadorAgregacio, tempsAgregacio);
        String periodeCamps = innerGroupingCols.replace("t.", "");
        String suffix = getIndicadorSuffix(indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);

        return "SELECT " + queryAgrupacio + " AS agrupacio, " + outerSelect +
            " FROM ( SELECT " +
            qualifyColumns("periodes", periodeCamps) + ", " +
            "COALESCE(agg.sum_fets" + suffix + ", 0) AS sum_fets" + suffix + " " +
            " FROM " + getPeriodesRangeQuery(innerUnitat) + " periodes " +
            " LEFT JOIN ( SELECT " + innerGroupingCols + ", " + innerSumSelect +
            BASE_JOIN + BASE_WHERE + queryConditions + innerGroupBy +
            " ) agg ON " + getPeriodesJoinCondition("periodes", "agg", periodeCamps) +
            ") " +
            " GROUP BY " + outerGroupingCols + " ORDER BY agrupacio";
    }

    @Override
    public String getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio, SeguretatFiltreSql seguretat) {
        // 1. Filtres i agrupacions base
        String queryConditions = generateDimensionConditions(dimensionsFiltre) + generateSecurityCondition(seguretat);
        String queryDescomposicio = getDimensionValueQuery(dimensioDescomposicioCodi);

        // 2. Estructura d'agrupació
        String innerGroupingCols = getChartInnerGroupingCols(indicadorAgregacio, tempsAgregacio);
        String innerGroupBy = innerGroupingCols.isEmpty() ? " GROUP BY " + queryDescomposicio : " GROUP BY " + innerGroupingCols + ", " + queryDescomposicio;
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";

        String outerGroupingCols = getTimeGroupingColumns(tempsAgregacio, true).replace("t.", "");
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);

        // 3. Càlculs
        PeriodeUnitat effectiveUnitat = resolveUnitat(indicadorAgregacio, tempsAgregacio);
        TableColumnsEnum realAgregacio = getRealGraficAgregacio(indicadorAgregacio.getAgregacio(), indicadorAgregacio.getUnitatAgregacio(), tempsAgregacio);

        String innerSumSelect = getSumIndicadorQuery(indicadorAgregacio) + getIndicadorSuffix(indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);
        String outerSelect = getSimpleQuerySelect(realAgregacio, indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);

        // 4. Assemblatge
        return "SELECT " + queryAgrupacio + " AS agrupacio, descomposicio, " + outerSelect +
            " FROM ( SELECT " + innerSelectCols + queryDescomposicio + " AS descomposicio, " + innerSumSelect +
            BASE_JOIN + BASE_WHERE + queryConditions + innerGroupBy + ") " +
            " GROUP BY " + outerGroupingCols + ", descomposicio ORDER BY agrupacio, descomposicio";
    }

    @Override
    public String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio,
              String dimensioDescomposicioCodi, SeguretatFiltreSql seguretat) {
        // 1. Filtres i dimensions
        String queryConditions = generateDimensionConditions(dimensionsFiltre) + generateSecurityCondition(seguretat);
        String queryDescomposicio = getDimensionValueQuery(dimensioDescomposicioCodi);

        // 2. Assemblatge (consulta plana, sense subconsulta)
        return "SELECT " + queryDescomposicio + " AS agrupacio, " + getSumIndicadorQuery(indicadorAgregacio) +
            BASE_JOIN + BASE_WHERE + queryConditions + " GROUP BY " + queryDescomposicio + " ORDER BY agrupacio";
    }

    @Override
    public String getGraficVarisIndicadorsQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio,
              PeriodeUnitat tempsAgregacio, SeguretatFiltreSql seguretat) {
        Map<String, List<IndicadorAgregacio>> indicadorsPerGrup = indicadorsAgregacio.stream()
            .collect(Collectors.groupingBy(ind -> getChartGroupingKey(ind, tempsAgregacio)));
        String fullQueryConditions = generateDimensionConditions(dimensionsFiltre) + generateSecurityCondition(seguretat);

        if (indicadorsPerGrup.size() == 1) {
            return generateGraficSingleGroupQuery(fullQueryConditions, indicadorsPerGrup.values().iterator().next(), tempsAgregacio);
        }

        String unionSubqueries = indicadorsPerGrup.values().stream()
            .map(grup -> generateGraficUnionSubquery(fullQueryConditions, grup, tempsAgregacio, indicadorsAgregacio))
            .collect(Collectors.joining(" UNION ALL "));

        return generateGraficUnionQuery(indicadorsAgregacio, unionSubqueries, tempsAgregacio);
    }

    @Override
    public String getTaulaQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio,
              String dimensioAgrupacioCodi, SeguretatFiltreSql seguretat) {
        Map<String, List<IndicadorAgregacio>> indicadorsPerGrup = indicadorsAgregacio.stream()
            .collect(Collectors.groupingBy(this::getInnerGroupingCols));
        String fullQueryConditions = generateDimensionConditions(dimensionsFiltre) + generateSecurityCondition(seguretat);

        if (indicadorsPerGrup.size() == 1) {
            return generateTaulaQuerySingleGroup(fullQueryConditions, indicadorsPerGrup.values().iterator().next(), dimensioAgrupacioCodi);
        }

        String unionSubqueries = indicadorsPerGrup.values().stream()
            .map(grup -> generateUnionSubquery(fullQueryConditions, grup, dimensioAgrupacioCodi, indicadorsAgregacio))
            .collect(Collectors.joining(" UNION ALL "));

        return generaUnionQuery(indicadorsAgregacio, unionSubqueries);
    }

    // ====================================================================================
    // UTILS DE AGRUPACIÓ I TEMPS
    // ====================================================================================

    private static PeriodeUnitat resolveUnitat(IndicadorAgregacio ind, PeriodeUnitat defaultUnitat) {
        if (ind.getAgregacio() == TableColumnsEnum.FIRST_SEEN || ind.getAgregacio() == TableColumnsEnum.LAST_SEEN) {
            return PeriodeUnitat.DIA;
        }
        return ind.getUnitatAgregacio() != null ? ind.getUnitatAgregacio() : defaultUnitat;
    }

    private String getTimeGroupingColumns(PeriodeUnitat unitat, boolean fallbackToDaily) {
        PeriodeUnitat u = unitat != null ? unitat : (fallbackToDaily ? PeriodeUnitat.DIA : null);
        if (u == null) return "";

        switch (u) {
            case DIA: return "t.anualitat, t.trimestre, t.mes, t.setmana, t.dia";
            case SETMANA: return "t.anualitat, t.trimestre, t.mes, t.setmana";
            case MES: return "t.anualitat, t.trimestre, t.mes";
            case TRIMESTRE: return "t.anualitat, t.trimestre";
            case ANY: return "t.anualitat";
            default: return "t.anualitat, t.trimestre, t.mes, t.setmana, t.dia";
        }
    }

    private String getInnerGroupingCols(IndicadorAgregacio ind) {
        if (ind.getAgregacio() == TableColumnsEnum.FIRST_SEEN || ind.getAgregacio() == TableColumnsEnum.LAST_SEEN) {
            return "t.data";
        }
        return getTimeGroupingColumns(ind.getUnitatAgregacio(), false);
    }

    private String getInnerGroupingColsForSingle(TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        if (agregacio == TableColumnsEnum.FIRST_SEEN || agregacio == TableColumnsEnum.LAST_SEEN || unitatAgregacio == PeriodeUnitat.DIA) {
            return "t.data";
        }
        return getTimeGroupingColumns(unitatAgregacio, false);
    }

    private String getChartInnerGroupingCols(IndicadorAgregacio ind, PeriodeUnitat defaultTemps) {
        return getTimeGroupingColumns(getChartInnerUnitat(ind, defaultTemps), false);
    }

    private PeriodeUnitat getChartInnerUnitat(IndicadorAgregacio ind, PeriodeUnitat defaultTemps) {
        PeriodeUnitat effectiveUnitat = resolveUnitat(ind, defaultTemps);
        PeriodeUnitat innerUnitat = defaultTemps;

        if (effectiveUnitat != null && getPeriodLevel(effectiveUnitat) < getPeriodLevel(defaultTemps)) {
            innerUnitat = effectiveUnitat;
        }

        return innerUnitat;
    }

    private String getChartGroupingKey(IndicadorAgregacio ind, PeriodeUnitat defaultTemps) {
        if (ind.getAgregacio() == TableColumnsEnum.FIRST_SEEN || ind.getAgregacio() == TableColumnsEnum.LAST_SEEN) {
            return "t.data";
        }
        PeriodeUnitat unitat = resolveUnitat(ind, defaultTemps);
        return getTimeGroupingColumns(unitat, false);
    }

    // ====================================================================================
    // GENERACIÓ DE CONSULTES DE TAULA (UNION)
    // ====================================================================================

    private String generateTaulaQuerySingleGroup(String queryConditions, List<IndicadorAgregacio> indicadors, String dimensioAgrupacioCodi) {
        // 1. Estructura base
        String queryAgrupacio = getDimensionValueQuery(dimensioAgrupacioCodi);
        PeriodeUnitat groupUnitat = resolveUnitat(indicadors.get(0), null);

        // 2. Agrupació interna
        String innerGroupingCols = getInnerGroupingCols(indicadors.get(0));
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "GROUP BY " + queryAgrupacio : "GROUP BY " + innerGroupingCols + ", " + queryAgrupacio;

        // 3. Càlculs
        String querySelect = indicadors.stream().map(ind -> getSimpleQuerySelect(ind.getAgregacio(), ind.getIndicadorCodi(), groupUnitat)).collect(Collectors.joining(", "));
        String subQuerySelects = getTaulaSubQuerySelects(indicadors, groupUnitat);

        // 4. Assemblatge
        return "SELECT agrupacio, " + querySelect + " FROM ( SELECT " + innerSelectCols + queryAgrupacio + " AS agrupacio, " + subQuerySelects +
            BASE_JOIN + BASE_WHERE + queryConditions + " " + innerGroupBy + ") GROUP BY agrupacio ORDER BY agrupacio";
    }

    private String generateUnionSubquery(String queryConditions, List<IndicadorAgregacio> indicadors, String dimensioAgrupacioCodi, List<IndicadorAgregacio> ordenOriginal) {
        if (indicadors.isEmpty()) return "";

        // 1. Estructura base
        String queryAgrupacio = getDimensionValueQuery(dimensioAgrupacioCodi);
        PeriodeUnitat groupUnitat = resolveUnitat(indicadors.get(0), null);

        // 2. Agrupació interna
        String innerGroupingCols = getInnerGroupingCols(indicadors.get(0));
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "GROUP BY " + queryAgrupacio : "GROUP BY " + innerGroupingCols + ", " + queryAgrupacio;

        // 3. Càlculs
        String querySelect = ordenOriginal.stream().map(ind -> {
            if (indicadors.contains(ind)) {
                return getSimpleQuerySelect(ind.getAgregacio(), ind.getIndicadorCodi(), groupUnitat);
            }
            String suffix = getIndicadorSuffix(ind.getIndicadorCodi(), resolveUnitat(ind, null));
            if (ind.getAgregacio() == TableColumnsEnum.AVERAGE) return "null AS average_result" + suffix;
            if (ind.getAgregacio() == TableColumnsEnum.FIRST_SEEN) return "null AS first_seen" + suffix;
            if (ind.getAgregacio() == TableColumnsEnum.LAST_SEEN) return "null AS last_seen" + suffix;
            return "null AS total_sum" + suffix;
        }).collect(Collectors.joining(", "));

        String subQuerySelects = getTaulaSubQuerySelects(indicadors, groupUnitat);

        // 4. Assemblatge
        return "SELECT agrupacio, " + querySelect + " FROM ( SELECT " + innerSelectCols + queryAgrupacio + " AS agrupacio, " + subQuerySelects +
            BASE_JOIN + BASE_WHERE + queryConditions + " " + innerGroupBy + ") GROUP BY agrupacio";
    }

    private static String generaUnionQuery(List<IndicadorAgregacio> allIndicadors, String unionSubqueries) {
        // 1. Càlculs de columnes externes
        Set<String> allResultColumns = allIndicadors.stream().map(ind -> {
            String suffix = getIndicadorSuffix(ind.getIndicadorCodi(), resolveUnitat(ind, null));
            switch (ind.getAgregacio()) {
                case AVERAGE: return "average_result" + suffix;
                case FIRST_SEEN: return "first_seen" + suffix;
                case LAST_SEEN: return "last_seen" + suffix;
                default: return "total_sum" + suffix;
            }
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        String outerSelect = allResultColumns.stream().map(col -> "MAX(" + col + ") as " + col).collect(Collectors.joining(", "));

        // 2. Assemblatge
        return "SELECT agrupacio, " + outerSelect + " FROM (" + unionSubqueries + ") GROUP BY agrupacio ORDER BY agrupacio";
    }

    // ====================================================================================
    // GENERACIÓ DE CONSULTES DE GRÀFIC (UNION)
    // ====================================================================================

    private String generateGraficSingleGroupQuery(String queryConditions, List<IndicadorAgregacio> indicadors, PeriodeUnitat tempsAgregacio) {
        // 1. Estructura base
        PeriodeUnitat groupUnitat = resolveUnitat(indicadors.get(0), tempsAgregacio);
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);

        // 2. Agrupació interna
        String innerGroupingCols = getChartInnerGroupingCols(indicadors.get(0), tempsAgregacio);
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "" : "GROUP BY " + innerGroupingCols;

        // 3. Càlculs
        String querySelect = indicadors.stream().map(ind -> {
            TableColumnsEnum realAgregacio = getRealGraficAgregacio(ind.getAgregacio(), ind.getUnitatAgregacio(), tempsAgregacio);
            return getSimpleQuerySelect(realAgregacio, ind.getIndicadorCodi(), resolveUnitat(ind, tempsAgregacio));
        }).collect(Collectors.joining(", "));

        String subQuerySelects = getTaulaSubQuerySelects(indicadors, groupUnitat);

        // 4. Assemblatge
        return "SELECT agrupacio, " + querySelect +
            " FROM ( SELECT " + innerSelectCols + queryAgrupacio + " AS agrupacio, " + subQuerySelects +
            BASE_JOIN + BASE_WHERE + queryConditions + " " + innerGroupBy + ") " +
            " GROUP BY agrupacio ORDER BY agrupacio";
    }

    private String generateGraficUnionSubquery(String queryConditions, List<IndicadorAgregacio> indicadors, PeriodeUnitat tempsAgregacio, List<IndicadorAgregacio> ordenOriginal) {
        if (indicadors.isEmpty()) return "";

        // 1. Estructura base
        String outerGroupingCols = getTimeGroupingColumns(tempsAgregacio, true).replace("t.", "");
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);
        PeriodeUnitat groupUnitat = resolveUnitat(indicadors.get(0), tempsAgregacio);

        // 2. Agrupació interna
        String innerGroupingCols = getChartInnerGroupingCols(indicadors.get(0), tempsAgregacio);
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "" : "GROUP BY " + innerGroupingCols;

        // 3. Càlculs
        String querySelect = ordenOriginal.stream().map(ind -> {
            TableColumnsEnum realAgregacio = getRealGraficAgregacio(ind.getAgregacio(), ind.getUnitatAgregacio(), tempsAgregacio);
            if (indicadors.contains(ind)) {
                return getSimpleQuerySelect(realAgregacio, ind.getIndicadorCodi(), resolveUnitat(ind, tempsAgregacio));
            }
            String suffix = getIndicadorSuffix(ind.getIndicadorCodi(), resolveUnitat(ind, tempsAgregacio));
            if (realAgregacio == TableColumnsEnum.AVERAGE) return "null AS average_result" + suffix;
            if (realAgregacio == TableColumnsEnum.FIRST_SEEN) return "null AS first_seen" + suffix;
            if (realAgregacio == TableColumnsEnum.LAST_SEEN) return "null AS last_seen" + suffix;
            return "null AS total_sum" + suffix;
        }).collect(Collectors.joining(", "));

        String subQuerySelects = getTaulaSubQuerySelects(indicadors, groupUnitat);

        // 4. Assemblatge
        return "SELECT " + queryAgrupacio + " AS agrupacio, " + querySelect +
            " FROM ( SELECT " + innerSelectCols + subQuerySelects + BASE_JOIN + BASE_WHERE + queryConditions + " " + innerGroupBy + ") " +
            " GROUP BY " + outerGroupingCols;
    }

    private String generateGraficUnionQuery(List<IndicadorAgregacio> allIndicadors, String unionSubqueries, PeriodeUnitat tempsAgregacio) {
        // 1. Càlculs de columnes externes
        Set<String> allResultColumns = allIndicadors.stream().map(ind -> {
            TableColumnsEnum realAgregacio = getRealGraficAgregacio(ind.getAgregacio(), ind.getUnitatAgregacio(), tempsAgregacio);
            String suffix = getIndicadorSuffix(ind.getIndicadorCodi(), resolveUnitat(ind, tempsAgregacio));
            switch (realAgregacio) {
                case AVERAGE: return "average_result" + suffix;
                case FIRST_SEEN: return "first_seen" + suffix;
                case LAST_SEEN: return "last_seen" + suffix;
                default: return "total_sum" + suffix;
            }
        }).collect(Collectors.toCollection(LinkedHashSet::new));

        String outerSelect = allResultColumns.stream().map(col -> "MAX(" + col + ") as " + col).collect(Collectors.joining(", "));

        // 2. Assemblatge
        return "SELECT agrupacio, " + outerSelect + " FROM (" + unionSubqueries + ") GROUP BY agrupacio ORDER BY agrupacio";
    }

    // ====================================================================================
    // UTILS DE FORMAT I SELECT
    // ====================================================================================

    private String getTaulaSubQuerySelects(List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat unitatParaSuffix) {
        Map<String, IndicadorAgregacio> unics = new LinkedHashMap<>();
        indicadorsAgregacio.forEach(ind -> unics.putIfAbsent(ind.getIndicadorCodi(), ind));
        return unics.values().stream()
            .map(ind -> getSumIndicadorQuery(ind) + getIndicadorSuffix(ind.getIndicadorCodi(), unitatParaSuffix))
            .collect(Collectors.joining(", "));
    }

    private String getSimpleQuerySelect(TableColumnsEnum agregacio, String indicadorCodi, PeriodeUnitat unitat) {
        String suffix = getIndicadorSuffix(indicadorCodi, unitat);
        switch (agregacio) {
            case AVERAGE: return "AVG(sum_fets" + suffix + ") AS average_result" + suffix;
            case FIRST_SEEN: return "CASE WHEN SUM(sum_fets" + suffix + ") > 0 THEN MIN(data) ELSE NULL END AS first_seen" + suffix;
            case LAST_SEEN: return "CASE WHEN SUM(sum_fets" + suffix + ") > 0 THEN MAX(data) ELSE NULL END AS last_seen" + suffix;
            default: return "SUM(sum_fets" + suffix + ") AS total_sum" + suffix;
        }
    }

    private static String getIndicadorSuffix(String indicadorCodi, PeriodeUnitat unitat) {
        String base = indicadorCodi != null ? "_" + indicadorCodi : "";
        return unitat != null ? base + "_" + unitat.name() : base;
    }

    private static String getSumIndicadorQuery(String indicadorCodi) {
        return String.format(SUM_INDICADOR_TEMPLATE, indicadorCodi);
    }

    /**
     * Com {@link #getSumIndicadorQuery(String)}, però si l'indicador és una FORMULA (té termesFormula), en
     * lloc d'un únic JSON_VALUE genera la suma/resta dels JSON_VALUE de tots els seus indicadors component
     * -dins del mateix SUM(...), perquè el càlcul es faci fila a fila abans d'agregar-.
     */
    private static String getSumIndicadorQuery(IndicadorAgregacio indicadorAgregacio) {
        List<IndicadorFormulaTermeResolt> termes = indicadorAgregacio != null ? indicadorAgregacio.getTermesFormula() : null;
        if (termes == null || termes.isEmpty()) {
            return getSumIndicadorQuery(indicadorAgregacio.getIndicadorCodi());
        }
        String expressio = termes.stream()
            .map(terme -> (OperadorFormulaEnum.RESTA.equals(terme.getOperador()) ? "- " : "+ ")
                + String.format(INDICADOR_VALUE_EXPR_TEMPLATE, terme.getIndicadorCodi()))
            .collect(Collectors.joining(" "));
        if (expressio.startsWith("+ ")) {
            expressio = expressio.substring(2);
        }
        return " SUM(" + expressio + ") AS sum_fets";
    }

    private static String getDimensionValueQuery(String dimensioCodi) {
        return String.format(DIMENSION_VALUE_TEMPLATE, dimensioCodi);
    }

    static String generateDimensionConditions(Map<String, List<String>> dimensionsFiltre) {
        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) return "";
        return dimensionsFiltre.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty())
            .map(entry -> {
                String codi = escapeSqlLiteral(entry.getKey());
                List<String> valors = entry.getValue();
                if (valors.size() == 1) {
                    return " AND " + getDimensionValueQuery(codi) + "= '" + escapeSqlLiteral(valors.get(0)) + "' ";
                } else {
                    String valorsStr = valors.stream().map(valor -> "'" + escapeSqlLiteral(valor) + "'").collect(Collectors.joining(","));
                    return " AND " + getDimensionValueQuery(codi) + " IN (" + valorsStr + ") ";
                }
            }).collect(Collectors.joining(" "));
    }

    private static String generateGraficAgrupacioConditions(PeriodeUnitat tempsAgregacio) {
        switch (tempsAgregacio) {
            case SETMANA: return "anualitat || '/' || LPAD(setmana, 2, '0')";
            case MES: return "anualitat || '/' || LPAD(mes, 2, '0')";
            case TRIMESTRE: return "anualitat || '/' || trimestre";
            case ANY: return "anualitat";
            default: return "anualitat || '/' || LPAD(mes, 2, '0') || '/' || LPAD(dia, 2, '0')";
        }
    }

    private int getPeriodLevel(PeriodeUnitat unitat) {
        if (unitat == null) return 0;
        switch (unitat) {
            case DIA: return 1;
            case SETMANA: return 2;
            case MES: return 3;
            case TRIMESTRE: return 4;
            case ANY: return 5;
            default: return 0;
        }
    }

    private TableColumnsEnum getRealGraficAgregacio(TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio, PeriodeUnitat tempsAgregacio) {
        if (agregacio == TableColumnsEnum.AVERAGE && unitatAgregacio != null && tempsAgregacio != null) {
            if (getPeriodLevel(unitatAgregacio) > getPeriodLevel(tempsAgregacio)) {
                return TableColumnsEnum.SUM;
            }
        }
        return agregacio;
    }

    /**
     * Genera la condición de seguridad de entidad/órgano.
     * Si la restricción es aplicable pero no hay valores permitidos, deniega el acceso (fail-closed: "AND 1 = 0").
     */
    private static String generateSecurityCondition(SeguretatFiltreSql seguretat) {
        if (seguretat == null || !seguretat.isActiva()) {
            return "";
        }
        List<String> clausules = new java.util.ArrayList<>();
        if (seguretat.getDimensioEntitatCodi() != null && seguretat.getValorsEntitatPermesos() != null && !seguretat.getValorsEntitatPermesos().isEmpty()) {
            clausules.add(buildSecurityInClause(seguretat.getDimensioEntitatCodi(), seguretat.getValorsEntitatPermesos()));
        }
        if (seguretat.getDimensioOrganCodi() != null && seguretat.getValorsOrganPermesos() != null && !seguretat.getValorsOrganPermesos().isEmpty()) {
            clausules.add(buildSecurityInClause(seguretat.getDimensioOrganCodi(), seguretat.getValorsOrganPermesos()));
        }
        if (clausules.isEmpty()) {
            return " AND 1 = 0 "; // Deniega la consulta si hay seguridad activa pero sin permisos
        }
        return " AND (" + String.join(" OR ", clausules) + ") ";
    }
    /**
     * Construye una cláusula IN para la seguridad, escapando los valores.
     */
    private static String buildSecurityInClause(String dimensioCodi, List<String> valors) {
        String valorsStr = valors.stream().map(v -> "'" + escapeSqlLiteral(v) + "'").collect(Collectors.joining(","));
        return getDimensionValueQuery(escapeSqlLiteral(dimensioCodi)) + " IN (" + valorsStr + ")";
    }

    /**
     * Escapa comillas simples para evitar SQL Injection.
     * Los valores de dimensiones pueden venir de fuentes externas y no son de confianza directa.
     */
    private static String escapeSqlLiteral(String value) {
        return value == null ? null : value.replace("'", "''");
    }

    /**
     * Genera un calendari sintètic amb tots els períodes (dies) entre :dataInici i :dataFi.
     * S'agrupa per DISTINCT per evitar duplicats si hi ha salts, i extreu les columnes de temps
     * exactament com ho fa la taula com_est_temps.
     */
    private String getPeriodesRangeQuery(PeriodeUnitat unitat) {
        String cols = getTimeGroupingColumns(unitat, false).replace("t.", "");
        return "(SELECT DISTINCT " + cols + " FROM (" +
            "SELECT EXTRACT(YEAR FROM d) AS anualitat, TO_NUMBER(TO_CHAR(d,'Q')) AS trimestre, " +
            "EXTRACT(MONTH FROM d) AS mes, TO_NUMBER(TO_CHAR(d,'IW')) AS setmana, EXTRACT(DAY FROM d) AS dia " +
            "FROM (SELECT :dataInici + LEVEL - 1 AS d FROM dual CONNECT BY LEVEL <= (:dataFi - :dataInici + 1))" +
            ") cal)";
    }

    /** Prefixa cada columna d'una llista (ex: "anualitat, mes") amb un alias de taula (ex: "periodes.anualitat, periodes.mes"). */
    private static String qualifyColumns(String tableAlias, String columns) {
        return java.util.Arrays.stream(columns.split(",\\s*"))
            .map(c -> tableAlias + "." + c.trim())
            .collect(Collectors.joining(", "));
    }

    /** Genera la condició d'igualtat per al LEFT JOIN entre el calendari i l'agregació real. */
    private static String getPeriodesJoinCondition(String leftAlias, String rightAlias, String columns) {
        return java.util.Arrays.stream(columns.split(",\\s*"))
            .map(c -> leftAlias + "." + c.trim() + " = " + rightAlias + "." + c.trim())
            .collect(Collectors.joining(" AND "));
    }
}
