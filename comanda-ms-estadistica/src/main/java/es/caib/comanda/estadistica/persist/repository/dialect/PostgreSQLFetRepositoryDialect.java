package es.caib.comanda.estadistica.persist.repository.dialect;

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

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        return "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND (f.dimensions_json->>:dimensioCodi) = :dimensioValor)";
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery() {
        return "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND (f.dimensions_json->>:dimensioCodi) IN (:dimensioValors)";
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        String query = "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data = :data ";

        String conditions = generateDimensionConditions(dimensionsFiltre);
        return query + conditions;
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(Map<String, List<String>> dimensionsFiltre) {
        String query = "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi ";

        String conditions = generateDimensionConditions(dimensionsFiltre);
        return query + conditions;
    }

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

//    @Override
//    public String getFindAnnualStatsByEntornAppIdQuery() {
//        return "SELECT t.anualitat as any, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "jsonb_build_object(" +
//                "  'suma', SUM(CAST(f.indicadors_json->>'*' AS DECIMAL(10,2))), " +
//                "  'mitja', AVG(CAST(f.indicadors_json->>'*' AS DECIMAL(10,2))), " +
//                "  'max', MAX(CAST(f.indicadors_json->>'*' AS DECIMAL(10,2))), " +
//                "  'min', MIN(CAST(f.indicadors_json->>'*' AS DECIMAL(10,2)))" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                "WHERE f.entorn_app_id = :entornAppId " +
//                "GROUP BY t.anualitat, f.dimensions_json";
//    }
//
//    @Override
//    public String getFindStatsByEntornAppIdAndPeriodQuery() {
//        return "SELECT " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN jsonb_build_object('any', t.anualitat) " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN jsonb_build_object('any', t.anualitat, 'trimestre', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN jsonb_build_object('any', t.anualitat, 'mes', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN jsonb_build_object('any', t.anualitat, 'setmana', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN jsonb_build_object('data', t.data) " +
//                "END as periode, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "jsonb_object_agg(" +
//                "  key, " +
//                "  jsonb_build_object(" +
//                "    'suma', SUM(value), " +
//                "    'mitja', AVG(value), " +
//                "    'max', MAX(value), " +
//                "    'min', MIN(value)" +
//                "  )" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id, " +
//                "jsonb_each_text(f.indicadors_json) AS jt(key, value) " +
//                "WHERE f.entorn_app_id = :entornAppId " +
//                "AND t.data BETWEEN :dataInici AND :dataFi " +
//                "GROUP BY " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN t.anualitat " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN CONCAT(t.anualitat, '-', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN CONCAT(t.anualitat, '-', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN CONCAT(t.anualitat, '-', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN t.data " +
//                "END, " +
//                "f.dimensions_json";
//    }
//
//    @Override
//    public String getFindStatsByEntornAppIdAndPeriodAndDimensionsQuery() {
//        return "SELECT " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN jsonb_build_object('any', t.anualitat) " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN jsonb_build_object('any', t.anualitat, 'trimestre', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN jsonb_build_object('any', t.anualitat, 'mes', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN jsonb_build_object('any', t.anualitat, 'setmana', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN jsonb_build_object('data', t.data) " +
//                "END as periode, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "jsonb_object_agg(" +
//                "  key, " +
//                "  jsonb_build_object(" +
//                "    'suma', SUM(CAST(value AS DECIMAL(10,2))), " +
//                "    'mitja', AVG(CAST(value AS DECIMAL(10,2))), " +
//                "    'max', MAX(CAST(value AS DECIMAL(10,2))), " +
//                "    'min', MIN(CAST(value AS DECIMAL(10,2)))" +
//                "  )" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id, " +
//                "jsonb_each_text(f.indicadors_json) AS jt(key, value) " +
//                "WHERE f.entorn_app_id = :entornAppId " +
//                "AND t.data BETWEEN :dataInici AND :dataFi " +
//                "AND (:dimensioNom IS NULL OR f.dimensions_json->>:dimensioNom IN (:dimensioValors)) " +
//                "GROUP BY " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN t.anualitat " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN CONCAT(t.anualitat, '-', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN CONCAT(t.anualitat, '-', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN CONCAT(t.anualitat, '-', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN t.data " +
//                "END, " +
//                "f.dimensions_json";
//    }
//
//    @Override
//    public String getFindStatsByEntornAppIdAndPeriodAndMultipleDimensionsQuery() {
//        return "WITH dimension_filters AS (" +
//                "  SELECT " +
//                "    key AS dimension_name, " +
//                "    jsonb_array_elements_text(value) AS dimension_value " +
//                "  FROM jsonb_each(:dimensionsJson::jsonb) " +
//                ") " +
//                "SELECT " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN jsonb_build_object('any', t.anualitat) " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN jsonb_build_object('any', t.anualitat, 'trimestre', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN jsonb_build_object('any', t.anualitat, 'mes', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN jsonb_build_object('any', t.anualitat, 'setmana', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN jsonb_build_object('data', t.data) " +
//                "END as periode, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "jsonb_object_agg(" +
//                "  key, " +
//                "  jsonb_build_object(" +
//                "    'suma', SUM(CAST(value AS DECIMAL(10,2))), " +
//                "    'mitja', AVG(CAST(value AS DECIMAL(10,2))), " +
//                "    'max', MAX(CAST(value AS DECIMAL(10,2))), " +
//                "    'min', MIN(CAST(value AS DECIMAL(10,2)))" +
//                "  )" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id, " +
//                "jsonb_each_text(f.indicadors_json) AS jt(key, value) " +
//                "WHERE f.entorn_app_id = :entornAppId " +
//                "AND t.data BETWEEN :dataInici AND :dataFi " +
//                "AND (" +
//                "    :dimensionsJson IS NULL " +
//                "    OR NOT EXISTS (" +
//                "        SELECT 1 " +
//                "        FROM dimension_filters df " +
//                "        GROUP BY df.dimension_name " +
//                "        HAVING NOT EXISTS (" +
//                "            SELECT 1 " +
//                "            FROM dimension_filters df2 " +
//                "            WHERE df2.dimension_name = df.dimension_name " +
//                "            AND f.dimensions_json->>df2.dimension_name = df2.dimension_value " +
//                "        )" +
//                "    )" +
//                ") " +
//                "GROUP BY " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN t.anualitat " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN CONCAT(t.anualitat, '-', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN CONCAT(t.anualitat, '-', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN CONCAT(t.anualitat, '-', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN t.data " +
//                "END, " +
//                "f.dimensions_json";
//    }
}