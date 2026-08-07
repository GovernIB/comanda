package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.AtributsVisualsHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaGraficWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.intf.model.atributsvisuals.AtributsVisualsGrafic;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.logic.intf.model.widget.WidgetBaseResource;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
@DisplayName("Tests per a EstadisticaGraficWidgetServiceImpl")
class EstadisticaGraficWidgetServiceImplTest {

    @Mock
    private EstadisticaGraficWidgetHelper estadisticaGraficWidgetHelper;

    @Mock
    private EstadisticaWidgetHelper estadisticaWidgetHelper;

    @Mock
    private AtributsVisualsHelper atributsVisualsHelper;

    @InjectMocks
    private EstadisticaGraficWidgetServiceImpl estadisticaGraficWidgetService;

    // ========================================================================
    // 1. TESTOS PER A beforeCreateSave
    // ========================================================================

    @Test
    @DisplayName("beforeCreateSave: converteix atributs visuals a JSON correctament")
    void beforeCreateSave_quanEsValid_llavorsConverteixJson() throws ResourceNotCreatedException {
        // Arrange
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();
        AtributsVisualsGrafic atributs = new AtributsVisualsGrafic();
        resource.setAtributsVisuals(atributs);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":\"value\"}");

        // Act
        estadisticaGraficWidgetService.beforeCreateSave(entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo("{\"test\":\"value\"}");
        verify(atributsVisualsHelper, times(1)).getAtributsVisualsJson(atributs);
    }

    @Test
    @DisplayName("beforeCreateSave: llança ResourceNotCreatedException quan falla la conversió JSON")
    void beforeCreateSave_quanFallaConversio_llancaExcepcioCreacio() {
        // Arrange
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();
        resource.setAtributsVisuals(new AtributsVisualsGrafic());

        when(atributsVisualsHelper.getAtributsVisualsJson(any())).thenThrow(new RuntimeException("Error de serialització"));

        // Act & Assert
        assertThatThrownBy(() -> estadisticaGraficWidgetService.beforeCreateSave(entity, resource, null))
            .isInstanceOf(ResourceNotCreatedException.class)
            .hasMessageContaining("Error convertint atributs visuals a JSON");
    }

    // ========================================================================
    // 2. TESTOS PER A beforeUpdateSave
    // ========================================================================

    @Test
    @DisplayName("beforeUpdateSave: converteix atributs visuals a JSON correctament")
    void beforeUpdateSave_quanEsValid_llavorsConverteixJson() {
        // Arrange
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        entity.setId(1L);
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();
        AtributsVisualsGrafic atributs = new AtributsVisualsGrafic();
        resource.setAtributsVisuals(atributs);

        when(atributsVisualsHelper.getAtributsVisualsJson(atributs)).thenReturn("{\"test\":\"value\"}");

        // Act
        estadisticaGraficWidgetService.beforeUpdateSave(entity, resource, null);

        // Assert
        assertThat(entity.getAtributsVisualsJson()).isEqualTo("{\"test\":\"value\"}");
        verify(atributsVisualsHelper, times(1)).getAtributsVisualsJson(atributs);
    }

    @Test
    @DisplayName("beforeUpdateSave: llança ResourceNotUpdatedException quan falla la conversió JSON")
    void beforeUpdateSave_quanFallaConversio_llancaExcepcioActualitzacio() {
        // Arrange
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        entity.setId(1L);
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();
        resource.setAtributsVisuals(new AtributsVisualsGrafic());

        when(atributsVisualsHelper.getAtributsVisualsJson(any())).thenThrow(new RuntimeException("Error de serialització"));

        // Act & Assert
        assertThatThrownBy(() -> estadisticaGraficWidgetService.beforeUpdateSave(entity, resource, null))
            .isInstanceOf(ResourceNotUpdatedException.class)
            .hasMessageContaining("Error convertint atributs visuals a JSON");
    }

    // ========================================================================
    // 3. TESTOS PER A afterCreateSave i afterUpdateSave
    // ========================================================================

    @Test
    @DisplayName("afterCreateSave: crida als helpers per actualitzar columnes i dimensions")
    void afterCreateSave_quanEsValid_llavorsCridaHelpers() {
        // Arrange
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();

        // Act
        estadisticaGraficWidgetService.afterCreateSave(entity, resource, null, false);

        // Assert
        verify(estadisticaGraficWidgetHelper, times(1)).upsertColumnes(entity, resource);
        verify(estadisticaWidgetHelper, times(1)).upsertDimensionsValors(entity, resource);
        verify(estadisticaWidgetHelper, never()).clearDashboardWidgetCacheByWidget(anyLong());
    }

    @Test
    @DisplayName("afterUpdateSave: crida als helpers per actualitzar columnes, dimensions i netejar cache")
    void afterUpdateSave_quanEsValid_llavorsCridaHelpersINetejaCache() {
        // Arrange
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        entity.setId(42L);
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();

        // Act
        estadisticaGraficWidgetService.afterUpdateSave(entity, resource, null, false);

        // Assert
        verify(estadisticaGraficWidgetHelper, times(1)).upsertColumnes(entity, resource);
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
        EstadisticaGraficWidgetEntity entity = new EstadisticaGraficWidgetEntity();
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();
        AtributsVisualsGrafic atributs = new AtributsVisualsGrafic();

        when(atributsVisualsHelper.getAtributsVisuals(entity)).thenReturn(atributs);

        // Act
        estadisticaGraficWidgetService.afterConversion(entity, resource);

        // Assert
        verify(estadisticaGraficWidgetHelper, times(1)).afterCoversionGetColumnes(entity, resource);
        verify(estadisticaWidgetHelper, times(1)).afterConversionGetDimensions(entity, resource);
        verify(estadisticaWidgetHelper, times(1)).afterConversionGetAppNom(entity, resource);
        verify(atributsVisualsHelper, times(1)).getAtributsVisuals(entity);
        assertThat(resource.getAtributsVisuals()).isSameAs(atributs);
    }

    @Test
    @DisplayName("completeResource: estableix l'appId a partir de l'aplicació")
    void completeResource_quanEsValid_llavorsEstableixAppId() {
        // Arrange
        EstadisticaGraficWidget resource = new EstadisticaGraficWidget();
        ResourceReference aplicacio = new ResourceReference();
        aplicacio.setId(99L);
        resource.setAplicacio(aplicacio);

        // Act
        estadisticaGraficWidgetService.completeResource(resource);

        // Assert
        assertThat(resource.getAppId()).isEqualTo(99L);
    }

    // ========================================================================
    // 5. TESTOS PER A onChange
    // ========================================================================

    @Test
    @DisplayName("onChange: retorna canvis quan el camp aplicacio canvia i hi ha indicadorsInfo")
    void onChange_quanAplicacioCanviaIAmbIndicadorsInfo_llavorsRetornaCanvis() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaGraficWidget previous = new EstadisticaGraficWidget();
        ResourceReference oldAplicacio = new ResourceReference();
        oldAplicacio.setId(1L);
        previous.setAplicacio(oldAplicacio);

        IndicadorTaula ind1 = new IndicadorTaula();
        ind1.setIndicador(ResourceReference.toResourceReference(10L, "Ind1"));
        previous.setIndicadorsInfo(Arrays.asList(ind1));

        ResourceReference newAplicacio = new ResourceReference();
        newAplicacio.setId(2L);

        // Act
        Map<String, Object> changes = estadisticaGraficWidgetService.onChange(id, previous, WidgetBaseResource.Fields.aplicacio, newAplicacio, new HashMap<>());

        // Assert
        assertThat(changes).containsEntry(WidgetBaseResource.Fields.dimensionsValor, null);
        assertThat(changes).containsEntry(EstadisticaGraficWidget.Fields.indicador, null);
        assertThat(changes).containsEntry(EstadisticaGraficWidget.Fields.descomposicioDimensio, null);

        @SuppressWarnings("unchecked")
        List<IndicadorTaula> indicadorsInfo = (List<IndicadorTaula>) changes.get(EstadisticaGraficWidget.Fields.indicadorsInfo);
        assertThat(indicadorsInfo).hasSize(1);
        assertThat(indicadorsInfo.get(0).getIndicador()).isNull();

        assertThat(changes).containsEntry(EstadisticaGraficWidget.Fields.indicadorsInfo + ".0." + IndicadorTaula.Fields.indicador, null);
    }

    @Test
    @DisplayName("onChange: retorna canvis quan el camp aplicacio canvia però indicadorsInfo és null")
    void onChange_quanAplicacioCanviaISenseIndicadorsInfo_llavorsRetornaCanvisSenseIndicadors() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaGraficWidget previous = new EstadisticaGraficWidget();
        ResourceReference oldAplicacio = new ResourceReference();
        oldAplicacio.setId(1L);
        previous.setAplicacio(oldAplicacio);
        previous.setIndicadorsInfo(null);

        ResourceReference newAplicacio = new ResourceReference();
        newAplicacio.setId(2L);

        // Act
        Map<String, Object> changes = estadisticaGraficWidgetService.onChange(id, previous, WidgetBaseResource.Fields.aplicacio, newAplicacio, new HashMap<>());

        // Assert
        assertThat(changes).containsEntry(WidgetBaseResource.Fields.dimensionsValor, null);
        assertThat(changes).doesNotContainKey(EstadisticaGraficWidget.Fields.indicadorsInfo);
    }

    @Test
    @DisplayName("onChange: retorna mapa buit quan el camp no és aplicacio")
    void onChange_quanCampNoEsAplicacio_llavorsRetornaMapaBuit() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaGraficWidget previous = new EstadisticaGraficWidget();

        // Act
        Map<String, Object> changes = estadisticaGraficWidgetService.onChange(id, previous, "altreCamp", "valor", new HashMap<>());

        // Assert
        assertThat(changes).isEmpty();
    }

    @Test
    @DisplayName("onChange: retorna mapa buit quan el valor d'aplicacio no ha canviat")
    void onChange_quanAplicacioNoCanvia_llavorsRetornaMapaBuit() throws ResourceFieldNotFoundException, AnswerRequiredException {
        // Arrange
        Long id = 1L;
        EstadisticaGraficWidget previous = new EstadisticaGraficWidget();
        ResourceReference aplicacio = new ResourceReference();
        aplicacio.setId(1L);
        previous.setAplicacio(aplicacio);

        // Act
        Map<String, Object> changes = estadisticaGraficWidgetService.onChange(id, previous, WidgetBaseResource.Fields.aplicacio, aplicacio, new HashMap<>());

        // Assert
        assertThat(changes).isEmpty();
    }
}
