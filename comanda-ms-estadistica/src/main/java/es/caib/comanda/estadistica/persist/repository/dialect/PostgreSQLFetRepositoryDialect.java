package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació PostgreSQL de FetRepositoryDialect.
 * Proporciona les consultes SQL específiques per a la base de dades PostgreSQL,
 * mantenint la mateixa lògica, estructura i comentaris en català que la implementació d'Oracle.
 */
@Component
public class PostgreSQLFetRepositoryDialect implements FetRepositoryDialect {

    private static final String BASE_JOIN = " FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id ";
    private static final String BASE_WHERE_ENTORN = " WHERE f.entorn_app_id = :entornAppId ";
    private static final String FILTER_BETWEEN = " AND t.data BETWEEN :dataInici AND :dataFi ";
    private static final String FILTER_DATE = " AND t.data = :data ";
    private static final String BASE_WHERE = BASE_WHERE_ENTORN + FILTER_BETWEEN;

    // Sintaxi específica de PostgreSQL per a l'extracció i conversió de valors JSON a numèric
    private static final String SUM_INDICADOR_TEMPLATE = " SUM((f.indicadors_json->>'%s')::numeric) AS sum_fets";
    private static final String DIMENSION_VALUE_TEMPLATE = " f.dimensions_json->>'%s' ";

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
    public String getSimpleQuery(Map<String, List<String>> dimensionsFiltre, String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        // 1. Filtres
        String queryConditions = generateDimensionConditions(dimensionsFiltre);

        // 2. Agrupació interna
        PeriodeUnitat effectiveUnitat = (agregacio == TableColumnsEnum.FIRST_SEEN || agregacio == TableColumnsEnum.LAST_SEEN)
            ? PeriodeUnitat.DIA
            : unitatAgregacio;
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
    public String getGraficUnIndicadorQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, PeriodeUnitat tempsAgregacio) {
        // 1. Filtres i agrupacions base
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String innerGroupingCols = getChartInnerGroupingCols(indicadorAgregacio, tempsAgregacio);
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String innerGroupBy = innerGroupingCols.isEmpty() ? "" : " GROUP BY " + innerGroupingCols;

        String outerGroupingCols = getTimeGroupingColumns(tempsAgregacio, true).replace("t.", "");
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);

        // 2. Càlculs (depenen de la unitat resolta)
        PeriodeUnitat effectiveUnitat = resolveUnitat(indicadorAgregacio, tempsAgregacio);
        TableColumnsEnum realAgregacio = getRealGraficAgregacio(indicadorAgregacio.getAgregacio(), indicadorAgregacio.getUnitatAgregacio(), tempsAgregacio);

        String innerSumSelect = getSumIndicadorQuery(indicadorAgregacio.getIndicadorCodi()) + getIndicadorSuffix(indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);
        String outerSelect = getSimpleQuerySelect(realAgregacio, indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);

        // 3. Assemblatge
        return "SELECT " + queryAgrupacio + " AS agrupacio, " + outerSelect +
            " FROM ( SELECT " + innerSelectCols + innerSumSelect +
            BASE_JOIN + BASE_WHERE + queryConditions + innerGroupBy + ") " +
            " GROUP BY " + outerGroupingCols + " ORDER BY agrupacio";
    }

    @Override
    public String getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi, PeriodeUnitat tempsAgregacio) {
        // 1. Filtres i agrupacions base
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryDescomposicio = getDimensionValueQuery(dimensioDescomposicioCodi);

        // 2. Estructura d'agrupació
        String innerGroupingCols = getChartInnerGroupingCols(indicadorAgregacio, tempsAgregacio);
        String innerGroupBy = innerGroupingCols.isEmpty() ? "GROUP BY " + queryDescomposicio : "GROUP BY " + innerGroupingCols + ", " + queryDescomposicio;
        String innerSelectCols = innerGroupingCols.isEmpty() ? "" : innerGroupingCols + ", ";
        String outerGroupingCols = getTimeGroupingColumns(tempsAgregacio, true).replace("t.", "");
        String queryAgrupacio = generateGraficAgrupacioConditions(tempsAgregacio);

        // 3. Càlculs
        PeriodeUnitat effectiveUnitat = resolveUnitat(indicadorAgregacio, tempsAgregacio);
        TableColumnsEnum realAgregacio = getRealGraficAgregacio(indicadorAgregacio.getAgregacio(), indicadorAgregacio.getUnitatAgregacio(), tempsAgregacio);

        String innerSumSelect = getSumIndicadorQuery(indicadorAgregacio.getIndicadorCodi()) + getIndicadorSuffix(indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);
        String outerSelect = getSimpleQuerySelect(realAgregacio, indicadorAgregacio.getIndicadorCodi(), effectiveUnitat);

        // 4. Assemblatge
        return "SELECT " + queryAgrupacio + " AS agrupacio, descomposicio, " + outerSelect +
            " FROM ( SELECT " + innerSelectCols + queryDescomposicio + " AS descomposicio, " + innerSumSelect +
            BASE_JOIN + BASE_WHERE + queryConditions + innerGroupBy + ") " +
            " GROUP BY " + outerGroupingCols + ", descomposicio ORDER BY agrupacio, descomposicio";
    }

    @Override
    public String getGraficUnIndicadorAmbDescomposicioQuery(Map<String, List<String>> dimensionsFiltre, IndicadorAgregacio indicadorAgregacio, String dimensioDescomposicioCodi) {
        // 1. Filtres i dimensions
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
        String queryDescomposicio = getDimensionValueQuery(dimensioDescomposicioCodi);

        // 2. Assemblatge (consulta plana, sense subconsulta)
        return "SELECT " + queryDescomposicio + " AS agrupacio, " + getSumIndicadorQuery(indicadorAgregacio.getIndicadorCodi()) +
            BASE_JOIN + BASE_WHERE + queryConditions + " GROUP BY " + queryDescomposicio + " ORDER BY agrupacio";
    }

    @Override
    public String getGraficVarisIndicadorsQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, PeriodeUnitat tempsAgregacio) {
        Map<String, List<IndicadorAgregacio>> indicadorsPerGrup = indicadorsAgregacio.stream()
            .collect(Collectors.groupingBy(ind -> getChartGroupingKey(ind, tempsAgregacio)));

        if (indicadorsPerGrup.size() == 1) {
            return generateGraficSingleGroupQuery(dimensionsFiltre, indicadorsPerGrup.values().iterator().next(), tempsAgregacio);
        }

        String unionSubqueries = indicadorsPerGrup.values().stream()
            .map(grup -> generateGraficUnionSubquery(dimensionsFiltre, grup, tempsAgregacio, indicadorsAgregacio))
            .collect(Collectors.joining(" UNION ALL "));

        return generateGraficUnionQuery(indicadorsAgregacio, unionSubqueries, tempsAgregacio);
    }

    @Override
    public String getTaulaQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadorsAgregacio, String dimensioAgrupacioCodi) {
        Map<String, List<IndicadorAgregacio>> indicadorsPerGrup = indicadorsAgregacio.stream()
            .collect(Collectors.groupingBy(this::getInnerGroupingCols));

        if (indicadorsPerGrup.size() == 1) {
            return generateTaulaQuerySingleGroup(dimensionsFiltre, indicadorsPerGrup.values().iterator().next(), dimensioAgrupacioCodi);
        }

        String unionSubqueries = indicadorsPerGrup.values().stream()
            .map(grup -> generateUnionSubquery(dimensionsFiltre, grup, dimensioAgrupacioCodi, indicadorsAgregacio))
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
        PeriodeUnitat effectiveUnitat = resolveUnitat(ind, defaultTemps);
        PeriodeUnitat innerUnitat = defaultTemps;

        if (effectiveUnitat != null && getPeriodLevel(effectiveUnitat) < getPeriodLevel(defaultTemps)) {
            innerUnitat = effectiveUnitat;
        }

        return getTimeGroupingColumns(innerUnitat, false);
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

    private String generateTaulaQuerySingleGroup(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadors, String dimensioAgrupacioCodi) {
        // 1. Estructura base
        String queryAgrupacio = getDimensionValueQuery(dimensioAgrupacioCodi);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
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

    private String generateUnionSubquery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadors, String dimensioAgrupacioCodi, List<IndicadorAgregacio> ordenOriginal) {
        if (indicadors.isEmpty()) return "";

        // 1. Estructura base
        String queryAgrupacio = getDimensionValueQuery(dimensioAgrupacioCodi);
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
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

    private String generateGraficSingleGroupQuery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadors, PeriodeUnitat tempsAgregacio) {
        // 1. Estructura base
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
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

    private String generateGraficUnionSubquery(Map<String, List<String>> dimensionsFiltre, List<IndicadorAgregacio> indicadors, PeriodeUnitat tempsAgregacio, List<IndicadorAgregacio> ordenOriginal) {
        if (indicadors.isEmpty()) return "";

        // 1. Estructura base
        String queryConditions = generateDimensionConditions(dimensionsFiltre);
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
        return indicadorsAgregacio.stream()
            .map(IndicadorAgregacio::getIndicadorCodi)
            .distinct()
            .map(indicadorCodi -> getSumIndicadorQuery(indicadorCodi) + getIndicadorSuffix(indicadorCodi, unitatParaSuffix))
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

    private static String getDimensionValueQuery(String dimensioCodi) {
        return String.format(DIMENSION_VALUE_TEMPLATE, dimensioCodi);
    }

    static String generateDimensionConditions(Map<String, List<String>> dimensionsFiltre) {
        if (dimensionsFiltre == null || dimensionsFiltre.isEmpty()) return "";
        return dimensionsFiltre.entrySet().stream()
            .filter(entry -> entry.getKey() != null && entry.getValue() != null && !entry.getValue().isEmpty())
            .map(entry -> {
                String codi = entry.getKey();
                List<String> valors = entry.getValue();
                if (valors.size() == 1) {
                    return " AND " + getDimensionValueQuery(codi) + "= '" + valors.get(0) + "' ";
                } else {
                    String valorsStr = valors.stream().map(valor -> "'" + valor + "'").collect(Collectors.joining(","));
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
}
