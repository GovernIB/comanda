package es.caib.comanda.estadistica.persist.repository.dialect;

import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorAgregacio;
import es.caib.comanda.estadistica.logic.intf.model.consulta.IndicadorFormulaTermeResolt;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.OperadorFormulaEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests unitaris (sense base de dades) per a la generació de SQL d'indicadors de fórmula amb PostgreSQL,
 * paral·lels als d'OracleFetRepositoryDialectSimpleTest.
 */
class PostgreSQLFetRepositoryDialectSimpleTest {

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
    @DisplayName("getSimpleQuery: un indicador FORMULA genera la suma/resta dels seus termes dins del mateix SUM(...)")
    void getSimpleQuery_quanIndicadorEsFormula_llavorsGeneraSumaIRestaDinsDelMateixSum() {
        IndicadorAgregacio indicadorAgregacio = IndicadorAgregacio.builder()
                .indicadorCodi("TOTAL")
                .agregacio(TableColumnsEnum.SUM)
                .termesFormula(List.of(
                        IndicadorFormulaTermeResolt.builder().indicadorCodi("IND1").operador(OperadorFormulaEnum.SUMA).build(),
                        IndicadorFormulaTermeResolt.builder().indicadorCodi("IND2").operador(OperadorFormulaEnum.SUMA).build(),
                        IndicadorFormulaTermeResolt.builder().indicadorCodi("IND3").operador(OperadorFormulaEnum.RESTA).build()
                ))
                .build();

        String query = removeConsecutiveSpaces(dialect.getSimpleQuery(null, indicadorAgregacio, null));

        String expected = removeConsecutiveSpaces("SELECT SUM(sum_fets) AS total_sum " +
                "FROM ( " +
                "    SELECT t.data as data, " +
                "        SUM((f.indicadors_json::jsonb->>'IND1')::numeric " +
                "            + (f.indicadors_json::jsonb->>'IND2')::numeric " +
                "            - (f.indicadors_json::jsonb->>'IND3')::numeric) AS sum_fets " +
                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                "GROUP BY t.data)");

        assertNotNull(query);
        assertTrue(query.equals(expected), "Query should be: " + expected + "\nActual query: " + query);
    }

    @Test
    @DisplayName("getSimpleQuery: un indicador SIMPLE manté el comportament amb un únic valor JSON")
    void getSimpleQuery_quanIndicadorEsSimple_llavorsMantéElComportamentAnterior() {
        IndicadorAgregacio indicadorAgregacio = IndicadorAgregacio.builder()
                .indicadorCodi("visites")
                .agregacio(TableColumnsEnum.SUM)
                .build();

        String query = removeConsecutiveSpaces(dialect.getSimpleQuery(null, indicadorAgregacio, null));

        String expected = removeConsecutiveSpaces("SELECT SUM(sum_fets) AS total_sum " +
                "FROM ( " +
                "    SELECT t.data as data, " +
                "        SUM((f.indicadors_json::jsonb->>'visites')::numeric) AS sum_fets " +
                "    FROM com_est_fet f JOIN com_est_temps t ON f.temps_id = t.id " +
                "    WHERE f.entorn_app_id = :entornAppId " +
                "    AND t.data BETWEEN :dataInici AND :dataFi " +
                "GROUP BY t.data)");

        assertNotNull(query);
        assertTrue(query.equals(expected), "Query should be: " + expected + "\nActual query: " + query);
    }

}
