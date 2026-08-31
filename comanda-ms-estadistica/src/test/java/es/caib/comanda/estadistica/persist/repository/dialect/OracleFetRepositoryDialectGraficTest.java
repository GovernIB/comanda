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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                    "GROUP BY anualitat, trimestre, mes " +
                    "ORDER BY agrupacio")
            ),

            // Test 2: Empty dimensions, AVERAGE aggregation, SETMANA period (tempsAgregacio = MES)
            Arguments.of(
                "Empty dimensions, AVERAGE aggregation, SETMANA period",
                new HashMap<>(),
                createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA),
                PeriodeUnitat.MES,
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "AVG(sum_fets_visites_SETMANA) AS average_result_visites_SETMANA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_SETMANA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || trimestre AS agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat AS agrupacio, " +
                    "CASE WHEN SUM(sum_fets_visites_DIA) > 0 THEN MIN(data) ELSE NULL END AS first_seen_visites_DIA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia) " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "CASE WHEN SUM(sum_fets_visites_DIA) > 0 THEN MAX(data) ELSE NULL END AS last_seen_visites_DIA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "SUM(sum_fets_sessions_MES) AS total_sum_sessions_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, descomposicio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS descomposicio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, descomposicio, " +
                    "AVG(sum_fets_visites_SETMANA) AS average_result_visites_SETMANA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, " +
                    "JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS descomposicio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_SETMANA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || trimestre AS agrupacio, descomposicio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "JSON_VALUE(f.dimensions_json, '$.\"area\"') AS descomposicio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat AS agrupacio, descomposicio, " +
                    "CASE WHEN SUM(sum_fets_visites_DIA) > 0 THEN MIN(data) ELSE NULL END AS first_seen_visites_DIA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "JSON_VALUE(f.dimensions_json, '$.\"usuari\"') AS descomposicio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, descomposicio, " +
                    "CASE WHEN SUM(sum_fets_visites_DIA) > 0 THEN MAX(data) ELSE NULL END AS last_seen_visites_DIA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS descomposicio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, descomposicio, " +
                    "SUM(sum_fets_sessions_MES) AS total_sum_sessions_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS descomposicio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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
                removeConsecutiveSpaces("SELECT JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                    "ORDER BY agrupacio")
            ),

            // Test 2: Empty dimensions, AVERAGE aggregation, "departament" descomposicio
            Arguments.of(
                "Empty dimensions, AVERAGE aggregation, 'departament' descomposicio",
                new HashMap<>(),
                createIndicadorAgregacio("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
                "departament",
                removeConsecutiveSpaces("SELECT JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
                    "ORDER BY agrupacio")
            ),

            // Test 3: Single dimension with single value, PERCENTAGE aggregation, "area" descomposicio
            Arguments.of(
                "Single dimension with single value, PERCENTAGE aggregation, 'area' descomposicio",
                Map.of("departament", List.of("RRHH")),
                createIndicadorAgregacio("visites", TableColumnsEnum.PERCENTAGE, PeriodeUnitat.MES),
                "area",
                removeConsecutiveSpaces("SELECT JSON_VALUE(f.dimensions_json, '$.\"area\"') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                    "GROUP BY JSON_VALUE(f.dimensions_json, '$.\"area\"') " +
                    "ORDER BY agrupacio")
            ),

            // Test 4: Single dimension with multiple values, FIRST_SEEN aggregation, "usuari" descomposicio
            Arguments.of(
                "Single dimension with multiple values, FIRST_SEEN aggregation, 'usuari' descomposicio",
                Map.of("departament", List.of("RRHH", "IT")),
                createIndicadorAgregacio("visites", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES),
                "usuari",
                removeConsecutiveSpaces("SELECT JSON_VALUE(f.dimensions_json, '$.\"usuari\"') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "GROUP BY JSON_VALUE(f.dimensions_json, '$.\"usuari\"') " +
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
                removeConsecutiveSpaces("SELECT JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                    "GROUP BY JSON_VALUE(f.dimensions_json, '$.\"aplicacio\"') " +
                    "ORDER BY agrupacio")
            ),

            // Test 6: Different indicator code, SUM aggregation, "departament" descomposicio
            Arguments.of(
                "Different indicator code, SUM aggregation, 'departament' descomposicio",
                Map.of("area", List.of("Finance")),
                createIndicadorAgregacio("sessions", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                "departament",
                removeConsecutiveSpaces("SELECT JSON_VALUE(f.dimensions_json, '$.\"departament\"') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"area\"') = 'Finance' " +
                    "GROUP BY JSON_VALUE(f.dimensions_json, '$.\"departament\"') " +
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
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, " +
                    "anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            ),

            // Test 2: Empty dimensions, multiple indicators with same aggregation unit, SETMANA period
            Arguments.of(
                "Empty dimensions, multiple indicators with same aggregation unit, SETMANA period",
                new HashMap<>(),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.SETMANA),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.SETMANA)
                ),
                PeriodeUnitat.MES,
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "SUM(sum_fets_visites_SETMANA) AS total_sum_visites_SETMANA, " +
                    "AVG(sum_fets_sessions_SETMANA) AS average_result_sessions_SETMANA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, " +
                    "anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_SETMANA, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_SETMANA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId " +
                    "AND t.data BETWEEN :dataInici AND :dataFi " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana) " +
                    "GROUP BY agrupacio " +
                    "ORDER BY agrupacio")
            ),

            // Test 3: Single dimension, mixed aggregations requiring UNION (SUM MES + FIRST_SEEN)
            Arguments.of(
                "Single dimension, mixed aggregations requiring UNION (SUM MES + FIRST_SEEN)",
                Map.of("departament", List.of("RRHH")),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.FIRST_SEEN, PeriodeUnitat.MES)
                ),
                PeriodeUnitat.TRIMESTRE,
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "MAX(total_sum_visites_MES) as total_sum_visites_MES, " +
                    "MAX(first_seen_sessions_DIA) as first_seen_sessions_DIA " +
                    "FROM (" +
                    "SELECT anualitat || '/' || trimestre AS agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES, " +
                    "null AS first_seen_sessions_DIA " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                    "GROUP BY anualitat, trimestre " +
                    "UNION ALL " +
                    "SELECT anualitat || '/' || trimestre AS agrupacio, " +
                    "null AS total_sum_visites_MES, " +
                    "CASE WHEN SUM(sum_fets_sessions_DIA) > 0 THEN MIN(data) ELSE NULL END AS first_seen_sessions_DIA " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') = 'RRHH' " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia) " +
                    "GROUP BY anualitat, trimestre) " +
                    "GROUP BY agrupacio ORDER BY agrupacio")
            ),

            // Test 4: Single dimension, mixed aggregations requiring UNION (SUM MES + LAST_SEEN)
            Arguments.of(
                "Single dimension, mixed aggregations requiring UNION (SUM MES + LAST_SEEN)",
                Map.of("departament", List.of("RRHH", "IT")),
                List.of(
                    createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.MES),
                    createIndicadorAgregacio("sessions", TableColumnsEnum.LAST_SEEN, PeriodeUnitat.MES)
                ),
                PeriodeUnitat.ANY,
                removeConsecutiveSpaces("SELECT agrupacio, " +
                    "MAX(total_sum_visites_MES) as total_sum_visites_MES, " +
                    "MAX(last_seen_sessions_DIA) as last_seen_sessions_DIA " +
                    "FROM (" +
                    "SELECT anualitat AS agrupacio, " +
                    "SUM(sum_fets_visites_MES) AS total_sum_visites_MES, " +
                    "null AS last_seen_sessions_DIA " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_MES " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                    "GROUP BY anualitat " +
                    "UNION ALL " +
                    "SELECT anualitat AS agrupacio, " +
                    "null AS total_sum_visites_MES, " +
                    "CASE WHEN SUM(sum_fets_sessions_DIA) > 0 THEN MAX(data) ELSE NULL END AS last_seen_sessions_DIA " +
                    "FROM ( SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                    "WHERE f.entorn_app_id = :entornAppId AND t.data BETWEEN :dataInici AND :dataFi " +
                    "AND JSON_VALUE(f.dimensions_json, '$.\"departament\"') IN ('RRHH','IT') " +
                    "GROUP BY t.anualitat, t.trimestre, t.mes, t.setmana, t.dia) " +
                    "GROUP BY anualitat) " +
                    "GROUP BY agrupacio ORDER BY agrupacio")
            ),

            // Test 5: Multiple dimensions, same aggregation unit (DIA)
            Arguments.of(
                "Multiple dimensions with mixed values, multiple indicators with same aggregation unit, DIA period",
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
                    "SUM(sum_fets_visites_DIA) AS total_sum_visites_DIA, " +
                    "SUM(sum_fets_sessions_DIA) AS total_sum_sessions_DIA " +
                    "FROM ( " +
                    "SELECT t.anualitat, t.trimestre, t.mes, t.setmana, t.dia, " +
                    "anualitat || '/' || LPAD(mes, 2, '0') AS agrupacio, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"visites\"'))) AS sum_fets_visites_DIA, " +
                    "SUM(TO_NUMBER(JSON_VALUE(f.indicadors_json, '$.\"sessions\"'))) AS sum_fets_sessions_DIA " +
                    "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
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

    static IndicadorAgregacio createIndicadorAgregacio(String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        IndicadorAgregacio indicadorAgregacio = new IndicadorAgregacio();
        indicadorAgregacio.setIndicadorCodi(indicadorCodi);
        indicadorAgregacio.setAgregacio(agregacio);
        indicadorAgregacio.setUnitatAgregacio(unitatAgregacio);
        return indicadorAgregacio;
    }
}
