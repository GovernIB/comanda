package es.caib.comanda.ms.logic.helper;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.map.IMap;
import com.hazelcast.query.impl.predicates.SqlPredicate;
import com.hazelcast.spring.cache.HazelcastCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Map;
import java.util.Set;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheHelperTest {

    @Mock private CacheManager cacheManager;
    @Mock private Config hazleCastConfig;
    @Mock private Cache cache;
    @Mock private HazelcastCache hazelcastCache;
    @Mock private IMap<Object, Object> map;
    @InjectMocks private CacheHelper cacheHelper;

    @Test
    void evictCache_llamaAClear() {
        when(cacheManager.getCache("testCache")).thenReturn(cache);

        cacheHelper.evictCache("testCache");

        verify(cache).clear();
    }

    @Test
    void evictAllCaches_iteraYLimpiaTodas() {
        when(cacheManager.getCacheNames()).thenReturn(Set.of("cache1", "cache2"));
        when(cacheManager.getCache("cache1")).thenReturn(cache);
        when(cacheManager.getCache("cache2")).thenReturn(cache);

        cacheHelper.evictAllCaches();

        verify(cache, times(2)).clear();
    }

    @Test
    void evictCacheItem_eliminaLaClaveEspecifica() {
        when(cacheManager.getCache("testCache")).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);

        cacheHelper.evictCacheItem("testCache", "miClave");

        verify(map).remove("miClave");
    }

    @Test
    void evictCacheByPrefix_filtraYEliminaCachesCoincidentes() {
        when(cacheManager.getCacheNames()).thenReturn(Set.of("app_cache", "other_cache"));
        when(cacheManager.getCache("app_cache")).thenReturn(cache);

        cacheHelper.evictCacheByPrefix("app");

        verify(cache).clear();
        verify(cacheManager, never()).getCache("other_cache");
    }

    @Test
    void evictCacheItemByPrefix_filtraClavesYEliminaLasCoincidentes() {
        when(cacheManager.getCache("testCache")).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);
        when(map.keySet()).thenReturn(Set.of("prefix_1", "prefix_2", "otra_clave"));

        cacheHelper.evictCacheItemByPrefix("testCache", "prefix_");

        verify(map).remove("prefix_1");
        verify(map).remove("prefix_2");
        verify(map, never()).remove("otra_clave");
    }

    @Test
    void evictOneCacheByUser_eliminaClavesQueCoincidenConElPatron() {
        when(cacheManager.getCache("testCache")).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);
        when(map.keySet(any(SqlPredicate.class))).thenReturn(Set.of("clave_usuario_123_fecha", "otra_clave"));

        cacheHelper.evictOneCacheByUser("usuario", "testCache");

        verify(map).remove("clave_usuario_123_fecha");
        verify(map).remove("otra_clave");
    }

    @Test
    void evictOneCacheByUser_ignoraValoresNulosOVacios() {
        cacheHelper.evictOneCacheByUser(null, "testCache");
        cacheHelper.evictOneCacheByUser("   ", "testCache");

        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void getCache_devuelveLaCacheSolicitada() {
        when(cacheManager.getCache("miCache")).thenReturn(hazelcastCache);

        assertThat(cacheHelper.getCache("miCache")).isEqualTo(hazelcastCache);
    }

    @Test
    void getCacheNames_devuelveLasCachesDeLaConfiguracion() {
        MapConfig mockConfig = mock(MapConfig.class);
        when(hazleCastConfig.getMapConfigs()).thenReturn(Map.of("cacheA", mockConfig, "cacheB", mockConfig));

        assertThat(cacheHelper.getCacheNames()).containsExactlyInAnyOrder("cacheA", "cacheB");
    }

    @Test
    void evictDashboardWidgetCacheByUser_delegacionCorrecta() {
        when(cacheManager.getCache(DASHBOARD_WIDGET_CACHE)).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);
        when(map.keySet(any(SqlPredicate.class))).thenReturn(Set.of("_user_"));

        cacheHelper.evictDashboardWidgetCacheByUser("user");

        verify(map).remove("_user_");
    }

    @Test
    void evictDashboardWidgetCacheByUser_ignoraUsuarioNulo() {
        cacheHelper.evictDashboardWidgetCacheByUser(null);
        verify(cacheManager, never()).getCache(any());
    }

    @Test
    void evictEntornAppCacheItem_eliminaLasTresCaches() {
        when(cacheManager.getCache(anyString())).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);

        cacheHelper.evictEntornAppCacheItem(10L);

        verify(map, times(2)).remove("10");
        verify(hazelcastCache, times(1)).clear();
    }

    @Test
    void evictAppCacheItem_conCodiNuevoYViejoDiferente() {
        when(cacheManager.getCache(anyString())).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);

        cacheHelper.evictAppCacheItem(5L, "codiVell", "codiNou");

        verify(map).remove("5");
        verify(map).remove("codiVell");
        verify(map).remove("codiNou");
    }

    @Test
    void evictAppCacheItem_conCodiNuevoIgualAlViejoNoLoBorraDosVeces() {
        when(cacheManager.getCache(anyString())).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);

        cacheHelper.evictAppCacheItem(5L, "codi", "codi");

        verify(map).remove("5");
        verify(map).remove("codi");
        verify(map, times(1)).remove("codi");
    }

    @Test
    void evictEntornCacheItem_delegacionCorrecta() {
        when(cacheManager.getCache(anyString())).thenReturn(hazelcastCache);
        when(hazelcastCache.getNativeCache()).thenReturn(map);

        cacheHelper.evictEntornCacheItem(8L, "entornVell", "entornNou");

        verify(map).remove("8");
        verify(map).remove("entornVell");
        verify(map).remove("entornNou");
    }
}
