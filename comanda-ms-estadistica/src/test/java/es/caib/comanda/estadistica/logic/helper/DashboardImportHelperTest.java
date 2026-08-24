package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardImportHelper")
class DashboardImportHelperTest {

    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private AtributsVisualsHelper atributsVisualsHelper;
    @Mock private DashboardExportMapper dashboardExportMapper;
    @Mock private DashboardRepository dashboardRepository;
    @Mock private DashboardTitolRepository dashboardTitolRepository;
    @Mock private DashboardItemRepository dashboardItemRepository;
    @Mock private PlantillaRepository plantillaRepository;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private EstadisticaWidgetRepository estadisticaWidgetRepository;
    @Mock private DimensioRepository dimensioRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private PaletaRepository paletaRepository;
    @Mock private I18nUtil i18nUtil;
    @Mock private ApplicationContext applicationContext;

    @InjectMocks
    private DashboardImportHelper dashboardImportHelper;

    @BeforeEach
    void setUp() {
        // Configuració per evitar NPE en crides estàtiques a I18nUtil
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
        lenient().when(i18nUtil.getI18nMessage(anyString(), any())).thenAnswer(i -> i.getArgument(0));

        // Configuració per defecte per evitar NPE en crides a repositoris dins de lògica de conflictes
        lenient().when(dashboardRepository.findByTitol(anyString())).thenReturn(null);
        lenient().when(estadisticaWidgetRepository.findByAppIdAndTitol(anyLong(), anyString())).thenReturn(null);
        lenient().when(plantillaRepository.findByNom(anyString())).thenReturn(Optional.empty());
        lenient().when(paletaRepository.findByNom(anyString())).thenReturn(Optional.empty());
    }

    // ========================================================================
    // 1. TESTOS PER A IMPORTACIÓ I RESOLUCIÓ DE CONFLICTES
    // ========================================================================

    @Test
    @DisplayName("toDashboardEntity: delega correctament al mapper")
    void toDashboardEntity_quanEsCrida_llavorsDelegaAlMapper() {
        // Arrange
        List<DashboardExport> exports = Collections.singletonList(new DashboardExport());
        List<DashboardEntity> expectedEntities = Collections.singletonList(new DashboardEntity());
        when(dashboardExportMapper.toDashboardEntity(eq(exports), any(), any(), any(), any(), any())).thenReturn(expectedEntities);

        // Act
        List<DashboardEntity> result = dashboardImportHelper.toDashboardEntity(exports);

        // Assert
        assertThat(result).isSameAs(expectedEntities);
        verify(dashboardExportMapper).toDashboardEntity(eq(exports), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("importDashboardFromEntity: retorna entitat existent quan conflicte és EMPRAR_EXISTENT")
    void importDashboardFromEntity_quanConflicteEmprarExistent_llavorsRetornaExistent() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setTitol("Dashboard Test");

        Conflict conflict = new Conflict("Dashboard Test", DashboardExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        DashboardEntity existingEntity = new DashboardEntity();
        when(dashboardRepository.findByTitol("Dashboard Test")).thenReturn(existingEntity);

        // Act
        DashboardEntity result = dashboardImportHelper.importDashboardFromEntity(entity, Collections.singletonList(conflict));

        // Assert
        assertThat(result).isSameAs(existingEntity);
        verify(dashboardRepository, never()).save(any());
    }

    @Test
    @DisplayName("importDashboardFromEntity: crea amb nou nom proporcionat quan conflicte és CREAR_AMB_ALTRE_NOM")
    void importDashboardFromEntity_quanConflicteCrearAmbAltreNomINouNomDefinit_llavorsUsaNouNom() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setTitol("Dashboard Test");
        entity.setTitols(new ArrayList<>());
        entity.setItems(new ArrayList<>());

        Conflict conflict = new Conflict("Dashboard Test", DashboardExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.CREAR_AMB_ALTRE_NOM);
        conflict.setNouNom("Dashboard Test (Copia)");

        // Act
        dashboardImportHelper.importDashboardFromEntity(entity, Collections.singletonList(conflict));

        // Assert
        assertThat(entity.getTitol()).isEqualTo("Dashboard Test (Copia)");
        verify(dashboardRepository).save(entity);
    }

    @Test
    @DisplayName("importDashboardFromEntity: genera nou nom automàticament quan nouNom és null")
    void importDashboardFromEntity_quanConflicteCrearAmbAltreNomINouNomNull_llavorsGeneraNomAutomatic() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setTitol("Dashboard Test");
        entity.setTitols(new ArrayList<>());
        entity.setItems(new ArrayList<>());

        Conflict conflict = new Conflict("Dashboard Test", DashboardExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.CREAR_AMB_ALTRE_NOM);
        // nouNom és null per defecte

        // Simulem que "Dashboard Test" i "Dashboard Test (1)" existeixen, però "Dashboard Test (2)" no
        when(dashboardRepository.findByTitol("Dashboard Test")).thenReturn(new DashboardEntity());
        when(dashboardRepository.findByTitol("Dashboard Test (1)")).thenReturn(new DashboardEntity());
        when(dashboardRepository.findByTitol("Dashboard Test (2)")).thenReturn(null);

        // Act
        dashboardImportHelper.importDashboardFromEntity(entity, Collections.singletonList(conflict));

        // Assert
        assertThat(entity.getTitol()).isEqualTo("Dashboard Test (2)");
        verify(dashboardRepository).save(entity);
    }

    // ========================================================================
    // 2. TESTOS PER A IMPORTACIÓ DE WIDGETS (Lògica instanceof)
    // ========================================================================

    @Test
    @DisplayName("importWidget: estableix referència al widget per a EstadisticaSimpleWidgetEntity")
    void importWidget_quanEsSimpleWidget_llavorsEstableixReferencia() {
        // Arrange
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Widget Simple");
        widget.setAppId(1L);
        IndicadorTaulaEntity indicador = new IndicadorTaulaEntity();
        widget.setIndicadorInfo(indicador);

        Conflict conflict = new Conflict();
        conflict.setTitol(widget.getTitol());
        conflict.setAppId(widget.getAppId());
        conflict.setNouNom("Nou Widget Simple");
        conflict.setOverwrite(OverwriteEnum.CREAR_AMB_ALTRE_NOM);
        conflict.setTipo(EstadisticaWidgetExport.class.getSimpleName());

        when(estadisticaWidgetRepository.findByAppIdAndTitol(any(), any())).thenReturn(null);

        // Act
        EstadisticaSimpleWidgetEntity result = (EstadisticaSimpleWidgetEntity) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importWidget", widget, List.of(conflict));

        // Assert
        assertThat(result.getTitol()).isSameAs(conflict.getNouNom());
        assertThat(result.getIndicadorInfo().getWidget()).isSameAs(result);
        verify(estadisticaWidgetRepository).findByAppIdAndTitol(any(), any());
        verify(estadisticaWidgetRepository).save(result);
    }

    @Test
    @DisplayName("importWidget: estableix referència al widget per a EstadisticaGraficWidgetEntity")
    void importWidget_quanEsGraficWidget_llavorsEstableixReferencia() {
        // Arrange
        EstadisticaGraficWidgetEntity widget = new EstadisticaGraficWidgetEntity();
        widget.setTitol("Widget Grafic");
        widget.setAppId(1L);
        IndicadorTaulaEntity indicador = new IndicadorTaulaEntity();
        widget.setIndicadorsInfo(Collections.singletonList(indicador));

        Conflict conflict = new Conflict();
        conflict.setTitol(widget.getTitol());
        conflict.setAppId(widget.getAppId());
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);
        conflict.setTipo(EstadisticaWidgetExport.class.getSimpleName());

        when(estadisticaWidgetRepository.findByAppIdAndTitol(any(), any())).thenReturn(new EstadisticaGraficWidgetEntity());

        // Act
        ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importWidget", widget, List.of(conflict));

        // Assert
        verify(estadisticaWidgetRepository).findByAppIdAndTitol(any(), any());
    }

    @Test
    @DisplayName("importWidget: estableix referència al widget per a EstadisticaTaulaWidgetEntity")
    void importWidget_quanEsTaulaWidget_llavorsEstableixReferencia() {
        // Arrange
        EstadisticaTaulaWidgetEntity widget = new EstadisticaTaulaWidgetEntity();
        widget.setTitol("Widget Taula");
        widget.setAppId(1L);
        IndicadorTaulaEntity columna = new IndicadorTaulaEntity();
        widget.setColumnes(Collections.singletonList(columna));

        // Act
        EstadisticaTaulaWidgetEntity result = (EstadisticaTaulaWidgetEntity) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importWidget", widget, Collections.emptyList());

        // Assert
        assertThat(result.getColumnes().get(0).getWidget()).isSameAs(result);
        verify(estadisticaWidgetRepository).save(result);
    }

    // ========================================================================
    // 3. TESTOS PER A CHECK CONFLICTS I EXCEPCIONS (Pre-flight checks)
    // ========================================================================

    @Test
    @DisplayName("checkDashboardConflicts: llança AnswerRequiredException quan l'Entorn no existeix")
    void checkDashboardConflicts_quanEntornNoExisteix_llancaExcepcio() {
        // Arrange
        DashboardExport dashboard = new DashboardExport();
        dashboard.setEntornCodi("ENTORN_INEXISTENT");

        when(estadisticaClientHelper.entornByCodi("ENTORN_INEXISTENT")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> dashboardImportHelper.checkDashboardConflicts(dashboard, Collections.emptyList()))
            .isInstanceOf(AnswerRequiredException.class)
            .hasMessageContaining("Answer 'ENTORN' required to process changes");
    }

    @Test
    @DisplayName("checkDashboardConflicts: llança AnswerRequiredException quan l'App no existeix")
    void checkDashboardConflicts_quanAppNoExisteix_llancaExcepcio() {
        // Arrange
        DashboardExport dashboard = new DashboardExport();
        dashboard.setAppCodi("APP_INEXISTENT");
        dashboard.setTitols(new ArrayList<>());
        dashboard.setItems(new ArrayList<>());

        when(estadisticaClientHelper.appFindByCodi("APP_INEXISTENT")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> dashboardImportHelper.checkDashboardConflicts(dashboard, Collections.emptyList()))
            .isInstanceOf(AnswerRequiredException.class)
            .hasMessageContaining("Answer 'APP' required to process changes");
    }

    @Test
    @DisplayName("checkDashboardItemConflicts: llança AnswerRequiredException quan l'Indicador no existeix (Simple Widget)")
    void checkDashboardItemConflicts_quanIndicadorNoExisteixSimple_llancaExcepcio() {
        // Arrange
        DashboardItemExport item = new DashboardItemExport();
        item.setEntornCodi("ENT");
        item.setAppCodi("APP");

        EstadisticaSimpleWidgetExport widget = new EstadisticaSimpleWidgetExport();
        widget.setTitol("Widget");
        IndicadorTaulaExport indInfo = new IndicadorTaulaExport();
        indInfo.setIndicadorCodi("IND1");
        widget.setIndicadorInfo(indInfo);
        widget.setDimensionsValor(new ArrayList<>());
        item.setWidget(widget);

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);
        when(indicadorRepository.findByCodiAndEntornAppId("IND1", 10L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> {
            List<Conflict> conflicts = Collections.emptyList();
            ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, conflicts);
        }).isInstanceOf(AnswerRequiredException.class)
            .hasMessageContaining("Answer 'INDICADOR' required to process changes");
    }

    @Test
    @DisplayName("checkDashboardItemConflicts: llança AnswerRequiredException quan la Dimensio no existeix (Taula Widget)")
    void checkDashboardItemConflicts_quanDimensioNoExisteixTaula_llancaExcepcio() {
        // Arrange
        DashboardItemExport item = new DashboardItemExport();
        item.setEntornCodi("ENT");
        item.setAppCodi("APP");

        EstadisticaTaulaWidgetExport widget = new EstadisticaTaulaWidgetExport();
        widget.setTitol("Widget Taula");
        widget.setDimensioAgrupacioCodi("DIM1");
        widget.setDimensionsValor(new ArrayList<>());
        item.setWidget(widget);

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(20L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);
        when(dimensioRepository.findByCodiAndEntornAppId("DIM1", 20L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> {
            List<Conflict> conflicts = Collections.emptyList();
            ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, conflicts);
        }).isInstanceOf(AnswerRequiredException.class)
            .hasMessageContaining("Answer 'DIMENSIO' required to process changes");
    }

    // ========================================================================
    // 4. TESTOS PER A LÒGICA AUXILIAR DE CONFLICTES
    // ========================================================================

    @Test
    @DisplayName("addConflict: afegeix conflicte quan l'element existeix i no està a la llista")
    void addConflict_quanElementExisteixINoEstaLlista_llavorsAfegeixConflicte() {
        // Arrange
        List<Conflict> conflicts = new java.util.ArrayList<>();
        when(dashboardRepository.findByTitol("Dash")).thenReturn(new DashboardEntity());

        // Act
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "addConflict", "Dash", null, "DashboardExport", conflicts);

        // Assert
        assertThat(conflicts).hasSize(1);
        assertThat(conflicts.get(0).getTitol()).isEqualTo("Dash");
    }

    @Test
    @DisplayName("addConflict: no afegeix conflicte si ja existeix a la llista")
    void addConflict_quanElementJaEstaLlista_llavorsNoAfegeixDuplicat() {
        // Arrange
        List<Conflict> conflicts = new java.util.ArrayList<>();
        conflicts.add(new Conflict("Dash", "DashboardExport"));
        when(dashboardRepository.findByTitol("Dash")).thenReturn(new DashboardEntity());

        // Act
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "addConflict", "Dash", null, "DashboardExport", conflicts);

        // Assert
        assertThat(conflicts).hasSize(1); // No ha crescut
    }

    @Test
    @DisplayName("getElementNewNom: incrementa correctament el comptador quan els noms existeixen")
    void getElementNewNom_quanNomsExisteixen_llavorsIncrementaComptador() {
        // Arrange
        when(dashboardRepository.findByTitol("Original")).thenReturn(new DashboardEntity());
        when(dashboardRepository.findByTitol("Original (1)")).thenReturn(new DashboardEntity());
        when(dashboardRepository.findByTitol("Original (2)")).thenReturn(null);

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "getElementNewNom", "Original", null, "DashboardExport");

        // Assert
        assertThat(result).isEqualTo("Original (2)");
    }

    @Test
    @DisplayName("importPlantilla: retorna existent quan conflicte és EMPRAR_EXISTENT")
    void importPlantilla_quanConflicteEmprarExistent_llavorsRetornaExistent() {
        // Arrange
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setNom("Plantilla Test");

        Conflict conflict = new Conflict("Plantilla Test", PlantillaExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.EMPRAR_EXISTENT);

        PlantillaEntity existing = new PlantillaEntity();
        when(plantillaRepository.findByNom("Plantilla Test")).thenReturn(Optional.of(existing));

        // Act
        PlantillaEntity result = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importPlantilla", plantilla, Collections.singletonList(conflict));

        // Assert
        assertThat(result).isSameAs(existing);
        verify(plantillaRepository, never()).save(any());
    }

    // ========================================================================
    // 5. TESTOS CRÍTICS PER PUJAR LA COBERTURA > 80%
    // ========================================================================

    @Test
    @DisplayName("importDashboardFromEntity: importa dashboard complet amb títols i items")
    void importDashboardFromEntity_quanDashboardComplet_llavorsImportaTotCorrectament() {
        // Arrange
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setTitol("Dash Test");

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setTitol("Titol 1");
        dashboard.setTitols(Collections.singletonList(titol));

        DashboardItemEntity item = new DashboardItemEntity();
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Widget 1");
        widget.setAppId(1L);
        widget.setIndicadorInfo(new IndicadorTaulaEntity());
        item.setWidget(widget);
        dashboard.setItems(Collections.singletonList(item));

        // Act
        dashboardImportHelper.importDashboardFromEntity(dashboard, Collections.emptyList());

        // Assert
        verify(dashboardRepository).save(dashboard);
        verify(dashboardTitolRepository).save(titol);
        verify(dashboardItemRepository).save(item);
        verify(estadisticaWidgetRepository).save(widget);
    }

    @Test
    @DisplayName("importPlantilla: importa plantilla amb grups de paletes i propietats d'estil")
    void importPlantilla_quanTeGrupsIPropietats_llavorsImportaTot() {
        // Arrange
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setNom("Plantilla Test");

        PaletaEntity widgetPalette = new PaletaEntity();
        widgetPalette.setNom("Widget Palette");
        widgetPalette.setColors(new ArrayList<>());

        PlantillaGrupPaletesEntity grup = new PlantillaGrupPaletesEntity();
        grup.setPlantilla(plantilla);
        grup.setWidgetPalette(widgetPalette);
        grup.setChartPalette(widgetPalette);
        plantilla.setPaletteGroups(Collections.singletonList(grup));
        plantilla.setStyleProperties(new ArrayList<>());

        Conflict conflict = new Conflict();
        conflict.setTitol(plantilla.getNom());
        conflict.setNouNom("Nou Plantilla");
        conflict.setOverwrite(OverwriteEnum.CREAR_AMB_ALTRE_NOM);
        conflict.setTipo(PlantillaExport.class.getSimpleName());

        when(plantillaRepository.findByNom(any())).thenReturn(Optional.empty());

        // Act
        PlantillaEntity result = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importPlantilla", plantilla, Collections.singletonList(conflict));

        // Assert
        assertThat(result.getNom()).isSameAs(conflict.getNouNom());
        verify(plantillaRepository).save(plantilla);
        verify(paletaRepository, times(2)).save(widgetPalette);
        assertThat(grup.getPlantilla()).isSameAs(plantilla);
    }

    @Test
    @DisplayName("importPaleta: importa paleta amb colors")
    void importPaleta_quanTeColors_llavorsImportaColors() {
        // Arrange
        PaletaEntity paleta = new PaletaEntity();
        paleta.setNom("Paleta Test");
        PaletaColorEntity color = new PaletaColorEntity();
        paleta.setColors(Collections.singletonList(color));

        // Act
        ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importPaleta", paleta, Collections.emptyList());

        // Assert
        verify(paletaRepository).save(paleta);
        assertThat(color.getPaleta()).isSameAs(paleta);
    }

    @Test
    @DisplayName("checkDashboardConflicts: valida dashboard complet amb items, títols i plantilles")
    void checkDashboardConflicts_quanDashboardComplet_llavorsValidaTot() {
        // Arrange
        DashboardExport dashboard = new DashboardExport();
        dashboard.setTitol("Dash Test");
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");

        DashboardItemExport item = new DashboardItemExport();
        item.setEntornCodi("ENT");
        item.setAppCodi("APP");
        EstadisticaSimpleWidgetExport widget = new EstadisticaSimpleWidgetExport();
        widget.setTitol("Widget Test");
        widget.setDimensionsValor(new ArrayList<>());
        item.setWidget(widget);
        dashboard.setItems(Collections.singletonList(item));

        DashboardTitolExport titol = new DashboardTitolExport();
        PlantillaExport plantillaTitol = new PlantillaExport();
        plantillaTitol.setNom("Plantilla Titol");
        plantillaTitol.setPaletteGroups(new ArrayList<>());
        titol.setPlantilla(plantillaTitol);
        dashboard.setTitols(Collections.singletonList(titol));

        PlantillaExport plantillaDash = new PlantillaExport();
        plantillaDash.setNom("Plantilla Dash");
        plantillaDash.setPaletteGroups(new ArrayList<>());
        dashboard.setPlantilla(plantillaDash);

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp(); entornApp.setId(1L);


        when(dashboardRepository.findByTitol(any())).thenReturn(new DashboardEntity());
        when(estadisticaWidgetRepository.findByAppIdAndTitol(any(), any())).thenReturn(new EstadisticaSimpleWidgetEntity());
        when(plantillaRepository.findByNom(any())).thenReturn(Optional.of(new PlantillaEntity()));
        lenient().when(paletaRepository.findByNom(any())).thenReturn(Optional.of(new PaletaEntity()));

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        lenient().when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);
        lenient().when(indicadorRepository.findByCodiAndEntornAppId(anyString(), anyLong())).thenReturn(Optional.of(new IndicadorEntity()));
        lenient().when(dimensioRepository.findByCodiAndEntornAppId(anyString(), anyLong())).thenReturn(Optional.of(new DimensioEntity()));

        List<Conflict> conflicts = new ArrayList<>();

        // Act
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        // Assert
        // S'han d'haver afegit conflictes per: Dashboard, Widget, Plantilla Titol, Plantilla Dash
        assertThat(conflicts).hasSize(4);
    }

    @Test
    @DisplayName("checkEntornApp: retorna EntornApp quan existeix")
    void checkEntornApp_quanExisteix_llavorsRetornaEntornApp() {
        // Arrange
        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp(); entornApp.setId(3L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);

        // Act
        EntornApp result = (EntornApp) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "checkEntornApp", "ENT", "APP");

        // Assert
        assertThat(result).isSameAs(entornApp);
    }

    @Test
    @DisplayName("checkPlantillaConflicts: valida plantilla amb grups de paletes")
    void checkPlantillaConflicts_quanTeGrupsPaletes_llavorsValidaPaletes() {
        // Arrange
        PlantillaExport plantilla = new PlantillaExport();
        plantilla.setNom("Plantilla Test");

        PaletaExport paleta1 = new PaletaExport();
        paleta1.setNom("Paleta Widget");
        PaletaExport paleta2 = new PaletaExport();
        paleta2.setNom("Paleta Chart");

        PlantillaGrupPaletesExport grup = new PlantillaGrupPaletesExport();
        grup.setWidgetPalette(paleta1);
        grup.setChartPalette(paleta2);
        plantilla.setPaletteGroups(Collections.singletonList(grup));

        List<Conflict> conflicts = new ArrayList<>();

        when(plantillaRepository.findByNom(any())).thenReturn(Optional.of(new PlantillaEntity()));
        lenient().when(paletaRepository.findByNom(any())).thenReturn(Optional.of(new PaletaEntity()));

        // Act
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkPlantillaConflicts", plantilla, conflicts);

        // Assert
        // S'han d'haver afegit conflictes per la Plantilla i les 2 Paletes
        assertThat(conflicts).hasSize(3);
    }

    @Test
    @DisplayName("importDashboardFromExport: delega correctament a toDashboardEntity i importDashboardFromEntity")
    void importDashboardFromExport_quanEsCrida_llavorsDelegaCorrectament() {
        // Arrange
        List<DashboardExport> exports = Collections.singletonList(new DashboardExport());
        List<Conflict> conflicts = Collections.emptyList();

        DashboardEntity entity = new DashboardEntity();
        entity.setTitols(new ArrayList<>());
        entity.setItems(new ArrayList<>());
        when(dashboardExportMapper.toDashboardEntity(eq(exports), any(), any(), any(), any(), any()))
            .thenReturn(Collections.singletonList(entity));

        // Act
        dashboardImportHelper.importDashboardFromExport(exports, conflicts);

        // Assert
        verify(dashboardExportMapper).toDashboardEntity(eq(exports), any(), any(), any(), any(), any());
        verify(dashboardRepository).save(entity);
    }
}
