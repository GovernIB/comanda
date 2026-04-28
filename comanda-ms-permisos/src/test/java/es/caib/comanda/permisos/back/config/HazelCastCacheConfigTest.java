package es.caib.comanda.permisos.back.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HazelCastCacheConfigTest {

    @Test
    void hazelcastConfig_defineixElsMapesIPolitiquesEsperades() {
        // Comprova que la configuració de cache defineix els mapes i polítiques esperades del mòdul.
        HazelCastCacheConfig config = new HazelCastCacheConfig();

        com.hazelcast.config.Config hazelConfig = config.hazelcastConfig();

        assertThat(hazelConfig.getInstanceName()).isEqualTo("shared-cache-instance");
        assertThat(hazelConfig.getClusterName()).isEqualTo("comanda-cluster");
        assertThat(hazelConfig.getMapConfigs()).containsKeys("entornAppCache", "appCache", "entornCache", "dashboardWidgetCache");
        assertThat(hazelConfig.getMapConfigs().get("dashboardWidgetCache").getEvictionConfig().getEvictionPolicy().name()).isEqualTo("LRU");
        assertThat(hazelConfig.getNetworkConfig().getJoin().getTcpIpConfig().getMembers()).contains("localhost");
    }
}
