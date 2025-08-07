package es.caib.comanda.api.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;

import static es.caib.comanda.ms.back.config.HazelCastCacheConfig.*;

//@Configuration
//@EnableCaching
public class ApiHazelCastCacheConfig {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("shared-cache-instance");
        config.setClusterName("comanda-cluster");

        config.getNetworkConfig().setPort(5701).setPortAutoIncrement(true);
//        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
        // O configuració TCP-IP explícita si multicast no funciona
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("localhost");

        config.addMapConfig(new MapConfig().setName(ENTORN_APP_CACHE).setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName(APP_CACHE).setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName(ENTORN_CACHE).setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName(DASHBOARD_WIDGET_CACHE).setTimeToLiveSeconds(28800).setEvictionConfig(getEvictionConfig())); // 8 hores de TTL
        return config;
    }

    private EvictionConfig getEvictionConfig() {
        return new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }

}
