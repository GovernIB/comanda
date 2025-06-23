package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
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

public class OracleFetRepositoryDialectGraficTest {

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
    @MethodSource("provideGetGraficUnIndicadorQueryTestCases")
    void testGetGraficUnIndicadorQueryParameterized(String testName, Map<String, List<String>> dimensionsFiltre, 
                                                   IndicadorAgregacio indicadorAgregacio, 
                                                   PeriodeUnitat tempsAgregacio, 
                                                   String expectedQuery) {
        // Act
        String query = removeConsecutiveSpaces(dialect.getGraficUnIndicadorQuery(dimensionsFiltre, indicadorAgregacio, tempsAgregacio));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetGraficUnIndicadorQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions, SUM aggregation, MES period
                Arguments.of(
                        "Null dimensions, SUM aggregation, MES period",
                        null,
                        createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY anualitat, trimestre, mes " +
                                "ORDER BY agrupacio")
                ),

                // Test 2: Empty dimensions, AVERAGE aggregation, SETMANA period
                Arguments.of(
                        "Empty dimensions, AVERAGE aggregation, SETMANA period",
                        new HashMap<>(),
                        createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "AVG(sum_fets) AS average_result " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana) " +
                                "GROUP BY anualitat, trimestre, mes " +
                                "ORDER BY agrupacio")
                ),

                // Test 3: Single dimension with single value, PERCENTAGE aggregation, TRIMESTRE period
                Arguments.of(
                        "Single dimension with single value, PERCENTAGE aggregation, TRIMESTRE period",
                        Map.of("departament", List.of("RRHH")),
                        createIndicadorAgregacio("visites", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.MES),
                        PeriodeUnitat.TRIMESTRE,
                        removeConsecutiveSpaces("SELECT trimestre || '/' || anualitat as agrupacio, " +
                                "SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY anualitat, trimestre " +
                                "ORDER BY agrupacio")
                ),

                // Test 4: Single dimension with multiple values, FIRST_SEEN aggregation, ANY period
                Arguments.of(
                        "Single dimension with multiple values, FIRST_SEEN aggregation, ANY period",
                        Map.of("departament", List.of("RRHH", "IT")),
                        createIndicadorAgregacio("visites", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES),
                        PeriodeUnitat.ANY,
                        removeConsecutiveSpaces("SELECT anualitat as agrupacio, " +
                                "CASE WHEN SUM(sum_fets) > 0 THEN MIN(data) ELSE NULL END AS first_seen " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY anualitat " +
                                "ORDER BY agrupacio")
                ),

                // Test 5: Multiple dimensions with mixed values, LAST_SEEN aggregation, DIA period
                Arguments.of(
                        "Multiple dimensions with mixed values, LAST_SEEN aggregation, DIA period",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        createIndicadorAgregacio("visites", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.DIA),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "CASE WHEN SUM(sum_fets) > 0 THEN MAX(data) ELSE NULL END AS last_seen " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia) " +
                                "GROUP BY anualitat, trimestre, mes " +
                                "ORDER BY agrupacio")
                ),

                // Test 6: Different indicator code, SUM aggregation, MES period
                Arguments.of(
                        "Different indicator code, SUM aggregation, MES period",
                        Map.of("departament", List.of("RRHH")),
                        createIndicadorAgregacio("sessions", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY anualitat, trimestre, mes " +
                                "ORDER BY agrupacio")
                )
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetGraficUnIndicadorAmbDescomposicioQueryWithTempsAgregacioTestCases")
    void testGetGraficUnIndicadorAmbDescomposicioQueryWithTempsAgregacioParameterized(
            String testName, 
            Map<String, List<String>> dimensionsFiltre, 
            IndicadorAgregacio indicadorAgregacio, 
            String dimensioDescomposicioCodi, 
            PeriodeUnitat tempsAgregacio, 
            String expectedQuery) {

        // Act
        String query = removeConsecutiveSpaces(dialect.getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(
                dimensionsFiltre,
                indicadorAgregacio,
                dimensioDescomposicioCodi,
                tempsAgregacio));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetGraficUnIndicadorAmbDescomposicioQueryWithTempsAgregacioTestCases() {
        return Stream.of(
                // Test 1: Null dimensions, SUM aggregation, "aplicacio" descomposicio, MES period
                Arguments.of(
                        "Null dimensions, SUM aggregation, 'aplicacio' descomposicio, MES period",
                        null,
                        createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                        "aplicacio",
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "descomposicio, " +
                                "SUM(sum_fets) AS total_sum " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS descomposicio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                                ") " +
                                "GROUP BY anualitat, trimestre, mes, descomposicio " +
                                "ORDER BY agrupacio, descomposicio")
                ),

                // Test 2: Empty dimensions, AVERAGE aggregation, "departament" descomposicio, SETMANA period
                Arguments.of(
                        "Empty dimensions, AVERAGE aggregation, 'departament' descomposicio, SETMANA period",
                        new HashMap<>(),
                        createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA),
                        "departament",
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "descomposicio, " +
                                "AVG(sum_fets) AS average_result " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, " +
                                "JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS descomposicio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                                ") " +
                                "GROUP BY anualitat, trimestre, mes, descomposicio " +
                                "ORDER BY agrupacio, descomposicio")
                ),

                // Test 3: Single dimension with single value, PERCENTAGE aggregation, "area" descomposicio, TRIMESTRE period
                Arguments.of(
                        "Single dimension with single value, PERCENTAGE aggregation, 'area' descomposicio, TRIMESTRE period",
                        Map.of("departament", List.of("RRHH")),
                        createIndicadorAgregacio("visites", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.MES),
                        "area",
                        PeriodeUnitat.TRIMESTRE,
                        removeConsecutiveSpaces("SELECT trimestre || '/' || anualitat as agrupacio, " +
                                "descomposicio, " +
                                "SUM(sum_fets) AS total_sum" +
                                " FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "JSON_VALUE(f.dimensions_json, '$.\"area\"') AS descomposicio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                                ") " +
                                "GROUP BY anualitat, trimestre, descomposicio " +
                                "ORDER BY agrupacio, descomposicio")
                ),

                // Test 4: Single dimension with multiple values, FIRST_SEEN aggregation, "usuari" descomposicio, ANY period
                Arguments.of(
                        "Single dimension with multiple values, FIRST_SEEN aggregation, 'usuari' descomposicio, ANY period",
                        Map.of("departament", List.of("RRHH", "IT")),
                        createIndicadorAgregacio("visites", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES),
                        "usuari",
                        PeriodeUnitat.ANY,
                        removeConsecutiveSpaces("SELECT anualitat as agrupacio, " +
                                "descomposicio, " +
                                "CASE WHEN SUM(sum_fets) > 0 THEN MIN(data) ELSE NULL END AS first_seen" +
                                " FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "JSON_VALUE(f.dimensions_json, '$.\"usuari\"') AS descomposicio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
                                ") " +
                                "GROUP BY anualitat, descomposicio " +
                                "ORDER BY agrupacio, descomposicio")
                ),

                // Test 5: Multiple dimensions with mixed values, LAST_SEEN aggregation, "aplicacio" descomposicio, DIA period
                Arguments.of(
                        "Multiple dimensions with mixed values, LAST_SEEN aggregation, 'aplicacio' descomposicio, DIA period",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        createIndicadorAgregacio("visites", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.DIA),
                        "aplicacio",
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "descomposicio, " +
                                "CASE WHEN SUM(sum_fets) > 0 THEN MAX(data) ELSE NULL END AS last_seen " +
                                " FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                                "JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS descomposicio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                                ") " +
                                "GROUP BY anualitat, trimestre, mes, descomposicio " +
                                "ORDER BY agrupacio, descomposicio")
                ),

                // Test 6: Different indicator code, SUM aggregation, "departament" descomposicio, MES period
                Arguments.of(
                        "Different indicator code, SUM aggregation, 'departament' descomposicio, MES period",
                        Map.of("area", List.of("Finance")),
                        createIndicadorAgregacio("sessions", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                        "departament",
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT mes || '/' || anualitat as agrupacio, " +
                                "descomposicio, " +
                                "SUM(sum_fets) AS total_sum" +
                                " FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS descomposicio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                                ") " +
                                "GROUP BY anualitat, trimestre, mes, descomposicio " +
                                "ORDER BY agrupacio, descomposicio")
                )
        );
    }




    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetGraficUnIndicadorAmbDescomposicioQueryTestCases")
    void testGetGraficUnIndicadorAmbDescomposicioQueryParameterized(
            String testName,
            Map<String, List<String>> dimensionsFiltre,
            IndicadorAgregacio indicadorAgregacio,
            String dimensioDescomposicioCodi,
            String expectedQuery) {

        // Act
        String query = removeConsecutiveSpaces(dialect.getGraficUnIndicadorAmbDescomposicioQuery(
                dimensionsFiltre, indicadorAgregacio, dimensioDescomposicioCodi));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetGraficUnIndicadorAmbDescomposicioQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions, SUM aggregation, "aplicacio" descomposicio
                Arguments.of(
                        "Null dimensions, SUM aggregation, 'aplicacio' descomposicio",
                        null,
                        createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                        "aplicacio",
                        removeConsecutiveSpaces("SELECT  JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY  JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                                "ORDER BY agrupacio")
                ),

                // Test 2: Empty dimensions, AVERAGE aggregation, "departament" descomposicio
                Arguments.of(
                        "Empty dimensions, AVERAGE aggregation, 'departament' descomposicio",
                        new HashMap<>(),
                        createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
                        "departament",
                        removeConsecutiveSpaces("SELECT  JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY  JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                                "ORDER BY agrupacio")
                ),

                // Test 3: Single dimension with single value, PERCENTAGE aggregation, "area" descomposicio
                Arguments.of(
                        "Single dimension with single value, PERCENTAGE aggregation, 'area' descomposicio",
                        Map.of("departament", List.of("RRHH")),
                        createIndicadorAgregacio("visites", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.MES),
                        "area",
                        removeConsecutiveSpaces("SELECT  JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "GROUP BY  JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                                "ORDER BY agrupacio")
                ),

                // Test 4: Single dimension with multiple values, FIRST_SEEN aggregation, "usuari" descomposicio
                Arguments.of(
                        "Single dimension with multiple values, FIRST_SEEN aggregation, 'usuari' descomposicio",
                        Map.of("departament", List.of("RRHH", "IT")),
                        createIndicadorAgregacio("visites", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES),
                        "usuari",
                        removeConsecutiveSpaces("SELECT  JSON_VALUE(f.dimensions_json, '$.\"usuari\"') AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "GROUP BY  JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
                                "ORDER BY agrupacio")
                ),

                // Test 5: Multiple dimensions with mixed values, LAST_SEEN aggregation, "aplicacio" descomposicio
                Arguments.of(
                        "Multiple dimensions with mixed values, LAST_SEEN aggregation, 'aplicacio' descomposicio",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        createIndicadorAgregacio("visites", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.MES),
                        "aplicacio",
                        removeConsecutiveSpaces("SELECT  JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "GROUP BY  JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                                "ORDER BY agrupacio")
                ),

                // Test 6: Different indicator code, SUM aggregation, "departament" descomposicio
                Arguments.of(
                        "Different indicator code, SUM aggregation, 'departament' descomposicio",
                        Map.of("area", List.of("Finance")),
                        createIndicadorAgregacio("sessions", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                        "departament",
                        removeConsecutiveSpaces("SELECT  JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "GROUP BY  JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                                "ORDER BY agrupacio")
                )
        );
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetGraficVarisIndicadorsQueryTestCases")
    void testGetGraficVarisIndicadorsQueryParameterized(
            String testName,
            Map<String, List<String>> dimensionsFiltre,
            List<IndicadorAgregacio> indicadorsAgregacio,
            PeriodeUnitat tempsAgregacio,
            String expectedQuery) {

        // Act
        String query = removeConsecutiveSpaces(dialect.getGraficVarisIndicadorsQuery(dimensionsFiltre, indicadorsAgregacio, tempsAgregacio));

        // Assert
        assertNotNull(query);
        assertTrue(query.equals(expectedQuery), "Query should be: " + expectedQuery + "\nActual query: " + query);
        System.out.println("Query: " + query);
    }

    private static Stream<Arguments> provideGetGraficVarisIndicadorsQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions, single indicator with SUM aggregation, MES period
                Arguments.of(
                        "Null dimensions, single indicator with SUM aggregation, MES period",
                        null,
                        List.of(createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES)),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "mes || '/' || anualitat AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 2: Empty dimensions, multiple indicators with different aggregations, SETMANA period
                Arguments.of(
                        "Empty dimensions, multiple indicators with different aggregations, SETMANA period",
                        new HashMap<>(),
                        List.of(
                            createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.SETMANA),
                            createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA)
                        ),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "AVG(sum_fets_sessions) AS average_result_sessions " +
                                "FROM ( " +
                                "SELECT mes || '/' || anualitat AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 3: Single dimension with single value, multiple indicators with different aggregations, TRIMESTRE period
                Arguments.of(
                        "Single dimension with single value, multiple indicators with different aggregations, TRIMESTRE period",
                        Map.of("departament", List.of("RRHH")),
                        List.of(
                            createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                            createIndicadorAgregacio("sessions", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES)
                        ),
                        PeriodeUnitat.TRIMESTRE,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "CASE WHEN SUM(sum_fets_sessions) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions " +
                                "FROM ( " +
                                "SELECT t.data, t.anualitat, t.trimestre, t.mes, " +
                                "trimestre || '/' || anualitat AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                                "GROUP BY t.data, t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 4: Single dimension with multiple values, multiple indicators with different aggregations, ANY period
                Arguments.of(
                        "Single dimension with multiple values, multiple indicators with different aggregations, ANY period",
                        Map.of("departament", List.of("RRHH", "IT")),
                        List.of(
                            createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                            createIndicadorAgregacio("sessions", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.MES)
                        ),
                        PeriodeUnitat.ANY,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "CASE WHEN SUM(sum_fets_sessions) > 0 THEN MAX(data) ELSE NULL END AS last_seen_sessions " +
                                "FROM ( " +
                                "SELECT t.data, t.anualitat, t.trimestre, t.mes, " +
                                "anualitat AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "GROUP BY t.data, t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 5: Multiple dimensions with mixed values, multiple indicators with different aggregations, DIA period
                Arguments.of(
                        "Multiple dimensions with mixed values, multiple indicators with different aggregations, DIA period",
                        new LinkedHashMap<>() {{
                            put("departament", List.of("RRHH", "IT"));
                            put("area", List.of("Finance"));
                        }},
                        List.of(
                            createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.DIA),
                            createIndicadorAgregacio("sessions", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.DIA)
                        ),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "SUM(sum_fets_sessions) AS total_sum_sessions " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                                "mes || '/' || anualitat AS agrupacio, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites, " +
                                "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
                                "FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                                "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                )
        );
    }

//    @ParameterizedTest(name = "{index}: {0}")
//    @MethodSource("provideGetGraficUnionQueryTestCases")
//    void testGetGraficUnionQueryParameterized(
//            String testName,
//            Map<String, List<String>> dimensionsFiltre,
//            List<IndicadorAgregacio> indicadorsAgregacio,
//            PeriodeUnitat tempsAgregacio,
//            String[] expectedQueryFragments) {
//
//        // Act
//        String query = removeConsecutiveSpaces(dialect.getGraficVarisIndicadorsQuery(dimensionsFiltre, indicadorsAgregacio, tempsAgregacio));
//
//        Arrays.stream(expectedQueryFragments)
//                .filter(fragment -> !query.contains(fragment))
//                .forEach(fragment -> System.out.println("Missing fragment: " + fragment));
//
//        assertNotNull(query);
//        Arrays.stream(expectedQueryFragments)
//                .forEach(fragment -> assertTrue(query.contains(fragment), "Query should contain: " + fragment + "\nActual query: " + query));
//        System.out.println("Query: " + query);
//    }

    // TODO: Modificar (si es permeten difirents unitats d'agregació)
//    private static Stream<Arguments> provideGetGraficUnionQueryTestCases() {
//        return Stream.of(
//
//
//                // Test 1: Multiple indicators with different unitatAgregacio
//                Arguments.of(
//                        "Multiple indicators with different unitatAgregacio",
//                        Map.of("departament", List.of("RRHH")),
//                        List.of(
//                                createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
//                                createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA)
//                        ),
//                        PeriodeUnitat.TRIMESTRE,
//                        new String[] {
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(average_result_visites) as average_result_visites"),
//                                removeConsecutiveSpaces("AVG(average_result_sessions) as average_result_sessions"),
//                                removeConsecutiveSpaces("FROM ("),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(sum_fets_visites) AS average_result_visites"),
//                                removeConsecutiveSpaces("null AS average_result_sessions"),
//                                removeConsecutiveSpaces("FROM ( " +
//                                        "   SELECT t.anualitat, t.trimestre, t.mes, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
//                                        "   FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
//                                        ") " +
//                                        "GROUP BY agrupacio "),
//                                removeConsecutiveSpaces("UNION ALL "),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(sum_fets_sessions) AS average_result_sessions"),
//                                removeConsecutiveSpaces("null AS average_result_visites"),
//                                removeConsecutiveSpaces("FROM ( " +
//                                        "   SELECT t.anualitat, t.trimestre, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
//                                        "   FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
//                                        ") " +
//                                        "GROUP BY agrupacio"),
//                                removeConsecutiveSpaces(") " +
//                                        "GROUP BY agrupacio " +
//                                        "ORDER BY agrupacio")
//                        }
//                ),
//
//                // Test 2: Mix of AVERAGE and data aggregations
//                Arguments.of(
//                        "Mix of AVERAGE and data aggregations",
//                        Map.of("departament", List.of("RRHH")),
//                        List.of(
//                                createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA),
//                                createIndicadorAgregacio("sessions", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.SETMANA)
//                        ),
//                        PeriodeUnitat.MES,
//                        new String[]{
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("MAX(average_result_visites) as average_result_visites"),
//                                removeConsecutiveSpaces("MAX(first_seen_sessions) as first_seen_sessions"),
//                                removeConsecutiveSpaces("FROM ("),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(sum_fets_visites) AS average_result_visites"),
//                                removeConsecutiveSpaces("null AS first_seen_sessions"),
//                                removeConsecutiveSpaces("FROM ( " +
//                                        "   SELECT t.anualitat, t.trimestre, t.mes, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
//                                        "   FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
//                                        ") " +
//                                        "GROUP BY agrupacio "),
//                                removeConsecutiveSpaces("UNION ALL "),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("CASE WHEN SUM(sum_fets_sessions) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions"),
//                                removeConsecutiveSpaces("null AS average_result_visites"),
//                                removeConsecutiveSpaces("FROM ( " +
//                                        "   SELECT t.data as data, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
//                                        "   FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.data, JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
//                                        ") " +
//                                        "GROUP BY agrupacio"),
//                                removeConsecutiveSpaces(") "),
//                                removeConsecutiveSpaces("GROUP BY agrupacio " +
//                                        "ORDER BY agrupacio")
//                        }
//                ),
//
//                // Test 3: Scenario from issue description
//                Arguments.of(
//                        "Scenario from issue description",
//                        Map.of("departament", List.of("RRHH")),
//                        List.of(
//                                createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.TRIMESTRE),
//                                createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES)
//                        ),
//                        PeriodeUnitat.ANY,
//                        new String[]{
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("MAX(average_result_visites) as average_result_visites"),
//                                removeConsecutiveSpaces("MAX(average_result_sessions) as average_result_sessions"),
//                                removeConsecutiveSpaces("FROM ("),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(sum_fets_visites) AS average_result_visites"),
//                                removeConsecutiveSpaces("null AS average_result_sessions"),
//                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
//                                        "   FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
//                                        "GROUP BY agrupacio "),
//                                removeConsecutiveSpaces("UNION ALL "),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(sum_fets_sessions) AS average_result_sessions"),
//                                removeConsecutiveSpaces("null AS average_result_visites"),
//                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
//                                        "   FROM cmd_est_fet f " +
//                                        "   JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
//                                        "GROUP BY agrupacio"),
//                                removeConsecutiveSpaces(") "),
//                                removeConsecutiveSpaces("GROUP BY agrupacio " +
//                                        "ORDER BY agrupacio")
//                        }
//                ),
//                // Test 4: Scenario from issue description
//                Arguments.of(
//                        "Scenario from issue description",
//                        Map.of("departament", List.of("RRHH")),
//                        List.of(
//                                createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.DIA),
//                                createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES)
//                        ),
//                        PeriodeUnitat.MES,
//                        new String[]{
//                                // SELECT agrupacio, MAX(average_result_sessions) as average_result_sessions, MAX(total_sum_visites) as total_sum_visites FROM (
//                                // SELECT agrupacio, AVG(sum_fets_sessions) AS average_result_sessions, null AS total_sum_visites FROM ( SELECT t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$."area"') AS agrupacio,SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$."sessions"'))) AS sum_fets_sessions FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi AND JSON_VALUE(f.dimensions_json, '$."departament"') = 'RRHH' GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$."area"') ) GROUP BY agrupacio UNION ALL SELECT agrupacio, null AS average_result_sessions, SUM(sum_fets_visites) AS total_sum_visites FROM ( SELECT t.anualitat, t.mes, JSON_VALUE(f.dimensions_json, '$."area"') AS agrupacio,SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$."visites"'))) AS sum_fets_visites FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi AND JSON_VALUE(f.dimensions_json, '$."departament"') = 'RRHH' GROUP BY t.anualitat, t.mes, JSON_VALUE(f.dimensions_json, '$."area"') ) GROUP BY agrupacio) GROUP BY agrupacio ORDER BY agrupacio
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("MAX(total_sum_visites) as total_sum_visites"),
//                                removeConsecutiveSpaces("MAX(average_result_sessions) as average_result_sessions"),
//                                removeConsecutiveSpaces("FROM ("),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("SUM(sum_fets_visites) AS total_sum_visites"),
//                                removeConsecutiveSpaces("null AS average_result_sessions "),
//                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites " +
//                                        "   FROM cmd_est_fet f JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, t.mes, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
//                                        "GROUP BY agrupacio "),
//                                removeConsecutiveSpaces("UNION ALL "),
//                                removeConsecutiveSpaces("SELECT agrupacio, "),
//                                removeConsecutiveSpaces("AVG(sum_fets_sessions) AS average_result_sessions"),
//                                removeConsecutiveSpaces("null AS total_sum_visites "),
//                                removeConsecutiveSpaces("FROM ( SELECT t.anualitat, t.trimestre, " +
//                                        "   JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio,   " +
//                                        "   SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions " +
//                                        "   FROM cmd_est_fet f " +
//                                        "   JOIN cmd_est_temps t ON f.temps_id = t.id " +
//                                        "   WHERE f.entorn_app_id = :entornAppId " +
//                                        "   AND t.data BETWEEN :dataInici AND :dataFi " +
//                                        "   AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
//                                        "   GROUP BY t.anualitat, t.trimestre, JSON_VALUE(f.dimensions_json, '$.\"area\"') ) " +
//                                        "GROUP BY agrupacio"),
//                                removeConsecutiveSpaces(") "),
//                                removeConsecutiveSpaces("GROUP BY agrupacio " +
//                                        "ORDER BY agrupacio")
//                        }
//                )
//        );
//    }

    private static IndicadorAgregacio createIndicadorAgregacio(String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        IndicadorAgregacio indicadorAgregacio = new IndicadorAgregacio();
        indicadorAgregacio.setIndicadorCodi(indicadorCodi);
        indicadorAgregacio.setAgregacio(agregacio);
        indicadorAgregacio.setUnitatAgregacio(unitatAgregacio);
        return indicadorAgregacio;
    }
}
