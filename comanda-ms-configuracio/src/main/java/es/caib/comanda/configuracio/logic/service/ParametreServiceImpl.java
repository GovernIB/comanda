package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.Parametre;
import es.caib.comanda.configuracio.logic.intf.service.ParametreService;
import es.caib.comanda.configuracio.persist.entity.ParametreEntity;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementació del servei de gestió de paràmetres.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParametreServiceImpl extends BaseMutableResourceService<Parametre, Long, ParametreEntity> implements ParametreService {

//    private final CacheHelper cacheHelper;

//    @Override
//    protected void afterUpdateSave(ParametreEntity entity, Parametre resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
//        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
//        cacheHelper.evictCacheItem(PARAMETRE_CACHE, entity.getId().toString());
//    }

}
