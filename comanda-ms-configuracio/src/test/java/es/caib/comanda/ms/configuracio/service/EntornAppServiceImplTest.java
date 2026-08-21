package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.configuracio.logic.intf.model.*;
import es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl;
import es.caib.comanda.configuracio.persist.entity.*;
import es.caib.comanda.configuracio.persist.repository.*;
import es.caib.comanda.ms.logic.helper.*;
import es.caib.comanda.model.v1.log.FitxerInfo;
import es.caib.comanda.model.v1.salut.AppInfo;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.domain.Specification;
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
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntornAppServiceImplTest {

    // Test subclass to expose protected methods
    static class TestableEntornAppServiceImpl extends EntornAppServiceImpl {

        public TestableEntornAppServiceImpl(AppIntegracioRepository appIntegracioRepository,
                                          SubsistemaRepository subsistemaRepository,
                                          ContextRepository contextRepository,
                                          EntornAppRepository entornAppRepository,
                                          EntornAppHistRepository entornAppHistRepository,
                                          AppInfoHelper appInfoHelper,
                                          CacheHelper cacheHeper,
                                          EntornAppHelper entornAppHelper,
                                          AuthenticationHelper authenticationHelper,
                                          HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
                                          AclServiceClient aclServiceClient,
                                          RestTemplate restTemplate,
                                          Validator validator,
                                          ResourceEntityMappingHelper resourceEntityMappingHelper,
                                          Environment environment) {
            super(appIntegracioRepository, subsistemaRepository, contextRepository, entornAppRepository, entornAppHistRepository, appInfoHelper,
                    cacheHeper, entornAppHelper, authenticationHelper, httpAuthorizationHeaderHelper, aclServiceClient,
                    restTemplate, validator, resourceEntityMappingHelper, environment);
        }

        @Override
        public void afterConversion(EntornAppEntity entity, EntornApp resource) {
            super.afterConversion(entity, resource);
        }

        @Override
        public void afterConversion(List<EntornAppEntity> entities, List<EntornApp> resources) {
            super.afterConversion(entities, resources);
        }

        @Override
        public void afterCreateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterCreateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterUpdateSave(EntornAppEntity entity, EntornApp resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterDelete(EntornAppEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
            super.afterDelete(entity, answers);
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
    private EntornAppHistRepository entornAppHistRepository;

    @Mock
    private AppInfoHelper appInfoHelper;

    @Mock
    private HttpAuthorizationHeaderHelper keycloakHelper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private EntornAppHelper entornAppHelper;

    @Mock
    private CacheHelper cacheHelper;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Validator validator;

    @Mock
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @Mock
    private I18nUtil i18nUtil;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ObjectMappingHelper objectMappingHelper;
    @Mock
    private Environment environment;

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
            entornAppHistRepository,
            appInfoHelper,
            cacheHelper,
            entornAppHelper,
            authenticationHelper,
            keycloakHelper,
            aclServiceClient,
            restTemplate,
            validator,
            resourceEntityMappingHelper,
            environment
        );
        ReflectionTestUtils.setField(entornAppService, "objectMappingHelper", objectMappingHelper);

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
    void testIntegracionsSubsistemesContextsPerspectiveApplicator() {
        // Mock repository calls
        when(integracioRepository.findByEntornApp(entornAppEntity)).thenReturn(integracions);
        when(subsistemaRepository.findByEntornApp(entornAppEntity)).thenReturn(subsistemes);

        // Call the method to test
        EntornAppServiceImpl.IntegracionsSubsistemesContextsPerspectiveApplicator applicator =
                entornAppService.new IntegracionsSubsistemesContextsPerspectiveApplicator();
        applicator.applySingle(EntornApp.PERSPECTIVE_INTEGRACIONS_SUBSISTEMES_CONTEXTS, entornAppEntity, entornAppResource);

        // Verify that the repositories were called
        verify(integracioRepository).findByEntornApp(entornAppEntity);
        verify(subsistemaRepository).findByEntornApp(entornAppEntity);

        // Verify that the resource was updated correctly
        assertNotNull(entornAppResource.getIntegracions());
        assertEquals(1, entornAppResource.getIntegracions().size());
        AppIntegracio appIntegracio = entornAppResource.getIntegracions().get(0);
        assertEquals("INT1", appIntegracio.getCodi());
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

        verify(cacheHelper).evictEntornAppCacheItem(entornAppEntity.getId());
        verify(entornAppHelper).publishEntornAppChanged(entornAppEntity.getId());
    }

    @Test
    void toogleActivaAction_quanSexecuta_inverteixActivaIPublicaEsdevenimentSse() throws Exception {
        when(entornAppRepository.findOne(any(Specification.class))).thenReturn(Optional.of(entornAppEntity));
        when(resourceEntityMappingHelper.entityToResource(entornAppEntity, EntornApp.class))
                .thenReturn(entornAppResource);
        ReflectionTestUtils.setField(entornAppService, "entityRepository", entornAppRepository);
        entornAppService.init();
        boolean activaAbans = entornAppEntity.isActiva();

        Object result = entornAppService.artifactActionExec(
                1L,
                EntornApp.ENTORN_APP_TOOGLE_ACTIVA,
                null);

        assertEquals(!activaAbans, entornAppEntity.isActiva());
        assertSame(entornAppResource, result);
        verify(cacheHelper).evictEntornAppCacheItem(entornAppEntity.getId());
        verify(entornAppHelper).publishEntornAppChanged(entornAppEntity.getId());
    }

    @Test
    void refreshInfoAction_quanSexecuta_refrescaInformacioIEvictaCache() throws Exception {
        when(entornAppRepository.findOne(any(Specification.class))).thenReturn(Optional.of(entornAppEntity));
        when(resourceEntityMappingHelper.entityToResource(entornAppEntity, EntornApp.class))
                .thenReturn(entornAppResource);
        ReflectionTestUtils.setField(entornAppService, "entityRepository", entornAppRepository);
        entornAppService.init();

        Object result = entornAppService.artifactActionExec(
                1L,
                EntornApp.ENTORN_APP_REFRESH_INFO,
                null);

        verify(appInfoHelper).refreshAppInfo(argThat(p -> p.getId().equals(1L)));
        assertSame(entornAppResource, result);
    }

    private EntornApp createFullyPopulatedResource(Long id, Long appId, Long entornId) {
        EntornApp resource = new EntornApp();
        resource.setId(id);
        resource.setApp(ResourceReference.toResourceReference(appId, "App " + appId));
        resource.setEntorn(ResourceReference.toResourceReference(entornId, "Entorn " + entornId));
        ReflectionTestUtils.setField(resource, "versio", "1.0.0");
        ReflectionTestUtils.setField(resource, "revisio", "rev123");
        ReflectionTestUtils.setField(resource, "jdkVersion", "11");
        resource.setActiva(true);
        resource.setEntornAppDescription("Description " + id);
        // Censored fields
        resource.setInfoUrl("http://test.com/info");
        resource.setLogsUrl("http://test.com/logs");
        resource.setSalutUrl("http://test.com/salut");
        resource.setEstadisticaInfoUrl("http://test.com/stats-info");
        resource.setEstadisticaUrl("http://test.com/stats");
        resource.setEstadisticaCron("0 0 * * *");
        resource.setEstadisticaAuth(true);
        resource.setSalutAuth(true);
        resource.setCompactable(true);
        resource.setCompactacioSetmanalMesos(3);
        resource.setCompactacioMensualMesos(6);
        resource.setEliminacioMesos(12);
        resource.setAlarmesEmail("admin@test.com");
        resource.setParametreAuth(true);
        resource.setNomUsuariAuth("user1");
        resource.setContrasenyaAuth("secret");
        resource.setNumPermisos(4);
        return resource;
    }

    @Test
    @DisplayName("afterConversion: quan l'usuari és ADMIN no censura cap camp")
    void afterConversion_quanUsuariEsAdmin_noCensuraCamps() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        EntornApp resource = createFullyPopulatedResource(1L, 1L, 1L);

        entornAppService.afterConversion(entornAppEntity, resource);

        assertEquals("http://test.com/info", resource.getInfoUrl());
        assertEquals("http://test.com/logs", resource.getLogsUrl());
        assertEquals("http://test.com/salut", resource.getSalutUrl());
        assertEquals("http://test.com/stats-info", resource.getEstadisticaInfoUrl());
        assertEquals("http://test.com/stats", resource.getEstadisticaUrl());
        assertEquals("0 0 * * *", resource.getEstadisticaCron());
        assertTrue(resource.isEstadisticaAuth());
        assertTrue(resource.isSalutAuth());
        assertEquals(Boolean.TRUE, resource.getCompactable());
        assertEquals(3, resource.getCompactacioSetmanalMesos());
        assertEquals(6, resource.getCompactacioMensualMesos());
        assertEquals(12, resource.getEliminacioMesos());
        assertEquals("admin@test.com", resource.getAlarmesEmail());
        assertTrue(resource.isParametreAuth());
        assertEquals("user1", resource.getNomUsuariAuth());
        assertEquals("secret", resource.getContrasenyaAuth());
        assertEquals(4, resource.getNumPermisos());
        assertEquals("1.0.0", resource.getVersio());
        assertEquals("rev123", resource.getRevisio());
        assertEquals("11", resource.getJdkVersion());
        assertTrue(resource.isActiva());
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    @DisplayName("afterConversion: quan l'usuari és CONSULTA no censura cap camp")
    void afterConversion_quanUsuariEsConsulta_noCensuraCamps() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);
        EntornApp resource = createFullyPopulatedResource(1L, 1L, 1L);

        entornAppService.afterConversion(entornAppEntity, resource);

        assertEquals("http://test.com/info", resource.getInfoUrl());
        assertEquals("http://test.com/salut", resource.getSalutUrl());
        assertEquals("secret", resource.getContrasenyaAuth());
        assertTrue(resource.isEstadisticaAuth());
        assertEquals(4, resource.getNumPermisos());
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    @DisplayName("afterConversion: quan l'usuari té permís sobre l'EntornApp no censura cap camp")
    void afterConversion_quanUsuariTePermisPerEntornApp_noCensuraCamps() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        EntornApp resource = createFullyPopulatedResource(1L, 1L, 1L);

        entornAppService.afterConversion(entornAppEntity, resource);

        assertEquals("http://test.com/info", resource.getInfoUrl());
        assertEquals("http://test.com/logs", resource.getLogsUrl());
        assertEquals("http://test.com/salut", resource.getSalutUrl());
        assertEquals("secret", resource.getContrasenyaAuth());
        assertTrue(resource.isParametreAuth());
        assertEquals(4, resource.getNumPermisos());
    }

    @Test
    @DisplayName("afterConversion: quan l'usuari té permís sobre l'App no censura cap camp")
    void afterConversion_quanUsuariTePermisPerApp_noCensuraCamps() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L)));

        EntornApp resource = createFullyPopulatedResource(1L, 1L, 1L);

        entornAppService.afterConversion(entornAppEntity, resource);

        assertEquals("http://test.com/info", resource.getInfoUrl());
        assertEquals("http://test.com/salut", resource.getSalutUrl());
        assertEquals("secret", resource.getContrasenyaAuth());
        assertEquals("1.0.0", resource.getVersio());
    }

    @Test
    @DisplayName("afterConversion: quan l'usuari NO té permisos censura els camps anotats")
    void afterConversion_quanUsuariNoTePermisos_censuraCampsAnnotats() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        EntornApp resource = createFullyPopulatedResource(1L, 1L, 1L);

        entornAppService.afterConversion(entornAppEntity, resource);

        // Camps censurats (String -> null, boolean -> false, Boolean/Integer -> null, int -> 0)
        assertNull(resource.getInfoUrl());
        assertNull(resource.getLogsUrl());
        assertNull(resource.getSalutUrl());
        assertNull(resource.getEstadisticaInfoUrl());
        assertNull(resource.getEstadisticaUrl());
        assertNull(resource.getEstadisticaCron());
        assertFalse(resource.isEstadisticaAuth());
        assertFalse(resource.isSalutAuth());
        assertNull(resource.getCompactable());
        assertNull(resource.getCompactacioSetmanalMesos());
        assertNull(resource.getCompactacioMensualMesos());
        assertNull(resource.getEliminacioMesos());
        assertNull(resource.getAlarmesEmail());
        assertFalse(resource.isParametreAuth());
        assertNull(resource.getNomUsuariAuth());
        assertNull(resource.getContrasenyaAuth());
        assertEquals(0, resource.getNumPermisos());

        // Camps NO censurats es conserven
        assertEquals(1L, resource.getId());
        assertEquals("1.0.0", resource.getVersio());
        assertEquals("rev123", resource.getRevisio());
        assertEquals("11", resource.getJdkVersion());
        assertTrue(resource.isActiva());
        assertEquals("Description 1", resource.getEntornAppDescription());
        assertNotNull(resource.getApp());
        assertNotNull(resource.getEntorn());
    }

    @Test
    @DisplayName("afterConversion: quan entity és null censura els camps del resource")
    void afterConversion_quanEntityEsNull_censuraCamps() {
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);

        EntornApp resource = createFullyPopulatedResource(1L, 1L, 1L);

        entornAppService.afterConversion((EntornAppEntity) null, resource);

        assertNull(resource.getInfoUrl());
        assertNull(resource.getSalutUrl());
        assertNull(resource.getContrasenyaAuth());
    }

    @Test
    @DisplayName("afterConversion amb llista: censura només els recursos sense permisos")
    void afterConversionList_quanHiHaMixDePermisos_censuraNomesElsNoPermesos() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Set.of(1L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(ResponseEntity.ok(Collections.emptySet()));

        AppEntity app2 = new AppEntity();
        app2.setId(2L);
        EntornEntity entorn2 = new EntornEntity();
        entorn2.setId(2L);
        EntornAppEntity entity2 = new EntornAppEntity();
        entity2.setId(2L);
        entity2.setApp(app2);
        entity2.setEntorn(entorn2);

        EntornApp resource1 = createFullyPopulatedResource(1L, 1L, 1L);
        EntornApp resource2 = createFullyPopulatedResource(2L, 2L, 2L);

        entornAppService.afterConversion(List.of(entornAppEntity, entity2), List.of(resource1, resource2));

        // resource1 té permís -> NO censurat
        assertEquals("http://test.com/info", resource1.getInfoUrl());
        assertEquals("secret", resource1.getContrasenyaAuth());
        assertEquals(4, resource1.getNumPermisos());

        // resource2 NO té permís -> CENSURAT
        assertNull(resource2.getInfoUrl());
        assertNull(resource2.getContrasenyaAuth());
        assertEquals(0, resource2.getNumPermisos());
        assertEquals("1.0.0", resource2.getVersio());
    }

    @Test
    @DisplayName("afterConversion amb llista: quan l'usuari és ADMIN no censura res")
    void afterConversionList_quanUsuariEsAdmin_noCensuraCapElement() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        EntornApp resource1 = createFullyPopulatedResource(1L, 1L, 1L);
        EntornApp resource2 = createFullyPopulatedResource(2L, 2L, 2L);

        entornAppService.afterConversion(List.of(entornAppEntity, entornAppEntity), List.of(resource1, resource2));

        assertEquals("http://test.com/info", resource1.getInfoUrl());
        assertEquals("http://test.com/info", resource2.getInfoUrl());
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    void additionalSpringFilter_retornaNull() {
        String result = entornAppService.exposedAdditionalSpringFilter();
        assertNull(result);
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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

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
                new EntornAppServiceImpl.PingUrlAction(restTemplate, validator, statsAuthUser, statsAuthPassword, environment);

        // Act
        EntornApp.PingUrlResponse result = pingAction.isEndpointReachable(params);

        // Assert
        assertTrue(result.getSuccess());
    }

    @Test
    @DisplayName("DefaultLogsPerspectiveApplicator: aplica els logs per defecte segons el nom de l'aplicació")
    void defaultLogsPerspectiveApplicator_aplicaLogsPerDefecte() {
        AppEntity appEntity = new AppEntity();
        appEntity.setNom("Comanda");

        EntornAppEntity entity = new EntornAppEntity();
        entity.setApp(appEntity);

        EntornApp resource = new EntornApp();

        EntornAppServiceImpl.DefaultLogsPerspectiveApplicator applicator =
                new EntornAppServiceImpl.DefaultLogsPerspectiveApplicator();

        applicator.applySingle(EntornApp.PERSPECTIVE_DEFAULT_LOGS, entity, resource);

        assertArrayEquals(
                new String[]{"es.caib.comanda.log", "server.log"},
                resource.getDefaultLogs()
        );
    }

    @Test
    @DisplayName("DefaultLogsPerspectiveApplicator: aplica un nom buit quan l'aplicació és null")
    void defaultLogsPerspectiveApplicator_quanAppEsNull_aplicaLogAmbNomBuit() {
        EntornAppEntity entity = new EntornAppEntity();
        EntornApp resource = new EntornApp();

        EntornAppServiceImpl.DefaultLogsPerspectiveApplicator applicator =
                new EntornAppServiceImpl.DefaultLogsPerspectiveApplicator();

        applicator.applySingle(EntornApp.PERSPECTIVE_DEFAULT_LOGS, entity, resource);

        assertArrayEquals(
                new String[]{"es.caib..log", "server.log"},
                resource.getDefaultLogs()
        );
    }

    @Test
    @DisplayName("HistoricVersionsPerspectiveApplicator: crida al repo i mapeja resultats")
    void historicVersionsPerspectiveApplicator_aplicaHistoric_cridaRepoIMapeja() {
        // Given
        EntornAppHistEntity hist1 = new EntornAppHistEntity();
        hist1.setId(1L);
        hist1.setVersio("1.0.0");
        hist1.setRevisio("rev-a");
        hist1.setCanviVersio(true);
        EntornAppHistEntity hist2 = new EntornAppHistEntity();
        hist2.setId(2L);
        hist2.setVersio("1.1.0");
        hist2.setRevisio("rev-b");
        hist2.setCanviVersio(false);
        List<EntornAppHistEntity> historics = Arrays.asList(hist1, hist2);
        when(entornAppHistRepository.findByEntornAppOrderByDataDesc(entornAppEntity)).thenReturn(historics);
        EntornAppHist resource1 = new EntornAppHist();
        resource1.setId(1L);
        EntornAppHist resource2 = new EntornAppHist();
        resource2.setId(2L);
        when(objectMappingHelper.newInstanceMap(eq(hist1), eq(EntornAppHist.class))).thenReturn(resource1);
        when(objectMappingHelper.newInstanceMap(eq(hist2), eq(EntornAppHist.class))).thenReturn(resource2);
        EntornApp resource = new EntornApp();

        // When
        EntornAppServiceImpl.HistoricVersionsPerspectiveApplicator applicator =
                entornAppService.new HistoricVersionsPerspectiveApplicator();
        applicator.applySingle(EntornApp.PERSPECTIVE_HISTORICS_VERSIONS, entornAppEntity, resource);

        // Then
        verify(entornAppHistRepository).findByEntornAppOrderByDataDesc(entornAppEntity);
        verify(objectMappingHelper, times(2)).newInstanceMap(any(EntornAppHistEntity.class), eq(EntornAppHist.class));
        assertNotNull(resource.getEntornAppHistorics());
        assertEquals(2, resource.getEntornAppHistorics().size());
    }

    @Test
    @DisplayName("HistoricVersionsPerspectiveApplicator: quan no hi ha dades, retorna llista buida")
    void historicVersionsPerspectiveApplicator_senseDades_retornaLlistaBuida() {
        // Given
        when(entornAppHistRepository.findByEntornAppOrderByDataDesc(entornAppEntity))
                .thenReturn(Collections.emptyList());

        EntornApp resource = new EntornApp();
        EntornAppServiceImpl.HistoricVersionsPerspectiveApplicator applicator =
                entornAppService.new HistoricVersionsPerspectiveApplicator();

        // When
        applicator.applySingle(EntornApp.PERSPECTIVE_HISTORICS_VERSIONS, entornAppEntity, resource);

        // Then
        verify(entornAppHistRepository).findByEntornAppOrderByDataDesc(entornAppEntity);
        assertNotNull(resource.getEntornAppHistorics());
        assertTrue(resource.getEntornAppHistorics().isEmpty());
        verifyNoInteractions(objectMappingHelper);
    }

    @ParameterizedTest(name = "ExistsParameterAction: clau={0}, valor={1} → exists={2}")
    @MethodSource("existsParameterCases")
    @DisplayName("ExistsParameterAction: diferents combinacions de clau i valor")
    void existsParameterAction_casosParametritzats(String clau, String valor, boolean expectedExists) {
        // Given
        EntornApp.EntornAppExistsParameterAction params = new EntornApp.EntornAppExistsParameterAction();
        params.setParameterValue(clau);

        if (clau != null && !clau.isBlank()) {
            lenient().when(environment.getProperty(clau)).thenReturn(valor);
        }

        EntornAppServiceImpl.ExistsParameterAction action = new EntornAppServiceImpl.ExistsParameterAction(environment);

        // When
        EntornApp.ExistsParameterResponse result = action.exec(
                EntornApp.ENTORN_APP_ACTION_EXISTS_PARAMETER, entornAppEntity, params);

        // Then
        assertNotNull(result);
        assertEquals(expectedExists, result.isExists());
    }

    private static Stream<Arguments> existsParameterCases() {
        return Stream.of(
                // clau, valor, expectedExists
                Arguments.of(null, null, false),                    // null → false
                Arguments.of("   ", null, false),                   // blank → false
                Arguments.of("valid.key", "someValue", true),       // Propietat existeix → true
                Arguments.of("nonexistent.key", null, false),       // Propietat no existeix → false
                Arguments.of("empty.key", "   ", false)             // Propietat blank → false
        );
    }
}
