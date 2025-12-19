package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.*;
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
import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
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
	/*private final AclServiceClient aclServiceClient;
	private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;*/

    @PostConstruct
    public void init() {
        register(EntornApp.ENTORN_APP_ACTION_PING_URL, new EntornAppServiceImpl.PingUrlAction(restTemplate));
        register(EntornApp.REPORT_LLISTAR_LOGS, new InformeLlistarLogs(restTemplate));
        register(EntornApp.REPORT_DESCARREGAR_LOG, new InformeDescarregarLog(restTemplate, entornAppRepository));
        register(EntornApp.REPORT_PREVISUALITZAR_LOG, new InformePrevisualitzarLog(restTemplate));
        register(EntornApp.ENTORN_APP_TOOGLE_ACTIVA, new EntornAppServiceImpl.ToogleActiva(resourceEntityMappingHelper));
    }

	@Override
	protected String additionalSpringFilter(
			String currentSpringFilter,
			String[] namedQueries) {
		/*ResponseEntity<Set<Serializable>> idsResponseEntity = aclServiceClient.findIdsWithAnyPermission(
				ResourceType.ENTORN_APP,
				Collections.singletonList(PermissionEnum.READ),
				httpAuthorizationHeaderHelper.getAuthorizationHeader());
		System.out.println(">>> allowedIds: " + idsResponseEntity.getBody());*/
		return null;
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
    }

    @Override
    protected void afterUpdateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);

        cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entity.getId().toString());
    }

    @Override
    protected void afterDelete(EntornAppEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        super.afterDelete(entity, answers);

        cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entity.getId().toString());
    }

    // ACCIONS
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
            cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entity.getId().toString());
            return resourceEntityMappingHelper.entityToResource(entity, EntornApp.class);
        }
    }

    private static HttpHeaders getLogsAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("USER", "PASSWORD"); // TODO
        return headers;
    }

    @RequiredArgsConstructor
    private static class InformeLlistarLogs implements ReportGenerator<EntornAppEntity, Long, FitxerInfo> {
        private final RestTemplate restTemplate;

        @Override
        public List<FitxerInfo> generateData(String code, EntornAppEntity entornAppEntity, Long params) throws ReportGenerationException {
            HttpEntity<Void> httpEntity = new HttpEntity<>(getLogsAuthHeaders());

            String logsUrl = entornAppEntity.getSalutUrl().substring(0, entornAppEntity.getSalutUrl().lastIndexOf("/")) + "/v1/logs"; // TODO
            URI uri = URI.create(logsUrl);
            ResponseEntity<List<FitxerInfo>> response = restTemplate
                    .exchange(uri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<FitxerInfo>>() {});

            return response.getBody();
        }

        @Override
        public void onChange(Serializable id, Long previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Long target) {}
    }

    @RequiredArgsConstructor
    public static class InformeDescarregarLog implements ReportGenerator<EntornAppEntity, String, InformeDescarregarLog.DescarregarLogParams> {
        private final RestTemplate restTemplate;
        private final EntornAppRepository entornAppRepository;

        @Getter
        @AllArgsConstructor
        public static class DescarregarLogParams implements Serializable {
            private Long entornAppId;
            private String nomFitxer;
        }

        @Override
        public List<DescarregarLogParams> generateData(String code, EntornAppEntity entity, String fileParams) throws ReportGenerationException {
            return List.of(new DescarregarLogParams(entity.getId(), fileParams));
        }

        @Override
        public DownloadableFile generateFile(String code, List<?> data, ReportFileType fileType, OutputStream out) {
            DescarregarLogParams params = (DescarregarLogParams) data.get(0);

            HttpEntity<Void> httpEntity = new HttpEntity<>(getLogsAuthHeaders());

            EntornAppEntity entornAppEntity = entornAppRepository.findById(params.getEntornAppId()).get();
            String logsUrl = entornAppEntity.getSalutUrl().substring(0, entornAppEntity.getSalutUrl().lastIndexOf("/")) + "/v1/logs/" + params.getNomFitxer(); // TODO
            URI uri = URI.create(logsUrl);
            ResponseEntity<FitxerContingut> response = restTemplate
                    .exchange(uri, HttpMethod.GET, httpEntity, FitxerContingut.class);

            return new DownloadableFile(response.getBody().getNom(), response.getBody().getMimeType(), response.getBody().getContingut());
        }

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {}
    }

    @RequiredArgsConstructor
    public static class InformePrevisualitzarLog implements ReportGenerator<EntornAppEntity, EntornApp.PrevisualitzarLogParams, EntornApp.PrevisualitzarLogResponse> {
        private final RestTemplate restTemplate;

        @Override
        public List<EntornApp.PrevisualitzarLogResponse> generateData(String code, EntornAppEntity entornAppEntity, EntornApp.PrevisualitzarLogParams params) throws ReportGenerationException {
            HttpEntity<Void> httpEntity = new HttpEntity<>(getLogsAuthHeaders());

            String logsUrl = entornAppEntity.getSalutUrl().substring(0, entornAppEntity.getSalutUrl().lastIndexOf("/")) +
                    "/v1/logs/" + params.getFileName() + "/linies/" + params.getLineCount(); // TODO
            URI uri = URI.create(logsUrl);
            ResponseEntity<List<String>> response = restTemplate
                    .exchange(uri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<String>>() {});

            return response.getBody().stream()
                    .map(EntornApp.PrevisualitzarLogResponse::new)
                    .collect(Collectors.toList());
        }

        @Override
        public void onChange(Serializable id, EntornApp.PrevisualitzarLogParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EntornApp.PrevisualitzarLogParams target) {}
    }

}
