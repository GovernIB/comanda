package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgreSQLFetRepositoryDialectGraficTest {

    private PostgreSQLFetRepositoryDialect dialect;

    @BeforeEach
    void setUp() {
        dialect = new PostgreSQLFetRepositoryDialect();
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("provideGetGraficVarisIndicadorsQueryTestCases")
    void testGetGraficVarisIndicadorsQueryParameterized(
            String testName,
            Map<String, List<String>> dimensionsFiltre,
            List<IndicadorAgregacio> indicadorsAgregacio,
            PeriodeUnitat tempsAgregacio,
            String expectedQuery) {

        String actualQuery = dialect.getGraficVarisIndicadorsQuery(dimensionsFiltre, indicadorsAgregacio, tempsAgregacio, null);
        assertEquals(expectedQuery, removeConsecutiveSpaces(actualQuery), "Failed test case: " + testName);
    }

    private static Stream<Arguments> provideGetGraficVarisIndicadorsQueryTestCases() {
        return Stream.of(
                // Test 1: Null dimensions, single indicator with SUM aggregation, MES period
                Arguments.of(
                        "Null dimensions, single indicator with SUM aggregation, MES period",
                        null,
                        List.of(createIndicadorAgregacio("visites", TableColumnsEnum.SUM, null)),
                        PeriodeUnitat.MES,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites " +
                                "FROM ( " +
                                "SELECT t.anualitat, t.trimestre, t.mes, " +
                                "anualitat || '/' || LPAD(mes::text, 2, '0') AS agrupacio, " +
                                "SUM((f.indicadors_json::jsonb->>'visites')::numeric) AS sum_fets_visites " +
                                "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "GROUP BY t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 2: Single dimension with single value, multiple indicators with different aggregations, TRIMESTRE period
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
                                "anualitat || '/' || trimestre AS agrupacio, " +
                                "SUM((f.indicadors_json::jsonb->>'visites')::numeric) AS sum_fets_visites, " +
                                "SUM((f.indicadors_json::jsonb->>'sessions')::numeric) AS sum_fets_sessions " +
                                "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND f.dimensions_json::jsonb->>'departament' = 'RRHH' " +
                                "GROUP BY t.data, t.anualitat, t.trimestre, t.mes) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                ),

                // Test 3: Multiple indicators, one is AVERAGE, ANY period
                Arguments.of(
                        "Multiple indicators, one is AVERAGE, ANY period",
                        Map.of("departament", List.of("RRHH", "IT")),
                        List.of(
                            createIndicadorAgregacio("visites", TableColumnsEnum.SUM, PeriodeUnitat.ANY),
                            createIndicadorAgregacio("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.ANY)
                        ),
                        PeriodeUnitat.ANY,
                        removeConsecutiveSpaces("SELECT agrupacio, " +
                                "SUM(sum_fets_visites) AS total_sum_visites, " +
                                "AVG(sum_fets_sessions) AS average_result_sessions " +
                                "FROM ( " +
                                "SELECT anualitat AS agrupacio, " +
                                "SUM((f.indicadors_json::jsonb->>'visites')::numeric) AS sum_fets_visites, " +
                                "SUM((f.indicadors_json::jsonb->>'sessions')::numeric) AS sum_fets_sessions " +
                                "FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                                "WHERE f.entorn_app_id = :entornAppId " +
                                "AND t.data BETWEEN :dataInici AND :dataFi " +
                                "AND f.dimensions_json::jsonb->>'departament' IN ('RRHH','IT') " +
                                "GROUP BY t.anualitat) " +
                                "GROUP BY agrupacio " +
                                "ORDER BY agrupacio")
                )
        );
    }

    private static String removeConsecutiveSpaces(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    private static IndicadorAgregacio createIndicadorAgregacio(String indicadorCodi, TableColumnsEnum agregacio, PeriodeUnitat unitatAgregacio) {
        IndicadorAgregacio indicadorAgregacio = new IndicadorAgregacio();
        indicadorAgregacio.setIndicadorCodi(indicadorCodi);
        indicadorAgregacio.setAgregacio(agregacio);
        indicadorAgregacio.setUnitatAgregacio(unitatAgregacio);
        return indicadorAgregacio;
    }
}
