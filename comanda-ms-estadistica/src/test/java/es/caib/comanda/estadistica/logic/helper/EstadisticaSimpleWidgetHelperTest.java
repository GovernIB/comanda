package es.caib.comanda.estadistica.logic.helper;

import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.PeriodeUnitat;
import es.caib.comanda.estadistica.logic.intf.model.widget.EstadisticaSimpleWidget;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests per a EstadisticaSimpleWidgetHelper")
class EstadisticaSimpleWidgetHelperTest {

    @Mock
    private IndicadorRepository indicadorRepository;

    @Mock
    private IndicadorTaulaRepository indicadorTaulaRepository;

    @InjectMocks
    private EstadisticaSimpleWidgetHelper estadisticaSimpleWidgetHelper;

    private EstadisticaSimpleWidgetEntity entity;
    private EstadisticaSimpleWidget resource;

    @BeforeEach
    void setUp() {
        entity = new EstadisticaSimpleWidgetEntity();
        resource = new EstadisticaSimpleWidget();
    }

    // ========================================================================
    // 1. TESTOS PER A upsertIndicadorTaula
    // ========================================================================

    @Test
    @DisplayName("upsertIndicadorTaula: crea una nova entitat quan la informació de l'indicador és null")
    void upsertIndicadorTaula_quanIndicadorInfoEsNull_llavorsCreaNovaEntitat() {
        // Arrange
        entity.setIndicadorInfo(null);
        resource.setTitolIndicador("Titol Test");
        resource.setTipusIndicador(TableColumnsEnum.SUM);
        resource.setPeriodeIndicador(PeriodeUnitat.MES);
        resource.setIndicador(null);

        IndicadorTaulaEntity savedEntity = new IndicadorTaulaEntity();
        when(indicadorTaulaRepository.save(any(IndicadorTaulaEntity.class))).thenReturn(savedEntity);

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        ArgumentCaptor<IndicadorTaulaEntity> captor = ArgumentCaptor.forClass(IndicadorTaulaEntity.class);
        verify(indicadorTaulaRepository, times(1)).save(captor.capture());

        IndicadorTaulaEntity captured = captor.getValue();
        assertThat(captured.getWidget()).isSameAs(entity);
        assertThat(captured.getTitol()).isEqualTo("Titol Test");
        assertThat(captured.getAgregacio()).isEqualTo(TableColumnsEnum.SUM);
        assertThat(captured.getUnitatAgregacio()).isEqualTo(PeriodeUnitat.MES);
        assertThat(entity.getIndicadorInfo()).isSameAs(savedEntity);
    }

    @Test
    @DisplayName("upsertIndicadorTaula: actualitza l'entitat existent quan ja té informació de l'indicador")
    void upsertIndicadorTaula_quanIndicadorInfoExisteix_llavorsActualitzaEntitat() {
        // Arrange
        IndicadorTaulaEntity existing = new IndicadorTaulaEntity();
        existing.setIndicadorId(10L);
        entity.setIndicadorInfo(existing);

        resource.setTitolIndicador("Nou Titol");
        resource.setTipusIndicador(TableColumnsEnum.AVERAGE);
        resource.setPeriodeIndicador(PeriodeUnitat.SETMANA);
        resource.setIndicador(null);

        when(indicadorTaulaRepository.save(any(IndicadorTaulaEntity.class))).thenReturn(existing);

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        assertThat(existing.getTitol()).isEqualTo("Nou Titol");
        assertThat(existing.getAgregacio()).isEqualTo(TableColumnsEnum.AVERAGE);
        assertThat(existing.getUnitatAgregacio()).isEqualTo(PeriodeUnitat.SETMANA);
        verify(indicadorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertIndicadorTaula: no busca l'indicador al repositori quan el resource no en té")
    void upsertIndicadorTaula_quanResourceNoTeIndicador_llavorsNoBuscaAlRepositori() {
        // Arrange
        entity.setIndicadorInfo(new IndicadorTaulaEntity());
        resource.setIndicador(null);
        when(indicadorTaulaRepository.save(any())).thenReturn(new IndicadorTaulaEntity());

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        verify(indicadorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertIndicadorTaula: no busca l'indicador al repositori quan l'ID del resource és null")
    void upsertIndicadorTaula_quanResourceIdEsNull_llavorsNoBuscaAlRepositori() {
        // Arrange
        entity.setIndicadorInfo(new IndicadorTaulaEntity());
        ResourceReference ref = new ResourceReference();
        ref.setId(null);
        resource.setIndicador(ref);
        when(indicadorTaulaRepository.save(any())).thenReturn(new IndicadorTaulaEntity());

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        verify(indicadorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertIndicadorTaula: no busca l'indicador al repositori quan l'ID no ha canviat")
    void upsertIndicadorTaula_quanIndicadorIdNoHaCanviat_llavorsNoBuscaAlRepositori() {
        // Arrange
        IndicadorTaulaEntity existing = new IndicadorTaulaEntity();
        existing.setIndicadorId(5L);
        entity.setIndicadorInfo(existing);

        ResourceReference ref = new ResourceReference();
        ref.setId(5L);
        resource.setIndicador(ref);
        when(indicadorTaulaRepository.save(any())).thenReturn(existing);

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        verify(indicadorRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("upsertIndicadorTaula: busca i actualitza l'indicador quan l'ID ha canviat")
    void upsertIndicadorTaula_quanIndicadorIdHaCanviat_llavorsBuscaIActualitza() {
        // Arrange
        IndicadorTaulaEntity existing = new IndicadorTaulaEntity();
        existing.setIndicadorId(5L);
        entity.setIndicadorInfo(existing);

        ResourceReference ref = new ResourceReference();
        ref.setId(10L);
        resource.setIndicador(ref);

        IndicadorEntity newIndicador = new IndicadorEntity();
        when(indicadorRepository.findById(10L)).thenReturn(Optional.of(newIndicador));
        when(indicadorTaulaRepository.save(any())).thenReturn(existing);

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        verify(indicadorRepository, times(1)).findById(10L);
        assertThat(existing.getIndicador()).isSameAs(newIndicador);
    }

    @Test
    @DisplayName("upsertIndicadorTaula: no actualitza l'indicador si el repositori retorna empty")
    void upsertIndicadorTaula_quanRepositoriRetornaEmpty_llavorsNoActualitzaIndicador() {
        // Arrange
        IndicadorTaulaEntity existing = new IndicadorTaulaEntity();
        existing.setIndicadorId(5L);
        entity.setIndicadorInfo(existing);

        ResourceReference ref = new ResourceReference();
        ref.setId(10L);
        resource.setIndicador(ref);

        when(indicadorRepository.findById(10L)).thenReturn(Optional.empty());
        when(indicadorTaulaRepository.save(any())).thenReturn(existing);

        // Act
        estadisticaSimpleWidgetHelper.upsertIndicadorTaula(entity, resource);

        // Assert
        verify(indicadorRepository, times(1)).findById(10L);
        assertThat(existing.getIndicador()).isNull();
    }

    // ========================================================================
    // 2. TESTOS PER A afterCoversionGetIndicadorTaulaAtributes
    // ========================================================================

    @Test
    @DisplayName("afterCoversionGetIndicadorTaulaAtributes: no fa res quan indicadorInfo és null")
    void afterCoversionGetIndicadorTaulaAtributes_quanIndicadorInfoEsNull_llavorsNoFaRes() {
        // Arrange
        entity.setIndicadorInfo(null);

        // Act
        estadisticaSimpleWidgetHelper.afterCoversionGetIndicadorTaulaAtributes(entity, resource);

        // Assert
        assertThat(resource.getIndicador()).isNull();
        assertThat(resource.getTitolIndicador()).isNull();
    }

    @Test
    @DisplayName("afterCoversionGetIndicadorTaulaAtributes: no fa res quan l'indicador intern és null")
    void afterCoversionGetIndicadorTaulaAtributes_quanIndicadorInternEsNull_llavorsNoFaRes() {
        // Arrange
        IndicadorTaulaEntity indicadorTaula = new IndicadorTaulaEntity();
        indicadorTaula.setIndicador(null);
        entity.setIndicadorInfo(indicadorTaula);

        // Act
        estadisticaSimpleWidgetHelper.afterCoversionGetIndicadorTaulaAtributes(entity, resource);

        // Assert
        assertThat(resource.getIndicador()).isNull();
        assertThat(resource.getTitolIndicador()).isNull();
    }

    @Test
    @DisplayName("afterCoversionGetIndicadorTaulaAtributes: mapeja correctament tots els atributs quan existeixen")
    void afterCoversionGetIndicadorTaulaAtributes_quanTotExisteix_llavorsMapejaCorrectament() {
        // Arrange
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setId(99L);
        // Simulem el mètode que retorna el text (en un entorn real vindria de l'entitat)

        IndicadorTaulaEntity indicadorTaula = new IndicadorTaulaEntity();
        indicadorTaula.setIndicador(indicador);
        indicadorTaula.setTitol("Titol Mapejat");
        indicadorTaula.setAgregacio(TableColumnsEnum.LAST_SEEN);
        indicadorTaula.setUnitatAgregacio(PeriodeUnitat.ANY);

        entity.setIndicadorInfo(indicadorTaula);

        // Act
        estadisticaSimpleWidgetHelper.afterCoversionGetIndicadorTaulaAtributes(entity, resource);

        // Assert
        assertThat(resource.getIndicador()).isNotNull();
        assertThat(resource.getIndicador().getId()).isEqualTo(99L);
        assertThat(resource.getTitolIndicador()).isEqualTo("Titol Mapejat");
        assertThat(resource.getTipusIndicador()).isEqualTo(TableColumnsEnum.LAST_SEEN);
        assertThat(resource.getPeriodeIndicador()).isEqualTo(PeriodeUnitat.ANY);
    }
}
