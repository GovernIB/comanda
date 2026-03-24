package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.intf.service.EntornService;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_CACHE;

/**
 * Implementació del servei de gestió d'entorns.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EntornServiceImpl extends BaseMutableResourceService<Entorn, Long, EntornEntity> implements EntornService {

    private final EntornRepository entornRepository;
    private final EntornAppRepository entornAppRepository;
    private final CacheHelper cacheHelper;
    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;

    @Override
    protected String additionalSpringFilter(String currentSpringFilter, String[] namedQueries) {
        if (authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
                || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)) {
            return null;
        }

        Set<Serializable> appPermissionIds = getAllowedIds(ResourceType.APP);
        Set<Serializable> entornAppPermissionIds = getAllowedIds(ResourceType.ENTORN_APP);
        List<EntornAppEntity> activeEntornApps = entornAppRepository.findByActivaTrueAndAppActivaTrue();
        Set<Long> allowedEntornIds = activeEntornApps.stream()
                .filter(entornApp -> appPermissionIds.contains(entornApp.getApp().getId())
                        || entornAppPermissionIds.contains(entornApp.getId()))
                .map(entornApp -> entornApp.getEntorn().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (allowedEntornIds.isEmpty()) {
            return "id:0";
        }

        return allowedEntornIds.stream()
                .map(id -> "id:" + id)
                .collect(Collectors.joining(" or "));
    }

    private Set<Serializable> getAllowedIds(ResourceType resourceType) {
        return Optional.ofNullable(aclServiceClient.findIdsWithAnyPermission(
                resourceType,
                Collections.singletonList(PermissionEnum.READ),
                authenticationHelper.getCurrentUserName(),
                Arrays.asList(authenticationHelper.getCurrentUserRoles()),
                httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
                .orElse(Collections.emptySet());
    }

    @Override
    protected List<EntornEntity> reorderFindLinesWithParent(Serializable parentId) {
        return entornRepository.findAllByOrderByOrdreAsc();
    }

    @Override
    protected void afterUpdateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        cacheHelper.evictCacheItem(ENTORN_CACHE, entity.getId().toString());
    }

}
