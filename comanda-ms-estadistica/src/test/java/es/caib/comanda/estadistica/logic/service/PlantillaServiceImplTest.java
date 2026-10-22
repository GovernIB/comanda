package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.PaletaHelper;
import es.caib.comanda.estadistica.logic.intf.model.paleta.*;
import es.caib.comanda.estadistica.persist.entity.paleta.*;
import es.caib.comanda.estadistica.persist.repository.DashboardTemplatePaletteGroupRepository;
import es.caib.comanda.estadistica.persist.repository.PaletaRepository;
import es.caib.comanda.estadistica.persist.repository.WidgetStylePropertyRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import es.caib.comanda.estadistica.logic.helper.DashboardImportHelper;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.PlantillaExport;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.estadistica.logic.service.PlantillaServiceImpl.PlantillaImportParams;
import es.caib.comanda.estadistica.logic.service.PlantillaServiceImpl.PlantillaImportResult;
import es.caib.comanda.ms.logic.service.BaseMutableResourceService.ActionExecutor;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FileReference;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.ReportGenerator;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a PlantillaServiceImpl")
class PlantillaServiceImplTest {

    @Mock private PaletaRepository paletaRepository;
    @Mock private PaletaHelper paletaHelper;
    @Mock private DashboardTemplatePaletteGroupRepository paletteGroupRepository;
    @Mock private WidgetStylePropertyRepository stylePropertyRepository;
    @Mock private EstadisticaWidgetHelper estadisticaWidgetHelper;
    @Mock private DashboardImportHelper dashboardImportHelper;
    @Mock private DashboardExportMapper dashboardExportMapper;
    @Spy private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @InjectMocks private PlantillaServiceImpl plantillaService;

    @Captor private ArgumentCaptor<PaletaEntity> paletaEntityCaptor;

    private Plantilla plantillaResource;
    private PlantillaEntity plantillaEntity;
    private Map<String, AnswerRequiredException.AnswerValue> answers;

    @BeforeEach
    void setUp() {
        plantillaResource = new Plantilla();
        plantillaResource.setNom("Plantilla Test");

        plantillaEntity = new PlantillaEntity();
        plantillaEntity.setId(2L);
        plantillaEntity.setNom("Plantilla Test");

        answers = new HashMap<>();
    }

    // ========================================================================
    // TESTOS EXISTENTS (Mantinguts i polits)
    // ========================================================================

    @Test
    @DisplayName("beforeCreateSave: crida syncTemplate i no llança excepció")
    void beforeCreateSave_quanTotCorrecte_noLlancaExcepcio() {
        // Arrange
        Paleta paletteResource = crearPaletaResource("910001", "Test Widget");
        plantillaResource.setPaletes(Collections.singletonList(paletteResource));
        PaletaEntity paletteEntity = crearPaletaEntity(910001L, "Test Widget");

        when(paletaRepository.findById(910001L)).thenReturn(Optional.of(paletteEntity));
        when(paletaRepository.saveAndFlush(paletteEntity)).thenReturn(paletteEntity);
        doNothing().when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatCode(() -> plantillaService.beforeCreateSave(plantillaEntity, plantillaResource, answers))
            .doesNotThrowAnyException();

        verify(paletaHelper).syncColors(eq(paletteEntity), eq(paletteResource));
    }

    @Test
    @DisplayName("beforeCreateSave: captura excepció i la converteix a ResourceNotCreatedException")
    void beforeCreateSave_quanSyncTemplateLlancaExcepcio_converteixExcepcio() {
        // Arrange
        doThrow(new RuntimeException("Error de sincronització")).when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> plantillaService.beforeCreateSave(plantillaEntity, plantillaResource, answers))
            .isInstanceOf(ResourceNotCreatedException.class)
            .hasMessageContaining("Error de sincronització");
    }

    @Test
    @DisplayName("beforeUpdateSave: captura excepció i la converteix a ResourceNotUpdatedException")
    void beforeUpdateSave_quanSyncTemplateLlancaExcepcio_converteixExcepcio() {
        // Arrange
        doThrow(new RuntimeException("Error en actualització")).when(paletaHelper).syncColors(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> plantillaService.beforeUpdateSave(plantillaEntity, plantillaResource, answers))
            .isInstanceOf(ResourceNotUpdatedException.class)
            .hasMessageContaining("Error en actualització")
            .hasMessageContaining(String.valueOf(plantillaEntity.getId()));
    }

    @Test
    @DisplayName("afterUpdateSave: invalida la cache d'estil resolt dels dashboards que utilitzen la plantilla")
    void afterUpdateSave_quanEsModificaLaPlantilla_llavorsInvalidaLaCacheDEstil() {
        // Act
        plantillaService.afterUpdateSave(plantillaEntity, plantillaResource, answers, false);

        // Assert
        verify(estadisticaWidgetHelper).clearDashboardWidgetCacheByPlantilla(plantillaEntity.getId());
    }

    @Test
    @DisplayName("afterConversion: popula valors per defecte quan no hi ha dades")
    void afterConversion_quanNoHiHaDadesExistentes_populaValorsPerDefecte() {
        // Arrange
        when(paletaRepository.findAllByOrderByNomAscIdAsc()).thenReturn(Collections.emptyList());

        // Act
        plantillaService.afterConversion(plantillaEntity, plantillaResource);

        // Assert
        assertThat(plantillaResource.getPaletes()).isNotEmpty();
        assertThat(plantillaResource.getPaletteGroups()).hasSize(4);
        assertThat(plantillaResource.getStyleProperties()).isNotEmpty();
    }

    @Test
    @DisplayName("afterConversion: fusiona propietats d'estil proporcionades amb les per defecte")
    void afterConversion_quanHiHaPropietatsPersonalitzades_llavorsFusionaCorrectament() {
        // Arrange
        Plantilla resource = new Plantilla();
        resource.setColors(new HashMap<>());

        WidgetStylePropertyEntity customProp = new WidgetStylePropertyEntity();
        customProp.setScope(WidgetStyleScope.COMMON);
        customProp.setPropertyName("colorFons");
        customProp.setScalarValue("#FF0000");
        plantillaEntity.setStyleProperties(Collections.singletonList(customProp));

        when(paletaRepository.findAllByOrderByNomAscIdAsc()).thenReturn(Collections.emptyList());

        // Act
        plantillaService.afterConversion(plantillaEntity, resource);

        // Assert
        assertThat(resource.getStyleProperties()).anyMatch(p ->
            p.getScope() == WidgetStyleScope.COMMON &&
                p.getPropertyName().equals("colorFons") &&
                p.getScalarValue().equals("#FF0000")
        );
    }

    @Test
    @DisplayName("savePaletteResources: actualitza paleta existent quan arriba amb clientId numèric")
    void savePaletteResources_quanClientIdNumeric_actualitzaPaletaExistent() {
        // Arrange
        Paleta palette = crearPaleta("910001", "Institucional clar widget", "#ffffff");
        PaletaEntity existing = crearPaletaEntity(910001L, "Institucional clar widget");

        when(paletaRepository.findById(910001L)).thenReturn(Optional.of(existing));
        when(paletaRepository.saveAndFlush(existing)).thenReturn(existing);

        // Act
        Map<String, PaletaEntity> result = invokeSavePaletteResources(Collections.singletonList(palette));

        // Assert
        assertThat(result).containsEntry("910001", existing);
        verify(paletaHelper).syncColors(eq(existing), eq(palette));
        verify(paletaRepository).saveAndFlush(existing);
    }

    // ========================================================================
    // NOUS TESTOS PER A COBERTURA > 90%
    // ========================================================================

    @Test
    @DisplayName("syncStyleProperties: actualitza existents, crea noves i elimina les obsoletes")
    void syncStyleProperties_quanHiHaCanvis_llavorsSincronitzaCorrectament() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        entity.setId(1L);

        WidgetStylePropertyEntity existingToUpdate = new WidgetStylePropertyEntity();
        existingToUpdate.setScope(WidgetStyleScope.COMMON);
        existingToUpdate.setPropertyName("colorFons");

        WidgetStylePropertyEntity existingToDelete = new WidgetStylePropertyEntity();
        existingToDelete.setScope(WidgetStyleScope.COMMON);
        existingToDelete.setPropertyName("colorText");

        when(stylePropertyRepository.findByPlantillaId(1L))
            .thenReturn(Arrays.asList(existingToUpdate, existingToDelete));

        List<WidgetStyleProperty> provided = new ArrayList<>();
        WidgetStyleProperty propToUpdate = new WidgetStyleProperty();
        propToUpdate.setScope(WidgetStyleScope.COMMON);
        propToUpdate.setPropertyName("colorFons");
        propToUpdate.setScalarValue("#FF0000");
        provided.add(propToUpdate);

        WidgetStyleProperty propToCreate = new WidgetStyleProperty();
        propToCreate.setScope(WidgetStyleScope.SIMPLE);
        propToCreate.setPropertyName("midaFont");
        propToCreate.setValueType(WidgetStyleValueType.NUMBER);
        propToCreate.setScalarValue("14");
        provided.add(propToCreate);

        // Act
        invokeSyncStyleProperties(entity, provided);

        // Assert
        ArgumentCaptor<List<WidgetStylePropertyEntity>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(stylePropertyRepository).saveAll(saveCaptor.capture());
        assertThat(saveCaptor.getValue()).hasSize(2);

        verify(stylePropertyRepository).deleteAll(any());
    }

    @Test
    @DisplayName("resolvePalette: llança excepció quan no es troba la paleta ni per ID ni per clientId")
    void resolvePalette_quanNoEsTrobaPaleta_llancaExcepcio() {
        // Arrange
        ResourceReference<Paleta, Long> ref = new ResourceReference<>();
        ref.setId(999L);

        Map<String, PaletaEntity> byClientId = new HashMap<>();
        Map<Long, PaletaEntity> byId = new HashMap<>();

        when(paletaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> invokeResolvePalette(ref, "unknown-client", byClientId, byId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cada grup ha de tenir paleta de widget i paleta de grafic");
    }

    @Test
    @DisplayName("mergeDefaultStyleProperties: ignora propietats no configurables com SIMPLE:icona")
    void mergeDefaultStyleProperties_quanEsNoConfigurable_llavorsIgnora() {
        // Arrange
        List<WidgetStyleProperty> provided = new ArrayList<>();
        WidgetStyleProperty nonConfigurable = new WidgetStyleProperty();
        nonConfigurable.setScope(WidgetStyleScope.SIMPLE);
        nonConfigurable.setPropertyName("icona");
        nonConfigurable.setScalarValue("true");
        provided.add(nonConfigurable);

        // Act
        @SuppressWarnings("unchecked")
        List<WidgetStyleProperty> result = (List<WidgetStyleProperty>) invokeMergeDefaultStyleProperties(provided);

        // Assert: La propietat 'icona' no hauria d'haver sobreescrit la default
        Optional<WidgetStyleProperty> iconaProp = result.stream()
            .filter(p -> p.getScope() == WidgetStyleScope.SIMPLE && "icona".equals(p.getPropertyName()))
            .findFirst();
        assertThat(iconaProp.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("validateTemplate: permet propietat colorSubtitol sense role ni index de paleta")
    void validateTemplate_quanEsColorSubtitolSensePaleta_llavorsNoLlancaExcepcio() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        entity.setPaletteGroups(createFourValidGroups());

        WidgetStylePropertyEntity prop = new WidgetStylePropertyEntity();
        prop.setPropertyName("colorSubtitol");
        prop.setValueType(WidgetStyleValueType.COLOR);
        prop.setPaletteRole(null);
        prop.setPaletteIndex(null);
        entity.setStyleProperties(Collections.singletonList(prop));

        // Act & Assert
        assertThatCode(() -> invokeValidateTemplate(entity)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateTemplate: llança excepció si no hi ha 4 grups de paletes")
    void validateTemplate_quanMenysDe4Grups_llancaExcepcio() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        entity.setPaletteGroups(new ArrayList<>());
        entity.getPaletteGroups().add(new PlantillaGrupPaletesEntity());

        // Act & Assert
        assertThatThrownBy(() -> invokeValidateTemplate(entity))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exactament quatre grups de paletes");
    }

    @Test
    @DisplayName("validateTemplate: llança excepció si una paleta no té colors")
    void validateTemplate_quanPaletaSenseColors_llancaExcepcio() {
        // Arrange
        PlantillaEntity entity = new PlantillaEntity();
        List<PlantillaGrupPaletesEntity> groups = new ArrayList<>();

        PaletaEntity palette = new PaletaEntity();
        palette.setColors(new ArrayList<>());

        for (PaletteGroupType type : PaletteGroupType.values()) {
            PlantillaGrupPaletesEntity g = new PlantillaGrupPaletesEntity();
            g.setGroupType(type);
            g.setWidgetPalette(palette);
            g.setChartPalette(palette);
            groups.add(g);
        }
        entity.setPaletteGroups(groups);

        // Act & Assert
        assertThatThrownBy(() -> invokeValidateTemplate(entity))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("no poden estar buides");
    }

    // ========================================================================
    // HELPERS PER A TESTS I REFLEXIÓ
    // ========================================================================

    private Paleta crearPaleta(String clientId, String nom, String color) {
        Paleta palette = new Paleta();
        palette.setClientId(clientId);
        palette.setNom(nom);
        PaletaColor colorEntity = new PaletaColor();
        colorEntity.setPosicio(0);
        colorEntity.setValor(color);
        palette.setColors(Collections.singletonList(colorEntity));
        return palette;
    }

    private PaletaEntity crearPaletaEntity(Long id, String nom) {
        PaletaEntity entity = new PaletaEntity();
        entity.setId(id);
        entity.setNom(nom);
        List<PaletaColorEntity> colors = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            PaletaColorEntity color = new PaletaColorEntity();
            color.setPaleta(entity);
            color.setPosicio(i);
            color.setValor(String.format("#%06d", i * 111111));
            colors.add(color);
        }
        entity.setColors(colors);
        return entity;
    }

    private Paleta crearPaletaResource(String clientId, String nom) {
        Paleta palette = new Paleta();
        palette.setClientId(clientId);
        palette.setNom(nom);
        List<PaletaColor> colors = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            PaletaColor color = new PaletaColor();
            color.setPosicio(i);
            color.setValor(String.format("#%06d", i * 111111));
            colors.add(color);
        }
        palette.setColors(colors);
        return palette;
    }

    private List<PlantillaGrupPaletesEntity> createFourValidGroups() {
        List<PlantillaGrupPaletesEntity> groups = new ArrayList<>();
        PaletaEntity validPalette = crearPaletaEntity(1L, "Valid");
        for (PaletteGroupType type : PaletteGroupType.values()) {
            PlantillaGrupPaletesEntity g = new PlantillaGrupPaletesEntity();
            g.setGroupType(type);
            g.setWidgetPalette(validPalette);
            g.setChartPalette(validPalette);
            groups.add(g);
        }
        return groups;
    }

    @SuppressWarnings("unchecked")
    private Map<String, PaletaEntity> invokeSavePaletteResources(List<Paleta> palettes) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class.getDeclaredMethod("savePaletteResources", List.class);
            method.setAccessible(true);
            return (Map<String, PaletaEntity>) method.invoke(plantillaService, palettes);
        } catch (Exception e) {
            throw new RuntimeException("Error invocant savePaletteResources", e);
        }
    }

    private void invokeSyncStyleProperties(PlantillaEntity entity, List<WidgetStyleProperty> properties) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class.getDeclaredMethod("syncStyleProperties", PlantillaEntity.class, List.class);
            method.setAccessible(true);
            method.invoke(plantillaService, entity, properties);
        } catch (Exception e) {
            throw new RuntimeException("Error invocant syncStyleProperties", e);
        }
    }

    private PaletaEntity invokeResolvePalette(ResourceReference<Paleta, Long> ref, String clientId,
                                              Map<String, PaletaEntity> byClientId, Map<Long, PaletaEntity> byId) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class.getDeclaredMethod("resolvePalette", ResourceReference.class, String.class, Map.class, Map.class);
            method.setAccessible(true);
            return (PaletaEntity) method.invoke(plantillaService, ref, clientId, byClientId, byId);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException("Error invocant resolvePalette", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<WidgetStyleProperty> invokeMergeDefaultStyleProperties(List<WidgetStyleProperty> provided) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class.getDeclaredMethod("mergeDefaultStyleProperties", List.class);
            method.setAccessible(true);
            return (List<WidgetStyleProperty>) method.invoke(plantillaService, provided);
        } catch (Exception e) {
            throw new RuntimeException("Error invocant mergeDefaultStyleProperties", e);
        }
    }

    private void invokeValidateTemplate(PlantillaEntity entity) {
        try {
            java.lang.reflect.Method method = PlantillaServiceImpl.class.getDeclaredMethod("validateTemplate", PlantillaEntity.class);
            method.setAccessible(true);
            method.invoke(plantillaService, entity);
        } catch (Exception e) {
            if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
            throw new RuntimeException("Error invocant validateTemplate", e);
        }
    }

    // ========================================================================
    // TESTS IMPORT / EXPORT
    // ========================================================================

    @Test
    @DisplayName("init: registra els artifacts d'exportació i importació")
    void init_registraArtifacts() {
        plantillaService.init();

        Map<?, ?> reportMap = (Map<?, ?>) ReflectionTestUtils.getField(plantillaService, "reportGeneratorMap");
        assertThat(reportMap.containsKey(Plantilla.PLANTILLA_EXPORT)).isTrue();

        Map<?, ?> actionMap = (Map<?, ?>) ReflectionTestUtils.getField(plantillaService, "actionExecutorMap");
        assertThat(actionMap.containsKey(Plantilla.PLANTILLA_IMPORT)).isTrue();
    }

    @Test
    @DisplayName("PlantillaExportReportGenerator: generateData converteix entitat a PlantillaExport")
    void exportReportGenerator_generateData_ambEntitat() throws Exception {
        PlantillaServiceImpl.PlantillaExportReportGenerator reportGen =
                plantillaService.new PlantillaExportReportGenerator();

        PlantillaExport expected = new PlantillaExport();
        expected.setNom("Plantilla Test");
        when(dashboardExportMapper.toPlantillaExport(plantillaEntity)).thenReturn(expected);

        List<PlantillaExport> data = reportGen.generateData(Plantilla.PLANTILLA_EXPORT, plantillaEntity, null);

        assertThat(data).containsExactly(expected);
        verify(dashboardExportMapper).toPlantillaExport(plantillaEntity);
    }

    @Test
    @DisplayName("PlantillaExportReportGenerator: generateFile genera fitxer JSON amb nom sanititzat")
    void exportReportGenerator_generateFile_generaFitxerJson() throws Exception {
        PlantillaServiceImpl.PlantillaExportReportGenerator reportGen =
                plantillaService.new PlantillaExportReportGenerator();

        PlantillaExport export = new PlantillaExport();
        export.setNom("Plantilla Única / Test");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DownloadableFile file = reportGen.generateFile(Plantilla.PLANTILLA_EXPORT, Collections.singletonList(export), ReportFileType.JSON, out);

        assertThat(file).isNotNull();
        assertThat(file.getContentType()).isEqualTo("application/json");
        assertThat(file.getName()).isEqualTo("Plantilla Unica _ Test.json");
        assertThat(out.size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("PlantillaImportActionExecutor: onChange analitza el fitxer i calcula els conflictes")
    void importActionExecutor_onChange_calculaConflictes() throws Exception {
        PlantillaServiceImpl.PlantillaImportActionExecutor actionExec =
                plantillaService.new PlantillaImportActionExecutor();

        FileReference file = new FileReference();
        ReflectionTestUtils.setField(file, "content", "{\"nom\": \"P1\"}".getBytes(StandardCharsets.UTF_8));

        PlantillaImportParams target = new PlantillaImportParams();
        PlantillaExport p1 = new PlantillaExport();
        p1.setNom("P1");

        when(dashboardImportHelper.parsePlantillesJson(anyString())).thenReturn(Collections.singletonList(p1));
        doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            List<Conflict> conflicts = inv.getArgument(1);
            conflicts.add(new Conflict("P1", PlantillaExport.class.getSimpleName()));
            return null;
        }).when(dashboardImportHelper).checkPlantillaConflicts(eq(Collections.singletonList(p1)), any());

        actionExec.onChange(null, null, PlantillaImportParams.Fields.file, file, null, null, target);

        assertThat(target.getConflicts()).hasSize(1);
        assertThat(target.getConflicts().get(0).getTitol()).isEqualTo("P1");
    }

    @Test
    @DisplayName("PlantillaImportActionExecutor: exec importa les plantilles correctament")
    void importActionExecutor_exec_importaPlantilles() throws Exception {
        PlantillaServiceImpl.PlantillaImportActionExecutor actionExec =
                plantillaService.new PlantillaImportActionExecutor();

        FileReference file = new FileReference();
        ReflectionTestUtils.setField(file, "content", "{\"nom\": \"P1\"}".getBytes(StandardCharsets.UTF_8));

        PlantillaImportParams params = new PlantillaImportParams();
        params.setFile(file);

        PlantillaExport p1 = new PlantillaExport();
        p1.setNom("P1");

        PlantillaEntity entity1 = new PlantillaEntity();
        entity1.setId(100L);
        entity1.setNom("P1");

        when(dashboardImportHelper.parsePlantillesJson(anyString())).thenReturn(Collections.singletonList(p1));
        when(dashboardImportHelper.importPlantillesFromExport(eq(Collections.singletonList(p1)), any()))
                .thenReturn(Collections.singletonList(entity1));

        PlantillaImportResult result = actionExec.exec(Plantilla.PLANTILLA_IMPORT, null, params);

        assertThat(result).isNotNull();
        verify(dashboardImportHelper).validatePlantillaExport(Collections.singletonList(p1));
        verify(dashboardImportHelper).importPlantillesFromExport(eq(Collections.singletonList(p1)), any());
    }

    @Test
    @DisplayName("PlantillaImportActionExecutor: exec llança ActionExecutionException si hi ha conflictes bloquejants")
    void importActionExecutor_exec_ambConflictesBloquejants_llancaExcepcio() throws Exception {
        PlantillaServiceImpl.PlantillaImportActionExecutor actionExec =
                plantillaService.new PlantillaImportActionExecutor();

        FileReference file = new FileReference();
        ReflectionTestUtils.setField(file, "content", "{\"nom\": \"P1\"}".getBytes(StandardCharsets.UTF_8));

        Conflict blockingConflict = new Conflict("P1", PlantillaExport.class.getSimpleName());
        blockingConflict.setBloquejant(true);

        PlantillaImportParams params = new PlantillaImportParams();
        params.setFile(file);
        params.setConflicts(Collections.singletonList(blockingConflict));

        PlantillaExport p1 = new PlantillaExport();
        p1.setNom("P1");
        when(dashboardImportHelper.parsePlantillesJson(anyString())).thenReturn(Collections.singletonList(p1));

        assertThatThrownBy(() -> actionExec.exec(Plantilla.PLANTILLA_IMPORT, null, params))
                .isInstanceOf(ActionExecutionException.class);
    }

    @Test
    @DisplayName("PlantillaImportActionExecutor: onChange afegeix conflicte bloquejant si el fitxer no conté plantilles")
    void importActionExecutor_onChange_sensePlantilles_afegeixConflicteBloquejant() throws Exception {
        PlantillaServiceImpl.PlantillaImportActionExecutor actionExec =
                plantillaService.new PlantillaImportActionExecutor();

        FileReference file = new FileReference();
        ReflectionTestUtils.setField(file, "content", "{}".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(file, "name", "buit.json");

        PlantillaImportParams target = new PlantillaImportParams();

        when(dashboardImportHelper.parsePlantillesJson(anyString())).thenReturn(Collections.emptyList());
        doThrow(new IllegalArgumentException("El fitxer no conté cap plantilla per importar."))
                .when(dashboardImportHelper).validatePlantillaExport(Collections.emptyList());

        actionExec.onChange(null, null, PlantillaImportParams.Fields.file, file, null, null, target);

        assertThat(target.getConflicts()).hasSize(1);
        Conflict c = target.getConflicts().get(0);
        assertThat(c.isBloquejant()).isTrue();
        assertThat(c.getMissatgeError()).isEqualTo("El fitxer no conté cap plantilla per importar.");
        assertThat(c.getTitol()).isEqualTo("buit.json");
    }

    @Test
    @DisplayName("PlantillaImportActionExecutor: onChange afegeix conflicte bloquejant si el fitxer JSON té format invàlid")
    void importActionExecutor_onChange_formatInvalid_afegeixConflicteBloquejant() throws Exception {
        PlantillaServiceImpl.PlantillaImportActionExecutor actionExec =
                plantillaService.new PlantillaImportActionExecutor();

        FileReference file = new FileReference();
        ReflectionTestUtils.setField(file, "content", "invalid json".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(file, "name", "corrupte.json");

        PlantillaImportParams target = new PlantillaImportParams();

        when(dashboardImportHelper.parsePlantillesJson(anyString()))
                .thenThrow(new IllegalArgumentException("Format JSON no reconegut"));

        actionExec.onChange(null, null, PlantillaImportParams.Fields.file, file, null, null, target);

        assertThat(target.getConflicts()).hasSize(1);
        Conflict c = target.getConflicts().get(0);
        assertThat(c.isBloquejant()).isTrue();
        assertThat(c.getMissatgeError()).isEqualTo("Format JSON no reconegut");
        assertThat(c.getTitol()).isEqualTo("corrupte.json");
    }
}
