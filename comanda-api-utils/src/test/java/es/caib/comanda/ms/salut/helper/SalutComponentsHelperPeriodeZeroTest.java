package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import es.caib.comanda.ms.salut.helper.components.MonitorComponentsMemoria;
import es.caib.comanda.ms.salut.helper.components.PoliticaSalutPerDefecte;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SalutComponentsHelperPeriodeZeroTest {

    @Test
    void mantéDarrerEstatQuanPeriodeEsZero() {
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10);
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(1);
        SalutComponentsHelper helper = new SalutComponentsHelper(monitor, politica, id -> false);

        // Activitat inicial per calcular estat UP
        helper.registraExit("C1", 50);
        SalutComponentsHelper.InformeSalutComponents informe1 = helper.obtenInforme();
        assertThat(informe1.getEstats().get("C1")).isEqualTo(EstatSalutEnum.UP);

        // Sense activitat: el període és 0 al següent snapshot
        SalutComponentsHelper.InformeSalutComponents informe2 = helper.obtenInforme();
        assertThat(informe2.getEstats().get("C1")).isEqualTo(EstatSalutEnum.UP); // darrer estat

        // Si ara hi ha un error, canvia a DOWN i es guarda com a darrer estat
        helper.registraError("C1");
        SalutComponentsHelper.InformeSalutComponents informe3 = helper.obtenInforme();
        assertThat(informe3.getEstats().get("C1")).isEqualTo(EstatSalutEnum.DOWN);

        // I sense activitat torna a informar el darrer estat DOWN
        SalutComponentsHelper.InformeSalutComponents informe4 = helper.obtenInforme();
        assertThat(informe4.getEstats().get("C1")).isEqualTo(EstatSalutEnum.DOWN);
    }
}
