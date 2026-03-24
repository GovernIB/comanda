package es.caib.comanda.ms.configuracio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.mapper.AppExportMapper;
import es.caib.comanda.configuracio.logic.service.AppServiceImpl;
import es.caib.comanda.configuracio.logic.service.ConfiguracioSchedulerService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.APP_CACHE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppServiceImplTest {

    // Test subclass to expose protected methods
    static class TestableAppServiceImpl extends AppServiceImpl {
        
        public TestableAppServiceImpl(CacheHelper cacheHelper,
                                      ObjectMapper objectMapper,
                                      AppExportMapper appExportMapper,
                                      AppRepository appRepository,
                                      EntornRepository entornRepository,
                                      EntornAppRepository entornAppRepository,
                                      AuthenticationHelper authenticationHelper,
                                      HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
                                      AclServiceClient aclServiceClient) {
            super(cacheHelper, objectMapper, appExportMapper, appRepository, entornRepository, entornAppRepository,
                    authenticationHelper, httpAuthorizationHeaderHelper, aclServiceClient);
        }
        
        @Override
        public void afterConversion(AppEntity entity, App resource) {
            super.afterConversion(entity, resource);
        }

        @Override
        public void afterUpdateSave(AppEntity entity, App resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }

        public String exposedAdditionalSpringFilter() {
            return super.additionalSpringFilter(null, null);
        }
    }

    @Mock
    private CacheHelper cacheHelper;
    
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AppExportMapper appExportMapper;

    @Mock
    private AppRepository appRepository;

    @Mock
    private EntornRepository entornRepository;

    @Mock
    private EntornAppRepository entornAppRepository;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    private TestableAppServiceImpl appService;

    private AppEntity appEntity;
    private App appResource;
    private EntornAppEntity entornAppEntity;
    private EntornEntity entornEntity;

    @BeforeEach
    void setUp() {
        // Initialize the service with mocked dependencies
        appService = new TestableAppServiceImpl(
                cacheHelper,
                objectMapper,
                appExportMapper,
                appRepository,
                entornRepository,
                entornAppRepository,
                authenticationHelper,
                httpAuthorizationHeaderHelper,
                aclServiceClient);
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer test");
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("anna");
        when(authenticationHelper.getCurrentUserRoles()).thenReturn(new String[]{"COM_USER"});
        
        // Setup test data
        appEntity = new AppEntity();
        appEntity.setId(1L);
        appEntity.setNom("Test App");

        entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");

        entornAppEntity = new EntornAppEntity();
        entornAppEntity.setId(1L);
        entornAppEntity.setApp(appEntity);
        entornAppEntity.setEntorn(entornEntity);
        entornAppEntity.setInfoUrl("http://test.com/info");
        entornAppEntity.setVersio("1.0.0");
        entornAppEntity.setActiva(true);

        List<EntornAppEntity> entornApps = new ArrayList<>();
        entornApps.add(entornAppEntity);
        appEntity.setEntornApps(entornApps);

        appResource = new App();
        appResource.setId(1L);
        appResource.setNom("Test App");
    }

    @Test
    void testAfterConversion() {
        // Test that afterConversion correctly maps EntornAppEntity to EntornApp
        appService.afterConversion(appEntity, appResource);

        assertNotNull(appResource.getEntornApps());
        assertEquals(1, appResource.getEntornApps().size());

        EntornApp entornApp = appResource.getEntornApps().get(0);
        assertEquals(1L, entornApp.getId());
        assertEquals(ResourceReference.toResourceReference(appEntity.getId(), appEntity.getNom()), entornApp.getApp());
        assertEquals(ResourceReference.toResourceReference(entornEntity.getId(), entornEntity.getNom()), entornApp.getEntorn());
        assertEquals("http://test.com/info", entornApp.getInfoUrl());
        assertEquals("1.0.0", entornApp.getVersio());
        assertTrue(entornApp.isActiva());
    }

    @Test
    void testAfterUpdateSave() {
        // Test that afterUpdateSave schedules tasks for each EntornApp
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        appService.afterUpdateSave(appEntity, appResource, answers, false);

        // Verify that cacheHelper.evictCacheItem was called for the App
        verify(cacheHelper, times(1)).evictCacheItem(APP_CACHE, appEntity.getId().toString());
    }

    @Test
    void testAfterUpdateSaveWithNoEntornApps() {
        // Test that afterUpdateSave doesn't schedule tasks when there are no EntornApps
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        // Set empty EntornApps list
        appEntity.setEntornApps(new ArrayList<>());

        appService.afterUpdateSave(appEntity, appResource, answers, false);

        verify(cacheHelper, times(1)).evictCacheItem(APP_CACHE, appEntity.getId().toString());
    }

    @Test
    void additionalSpringFilter_quanLusuariEsAdmin_noAplicaFiltreAcl() {
        when(authenticationHelper.isCurrentUserInRole("COM_ADMIN")).thenReturn(true);

        String result = appService.exposedAdditionalSpringFilter();

        assertNull(result);
        verifyNoInteractions(aclServiceClient);
    }

    @Test
    void additionalSpringFilter_quanLusuariSenseRols_tePermisPerAppIEntornApp_filtraAppsVisibles() {
        AppEntity secondAppEntity = new AppEntity();
        secondAppEntity.setId(2L);
        EntornAppEntity permittedEntornApp = new EntornAppEntity();
        permittedEntornApp.setId(11L);
        permittedEntornApp.setApp(secondAppEntity);
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(org.springframework.http.ResponseEntity.ok(Set.of(1L)));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(org.springframework.http.ResponseEntity.ok(Set.of(11L)));
        when(entornAppRepository.findAllById(Set.of(11L))).thenReturn(List.of(permittedEntornApp));

        String result = appService.exposedAdditionalSpringFilter();

        assertEquals("id:1 or id:2", result);
    }

    @Test
    void additionalSpringFilter_quanAclNoRetornaCapId_retornaFiltreQueNoTornaResultats() {
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(org.springframework.http.ResponseEntity.ok(null));
        when(aclServiceClient.findIdsWithAnyPermission(
                eq(ResourceType.ENTORN_APP),
                eq(Collections.singletonList(PermissionEnum.READ)),
                eq("anna"),
                eq(List.of("COM_USER")),
                eq("Bearer test"))).thenReturn(org.springframework.http.ResponseEntity.ok(Collections.emptySet()));

        String result = appService.exposedAdditionalSpringFilter();

        assertEquals("id:0", result);
    }
}
