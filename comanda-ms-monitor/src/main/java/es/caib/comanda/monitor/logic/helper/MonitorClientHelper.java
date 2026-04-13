package es.caib.comanda.monitor.logic.helper;

import es.caib.comanda.client.EntornAppServiceClient;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;



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

}
