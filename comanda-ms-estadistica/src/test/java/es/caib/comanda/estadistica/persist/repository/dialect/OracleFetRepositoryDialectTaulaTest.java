package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OracleFetRepositoryDialectTaulaTest {

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
    @MethodSource("provideGetTaulaQueryTestCases")
    void testGetTaulaQueryParameterized(
        String testName,
        Map<String, List<String>> dimensionsFiltre,
        List<IndicadorAgregacio> indicadorsAgregacio,
        String dimensioAgrupacioCodi,
        String expectedQuery) {

        // Act
        String query = removeConsecutiveSpaces(dialect.getTaulaQuery(dimensionsFiltre, indicadorsAgregacio, dimensioAgrupacioCodi, null));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetTaulaQueryTestCases() {
        return Stream.of(
            // Test 1: Null dimensions, single indicator with SUM aggregation, "departament" agrupacio
            Arguments.of(
                "Null dimensions, single indicator with SUM aggregation, 'departament' agrupacio",
                null,
                List.of(createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES)),
                "departament",
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES " +
                    "FROM ( " +
                    "    SELECT t.anualitat, t.trimestre, t.mes, " +
                    "    JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS agrupacio, " +
                    "    SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "    WHERE f.entorn_app_id = :entornAppId " +
                    "    AND t.data BETWEEN :dataInici AND :dataFi " +
                    "    GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                    ") " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            ),
            // Test 2: Empty dimensions, multiple indicators with same aggregation unit, "area" agrupacio
            Arguments.of(
                "Empty dimensions, multiple indicators with same aggregation unit, 'area' agrupacio",
                new HashMap<>(),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES)
                ),
                "area",
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES, " +
                    "AVG(sum_fets_sessions_MES) AS average_result_sessions_MES " +
                    "FROM ( " +
                    "   SELECT t.anualitat, t.trimestre, t.mes, " +
                    "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_MES " +
                    "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "   WHERE f.entorn_app_id = :entornAppId " +
                    "   AND t.data BETWEEN :dataInici AND :dataFi " +
                    "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                    ") " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            ),
            // Test 3: Single dimension with single value, multiple indicators with DIFFERENT aggregations (UNION ALL)
            Arguments.of(
                "Single dimension with single value, multiple indicators with different aggregations, 'usuari' agrupacio",
                Map.of("departament", List.of("RRHH")),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES)
                ),
                "usuari",
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "MAX(total_sum_visites_MES) as total_sum_visites_MES, " +
                    "MAX(first_seen_sessions_DIA) as first_seen_sessions_DIA " +
                    "FROM (" +
                    "SELECT agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES, " +
                    "null AS first_seen_sessions_DIA " +
                    "FROM ( " +
                    "   SELECT t.anualitat, t.trimestre, t.mes, " +
                    "   JSON_VALUE(f.dimensions_json, '$.\"usuari\"') AS agrupacio, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "   WHERE f.entorn_app_id = :entornAppId " +
                    "   AND t.data BETWEEN :dataInici AND :dataFi " +
                    "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                    "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
                    ") " +
                    "GROUP BY agrupacio " +
                    "UNION ALL " +
                    "SELECT agrupacio, " +
                    "null AS total_sum_visites_MES, " +
                    "CASE WHEN SUM(sum_fets_sessions_DIA) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions_DIA " +
                    "FROM ( " +
                    "   SELECT t.data, " +
                    "   JSON_VALUE(f.dimensions_json, '$.\"usuari\"') AS agrupacio, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_DIA " +
                    "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "   WHERE f.entorn_app_id = :entornAppId " +
                    "   AND t.data BETWEEN :dataInici AND :dataFi " +
                    "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                    "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
                    ") " +
                    "GROUP BY agrupacio) " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            ),
            // Test 4: Single dimension with multiple values, multiple indicators with DIFFERENT aggregations (UNION ALL)
            Arguments.of(
                "Single dimension with multiple values, multiple indicators with different aggregations, 'aplicacio' agrupacio",
                Map.of("departament", List.of("RRHH", "IT")),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.MES)
                ),
                "aplicacio",
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "MAX(total_sum_visites_MES) as total_sum_visites_MES, " +
                    "MAX(last_seen_sessions_DIA) as last_seen_sessions_DIA " +
                    "FROM (" +
                    "SELECT agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES, " +
                    "null AS last_seen_sessions_DIA " +
                    "FROM ( " +
                    "   SELECT t.anualitat, t.trimestre, t.mes, " +
                    "   JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS agrupacio, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "   WHERE f.entorn_app_id = :entornAppId " +
                    "   AND t.data BETWEEN :dataInici AND :dataFi " +
                    "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                    ") " +
                    "GROUP BY agrupacio " +
                    "UNION ALL " +
                    "SELECT agrupacio, " +
                    "null AS total_sum_visites_MES, " +
                    "CASE WHEN SUM(sum_fets_sessions_DIA) > 0 THEN MAX(data) ELSE NULL END AS last_seen_sessions_DIA " +
                    "FROM ( " +
                    "   SELECT t.data, " +
                    "   JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS agrupacio, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_DIA " +
                    "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "   WHERE f.entorn_app_id = :entornAppId " +
                    "   AND t.data BETWEEN :dataInici AND :dataFi " +
                    "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                    ") " +
                    "GROUP BY agrupacio) " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            ),
            // Test 5: Multiple dimensions with mixed values, multiple indicators with same aggregation unit, "departament" agrupacio
            Arguments.of(
                "Multiple dimensions with mixed values, multiple indicators with same aggregation unit, 'departament' agrupacio",
                new LinkedHashMap<>() {{
                    put("departament", List.of("RRHH", "IT"));
                    put("area", List.of("Finance"));
                }},
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.MES)
                ),
                "departament",
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES, " +
                    "SUM(sum_fets_sessions_MES) AS total_sum_sessions_MES " +
                    "FROM ( " +
                    "   SELECT t.anualitat, t.trimestre, t.mes, " +
                    "   JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS agrupacio, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES, " +
                    "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_MES " +
                    "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "   WHERE f.entorn_app_id = :entornAppId " +
                    "   AND t.data BETWEEN :dataInici AND :dataFi " +
                    "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "   AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                    "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                    ") " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            )
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetTaulaUnionQueryTestCases")
    void testGetTaulaUnionQueryParameterized(
        String testName,
        Map<String, List<String>> dimensionsFiltre,
        List<IndicadorAgregacio> indicadorsAgregacio,
        String dimensioAgrupacioCodi,
        String[] expectedQueryFragments) {

        // Act
        String query = removeConsecutiveSpaces(dialect.getTaulaQuery(dimensionsFiltre, indicadorsAgregacio, dimensioAgrupacioCodi, null));

        Arrays.stream(expectedQueryFragments)
            .filter(fragment -> !query.contains(fragment))
            .forEach(fragment -> System.out.println("Missing fragment: " + fragment));

        assertNotNull(query);
        Arrays.stream(expectedQueryFragments)
            .forEach(fragment -> assertTrue(query.contains(fragment), "Query should contain: " + fragment + "\nActual query: " + query));
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetTaulaUnionQueryTestCases() {
        return Stream.of(
            // Test 1: Multiple indicators with different unitatAgregacio
            Arguments.of(
                "Multiple indicators with different unitatAgregacio",
                Map.of("departament", List.of("RRHH")),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.TRIMESTRE)
                ),
                "area",
                new String[] {
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("MAX(average_result_visites_MES) as average_result_visites_MES"),
                    removeConsecutiveSpaces("MAX(average_result_sessions_TRIMESTRE) as average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("FROM ("),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("AVG(sum_fets_visites_MES) AS average_result_visites_MES"),
                    removeConsecutiveSpaces("null AS average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("FROM ( " +
                        "   SELECT t.anualitat, t.trimestre, t.mes, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                        ") " +
                        "GROUP BY agrupacio "),
                    removeConsecutiveSpaces("UNION ALL "),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("AVG(sum_fets_sessions_TRIMESTRE) AS average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("null AS average_result_visites_MES"),
                    removeConsecutiveSpaces("FROM ( " +
                        "   SELECT t.anualitat, t.trimestre, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_TRIMESTRE " +
                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                        ") " +
                        "GROUP BY agrupacio"),
                    removeConsecutiveSpaces(") " +
                        "GROUP BY agrupacio " +
                        "ORDER BY agrupacio")
                }
            ),
            // Test 2: Mix of AVERAGE and data aggregations
            Arguments.of(
                "Mix of AVERAGE and data aggregations",
                Map.of("departament", List.of("RRHH")),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES)
                ),
                "area",
                new String[]{
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("MAX(average_result_visites_MES) as average_result_visites_MES"),
                    removeConsecutiveSpaces("MAX(first_seen_sessions_DIA) as first_seen_sessions_DIA"),
                    removeConsecutiveSpaces("FROM ("),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("AVG(sum_fets_visites_MES) AS average_result_visites_MES"),
                    removeConsecutiveSpaces("null AS first_seen_sessions_DIA"),
                    removeConsecutiveSpaces("FROM ( " +
                        "   SELECT t.anualitat, t.trimestre, t.mes, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                        ") " +
                        "GROUP BY agrupacio "),
                    removeConsecutiveSpaces("UNION ALL "),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("CASE WHEN SUM(sum_fets_sessions_DIA) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions_DIA"),
                    removeConsecutiveSpaces("null AS average_result_visites_MES"),
                    removeConsecutiveSpaces("FROM ( " +
                        "   SELECT t.data, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_DIA " +
                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                        ") " +
                        "GROUP BY agrupacio"),
                    removeConsecutiveSpaces(") "),
                    removeConsecutiveSpaces("GROUP BY agrupacio " +
                        "ORDER BY agrupacio")
                }
            ),
            // Test 3: Scenario from issue description
            Arguments.of(
                "Scenario from issue description",
                Map.of("departament", List.of("RRHH")),
                List.of(
                    createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.TRIMESTRE),
                    createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES)
                ),
                "area",
                new String[]{
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("MAX(average_result_visites_MES) as average_result_visites_MES"),
                    removeConsecutiveSpaces("MAX(average_result_sessions_TRIMESTRE) as average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("FROM ("),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("AVG(sum_fets_visites_MES) AS average_result_visites_MES"),
                    removeConsecutiveSpaces("null AS average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
                        "GROUP BY agrupacio "),
                    removeConsecutiveSpaces("UNION ALL "),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("AVG(sum_fets_sessions_TRIMESTRE) AS average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("null AS average_result_visites_MES"),
                    removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_TRIMESTRE " +
                        "   FROM com_est_fet f " +
                        "   JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
                        "GROUP BY agrupacio"),
                    removeConsecutiveSpaces(") "),
                    removeConsecutiveSpaces("GROUP BY agrupacio " +
                        "ORDER BY agrupacio")
                }
            ),
            // Test 4: Scenario from issue description
            Arguments.of(
                "Scenario from issue description",
                Map.of("departament", List.of("RRHH")),
                List.of(
                    createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.TRIMESTRE),
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES)
                ),
                "area",
                new String[]{
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("MAX(total_sum_visites_MES) as total_sum_visites_MES"),
                    removeConsecutiveSpaces("MAX(average_result_sessions_TRIMESTRE) as average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("FROM ("),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("SUM(sum_fets_visites_MES) AS total_sum_visites_MES"),
                    removeConsecutiveSpaces("null AS average_result_sessions_TRIMESTRE "),
                    removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
                        "GROUP BY agrupacio "),
                    removeConsecutiveSpaces("UNION ALL "),
                    removeConsecutiveSpaces("SELECT agrupacio, "),
                    removeConsecutiveSpaces("AVG(sum_fets_sessions_TRIMESTRE) AS average_result_sessions_TRIMESTRE"),
                    removeConsecutiveSpaces("null AS total_sum_visites_MES "),
                    removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, " +
                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio,   " +
                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_TRIMESTRE " +
                        "   FROM com_est_fet f " +
                        "   JOIN com_est_temps t ON f.temps_id = t.id " +
                        "   WHERE f.entorn_app_id = :entornAppId " +
                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                        "   GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
                        "GROUP BY agrupacio"),
                    removeConsecutiveSpaces(") "),
                    removeConsecutiveSpaces("GROUP BY agrupacio " +
                        "ORDER BY agrupacio")
                }
            )
        );
    }

    private static IndicadorAgregacio createIndicadorAgregacio(String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        IndicadorAgregacio indicadorAgregacio = new IndicadorAgregacio();
        indicadorAgregacio.setIndicadorCodi(indicadorCodi);
        indicadorAgregacio.setAgregacio(agregacio);
        indicadorAgregacio.setUnitatAgregacio(unitatAgregacio);
        return indicadorAgregacio;
    }
}
