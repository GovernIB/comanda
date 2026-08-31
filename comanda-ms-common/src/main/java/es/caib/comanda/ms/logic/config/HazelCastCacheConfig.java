package es.caib.comanda.ms.logic.config;

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

@Configuration
@EnableCaching
public class HazelCastCacheConfig {

    public static final String ENTORN_APP_CACHE = "entornAppCache";
    public static final String ENTORN_APP_BY_APP_AND_ENTORN_CACHE = "entornAppByAppAndEntornCache";
    public static final String ENTORN_APP_INTEGRACIONS_SUBSISTEMES_CONTEXTS_CACHE = "entornAppIntSubContCache";
    public static final String APP_CACHE = "appCache";
    public static final String ENTORN_CACHE = "entornCache";
    public static final String DASHBOARD_WIDGET_CACHE = "dashboardWidgetCache";
    public static final String PARAMETRE_CACHE = "parametreCache";
    public static final String ACL_CACHE = "aclCache";
    public static final String ACL_HAS_PERMISSION_CACHE = "aclCountCache";
    public static final String ACL_IDS_WITH_PERMISSION_CACHE = "aclCountCache";
    public static final String ACL_COUNT_CACHE = "aclCountCache";
//    public static final String ACL_CHECK_CACHE = "aclCheckCache";
    public static final String USUARI_CACHE = "usuariCache";
    public static final String ORG_TREE_CACHE = "orgTreeCache";
    public static final String ORGANIGRAMA_CACHE = "organigramaCache";

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("shared-cache-instance");
        config.setClusterName("comanda-cluster");

        config.getNetworkConfig().setPort(5701).setPortAutoIncrement(true);
//        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(true);
        // O configuració TCP-IP explícita si multicast no funciona
        config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true).addMember("localhost");

        config.addMapConfig(new MapConfig().setName(ENTORN_APP_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ENTORN_APP_BY_APP_AND_ENTORN_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ENTORN_APP_INTEGRACIONS_SUBSISTEMES_CONTEXTS_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(APP_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ENTORN_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(DASHBOARD_WIDGET_CACHE).setTimeToLiveSeconds(28800).setEvictionConfig(getEvictionConfig())); // 8 hores de TTL
        config.addMapConfig(new MapConfig().setName(PARAMETRE_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ACL_CACHE).setTimeToLiveSeconds(900).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ACL_HAS_PERMISSION_CACHE).setTimeToLiveSeconds(86400).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ACL_IDS_WITH_PERMISSION_CACHE).setTimeToLiveSeconds(86400).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(ACL_COUNT_CACHE).setTimeToLiveSeconds(86400).setEvictionConfig(getEvictionConfig()));
//        config.addMapConfig(new MapConfig().setName(ACL_CHECK_CACHE).setTimeToLiveSeconds(900).setEvictionConfig(getEvictionConfig()));
        config.addMapConfig(new MapConfig().setName(USUARI_CACHE).setTimeToLiveSeconds(900).setEvictionConfig(getEvictionConfig()));
        // Arbre d'unitats organitzatives per arrel (codiUnitatArrel -> mapa de fills) - només canvia amb sincronitzacions Dir3 manuals.
        config.addMapConfig(new MapConfig().setName(ORG_TREE_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
        // Llista d'unitats organitzatives d'un arrel per a la pantalla d'organigrama - només canvia amb sincronitzacions Dir3 manuals.
        config.addMapConfig(new MapConfig().setName(ORGANIGRAMA_CACHE).setTimeToLiveSeconds(3600).setEvictionConfig(getEvictionConfig()));
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
