package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.helper.DashboardItemTitolHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.logic.intf.service.DashboardTitolService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els títols de dashboards.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardTitolServiceImpl extends BaseMutableResourceService<DashboardTitol, Long, DashboardTitolEntity> implements DashboardTitolService {

	private final DashboardItemTitolHelper dashboardItemTitolHelper;
    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;

    @Override
    protected String additionalSpringFilter(
        String currentSpringFilter,
        String[] namedQueries) {
        if (authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
            || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)) {
            return currentSpringFilter;
        }
        Set<Serializable> appPermissionIds = getAllowedIds(ResourceType.APP,
            List.of(PermissionEnum.PERM0, PermissionEnum.PERM1));
        String appFilter = SpringFilterHelper.buildOrFilter("dashboard.appId", appPermissionIds);

        Set<Serializable> entornAppPermissionIds = getAllowedIds(ResourceType.ENTORN_APP,
            List.of(PermissionEnum.PERM0, PermissionEnum.PERM1));
        String entornAppFilter = SpringFilterHelper.buildOrFilter("dashboard.entornId", entornAppPermissionIds);

        Set<Serializable> dashboardPermissionIds = getAllowedIds(ResourceType.DASHBOARD,
            List.of(PermissionEnum.READ, PermissionEnum.WRITE));
        String dashboardFilter = SpringFilterHelper.buildOrFilter("dashboard.id", dashboardPermissionIds);

        String filter = SpringFilterHelper.or(
            appFilter,
            entornAppFilter,
            dashboardFilter
        );

        return SpringFilterHelper.and(
            currentSpringFilter,
            (filter.isBlank())
                ? "id:0"
                : filter
        );
    }

    public Set<Serializable> getAllowedIds(ResourceType resourceType, List<PermissionEnum> permissions) {
        return Optional.ofNullable(aclServiceClient.findIdsWithAnyPermission(
                resourceType,
                permissions,
                authenticationHelper.getCurrentUserName(),
                Arrays.asList(authenticationHelper.getCurrentUserRealmRoles()),
                httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
            .orElse(Collections.emptySet());
    }

	@Override
	protected void completeResource(DashboardTitol resource) {
		dashboardItemTitolHelper.completeResourceTitolLogic(resource);
	}

}
