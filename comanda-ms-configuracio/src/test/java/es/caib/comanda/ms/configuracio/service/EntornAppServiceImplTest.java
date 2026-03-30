package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.intf.model.ExpectedResponseTypeEnum;
import es.caib.comanda.configuracio.logic.service.ConfiguracioSchedulerService;
import es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl;
import es.caib.comanda.configuracio.persist.entity.*;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.configuracio.persist.repository.AppIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ContextRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.model.v1.log.FitxerInfo;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import java.util.*;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_APP_CACHE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntornAppServiceImplTest {

    // Test subclass to expose protected methods
    static class TestableEntornAppServiceImpl extends EntornAppServiceImpl {
        
        public TestableEntornAppServiceImpl(AppIntegracioRepository appIntegracioRepository,
                                          SubsistemaRepository subsistemaRepository,
                                          ContextRepository contextRepository,
                                          EntornAppRepository entornAppRepository,
                                          AppInfoHelper appInfoHelper,
                                          CacheHelper cacheHeper,
                                          ConfiguracioSchedulerService schedulerService,
                                          AuthenticationHelper authenticationHelper,
                                          HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
                                          AclServiceClient aclServiceClient,
                                          RestTemplate restTemplate,
                                          Validator validator,
                                          ResourceEntityMappingHelper resourceEntityMappingHelper,
                                          ApplicationEventPublisher eventPublisher) {
            super(appIntegracioRepository, subsistemaRepository, contextRepository, entornAppRepository, appInfoHelper,
                    cacheHeper, schedulerService, authenticationHelper, httpAuthorizationHeaderHelper, aclServiceClient,
                    restTemplate, validator, resourceEntityMappingHelper, eventPublisher);
        }

        @Override
        public void afterConversion(EntornAppEntity entity, EntornApp resource) {
            super.afterConversion(entity, resource);
        }

        @Override
        public void afterCreateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterCreateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterUpdateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }

        public String exposedAdditionalSpringFilter() {
            return super.additionalSpringFilter(null, null);
        }
    }

    @Mock
    private AppIntegracioRepository integracioRepository;

    @Mock
    private SubsistemaRepository subsistemaRepository;
    
    @Mock
    private ContextRepository contextRepository;

    @Mock
    private EntornAppRepository entornAppRepository;

    @Mock
    private AppInfoHelper appInfoHelper;

    @Mock
    private HttpAuthorizationHeaderHelper keycloakHelper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private SalutServiceClient salutServiceClient;

    @Mock
    private EstadisticaServiceClient estadisticaServiceClient;

    @Mock
    private ConfiguracioSchedulerService schedulerService;

    @Mock
    private CacheHelper cacheHelper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Validator validator;

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private I18nUtil i18nUtil;
    @Mock
    private ApplicationContext applicationContext;

    private TestableEntornAppServiceImpl entornAppService;

    private EntornAppEntity entornAppEntity;
    private EntornApp entornAppResource;
    private List<AppIntegracioEntity> integracions;
    private List<AppSubsistemaEntity> subsistemes;

    private String statsAuthUser;
    private String statsAuthPassword;

    @BeforeEach
    void setUp() {
        // Initialize the service with mocked dependencies
        entornAppService = new TestableEntornAppServiceImpl(
            integracioRepository,
            subsistemaRepository,
            contextRepository,
            entornAppRepository,
            appInfoHelper,
            cacheHelper,
            schedulerService,
            authenticationHelper,
            keycloakHelper,
            aclServiceClient,
            restTemplate,
            validator,
            resourceEntityMappingHelper,
            eventPublisher
        );
        
        // Setup test data
        AppEntity appEntity = new AppEntity();
        appEntity.setId(1L);
        appEntity.setNom("Test App");
        
        EntornEntity entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");
        
        entornAppEntity = new EntornAppEntity();
        entornAppEntity.setId(1L);
        entornAppEntity.setApp(appEntity);
        entornAppEntity.setEntorn(entornEntity);
        entornAppEntity.setInfoUrl("http://test.com/info");
        entornAppEntity.setVersio("1.0.0");
        entornAppEntity.setActiva(true);
        
        entornAppResource = new EntornApp();
        entornAppResource.setId(1L);
        
        // Setup integracions
        AppIntegracioEntity appIntegracio = new AppIntegracioEntity();
        IntegracioEntity integracio = new IntegracioEntity();
        appIntegracio.setId(1L);
        integracio.setCodi("INT1");
        integracio.setNom("Integracio 1");
        appIntegracio.setIntegracio(integracio);
        appIntegracio.setActiva(true);
        appIntegracio.setEntornApp(entornAppEntity);
        
        integracions = new ArrayList<>();
        integracions.add(appIntegracio);
        
        // Setup subsistemes
        AppSubsistemaEntity subsistema = new AppSubsistemaEntity();
        subsistema.setId(1L);
        subsistema.setCodi("SUB1");
        subsistema.setNom("Subsistema 1");
        subsistema.setActiu(true);
        subsistema.setEntornApp(entornAppEntity);
        
        subsistemes = new ArrayList<>();
        subsistemes.add(subsistema);

        this.statsAuthUser = "test_user";
        this.statsAuthPassword = "test_pass";
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
    }

    private void stubAclContext(String... roles) {
        when(keycloakHelper.getAuthorizationHeader()).thenReturn("Bearer test");
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("anna");
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(roles);
    }

    @Test
    void testAfterConversion() {
        // Mock repository calls
        when(integracioRepository.findByEntornApp(entornAppEntity)).thenReturn(integracions);
        when(subsistemaRepository.findByEntornApp(entornAppEntity)).thenReturn(subsistemes);
        
        // Call the method to test
        entornAppService.afterConversion(entornAppEntity, entornAppResource);
        
        // Verify that the repositories were called
        verify(integracioRepository).findByEntornApp(entornAppEntity);
        verify(subsistemaRepository).findByEntornApp(entornAppEntity);
        
        // Verify that the resource was updated correctly
        assertNotNull(entornAppResource.getIntegracions());
        assertEquals(1, entornAppResource.getIntegracions().size());
        AppIntegracio appIntegracio = entornAppResource.getIntegracions().get(0);
        assertEquals("INT1", appIntegracio.getCodi());
//        assertEquals("Integracio 1", appIntegracio.getNom());
        assertTrue(appIntegracio.isActiva());
        
        assertNotNull(entornAppResource.getSubsistemes());
        assertEquals(1, entornAppResource.getSubsistemes().size());
        AppSubsistema appSubsistema = entornAppResource.getSubsistemes().get(0);
        assertEquals("SUB1", appSubsistema.getCodi());
        assertEquals("Subsistema 1", appSubsistema.getNom());
        assertTrue(appSubsistema.isActiu());
    }

    @Test
    void testAfterUpdateSave() {
        // Setup test data
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        
        // Call the method to test
        entornAppService.afterUpdateSave(entornAppEntity, entornAppResource, answers, false);

        verify(cacheHelper).evictCacheItem(ENTORN_APP_CACHE, entornAppEntity.getId().toString());
    }

    @Test
    void additionalSpringFilter_quanLusuariEsConsulta_noAplicaFiltreAcl() {
        when(authenticationHelper.isCurrentUserInRole("COM_ADMIN")).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole("COM_CONSULTA")).thenReturn(true);

        String result = entornAppService.exposedAdditionalSpringFilter();

        assertNull(result);
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    void additionalSpringFilter_quanHiHaPermisosPerAppIEntornApp_combinaElsDosFiltres() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L, 2L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(5L)));

        String result = entornAppService.exposedAdditionalSpringFilter();

        assertEquals("app.id:1 or app.id:2 or id:5", result);
    }

    @Test
    void additionalSpringFilter_quanNomesHiHaPermisosPerApp_retornaFiltrePerAplicacions() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L, 2L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        String result = entornAppService.exposedAdditionalSpringFilter();

        assertEquals("app.id:1 or app.id:2", result);
    }

    @Test
    void additionalSpringFilter_quanNomesHiHaPermisosPerEntornApp_retornaFiltrePerEntornsApp() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(5L, 7L)));

        String result = entornAppService.exposedAdditionalSpringFilter();

        assertEquals("id:5 or id:7", result);
    }

    @Test
    void additionalSpringFilter_quanConsultaAcl_passaUsuariIRolsActuals() {
        stubAclContext("COM_USER", "COM_EXTRA");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        entornAppService.exposedAdditionalSpringFilter();

        verify(aclServiceClient).findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"));
        verify(aclServiceClient).findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER", "COM_EXTRA")),
                eq("Bearer test"));
    }

    @Test
    void additionalSpringFilter_quanNoHiHaPermisos_retornaFiltreBuit() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(null));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        String result = entornAppService.exposedAdditionalSpringFilter();

        assertEquals("id:0", result);
    }

    @Test
    @DisplayName("PingUrlAction: ping bàsic sense validació de tipus")
    void pingUrlAction_pingBasic_resposta200() {
        // Arrange
        EntornApp formData = new EntornApp();
        formData.setSalutUrl("http://test.com/salut");
        formData.setEstadisticaUrl("http://test.com/estadistica");
        formData.setEstadisticaInfoUrl("http://test.com/estadistica-info");

        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/any-endpoint");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(null);

        ResponseEntity<Void> response = ResponseEntity.ok().build();
        when(restTemplate.exchange(
                eq("http://test.com/any-endpoint"),
                eq(HttpMethod.GET),
                any(),
                eq(Void.class)))
                .thenReturn(response);

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertTrue(result.getSuccess());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(Void.class));
    }

    @Test
    @DisplayName("PingUrlAction: ping bàsic amb error HTTP 404")
    void pingUrlAction_pingBasic_error404() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/not-found");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.BASIC_PING);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("PingUrlAction: validació de tipus amb resposta correcta (AppInfo)")
    void pingUrlAction_validacioTipus_respostaValida() {
        // Arrange
        EntornApp formData = new EntornApp();
        formData.setInfoUrl("http://test.com/info");

        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        AppInfo appInfo = new AppInfo();
        appInfo.setCodi("APP");
        appInfo.setNom("Aplicació Test");

        ResponseEntity<AppInfo> response = ResponseEntity.ok(appInfo);
        when(restTemplate.exchange(
                eq("http://test.com/info"),
                eq(HttpMethod.GET),
                any(),
                eq(AppInfo.class)))
                .thenReturn(response);

        when(validator.validate(appInfo)).thenReturn(Set.of());

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertTrue(result.getSuccess());
        verify(validator).validate(appInfo);
    }

    @Test
    @DisplayName("PingUrlAction: validació de tipus amb tipus incorrecte")
    void pingUrlAction_validacioTipus_tipusIncorrecte() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        ResponseEntity<String> response = ResponseEntity.ok("Resposta inesperada");
        when(restTemplate.exchange(
                eq("http://test.com/info"),
                eq(HttpMethod.GET),
                any(),
                eq(AppInfo.class)))
                .thenReturn((ResponseEntity) response);

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
        verify(validator, never()).validate(any());
    }

    @Test
    @DisplayName("PingUrlAction: validació de tipus amb cos buit (null)")
    void pingUrlAction_validacioTipus_cosBuit() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        ResponseEntity<AppInfo> response = ResponseEntity.ok(null);
        when(restTemplate.exchange(
                eq("http://test.com/info"),
                eq(HttpMethod.GET),
                any(),
                eq(AppInfo.class)))
                .thenReturn(response);

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("PingUrlAction: validació Bean Validation falla per @Size")
    void pingUrlAction_validacioBean_sizeViolat() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        AppInfo appInfoInvalida = new AppInfo();
        appInfoInvalida.setCodi("");
        appInfoInvalida.setNom("Nom Vàlid");

        ResponseEntity<AppInfo> response = ResponseEntity.ok(appInfoInvalida);
        when(restTemplate.exchange(
                eq("http://test.com/info"),
                eq(HttpMethod.GET),
                any(),
                eq(AppInfo.class)))
                .thenReturn(response);

        ConstraintViolation<AppInfo> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("codi");
        when(violation.getMessage()).thenReturn("size must be between 1 and 16");
        when(validator.validate(appInfoInvalida)).thenReturn(Set.of(violation));

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("PingUrlAction: validació Bean Validation falla per @NotNull")
    void pingUrlAction_validacioBean_notNullViolat() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        AppInfo appInfoInvalida = new AppInfo();
        appInfoInvalida.setCodi(null);
        appInfoInvalida.setNom("Nom Vàlid");

        ResponseEntity<AppInfo> response = ResponseEntity.ok(appInfoInvalida);
        when(restTemplate.exchange(
                eq("http://test.com/info"),
                eq(HttpMethod.GET),
                any(),
                eq(AppInfo.class)))
                .thenReturn(response);

        ConstraintViolation<AppInfo> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("codi");
        when(violation.getMessage()).thenReturn("must not be null");
        when(validator.validate(appInfoInvalida)).thenReturn(Set.of(violation));

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("PingUrlAction: error de deserialització JSON (HttpMessageNotReadableException)")
    void pingUrlAction_errorDeserialitzacio() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        when(restTemplate.exchange(
                eq("http://test.com/info"),
                eq(HttpMethod.GET),
                any(),
                eq(AppInfo.class)))
                .thenThrow(new HttpMessageNotReadableException(
                        "JSON parse error: Unexpected character",
                        new java.io.IOException("Mock error")));

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("PingUrlAction: timeout de connexió (ResourceAccessException)")
    void pingUrlAction_timeoutConnexio() {
        // Arrange
        EntornApp formData = new EntornApp();
        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test-no-existeix.com/info");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.INFO);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(AppInfo.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertFalse(result.getSuccess());
    }

    @Test
    @DisplayName("PingUrlAction: validació de llista genèrica List<FitxerInfo>")
    void pingUrlAction_validacioLlistaGenerica_respostaValida() {
        // Arrange
        EntornApp formData = new EntornApp();
        formData.setLogsUrl("http://test.com/logs");

        EntornApp.EntornAppPingAction params = new EntornApp.EntornAppPingAction();
        params.setEndpoint("http://test.com/logs");
        params.setFormData(formData);
        params.setExpectedResponseTypeEnum(ExpectedResponseTypeEnum.LOGS);

        FitxerInfo fitxer1 = new FitxerInfo();
        fitxer1.setNom("log1.txt");
        fitxer1.setMida(1024L);

        List<FitxerInfo> logs = List.of(fitxer1);

        ParameterizedTypeReference<List<FitxerInfo>> typeRef =
                new ParameterizedTypeReference<List<FitxerInfo>>() {};

        ResponseEntity<List<FitxerInfo>> response = ResponseEntity.ok(logs);
        when(restTemplate.exchange(
                eq("http://test.com/logs"),
                eq(HttpMethod.GET),
                any(),
                eq(typeRef)))
                .thenReturn(response);

        when(validator.validate(logs)).thenReturn(Set.of());

        EntornAppServiceImpl.PingUrlAction pingAction =
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertTrue(result.getSuccess());
    }
}
