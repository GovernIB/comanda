package es.caib.comanda.salut.persist.entity;

import es.caib.comanda.salut.logic.intf.model.SalutEstat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SalutEntityTest {

    @Test
    void latencies_quanEsInicialitzenIIncrementen_actualitzaMitjanesIComptadors() {
        SalutEntity entity = new SalutEntity();

        entity.setAppLatenciaMitjana(null);
        entity.setBdLatenciaMitjana(null);
        entity.addAppLatenciaMitjana(null);
        entity.addBdLatenciaMitjana(null);

        assertThat(entity.getAppLatenciaMitjana()).isNull();
        assertThat(entity.getBdLatenciaMitjana()).isNull();

        entity.setAppLatenciaMitjana(100);
        entity.addAppLatenciaMitjana(200);
        entity.setBdLatenciaMitjana(80);
        entity.addBdLatenciaMitjana(40);

        assertThat(entity.getAppLatenciaMitjana()).isEqualTo(150);
        assertThat(entity.getAppLatenciaNumElements()).isEqualTo(2);
        assertThat(entity.getBdLatenciaMitjana()).isEqualTo(60);
        assertThat(entity.getBdLatenciaNumElements()).isEqualTo(2);
    }

    @Test
    void updateCountByEstat_quanRepTotsElsEstats_incrementaElsComptadorsEsperats() {
        SalutEntity entity = new SalutEntity();

        entity.updateAppCountByEstat(SalutEstat.UP);
        entity.updateAppCountByEstat(SalutEstat.WARN);
        entity.updateAppCountByEstat(SalutEstat.DEGRADED);
        entity.updateAppCountByEstat(SalutEstat.DOWN);
        entity.updateAppCountByEstat(SalutEstat.ERROR);
        entity.updateAppCountByEstat(SalutEstat.MAINTENANCE);
        entity.updateAppCountByEstat(SalutEstat.UNKNOWN);

        entity.updateBdCountByEstat(SalutEstat.UP);
        entity.updateBdCountByEstat(SalutEstat.WARN);
        entity.updateBdCountByEstat(SalutEstat.DEGRADED);
        entity.updateBdCountByEstat(SalutEstat.DOWN);
        entity.updateBdCountByEstat(SalutEstat.ERROR);
        entity.updateBdCountByEstat(SalutEstat.MAINTENANCE);
        entity.updateBdCountByEstat(SalutEstat.UNKNOWN);

        assertThat(entity.getAppCountUp()).isEqualTo(1);
        assertThat(entity.getAppCountWarn()).isEqualTo(1);
        assertThat(entity.getAppCountDegraded()).isEqualTo(1);
        assertThat(entity.getAppCountDown()).isEqualTo(1);
        assertThat(entity.getAppCountError()).isEqualTo(1);
        assertThat(entity.getAppCountMaintenance()).isEqualTo(1);
        assertThat(entity.getAppCountUnknown()).isEqualTo(1);

        assertThat(entity.getBdCountUp()).isEqualTo(1);
        assertThat(entity.getBdCountWarn()).isEqualTo(1);
        assertThat(entity.getBdCountDegraded()).isEqualTo(1);
        assertThat(entity.getBdCountDown()).isEqualTo(1);
        assertThat(entity.getBdCountError()).isEqualTo(1);
        assertThat(entity.getBdCountMaintenance()).isEqualTo(1);
        assertThat(entity.getBdCountUnknown()).isEqualTo(1);
    }

    @Test
    void getPctMethods_quanNoHiHaElements_retornenNullIQuanNHiHaCalculenPercentatge() {
        SalutEntity entity = new SalutEntity();

        assertThat(entity.getAppPctUp()).isNull();
        assertThat(entity.getBdPctUp()).isNull();

        entity.setNumElements(4);
        entity.updateAppCountByEstat(SalutEstat.UP);
        entity.updateAppCountByEstat(SalutEstat.UP);
        entity.updateAppCountByEstat(SalutEstat.WARN);
        entity.updateAppCountByEstat(SalutEstat.DOWN);
        entity.updateBdCountByEstat(SalutEstat.UP);
        entity.updateBdCountByEstat(SalutEstat.ERROR);
        entity.updateBdCountByEstat(SalutEstat.ERROR);
        entity.updateBdCountByEstat(SalutEstat.UNKNOWN);

        assertThat(entity.getAppPctUp()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(entity.getAppPctWarn()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(entity.getAppPctDown()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(entity.getBdPctUp()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(entity.getBdPctError()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(entity.getBdPctUnknown()).isEqualByComparingTo(new BigDecimal("25.00"));
    }
}
