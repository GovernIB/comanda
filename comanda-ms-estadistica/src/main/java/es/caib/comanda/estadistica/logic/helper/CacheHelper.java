package es.caib.comanda.estadistica.logic.helper;

import com.hazelcast.spring.cache.HazelcastCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHelper {

    private final CacheManager cacheManager;
    
    public void evictCache(String cacheName) {
        log.info("Evicting cache '{}'", cacheName);
        cacheManager.getCache(cacheName).clear();
    }
    
    public void evictAllCaches() {
        log.info("Evicting all caches");
        cacheManager.getCacheNames().forEach(cacheName -> evictCache(cacheName));
    }

    public void evictCacheByPrefix(String cacheNamePrefix) {
        log.info("Evicting cache '{}' by prefix '{}'", cacheNamePrefix, cacheNamePrefix);
        cacheManager.getCacheNames().stream()
                .filter(cacheName -> cacheName.startsWith(cacheNamePrefix))
                .forEach(cacheName -> evictCache(cacheName));
    }
    
    public void evictCacheItemByPrefix(String cacheName, String cacheItemPrefix) {
        log.info("Evicting item in cache '{}' by prefix '{}'", cacheName, cacheItemPrefix);
        var cache = (HazelcastCache) cacheManager.getCache(cacheName);
        cache.getNativeCache().keySet().stream()
                .filter(key -> key.toString().startsWith(cacheItemPrefix))
                .forEach(key -> cache.getNativeCache().remove(key));
        log.info("Netejades caches");
    }

}
