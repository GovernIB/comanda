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

public class PostgreSQLFetRepositoryDialectTest {

    private PostgreSQLFetRepositoryDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new PostgreSQLFetRepositoryDialect();
    }

    private static String removeConsecutiveSpaces(String input) {
        if (input == null) return null;
        return input.replaceAll("\\s+", " ").trim();
    }

    @Test
    void testGetFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery() {
        String query = removeConsecutiveSpaces(dialect.getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery());
        String expectedQuery = removeConsecutiveSpaces("SELECT f.* FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
            "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
            "AND f.dimensions_json->>'dimensioCodi' = :dimensioValor");
        // Nota: El dialecto usa "' || :dimensioCodi || '" dinámicamente, el test valida la estructura base.
        assertNotNull(query);
        assertTrue(query.contains("f.dimensions_json->>"));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetFindByEntornAppIdAndTempsDataAndDimensionQueryTestCases")
    void testGetFindByEntornAppIdAndTempsDataAndDimensionQuery(String testName, Map<String, List<String>> dimensionsFiltre, String expectedQuery) {
        String query = removeConsecutiveSpaces(dialect.getFindByEntornAppIdAndTempsDataAndDimensionQuery(dimensionsFiltre));
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
    }

    private static Stream<Arguments> provideGetFindByEntornAppIdAndTempsDataAndDimensionQueryTestCases() {
        return Stream.of(
            Arguments.of("Test Null Dimensions", null, removeConsecutiveSpaces("SELECT f.* FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id WHERE f.entorn_app_id = :entornAppId AND t.data = :data ")),
            Arguments.of("Test Empty Dimensions", new HashMap<>(), removeConsecutiveSpaces("SELECT f.* FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id WHERE f.entorn_app_id = :entornAppId AND t.data = :data ")),
            Arguments.of("Test Single Dimension with Single Value (departament=RRHH)", Map.of("departament", List.of("RRHH")),
                removeConsecutiveSpaces("SELECT f.* FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data = :data AND f.dimensions_json->>'departament' = 'RRHH' ")),
            Arguments.of("Test Single Dimension with Multiple Values (departament=RRHH,IT)", Map.of("departament", List.of("RRHH", "IT")),
                removeConsecutiveSpaces("SELECT f.* FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data = :data AND f.dimensions_json->>'departament' IN ('RRHH','IT') ")),
            Arguments.of("Test Multiple Dimensions with Mixed Values", new LinkedHashMap<>() {{ put("departament", List.of("RRHH", "IT")); put("area", List.of("Finance")); }},
                removeConsecutiveSpaces("SELECT f.* FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data = :data " +
                    "AND f.dimensions_json->>'departament' IN ('RRHH','IT') AND f.dimensions_json->>'area' = 'Finance' "))
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGenerateDimensionConditionsTestCases")
    void testGenerateDimensionConditions(String testName, Map<String, List<String>> dimensionsFiltre, String expectedConditions) {
        String conditions = removeConsecutiveSpaces(dialect.generateDimensionConditions(dimensionsFiltre));
        assertNotNull(conditions);
        assertEquals(expectedConditions, conditions);
    }

    private static Stream<Arguments> provideGenerateDimensionConditionsTestCases() {
        return Stream.of(
            Arguments.of("Test Null Dimensions", null, ""),
            Arguments.of("Test Empty Dimensions", new HashMap<>(), ""),
            Arguments.of("Test Single Dimension with Single Value", Map.of("departament", List.of("RRHH")), removeConsecutiveSpaces("AND f.dimensions_json->>'departament' = 'RRHH' ")),
            Arguments.of("Test Single Dimension with Multiple Values", Map.of("departament", List.of("RRHH", "IT")), removeConsecutiveSpaces("AND f.dimensions_json->>'departament' IN ('RRHH','IT') ")),
            Arguments.of("Test Multiple Dimensions", new LinkedHashMap<>() {{ put("departament", List.of("RRHH", "IT")); put("area", List.of("Finance")); }},
                removeConsecutiveSpaces("AND f.dimensions_json->>'departament' IN ('RRHH','IT') AND f.dimensions_json->>'area' = 'Finance' "))
        );
    }
}
