package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaTaulaWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaTaulaWidgetHelper")
class EstadisticaTaulaWidgetHelperTest {

    @Mock
    private IndicadorRepository indicadorRepository;

    @Mock
    private IndicadorTaulaRepository indicadorTaulaRepository;

    @InjectMocks
    private EstadisticaTaulaWidgetHelper estadisticaTaulaWidgetHelper;

    private EstadisticaTaulaWidgetEntity entity;
    private EstadisticaTaulaWidget resource;

    @BeforeEach
    void setUp() {
        entity = new EstadisticaTaulaWidgetEntity();
        resource = new EstadisticaTaulaWidget();
    }

    // ========================================================================
    // 1. TESTOS PER A upsertColumnes
    // ========================================================================

    @Test
    @DisplayName("upsertColumnes: esborra i neteja columnes existents quan resource és null")
    void upsertColumnes_quanEntityTeColumnesIResourceEsNull_llavorsEsborraIClearIretorna() {
        // Arrange
        List<IndicadorTaulaEntity> existingColumns = new ArrayList<>();
        existingColumns.add(new IndicadorTaulaEntity());
        entity.setColumnes(existingColumns);
        resource.setColumnes(null);

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorTaulaRepository, times(1)).deleteAll(existingColumns);
        assertThat(entity.getColumnes()).isEmpty();
        verify(indicadorTaulaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("upsertColumnes: no fa res quan entity no té columnes i resource és null")
    void upsertColumnes_quanEntityNoTeColumnesIResourceEsNull_llavorsNoFaRes() {
        // Arrange
        entity.setColumnes(null);
        resource.setColumnes(null);

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorTaulaRepository, never()).deleteAll(any());
        verify(indicadorTaulaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("upsertColumnes: crea i guarda columnes quan resource té columnes amb indicador")
    void upsertColumnes_quanResourceTeColumnesAmbIndicador_llavorsCreaIGuardaColumnes() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(1L);
        colResource.setIndicador(ref);
        resource.setColumnes(Collections.singletonList(colResource));

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        ArgumentCaptor<List<IndicadorTaulaEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(indicadorTaulaRepository, times(1)).saveAll(captor.capture());
        List<IndicadorTaulaEntity> savedColumns = captor.getValue();
        assertThat(savedColumns).hasSize(1);
        assertThat(savedColumns.get(0).getTitol()).isEqualTo("Titol 1");
        assertThat(savedColumns.get(0).getWidget()).isSameAs(entity);
    }

    @Test
    @DisplayName("upsertColumnes: filtra columnes quan el resource té indicador null")
    void upsertColumnes_quanResourceTeColumnesAmbIndicadorNull_llavorsFiltraColumna() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        colResource.setIndicador(null);
        resource.setColumnes(Collections.singletonList(colResource));

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorTaulaRepository, times(1)).saveAll(Collections.emptyList());
        verify(indicadorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertColumnes: filtra columnes quan l'ID de l'indicador del resource és null")
    void upsertColumnes_quanResourceTeColumnesAmbIndicadorIdNull_llavorsFiltraColumna() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(null);
        colResource.setIndicador(ref);
        resource.setColumnes(Collections.singletonList(colResource));

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorTaulaRepository, times(1)).saveAll(Collections.emptyList());
        verify(indicadorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertColumnes: busca i assigna l'indicador quan l'ID existeix i es troba al repositori")
    void upsertColumnes_quanResourceTeColumnesAmbIndicadorIdIExisteix_llavorsBuscaIAssignaIndicador() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(99L);
        colResource.setIndicador(ref);
        resource.setColumnes(Collections.singletonList(colResource));

        IndicadorEntity indicadorEntity = new IndicadorEntity();
        when(indicadorRepository.findById(99L)).thenReturn(Optional.of(indicadorEntity));

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorRepository, times(1)).findById(99L);
        ArgumentCaptor<List<IndicadorTaulaEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(indicadorTaulaRepository, times(1)).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getIndicador()).isSameAs(indicadorEntity);
    }

    @Test
    @DisplayName("upsertColumnes: no assigna l'indicador quan l'ID existeix però no es troba al repositori")
    void upsertColumnes_quanResourceTeColumnesAmbIndicadorIdINoExisteix_llavorsNoAssignaIndicador() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(99L);
        colResource.setIndicador(ref);
        resource.setColumnes(Collections.singletonList(colResource));

        when(indicadorRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorRepository, times(1)).findById(99L);
        ArgumentCaptor<List<IndicadorTaulaEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(indicadorTaulaRepository, times(1)).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getIndicador()).isNull();
    }

    @Test
    @DisplayName("upsertColumnes: assigna unitatAgregacio quan l'agregació és AVERAGE")
    void upsertColumnes_quanAgregacioEsAverage_llavorsAssignaUnitatAgregacio() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.AVERAGE);
        colResource.setUnitatAgregacio(PeriodeUnitat.DIA);
        ResourceReference ref = new ResourceReference();
        ref.setId(1L);
        colResource.setIndicador(ref);
        resource.setColumnes(Collections.singletonList(colResource));

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        ArgumentCaptor<List<IndicadorTaulaEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(indicadorTaulaRepository, times(1)).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getUnitatAgregacio()).isEqualTo(PeriodeUnitat.DIA);
    }

    @Test
    @DisplayName("upsertColumnes: assigna unitatAgregacio a null quan l'agregació no és AVERAGE")
    void upsertColumnes_quanAgregacioNoEsAverage_llavorsUnitatAgregacioEsNull() {
        // Arrange
        entity.setColumnes(new ArrayList<>());
        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol 1");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        colResource.setUnitatAgregacio(PeriodeUnitat.DIA);
        ResourceReference ref = new ResourceReference();
        ref.setId(1L);
        colResource.setIndicador(ref);
        resource.setColumnes(Collections.singletonList(colResource));

        // Act
        estadisticaTaulaWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        ArgumentCaptor<List<IndicadorTaulaEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(indicadorTaulaRepository, times(1)).saveAll(captor.capture());
        assertThat(captor.getValue().get(0).getUnitatAgregacio()).isNull();
    }

    // ========================================================================
    // 2. TESTOS PER A afterCoversionGetColumnes
    // ========================================================================

    @Test
    @DisplayName("afterCoversionGetColumnes: no fa res quan les columnes de l'entitat són null")
    void afterCoversionGetColumnes_quanEntityColumnesEsNull_llavorsNoFaRes() {
        // Arrange
        entity.setColumnes(null);

        // Act
        estadisticaTaulaWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getColumnes()).isNull();
    }

    @Test
    @DisplayName("afterCoversionGetColumnes: no fa res quan les columnes de l'entitat són buides")
    void afterCoversionGetColumnes_quanEntityColumnesEsBuida_llavorsNoFaRes() {
        // Arrange
        entity.setColumnes(Collections.emptyList());

        // Act
        estadisticaTaulaWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getColumnes()).isNull();
    }

    @Test
    @DisplayName("afterCoversionGetColumnes: mapeja correctament les columnes quan l'entitat en té")
    void afterCoversionGetColumnes_quanEntityTeColumnes_llavorsMapejaCorrectament() {
        // Arrange
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setId(5L);
        // Simulem el mètode que retorna el text (en un entorn real vindria de l'entitat)

        IndicadorTaulaEntity colEntity = new IndicadorTaulaEntity();
        colEntity.setTitol("Titol Mapejat");
        colEntity.setAgregacio(TableColumnsEnum.SUM);
        colEntity.setUnitatAgregacio(PeriodeUnitat.DIA);
        colEntity.setIndicador(indicador);

        entity.setColumnes(Collections.singletonList(colEntity));

        // Act
        estadisticaTaulaWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getColumnes()).hasSize(1);
        assertThat(resource.getColumnes().get(0).getTitol()).isEqualTo("Titol Mapejat");
        assertThat(resource.getColumnes().get(0).getAgregacio()).isEqualTo(TableColumnsEnum.SUM);
        assertThat(resource.getColumnes().get(0).getUnitatAgregacio()).isEqualTo(PeriodeUnitat.DIA);
        assertThat(resource.getColumnes().get(0).getIndicador()).isNotNull();
        assertThat(resource.getColumnes().get(0).getIndicador().getId()).isEqualTo(5L);
    }
}
