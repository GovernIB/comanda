package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import es.caib.comanda.model.server.monitoring.IntegracioSalut;
import es.caib.comanda.model.server.monitoring.SubsistemaSalut;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMemoria;
import es.caib.comanda.ms.salut.helper.components.PoliticaSalutPerDefecte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SalutComponentsHelperFullCircuitTest {

    @Test
    @DisplayName("Deu funcionar tot el circuit: inicialització, registre d'activitat, informe i conversions amb endpoint")
    void testCircuitComplet() {
        // 1. Inicialització del monitor i helper amb endpoints i política
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10);
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(2); // Calen 2 per percentatge
        
        Map<String, String> componentsAmbEndpoint = new HashMap<>();
        componentsAmbEndpoint.put("INT_REG", "https://registre.caib.es/api");
        componentsAmbEndpoint.put("INT_NOT", "https://notificacions.caib.es/ws");
        componentsAmbEndpoint.put("SUB_DB", null); // Subsistema sense endpoint
        
        // INT_REG serà crític, INT_NOT no
        SalutComponentsHelper helper = new SalutComponentsHelper(
                monitor, 
                politica, 
                id -> id.equals("INT_REG") || id.equals("SUB_DB"), 
                componentsAmbEndpoint
        );

        // 2. Estat inicial (UNKNOWN)
        SalutComponentsHelper.InformeSalutComponents informeInicial = helper.obtenInforme();
        assertThat(informeInicial.getEstatGlobal()).isEqualTo(EstatSalutEnum.UNKNOWN);
        assertThat(informeInicial.getEstats().get("INT_REG")).isEqualTo(EstatSalutEnum.UNKNOWN);

        // 3. Registre d'èxits i errors
        // INT_REG: 2 OK (UP)
        helper.registraExit("INT_REG", 100);
        helper.registraExit("INT_REG", 120);
        
        // INT_NOT: 1 OK, 1 KO (50% error -> ERROR segons EstatHelper, però no és crític -> WARN global si no hi ha res més)
        helper.registraExit("INT_NOT", 50);
        helper.registraError("INT_NOT");
        
        // SUB_DB: 1 OK (UP)
        helper.registraExit("SUB_DB", 10);

        // 4. Generació de l'informe i verificació de l'estat global
        SalutComponentsHelper.InformeSalutComponents informe = helper.obtenInforme();
        
        // Verificació d'estats individuals
        assertThat(informe.getEstats().get("INT_REG")).isEqualTo(EstatSalutEnum.UP);
        assertThat(informe.getEstats().get("INT_NOT")).isEqualTo(EstatSalutEnum.DEGRADED); // 50% d'error
        assertThat(informe.getEstats().get("SUB_DB")).isEqualTo(EstatSalutEnum.UP);
        
        // Estat global:
        // INT_REG (crític) = UP
        // INT_NOT (no crític) = ERROR -> es converteix en WARN per al global
        // SUB_DB (crític) = UP
        // Global hauria de ser WARN
        assertThat(informe.getEstatGlobal()).isEqualTo(EstatSalutEnum.WARN);

        // 5. Conversió a Integracions i verificació d'endpoints i mètriques
        List<IntegracioSalut> integracions = informe.toIntegracionsSalut();
        
        IntegracioSalut reg = integracions.stream().filter(i -> "INT_REG".equals(i.getCodi())).findFirst().orElseThrow();
        assertThat(reg.getEstat()).isEqualTo(EstatSalutEnum.UP);
        assertThat(reg.getPeticions().getEndpoint()).isEqualTo("https://registre.caib.es/api");
        assertThat(reg.getPeticions().getTotalOk()).isEqualTo(2);
        assertThat(reg.getPeticions().getPeticionsOkUltimPeriode()).isEqualTo(2);
        assertThat(reg.getPeticions().getTotalTempsMig()).isEqualTo(110); // (100+120)/2

        IntegracioSalut not = integracions.stream().filter(i -> "INT_NOT".equals(i.getCodi())).findFirst().orElseThrow();
        assertThat(not.getEstat()).isEqualTo(EstatSalutEnum.DEGRADED);
        assertThat(not.getPeticions().getEndpoint()).isEqualTo("https://notificacions.caib.es/ws");
        assertThat(not.getPeticions().getTotalOk()).isEqualTo(1);
        assertThat(not.getPeticions().getTotalError()).isEqualTo(1);
        assertThat(not.getPeticions().getPeticionsErrorUltimPeriode()).isEqualTo(1);

        // 6. Conversió a Subsistemes
        List<SubsistemaSalut> subsistemes = informe.toSubsistemesSalut();
        SubsistemaSalut db = subsistemes.stream().filter(s -> "SUB_DB".equals(s.getCodi())).findFirst().orElseThrow();
        assertThat(db.getEstat()).isEqualTo(EstatSalutEnum.UP);
        assertThat(db.getTotalOk()).isEqualTo(1);
        assertThat(db.getPeticionsOkUltimPeriode()).isEqualTo(1);
        
        // 7. Segon període (reset-on-read)
        // No fem res, l'estat del període ha de ser 0, però ha de mantenir el darrer estat (UP/ERROR/UP)
        SalutComponentsHelper.InformeSalutComponents informeBuit = helper.obtenInforme();
        assertThat(informeBuit.getEstats().get("INT_REG")).isEqualTo(EstatSalutEnum.UP);
        assertThat(informeBuit.getEstats().get("INT_NOT")).isEqualTo(EstatSalutEnum.DEGRADED);
        
        assertThat(informeBuit.getEstadistiques().get("INT_REG").getOkPeriode()).isEqualTo(0);
        assertThat(informeBuit.getEstadistiques().get("INT_REG").getOkTotal()).isEqualTo(2);
    }
}
