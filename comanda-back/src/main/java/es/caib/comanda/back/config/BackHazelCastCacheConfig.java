package es.caib.comanda.back.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
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
        return config;
    }

}
