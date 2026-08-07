package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.estadistiques.IndicadorTaula;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaGraficWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.repository.IndicadorRepository;
import es.caib.comanda.estadistica.persist.repository.IndicadorTaulaRepository;
import es.caib.comanda.ms.logic.intf.model.ResourceReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaGraficWidgetHelper")
class EstadisticaGraficWidgetHelperTest {

    @Mock
    private IndicadorRepository indicadorRepository;

    @Mock
    private IndicadorTaulaRepository indicadorTaulaRepository;

    @InjectMocks
    private EstadisticaGraficWidgetHelper estadisticaGraficWidgetHelper;

    private EstadisticaGraficWidgetEntity entity;
    private EstadisticaGraficWidget resource;

    @BeforeEach
    void setUp() {
        entity = new EstadisticaGraficWidgetEntity();
        resource = new EstadisticaGraficWidget();

        lenient().when(indicadorTaulaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // ========================================================================
    // 1. TESTOS PER A upsertColumnes (VARIS_INDICADORS)
    // ========================================================================

    @Test
    @DisplayName("upsertColumnes: esborra indicadors existents i en crea de nous quan és VARIS_INDICADORS")
    void upsertColumnes_quanVarisIndicadorsIEntityTeIndicadors_llavorsEsborraICreaNous() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        IndicadorTaulaEntity existingColumn = new IndicadorTaulaEntity();
        entity.setIndicadorsInfo(new ArrayList<>(Collections.singletonList(existingColumn)));

        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Nou Titol");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        resource.setIndicadorsInfo(Collections.singletonList(colResource));

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorTaulaRepository, times(1)).deleteAll(any());
        assertThat(entity.getIndicadorsInfo()).hasSize(1);
        assertThat(entity.getIndicadorsInfo().get(0).getTitol()).isEqualTo("Nou Titol");
        verify(indicadorTaulaRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("upsertColumnes: retorna sense fer res quan és VARIS_INDICADORS però resource.getIndicadorsInfo() és null")
    void upsertColumnes_quanVarisIndicadorsIResourceIndicadorsNull_llavorsRetornaSenseFerRes() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        resource.setIndicadorsInfo(null);

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorTaulaRepository, never()).deleteAll(any());
        verify(indicadorTaulaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("upsertColumnes: assigna unitatAgregacio només quan l'agregació és AVERAGE (VARIS_INDICADORS)")
    void upsertColumnes_quanVarisIndicadorsIAgregacioEsAverage_llavorsAssignaUnitatAgregacio() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);

        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol");
        colResource.setAgregacio(TableColumnsEnum.AVERAGE);
        colResource.setUnitatAgregacio(PeriodeUnitat.DIA);
        resource.setIndicadorsInfo(Collections.singletonList(colResource));

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        assertThat(entity.getIndicadorsInfo().get(0).getUnitatAgregacio()).isEqualTo(PeriodeUnitat.DIA);
    }

    @Test
    @DisplayName("upsertColumnes: busca i assigna l'indicador quan té ID (VARIS_INDICADORS)")
    void upsertColumnes_quanVarisIndicadorsIIndicadorTeId_llavorsBuscaIAssignaIndicador() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);

        IndicadorTaula colResource = new IndicadorTaula();
        colResource.setTitol("Titol");
        colResource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(99L);
        colResource.setIndicador(ref);
        resource.setIndicadorsInfo(Collections.singletonList(colResource));

        IndicadorEntity indicadorEntity = new IndicadorEntity();
        when(indicadorRepository.findById(99L)).thenReturn(Optional.of(indicadorEntity));

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorRepository, times(1)).findById(99L);
        assertThat(entity.getIndicadorsInfo().get(0).getIndicador()).isSameAs(indicadorEntity);
    }

    // ========================================================================
    // 2. TESTOS PER A upsertColumnes (UN_INDICADOR / Default)
    // ========================================================================

    @Test
    @DisplayName("upsertColumnes: crea nou IndicadorTaulaEntity quan la llista de l'entitat és buida (UN_INDICADOR)")
    void upsertColumnes_quanUnIndicadorIEntityNoTeIndicadors_llavorsCreaNouIndicadorTaula() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);
        entity.setIndicadorsInfo(Collections.emptyList());

        resource.setTitolIndicador("Titol Unic");
        resource.setAgregacio(TableColumnsEnum.SUM);

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        assertThat(entity.getIndicadorsInfo()).hasSize(1);
        assertThat(entity.getIndicadorsInfo().get(0).getTitol()).isEqualTo("Titol Unic");
        verify(indicadorTaulaRepository, times(1)).save(any(IndicadorTaulaEntity.class));
    }

    @Test
    @DisplayName("upsertColumnes: assigna unitatAgregacio només quan l'agregació és AVERAGE (UN_INDICADOR)")
    void upsertColumnes_quanUnIndicadorIAgregacioEsAverage_llavorsAssignaUnitatAgregacio() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);
        entity.setIndicadorsInfo(new ArrayList<>());

        resource.setAgregacio(TableColumnsEnum.AVERAGE);
        resource.setUnitatAgregacio(PeriodeUnitat.DIA);

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        assertThat(entity.getIndicadorsInfo().get(0).getUnitatAgregacio()).isEqualTo(PeriodeUnitat.DIA);
    }

    @Test
    @DisplayName("upsertColumnes: actualitza l'indicador quan l'ID de resource és diferent de l'actual (UN_INDICADOR)")
    void upsertColumnes_quanUnIndicadorIIndicadorCanvia_llavorsActualitzaIndicador() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);

        IndicadorTaulaEntity existing = new IndicadorTaulaEntity();
        existing.setIndicadorId(10L); // ID actual diferent
        entity.setIndicadorsInfo(Collections.singletonList(existing));

        resource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(20L); // Nou ID
        resource.setIndicador(ref);

        IndicadorEntity newIndicador = new IndicadorEntity();
        when(indicadorRepository.findById(20L)).thenReturn(Optional.of(newIndicador));

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorRepository, times(1)).findById(20L);
        assertThat(existing.getIndicador()).isSameAs(newIndicador);
    }

    @Test
    @DisplayName("upsertColumnes: no busca l'indicador si l'ID de resource és igual a l'actual (UN_INDICADOR)")
    void upsertColumnes_quanUnIndicadorIIndicadorNoCanvia_llavorsNoBuscaIndicador() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);

        IndicadorTaulaEntity existing = new IndicadorTaulaEntity();
        existing.setIndicadorId(10L);
        entity.setIndicadorsInfo(Collections.singletonList(existing));

        resource.setAgregacio(TableColumnsEnum.SUM);
        ResourceReference ref = new ResourceReference();
        ref.setId(10L); // Mateix ID
        resource.setIndicador(ref);

        // Act
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);

        // Assert
        verify(indicadorRepository, never()).findById(anyLong());
    }

    // ========================================================================
    // 3. TESTOS PER A upsertColumnes (DOS_INDICADORS - Cobertura de línia TODO)
    // ========================================================================

    @Test
    @DisplayName("upsertColumnes: executa sense errors quan el tipus és DOS_INDICADORS (cobertura de línia TODO)")
    void upsertColumnes_quanDosIndicadors_llavorsExecutaSenseErrors() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
        entity.setIndicadorsInfo(new ArrayList<>());
        resource.setAgregacio(TableColumnsEnum.SUM);

        // Act & Assert
        // No ha de llançar cap excepció, només cobrir la línia del TODO
        estadisticaGraficWidgetHelper.upsertColumnes(entity, resource);
        verify(indicadorTaulaRepository, times(1)).save(any(IndicadorTaulaEntity.class));
    }

    // ========================================================================
    // 4. TESTOS PER A afterCoversionGetColumnes (VARIS_INDICADORS)
    // ========================================================================

    @Test
    @DisplayName("afterCoversionGetColumnes: retorna sense fer res quan és VARIS_INDICADORS però la llista és buida")
    void afterCoversionGetColumnes_quanVarisIndicadorsIEntityNoTeIndicadors_llavorsRetornaSenseFerRes() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);
        entity.setIndicadorsInfo(Collections.emptyList());

        // Act
        estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getIndicadorsInfo()).isNull();
    }

    @Test
    @DisplayName("afterCoversionGetColumnes: mapeja correctament la llista d'indicadors quan és VARIS_INDICADORS")
    void afterCoversionGetColumnes_quanVarisIndicadorsIEntityTeIndicadors_llavorsMapejaCorrectament() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.VARIS_INDICADORS);

        IndicadorEntity indEntity = new IndicadorEntity();
        indEntity.setId(5L);
        // Simulem el mètode que retorna el text (en un entorn real vindria de l'entitat)

        IndicadorTaulaEntity colEntity = new IndicadorTaulaEntity();
        colEntity.setTitol("Titol Mapejat");
        colEntity.setAgregacio(TableColumnsEnum.SUM);
        colEntity.setUnitatAgregacio(PeriodeUnitat.DIA);
        colEntity.setIndicador(indEntity);

        entity.setIndicadorsInfo(Collections.singletonList(colEntity));

        // Act
        estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getIndicadorsInfo()).hasSize(1);
        assertThat(resource.getIndicadorsInfo().get(0).getTitol()).isEqualTo("Titol Mapejat");
        assertThat(resource.getIndicadorsInfo().get(0).getIndicador()).isNotNull();
        assertThat(resource.getIndicadorsInfo().get(0).getIndicador().getId()).isEqualTo(5L);
    }

    // ========================================================================
    // 5. TESTOS PER A afterCoversionGetColumnes (UN_INDICADOR / Default)
    // ========================================================================

    @Test
    @DisplayName("afterCoversionGetColumnes: no modifica el resource quan és UN_INDICADOR però la llista és buida o null")
    void afterCoversionGetColumnes_quanUnIndicadorIEntityNoTeIndicadors_llavorsNoModificaResource() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);
        entity.setIndicadorsInfo(null);
        resource.setTitolIndicador("Titol Original");

        // Act
        estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getTitolIndicador()).isEqualTo("Titol Original"); // No s'ha sobreescript
    }

    @Test
    @DisplayName("afterCoversionGetColumnes: mapeja correctament l'indicador únic quan existeix")
    void afterCoversionGetColumnes_quanUnIndicadorIEntityTeIndicador_llavorsMapejaCorrectament() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);

        IndicadorEntity indEntity = new IndicadorEntity();
        indEntity.setId(10L);

        IndicadorTaulaEntity colEntity = new IndicadorTaulaEntity();
        colEntity.setTitol("Titol Unic Mapejat");
        colEntity.setAgregacio(TableColumnsEnum.AVERAGE);
        colEntity.setUnitatAgregacio(PeriodeUnitat.DIA);
        colEntity.setIndicador(indEntity);

        entity.setIndicadorsInfo(Collections.singletonList(colEntity));

        // Act
        estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);

        // Assert
        assertThat(resource.getTitolIndicador()).isEqualTo("Titol Unic Mapejat");
        assertThat(resource.getAgregacio()).isEqualTo(TableColumnsEnum.AVERAGE);
        assertThat(resource.getUnitatAgregacio()).isEqualTo(PeriodeUnitat.DIA);
        assertThat(resource.getIndicador()).isNotNull();
        assertThat(resource.getIndicador().getId()).isEqualTo(10L);
    }

    // ========================================================================
    // 6. TESTOS PER A afterCoversionGetColumnes (DOS_INDICADORS - Cobertura de línia TODO)
    // ========================================================================

    @Test
    @DisplayName("afterCoversionGetColumnes: executa sense errors quan el tipus és DOS_INDICADORS (cobertura de línia TODO)")
    void afterCoversionGetColumnes_quanDosIndicadors_llavorsExecutaSenseErrors() {
        // Arrange
        entity.setTipusDades(TipusGraficDataEnum.DOS_INDICADORS);
        entity.setIndicadorsInfo(Collections.emptyList());

        // Act & Assert
        // No ha de llançar cap excepció, només cobrir la línia del TODO
        estadisticaGraficWidgetHelper.afterCoversionGetColumnes(entity, resource);
    }
}
