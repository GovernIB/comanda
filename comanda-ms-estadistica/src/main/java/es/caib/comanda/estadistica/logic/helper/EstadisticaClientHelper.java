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
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.*;

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

	@Cacheable(value = APP_CACHE, key = "#appId?.toString()")
	public App appFindById(Long appId) {
		try {
			EntityModel<App> app = appServiceClient.getOne(
					appId,
					null,
					httpAuthorizationHeaderHelper.getAuthorizationHeader());
			if (app != null) {
				return app.getContent();
			}
		} catch (FeignException.NotFound e) {
			return null;
		}
		return null;
	}

	@Cacheable(value = APP_CACHE, key = "#appCodi?.toString()")
	public App appFindByCodi(String appCodi) {
		try {
			List<EntityModel<App>> apps = new ArrayList(appServiceClient.find(
                    null,
                    "codi:'" + appCodi + "'",
                    null,
                    null,
                    "0",
                    1,
                    httpAuthorizationHeaderHelper.getAuthorizationHeader()).getContent());
            if (!apps.isEmpty()) {
                return apps.get(0).getContent();
            }
		} catch (FeignException.NotFound e) {
			return null;
		}
		return null;
	}


    // Client EntornApp
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
		} catch (FeignException.NotFound e) {
			return null;
		}
		return null;
	}

	@Cacheable(value = ENTORN_APP_BY_APP_AND_ENTORN_CACHE, key = "#appId + '-' + #entornId")
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

    /** Recupera tots els ID d'un EntornApp donada la id d'una App **/
    public List<Long> getEntornAppsIdByAppId(Long appId) {
        PagedModel<EntityModel<EntornApp>> entornApps = entornAppServiceClient.find(
                null,
                appId != null ? "app.id:" + appId : "",
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
                filter(Objects::nonNull).
                map(EntornApp::getId).
                collect(Collectors.toList());
    }

    // Client Entorn
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Cacheable(value = ENTORN_CACHE, key = "#entornId?.toString()")
	public Entorn entornById(Long entornId) {
		try {
			EntityModel<Entorn> entorn = entornServiceClient.getOne(
					entornId,
					null,
					httpAuthorizationHeaderHelper.getAuthorizationHeader());
			if (entorn != null) {
				return entorn.getContent();
			}
		} catch (FeignException.NotFound e) {
			return null;
		}
		return null;
	}

	@Cacheable(value = ENTORN_CACHE, key = "#entornCodi?.toString()")
	public Entorn entornByCodi(String entornCodi) {
		try {
            List<EntityModel<Entorn>> entorns = new ArrayList<>(entornServiceClient.find(
                    null,
                    "codi:'" + entornCodi + "'",
                    null,
                    null,
                    "0",
                    1,
                    httpAuthorizationHeaderHelper.getAuthorizationHeader()).getContent());
			if (!entorns.isEmpty()) {
				return entorns.get(0).getContent();
			}
		} catch (FeignException.NotFound e) {
			return null;
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
