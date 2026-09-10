package es.caib.comanda.ms.logic.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HazelCastCacheConfigTest {

    @Test
    @DisplayName("hazelcastConfig configura correctament la instància, la xarxa i tots els mapes")
    void hazelcastConfig_configuraCorrectament() {
        HazelCastCacheConfig config = new HazelCastCacheConfig();

        Config hazelcastConfig = config.hazelcastConfig();

        assertThat(hazelcastConfig.getInstanceName()).isEqualTo("shared-cache-instance");
        assertThat(hazelcastConfig.getClusterName()).isEqualTo("comanda-cluster");
        assertThat(hazelcastConfig.getNetworkConfig().getPort()).isEqualTo(5701);
        assertThat(hazelcastConfig.getNetworkConfig().isPortAutoIncrement()).isTrue();
        assertThat(hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().isEnabled()).isTrue();
        assertThat(hazelcastConfig.getNetworkConfig().getJoin().getTcpIpConfig().getMembers()).contains("localhost");

        // Mapes estàndard (TTL: 3600s)
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ENTORN_APP_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ENTORN_APP_BY_APP_AND_ENTORN_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ENTORN_APP_INTEGRACIONS_SUBSISTEMES_CONTEXTS_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.APP_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ENTORN_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.PARAMETRE_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ORG_TREE_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ORGANIGRAMA_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.APP_BY_CODI_CACHE, 3600);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ENTORN_BY_CODI_CACHE, 3600);

        // Mapes de TTL curt (900s)
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ACL_CACHE, 900);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.USUARI_CACHE, 900);

        // Mapes de TTL llarg
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.DASHBOARD_WIDGET_CACHE, 28800);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ACL_HAS_PERMISSION_CACHE, 86400);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ACL_IDS_WITH_PERMISSION_CACHE, 86400);
        assertMapConfig(hazelcastConfig, HazelCastCacheConfig.ACL_COUNT_CACHE, 86400);
    }

    private void assertMapConfig(Config config, String mapName, int expectedTtlSeconds) {
        MapConfig mapConfig = config.getMapConfig(mapName);
        assertThat(mapConfig)
                .as("El mapa '%s' ha d'estar configurat", mapName)
                .isNotNull();
        assertThat(mapConfig.getTimeToLiveSeconds())
                .as("TTL per al mapa '%s'", mapName)
                .isEqualTo(expectedTtlSeconds);
        assertThat(mapConfig.getEvictionConfig().getEvictionPolicy())
                .as("EvictionPolicy per al mapa '%s'", mapName)
                .isEqualTo(EvictionPolicy.LRU);
        assertThat(mapConfig.getEvictionConfig().getMaxSizePolicy())
                .as("MaxSizePolicy per al mapa '%s'", mapName)
                .isEqualTo(MaxSizePolicy.USED_HEAP_PERCENTAGE);
    }
}
