package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.intf.service.EntornService;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static es.caib.comanda.ms.back.config.HazelCastCacheConfig.ENTORN_CACHE;

/**
 * Implementació del servei de gestió d'entorns.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EntornServiceImpl extends BaseMutableResourceService<Entorn, Long, EntornEntity> implements EntornService {

    private final CacheHelper cacheHelper;

    @Override
    protected void afterUpdateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        cacheHelper.evictCacheItem(ENTORN_CACHE, entity.getId().toString());
    }

}
