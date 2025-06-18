package es.caib.comanda.estadistica.logic.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.spring.cache.HazelcastCache;
import es.caib.comanda.estadistica.logic.intf.model.cache.ComandaCache;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final CacheManager cacheManager;
    private final Config hazleCastConfig;
    
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

    // Servei de caché

    public ComandaCache getComandaCache(String id) {
        String descripcio = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.cache." + id, null);
        var cache = (HazelcastCache) cacheManager.getCache(id);
        if (cache == null) {
            return ComandaCache.builder()
                    .descripcio(descripcio)
                    .entrades(0)
                    .mida(0L)
                    .build();
        }

        ComandaCache comandaCache = ComandaCache.builder()
                .descripcio(descripcio)
                .entrades(cache.getNativeCache().size())
                .mida(cache.getNativeCache().values().stream()
                        .mapToLong(value -> {
                            try {
                                return objectMapper.writeValueAsBytes(value).length;
                            } catch (Exception e) {
                                log.error("Error calculating cache value size", e);
                                return 0L;
                            }
                        })
                        .sum()
                )
                .build();
        comandaCache.setId(id);
        return comandaCache;
    }

    public List<ComandaCache> getComandaCaches() {
        return hazleCastConfig.getMapConfigs().keySet().stream()
                .map(name -> getComandaCache(name))
                .collect(Collectors.toList());
    }

}
