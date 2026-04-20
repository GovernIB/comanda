package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorClientHelper {

    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final EntornAppServiceClient entornAppServiceClient;

    @Cacheable(value = ENTORN_APP_CACHE, key = "#entornAppId?.toString()")
    public EntornApp entornAppFindById(Long entornAppId) {
        try {
            EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
                    entornAppId,
                    null,
                    httpAuthorizationHeaderHelper.getAuthorizationHeader());
            if (entornApp != null) {
                return entornApp.getContent();
            }
        } catch (FeignException.NotFound ignored) {}
        return null;
    }

    public List<Long> findEntornAppIdsByAppId(long appId) {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "app.id:" + appId,
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return Optional.ofNullable(entornApps.getContent())
                .orElse(List.of())
                .stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .map(EntornApp::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Long> findEntornAppIdsByEntornId(long entornId) {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "entorn.id:" + entornId,
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return Optional.ofNullable(entornApps.getContent())
                .orElse(List.of())
                .stream()
                .map(EntityModel::getContent)
                .filter(Objects::nonNull)
                .map(EntornApp::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
