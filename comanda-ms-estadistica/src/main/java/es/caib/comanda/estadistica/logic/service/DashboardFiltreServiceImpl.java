package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltre;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltreTipus;
import es.caib.comanda.estadistica.logic.intf.service.DashboardFiltreService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardFiltreRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
 * Implementació del servei per gestionar la lògica de negoci relacionada amb els filtres de capçalera de dashboards.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardFiltreServiceImpl extends BaseMutableResourceService<DashboardFiltre, Long, DashboardFiltreEntity> implements DashboardFiltreService {

    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;
    private final DashboardFiltreRepository dashboardFiltreRepository;

    @Override
    protected void beforeCreateEntity(
        DashboardFiltreEntity entity,
        DashboardFiltre resource,
        Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotCreatedException {

        Long dashboardId = resource.getDashboard() != null ? resource.getDashboard().getId() : entity.getDashboard().getId();
        String errorMessage = findDuplicateErrorMessage(
            dashboardId,
            resource.getTipus(),
            resource.getDimensioCodi(),
            null);
        if (errorMessage != null) {
            throw new ResourceNotCreatedException(getResourceClass(), errorMessage);
        }
    }

    @Override
    protected void beforeUpdateEntity(
        DashboardFiltreEntity entity,
        DashboardFiltre resource,
        Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {

        Long dashboardId = resource.getDashboard() != null ? resource.getDashboard().getId() : entity.getDashboard().getId();
        String errorMessage = findDuplicateErrorMessage(dashboardId, resource.getTipus(), resource.getDimensioCodi(), entity.getId());
        if (errorMessage != null) {
            throw new ResourceNotUpdatedException(getResourceClass(), String.valueOf(entity.getId()), errorMessage);
        }
    }

    /**
     * Comprova les regles d'unicitat dels filtres de capçalera d'un dashboard: només se n'admet un de tipus
     * PERIODE, i no es pot repetir la mateixa dimensió (dimensioCodi) en dos filtres de tipus DIMENSIO.
     *
     * @return el missatge d'error localitzat si es vulnera alguna de les regles, o {@code null} si tot és correcte.
     */
    private String findDuplicateErrorMessage(Long dashboardId,
                                             DashboardFiltreTipus tipus,
                                             String dimensioCodi,
                                             Long excludeId) {
        if (dashboardId == null || tipus == null) {
            return null;
        }
        boolean duplicate = dashboardFiltreRepository.findByDashboardIdOrderByOrdre(dashboardId).stream()
            .filter(existing -> !Objects.equals(existing.getId(), excludeId))
            .anyMatch(existing -> existing.getTipus() == tipus
                && (tipus != DashboardFiltreTipus.DIMENSIO || Objects.equals(existing.getDimensioCodi(), dimensioCodi)));
        if (!duplicate) {
            return null;
        }
        return I18nUtil.getInstance().getI18nMessage(
            tipus == DashboardFiltreTipus.PERIODE
                ? "es.caib.comanda.estadistica.logic.service.DashboardFiltreServiceImpl.periodeDuplicat"
                : "es.caib.comanda.estadistica.logic.service.DashboardFiltreServiceImpl.dimensioDuplicada");
    }

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

}
