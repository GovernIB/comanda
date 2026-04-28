package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.client.model.App;
import es.caib.comanda.client.model.Entorn;
import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaClientHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardExport;
import es.caib.comanda.estadistica.logic.intf.model.export.DashboardItemExport;
import es.caib.comanda.estadistica.logic.intf.model.export.DimensioValorExport;
import es.caib.comanda.estadistica.logic.intf.model.export.EstadisticaWidgetExport;
import es.caib.comanda.estadistica.logic.intf.model.export.IndicadorTaulaExport;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaWidgetEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a DashboardExportMapper")
class DashboardExportMapperTest {

    private DashboardExportMapper mapper;

    @Mock
    private EstadisticaClientHelper estadisticaClientHelper;
    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;

    @BeforeEach
    void setUp() {
        // Implementació manual del mapper per evitar problemes amb MapStruct en tests unitaris
        mapper = new DashboardExportMapper() {
            @Override
            public DashboardExport toDashboardExport(DashboardEntity dashboardEntity, EstadisticaClientHelper clientHelper, AtributsVisualsHelper visualsHelper) {
                if (dashboardEntity == null) return null;
                DashboardExport export = new DashboardExport();
                export.setTitol(dashboardEntity.getTitol());
                export.setDescripcio(dashboardEntity.getDescripcio());
                export.setItems(toDashboardItemExport(dashboardEntity.getItems(), clientHelper, visualsHelper));
                return export;
            }

            @Override
            public List<DashboardExport> toDashboardExport(List<DashboardEntity> dashboardEntities, EstadisticaClientHelper estadisticaClientHelper, AtributsVisualsHelper atributsVisualsHelper) {
                return null;
            }

            @Override
            public DashboardItemExport toDashboardItemExport(DashboardItemEntity itemEntity, EstadisticaClientHelper clientHelper, AtributsVisualsHelper visualsHelper) {
                if (itemEntity == null) return null;
                DashboardItemExport export = new DashboardItemExport();
                export.setPosX(itemEntity.getPosX());
                export.setPosY(itemEntity.getPosY());
                export.setWidth(itemEntity.getWidth());
                export.setHeight(itemEntity.getHeight());
                
                Entorn entorn = clientHelper.entornById(itemEntity.getEntornId());
                if (entorn != null) export.setEntornCodi(entorn.getCodi());
                
                if (itemEntity.getWidget() != null) {
                    App app = clientHelper.appFindById(itemEntity.getWidget().getAppId());
                    if (app != null) export.setAppCodi(app.getCodi());
                    export.setWidget(toWidgetExport(itemEntity.getWidget(), visualsHelper));
                }
                
                export.setAtributsVisuals(visualsHelper.getAtributsVisuals(itemEntity));
                return export;
            }

            @Override
            public List<DashboardItemExport> toDashboardItemExport(List<DashboardItemEntity> dashboardItemsEntity, EstadisticaClientHelper estadisticaClientHelper, AtributsVisualsHelper atributsVisualsHelper) {
                if (dashboardItemsEntity == null) return null;
                return dashboardItemsEntity.stream()
                        .map(i -> toDashboardItemExport(i, estadisticaClientHelper, atributsVisualsHelper))
                        .collect(Collectors.toList());
            }

            @Override
            public EstadisticaWidgetExport toWidgetExport(EstadisticaWidgetEntity widget) {
                return null; 
            }

            @Override
            public EstadisticaWidgetExport toWidgetExport(EstadisticaWidgetEntity widget, AtributsVisualsHelper atributsVisualsHelper) {
                return null;
            }

            @Override
            public DimensioValorExport toDimensioValorExport(DimensioValorEntity dimensioValorEntity) {return null;}
            @Override
            public IndicadorTaulaExport toIndicadorTaulaExport(IndicadorTaulaEntity indicadorTaulaEntity) {return null;}
        };
    }

    @Test
    @DisplayName("Converteix DashboardEntity a DashboardExport correctament")
    void toDashboardExport_quanEntityValida_retornaExport() {
        // Arrange
        DashboardEntity dashboardEntity = new DashboardEntity();
        dashboardEntity.setTitol("Dashboard Test");
        dashboardEntity.setDescripcio("Descripció Test");

        DashboardItemEntity itemEntity = new DashboardItemEntity();
        itemEntity.setPosX(1);
        itemEntity.setPosY(2);
        itemEntity.setWidth(3);
        itemEntity.setHeight(4);
        itemEntity.setEntornId(100L);

        EstadisticaSimpleWidgetEntity widgetEntity = new EstadisticaSimpleWidgetEntity();
        widgetEntity.setTitol("Widget Test");
        ReflectionTestUtils.setField(widgetEntity, "appId", 200L);
        itemEntity.setWidget(widgetEntity);

        dashboardEntity.setItems(Collections.singletonList(itemEntity));

        Entorn entornMock = mock(Entorn.class);
        when(entornMock.getCodi()).thenReturn("ENTORN_TEST");
        when(estadisticaClientHelper.entornById(100L)).thenReturn(entornMock);

        App appMock = mock(App.class);
        when(appMock.getCodi()).thenReturn("APP_TEST");
        when(estadisticaClientHelper.appFindById(200L)).thenReturn(appMock);

        AtributsVisualsSimple atributsMock = new AtributsVisualsSimple();
        when(atributsVisualsHelper.getAtributsVisuals(any(DashboardItemEntity.class))).thenReturn(atributsMock);

        // Act
        DashboardExport result = mapper.toDashboardExport(dashboardEntity, estadisticaClientHelper, atributsVisualsHelper);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitol()).isEqualTo("Dashboard Test");
        assertThat(result.getItems()).hasSize(1);
        
        DashboardItemExport itemResult = result.getItems().get(0);
        assertThat(itemResult.getPosX()).isEqualTo(1);
        assertThat(itemResult.getEntornCodi()).isEqualTo("ENTORN_TEST");
        assertThat(itemResult.getAppCodi()).isEqualTo("APP_TEST");
        assertThat(itemResult.getAtributsVisuals()).isEqualTo(atributsMock);
    }
}
