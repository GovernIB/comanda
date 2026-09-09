package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.estadistica.logic.helper.DashboardItemTitolHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardPermisosHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitol;
import es.caib.comanda.estadistica.logic.intf.service.DashboardTitolService;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
    private final DashboardClonerMapper dashboardClonerMapper;
    private final DashboardTitolRepository dashboardTitolRepository;
    private final DashboardPermisosHelper dashboardPermisosHelper;

    @PostConstruct
    public void init() {
        register(DashboardTitol.DUPLICATE_ACTION, new DuplicateDashboardTitolAction());
    }

    @Override
    protected String additionalSpringFilter(
        String currentSpringFilter,
        String[] namedQueries) {
        return dashboardPermisosHelper.buildDashboardChildFilter(currentSpringFilter, "dashboard");
    }

    public Set<Serializable> getAllowedIds(ResourceType resourceType, List<PermissionEnum> permissions) {
        return dashboardPermisosHelper.getAllowedIds(resourceType, permissions);
    }

    @Override
    protected void beforeCreateEntity(DashboardTitolEntity entity, DashboardTitol resource, Map<String, AnswerRequiredException.AnswerValue> answers) {
        Long dashboardId = resource.getDashboard() != null ? resource.getDashboard().getId() : null;
        dashboardPermisosHelper.checkCanDesignDashboard(dashboardId, "No teniu permisos de disseny per afegir títols a aquest quadre de control");
    }

    @Override
    protected void beforeUpdateEntity(DashboardTitolEntity entity, DashboardTitol resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
        Long currentDashboardId = entity.getDashboard() != null ? entity.getDashboard().getId() : null;
        dashboardPermisosHelper.checkCanDesignDashboard(currentDashboardId, "No teniu permisos de disseny per modificar títols d'aquest quadre de control");

        if (resource.getDashboard() != null && !Objects.equals(resource.getDashboard().getId(), currentDashboardId)) {
            Long targetDashboardId = resource.getDashboard().getId();
            dashboardPermisosHelper.checkCanDesignDashboard(targetDashboardId, "No teniu permisos de disseny per moure títols a aquest quadre de control");
        }
    }

    @Override
    protected void beforeDelete(DashboardTitolEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        Long dashboardId = entity.getDashboard() != null ? entity.getDashboard().getId() : null;
        dashboardPermisosHelper.checkCanDesignDashboard(dashboardId, "No teniu permisos de disseny per eliminar títols d'aquest quadre de control");
    }

    @Override
    protected void completeResource(DashboardTitol resource) {
        dashboardItemTitolHelper.completeResourceTitolLogic(resource);
    }

    // ========================================================================
    // ACCIÓ PER DUPLICAR UN DASHBOARD TITOL AMB TOTES LES SEVES PROPIETATS
    // ========================================================================
    public class DuplicateDashboardTitolAction implements ActionExecutor<DashboardTitolEntity, Serializable, DashboardTitol> {

        @Override
        public DashboardTitol exec(String code, DashboardTitolEntity entity, Serializable params) throws ActionExecutionException {
            if (entity == null || entity.getDashboard() == null) {
                throw new ActionExecutionException(DashboardTitol.class, entity != null ? entity.getId() : null, code, "Dashboard title or dashboard is null");
            }

            dashboardPermisosHelper.checkCanDesignDashboard(entity.getDashboard().getId(), "No teniu permisos de disseny per duplicar títols d'aquest quadre de control");

            // 1. Clona l'entitat DashboardTitol amb MapStruct (copia colors, vores, subtítols, destacat, personalitzat, plantilla, width, height)
            DashboardTitolEntity newTitol = dashboardClonerMapper.cloneTitol(entity);
            newTitol.setDashboard(entity.getDashboard());

            // 2. Genera títol seqüencial únic per evitar col·lisions
            String candidateTitle = buildDuplicateTitle(entity.getTitol(), entity.getDashboard().getId());
            newTitol.setTitol(candidateTitle);

            // 3. Auto-posiciona al primer espai lliure 2D
            int width = newTitol.getWidth() > 0 ? newTitol.getWidth() : DashboardItemTitolHelper.GRID_COLUMNS;
            int height = newTitol.getHeight() > 0 ? newTitol.getHeight() : 1;
            if (dashboardItemTitolHelper != null) {
                DashboardItemTitolHelper.GridPosition pos = dashboardItemTitolHelper.findFirstAvailableSpace(entity.getDashboard().getId(), width, height);
                newTitol.setPosX(pos.getPosX());
                newTitol.setPosY(pos.getPosY());
            }

            DashboardTitolEntity saved = entityRepository.save(newTitol);
            return entityDetachConvertAndMerge(saved, null, true);
        }

        @Override
        public void onChange(Serializable id, Serializable previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Serializable target) {
        }

        private String buildDuplicateTitle(String originalTitle, Long dashboardId) {
            List<DashboardTitolEntity> existingTitols = dashboardTitolRepository != null ? dashboardTitolRepository.findByDashboardId(dashboardId) : null;
            List<String> existingTitles = existingTitols != null ? existingTitols.stream().map(DashboardTitolEntity::getTitol).filter(Objects::nonNull).collect(Collectors.toList()) : Collections.emptyList();

            java.util.regex.Pattern patternWithSeq = java.util.regex.Pattern.compile("^(.*) \\((\\d+)\\)$");
            java.util.regex.Matcher m = patternWithSeq.matcher(originalTitle != null ? originalTitle : "");
            String baseTitle = m.matches() ? m.group(1) : (originalTitle != null ? originalTitle : "Títol");

            String escapedBase = java.util.regex.Pattern.quote(baseTitle);
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile("^" + escapedBase + " \\((\\d+)\\)$");
            int maxSeq = 1;
            for (String t : existingTitles) {
                java.util.regex.Matcher tm = regex.matcher(t);
                if (tm.matches()) {
                    try {
                        int seq = Integer.parseInt(tm.group(1));
                        if (seq > maxSeq) {
                            maxSeq = seq;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            return baseTitle + " (" + (maxSeq + 1) + ")";
        }

    }

}
