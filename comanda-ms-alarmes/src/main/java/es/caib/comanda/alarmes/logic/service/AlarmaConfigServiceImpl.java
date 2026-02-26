package es.caib.comanda.alarmes.logic.service;

import es.caib.comanda.alarmes.logic.intf.model.AlarmaConfig;
import es.caib.comanda.alarmes.logic.intf.service.AlarmaConfigService;
import es.caib.comanda.alarmes.persist.entity.AlarmaConfigEntity;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
