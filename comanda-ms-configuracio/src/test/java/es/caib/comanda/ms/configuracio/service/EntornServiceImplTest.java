package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.configuracio.logic.intf.model.Entorn;
import es.caib.comanda.configuracio.logic.service.EntornServiceImpl;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.projection.EntornPermissionQueryProjection;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.http.ResponseEntity;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.ENTORN_CACHE;

@ExtendWith(MockitoExtension.class)
public class EntornServiceImplTest {

    static class TestableEntornServiceImpl extends EntornServiceImpl {
        public TestableEntornServiceImpl(EntornRepository entornRepository,
                                         EntornAppRepository entornAppRepository,
                                         CacheHelper cacheHelper,
                                         AuthenticationHelper authenticationHelper,
                                         HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
                                         AclServiceClient aclServiceClient,
                                         EntornAppHelper entornAppHelper,
                                         ApplicationEventPublisher eventPublisher) {
            super(entornRepository, entornAppRepository, cacheHelper, authenticationHelper,
                httpAuthorizationHeaderHelper, aclServiceClient, entornAppHelper, eventPublisher);
        }

        @Override
        public void afterCreateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterCreateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterUpdateSave(EntornEntity entity, Entorn resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterDelete(EntornEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
            super.afterDelete(entity, answers);
        }
    }

    @Mock
    private EntornRepository entornRepository;

    @Mock
    private EntornAppRepository entornAppRepository;

    @Mock
    private CacheHelper cacheHelper;

    @Mock
    private AuthenticationHelper authenticationHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private EntornAppHelper entornAppHelper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TestableEntornServiceImpl entornService;

    private EntornEntity entornEntity;
    private Entorn entornResource;

    @BeforeEach
    void setUp() {
        entornService = new TestableEntornServiceImpl(
                entornRepository,
                entornAppRepository,
                cacheHelper,
                authenticationHelper,
                httpAuthorizationHeaderHelper,
                aclServiceClient,
                entornAppHelper,
                eventPublisher);

        // Setup test data
        entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setNom("Test Entorn");
        entornEntity.setEntornAppEntities(new HashSet<>());

        entornResource = new Entorn();
        entornResource.setId(1L);
        entornResource.setNom("Test Entorn");
    }

    private void stubAclContext(String... roles) {
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer test");
        when(authenticationHelper.isCurrentUserInRole(anyString())).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("anna");
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(roles);
    }

    @Test
    void testEntornServiceExists() {
        assertNotNull(entornService);
    }

    @Test
    void testAfterCreateSave() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        entornService.afterCreateSave(entornEntity, entornResource, answers, false);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.ENTORN_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(entornEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    @Test
    void testAfterUpdateSave() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        entornService.afterUpdateSave(entornEntity, entornResource, answers, false);

        verify(cacheHelper, times(1)).evictCacheItem(ENTORN_CACHE, entornEntity.getId().toString());

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.ENTORN_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(entornEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    @Test
    void testAfterDelete() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        entornService.afterDelete(entornEntity, answers);

        verify(cacheHelper, times(1)).evictCacheItem(ENTORN_CACHE, entornEntity.getId().toString());

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.ENTORN_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(entornEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    private EntornPermissionQueryProjection mockProjection(Long entornAppId, Long appId, Long entornId) {
        EntornPermissionQueryProjection projection = mock(EntornPermissionQueryProjection.class);
        lenient().when(projection.getEntornAppId()).thenAnswer(invocation -> entornAppId);
        lenient().when(projection.getAppId()).thenAnswer(invocation -> appId);
        lenient().when(projection.getEntornId()).thenAnswer(invocation -> entornId);
        return projection;
    }

    @Test
    void additionalSpringFilter_quanNoHiHaPermisos_retornaFiltreBuitValid() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(null));
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(entornAppRepository.findAllEntornPermissionQueryProjection()).thenReturn(Collections.emptySet());

        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(entornService, "additionalSpringFilter", null, null);

        assertEquals("id:0", result);
    }

    @Test
    void additionalSpringFilter_quanNomesHiHaPermisosPerApp_retornaEntornsDelsEntornsAppActiusDeLaApp() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Set.of("1")));
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Collections.emptySet()));

        EntornPermissionQueryProjection projection = mock(EntornPermissionQueryProjection.class);
        lenient().when(projection.getEntornAppId()).thenReturn(100L);
        lenient().when(projection.getAppId()).thenReturn(1L);
        lenient().when(projection.getEntornId()).thenReturn(10L);
        when(entornAppRepository.findAllEntornPermissionQueryProjection()).thenReturn(Set.of(projection));

        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(entornService, "additionalSpringFilter", null, null);

        assertEquals("id:10", result);
    }

    @Test
    void additionalSpringFilter_quanNomesHiHaPermisosPerEntornApp_retornaEntornsDelsEntornsAppPermesos() {
        stubAclContext("COM_USER");
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Set.of("100")));

        EntornPermissionQueryProjection projection = mock(EntornPermissionQueryProjection.class);
        lenient().when(projection.getEntornAppId()).thenReturn(100L);
        lenient().when(projection.getAppId()).thenReturn(99L);
        lenient().when(projection.getEntornId()).thenReturn(10L);
        when(entornAppRepository.findAllEntornPermissionQueryProjection()).thenReturn(Set.of(projection));

        String result = org.springframework.test.util.ReflectionTestUtils.invokeMethod(entornService, "additionalSpringFilter", null, null);

        assertEquals("id:10", result);
    }

    @Test
    void additionalSpringFilter_quanConsultaAcl_passaUsuariIRolsActuals() {
        stubAclContext("COM_USER", "COM_EXTRA");
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER", "COM_EXTRA")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(aclServiceClient.findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER", "COM_EXTRA")), eq("Bearer test")))
                .thenReturn(ResponseEntity.ok(Collections.emptySet()));
        when(entornAppRepository.findAllEntornPermissionQueryProjection()).thenReturn(Collections.emptySet());

        org.springframework.test.util.ReflectionTestUtils.invokeMethod(entornService, "additionalSpringFilter", null, null);

        verify(aclServiceClient).findIdsWithAnyPermission(eq(ResourceType.APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER", "COM_EXTRA")), eq("Bearer test"));
        verify(aclServiceClient).findIdsWithAnyPermission(eq(ResourceType.ENTORN_APP), eq(List.of(PermissionEnum.READ)), eq("anna"), eq(List.of("COM_USER", "COM_EXTRA")), eq("Bearer test"));
    }
}
