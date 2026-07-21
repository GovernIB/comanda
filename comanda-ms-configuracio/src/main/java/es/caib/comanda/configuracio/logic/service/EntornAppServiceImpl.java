package es.caib.comanda.configuracio.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.configuracio.logic.intf.model.*;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.EntornAppExistsParameterAction;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.EntornAppPingAction;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.ExistsParameterResponse;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp.PingUrlResponse;
import es.caib.comanda.configuracio.logic.intf.service.EntornAppService;
import es.caib.comanda.configuracio.logic.intf.util.AuthHeaderUtil;
import es.caib.comanda.configuracio.persist.entity.*;
import es.caib.comanda.configuracio.persist.repository.*;
import es.caib.comanda.model.v1.log.FitxerContingut;
import es.caib.comanda.model.v1.log.FitxerInfo;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.PerspectiveApplicationException;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.*;
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

    @Value("${" + BaseConfig.PROP_STATS_AUTH_USER + ":}")
    private String statsAuthUser;
    @Value("${" + BaseConfig.PROP_STATS_AUTH_PASSWORD + ":}")
    private String statsAuthPassword;

    private final AppIntegracioRepository appIntegracioRepository;
    private final SubsistemaRepository subsistemaRepository;
    private final ContextRepository contextRepository;
    private final EntornAppRepository entornAppRepository;
    private final EntornAppHistRepository entornAppHistRepository;
    private final AppInfoHelper appInfoHelper;
    private final CacheHelper cacheHelper;
    private final EntornAppHelper entornAppHelper;
    private final AuthenticationHelper authenticationHelper;
    private final HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    private final AclServiceClient aclServiceClient;
    private final RestTemplate restTemplate;
    private final Validator validator;
    private final ResourceEntityMappingHelper resourceEntityMappingHelper;
    private final Environment environment;

    @PostConstruct
    public void init() {
        register(EntornApp.ENTORN_APP_ACTION_PING_URL, new PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment));
        register(EntornApp.ENTORN_APP_ACTION_EXISTS_PARAMETER, new ExistsParameterAction(environment));
        register(EntornApp.REPORT_LLISTAR_LOGS, new InformeLlistarLogs(restTemplate, statsAuthUser, statsAuthPassword, environment));
        register(EntornApp.REPORT_DESCARREGAR_LOG, new InformeDescarregarLog(restTemplate, entornAppRepository, statsAuthUser, statsAuthPassword, environment));
        register(EntornApp.REPORT_PREVISUALITZAR_LOG, new InformePrevisualitzarLog(restTemplate, statsAuthUser, statsAuthPassword, environment));
        register(EntornApp.ENTORN_APP_TOOGLE_ACTIVA, new ToogleActiva(resourceEntityMappingHelper));
        register(EntornApp.ENTORN_APP_REFRESH_INFO, new RefreshInfo(resourceEntityMappingHelper));
        register(EntornApp.PERSPECTIVE_DEFAULT_LOGS, new DefaultLogsPerspectiveApplicator());
        register(EntornApp.PERSPECTIVE_HISTORICS_VERSIONS, new HistoricVersionsPerspectiveApplicator());
        register(EntornApp.PERSPECTIVE_INTEGRACIONS_SUBSISTEMES_CONTEXTS, new IntegracionsSubsistemesContextsPerspectiveApplicator());
    }

    @Override
    protected String additionalSpringFilter(
        String currentSpringFilter,
        String[] namedQueries) {
        if (authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)
            || authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)) {
            return null;
        }
        Set<Serializable> appPermissionIds = getAllowedIds(ResourceType.APP);
        Set<Serializable> entornAppPermissionIds = getAllowedIds(ResourceType.ENTORN_APP);
        String appFilter = buildOrFilter("app.id", appPermissionIds);
        String entornAppFilter = buildOrFilter("id", entornAppPermissionIds);
        if (appFilter == null && entornAppFilter == null) {
            return "id:0";
        }
        if (appFilter == null) {
            return entornAppFilter;
        }
        if (entornAppFilter == null) {
            return appFilter;
        }
        return appFilter + " or " + entornAppFilter;
    }

    private Set<Serializable> getAllowedIds(ResourceType resourceType) {
        return Optional.ofNullable(aclServiceClient.findIdsWithAnyPermission(
                resourceType,
                Collections.singletonList(PermissionEnum.READ),
                authenticationHelper.getCurrentUserName(),
                Arrays.asList(authenticationHelper.getCurrentUserRealmRoles()),
                httpAuthorizationHeaderHelper.getAuthorizationHeader()).getBody())
            .orElse(Collections.emptySet());
    }

    private String buildOrFilter(String fieldName, Set<Serializable> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids.stream()
            .sorted(Comparator.comparingLong(id -> Long.parseLong(String.valueOf(id))))
            .map(String::valueOf)
            .map(id -> fieldName + ":" + id)
            .collect(Collectors.joining(" or "));
    }

    @Override
    protected void afterCreateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterCreateSave(entity, resource, answers, anyOrderChanged);

        entornAppHelper.publishEntornAppChanged(entity.getId());
    }

    @Override
    protected void afterUpdateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
        super.afterUpdateSave(entity, resource, answers, anyOrderChanged);

        cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entity.getId().toString());
        entornAppHelper.publishEntornAppChanged(entity.getId());
    }

    @Override
    protected void afterDelete(EntornAppEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
        super.afterDelete(entity, answers);
        entornAppHelper.logicAfterDelete(entity.getId());
    }

    // ACCIONS

    public static class PingUrlAction implements ActionExecutor<EntornAppEntity, EntornAppPingAction, PingUrlResponse> {
        private final RestTemplate restTemplate;
        private final Validator validator;
        private final String statsAuthUser;
        private final String statsAuthPassword;
        private final Environment environment;

        public PingUrlAction(RestTemplate restTemplate, Validator validator, String statsAuthUser, String statsAuthPassword, Environment environment) {
            this.restTemplate = restTemplate;
            this.validator = validator;
            this.statsAuthUser = statsAuthUser;
            this.statsAuthPassword = statsAuthPassword;
            this.environment = environment;
        }

        @Override
        public PingUrlResponse exec(String code, EntornAppEntity entity, EntornAppPingAction params) throws ActionExecutionException {
            return isEndpointReachable(params);
        }

        @Override
        public void onChange(Serializable id, EntornAppPingAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EntornAppPingAction target) {
        }

        public PingUrlResponse isEndpointReachable(EntornAppPingAction params) {
            I18nUtil i18nUtil = I18nUtil.getInstance();
            PingUrlResponse pingUrlResponse = new PingUrlResponse();
            pingUrlResponse.setSuccess(false);
            String message = null;
            try {
                ResponseEntity<?> response;
                ExpectedResponseTypeEnum responseType = params.getExpectedResponseTypeEnum();
                boolean shouldValidateBody = (responseType != null && responseType.requiresBodyValidation());
                HttpEntity<Void> entity = buildAuthEntityIfNeeded(params);
                if (!shouldValidateBody || responseType == ExpectedResponseTypeEnum.BASIC_PING) {
                    response = restTemplate.exchange(params.getEndpoint(), HttpMethod.GET, entity, Void.class);
                } else if (responseType.getGenericType() != null) {
                    response = restTemplate.exchange(params.getEndpoint(), HttpMethod.GET, entity, responseType.getGenericType());
                } else {
                    response = restTemplate.exchange(params.getEndpoint(), HttpMethod.GET, entity, responseType.getRawType());
                }

                if (response.getStatusCode().is2xxSuccessful()) {
                    if (shouldValidateBody) {
                        boolean hasBody = response.getBody() != null;
                        boolean hasCorrectType = hasBody && responseType.getRawType().isInstance(response.getBody());
                        if (!hasBody) {
                            message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.emptyBody");
                        } else if (!hasCorrectType) {
                            message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.incorrectBody");
                        } else {
                            Object body = response.getBody();
                            Set<ConstraintViolation<Object>> violations = validator.validate(body);
                            if (!violations.isEmpty()) {
                                pingUrlResponse.setValidationError(true);
                                message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.incorrectValidate") +
                                    " " + violations.stream()
                                    .map(v -> "[" + v.getPropertyPath() + ": " + v.getMessage() + "]")
                                    .collect(Collectors.joining(",\n "));
                            } else {
                                pingUrlResponse.setSuccess(true);
                                message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.correctBody");
                            }
                        }
                    } else {
                        pingUrlResponse.setSuccess(true);
                        message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.success");
                    }
                }
            } catch (HttpMessageNotReadableException e) {
                message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.deserializationError");
            } catch (IllegalArgumentException e) {
                message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.illegalArgument");
            } catch (ResourceAccessException e) {
                message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.timeout");
            } catch (HttpStatusCodeException e) {
                message = getMessageForStatusCode(e.getRawStatusCode());
            } catch (Exception e) {
                message = i18nUtil.getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.unknown", e.getClass().getSimpleName());
            }
            pingUrlResponse.setMessage(message);
            return pingUrlResponse;
        }

        private HttpEntity<Void> buildAuthEntityIfNeeded(EntornAppPingAction params) {
            // Comprova si el params.getEndpoint() enviat pel client coincideix amb alguna de les URLs d'estadístiques
            List<String> estadisticaURLs = Arrays.asList(params.getFormData().getEstadisticaUrl(), params.getFormData().getEstadisticaInfoUrl());
            boolean isEstadisticaRequest = estadisticaURLs.contains(params.getEndpoint());
            // Comprova si el params.getEndpoint() correspon a la URL de salut. Les peticions de salut que es fan cada minut
            // no es poden autenticar per motius de rendiment amb el servei d'autenticació.
            boolean isExcludedSalutRequest = params.getEndpoint().equals(params.getFormData().getSalutUrl());
            if (isExcludedSalutRequest ||
                (isEstadisticaRequest && !params.getFormData().isEstadisticaAuth()) ||
                (!isEstadisticaRequest && !params.getFormData().isSalutAuth())) {
                return null;
            }
            return AuthHeaderUtil.buildAuthHttpEntity(
                statsAuthUser, statsAuthPassword,
                params.getFormData().getNomUsuariAuth(),
                params.getFormData().getContrasenyaAuth(),
                params.getFormData().isParametreAuth(),
                environment
            );
        }

        private String getMessageForStatusCode(int statusCode) {
            switch (statusCode) {
                case 401:
                    return I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.401");
                case 403:
                    return I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.403");
                case 404:
                    return I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.404");
                case 500:
                    return I18nUtil.getInstance().getI18nMessage("es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.500");
                default:
                    return I18nUtil.getInstance().getI18nMessage(
                        "es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl.PingUrlAction.default",
                        statusCode, "HTTP Error");
            }
        }
    }

    @RequiredArgsConstructor
    public static class ExistsParameterAction implements ActionExecutor<EntornAppEntity, EntornAppExistsParameterAction, ExistsParameterResponse> {
        private final Environment environment;

        @Override
        public void onChange(Serializable id, EntornAppExistsParameterAction previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EntornAppExistsParameterAction target) {
        }

        @Override
        public ExistsParameterResponse exec(String code, EntornAppEntity entity, EntornAppExistsParameterAction params) throws ActionExecutionException {
            String key = params.getParameterValue();
            if (key == null || key.isBlank()) {
                return new ExistsParameterResponse(false);
            }
            String valor = environment.getProperty(key);
            return new ExistsParameterResponse(valor != null && !valor.isBlank());
        }
    }

    @RequiredArgsConstructor
    private class ToogleActiva implements ActionExecutor<EntornAppEntity, String, EntornApp> {
        private final ResourceEntityMappingHelper resourceEntityMappingHelper;

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
        }

        @Override
        public EntornApp exec(String code, EntornAppEntity entity, String params) throws ActionExecutionException {
            entity.setActiva(!entity.isActiva());
            cacheHelper.evictCacheItem(ENTORN_APP_CACHE, entity.getId().toString());
            // No passa per afterUpdateSave (l'acció no fa servir el flux normal d'actualització),
            // cal notificar-ho explícitament perquè el dashboard de Salut es refresqui via SSE.
            entornAppHelper.publishEntornAppChanged(entity.getId());
            return resourceEntityMappingHelper.entityToResource(entity, EntornApp.class);
        }
    }

    @RequiredArgsConstructor
    private class RefreshInfo implements ActionExecutor<EntornAppEntity, String, EntornApp> {
        private final ResourceEntityMappingHelper resourceEntityMappingHelper;

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
        }

        @Override
        public EntornApp exec(String code, EntornAppEntity entity, String params) throws ActionExecutionException {
            var appInfoProjection = new AppInfoHelper.AppInfoEntornAppProjection(entity.getId(), entity.getInfoUrl(),
                entity.isSalutAuth(), entity.getApp().getNom(), entity.getEntorn().getNom(),
                entity.isParametreAuth(), entity.getNomUsuariAuth(), entity.getContrasenyaAuth());
            appInfoHelper.refreshAppInfo(appInfoProjection);
            return resourceEntityMappingHelper.entityToResource(entity, EntornApp.class);
        }
    }

    @RequiredArgsConstructor
    private static class InformeLlistarLogs implements ReportGenerator<EntornAppEntity, Long, FitxerInfo> {
        private final RestTemplate restTemplate;
        private final String statsAuthUser;
        private final String statsAuthPassword;
        private final Environment environment;

        @Override
        public List<FitxerInfo> generateData(String code, EntornAppEntity entornAppEntity, Long params) throws ReportGenerationException {
            HttpEntity<Void> httpEntity = AuthHeaderUtil.buildAuthHttpEntity(
                statsAuthUser, statsAuthPassword,
                entornAppEntity.getNomUsuariAuth(),
                entornAppEntity.getContrasenyaAuth(),
                entornAppEntity.isParametreAuth(),
                environment
            );

            String logsUrl = entornAppEntity.getLogsUrl();
            URI uri = URI.create(logsUrl);
            ResponseEntity<List<FitxerInfo>> response = restTemplate
                .exchange(uri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<FitxerInfo>>() {
                });

            return response.getBody();
        }

        @Override
        public void onChange(Serializable id, Long previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, Long target) {
        }
    }

    @RequiredArgsConstructor
    public static class InformeDescarregarLog implements ReportGenerator<EntornAppEntity, String, InformeDescarregarLog.DescarregarLogParams> {
        private final RestTemplate restTemplate;
        private final EntornAppRepository entornAppRepository;
        private final String statsAuthUser;
        private final String statsAuthPassword;
        private final Environment environment;

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
            EntornAppEntity entornAppEntity = entornAppRepository.findById(params.getEntornAppId()).get();
            HttpEntity<Void> httpEntity = AuthHeaderUtil.buildAuthHttpEntity(
                statsAuthUser, statsAuthPassword,
                entornAppEntity.getNomUsuariAuth(),
                entornAppEntity.getContrasenyaAuth(),
                entornAppEntity.isParametreAuth(),
                environment
            );

            String baseUrl = entornAppEntity.getLogsUrl();
            String logsUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/")
                + params.getNomFitxer();
            String logsUrlDirecte = logsUrl + "/directe";

            // Provem si existeix el mètode /directe (per evitar carregar en memòria base64)
            try {
                URI uriDirecte = URI.create(logsUrlDirecte);
                return restTemplate.execute(uriDirecte, HttpMethod.GET, request -> {
                    request.getHeaders().addAll(httpEntity.getHeaders());
                }, response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
                        String filename = params.getNomFitxer();
                        if (contentDisposition != null) {
                            ContentDisposition cd = ContentDisposition.parse(contentDisposition);
                            if (cd.getFilename() != null) {
                                filename = cd.getFilename();
                            }
                        }
                        String contentType = response.getHeaders().getContentType() != null ?
                            response.getHeaders().getContentType().toString() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = response.getBody().read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                        return new DownloadableFile(filename, contentType, null);
                    } else {
                        throw new ResourceAccessException("L'endpoint /directe ha retornat " + response.getStatusCode());
                    }
                });
            } catch (Exception e) {
                log.debug("No s'ha pogut descarregar el log via /directe, provem el mètode actual: " + e.getMessage());
                URI uri = URI.create(logsUrl);
                ResponseEntity<FitxerContingut> response = restTemplate
                    .exchange(uri, HttpMethod.GET, httpEntity, FitxerContingut.class);

                return new DownloadableFile(response.getBody().getNom(), response.getBody().getMimeType(), response.getBody().getContingut());
            }
        }

        @Override
        public void onChange(Serializable id, String previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, String target) {
        }
    }

    @RequiredArgsConstructor
    public static class InformePrevisualitzarLog implements ReportGenerator<EntornAppEntity, EntornApp.PrevisualitzarLogParams, EntornApp.PrevisualitzarLogResponse> {
        private final RestTemplate restTemplate;
        private final String statsAuthUser;
        private final String statsAuthPassword;
        private final Environment environment;

        @Override
        public List<EntornApp.PrevisualitzarLogResponse> generateData(String code, EntornAppEntity entornAppEntity, EntornApp.PrevisualitzarLogParams params) throws ReportGenerationException {
            HttpEntity<Void> httpEntity = AuthHeaderUtil.buildAuthHttpEntity(
                statsAuthUser, statsAuthPassword,
                entornAppEntity.getNomUsuariAuth(),
                entornAppEntity.getContrasenyaAuth(),
                entornAppEntity.isParametreAuth(),
                environment
            );

            String baseUrl = entornAppEntity.getLogsUrl();
            String logsUrl = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + params.getFileName() + "/linies/" + params.getLineCount();
            URI uri = URI.create(logsUrl);
            ResponseEntity<List<String>> response = restTemplate
                .exchange(uri, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<String>>() {
                });

            return response.getBody().stream()
                .map(EntornApp.PrevisualitzarLogResponse::new)
                .collect(Collectors.toList());
        }

        @Override
        public void onChange(Serializable id, EntornApp.PrevisualitzarLogParams previous, String fieldName, Object fieldValue, Map<String, AnswerRequiredException.AnswerValue> answers, String[] previousFieldNames, EntornApp.PrevisualitzarLogParams target) {
        }
    }

    public class IntegracionsSubsistemesContextsPerspectiveApplicator implements PerspectiveApplicator<EntornAppEntity, EntornApp> {
        @Override
        public void applySingle(String code, EntornAppEntity entity, EntornApp resource) throws PerspectiveApplicationException {
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
            if (!subsistemes.isEmpty()) {
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
                        (s.getManuals() != null ? s.getManuals().stream().map(m -> new AppManual(m.getNom(), m.getPath(), null)).collect(Collectors.toList()) : null),
                        s.getApi(),
                        s.isActiu(),
                        null)).collect(Collectors.toList()));
            }
        }
    }

    public static class DefaultLogsPerspectiveApplicator implements PerspectiveApplicator<EntornAppEntity, EntornApp> {
        @Override
        public void applySingle(String code, EntornAppEntity entity, EntornApp resource) throws PerspectiveApplicationException {
            String appName = entity.getApp() != null ? entity.getApp().getNom() : "";
            String[] defaultLogs = {"es.caib." + appName.toLowerCase() + ".log", "server.log"};
            resource.setDefaultLogs(defaultLogs);
        }
    }

    public class HistoricVersionsPerspectiveApplicator implements PerspectiveApplicator<EntornAppEntity, EntornApp> {
        @Override
        public void applySingle(String code, EntornAppEntity entity, EntornApp resource) throws PerspectiveApplicationException {
            List<EntornAppHistEntity> entornAppHistEntities = entornAppHistRepository.findByEntornAppOrderByDataDesc(entity);
            resource.setEntornAppHistorics(
                entornAppHistEntities.stream()
                    .map(s -> objectMappingHelper.newInstanceMap(
                        s,
                        EntornAppHist.class))
                    .collect(Collectors.toList()));
        }
    }

}
