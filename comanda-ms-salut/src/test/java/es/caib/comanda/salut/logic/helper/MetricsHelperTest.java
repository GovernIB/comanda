package es.caib.comanda.salut.logic.helper;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsHelperTest {

    private SimpleMeterRegistry meterRegistry;
    private MetricsHelper metricsHelper;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsHelper = new MetricsHelper(meterRegistry);
    }

    @Test
    void getSalutInfoGlobalTimer_quanNoHiHaTags_creaElTimerGlobalSenseEtiquetes() {
        Timer timer = metricsHelper.getSalutInfoGlobalTimer(null, null);

        assertThat(timer.getId().getName()).isEqualTo("salut_info_global_latencia_milliseconds");
        assertThat(timer.getId().getTags()).isEmpty();
    }

    @Test
    void getSalutInfoGlobalTimer_quanHiHaEntornIApp_registraLesEtiquetesCorrespondents() {
        Timer timer = metricsHelper.getSalutInfoGlobalTimer("PRO", "comanda");

        assertThat(timer.getId().getTag("entorn")).isEqualTo("PRO");
        assertThat(timer.getId().getTag("app")).isEqualTo("comanda");
    }

    @Test
    void getSalutLastTimers_quanEsConsulten_registraElsNomsEsperatsAlRegistry() {
        Timer global = metricsHelper.getSalutLastGlobalTimer();
        Timer entornApps = metricsHelper.getSalutLastEntornAppsTimer();
        Timer dades = metricsHelper.getSalutLastDadesTimer();

        assertThat(global.getId().getName()).isEqualTo("salut_last_global_latencia_milliseconds");
        assertThat(entornApps.getId().getName()).isEqualTo("salut_last_entornapps_latencia_milliseconds");
        assertThat(dades.getId().getName()).isEqualTo("salut_last_dades_latencia_milliseconds");
        assertThat(meterRegistry.find("salut_last_global_latencia_milliseconds").timer()).isSameAs(global);
        assertThat(meterRegistry.find("salut_last_entornapps_latencia_milliseconds").timer()).isSameAs(entornApps);
        assertThat(meterRegistry.find("salut_last_dades_latencia_milliseconds").timer()).isSameAs(dades);
    }
}
