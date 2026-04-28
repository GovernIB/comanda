package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SalutIntegracioEntityTest {

    @Test
    void latenciaICounts_quanEsGestionen_actualitzaMitjanesComptadorsIPercentatges() {
        SalutIntegracioEntity entity = new SalutIntegracioEntity();

        entity.setLatenciaMitjana(null);
        entity.addLatenciaMitjana(null);
        assertThat(entity.getLatenciaMitjana()).isNull();

        entity.setLatenciaMitjana(120);
        entity.addLatenciaMitjana(60);
        entity.updateCountByEstat(SalutEstat.UP);
        entity.updateCountByEstat(SalutEstat.WARN);
        entity.updateCountByEstat(SalutEstat.DEGRADED);
        entity.updateCountByEstat(SalutEstat.DOWN);
        entity.updateCountByEstat(SalutEstat.ERROR);
        entity.updateCountByEstat(SalutEstat.MAINTENANCE);
        entity.updateCountByEstat(SalutEstat.UNKNOWN);
        entity.setEstatNumElements(7);

        assertThat(entity.getLatenciaMitjana()).isEqualTo(90);
        assertThat(entity.getLatenciaNumElements()).isEqualTo(2);
        assertThat(entity.getPctUp()).isEqualByComparingTo(new BigDecimal("14.29"));
        assertThat(entity.getPctWarn()).isEqualByComparingTo(new BigDecimal("14.29"));
        assertThat(entity.getPctDegraded()).isEqualByComparingTo(new BigDecimal("14.29"));
        assertThat(entity.getPctDown()).isEqualByComparingTo(new BigDecimal("14.29"));
        assertThat(entity.getPctError()).isEqualByComparingTo(new BigDecimal("14.29"));
        assertThat(entity.getPctMaintenance()).isEqualByComparingTo(new BigDecimal("14.29"));
        assertThat(entity.getPctUnknown()).isEqualByComparingTo(new BigDecimal("14.29"));
    }

    @Test
    void peticionsUltimPeriode_quanRepNullZeroIValors_acumulaIRecalculaTempsMig() {
        SalutIntegracioEntity entity = new SalutIntegracioEntity();
        entity.setPeticionsOkUltimPeriode(2L);
        entity.setTempsMigUltimPeriode(100);

        entity.addPeticionsOkUltimPeriode(null, null);
        entity.addPeticionsErrorUltimPeriode(null);
        entity.addPeticionsOkUltimPeriode(null, 1L);
        entity.addPeticionsOkUltimPeriode(200, null);
        entity.addPeticionsOkUltimPeriode(200, 0L);

        assertThat(entity.getPeticionsOkUltimPeriode()).isEqualTo(3L);
        assertThat(entity.getPeticionsErrorUltimPeriode()).isNull();
        assertThat(entity.getTempsMigUltimPeriode()).isEqualTo(100);

        entity.addPeticionsOkUltimPeriode(300, 3L);
        entity.addPeticionsErrorUltimPeriode(4L);

        assertThat(entity.getPeticionsOkUltimPeriode()).isEqualTo(6L);
        assertThat(entity.getPeticionsErrorUltimPeriode()).isEqualTo(4L);
        assertThat(entity.getTempsMigUltimPeriode()).isEqualTo(200);
    }
}
