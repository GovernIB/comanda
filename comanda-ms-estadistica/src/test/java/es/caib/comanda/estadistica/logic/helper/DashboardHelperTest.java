package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.client.model.EntornApp;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.estadistica.persist.repository.EstadisticaWidgetRepository;
import es.caib.comanda.estadistica.persist.repository.PlantillaRepository;
import es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import es.caib.comanda.ms.logic.intf.util.I18nUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardHelper")
class DashboardHelperTest {

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;

    @Mock
    private DashboardRepository dashboardRepository;

    @Mock
    private DashboardTitolRepository dashboardTitolRepository;

    @Mock
    private DashboardItemRepository dashboardItemRepository;

    @Mock
    private PlantillaRepository plantillaRepository;

    @Mock
    private EstadisticaWidgetRepository estadisticaWidgetRepository;

    private final DashboardClonerMapper dashboardClonerMapper = Mappers.getMapper(DashboardClonerMapper.class);

    @Mock
    private I18nUtil i18nUtil;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private DashboardHelper dashboardHelper;

    @BeforeEach
    void setUp() {
        // Configuració per evitar NPE en crides estàtiques a I18nUtil
        ReflectionTestUtils.setField(I18nUtil.class, "applicationContext", applicationContext);
        lenient().when(applicationContext.getBean(I18nUtil.class)).thenReturn(i18nUtil);
        lenient().when(i18nUtil.getI18nMessage(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    // ========================================================================
    // 1. TESTOS PER A completeResourceLogic
    // ========================================================================

    @Test
    @DisplayName("completeResourceLogic: assigna appId i entornId quan aplicacio i entorn no són nulls")
    void completeResourceLogic_quanAplicacioIEntornNoNuls_llavorsAssignaIds() {
        // Arrange
        Dashboard resource = new Dashboard();
        resource.setAplicacio(ResourceReference.toResourceReference(10L, "App Test"));
        resource.setEntorn(ResourceReference.toResourceReference(20L, "Entorn Test"));

        // Act
        dashboardHelper.completeResourceLogic(resource);

        // Assert
        assertThat(resource.getAppId()).isEqualTo(10L);
        assertThat(resource.getEntornId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("completeResourceLogic: assigna nulls quan aplicacio i entorn són nulls")
    void completeResourceLogic_quanAplicacioIEntornNuls_llavorsAssignaNulls() {
        // Arrange
        Dashboard resource = new Dashboard();
        resource.setAplicacio(null);
        resource.setEntorn(null);

        // Act
        dashboardHelper.completeResourceLogic(resource);

        // Assert
        assertThat(resource.getAppId()).isNull();
        assertThat(resource.getEntornId()).isNull();
    }

    // ========================================================================
    // 2. TESTOS PER A afterConversionLogic
    // ========================================================================

    @Test
    @DisplayName("afterConversionLogic: assigna noms d'aplicació i entorn quan existeixen")
    void afterConversionLogic_quanAppIEntornExisteixen_llavorsAssignaNoms() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setAppId(10L);
        entity.setEntornId(20L);
        Dashboard resource = new Dashboard();

        App app = new App();
        ReflectionTestUtils.setField(app, "id", 10L);
        ReflectionTestUtils.setField(app, "nom", "App Nom");

        Entorn entorn = new Entorn();
        ReflectionTestUtils.setField(entorn, "id", 20L);
        ReflectionTestUtils.setField(entorn, "nom", "Entorn Nom");

        when(estadisticaClientHelper.appFindById(10L)).thenReturn(app);
        when(estadisticaClientHelper.entornById(20L)).thenReturn(entorn);

        // Act
        dashboardHelper.afterConversionLogic(entity, resource);

        // Assert
        assertThat(resource.getAplicacio()).isNotNull();
        assertThat(resource.getAplicacio().getId()).isEqualTo(10L);
        assertThat(resource.getAplicacio().getDescription()).isEqualTo("App Nom");

        assertThat(resource.getEntorn()).isNotNull();
        assertThat(resource.getEntorn().getId()).isEqualTo(20L);
        assertThat(resource.getEntorn().getDescription()).isEqualTo("Entorn Nom");
    }

    @Test
    @DisplayName("afterConversionGetAppNom: no fa res quan l'aplicació no existeix")
    void afterConversionGetAppNom_quanAppNoExisteix_llavorsNoFaRes() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setAppId(10L);
        Dashboard resource = new Dashboard();

        when(estadisticaClientHelper.appFindById(10L)).thenReturn(null);

        // Act
        ReflectionTestUtils.invokeMethod(dashboardHelper, "afterConversionGetAppNom", entity, resource);

        // Assert
        assertThat(resource.getAplicacio()).isNull();
    }

    @Test
    @DisplayName("afterConversionGetAppNom: logueja error quan es llança una excepció")
    void afterConversionGetAppNom_quanExcepcio_llavorsLoguejaError() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setAppId(10L);
        Dashboard resource = new Dashboard();

        when(estadisticaClientHelper.appFindById(10L)).thenThrow(new RuntimeException("Error de xarxa"));

        // Act
        ReflectionTestUtils.invokeMethod(dashboardHelper, "afterConversionGetAppNom", entity, resource);

        // Assert
        assertThat(resource.getAplicacio()).isNull();
    }

    @Test
    @DisplayName("afterConversionGetEntornNom: no fa res quan l'entorn no existeix")
    void afterConversionGetEntornNom_quanEntornNoExisteix_llavorsNoFaRes() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setEntornId(20L);
        Dashboard resource = new Dashboard();

        when(estadisticaClientHelper.entornById(20L)).thenReturn(null);

        // Act
        ReflectionTestUtils.invokeMethod(dashboardHelper, "afterConversionGetEntornNom", entity, resource);

        // Assert
        assertThat(resource.getEntorn()).isNull();
    }

    @Test
    @DisplayName("afterConversionGetEntornNom: logueja error quan es llança una excepció")
    void afterConversionGetEntornNom_quanExcepcio_llavorsLoguejaError() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setEntornId(20L);
        Dashboard resource = new Dashboard();

        when(estadisticaClientHelper.entornById(20L)).thenThrow(new RuntimeException("Error de xarxa"));

        // Act
        ReflectionTestUtils.invokeMethod(dashboardHelper, "afterConversionGetEntornNom", entity, resource);

        // Assert
        assertThat(resource.getEntorn()).isNull();
    }

    // ========================================================================
    // 3. TESTOS PER A beforeUpdateEntityLogic
    // ========================================================================

    @Test
    @DisplayName("beforeUpdateEntityLogic: retorna sense fer res quan la llista d'items és buida")
    void beforeUpdateEntityLogic_quanItemsBuits_llavorsRetornaSenseFerRes() throws ResourceNotUpdatedException {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setItems(Collections.emptyList());
        Dashboard resource = new Dashboard();
        Map<String, Object> answers = new HashMap<>();

        // Act
        dashboardHelper.beforeUpdateEntityLogic(entity, resource, (Map) answers);

        // Assert
        verify(estadisticaClientHelper, never()).entornAppFindByAppAndEntornOrDefaultNull(anyLong(), anyLong());
    }

    @Test
    @DisplayName("beforeUpdateChangeEntornApp: llança ResourceNotUpdatedException quan la resposta és refusada")
    void beforeUpdateChangeEntornApp_quanRespostaRefusada_llavorsLlancaResourceNotUpdatedException() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setItems(Collections.singletonList(new DashboardItemEntity()));

        Dashboard resource = new Dashboard();
        resource.setAppId(10L); // Canvi d'AppId

        Map<String, Object> answers = new HashMap<>();
        answers.put(DashboardHelper.ANSWER_CODE_APP_ID, mock(Object.class)); // Simula que hi ha resposta però no és vàlida o es vol forçar el refús

        // Act & Assert
        assertThatThrownBy(() -> dashboardHelper.beforeUpdateEntityLogic(entity, resource, (Map) answers))
            .isInstanceOf(ResourceNotUpdatedException.class);
    }

    @Test
    @DisplayName("beforeUpdateChangeEntornApp: retorna sense fer res quan no hi ha canvis d'AppId ni EntornId")
    void beforeUpdateChangeEntornApp_quanNoCanvisAppIEntorn_llavorsRetornaSenseFerRes() throws ResourceNotUpdatedException {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setAppId(10L);
        entity.setEntornId(20L);
        entity.setItems(Collections.singletonList(new DashboardItemEntity()));

        Dashboard resource = new Dashboard();
        resource.setAppId(10L);
        resource.setEntornId(20L);

        Map<String, Object> answers = new HashMap<>();

        // Act
        dashboardHelper.beforeUpdateEntityLogic(entity, resource, (Map) answers);

        // Assert
        verify(estadisticaClientHelper, never()).entornAppFindByAppAndEntornOrDefaultNull(anyLong(), anyLong());
    }

    @Test
    @DisplayName("beforeUpdateChangeEntornApp: llança AnswerRequiredException quan el widget no és compatible amb la nova App")
    void beforeUpdateChangeEntornApp_quanCanviAppIdIWidgetNoCompatible_llavorsLlancaAnswerRequiredException() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setAppId(10L);

        DashboardItemEntity item = new DashboardItemEntity();
        EstadisticaWidgetEntity<?> widget = mock(EstadisticaWidgetEntity.class);
        when(widget.getAppId()).thenReturn(99L); // AppId diferent
        item.setWidget(widget);
        entity.setItems(Collections.singletonList(item));

        Dashboard resource = new Dashboard();
        resource.setAppId(20L); // Canvi d'AppId

        Map<String, Object> answers = new HashMap<>();

        // Act & Assert
        assertThatThrownBy(() -> dashboardHelper.beforeUpdateEntityLogic(entity, resource, (Map) answers))
            .isInstanceOf(AnswerRequiredException.class)
            .hasFieldOrPropertyWithValue("answerCode", DashboardHelper.ANSWER_CODE_APP_ID);
    }

    @Test
    @DisplayName("beforeUpdateChangeEntornApp: actualitza l'EntornId dels items quan existeix la nova EntornApp")
    void beforeUpdateChangeEntornApp_quanCanviAppIdIEntornAppExisteix_llavorsActualitzaEntornIdDelsItems() throws ResourceNotUpdatedException {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setAppId(10L);
        entity.setEntornId(20L);

        DashboardItemEntity item = new DashboardItemEntity();
        item.setEntornId(20L);
        EstadisticaWidgetEntity<?> widget = mock(EstadisticaWidgetEntity.class);
        widget.setAppId(10L);
        item.setWidget(widget);
        entity.setItems(Collections.singletonList(item));

        Dashboard resource = new Dashboard();
        resource.setAppId(10L);
        resource.setEntornId(30L); // Canvi d'EntornId

        EntornApp newEntornApp = new EntornApp();
        newEntornApp.setId(30L);

        Map<String, Object> answers = new HashMap<>();

        when(estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(10L, 30L)).thenReturn(newEntornApp);

        // Act
        dashboardHelper.beforeUpdateEntityLogic(entity, resource, (Map) answers);

        // Assert
        assertThat(item.getEntornId()).isEqualTo(30L);
    }

    @Test
    @DisplayName("beforeUpdateChangeEntornApp: llança AnswerRequiredException quan no es troba la nova EntornApp i no hi ha resposta")
    void beforeUpdateChangeEntornApp_quanCanviAppIdIEntornAppNoExisteixIRespostaNoDonada_llavorsLlancaAnswerRequiredException() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setAppId(10L);
        entity.setEntornId(20L);

        DashboardItemEntity item = new DashboardItemEntity();
        item.setEntornId(20L);
        EstadisticaWidgetEntity<?> widget = mock(EstadisticaWidgetEntity.class);
        widget.setAppId(10L);
        item.setWidget(widget);
        entity.setItems(Collections.singletonList(item));

        Dashboard resource = new Dashboard();
        resource.setAppId(10L);
        resource.setEntornId(99L); // Canvi a un EntornId que no existeix

        Map<String, Object> answers = new HashMap<>();

        when(estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(10L, 99L)).thenReturn(null);
        when(estadisticaClientHelper.entornAppFindById(20L)).thenReturn(new EntornApp()); // Mock per evitar NPE intern

        // Act & Assert
        assertThatThrownBy(() -> dashboardHelper.beforeUpdateEntityLogic(entity, resource, (Map) answers))
            .isInstanceOf(AnswerRequiredException.class)
            .hasFieldOrPropertyWithValue("answerCode", DashboardHelper.ANSWER_CODE_ENTORN_ID);
    }

    // ========================================================================
    // 4. TESTOS PER A CloneDashboardAction
    // ========================================================================

    @Test
    @DisplayName("CloneDashboardAction.exec: clona correctament quan es proporcionen paràmetres")
    void cloneDashboardAction_exec_quanParamsNoNuls_llavorsClonaAmbParams() throws ActionExecutionException {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setTitol("Original");
        entity.setAppId(10L);
        entity.setEntornId(20L);

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(5L);
        when(plantillaRepository.findById(5L)).thenReturn(Optional.of(plantilla));

        Dashboard params = new Dashboard();
        params.setTitol("Nou Titol");
        params.setDescripcio("Nova Descripcio");
        params.setAplicacio(ResourceReference.toResourceReference(15L, "Nova App"));
        params.setEntorn(ResourceReference.toResourceReference(25L, "Nou Entorn"));
        params.setPlantilla(ResourceReference.toResourceReference(5L, "Plantilla"));

        // Act
        action.exec(Dashboard.CLONE_ACTION, entity, params);

        // Assert
        verify(dashboardRepository, times(1)).save(argThat(d ->
            d.getTitol().equals("Nou Titol") &&
            d.getPlantilla() != null &&
            Long.valueOf(5L).equals(d.getPlantilla().getId())
        ));
        verify(dashboardTitolRepository, times(1)).saveAll(anyList());
        verify(dashboardItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("CloneDashboardAction.exec: clona correctament quan els paràmetres són nulls (usa valors de l'entitat)")
    void cloneDashboardAction_exec_quanParamsNuls_llavorsClonaAmbValorsEntitat() throws ActionExecutionException {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(5L);

        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        entity.setTitol("Original");
        entity.setDescripcio("Desc Original");
        entity.setAppId(10L);
        entity.setEntornId(20L);
        entity.setPlantilla(plantilla);

        // Act
        action.exec(Dashboard.CLONE_ACTION, entity, null);

        // Assert
        verify(dashboardRepository, times(1)).save(argThat(d ->
            d.getTitol().equals("Original (Copia)") &&
            d.getPlantilla() == plantilla
        ));
        verify(dashboardTitolRepository, times(1)).saveAll(anyList());
        verify(dashboardItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("CloneDashboardAction.getClonedTitulos: clona correctament tots els títols incloent personalitzat, destacat i plantilla")
    void cloneDashboardAction_getClonedTitulos_quanTitolsNoNuls_llavorsClonaTitols() throws Exception {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        DashboardEntity original = new DashboardEntity();
        DashboardEntity newDashboard = new DashboardEntity();

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(12L);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setTitol("Titol 1");
        titol.setSubtitol("Subtitol 1");
        titol.setPosX(10);
        titol.setPersonalitzat(true);
        titol.setDestacat(true);
        titol.setPlantilla(plantilla);
        titol.setColorTitol("#FF0000");
        original.setTitols(Collections.singletonList(titol));

        // Act
        List<DashboardTitolEntity> result = (List<DashboardTitolEntity>) ReflectionTestUtils.invokeMethod(
            action, "getClonedTitulos", original, newDashboard);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitol()).isEqualTo("Titol 1");
        assertThat(result.get(0).getSubtitol()).isEqualTo("Subtitol 1");
        assertThat(result.get(0).getPersonalitzat()).isTrue();
        assertThat(result.get(0).getDestacat()).isTrue();
        assertThat(result.get(0).getPlantilla()).isEqualTo(plantilla);
        assertThat(result.get(0).getColorTitol()).isEqualTo("#FF0000");
        assertThat(result.get(0).getDashboard()).isSameAs(newDashboard);
    }

    @Test
    @DisplayName("CloneDashboardAction.getClonedItem: llança ActionExecutionException quan el widget no és compatible amb la nova App")
    void cloneDashboardAction_getClonedItem_quanWidgetAppIdNoCompatible_llavorsLlancaActionExecutionException() {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        DashboardEntity original = new DashboardEntity();
        original.setId(1L);
        original.setAppId(10L);

        DashboardEntity newDashboard = new DashboardEntity();
        newDashboard.setAppId(20L); // Canvi d'AppId

        DashboardItemEntity item = new DashboardItemEntity();
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setAppId(10L); // No compatible amb 20L
        item.setWidget(widget);
        original.setItems(Collections.singletonList(item));

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(action, "getClonedItem", original, newDashboard))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("es.caib.comanda.estadistica.logic.helper.DashboardHelper.error.appId");
    }

    @Test
    @DisplayName("CloneDashboardAction.getClonedItem: llança ActionExecutionException quan no es pot resoldre la nova EntornApp")
    void cloneDashboardAction_getClonedItem_quanEntornAppNoExisteix_llavorsLlancaActionExecutionException() {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        DashboardEntity original = new DashboardEntity();
        original.setId(1L);
        original.setAppId(10L);
        original.setEntornId(20L);

        DashboardEntity newDashboard = new DashboardEntity();
        newDashboard.setAppId(10L);
        newDashboard.setEntornId(99L); // Entorn que no existeix

        DashboardItemEntity item = new DashboardItemEntity();
        item.setEntornId(20L);
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setAppId(10L); // Compatible
        item.setWidget(widget);
        original.setItems(Collections.singletonList(item));

        when(estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(10L, 99L)).thenReturn(null);
        when(estadisticaClientHelper.entornAppFindById(20L)).thenReturn(new EntornApp()); // Per evitar NPE intern
        lenient().when(estadisticaClientHelper.entornAppFindByAppAndEntornOrDefaultNull(10L, 20L)).thenReturn(null); // Fallada final

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(action, "getClonedItem", original, newDashboard))
            .isInstanceOf(ActionExecutionException.class)
            .hasMessageContaining("es.caib.comanda.estadistica.logic.helper.DashboardHelper.action.error.appId");
    }

    @Test
    @DisplayName("CloneDashboardAction.getClonedItem: clona correctament els items i els widgets associats")
    void cloneDashboardAction_getClonedItem_quanTotCompatible_llavorsClonaItemsIWidgets() throws Exception {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        DashboardEntity original = new DashboardEntity();
        original.setId(1L);
        original.setAppId(10L);
        original.setEntornId(20L);

        DashboardEntity newDashboard = new DashboardEntity();
        newDashboard.setAppId(10L);
        newDashboard.setEntornId(20L);

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(12L);

        // Simple widget
        EstadisticaSimpleWidgetEntity originalSimpleWidget = new EstadisticaSimpleWidgetEntity();
        originalSimpleWidget.setId(100L);
        originalSimpleWidget.setAppId(10L);
        originalSimpleWidget.setTitol("Widget Simple");
        originalSimpleWidget.setUnitat("unitats");
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setId(50L);
        IndicadorTaulaEntity indInfo = new IndicadorTaulaEntity();
        indInfo.setIndicador(indicador);
        indInfo.setTitol("Ind Info");
        originalSimpleWidget.setIndicadorInfo(indInfo);

        // Grafic widget
        EstadisticaGraficWidgetEntity originalGraficWidget = new EstadisticaGraficWidgetEntity();
        originalGraficWidget.setId(200L);
        originalGraficWidget.setAppId(10L);
        originalGraficWidget.setTitol("Widget Grafic");
        originalGraficWidget.setLlegendaX("X");
        originalGraficWidget.setLlegendaY("Y");
        IndicadorTaulaEntity indGrafic = new IndicadorTaulaEntity();
        indGrafic.setIndicador(indicador);
        indGrafic.setTitol("Ind Grafic");
        originalGraficWidget.setIndicadorsInfo(Collections.singletonList(indGrafic));

        // Taula widget
        EstadisticaTaulaWidgetEntity originalTaulaWidget = new EstadisticaTaulaWidgetEntity();
        originalTaulaWidget.setId(300L);
        originalTaulaWidget.setAppId(10L);
        originalTaulaWidget.setTitol("Widget Taula");
        DimensioEntity dimensio = new DimensioEntity();
        dimensio.setId(70L);
        originalTaulaWidget.setDimensioAgrupacio(dimensio);
        IndicadorTaulaEntity indTaula = new IndicadorTaulaEntity();
        indTaula.setIndicador(indicador);
        indTaula.setTitol("Columna 1");
        originalTaulaWidget.setColumnes(Collections.singletonList(indTaula));

        // Item 1 with Simple widget
        DashboardItemEntity item1 = new DashboardItemEntity();
        item1.setEntornId(20L);
        item1.setPosX(0);
        item1.setPersonalitzat(true);
        item1.setDestacat(false);
        item1.setPlantilla(plantilla);
        item1.setWidget(originalSimpleWidget);

        // Item 2 with Grafic widget
        DashboardItemEntity item2 = new DashboardItemEntity();
        item2.setEntornId(20L);
        item2.setPosX(4);
        item2.setPlantilla(plantilla);
        item2.setWidget(originalGraficWidget);

        // Item 3 with Taula widget
        DashboardItemEntity item3 = new DashboardItemEntity();
        item3.setEntornId(20L);
        item3.setPosX(8);
        item3.setPlantilla(plantilla);
        item3.setWidget(originalTaulaWidget);

        // Item 4 sharing the SAME Simple widget as Item 1
        DashboardItemEntity item4 = new DashboardItemEntity();
        item4.setEntornId(20L);
        item4.setPosX(12);
        item4.setPlantilla(plantilla);
        item4.setWidget(originalSimpleWidget);

        original.setItems(List.of(item1, item2, item3, item4));

        // Simulació que el widget ja existeix amb el títol original per forçar el nom "(Copia)"
        lenient().when(estadisticaWidgetRepository.findByAppIdAndTitol(10L, "Widget Simple")).thenReturn(originalSimpleWidget);
        lenient().when(estadisticaWidgetRepository.findByAppIdAndTitol(10L, "Widget Grafic")).thenReturn(originalGraficWidget);
        lenient().when(estadisticaWidgetRepository.findByAppIdAndTitol(10L, "Widget Taula")).thenReturn(originalTaulaWidget);

        // Act
        List<DashboardItemEntity> result = (List<DashboardItemEntity>) ReflectionTestUtils.invokeMethod(
            action, "getClonedItem", original, newDashboard);

        // Assert
        assertThat(result).hasSize(4);

        // Verify widgets were cloned (new instances, not same reference)
        assertThat(result.get(0).getWidget()).isNotSameAs(originalSimpleWidget);
        assertThat(result.get(0).getWidget()).isInstanceOf(EstadisticaSimpleWidgetEntity.class);
        assertThat(result.get(0).getWidget().getTitol()).isEqualTo("Widget Simple (Copia)");
        assertThat(((EstadisticaSimpleWidgetEntity) result.get(0).getWidget()).getUnitat()).isEqualTo("unitats");
        assertThat(((EstadisticaSimpleWidgetEntity) result.get(0).getWidget()).getIndicadorInfo()).isNotNull();
        assertThat(((EstadisticaSimpleWidgetEntity) result.get(0).getWidget()).getIndicadorInfo().getTitol()).isEqualTo("Ind Info");
        assertThat(((EstadisticaSimpleWidgetEntity) result.get(0).getWidget()).getIndicadorInfo().getWidget()).isSameAs(result.get(0).getWidget());

        assertThat(result.get(1).getWidget()).isNotSameAs(originalGraficWidget);
        assertThat(result.get(1).getWidget()).isInstanceOf(EstadisticaGraficWidgetEntity.class);
        assertThat(result.get(1).getWidget().getTitol()).isEqualTo("Widget Grafic (Copia)");
        assertThat(((EstadisticaGraficWidgetEntity) result.get(1).getWidget()).getLlegendaX()).isEqualTo("X");
        assertThat(((EstadisticaGraficWidgetEntity) result.get(1).getWidget()).getIndicadorsInfo()).hasSize(1);
        assertThat(((EstadisticaGraficWidgetEntity) result.get(1).getWidget()).getIndicadorsInfo().get(0).getWidget()).isSameAs(result.get(1).getWidget());

        assertThat(result.get(2).getWidget()).isNotSameAs(originalTaulaWidget);
        assertThat(result.get(2).getWidget()).isInstanceOf(EstadisticaTaulaWidgetEntity.class);
        assertThat(result.get(2).getWidget().getTitol()).isEqualTo("Widget Taula (Copia)");
        assertThat(((EstadisticaTaulaWidgetEntity) result.get(2).getWidget()).getDimensioAgrupacio()).isEqualTo(dimensio);
        assertThat(((EstadisticaTaulaWidgetEntity) result.get(2).getWidget()).getColumnes()).hasSize(1);
        assertThat(((EstadisticaTaulaWidgetEntity) result.get(2).getWidget()).getColumnes().get(0).getWidget()).isSameAs(result.get(2).getWidget());

        // Item 4 had the same original widget as Item 1, so it should reuse the cloned widget instance
        assertThat(result.get(3).getWidget()).isSameAs(result.get(0).getWidget());

        // Verify widgets were saved (3 distinct widgets cloned)
        verify(estadisticaWidgetRepository, times(3)).save(any(EstadisticaWidgetEntity.class));

        // Plantilles are kept by reference
        assertThat(result.get(0).getPlantilla()).isSameAs(plantilla);
        assertThat(result.get(1).getPlantilla()).isSameAs(plantilla);
        assertThat(result.get(2).getPlantilla()).isSameAs(plantilla);
        assertThat(result.get(3).getPlantilla()).isSameAs(plantilla);
    }

    @Test
    @DisplayName("CloneDashboardAction.getWidgetNewTitol: llança IllegalStateException quan es supera el màxim d'intents (1000)")
    void cloneDashboardAction_getWidgetNewTitol_quanSuperaMaximIntents_llavorsLlancaIllegalStateException() {
        // Arrange
        DashboardHelper.CloneDashboardAction action = new DashboardHelper.CloneDashboardAction(
            estadisticaClientHelper, dashboardRepository, dashboardTitolRepository, dashboardItemRepository, plantillaRepository, estadisticaWidgetRepository, dashboardClonerMapper);

        // Sempre retorna un widget existent per forçar el límit de 1000 intents
        when(estadisticaWidgetRepository.findByAppIdAndTitol(eq(10L), anyString()))
            .thenReturn(new EstadisticaSimpleWidgetEntity());

        // Act & Assert
        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(action, "getWidgetNewTitol", "Widget Test", 10L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("S'ha superat el nombre màxim d'intents (1000)");
    }
}
