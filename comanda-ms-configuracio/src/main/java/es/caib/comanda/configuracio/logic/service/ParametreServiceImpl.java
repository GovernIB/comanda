package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.configuracio.logic.intf.model.Parametre;
import es.caib.comanda.configuracio.logic.intf.service.ParametreService;
import es.caib.comanda.configuracio.persist.entity.ParametreEntity;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.config.BaseConfig;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static es.caib.comanda.ms.back.config.HazelCastCacheConfig.PARAMETRE_CACHE;

/**
 * Implementació del servei de gestió de paràmetres.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ParametreServiceImpl extends BaseMutableResourceService<Parametre, Long, ParametreEntity> implements ParametreService {

    private final MonitorServiceClient monitorServiceClient;
    private final CacheHelper cacheHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Override
    protected void afterUpdateSave(ParametreEntity entity, Parametre resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        cacheHelper.evictCacheItem(PARAMETRE_CACHE, entity.getId().toString());

        switch (entity.getCodi()) {
            case BaseConfig.PROP_MONITOR_BUIDAT_PERIODE_MINUTS:
            case BaseConfig.PROP_MONITOR_BUIDAT_RETENCIO_DIES:
                monitorServiceClient.programarBorrat(httpAuthorizationHeaderHelper.getAuthorizationHeader());
                break;
        }
    }

}
