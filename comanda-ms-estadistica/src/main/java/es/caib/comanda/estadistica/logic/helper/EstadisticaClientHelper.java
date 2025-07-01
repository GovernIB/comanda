package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstadisticaClientHelper {

    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final MonitorServiceClient monitorServiceClient;
    private final EntornAppServiceClient entornAppServiceClient;
    private final EntornServiceClient entornServiceClient;
    private final AppServiceClient appServiceClient;

    // Client App
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = "appCache", key = "#appId")
    public App appFindById(Long appId) {
        EntityModel<App> app = appServiceClient.getOne(
                appId,
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (app != null) {
            return app.getContent();
        }
        return null;
    }


    // Client EntornApp
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = "entornAppCache", key = "#entornAppId")
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

    public EntornApp entornAppFindByAppAndEntorn(Long appId, Long entornId) {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "app.id:" + appId + " and entorn.id:" + entornId,
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (entornApps == null) {
            return null;
        }
        return entornApps.getContent().stream().
                findFirst().orElseThrow(() -> new ResourceNotFoundException(EntornApp.class, "app:" + appId + ", entorn:" + entornId)).getContent();
    }

    public List<EntornApp> entornAppFindByActivaTrue() {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "activa:true and app.activa:true",
                null,
                null,
                "UNPAGED",
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (entornApps == null) {
            return List.of();
        }
        return entornApps.getContent().stream().
                map(EntityModel::getContent).
                collect(Collectors.toList());
    }


    // Client Entorn
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = "entornCache", key = "#entornId")
    public Entorn entornById(Long entornId) {
        EntityModel<Entorn> entorn = entornServiceClient.getOne(
                entornId,
                null,
                httpAuthorizationHeaderHelper.getAuthorizationHeader());
        if (entorn != null) {
            return entorn.getContent();
        }
        return null;
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
