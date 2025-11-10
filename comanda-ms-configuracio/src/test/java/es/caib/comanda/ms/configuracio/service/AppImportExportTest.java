package es.caib.comanda.ms.configuracio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.configuracio.logic.helper.AppInfoHelper;
import es.caib.comanda.configuracio.logic.intf.model.App;
import es.caib.comanda.configuracio.logic.intf.model.App.AppImportForm;
import es.caib.comanda.configuracio.logic.intf.model.export.AppExport;
import es.caib.comanda.configuracio.logic.mapper.AppExportMapper;
import es.caib.comanda.configuracio.logic.service.AppServiceImpl;
import es.caib.comanda.configuracio.logic.service.ConfiguracioSchedulerService;
import es.caib.comanda.configuracio.persist.entity.AppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornAppEntity;
import es.caib.comanda.configuracio.persist.entity.EntornEntity;
import es.caib.comanda.configuracio.persist.repository.AppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornAppRepository;
import es.caib.comanda.configuracio.persist.repository.EntornRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppImportExportTest {

    static class TestableAppServiceImpl extends AppServiceImpl {
        public TestableAppServiceImpl(AppInfoHelper appInfoHelper,
                                      ConfiguracioSchedulerService schedulerService,
                                      es.caib.comanda.ms.logic.helper.CacheHelper cacheHelper,
                                      ObjectMapper objectMapper,
                                      AppExportMapper appExportMapper,
                                      AppRepository appRepository,
                                      EntornRepository entornRepository,
                                      EntornAppRepository entornAppRepository) {
            super(appInfoHelper, schedulerService, cacheHelper, objectMapper, appExportMapper, appRepository, entornRepository, entornAppRepository);
        }
        // Simplify mapping to avoid needing ObjectMappingHelper in unit tests
        @Override
        protected App entityToResource(AppEntity entity) {
            App r = new App();
            r.setId(entity.getId());
            r.setNom(entity.getNom());
            return r;
        }
    }

    @Mock private AppInfoHelper appInfoHelper;
    @Mock private ConfiguracioSchedulerService schedulerService;
    @Mock private es.caib.comanda.ms.logic.helper.CacheHelper cacheHelper;
    @Mock private AppExportMapper appExportMapper;
    @Mock private AppRepository appRepository;
    @Mock private EntornRepository entornRepository;
    @Mock private EntornAppRepository entornAppRepository;

    private ObjectMapper realObjectMapper;
    private TestableAppServiceImpl service;

    @BeforeEach
    void setup() {
        realObjectMapper = new ObjectMapper();
        service = new TestableAppServiceImpl(
                appInfoHelper,
                schedulerService,
                cacheHelper,
                realObjectMapper,
                appExportMapper,
                appRepository,
                entornRepository,
                entornAppRepository);
    }

    // ---------- EXPORT TESTS ----------

    @Test
    void export_generateData_withEntity_returnsSingleMappedExport() throws Exception {
        AppEntity entity = new AppEntity();
        entity.setId(10L);
        entity.setCodi("APP1");
        entity.setNom("Aplicacio 1");

        AppExport mapped = AppExport.builder().codi("APP1").nom("Aplicacio 1").build();
        when(appExportMapper.toExport(entity)).thenReturn(mapped);

        AppServiceImpl.AppExportReportGenerator generator = service.new AppExportReportGenerator();
        List<AppExport> data = generator.generateData(App.APP_EXPORT, entity, null);

        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(mapped, data.get(0));
        verify(appExportMapper, times(1)).toExport(entity);
    }

    @Test
    void export_generateData_withoutEntity_mapsAllEntities() throws Exception {
        // Prepare list of entities
        AppEntity e1 = new AppEntity(); e1.setId(1L); e1.setCodi("A1"); e1.setNom("A1");
        AppEntity e2 = new AppEntity(); e2.setId(2L); e2.setCodi("A2"); e2.setNom("A2");
        List<AppEntity> entities = Arrays.asList(e1, e2);

        // Inject mock entityRepository into base service via reflection
        injectEntityRepositoryMock(service, entities);

        List<AppExport> mappedList = Arrays.asList(
                AppExport.builder().codi("A1").nom("A1").build(),
                AppExport.builder().codi("A2").nom("A2").build()
        );
        when(appExportMapper.toExport(entities)).thenReturn(mappedList);

        AppServiceImpl.AppExportReportGenerator generator = service.new AppExportReportGenerator();
        List<AppExport> data = generator.generateData(App.APP_EXPORT, null, null);

        assertEquals(2, data.size());
        assertEquals("A1", data.get(0).getCodi());
        assertEquals("A2", data.get(1).getCodi());
        verify(appExportMapper, times(1)).toExport(entities);
    }

    @Test
    void export_generateFile_producesJsonFile() {
        AppServiceImpl.AppExportReportGenerator generator = service.new AppExportReportGenerator();
        List<AppExport> data = Collections.singletonList(
                AppExport.builder().codi("APPX").nom("App X").build()
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DownloadableFile file = generator.generateFile(App.APP_EXPORT, data, ReportFileType.JSON, out);

        assertNotNull(file);
        assertEquals("App X.json", file.getName());
        assertEquals("application/json", file.getContentType());
        assertNotNull(file.getContent());
        assertTrue(file.getContent().length > 0);
        // Validate JSON roundtrip
        assertDoesNotThrow(() -> realObjectMapper.readTree(file.getContent()));
    }

    // ---------- IMPORT TESTS ----------

    @Test
    void import_newApp_createsEntitiesAndSchedulesTasks() {
        String json = "[{\"codi\":\"APP1\",\"nom\":\"App 1\",\"activa\":true,\"entornApps\":[{\"entornCodi\":\"DEV\",\"entornNom\":\"Desenvolupament\",\"infoUrl\":\"http://info\",\"activa\":true,\"salutUrl\":\"http://health\"}]}]";

        // App doesn't exist
        when(appRepository.findByCodi("APP1")).thenReturn(null);
        // Save and flush assigns ID
        when(appRepository.saveAndFlush(any(AppEntity.class))).thenAnswer(inv -> {
            AppEntity ae = inv.getArgument(0);
            if (ae.getId() == null) ae.setId(100L);
            // Ensure entornApps list exists so scheduler loop runs
            if (ae.getEntornApps() == null) {
                ae.setEntornApps(new ArrayList<>());
            }
            return ae;
        });
        doAnswer(inv -> { return null; }).when(appRepository).refresh(any(AppEntity.class));

        // Entorn DEV doesn't exist initially
        when(entornRepository.findByCodi("DEV")).thenReturn(null);
        when(entornRepository.saveAndFlush(any(EntornEntity.class))).thenAnswer(inv -> {
            EntornEntity ent = inv.getArgument(0);
            ent.setId(10L);
            return ent;
        });
        when(entornAppRepository.findByEntornIdAndAppId(10L, 100L)).thenReturn(Optional.empty());
        when(entornAppRepository.save(any(EntornAppEntity.class))).thenAnswer(inv -> {
            EntornAppEntity saved = inv.getArgument(0);
            if (saved.getApp() != null) {
                AppEntity ae = saved.getApp();
                if (ae.getEntornApps() == null) {
                    ae.setEntornApps(new ArrayList<>());
                }
                if (!ae.getEntornApps().contains(saved)) {
                    ae.getEntornApps().add(saved);
                }
            }
            return saved;
        });

        AppServiceImpl.AppImportActionExecutor executor = service.new AppImportActionExecutor();
        AppImportForm params = new AppImportForm(json, null);
        AppServiceImpl.AppImportResult result = executor.exec(App.APP_IMPORT, null, params);

        assertNotNull(result);
        assertNotNull(result.getApps());
        assertEquals(1, result.getApps().size());
        assertEquals(100L, result.getApps().get(0).getId());

        // Verify side effects
        verify(schedulerService, atLeastOnce()).programarTasca(any(EntornAppEntity.class));
        verify(appInfoHelper, atLeastOnce()).programarTasquesSalutEstadistica(any(EntornAppEntity.class));
        verify(cacheHelper, times(1)).evictCacheItem(anyString(), eq("100"));
    }

    @Test
    void import_existingApp_withoutDecision_throwsAnswerRequired() {
        String json = "{\"codi\":\"APP1\",\"nom\":\"App 1\",\"activa\":true}";
        AppEntity existing = new AppEntity(); existing.setId(5L); existing.setCodi("APP1");
        when(appRepository.findByCodi("APP1")).thenReturn(existing);

        AppServiceImpl.AppImportActionExecutor executor = service.new AppImportActionExecutor();
        AppImportForm params = new AppImportForm(json, null);

        AnswerRequiredException ex = assertThrows(AnswerRequiredException.class,
                () -> executor.exec(App.APP_IMPORT, null, params));
        assertEquals("app-import-decision", ex.getAnswerCode());
        assertNotNull(ex.getAvailableAnswers());
        assertEquals(3, ex.getAvailableAnswers().size());
    }

    @Test
    void import_existingApp_overwrite_deletesMissingAndSavesPresent() {
        String json = "{\"codi\":\"APP1\",\"nom\":\"App 1\",\"activa\":true,\"entornApps\":[{\"entornCodi\":\"E1\",\"infoUrl\":\"http://i1\",\"activa\":true,\"salutUrl\":\"http://h1\"}]}";

        // Existing app with two entorns (E1, E2)
        AppEntity existing = new AppEntity(); existing.setId(7L); existing.setCodi("APP1"); existing.setNom("App 1");
        EntornEntity ent1 = new EntornEntity(); ent1.setId(11L); ent1.setCodi("E1"); ent1.setNom("E1");
        EntornEntity ent2 = new EntornEntity(); ent2.setId(12L); ent2.setCodi("E2"); ent2.setNom("E2");
        EntornAppEntity eae1 = new EntornAppEntity(); eae1.setId(101L); eae1.setApp(existing); eae1.setEntorn(ent1);
        EntornAppEntity eae2 = new EntornAppEntity(); eae2.setId(102L); eae2.setApp(existing); eae2.setEntorn(ent2);
        existing.setEntornApps(new ArrayList<>(Arrays.asList(eae1, eae2)));

        when(appRepository.findByCodi("APP1")).thenReturn(existing);
        when(appRepository.saveAndFlush(existing)).thenReturn(existing);
        doAnswer(inv -> null).when(appRepository).refresh(existing);
        // Delete should be called for eae2 (E2 not present in import)
        doNothing().when(entornAppRepository).delete(eae2);
        when(entornRepository.findByCodi("E1")).thenReturn(ent1);
        when(entornAppRepository.save(any(EntornAppEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AppServiceImpl.AppImportActionExecutor executor = service.new AppImportActionExecutor();
        AppImportForm params = new AppImportForm(json, "OVERWRITE");
        AppServiceImpl.AppImportResult result = executor.exec(App.APP_IMPORT, null, params);

        assertNotNull(result);
        verify(entornAppRepository, times(1)).delete(eae2);
        verify(entornAppRepository, atLeastOnce()).save(any(EntornAppEntity.class));
    }

    @Test
    void import_existingApp_combine_addsOnlyNewAssociations() {
        String json = "{\"codi\":\"APP1\",\"nom\":\"App 1\",\"activa\":true,\"entornApps\":[{\"entornCodi\":\"E1\",\"infoUrl\":\"http://i1\",\"activa\":true,\"salutUrl\":\"http://h1\"},{\"entornCodi\":\"E2\",\"infoUrl\":\"http://i2\",\"activa\":true,\"salutUrl\":\"http://h2\"}]}";

        AppEntity existing = new AppEntity(); existing.setId(7L); existing.setCodi("APP1");
        EntornEntity ent1 = new EntornEntity(); ent1.setId(11L); ent1.setCodi("E1"); ent1.setNom("E1");
        EntornEntity ent2 = new EntornEntity(); ent2.setId(12L); ent2.setCodi("E2"); ent2.setNom("E2");
        EntornAppEntity eae1 = new EntornAppEntity(); eae1.setId(101L); eae1.setApp(existing); eae1.setEntorn(ent1);
        existing.setEntornApps(new ArrayList<>(Collections.singletonList(eae1)));

        when(appRepository.findByCodi("APP1")).thenReturn(existing);
        when(appRepository.saveAndFlush(existing)).thenReturn(existing);
        doAnswer(inv -> null).when(appRepository).refresh(existing);

        when(entornRepository.findByCodi(anyString())).thenReturn(ent1);
        when(entornRepository.findByCodi("E2")).thenReturn(ent2);
        when(entornAppRepository.findByEntornIdAndAppId(11L, 7L)).thenReturn(Optional.of(eae1));
        when(entornAppRepository.findByEntornIdAndAppId(12L, 7L)).thenReturn(Optional.empty());
        when(entornAppRepository.save(any(EntornAppEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AppServiceImpl.AppImportActionExecutor executor = service.new AppImportActionExecutor();
        AppImportForm params = new AppImportForm(json, "COMBINE");
        AppServiceImpl.AppImportResult result = executor.exec(App.APP_IMPORT, null, params);

        assertNotNull(result);
        // Only one new save for E2
        verify(entornAppRepository, times(1)).save(argThat(e -> ((EntornAppEntity)e).getEntorn().getCodi().equals("E2")));
        // No delete called
        verify(entornAppRepository, never()).delete(any(EntornAppEntity.class));
    }

    @Test
    void import_existingApp_skip_doesNotModifyAssociations() {
        String json = "{\"codi\":\"APP1\",\"nom\":\"App 1\",\"activa\":true,\"entornApps\":[{\"entornCodi\":\"E1\",\"infoUrl\":\"http://i1\",\"activa\":true,\"salutUrl\":\"http://h1\"}]}";

        AppEntity existing = new AppEntity(); existing.setId(7L); existing.setCodi("APP1");
        EntornEntity ent1 = new EntornEntity(); ent1.setId(11L); ent1.setCodi("E1"); ent1.setNom("E1");
        EntornAppEntity eae1 = new EntornAppEntity(); eae1.setId(101L); eae1.setApp(existing); eae1.setEntorn(ent1);
        existing.setEntornApps(new ArrayList<>(Collections.singletonList(eae1)));

        when(appRepository.findByCodi("APP1")).thenReturn(existing);
        when(appRepository.saveAndFlush(existing)).thenReturn(existing);
        doAnswer(inv -> null).when(appRepository).refresh(existing);

        AppServiceImpl.AppImportActionExecutor executor = service.new AppImportActionExecutor();
        AppImportForm params = new AppImportForm(json, "SKIP");
        AppServiceImpl.AppImportResult result = executor.exec(App.APP_IMPORT, null, params);

        assertNotNull(result);
        verify(entornAppRepository, never()).save(any());
        // Also ensure no delete took place
        verify(entornAppRepository, never()).delete(any());
    }

    // --- helpers ---
    private void injectEntityRepositoryMock(AppServiceImpl target, List<AppEntity> entities) throws Exception {
        // Create a simple BaseRepository mock that returns the provided list on findAll()
        es.caib.comanda.ms.persist.repository.BaseRepository<AppEntity, Long> baseRepo = mock(es.caib.comanda.ms.persist.repository.BaseRepository.class);
        when(baseRepo.findAll()).thenReturn(entities);
        Field f = ReflectionUtils.findField(es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.class, "entityRepository");
        assertNotNull(f);
        f.setAccessible(true);
        f.set(target, baseRepo);
    }
}
