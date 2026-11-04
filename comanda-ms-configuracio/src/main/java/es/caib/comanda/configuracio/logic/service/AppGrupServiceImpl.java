package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.intf.model.AppGrup;
import es.caib.comanda.configuracio.logic.intf.service.AppGrupService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppGrupEntity;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Implementació del servei de gestió de grups d'aplicacions.
 */
@Service
@Transactional
public class AppGrupServiceImpl extends BaseMutableResourceService<AppGrup, Long, AppGrupEntity> implements AppGrupService {

    @Autowired(required = false)
    private CacheHelper cacheHelper;

    @Override
    protected void beforeDelete(AppGrupEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        super.beforeDelete(entity, answers);
        if (entity.getApps() != null) {
            for (AppEntity app : entity.getApps()) {
                app.setGrup(null);
                if (cacheHelper != null && app.getId() != null) {
                    cacheHelper.evictAppCacheItem(app.getId(), app.getCodi());
                }
            }
        }
    }
}
