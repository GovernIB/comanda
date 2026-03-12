package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.IntegracioSalut;
import es.caib.comanda.model.server.monitoring.SubsistemaSalut;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMemoria;
import es.caib.comanda.ms.salut.helper.components.PoliticaSalutPerDefecte;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalutComponentsHelperTest {

    @Test
    void testInicialitzacioIConversions() {
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10);
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(1);
        
        // 1. Inicialització amb components
        List<String> inicials = Arrays.asList("COMP1", "COMP2");
        SalutComponentsHelper helper = new SalutComponentsHelper(monitor, politica, id -> false, inicials);
        
        SalutComponentsHelper.InformeSalutComponents informe = helper.obtenInforme();
        
        assertThat(informe.getEstats()).containsKeys("COMP1", "COMP2");
        assertThat(informe.getEstats().get("COMP1")).isEqualTo(es.caib.comanda.model.server.monitoring.EstatSalutEnum.UNKNOWN);
        
        // 2. Registre d'activitat
        helper.registraExit("COMP1", 100);
        helper.registraError("COMP2");
        helper.registraExit("COMP3", 200); // Dinàmic
        
        informe = helper.obtenInforme();
        
        assertThat(informe.getEstats()).containsKeys("COMP1", "COMP2", "COMP3");
        assertThat(informe.getEstats().get("COMP1")).isEqualTo(es.caib.comanda.model.server.monitoring.EstatSalutEnum.UP);
        assertThat(informe.getEstats().get("COMP2")).isEqualTo(es.caib.comanda.model.server.monitoring.EstatSalutEnum.DOWN);
        
        // 3. Conversió a Integracions
        List<IntegracioSalut> integracions = informe.toIntegracionsSalut();
        assertThat(integracions).hasSize(3);
        IntegracioSalut i1 = integracions.stream().filter(i -> i.getCodi().equals("COMP1")).findFirst().orElseThrow();
        assertThat(i1.getEstat()).isEqualTo(es.caib.comanda.model.server.monitoring.EstatSalutEnum.UP);
        assertThat(i1.getPeticions().getTotalOk()).isEqualTo(1);
        
        // 4. Conversió a Subsistemes
        List<SubsistemaSalut> subsistemes = informe.toSubsistemesSalut();
        assertThat(subsistemes).hasSize(3);
        SubsistemaSalut s2 = subsistemes.stream().filter(s -> s.getCodi().equals("COMP2")).findFirst().orElseThrow();
        assertThat(s2.getEstat()).isEqualTo(es.caib.comanda.model.server.monitoring.EstatSalutEnum.DOWN);
        assertThat(s2.getTotalError()).isEqualTo(1);
    }

    @Test
    void testInicialitzacioAmbEndpoints() {
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10);
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(1);

        java.util.Map<String, String> inicials = new java.util.HashMap<>();
        inicials.put("COMP1", "http://endpoint1");
        inicials.put("COMP2", "http://endpoint2");

        SalutComponentsHelper helper = new SalutComponentsHelper(monitor, politica, id -> false, inicials);

        SalutComponentsHelper.InformeSalutComponents informe = helper.obtenInforme();

        assertThat(informe.getEstats()).containsKeys("COMP1", "COMP2");

        List<IntegracioSalut> integracions = informe.toIntegracionsSalut();
        assertThat(integracions).hasSize(2);

        IntegracioSalut i1 = integracions.stream().filter(i -> i.getCodi().equals("COMP1")).findFirst().orElseThrow();
        assertThat(i1.getPeticions().getEndpoint()).isEqualTo("http://endpoint1");

        IntegracioSalut i2 = integracions.stream().filter(i -> i.getCodi().equals("COMP2")).findFirst().orElseThrow();
        assertThat(i2.getPeticions().getEndpoint()).isEqualTo("http://endpoint2");
    }
}
