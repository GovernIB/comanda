package es.caib.comanda.avisos.back.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HazelCastCacheConfigTest {

    @Test
    @DisplayName("hazelcastConfig ha de configurar correctament la instància i els mapes")
    void hazelcastConfig_haDeTenirConfiguracioCorrecta() {
        // Arrange
        HazelCastCacheConfig config = new HazelCastCacheConfig();

        // Act
        Config hazelcastConfig = config.hazelcastConfig();

        // Assert
        assertThat(hazelcastConfig.getInstanceName()).isEqualTo("shared-cache-instance");
        assertThat(hazelcastConfig.getClusterName()).isEqualTo("comanda-cluster");

        // Xarxa
        assertThat(hazelcastConfig.getNetworkConfig().getPort()).isEqualTo(5701);
        assertThat(hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled()).isTrue();
        assertThat(hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().getMembers()).contains("localhost");

        // Mapes
        assertThat(hazelcastConfig.getMapConfigs()).containsKey("entornAppCache");
        assertThat(hazelcastConfig.getMapConfigs()).containsKey("appCache");
        assertThat(hazelcastConfig.getMapConfigs()).containsKey("entornCache");
        assertThat(hazelcastConfig.getMapConfigs()).containsKey("dashboardWidgetCache");

        // TTL i Evicció
        MapConfig dashboardConfig = hazelcastConfig.getMapConfig("dashboardWidgetCache");
        assertThat(dashboardConfig.getTimeToLiveSeconds()).isEqualTo(28800);
        assertThat(dashboardConfig.getEvictionConfig().getEvictionPolicy()).isEqualTo(EvictionPolicy.LRU);
        assertThat(dashboardConfig.getEvictionConfig().getMaxSizePolicy()).isEqualTo(MaxSizePolicy.USED_HEAP_PERCENTAGE);
        
        assertThat(hazelcastConfig.getMapConfig("entornAppCache").getTimeToLiveSeconds()).isEqualTo(3600);
    }
}
