package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.base.config.BaseConfig;
import es.caib.comanda.client.AclServiceClient;
import es.caib.comanda.client.model.acl.PermissionEnum;
import es.caib.comanda.client.model.acl.ResourceType;
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
import es.caib.comanda.ms.logic.helper.AuthenticationHelper;
import es.caib.comanda.ms.logic.helper.HttpAuthorizationHeaderHelper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardItemServiceImpl")
class DashboardItemServiceImplTest {

    @Mock private ConsultaEstadisticaHelper consultaEstadisticaHelper;
    @Mock private AtributsVisualsHelper atributsVisualsHelper;
    @Mock private EstadisticaWidgetHelper estadisticaWidgetHelper;
    @Mock private DashboardItemTitolHelper dashboardItemTitolHelper;
    @Mock private DashboardItemRepository dashboardItemRepository;

    // Mocks afegits per cobrir la lògica de filtres i permisos
    @Mock private AuthenticationHelper authenticationHelper;
    @Mock private HttpAuthorizationHeaderHelper httpAuthorizationHeaderHelper;
    @Mock private AclServiceClient aclServiceClient;

    @InjectMocks
    private DashboardItemServiceImpl dashboardItemService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dashboardItemService, "entityRepository", dashboardItemRepository);
        lenient().when(httpAuthorizationHeaderHelper.getAuthorizationHeader()).thenReturn("Bearer token");
        lenient().when(authenticationHelper.getCurrentUserName()).thenReturn("testUser");
        lenient().when(authenticationHelper.getCurrentUserRealmRoles()).thenReturn(new String[]{"ROLE_USER"});
    }

    // ========================================================================
    // 1. TESTOS PER A FILTRATGE I PERMISOS (COBERTURA CRÍTICA)
    // ========================================================================

    @Test
    @DisplayName("additionalSpringFilter: retorna filtre original per a usuaris ADMIN")
    void additionalSpringFilter_quanEsAdmin_llavorsRetornaFiltreOriginal() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(true);

        // Act
        String result = dashboardItemService.additionalSpringFilter("filtreBase", new String[0]);

        // Assert
        assertThat(result).isEqualTo("filtreBase");
        verify(aclServiceClient, never()).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna filtre original per a usuaris CONSULTA")
    void additionalSpringFilter_quanEsConsulta_llavorsRetornaFiltreOriginal() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(true);

        // Act
        String result = dashboardItemService.additionalSpringFilter("filtreBase", new String[0]);

        // Assert
        assertThat(result).isEqualTo("filtreBase");
    }

    @Test
    @DisplayName("additionalSpringFilter: aplica filtres de permisos per a usuaris normals amb accés")
    void additionalSpringFilter_quanEsUsuariNormalIAccessible_llavorsAplicaFiltres() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);

        Set<Serializable> permisos = Set.of(1L, 2L);
        when(aclServiceClient.findIdsWithAnyPermission(any(ResourceType.class), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(permisos));

        // Act
        String result = dashboardItemService.additionalSpringFilter("base", new String[0]);

        // Assert
        assertThat(result).contains("base");
        assertThat(result).contains("widget.appId:1 or widget.appId:2"); // Simplificat per la lògica de SpringFilterHelper
        verify(aclServiceClient, times(3)).findIdsWithAnyPermission(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("additionalSpringFilter: retorna id:0 quan l'usuari normal no té cap permís")
    void additionalSpringFilter_quanEsUsuariNormalISensePermisos_llavorsRetornaIdZero() {
        // Arrange
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_ADMIN)).thenReturn(false);
        when(authenticationHelper.isCurrentUserInRole(BaseConfig.ROLE_CONSULTA)).thenReturn(false);

        when(aclServiceClient.findIdsWithAnyPermission(any(ResourceType.class), anyList(), anyString(), anyList(), anyString()))
            .thenReturn(ResponseEntity.ok(Collections.emptySet()));

        // Act
        String result = dashboardItemService.additionalSpringFilter("base", new String[0]);

        // Assert
        assertThat(result).isEqualTo("base and id:0");
    }

    @Test
    @DisplayName("getAllowedIds: retorna conjunt buit quan el client ACL retorna null")
    void getAllowedIds_quanAclRetornaNull_llavorsRetornaConjuntBuit() {
        // Arrange
        when(aclServiceClient.findIdsWithAnyPermission(any(), any(), any(), any(), anyString())).thenReturn(ResponseEntity.ok(null));

        // Act
        Set<Serializable> result = dashboardItemService.getAllowedIds(ResourceType.APP, List.of(PermissionEnum.READ));

        // Assert
        assertThat(result).isEmpty();
    }

    // ========================================================================
    // 2. TESTOS PER A CICLE DE VIDA DE L'ENTITAT
    // ========================================================================

    @Test
    @DisplayName("completeResource: delega correctament a dashboardItemTitolHelper")
    void completeResource_quanEsCrida_llavorsDelegaAHelper() {
        // Arrange
        DashboardItem item = new DashboardItem();

        // Act
        dashboardItemService.completeResource(item);

        // Assert
        verify(dashboardItemTitolHelper, times(1)).completeResourceItemLogic(item);
    }

    @Test
    @DisplayName("beforeCreateSave: converteix atributs visuals a JSON correctament")
    void beforeCreateSave_quanEsValid_llavorsConverteixJson() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributs = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributs);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":\"value\"}");

        // Act
        dashboardItemService.beforeCreateSave(entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo("{\"test\":\"value\"}");
    }

    @Test
    @DisplayName("beforeCreateSave: llança ResourceNotCreatedException quan falla la conversió JSON")
    void beforeCreateSave_quanFallaConversio_llancaExcepcioCreacio() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        DashboardItem resource = new DashboardItem();
        resource.setAtributsVisuals(new AtributsVisualsSimple());

        when(atributsVisualsHelper.getAtributsVisualsJson(any())).thenThrow(new RuntimeException("Error JSON"));

        // Act & Assert
        assertThatThrownBy(() -> dashboardItemService.beforeCreateSave(entity, resource, null))
            .isInstanceOf(ResourceNotCreatedException.class);
    }

    @Test
    @DisplayName("beforeUpdateSave: converteix atributs visuals a JSON correctament")
    void beforeUpdateSave_quanEsValid_llavorsConverteixJson() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(1L);
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributs = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributs);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":\"value\"}");

        // Act
        dashboardItemService.beforeUpdateSave(entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo("{\"test\":\"value\"}");
    }

    @Test
    @DisplayName("beforeUpdateSave: llança ResourceNotUpdatedException quan falla la conversió JSON")
    void beforeUpdateSave_quanFallaConversio_llancaExcepcioActualitzacio() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(1L);
        DashboardItem resource = new DashboardItem();
        resource.setAtributsVisuals(new AtributsVisualsSimple());

        when(atributsVisualsHelper.getAtributsVisualsJson(any())).thenThrow(new RuntimeException("Error JSON"));

        // Act & Assert
        assertThatThrownBy(() -> dashboardItemService.beforeUpdateSave(entity, resource, null))
            .isInstanceOf(ResourceNotUpdatedException.class);
    }

    @Test
    @DisplayName("afterUpdateSave: neteja la cache del widget independentment de anyOrderChanged")
    void afterUpdateSave_quanEsCrida_llavorsNetejaCache() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(42L);

        // Act
        dashboardItemService.afterUpdateSave(entity, new DashboardItem(), null, true);

        // Assert
        verify(estadisticaWidgetHelper, times(1)).clearDashboardWidgetCache(42L);
    }

    @Test
    @DisplayName("afterConversion: assigna atributs visuals deserialitzats al recurs")
    void afterConversion_quanEsCrida_llavorsAssignaAtributs() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        DashboardItem resource = new DashboardItem();
        AtributsVisuals atributs = new AtributsVisualsSimple();

        when(atributsVisualsHelper.getAtributsVisuals(entity)).thenReturn(atributs);

        // Act
        dashboardItemService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getAtributsVisuals()).isSameAs(atributs);
    }

    // ========================================================================
    // 3. TESTOS PER A InformeWidget (ReportGenerator)
    // ========================================================================

    @Test
    @DisplayName("InformeWidget.generateData: genera dades del widget correctament amb tema clar")
    void informeWidgetGenerateData_quanEsValid_llavorsRetornaItem() {
        // Arrange
        Long itemId = 1L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);
        entity.setWidget(new EstadisticaSimpleWidgetEntity());

        InformeWidgetItem expectedItem = InformeWidgetItem.builder().dashboardItemId(itemId).build();
        when(dashboardItemRepository.findById(itemId)).thenReturn(Optional.of(entity));
        when(consultaEstadisticaHelper.getDadesWidget(entity, false)).thenReturn(expectedItem);

        // Instanciació neta de la classe interna sense reflexió complexa
        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> generator =
            dashboardItemService.new InformeWidget();

        // Act
        List<InformeWidgetItem> result = generator.generateData(DashboardItem.WIDGET_REPORT, entity, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(expectedItem);
        verify(consultaEstadisticaHelper).getDadesWidget(entity, false);
    }

    @Test
    @DisplayName("InformeWidget.generateData: passa temaFosc=true correctament als paràmetres")
    void informeWidgetGenerateData_quanTemaFoscEsTrue_llavorsPassaParametre() {
        // Arrange
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(1L);
        entity.setWidget(new EstadisticaSimpleWidgetEntity());

        InformeWidgetParams params = new InformeWidgetParams();
        params.setTemaFosc(true);

        when(dashboardItemRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(consultaEstadisticaHelper.getDadesWidget(entity, true)).thenReturn(InformeWidgetItem.builder().build());

        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> generator =
            dashboardItemService.new InformeWidget();

        // Act
        generator.generateData(DashboardItem.WIDGET_REPORT, entity, params);

        // Assert
        verify(consultaEstadisticaHelper).getDadesWidget(entity, true);
    }

    @Test
    @DisplayName("InformeWidget.generateData: captura excepció i retorna item amb estat d'error")
    void informeWidgetGenerateData_quanFallaGeneracio_llavorsRetornaItemError() {
        // Arrange
        Long itemId = 99L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);
        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setTitol("Widget Test");
        entity.setWidget(widget);

        when(dashboardItemRepository.findById(itemId)).thenReturn(Optional.of(entity));
        when(consultaEstadisticaHelper.getDadesWidget(any(), anyBoolean())).thenThrow(new RuntimeException("Fallada de xarxa"));
        when(consultaEstadisticaHelper.determineWidgetType(entity)).thenReturn(WidgetTipus.SIMPLE);

        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> generator =
            dashboardItemService.new InformeWidget();

        // Act
        List<InformeWidgetItem> result = generator.generateData(DashboardItem.WIDGET_REPORT, entity, null);

        // Assert
        assertThat(result).hasSize(1);
        InformeWidgetItem errorItem = result.get(0);
        assertThat(errorItem.isError()).isTrue();
        assertThat(errorItem.getErrorMsg()).contains("Error processing item 99");
        assertThat(errorItem.getErrorTrace()).contains("java.lang.RuntimeException");
        assertThat(errorItem.getTitol()).isEqualTo("Widget Test");
    }

    @Test
    @DisplayName("InformeWidget.generateData: llança ReportGenerationException quan l'entitat no existeix")
    void informeWidgetGenerateData_quanEntitatNoExisteix_llancaExcepcio() {
        // Arrange
        Long itemId = 1L;
        DashboardItemEntity entity = new DashboardItemEntity();
        entity.setId(itemId);

        when(dashboardItemRepository.findById(itemId)).thenReturn(Optional.empty());

        ReportGenerator<DashboardItemEntity, InformeWidgetParams, InformeWidgetItem> generator =
            dashboardItemService.new InformeWidget();

        // Act & Assert
        assertThatThrownBy(() -> generator.generateData(DashboardItem.WIDGET_REPORT, entity, null))
            .isInstanceOf(ReportGenerationException.class)
            .hasMessageContaining("No existeix");
    }
}
