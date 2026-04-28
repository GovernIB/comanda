package es.caib.comanda.alarmes.back.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HazelCastCacheConfigTest {

    @Test
    @DisplayName("hazelcastConfig configura correctament la instància i els mapes")
    void hazelcastConfig_configuraCorrectament() {
        // Arrange
        HazelCastCacheConfig config = new HazelCastCacheConfig();

        // Act
        Config hazelcastConfig = config.hazelcastConfig();

        // Assert
        assertThat(hazelcastConfig.getInstanceName()).isEqualTo("shared-cache-instance");
        assertThat(hazelcastConfig.getClusterName()).isEqualTo("comanda-cluster");
        assertThat(hazelcastConfig.getNetworkConfig().getPort()).isEqualTo(5701);

        MapConfig entornAppCache = hazelcastConfig.getMapConfig("entornAppCache");
        assertThat(entornAppCache).isNotNull();
        assertThat(entornAppCache.getTimeToLiveSeconds()).isEqualTo(3600);

        MapConfig appCache = hazelcastConfig.getMapConfig("appCache");
        assertThat(appCache).isNotNull();
        assertThat(appCache.getTimeToLiveSeconds()).isEqualTo(3600);

        MapConfig entornCache = hazelcastConfig.getMapConfig("entornCache");
        assertThat(entornCache).isNotNull();
        assertThat(entornCache.getTimeToLiveSeconds()).isEqualTo(3600);

        MapConfig dashboardWidgetCache = hazelcastConfig.getMapConfig("dashboardWidgetCache");
        assertThat(dashboardWidgetCache).isNotNull();
        assertThat(dashboardWidgetCache.getTimeToLiveSeconds()).isEqualTo(28800);
        assertThat(dashboardWidgetCache.getEvictionConfig().getEvictionPolicy()).isEqualTo(EvictionPolicy.LRU);
    }
}
