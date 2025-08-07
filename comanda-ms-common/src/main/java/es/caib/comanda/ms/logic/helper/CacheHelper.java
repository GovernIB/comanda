package es.caib.comanda.ms.logic.helper;

import com.hazelcast.config.Config;
import com.hazelcast.spring.cache.HazelcastCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHelper {

    private final CacheManager cacheManager;
    private final Config hazleCastConfig;

    /**
     * Neteja una cache específica identificada pel seu nom.
     *
     * @param cacheName nom de la cache que es vol netejar
     */
    public void evictCache(String cacheName) {
        log.info("Evicting cache '{}'", cacheName);
        if (cacheManager != null) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    /**
     * Neteja totes les caches disponibles en el sistema.
     */
    public void evictAllCaches() {
        log.info("Evicting all caches");
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> evictCache(cacheName));
        }
    }

    /**
     * Elimina un element específic d'una cache utilitzant la seva clau.
     *
     * @param cacheName nom de la cache
     * @param key       clau de l'element a eliminar
     */
    public void evictCacheItem(String cacheName, String key) {
        log.info("Evicting item in cache '{}' by key '{}'", cacheName, key);
        if (cacheManager != null) {
            var cache = (HazelcastCache) cacheManager.getCache(cacheName);
            if (cache != null && cache.getNativeCache() != null) {
                cache.getNativeCache().remove(key);
            }
        }
    }

    /**
     * Neteja totes les caches que comencen amb un prefix específic.
     *
     * @param cacheNamePrefix prefix del nom de les caches a netejar
     */
    public void evictCacheByPrefix(String cacheNamePrefix) {
        log.info("Evicting cache '{}' by prefix '{}'", cacheNamePrefix, cacheNamePrefix);
        if (cacheManager != null) {
            cacheManager.getCacheNames().stream()
                    .filter(cacheName -> cacheName.startsWith(cacheNamePrefix))
                    .forEach(cacheName -> evictCache(cacheName));
        }
    }

    /**
     * Elimina tots els elements d'una cache que tenen claus que comencen amb un prefix específic.
     *
     * @param cacheName       nom de la cache
     * @param cacheItemPrefix prefix de les claus dels elements a eliminar
     */
    public void evictCacheItemByPrefix(String cacheName, String cacheItemPrefix) {
        log.info("Evicting item in cache '{}' by prefix '{}'", cacheName, cacheItemPrefix);
        if (cacheManager != null) {
            var cache = (HazelcastCache) cacheManager.getCache(cacheName);
            if (cache != null && cache.getNativeCache() != null && cache.getNativeCache().keySet() != null) {
                cache.getNativeCache().keySet().stream()
                        .filter(key -> key.toString().startsWith(cacheItemPrefix))
                        .forEach(key -> cache.getNativeCache().remove(key));
                log.info("Netejades caches");
            }
        }
    }

    /**
     * Obté una cache específica pel seu nom.
     *
     * @param cacheName nom de la cache a obtenir
     * @return la cache sol·licitada o null si no existeix
     */
    public HazelcastCache getCache(String cacheName) {
        return cacheManager != null ? (HazelcastCache) cacheManager.getCache(cacheName) : null;
    }

    /**
     * Obté els noms de totes les caches configurades.
     *
     * @return conjunt amb els noms de les caches
     */
    public Set<String> getCacheNames() {
        return hazleCastConfig != null && hazleCastConfig.getMapConfigs() != null ?
                hazleCastConfig.getMapConfigs().keySet() : Set.of();
    }

}
