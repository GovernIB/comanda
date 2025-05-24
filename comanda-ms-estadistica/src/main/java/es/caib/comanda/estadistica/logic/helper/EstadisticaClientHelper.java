package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.MonitorServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.client.model.monitor.Monitor;
import es.caib.comanda.ms.logic.helper.KeycloakHelper;
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

    private final KeycloakHelper keycloakHelper;
    private final MonitorServiceClient monitorServiceClient;
    private final EntornAppServiceClient entornAppServiceClient;

    // Client EntornApp
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = "entornAppCache", key = "#entornAppId")
    public EntornApp entornAppFindById(Long entornAppId) {
        EntityModel<EntornApp> entornApp = entornAppServiceClient.getOne(
                entornAppId,
                null,
                keycloakHelper.getAuthorizationHeader());
        if (entornApp != null) {
            return entornApp.getContent();
        }
        return null;
    }

    public List<EntornApp> entornAppFindByActivaTrue() {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                "activa:true and app.activa:true",
                null,
                null,
                "UNPAGED",
                null,
                keycloakHelper.getAuthorizationHeader());
        return entornApps.getContent().stream().
                map(EntityModel::getContent).
                collect(Collectors.toList());
    }


    // Client Monitor
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void monitorCreate(Monitor monitor) {
        try {
            monitorServiceClient.create(monitor, keycloakHelper.getAuthorizationHeader());
        } catch (Exception e) {
            log.error("Error al guardar el monitor: " + monitor, e);
        }
    }

}
