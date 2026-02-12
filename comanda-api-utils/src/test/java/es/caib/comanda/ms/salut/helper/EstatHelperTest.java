package es.caib.comanda.ms.salut.helper;

import es.caib.comanda.model.server.monitoring.EstatSalutEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EstatHelperTest {

    @Test
    void calculaEstat_percentatge_returnsCorrectStatus() {
        // Rang DOWN: >= 100
        assertThat(EstatHelper.calculaEstat(100.0)).isEqualTo(EstatSalutEnum.DOWN);
        assertThat(EstatHelper.calculaEstat(101.0)).isEqualTo(EstatSalutEnum.DOWN);

        // Rang ERROR: > 50 i < 100
        assertThat(EstatHelper.calculaEstat(51.0)).isEqualTo(EstatSalutEnum.ERROR);
        assertThat(EstatHelper.calculaEstat(99.9)).isEqualTo(EstatSalutEnum.ERROR);

        // Rang DEGRADED: > 20 i <= 50
        assertThat(EstatHelper.calculaEstat(21.0)).isEqualTo(EstatSalutEnum.DEGRADED);
        assertThat(EstatHelper.calculaEstat(50.0)).isEqualTo(EstatSalutEnum.DEGRADED);

        // Rang WARN: > 10 i <= 20
        assertThat(EstatHelper.calculaEstat(11.0)).isEqualTo(EstatSalutEnum.WARN);
        assertThat(EstatHelper.calculaEstat(20.0)).isEqualTo(EstatSalutEnum.WARN);

        // Rang UP: <= 10
        assertThat(EstatHelper.calculaEstat(10.0)).isEqualTo(EstatSalutEnum.UP);
        assertThat(EstatHelper.calculaEstat(5.0)).isEqualTo(EstatSalutEnum.UP);
        assertThat(EstatHelper.calculaEstat(0.0)).isEqualTo(EstatSalutEnum.UP);
    }

    @Test
    void calculaEstat_execucions_returnsCorrectStatus() {
        // 100% error (execucionsOk = 0, execucionsError = 10)
        assertThat(EstatHelper.calculaEstat(0, 10)).isEqualTo(EstatSalutEnum.DOWN);
        
        // 0% error (execucionsOk = 10, execucionsError = 0)
        assertThat(EstatHelper.calculaEstat(10, 0)).isEqualTo(EstatSalutEnum.UP);
        
        // 50% error (execucionsOk = 5, execucionsError = 5)
        assertThat(EstatHelper.calculaEstat(5, 5)).isEqualTo(EstatSalutEnum.DEGRADED);

        // Cas límit total = 0
        assertThat(EstatHelper.calculaEstat(0, 0)).isEqualTo(EstatSalutEnum.UP);
    }

    @Test
    void mergeEstats_returnsHighestSeverity() {
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.UP, EstatSalutEnum.DOWN)).isEqualTo(EstatSalutEnum.DOWN);
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.DOWN, EstatSalutEnum.ERROR)).isEqualTo(EstatSalutEnum.DOWN);
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.ERROR, EstatSalutEnum.DEGRADED)).isEqualTo(EstatSalutEnum.ERROR);
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.DEGRADED, EstatSalutEnum.WARN)).isEqualTo(EstatSalutEnum.DEGRADED);
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.WARN, EstatSalutEnum.UP)).isEqualTo(EstatSalutEnum.WARN);
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.UP, EstatSalutEnum.UP)).isEqualTo(EstatSalutEnum.UP);
        
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.UNKNOWN, EstatSalutEnum.UP)).isEqualTo(EstatSalutEnum.UP);
        assertThat(EstatHelper.mergeEstats(EstatSalutEnum.UP, EstatSalutEnum.UNKNOWN)).isEqualTo(EstatSalutEnum.UP);
    }
}
