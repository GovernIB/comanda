package es.caib.comanda.estadistica.logic.service;

import es.caib.comanda.estadistica.logic.helper.EstadisticaWidgetHelper;
import es.caib.comanda.estadistica.logic.helper.PaletaHelper;
import es.caib.comanda.estadistica.logic.intf.model.paleta.Paleta;
import es.caib.comanda.estadistica.logic.intf.model.paleta.PaletaColor;
import es.caib.comanda.estadistica.persist.entity.paleta.PaletaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaGrupPaletesEntity;
import es.caib.comanda.estadistica.persist.repository.DashboardTemplatePaletteGroupRepository;
import es.caib.comanda.ms.logic.intf.exception.AnswerRequiredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a PaletaServiceImpl")
class PaletaServiceImplTest {

    @Mock
    private PaletaHelper paletaHelper;
    @Mock
    private DashboardTemplatePaletteGroupRepository dashboardTemplatePaletteGroupRepository;
    @Mock
    private EstadisticaWidgetHelper estadisticaWidgetHelper;

    @InjectMocks
    private PaletaServiceImpl paletaService;

    // ========================================================================
    // 1. TESTOS PER A afterCreateSave
    // ========================================================================

    @Test
    @DisplayName("afterCreateSave: delega correctament la sincronització de colors al helper")
    void afterCreateSave_quanEsCrida_llavorsDelegaSincronitzacioColors() {
        // Arrange
        PaletaEntity entity = new PaletaEntity();
        Paleta resource = new Paleta();
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();
        boolean anyOrderChanged = false;

        // Act
        paletaService.afterCreateSave(entity, resource, answers, anyOrderChanged);

        // Assert
        verify(paletaHelper, times(1)).syncColors(entity, resource);
    }

    // ========================================================================
    // 2. TESTOS PER A beforeUpdateSave
    // ========================================================================

    @Test
    @DisplayName("beforeUpdateSave: delega correctament la sincronització de colors al helper")
    void beforeUpdateSave_quanEsCrida_llavorsDelegaSincronitzacioColors() {
        // Arrange
        PaletaEntity entity = new PaletaEntity();
        Paleta resource = new Paleta();
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        // Act
        paletaService.beforeUpdateSave(entity, resource, answers);

        // Assert
        verify(paletaHelper, times(1)).syncColors(entity, resource);
    }

    // ========================================================================
    // 2b. TESTOS PER A afterUpdateSave
    // ========================================================================

    @Test
    @DisplayName("afterUpdateSave: invalida la cache d'estil resolt de totes les plantilles que utilitzen la paleta")
    void afterUpdateSave_quanPaletaUtilitzadaPerVariesPlantilles_llavorsInvalidaCadaUnaUnCop() {
        // Arrange
        PaletaEntity entity = new PaletaEntity();
        entity.setId(50L);
        Paleta resource = new Paleta();
        Map<String, AnswerRequiredException.AnswerValue> answers = new HashMap<>();

        PlantillaEntity plantilla1 = new PlantillaEntity();
        plantilla1.setId(1L);
        PlantillaEntity plantilla2 = new PlantillaEntity();
        plantilla2.setId(2L);

        PlantillaGrupPaletesEntity grupWidget = new PlantillaGrupPaletesEntity();
        grupWidget.setPlantilla(plantilla1);
        PlantillaGrupPaletesEntity grupChart = new PlantillaGrupPaletesEntity();
        grupChart.setPlantilla(plantilla2);
        // Mateixa plantilla que grupWidget: la paleta hi apareix dues vegades (widget i gràfic) però
        // només s'ha d'invalidar un cop.
        PlantillaGrupPaletesEntity grupDuplicat = new PlantillaGrupPaletesEntity();
        grupDuplicat.setPlantilla(plantilla1);

        when(dashboardTemplatePaletteGroupRepository.findByWidgetPaletteIdOrChartPaletteId(50L, 50L))
            .thenReturn(List.of(grupWidget, grupChart, grupDuplicat));

        // Act
        paletaService.afterUpdateSave(entity, resource, answers, false);

        // Assert
        verify(estadisticaWidgetHelper, times(1)).clearDashboardWidgetCacheByPlantilla(1L);
        verify(estadisticaWidgetHelper, times(1)).clearDashboardWidgetCacheByPlantilla(2L);
    }

    @Test
    @DisplayName("afterUpdateSave: no fa res quan cap plantilla utilitza la paleta")
    void afterUpdateSave_quanCapPlantillaLaUtilitza_llavorsNoFaRes() {
        // Arrange
        PaletaEntity entity = new PaletaEntity();
        entity.setId(50L);

        when(dashboardTemplatePaletteGroupRepository.findByWidgetPaletteIdOrChartPaletteId(anyLong(), anyLong()))
            .thenReturn(Collections.emptyList());

        // Act
        paletaService.afterUpdateSave(entity, new Paleta(), new HashMap<>(), false);

        // Assert
        verify(estadisticaWidgetHelper, never()).clearDashboardWidgetCacheByPlantilla(anyLong());
    }

    // ========================================================================
    // 3. TESTOS PER A afterConversion
    // ========================================================================

    @Test
    @DisplayName("afterConversion: estableix clientId com a String quan l'entitat té un ID no nul")
    void afterConversion_quanEntitatTeId_llavorsEstableixClientIdComString() {
        // Arrange
        PaletaEntity entity = new PaletaEntity();
        entity.setId(42L);

        Paleta resource = new Paleta();
        List<PaletaColor> mockColors = Collections.singletonList(new PaletaColor());

        when(paletaHelper.paletaEntitytoColorResources(entity)).thenReturn(mockColors);

        // Act
        paletaService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getClientId()).isEqualTo("42");
        assertThat(resource.getColors()).isSameAs(mockColors);
        verify(paletaHelper, times(1)).paletaEntitytoColorResources(entity);
    }

    @Test
    @DisplayName("afterConversion: estableix clientId com a null quan l'entitat té un ID nul")
    void afterConversion_quanEntitatNoTeId_llavorsEstableixClientIdComNull() {
        // Arrange
        PaletaEntity entity = new PaletaEntity();
        entity.setId(null); // ID explícitament nul

        Paleta resource = new Paleta();
        List<PaletaColor> mockColors = Collections.emptyList();

        when(paletaHelper.paletaEntitytoColorResources(entity)).thenReturn(mockColors);

        // Act
        paletaService.afterConversion(entity, resource);

        // Assert
        assertThat(resource.getClientId()).isNull();
        assertThat(resource.getColors()).isSameAs(mockColors);
        verify(paletaHelper, times(1)).paletaEntitytoColorResources(entity);
    }
}
