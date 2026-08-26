package es.caib.comanda.estadistica.logic.mapper;

import es.caib.comanda.estadistica.logic.intf.model.dashboard.DashboardTitolTipus;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TableColumnsEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficDataEnum;
import es.caib.comanda.estadistica.logic.intf.model.enumerats.TipusGraficEnum;
import es.caib.comanda.estadistica.logic.intf.model.periode.*;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardItemEntity;
import es.caib.comanda.estadistica.persist.entity.dashboard.DashboardTitolEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.DimensioValorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorEntity;
import es.caib.comanda.estadistica.persist.entity.estadistiques.IndicadorTaulaEntity;
import es.caib.comanda.estadistica.persist.entity.paleta.PlantillaEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaGraficWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaSimpleWidgetEntity;
import es.caib.comanda.estadistica.persist.entity.widget.EstadisticaTaulaWidgetEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests per a DashboardClonerMapper")
class DashboardClonerMapperTest {

    private final DashboardClonerMapper mapper = Mappers.getMapper(DashboardClonerMapper.class);

    @Test
    @DisplayName("cloneTitol: clona totes les propietats visuals i ignora id, dashboard i auditoria")
    void cloneTitol_clonaPropietatsCorrectament() {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(1L);

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(5L);

        DashboardTitolEntity original = new DashboardTitolEntity();
        ReflectionTestUtils.setField(original, "id", 100L);
        original.setDashboard(dashboard);
        original.setTitol("Titol Principal");
        original.setSubtitol("Subtitol");
        original.setPosX(1);
        original.setPosY(2);
        original.setWidth(6);
        original.setHeight(3);
        original.setTipusTitol(DashboardTitolTipus.TIPUS_1);
        original.setColorTitol("#112233");
        original.setMidaFontTitol(18);
        original.setColorSubtitol("#445566");
        original.setMidaFontSubtitol(14);
        original.setColorFons("#FAFAFA");
        original.setMostrarVoraBottom(true);
        original.setColorVoraBottom("#CCCCCA");
        original.setAmpleVoraBottom(1);
        original.setMostrarVoraRight(true);
        original.setColorVoraRight("#CCCCCB");
        original.setAmpleVoraRight(2);
        original.setMostrarVoraTop(true);
        original.setColorVoraTop("#CCCCCC");
        original.setAmpleVoraTop(3);
        original.setMostrarVoraLeft(true);
        original.setColorVoraLeft("#CCCCCD");
        original.setAmpleVoraLeft(4);
        original.setDestacat(true);
        original.setPersonalitzat(true);
        original.setPlantilla(plantilla);

        DashboardTitolEntity clone = mapper.cloneTitol(original);

        assertThat(clone).isNotNull();
        assertThat(clone.getId()).isNull();
        assertThat(clone.getDashboard()).isNull();
        assertThat(clone.getTitol()).isEqualTo("Titol Principal");
        assertThat(clone.getSubtitol()).isEqualTo("Subtitol");
        assertThat(clone.getPosX()).isEqualTo(1);
        assertThat(clone.getPosY()).isEqualTo(2);
        assertThat(clone.getWidth()).isEqualTo(6);
        assertThat(clone.getHeight()).isEqualTo(3);
        assertThat(clone.getTipusTitol()).isEqualTo(DashboardTitolTipus.TIPUS_1);
        assertThat(clone.getColorTitol()).isEqualTo("#112233");
        assertThat(clone.getMidaFontTitol()).isEqualTo(18);
        assertThat(clone.getColorSubtitol()).isEqualTo("#445566");
        assertThat(clone.getMidaFontSubtitol()).isEqualTo(14);
        assertThat(clone.getColorFons()).isEqualTo("#FAFAFA");
        assertThat(clone.getMostrarVoraBottom()).isTrue();
        assertThat(clone.getColorVoraBottom()).isEqualTo("#CCCCCA");
        assertThat(clone.getAmpleVoraBottom()).isEqualTo(1);
        assertThat(clone.getMostrarVoraRight()).isTrue();
        assertThat(clone.getColorVoraRight()).isEqualTo("#CCCCCB");
        assertThat(clone.getAmpleVoraRight()).isEqualTo(2);
        assertThat(clone.getMostrarVoraTop()).isTrue();
        assertThat(clone.getColorVoraTop()).isEqualTo("#CCCCCC");
        assertThat(clone.getAmpleVoraTop()).isEqualTo(3);
        assertThat(clone.getMostrarVoraLeft()).isTrue();
        assertThat(clone.getColorVoraLeft()).isEqualTo("#CCCCCD");
        assertThat(clone.getAmpleVoraLeft()).isEqualTo(4);
        assertThat(clone.getDestacat()).isTrue();
        assertThat(clone.getPersonalitzat()).isTrue();
        assertThat(clone.getPlantilla()).isSameAs(plantilla);
    }

    @Test
    @DisplayName("cloneItem: clona les propietats de layout i ignora id, dashboard, widget, entornId")
    void cloneItem_clonaPropietatsCorrectament() {
        DashboardEntity dashboard = new DashboardEntity();
        dashboard.setId(1L);

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setId(50L);

        PlantillaEntity plantilla = new PlantillaEntity();
        plantilla.setId(10L);

        DashboardItemEntity original = new DashboardItemEntity();
        ReflectionTestUtils.setField(original, "id", 200L);
        original.setDashboard(dashboard);
        original.setWidget(widget);
        original.setEntornId(99L);
        original.setPosX(2);
        original.setPosY(4);
        original.setWidth(8);
        original.setHeight(5);
        original.setDestacat(true);
        original.setPersonalitzat(true);
        original.setPlantilla(plantilla);
        original.setAtributsVisualsJson("{\"bgColor\":\"#fff\"}");

        DashboardItemEntity clone = mapper.cloneItem(original);

        assertThat(clone).isNotNull();
        assertThat(clone.getId()).isNull();
        assertThat(clone.getDashboard()).isNull();
        assertThat(clone.getWidget()).isNull();
        assertThat(clone.getEntornId()).isNull();
        assertThat(clone.getPosX()).isEqualTo(2);
        assertThat(clone.getPosY()).isEqualTo(4);
        assertThat(clone.getWidth()).isEqualTo(8);
        assertThat(clone.getHeight()).isEqualTo(5);
        assertThat(clone.getDestacat()).isTrue();
        assertThat(clone.getPersonalitzat()).isTrue();
        assertThat(clone.getPlantilla()).isSameAs(plantilla);
        assertThat(clone.getAtributsVisualsJson()).isEqualTo("{\"bgColor\":\"#fff\"}");
    }

    @Test
    @DisplayName("cloneSimpleWidget: clona propietats base i especifiques de simple, ignora id, titol, appId, indicadorInfo")
    void cloneSimpleWidget_clonaPropietatsCorrectament() {
        DimensioValorEntity dimVal = new DimensioValorEntity();
        dimVal.setId(1L);

        IndicadorTaulaEntity indInfo = new IndicadorTaulaEntity();
        indInfo.setId(10L);

        EstadisticaSimpleWidgetEntity original = new EstadisticaSimpleWidgetEntity();
        ReflectionTestUtils.setField(original, "id", 300L);
        original.setTitol("Original Simple");
        original.setAppId(5L);
        original.setDescripcio("Descripcio Simple");
        original.setDimensionsValor(List.of(dimVal));
        original.setPeriodeMode(PeriodeMode.PRESET);
        original.setPresetPeriode(PresetPeriode.DARRERS_30_DIES);
        original.setPresetCount(1);
        original.setRelatiuPuntReferencia(PeriodeAnchor.ARA);
        original.setRelatiuCount(7);
        original.setRelatiueUnitat(PeriodeUnitat.DIA);
        original.setRelatiuAlineacio(PeriodeAlineacio.ROLLING);
        original.setAbsolutTipus(PeriodeAbsolutTipus.DATE_RANGE);
        original.setAbsolutDataInici(LocalDate.of(2024, 1, 1));
        original.setAbsolutDataFi(LocalDate.of(2024, 12, 31));
        original.setAbsolutAnyReferencia(PeriodeEspecificAny.CURRENT_YEAR);
        original.setAbsolutAnyValor(2024);
        original.setAbsolutPeriodeUnitat(PeriodeUnitat.MES);
        original.setAbsolutPeriodeInici(1);
        original.setAbsolutPeriodeFi(6);
        original.setAtributsVisualsJson("{\"color\":\"blue\"}");
        original.setUnitat("dies");
        original.setCompararPeriodeAnterior(true);
        original.setIndicadorInfo(indInfo);

        EstadisticaSimpleWidgetEntity clone = mapper.cloneSimpleWidget(original);

        assertThat(clone).isNotNull();
        assertThat(clone.getId()).isNull();
        assertThat(clone.getTitol()).isNull();
        assertThat(clone.getAppId()).isNull();
        assertThat(clone.getIndicadorInfo()).isNull();
        assertThat(clone.getDescripcio()).isEqualTo("Descripcio Simple");
        assertThat(clone.getDimensionsValor()).containsExactly(dimVal);
        assertThat(clone.getPeriodeMode()).isEqualTo(PeriodeMode.PRESET);
        assertThat(clone.getPresetPeriode()).isEqualTo(PresetPeriode.DARRERS_30_DIES);
        assertThat(clone.getPresetCount()).isEqualTo(1);
        assertThat(clone.getRelatiuPuntReferencia()).isEqualTo(PeriodeAnchor.ARA);
        assertThat(clone.getRelatiuCount()).isEqualTo(7);
        assertThat(clone.getRelatiueUnitat()).isEqualTo(PeriodeUnitat.DIA);
        assertThat(clone.getRelatiuAlineacio()).isEqualTo(PeriodeAlineacio.ROLLING);
        assertThat(clone.getAbsolutTipus()).isEqualTo(PeriodeAbsolutTipus.DATE_RANGE);
        assertThat(clone.getAbsolutDataInici()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(clone.getAbsolutDataFi()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(clone.getAbsolutAnyReferencia()).isEqualTo(PeriodeEspecificAny.CURRENT_YEAR);
        assertThat(clone.getAbsolutAnyValor()).isEqualTo(2024);
        assertThat(clone.getAbsolutPeriodeUnitat()).isEqualTo(PeriodeUnitat.MES);
        assertThat(clone.getAbsolutPeriodeInici()).isEqualTo(1);
        assertThat(clone.getAbsolutPeriodeFi()).isEqualTo(6);
        assertThat(clone.getAtributsVisualsJson()).isEqualTo("{\"color\":\"blue\"}");
        assertThat(clone.getUnitat()).isEqualTo("dies");
        assertThat(clone.isCompararPeriodeAnterior()).isTrue();
    }

    @Test
    @DisplayName("cloneGraficWidget: clona propietats base i grafic, ignora id, titol, appId, indicadorsInfo")
    void cloneGraficWidget_clonaPropietatsCorrectament() {
        DimensioEntity descDim = new DimensioEntity();
        descDim.setId(20L);

        IndicadorTaulaEntity ind = new IndicadorTaulaEntity();
        ind.setId(30L);

        EstadisticaGraficWidgetEntity original = new EstadisticaGraficWidgetEntity();
        ReflectionTestUtils.setField(original, "id", 400L);
        original.setTitol("Original Grafic");
        original.setAppId(5L);
        original.setDescripcio("Desc Grafic");
        original.setTipusGrafic(TipusGraficEnum.BAR_CHART);
        original.setTipusDades(TipusGraficDataEnum.UN_INDICADOR);
        original.setDescomposicioDimensio(descDim);
        original.setAgruparPerDimensioDescomposicio(true);
        original.setTempsAgrupacio(PeriodeUnitat.DIA);
        original.setLlegendaX("Eix X");
        original.setLlegendaY("Eix Y");
        original.setIndicadorsInfo(List.of(ind));

        EstadisticaGraficWidgetEntity clone = mapper.cloneGraficWidget(original);

        assertThat(clone).isNotNull();
        assertThat(clone.getId()).isNull();
        assertThat(clone.getTitol()).isNull();
        assertThat(clone.getAppId()).isNull();
        assertThat(clone.getIndicadorsInfo()).isNull();
        assertThat(clone.getDescripcio()).isEqualTo("Desc Grafic");
        assertThat(clone.getTipusGrafic()).isEqualTo(TipusGraficEnum.BAR_CHART);
        assertThat(clone.getTipusDades()).isEqualTo(TipusGraficDataEnum.UN_INDICADOR);
        assertThat(clone.getDescomposicioDimensio()).isSameAs(descDim);
        assertThat(clone.getAgruparPerDimensioDescomposicio()).isTrue();
        assertThat(clone.getTempsAgrupacio()).isEqualTo(PeriodeUnitat.DIA);
        assertThat(clone.getLlegendaX()).isEqualTo("Eix X");
        assertThat(clone.getLlegendaY()).isEqualTo("Eix Y");
    }

    @Test
    @DisplayName("cloneTaulaWidget: clona propietats base i taula, ignora id, titol, appId, columnes")
    void cloneTaulaWidget_clonaPropietatsCorrectament() {
        DimensioEntity dimAgrupacio = new DimensioEntity();
        dimAgrupacio.setId(40L);

        IndicadorTaulaEntity col = new IndicadorTaulaEntity();
        col.setId(50L);

        EstadisticaTaulaWidgetEntity original = new EstadisticaTaulaWidgetEntity();
        ReflectionTestUtils.setField(original, "id", 500L);
        original.setTitol("Original Taula");
        original.setAppId(5L);
        original.setDescripcio("Desc Taula");
        original.setDimensioAgrupacio(dimAgrupacio);
        original.setTitolAgrupament("Grup");
        original.setColumnes(List.of(col));

        EstadisticaTaulaWidgetEntity clone = mapper.cloneTaulaWidget(original);

        assertThat(clone).isNotNull();
        assertThat(clone.getId()).isNull();
        assertThat(clone.getTitol()).isNull();
        assertThat(clone.getAppId()).isNull();
        assertThat(clone.getColumnes()).isNull();
        assertThat(clone.getDescripcio()).isEqualTo("Desc Taula");
        assertThat(clone.getDimensioAgrupacio()).isSameAs(dimAgrupacio);
        assertThat(clone.getTitolAgrupament()).isEqualTo("Grup");
    }

    @Test
    @DisplayName("cloneIndicadorTaula: clona dades d'indicador i ignora id, widget, indicadorId")
    void cloneIndicadorTaula_clonaPropietatsCorrectament() {
        IndicadorEntity indicador = new IndicadorEntity();
        indicador.setId(60L);

        EstadisticaSimpleWidgetEntity widget = new EstadisticaSimpleWidgetEntity();
        widget.setId(70L);

        IndicadorTaulaEntity original = new IndicadorTaulaEntity();
        ReflectionTestUtils.setField(original, "id", 600L);
        original.setIndicador(indicador);
        ReflectionTestUtils.setField(original, "indicadorId", 60L);
        original.setWidget(widget);
        original.setAgregacio(TableColumnsEnum.AVERAGE);
        original.setUnitatAgregacio(PeriodeUnitat.DIA);
        original.setTitol("Mitjana");

        IndicadorTaulaEntity clone = mapper.cloneIndicadorTaula(original);

        assertThat(clone).isNotNull();
        assertThat(clone.getId()).isNull();
        assertThat(clone.getWidget()).isNull();
        assertThat(clone.getIndicadorId()).isNull();
        assertThat(clone.getIndicador()).isSameAs(indicador);
        assertThat(clone.getAgregacio()).isEqualTo(TableColumnsEnum.AVERAGE);
        assertThat(clone.getUnitatAgregacio()).isEqualTo(PeriodeUnitat.DIA);
        assertThat(clone.getTitol()).isEqualTo("Mitjana");
    }

}
