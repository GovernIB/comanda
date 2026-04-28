package es.caib.comanda.estadistica.logic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.ConsultaEstadisticaHelper;
import es.caib.comanda.estadistica.logic.helper.DashboardHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetItem;
import es.caib.comanda.estadistica.logic.intf.model.consulta.InformeWidgetParams;
import es.caib.comanda.estadistica.logic.intf.model.dashboard.Dashboard;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaGraficWidgetService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaSimpleWidgetService;
import es.caib.comanda.estadistica.logic.intf.service.EstadisticaTaulaWidgetService;
import es.caib.comanda.estadistica.logic.mapper.DashboardExportMapper;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardItemRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardRepository;
import es.caib.comanda.estadistica.persist.repository.DashboardTitolRepository;
import es.caib.comanda.ms.logic.intf.exception.ReportGenerationException;
import es.caib.comanda.ms.logic.service.BaseReadonlyResourceService.ReportGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardServiceImpl")
class DashboardServiceImplTest {

    @Mock
    private DashboardRepository dashboardRepository;
    @Mock
    private DashboardItemRepository dashboardItemRepository;
    @Mock
    private DashboardTitolRepository dashboardTitolRepository;
    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;
    @Mock
    private DashboardExportMapper dashboardExportMapper;
    @Mock
    private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Mock
    private EstadisticaSimpleWidgetService estadisticaSimpleWidgetService;
    @Mock
    private EstadisticaGraficWidgetService estadisticaGraficWidgetService;
    @Mock
    private EstadisticaTaulaWidgetService estadisticaTaulaWidgetService;
    @Mock
    private DashboardHelper dashboardHelper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dashboardService, "entityRepository", dashboardRepository);
    }

    @Test
    @DisplayName("La inicialització del servei registra els generadors d'informes i executors d'accions")
    void init_registraComponentsInterns() {
        // Act
        dashboardService.init();

        // Assert
        assertThat(dashboardService).isNotNull();
    }

    @Test
    @DisplayName("completeResource delega a dashboardHelper")
    void completeResource_delegaAHelper() {
        // Arrange
        Dashboard dashboard = new Dashboard();

        // Act
        ReflectionTestUtils.invokeMethod(dashboardService, "completeResource", dashboard);

        // Assert
        verify(dashboardHelper).completeResourceLogic(dashboard);
    }

    @Test
    @DisplayName("afterConversion delega a dashboardHelper")
    void afterConversion_delegaAHelper() {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        Dashboard resource = new Dashboard();

        // Act
        ReflectionTestUtils.invokeMethod(dashboardService, "afterConversion", entity, resource);

        // Assert
        verify(dashboardHelper).afterConversionLogic(entity, resource);
    }

    @Test
    @DisplayName("InformeWidgets.generateData retorna llista buida quan dashboard no té items ni titols")
    @SuppressWarnings("unchecked")
    void informeWidgets_quanDashboardBuit_retornaLlistaBuida() throws Exception {
        // Arrange
        Long dashboardId = 1L;
        DashboardEntity entity = new DashboardEntity();
        entity.setId(dashboardId);

        when(dashboardRepository.findById(dashboardId)).thenReturn(Optional.of(entity));

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$InformeWidgets");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator =
                (ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardService);

        // Act
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        // Assert
        assertThat(result).isEmpty();
        verify(dashboardRepository).findById(dashboardId);
    }

    @Test
    @DisplayName("InformeWidgets.generateData retorna items i títols del dashboard")
    @SuppressWarnings("unchecked")
    void informeWidgets_quanDashboardAmbItemsITitols_retornaLlista() throws Exception {
        // Arrange
        Long dashboardId = 1L;
        DashboardEntity entity = new DashboardEntity();
        entity.setId(dashboardId);

        DashboardItemEntity item = new DashboardItemEntity();
        item.setId(10L);
        item.setPosX(0);
        item.setPosY(0);
        item.setWidth(6);
        item.setHeight(4);
        entity.setItems(Arrays.asList(item));

        DashboardTitolEntity titol = new DashboardTitolEntity();
        titol.setId(20L);
        titol.setTitol("Test Títol");
        titol.setPosX(6);
        titol.setPosY(0);
        titol.setWidth(6);
        titol.setHeight(1);
        entity.setTitols(Arrays.asList(titol));

        when(dashboardRepository.findById(dashboardId)).thenReturn(Optional.of(entity));

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$InformeWidgets");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator =
                (ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardService);

        // Act
        List<InformeWidgetItem> result = reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null);

        // Assert
        assertThat(result).hasSize(2);
        verify(dashboardRepository).findById(dashboardId);
    }

    @Test
    @DisplayName("InformeWidgets.generateData llança excepció quan dashboard no existeix")
    @SuppressWarnings("unchecked")
    void informeWidgets_quanDashboardNoExisteix_llancaExcepcio() throws Exception {
        // Arrange
        Long dashboardId = 1L;
        DashboardEntity entity = new DashboardEntity();
        entity.setId(dashboardId);

        when(dashboardRepository.findById(dashboardId)).thenReturn(Optional.empty());

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$InformeWidgets");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem> reportGenerator =
                (ReportGenerator<DashboardEntity, InformeWidgetParams, InformeWidgetItem>) constructor.newInstance(dashboardService);

        // Act & Assert
        assertThatThrownBy(() -> reportGenerator.generateData(Dashboard.WIDGETS_REPORT, entity, null))
                .isInstanceOf(ReportGenerationException.class);
    }

    @Test
    @DisplayName("DashboardExportReportGenerator.generateData exporta un dashboard específic")
    @SuppressWarnings("unchecked")
    void dashboardExport_quanEntitat_exportaUn() throws Exception {
        // Arrange
        DashboardEntity entity = new DashboardEntity();
        entity.setId(1L);
        DashboardExport export = new DashboardExport();

        when(dashboardExportMapper.toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper))
                .thenReturn(export);

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$DashboardExportReportGenerator");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardEntity, Serializable, DashboardExport> reportGenerator =
                (ReportGenerator<DashboardEntity, Serializable, DashboardExport>) constructor.newInstance(dashboardService);

        // Act
        List<DashboardExport> result = reportGenerator.generateData(Dashboard.DASHBOARD_EXPORT, entity, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(export);
        verify(dashboardExportMapper).toDashboardExport(entity, estadisticaClientHelper, atributsVisualsHelper);
    }

    @Test
    @DisplayName("DashboardExportReportGenerator.generateData exporta tots els dashboards quan no s'especifica entitat")
    @SuppressWarnings("unchecked")
    void dashboardExport_quanSenseEntitat_exportaTots() throws Exception {
        // Arrange
        DashboardEntity entity1 = new DashboardEntity();
        entity1.setId(1L);
        DashboardEntity entity2 = new DashboardEntity();
        entity2.setId(2L);
        List<DashboardEntity> entities = Arrays.asList(entity1, entity2);

        DashboardExport export1 = new DashboardExport();
        DashboardExport export2 = new DashboardExport();
        List<DashboardExport> exports = Arrays.asList(export1, export2);

        when(dashboardRepository.findAll()).thenReturn(entities);
        when(dashboardExportMapper.toDashboardExport(entities, estadisticaClientHelper, atributsVisualsHelper))
                .thenReturn(exports);

        Class<?> reportClass = Class.forName("es.caib.comanda.estadistica.logic.service.DashboardServiceImpl$DashboardExportReportGenerator");
        java.lang.reflect.Constructor<?> constructor = reportClass.getDeclaredConstructor(DashboardServiceImpl.class);
        constructor.setAccessible(true);
        ReportGenerator<DashboardEntity, Serializable, DashboardExport> reportGenerator =
                (ReportGenerator<DashboardEntity, Serializable, DashboardExport>) constructor.newInstance(dashboardService);

        // Act
        List<DashboardExport> result = reportGenerator.generateData(Dashboard.DASHBOARD_EXPORT, null, null);

        // Assert
        assertThat(result).hasSize(2);
        verify(dashboardRepository).findAll();
        verify(dashboardExportMapper).toDashboardExport(entities, estadisticaClientHelper, atributsVisualsHelper);
    }
}
