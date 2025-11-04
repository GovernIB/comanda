package es.caib.comanda.acl.back.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!back")
@Configuration
@EnableCaching
public class HazelCastCacheConfig {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("shared-cache-instance");
        config.setClusterName("comanda-cluster");

        config.getNetworkConfig().setPort(5701).setPortAutoIncrement(true);
//        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
        // O configuració TCP-IP explícita si multicast no funciona
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("localhost");

        config.addMapConfig(new MapConfig().setName("entornAppCache").setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName("appCache").setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName("entornCache").setTimeToLiveSeconds(3600));
        config.addMapConfig(new MapConfig().setName("dashboardWidgetCache").setTimeToLiveSeconds(28800).setEvictionConfig(getEvictionConfig())); // 8 hores de TTL
        // Caché per a resolució ràpida d'ACL (invalidada quan es modifiquen permisos)
        config.addMapConfig(new MapConfig().setName("aclCheckCache").setTimeToLiveSeconds(900).setEvictionConfig(getEvictionConfig())); // 15 min TTL
        // Caché per al motor Spring Security ACL
        config.addMapConfig(new MapConfig().setName("springAclCache").setTimeToLiveSeconds(900).setEvictionConfig(getEvictionConfig()));
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
