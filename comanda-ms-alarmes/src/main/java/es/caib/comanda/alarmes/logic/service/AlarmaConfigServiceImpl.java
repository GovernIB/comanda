package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaConfigService;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementació del servei de gestió d'alarmes.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmaConfigServiceImpl extends BaseMutableResourceService<AlarmaConfig, Long, AlarmaConfigEntity> implements AlarmaConfigService {
    private final AuthenticationHelper authenticationHelper;

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
        String currentUser = authenticationHelper.getCurrentUserName();
        boolean isAdmin = authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
        if (isAdmin) return null;
        return "createdBy:'" + currentUser + "'";
    }

    @Override
    protected void beforeCreateEntity(AlarmaConfigEntity entity, AlarmaConfig resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {
        if (resource.isAdmin() && !authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)){
            throw new ResourceNotCreatedException(resource.getClass(), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.AlarmaConfigServiceImpl.beforeCreateEntity.not.admin"));
        }
    }

    @Override
    protected void beforeUpdateEntity(AlarmaConfigEntity entity, AlarmaConfig resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        if (entity.isAdmin() && !authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)) {
            throw new ResourceNotUpdatedException(getResourceClass(), String.valueOf(entity.getId()), I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.AlarmaConfigServiceImpl.beforeUpdateEntity.not.admin"));
        }
    }

}
