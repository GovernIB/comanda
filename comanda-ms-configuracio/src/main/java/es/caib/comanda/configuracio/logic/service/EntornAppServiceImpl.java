package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.AppContext;
import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.configuracio.logic.intf.model.AppManual;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.EntornAppParamAction;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.PingUrlResponse;
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
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_APP_CACHE;

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
    private final ResourceEntityMappingHelper resourceEntityMappingHelper;
    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        register(EntornApp.ENTORN_APP_ACTION_REPROGRAMAR, new EntornAppServiceImpl.ReprogramarAction(entornAppRepository, schedulerService));
        register(EntornApp.ENTORN_APP_ACTION_PING_URL, new EntornAppServiceImpl.PingUrlAction(restTemplate));
        register(EntornApp.ENTORN_APP_TOOGLE_ACTIVA, new EntornAppServiceImpl.ToogleActiva(resourceEntityMappingHelper));
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


    /**
     * Esdeveniment que indica que s'han de reprogramar les tasques en segon pla de l'entornApp indicat.
     */
    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class ReprogramarEvent {
        private final EntornAppEntity entity;
        private final boolean netejarCache;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void reprogramarTasques(ReprogramarEvent event){
        if (event.isNetejarCache())
            cacheHelper.evictCacheItem(ENTORN_APP_CACHE, event.getEntity().getId().toString());
        schedulerService.programarTasca(event.getEntity());
        appInfoHelper.programarTasquesSalutEstadistica(event.getEntity());
    }

    @Override
    protected void afterCreateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterCreateSave(entity, resource, answers, anyOrderChanged);

//        reprogramarTasques(entity, false);
        eventPublisher.publishEvent(new ReprogramarEvent(entity, false));
    }

    @Override
    protected void afterUpdateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);

//        reprogramarTasques(entity, true);
        eventPublisher.publishEvent(new ReprogramarEvent(entity, true));
    }

    @Override
    protected void afterDelete(EntornAppEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        super.afterDelete(entity, answers);
//        Setejam entornApp actiu a false per a no es tornin a reprogramar les tasques sobre l'entornApp esborrat
        entity.setActiva(false);

//        reprogramarTasques(entity, true);
        eventPublisher.publishEvent(new ReprogramarEvent(entity, true));
    }

    // ACCIONS

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

    @RequiredArgsConstructor
    private class ToogleActiva implements ActionExecutor<EntornAppEntity, String, EntornApp> {
        private final ResourceEntityMappingHelper resourceEntityMappingHelper;

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {}

        @Override
        public EntornApp exec(String code, EntornAppEntity entity, String params) throws ActionExecutionException {
            entity.setActiva(!entity.isActiva());
//            reprogramarTasques(entity, true);
            eventPublisher.publishEvent(new ReprogramarEvent(entity, true));
            return resourceEntityMappingHelper.entityToResource(entity, EntornApp.class);
        }
    }

}
