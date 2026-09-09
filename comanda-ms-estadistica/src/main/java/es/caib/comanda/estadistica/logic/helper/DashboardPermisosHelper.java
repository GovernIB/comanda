package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

/**
 * Helper centralitzat per a la comprovació de permisos ACL i drets de disseny
 * relacionats amb quadres de comandament (Dashboards) i els seus components (items, títols, etc.).
 *
 * @author Límit Tecnologies
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardPermisosHelper {

    public static final List<PermissionEnum> PERMISSIONS_APP_READ = List.of(PermissionEnum.PERM0, PermissionEnum.PERM1);
    public static final List<PermissionEnum> PERMISSIONS_APP_WRITE = List.of(PermissionEnum.PERM1);
    public static final List<PermissionEnum> PERMISSIONS_DASHBOARD_READ = List.of(PermissionEnum.READ, PermissionEnum.WRITE);
    public static final List<PermissionEnum> PERMISSIONS_DASHBOARD_WRITE = List.of(PermissionEnum.WRITE);

    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;
    private final DashboardRepository dashboardRepository;
    private final EstadisticaClientHelper estadisticaClientHelper;

    /**
     * Comprova si l'usuari actual té el rol ADMIN o CONSULTA (exempt de restriccions de lectura).
     */
    public boolean isAdminOrConsulta() {
        return authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
            || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA);
    }

    /**
     * Comprova si l'usuari actual té el rol ADMIN (exempt de restriccions de disseny/escriptura).
     */
    public boolean isAdmin() {
        return authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN);
    }

    /**
     * Comprova si l'usuari actual té algun dels permisos indicats sobre un recurs concret.
     */
    public boolean hasPermission(ResourceType resourceType, Serializable resourceId, List<PermissionEnum> permissions) {
        if (resourceId == null) return false;
        try {
            ResponseEntity<Boolean> response = aclServiceClient.anyPermissionGranted(
                    resourceType,
                    resourceId,
                    permissions,
                    authenticationHelper.getCurrentUserName(),
                    getCurrentUserRoles(),
                    httpAuthorizationHeaderHelper.getAuthorizationHeader());
            return response != null && Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            log.error("Error comprovant permisos per " + resourceType + " amb id=" + resourceId, e);
            return false;
        }
    }

    /**
     * Recupera els identificadors dels recursos sobre els quals l'usuari actual té algun dels permisos indicats.
     */
    public Set<Serializable> getAllowedIds(ResourceType resourceType, List<PermissionEnum> permissions) {
        try {
            ResponseEntity<Set<Serializable>> response = aclServiceClient.findIdsWithAnyPermission(
                    resourceType,
                    permissions,
                    authenticationHelper.getCurrentUserName(),
                    getCurrentUserRoles(),
                    httpAuthorizationHeaderHelper.getAuthorizationHeader());
            return (response != null && response.getBody() != null) ? response.getBody() : Collections.emptySet();
        } catch (Exception e) {
            log.error("Error obtenint IDs permesos per " + resourceType, e);
            return Collections.emptySet();
        }
    }

    private List<String> getCurrentUserRoles() {
        if (authenticationHelper == null || authenticationHelper.getCurrentUserRealmRoles() == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(authenticationHelper.getCurrentUserRealmRoles());
    }

    /**
     * Compta el nombre de subjectes (SIDs) amb permisos sobre el recurs indicat.
     */
    public Integer countSidsWithPermission(ResourceType resourceType, Serializable resourceId) {
        if (resourceId == null) return 0;
        try {
            ResponseEntity<Integer> response = aclServiceClient.countSidsWithPermission(
                    resourceType,
                    resourceId,
                    httpAuthorizationHeaderHelper.getAuthorizationHeader());
            return (response != null && response.getBody() != null) ? response.getBody() : 0;
        } catch (Exception e) {
            log.error("Error comptant SIDs per a " + resourceType + " amb id=" + resourceId, e);
            return 0;
        }
    }

    public Set<Serializable> getAllowedAppIds(boolean isWrite) {
        return getAllowedIds(ResourceType.APP, isWrite ? PERMISSIONS_APP_WRITE : PERMISSIONS_APP_READ);
    }

    public Set<Serializable> getAllowedEntornAppIds(boolean isWrite) {
        return getAllowedIds(ResourceType.ENTORN_APP, isWrite ? PERMISSIONS_APP_WRITE : PERMISSIONS_APP_READ);
    }

    public Set<Serializable> getAllowedDashboardIds(boolean isWrite) {
        return getAllowedIds(ResourceType.DASHBOARD, isWrite ? PERMISSIONS_DASHBOARD_WRITE : PERMISSIONS_DASHBOARD_READ);
    }

    /**
     * Comprova si l'usuari actual pot dissenyar un dashboard coneguts el seu id, appId i entornId.
     * Jerarquia de permisos:
     * 1. Rol ADMIN
     * 2. Permís WRITE sobre el DASHBOARD
     * 3. Permís PERM1 sobre l'APP
     * 4. Permís PERM1 sobre l'ENTORN_APP associat
     */
    public boolean canDesign(Long dashboardId, Long appId, Long entornId) {
        if (isAdmin()) {
            return true;
        }
        if (dashboardId != null && hasPermission(ResourceType.DASHBOARD, dashboardId, PERMISSIONS_DASHBOARD_WRITE)) {
            return true;
        }
        if (appId != null && hasPermission(ResourceType.APP, appId, PERMISSIONS_APP_WRITE)) {
            return true;
        }
        if (appId != null && entornId != null) {
            EntornApp entornApp = estadisticaClientHelper.entornAppFindByAppAndEntorn(appId, entornId);
            if (entornApp != null && hasPermission(ResourceType.ENTORN_APP, entornApp.getId(), PERMISSIONS_APP_WRITE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Comprova si l'usuari actual pot crear un dashboard per a una combinació app/entorn.
     */
    public boolean canCreate(Long appId, Long entornId) {
        return canDesign(null, appId, entornId);
    }

    /**
     * Comprova si l'usuari actual pot dissenyar el dashboard especificat pel seu identificador.
     */
    public boolean canDesignDashboard(Long dashboardId) {
        if (isAdmin()) {
            return true;
        }
        if (dashboardId == null) {
            return false;
        }
        DashboardEntity dashboard = dashboardRepository.findById(dashboardId).orElse(null);
        if (dashboard == null) {
            return false;
        }
        return canDesign(dashboard.getId(), dashboard.getAppId(), dashboard.getEntornId());
    }

    /**
     * Verifica que l'usuari pot dissenyar el dashboard especificat pel seu ID, o llança {@link AccessDeniedException}.
     */
    public void checkCanDesignDashboard(Long dashboardId, String errorMessage) {
        if (!canDesignDashboard(dashboardId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    /**
     * Verifica que l'usuari pot dissenyar el dashboard (amb id, appId i entornId), o llança {@link AccessDeniedException}.
     */
    public void checkCanDesign(Long dashboardId, Long appId, Long entornId, String errorMessage) {
        if (!canDesign(dashboardId, appId, entornId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    /**
     * Verifica que l'usuari pot crear un dashboard per a l'app/entorn indicats, o llança {@link AccessDeniedException}.
     */
    public void checkCanCreate(Long appId, Long entornId, String errorMessage) {
        if (!canCreate(appId, entornId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    /**
     * Construeix una clàusula de filtre Spring RSQL a partir dels IDs d'EntornApp permesos.
     */
    public String buildEntornAppFilter(Set<Serializable> entornAppPermissionIds, String prefix) {
        String fieldPrefix = (prefix != null && !prefix.isBlank()) ? prefix + "." : "";
        return buildEntornAppFilter(entornAppPermissionIds, fieldPrefix + "appId", fieldPrefix + "entornId");
    }

    /**
     * Construeix una clàusula de filtre Spring RSQL a partir dels IDs d'EntornApp permesos,
     * permetent especificar els noms de propietat concrets per a l'aplicació i per a l'entorn.
     */
    public String buildEntornAppFilter(Set<Serializable> entornAppPermissionIds, String appProperty, String entornProperty) {
        if (entornAppPermissionIds == null || entornAppPermissionIds.isEmpty()) {
            return null;
        }
        List<String> clauses = new ArrayList<>();
        for (Serializable id : entornAppPermissionIds) {
            try {
                Long entornAppId = Long.parseLong(String.valueOf(id));
                EntornApp ea = estadisticaClientHelper.entornAppFindById(entornAppId);
                if (ea != null && ea.getApp() != null && ea.getEntorn() != null) {
                    clauses.add("(" + appProperty + ":" + ea.getApp().getId() + " and " + entornProperty + ":" + ea.getEntorn().getId() + ")");
                }
            } catch (Exception e) {
                log.error("Error resolvent EntornApp per a filtre de dashboard: " + id, e);
            }
        }
        return clauses.isEmpty() ? null : String.join(" or ", clauses);
    }

    /**
     * Construeix el filtre Spring RSQL d'autorització per a un recurs combinant permisos d'APP, ENTORN_APP i DASHBOARD.
     */
    public String buildAclFilterPrefix(
            String currentSpringFilter,
            String appIdProperty,
            String entornAppFilterPrefix,
            String dashboardIdProperty,
            boolean isWrite) {
        if (isAdminOrConsulta()) {
            return currentSpringFilter;
        }

        String appFilter = SpringFilterHelper.buildOrFilter(
                appIdProperty,
                getAllowedAppIds(isWrite));

        String entornAppFilter = buildEntornAppFilter(
                getAllowedEntornAppIds(isWrite),
                entornAppFilterPrefix);

        String dashboardFilter = SpringFilterHelper.buildOrFilter(
                dashboardIdProperty,
                getAllowedDashboardIds(isWrite));

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

    /**
     * Construeix el filtre Spring RSQL d'autorització per a un recurs amb propietats d'aplicació i entorn específiques (com a DashboardItem).
     */
    public String buildAclFilter(
            String currentSpringFilter,
            String appIdProperty,
            String entornIdProperty,
            String dashboardIdProperty,
            boolean isWrite) {
        if (isAdminOrConsulta()) {
            return currentSpringFilter;
        }

        String appFilter = SpringFilterHelper.buildOrFilter(
                appIdProperty,
                getAllowedAppIds(isWrite));

        String entornAppFilter = buildEntornAppFilter(
                getAllowedEntornAppIds(isWrite),
                appIdProperty,
                entornIdProperty);

        String dashboardFilter = SpringFilterHelper.buildOrFilter(
                dashboardIdProperty,
                getAllowedDashboardIds(isWrite));

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

    /**
     * Construeix el filtre RSQL per a dashboards o entitats filles amb prefix comú (p. ex. dashboard.appId, dashboard.entornId, dashboard.id).
     */
    public String buildDashboardChildFilter(String currentSpringFilter, String dashboardPrefix) {
        String p = (dashboardPrefix != null && !dashboardPrefix.isBlank()) ? dashboardPrefix + "." : "";
        return buildAclFilterPrefix(currentSpringFilter, p + "appId", dashboardPrefix, p + "id", false);
    }

    /**
     * Construeix el filtre RSQL per al quadre de control (Dashboard).
     */
    public String buildDashboardFilter(String currentSpringFilter, boolean isWrite) {
        return buildAclFilterPrefix(currentSpringFilter, "appId", null, "id", isWrite);
    }

    /**
     * Construeix el filtre RSQL per als elements d'un quadre de control (DashboardItem).
     */
    public String buildDashboardItemFilter(String currentSpringFilter) {
        return buildAclFilter(currentSpringFilter, "widget.appId", "entornId", "dashboard.id", false);
    }
}
