package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardImportHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardStyleResolverHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.helper.SpringFilterHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsTitol;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetTitolItem;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitolTipus;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletteGroupType;
import es.caib.comanda.estadistica.logic.intf.model.paleta.WidgetStyleScope;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.logic.mapper.DashboardClonerMapper;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
import es.caib.comanda.ms.logic.intf.exception.ActionExecutionException;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.model.DownloadableFile;
import es.caib.comanda.ms.logic.intf.model.FileReference;
import es.caib.comanda.ms.logic.intf.model.ReportFileType;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardServiceImpl")
class DashboardServiceImplTest {

    @Mock private DashboardRepository dashboardRepository;
    @Mock private DashboardItemRepository dashboardItemRepository;
    @Mock private DashboardTitolRepository dashboardTitolRepository;
    @Mock private EstadisticaClientHelper estadisticaClientHelper;
    @Mock private AtributsVisualsHelper atributsVisualsHelper;
    @Mock private DashboardExportMapper dashboardExportMapper;
    @Mock private DashboardClonerMapper dashboardClonerMapper;
    @Mock private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Mock private DashboardHelper dashboardHelper;
    @Mock private ObjectMapper objectMapper;
    @Mock private DashboardStyleResolverHelper dashboardStyleResolverHelper;
    @Mock private DashboardImportHelper dashboardImportHelper;
    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private AclServiceClient aclServiceClient;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dashboardService, "entityRepository", dashboardRepository);
        dashboardService.init();
    }

    /**
     * Mockeja la càrrega del dashboard des del repository.
     * IMPORTANT: getDashboard() carrega l'entitat del repository, no la que passem.
     */
    private void mockDashboardEntity(DashboardEntity entity) {
        when(dashboardRepository.findById(entity.getId())).thenReturn(Optional.of(entity));
    }

    @SuppressWarnings("unchecked")
    private ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> createInformeWidgets() throws Exception {
        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$InformeWidgets");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        return (ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardService);
    }

    @SuppressWarnings("unchecked")
    private ReportGenerator<DashboardEntity, Serializable, DashboardExport> createDashboardExportReportGenerator() throws Exception {
        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$DashboardExportReportGenerator");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        return (ReportGenerator<DashboardEntity, Serializable, DashboardExport>) constructor.newInstance(dashboardService);
    }

    private DashboardServiceImpl.DashboardImportActionExecutor createDashboardImportActionExecutor() throws Exception {
        Class<?> executorClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$DashboardImportActionExecutor");
        java.lang.reflect.Constructor<?> constructor = executorClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        return (DashboardServiceImpl.DashboardImportActionExecutor) constructor.newInstance(dashboardService);
    }

    // ==========================================
    // Tests existents
    // ==========================================

    @Test
    @DisplayName("completeResource delega a dashboardHelper")
    void completeResource_delegaAHelper() {
        Dashboard dashboard = new Dashboard();
        ReflectionTestUtils.invokeMethod(dashboardService, "completeResource", dashboard);
        verify(dashboardHelper).completeResourceLogic(dashboard);
    }

    @Test
    @DisplayName("afterConversion delega a dashboardHelper")
    void afterConversion_delegaAHelper() {
        DashboardEntity entity = new DashboardEntity();
        Dashboard resource = new Dashboard();
        ReflectionTestUtils.invokeMethod(dashboardService, "afterConversion", entity, resource);
        verify(dashboardHelper).afterConversionLogic(entity, resource);
    }

    @Test
    @DisplayName("InformeWidgets: retorna llista buida quan dashboard no té items ni títols")
    void informeWidgets_quanDashboardBuit_retornaLlistaBuida() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        mockDashboardEntity(entity);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("InformeWidgets: llança excepció quan dashboard no existeix")
    void informeWidgets_quanDashboardNoExisteix_llancaExcepcio() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(999L);
        when(dashboardRepository.findById(999L)).thenReturn(Optional.empty());

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();

        assertThatThrownBy(() -> reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null))
                .isInstanceOf(ReportGenerationException.class);
    }

    @Test
    @DisplayName("InformeWidgets: retorna items amb loading=true i determina el tipus de widget")
    void informeWidgets_ambItems_retornaItemsAmbLoadingTrue() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        EstadisticaWidgetEntity widget = Mockito.mock(EstadisticaWidgetEntity.class, CALLS_REAL_METHODS);
        widget.setId(100L);
        widget.setTitol("Widget Test");

        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(10L);
        item.setPosX(0);
        item.setPosY(0);
        item.setWidth(6);
        item.setHeight(4);
        item.setWidget(widget);
        entity.setItems(List.of(item));

        mockDashboardEntity(entity);
        when(consultaEstadisticaHelper.determineWidgetType(item)).thenReturn(WidgetTipus.SIMPLE);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        assertThat(result).hasSize(1);
        InformeWidgetItem informeItem = result.get(0);
        assertThat(informeItem.getDashboardItemId()).isEqualTo(10L);
        assertThat(informeItem.getWidgetId()).isEqualTo(100L);
        assertThat(informeItem.getTitol()).isEqualTo("Widget Test");
        assertThat(informeItem.getTipus()).isEqualTo(WidgetTipus.SIMPLE);
        assertThat(informeItem.isLoading()).isTrue();
        verify(consultaEstadisticaHelper).determineWidgetType(item);
    }

    @Test
    @DisplayName("InformeWidgets: títol sense plantilla no crida applyTemplateDefaults")
    void informeWidgets_titolSensePlantilla_noAplicaPlantilla() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol sense plantilla");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(InformeWidgetTitolItem.class);
        verify(dashboardStyleResolverHelper, never()).applyTemplateDefaults(any(), any(), any(), any());
    }

    @Test
    @DisplayName("InformeWidgets: sense personalitzat, s'ignoren els camps propis del títol (encara que hi hagi valors residuals)")
    void informeWidgets_titolSensePersonalitzat_ignoraElsCampsPropis() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol amb residus");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setColorTitol("#AAAAAA");
        titol.setColorFons("#BBBBBB");
        titol.setPersonalitzat(false);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        InformeWidgetTitolItem item = (InformeWidgetTitolItem) result.get(0);
        assertThat(item.getAtributsVisuals().getColorTitol()).isNull();
        assertThat(item.getAtributsVisuals().getColorFons()).isNull();
    }

    @Test
    @DisplayName("InformeWidgets: amb personalitzat, s'apliquen els camps propis del títol")
    void informeWidgets_titolAmbPersonalitzat_aplicaElsCampsPropis() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol personalitzat");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setColorTitol("#AAAAAA");
        titol.setPersonalitzat(true);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        InformeWidgetTitolItem item = (InformeWidgetTitolItem) result.get(0);
        assertThat(item.getAtributsVisuals().getColorTitol()).isEqualTo("#AAAAAA");
    }

    @Test
    @DisplayName("InformeWidgets: títol amb tema clar aplica PaletteGroupType.LIGHT")
    void informeWidgets_titolAmbTemaClar_aplicaLightPalette() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol clar");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setDestacat(false);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(false);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        ArgumentCaptor<PaletteGroupType> groupTypeCaptor = ArgumentCaptor.forClass(PaletteGroupType.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                groupTypeCaptor.capture(),
                eq(WidgetStyleScope.TITOL_1)
        );
        assertThat(groupTypeCaptor.getValue()).isEqualTo(PaletteGroupType.LIGHT);
    }

    @Test
    @DisplayName("InformeWidgets: títol amb tema fosc aplica PaletteGroupType.DARK")
    void informeWidgets_titolAmbTemaFosc_aplicaDarkPalette() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol fosc");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setDestacat(false);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(true);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        ArgumentCaptor<PaletteGroupType> groupTypeCaptor = ArgumentCaptor.forClass(PaletteGroupType.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                groupTypeCaptor.capture(),
                eq(WidgetStyleScope.TITOL_1)
        );
        assertThat(groupTypeCaptor.getValue()).isEqualTo(PaletteGroupType.DARK);
    }

    @Test
    @DisplayName("InformeWidgets: títol destacat amb tema fosc aplica DARK_HIGHLIGHTED")
    void informeWidgets_titolDestacatFosc_aplicaDarkHighlighted() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol destacat fosc");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setDestacat(true);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(true);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        ArgumentCaptor<PaletteGroupType> groupTypeCaptor = ArgumentCaptor.forClass(PaletteGroupType.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                groupTypeCaptor.capture(),
                eq(WidgetStyleScope.TITOL_1)
        );
        assertThat(groupTypeCaptor.getValue()).isEqualTo(PaletteGroupType.DARK_HIGHLIGHTED);
    }

    @Test
    @DisplayName("InformeWidgets: títol destacat amb tema clar aplica LIGHT_HIGHLIGHTED")
    void informeWidgets_titolDestacatClar_aplicaLightHighlighted() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol destacat clar");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setDestacat(true);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(false);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        ArgumentCaptor<PaletteGroupType> groupTypeCaptor = ArgumentCaptor.forClass(PaletteGroupType.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                groupTypeCaptor.capture(),
                eq(WidgetStyleScope.TITOL_1)
        );
        assertThat(groupTypeCaptor.getValue()).isEqualTo(PaletteGroupType.LIGHT_HIGHLIGHTED);
    }

    @Test
    @DisplayName("InformeWidgets: títol TIPUS_2 aplica WidgetStyleScope.TITOL_2")
    void informeWidgets_titolTipus2_aplicaScopeCorrecte() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol Tipus 2");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_2);
        titol.setDestacat(false);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(false);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                any(PaletteGroupType.class),
                eq(WidgetStyleScope.TITOL_2)
        );
    }

    @Test
    @DisplayName("InformeWidgets: títol TIPUS_3 aplica WidgetStyleScope.TITOL_3")
    void informeWidgets_titolTipus3_aplicaScopeCorrecte() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol Tipus 3");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_3);
        titol.setDestacat(false);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(false);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                any(PaletteGroupType.class),
                eq(WidgetStyleScope.TITOL_3)
        );
    }

    @Test
    @DisplayName("InformeWidgets: plantilla del títol prioritza sobre la del dashboard")
    void informeWidgets_plantillaTitolPrioritzaSobreDashboard() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantillaDashboard = new PlantillaEntity();
        plantillaDashboard.setId(1L);
        entity.setPlantilla(plantillaDashboard);

        PlantillaEntity plantillaTitol = new PlantillaEntity();
        plantillaTitol.setId(2L);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol amb plantilla pròpia");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setPlantilla(plantillaTitol);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(false);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, params);

        ArgumentCaptor<PlantillaEntity> plantillaCaptor = ArgumentCaptor.forClass(PlantillaEntity.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                plantillaCaptor.capture(),
                any(PaletteGroupType.class),
                any(WidgetStyleScope.class)
        );
        assertThat(plantillaCaptor.getValue()).isSameAs(plantillaTitol);
    }

    @Test
    @DisplayName("InformeWidgets: params null es tracta com tema clar")
    void informeWidgets_paramsNull_esTemaClar() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        entity.setPlantilla(plantilla);

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol");
        titol.setPosX(0);
        titol.setPosY(0);
        titol.setWidth(12);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        titol.setDestacat(false);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        ArgumentCaptor<PaletteGroupType> groupTypeCaptor = ArgumentCaptor.forClass(PaletteGroupType.class);
        verify(dashboardStyleResolverHelper).applyTemplateDefaults(
                any(AtributsVisualsTitol.class),
                eq(plantilla),
                groupTypeCaptor.capture(),
                any(WidgetStyleScope.class)
        );
        assertThat(groupTypeCaptor.getValue()).isEqualTo(PaletteGroupType.LIGHT);
    }

    @Test
    @DisplayName("InformeWidgets: retorna items i títols combinats")
    void informeWidgets_ambItemsITitols_retornaCombinats() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);

        EstadisticaWidgetEntity widget = Mockito.mock(EstadisticaWidgetEntity.class, CALLS_REAL_METHODS);
        widget.setId(100L);
        widget.setTitol("Widget");

        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(10L);
        item.setPosX(0);
        item.setPosY(0);
        item.setWidth(6);
        item.setHeight(4);
        item.setWidget(widget);
        entity.setItems(List.of(item));

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Títol");
        titol.setPosX(6);
        titol.setPosY(0);
        titol.setWidth(6);
        titol.setHeight(1);
        titol.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        entity.setTitols(List.of(titol));

        mockDashboardEntity(entity);
        when(consultaEstadisticaHelper.determineWidgetType(item)).thenReturn(WidgetTipus.SIMPLE);

        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator = createInformeWidgets();
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTipus()).isEqualTo(WidgetTipus.SIMPLE);
        assertThat(result.get(1).getTipus()).isEqualTo(WidgetTipus.TITOL);
    }

    @Test
    @DisplayName("DashboardExport: exporta un dashboard específic")
    void dashboardExport_quanEntitat_exportaUn() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        DashboardExport export = new DashboardExport();

        when(dashboardExportMapper.toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper))
                .thenReturn(export);

        ReportGenerator<DashboardEntity, Serializable, DashboardExport> reportGenerator = createDashboardExportReportGenerator();
        List<DashboardExport> result = reportGenerator.generateData(Dashboard.DASHBOARD_EXPORT, entity, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(export);
        verify(dashboardExportMapper).toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper);
    }

    @Test
    @DisplayName("DashboardExport: exporta tots els dashboards quan no s'especifica entitat")
    void dashboardExport_quanSenseEntitat_exportaTots() throws Exception {
        DashboardEntity entity1 = new DashboardEntity();
        entity1.setId(1L);
        DashboardEntity entity2 = new DashboardEntity();
        entity2.setId(2L);
        List<DashboardEntity> entities = Arrays.asList(entity1, entity2);

        List<DashboardExport> exports = Arrays.asList(new DashboardExport(), new DashboardExport());

        when(dashboardRepository.findAll()).thenReturn(entities);
        when(dashboardExportMapper.toDashboardExport(entities, estadisticaClientHelper, atributsVisualsHelper))
                .thenReturn(exports);

        ReportGenerator<DashboardEntity, Serializable, DashboardExport> reportGenerator = createDashboardExportReportGenerator();
        List<DashboardExport> result = reportGenerator.generateData(Dashboard.DASHBOARD_EXPORT, null, null);

        assertThat(result).hasSize(2);
        verify(dashboardRepository).findAll();
        verify(dashboardExportMapper).toDashboardExport(entities, estadisticaClientHelper, atributsVisualsHelper);
    }

    // ==========================================
    // Tests per a la nova lògica
    // ==========================================

    @Test
    @DisplayName("beforeUpdateEntity delega a dashboardHelper")
    void beforeUpdateEntity_delegaAHelper() throws Exception {
        DashboardEntity entity = new DashboardEntity();
        Dashboard resource = new Dashboard();
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        ReflectionTestUtils.invokeMethod(dashboardService, "beforeUpdateEntity", entity, resource, answers);
        verify(dashboardHelper).beforeUpdateEntityLogic(entity, resource, answers);
    }

    @Test
    @DisplayName("additionalSpringFilter: usuari ADMIN retorna el filtre actual sense modificar")
    void additionalSpringFilter_usuariAdmin_retornaFiltreActual() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);
        String currentFilter = "nom:'test'";

        String result = ReflectionTestUtils.invokeMethod(dashboardService, "additionalSpringFilter", currentFilter, new String[]{});

        assertThat(result).isEqualTo(currentFilter);
    }

    @Test
    @DisplayName("additionalSpringFilter: usuari CONSULTA retorna el filtre actual sense modificar")
    void additionalSpringFilter_usuariConsulta_retornaFiltreActual() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);
        String currentFilter = "nom:'test'";

        String result = ReflectionTestUtils.invokeMethod(dashboardService, "additionalSpringFilter", currentFilter, new String[]{});

        assertThat(result).isEqualTo(currentFilter);
    }

    @Test
    @DisplayName("additionalSpringFilter: usuari normal sense permisos retorna id:0")
    void additionalSpringFilter_usuariNormalSensePermisos_retornaId0() {
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);
        when(authenticationHelper.getCurrentUserName()).thenReturn("user");
        when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{});
        when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("auth");

        ResponseEntity<Set<Serializable>> response = new ResponseEntity<>(Collections.emptySet(), HttpStatus.OK);
        when(aclServiceClient.findIdsWithAnyPermission(any(), any(), any(), any(), any())).thenReturn(response);

        try (MockedStatic<SpringFilterHelper> mockedStatic = mockStatic(SpringFilterHelper.class)) {
            mockedStatic.when(() -> SpringFilterHelper.buildOrFilter(anyString(), any())).thenReturn("");
            mockedStatic.when(() -> SpringFilterHelper.or(anyString(), anyString(), anyString())).thenReturn("");
            mockedStatic.when(() -> SpringFilterHelper.and(eq("current"), eq("id:0"))).thenReturn("current AND id:0");

            String result = ReflectionTestUtils.invokeMethod(dashboardService, "additionalSpringFilter", "current", new String[]{});

            assertThat(result).isEqualTo("current AND id:0");
            verify(aclServiceClient, times(3)).findIdsWithAnyPermission(any(), any(), any(), any(), any());
        }
    }

    @Test
    @DisplayName("DashboardExport: generateFile escriu JSON i retorna DownloadableFile")
    void dashboardExport_generateFile_retornaDownloadableFile() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        ReportGenerator<DashboardEntity, Serializable, DashboardExport> reportGenerator = createDashboardExportReportGenerator();

        List<DashboardExport> data = List.of(new DashboardExport());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        DownloadableFile file = reportGenerator.generateFile(Dashboard.DASHBOARD_EXPORT, data, ReportFileType.JSON, out);

        assertThat(file).isNotNull();
        assertThat(file.getName()).isEqualTo("dashboards.json");
        assertThat(file.getContentType()).isEqualTo("application/json");
        assertThat(out.toByteArray()).isNotEmpty();

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardExport: generateFile genera el nom del fitxer a partir del títol del dashboard sanejat")
    void dashboardExport_generateFile_usaNomDashboardSanejat() throws Exception {
        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        ReportGenerator<DashboardEntity, Serializable, DashboardExport> reportGenerator = createDashboardExportReportGenerator();

        // 1. Dashboard amb caràcters especials i accents
        DashboardExport export1 = new DashboardExport();
        export1.setTitol("Estadístiques d'ús: Vendes / Facturació (2024)");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        DownloadableFile file1 = reportGenerator.generateFile(Dashboard.DASHBOARD_EXPORT, List.of(export1), ReportFileType.JSON, out1);

        assertThat(file1).isNotNull();
        assertThat(file1.getName()).isEqualTo("Estadistiques d_us_ Vendes _ Facturacio (2024).json");

        // 2. Dashboard amb nom estàndard
        DashboardExport export2 = new DashboardExport();
        export2.setTitol("Tauler General");
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        DownloadableFile file2 = reportGenerator.generateFile(Dashboard.DASHBOARD_EXPORT, List.of(export2), ReportFileType.JSON, out2);

        assertThat(file2).isNotNull();
        assertThat(file2.getName()).isEqualTo("Tauler General.json");

        // 3. Múltiples dashboards -> dashboards.json
        DashboardExport export3 = new DashboardExport();
        export3.setTitol("Tauler 2");
        ByteArrayOutputStream out3 = new ByteArrayOutputStream();
        DownloadableFile file3 = reportGenerator.generateFile(Dashboard.DASHBOARD_EXPORT, List.of(export2, export3), ReportFileType.JSON, out3);

        assertThat(file3).isNotNull();
        assertThat(file3.getName()).isEqualTo("dashboards.json");

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardImport: exec importa dashboards correctament")
    void dashboardImport_exec_importaCorrectament() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();

        String json = "[{\"titol\":\"Titol\"}]";
        FileReference fileRef = new FileReference();
        ReflectionTestUtils.setField(fileRef, "content", json.getBytes(StandardCharsets.UTF_8));

        DashboardServiceImpl.DashboardImportParams params = new DashboardServiceImpl.DashboardImportParams();
        params.setFile(fileRef);

        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        DashboardServiceImpl.DashboardImportResult result = executor.exec(Dashboard.DASHBOARD_IMPORT, new DashboardEntity(), params);

        assertThat(result).isNotNull();
        verify(dashboardImportHelper).importDashboardFromExport(anyList(), any());

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardImport: exec llança ActionExecutionException si falla la importació")
    void dashboardImport_exec_llancaExcepcioSiFall() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();

        String invalidJson = "invalid json";
        FileReference fileRef = new FileReference();
        ReflectionTestUtils.setField(fileRef, "content", invalidJson.getBytes(StandardCharsets.UTF_8));

        DashboardServiceImpl.DashboardImportParams params = new DashboardServiceImpl.DashboardImportParams();
        params.setFile(fileRef);

        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        assertThatThrownBy(() -> executor.exec(Dashboard.DASHBOARD_IMPORT, new DashboardEntity(), params))
            .isInstanceOf(ActionExecutionException.class);

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardImport: onChange amb fitxer null buida els conflictes")
    void dashboardImport_onChange_ambFitxerNull_buidaConflictes() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();

        DashboardServiceImpl.DashboardImportParams target = new DashboardServiceImpl.DashboardImportParams();
        target.setConflicts(List.of(new DashboardServiceImpl.Conflict("t", "t")));

        executor.onChange(null, null, DashboardServiceImpl.DashboardImportParams.Fields.file, null, new HashMap<>(), new String[]{}, target);

        assertThat(target.getConflicts()).isEmpty();
    }

    @Test
    @DisplayName("DashboardImport: onChange amb fitxer JSON buit o d'objecte buit retorna conflictes buits")
    void dashboardImport_onChange_ambFitxerSenseItems_retornaConflictesBuits() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();
        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        FileReference fileRef = new FileReference();
        ReflectionTestUtils.setField(fileRef, "content", "[]".getBytes(StandardCharsets.UTF_8));

        DashboardServiceImpl.DashboardImportParams target = new DashboardServiceImpl.DashboardImportParams();
        executor.onChange(null, null, DashboardServiceImpl.DashboardImportParams.Fields.file, fileRef, new HashMap<>(), new String[]{}, target);

        assertThat(target.getConflicts()).isNotNull().isEmpty();

        ReflectionTestUtils.setField(fileRef, "content", "{}".getBytes(StandardCharsets.UTF_8));
        DashboardServiceImpl.DashboardImportParams targetObj = new DashboardServiceImpl.DashboardImportParams();
        executor.onChange(null, null, DashboardServiceImpl.DashboardImportParams.Fields.file, fileRef, new HashMap<>(), new String[]{}, targetObj);

        assertThat(targetObj.getConflicts()).isNotNull().isEmpty();

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardImport: exec amb objecte JSON únic importa correctament")
    void dashboardImport_exec_ambObjecteUnic_importaCorrectament() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();
        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        String json = "{\"titol\":\"Titol Unic\"}";
        FileReference fileRef = new FileReference();
        ReflectionTestUtils.setField(fileRef, "content", json.getBytes(StandardCharsets.UTF_8));

        DashboardServiceImpl.DashboardImportParams params = new DashboardServiceImpl.DashboardImportParams();
        params.setFile(fileRef);

        DashboardServiceImpl.DashboardImportResult result = executor.exec(Dashboard.DASHBOARD_IMPORT, new DashboardEntity(), params);

        assertThat(result).isNotNull();
        verify(dashboardImportHelper, atLeastOnce()).importDashboardFromExport(anyList(), any());

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardImport: onChange amb dashboard sense items ni titols popula conflictes sense fallar")
    void dashboardImport_onChange_ambDashboardSenseItemsNiTitols_populaConflictes() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();
        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        String json = "[\n" +
                "  {\n" +
                "    \"titol\": \"Dashboard de prova2\",\n" +
                "    \"descripcio\": \"Dashboard de prova\",\n" +
                "    \"entornCodi\": \"DEV\"\n" +
                "  }\n" +
                "]";
        FileReference fileRef = new FileReference();
        ReflectionTestUtils.setField(fileRef, "content", json.getBytes(StandardCharsets.UTF_8));

        DashboardServiceImpl.DashboardImportParams target = new DashboardServiceImpl.DashboardImportParams();
        executor.onChange(null, null, DashboardServiceImpl.DashboardImportParams.Fields.file, fileRef, new HashMap<>(), new String[]{}, target);

        assertThat(target.getConflicts()).isNotNull();
        verify(dashboardImportHelper, atLeastOnce()).checkDashboardConflicts(anyList(), any());

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }

    @Test
    @DisplayName("DashboardImport: exec llança ActionExecutionException quan la validació falla")
    void dashboardImport_exec_llancaExcepcioQuanValidacioFalla() throws Exception {
        DashboardServiceImpl.DashboardImportActionExecutor executor = createDashboardImportActionExecutor();

        String json = "[{\"titol\":\"Titol\"}]";
        FileReference fileRef = new FileReference();
        ReflectionTestUtils.setField(fileRef, "content", json.getBytes(StandardCharsets.UTF_8));

        DashboardServiceImpl.DashboardImportParams params = new DashboardServiceImpl.DashboardImportParams();
        params.setFile(fileRef);

        ObjectMapper realMapper = new ObjectMapper();
        ReflectionTestUtils.setField(dashboardService, "objectMapper", realMapper);

        doThrow(new IllegalArgumentException("Dades del tauler invàlides (titol: no pot ser buit)"))
                .when(dashboardImportHelper).validateDashboardExport(anyList());

        assertThatThrownBy(() -> executor.exec(Dashboard.DASHBOARD_IMPORT, new DashboardEntity(), params))
                .isInstanceOf(ActionExecutionException.class)
                .hasMessageContaining("Dades del tauler invàlides (titol: no pot ser buit)");

        ReflectionTestUtils.setField(dashboardService, "objectMapper", objectMapper);
    }
}
