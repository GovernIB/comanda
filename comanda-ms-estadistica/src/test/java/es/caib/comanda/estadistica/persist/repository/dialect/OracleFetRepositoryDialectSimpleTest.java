package es.caib.comanda.estadistica.persist.repository.dialect;

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

public class OracleFetRepositoryDialectSimpleTest {

    private OracleFetRepositoryDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new OracleFetRepositoryDialect();
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
            String expectedQuery) {
        // Act
        String query = removeConsecutiveSpaces(dialect.getSimpleQuery(dimensionsFiltre, indicadorCodi, agregacio, unitatAgregacio));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetSimpleQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions, SUM aggregation, DIA period
                Arguments.of(
                        "Null dimensions, SUM aggregation, DIA period",
                        null,
                        "visites",
                        TableColumnsEnum.SUM,
                        PeriodeUnitat.DIA,
                        removeConsecutiveSpaces("SELECT SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "    SELECT t.data as data, " +
                                "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "    WHERE f.entorn_app_id = :entornAppId " +
                                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.data)")
                ),

                // Test 2: Empty dimensions, AVERAGE aggregation, MES period
                Arguments.of(
                        "Empty dimensions, AVERAGE aggregation, MES period",
                        new HashMap<>(),
                        "visites",
                        TableColumnsEnum.AVERAGE,
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT AVG(sum_fets) AS average_result " +
                                "FROM ( " +
                                "    SELECT " +
                                "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "    WHERE f.entorn_app_id = :entornAppId " +
                                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes)")
                ),

                // Test 3: Single dimension with single value, PERCENTAGE aggregation, SETMANA period
                Arguments.of(
                        "Single dimension with single value, PERCENTAGE aggregation, SETMANA period",
                        Map.of("departament", List.of("RRHH")),
                        "visites",
                        TableColumnsEnum.PERCENTAGE,
                        PeriodeUnitat.SETMANA,
                        removeConsecutiveSpaces("SELECT SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "    SELECT t.data as data, " +
                                "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "    WHERE f.entorn_app_id = :entornAppId " +
                                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "GROUP BY t.data)")
                ),

                // Test 4: Single dimension with multiple values, FIRST_SEEN aggregation, TRIMESTRE period
                Arguments.of(
                        "Single dimension with multiple values, FIRST_SEEN aggregation, TRIMESTRE period",
                        Map.of("departament", List.of("RRHH", "IT")),
                        "visites",
                        TableColumnsEnum.FIRST_SEEN,
                        PeriodeUnitat.TRIMESTRE,
                        removeConsecutiveSpaces("SELECT CASE WHEN SUM(sum_fets) > 0 THEN MIN(data) ELSE NULL END AS first_seen " +
                                "FROM ( " +
                                "    SELECT t.data as data, " +
                                "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "    WHERE f.entorn_app_id = :entornAppId " +
                                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "GROUP BY t.data)")
                ),

                // Test 5: Multiple dimensions with mixed values, LAST_SEEN aggregation, ANY period
                Arguments.of(
                        "Multiple dimensions with mixed values, LAST_SEEN aggregation, ANY period",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        "visites",
                        TableColumnsEnum.LAST_SEEN,
                        PeriodeUnitat.ANY,
                        removeConsecutiveSpaces("SELECT CASE WHEN SUM(sum_fets) > 0 THEN MAX(data) ELSE NULL END AS last_seen " +
                            "FROM ( " +
                            "    SELECT t.data as data, " +
                            "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                            "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                            "    WHERE f.entorn_app_id = :entornAppId " +
                            "    AND t.data BETWEEN :dataInici AND :dataFi " +
                            "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT')  " +
                            "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                            "GROUP BY t.data)")
                ),

                // Test 6: Multiple dimensions with multiple values, SUM aggregation, DIA period
                Arguments.of(
                        "Multiple dimensions with multiple values, SUM aggregation, DIA period",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance", "HR"));
                        }},
                        "usuaris",
                        TableColumnsEnum.SUM,
                        PeriodeUnitat.DIA,
                        removeConsecutiveSpaces("SELECT SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "    SELECT t.data as data, " +
                                "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"usuaris\"'))) AS sum_fets " +
                                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "    WHERE f.entorn_app_id = :entornAppId " +
                                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT')  " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') IN ('Finance','HR') " +
                                "GROUP BY t.data)")
                ),

                // Test 7: Different indicator code, AVERAGE aggregation, MES period
                Arguments.of(
                        "Different indicator code, AVERAGE aggregation, MES period",
                        Map.of("departament", List.of("RRHH")),
                        "sessions",
                        TableColumnsEnum.AVERAGE,
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT AVG(sum_fets) AS average_result " +
                            "FROM ( " +
                            "    SELECT " +
                            "        SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets " +
                            "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                            "    WHERE f.entorn_app_id = :entornAppId " +
                            "    AND t.data BETWEEN :dataInici AND :dataFi " +
                            "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                            "GROUP BY t.anualitat, t.trimestre, t.mes)")
                )
        );
    }

}
