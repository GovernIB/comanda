package es.caib.comanda.alarmes.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_APP_CACHE;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmaClientHelper {
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final EntornAppServiceClient entornAppServiceClient;

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
}
