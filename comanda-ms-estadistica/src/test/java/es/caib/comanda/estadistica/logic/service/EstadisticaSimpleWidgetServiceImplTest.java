package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaSimpleWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsSimple;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetBaseResource;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import es.caib.comanda.ms.logic.intf.exception.ResourceFieldNotFoundException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotCreatedException;
import es.caib.comanda.ms.logic.intf.exception.ResourceNotUpdatedException;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaSimpleWidgetServiceImpl")
class EstadisticaSimpleWidgetServiceImplTest {

    @Mock
    private EstadisticaWidgetHelper estadisticaWidgetHelper;

    @Mock
    private EstadisticaSimpleWidgetHelper estadisticaSimpleWidgetHelper;

    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;

    @InjectMocks
    private EstadisticaSimpleWidgetServiceImpl estadisticaSimpleWidgetService;

    // ========================================================================
    // 1. TESTOS PER A beforeCreateSave
    // ========================================================================

    @Test
    @DisplayName("beforeCreateSave: converteix atributs visuals a JSON i actualitza indicador correctament")
    void beforeCreateSave_quanEsValid_llavorsConverteixJsonIActualitzaIndicador() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();
        AtributsVisualsSimple atributs = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributs);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":\"value\"}");

        // Act
        estadisticaSimpleWidgetService.beforeCreateSave(entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo("{\"test\":\"value\"}");
        verify(atributsVisualsHelper, times(1)).getAtributsVisualsJson(atributs);
        verify(estadisticaSimpleWidgetHelper, times(1)).upsertIndicadorTaula(entity, resource);
    }

    @Test
    @DisplayName("beforeCreateSave: llança ResourceNotCreatedException quan falla la conversió JSON")
    void beforeCreateSave_quanFallaConversio_llancaExcepcioCreacio() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();
        resource.setAtributsVisuals(new AtributsVisualsSimple());

        when(atributsVisualsHelper.getAtributsVisualsJson(any())).thenThrow(new RuntimeException("Error de serialització"));

        // Act & Assert
        assertThatThrownBy(() -> estadisticaSimpleWidgetService.beforeCreateSave(entity, resource, null))
            .isInstanceOf(ResourceNotCreatedException.class)
            .hasMessageContaining("Error convertint atributs visuals a JSON");
    }

    // ========================================================================
    // 2. TESTOS PER A beforeUpdateSave
    // ========================================================================

    @Test
    @DisplayName("beforeUpdateSave: converteix atributs visuals a JSON i actualitza indicador")
    void beforeUpdateSave_quanEsValid_llavorsConverteixJsonIActualitzaIndicador() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        entity.setId(1L);
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();
        AtributsVisualsSimple atributs = new AtributsVisualsSimple();
        resource.setAtributsVisuals(atributs);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":\"value\"}");

        // Act
        estadisticaSimpleWidgetService.beforeUpdateSave(entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo("{\"test\":\"value\"}");
        verify(atributsVisualsHelper, times(1)).getAtributsVisualsJson(atributs);
        verify(estadisticaSimpleWidgetHelper, times(1)).upsertIndicadorTaula(entity, resource);
    }

    @Test
    @DisplayName("beforeUpdateSave: llança ResourceNotUpdatedException quan falla la conversió JSON")
    void beforeUpdateSave_quanFallaConversio_llancaExcepcioActualitzacio() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        entity.setId(1L);
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();
        resource.setAtributsVisuals(new AtributsVisualsSimple());

        when(atributsVisualsHelper.getAtributsVisualsJson(any())).thenThrow(new RuntimeException("Error de serialització"));

        // Act & Assert
        assertThatThrownBy(() -> estadisticaSimpleWidgetService.beforeUpdateSave(entity, resource, null))
            .isInstanceOf(ResourceNotUpdatedException.class)
            .hasMessageContaining("Error convertint atributs visuals a JSON");
    }

    // ========================================================================
    // 3. TESTOS PER A afterCreateSave i afterUpdateSave
    // ========================================================================

    @Test
    @DisplayName("afterCreateSave: crida als helpers per actualitzar dimensions")
    void afterCreateSave_quanEsValid_llavorsCridaHelpers() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();

        // Act
        estadisticaSimpleWidgetService.afterCreateSave(entity, resource, null, false);

        // Assert
        verify(estadisticaWidgetHelper, times(1)).upsertDimensionsValors(entity, resource);
        verify(estadisticaWidgetHelper, never()).clearDashboardWidgetCacheByWidget(anyLong());
    }

    @Test
    @DisplayName("afterUpdateSave: crida als helpers per actualitzar dimensions i netejar cache")
    void afterUpdateSave_quanEsValid_llavorsCridaHelpersINetejaCache() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        entity.setId(42L);
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();

        // Act
        estadisticaSimpleWidgetService.afterUpdateSave(entity, resource, null, false);

        // Assert
        verify(estadisticaWidgetHelper, times(1)).upsertDimensionsValors(entity, resource);
        verify(estadisticaWidgetHelper, times(1)).clearDashboardWidgetCacheByWidget(42L);
    }

    // ========================================================================
    // 4. TESTOS PER A afterConversion i completeResource
    // ========================================================================

    @Test
    @DisplayName("afterConversion: assigna atributs visuals i crida als helpers de conversió")
    void afterConversion_quanEsValid_llavorsAssignaAtributsICridaHelpers() {
        // Arrange
        EstadisticaSimpleWidgetEntity entity = new EstadisticaSimpleWidgetEntity();
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();
        AtributsVisualsSimple atributs = new AtributsVisualsSimple();

        when(atributsVisualsHelper.getAtributsVisuals(entity)).thenReturn(atributs);

        // Act
        estadisticaSimpleWidgetService.afterConversion(entity, resource);

        // Assert
        verify(estadisticaSimpleWidgetHelper, times(1)).afterCoversionGetIndicadorTaulaAtributes(entity, resource);
        verify(estadisticaWidgetHelper, times(1)).afterConversionGetDimensions(entity, resource);
        verify(estadisticaWidgetHelper, times(1)).afterConversionGetAppNom(entity, resource);
        verify(atributsVisualsHelper, times(1)).getAtributsVisuals(entity);
        assertThat(resource.getAtributsVisuals()).isSameAs(atributs);
    }

    @Test
    @DisplayName("completeResource: estableix l'appId a partir de l'aplicació")
    void completeResource_quanEsValid_llavorsEstableixAppId() {
        // Arrange
        EstadisticaSimpleWidget resource = new EstadisticaSimpleWidget();
        ResourceReference aplicacio = new ResourceReference();
        aplicacio.setId(99L);
        resource.setAplicacio(aplicacio);

        // Act
        estadisticaSimpleWidgetService.completeResource(resource);

        // Assert
        assertThat(resource.getAppId()).isEqualTo(99L);
    }

    // ========================================================================
    // 5. TESTOS PER A onChange
    // ========================================================================

    @Test
    @DisplayName("onChange: retorna canvis quan el camp aplicacio canvia")
    void onChange_quanAplicacioCanvia_llavorsRetornaCanvis() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaSimpleWidget previous = new EstadisticaSimpleWidget();
        ResourceReference oldAplicacio = new ResourceReference();
        oldAplicacio.setId(1L);
        previous.setAplicacio(oldAplicacio);

        ResourceReference newAplicacio = new ResourceReference();
        newAplicacio.setId(2L);

        // Act
        Map<String, Object> changes = estadisticaSimpleWidgetService.onChange(id, previous, WidgetBaseResource.Fields.aplicacio, newAplicacio, new HashMap<>());

        // Assert
        assertThat(changes).hasSize(2);
        assertThat(changes).containsEntry(WidgetBaseResource.Fields.dimensionsValor, null);
        assertThat(changes).containsEntry(EstadisticaSimpleWidget.Fields.indicador, null);
    }

    @Test
    @DisplayName("onChange: retorna mapa buit quan el camp no és aplicacio")
    void onChange_quanCampNoEsAplicacio_llavorsRetornaMapaBuit() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaSimpleWidget previous = new EstadisticaSimpleWidget();

        // Act
        Map<String, Object> changes = estadisticaSimpleWidgetService.onChange(id, previous, "altreCamp", "valor", new HashMap<>());

        // Assert
        assertThat(changes).isEmpty();
    }

    @Test
    @DisplayName("onChange: retorna mapa buit quan el valor d'aplicacio no ha canviat")
    void onChange_quanAplicacioNoCanvia_llavorsRetornaMapaBuit() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaSimpleWidget previous = new EstadisticaSimpleWidget();
        ResourceReference aplicacio = new ResourceReference();
        aplicacio.setId(1L);
        previous.setAplicacio(aplicacio);

        // Act
        Map<String, Object> changes = estadisticaSimpleWidgetService.onChange(id, previous, WidgetBaseResource.Fields.aplicacio, aplicacio, new HashMap<>());

        // Assert
        assertThat(changes).isEmpty();
    }
}
