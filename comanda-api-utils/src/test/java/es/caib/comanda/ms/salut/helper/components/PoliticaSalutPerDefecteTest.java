package es.caib.comanda.ms.salut.helper.components;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaSalutPerDefecteTest {

    private static EstadistiquesComponent stats(String id, long okP, long koP, long okT, long koT) {
        return new EstadistiquesComponent(
                id,
                null,
                okP, koP, okP == 0 ? 0.0 : 1.0 * (100) / Math.max(1, okP), // el valor de latència no afecta
                okT, koT, okT == 0 ? 0.0 : 1.0 * (200) / Math.max(1, okT),
                Instant.now()
        );
    }

    @Test
    void quanPeriodeSuficient_empraPeriode() {
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(3); // mínim 3 mostres
        EstadistiquesComponent est = stats("C1", 2, 1, 2, 1); // 3 mostres al període => 33% errors => DEGRADED

        EstatSalutEnum resultat = politica.calculaEstat(est, 0, 0, false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.DEGRADED);
    }

    @Test
    void quanPeriodeCurt_iHiHaFallback_empraFallback() {
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(5); // mínim 5 mostres
        EstadistiquesComponent est = stats("C1", 1, 0, 1, 0); // 1 mostra al període < mínim

        // Fallback amb 100% errors
        EstatSalutEnum resultat = politica.calculaEstat(est, 0, 10, true);
        assertThat(resultat).isEqualTo(EstatSalutEnum.DOWN);
    }

    @Test
    void quanPeriodeBuit_iSenseFallback_retornaUnknown() {
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(2);
        EstadistiquesComponent est = stats("C1", 0, 0, 0, 0);

        EstatSalutEnum resultat = politica.calculaEstat(est, 0, 0, false);
        assertThat(resultat).isEqualTo(EstatSalutEnum.UNKNOWN);
    }

    @Test
    void quanPeriodeBuit_peroAmbFallback_empraFallback() {
        PoliticaSalutPerDefecte politica = new PoliticaSalutPerDefecte(2);
        EstadistiquesComponent est = stats("C1", 0, 0, 0, 0);

        // Fallback 1 OK, 0 KO => UP
        EstatSalutEnum resultat = politica.calculaEstat(est, 1, 0, true);
        assertThat(resultat).isEqualTo(EstatSalutEnum.UP);
    }
}
