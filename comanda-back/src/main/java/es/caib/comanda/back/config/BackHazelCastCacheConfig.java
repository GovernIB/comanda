package es.caib.comanda.back.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class BackHazelCastCacheConfig {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.addMapConfig(new MapConfig().setName("entornAppCache").setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName("appCache").setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName("entornCache").setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName("dashboardWidgetCache").setTimeToLiveSeconds(28800).setEvictionConfig(getEvictionConfig())); // 8 hores de TTL
        return config;
    }

    private EvictionConfig getEvictionConfig() {
        return new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.USED_HEAP_PERCENTAGE);
    }

}
