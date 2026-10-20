package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.OverwriteEnum;
import es.caib.comanda.estadistica.logic.intf.model.export.*;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.logic.service.DashboardServiceImpl.Conflict;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardFiltreEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardFiltreTipus;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteRole;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleValueType;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaColorEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.WidgetStylePropertyEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.*;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardImportHelper")
class DashboardImportHelperTest {

    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private AtributsVisualsHelper atributsVisualsHelper;
    @Spy private DashboardExportMapper dashboardExportMapper = new DashboardExportMapperImpl();
    @Mock private DashboardRepository dashboardRepository;
    @Mock private DashboardTitolRepository dashboardTitolRepository;
    @Mock private DashboardItemRepository dashboardItemRepository;
    @Mock private PlantillaRepository plantillaRepository;
    @Mock private IndicadorRepository indicadorRepository;
    @Mock private EstadisticaWidgetRepository estadisticaWidgetRepository;
    @Mock private DimensioRepository dimensioRepository;
    @Mock private DimensioValorRepository dimensioValorRepository;
    @Mock private PaletaRepository paletaRepository;
    @Mock private IndicadorExportHelper indicadorExportHelper;
    @Mock private DashboardFiltreRepository dashboardFiltreRepository;
    @Mock private javax.validation.Validator validator;
    @Mock private PaletaColorRepository paletaColorRepository;
    @Mock private DashboardTemplatePaletteGroupRepository dashboardTemplatePaletteGroupRepository;
    @Mock private WidgetStylePropertyRepository widgetStylePropertyRepository;
    @Mock private EstadisticaWidgetHelper estadisticaWidgetHelper;
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
        verify(estadisticaWidgetRepository).findByAppIdAndTitol(1L, "Nou Widget Simple");
        verify(estadisticaWidgetRepository).save(result);
    }

    @Test
    @DisplayName("importWidget: retorna widget existent si ja existeix amb el nou nom proporcionat")
    void importWidget_quanCrearAmbAltreNomIExisteixWidgetAmbNouNom_llavorsRetornaWidgetExistent() {
        // Arrange
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Widget Simple");
        widget.setAppId(1L);

        Conflict conflict = new Conflict();
        conflict.setTitol(widget.getTitol());
        conflict.setAppId(widget.getAppId());
        conflict.setNouNom("Nou Widget Simple");
        conflict.setOverwrite(OverwriteEnum.CREAR_AMB_ALTRE_NOM);
        conflict.setTipo(EstadisticaWidgetExport.class.getSimpleName());

        EstadisticaWidgetEntity existingWidget = new EstadisticaSimpleWidgetEntity();
        existingWidget.setTitol("Nou Widget Simple");
        existingWidget.setAppId(1L);

        when(estadisticaWidgetRepository.findByAppIdAndTitol(1L, "Nou Widget Simple")).thenReturn(existingWidget);

        // Act
        EstadisticaWidgetEntity result = (EstadisticaWidgetEntity) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "importWidget", widget, List.of(conflict));

        // Assert
        assertThat(result).isSameAs(existingWidget);
        verify(estadisticaWidgetRepository).findByAppIdAndTitol(1L, "Nou Widget Simple");
        verify(estadisticaWidgetRepository, never()).save(widget);
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
    @DisplayName("checkDashboardConflicts: afegeix conflicte bloquejant quan l'Entorn no existeix")
    void checkDashboardConflicts_quanEntornNoExisteix_llancaExcepcio() {
        // Arrange
        DashboardExport dashboard = new DashboardExport();
        dashboard.setEntornCodi("ENTORN_INEXISTENT");

        when(estadisticaClientHelper.entornByCodi("ENTORN_INEXISTENT")).thenReturn(null);

        // Act
        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        // Assert
        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.isBloquejant()).isTrue();
        assertThat(conflict.getTipo()).isEqualTo("EntornExport");
        assertThat(conflict.getCodi()).isEqualTo("ENTORN_INEXISTENT");
    }

    @Test
    @DisplayName("checkDashboardConflicts: afegeix conflicte bloquejant quan l'App no existeix")
    void checkDashboardConflicts_quanAppNoExisteix_llancaExcepcio() {
        // Arrange
        DashboardExport dashboard = new DashboardExport();
        dashboard.setAppCodi("APP_INEXISTENT");
        dashboard.setTitols(new ArrayList<>());
        dashboard.setItems(new ArrayList<>());

        when(estadisticaClientHelper.appFindByCodi("APP_INEXISTENT")).thenReturn(null);

        // Act
        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        // Assert
        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.isBloquejant()).isTrue();
        assertThat(conflict.getTipo()).isEqualTo("AppExport");
        assertThat(conflict.getCodi()).isEqualTo("APP_INEXISTENT");
    }

    @Test
    @DisplayName("checkDashboardItemConflicts: afegeix conflicte bloquejant quan l'Indicador no existeix (Simple Widget)")
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
        ReflectionTestUtils.setField(entorn, "nom", "Entorn Test");
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        ReflectionTestUtils.setField(app, "nom", "App Test");
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);
        when(indicadorRepository.findByCodiAndEntornAppId("IND1", 10L)).thenReturn(Optional.empty());

        // Act
        List<Conflict> conflicts = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, conflicts);

        // Assert
        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.isBloquejant()).isTrue();
        assertThat(conflict.getTipo()).isEqualTo("IndicadorExport");
        assertThat(conflict.getCodi()).isEqualTo("IND1");

        verify(i18nUtil).getI18nMessage(
                "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.indicador",
                "IND1",
                "App Test",
                "Entorn Test");
    }

    @Test
    @DisplayName("checkDashboardItemConflicts: afegeix conflicte bloquejant quan la Dimensio no existeix (Taula Widget)")
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
        ReflectionTestUtils.setField(entorn, "nom", "Entorn Test");
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        ReflectionTestUtils.setField(app, "nom", "App Test");
        EntornApp entornApp = new EntornApp();
        entornApp.setId(20L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);
        when(dimensioRepository.findByCodiAndEntornAppId("DIM1", 20L)).thenReturn(Optional.empty());

        // Act
        List<Conflict> conflicts = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, conflicts);

        // Assert
        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.isBloquejant()).isTrue();
        assertThat(conflict.getTipo()).isEqualTo("DimensioExport");
        assertThat(conflict.getCodi()).isEqualTo("DIM1");

        verify(i18nUtil).getI18nMessage(
                "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.dimensio",
                "DIM1",
                "App Test",
                "Entorn Test");
    }

    @Test
    @DisplayName("checkDashboardItemConflicts: afegeix conflicte bloquejant quan el DimensioValor no existeix")
    void checkDashboardItemConflicts_quanDimensioValorNoExisteix_afegeixConflicteBloquejant() {
        // Arrange
        DashboardItemExport item = new DashboardItemExport();
        item.setEntornCodi("ENT");
        item.setAppCodi("APP");

        EstadisticaSimpleWidgetExport widget = new EstadisticaSimpleWidgetExport();
        widget.setTitol("Widget Simple");
        DimensioValorExport dmv = new DimensioValorExport("DIM1", "VAL1");
        widget.setDimensionsValor(List.of(dmv));
        item.setWidget(widget);

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        ReflectionTestUtils.setField(entorn, "nom", "Entorn Test");
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        ReflectionTestUtils.setField(app, "nom", "App Test");
        EntornApp entornApp = new EntornApp();
        entornApp.setId(20L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("DIM1");
        dimensio.setEntornAppId(20L);
        when(dimensioRepository.findByCodiAndEntornAppId("DIM1", 20L)).thenReturn(Optional.of(dimensio));
        when(dimensioValorRepository.findByDimensioAndValor(dimensio, "VAL1")).thenReturn(Optional.empty());

        // Act
        List<Conflict> conflicts = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, conflicts);

        // Assert
        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.isBloquejant()).isTrue();
        assertThat(conflict.getTipo()).isEqualTo("DimensioValorExport");
        assertThat(conflict.getCodi()).isEqualTo("DIM1 (VAL1)");
        assertThat(conflict.getTitol()).isEqualTo("DIM1 (VAL1)");

        verify(i18nUtil).getI18nMessage(
                "es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.dimensioValor",
                "VAL1",
                "DIM1",
                "App Test",
                "Entorn Test");
    }

    @Test
    @DisplayName("checkDashboardItemConflicts: no afegeix conflicte quan el DimensioValor existeix")
    void checkDashboardItemConflicts_quanDimensioValorExisteix_noAfegeixConflicte() {
        // Arrange
        DashboardItemExport item = new DashboardItemExport();
        item.setEntornCodi("ENT");
        item.setAppCodi("APP");

        EstadisticaSimpleWidgetExport widget = new EstadisticaSimpleWidgetExport();
        widget.setTitol("Widget Simple");
        DimensioValorExport dmv = new DimensioValorExport("DIM1", "VAL1");
        widget.setDimensionsValor(List.of(dmv));
        item.setWidget(widget);

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        ReflectionTestUtils.setField(entorn, "nom", "Entorn Test");
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        ReflectionTestUtils.setField(app, "nom", "App Test");
        EntornApp entornApp = new EntornApp();
        entornApp.setId(20L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("DIM1");
        dimensio.setEntornAppId(20L);
        when(dimensioRepository.findByCodiAndEntornAppId("DIM1", 20L)).thenReturn(Optional.of(dimensio));
        when(dimensioValorRepository.findByDimensioAndValor(dimensio, "VAL1")).thenReturn(Optional.of(new DimensioValorEntity()));

        // Act
        List<Conflict> conflicts = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, conflicts);

        // Assert
        assertThat(conflicts).noneMatch(Conflict::isBloquejant);
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
    @DisplayName("getElementNewNom: trunca la base quan el nom generat superaria la longitud màxima de l'entitat")
    void getElementNewNom_quanNomExcedeixLongitudMaxima_llavorsTruncaLaBase() {
        String titol64 = "A".repeat(64);
        when(dashboardRepository.findByTitol(titol64)).thenReturn(new DashboardEntity());
        String expectedSuggestion = "A".repeat(60) + " (1)";
        when(dashboardRepository.findByTitol(expectedSuggestion)).thenReturn(null);

        String result = (String) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "getElementNewNom", titol64, null, "DashboardExport");

        assertThat(result).isEqualTo(expectedSuggestion);
        assertThat(result).hasSize(DashboardEntity.TITOL_MAX_LENGTH);
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

    @Test
    @DisplayName("importPlantilla: sobreescriu plantilla existent quan conflicte és SOBRESCRIURE")
    void importPlantilla_quanConflicteSobreescriure_llavorsSobreescriuPlantillaISincronitzaTot() {
        // Arrange
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setNom("Plantilla Test");
        plantilla.setMostrarVora(true);
        plantilla.setAmpleVora(4);
        plantilla.setMidaFontTitol("24px");

        PlantillaGrupPaletesEntity group = new PlantillaGrupPaletesEntity();
        group.setGroupType(PaletteGroupType.LIGHT);
        PaletaEntity wp = new PaletaEntity();
        wp.setNom("WPal");
        PaletaEntity cp = new PaletaEntity();
        cp.setNom("CPal");
        group.setWidgetPalette(wp);
        group.setChartPalette(cp);
        plantilla.setPaletteGroups(Collections.singletonList(group));

        WidgetStylePropertyEntity prop = new WidgetStylePropertyEntity();
        prop.setScope(WidgetStyleScope.COMMON);
        prop.setPropertyName("color");
        prop.setValueType(WidgetStyleValueType.COLOR);
        prop.setScalarValue("#111111");
        plantilla.setStyleProperties(Collections.singletonList(prop));

        Conflict conflict = new Conflict("Plantilla Test", PlantillaExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        PlantillaEntity existing = new PlantillaEntity();
        existing.setId(10L);
        existing.setNom("Plantilla Test");
        existing.setMostrarVora(false);
        existing.setAmpleVora(1);

        PlantillaGrupPaletesEntity oldGroup = new PlantillaGrupPaletesEntity();
        oldGroup.setId(101L);
        oldGroup.setGroupType(PaletteGroupType.DARK);
        oldGroup.setPlantilla(existing);

        WidgetStylePropertyEntity oldProp = new WidgetStylePropertyEntity();
        oldProp.setId(201L);
        oldProp.setScope(WidgetStyleScope.COMMON);
        oldProp.setPropertyName("oldColor");
        oldProp.setPlantilla(existing);

        when(plantillaRepository.findByNom("Plantilla Test")).thenReturn(Optional.of(existing));
        when(dashboardTemplatePaletteGroupRepository.findByPlantillaId(10L)).thenReturn(new ArrayList<>(Collections.singletonList(oldGroup)));
        when(widgetStylePropertyRepository.findByPlantillaId(10L)).thenReturn(new ArrayList<>(Collections.singletonList(oldProp)));
        when(plantillaRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        // Act
        PlantillaEntity result = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPlantilla", plantilla, Collections.singletonList(conflict));

        // Assert
        assertThat(result).isSameAs(existing);
        assertThat(result.getMostrarVora()).isTrue();
        assertThat(result.getAmpleVora()).isEqualTo(4);
        assertThat(result.getMidaFontTitol()).isEqualTo("24px");

        // Grups: eliminació de l'antic i guardat del nou
        verify(dashboardTemplatePaletteGroupRepository).deleteAll(argThat(it -> {
            List<PlantillaGrupPaletesEntity> list = StreamSupport.stream(it.spliterator(), false).collect(Collectors.toList());
            return list.size() == 1 && list.contains(oldGroup);
        }));
        verify(dashboardTemplatePaletteGroupRepository).saveAll(argThat(it -> {
            List<PlantillaGrupPaletesEntity> list = StreamSupport.stream(it.spliterator(), false).collect(Collectors.toList());
            return list.size() == 1 && list.get(0).getGroupType() == PaletteGroupType.LIGHT;
        }));

        // Propietats: eliminació de l'antiga i guardat de la nova
        verify(widgetStylePropertyRepository).deleteAll(argThat(it -> {
            List<WidgetStylePropertyEntity> list = StreamSupport.stream(it.spliterator(), false).collect(Collectors.toList());
            return list.size() == 1 && list.contains(oldProp);
        }));
        verify(widgetStylePropertyRepository).saveAll(argThat(it -> {
            List<WidgetStylePropertyEntity> list = StreamSupport.stream(it.spliterator(), false).collect(Collectors.toList());
            return list.size() == 1 && "color".equals(list.get(0).getPropertyName());
        }));

        verify(plantillaRepository).save(existing);
        verify(estadisticaWidgetHelper).clearDashboardWidgetCacheByPlantilla(10L);
    }

    @Test
    @DisplayName("importPaleta: sobreescriu paleta existent quan conflicte és SOBRESCRIURE")
    void importPaleta_quanConflicteSobreescriure_llavorsSobreescriuPaletaISincronitzaColors() {
        // Arrange
        PaletaEntity paleta = new PaletaEntity();
        paleta.setNom("Paleta Test");
        paleta.setDescripcio("Nova Descripció");

        PaletaColorEntity c0 = new PaletaColorEntity();
        c0.setPosicio(0);
        c0.setValor("#000000");

        PaletaColorEntity c1 = new PaletaColorEntity();
        c1.setPosicio(1);
        c1.setValor("#FFFFFF");

        paleta.setColors(Arrays.asList(c0, c1));

        Conflict conflict = new Conflict("Paleta Test", PaletaExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        PaletaEntity existing = new PaletaEntity();
        existing.setId(50L);
        existing.setNom("Paleta Test");
        existing.setDescripcio("Vella Descripció");

        PaletaColorEntity oldC0 = new PaletaColorEntity();
        oldC0.setId(501L);
        oldC0.setPosicio(0);
        oldC0.setValor("#111111");

        PaletaColorEntity oldC2 = new PaletaColorEntity();
        oldC2.setId(502L);
        oldC2.setPosicio(2);
        oldC2.setValor("#222222");

        when(paletaRepository.findByNom("Paleta Test")).thenReturn(Optional.of(existing));
        when(paletaColorRepository.findByPaletaId(50L)).thenReturn(new ArrayList<>(Arrays.asList(oldC0, oldC2)));
        when(paletaRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        PlantillaEntity plantillaRelacionada = new PlantillaEntity();
        plantillaRelacionada.setId(99L);
        PlantillaGrupPaletesEntity groupUsingPalette = new PlantillaGrupPaletesEntity();
        groupUsingPalette.setPlantilla(plantillaRelacionada);
        when(dashboardTemplatePaletteGroupRepository.findByWidgetPaletteIdOrChartPaletteId(50L, 50L))
                .thenReturn(Collections.singletonList(groupUsingPalette));

        // Act
        PaletaEntity result = (PaletaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPaleta", paleta, Collections.singletonList(conflict));

        // Assert
        assertThat(result).isSameAs(existing);
        assertThat(result.getDescripcio()).isEqualTo("Nova Descripció");

        // El color en pos 0 s'ha actualitzat mantenint la mateixa instància (id 501), pos 1 és nou, pos 2 s'esborra
        verify(paletaColorRepository).deleteAll(argThat(it -> {
            List<PaletaColorEntity> list = StreamSupport.stream(it.spliterator(), false).collect(Collectors.toList());
            return list.size() == 1 && list.contains(oldC2);
        }));
        verify(paletaColorRepository).saveAll(argThat(it -> {
            List<PaletaColorEntity> l = StreamSupport.stream(it.spliterator(), false).collect(Collectors.toList());
            return l.size() == 2 && "#000000".equals(l.get(0).getValor()) && "#FFFFFF".equals(l.get(1).getValor());
        }));

        verify(paletaRepository).save(existing);
        verify(estadisticaWidgetHelper).clearDashboardWidgetCacheByPlantilla(99L);
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


        when(dashboardRepository.findByTitol("Dash Test")).thenReturn(new DashboardEntity());
        when(estadisticaWidgetRepository.findByAppIdAndTitol(2L, "Widget Test")).thenReturn(new EstadisticaSimpleWidgetEntity());

        WidgetStylePropertyEntity diffProp = new WidgetStylePropertyEntity();
        diffProp.setScope(WidgetStyleScope.COMMON);
        diffProp.setPropertyName("colorFons");
        PlantillaEntity dbPlantillaTitol = new PlantillaEntity();
        dbPlantillaTitol.setStyleProperties(List.of(diffProp));
        PlantillaEntity dbPlantillaDash = new PlantillaEntity();
        dbPlantillaDash.setStyleProperties(List.of(diffProp));

        when(plantillaRepository.findByNom("Plantilla Titol")).thenReturn(Optional.of(dbPlantillaTitol));
        when(plantillaRepository.findByNom("Plantilla Dash")).thenReturn(Optional.of(dbPlantillaDash));
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
        assertThat(conflicts).allSatisfy(c -> {
            assertThat(c.getSuggerenciaNouNom()).isNotNull();
            assertThat(c.getSuggerenciaNouNom()).isEqualTo(c.getTitol() + " (1)");
        });
        Conflict dashConflict = conflicts.stream()
                .filter(c -> DashboardExport.class.getSimpleName().equals(c.getTipo()))
                .findFirst().orElseThrow();
        assertThat(dashConflict.getOverwrite()).isEqualTo(OverwriteEnum.CREAR_AMB_ALTRE_NOM);
    }

    @Test
    @DisplayName("getElementNewNom: retorna el nom original sense modificar si s'esgoten els intents màxims")
    void getElementNewNom_quanSExhaureixenIntents_llavorsRetornaNomOriginal() {
        // Arrange - Qualsevol nom existeix a la base de dades
        when(dashboardRepository.findByTitol(any())).thenReturn(new DashboardEntity());

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(
            dashboardImportHelper, "getElementNewNom", "Dash Test", null, DashboardExport.class.getSimpleName());

        // Assert - Ha de retornar el nom original sense modificar
        assertThat(result).isEqualTo("Dash Test");
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
        paleta1.setColors("#fff");
        PaletaExport paleta2 = new PaletaExport();
        paleta2.setNom("Paleta Chart");
        paleta2.setColors("#000");

        PlantillaGrupPaletesExport grup = new PlantillaGrupPaletesExport();
        grup.setWidgetPalette(paleta1);
        grup.setChartPalette(paleta2);
        plantilla.setPaletteGroups(Collections.singletonList(grup));

        List<Conflict> conflicts = new ArrayList<>();

        PaletaEntity dbPaleta1 = new PaletaEntity();
        dbPaleta1.setNom("Paleta Widget");
        PaletaColorEntity c1 = new PaletaColorEntity(); c1.setPosicio(0); c1.setValor("#111");
        dbPaleta1.setColors(List.of(c1));

        PaletaEntity dbPaleta2 = new PaletaEntity();
        dbPaleta2.setNom("Paleta Chart");
        PaletaColorEntity c2 = new PaletaColorEntity(); c2.setPosicio(0); c2.setValor("#222");
        dbPaleta2.setColors(List.of(c2));

        when(plantillaRepository.findByNom(any())).thenReturn(Optional.of(new PlantillaEntity()));
        when(paletaRepository.findByNom("Paleta Widget")).thenReturn(Optional.of(dbPaleta1));
        when(paletaRepository.findByNom("Paleta Chart")).thenReturn(Optional.of(dbPaleta2));

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

    @Test
    @DisplayName("validateDashboardExport: quan la llista és buida o null, llança IllegalArgumentException")
    void validateDashboardExport_quanLlistaBuida_llançaExcepcio() {
        assertThatThrownBy(() -> dashboardImportHelper.validateDashboardExport(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.buit");

        assertThatThrownBy(() -> dashboardImportHelper.validateDashboardExport(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.buit");
    }

    @Test
    @DisplayName("validateDashboardExport: quan hi ha violacions de validació, llança IllegalArgumentException amb detall")
    void validateDashboardExport_quanHiHaViolacions_llançaExcepcio() {
        DashboardExport export = new DashboardExport();
        javax.validation.ConstraintViolation<DashboardExport> violation = mock(javax.validation.ConstraintViolation.class);
        javax.validation.Path path = mock(javax.validation.Path.class);
        when(path.toString()).thenReturn("titol");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("no pot ser buit");

        Set<javax.validation.ConstraintViolation<DashboardExport>> violations = Collections.singleton(violation);
        when(validator.validate(export)).thenReturn(violations);

        assertThatThrownBy(() -> dashboardImportHelper.validateDashboardExport(Collections.singletonList(export)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("es.caib.comanda.estadistica.logic.helper.DashboardImportHelper.error.dadesInvalides");
    }

    @Test
    @DisplayName("validateDashboardExport: quan no hi ha violacions, no llança cap excepció")
    void validateDashboardExport_quanTotCorrecte_noLlançaExcepcio() {
        DashboardExport export = new DashboardExport();
        when(validator.validate(export)).thenReturn(Collections.emptySet());

        dashboardImportHelper.validateDashboardExport(Collections.singletonList(export));

        verify(validator).validate(export);
    }

    // ========================================================================
    // 6. TESTOS PER A INDICADORS INCLOSOS A L'EXPORTACIÓ (FORMULA auto-creables)
    // ========================================================================

    @Test
    @DisplayName("checkDashboardItemConflicts: no llança excepció quan l'indicador FORMULA que falta és a dashboard.indicadors")
    void checkDashboardItemConflicts_quanFormulaPendentImportacio_noLlancaExcepcio() {
        // Arrange
        DashboardItemExport item = new DashboardItemExport();
        item.setEntornCodi("ENT");
        item.setAppCodi("APP");

        EstadisticaSimpleWidgetExport widget = new EstadisticaSimpleWidgetExport();
        widget.setTitol("Widget");
        IndicadorTaulaExport indInfo = new IndicadorTaulaExport();
        indInfo.setIndicadorCodi("FORM");
        widget.setIndicadorInfo(indInfo);
        widget.setDimensionsValor(new ArrayList<>());
        item.setWidget(widget);

        DashboardExport dashboard = new DashboardExport();
        IndicadorExport indicadorExport = IndicadorExport.builder()
                .codi("FORM").entornCodi("ENT").appCodi("APP")
                .tipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus.FORMULA)
                .build();
        dashboard.setIndicadors(List.of(indicadorExport));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);
        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(anyLong(), anyLong())).thenReturn(entornApp);
        when(indicadorRepository.findByCodiAndEntornAppId("FORM", 10L)).thenReturn(Optional.empty());

        // Act & Assert (no ha de llançar excepció)
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkDashboardItemConflicts", item, dashboard, new ArrayList<Conflict>());
    }

    @Test
    @DisplayName("importDashboardFromExport: crea prèviament els indicadors FORMULA de l'exportació")
    void importDashboardFromExport_quanDashboardTeIndicadors_creaIndicadorsFormulaAbansDeConvertir() {
        // Arrange
        DashboardExport export = new DashboardExport();
        IndicadorExport indicadorExport = IndicadorExport.builder().codi("FORM")
                .tipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus.FORMULA).build();
        export.setIndicadors(List.of(indicadorExport));
        List<DashboardExport> exports = Collections.singletonList(export);

        DashboardEntity entity = new DashboardEntity();
        entity.setTitols(new ArrayList<>());
        entity.setItems(new ArrayList<>());
        when(dashboardExportMapper.toDashboardEntity(eq(exports), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(entity));

        // Act
        dashboardImportHelper.importDashboardFromExport(exports, Collections.emptyList());

        // Assert
        verify(indicadorExportHelper).importIndicadorsFormula(eq(export), anyList(), anyMap());
        verify(dashboardExportMapper).toDashboardEntity(eq(exports), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("checkIndicadorConflicts: detecta conflicte quan l'indicador ja existeix per nom")
    void checkIndicadorConflicts_quanExisteixPerNom_afegeixConflicte() {
        DashboardExport dashboard = new DashboardExport();
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");
        IndicadorExport indExport = IndicadorExport.builder()
                .codi("IND_FORM").nom("Indicador Formula").tipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus.FORMULA)
                .entornCodi("ENT").appCodi("APP").build();
        dashboard.setIndicadors(List.of(indExport));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);

        IndicadorEntity existent = new IndicadorEntity();
        existent.setCodi("IND_FORM_EXISTENT");
        existent.setNom("Indicador Formula");
        when(indicadorRepository.findByNomAndEntornAppId("Indicador Formula", 10L)).thenReturn(Optional.of(existent));

        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.getTipo()).isEqualTo("IndicadorExport");
        assertThat(conflict.getTitol()).isEqualTo("Indicador Formula");
        assertThat(conflict.getCodi()).isEqualTo("IND_FORM");
        assertThat(conflict.getEntornAppId()).isEqualTo(10L);
        assertThat(conflict.isBloquejant()).isTrue();
    }

    @Test
    @DisplayName("checkIndicadorConflicts: detecta conflicte quan l'indicador ja existeix per codi")
    void checkIndicadorConflicts_quanExisteixPerCodi_afegeixConflicte() {
        DashboardExport dashboard = new DashboardExport();
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");
        IndicadorExport indExport = IndicadorExport.builder()
                .codi("IND_FORM").nom("Indicador Nou").tipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus.FORMULA)
                .entornCodi("ENT").appCodi("APP").build();
        dashboard.setIndicadors(List.of(indExport));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);

        when(indicadorRepository.findByNomAndEntornAppId("Indicador Nou", 10L)).thenReturn(Optional.empty());
        IndicadorEntity existent = new IndicadorEntity();
        existent.setCodi("IND_FORM");
        existent.setNom("Indicador Amb Codi Existent");
        when(indicadorRepository.findByCodiAndEntornAppId("IND_FORM", 10L)).thenReturn(Optional.of(existent));

        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        assertThat(conflicts).hasSize(1);
        Conflict conflict = conflicts.get(0);
        assertThat(conflict.getTipo()).isEqualTo("IndicadorExport");
        assertThat(conflict.getCodi()).isEqualTo("IND_FORM");
        assertThat(conflict.isBloquejant()).isFalse();
    }

    @Test
    @DisplayName("checkIndicadorConflicts: no afegeix conflicte quan l'indicador no existeix a la base de dades")
    void checkIndicadorConflicts_quanNoExisteix_noAfegeixConflicte() {
        DashboardExport dashboard = new DashboardExport();
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");
        IndicadorExport indExport = IndicadorExport.builder()
                .codi("IND_FORM").nom("Indicador Nou").tipus(es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTipus.FORMULA)
                .entornCodi("ENT").appCodi("APP").build();
        dashboard.setIndicadors(List.of(indExport));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);

        when(indicadorRepository.findByNomAndEntornAppId("Indicador Nou", 10L)).thenReturn(Optional.empty());
        when(indicadorRepository.findByCodiAndEntornAppId("IND_FORM", 10L)).thenReturn(Optional.empty());

        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        assertThat(conflicts).isEmpty();
    }

    @Test
    @DisplayName("addBlockingConflict: permet conflictes bloquejants per al mateix codi i app si entornAppId és diferent, però evita duplicats si és igual")
    void addBlockingConflict_quanDiferentEntornAppId_afegeixAmbdosConflictes() {
        List<Conflict> conflicts = new ArrayList<>();

        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "addBlockingConflict", "IND", "IND", 1L, 10L, "IndicadorExport", "Error Entorn 1", conflicts);
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "addBlockingConflict", "IND", "IND", 1L, 20L, "IndicadorExport", "Error Entorn 2", conflicts);

        assertThat(conflicts).hasSize(2);
        assertThat(conflicts.get(0).getEntornAppId()).isEqualTo(10L);
        assertThat(conflicts.get(1).getEntornAppId()).isEqualTo(20L);

        // Si s'intenta afegir un duplicat per al mateix entornAppId, no s'afegeix
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "addBlockingConflict", "IND", "IND", 1L, 10L, "IndicadorExport", "Error Entorn 1 duplicat", conflicts);
        assertThat(conflicts).hasSize(2);
    }

    // ========================================================================
    // TESTOS PER A DUPLICATS EN EL MATEIX FITXER D'IMPORTACIÓ (POINT 3)
    // ========================================================================

    @Test
    @DisplayName("importPlantilla: quan conflicte és null però la plantilla ja ha estat guardada a la BBDD, retorna l'existent sense duplicar save")
    void importPlantilla_quanConflicteNullIExisteixEnBBDD_llavorsRetornaExistentISenseGuardar() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setNom("Plantilla Compartida");

        PlantillaEntity existent = new PlantillaEntity();
        existent.setNom("Plantilla Compartida");
        when(plantillaRepository.findByNom("Plantilla Compartida")).thenReturn(Optional.of(existent));

        PlantillaEntity result = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPlantilla", plantilla, Collections.emptyList());

        assertThat(result).isSameAs(existent);
        verify(plantillaRepository, never()).save(any());
    }

    @Test
    @DisplayName("importPaleta: quan conflicte és null però la paleta ja ha estat guardada a la BBDD, retorna l'existent sense duplicar save")
    void importPaleta_quanConflicteNullIExisteixEnBBDD_llavorsRetornaExistentISenseGuardar() {
        PaletaEntity paleta = new PaletaEntity();
        paleta.setNom("Paleta Compartida");

        PaletaEntity existent = new PaletaEntity();
        existent.setNom("Paleta Compartida");
        when(paletaRepository.findByNom("Paleta Compartida")).thenReturn(Optional.of(existent));

        PaletaEntity result = (PaletaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPaleta", paleta, Collections.emptyList());

        assertThat(result).isSameAs(existent);
        verify(paletaRepository, never()).save(any());
    }

    @Test
    @DisplayName("importWidget: quan conflicte és null però el widget ja ha estat guardat a la BBDD, retorna l'existent sense duplicar save")
    void importWidget_quanConflicteNullIExisteixEnBBDD_llavorsRetornaExistentISenseGuardar() {
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Widget Compartit");
        widget.setAppId(10L);

        EstadisticaWidgetEntity existent = new EstadisticaSimpleWidgetEntity();
        existent.setTitol("Widget Compartit");
        existent.setAppId(10L);
        when(estadisticaWidgetRepository.findByAppIdAndTitol(10L, "Widget Compartit")).thenReturn(existent);

        EstadisticaWidgetEntity result = (EstadisticaWidgetEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importWidget", widget, Collections.emptyList());

        assertThat(result).isSameAs(existent);
        verify(estadisticaWidgetRepository, never()).save(any());
    }

    @Test
    @DisplayName("importPlantilla: reutilitza la paleta ja guardada quan s'utilitza a múltiples llocs dins la mateixa importació")
    void importPlantilla_quanMateixaPaletaEnDosGrups_llavorsGuardaPaletaUnSolCop() {
        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setNom("Plantilla Nova");

        PaletaEntity paletaCompartida = new PaletaEntity();
        paletaCompartida.setNom("Paleta Compartida");
        paletaCompartida.setColors(new ArrayList<>());

        PlantillaGrupPaletesEntity grup1 = new PlantillaGrupPaletesEntity();
        grup1.setPlantilla(plantilla);
        grup1.setWidgetPalette(paletaCompartida);

        PlantillaGrupPaletesEntity grup2 = new PlantillaGrupPaletesEntity();
        grup2.setPlantilla(plantilla);
        grup2.setWidgetPalette(paletaCompartida);

        plantilla.setPaletteGroups(List.of(grup1, grup2));
        plantilla.setStyleProperties(new ArrayList<>());

        when(plantillaRepository.findByNom("Plantilla Nova")).thenReturn(Optional.empty());

        PaletaEntity paletaJaGuardada = new PaletaEntity();
        paletaJaGuardada.setNom("Paleta Compartida");

        // La primera crida no troba la paleta (es guardarà), la segona ja la troba
        when(paletaRepository.findByNom("Paleta Compartida"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(paletaJaGuardada));

        PlantillaEntity result = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPlantilla", plantilla, Collections.emptyList());

        assertThat(result).isNotNull();
        verify(plantillaRepository, times(1)).save(plantilla);
        // La paleta només s'ha de guardar una sola vegada!
        verify(paletaRepository, times(1)).save(paletaCompartida);
        assertThat(grup2.getWidgetPalette()).isSameAs(paletaJaGuardada);
    }

    @Test
    @DisplayName("importPlantilla, importPaleta, importWidget: retornen null quan l'entitat d'entrada és null")
    void importMethods_quanEntitatsNull_llavorsRetornaNull() {
        PlantillaEntity resultPlantilla = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPlantilla", (PlantillaEntity) null, Collections.emptyList());
        assertThat(resultPlantilla).isNull();

        PaletaEntity resultPaleta = (PaletaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPaleta", (PaletaEntity) null, Collections.emptyList());
        assertThat(resultPaleta).isNull();

        EstadisticaWidgetEntity resultWidget = (EstadisticaWidgetEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importWidget", (EstadisticaWidgetEntity) null, Collections.emptyList());
        assertThat(resultWidget).isNull();
    }

    // ========================================================================
    // TESTOS PER A FILTRES DE CAPÇALERA (DashboardFiltreEntity)
    // ========================================================================

    @Test
    @DisplayName("checkDashboardConflicts: filtre DIMENSIO amb dimensioCodi inexistent genera conflicte bloquejant")
    void checkDashboardConflicts_quanFiltreDimensioNoExisteix_llavorsAfegeixConflicteBloquejant() {
        DashboardExport dashboard = new DashboardExport();
        dashboard.setTitol("Dash amb filtre");
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");

        DashboardFiltreExport filtre = new DashboardFiltreExport();
        filtre.setTipus(DashboardFiltreTipus.DIMENSIO);
        filtre.setDimensioCodi("DIM_INEXISTENT");
        filtre.setOrdre(1);
        dashboard.setFiltres(List.of(filtre));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);
        when(dimensioRepository.findByCodiAndEntornAppId("DIM_INEXISTENT", 10L)).thenReturn(Optional.empty());

        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        // Ha d'haver-hi almenys un conflicte bloquejant per la dimensió
        assertThat(conflicts.stream().anyMatch(c -> c.isBloquejant() && "DimensioExport".equals(c.getTipo()))).isTrue();
    }

    @Test
    @DisplayName("checkDashboardConflicts: filtre PERIODE no valida dimensioCodi")
    void checkDashboardConflicts_quanFiltrePeriode_noValidaDimensio() {
        DashboardExport dashboard = new DashboardExport();
        dashboard.setTitol("Dash amb filtre període");
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");

        DashboardFiltreExport filtre = new DashboardFiltreExport();
        filtre.setTipus(DashboardFiltreTipus.PERIODE);
        filtre.setDimensioCodi(null);
        filtre.setOrdre(1);
        dashboard.setFiltres(List.of(filtre));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);

        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        // Només el conflicte del dashboard (addConflict), cap bloquejant per dimensió
        assertThat(conflicts.stream().noneMatch(c -> c.isBloquejant() && "DimensioExport".equals(c.getTipo()))).isTrue();
    }

    @Test
    @DisplayName("checkDashboardConflicts: filtre DIMENSIO amb dimensioCodi existent no genera conflicte bloquejant")
    void checkDashboardConflicts_quanFiltreDimensioExisteix_noAfegeixConflicteBloquejant() {
        DashboardExport dashboard = new DashboardExport();
        dashboard.setTitol("Dash amb filtre vàlid");
        dashboard.setEntornCodi("ENT");
        dashboard.setAppCodi("APP");

        DashboardFiltreExport filtre = new DashboardFiltreExport();
        filtre.setTipus(DashboardFiltreTipus.DIMENSIO);
        filtre.setDimensioCodi("DIM_OK");
        filtre.setOrdre(1);
        dashboard.setFiltres(List.of(filtre));

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 1L);
        App app = new App();
        ReflectionTestUtils.setField(app, "id", 2L);
        EntornApp entornApp = new EntornApp();
        entornApp.setId(10L);

        when(estadisticaClientHelper.entornByCodi("ENT")).thenReturn(entorn);
        when(estadisticaClientHelper.appFindByCodi("APP")).thenReturn(app);
        when(estadisticaClientHelper.entornAppFindByAppAndEntorn(2L, 1L)).thenReturn(entornApp);

        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setCodi("DIM_OK");
        when(dimensioRepository.findByCodiAndEntornAppId("DIM_OK", 10L)).thenReturn(Optional.of(dimensio));

        List<Conflict> conflicts = new ArrayList<>();
        dashboardImportHelper.checkDashboardConflicts(dashboard, conflicts);

        assertThat(conflicts.stream().noneMatch(c -> c.isBloquejant() && "DimensioExport".equals(c.getTipo()))).isTrue();
    }

    @Test
    @DisplayName("importDashboardFromEntity: quan dashboard té filtres, guarda cada filtre amb la referència al dashboard")
    void importDashboardFromEntity_quanDashboardTeFiltres_llavorsGuardaFiltres() {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setTitol("Dash Import");

        DashboardFiltreEntity filtre1 = new DashboardFiltreEntity();
        filtre1.setTipus(DashboardFiltreTipus.DIMENSIO);
        filtre1.setDimensioCodi("DIM1");
        filtre1.setOrdre(1);

        DashboardFiltreEntity filtre2 = new DashboardFiltreEntity();
        filtre2.setTipus(DashboardFiltreTipus.PERIODE);
        filtre2.setOrdre(2);

        dashboard.setFiltres(new ArrayList<>(List.of(filtre1, filtre2)));

        dashboardImportHelper.importDashboardFromEntity(dashboard, Collections.emptyList());

        verify(dashboardRepository).save(dashboard);
        verify(dashboardFiltreRepository).save(filtre1);
        verify(dashboardFiltreRepository).save(filtre2);
        assertThat(filtre1.getDashboard()).isSameAs(dashboard);
        assertThat(filtre2.getDashboard()).isSameAs(dashboard);
    }

    @Test
    @DisplayName("importDashboardFromEntity: quan filtres és null, no falla")
    void importDashboardFromEntity_quanFiltresNull_noFalla() {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setTitol("Dash Sense Filtres");
        dashboard.setFiltres(null);

        dashboardImportHelper.importDashboardFromEntity(dashboard, Collections.emptyList());

        verify(dashboardRepository).save(dashboard);
        verify(dashboardFiltreRepository, never()).save(any());
    }

    // ========================================================================
    // TESTOS PER A DETECCIÓ DE DIFERÈNCIES EN PALETES I PLANTILLES (EN CASCADA)
    // ========================================================================

    @Test
    @DisplayName("hasPaletaDifferences: retorna false quan la paleta és idèntica a la BD")
    void hasPaletaDifferences_quanPaletaIdentica_retornaFalse() {
        PaletaExport export = PaletaExport.builder()
                .nom("Paleta Test")
                .descripcio("Descripció")
                .colors("#ffffff,#000000,#ff0000")
                .build();

        PaletaEntity existent = new PaletaEntity();
        existent.setNom("Paleta Test");
        existent.setDescripcio("Descripció");

        PaletaColorEntity c0 = new PaletaColorEntity(); c0.setPosicio(0); c0.setValor("#ffffff");
        PaletaColorEntity c1 = new PaletaColorEntity(); c1.setPosicio(1); c1.setValor("#000000");
        PaletaColorEntity c2 = new PaletaColorEntity(); c2.setPosicio(2); c2.setValor("#ff0000");
        existent.setColors(List.of(c0, c1, c2));

        boolean diff = dashboardImportHelper.hasPaletaDifferences(export, existent);
        assertThat(diff).isFalse();
    }

    @Test
    @DisplayName("hasPaletaDifferences: retorna true quan els colors de la paleta difereixen")
    void hasPaletaDifferences_quanColorsDiferents_retornaTrue() {
        PaletaExport export = PaletaExport.builder()
                .nom("Paleta Test")
                .descripcio("Descripció")
                .colors("#ffffff,#111111")
                .build();

        PaletaEntity existent = new PaletaEntity();
        existent.setNom("Paleta Test");
        existent.setDescripcio("Descripció");

        PaletaColorEntity c0 = new PaletaColorEntity(); c0.setPosicio(0); c0.setValor("#ffffff");
        PaletaColorEntity c1 = new PaletaColorEntity(); c1.setPosicio(1); c1.setValor("#000000");
        existent.setColors(List.of(c0, c1));

        boolean diff = dashboardImportHelper.hasPaletaDifferences(export, existent);
        assertThat(diff).isTrue();
    }

    @Test
    @DisplayName("hasPaletaDifferences: detecta com a diferència quan un camp és null al JSON però té valor a BD")
    void hasPaletaDifferences_quanJsonAnticAmbCampNull_retornaTrue() {
        PaletaExport export = PaletaExport.builder()
                .nom("Paleta Test")
                .descripcio(null) // JSON antic sense camp descripcio
                .colors("#ffffff")
                .build();

        PaletaEntity existent = new PaletaEntity();
        existent.setNom("Paleta Test");
        existent.setDescripcio("Descripció existent a BD");

        PaletaColorEntity c0 = new PaletaColorEntity(); c0.setPosicio(0); c0.setValor("#ffffff");
        existent.setColors(List.of(c0));

        boolean diff = dashboardImportHelper.hasPaletaDifferences(export, existent);
        assertThat(diff).isTrue();
    }

    @Test
    @DisplayName("hasPlantillaDifferences: retorna false quan la plantilla i les seves paletes són idèntiques a la BD")
    void hasPlantillaDifferences_quanTotIdentic_retornaFalse() {
        PaletaExport palWidget = PaletaExport.builder().nom("Pal Widget").descripcio("D1").colors("#fff").build();
        PaletaExport palChart = PaletaExport.builder().nom("Pal Chart").descripcio("D2").colors("#000").build();

        PlantillaGrupPaletesExport grupExport = PlantillaGrupPaletesExport.builder()
                .groupType(PaletteGroupType.LIGHT)
                .widgetPalette(palWidget)
                .chartPalette(palChart)
                .build();

        WidgetStylePropertyExport propExport = WidgetStylePropertyExport.builder()
                .scope(WidgetStyleScope.COMMON)
                .propertyName("colorFons")
                .valueType(WidgetStyleValueType.COLOR)
                .paletteRole(PaletteRole.WIDGET)
                .paletteIndex(0)
                .defaultProperty(true)
                .build();

        PlantillaExport plantillaExport = PlantillaExport.builder()
                .nom("Plantilla Test")
                .paletteGroups(List.of(grupExport))
                .styleProperties(List.of(propExport))
                .build();

        // Entitats BD
        PaletaEntity dbPalWidget = new PaletaEntity();
        dbPalWidget.setNom("Pal Widget"); dbPalWidget.setDescripcio("D1");
        PaletaColorEntity cw = new PaletaColorEntity(); cw.setPosicio(0); cw.setValor("#fff");
        dbPalWidget.setColors(List.of(cw));

        PaletaEntity dbPalChart = new PaletaEntity();
        dbPalChart.setNom("Pal Chart"); dbPalChart.setDescripcio("D2");
        PaletaColorEntity cc = new PaletaColorEntity(); cc.setPosicio(0); cc.setValor("#000");
        dbPalChart.setColors(List.of(cc));

        when(paletaRepository.findByNom("Pal Widget")).thenReturn(Optional.of(dbPalWidget));
        when(paletaRepository.findByNom("Pal Chart")).thenReturn(Optional.of(dbPalChart));

        PlantillaEntity dbPlantilla = new PlantillaEntity();
        dbPlantilla.setNom("Plantilla Test");

        PlantillaGrupPaletesEntity dbGrup = new PlantillaGrupPaletesEntity();
        dbGrup.setGroupType(PaletteGroupType.LIGHT);
        dbGrup.setWidgetPalette(dbPalWidget);
        dbGrup.setChartPalette(dbPalChart);
        dbPlantilla.setPaletteGroups(List.of(dbGrup));

        WidgetStylePropertyEntity dbProp = new WidgetStylePropertyEntity();
        dbProp.setScope(WidgetStyleScope.COMMON);
        dbProp.setPropertyName("colorFons");
        dbProp.setValueType(WidgetStyleValueType.COLOR);
        dbProp.setPaletteRole(PaletteRole.WIDGET);
        dbProp.setPaletteIndex(0);
        dbProp.setDefaultProperty(true);
        dbPlantilla.setStyleProperties(List.of(dbProp));

        boolean diff = dashboardImportHelper.hasPlantillaDifferences(plantillaExport, dbPlantilla);
        assertThat(diff).isFalse();
    }

    @Test
    @DisplayName("hasPlantillaDifferences: retorna true en cascada quan la plantilla és idèntica però 1 paleta té diferències")
    void hasPlantillaDifferences_quanPaletaDiferentEnCascada_retornaTrue() {
        // La paleta al JSON té color #fff, però a la BD té #111
        PaletaExport palWidget = PaletaExport.builder().nom("Pal Widget").descripcio("D1").colors("#fff").build();
        PaletaExport palChart = PaletaExport.builder().nom("Pal Chart").descripcio("D2").colors("#000").build();

        PlantillaGrupPaletesExport grupExport = PlantillaGrupPaletesExport.builder()
                .groupType(PaletteGroupType.LIGHT)
                .widgetPalette(palWidget)
                .chartPalette(palChart)
                .build();

        PlantillaExport plantillaExport = PlantillaExport.builder()
                .nom("Plantilla Test")
                .paletteGroups(List.of(grupExport))
                .styleProperties(Collections.emptyList())
                .build();

        PaletaEntity dbPalWidget = new PaletaEntity();
        dbPalWidget.setNom("Pal Widget"); dbPalWidget.setDescripcio("D1");
        PaletaColorEntity cw = new PaletaColorEntity(); cw.setPosicio(0); cw.setValor("#111"); // DIFERÈNCIA DE COLOR
        dbPalWidget.setColors(List.of(cw));

        PaletaEntity dbPalChart = new PaletaEntity();
        dbPalChart.setNom("Pal Chart"); dbPalChart.setDescripcio("D2");
        PaletaColorEntity cc = new PaletaColorEntity(); cc.setPosicio(0); cc.setValor("#000");
        dbPalChart.setColors(List.of(cc));

        when(paletaRepository.findByNom("Pal Widget")).thenReturn(Optional.of(dbPalWidget));
        lenient().when(paletaRepository.findByNom("Pal Chart")).thenReturn(Optional.of(dbPalChart));

        PlantillaEntity dbPlantilla = new PlantillaEntity();
        dbPlantilla.setNom("Plantilla Test");

        PlantillaGrupPaletesEntity dbGrup = new PlantillaGrupPaletesEntity();
        dbGrup.setGroupType(PaletteGroupType.LIGHT);
        dbGrup.setWidgetPalette(dbPalWidget);
        dbGrup.setChartPalette(dbPalChart);
        dbPlantilla.setPaletteGroups(List.of(dbGrup));
        dbPlantilla.setStyleProperties(Collections.emptyList());

        boolean diff = dashboardImportHelper.hasPlantillaDifferences(plantillaExport, dbPlantilla);
        assertThat(diff).isTrue();
    }

    @Test
    @DisplayName("checkPlantillaConflicts: no afegeix conflicte si la plantilla i les paletes són idèntiques a la BD")
    void checkPlantillaConflicts_quanIdentiques_noAfegeixConflicte() {
        PaletaExport palWidget = PaletaExport.builder().nom("Pal Widget").colors("#fff").build();
        PaletaExport palChart = PaletaExport.builder().nom("Pal Chart").colors("#000").build();

        PlantillaGrupPaletesExport grup = PlantillaGrupPaletesExport.builder()
                .groupType(PaletteGroupType.LIGHT)
                .widgetPalette(palWidget)
                .chartPalette(palChart)
                .build();

        PlantillaExport plantilla = PlantillaExport.builder()
                .nom("Plantilla Test")
                .paletteGroups(List.of(grup))
                .styleProperties(Collections.emptyList())
                .build();

        PaletaEntity dbPalWidget = new PaletaEntity();
        dbPalWidget.setNom("Pal Widget");
        PaletaColorEntity cw = new PaletaColorEntity(); cw.setPosicio(0); cw.setValor("#fff");
        dbPalWidget.setColors(List.of(cw));

        PaletaEntity dbPalChart = new PaletaEntity();
        dbPalChart.setNom("Pal Chart");
        PaletaColorEntity cc = new PaletaColorEntity(); cc.setPosicio(0); cc.setValor("#000");
        dbPalChart.setColors(List.of(cc));

        PlantillaEntity dbPlantilla = new PlantillaEntity();
        dbPlantilla.setNom("Plantilla Test");
        PlantillaGrupPaletesEntity dbGrup = new PlantillaGrupPaletesEntity();
        dbGrup.setGroupType(PaletteGroupType.LIGHT);
        dbGrup.setWidgetPalette(dbPalWidget);
        dbGrup.setChartPalette(dbPalChart);
        dbPlantilla.setPaletteGroups(List.of(dbGrup));
        dbPlantilla.setStyleProperties(Collections.emptyList());

        when(plantillaRepository.findByNom("Plantilla Test")).thenReturn(Optional.of(dbPlantilla));
        when(paletaRepository.findByNom("Pal Widget")).thenReturn(Optional.of(dbPalWidget));
        when(paletaRepository.findByNom("Pal Chart")).thenReturn(Optional.of(dbPalChart));

        List<Conflict> conflicts = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(dashboardImportHelper, "checkPlantillaConflicts", plantilla, conflicts);

        // Ni la plantilla ni les paletes s'han d'afegir com a conflicte perquè són idèntiques
        assertThat(conflicts).isEmpty();
    }

    @Test
    @DisplayName("hasPaletaDifferences: retorna false quan descripció a export és buida i a BD és null")
    void hasPaletaDifferences_quanDescripcioBuidaINull_retornaFalse() {
        PaletaExport export = PaletaExport.builder().nom("Pal").descripcio("").colors("#fff").build();
        PaletaEntity existent = new PaletaEntity();
        existent.setNom("Pal");
        existent.setDescripcio(null);
        PaletaColorEntity c = new PaletaColorEntity(); c.setPosicio(0); c.setValor("#fff");
        existent.setColors(List.of(c));

        boolean diff = dashboardImportHelper.hasPaletaDifferences(export, existent);
        assertThat(diff).isFalse();
    }

    @Test
    @DisplayName("hasPlantillaDifferences: retorna false quan propietat escalar a export és buida i a BD és null")
    void hasPlantillaDifferences_quanScalarPropertyBuidaINull_retornaFalse() {
        WidgetStylePropertyExport propExport = WidgetStylePropertyExport.builder()
                .scope(WidgetStyleScope.COMMON)
                .propertyName("text")
                .valueType(WidgetStyleValueType.TEXT)
                .scalarValue("")
                .build();

        PlantillaExport plantillaExport = PlantillaExport.builder()
                .nom("Plantilla Test")
                .paletteGroups(Collections.emptyList())
                .styleProperties(List.of(propExport))
                .build();

        PlantillaEntity dbPlantilla = new PlantillaEntity();
        dbPlantilla.setNom("Plantilla Test");
        dbPlantilla.setPaletteGroups(Collections.emptyList());

        WidgetStylePropertyEntity dbProp = new WidgetStylePropertyEntity();
        dbProp.setScope(WidgetStyleScope.COMMON);
        dbProp.setPropertyName("text");
        dbProp.setValueType(WidgetStyleValueType.TEXT);
        dbProp.setScalarValue(null);
        dbPlantilla.setStyleProperties(List.of(dbProp));

        boolean diff = dashboardImportHelper.hasPlantillaDifferences(plantillaExport, dbPlantilla);
        assertThat(diff).isFalse();
    }

    @Test
    @DisplayName("importPlantilla: sobreescriure no esborra propietats escalars no presents a l'export")
    void importPlantilla_quanSobreescriureAmbSourceNull_mantéPropietatsBD() {
        PlantillaEntity plantillaSource = new PlantillaEntity();
        plantillaSource.setNom("Plantilla Test");
        plantillaSource.setPaletteGroups(Collections.emptyList());
        plantillaSource.setStyleProperties(Collections.emptyList());
        // source no té mostrarVora ni ampleVora (són null)

        Conflict conflict = new Conflict("Plantilla Test", PlantillaExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        PlantillaEntity existing = new PlantillaEntity();
        existing.setId(25L);
        existing.setNom("Plantilla Test");
        existing.setMostrarVora(true);
        existing.setAmpleVora(8);

        when(plantillaRepository.findByNom("Plantilla Test")).thenReturn(Optional.of(existing));
        when(dashboardTemplatePaletteGroupRepository.findByPlantillaId(25L)).thenReturn(new ArrayList<>());
        when(widgetStylePropertyRepository.findByPlantillaId(25L)).thenReturn(new ArrayList<>());
        when(plantillaRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        PlantillaEntity result = (PlantillaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPlantilla", plantillaSource, Collections.singletonList(conflict));

        assertThat(result.getMostrarVora()).isTrue();
        assertThat(result.getAmpleVora()).isEqualTo(8);
    }

    @Test
    @DisplayName("hasPaletaDifferences: retorna false quan els colors només difereixen en majúscules/minúscules")
    void hasPaletaDifferences_quanColorsCaseInsensitive_retornaFalse() {
        PaletaExport export = PaletaExport.builder()
                .nom("Paleta Test")
                .descripcio("Desc")
                .colors("#ffffff, #aabbcc")
                .build();

        PaletaEntity existent = new PaletaEntity();
        existent.setNom("Paleta Test");
        existent.setDescripcio("Desc");
        PaletaColorEntity c0 = new PaletaColorEntity(); c0.setPosicio(0); c0.setValor("#FFFFFF");
        PaletaColorEntity c1 = new PaletaColorEntity(); c1.setPosicio(1); c1.setValor("#AABBCC");
        existent.setColors(List.of(c0, c1));

        boolean diff = dashboardImportHelper.hasPaletaDifferences(export, existent);
        assertThat(diff).isFalse();
    }

    @Test
    @DisplayName("syncPlantillaStyleProperties: actualitza propietat existent preservant l'ordre i id")
    void syncPlantillaStyleProperties_actualitzaPropietatExistent() {
        PlantillaEntity entity = new PlantillaEntity();
        entity.setId(10L);

        WidgetStylePropertyEntity existingProp = new WidgetStylePropertyEntity();
        existingProp.setId(100L);
        existingProp.setPlantilla(entity);
        existingProp.setScope(WidgetStyleScope.COMMON);
        existingProp.setPropertyName("colorFons");
        existingProp.setValueType(WidgetStyleValueType.COLOR);
        existingProp.setScalarValue("#111111");
        existingProp.setOrdre(0);

        when(widgetStylePropertyRepository.findByPlantillaId(10L)).thenReturn(new ArrayList<>(List.of(existingProp)));

        WidgetStylePropertyEntity newProp = new WidgetStylePropertyEntity();
        newProp.setScope(WidgetStyleScope.COMMON);
        newProp.setPropertyName("colorFons");
        newProp.setValueType(WidgetStyleValueType.COLOR);
        newProp.setScalarValue("#222222"); // Nou valor
        newProp.setOrdre(0);

        ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "syncPlantillaStyleProperties", entity, List.of(newProp));

        assertThat(entity.getStyleProperties()).hasSize(1);
        WidgetStylePropertyEntity saved = entity.getStyleProperties().get(0);
        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getScalarValue()).isEqualTo("#222222");
    }

    @Test
    @DisplayName("syncPlantillaStyleProperties: sobreescriu propietats a null quan s'elimina color o valor")
    void syncPlantillaStyleProperties_quanValorsFontNull_llavorsSobreescriuAmbNull() {
        PlantillaEntity entity = new PlantillaEntity();
        entity.setId(10L);

        WidgetStylePropertyEntity existingProp = new WidgetStylePropertyEntity();
        existingProp.setId(100L);
        existingProp.setPlantilla(entity);
        existingProp.setScope(WidgetStyleScope.COMMON);
        existingProp.setPropertyName("colorFons");
        existingProp.setValueType(WidgetStyleValueType.COLOR);
        existingProp.setPaletteRole(PaletteRole.WIDGET);
        existingProp.setPaletteIndex(0);
        existingProp.setScalarValue("#111111");

        when(widgetStylePropertyRepository.findByPlantillaId(10L))
                .thenReturn(new ArrayList<>(List.of(existingProp)));

        WidgetStylePropertyEntity newProp = new WidgetStylePropertyEntity();
        newProp.setScope(WidgetStyleScope.COMMON);
        newProp.setPropertyName("colorFons");
        newProp.setValueType(WidgetStyleValueType.COLOR);
        newProp.setPaletteRole(null);
        newProp.setPaletteIndex(null);
        newProp.setScalarValue(null);

        ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "syncPlantillaStyleProperties", entity, List.of(newProp));

        ArgumentCaptor<List<WidgetStylePropertyEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(widgetStylePropertyRepository).saveAll(captor.capture());
        List<WidgetStylePropertyEntity> savedList = captor.getValue();
        assertThat(savedList).hasSize(1);
        WidgetStylePropertyEntity savedInDb = savedList.get(0);
        assertThat(savedInDb.getId()).isEqualTo(100L);
        assertThat(savedInDb.getPaletteIndex()).isNull();
        assertThat(savedInDb.getScalarValue()).isNull();
        assertThat(savedInDb.getPaletteRole()).isNull();
    }

    @Test
    @DisplayName("importPaleta: sobreescriu descripcio a null quan el conflicte és SOBRESCRIURE i la font té null")
    void importPaleta_quanConflicteSobreescriureIDescripcioNull_llavorsSobreescriuDescripcioAmbNull() {
        PaletaEntity paleta = new PaletaEntity();
        paleta.setNom("Paleta Test Buidar");
        paleta.setDescripcio(null);
        paleta.setColors(Collections.emptyList());

        Conflict conflict = new Conflict("Paleta Test Buidar", PaletaExport.class.getSimpleName());
        conflict.setOverwrite(OverwriteEnum.SOBRESCRIURE);

        PaletaEntity existing = new PaletaEntity();
        existing.setId(60L);
        existing.setNom("Paleta Test Buidar");
        existing.setDescripcio("Descripció existent que ha de desaparèixer");

        when(paletaRepository.findByNom("Paleta Test Buidar")).thenReturn(Optional.of(existing));
        when(paletaColorRepository.findByPaletaId(60L)).thenReturn(new ArrayList<>());
        when(paletaRepository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        PaletaEntity result = (PaletaEntity) ReflectionTestUtils.invokeMethod(
                dashboardImportHelper, "importPaleta", paleta, Collections.singletonList(conflict));

        assertThat(result).isSameAs(existing);
        assertThat(result.getDescripcio()).isNull();
        verify(paletaRepository).save(argThat(p -> p.getId().equals(60L) && p.getDescripcio() == null));
    }
}

