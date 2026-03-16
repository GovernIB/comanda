package es.caib.comanda.tasques.back.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitari per a la configuració de HazelCast.
 */
class HazelCastCacheConfigTest {

    @Test
    @DisplayName("GIVEN HazelCastCacheConfig WHEN hazelcastConfig is called THEN returns expected configuration")
    void hazelcastConfig_ShouldReturnExpectedConfiguration() {
        // Arrange
        HazelCastCacheConfig config = new HazelCastCacheConfig();

        // Act
        Config hazelConfig = config.hazelcastConfig();

        // Assert
        assertThat(hazelConfig.getInstanceName()).isEqualTo("shared-cache-instance");
        assertThat(hazelConfig.getClusterName()).isEqualTo("comanda-cluster");

        // Network
        assertThat(hazelConfig.getNetworkConfig().getPort()).isEqualTo(5701);
        assertThat(hazelConfig.getNetworkConfig().isPortAutoIncrement()).isTrue();
        assertThat(hazelConfig.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled()).isTrue();
        assertThat(hazelConfig.getNetworkConfig().getJoin().getTcpIpConfig().getMembers()).contains("localhost");

        // Maps
        assertThat(hazelConfig.getMapConfigs()).containsKeys("entornAppCache", "appCache", "entornCache", "dashboardWidgetCache");

        // Specific Map TTLs
        assertThat(hazelConfig.getMapConfig("entornAppCache").getTimeToLiveSeconds()).isEqualTo(3600);
        assertThat(hazelConfig.getMapConfig("appCache").getTimeToLiveSeconds()).isEqualTo(3600);
        assertThat(hazelConfig.getMapConfig("entornCache").getTimeToLiveSeconds()).isEqualTo(3600);
        assertThat(hazelConfig.getMapConfig("dashboardWidgetCache").getTimeToLiveSeconds()).isEqualTo(28800);

        // Eviction configuration for dashboardWidgetCache
        assertThat(hazelConfig.getMapConfig("dashboardWidgetCache").getEvictionConfig().getEvictionPolicy()).isEqualTo(EvictionPolicy.LRU);
        assertThat(hazelConfig.getMapConfig("dashboardWidgetCache").getEvictionConfig().getMaxSizePolicy()).isEqualTo(MaxSizePolicy.USED_HEAP_PERCENTAGE);
    }
}
