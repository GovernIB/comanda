package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.CacheHelper;
import es.caib.comanda.estadistica.logic.intf.model.cache.ComandaCache;
import es.caib.comanda.estadistica.logic.intf.service.CacheService;
import es.caib.comanda.estadistica.persist.entity.cache.FakeCacheEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotDeletedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CacheServiceImpl extends BaseMutableResourceService<ComandaCache, String, FakeCacheEntity> implements CacheService {

    private final CacheHelper cacheHelper;

    @Override
    public ComandaCache getOne(String s, String[] perspectives) throws ResourceNotFoundException {
        if ("TOTES".equals(s)) {
            var cache = ComandaCache.builder().build();
            cache.setId("TOTES");
            return cache;
        }

        return cacheHelper.getComandaCache(s);
    }

    @Override
    public Page<ComandaCache> findPage(String quickFilter, String filter, String[] namedQueries, String[] perspectives, Pageable pageable) {
        return new PageImpl<>(cacheHelper.getComandaCaches(), pageable, cacheHelper.getComandaCaches().size());
    }

    @Override
    public void delete(String s, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotFoundException, ResourceNotDeletedException, AnswerRequiredException {
        if ("TOTES".equals(s)) {
            cacheHelper.evictAllCaches();
        } else {
            cacheHelper.evictCache(s);
        }
    }

}