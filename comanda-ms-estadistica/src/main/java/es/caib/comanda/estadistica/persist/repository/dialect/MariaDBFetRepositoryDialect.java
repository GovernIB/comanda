package es.caib.comanda.estadistica.persist.repository.dialect;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementació MariaDB de FetRepositoryDialect.
 * Proporciona consultes SQL específiques per a la base de dades MariaDB.
 */
@Component
public class MariaDBFetRepositoryDialect implements FetRepositoryDialect {

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        return "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND JSON_UNQUOTE((JSON_EXTRACT(f.dimensions_json, CONCAT('$.\"', :dimensioCodi, '\"'))) = :dimensioValor";
    }

    @Override
    public String getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery() {
        return "SELECT f.* FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND JSON_UNQUOTE(JSON_EXTRACT(f.dimensions_json, CONCAT('$.\"', :dimensioCodi, '\"'))) IN (:dimensioValors)";
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
                        return "AND JSON_UNQUOTE(JSON_EXTRACT(f.dimensions_json, '$.\"" + codi + "\"')) = '" + valors.get(0) + "'";
                    } else {
                        String valorsStr = valors.stream()
                                .map(valor -> "'" + valor + "'")
                                .collect(Collectors.joining(","));
                        return "AND JSON_UNQUOTE(JSON_EXTRACT(f.dimensions_json, '$.\"" + codi + "\"')) IN (" + valorsStr + ")";
                    }
                })
                .collect(Collectors.joining(" "));
    }

//    @Override
//    public String getFindAnnualStatsByEntornAppIdQuery() {
//        return "SELECT t.anualitat as any, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "JSON_OBJECT(" +
//                "  'suma', SUM(CAST(JSON_EXTRACT(f.indicadors_json, '$.*') AS DECIMAL(10,2))), " +
//                "  'mitja', AVG(CAST(JSON_EXTRACT(f.indicadors_json, '$.*') AS DECIMAL(10,2))), " +
//                "  'max', MAX(CAST(JSON_EXTRACT(f.indicadors_json, '$.*') AS DECIMAL(10,2))), " +
//                "  'min', MIN(CAST(JSON_EXTRACT(f.indicadors_json, '$.*') AS DECIMAL(10,2)))" +
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
//                "  WHEN :nivellAgrupacio = 'ANY' THEN JSON_OBJECT('any', t.anualitat) " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN JSON_OBJECT('any', t.anualitat, 'trimestre', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN JSON_OBJECT('any', t.anualitat, 'mes', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN JSON_OBJECT('any', t.anualitat, 'setmana', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN JSON_OBJECT('data', t.data) " +
//                "END as periode, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "JSON_OBJECTAGG(" +
//                "  jt.key, " +
//                "  JSON_OBJECT(" +
//                "    'suma', SUM(jt.value), " +
//                "    'mitja', AVG(jt.value), " +
//                "    'max', MAX(jt.value), " +
//                "    'min', MIN(jt.value)" +
//                "  )" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                "JOIN JSON_TABLE(f.indicadors_json, '$.*' COLUMNS (" +
//                "  key VARCHAR(255) PATH '$.key', " +
//                "  value DECIMAL(10,2) PATH '$.value'" +
//                ")) AS jt " +
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
//                "  WHEN :nivellAgrupacio = 'ANY' THEN JSON_OBJECT('any', t.anualitat) " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN JSON_OBJECT('any', t.anualitat, 'trimestre', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN JSON_OBJECT('any', t.anualitat, 'mes', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN JSON_OBJECT('any', t.anualitat, 'setmana', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN JSON_OBJECT('data', t.data) " +
//                "END as periode, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "JSON_OBJECTAGG(" +
//                "  jt.key, " +
//                "  JSON_OBJECT(" +
//                "    'suma', SUM(jt.value), " +
//                "    'mitja', AVG(jt.value), " +
//                "    'max', MAX(jt.value), " +
//                "    'min', MIN(jt.value)" +
//                "  )" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                "JOIN JSON_TABLE(f.indicadors_json, '$.*' COLUMNS (" +
//                "  key VARCHAR(255) PATH '$.key', " +
//                "  value DECIMAL(10,2) PATH '$.value'" +
//                ")) AS jt " +
//                "WHERE f.entorn_app_id = :entornAppId " +
//                "AND t.data BETWEEN :dataInici AND :dataFi " +
//                "AND (:dimensioNom IS NULL OR JSON_EXTRACT(f.dimensions_json, CONCAT('$.\"', :dimensioNom, '\"')) IN (:dimensioValors)) " +
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
//        return "WITH RECURSIVE dimension_filters AS (" +
//                "  SELECT " +
//                "    JSON_KEYS(:dimensionsJson) AS dimension_names" +
//                "), " +
//                "dimension_values AS (" +
//                "  SELECT " +
//                "    dimension_name, " +
//                "    JSON_EXTRACT(:dimensionsJson, CONCAT('$.\"', dimension_name, '\"')) AS values " +
//                "  FROM JSON_TABLE(dimension_filters.dimension_names, '$[*]' COLUMNS (" +
//                "    dimension_name VARCHAR(255) PATH '$'" +
//                "  )) AS jt" +
//                ") " +
//                "SELECT " +
//                "CASE " +
//                "  WHEN :nivellAgrupacio = 'ANY' THEN JSON_OBJECT('any', t.anualitat) " +
//                "  WHEN :nivellAgrupacio = 'TRIMESTRE' THEN JSON_OBJECT('any', t.anualitat, 'trimestre', t.trimestre) " +
//                "  WHEN :nivellAgrupacio = 'MES' THEN JSON_OBJECT('any', t.anualitat, 'mes', t.mes) " +
//                "  WHEN :nivellAgrupacio = 'SETMANA' THEN JSON_OBJECT('any', t.anualitat, 'setmana', t.setmana) " +
//                "  WHEN :nivellAgrupacio = 'DIA' THEN JSON_OBJECT('data', t.data) " +
//                "END as periode, " +
//                "f.dimensions_json as dimensions, " +
//                "COUNT(*) as num_registres, " +
//                "JSON_OBJECTAGG(" +
//                "  jt.key, " +
//                "  JSON_OBJECT(" +
//                "    'suma', SUM(jt.value), " +
//                "    'mitja', AVG(jt.value), " +
//                "    'max', MAX(jt.value), " +
//                "    'min', MIN(jt.value)" +
//                "  )" +
//                ") as estadistiques " +
//                "FROM cmd_est_fet f " +
//                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                "JOIN JSON_TABLE(f.indicadors_json, '$.*' COLUMNS (" +
//                "  key VARCHAR(255) PATH '$.key', " +
//                "  value DECIMAL(10,2) PATH '$.value'" +
//                ")) AS jt " +
//                "WHERE f.entorn_app_id = :entornAppId " +
//                "AND t.data BETWEEN :dataInici AND :dataFi " +
//                "AND (" +
//                "    :dimensionsJson IS NULL " +
//                "    OR NOT EXISTS (" +
//                "        SELECT 1 " +
//                "        FROM dimension_values dv " +
//                "        WHERE NOT JSON_CONTAINS(dv.values, JSON_EXTRACT(f.dimensions_json, CONCAT('$.\"', dv.dimension_name, '\"')))" +
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