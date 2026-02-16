package es.caib.comanda.usuaris.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_APP_CACHE;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarisClientHelper {

    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final MonitorServiceClient monitorServiceClient;
    private final EntornAppServiceClient entornAppServiceClient;

    // Client EntornApp
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = ENTORN_APP_CACHE, key = "#entornAppId.toString()")
    public EntornApp entornAppFindById(Long entornAppId) {
        EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
                entornAppId,
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (entornApp != null) {
            return entornApp.getContent();
        }
        return null;
    }

    public Optional<EntornApp> entornAppFindByEntornAndApp(Long entornId, Long appId) {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "entorn.id:" + entornId + " and app.id:" + appId,
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        return entornApps.getContent().stream().
                map(EntityModel::getContent).
                findFirst();
    }


    // Client Monitor
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void monitorCreate(Monitor monitor) {
        try {
            monitorServiceClient.create(monitor, httpAuthorizationHeaderHelper.getAuthorizationHeader());
        } catch (Exception e) {
            log.error("Error al guardar el monitor: " + monitor, e);
        }
    }

}
