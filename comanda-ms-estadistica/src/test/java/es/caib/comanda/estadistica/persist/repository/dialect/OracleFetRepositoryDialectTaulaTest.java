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
        String query = removeConsecutiveSpaces(dialect.getTaulaQuery(dimensionsFiltre, indicadorsAgregacio, dimensioAgrupacioCodi));

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
                                "SUM(sum_fets_visites) AS total_sum_visites " +
                                "FROM ( " +
                                "    SELECT t.data as data, " +
                                " JSON_VALUE(f.dimensions_json, '$.\"departament\"')  AS agrupacio, " +
                                " SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
                                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "    WHERE f.entorn_app_id = :entornAppId " +
                                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                                " GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                                ") " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 2: Empty dimensions, multiple indicators with different aggregations, "area" agrupacio
                Arguments.of(
                        "Empty dimensions, multiple indicators with different aggregations, 'area' agrupacio",
                        new HashMap<>(),
                        List.of(
                                createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                                createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES)
                        ),
                        "area",
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "AVG(sum_fets_sessions) AS average_result_sessions " +
                                "FROM ( " +
                                "   SELECT t.anualitat, t.trimestre, t.mes, " +
                                "   JSON_VALUE(f.dimensions_json, '$.\"area\"')  AS agrupacio, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "   WHERE f.entorn_app_id = :entornAppId " +
                                "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                                ") " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 3: Single dimension with single value, multiple indicators with different aggregations, "usuari" agrupacio
                Arguments.of(
                        "Single dimension with single value, multiple indicators with different aggregations, 'usuari' agrupacio",
                        Map.of("departament", List.of("RRHH")),
                        List.of(
                                createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                                createIndicadorAgregacio("sessions", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES)
                        ),
                        "usuari",
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "CASE WHEN SUM(sum_fets_sessions) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions " +
                                "FROM ( " +
                                "   SELECT t.data as data, " +
                                "   JSON_VALUE(f.dimensions_json, '$.\"usuari\"')  AS agrupacio, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "   WHERE f.entorn_app_id = :entornAppId " +
                                "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
                                ") " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 4: Single dimension with multiple values, multiple indicators with different aggregations, "aplicacio" agrupacio
                Arguments.of(
                        "Single dimension with multiple values, multiple indicators with different aggregations, 'aplicacio' agrupacio",
                        Map.of("departament", List.of("RRHH", "IT")),
                        List.of(
                                createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                                createIndicadorAgregacio("sessions", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.MES)
                        ),
                        "aplicacio",
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "CASE WHEN SUM(sum_fets_sessions) > 0 THEN MAX(data) ELSE NULL END AS last_seen_sessions " +
                                "   FROM ( " +
                                "   SELECT t.data as data, " +
                                "   JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"')  AS agrupacio, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "   WHERE f.entorn_app_id = :entornAppId " +
                                "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                                ") " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 5: Multiple dimensions with mixed values, multiple indicators with different aggregations, "departament" agrupacio
                Arguments.of(
                        "Multiple dimensions with mixed values, multiple indicators with different aggregations, 'departament' agrupacio",
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
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "SUM(sum_fets_sessions) AS total_sum_sessions " +
                                "FROM ( " +
                                "   SELECT t.data as data, " +
                                "   JSON_VALUE(f.dimensions_json, '$.\"departament\"')  AS agrupacio, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "   WHERE f.entorn_app_id = :entornAppId " +
                                "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "   AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
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
        String query = removeConsecutiveSpaces(dialect.getTaulaQuery(dimensionsFiltre, indicadorsAgregacio, dimensioAgrupacioCodi));

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
                                removeConsecutiveSpaces("MAX(average_result_visites) as average_result_visites"),
                                removeConsecutiveSpaces("MAX(average_result_sessions) as average_result_sessions"),
                                removeConsecutiveSpaces("FROM ("),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("AVG(sum_fets_visites) AS average_result_visites"),
                                removeConsecutiveSpaces("null AS average_result_sessions"),
                                removeConsecutiveSpaces("FROM ( " +
                                        "   SELECT t.anualitat, t.trimestre, t.mes, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
                                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                        "   WHERE f.entorn_app_id = :entornAppId " +
                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                                        ") " +
                                        "GROUP BY agrupacio "),
                                removeConsecutiveSpaces("UNION ALL "),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("AVG(sum_fets_sessions) AS average_result_sessions"),
                                removeConsecutiveSpaces("null AS average_result_visites"),
                                removeConsecutiveSpaces("FROM ( " +
                                        "   SELECT t.anualitat, t.trimestre, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
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
                                removeConsecutiveSpaces("MAX(average_result_visites) as average_result_visites"),
                                removeConsecutiveSpaces("MAX(first_seen_sessions) as first_seen_sessions"),
                                removeConsecutiveSpaces("FROM ("),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("AVG(sum_fets_visites) AS average_result_visites"),
                                removeConsecutiveSpaces("null AS first_seen_sessions"),
                                removeConsecutiveSpaces("FROM ( " +
                                        "   SELECT t.anualitat, t.trimestre, t.mes, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
                                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                        "   WHERE f.entorn_app_id = :entornAppId " +
                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                                        ") " +
                                        "GROUP BY agrupacio "),
                                removeConsecutiveSpaces("UNION ALL "),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("CASE WHEN SUM(sum_fets_sessions) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions"),
                                removeConsecutiveSpaces("null AS average_result_visites"),
                                removeConsecutiveSpaces("FROM ( " +
                                        "   SELECT t.data as data, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
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
                                removeConsecutiveSpaces("MAX(average_result_visites) as average_result_visites"),
                                removeConsecutiveSpaces("MAX(average_result_sessions) as average_result_sessions"),
                                removeConsecutiveSpaces("FROM ("),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("AVG(sum_fets_visites) AS average_result_visites"),
                                removeConsecutiveSpaces("null AS average_result_sessions"),
                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
                                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                        "   WHERE f.entorn_app_id = :entornAppId " +
                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
                                        "GROUP BY agrupacio "),
                                removeConsecutiveSpaces("UNION ALL "),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("AVG(sum_fets_sessions) AS average_result_sessions"),
                                removeConsecutiveSpaces("null AS average_result_visites"),
                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
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
                                // SELECT agrupacio, MAX(average_result_sessions) as average_result_sessions, MAX(total_sum_visites) as total_sum_visites FROM (
                                // SELECT agrupacio, AVG(sum_fets_sessions) AS average_result_sessions, null AS total_sum_visites FROM ( SELECT t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$."area"') AS agrupacio,SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$."sessions"'))) AS sum_fets_sessions FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi AND JSON_VALUE(f.dimensions_json, '$."departament"') = 'RRHH' GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$."area"') ) GROUP BY agrupacio UNION ALL SELECT agrupacio, null AS average_result_sessions, SUM(sum_fets_visites) AS total_sum_visites FROM ( SELECT t.anualitat, t.mes, JSON_VALUE(f.dimensions_json, '$."area"') AS agrupacio,SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$."visites"'))) AS sum_fets_visites FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi AND JSON_VALUE(f.dimensions_json, '$."departament"') = 'RRHH' GROUP BY t.anualitat, t.mes, JSON_VALUE(f.dimensions_json, '$."area"') ) GROUP BY agrupacio) GROUP BY agrupacio ORDER BY agrupacio
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("MAX(total_sum_visites) as total_sum_visites"),
                                removeConsecutiveSpaces("MAX(average_result_sessions) as average_result_sessions"),
                                removeConsecutiveSpaces("FROM ("),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("SUM(sum_fets_visites) AS total_sum_visites"),
                                removeConsecutiveSpaces("null AS average_result_sessions "),
                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
                                        "   FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                        "   WHERE f.entorn_app_id = :entornAppId " +
                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
                                        "GROUP BY agrupacio "),
                                removeConsecutiveSpaces("UNION ALL "),
                                removeConsecutiveSpaces("SELECT agrupacio, "),
                                removeConsecutiveSpaces("AVG(sum_fets_sessions) AS average_result_sessions"),
                                removeConsecutiveSpaces("null AS total_sum_visites "),
                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, " +
                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio,   " +
                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
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
