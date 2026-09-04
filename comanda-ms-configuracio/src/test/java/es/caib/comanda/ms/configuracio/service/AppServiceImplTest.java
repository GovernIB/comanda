package es.caib.comanda.ms.configuracio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
import es.caib.comanda.configuracio.logic.helper.EntornAppHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.mapper.AppExportMapper;
import es.caib.comanda.configuracio.logic.service.AppServiceImpl;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.config.HazelCastCacheConfig;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.sse.ComandaSseEventTypes;
import es.caib.comanda.ms.sse.ComandaSsePublishRequest;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static es.caib.comanda.ms.logic.config.HazelCastCacheConfig.APP_CACHE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
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
                                      EntornAppHelper entornAppHelper,
                                      HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper,
                                      AclServiceClient aclServiceClient,
                                      ApplicationEventPublisher eventPublisher) {
            super(cacheHelper, objectMapper, appExportMapper, appRepository, entornRepository, entornAppRepository,
                entornAppHelper, httpAuthorizationHeaderHelper, aclServiceClient, eventPublisher);
        }

        @Override
        public void afterConversion(AppEntity entity, App resource) {
            super.afterConversion(entity, resource);
        }

        @Override
        public void afterCreateSave(AppEntity entity, App resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterCreateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void beforeUpdateEntity(AppEntity entity, App resource, Map<String, AnswerRequiredException.AnswerValue> answers) throws ResourceNotUpdatedException {
            super.beforeUpdateEntity(entity, resource, answers);
        }

        @Override
        public void afterUpdateSave(AppEntity entity, App resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }

        @Override
        public void afterDelete(AppEntity entity, Map<String, AnswerRequiredException.AnswerValue> answers) {
            super.afterDelete(entity, answers);
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
    private EntornAppHelper entornAppHelper;

    @Mock
    private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;

    @Mock
    private AclServiceClient aclServiceClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

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
                entornAppHelper,
                httpAuthorizationHeaderHelper,
                aclServiceClient,
                eventPublisher);

        // Setup test data
        appEntity = new AppEntity();
        appEntity.setId(1L);
        appEntity.setCodi("APP1");
        appEntity.setNom("Test App");

        entornEntity = new EntornEntity();
        entornEntity.setId(1L);
        entornEntity.setCodi("ENT1");
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
    }

    @Test
    void testAfterCreateSave() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        appService.afterCreateSave(appEntity, appResource, answers, false);

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.APP_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(appEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    @Test
    void testBeforeUpdateEntity_codiChanged() throws Exception {
        appEntity.setCodi("OLD_CODE");
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        App resourceWithNewCodi = new App();
        resourceWithNewCodi.setCodi("NEW_CODE");

        appService.beforeUpdateEntity(appEntity, resourceWithNewCodi, answers);

        verify(cacheHelper, times(1)).evictCacheItem(HazelCastCacheConfig.APP_BY_CODI_CACHE, "OLD_CODE");
    }

    @Test
    void testBeforeUpdateEntity_codiNotChanged() throws Exception {
        appEntity.setCodi("SAME_CODE");
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        App resourceWithSameCodi = new App();
        resourceWithSameCodi.setCodi("SAME_CODE");

        appService.beforeUpdateEntity(appEntity, resourceWithSameCodi, answers);

        verify(cacheHelper, never()).evictCacheItem(eq(HazelCastCacheConfig.APP_BY_CODI_CACHE), anyString());
    }

    @Test
    void testAfterUpdateSave() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        appService.afterUpdateSave(appEntity, appResource, answers, false);

        verify(cacheHelper, times(1)).evictAppCacheItem(appEntity.getId(), appEntity.getCodi());

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.APP_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(appEntity.getId(), captor.getValue().getEvent().getPayload());
    }

    @Test
    void testAfterDelete() {
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        appService.afterDelete(appEntity, answers);

        verify(cacheHelper, times(1)).evictAppCacheItem(appEntity.getId(), appEntity.getCodi());
        verify(entornAppHelper, times(1)).logicAfterDelete(entornAppEntity.getId());

        ArgumentCaptor<ComandaSsePublishRequest> captor = ArgumentCaptor.forClass(ComandaSsePublishRequest.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(ComandaSseEventTypes.APP_CHANGED, captor.getValue().getEvent().getType());
        assertEquals(appEntity.getId(), captor.getValue().getEvent().getPayload());
    }
}
