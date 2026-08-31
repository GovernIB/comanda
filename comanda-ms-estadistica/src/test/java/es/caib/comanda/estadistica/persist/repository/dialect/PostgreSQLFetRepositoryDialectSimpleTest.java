package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatFiltreSql;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgreSQLFetRepositoryDialectSimpleTest {

    private PostgreSQLFetRepositoryDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new PostgreSQLFetRepositoryDialect();
    }

    private static String removeConsecutiveSpaces(String input) {
        if (input == null) return null;
        return input.replaceAll("\\s+", " ").trim();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetSimpleQueryTestCases")
    void testGetSimpleQueryParameterized(
        String testName,
        Map<String, List<String>> dimensionsFiltre,
        String indicadorCodi,
        TableColumnsEnum agregacio,
        PeriodeUnitat unitatAgregacio,
        SeguretatFiltreSql seguretat,
        String expectedQuery) {

        String query = removeConsecutiveSpaces(dialect.getSimpleQuery(dimensionsFiltre, indicadorCodi, agregacio, unitatAgregacio, seguretat));

        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetSimpleQueryTestCases() {
        return Stream.of(
            Arguments.of("Null dimensions, SUM aggregation, DIA period", null, "visites", TableColumnsEnum.SUM, PeriodeUnitat.DIA, null,
                removeConsecutiveSpaces("SELECT SUM(sum_fets_visites_DIA) AS total_sum_visites_DIA " +
                    "FROM ( SELECT t.data, SUM((f.indicadors_json->>'visites')::numeric) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY t.data)")),
            Arguments.of("Empty dimensions, AVERAGE aggregation, MES period", new HashMap<>(), "visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES, null,
                removeConsecutiveSpaces("SELECT AVG(sum_fets_visites_MES) AS average_result_visites_MES " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM((f.indicadors_json->>'visites')::numeric) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes)")),
            Arguments.of("Single dimension with single value, PERCENTAGE aggregation, SETMANA period", Map.of("departament", List.of("RRHH")), "visites", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.SETMANA, null,
                removeConsecutiveSpaces("SELECT SUM(sum_fets_visites_SETMANA) AS total_sum_visites_SETMANA " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, t.setmana, " +
                    "SUM((f.indicadors_json->>'visites')::numeric) AS sum_fets_visites_SETMANA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND f.dimensions_json->>'departament' = 'RRHH' " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana)")),
            Arguments.of("Single dimension with multiple values, FIRST_SEEN aggregation, TRIMESTRE period", Map.of("departament", List.of("RRHH", "IT")), "visites", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.TRIMESTRE, null,
                removeConsecutiveSpaces("SELECT CASE WHEN SUM(sum_fets_visites_DIA) > 0 THEN MIN(t.data) ELSE NULL END AS first_seen_visites_DIA " +
                    "FROM ( SELECT t.data, " +
                    "SUM((f.indicadors_json->>'visites')::numeric) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND f.dimensions_json->>'departament' IN ('RRHH','IT') " +
                    "GROUP BY t.data)")),
            Arguments.of("Multiple dimensions with mixed values, LAST_SEEN aggregation, ANY period", new LinkedHashMap<>() {{ put("departament", List.of("RRHH", "IT")); put("area", List.of("Finance")); }}, "visites", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.ANY, null,
                removeConsecutiveSpaces("SELECT CASE WHEN SUM(sum_fets_visites_DIA) > 0 THEN MAX(t.data) ELSE NULL END AS last_seen_visites_DIA " +
                    "FROM ( SELECT t.data, " +
                    "SUM((f.indicadors_json->>'visites')::numeric) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND f.dimensions_json->>'departament' IN ('RRHH','IT') AND f.dimensions_json->>'area' = 'Finance' " +
                    "GROUP BY t.data)")),
            Arguments.of("Multiple dimensions with multiple values, SUM aggregation, DIA period", new LinkedHashMap<>() {{ put("departament", List.of("RRHH", "IT")); put("area", List.of("Finance", "HR")); }}, "usuaris", TableColumnsEnum.SUM, PeriodeUnitat.DIA, null,
                removeConsecutiveSpaces("SELECT SUM(sum_fets_usuaris_DIA) AS total_sum_usuaris_DIA " +
                    "FROM ( SELECT t.data, " +
                    "SUM((f.indicadors_json->>'usuaris')::numeric) AS sum_fets_usuaris_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND f.dimensions_json->>'departament' IN ('RRHH','IT') AND f.dimensions_json->>'area' IN ('Finance','HR') " +
                    "GROUP BY t.data)")),
            Arguments.of("Different indicator code, AVERAGE aggregation, MES period", Map.of("departament", List.of("RRHH")), "sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES, null,
                removeConsecutiveSpaces("SELECT AVG(sum_fets_sessions_MES) AS average_result_sessions_MES " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM((f.indicadors_json->>'sessions')::numeric) AS sum_fets_sessions_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND f.dimensions_json->>'departament' = 'RRHH' " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes)"))
        );
    }
}
