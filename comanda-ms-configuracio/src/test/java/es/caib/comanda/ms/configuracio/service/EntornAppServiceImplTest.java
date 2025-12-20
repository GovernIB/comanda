package es.caib.comanda.ms.configuracio.service;

import es.caib.comanda.client.EstadisticaServiceClient;
import es.caib.comanda.client.SalutServiceClient;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.AppIntegracio;
import es.caib.comanda.configuracio.logic.intf.model.AppSubsistema;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.service.ConfiguracioSchedulerService;
import es.caib.comanda.configuracio.logic.service.EntornAppServiceImpl;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.AppIntegracioEntity;
import es.caib.comanda.configuracio.persist.entity.AppSubsistemaEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.entity.IntegracioEntity;
import es.caib.comanda.configuracio.persist.repository.AppIntegracioRepository;
import es.caib.comanda.configuracio.persist.repository.ContextRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.SubsistemaRepository;
import es.caib.comanda.ms.logic.helper.CacheHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.helper.ResourceEntityMappingHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                                          RestTemplate restTemplate,
                                          ResourceEntityMappingHelper resourceEntityMappingHelper,
                                          ApplicationEventPublisher eventPublisher) {
            super(appIntegracioRepository, subsistemaRepository, contextRepository, entornAppRepository, appInfoHelper,
                    cacheHeper, schedulerService, restTemplate, resourceEntityMappingHelper, eventPublisher);
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
    private ResourceEntityMappingHelper resourceEntityMappingHelper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TestableEntornAppServiceImpl entornAppService;

    private EntornAppEntity entornAppEntity;
    private EntornApp entornAppResource;
    private List<AppIntegracioEntity> integracions;
    private List<AppSubsistemaEntity> subsistemes;

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
            restTemplate,
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
}