package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.consulta.SeguretatFiltreSql;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Comprova que OracleFetRepositoryDialect i PostgreSQLFetRepositoryDialect retornen els mateixos valors, correctes,
 * quan la SQL que generen s'executa realment contra la base de dades corresponent (no només que el text generat
 * sigui l'esperat, com fan els altres tests d'aquest paquet).
 * <p>
 * Les subclasses aixequen un contenidor Testcontainers amb la base de dades real i implementen {@link #dialect()}
 * i {@link #jdbc()}; la resta - l'esquema mínim (idèntic a comanda-ms-estadistica/liquibase/init/00_est_init_table),
 * les dades de proves i totes les comprovacions de valors - és compartit, perquè ambdós dialectes han de comportar-se
 * de manera idèntica donades les mateixes dades.
 * <p>
 * Dades de proves (entornAppId=1, llevat que s'indiqui el contrari):
 * <pre>
 * temps: 101=2024-01-10(any2024,t1,m1)  102=2024-01-20(any2024,t1,m1)  103=2024-02-15(any2024,t1,m2)
 *        104=2024-04-05(any2024,t2,m4)  105=2025-01-10(any2025,t1,m1)
 * fet 1: temps=101 dept=RRHH      canal=web        visites=10  sessions=2
 * fet 2: temps=102 dept=RRHH      canal=presencial visites=20  sessions=4
 * fet 3: temps=103 dept=IT        canal=web        visites=5   sessions=1
 * fet 4: temps=104 dept=IT        canal=presencial visites=7   (sense sessions)
 * fet 5: temps=101 dept=O'Higgins canal=web        visites=100 (sense sessions; departament amb cometa - escapament)
 * fet 6: temps=101 dept=RRHH      canal=web        visites=9999  entornAppId=2 (aïllament entre entorns)
 * fet 7: temps=105 dept=RRHH      canal=web        visites=50    (2025, fora del rang de dates habitual dels tests)
 * </pre>
 */
public abstract class AbstractFetRepositoryDialectValuesTest {

    protected static final LocalDate DATA_INICI_2024 = LocalDate.of(2024, 1, 1);
    protected static final LocalDate DATA_FI_2024 = LocalDate.of(2024, 12, 31);
    protected static final LocalDate DATA_FI_2025 = LocalDate.of(2025, 12, 31);

    protected abstract FetRepositoryDialect dialect();

    protected abstract NamedParameterJdbcTemplate jdbc();

    @BeforeEach
    void seedData() {
        insertTemps(101L, LocalDate.of(2024, 1, 10), 2024, 1, 1, 2, 10);
        insertTemps(102L, LocalDate.of(2024, 1, 20), 2024, 1, 1, 3, 20);
        insertTemps(103L, LocalDate.of(2024, 2, 15), 2024, 1, 2, 7, 15);
        insertTemps(104L, LocalDate.of(2024, 4, 5), 2024, 2, 4, 14, 5);
        insertTemps(105L, LocalDate.of(2025, 1, 10), 2025, 1, 1, 2, 10);

        insertFet(1L, 1L, 101L, "{\"departament\":\"RRHH\",\"canal\":\"web\"}", "{\"visites\":10,\"sessions\":2}");
        insertFet(2L, 1L, 102L, "{\"departament\":\"RRHH\",\"canal\":\"presencial\"}", "{\"visites\":20,\"sessions\":4}");
        insertFet(3L, 1L, 103L, "{\"departament\":\"IT\",\"canal\":\"web\"}", "{\"visites\":5,\"sessions\":1}");
        insertFet(4L, 1L, 104L, "{\"departament\":\"IT\",\"canal\":\"presencial\"}", "{\"visites\":7}");
        insertFet(5L, 1L, 101L, "{\"departament\":\"O'Higgins\",\"canal\":\"web\"}", "{\"visites\":100}");
        insertFet(6L, 2L, 101L, "{\"departament\":\"RRHH\",\"canal\":\"web\"}", "{\"visites\":9999}");
        insertFet(7L, 1L, 105L, "{\"departament\":\"RRHH\",\"canal\":\"web\"}", "{\"visites\":50}");
    }

    @AfterEach
    void cleanData() {
        jdbc().getJdbcTemplate().update("DELETE FROM com_est_fet");
        jdbc().getJdbcTemplate().update("DELETE FROM com_est_temps");
    }

    private void insertTemps(long id, LocalDate data, int any, int trimestre, int mes, int setmana, int dia) {
        jdbc().update("INSERT INTO com_est_temps (id, data, anualitat, trimestre, mes, setmana, dia, dia_setmana) " +
                        "VALUES (:id, :data, :any, :trimestre, :mes, :setmana, :dia, :diaSetmana)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("data", Date.valueOf(data))
                        .addValue("any", any)
                        .addValue("trimestre", trimestre)
                        .addValue("mes", mes)
                        .addValue("setmana", setmana)
                        .addValue("dia", dia)
                        .addValue("diaSetmana", "1"));
    }

    private void insertFet(long id, long entornAppId, long tempsId, String dimensionsJson, String indicadorsJson) {
        jdbc().update("INSERT INTO com_est_fet (id, temps_id, dimensions_json, indicadors_json, entorn_app_id) " +
                        "VALUES (:id, :tempsId, :dimensions, :indicadors, :entornAppId)",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("tempsId", tempsId)
                        .addValue("dimensions", dimensionsJson)
                        .addValue("indicadors", indicadorsJson)
                        .addValue("entornAppId", entornAppId));
    }

    // ---------------------------------------------------------------- helpers de crida i lectura de resultats

    private MapSqlParameterSource baseParams(long entornAppId, LocalDate dataInici, LocalDate dataFi) {
        return new MapSqlParameterSource()
                .addValue("entornAppId", entornAppId)
                .addValue("dataInici", Date.valueOf(dataInici))
                .addValue("dataFi", Date.valueOf(dataFi));
    }

    private List<Map<String, Object>> query(String sql, MapSqlParameterSource params) {
        return jdbc().queryForList(sql, params);
    }

    private static Double num(Map<String, Object> row, String column) {
        Object value = row.get(column);
        return value == null ? null : ((Number) value).doubleValue();
    }

    private static LocalDate asLocalDate(Map<String, Object> row, String column) {
        Object value = row.get(column);
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        if (value instanceof Date) return ((Date) value).toLocalDate();
        if (value instanceof Timestamp) return ((Timestamp) value).toLocalDateTime().toLocalDate();
        throw new IllegalArgumentException("Tipus de data inesperat: " + value.getClass());
    }

    private static IndicadorAgregacio indicador(String codi, TableColumnsEnum agregacio, PeriodeUnitat unitat) {
        return IndicadorAgregacio.builder().indicadorCodi(codi).agregacio(agregacio).unitatAgregacio(unitat).build();
    }

    // ============================================================================ getSimpleQuery

    @Test
    void simpleQuery_sum_senseFiltreDimensions_sumaTotesLesFiles() {
        String sql = dialect().getSimpleQuery(null, "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(142.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_sum_filtreDimensioValorUnic() {
        String sql = dialect().getSimpleQuery(Map.of("departament", List.of("RRHH")), "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(30.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_sum_valorDimensioAmbCometaSimple_escapatCorrectament() {
        String sql = dialect().getSimpleQuery(Map.of("departament", List.of("O'Higgins")), "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(100.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_sum_filtreMultiplesValorsIN() {
        String sql = dialect().getSimpleQuery(Map.of("departament", List.of("RRHH", "IT")), "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(42.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_sum_filtreMultiplesDimensionsAND() {
        String sql = dialect().getSimpleQuery(
                Map.of("departament", List.of("RRHH"), "canal", List.of("web")),
                "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(10.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_average_agrupatPerMes() {
        String sql = dialect().getSimpleQuery(Map.of("departament", List.of("IT")), "visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        // IT té dos mesos diferents amb un valor cadascun (mes 2 = 5, mes 4 = 7) -> mitjana (5+7)/2
        assertEquals(6.0, num(row, "average_result"));
    }

    @Test
    void simpleQuery_firstSeen_iLastSeen() {
        Map<String, List<String>> filtre = Map.of("departament", List.of("RRHH"));

        Map<String, Object> first = query(
                dialect().getSimpleQuery(filtre, "visites", TableColumnsEnum.FIRST_SEEN, null, null),
                baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);
        Map<String, Object> last = query(
                dialect().getSimpleQuery(filtre, "visites", TableColumnsEnum.LAST_SEEN, null, null),
                baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(LocalDate.of(2024, 1, 10), asLocalDate(first, "first_seen"));
        assertEquals(LocalDate.of(2024, 1, 20), asLocalDate(last, "last_seen"));
    }

    @Test
    void simpleQuery_aillamentPerEntornAppId() {
        // Entorn 1 amb rang que cobreix 2024 i 2025: ha d'incloure el fet de 2025 (mateix entorn) però mai el
        // fet 6 (visites=9999), que pertany a l'entorn 2.
        String sql = dialect().getSimpleQuery(null, "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2025)).get(0);

        assertEquals(192.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_rangDeDatesInclusiuAAmbduesBandes() {
        String sql = dialect().getSimpleQuery(Map.of("departament", List.of("RRHH")), "visites", TableColumnsEnum.SUM, null, null);
        Map<String, Object> row = query(sql, baseParams(1L, LocalDate.of(2024, 1, 10), LocalDate.of(2024, 1, 20))).get(0);

        assertEquals(30.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_seguretat_unioOrEntreEntitatIOrgan() {
        SeguretatFiltreSql seguretat = SeguretatFiltreSql.builder()
                .dimensioEntitatCodi("departament").valorsEntitatPermesos(List.of("RRHH"))
                .dimensioOrganCodi("canal").valorsOrganPermesos(List.of("presencial"))
                .build();

        String sql = dialect().getSimpleQuery(null, "visites", TableColumnsEnum.SUM, null, seguretat);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        // dept=RRHH OR canal=presencial -> fet1(RRHH,web)=10, fet2(RRHH,presencial)=20, fet4(IT,presencial)=7
        assertEquals(37.0, num(row, "total_sum"));
    }

    @Test
    void simpleQuery_seguretat_denegacioTotalQuanCapValorPermes() {
        SeguretatFiltreSql seguretat = SeguretatFiltreSql.builder()
                .dimensioEntitatCodi("departament").valorsEntitatPermesos(List.of())
                .build();

        String sql = dialect().getSimpleQuery(null, "visites", TableColumnsEnum.SUM, null, seguretat);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertNull(num(row, "total_sum"));
    }

    @Test
    void simpleQuery_seguretat_inactivaNoAplicaCapRestriccio() {
        SeguretatFiltreSql seguretat = SeguretatFiltreSql.builder().build(); // cap codi establert -> isActiva()==false

        String sql = dialect().getSimpleQuery(null, "visites", TableColumnsEnum.SUM, null, seguretat);
        Map<String, Object> row = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)).get(0);

        assertEquals(142.0, num(row, "total_sum"));
    }

    // ============================================================================ getGraficUnIndicadorQuery

    @Test
    void graficUnIndicador_agrupatPerMes() {
        IndicadorAgregacio ind = indicador("visites", TableColumnsEnum.SUM, null);
        String sql = dialect().getGraficUnIndicadorQuery(null, ind, PeriodeUnitat.MES, null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(3, rows.size());
        assertEquals("2024/01", rows.get(0).get("agrupacio"));
        assertEquals(130.0, num(rows.get(0), "total_sum"));
        assertEquals("2024/02", rows.get(1).get("agrupacio"));
        assertEquals(5.0, num(rows.get(1), "total_sum"));
        assertEquals("2024/04", rows.get(2).get("agrupacio"));
        assertEquals(7.0, num(rows.get(2), "total_sum"));
    }

    // ============================================================================ getGraficUnIndicadorAmbDescomposicioQuery

    @Test
    void graficUnIndicadorAmbDescomposicio_senseAgrupacioTemporal() {
        IndicadorAgregacio ind = indicador("visites", TableColumnsEnum.SUM, null);
        String sql = dialect().getGraficUnIndicadorAmbDescomposicioQuery(null, ind, "canal", null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(2, rows.size());
        assertEquals("presencial", rows.get(0).get("agrupacio"));
        assertEquals(27.0, num(rows.get(0), "total_sum"));
        assertEquals("web", rows.get(1).get("agrupacio"));
        assertEquals(115.0, num(rows.get(1), "total_sum"));
    }

    @Test
    void graficUnIndicadorAmbDescomposicio_senseAgrupacioTemporal_average() {
        // Comprova que el tipus d'agregació triat s'aplica realment (AVERAGE, no només SUM).
        IndicadorAgregacio ind = indicador("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES);
        String sql = dialect().getGraficUnIndicadorAmbDescomposicioQuery(null, ind, "departament", null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        Map<String, Map<String, Object>> byAgrupacio = rows.stream()
                .collect(java.util.stream.Collectors.toMap(r -> (String) r.get("agrupacio"), r -> r));

        assertEquals(30.0, num(byAgrupacio.get("RRHH"), "average_result"));
        assertEquals(6.0, num(byAgrupacio.get("IT"), "average_result"));
        assertEquals(100.0, num(byAgrupacio.get("O'Higgins"), "average_result"));
    }

    // ============================================================================ getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery

    @Test
    void graficUnIndicadorAmbDescomposicioIAgrupacio_perMesIDescomposicio() {
        IndicadorAgregacio ind = indicador("visites", TableColumnsEnum.SUM, null);
        String sql = dialect().getGraficUnIndicadorAmbDescomposicioAndAgrupacioQuery(
                Map.of("departament", List.of("RRHH")), ind, "canal", PeriodeUnitat.MES, null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        // dept=RRHH -> fet1 (mes1, web, 10) i fet2 (mes1, presencial, 20): mateix mes, descomposicions diferents
        assertEquals(2, rows.size());
        assertEquals("2024/01", rows.get(0).get("agrupacio"));
        assertEquals("presencial", rows.get(0).get("descomposicio"));
        assertEquals(20.0, num(rows.get(0), "total_sum"));
        assertEquals("2024/01", rows.get(1).get("agrupacio"));
        assertEquals("web", rows.get(1).get("descomposicio"));
        assertEquals(10.0, num(rows.get(1), "total_sum"));
    }

    // ============================================================================ getGraficVarisIndicadorsQuery

    @Test
    void graficVarisIndicadors_dosIndicadorsSumaMateixaUnitat_agrupatPerMes() {
        List<IndicadorAgregacio> indicadors = List.of(
                indicador("visites", TableColumnsEnum.SUM, null),
                indicador("sessions", TableColumnsEnum.SUM, null));
        String sql = dialect().getGraficVarisIndicadorsQuery(null, indicadors, PeriodeUnitat.MES, null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(3, rows.size());
        assertEquals("2024/01", rows.get(0).get("agrupacio"));
        assertEquals(130.0, num(rows.get(0), "total_sum_visites"));
        assertEquals(6.0, num(rows.get(0), "total_sum_sessions"));
        assertEquals("2024/02", rows.get(1).get("agrupacio"));
        assertEquals(5.0, num(rows.get(1), "total_sum_visites"));
        assertEquals(1.0, num(rows.get(1), "total_sum_sessions"));
        assertEquals("2024/04", rows.get(2).get("agrupacio"));
        assertEquals(7.0, num(rows.get(2), "total_sum_visites"));
        // fet4 no té "sessions": SUM sobre un grup on l'únic valor és NULL dona NULL, no 0
        assertNull(num(rows.get(2), "total_sum_sessions"));
    }

    // ============================================================================ getTaulaQuery

    @Test
    void taulaQuery_dosIndicadorsSumaAgrupatPerDimensio() {
        List<IndicadorAgregacio> indicadors = List.of(
                indicador("visites", TableColumnsEnum.SUM, null),
                indicador("sessions", TableColumnsEnum.SUM, null));
        String sql = dialect().getTaulaQuery(null, indicadors, "departament", null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(3, rows.size());
        Map<String, Map<String, Object>> byAgrupacio = rows.stream()
                .collect(java.util.stream.Collectors.toMap(r -> (String) r.get("agrupacio"), r -> r));

        assertEquals(30.0, num(byAgrupacio.get("RRHH"), "total_sum_visites"));
        assertEquals(6.0, num(byAgrupacio.get("RRHH"), "total_sum_sessions"));
        assertEquals(12.0, num(byAgrupacio.get("IT"), "total_sum_visites"));
        assertEquals(1.0, num(byAgrupacio.get("IT"), "total_sum_sessions"));
        assertEquals(100.0, num(byAgrupacio.get("O'Higgins"), "total_sum_visites"));
        assertNull(num(byAgrupacio.get("O'Higgins"), "total_sum_sessions"));
    }

    @Test
    void taulaQuery_mixAverageIFirstSeen_generaUnioPerColumnesNoNulles() {
        // hasAverage && hasDataCols -> genaMixedUnionQuery: cada branca de la UNION ALL omple la seva columna i
        // deixa NULL l'altra; el SELECT exterior fa MAX per columna per obtenir una única fila amb totes dues.
        List<IndicadorAgregacio> indicadors = List.of(
                indicador("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
                indicador("sessions", TableColumnsEnum.FIRST_SEEN, null));
        String sql = dialect().getTaulaQuery(Map.of("departament", List.of("RRHH")), indicadors, "departament", null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(1, rows.size());
        assertEquals("RRHH", rows.get(0).get("agrupacio"));
        // fet1(mes1,10) i fet2(mes1,20) són del mateix mes -> un únic grup de suma 30 -> mitjana = 30
        assertEquals(30.0, num(rows.get(0), "average_result_visites"));
        assertEquals(LocalDate.of(2024, 1, 10), asLocalDate(rows.get(0), "first_seen_sessions"));
    }

    @Test
    void taulaQuery_mixAverageAmbDiferentsUnitats_generaUnioPerColumnesNoNulles() {
        // hasAverage sense hasDataCols, però amb unitatAgregacio diferents -> genaAvgUnionQuery.
        List<IndicadorAgregacio> indicadors = List.of(
                indicador("visites", TableColumnsEnum.AVERAGE, PeriodeUnitat.MES),
                indicador("sessions", TableColumnsEnum.AVERAGE, PeriodeUnitat.ANY));
        String sql = dialect().getTaulaQuery(Map.of("departament", List.of("RRHH")), indicadors, "departament", null);
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(1, rows.size());
        assertEquals("RRHH", rows.get(0).get("agrupacio"));
        assertEquals(30.0, num(rows.get(0), "average_result_visites"));
        assertEquals(6.0, num(rows.get(0), "average_result_sessions"));
    }

    // ============================================================================ mètodes getFindByEntornAppId...

    @Test
    void findByEntornAppIdAndTempsDataBetweenAndDimensionValue_retornaNomesLesFilesQueCoincideixen() {
        String sql = dialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionValueQuery();//"departament"
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)
                .addValue("dimensioValor", "RRHH"));

        assertEquals(2, rows.size());
        assertEquals(java.util.Set.of(1.0, 2.0), rows.stream().map(r -> num(r, "id")).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void findByEntornAppIdAndTempsDataBetweenAndDimensionValues_ambListaDeValors() {
        String sql = dialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionValuesQuery();//"departament"
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024)
                .addValue("dimensioValor", List.of("RRHH", "IT")));

        assertEquals(4, rows.size());
    }

    @Test
    void findByEntornAppIdAndTempsDataAndDimensions_dataExactaAmbFiltreDimensions() {
        String sql = dialect().getFindByEntornAppIdAndTempsDataAndDimensionQuery(Map.of("departament", List.of("RRHH")));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("entornAppId", 1L)
                .addValue("data", Date.valueOf(LocalDate.of(2024, 1, 10)));
        List<Map<String, Object>> rows = query(sql, params);

        // fet5 també és temps=101 (2024-01-10) però amb departament O'Higgins -> exclòs pel filtre
        assertEquals(1, rows.size());
        assertEquals(1.0, num(rows.get(0), "id"));
    }

    @Test
    void findByEntornAppIdAndTempsDataBetweenAndDimensions_multiplesDimensionsAND() {
        String sql = dialect().getFindByEntornAppIdAndTempsDataBetweenAndDimensionQuery(
                Map.of("departament", List.of("IT"), "canal", List.of("web")));
        List<Map<String, Object>> rows = query(sql, baseParams(1L, DATA_INICI_2024, DATA_FI_2024));

        assertEquals(1, rows.size());
        assertEquals(3.0, num(rows.get(0), "id"));
    }
}
