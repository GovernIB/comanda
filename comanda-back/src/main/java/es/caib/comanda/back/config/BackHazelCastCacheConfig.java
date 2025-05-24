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
        config.addMapConfig(new MapConfig()
                .setName("entornAppCache") // Nom de la memòria cau
                .setTimeToLiveSeconds(3600)); // Expiració a 1 hora (3600 segons)
        return config;
    }

}
