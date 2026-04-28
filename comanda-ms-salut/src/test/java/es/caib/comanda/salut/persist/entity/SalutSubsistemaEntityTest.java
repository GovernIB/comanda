package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SalutSubsistemaEntityTest {

    @Test
    void latenciaICounts_quanEsGestionen_actualitzaMitjanesComptadorsIPercentatges() {
        SalutSubsistemaEntity entity = new SalutSubsistemaEntity();

        entity.setLatenciaMitjana(null);
        entity.addLatenciaMitjana(null);
        assertThat(entity.getLatenciaMitjana()).isNull();

        entity.setLatenciaMitjana(200);
        entity.addLatenciaMitjana(100);
        entity.updateCountByEstat(SalutEstat.UP);
        entity.updateCountByEstat(SalutEstat.WARN);
        entity.updateCountByEstat(SalutEstat.DEGRADED);
        entity.updateCountByEstat(SalutEstat.DOWN);
        entity.updateCountByEstat(SalutEstat.ERROR);
        entity.updateCountByEstat(SalutEstat.MAINTENANCE);
        entity.updateCountByEstat(SalutEstat.UNKNOWN);
        entity.setEstatNumElements(7);

        assertThat(entity.getLatenciaMitjana()).isEqualTo(150);
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
        SalutSubsistemaEntity entity = new SalutSubsistemaEntity();
        entity.setTotalOk(1L);
        entity.setTotalError(0L);
        entity.setTotalTempsMig(50);
        entity.setPeticionsOkUltimPeriode(1L);
        entity.setTempsMigUltimPeriode(50);

        entity.addPeticionsErrorUltimPeriode(null);
        entity.addPeticionsOkUltimPeriode(null, 1L);
        entity.addPeticionsOkUltimPeriode(200, null);
        entity.addPeticionsOkUltimPeriode(200, 0L);

        assertThat(entity.getPeticionsOkUltimPeriode()).isEqualTo(2L);
        assertThat(entity.getPeticionsErrorUltimPeriode()).isNull();
        assertThat(entity.getTempsMigUltimPeriode()).isEqualTo(50);

        entity.setTotalOk(5L);
        entity.setTotalError(2L);
        entity.setTotalTempsMig(125);
        entity.addPeticionsErrorUltimPeriode(2L);
        entity.addPeticionsOkUltimPeriode(150, 3L);

        assertThat(entity.getPeticionsOkUltimPeriode()).isEqualTo(5L);
        assertThat(entity.getPeticionsErrorUltimPeriode()).isEqualTo(2L);
        assertThat(entity.getTempsMigUltimPeriode()).isEqualTo(110);
    }
}
