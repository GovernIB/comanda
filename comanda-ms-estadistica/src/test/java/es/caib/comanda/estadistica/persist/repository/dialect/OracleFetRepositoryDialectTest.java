package es.caib.comanda.estadistica.persist.repository.dialect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class OracleFetRepositoryDialectTest {

    private OracleFetRepositoryDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new OracleFetRepositoryDialect();
    }

    private static String removeConsecutiveSpaces(String input) {
        if (input == null) return null;
        return input.replaceAll("\\s+", " ");
    }

    @Test
    void testGetFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        // Act
        String query = removeConsecutiveSpaces(dialect.getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery());

        // Assert
        String expectedQuery = removeConsecutiveSpaces("SELECT f.* " +
                "FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND JSON_VALUE(f.dimensions_json, '$.\"' || :dimensioCodi || '\"') = :dimensioValor");
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
    }

    @Test
    void testGetFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery() {
        // Act
        String query = removeConsecutiveSpaces(dialect.getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery());

        // Assert
        String expectedQuery = removeConsecutiveSpaces("SELECT f.* " +
                "FROM cmd_est_fet f " +
                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                "WHERE f.entorn_app_id = :entornAppId " +
                "AND t.data BETWEEN :dataInici AND :dataFi " +
                "AND JSON_VALUE(f.dimensions_json, '$.\"' || :dimensioCodi || '\"') IN (:dimensioValor)");
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetFindByEntornAppIdAndTempsDataAndDimensionQueryTestCases")
    void testGetFindByEntornAppIdAndTempsDataAndDimensionQuery(String testName, Map<String, List<String>> dimensionsFiltre, String expectedQuery) {
        // Act
        String query = removeConsecutiveSpaces(dialect.getFindByEntornAppIdAndTempsDataAndDimensionQuery(dimensionsFiltre));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
    }

    private static Stream<Arguments> provideGetFindByEntornAppIdAndTempsDataAndDimensionQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions
                Arguments.of(
                        "Test Null Dimensions",
                        null,
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f " +
                                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data = :data ")),

                // Test 2: Empty dimensions
                Arguments.of(
                        "Test Empty Dimensions",
                        new HashMap<>(),
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f " +
                                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data = :data ")),

                // Test 3: Single dimension, single value
                Arguments.of(
                        "Test Single Dimension with Single Value (departament=RRHH)",
                        Map.of("departament", List.of("RRHH")),
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f " +
                                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data = :data " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' ")
                ),

                // Test 4: Single dimension, multiple values
                Arguments.of(
                        "Test Single Dimension with Multiple Values (departament=RRHH,IT)",
                        Map.of("departament", List.of("RRHH", "IT")),
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f " +
                                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data = :data " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') ")
                ),

                // Test 5: Multiple dimensions, mixed values
                Arguments.of(
                        "Test Multiple Dimensions with Mixed Values (departament=RRHH,IT and area=Finance)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f " +
                                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data = :data " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' ")
                ),

                // Test 6: Multiple dimensions, multiple values
                Arguments.of(
                        "Test Multiple Dimensions with Multiple Values (departament=RRHH,IT and area=Finance,HR)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance", "HR"));
                        }},
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f " +
                                "JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data = :data " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') IN ('Finance','HR') ")
                )
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetFindByEntornAppIdAndTempsDataBetweenAndDimensionQueryTestCases")
    void testGetFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(String testName, Map<String, List<String>> dimensionsFiltre, String expectedQuery) {
        // Act
        String query = removeConsecutiveSpaces(dialect.getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(dimensionsFiltre));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
    }

    private static Stream<Arguments> provideGetFindByEntornAppIdAndTempsDataBetweenAndDimensionQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions
                Arguments.of(
                        "Test Null Dimensions",
                        null,
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi ")
                ),

                // Test 2: Empty dimensions
                Arguments.of(
                        "Test Empty Dimensions",
                        new HashMap<>(),
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi ")
                ),

                // Test 3: Single dimension, single value
                Arguments.of(
                        "Test Single Dimension with Single Value (departament=RRHH)",
                        Map.of("departament", List.of("RRHH")),
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' ")
                ),

                // Test 4: Single dimension, multiple values
                Arguments.of(
                        "Test Single Dimension with Multiple Values (departament=RRHH,IT)",
                        Map.of("departament", List.of("RRHH", "IT")),
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') ")
                ),

                // Test 5: Multiple dimensions, mixed values
                Arguments.of(
                        "Test Multiple Dimensions with Mixed Values (departament=RRHH,IT and area=Finance)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT')  " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' ")
                ),

                // Test 6: Multiple dimensions, multiple values
                Arguments.of(
                        "Test Multiple Dimensions with Multiple Values (departament=RRHH,IT and area=Finance,HR)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance", "HR"));
                        }},
                        removeConsecutiveSpaces("SELECT f.* " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT')  " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') IN ('Finance','HR') ")
                )
        );
    }


    // Mètode: generateDimensionConditions
    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGenerateDimensionConditionsTestCases")
    void testGenerateDimensionConditions_NullInput(String testName, Map<String, List<String>> dimensionsFiltre, String expectedConditions) {
        // Act
        String conditions = removeConsecutiveSpaces(dialect.generateDimensionConditions(dimensionsFiltre));

        // Assert
        assertNotNull(conditions);
        assertEquals(expectedConditions, conditions);
    }

    private static Stream<Arguments> provideGenerateDimensionConditionsTestCases() {
        return Stream.of(
                // Test 1: Null dimensions
                Arguments.of(
                        "Test Null Dimensions",
                        null,
                        ""),

                // Test 2: Empty dimensions
                Arguments.of(
                        "Test Empty Dimensions",
                        new HashMap<>(),
                        ""),

                // Test 3: Single dimension, single value
                Arguments.of(
                        "Test Single Dimension with Single Value (departament=RRHH)",
                        Map.of("departament", List.of("RRHH")),
                        removeConsecutiveSpaces("AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' ")),

                // Test 4: Single dimension, multiple values
                Arguments.of(
                        "Test Single Dimension with Multiple Values (departament=RRHH,IT)",
                        Map.of("departament", List.of("RRHH", "IT")),
                        removeConsecutiveSpaces("AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') ")),

                // Test 5: Multiple dimensions, single values
                Arguments.of(
                        "Test Multiple Dimensions with Mixed Values (departament=RRHH,IT and area=Finance)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH"));
                            put("area", List.of("Finance"));
                        }},
                        removeConsecutiveSpaces("AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH'  AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' ")),

                // Test 6: Multiple dimensions, mixed values
                Arguments.of(
                        "Test Multiple Dimensions with Mixed Values (departament=RRHH,IT and area=Finance)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        removeConsecutiveSpaces("AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT')  AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' ")),

                // Test 7: Multiple dimensions, multiple values
                Arguments.of(
                        "Test Multiple Dimensions with Multiple Values (departament=RRHH,IT and area=Finance,HR)",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance", "HR"));
                        }},
                        removeConsecutiveSpaces("AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT')  AND JSON_VALUE(f.dimensions_json, '$.\"area\"') IN ('Finance','HR') "))
        );
    }

}
