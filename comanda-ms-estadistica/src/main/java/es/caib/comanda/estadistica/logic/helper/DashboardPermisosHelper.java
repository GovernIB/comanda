package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static es.caib.comanda.estadistica.logic.intf.model.widget.WidgetBaseResource.FILTER_BY_ENTORN_NAMEDFILTER;

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
    private final DashboardItemRepository dashboardItemRepository;
    private final EstadisticaWidgetRepository estadisticaWidgetRepository;

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
     * Construeix una clàusula de filtre Spring Filter a partir dels IDs d'EntornApp permesos.
     */
    public String buildEntornAppFilter(Set<Serializable> entornAppPermissionIds, String prefix) {
        String fieldPrefix = (prefix != null && !prefix.isBlank()) ? prefix + "." : "";
        return buildEntornAppFilter(entornAppPermissionIds, fieldPrefix + "appId", fieldPrefix + "entornId");
    }

    /**
     * Construeix una clàusula de filtre Spring Filter a partir dels IDs d'EntornApp permesos,
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
     * Construeix el filtre Spring Filter d'autorització per a un recurs combinant permisos d'APP, ENTORN_APP i DASHBOARD.
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
     * Construeix el filtre Spring Filter d'autorització per a un recurs amb propietats d'aplicació i entorn específiques (com a DashboardItem).
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
     * Construeix el filtre Spring Filter per a dashboards o entitats filles amb prefix comú (p. ex. dashboard.appId, dashboard.entornId, dashboard.id).
     */
    public String buildDashboardChildFilter(String currentSpringFilter, String dashboardPrefix) {
        String p = (dashboardPrefix != null && !dashboardPrefix.isBlank()) ? dashboardPrefix + "." : "";
        return buildAclFilterPrefix(currentSpringFilter, p + "appId", dashboardPrefix, p + "id", false);
    }

    /**
     * Construeix el filtre Spring Filter per al quadre de control (Dashboard).
     */
    public String buildDashboardFilter(String currentSpringFilter, boolean isWrite) {
        return buildAclFilterPrefix(currentSpringFilter, "appId", null, "id", isWrite);
    }

    /**
     * Construeix el filtre Spring Filter per als elements d'un quadre de control (DashboardItem).
     */
    public String buildDashboardItemFilter(String currentSpringFilter) {
        return buildAclFilter(currentSpringFilter, "widget.appId", "entornId", "dashboard.id", false);
    }

    /**
     * Comprova si l'usuari actual pot accedir o visualitzar el widget estadístic indicat.
     * Es pot visualitzar un widget si:
     * - L'usuari és ADMIN o CONSULTA.
     * - O pertany a alguna app amb permís directe (ResourceType.APP).
     * - O pertany a algun dashboard amb permís directe (ResourceType.DASHBOARD).
     * - O pertany a una app accessible a través d'EntornApp:
     *     - Si s'especifica entornId (p. ex. a l'editor de dashboard o en validar per a un entorn específic),
     *       només si l'entorn de l'EntornApp també coincideix.
     *     - Si entornId és null ("de normal"), qualsevol EntornApp de la mateixa app és suficient.
     *
     * @param widget L'entitat del widget estadístic
     * @param entornId L'identificador de l'entorn de context (opcional)
     * @return true si l'accés és permès
     */
    public boolean canAccessWidget(EstadisticaWidgetEntity<?> widget, Long entornId) {
        if (isAdminOrConsulta()) {
            return true;
        }
        if (widget == null) {
            return false;
        }

        Long appId = widget.getAppId();
        Long widgetId = widget.getId();

        // Permís directe sobre l'aplicació
        if (appId != null && containsId(getAllowedAppIds(false), appId)) {
            return true;
        }

        // Permís directe sobre algun dashboard que contingui aquest widget
        Set<Serializable> allowedDashboardIds = getAllowedDashboardIds(false);
        if (widgetId != null && !allowedDashboardIds.isEmpty() && dashboardItemRepository != null) {
            List<Long> dashIds = allowedDashboardIds.stream()
                    .map(id -> (id instanceof Number) ? ((Number) id).longValue() : Long.parseLong(id.toString()))
                    .collect(Collectors.toList());
            if (!dashIds.isEmpty() && dashboardItemRepository.existsByWidgetIdAndDashboardIdIn(widgetId, dashIds)) {
                return true;
            }
        }

        // Permís a través d'EntornApp
        if (appId != null) {
            Set<Serializable> allowedEntornAppIds = getAllowedEntornAppIds(false);
            if (!allowedEntornAppIds.isEmpty()) {
                if (entornId != null) {
                    try {
                        EntornApp ea = estadisticaClientHelper.entornAppFindByAppAndEntorn(appId, entornId);
                        if (ea != null && ea.getId() != null && containsId(allowedEntornAppIds, ea.getId())) {
                            return true;
                        }
                    } catch (Exception e) {
                        log.warn("Error consultant EntornApp per appId=" + appId + " i entornId=" + entornId, e);
                    }
                } else {
                    for (Serializable eaId : allowedEntornAppIds) {
                        try {
                            Long id = (eaId instanceof Number) ? ((Number) eaId).longValue() : Long.parseLong(eaId.toString());
                            EntornApp ea = estadisticaClientHelper.entornAppFindById(id);
                            if (ea != null && ea.getApp() != null && appId.equals(ea.getApp().getId())) {
                                return true;
                            }
                        } catch (Exception e) {
                            log.warn("Error resolvent EntornApp per id=" + eaId, e);
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean canAccessWidget(Long widgetId, Long entornId) {
        if (isAdminOrConsulta()) {
            return true;
        }
        if (widgetId == null) {
            return false;
        }
        if (estadisticaWidgetRepository != null) {
            return estadisticaWidgetRepository.findById(widgetId)
                    .map(w -> canAccessWidget(w, entornId))
                    .orElse(false);
        }
        return false;
    }

    public void checkCanAccessWidget(EstadisticaWidgetEntity<?> widget, Long entornId, String errorMessage) {
        if (!canAccessWidget(widget, entornId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    public void checkCanAccessWidget(Long widgetId, Long entornId, String errorMessage) {
        if (!canAccessWidget(widgetId, entornId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

    /**
     * Construeix el filtre Spring Filter d'autorització per als widgets estadístics (Simple, Gràfic, Taula).
     * <p>
     * Si l'usuari és ADMIN o CONSULTA, no s'aplica cap restricció.
     * En cas contrari, un widget és visible si:
     * - Pertany a una aplicació sobre la qual l'usuari té permís directe (ResourceType.APP).
     * - O pertany a algun dashboard sobre el qual l'usuari té permís directe (ResourceType.DASHBOARD).
     * - O pertany a una aplicació accessible a través dels seus permisos sobre EntornApp:
     *     - Si a namedQueries s'indica un entorn ("filterByEntorn:<id>"), només es mostren els widgets
     *       de l'aplicació si l'entornApp té EXACTAMENT aquest mateix entorn (editor de dashboards).
     *     - Si no s'indica cap entorn ("de normal"), es mostren tots els widgets de les aplicacions de les
     *       quals l'usuari té permís sobre algun EntornApp.
     */
    public String buildWidgetFilter(String currentSpringFilter, String[] namedQueries) {
        if (isAdminOrConsulta()) {
            return currentSpringFilter;
        }

        Long entornId = extractEntornIdFromNamedQueries(namedQueries);

        Set<Serializable> allowedApps = new HashSet<>(getAllowedAppIds(false));

        Set<Serializable> allowedEntornAppIds = getAllowedEntornAppIds(false);
        for (Serializable eaId : allowedEntornAppIds) {
            try {
                Long id = (eaId instanceof Number) ? ((Number) eaId).longValue() : Long.parseLong(eaId.toString());
                EntornApp ea = estadisticaClientHelper.entornAppFindById(id);
                if (ea != null && ea.getApp() != null && ea.getApp().getId() != null) {
                    if (entornId != null) {
                        if (ea.getEntorn() != null && entornId.equals(ea.getEntorn().getId())) {
                            allowedApps.add(ea.getApp().getId());
                        }
                    } else {
                        allowedApps.add(ea.getApp().getId());
                    }
                }
            } catch (Exception e) {
                log.warn("Error resolvent EntornApp per id=" + eaId, e);
            }
        }

        String appFilter = SpringFilterHelper.buildOrFilter("appId", allowedApps);

        Set<Serializable> allowedDashboardIds = getAllowedDashboardIds(false);
        String dashboardWidgetFilter = null;
        if (!allowedDashboardIds.isEmpty() && dashboardItemRepository != null) {
            List<Long> dashIds = allowedDashboardIds.stream()
                    .map(id -> (id instanceof Number) ? ((Number) id).longValue() : Long.parseLong(id.toString()))
                    .collect(Collectors.toList());
            if (!dashIds.isEmpty()) {
                List<Long> widgetIds = dashboardItemRepository.findWidgetIdsByDashboardIdIn(dashIds);
                dashboardWidgetFilter = SpringFilterHelper.buildOrFilter("id", widgetIds);
            }
        }

        String filter = SpringFilterHelper.or(appFilter, dashboardWidgetFilter);

        return SpringFilterHelper.and(
                currentSpringFilter,
                (filter.isBlank())
                        ? "id:0"
                        : filter
        );
    }

    public Long extractEntornIdFromNamedQueries(String[] namedQueries) {
        if (namedQueries == null) return null;
        for (String q : namedQueries) {
            if (q == null) continue;
            if (q.startsWith(FILTER_BY_ENTORN_NAMEDFILTER)) {
                try {
                    String[] parts = q.split(":");
                    if (parts.length > 1) {
                        return Long.parseLong(parts[1].trim());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    private boolean containsId(Set<Serializable> ids, Long targetId) {
        if (ids == null || targetId == null) return false;
        return ids.stream().anyMatch(id -> {
            if (id instanceof Number) {
                return ((Number) id).longValue() == targetId;
            }
            return Objects.equals(id.toString(), targetId.toString());
        });
    }
}
