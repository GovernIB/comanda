package es.caib.comanda.ms.salut.helper.components;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorComponentsMemoriaTest {

    @Test
    void testRegistreIResetPeriode() {
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10);
        
        monitor.registraExit("C1", 100);
        monitor.registraError("C1");
        monitor.registraExit("C1", 200);
        
        Map<String, EstadistiquesComponent> snapshot1 = monitor.obtenSnapshot();
        assertThat(snapshot1).containsKey("C1");
        EstadistiquesComponent stats1 = snapshot1.get("C1");
        
        assertThat(stats1.getOkPeriode()).isEqualTo(2);
        assertThat(stats1.getErrorPeriode()).isEqualTo(1);
        assertThat(stats1.getTempsMigMsPeriode()).isEqualTo(150.0);
        assertThat(stats1.getOkTotal()).isEqualTo(2);
        assertThat(stats1.getErrorTotal()).isEqualTo(1);
        
        // El snapshot hauria d'haver resetejat el període
        Map<String, EstadistiquesComponent> snapshot2 = monitor.obtenSnapshot();
        EstadistiquesComponent stats2 = snapshot2.get("C1");
        assertThat(stats2.getOkPeriode()).isEqualTo(0);
        assertThat(stats2.getErrorPeriode()).isEqualTo(0);
        assertThat(stats2.getOkTotal()).isEqualTo(2); // El total es manté
    }

    @Test
    void testInicialitzaAmbEndpoint() {
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10);
        monitor.inicialitzaComponent("C1", "http://api.test");
        
        Map<String, EstadistiquesComponent> snapshot = monitor.obtenSnapshot();
        assertThat(snapshot.get("C1").getEndpoint()).isEqualTo("http://api.test");
        
        // Actualitza endpoint
        monitor.inicialitzaComponent("C1", "http://api.v2");
        snapshot = monitor.obtenSnapshot();
        assertThat(snapshot.get("C1").getEndpoint()).isEqualTo("http://api.v2");
    }

    @Test
    void testObtenFallback() {
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(2);
        
        // Encara no hi ha dades
        MonitorComponentsMemoria.DadesFallback fb1 = monitor.obtenFallback("C1");
        assertThat(fb1.te).isFalse();
        
        monitor.registraExit("C1", 50);
        monitor.registraError("C1");
        
        MonitorComponentsMemoria.DadesFallback fb2 = monitor.obtenFallback("C1");
        assertThat(fb2.te).isTrue();
        assertThat(fb2.ok).isEqualTo(1);
        assertThat(fb2.ko).isEqualTo(1);
        
        // Mida finestra = 2, afegim un altre i surt el primer OK
        monitor.registraError("C1");
        MonitorComponentsMemoria.DadesFallback fb3 = monitor.obtenFallback("C1");
        assertThat(fb3.ok).isEqualTo(0);
        assertThat(fb3.ko).isEqualTo(2);
    }

    @Test
    void testAmbRellotgeFix() {
        Instant ara = Instant.parse("2026-02-17T12:00:00Z");
        Clock clock = Clock.fixed(ara, ZoneId.of("UTC"));
        MonitorComponentsMemoria monitor = new MonitorComponentsMemoria(10, clock);
        
        monitor.registraExit("C1", 10);
        Map<String, EstadistiquesComponent> snapshot = monitor.obtenSnapshot();
        
        assertThat(snapshot.get("C1").getInstantSnapshot()).isEqualTo(ara);
    }
}
