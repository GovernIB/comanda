package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import es.caib.comanda.estadistica.logic.intf.model.cache.ComandaCache;
import es.caib.comanda.estadistica.logic.intf.service.CacheService;
import es.caib.comanda.estadistica.persist.entity.cache.FakeCacheEntity;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotDeletedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheServiceImpl extends BaseMutableResourceService<ComandaCache, String, FakeCacheEntity> implements CacheService {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final CacheHelper cacheHelper;

    @Override
    public ComandaCache getOne(String s, String[] perspectives) throws ResourceNotFoundException {
        if ("TOTES".equals(s)) {
            var cache = ComandaCache.builder().build();
            cache.setId("TOTES");
            return cache;
        }

        return getComandaCache(s);
    }

    @Override
    public Page<ComandaCache> findPage(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable) {
        return new PageImpl<>(getComandaCaches(), pageable, getComandaCaches().size());
    }

    @Override
    public void delete(String s, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotFoundException, ResourceNotDeletedException, AnswerRequiredException {
        if ("TOTES".equals(s)) {
            cacheHelper.evictAllCaches();
        } else {
            cacheHelper.evictCache(s);
        }
    }

    private ComandaCache getComandaCache(String id) {
        String descripcio = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.estadistica.cache." + id, null);
        var cache = cacheHelper.getCache(id);
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

    private List<ComandaCache> getComandaCaches() {
        return cacheHelper.getCacheNames().stream()
                .map(name -> getComandaCache(name))
                .collect(Collectors.toList());
    }

}