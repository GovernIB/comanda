package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.intf.service.EntornService;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import es.caib.comanda.ms.sse.ComandaSseEvent;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_CACHE;

/**
 * Implementació del servei de gestió d'entorns.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EntornServiceImpl extends BaseMutableResourceService<Entorn, Long, EntornEntity> implements EntornService {

    private final EntornRepository entornRepository;
    private final CacheHelper cacheHelper;
    private final EntornAppHelper entornAppHelper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    protected List<EntornEntity> reorderFindLinesWithParent(Serializable parentId) {
        return entornRepository.findAllByOrderByOrdreAsc();
    }

    @Override
    protected void afterCreateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterCreateSave(entity, resource, answers, anyOrderChanged);

        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(ComandaSseEventTypes.ENTORN_CHANGED, entity.getId(), LocalDateTime.now())));
    }

    @Override
    protected void afterUpdateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        cacheHelper.evictCacheItem(ENTORN_CACHE, entity.getId().toString());
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(ComandaSseEventTypes.ENTORN_CHANGED, entity.getId(), LocalDateTime.now())));
    }

    @Override
    protected void afterDelete(EntornEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        super.afterDelete(entity, answers);

        cacheHelper.evictCacheItem(ENTORN_CACHE, entity.getId().toString());
        for (EntornAppEntity entornApp : entity.getEntornAppEntities()) {
            entornAppHelper.logicAfterDelete(entornApp.getId());
        }
        eventPublisher.publishEvent(new ComandaSsePublishRequest(
            new ComandaSseEvent(ComandaSseEventTypes.ENTORN_CHANGED, entity.getId(), LocalDateTime.now())));
    }
}
