package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class CalculSalutGlobalTest {

    @Test
    void testCalculaEstatGlobal_Buit() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, id -> false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.UNKNOWN);
    }

    @Test
    void testCalculaEstatGlobal_TotsUp() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.UP);
        
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, id -> false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.UP);
    }

    @Test
    void testCalculaEstatGlobal_AmbWarnNoCritic() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.WARN);
        
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, id -> false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.WARN);
    }

    @Test
    void testCalculaEstatGlobal_AmbErrorNoCritic_EsDevéWarn() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.ERROR);
        
        // C2 no és crític
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, id -> false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.WARN);
    }

    @Test
    void testCalculaEstatGlobal_AmbErrorCritic() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.ERROR);
        
        // C2 és crític
        Function<String, Boolean> esCritic = id -> "C2".equals(id);
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, esCritic);
        assertThat(resultat).isEqualTo(EstatSalutEnum.ERROR);
    }

    @Test
    void testCalculaEstatGlobal_AmbDownCritic() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.DOWN);
        
        // C2 és crític
        Function<String, Boolean> esCritic = id -> "C2".equals(id);
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, esCritic);
        assertThat(resultat).isEqualTo(EstatSalutEnum.ERROR);
    }

    @Test
    void testCalculaEstatGlobal_AmbDegradedCritic() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.DEGRADED);
        
        // C2 és crític
        Function<String, Boolean> esCritic = id -> "C2".equals(id);
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, esCritic);
        assertThat(resultat).isEqualTo(EstatSalutEnum.DEGRADED);
    }

    @Test
    void testCalculaEstatGlobal_AmbDegradedNoCritic_EsDevéWarn() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.DEGRADED);
        
        // C2 no és crític
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, id -> false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.WARN);
    }

    @Test
    void testCalculaEstatGlobal_PrioritatErrorSobreDegraded() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.DEGRADED);
        estats.put("C2", EstatSalutEnum.ERROR);
        
        Function<String, Boolean> esCritic = id -> true;
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, esCritic);
        assertThat(resultat).isEqualTo(EstatSalutEnum.ERROR);
    }

    @Test
    void testCalculaEstatGlobal_AmbUnknown() {
        Map<String, EstatSalutEnum> estats = new HashMap<>();
        estats.put("C1", EstatSalutEnum.UP);
        estats.put("C2", EstatSalutEnum.UNKNOWN);
        
        EstatSalutEnum resultat = CalculSalutGlobal.calculaEstatGlobal(estats, id -> true);
        assertThat(resultat).isEqualTo(EstatSalutEnum.UP);
    }
}
