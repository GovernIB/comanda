package es.caib.comanda.ms.configuracio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.EntornApp;
import es.caib.comanda.configuracio.logic.mapper.AppExportMapper;
import es.caib.comanda.configuracio.logic.service.AppServiceImpl;
import es.caib.comanda.configuracio.logic.service.ConfiguracioSchedulerService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppServiceImplTest {

    // Test subclass to expose protected methods
    static class TestableAppServiceImpl extends AppServiceImpl {
        
        public TestableAppServiceImpl(AppInfoHelper appInfoHelper,
                                      ConfiguracioSchedulerService schedulerService,
                                      ObjectMapper objectMapper,
                                      AppExportMapper appExportMapper) {
            super(appInfoHelper, schedulerService, objectMapper, appExportMapper);
        }
        
        @Override
        public void afterConversion(AppEntity entity, App resource) {
            super.afterConversion(entity, resource);
        }

        @Override
        public void afterUpdateSave(AppEntity entity, App resource, Map<String, AnswerRequiredException.AnswerValue> answers, boolean anyOrderChanged) {
            super.afterUpdateSave(entity, resource, answers, anyOrderChanged);
        }
    }

    @Mock
    private AppInfoHelper appInfoHelper;

    @Mock
    private ConfiguracioSchedulerService schedulerService;
    
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AppExportMapper appExportMapper;

    private TestableAppServiceImpl appService;

    private AppEntity appEntity;
    private App appResource;
    private EntornAppEntity entornAppEntity;
    private EntornEntity entornEntity;

    @BeforeEach
    void setUp() {
        // Initialize the service with mocked dependencies
        appService = new TestableAppServiceImpl(appInfoHelper, schedulerService, objectMapper, appExportMapper);
        
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

        // Verify that schedulerService.programarTasca was called for the EntornApp
        verify(schedulerService, times(1)).programarTasca(entornAppEntity);

        // Verify that appInfoHelper.programarTasquesSalutEstadistica was called for the EntornApp
        verify(appInfoHelper, times(1)).programarTasquesSalutEstadistica(entornAppEntity);
    }

    @Test
    void testAfterUpdateSaveWithNoEntornApps() {
        // Test that afterUpdateSave doesn't schedule tasks when there are no EntornApps
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        // Set empty EntornApps list
        appEntity.setEntornApps(new ArrayList<>());

        appService.afterUpdateSave(appEntity, appResource, answers, false);

        // Verify that schedulerService.programarTasca was not called
        verify(schedulerService, never()).programarTasca(any());

        // Verify that appInfoHelper.programarTasquesSalutEstadistica was not called
        verify(appInfoHelper, never()).programarTasquesSalutEstadistica(any());
    }
}
