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

}