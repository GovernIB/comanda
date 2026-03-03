package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.AppServiceClient;
import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.EntornServiceClient;
import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaClientHelper {
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final EntornAppServiceClient entornAppServiceClient;
    private final EntornServiceClient entornServiceClient;
    private final AppServiceClient appServiceClient;

    // Client App
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = APP_CACHE, key = "#appId?.toString()")
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

    @Cacheable(value = ENTORN_APP_CACHE, key = "#entornAppId?.toString()")
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

    // Client Entorn
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Cacheable(value = ENTORN_CACHE, key = "#entornId?.toString()")
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
}
