package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.*;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.EntornAppParamAction;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.PingUrlResponse;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.EntornAppFilter;
import es.caib.comanda.configuracio.logic.intf.service.EntornAppService;
import es.caib.comanda.configuracio.persist.entity.AppContextEntity;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.repository.AppIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ContextRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ArtifactNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.ms.logic.intf.model.ResourceArtifactType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.back.config.HazelCastCacheConfig.ENTORN_APP_CACHE;

/**
 * Implementació del servei de gestió d'aplicacions per entorn.
 *
 * @author Límit Tecnologies
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EntornAppServiceImpl extends BaseMutableResourceService<EntornApp, Long, EntornAppEntity> implements EntornAppService {

    private final AppIntegracioRepository appIntegracioRepository;
    private final SubsistemaRepository subsistemaRepository;
    private final ContextRepository contextRepository;
    private final EntornAppRepository entornAppRepository;
    private final AppInfoHelper appInfoHelper;
    private final CacheHelper cacheHelper;
    private final ConfiguracioSchedulerService schedulerService;
    private final RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        register(EntornApp.ENTORN_APP_ACTION_REFRESH, new EntornAppServiceImpl.RefreshAction(entornAppRepository, appInfoHelper));
        register(EntornApp.ENTORN_APP_ACTION_REPROGRAMAR, new EntornAppServiceImpl.ReprogramarAction(entornAppRepository, schedulerService));
        register(EntornApp.ENTORN_APP_ACTION_PING_URL, new EntornAppServiceImpl.PingUrlAction(restTemplate));
    }

    @Override
    protected void afterConversion(EntornAppEntity entity, EntornApp resource) {
        List<AppIntegracioEntity> integracions = appIntegracioRepository.findByEntornApp(entity);
        if (!integracions.isEmpty()) {
            resource.setIntegracions(
                    integracions.stream().map(i -> new AppIntegracio(
                            ResourceReference.toResourceReference(i.getIntegracio().getId(), i.getIntegracio().getNom()),
                            null,
                            i.getIntegracio().getCodi(),
                            i.getIntegracio().getLogo(),
                            i.isActiva())).collect(Collectors.toList()));
        }
        List<AppSubsistemaEntity> subsistemes = subsistemaRepository.findByEntornApp(entity);
        if (!integracions.isEmpty()) {
            resource.setSubsistemes(
                    subsistemes.stream().map(s -> new AppSubsistema(
                            s.getCodi(),
                            s.getNom(),
                            s.isActiu(),
                            null)).collect(Collectors.toList()));
        }
        List<AppContextEntity> contexts = contextRepository.findByEntornApp(entity);
        if (!contexts.isEmpty()) {
            resource.setContexts(
                    contexts.stream().map(s -> new AppContext(
                            s.getCodi(),
                            s.getNom(),
                            s.getPath(),
//                            null,
                            (s.getManuals() != null ? s.getManuals().stream().map(m -> new AppManual(m.getNom(), m.getPath(), null)).collect(Collectors.toList()) : null),
                            s.getApi(),
                            s.isActiu(),
                            null)).collect(Collectors.toList()));
        }
        resource.setEntornAppDescription((resource.getApp() != null ? resource.getApp().getDescription() : "")
                + " - "
                + (resource.getEntorn() != null ? resource.getEntorn().getDescription() : ""));
    }

    @Override
    protected void afterCreateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterCreateSave(entity, resource, answers, anyOrderChanged);
        schedulerService.programarTasca(entity);
        appInfoHelper.programarTasquesSalutEstadistica(entity);
    }

    @Override
    protected void afterUpdateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        schedulerService.programarTasca(entity);
        appInfoHelper.programarTasquesSalutEstadistica(entity);
        cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entity.getId().toString());
    }

    // ACCIONS

    public static class RefreshAction implements ActionExecutor<EntornAppEntity, EntornAppParamAction, EntornApp> {
        private final EntornAppRepository entornAppRepository;
        private final AppInfoHelper appInfoHelper;

        public RefreshAction(EntornAppRepository entornAppRepository, AppInfoHelper appInfoHelper) {
            this.entornAppRepository = entornAppRepository;
            this.appInfoHelper = appInfoHelper;
        }

        @Transactional
        @Override
        public EntornApp exec(String code, EntornAppEntity entity, EntornAppParamAction params) throws ActionExecutionException {
            Long entornAppId = Objects.nonNull(params) ? params.getEntornAppId() : null;
            if (Objects.nonNull(entornAppId)) {
                try {
                    log.info("Executant procés per l'entornApp {}", entornAppId);
                    // Refrescar informació per a un únic entorn-app
                    appInfoHelper.refreshAppInfo(params.getEntornAppId());
                } catch (Exception e) {
                    log.error("Error en l'execució del procés de refresc de la informació per l'entornApp {}", entornAppId, e);
                }
            } else {
                try {
                    log.info("Executant procés per a tots els entorns-app");
                    // Refrescar informació de TOTS els entorns-app.
                    // Si una d'elles falla, es captura i es continua (cada entorn ja gestiona el seu propi error internament).
                    appInfoHelper.refreshAppInfo();
                } catch (Exception e) {
                    log.error("Error inesperat durant l'execució global del refresc d'informació d'entorns-app", e);
                }
            }
            return null;
        }

        @Override
        public void onChange(Serializable id, EntornAppParamAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EntornAppParamAction target) {
        }
    }

    public static class ReprogramarAction implements ActionExecutor<EntornAppEntity, EntornAppParamAction, EntornApp> {
        private final EntornAppRepository entornAppRepository;
        private final ConfiguracioSchedulerService schedulerService;

        public ReprogramarAction(EntornAppRepository entornAppRepository, ConfiguracioSchedulerService schedulerService) {
            this.entornAppRepository = entornAppRepository;
            this.schedulerService = schedulerService;
        }

        @Override
        public EntornApp exec(String code, EntornAppEntity entity, EntornAppParamAction params) throws ActionExecutionException {

            EntornAppEntity entornApp = entornAppRepository.findById(params.getEntornAppId())
                    .orElseThrow(() -> new ActionExecutionException(EntornApp.class, params.getEntornAppId(), code, "EntornApp actiu no trobat"));

            schedulerService.programarTasca(entornApp);
            return null;
        }

        @Override
        public void onChange(Serializable id, EntornAppParamAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EntornAppParamAction target) {
        }
    }

    public static class PingUrlAction implements ActionExecutor<EntornAppEntity, String, PingUrlResponse> {
        private final RestTemplate restTemplate;

        public PingUrlAction(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        @Override
        public PingUrlResponse exec(String code, EntornAppEntity entity, String params) throws ActionExecutionException {
            return isEndpointReachable(params);
        }

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
        }

        public PingUrlResponse isEndpointReachable(String url) {
            PingUrlResponse pingUrlResponse = new PingUrlResponse();
            pingUrlResponse.setSuccess(false);
            String message = null;
            try {
                ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, null, Void.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    pingUrlResponse.setSuccess(true);
                    message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.success");
                }
            } catch (IllegalArgumentException e) {
                message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.illegalArgument");
            } catch (ResourceAccessException e) {
                message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.timeout");
            } catch (HttpStatusCodeException e) {
                int statusCode = e.getRawStatusCode();
                switch (statusCode) {
                    case 401:
                        message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.401");
                        break;
                    case 403 :
                        message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.403");
                        break;
                    case 404 :
                        message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.404");
                        break;
                    case 500 :
                        message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.500");
                        break;
                    default :
                        message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.default", statusCode, e.getStatusText());
                        break;
                }
            } catch (Exception e) {
                message = I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.unknown", e.getClass().getSimpleName());
            }
            pingUrlResponse.setMessage(message);
            return pingUrlResponse;
        }
    }

}
