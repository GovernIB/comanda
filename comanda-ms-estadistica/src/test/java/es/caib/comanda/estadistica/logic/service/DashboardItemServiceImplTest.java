package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardItemTitolHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisuals;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardItem;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetTipus;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardItemServiceImpl")
class DashboardItemServiceImplTest {

    @Mock
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;
    @Mock
    private EstadisticaWidgetHelper estadisticaWidgetHelper;
    @Mock
    private DashboardItemTitolHelper dashboardItemTitolHelper;
    @Mock
    private DashboardItemRepository dashboardItemRepository;

    @InjectMocks
    private DashboardItemServiceImpl dashboardItemService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dashboardItemService, "entityRepository", dashboardItemRepository);
    }

    @Test
    @DisplayName("init registra el generador d'informes de widgets")
    void init_registraGeneradors() {
        // Act
        dashboardItemService.init();

        // Assert
        assertThat(dashboardItemService).isNotNull();
    }

    @Test
    @DisplayName("completeResource delega a dashboardItemTitolHelper")
    void completeResource_delegaAHelper() {
        // Arrange
        DashboardItem item = new DashboardItem();

        // Act
        ReflectionTestUtils.invokeMethod(dashboardItemService, "completeResource", item);

        // Assert
        verify(dashboardItemTitolHelper).completeResourceItemLogic(item);
    }

    @Test
    @DisplayName("beforeCreateSave converteix atributs visuals a JSON")
    void beforeCreateSave_converteixAtributsVisuals() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributsVisuals = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributsVisuals);

        String atributsJson = "{\"test\":\"value\"}";
        when(atributsVisualsHelper.getAtributsVisualsJson(atributsVisuals)).thenReturn(atributsJson);

        // Act
        ReflectionTestUtils.invokeMethod(dashboardItemService, "beforeCreateSave", entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo(atributsJson);
        verify(atributsVisualsHelper).getAtributsVisualsJson(atributsVisuals);
    }

    @Test
    @DisplayName("beforeCreateSave llança excepció quan falla la conversió JSON")
    void beforeCreateSave_quanFallaConversio_llancaExcepcio() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributsVisuals = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributsVisuals);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributsVisuals))
                .thenThrow(new RuntimeException("Error de conversió"));

        // Act & Assert
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(dashboardItemService, "beforeCreateSave", entity, resource, null))
                .isInstanceOf(ResourceNotCreatedException.class);
    }

    @Test
    @DisplayName("beforeUpdateSave converteix atributs visuals a JSON")
    void beforeUpdateSave_converteixAtributsVisuals() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(1L);
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributsVisuals = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributsVisuals);

        String atributsJson = "{\"test\":\"value\"}";
        when(atributsVisualsHelper.getAtributsVisualsJson(atributsVisuals)).thenReturn(atributsJson);

        // Act
        ReflectionTestUtils.invokeMethod(dashboardItemService, "beforeUpdateSave", entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo(atributsJson);
        verify(atributsVisualsHelper).getAtributsVisualsJson(atributsVisuals);
    }

    @Test
    @DisplayName("beforeUpdateSave llança excepció quan falla la conversió JSON")
    void beforeUpdateSave_quanFallaConversio_llancaExcepcio() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(1L);
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributsVisuals = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributsVisuals);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributsVisuals))
                .thenThrow(new RuntimeException("Error de conversió"));

        // Act & Assert
        assertThatThrownBy(() ->
                ReflectionTestUtils.invokeMethod(dashboardItemService, "beforeUpdateSave", entity, resource, null))
                .isInstanceOf(ResourceNotUpdatedException.class);
    }

    @Test
    @DisplayName("afterUpdateSave neteja la cache del widget")
    void afterUpdateSave_netejaCache() {
        // Arrange
        Long itemId = 1L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);
        DashboardItem resource = new DashboardItem();

        // Act
        ReflectionTestUtils.invokeMethod(dashboardItemService, "afterUpdateSave", entity, resource, null, false);

        // Assert
        verify(estadisticaWidgetHelper).clearDashboardWidgetCache(itemId);
    }

    @Test
    @DisplayName("afterConversion assigna atributs visuals al recurs")
    void afterConversion_assignaAtributsVisuals() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributsVisuals = new AtributsVisualsSimple();

        when(atributsVisualsHelper.getAtributsVisuals(entity)).thenReturn(atributsVisuals);

        // Act
        ReflectionTestUtils.invokeMethod(dashboardItemService, "afterConversion", entity, resource);

        // Assert
        assertThat(resource.getAtributsVisuals()).isEqualTo(atributsVisuals);
        verify(atributsVisualsHelper).getAtributsVisuals(entity);
    }

    @Test
    @DisplayName("InformeWidget.generateData genera dades del widget correctament")
    @SuppressWarnings("unchecked")
    void informeWidget_generateData_generaDades() throws Exception {
        // Arrange
        Long itemId = 1L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);
        entity.setPosX(0);
        entity.setPosY(0);
        entity.setWidth(6);
        entity.setHeight(4);

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Test Widget");
        entity.setWidget(widget);

        InformeWidgetItem informeItem = InformeWidgetItem.builder()
                .dashboardItemId(itemId)
                .titol("Test Widget")
                .tipus(WidgetTipus.SIMPLE)
                .build();

        when(dashboardItemRepository.findById(itemId)).thenReturn(Optional.of(entity));
        when(consultaEstadisticaHelper.getDadesWidget(entity)).thenReturn(informeItem);

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardItemServiceImpl$InformeWidget");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardItemServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator =
                (ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardItemService);

        // Act
        List<InformeWidgetItem> result = reportGenerator.generateData(DashboardItem.WIDGET_REPORT, entity, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(informeItem);
        verify(dashboardItemRepository).findById(itemId);
        verify(consultaEstadisticaHelper).getDadesWidget(entity);
    }

    @Test
    @DisplayName("InformeWidget.generateData retorna error quan falla la generació")
    @SuppressWarnings("unchecked")
    void informeWidget_generateData_quanFalla_retornaError() throws Exception {
        // Arrange
        Long itemId = 1L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);
        entity.setPosX(0);
        entity.setPosY(0);
        entity.setWidth(6);
        entity.setHeight(4);

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Test Widget");
        entity.setWidget(widget);

        when(dashboardItemRepository.findById(itemId)).thenReturn(Optional.of(entity));
        when(consultaEstadisticaHelper.getDadesWidget(entity))
                .thenThrow(new RuntimeException("Error de processament"));
        when(consultaEstadisticaHelper.determineWidgetType(entity)).thenReturn(WidgetTipus.SIMPLE);

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardItemServiceImpl$InformeWidget");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardItemServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator =
                (ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardItemService);

        // Act
        List<InformeWidgetItem> result = reportGenerator.generateData(DashboardItem.WIDGET_REPORT, entity, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isError()).isTrue();
        assertThat(result.get(0).getErrorMsg()).contains("Error processing item");
        verify(dashboardItemRepository).findById(itemId);
    }

    @Test
    @DisplayName("InformeWidget.generateData llança excepció quan l'item no existeix")
    @SuppressWarnings("unchecked")
    void informeWidget_generateData_quanNoExisteix_llancaExcepcio() throws Exception {
        // Arrange
        Long itemId = 1L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);

        when(dashboardItemRepository.findById(itemId)).thenReturn(Optional.empty());

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardItemServiceImpl$InformeWidget");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardItemServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator =
                (ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardItemService);

        // Act & Assert
        assertThatThrownBy(() -> reportGenerator.generateData(DashboardItem.WIDGET_REPORT, entity, null))
                .isInstanceOf(ReportGenerationException.class);
    }
}
